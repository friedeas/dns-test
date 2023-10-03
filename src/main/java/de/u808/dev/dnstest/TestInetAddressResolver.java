package de.u808.dev.dnstest;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Cache;
import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.lookup.LookupResult;
import org.xbill.DNS.lookup.LookupSession;
import org.xbill.DNS.lookup.LookupSession.LookupSessionBuilder;

import com.kstruct.gethostname4j.Hostname;

public class TestInetAddressResolver implements InetAddressResolver {

	private static final String LOCALHOST = "localhost";

	private static final String domainProperty = "sun.net.spi.nameservice.domain";

	final LookupSession lookupSession;

	private Name localhostName = null;
	private InetAddress[] localhostNamedAddresses = null;
	private InetAddress[] localhostAddresses = null;
	private boolean addressesLoaded = false;

	private static Logger LOG = LoggerFactory.getLogger(TestInetAddressResolver.class);

	public TestInetAddressResolver() {
		lookupSession = createLookupSessionBuilder().build();
		setLocalHost();
	}

	protected Resolver getConfiguredResolver() {
		return new SimpleResolver(new InetSocketAddress("192.168.178.1", 53));
	}

	protected Resolver getInternalResolver() {
		return null;
	}

	protected List<Resolver> getResolverList() {
		final ArrayList<Resolver> resolver = new ArrayList<>(2);
		final Resolver configuredResol = getConfiguredResolver();
		final Resolver internalResolver = getInternalResolver();
		if (configuredResol != null) {
			resolver.add(configuredResol);
		}
		if (internalResolver != null) {
			resolver.add(internalResolver);
		}
		return resolver;
	}

	protected void setLocalHost() {
		try {
			localhostName = getName(Hostname.getHostname());
			try (final DatagramSocket socket = new DatagramSocket()) {
				socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
				localhostNamedAddresses = new InetAddress[1];
				localhostNamedAddresses[0] = socket.getLocalAddress();
			}			
			localhostAddresses = new InetAddress[1];
			//TODO could be improved
			localhostAddresses[0] = InetAddress.getByAddress(new byte[] {127, 0, 0, 1});			
			addressesLoaded = true;
		} catch (Exception e) {
			LOG.error("Could not obtain localhost", e);
		}
	}

	protected LookupSessionBuilder createLookupSessionBuilder() {
		final String domain = System.getProperty(domainProperty);
		final LookupSessionBuilder builder = LookupSession.builder().resolver(new ExtendedResolver(getResolverList()))
				.ndots(ResolverConfig.getCurrentConfig().ndots()).cache(new Cache(DClass.IN)).defaultHostsFileParser();
		if (domain != null) {
			try {
				builder.searchPath(Name.fromString(domain, Name.root));
			} catch (TextParseException e) {
				LOG.error("DNSJavaNameService: invalid {}", domainProperty);
			}
		}
		return builder;
	}

	@Override
	public Stream<InetAddress> lookupByName(final String host, final LookupPolicy lookupPolicy)
			throws UnknownHostException {
		final Name name = getName(host);
		if (addressesLoaded) {
			if (name.equals(localhostName)) {
				return Arrays.stream(localhostNamedAddresses);
			} else if (LOCALHOST.equalsIgnoreCase(host)) {
				return Arrays.stream(localhostAddresses);
			}
		}
		try {						
			List<Record> resultRecors = new LinkedList<>();
			for (Integer lType : getSortedLookupType(lookupPolicy)) {
				LookupResult result = performLookup(name, lType);
				if(result != null && result.getRecords() != null) {
					resultRecors.addAll(result.getRecords());
				}
			}			
			
			LOG.debug("Result: {}", resultRecors);
			if (resultRecors == null || resultRecors.isEmpty()) {
				throw new UnknownHostException(host);
			}
			return resultRecors.stream().filter(r -> r instanceof ARecord || r instanceof AAAARecord).map(r -> {
				return this.getAddress(r);
			});
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Failed to lookup host: {}", host, e);
			throw new UnknownHostException(host);
		}
	}

	protected Name getName(final String host) throws UnknownHostException {
		try {
			return new Name(host);
		} catch (TextParseException e) {
			throw new UnknownHostException(host);
		}
	}
	
	protected List<Integer> getSortedLookupType(final LookupPolicy lookupPolicy){
		List<Integer> lookupTypes = new ArrayList<>();
		//java.net.spi.InetAddressResolver.LookupPolicy.characteristics() is a bitmask based configuration
		final String bitmaskString = String.format("%4s", Integer.toBinaryString(lookupPolicy.characteristics())).replaceAll(" ", "0");
		boolean ip4 = bitmaskString.charAt(3) == '1';
		boolean ip6 = bitmaskString.charAt(2) == '1';
		boolean ip4first = bitmaskString.charAt(1) == '1';
		boolean ip6first = bitmaskString.charAt(0) == '1';
		if(ip4first) {
			lookupTypes.add(Type.A);
			if(ip6)	{
				lookupTypes.add(Type.AAAA);
			}
		} else if(ip6first) {
			lookupTypes.add(Type.AAAA);
			if(ip4) {
				lookupTypes.add(Type.A);
			}
		}
		return lookupTypes;
	}

	protected LookupResult performLookup(Name name, int type) throws InterruptedException, ExecutionException {
		return lookupSession.lookupAsync(name, type, DClass.IN).whenComplete((answer, ex) -> {
			if (ex == null) {
				LOG.debug("Answer : {}", answer);
			} else {
				LOG.error("Failed to lookup host: {}", name.toString(), ex);
			}
		}).toCompletableFuture().get();
	}

	@Override
	public String lookupByAddress(final byte[] addr) throws UnknownHostException {
		Name name = ReverseMap.fromAddress(addr);
//		Name name = ReverseMap.fromAddress(InetAddress.getByAddress(addr));	    
		try {
			LookupResult result = performLookup(name, Type.PTR);
			if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
				return ((PTRRecord)result.getRecords().stream().findFirst().get()).getTarget().toString();
			}			
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Exception during PTR lookup", e);
		}
		throw new UnknownHostException("Unknown address: " + name);
	}

	protected InetAddress getAddress(Record r) {
		assert r instanceof ARecord || r instanceof AAAARecord;
		if (r instanceof ARecord) {
			return ((ARecord) r).getAddress();
		} else {
			return ((AAAARecord) r).getAddress();
		}
	}
}

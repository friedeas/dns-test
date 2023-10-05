package de.u808.dev.dnstest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class TestInetAddressResolverTest {
	
	private static Logger LOG = LoggerFactory.getLogger(TestInetAddressResolverTest.class);

	@Test
	public void testDnsResolver() throws UnknownHostException {
		final String nameToTest = "www.google.com";
		InetAddress[] addresses = InetAddress.getAllByName(nameToTest);
		LOG.info("Addresses for {} = {}", nameToTest, Arrays.toString(addresses));
		assertNotNull(addresses);
	}
	
//	@Test
//	public void testLookupByAddress() throws UnknownHostException {
//		final String ipAddressToTest = "142.250.185.164";
//		byte[] byAddress = new byte[4];
//		String[] ipArr = ipAddressToTest.split("\\.");
//		byAddress[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
//		byAddress[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
//		byAddress[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
//		byAddress[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);		
//		InetAddress address = InetAddress.getByAddress(byAddress);
//		String canonicalHostName = address.getCanonicalHostName();
//		LOG.info("CanonicalHostName for IP {} = {}", ipAddressToTest, canonicalHostName);
//		assertNotNull(canonicalHostName);
//	}
	
	@Test
	public void simpleLookupByAddress() throws UnknownHostException {
//		final String ipAddressToTest = "142.250.185.164";
//		Name name = ReverseMap.fromAddress(ipAddressToTest);
//		Record[] records = new Lookup(name, Type.PTR).run();
//		assertNotNull(records); 
		//142.250.186.132
		
		final String nameToTest = "www.google.com";
		InetAddress[] resolvedAddresses = InetAddress.getAllByName(nameToTest);
		
		for (InetAddress inetAddress : resolvedAddresses) {
			LOG.info("IP for {} = {}", nameToTest, inetAddress.toString());
			
			String address = InetAddress.getByAddress(inetAddress.getAddress()).getHostName();
			LOG.info("rDNS for IP {} = {}", inetAddress, address);
		}
		
	}
}

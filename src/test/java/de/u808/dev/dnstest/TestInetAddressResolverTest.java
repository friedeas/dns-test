package de.u808.dev.dnstest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestInetAddressResolverTest {
	
	DoHDnsResolver resolver = new DoHDnsResolver();
	
	private static Logger LOG = LoggerFactory.getLogger(TestInetAddressResolverTest.class);

	@Test
	public void testDnsResolver() throws UnknownHostException {
		final String nameToTest = "www.google.com";
		InetAddress[] addresses = InetAddress.getAllByName(nameToTest);
		LOG.info("Addresses for {} = {}", nameToTest, Arrays.toString(addresses));
		assertNotNull(addresses);
	}
	
	@Test
	public void simpleLookupByAddress() throws UnknownHostException {
		final String nameToTest = "www.google.com";
		InetAddress[] resolvedAddresses = InetAddress.getAllByName(nameToTest);
		
		for (InetAddress inetAddress : resolvedAddresses) {
			LOG.info("IP for {} = {}", nameToTest, inetAddress.toString());
			
			String address = InetAddress.getByAddress(inetAddress.getAddress()).getHostName();
			LOG.info("rDNS for IP {} = {}", inetAddress, address);
		}		
	}
	
	@Test
	public void testIp6UrlGeneration() throws UnknownHostException {
		byte[] ip6Addr = {42, 0, 20, 80, 64, 1, 8, 8, 0, 0, 0, 0, 0, 0, 32, 4};		
		String url = this.resolver.buildDoHRequestUrl(ip6Addr);
		assertNotNull(url);
		assertTrue("https://dns.google/resolve?name=4.0.0.2.0.0.0.0.0.0.0.0.0.0.0.0.8.0.8.0.1.0.0.4.0.5.4.1.0.0.a.2.ip6.arpa&type=PTR".equalsIgnoreCase(url));		
	}
}

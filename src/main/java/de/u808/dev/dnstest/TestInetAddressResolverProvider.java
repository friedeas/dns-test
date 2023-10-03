package de.u808.dev.dnstest;

import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;

public class TestInetAddressResolverProvider extends InetAddressResolverProvider{

	@Override
	public InetAddressResolver get(Configuration configuration) {
		 return new TestInetAddressResolver();
	}

	@Override
	public String name() {
		return "TestInetAddressResolver";
	}

}

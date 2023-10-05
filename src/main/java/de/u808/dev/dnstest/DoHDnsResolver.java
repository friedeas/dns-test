package de.u808.dev.dnstest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.dnsoverhttps.DnsOverHttps;

public class DoHDnsResolver implements InetAddressResolver {

	private static final String BASE_URL = " https://dns.google";

	DnsOverHttps dns;

	OkHttpClient client;

	Gson gson = new Gson();

	private static Logger LOG = LoggerFactory.getLogger(DoHDnsResolver.class);

	public DoHDnsResolver() {

//		Security.insertProviderAt(new org.conscrypt.OpenSSLProvider(), 1);		
		client = new OkHttpClient.Builder().proxy(new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 3128)))
				.build();

		try {
			dns = new DnsOverHttps.Builder().client(client).url(HttpUrl.get("https://dns.google/dns-query")).post(false)
					.bootstrapDnsHosts(InetAddress.getByName("8.8.4.4"), InetAddress.getByName("8.8.8.8")).build();
		} catch (UnknownHostException e) {
			LOG.error("Failed to init DnsOverHttps", e);
		}
	}

	@Override
	public Stream<InetAddress> lookupByName(String host, LookupPolicy lookupPolicy) throws UnknownHostException {
		return dns.lookup(host).stream();
	}

	@Override
	public String lookupByAddress(byte[] addr) throws UnknownHostException {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/resolve").newBuilder();
		if (addr.length == 4) {
			urlBuilder.addQueryParameter("name", "%s.%s.%s.%s.in-addr.arpa.".formatted(addr[3] & 0xFF, addr[2] & 0xFF,
					addr[1] & 0xFF, addr[0] & 0xFF));
		} else if (addr.length == 16) {
			urlBuilder.addQueryParameter("name",
					"%s.%s.%s.%s.%s.%s.%s.%s.%s.%s.%s.%s.%s.%s.%s.%s.ip6.arpa.".formatted(addr[15] & 0xFF, addr[14] & 0xFF, addr[13] & 0xFF,
							addr[12] & 0xFF, addr[11] & 0xFF, addr[10] & 0xFF, addr[9] & 0xFF, addr[8] & 0xFF,
							addr[7] & 0xFF, addr[6] & 0xFF, addr[5] & 0xFF, addr[4] & 0xFF, addr[3] & 0xFF,
							addr[2] & 0xFF, addr[1] & 0xFF, addr[0] & 0xFF));
		}
		urlBuilder.addQueryParameter("type", "ptr");

		String url = urlBuilder.build().toString();

		Request request = new Request.Builder().url(url).build();
		Call call = client.newCall(request);
		try {
			ResponseBody responseBody = call.execute().body();

			DoHResponse response = gson.fromJson(responseBody.string(), DoHResponse.class);
			if (response != null && response.successful()) {
				return response.getAnswers()[0].getData();
			}
		} catch (IOException e) {
			LOG.error("PTR request failed", e);
		}
		return null;
	}

}

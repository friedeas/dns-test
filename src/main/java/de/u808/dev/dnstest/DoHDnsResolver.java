package de.u808.dev.dnstest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.dnsoverhttps.DnsOverHttps;

public class DoHDnsResolver implements InetAddressResolver {
	
	private static final String BASE_URL = " https://dns.google";

	DnsOverHttps dns;
	
	OkHttpClient client;
	
	private static Logger LOG = LoggerFactory.getLogger(DoHDnsResolver.class);
	
	public DoHDnsResolver() {
		
//		Security.insertProviderAt(new org.conscrypt.OpenSSLProvider(), 1);

	    client = new OkHttpClient.Builder().build();
		
		try {
			dns = new DnsOverHttps.Builder().client(client)
				    .url(HttpUrl.get("https://dns.google/dns-query")).post(false)
				    .bootstrapDnsHosts(InetAddress.getByName("8.8.4.4"), InetAddress.getByName("8.8.8.8"))
				    .build();
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
		// TODO Auto-generated method stub
		//142.250.186.132
		
		//query = DnsRecordCodec.encodeQuery(hostname, type)
		
//		List<InetAddress> res =  dns.lookup("132.186.250.142.in-addr.arpa");
//		return res.toString();
		
		//'https://dns.google/resolve?name=142.250.181.228&type=ptr&do=1'
		
		HttpUrl.Builder urlBuilder 
	      = HttpUrl.parse(BASE_URL + "/resolve").newBuilder();
	    urlBuilder.addQueryParameter("name", "228.181.250.142.in-addr.arpa").addQueryParameter("type", "ptr");

	    String url = urlBuilder.build().toString();

	    Request request = new Request.Builder()
	      .url(url)
	      .build();
	    Call call = client.newCall(request);
	    try {
			Response response = call.execute();
			return response.body().string();
		} catch (IOException e) {
			LOG.error("PTR request failed", e);
		}
		return null;
	}

}

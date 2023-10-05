package de.u808.dev.dnstest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.xbill.DNS.EDNSOption;
import org.xbill.DNS.Message;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.utils.base64;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CustomDoHResolver implements Resolver {

	private static final String BASE_URL = "https://dns.google";
	private Executor defaultExecutor = ForkJoinPool.commonPool();
	private Duration timeout;

	OkHttpClient client = new OkHttpClient.Builder().readTimeout(240, TimeUnit.SECONDS).build();

	@Override
	public void setPort(int port) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTCP(boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIgnoreTruncation(boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEDNS(int version, int payloadSize, int flags, List<EDNSOption> options) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTSIGKey(TSIG key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
		this.client = new OkHttpClient.Builder().readTimeout(timeout).build();
	}

	@Override
	public Duration getTimeout() {
		return timeout;
	}

	@Override
	public CompletionStage<Message> sendAsync(Message query) {
		return sendAsync(query, defaultExecutor);
	}

	@Override
	public CompletionStage<Message> sendAsync(Message query, Executor executor) {

		return sendAsync11(query, executor);
	}

	private CompletionStage<Message> sendAsync11(Message query, Executor executor) {
		byte[] queryBytes = prepareQuery(query).toWire();

		HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/dns-query").newBuilder();
		urlBuilder.addQueryParameter("dns", base64.toString(queryBytes, true));
		
		String url = urlBuilder.build().toString();

	    Request request = new Request.Builder()
	      .url(url)
	      .build();

		client.newCall(request).enqueue(new Callback() {
			
			@Override
			public void onResponse(Call arg0, Response arg1) throws IOException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(Call arg0, IOException arg1) {
				// TODO Auto-generated method stub
				
			}
		});
		return null;
	}

	private Message prepareQuery(Message query) {
		Message preparedQuery = query.clone();
		preparedQuery.getHeader().setID(0);
		return preparedQuery;
	}
}

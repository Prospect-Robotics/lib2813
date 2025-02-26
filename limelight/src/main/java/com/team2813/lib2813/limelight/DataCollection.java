package com.team2813.lib2813.limelight;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.json.JSONObject;

class DataCollection implements Runnable {
	private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(20))
			.executor(Executors.newFixedThreadPool(2)).build();
	private final HttpRequest dumpRequest;

	public DataCollection(String hostname) {
		lastResult = Optional.empty();
		try {
			URI dumpRequestUri = new URI("http", null, hostname, 5807, "/results", null, null);
			dumpRequest = HttpRequest.newBuilder(dumpRequestUri).GET().build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("invalid hostname", e);
		}
	}

	record Result(JSONObject json, double jsonParseTimeMillis) {}

	private volatile Optional<Result> lastResult;

	private static class JSONHandler implements BodyHandler<Result> {
		@Override
		public BodySubscriber<Result> apply(ResponseInfo responseInfo) {
			long startTimeNanos = System.nanoTime();
			return BodySubscribers.mapping(BodyHandlers.ofString(Charset.defaultCharset()).apply(responseInfo), body -> {
				var json = new JSONObject(body);
				double parseTimeMillis = (System.nanoTime() - startTimeNanos) / 1_000_000d;
				return new Result(json, parseTimeMillis);
			});
		}
	}

	private static final JSONHandler handler = new JSONHandler();

	private void updateJSON(HttpResponse<Result> obj) {
		Result result = obj.body();
		lastResult = Optional.of(result);
	}

	@Override
	public void run() {
		try {
			updateJSON(client.send(dumpRequest, handler));
		} catch (InterruptedException e) {
			lastResult = Optional.empty();
			// background thread canceled
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			lastResult = Optional.empty();
		}
	}

	public Optional<Result> getMostRecent() {
		return lastResult;
	}
}
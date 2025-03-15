package com.team2813.lib2813.limelight;

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

import static com.ctre.phoenix6.Utils.getCurrentTimeSeconds;

class DataCollection implements Runnable {
	private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(20))
			.executor(Executors.newFixedThreadPool(2)).build();
	private final HttpRequest dumpRequest;

	public DataCollection(LimelightClient limelightClient) {
		lastResult = Optional.empty();
		try {
			dumpRequest = limelightClient.newRequestBuilder("/results").GET().build();
		} catch (LimelightClient.HttpRequestException e) {
			throw new RuntimeException(e);
		}
	}

	record Result(JSONObject json, double responseTimestamp) {}

	private volatile Optional<Result> lastResult;

	private static class JSONHandler implements BodyHandler<Result> {
		@Override
		public BodySubscriber<Result> apply(ResponseInfo responseInfo) {
			// Get the timestamp before we parse the JSON.
			double responseTimestamp = getCurrentTimeSeconds();

			return BodySubscribers.mapping(BodyHandlers.ofString(Charset.defaultCharset()).apply(responseInfo), body -> {
				return new Result(new JSONObject(body), responseTimestamp);
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
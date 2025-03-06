package com.team2813.lib2813.limelight;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class FakeLimelight extends ExternalResource {
	private final static final Logger logger = Logger.getLogger("FakeLimelight");
	HttpServer server;
	@Override
	protected void before() throws Throwable {
		server = HttpServer.create(new InetSocketAddress(5807), 0);
		server.createContext("/results", resultsResponse);
		server.createContext("/upload-fieldmap", fieldMapResponse);
		server.setExecutor(Executors.newSingleThreadExecutor());
		server.start();
	}

	@Override
	protected void after() {
		server.stop(2);
		server = null;
	}

	private static class FakeGet implements HttpHandler {
		private String body;
		public void setBody(String body) {
			this.body = body;
		}

		public String getBody() {
			return body;
		}

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			logger.info("Request for results received");
			if (!"GET".equals(exchange.getRequestMethod())) {
				exchange.sendResponseHeaders(405, -1);
				logger.warning(String.format("Unexpected request method: %s", exchange.getRequestMethod()));
				return;
			}
			exchange.sendResponseHeaders(200, body.length());
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(body.getBytes());
			}
		}
	}
	
	private static class FakeFieldMap implements HttpHandler {
		private String post;
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			logger.info("Request for field map received");
			if (!"POST".equals(exchange.getRequestMethod())) {
				exchange.sendResponseHeaders(405, -1);
				logger.warning(String.format("Unexpected request method: %s", exchange.getRequestMethod()));
				return;
			}
			try (InputStream is = exchange.getRequestBody()) {
				byte[] data = is.readAllBytes();
				post = new String(data, StandardCharsets.UTF_8);
			}
			exchange.sendResponseHeaders(200, -1);
		}
	}

	private final FakeGet resultsResponse = new FakeGet();
	private final FakeFieldMap fieldMapResponse = new FakeFieldMap();

	public void reset() {
		resultsResponse.setBody("");
	}

	public void setResultsResponse(JSONObject response) {
		resultsResponse.setBody(response.toString());
	}
	
	public String getFieldMap() {
		return fieldMapResponse.post;
	}

	public JSONObject getResultsResponse() {
		return new JSONObject(resultsResponse.getBody());
	}
}

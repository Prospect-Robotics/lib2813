package com.team2813.lib2813.limelight;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.junit.rules.ExternalResource;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class FakeLimelight extends ExternalResource {
	private static Logger logger = Logger.getLogger("FakeLimelight");
	HttpServer server;
	@Override
	protected void before() throws Throwable {
		server = HttpServer.create(new InetSocketAddress(5807), 0);
		server.createContext("/results", resultsResponse);
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
			logger.info("Request Recieved:");
			logger.info(exchange.getRequestHeaders().toString());
			exchange.sendResponseHeaders(200, body.length());
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(body.getBytes());
			}
		}
	}

	private FakeGet resultsResponse = new FakeGet();
	public void setResultsResponse(JSONObject response) {
		resultsResponse.setBody(response.toString());
	}
	public JSONObject getResultsResponse() {
		return new JSONObject(resultsResponse.getBody());
	}
}

package net.xby1993.crawler;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

public interface RequestAction {
	void before(CloseableHttpClient client,HttpUriRequest req);
	void after(CloseableHttpClient client,CloseableHttpResponse resp);
}

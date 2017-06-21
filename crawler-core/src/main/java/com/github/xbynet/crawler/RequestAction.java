package com.github.xbynet.crawler;

import java.io.Serializable;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

public interface RequestAction extends Serializable {
	void before(CloseableHttpClient client,HttpUriRequest req);
	void after(CloseableHttpClient client,CloseableHttpResponse resp);
}

package com.github.xbynet.crawler;

public interface SpiderListener {
	void success(Spider spider,Request request);
	void fail(Spider spider,Request request,Exception e);
}

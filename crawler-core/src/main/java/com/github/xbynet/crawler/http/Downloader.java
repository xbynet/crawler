package com.github.xbynet.crawler.http;

import java.io.Closeable;

import com.github.xbynet.crawler.Request;
import com.github.xbynet.crawler.Spider;

public interface Downloader extends Closeable{
	void init();
	void download(Request request);
	void setSpider(Spider spider);
}

package net.xby1993.crawler.http;

import java.io.Closeable;

import net.xby1993.crawler.Request;
import net.xby1993.crawler.Spider;

public interface Downloader extends Closeable{
	void init();
	void download(Request request);
	void setSpider(Spider spider);
}

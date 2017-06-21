package com.github.xbynet.crawler;

import java.util.HashMap;
import java.util.Map;

public class Site {
	private String encoding="UTF-8";
	private String ua="Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";
	private int sleep=20;
	private int retry=3;
	private int retrySleep=500;
	private int timeout=30000;
	private Map<String,String> headers=new HashMap<String,String>();
	
	public Site(){
		getHeaders().put("User-Agent", ua);
	}
	public String getEncoding() {
		return encoding;
	}

	public Site setEncoding(String encoding) {
		this.encoding = encoding;
		return this;
	}

	public String getUa() {
		return ua;
	}

	public Site setUa(String ua) {
		getHeaders().put("User-Agent", ua);
		return this;
	}

	public int getSleep() {
		return sleep;
	}

	public Site setSleep(int sleep) {
		this.sleep = sleep;
		return this;
	}

	public int getRetry() {
		return retry;
	}

	public Site setRetry(int retry) {
		this.retry = retry;
		return this;
	}

	public int getRetrySleep() {
		return retrySleep;
	}

	public Site setRetrySleep(int retrySleep) {
		this.retrySleep = retrySleep;
		return this;
	}

	public int getTimeout() {
		return timeout;
	}

	public Site setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}
	
	public Site setHeader(String name,String value){
		getHeaders().put(name, value);
		return this;
	}
	public Map<String,String> getHeaders() {
		return headers;
	}
}

package net.xby1993.crawler;

import java.beans.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.protocol.HttpClientContext;

import com.alibaba.fastjson.annotation.JSONField;

public class Request implements Serializable{
	private String url;
	private String encoding;
	private Const.HttpMethod method=Const.HttpMethod.GET;
	
	private int retrySleepTime=-1;//millis
	private int retryCount=-1;//millis
	
	private Map<String,String> headers=new HashMap<String,String>();
	private Map<String,String> params=new HashMap<String,String>();
	/**可以在添加请求时附加额外信息*/
	private Map<String, Object> extras=new HashMap<String,Object>();
	
	private transient HttpClientContext ctx;
	
	/**
	 * support for json,xml or more,在post时，设置此选项会使params参数失效。
	  */
	private transient HttpEntity entity;
	
	private RequestAction action;
	
	/**支持存在分块请求的情形，(比如一篇文章需要翻多页抓取，歌手信息不分布在多个页面中)*/
	private List<Request> partRequest=new ArrayList<Request>();
	/**是否分块*/
	private boolean supportPart=false;
	
	public Request(){
		
	}
	public Request(String url){
		this.url=url;
	}
	public Const.HttpMethod getMethod() {
		return method;
	}
	public Request setMethod(Const.HttpMethod method) {
		this.method = method;
		return this;
	}
	public Map<String,String> getHeaders() {
		return headers;
	}
	public Request setHeader(String key,String value) {
		headers.put(key, value);
		return this;
	}
	public Map<String,String> getParams() {
		return params;
	}
	public Request setParams(String key,String value) {
		params.put(key, value);
		return this;
	}
	public Map<String, Object> getExtras() {
		return extras;
	}
	public Request setExtras(Map<String, Object> extras) {
		this.extras=extras;
		return this;
	}
	public Request putExtra(String key,String value) {
		extras.put(key, value);
		return this;
	}
	
	public HttpClientContext getCtx() {
		return ctx;
	}
	public Request setCtx(HttpClientContext ctx) {
		this.ctx = ctx;
		return this;
	}
	
	public HttpEntity getEntity() {
		return entity;
	}
	public Request setEntity(HttpEntity entity) {
		this.entity = entity;
		return this;
	}
	public String getEncoding() {
		return encoding;
	}
	public Request setEncoding(String encoding) {
		this.encoding = encoding;
		return this;
	}

	public int getRetryCount() {
		return retryCount;
	}
	public Request setRetryCount(int retryCount) {
		this.retryCount = retryCount;
		return this;
	}
	public int getRetrySleepTime() {
		return retrySleepTime;
	}
	public Request setRetrySleepTime(int retrySleepTime) {
		this.retrySleepTime = retrySleepTime;
		return this;
	}
	public RequestAction getAction() {
		return action;
	}
	public Request setAction(RequestAction action) {
		this.action = action;
		return this;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public List<Request> getPartRequest() {
		return partRequest;
	}
	public Request setPartRequest(List<Request> list) {
		this.partRequest=list;
		return this;
	}
	public void addPartRequest(Request req) {
		this.partRequest.add(req);
		supportPart=true;
	}
	
	@Override
	public String toString() {
		return "Request [url=" + url + ", encoding=" + encoding + ", method="
				+ method + ", retrySleepTime=" + retrySleepTime
				+ ", retryCount=" + retryCount + ", headers=" + headers
				+ ", params=" + params + ", extras=" + extras + ", ctx=" + ctx
				+ ", entity=" + entity + ", action=" + action + "]";
	}
	public boolean isSupportPart() {
		return supportPart;
	}

}

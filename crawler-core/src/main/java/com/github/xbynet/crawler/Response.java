package com.github.xbynet.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.xbynet.crawler.parser.JsonPathParser;
import com.github.xbynet.crawler.parser.JsoupParser;
import com.github.xbynet.crawler.parser.XpathParser;
import com.github.xbynet.crawler.utils.BeanUtil;

public class Response {
	private int code;
	private String contentType;
	private Map<String,List<String>> headers;
	private Const.ResponseType respType;
	private String raw;//如果respType为Const.ResponseType.TEXT，则有值
	private byte[] bytes;//如果respType为Const.ResponseType.BIN，则有值
	private Request request;
	private List<Request> continueRequest;
	private Response parentResponse=null;//用于分块时

	public Response(){
		
	}
	public Response(Response parent){
		this.parentResponse=parent;
	}
	public JsoupParser html(){
		return new JsoupParser(raw);
	}
	public JsoupParser xml(){
		return new JsoupParser(raw);
	}
	public JsonPathParser json(){
		//处理jsonp的情形
		if(!raw.startsWith("{")&&!raw.startsWith("[")){
			raw=raw.substring(raw.indexOf("(")+1,raw.length()-1);
		}
		return new JsonPathParser(raw);
	}
	public XpathParser xpath(){
		return new XpathParser(raw);
	}
	
	public String getRaw(){
		return raw;
	}
	public Response setRaw(String raw) {
		this.raw = raw;
		return this;
	}
	public int getCode() {
		return code;
	}
	public Response setCode(int code) {
		this.code = code;
		return this;
	}
	public String getContentType() {
		return contentType;
	}
	public Response setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}
	public Map<String,List<String>> getHeaders() {
		return headers;
	}
	public Response setHeaders(Map<String,List<String>> headers) {
		this.headers = headers;
		return this;
	}
	public Const.ResponseType getRespType() {
		return respType;
	}
	public Response setRespType(Const.ResponseType respType) {
		this.respType = respType;
		return this;
	}
	public byte[] getBytes() {
		return bytes;
	}
	public Response setBytes(byte[] bytes) {
		this.bytes = bytes;
		return this;
	}
	public Request getRequest() {
		return request;
	}
	public Response setRequest(Request request) {
		this.request = request;
		return this;
	}
	
	public Response addRequest(String url,boolean copyParent){
		if(continueRequest==null){
			continueRequest=new ArrayList<Request>();
		}
		Request req=new Request();
		if(copyParent){
			BeanUtil.copyProperties(request, req);
		}
		req.setUrl(url);
		continueRequest.add(req);
		return this;
	}
	public Response addRequest(Request req){
		if(continueRequest==null){
			continueRequest=new ArrayList<Request>();
		}
		continueRequest.add(req);
		return this;
	}
	public List<Request> getContinueReqeusts(){
		return continueRequest;
	}
	public Response addPartRequest(String url,boolean copyParent){
		Request req=new Request();
		if(copyParent){
			//不支持分块嵌套分块
			if(parentResponse==null){
				BeanUtil.copyProperties(request, req);
			}else{
				BeanUtil.copyProperties(parentResponse.getRequest(),req);
			}
		}
		req.setUrl(url);
		req.setPartRequest(null);
		return this;
	}
	public Response addPartRequest(Request req){
		if(parentResponse==null){
			request.getPartRequest().add(req);
		}else{
			parentResponse.getRequest().getPartRequest().add(req);
		}
		return this;
	}
	public boolean isPartResponse(){
		return parentResponse!=null;
	}
	public Response getParentResponse() {
		return parentResponse;
	}

}

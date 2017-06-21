package com.github.xbynet.crawler.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xbynet.crawler.IpProxyProvider;
import com.github.xbynet.crawler.Request;
import com.github.xbynet.crawler.RequestAction;
import com.github.xbynet.crawler.Response;
import com.github.xbynet.crawler.Site;
import com.github.xbynet.crawler.Spider;
import com.github.xbynet.crawler.SpiderListener;
import com.github.xbynet.crawler.Const.HttpMethod;
import com.github.xbynet.crawler.utils.CrawlerUtils;

public abstract class AbsDownloader implements Downloader{
	private Logger log=LoggerFactory.getLogger(AbsDownloader.class);
	
	private CloseableHttpClient client;
	private Spider spider;
	
	public AbsDownloader(){
		
	}
	public void init(){
		HttpClientFactory clientFactory=spider.getHttpClientFactory();
		if(clientFactory==null){
			clientFactory=new HttpClientFactory();
		}
		this.client=clientFactory.getClient();
	}
	protected void doDownload(Request request,Object... extras){
		String url=request.getUrl();
		Site site=getSpider().getSite();
		IpProxyProvider ipProxyProvider=getSpider().getIpProvider();
		HttpHost proxy=null;
		if(ipProxyProvider!=null){
			proxy=ipProxyProvider.getIp();
		}
		
		log.debug(getSpider().getName()+",开始请求"+url);
		HttpUriRequest httpUriRequest=generateHttpRequest(site, request, proxy);
		
		Response response=new Response();
		boolean state=cycleRequest(httpUriRequest,request,site,response,extras);
		
		if(!state){
			log.error("no content crawled for "+request.getUrl());
			notifyListener(false,request,null);
			return;
		}
		addContinueRequest(response);
		notifyListener(true,request,null);
		//循环遍历所有分块请求
		List<Request> orderReqList=request.getPartRequest();
		while(orderReqList!=null && orderReqList.size()>0){
			Request req=orderReqList.remove(0);
			spider.getScheduler().getDuplicateRemover().isDuplicate(req, spider);
			Response resp=new Response(response);
			state=cycleRequest(generateHttpRequest(site, req, proxy), req, site, resp, extras);
			if(!state){
				log.error("no content crawled for "+req.getUrl());
				notifyListener(false, req, null);
			}else{
				notifyListener(true,req,null);
			}
			addContinueRequest(resp);
		}
	}
	protected void addContinueRequest(Response response){
		List<Request> reqlist=response.getContinueReqeusts();
		if(reqlist!=null){
			for(Request req:reqlist){
				spider.getScheduler().push(req, spider);
			}
		}
	}
	protected boolean cycleRequest(HttpUriRequest httpUriRequest,Request request,Site site,Response response,Object... extras){
		boolean state=false;
		try {
			state = doRequest(httpUriRequest, request,site,response,extras);
		} catch (Exception e) {
			log.error("",e);
		}
		int retryCount=request.getRetryCount()>=0?request.getRetryCount():site.getRetry();
		int retrySleepTimes=request.getRetrySleepTime()>=0?request.getRetrySleepTime():site.getRetrySleep();
		int retryIndex=1;
		while(!state && retryIndex<retryCount){
			retryIndex++;
			CrawlerUtils.sleep(retrySleepTimes);
			try {
				state=doRequest(httpUriRequest, request,site,response,extras);
			} catch (Exception e) {
				log.error("",e);
			}
		}
		return state;
	}
	protected void notifyListener(boolean state,Request request,Exception e){
		SpiderListener listener=spider.getSpiderListener();
		if(listener==null){
			return;
		}
		if(state){
			listener.success(spider, request);
		}else{
			listener.fail(spider, request, e);
		}
	}
	protected boolean doRequest(HttpUriRequest httpUriRequest,Request request,Site site,Response response,Object... extras) throws Exception{
		RequestAction action=request.getAction();
		boolean state=false;
		HttpClientContext clientContext=request.getCtx();
		if(clientContext==null){
			clientContext=new HttpClientContext();
		}
		IpProxyProvider ipProxyProvider=getSpider().getIpProvider();
		HttpHost proxy=null;
		if(ipProxyProvider!=null){
			proxy=ipProxyProvider.getIp();
		}
		CloseableHttpResponse resp=null;
		try {
			if(action!=null){
				action.before(client, httpUriRequest);
			}
			resp=client.execute(httpUriRequest,clientContext);
			if(ipProxyProvider!=null){
				ipProxyProvider.valid(proxy);
			}
			state=true;
			if(action!=null){
				action.after(client, resp);
			}
			process(httpUriRequest, resp, request, site,response,extras);
			
		} catch (ClientProtocolException e) {
			if(ipProxyProvider!=null){
				ipProxyProvider.invalid(proxy);
			}
			throw new RuntimeException(e);
		} catch (IOException e) {
			if(ipProxyProvider!=null){
				ipProxyProvider.invalid(proxy);
			}
			throw new RuntimeException(e);
		}finally{
			if(resp!=null){
				EntityUtils.consumeQuietly(resp.getEntity());
			}
		}
		return state;
	}
	protected abstract void process(HttpUriRequest httpUriRequest,CloseableHttpResponse resp,Request request,Site site,Response response,Object... extras);
	
	protected HttpUriRequest generateHttpRequest(Site site,Request request,HttpHost proxy){
		RequestBuilder requestBuilder = selectRequestMethod(request).setUri(request.getUrl());
        if (site.getHeaders() != null) {
            for (Map.Entry<String, String> headerEntry : site.getHeaders().entrySet()) {
                requestBuilder.setHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        if (site != null) {
            requestConfigBuilder.setConnectionRequestTimeout(site.getTimeout())
                    .setSocketTimeout(site.getTimeout())
                    .setConnectTimeout(site.getTimeout())
                    .setCookieSpec(CookieSpecs.STANDARD);
        }

        if (proxy != null) {
            requestConfigBuilder.setProxy(proxy);
        }
        requestBuilder.setConfig(requestConfigBuilder.build());
        HttpUriRequest httpUriRequest = requestBuilder.build();
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                httpUriRequest.setHeader(header.getKey(), header.getValue());
            }
        }
        return httpUriRequest;
	}
	private RequestBuilder selectRequestMethod(Request request) {
        HttpMethod method = request.getMethod();
        if (method == null || method==HttpMethod.GET) {
            return addFormParams(RequestBuilder.get(),request);
        } else if (method==HttpMethod.POST) {
            return addFormParams(RequestBuilder.post(),request);
        } else if (method==HttpMethod.HEAD) {
            return addFormParams(RequestBuilder.head(),request);
        }
        throw new IllegalArgumentException("Illegal HTTP Method " + method);
    }

    private RequestBuilder addFormParams(RequestBuilder requestBuilder, Request request) {
        if (request.getEntity() != null && "POST".equalsIgnoreCase(requestBuilder.getMethod())) {
            requestBuilder.setEntity(request.getEntity());
        }else if(request.getParams()!=null){
        	List<NameValuePair> nameValuePairs=new ArrayList<NameValuePair>();
        	for(String key:request.getParams().keySet()){
        		BasicNameValuePair pair=new BasicNameValuePair(key, request.getParams().get(key));
        		nameValuePairs.add(pair);
        	}
        	try {
				requestBuilder.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				log.error("",e);
			}
        }
        return requestBuilder;
    }

	public Spider getSpider() {
		return spider;
	}

	public void setSpider(Spider spider) {
		this.spider = spider;
	}

	@Override
	public void close() throws IOException {
		spider=null;
		client.close();
		client=null;
	}

	@Override
	public void download(Request request) {
		throw new RuntimeException("not support!");
	}
	
}

package com.github.xbynet.crawler.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xbynet.crawler.Const;
import com.github.xbynet.crawler.Request;
import com.github.xbynet.crawler.Response;
import com.github.xbynet.crawler.Site;

public class DefaultDownloader extends AbsDownloader {
	private final Logger log = LoggerFactory.getLogger(DefaultDownloader.class);

	@Override
	public void download(Request request){
		super.doDownload(request);
	}
	@Override
	protected void process(HttpUriRequest httpUriRequest,
			CloseableHttpResponse resp, Request request, Site site,Response response,
			Object... extras) {
		if (resp == null) {
			log.error(request.getUrl() + "请求失败");
			return ;
		}
		response.setCode(resp.getStatusLine().getStatusCode());
		response.setContentType(resp.getFirstHeader("Content-Type").getValue());
		Const.ResponseType type = null;
		try {
			if (response.getContentType().contains("text")
					|| response.getContentType().contains("json")) {
				type = Const.ResponseType.TEXT;
				String raw=IOUtils.toString(resp.getEntity().getContent(),
						request.getEncoding() != null ? request.getEncoding()
								: site.getEncoding());
				response.setRaw(raw);
			} else {
				type = Const.ResponseType.BIN;
				response.setBytes(IOUtils.toByteArray(resp.getEntity()
						.getContent()));
			}
		} catch (UnsupportedOperationException e) {
			log.error("", e);
		} catch (IOException e) {
			log.error("", e);
		}
		response.setRespType(type);
		response.setRequest(request);
		
		Map<String,List<String>> headers=new HashMap<String,List<String>>();
		for(Header header:resp.getAllHeaders()){
			List<String> value=new ArrayList<String>();
			HeaderElement[] hes=header.getElements();
			if(hes!=null && hes.length>1){
				for(HeaderElement e:hes){
					value.add(e.getValue());
				}
			}else{
				value.add(header.getValue());
			}
			headers.put(header.getName(), value);
		}
		response.setHeaders(headers);
		try {
			getSpider().getProcessor().process(response);
		} catch (Exception e) {
			log.error("",e);
		}
	}

}

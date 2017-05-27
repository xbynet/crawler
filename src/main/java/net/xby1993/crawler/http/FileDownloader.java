package net.xby1993.crawler.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.xby1993.crawler.Request;
import net.xby1993.crawler.Response;
import net.xby1993.crawler.Site;
import net.xby1993.crawler.Spider;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileDownloader extends AbsDownloader{
	private final Logger log=LoggerFactory.getLogger(FileDownloader.class);
	
	public FileDownloader(Spider spider) {
		super(spider);
	}
	public boolean download(Request request,String savePath){
		log.debug("开始下载文件"+request.getUrl()+"到路径"+savePath);
		super.download(request,savePath);
		File file=new File(savePath);
		return file.exists();
	}
	@Override
	protected void process(HttpUriRequest httpUriRequest,
			CloseableHttpResponse resp, Request request, Site site,Response response,Object... extras) {
		if(resp==null){
			log.error("文件"+httpUriRequest.getURI().toString()+"下载失败");
			return;
		}
		String savePath=extras[0].toString();
		File saveFile=new File(savePath);
		if(saveFile.exists()){
			saveFile.delete();
		}
		FileOutputStream fous=null;
		try {
			fous=new FileOutputStream(saveFile);
			IOUtils.copy(resp.getEntity().getContent(), fous);
			log.debug("文件"+httpUriRequest.getURI().toString()+"下载成功");
		} catch (UnsupportedOperationException e) {
			log.error("",e);
		} catch (IOException e) {
			log.error("",e);
		}finally{
			IOUtils.closeQuietly(fous);
		}
	}

	
}

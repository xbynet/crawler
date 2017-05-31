package net.xby1993.crawler;

import java.io.Closeable;
import java.io.IOException;

import net.xby1993.crawler.http.FileDownloader;

/**
 *爬虫页面处理器，撰写爬虫时需要扩展此类
 */
public abstract class Processor implements Closeable{
	private FileDownloader fileDownloader=null;
	private Spider spider=null;
	
	public abstract void process(Response resp);
	
	public boolean download(Request req,String savePath){
		return fileDownloader.download(req, savePath);
	}
	public boolean download(String url,String savePath){
		Request req=new Request(url);
		return fileDownloader.download(req, savePath);
	}
	public FileDownloader getFileDownloader() {
		return fileDownloader;
	}

	public void setFileDownloader(FileDownloader fileDownloader) {
		this.fileDownloader = fileDownloader;
	}
	@Override
	public void close()throws IOException{
		
	}

	public Spider getSpider() {
		return spider;
	}

	public void setSpider(Spider spider) {
		this.spider = spider;
	}
}

package net.xby1993.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.xby1993.crawler.parser.JsoupParser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class OSChinaTweetsCrawler extends Processor{
	private final int maxPageCount=20;
	private final AtomicInteger count=new AtomicInteger(0);
	@Override
	public void process(Response resp) {
		synchronized (count) {
			if(count.get()>maxPageCount)
				return;
		}
		count.addAndGet(1);
		String currentUrl=resp.getRequest().getUrl();
		JsoupParser parser=resp.html();
		List<String> lastIds=parser.list("span[data-last]","data-last");
		String lastId=lastIds.get(lastIds.size()-1);
		String continueUrl="https://www.oschina.net/tweets?lastLogId="+lastId;
		Request req=new Request(continueUrl);
		req.setHeader("Referer", currentUrl);
		req.setHeader("X-Requested-With", "XMLHttpRequest");
		resp.addRequest(req);
		
		StringBuilder sb=new StringBuilder();
		List<String> authors=parser.list(".tweetitem .box-fl > a","title");
		List<String> itemUrls=parser.list(".tweetitem .ti-toolbox a[title=\"查看详情\"]","href");
		List<String> itemContents=new ArrayList<String>(itemUrls.size());
		Elements els=parser.elements(".tweetitem");
		for(Element e:els){
			String tmp=e.select(".ti-content > .inner-content").first().html();
			itemContents.add(tmp.replace("src=\"/", "src=\"https://www.oschina.net/"));
		}
		for(int i=0;i<itemContents.size();i++){
			sb.append("<div style='margin: 5px 0;'><span style='color:blue;'>"+authors.get(i)+"</span><a style='color:red;margin:0 10px;' href='"+itemUrls.get(i)+"'>查看</a><span>"+itemContents.get(i)+"</span></div>\n");
		}
		appendToFile(sb.toString());
	}
	public void start() {
		Site site = new Site();
		site.setEncoding("UTF-8");
		site.setHeader("Referer", "https://www.oschina.net/");
		Spider spider = Spider.builder(this).threadNum(1).site(site)
				.urls("https://www.oschina.net/tweets?nocache="+System.currentTimeMillis()).build();
		spider.run();
		appendToFile("</body></html>");
	}
	public static void main(String[] args) {
		new OSChinaTweetsCrawler().start();
	}
	private synchronized void appendToFile(String content){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd-HH");
		File f=new File("D:\\code\\test\\oschina_tweets\\"+sdf.format(new Date())+".html");
		if(!f.exists()){
			try {
				f.createNewFile();
				FileUtils.write(f, "<!DOCTYPE html><html><head><title></title><meta charset=\"UTF-8\"></head><body>","UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileWriter writer=null;
		try {
			writer=new FileWriter(f,true);
			writer.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			IOUtils.closeQuietly(writer);
		}
	}
}

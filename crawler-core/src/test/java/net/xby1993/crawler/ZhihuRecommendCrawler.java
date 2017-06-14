package net.xby1993.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import net.xby1993.crawler.parser.JsonPathParser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZhihuRecommendCrawler extends Processor{
	private Logger log=LoggerFactory.getLogger(ZhihuRecommendCrawler.class);
	private AtomicInteger offset=new AtomicInteger(0);
	
	@Override
	public void process(Response resp) {
		String curUrl=resp.getRequest().getUrl();
		JsonPathParser parser=resp.json();
		int count=Integer.valueOf(parser.single("$.msg.length()"));
		if(count>0){
			resp.addRequest(getPostRequest(offset.addAndGet(20)));
		}
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<count;i++){
			String itemStr=parser.single("$.msg["+i+"]");
			Element e=Jsoup.parse(itemStr).select("div.zm-item").get(0);
			String title=e.select("h2 a.question_link").text();
			String link="https://www.zhihu.com"+e.select("h2 a.question_link").attr("href");
			String authorAndInfo=e.select(".summary-wrapper").text();
			String content=e.select(".zm-item-rich-text .zh-summary").html();
			sb.append("<div style='margin: 6px 0 6px 400px;max-width:800px;border-bottom: 3px dashed #ccc;'><span style='color:blue;'>"+authorAndInfo+"</span><a style='color:red;margin:0 10px;' href='"+link+"'>查看</a><div>"+content+"</div></div>\n");
		}
		appendToFile(sb.toString());
		
	}
	public void start() {
		Site site = new Site();
		site.setHeader("Referer", "https://www.zhihu.com/explore/recommendations");
		Spider spider = Spider.builder(this).threadNum(5).site(site)
				.requests(getPostRequest(0)).build();
		spider.run();
		appendToFile("</body></html>");
	}
	private Request getPostRequest(int offset){
		Request req=new Request("https://www.zhihu.com/node/ExploreRecommendListV2");
		req.setMethod(Const.HttpMethod.POST);
		req.setParams("method", "next");
		req.setParams("params", "{\"limit\":20,\"offset\":"+offset+"}");
		return req;
	}
	private synchronized void appendToFile(String content){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd-HH");
		File f=new File("D:\\code\\test\\tweets\\"+sdf.format(new Date())+".zhihu.html");
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
	public static void main(String[] args) {
		new ZhihuRecommendCrawler().start();
	}

}

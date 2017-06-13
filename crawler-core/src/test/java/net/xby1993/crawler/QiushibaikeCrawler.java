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

public class QiushibaikeCrawler extends Processor{
	@Override
	public void process(Response resp) {
		String currentUrl=resp.getRequest().getUrl();
		JsoupParser parser=resp.html();
		if(currentUrl.equals("https://www.qiushibaike.com/")){
			int pageCount=Integer.valueOf(parser.single("ul.pagination > li:nth-last-child(2) .page-numbers","text").trim());
			System.out.println("8hr共有"+pageCount+"页");
			for(int i=2;i<=pageCount;i++){
				resp.addRequest("https://www.qiushibaike.com/8hr/page/"+i+"/", false);
			}
		}else if(currentUrl.equals("https://www.qiushibaike.com/hot/")){
			int pageCount=Integer.valueOf(parser.single("ul.pagination > li:nth-last-child(2) .page-numbers","text").trim());
			System.out.println("hot共有"+pageCount+"页");
			for(int i=2;i<=pageCount;i++){
				resp.addRequest("https://www.qiushibaike.com/hot/page/"+i+"/", false);
			}
		}
		Elements els=parser.elements("#content-left > div");
		StringBuilder sb=new StringBuilder();
		for(Element e:els){
			String author=e.select(".author > a:nth-child(2)").attr("title").trim();
			String link="https://www.qiushibaike.com"+e.select(".contentHerf").attr("href");
			String content=e.select(".contentHerf .content").html();
			Elements thumbEls=e.select(".thumb");
			if(thumbEls!=null && thumbEls.size()>0){
				content+=thumbEls.get(0).outerHtml().replace("src=\"//", "src=\"http://");
			}
			sb.append("<div style='margin: 6px 0 6px 400px;max-width:800px;border-bottom: 3px dashed #ccc;'><span style='color:blue;'>"+author+"</span><a style='color:red;margin:0 10px;' href='"+link+"'>查看</a><span>"+content+"</span></div>\n");
			
		}
		appendToFile(sb.toString());
	}
	public void start() {
		Site site = new Site();
//		site.setEncoding("UTF-8");
		site.setHeader("Referer", "https://www.qiushibaike.com/");
		Spider spider = Spider.builder(this).threadNum(1).site(site)
				.urls("https://www.qiushibaike.com/","https://www.qiushibaike.com/hot/").build();
		spider.run();
		appendToFile("</body></html>");
	}
	public static void main(String[] args) {
		new QiushibaikeCrawler().start();
	}
	private synchronized void appendToFile(String content){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd-HH");
		File f=new File("D:\\code\\test\\tweets\\"+sdf.format(new Date())+".qiushibaike.html");
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

package net.xby1993.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.github.xbynet.crawler.Processor;
import com.github.xbynet.crawler.Response;
import com.github.xbynet.crawler.Site;
import com.github.xbynet.crawler.Spider;
import com.github.xbynet.crawler.parser.JsonPathParser;
import com.github.xbynet.crawler.parser.JsoupParser;

public class NeihanshequCrawler  extends Processor{
	private static final int maxCount=100;
	private AtomicInteger count=new AtomicInteger(0);
	
	@Override
	public void process(Response resp) {
		String currentUrl=resp.getRequest().getUrl();
		
		if(currentUrl.equals("http://neihanshequ.com/")){
			JsoupParser parser=resp.html();
			List<String> scripts=parser.scripts("script");
			for(String str:scripts){
				if(str.contains("var gListViewConfig")){
					Pattern p=Pattern.compile("max_time: '(.*?)',",Pattern.MULTILINE);
					Matcher m=p.matcher(str);
					if(m.find()){
						String maxTime=m.group(1);
						if(maxTime.contains(".")){
							maxTime=maxTime.split("\\.")[0];
						}
						if(count.getAndIncrement()<=maxCount){
							resp.addRequest("http://neihanshequ.com/joke/?is_json=1&app_name=neihanshequ_web&max_time="+maxTime, true);
						}
						return;
					}
					break;
				}
			}
		}else{
			JsonPathParser parser=resp.json();
			String maxTime=parser.single("$.data.max_time");
			if(maxTime.contains("E")){
				maxTime=new BigDecimal(maxTime).toPlainString();
			}
			if(count.getAndIncrement()<=maxCount){
				resp.addRequest("http://neihanshequ.com/joke/?is_json=1&app_name=neihanshequ_web&max_time="+maxTime, true);
			}
			StringBuilder sb=new StringBuilder();
			int size=Integer.valueOf(parser.single("$.data.data.length()"));
			for(int i=0;i<size;i++){
				String author=parser.single("$.data.data["+i+"].group.user.name");
				String link="http://neihanshequ.com/p"+parser.single("$.data.data["+i+"].group.id")+"/";
				String content=parser.single("$.data.data["+i+"].group.content");
				sb.append("<div style='margin: 6px 0 6px 400px;max-width:800px;border-bottom: 3px dashed #ccc;'><span style='color:blue;'>"+author+"</span><a style='color:red;margin:0 10px;' href='"+link+"'>查看</a><span>"+content+"</span></div>\n");
			}
			appendToFile(sb.toString());
		}
	}
	public void start() {
		Site site = new Site();
//		site.setEncoding("UTF-8");
		site.setHeader("Referer", "http://neihanshequ.com/");
		Spider spider = Spider.builder(this).threadNum(1).site(site)
				.urls("http://neihanshequ.com/").build();
		spider.run();
		appendToFile("</body></html>");
	}
	public static void main(String[] args) {
		new NeihanshequCrawler().start();
	}
	private synchronized void appendToFile(String content){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd-HH");
		File f=new File("D:\\code\\test\\tweets\\"+sdf.format(new Date())+".neihanshequ.html");
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
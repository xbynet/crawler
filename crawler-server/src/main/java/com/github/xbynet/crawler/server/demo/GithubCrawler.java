package com.github.xbynet.crawler.server.demo;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;

import com.github.xbynet.crawler.Const;
import com.github.xbynet.crawler.Processor;
import com.github.xbynet.crawler.Request;
import com.github.xbynet.crawler.RequestAction;
import com.github.xbynet.crawler.Response;
import com.github.xbynet.crawler.Site;
import com.github.xbynet.crawler.Spider;
import com.github.xbynet.crawler.http.DefaultDownloader;
import com.github.xbynet.crawler.http.FileDownloader;
import com.github.xbynet.crawler.http.HttpClientFactory;
import com.github.xbynet.crawler.parser.JsoupParser;
import com.github.xbynet.crawler.scheduler.DefaultScheduler;

public class GithubCrawler extends Processor {
	@Override
	public void process(Response resp) {
		String currentUrl = resp.getRequest().getUrl();
		System.out.println("CurrentUrl:" + currentUrl);
		int respCode = resp.getCode();
		System.out.println("ResponseCode:" + respCode);
		System.out.println("type:" + resp.getRespType().name());
		String contentType = resp.getContentType();
		System.out.println("ContentType:" + contentType);
		Map<String, List<String>> headers = resp.getHeaders();
		System.out.println("ResonseHeaders:");
		for (String key : headers.keySet()) {
			List<String> values=headers.get(key);
			for(String str:values){
				System.out.println(key + ":" +str);
			}
		}
		JsoupParser parser = resp.html();
		// suppport parted ,分块抓取是会有个parent response来关联所有分块response
		// System.out.println("isParted:"+resp.isPartResponse());
		// Response parent=resp.getParentResponse();
		// resp.addPartRequest(null);
		//Map<String,Object> extras=resp.getRequest().getExtras();

		if (currentUrl.equals("https://github.com/xbynet")) {
			String avatar = parser.single("img.avatar", "src");
			String dir = System.getProperty("java.io.tmpdir");
			String savePath = Paths.get(dir, UUID.randomUUID().toString())
					.toString();
			boolean avatarDownloaded = download(avatar, savePath);
			System.out.println("avatar:" + avatar + ", saved:" + savePath);
			// System.out.println("avtar downloaded status:"+avatarDownloaded);
			String name = parser.single(".vcard-names > .vcard-fullname",
					"text");
			System.out.println("name:" + name);
			List<String> reponames = parser.list(
					".pinned-repos-list .repo.js-repo", "text");
			List<String> repoUrls = parser.list(
					".pinned-repo-item .d-block >a", "href");
			System.out.println("reponame:url");
			if (reponames != null) {
				for (int i = 0; i < reponames.size(); i++) {
					String tmpUrl="https://github.com"+repoUrls.get(i);
					System.out.println(reponames.get(i) + ":"+tmpUrl);
					Request req=new Request(tmpUrl).putExtra("name", reponames.get(i));
					resp.addRequest(req);
				}
			}
		}else{
			Map<String,Object> extras=resp.getRequest().getExtras();
			String name=extras.get("name").toString();
			System.out.println("repoName:"+name);
			String shortDesc=parser.single(".repository-meta-content","allText");
			System.out.println("shortDesc:"+shortDesc);
		}
	}

	public Spider createSpider() {
		Site site = new Site();
		Spider spider = Spider.builder(this).threadNum(5).site(site)
				.urls("https://github.com/xbynet").build();
//		spider.run();
		return spider;
	}

	public void startCompleteConfig() {
		String pcUA = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";
		String androidUA = "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.23 Mobile Safari/537.36";

		Site site = new Site();
		site.setEncoding("UTF-8").setHeader("Referer", "https://github.com/")
				.setRetry(3).setRetrySleep(3000).setSleep(50).setTimeout(30000)
				.setUa(pcUA);

		Request request = new Request("https://github.com/xbynet");
		HttpClientContext ctx = new HttpClientContext();
		BasicCookieStore cookieStore = new BasicCookieStore();
		ctx.setCookieStore(cookieStore);
		request.setAction(new RequestAction() {
			@Override
			public void before(CloseableHttpClient client, HttpUriRequest req) {
				System.out.println("before-haha");
			}

			@Override
			public void after(CloseableHttpClient client,
					CloseableHttpResponse resp) {
				System.out.println("after-haha");
			}
		}).setCtx(ctx).setEncoding("GBK")
				.putExtra("somekey", "我是可以在response中使用的extras哦")
				.setHeader("User-Agent", pcUA).setMethod(Const.HttpMethod.GET)
				.setPartRequest(null).setEntity(null)
				.setParams("appkeyqqqqqq", "1213131232141").setRetryCount(5)
				.setRetrySleepTime(10000);

		Spider spider = Spider.builder(this).threadNum(5)
				.name("Spider-github-xbynet")
				.defaultDownloader(new DefaultDownloader())
				.fileDownloader(new FileDownloader())
				.httpClientFactory(new HttpClientFactory()).ipProvider(null)
				.listener(null).pool(null).scheduler(new DefaultScheduler())
				.shutdownOnComplete(true).site(site).build();
		spider.run();
	}

	public static void main(String[] args) {
//		new GithubCrawler().start();
	}
}

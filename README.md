# crawler
A simple and flexible web crawler framework for java.

## Features:
1、Code  is easy to understand and customized (代码简单易懂，可定制性强)     
2、Api is simple and easy to use         
3、Support File download、Content part fetch.(支持文件下载、分块抓取)          
4、Request And Response support much options、strong customizable.(请求和响应支持的内容和选项比较丰富、每个请求可定制性强)   
5、Support do your own operation before or after network request in downloader(支持网络请求前后执行自定义操作)        
6、Selenium+PhantomJS support     
7、Redis support      

## Future:
1、Complete the code comment and test(完善代码注释和完善测试代码)     

## Install:

The only module that must be added is crawler-core
```xml
<dependency>
    <groupId>com.github.xbynet</groupId>
    <artifactId>crawler-core</artifactId>
    <version>0.3.0</version>
</dependency
```
But if you want to use selenium support:
```xml
<dependency>
    <groupId>com.github.xbynet</groupId>
    <artifactId>crawler-selenium</artifactId>
    <version>0.3.0</version>
</dependency
```

Module crawler-server is now a experimental attempt, and now has more work to do on it.

## Demo:

```java
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

	public void start() {
		Site site = new Site();
		Spider spider = Spider.builder(this).threadNum(5).site(site)
				.urls("https://github.com/xbynet").build();
		spider.run();
	}
  
	public static void main(String[] args) {
		new GithubCrawler().start();
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
		}).setCtx(ctx).setEncoding("UTF-8")
				.putExtra("somekey", "I can use in the response by your own")
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


}

```
## Examples:

- Github(github个人项目信息)
- OSChinaTweets(开源中国动弹)
- Qiushibaike(醜事百科)
- Neihanshequ(内涵段子)   
- ZihuRecommend(知乎推荐)   
 
**More Examples:** Please see [here](https://github.com/xbynet/crawler/tree/master/crawler-core/src/test/java/net/xby1993/crawler)  

## Thanks: 
[webmagic](https://github.com/code4craft/webmagic):本项目借鉴了webmagic多处代码，设计上也作了较多参考，非常感谢。     
[xsoup](https://github.com/code4craft/xsoup)：本项目使用xsoup作为底层xpath处理器      
[JsonPath](https://github.com/json-path/JsonPath)：本项目使用JsonPath作为底层jsonpath处理器    
[Jsoup](https://jsoup.org/) 本项目使用Jsoup作为底层HTML/XML处理器      
[HttpClient](http://hc.apache.org/) 本项目使用HttpClient作为底层网络请求工具    

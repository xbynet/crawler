package net.xby1993.crawler.selenium;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xby1993.crawler.Const;
import net.xby1993.crawler.Request;
import net.xby1993.crawler.Response;
import net.xby1993.crawler.Spider;
import net.xby1993.crawler.http.Downloader;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeleniumDownloader implements Downloader {
	private static final Logger log = LoggerFactory
			.getLogger(SeleniumDownloader.class);
	private int sleepTime = 3000;// 3s
	private SeleniumAction action = null;
	private WebDriverPool webDriverPool;
	private Spider spider;

	public SeleniumDownloader(WebDriverPool webDriverPool) {
		this.webDriverPool = webDriverPool;
	}

	public SeleniumDownloader(int sleepTime, WebDriverPool pool) {
		this(sleepTime, pool, null);
	}

	public SeleniumDownloader(int sleepTime, WebDriverPool pool,
			SeleniumAction action) {
		this.sleepTime = sleepTime;
		this.action = action;
		this.webDriverPool = pool;
	}

	public void setOperator(SeleniumAction action) {
		this.action = action;
	}

	@Override
	public void download(Request request) {
		WebDriver webDriver;
		try {
			webDriver = webDriverPool.get();
		} catch (InterruptedException e) {
			log.warn("interrupted", e);
			return;
		}
		log.info("downloading page " + request.getUrl());
		Response resp = new Response();
		resp.setRequest(request);
		resp.setRespType(Const.ResponseType.TEXT);
		try {
			webDriver.get(request.getUrl());
			Thread.sleep(sleepTime);
		} catch (Exception e) {
			log.error("", e);
			webDriverPool.close(webDriver);
			return;
		}
		try {
			WebDriver.Options manage = webDriver.manage();
			manage.window().maximize();
			if (action != null) {
				action.execute(webDriver);
			}
			SeleniumAction reqAction = null;
			if (request.getExtras() != null
					&& request.getExtras().containsKey("action")) {
				reqAction = (SeleniumAction) request.getExtras().get("action");
			}
			if (reqAction != null) {
				reqAction.execute(webDriver);
			}

			WebElement webElement = webDriver.findElement(By.xpath("/html"));
			String content = webElement.getAttribute("outerHTML");

			resp.setRaw(content);
			Map<String, List<String>> headers = new HashMap<String, List<String>>();
			List<String> cookielist = new ArrayList<String>(1);
			cookielist.add(WindowUtil.getHttpCookieString(webDriver.manage()
					.getCookies()));
			headers.put("Set-Cookie", cookielist);
			resp.setHeaders(headers);
			
			getSpider().getProcessor().process(resp);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			webDriverPool.returnToPool(webDriver);
		}
	}
	public Spider getSpider() {
		return spider;
	}

	public void setSpider(Spider spider) {
		this.spider = spider;
	}

	@Override
	public void close() throws IOException {
		webDriverPool.shutdown();
	}

	@Override
	public void init() {
		
	}
}

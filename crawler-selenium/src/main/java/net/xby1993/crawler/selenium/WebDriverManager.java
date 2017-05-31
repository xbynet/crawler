package net.xby1993.crawler.selenium;

import java.io.Closeable;
import java.io.IOException;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDriverManager implements Closeable{
	private static final Logger log=LoggerFactory.getLogger(WebDriverManager.class);
	
	private WebDriverPool webDriverPool=null;
	
	public WebDriverManager(String phantomjsPath){
		this.webDriverPool=new PhantomjsWebDriverPool(1,false,phantomjsPath);
	}
	public WebDriverManager(WebDriverPool webDriverPool){
		this.webDriverPool=webDriverPool;
	}
	public void load(String url,int sleepTimeMillis,SeleniumAction... actions){
		WebDriver driver=null;
		try {
			driver=webDriverPool.get();
			driver.get(url);
			sleep(sleepTimeMillis);
			WebDriver.Options manage = driver.manage();
			manage.window().maximize();
			for(SeleniumAction action:actions){
				action.execute(driver);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.error("",e);
		}finally{
			if(driver!=null){
				webDriverPool.returnToPool(driver);
			}
		}
	}
	public void load(SeleniumAction... actions){
		WebDriver driver=null;
		try {
			driver=webDriverPool.get();
			WebDriver.Options manage = driver.manage();
			manage.window().maximize();
			for(SeleniumAction action:actions){
				action.execute(driver);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.error("",e);
		}finally{
			if(driver!=null){
				webDriverPool.returnToPool(driver);
			}
		}
	}
	public void shutDown(){
		if(webDriverPool!=null){
			webDriverPool.shutdown();
		}
	}
	@Override
	public void close() throws IOException {
		shutDown();
	}
	public void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}

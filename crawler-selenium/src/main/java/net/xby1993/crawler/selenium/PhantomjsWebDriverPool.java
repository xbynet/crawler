package net.xby1993.crawler.selenium;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author taojw
 */
public class PhantomjsWebDriverPool implements WebDriverPool {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private int CAPACITY = 5;
	private AtomicInteger refCount = new AtomicInteger(0);
	private static final String DRIVER_PHANTOMJS = "phantomjs";

	/**
	 * store webDrivers available
	 */
	private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>(
			CAPACITY);

	private AtomicBoolean shutdowned = new AtomicBoolean(false);

	private String PHANTOMJS_PATH;
	private DesiredCapabilities caps = DesiredCapabilities.phantomjs();

	public PhantomjsWebDriverPool(String phantomjsPath) {
		this(5, false, phantomjsPath);
	}

	/**
	 * 
	 * @param poolsize
	 * @param loadImg
	 *            是否加载图片，默认不加载
	 */
	public PhantomjsWebDriverPool(int poolsize, boolean loadImg,
			String phantomjsPath) {
		this.CAPACITY = poolsize;
		innerQueue = new LinkedBlockingDeque<WebDriver>(poolsize);
		PHANTOMJS_PATH = phantomjsPath;
		caps.setJavascriptEnabled(true);
		caps.setCapability(
				PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
				PHANTOMJS_PATH);
		// caps.setCapability("takesScreenshot", false);
		caps.setCapability(
				PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX
						+ "User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
		ArrayList<String> cliArgsCap = new ArrayList<String>();
		// http://phantomjs.org/api/command-line.html
		cliArgsCap.add("--web-security=false");
		cliArgsCap.add("--ssl-protocol=any");
		cliArgsCap.add("--ignore-ssl-errors=true");
		if (loadImg) {
			cliArgsCap.add("--load-images=true");
		} else {
			cliArgsCap.add("--load-images=false");
		}
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
				cliArgsCap);
		caps.setCapability(
				PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_CLI_ARGS,
				new String[] { "--logLevel=INFO" });

	}

	public WebDriver get() throws InterruptedException {
		WebDriver poll = innerQueue.poll();
		if (poll != null) {
			return poll;
		}
		if (refCount.get() < CAPACITY) {
			synchronized (innerQueue) {
				if (refCount.get() < CAPACITY) {

					WebDriver mDriver = new PhantomJSDriver(caps);
					// 尝试性解决：https://github.com/ariya/phantomjs/issues/11526问题
					mDriver.manage().timeouts()
							.pageLoadTimeout(60, TimeUnit.SECONDS);
					// mDriver.manage().window().setSize(new Dimension(1366,
					// 768));
					innerQueue.add(mDriver);
					refCount.incrementAndGet();
				}
			}
		}
		return innerQueue.take();
	}

	public void returnToPool(WebDriver webDriver) {
		if (shutdowned.get()) {
			webDriver.quit();
			webDriver = null;
		} else {
			Set<String> handles = webDriver.getWindowHandles();
			if (handles.size() > 1) {
				int index = 0;
				for (String handle : handles) {
					if (index == 0) {
						index++;
						continue;
					}
					WindowUtil.changeWindowTo(webDriver, handle);
					webDriver.close();
					index++;
				}
			}
			synchronized (shutdowned) {
				if(!shutdowned.get()){
					innerQueue.add(webDriver);
				}else{
					webDriver.quit();
					webDriver = null;
				}
			}
		}
	}

	public void close(WebDriver webDriver) {
		refCount.decrementAndGet();
		webDriver.quit();
		webDriver = null;
	}

	public void shutdown() {
		synchronized (shutdowned) {
			shutdowned.set(true);
		}
		try {
			for (WebDriver driver : innerQueue) {
				close(driver);
			}
			innerQueue.clear();
			refCount.set(0);
		} catch (Exception e) {
			logger.warn("webdriverpool关闭失败", e);
		}
	}
}

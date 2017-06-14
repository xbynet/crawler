package net.xby1993.crawler.selenium;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author taojw
 *
 */
public class WindowUtil {
	private static final Logger log=LoggerFactory.getLogger(WindowUtil.class);
	/**
	 * 窗口最大化
	 * @param driver
	 */
	public static void maximize(WebDriver driver){
		WebDriver.Options manage = driver.manage();
//		manage.window().maximize();
		manage.window().setSize(new Dimension(1920,1080));
		driver.navigate().refresh();
	}
	/**
	 * 滚动窗口。
	 * @param driver
	 * @param height
	 */
	public static void scroll(WebDriver driver,int height){
		((JavascriptExecutor)driver).executeScript("window.scrollTo(0,"+height+" );");	
	}
	/**
	 * 重新调整窗口大小，以适应页面，需要耗费一定时间。建议等待合理的时间。
	 * @param driver
	 */
	public static void loadAll(WebDriver driver){
		Dimension od=driver.manage().window().getSize();
		int width=driver.manage().window().getSize().width;
		//尝试性解决：https://github.com/ariya/phantomjs/issues/11526问题
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS); 
		long height=(Long)((JavascriptExecutor)driver).executeScript("return document.body.scrollHeight;");
		driver.manage().window().setSize(new Dimension(width, (int)height));
		driver.navigate().refresh();
	}
	public static void refresh(WebDriver driver){
		driver.navigate().refresh();
	}
	public static void taskScreenShot(WebDriver driver,File saveFile){
		if(saveFile.exists()){
			saveFile.delete();
		}
		byte[] src=((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);//.FILE);linux下非root用户，java创建临时文件存在问题
		log.info("截图文件字节长度"+src.length);
		try {
			FileUtils.writeByteArrayToFile(saveFile, src);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("截图写入失败",e);
		}
	}
	public static void changeWindow(WebDriver driver){
		// 获取当前页面句柄
		String handle = driver.getWindowHandle();
		// 获取所有页面的句柄，并循环判断不是当前的句柄，就做选取switchTo()
		for (String handles : driver.getWindowHandles()) {
			if (handles.equals(handle))
				continue;
			driver.switchTo().window(handles);
		}
	}
	public static void changeWindowTo(WebDriver driver,String handle){
		for (String tmp : driver.getWindowHandles()) {
			if (tmp.equals(handle)){
				driver.switchTo().window(handle);
				break;
			}
		}
	}
	
	/**
	 * 打开一个新tab页，返回该tab页的windowhandle
	 * @param driver
	 * @param url
	 * @return
	 */
	public static String openNewTab(WebDriver driver,String url){
		Set<String> strSet1=driver.getWindowHandles();
		((JavascriptExecutor)driver).executeScript("window.open('"+url+"','_blank');");
		sleep(1000);
		Set<String> strSet2=driver.getWindowHandles();
		for(String tmp:strSet2){
			if(!strSet1.contains(tmp)){
				return tmp;
			}
		}
		return null;
	}
	public static void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 操作关闭模态窗口
	 * @param driver
	 * @param type 如Id,ClassName
	 * @param sel 选择器
	 */
	public static void clickModal(WebDriver driver,String type,String sel){
		String js="document.getElementsBy"+type+"('"+sel+"')[0].click();";
		((JavascriptExecutor)driver).executeScript(js);
	}
	
	/**
	 * 判断一个元素是否存在
	 * @param driver
	 * @param by
	 * @return
	 */
	public static boolean checkElementExists(WebDriver driver,By by){
		try{
			driver.findElement(by);
			return true;
		}catch(NoSuchElementException e){
			return false;
		}
	}
	/**
	 * 点击一个元素
	 * @param driver
	 * @param by
	 */
	public static void clickElement(WebDriver driver,By by){
		WebElement tmp=driver.findElement(by);
		Actions actions=new Actions(driver);
		actions.moveToElement(tmp).click().perform();
	}
	public static void clickElement(WebDriver driver,WebElement tmp){
		Actions actions=new Actions(driver);
		actions.moveToElement(tmp).click().perform();
	}
	public static Object execJs(WebDriver driver,String js){
		return ((JavascriptExecutor)driver).executeScript(js);
	}
	public static void clickByJsCssSelector(WebDriver driver,String cssSelector){
		String js="document.querySelector('"+cssSelector+"').click();";
		((JavascriptExecutor)driver).executeScript(js);
	}
	
	public static Set<Cookie> getCookies(WebDriver driver){
		return driver.manage().getCookies();
	}
	public static void setCookies(WebDriver driver,Set<Cookie> cookies){
		if(cookies==null){
			return;
		}
		//Phantomjs存在Cookie设置bug,只能通过js来设置了。
		StringBuilder sb=new StringBuilder();
		for(Cookie cookie:cookies){
			String js="document.cookie=\""+cookie.getName()+"="+cookie.getValue()+";path="+cookie.getPath()+";domain="+cookie.getDomain()+"\";";
			sb.append(js);
		}
		((JavascriptExecutor)driver).executeScript(sb.toString());
	}
	
	public static String getHttpCookieString(Set<Cookie> cookies){
		if(cookies==null){
			return "";
		}
		String httpCookie="";
		int index=0;
		for(Cookie c:cookies){
			index++;
			if(index==cookies.size()){
				httpCookie+=c.getName()+"="+c.getValue();
			}else{
				httpCookie+=c.getName()+"="+c.getValue()+"; ";
			}
		}
		return httpCookie;
	}
	
	/**
	 * 获取css属性，最典型的就是获取某个元素的display状态
	 * @param driver
	 * @param cssSelector
	 * @param attr
	 * @return
	 */
	public static Object getCssAttr(WebDriver driver,String cssSelector,String attr){
		InputStream ins=WindowUtil.class.getResourceAsStream("getCssAttr.js");
		String externalJS="";
		try {
			externalJS = IOUtils.toString(ins,"UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		IOUtils.closeQuietly(ins);
		Object res = ((JavascriptExecutor) driver).executeScript(externalJS,cssSelector,attr);
		return res;
	}
}

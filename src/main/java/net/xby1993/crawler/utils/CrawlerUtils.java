package net.xby1993.crawler.utils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.xby1993.crawler.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlerUtils {
	private static final Logger log=LoggerFactory.getLogger(CrawlerUtils.class);
	
	public static void sleep(int millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			log.warn("",e);
		}
	}
	
	public Object executeJs(String js,@Nullable String funcName,Object... args){
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");
		try {
			Object res=engine.eval(js);
			if(StringUtils.isNotBlank(funcName)){
				if (engine instanceof Invocable) {
					Invocable invoke = (Invocable) engine;
					res = invoke.invokeFunction(funcName, args);
				}
			}
			return res;
		} catch (Exception e) {
			log.error("",e);
		}
		return null;
	}
}

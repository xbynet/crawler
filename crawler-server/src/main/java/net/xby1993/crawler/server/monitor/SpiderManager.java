package net.xby1993.crawler.server.monitor;

import java.util.concurrent.ConcurrentHashMap;

import net.xby1993.crawler.Spider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpiderManager {
	private Logger log=LoggerFactory.getLogger(SpiderManager.class);
	
	private ConcurrentHashMap<String, Spider> spiders=new ConcurrentHashMap<>();
	
	private SpiderManager(){
		
	}
	
	private static class SingleHolder{
		static SpiderManager instance=new SpiderManager();
	}
	
	public static SpiderManager get(){
		return SingleHolder.instance;
	}
	
	public synchronized void add(Spider... spiders1){
		for(Spider s:spiders1){
			getSpiders().put(s.getName(),s);
		}
	}
	public synchronized Spider remove(String name){
		return getSpiders().remove(name);
	}
	public synchronized void stopAll(){
		for(String key:getSpiders().keySet()){
			stop(key);
		}
	}
	public synchronized void startAll(){
		for(String key:getSpiders().keySet()){
			start(key);
		}
	}
	public String status(String name){
		if(!getSpiders().containsKey(name)){
			throw new IllegalArgumentException("the spider of "+name+" is not in manager");
		}
		Spider spider=getSpiders().get(name);
		return spider.getState().name();
	}
	
	public synchronized boolean stop(String name){
		if(!getSpiders().containsKey(name)){
			throw new IllegalArgumentException("the spider of "+name+" is not in manager");
		}
		Spider spider=getSpiders().get(name);
		if(spider.isRunning()){
			spider.stop();
			return true;
		}else{
			log.warn("illegal status "+spider.getState().name()+" for stop");
			return false;
		}
	}
	public synchronized boolean start(String name){
		if(!getSpiders().containsKey(name)){
			throw new IllegalArgumentException("the spider of "+name+" is not in manager");
		}
		Spider spider=getSpiders().get(name);
		if(spider.getState()==Spider.Status.NotRun){
			spider.runAsync();
			return true;
		}
		if(spider.isStopped()){
			if(spider.isShutdownOnComplete()){
				log.warn("spider of "+name+" setShutdownOnComplete=true, so it's not support restart");
				return false;
			}
			spider.runAsync();
			return true;
		}
		log.warn("illegal status "+spider.getState().name()+" for start");
		return false;
	}

	public ConcurrentHashMap<String, Spider> getSpiders() {
		return spiders;
	}
}

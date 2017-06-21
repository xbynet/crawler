package com.github.xbynet.crawler.server;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;


/**
 *Embeded Tomcat 
 *http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/basic_app_embedded_tomcat/basic_app-tomcat-embedded.html
 *https://github.com/heroku/devcenter-embedded-tomcat
 */
public class Main {
	
	public static void main(String[] args) throws Exception {
		String contextPath = "/";
		String appBase = ".";
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(8666);
		tomcat.getHost().setAppBase(appBase);
		StandardContext ctx=(StandardContext)tomcat.addWebapp(contextPath, appBase);//Context ctx = tomcat.addContext("/", new File(".").getAbsolutePath());
		
		tomcat.start();
		tomcat.getServer().await();
	}
}

package net.xby1993.crawler;

public class StartAllJoke {
	public static void main(String[] args) {
		new OSChinaTweetsCrawler().start();
		new QiushibaikeCrawler().start();
		new NeihanshequCrawler().start();
	}
}

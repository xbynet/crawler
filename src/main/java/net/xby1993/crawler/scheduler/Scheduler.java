package net.xby1993.crawler.scheduler;

import net.xby1993.crawler.ISpider;
import net.xby1993.crawler.Request;

public interface Scheduler {
    public void push(Request request,ISpider spider);
    public Request poll(ISpider spider);
    public int getLeftRequestsCount(ISpider spider);
    public int getTotalRequestsCount(ISpider spider);
    public DuplicateRemover getDuplicateRemover();
}

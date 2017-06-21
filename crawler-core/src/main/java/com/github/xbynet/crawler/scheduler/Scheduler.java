package com.github.xbynet.crawler.scheduler;

import com.github.xbynet.crawler.ISpider;
import com.github.xbynet.crawler.Request;

public interface Scheduler {
    public void push(Request request,ISpider spider);
    public Request poll(ISpider spider);
    public int getLeftRequestsCount(ISpider spider);
    public int getTotalRequestsCount(ISpider spider);
    public DuplicateRemover getDuplicateRemover();
}

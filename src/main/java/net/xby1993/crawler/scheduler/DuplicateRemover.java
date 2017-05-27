package net.xby1993.crawler.scheduler;

import net.xby1993.crawler.ISpider;
import net.xby1993.crawler.Request;

public interface DuplicateRemover {
    public boolean isDuplicate(Request request, ISpider spider);
    public void resetDuplicateCheck(ISpider spider);
    public int getTotalRequestsCount(ISpider spider);

}
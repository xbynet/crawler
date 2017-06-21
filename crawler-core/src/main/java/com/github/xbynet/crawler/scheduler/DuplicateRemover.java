package com.github.xbynet.crawler.scheduler;

import com.github.xbynet.crawler.ISpider;
import com.github.xbynet.crawler.Request;

public interface DuplicateRemover {
    public boolean isDuplicate(Request request, ISpider spider);
    public void resetDuplicateCheck(ISpider spider);
    public int getTotalRequestsCount(ISpider spider);

}
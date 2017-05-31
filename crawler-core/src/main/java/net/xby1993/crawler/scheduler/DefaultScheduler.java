package net.xby1993.crawler.scheduler;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import net.xby1993.crawler.Const;
import net.xby1993.crawler.ISpider;
import net.xby1993.crawler.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultScheduler implements Scheduler, DuplicateRemover {
	private final Logger log = LoggerFactory.getLogger(DefaultScheduler.class);
	private Set<String> urls = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private BlockingQueue<Request> queue = new LinkedBlockingQueue<Request>();

	public void push(Request request, ISpider spider) {
		if (Const.HttpMethod.POST == request.getMethod()
				|| !isDuplicate(request, spider)) {
			log.debug("push to queue {}", request.getUrl());
			queue.add(request);
		}
	}

	public Request poll(ISpider spider) {
		return queue.poll();
	}

	public DuplicateRemover getDuplicateRemover(){
		return this;
	}
	public boolean isDuplicate(Request request, ISpider spider) {
		return !urls.add(request.getUrl());
	}

	public void resetDuplicateCheck(ISpider spider) {
		urls.clear();
	}

	public int getTotalRequestsCount(ISpider spider) {
		return urls.size();
	}

	public int getLeftRequestsCount(ISpider spider) {
		return queue.size();
	}
}

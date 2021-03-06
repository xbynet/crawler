package com.github.xbynet.crawler;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xbynet.crawler.http.DefaultDownloader;
import com.github.xbynet.crawler.http.Downloader;
import com.github.xbynet.crawler.http.FileDownloader;
import com.github.xbynet.crawler.http.HttpClientFactory;
import com.github.xbynet.crawler.scheduler.DefaultScheduler;
import com.github.xbynet.crawler.scheduler.Scheduler;
import com.github.xbynet.crawler.utils.CountableThreadPool;
import com.github.xbynet.crawler.utils.CrawlerUtils;

public class Spider implements ISpider, Runnable {
	private static final Logger log=LoggerFactory.getLogger(Spider.class);
	
	private String name;
	private Site site;
	private Scheduler scheduler = new DefaultScheduler();
	private IpProxyProvider ipProvider;
	private HttpClientFactory httpClientFactory = new HttpClientFactory();
	private FileDownloader fileDownloader = null;
	private Downloader defaultDownloader=null;
	private Processor processor;
	private SpiderListener spiderListener;
	/** 是否在任务结束后释放所有资源并终止 */
	private boolean shutdownOnComplete = true;
	/** 空闲等待时长，超过此时长便自动结束爬虫 */
	private int idleWaitTime=1*60*1000;
	private Date startTime;
	private Date endTime;
	private AtomicLong processUrlCount=new AtomicLong(0L);

	private ReentrantLock newUrlLock = new ReentrantLock();

    private Condition newUrlCondition = newUrlLock.newCondition();
    
	public enum Status {
		NotRun, Running, Stopped, Destroyed
	}

	private Status state = Status.NotRun;
	private int threadNum = 1;

	private CountableThreadPool pool;

	private Spider() {
		this.name = "Spider-" + UUID.randomUUID().toString();
		this.fileDownloader = new FileDownloader();
		this.fileDownloader.setSpider(this);
		this.fileDownloader.init();
		this.defaultDownloader=new DefaultDownloader();
		this.defaultDownloader.setSpider(this);
		this.defaultDownloader.init();
	}
	
	
	public static class Builder{
		private Spider spider;
		private Builder(Spider spider1,Processor p){
			this.spider=spider1;
			p.setSpider(spider);
			p.setFileDownloader(spider.fileDownloader);
			this.spider.processor=p;
		}
		
		public Spider build(){
			return spider;
		}
		
		public Builder urls(String... urls){
			for(String url:urls){
				Request req=new Request(url);
				spider.scheduler.push(req, spider);
			}
			return this;
		}
		public Builder requests(Request... requestlist){
			for(Request req:requestlist){
				spider.scheduler.push(req, spider);
			}
			return this;
		}
		public Builder site(Site site) {
			spider.site = site;
			return this;
		}
		public Builder scheduler(Scheduler scheduler) {
			Scheduler old=spider.scheduler;
			spider.scheduler = scheduler;
			Request req=null;
			while((req=old.poll(spider))!=null){
				spider.scheduler.push(req, spider);
			}
			return this;
		}
		public Builder name(String name) {
			spider.name = name;
			return this;
		}
		public Builder ipProvider(IpProxyProvider ipProvider) {
			spider.ipProvider = ipProvider;
			return this;
		}
		public Builder httpClientFactory(HttpClientFactory httpClientFactory) {
			spider.httpClientFactory = httpClientFactory;
			return this;
		}
		public Builder fileDownloader(FileDownloader fileDownloader1) {
			fileDownloader1.setSpider(spider);
			fileDownloader1.init();
			spider.fileDownloader=fileDownloader1;
			return this;
		}
		public Builder listener(SpiderListener spiderListener) {
			spider.spiderListener = spiderListener;
			return this;
		}
		public Builder threadNum(int threadNum) {
			spider.threadNum = threadNum;
			return this;
		}
		public Builder pool(CountableThreadPool pool) {
			spider.pool = pool;
			return this;
		}
		public Builder shutdownOnComplete(boolean shutdownOnComplete) {
			spider.shutdownOnComplete = shutdownOnComplete;
			return this;
		}

		public Builder defaultDownloader(Downloader downloader) {
			downloader.setSpider(spider);
			downloader.init();
			spider.defaultDownloader=downloader;
			return this;
		}

	}
	public static Builder builder(Processor p) {
		return new Builder(new Spider(),p);
	}

	public String getName() {
		return this.name;
	}

	
	public Site getSite() {
		return site;
	}


	public Scheduler getScheduler() {
		return scheduler;
	}


	public IpProxyProvider getIpProvider() {
		return ipProvider;
	}

	public HttpClientFactory getHttpClientFactory() {
		return httpClientFactory;
	}


	public FileDownloader getFileDownloader() {
		return fileDownloader;
	}


	public Processor getProcessor() {
		return processor;
	}

	public SpiderListener getSpiderListener() {
		return spiderListener;
	}

	public int getThreadNum() {
		return threadNum;
	}

	public void run() {
		setStatus(Status.Running);
		init();
		log.debug("Spider "+getName()+" start!");
		System.out.println("--------------------------------------------------------------");
		System.out.println("### 不要问我为什么，你要记住，在你最落寞的时候，有个人对你说过，你可以的！###");
		System.out.println("### 为什么要写爬虫呢？因为我们爬的是寂寞;因为泡妹子需要笑话;因为找工作需要筛选职位;因为老板要求;也许因为要装x才是正解   ###");
		System.out.println("--------------------------------------------------------------");
		while (!Thread.currentThread().isInterrupted() && state==Status.Running) {
			Request request = scheduler.poll(this);
            if (request == null) {
                if (pool.getThreadAlive() == 0) {
                	CrawlerUtils.sleep(idleWaitTime);
                	request = scheduler.poll(this);
                	if(request==null && shutdownOnComplete){
                		break;
                	}
                }
                // wait until new url added
                waitNewUrl();
            } else {
            	final Request tmpReq=request;
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            defaultDownloader.download(tmpReq);
                        } catch (Exception e) {
                            log.error("process request " + tmpReq + " error", e);
                        } finally {
                            processUrlCount.incrementAndGet();
                            signalNewUrl();
                        }
                    }
                });
            }
		}
		setStatus(Status.Stopped);
		if(shutdownOnComplete){
			shutdown();
		}
		
	}
	private void waitNewUrl() {
        newUrlLock.lock();
        try {
            //double check
            if (pool.getThreadAlive() == 0 && shutdownOnComplete) {
                return;
            }
            newUrlCondition.await(idleWaitTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.warn("waitNewUrl - interrupted, error {}", e);
        } finally {
            newUrlLock.unlock();
        }
    }

    private void signalNewUrl() {
        try {
            newUrlLock.lock();
            newUrlCondition.signalAll();
        } finally {
            newUrlLock.unlock();
        }
    }
	public void runAsync() {
		Thread thread = new Thread(this);
		thread.setDaemon(false);
		thread.start();
	}

	public void stop() {
		setStatus(Status.Stopped);
	}

	public synchronized void shutdown() {
		if(state==Status.Destroyed || state==Status.NotRun){
			throw new IllegalStateException("Spider has never start or already destroyed");
		}
		setStatus(Status.Destroyed);
		endTime=new Date();
		if(pool!=null){
			pool.shutdown();
			try {
				pool.awaitTermination(idleWaitTime<60000?60000:idleWaitTime, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				log.warn("thread pool termination interrupted",e);
			}
		}
		closeQuietly(defaultDownloader);
		closeQuietly(fileDownloader);
		closeQuietly(ipProvider);
		closeQuietly(ipProvider);
		
	}
	private void closeQuietly(Closeable clo){
		if(clo!=null){
			try {
				clo.close();
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	protected synchronized void init() {
		if (pool == null) {
			if (state != Status.Destroyed) {
				pool = new CountableThreadPool(threadNum);
			} else {
				throw new IllegalStateException("current spider is destroyed!");
			}
		}
		startTime=new Date();
	}

	public CountableThreadPool getPool() {
		return pool;
	}

	

	public boolean isShutdownOnComplete() {
		return shutdownOnComplete;
	}

	public Status getState() {
		return state;
	}

	private synchronized void setStatus(Status s) {
		state = s;
	}

	public boolean isRunning() {
		return state == Status.Running;
	}

	public boolean isStopped() {
		return state == Status.Stopped;
	}

	public boolean isDestroyed() {
		return state == Status.Destroyed;
	}

	public Date getStartTime() {
		return startTime;
	}

	private void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	private void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Downloader getDefaultDownloader() {
		return defaultDownloader;
	}

	public AtomicLong getProcessUrlCount() {
		return processUrlCount;
	}
	/**
	 * 是否处于空闲状态
	 */
	public boolean isIdle(){
		return pool.getThreadAlive() == 0;
	}
}

package net.xby1993.crawler.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.xby1993.crawler.Const;
import net.xby1993.crawler.ISpider;
import net.xby1993.crawler.Request;
import net.xby1993.crawler.RequestAction;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.protocol.HttpClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.alibaba.fastjson.JSON;
/**
 * Use Redis as url scheduler for distributed crawlers.<br>
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.2.0
 */
public class RedisScheduler  implements Scheduler, DuplicateRemover  {
	private Logger log=LoggerFactory.getLogger(RedisScheduler.class);
	
    protected JedisPool pool;

    private static final String QUEUE_PREFIX = "queue_";

    private static final String SET_PREFIX = "set_";

    private static final String ITEM_PREFIX = "item_";
    
    private ConcurrentHashMap<String,List<Object>> requestDeserialList=new ConcurrentHashMap<String,List<Object>>();

    public RedisScheduler(String host) {
        this(new JedisPool(new JedisPoolConfig(), host));
    }

    public RedisScheduler(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public void resetDuplicateCheck(ISpider spider) {
        Jedis jedis = pool.getResource();
        try {
            jedis.del(getSetKey(spider));
        } finally {
            jedis.close();
        }
    }

    @Override
    public boolean isDuplicate(Request request, ISpider spider) {
        Jedis jedis = pool.getResource();
        try {
            return jedis.sadd(getSetKey(spider), request.getUrl()) > 0;
        } finally {
        	jedis.close();
        }

    }

    @Override
	public void push(Request request, ISpider spider) {
        Jedis jedis = pool.getResource();
        if (Const.HttpMethod.POST == request.getMethod()
				|| !isDuplicate(request, spider)) {
			log.debug("push to queue {}", request.getUrl());
			 try {
		            jedis.rpush(getQueueKey(spider), request.getUrl());
		            String field = DigestUtils.md5Hex(request.getUrl());
		            String value = JSON.toJSONString(request);
		            jedis.hset((ITEM_PREFIX + spider.getName()), field, value);
		            
		            List<Object> slist=new ArrayList<Object>();
		            
		            slist.add(request.getCtx());
		            slist.add(request.getEntity());
		            slist.add(request.getAction());
		            slist.add(request.getExtras());
		            
		            requestDeserialList.put(field,slist);
		        } finally {
		            jedis.close();
		        }
		}
    }

    @Override
    public synchronized Request poll(ISpider spider) {
        Jedis jedis = pool.getResource();
        try {
            String url = jedis.lpop(getQueueKey(spider));
            if (url == null) {
                return null;
            }
            String key = ITEM_PREFIX + spider.getName();
            String field = DigestUtils.md5Hex(url);
            byte[] bytes = jedis.hget(key.getBytes(), field.getBytes());
            if (bytes != null) {
                Request o = JSON.parseObject(new String(bytes), Request.class);
                
                List<Object> slist=requestDeserialList.remove(field);
                
                o.setCtx((HttpClientContext) slist.get(0));
                o.setEntity((HttpEntity) slist.get(1));
                o.setAction((RequestAction) slist.get(2));
                o.setExtras((Map<String, Object>) slist.get(3));
                
                return o;
            }
            Request request = new Request(url);
            return request;
        } finally {
        	jedis.close();
        }
    }

    protected String getSetKey(ISpider spider) {
        return SET_PREFIX + spider.getName();
    }

    protected String getQueueKey(ISpider spider) {
        return QUEUE_PREFIX + spider.getName();
    }

    protected String getItemKey(ISpider spider)
    {
        return ITEM_PREFIX + spider.getName();
    }

    @Override
    public int getLeftRequestsCount(ISpider spider) {
        Jedis jedis = pool.getResource();
        try {
            Long size = jedis.llen(getQueueKey(spider));
            return size.intValue();
        } finally {
            jedis.close();
        }
    }

    @Override
    public int getTotalRequestsCount(ISpider spider) {
        Jedis jedis = pool.getResource();
        try {
            Long size = jedis.scard(getSetKey(spider));
            return size.intValue();
        } finally {
        	jedis.close();
        }
    }


	@Override
	public DuplicateRemover getDuplicateRemover() {
		return this;
	}
}

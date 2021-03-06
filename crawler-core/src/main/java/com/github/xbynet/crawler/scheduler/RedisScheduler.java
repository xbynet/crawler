package com.github.xbynet.crawler.scheduler;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.protocol.HttpClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.alibaba.fastjson.JSON;
import com.github.xbynet.crawler.Const;
import com.github.xbynet.crawler.ISpider;
import com.github.xbynet.crawler.Request;
import com.github.xbynet.crawler.RequestAction;
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
		            byte[] data=SerializationUtils.serialize(request);
		            jedis.hset((ITEM_PREFIX + spider.getName()).getBytes(), field.getBytes(), data);
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
            Request request=SerializationUtils.deserialize(bytes);
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

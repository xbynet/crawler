package com.github.xbynet.crawler.utils;

import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.beans.BeanCopier;

public class BeanUtil {
	public static ConcurrentHashMap<String, BeanCopier> beanCopierMap = new ConcurrentHashMap<String, BeanCopier>();

	public static void copyProperties(Object source, Object target) {
		String beanKey = generateKey(source.getClass(), target.getClass());
		BeanCopier copier = null;
		copier = BeanCopier.create(source.getClass(), target.getClass(), false);
		beanCopierMap.putIfAbsent(beanKey, copier);
		copier = beanCopierMap.get(beanKey);
		copier.copy(source, target, null);
	}

	private static String generateKey(Class<?> class1, Class<?> class2) {
		return class1.toString() + class2.toString();
	}
}

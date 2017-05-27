package net.xby1993.crawler.parser;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

public class JsonPathParser implements Parser {
	private ReadContext ctx;

	public JsonPathParser(String raw) {
		this.ctx = JsonPath.parse(raw);
	}

	public String single(String jsonpath) {
		Object object = ctx.read(jsonpath);
		if (object == null) {
			return null;
		}
		if (object instanceof List) {
			List list = (List) object;
			if (list != null && list.size() > 0) {
				return list.get(0).toString();
			}
		}
		return object.toString();
	}

	public List<String> list(String jsonpath) {
		List<String> reslist = new ArrayList<String>();
		Object object = ctx.read(jsonpath);
		if (object == null) {
			return reslist;
		}
		if (object instanceof List) {
			List list = (List) object;
			for (Object item : list) {
				reslist.add(item.toString());
			}
		} else {
			reslist.add(object.toString());
		}
		return reslist;
	}

	public ReadContext getCtx() {
		return ctx;
	}

}

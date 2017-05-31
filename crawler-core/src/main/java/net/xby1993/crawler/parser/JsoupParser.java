package net.xby1993.crawler.parser;

import java.util.ArrayList;
import java.util.List;

import net.xby1993.crawler.Const;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsoupParser implements Parser {
	private static final Logger log = LoggerFactory
			.getLogger(JsoupParser.class);

	private Document doc;

	public JsoupParser(String raw) {
		doc=Jsoup.parse(raw);
	}

	public String single(String cssSelector) {
		Elements els = getDoc().select(cssSelector);
		if (els == null || els.size() == 0) {
			log.warn("所选元素不存在" + cssSelector);
			return null;
		}
		return getValue(getDoc().select(cssSelector).get(0), null);
	}

	public String single(String cssSelector, String attrName) {
		Elements els = getDoc().select(cssSelector);
		if (els == null || els.size() == 0) {
			log.warn("所选元素不存在" + cssSelector);
			return null;
		}
		return getValue(getDoc().select(cssSelector).get(0), attrName);
	}

	public List<String> list(String cssSelector) {
		List<String> reslist = new ArrayList<String>();
		Elements els = getDoc().select(cssSelector);
		if (els == null || els.size() == 0) {
			log.warn("所选元素不存在" + cssSelector);
			return reslist;
		}
		for (Element e : els) {
			reslist.add(getValue(e, null));
		}
		return reslist;
	}

	public List<String> list(String cssSelector, String attrName) {
		List<String> reslist = new ArrayList<String>();
		Elements els = getDoc().select(cssSelector);
		if (els == null || els.size() == 0) {
			log.warn("所选元素不存在" + cssSelector);
			return reslist;
		}
		for (Element e : els) {
			reslist.add(getValue(e, attrName));
		}
		return reslist;
	}

	private String getValue(Element element, String attrName) {
		if (attrName == null) {
			return element.outerHtml();
		} else if ("innerHtml".equalsIgnoreCase(attrName)) {
			return element.html();
		} else if ("text".equalsIgnoreCase(attrName)) {
			return getText(element);
		} else if ("allText".equalsIgnoreCase(attrName)) {
			return element.text();
		} else {
			return element.attr(attrName);
		}
	}

	protected String getText(Element element) {
		StringBuilder accum = new StringBuilder();
		for (Node node : element.childNodes()) {
			if (node instanceof TextNode) {
				TextNode textNode = (TextNode) node;
				accum.append(textNode.text());
			}
		}
		return accum.toString();
	}

	public Element element(String cssSelector) {
		Elements els = getDoc().select(cssSelector);
		if (els == null || els.size() == 0) {
			log.warn("所选元素不存在" + cssSelector);
			return null;
		}
		return els.get(0);
	}

	public Elements elements(String cssSelector) {
		Elements els = getDoc().select(cssSelector);
		return els;
	}
	public String script(String cssSelector) {
		return single(cssSelector,Const.CssAttr.innerHtml.name());
	}
	public List<String> scripts(String cssSelector) {
		return list(cssSelector,Const.CssAttr.innerHtml.name());
	}

	public Document getDoc() {
		return doc;
	}
}

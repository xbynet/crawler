package com.github.xbynet.crawler.parser;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import us.codecraft.xsoup.XPathEvaluator;
import us.codecraft.xsoup.Xsoup;

public class XpathParser implements Parser{
	
	private Document doc;

    public XpathParser(String raw) {
        this.doc=Jsoup.parse(raw);
    }

    public String single(String xpathStr) {
    	XPathEvaluator xPathEvaluator = Xsoup.compile(xpathStr);
        return xPathEvaluator.evaluate(doc).get();
    }

    public List<String> list(String xpathStr) {
    	XPathEvaluator xPathEvaluator = Xsoup.compile(xpathStr);
        return xPathEvaluator.evaluate(doc).list();
    }

    public Element element(String xpathStr) {
        List<Element> elements = elements(xpathStr);
        if (elements!=null && elements.size()>0){
            return elements.get(0);
        }
        return null;
    }

    public List<Element> elements(String xpathStr) {
    	XPathEvaluator xPathEvaluator = Xsoup.compile(xpathStr);
        return xPathEvaluator.evaluate(doc).getElements();
    }

}

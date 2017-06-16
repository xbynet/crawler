package net.xby1993.crawler.server.monitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import net.xby1993.crawler.Spider;

@WebServlet(
        name = "MonitorServlet",
        urlPatterns = {"/monitor"}
    )
public class MonitorServlet extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String method=req.getParameter("method");
		String name=req.getParameter("name");
		String uri=req.getRequestURI();
		if(StringUtils.isBlank(method)){
			List<Map<String, String>> infolist = new ArrayList<>();
			ConcurrentHashMap<String, Spider> spiders = SpiderManager.get()
					.getSpiders();
			for (String key : spiders.keySet()) {
				Map<String, String> map = new HashMap<>();
				Spider spider = spiders.get(key);
				map.put("name", key);
				map.put("processor", spider.getProcessor().getClass().getName());
				map.put("status", spider.getState().name().toLowerCase());
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date start = spider.getStartTime();
				Date end = spider.getEndTime();
				end = end == null ? new Date() : end;
				long runsecs = start == null ? 0 : (end.getTime() - start
						.getTime()) / 1000;
				map.put("info",
						"开始时间:"
								+ (start == null ? "无" : sdf.format(start))
								+ ",运行时间:"
								+ runsecs
								+ "秒，"
								+ "总请求数:"
								+ spider.getScheduler().getTotalRequestsCount(
										spider)
								+ ",剩余请求数:"
								+ spider.getScheduler().getLeftRequestsCount(
										spider));

				infolist.add(map);
			}
			req.setAttribute("root", req.getServletContext().getContextPath());
			req.setAttribute("spiders", infolist);
			req.getRequestDispatcher("/jsp/spider-list.jsp").forward(req, resp);
		}else if(method.equals("start")){
			outString(resp, String.valueOf(SpiderManager.get().start(name)));
		}else if(method.equals("stop")){
			outString(resp, String.valueOf(SpiderManager.get().stop(name)));
		}
	}
	public void outString(HttpServletResponse resp,String content) throws IOException{
		ServletOutputStream out = resp.getOutputStream();
		out.write(content.getBytes());
		out.flush();
		out.close();
	}
}

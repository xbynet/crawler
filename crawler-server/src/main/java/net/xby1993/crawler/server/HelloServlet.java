package net.xby1993.crawler.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.xby1993.crawler.Spider;
import net.xby1993.crawler.server.demo.GithubCrawler;
import net.xby1993.crawler.server.monitor.SpiderManager;

@WebServlet(
        name = "MyServlet",
        urlPatterns = {"/hello"}
    )
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletOutputStream out = resp.getOutputStream();
        Spider s=new GithubCrawler().createSpider();
        SpiderManager.get().add(s);
        out.write(("add spider of "+s.getName()).getBytes());
        out.flush();
        out.close();
    }

}
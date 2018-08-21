package com.viewol.task;

import com.viewol.crawl.WxCrawler;
import org.springframework.stereotype.Component;

@Component("wxCrawlerJob")
public class WxCrawlerJob {

    public void run(){
        try {
            WxCrawler crawler = new WxCrawler("viewol_data", "D:/out", 5000L);
            crawler.addAccount("中国安防协会");
            crawler.setThreads(1);
            crawler.setResumable(false);
            crawler.start(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

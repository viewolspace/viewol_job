package com.viewol.task;

import com.viewol.crawl.WxCrawler;
import com.viewol.pojo.Info;
import com.viewol.service.IInfoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("wxCrawlerJob")
public class WxCrawlerJob {

    @Resource
    private IInfoService infoService;

    public void run(){
        try {
            List<Info> infoList = new ArrayList<>();

            WxCrawler crawler = new WxCrawler("viewol_data", "/data/out", 5000L, infoList);
//            crawler.setRequester(new MyRequester("", 0, ""));

            crawler.addAccount("中国安防协会");
            crawler.setThreads(1);
            crawler.setResumable(false);
            crawler.start(10);

            if(infoList.size()>0){
                Collections.sort(infoList);//正序比较
                for(Info info : infoList){
                    infoService.save(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

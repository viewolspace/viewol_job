package com.viewol.task;

import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import com.viewol.crawl.WxCrawler;
import com.viewol.pojo.Info;
import com.viewol.service.IInfoService;
import com.youguu.core.util.PropertiesUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Component("wxCrawlerJob")
public class WxCrawlerJob {

    @Resource
    private IInfoService infoService;

    public void run() {
        try {
            List<Info> infoList = new ArrayList<>();
            Properties properties = PropertiesUtil.getProperties("properties/config.properties");
            String filePath = properties.getProperty("info.save.url");
            WxCrawler crawler = new WxCrawler("viewol_data", filePath, 5000L, infoList);
            OkHttpRequester okHttpRequester = new OkHttpRequester();
            crawler.addAccount("中国安防协会");
            crawler.setThreads(1);
            crawler.setResumable(false);
            crawler.start(10);
            crawler.setRequester(okHttpRequester);

            if (infoList.size() > 0) {
                Collections.sort(infoList);//正序比较
                for (Info info : infoList) {
                    infoService.save(1, info);//1-安防展
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WxCrawlerJob job = new WxCrawlerJob();
        job.run();
    }
}

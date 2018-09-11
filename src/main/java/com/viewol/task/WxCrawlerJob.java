package com.viewol.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.viewol.crawl.IpCache;
import com.viewol.crawl.WxCrawler;
import com.viewol.pojo.Info;
import com.viewol.service.IInfoService;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import com.youguu.core.pojo.Response;
import com.youguu.core.util.HttpUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("wxCrawlerJob")
public class WxCrawlerJob {

    private static final Log log = LogFactory.getLog("viewol_crawl");

    @Resource
    private IInfoService infoService;

    public void run(){
        try {
            List<Info> infoList = new ArrayList<>();

            while (true){
                List<com.viewol.crawl.proxy.Proxy> ipList = xiongmaodaili();
                String ip = ipList.get(0).getIp();
                String port = ipList.get(0).getPort();
                WxCrawler crawler = new WxCrawler("viewol_data", "/data/out", 5000L, infoList, ip, port);

                crawler.addAccount("中国安防协会");
                crawler.setThreads(1);
                crawler.setResumable(true);
                crawler.start(10);

                if(infoList.size()>0){
                    Collections.sort(infoList);//正序比较
                    for(Info info : infoList){
                        infoService.save(info);
                    }
                    break;
                }

                Thread.sleep(200);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<com.viewol.crawl.proxy.Proxy> xiongmaodaili(){
        List<com.viewol.crawl.proxy.Proxy> ipList = new ArrayList<>();
        String url = "http://www.xiongmaodaili.com/xiongmao-web/api/glip?secret=678849a490966df97c3915ea15159d33&orderNo=GL20180911140922dNP3VKgg&count=1&isTxt=0&proxyType=1";

        Response<String> response = HttpUtil.sendGet(url, null, "UTF-8");
        if(response!=null && "0000".equals(response.getCode())){
            String json = response.getT();
            JSONObject jsonObject = JSONObject.parseObject(json);
            if("0".equals(jsonObject.getString("code"))){
                JSONArray ipArray = jsonObject.getJSONArray("obj");

                for(int i = 0 ; i < ipArray.size() ; i++){
                    JSONObject ipObj = (JSONObject)ipArray.get(i);
                    com.viewol.crawl.proxy.Proxy proxy = new com.viewol.crawl.proxy.Proxy();
                    proxy.setIp(ipObj.getString("ip"));
                    proxy.setPort(ipObj.getString("port"));
                    log.info(proxy.toString());
                    ipList.add(proxy);

                    IpCache.ipList.add(proxy);
                }
            } else {
                log.info("获取代理IP失败: status:"+jsonObject.getString("status")+", msg:"+jsonObject.getString("msg"));
            }
        } else {
            log.info("获取代理IP失败: msg: "+response.getMsg());
        }

        return ipList;
    }
}

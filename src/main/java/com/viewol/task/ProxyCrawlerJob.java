package com.viewol.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.viewol.crawl.WxCrawler;
import com.viewol.crawl.proxy.MyRequester;
import com.viewol.crawl.proxy.Proxy;
import com.viewol.crawl.proxy.ProxyCrawler;
import com.viewol.pojo.Info;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import com.youguu.core.pojo.Response;
import com.youguu.core.util.HttpUtil;
import com.youguu.core.util.PropertiesUtil;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;

@Component("proxyCrawlerJob")
public class ProxyCrawlerJob {

    private static final Log log = LogFactory.getLog("viewol_crawl");

//    @Resource
//    private IInfoService infoService;

    public void run(){
//        crawlIp();
        List<Proxy> ipList = xiongmaodaili();

        for(int i = 0 ; i < ipList.size(); i++){
            log.info("第"+(i+1)+"次抓取新闻");
            Proxy proxy = ipList.get(i);
            boolean result = crawlInfo(proxy.getIp(), Integer.parseInt(proxy.getPort()), proxy.getMethod());
            if(result){
                break;
            }
        }
    }

    private boolean crawlInfo(String ip, int port, String method){
        try {
            log.info("开始抓取新闻");
            List<Info> infoList = new ArrayList<>();

            WxCrawler crawler = new WxCrawler("viewol_data", "/data/out", 5000L, infoList);
            crawler.setRequester(new MyRequester(ip, port, method));

            crawler.addAccount("中国安防协会");
            crawler.setThreads(1);
            crawler.setResumable(false);
            crawler.start(10);
            log.info("抓取新闻完成");
            if(infoList.size()>0){
                Collections.sort(infoList);//正序比较
                for(Info info : infoList){
//                    infoService.save(info);
                }
                return true;
            }
            log.info("保存新闻完成,新闻数量："+infoList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }



    /**
     * 西刺代理IP抓取
     */
    private void crawlIp(){
        log.info("开始抓取代理IP");
        ProxyCrawler crawler = new ProxyCrawler("proxy_crawl");
        crawler.setThreads(1);
        crawler.setResumable(false);

        try {
            crawler.start(1);
            log.info("抓取代理IP完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Proxy> aliProxyIp(){
        List<Proxy> ipList = new ArrayList<>();
        String aliUrl = "https://jisuproxy.market.alicloudapi.com/proxy/get";

        Map<String, String> headers = new HashMap<>();
        String appcode = null;
        try {
            Properties properties = PropertiesUtil.getProperties("properties/config.properties");
            appcode = properties.getProperty("ali.appCode");
        } catch (IOException e) {
            log.error("aliProxyIp not find file properties/config.properties");
        }
        if(appcode == null || "".equals(appcode)){
            log.error("Ali appCode 为空，请检查配置文件properties/config.properties");
            return ipList;
        }
        headers.put("Authorization", "APPCODE " + appcode);

        Map<String, String> querys = new HashMap<>();
        querys.put("num", "100");//提取数量 默认200 最大2000
        querys.put("protocol", "1");//协议 1 HTTP, 2 HTTPS(也支持HTTP)
        querys.put("type", "1");//类型 1高匿名 2匿名 3透明

//        querys.put("area", "area");
//        querys.put("areaex", "areaex");
//        querys.put("port", "port");
//        querys.put("portex", "portex");

        Response<String> response = HttpUtil.sendGet(aliUrl, querys, headers, "UTF-8");
        if(response!=null && "0000".equals(response.getCode())){
            String json = response.getT();
            JSONObject jsonObject = JSONObject.parseObject(json);
            if("0".equals(jsonObject.getString("status"))){
                JSONObject result = jsonObject.getJSONObject("result");
                int count = result.getInteger("count");
                log.info("本次获取代理IP数量："+count);

                JSONArray ipArray = result.getJSONArray("list");

                for(int i = 0 ; i < ipArray.size() ; i++){
                    JSONObject ipObj = (JSONObject)ipArray.get(i);
                    Proxy proxy = new Proxy();
                    String ipPort = ipObj.getString("ip");
                    proxy.setIp(ipPort.split(":")[0]);
                    proxy.setPort(ipPort.split(":")[1]);
                    proxy.setMethod(ipObj.getString("protocol"));
                    proxy.setAnonymity(ipObj.getString("anonymity"));
                    proxy.setSpeed(ipObj.getString("speed"));
                    proxy.setArea(ipObj.getString("area"));
                    boolean ret = testProxy(proxy);
                    if(ret){
                        ipList.add(proxy);
                    }
                }
            } else {
                log.info("获取代理IP失败: status:"+jsonObject.getString("status")+", msg:"+jsonObject.getString("msg"));
            }
        } else {
            log.info("获取代理IP失败: msg: "+response.getMsg());
        }
        return ipList;
    }

    private List<Proxy> xiongmaodaili(){
        List<Proxy> ipList = new ArrayList<>();
        String url = "http://www.xiongmaodaili.com/xiongmao-web/api/glip?secret=678849a490966df97c3915ea15159d33&orderNo=GL20180911140922dNP3VKgg&count=5&isTxt=0&proxyType=1";

        Response<String> response = HttpUtil.sendGet(url, null, "UTF-8");
        if(response!=null && "0000".equals(response.getCode())){
            String json = response.getT();
            JSONObject jsonObject = JSONObject.parseObject(json);
            if("0".equals(jsonObject.getString("code"))){
                JSONArray ipArray = jsonObject.getJSONArray("obj");

                for(int i = 0 ; i < ipArray.size() ; i++){
                    JSONObject ipObj = (JSONObject)ipArray.get(i);
                    Proxy proxy = new Proxy();
                    proxy.setIp(ipObj.getString("ip"));
                    proxy.setPort(ipObj.getString("port"));
//                    boolean ret = testProxy(proxy);
//                    if(ret){
//                        ipList.add(proxy);
//                    }
                    log.info(proxy.toString());
                    ipList.add(proxy);
                }
            } else {
                log.info("获取代理IP失败: status:"+jsonObject.getString("status")+", msg:"+jsonObject.getString("msg"));
            }
        } else {
            log.info("获取代理IP失败: msg: "+response.getMsg());
        }

        return ipList;
    }


    private static RequestConfig defaultRequestConfig;

    static {
        defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(2000)
                .setConnectTimeout(2000)
                .setConnectionRequestTimeout(2000)
                .build();
    }

    private boolean testProxy(Proxy proxyObj) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet("http://www.baidu.com/");
            HttpHost proxy = new HttpHost(proxyObj.ip, Integer.parseInt(proxyObj.port));
            RequestConfig requestConfig= RequestConfig.copy(defaultRequestConfig).setProxy(proxy).build();
            httpGet.setConfig(requestConfig);
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
            response=httpClient.execute(httpGet);
            return response.getStatusLine().getStatusCode() == 200;
        } catch(SocketTimeoutException e1) {
            return false;
        } catch(ConnectTimeoutException e2) {
            return false;
        } catch(HttpHostConnectException e3) {
            return false;
        } catch(Exception e) {
            return false;
        } finally {
            if(httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    log.info("Failed to close IO, {}", e);
                }
            }
            if(response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    log.info("Failed to close IO, {}", e);
                }
            }
        }
    }

    public static void main(String[] args) {
        ProxyCrawlerJob job = new ProxyCrawlerJob();
        job.run();
    }
}

package com.viewol.crawl.proxy;

import avro.shaded.com.google.common.collect.Lists;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import com.viewol.crawl.IpCache;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.SocketTimeoutException;
import java.util.List;

/**
 * 爬取西刺代理
 */
public class ProxyCrawler extends BreadthCrawler {
    private static final Log log = LogFactory.getLog(ProxyCrawler.class);
    private static RequestConfig defaultRequestConfig;

    static {
        defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(2000)
                .setConnectTimeout(2000)
                .setConnectionRequestTimeout(2000)
                .build();
    }

    public ProxyCrawler(String crawlPath) {
        super(crawlPath, true);
        getConf().setExecuteInterval(5000);
        addSeed("http://www.xicidaili.com/nt");
        addRegex("http://www.xicidaili.com/nt/[1-9]");
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

    @Override
    public void visit(Page page, CrawlDatums crawlDatums) {
        try {
            Thread.sleep(3000L);
        } catch(Exception e) {
            log.info("Failed to sleep");
        }

        log.info("Crawling {}", page.url());
        Document doc = Jsoup.parse(page.html());
        Elements trElements = doc.select("table#ip_list tr");
        List<Proxy> proxyList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(trElements)) {
            for(Element tr : trElements){
                Elements tdElements = tr.select("td");
                if (CollectionUtils.isNotEmpty(tdElements) && tdElements.size() >= 8){
                    String ip = tdElements.get(1).text().trim();
                    String port = tdElements.get(2).text().trim();
                    String method = tdElements.get(5).text().trim();
                    Proxy proxy = new Proxy(ip, port, method);
                    proxyList.add(proxy);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(proxyList)) {
            for(Proxy proxy : proxyList){
                boolean ret = testProxy(proxy);
                log.info("Proxy " + proxy.ip + ":" + proxy.getPort() + " is " + (ret ? "ok" : "bad"));
                if(ret){
                    IpCache.ipList.add(proxy);
                }

            }
            log.info("可用代理：{}", IpCache.ipList);
        }
    }
}

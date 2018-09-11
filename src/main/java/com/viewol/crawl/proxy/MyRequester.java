package com.viewol.crawl.proxy;

import cn.edu.hfut.dmic.webcollector.net.Proxys;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.net.*;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

public class MyRequester extends OkHttpRequester {

    private static final Log log = LogFactory.getLog("viewol_crawl");

    private String ip;
    private int port;
    private String method;

    public MyRequester(String ip, int port, String method) {
        this.ip = ip;
        this.port = port;
        this.method = method;
    }

    @Override
    public OkHttpClient.Builder createOkHttpClientBuilder() {
        return super.createOkHttpClientBuilder()
                .proxySelector(new ProxySelector() {
                    @Override
                    public List<Proxy> select(URI uri) {
                        Proxys proxies = new Proxys();
//                        if(IpCache.ipList.size()>0){
//                            for(com.viewol.crawl.proxy.Proxy p : IpCache.ipList){
//                                proxies.add(p.getIp(), Integer.parseInt(p.getPort()));
//                            }
//                        }
//                        java.net.Proxy randomProxy = proxies.nextRandom();
//                        List<java.net.Proxy> randomProxies = new ArrayList<>();
//                        if (randomProxy != null) {
//                            randomProxies.add(randomProxy);
//                        } else {
//                            randomProxies.add(java.net.Proxy.NO_PROXY);
//                        }

                        List<java.net.Proxy> randomProxies = new ArrayList<>();
                        java.net.Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
                        randomProxies.add(proxy);

                        log.info("Random Proxies:" + randomProxies);
                        return randomProxies;
                    }

                    @Override
                    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                        log.error("代理连接失败");
                    }
                });
    }
}
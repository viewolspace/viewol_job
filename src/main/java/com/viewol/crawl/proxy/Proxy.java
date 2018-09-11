package com.viewol.crawl.proxy;

public class Proxy {
    public String ip;
    public String port;
    public String method;//协议
    public String area;//ip归属地
    public String anonymity;//匿名类型
    public String speed;//响应速度

    public Proxy() {
    }

    public Proxy(String ip, String port, String method) {
        this.ip = ip;
        this.port = port;
        this.method = method;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAnonymity() {
        return anonymity;
    }

    public void setAnonymity(String anonymity) {
        this.anonymity = anonymity;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "Proxy{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", method='" + method + '\'' +
                ", area='" + area + '\'' +
                ", anonymity='" + anonymity + '\'' +
                ", speed='" + speed + '\'' +
                '}';
    }
}
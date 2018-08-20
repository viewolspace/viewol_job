package com.viewol.crawl.pojo;

import java.io.Serializable;

public class CommMsgInfo implements Serializable{

    private static final long serialVersionUID = 8327261273161784601L;

    /**
     * 发布时间，为unix时间戳
     */
    private String datetime;

    /**
     * 公众号独一无二的id
     */
    private String fakeid;

    private String id;

    /**
     * 49:图文，1:文字，3:图片，34:音频，62:视频
     */
    private String type;

    private String content;

    private String status;

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getFakeid() {
        return fakeid;
    }

    public void setFakeid(String fakeid) {
        this.fakeid = fakeid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CommMsgInfo{" +
                "datetime='" + datetime + '\'' +
                ", fakeid='" + fakeid + '\'' +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

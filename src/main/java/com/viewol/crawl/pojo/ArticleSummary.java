package com.viewol.crawl.pojo;

import java.io.Serializable;

/**
 * 一条多图文或单图文消息，通俗说就是一天的群发消息都在这个对象中
 *
 */
public class ArticleSummary implements Serializable{

    private static final long serialVersionUID = 2555742115275768017L;

    /**
     * 图文消息的扩展信息
     */
    private MsgExtInfo app_msg_ext_info;

    /**
     * 图文消息的基本信息
     */
    private CommMsgInfo comm_msg_info;

    public MsgExtInfo getApp_msg_ext_info() {
        return app_msg_ext_info;
    }

    public void setApp_msg_ext_info(MsgExtInfo app_msg_ext_info) {
        this.app_msg_ext_info = app_msg_ext_info;
    }

    public CommMsgInfo getComm_msg_info() {
        return comm_msg_info;
    }

    public void setComm_msg_info(CommMsgInfo comm_msg_info) {
        this.comm_msg_info = comm_msg_info;
    }

    @Override
    public String toString() {
        return "ArticleSummary{" +
                "app_msg_ext_info=" + app_msg_ext_info +
                ", comm_msg_info=" + comm_msg_info +
                '}';
    }
}

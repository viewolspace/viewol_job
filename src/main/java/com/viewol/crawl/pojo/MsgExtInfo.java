package com.viewol.crawl.pojo;

import java.io.Serializable;

public class MsgExtInfo implements Serializable {

    private static final long serialVersionUID = -5657010332956164846L;

    private String del_flag;

    /**
     * 是否原创?
     */
    private String copyright_stat;

    private String play_url;

    /**
     * 作者名
     */
    private String author;

    private String malicious_content_type;

    private String item_show_type;

    /**
     * 子内容标题
     */
    private String title;

    /**
     * 文本内容，针对于type1文字类型
     */
    private String content;

    /**
     * 阅读原文的地址
     */
    private String source_url;

    /**
     * 封面图片url
     */
    private String cover;

    private String duration;

    private String audio_fileid;

    private String subtype;

    /**
     * 摘要
     */
    private String digest;

    private String multi_app_msg_item_list;

    /**
     * 文章来源，针对于type49图文类型，（可以用来去重？）
     */
    private String content_url;

    private String malicious_title_reason_id;

    /**
     * 音频文件id，针对于type34音频类型 （微信定义的一个id，每条文章唯一）？
     */
    private String fileid;

    /**
     * 是否多图文， 1：是； 2：否
     */
    private String is_multi;

    public String getDel_flag() {
        return del_flag;
    }

    public void setDel_flag(String del_flag) {
        this.del_flag = del_flag;
    }

    public String getCopyright_stat() {
        return copyright_stat;
    }

    public void setCopyright_stat(String copyright_stat) {
        this.copyright_stat = copyright_stat;
    }

    public String getPlay_url() {
        return play_url;
    }

    public void setPlay_url(String play_url) {
        this.play_url = play_url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMalicious_content_type() {
        return malicious_content_type;
    }

    public void setMalicious_content_type(String malicious_content_type) {
        this.malicious_content_type = malicious_content_type;
    }

    public String getItem_show_type() {
        return item_show_type;
    }

    public void setItem_show_type(String item_show_type) {
        this.item_show_type = item_show_type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSource_url() {
        return source_url;
    }

    public void setSource_url(String source_url) {
        this.source_url = source_url;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getAudio_fileid() {
        return audio_fileid;
    }

    public void setAudio_fileid(String audio_fileid) {
        this.audio_fileid = audio_fileid;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getMulti_app_msg_item_list() {
        return multi_app_msg_item_list;
    }

    public void setMulti_app_msg_item_list(String multi_app_msg_item_list) {
        this.multi_app_msg_item_list = multi_app_msg_item_list;
    }

    public String getContent_url() {
        return content_url;
    }

    public void setContent_url(String content_url) {
        this.content_url = content_url;
    }

    public String getMalicious_title_reason_id() {
        return malicious_title_reason_id;
    }

    public void setMalicious_title_reason_id(String malicious_title_reason_id) {
        this.malicious_title_reason_id = malicious_title_reason_id;
    }

    public String getFileid() {
        return fileid;
    }

    public void setFileid(String fileid) {
        this.fileid = fileid;
    }

    public String getIs_multi() {
        return is_multi;
    }

    public void setIs_multi(String is_multi) {
        this.is_multi = is_multi;
    }

    @Override
    public String toString() {
        return "MsgExtInfo{" +
                "del_flag='" + del_flag + '\'' +
                ", copyright_stat='" + copyright_stat + '\'' +
                ", play_url='" + play_url + '\'' +
                ", author='" + author + '\'' +
                ", malicious_content_type='" + malicious_content_type + '\'' +
                ", item_show_type='" + item_show_type + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", source_url='" + source_url + '\'' +
                ", cover='" + cover + '\'' +
                ", duration='" + duration + '\'' +
                ", audio_fileid='" + audio_fileid + '\'' +
                ", subtype='" + subtype + '\'' +
                ", digest='" + digest + '\'' +
                ", multi_app_msg_item_list='" + multi_app_msg_item_list + '\'' +
                ", content_url='" + content_url + '\'' +
                ", malicious_title_reason_id='" + malicious_title_reason_id + '\'' +
                ", fileid='" + fileid + '\'' +
                ", is_multi='" + is_multi + '\'' +
                '}';
    }
}

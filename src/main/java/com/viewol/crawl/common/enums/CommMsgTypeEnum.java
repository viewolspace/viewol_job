package com.viewol.crawl.common.enums;

public enum CommMsgTypeEnum {

    ARTICLE_WITH_IMG("49", "图文"),
    ARTICLE("1", "文字"),
    IMAGE("3", "图片"),
    AUDIO("34", "音频"),
    VIDEO("62", "视频"),
    ;

    private String code;

    private String name;

    CommMsgTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

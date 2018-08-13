package com.viewol.task;

import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;

/**
 * 定时推送：定时给报名参加活动的用户推送消息提醒。活动开始前5分钟推送
 */
public class TimingPushTask {
    private static final Log logger = LogFactory.getLog("viewol_job");

    public void timePush() {
        logger.info("推送任务启动");


        logger.info("推送完成");
    }
}

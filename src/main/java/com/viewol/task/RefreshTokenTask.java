package com.viewol.task;

import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;

/**
 * 刷新token任务，目前不适用定时刷新，viewol_service和微信交互时检测token是否失效，如果失效刷新token并更新数据库
 * 如果使用定时刷新，viewol_service的WxMpInMysqlConfigStorage和WxMaInMysqlConfig两个类需要重写getToken()方法
 */
public class RefreshTokenTask {
    private static final Log logger = LogFactory.getLog("viewol_job");

    public void refresh(){
        logger.info("启动刷新token任务");

        logger.info("刷新token任务完成");
    }
}

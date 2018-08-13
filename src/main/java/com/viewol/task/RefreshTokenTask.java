package com.viewol.task;

import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;

public class RefreshTokenTask {
    private static final Log logger = LogFactory.getLog("viewol_job");

    public void refresh(){
        logger.info("启动刷新token任务");

        logger.info("刷新token任务完成");
    }
}

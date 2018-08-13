package com.viewol.main;

import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class BootStrap {
    private static final Log logger = LogFactory.getLog("viewol_job");
    private static ApplicationContext springTask;

    public static void main(String[] args) {
        if(args.length==0) {
            logger.info("加载定时任务");
            springTask = new FileSystemXmlApplicationContext("classpath:spring/job.xml");
            logger.info("加载定时任务完成");
        }
    }
}

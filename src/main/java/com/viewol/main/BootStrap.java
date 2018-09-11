package com.viewol.main;

import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BootStrap {
    private static final Log logger = LogFactory.getLog("viewol_job");

    public static void main(String[] args) {
        if (args.length == 0) {
            logger.info("加载定时任务");
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/job.xml");
            logger.info("加载定时任务完成");
        }

        /*logger.info("启动jetty");
        Server server = new Server(9999);

        ServletContextHandler context = new ServletContextHandler(server, "/");
        server.setHandler(context);

        context.addServlet(MpServlet.class,"/mpAuth");

        try {
            server.start();
            logger.info("启动jetty成功");
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Jetty启动异常");
        }*/

    }
}

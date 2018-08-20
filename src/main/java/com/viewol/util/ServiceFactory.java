package com.viewol.util;

import com.viewol.base.ContextLoader;
import com.viewol.service.IInfoService;
import com.viewol.service.IScheduleService;
import com.viewol.service.IWxService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ServiceFactory {

    private static IScheduleService scheduleService = null;
    private static IWxService wxService = null;
    private static IInfoService infoService = null;

    public static synchronized IScheduleService getScheduleService() {
        if (scheduleService == null) {
            try {
                scheduleService = new AnnotationConfigApplicationContext(ContextLoader.class).getBean("scheduleService", IScheduleService.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return scheduleService;
    }

    public static synchronized IWxService getWxService() {
        if (wxService == null) {
            try {
                wxService = new AnnotationConfigApplicationContext(ContextLoader.class).getBean("wxService", IWxService.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return wxService;
    }

    public static synchronized IInfoService getInfoService() {
        if (infoService == null) {
            try {
                infoService = new AnnotationConfigApplicationContext(ContextLoader.class).getBean("infoService", IInfoService.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return infoService;
    }
}

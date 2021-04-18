package com.viewol.sys.service;

import com.viewol.sys.base.ContextLoader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SysServiceFactory {

    private static SysUserRoleService sysUserRoleService = null;
    private static SysUserService sysUserService = null;


    public static synchronized SysUserRoleService getSysUserRoleService() {
        if (sysUserRoleService == null) {
            try {
                sysUserRoleService = new AnnotationConfigApplicationContext(
                        ContextLoader.class).getBean("sysUserRoleService", SysUserRoleService.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sysUserRoleService;
    }

    public static synchronized SysUserService getSysUserService() {
        if (sysUserService == null) {
            try {
                sysUserService = new AnnotationConfigApplicationContext(ContextLoader.class).getBean("sysUserService", SysUserService.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sysUserService;
    }
}

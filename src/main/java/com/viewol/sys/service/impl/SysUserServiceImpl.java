package com.viewol.sys.service.impl;

import com.viewol.sys.dao.SysUserDAO;
import com.viewol.sys.pojo.SysUser;
import com.viewol.sys.service.SysUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by leo on 2017/11/23.
 */
@Service("sysUserService")
public class SysUserServiceImpl implements SysUserService {

    @Resource
    private SysUserDAO sysUserDAO;

    @Override
    public int saveSysUser(SysUser sysUser) {
        return sysUserDAO.saveSysUser(sysUser);
    }

    @Override
    public int updateSysUser(SysUser sysUser) {
        return sysUserDAO.updateSysUser(sysUser);
    }

    @Override
    public int deleteSysUser(int id) {
        return sysUserDAO.deleteSysUser(id);
    }


    @Override
    public SysUser findSysUserByUserName(String username) {
        SysUser sysUser = sysUserDAO.findSysUserByUserName(username);
        return sysUser;
    }

    @Override
    public SysUser findSysUserByCompanyId(int companyId) {
        SysUser sysUser = sysUserDAO.findSysUserByCompanyId(companyId);
        return sysUser;
    }
}

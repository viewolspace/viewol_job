package com.viewol.sys.service;

import com.viewol.sys.pojo.SysUser;
import com.youguu.core.util.PageHolder;

import java.util.Date;

public interface SysUserService {

	public int saveSysUser(SysUser sysUser);

	public int updateSysUser(SysUser sysUser);

	public int deleteSysUser(int id);

	public SysUser findSysUserByUserName(String username);

	public SysUser findSysUserByCompanyId(int companyId);

}

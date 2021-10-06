package com.viewol.task;

import com.alibaba.fastjson.JSON;
import com.viewol.pojo.CfpaCompany;
import com.viewol.pojo.CfpaProduct;
import com.viewol.pojo.Company;
import com.viewol.pojo.Product;
import com.viewol.service.CfpaService;
import com.viewol.service.ICompanyService;
import com.viewol.service.IProductService;
import com.viewol.sys.pojo.SysUser;
import com.viewol.sys.pojo.SysUserRole;
import com.viewol.sys.service.SysUserRoleService;
import com.viewol.sys.service.SysUserService;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import com.youguu.core.util.MD5;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 通过接口方式获取展商，展品信息
 */
@Component("cfpaCompanyTask")
public class CfpaCompanyTask {
    private static final Log logger = LogFactory.getLog("viewol_job");

    @Resource
    private ICompanyService companyService;
    @Resource
    private IProductService productService;
    @Resource
    private CfpaService cfpaService;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private SysUserRoleService sysUserRoleService;

    public void execute() {
        logger.info("开始更新展商，展品数据");
        List<CfpaCompany> list = cfpaService.queryAllCfpaCompany();
        logger.info("共计展商{}个", list.size());
        for (CfpaCompany cfpaCompany : list) {
            logger.info("统一信用代码：{}", cfpaCompany.getTyshxydm());
            //查询本地表里是否有该展商数据，有则更新，无则插入。
            if (StringUtils.isEmpty(cfpaCompany.getTyshxydm())) {
                logger.error("统一信用代码为空：名称={}, 代码={}", cfpaCompany.getZwgsmc(), cfpaCompany.getTyshxydm());
                continue;
            }
            Company company = companyService.getCompanyByUserNum(cfpaCompany.getTyshxydm());
            int companyId = 0;
            CfpaCompany fCompany = cfpaService.getCfpaCompany(cfpaCompany.getTyshxydm());

            try {
                if (company == null) {
                    company = new Company();
                    company.setName(fCompany.getZwgsmc());
                    company.setShortName(fCompany.getZwgsmc());
                    company.setLogo("/" + fCompany.getQyjsSrc());
                    company.setContent(fCompany.getQyjj());
                    company.setProductNum(100);
                    company.setCanApply(1);
                    company.setPlace(fCompany.getZwh());
                    company.setPlaceSvg(fCompany.getZwh());
                    company.setcTime(new Date());
                    company.setUserNum(fCompany.getTyshxydm());

                    List<String> cList = new ArrayList<>();
                    cList.add("00010009");

                    cfpaService.downloadImg(fCompany.getQyjsSrc());
                    logger.info("新增展商："+ JSON.toJSONString(company));
                    companyId = companyService.addCompany(2, company, cList);
                    logger.info("添加展商数据insert完成，companyId={}", companyId);
                    /**
                     * 注册用户
                     */
                    SysUser sysUser = new SysUser();
                    sysUser.setPswd(new MD5().getMD5ofStr("123456").toLowerCase());
                    sysUser.setUserName(company.getName());
                    sysUser.setRealName(company.getName());
                    sysUser.setCompanyId(companyId);
                    sysUser.setUserStatus(1);
                    sysUser.setCreateTime(new Date());
                    sysUser.setEmail("");
                    sysUser.setPhone("");
                    sysUserService.saveSysUser(sysUser);
                    logger.info("saveSysUser完成，companyId={}", companyId);

                    /**
                     * 给用户分配权限
                     */
                    SysUserRole sysUserRole = new SysUserRole();
                    sysUserRole.setRid(8);
                    sysUserRole.setUid(sysUser.getId());
                    sysUserRole.setCreateTime(new Date());
                    sysUserRoleService.saveSysUserRole(sysUserRole);
                    logger.info("saveSysUserRole完成，companyId={}", companyId);
                } else {
                    //如果展商存在，先更新SysUser表登录账户，再更新展商表数据。
                    try {
                        SysUser sysUser = sysUserService.findSysUserByCompanyId(company.getId());

                        sysUser.setUserName(fCompany.getZwgsmc());
                        sysUser.setRealName(fCompany.getZwgsmc());
                        sysUserService.updateSysUser(sysUser);
                        logger.info("updateSysUser完成，companyId={}", companyId);

                        company.setName(fCompany.getZwgsmc());
                        company.setShortName(fCompany.getZwgsmc());
                        company.setLogo("/" + fCompany.getQyjsSrc());
                        if(StringUtils.isEmpty(company.getContent())){
                            company.setContent(fCompany.getQyjj());
                        }
                        company.setPlace(fCompany.getZwh());
                        company.setPlaceSvg(fCompany.getZwh());
                        cfpaService.downloadImg(fCompany.getQyjsSrc());

                        logger.info("更新展商："+ JSON.toJSONString(company));
                        companyService.updateByUserNum(company);
                        logger.info("updateByUserNum完成，companyId={}", companyId);

                        companyId = company.getId();
                    } catch (Exception e) {
                        logger.error("定时任务更新异常", e);
                    }

                }
            } catch (Exception e) {
                logger.error("定时任务更新异常", e);
            }

            //统一信用代码（唯一标识）
            String tyshxydm = cfpaCompany.getTyshxydm();
            List<CfpaProduct> cfpaProductList = cfpaService.getCfpaProduct(tyshxydm);
            for (CfpaProduct cfpaProduct : cfpaProductList) {
                Product product = new Product();
                product.setName(cfpaProduct.getCplxmc());
                product.setCompanyId(companyId);
                product.setContent(cfpaProduct.getCpjj());
                product.setImage("/" + cfpaProduct.getSrc());
                product.setCategoryId("00020009");
                product.setStatus(0);//0上架
                product.setcTime(new Date());
                product.setUuid(cfpaProduct.getUuid());
                cfpaService.downloadImg(cfpaProduct.getSrc());

                try {
                    Product dbProduct = productService.selectByUuid(cfpaProduct.getUuid());
                    if (null == dbProduct) {
                        productService.addProduct(2, product);
                        logger.info("addProduct完成，companyId={}, productId={}", companyId, cfpaProduct.getUuid());
                    } else {
                        logger.info("数据库中已存在该产品，跳过插入操作，companyId={}, productId={}", companyId, cfpaProduct.getUuid());
                    }

                } catch (Exception e) {
                    logger.error("定时任务更新产品异常", e);
                }

            }
        }
        logger.info("更新展商，展品数据结束");

    }
}

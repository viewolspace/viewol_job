package com.viewol.task;

import com.viewol.pojo.CfpaCompany;
import com.viewol.pojo.CfpaProduct;
import com.viewol.pojo.Company;
import com.viewol.pojo.Product;
import com.viewol.service.CfpaService;
import com.viewol.service.ICompanyService;
import com.viewol.service.IProductService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 通过接口方式获取展商，展品信息
 */
@Component("cfpaCompanyTask")
public class CfpaCompanyTask {

    @Resource
    private ICompanyService companyService;
    @Resource
    private IProductService productService;
    @Resource
    private CfpaService cfpaService;

    public void execute() {
        List<CfpaCompany> list = cfpaService.queryAllCfpaCompany();
        for (CfpaCompany cfpaCompany : list) {
            Company company = new Company();
            company.setName(cfpaCompany.getZwgsmc());
            company.setShortName(cfpaCompany.getZwgsmc());
            company.setLogo("/" + cfpaCompany.getSrc());
            company.setContent(cfpaCompany.getQyjj());
            company.setProductNum(100);
            company.setCanApply(1);
            company.setPlace(cfpaCompany.getZwh());
            company.setPlaceSvg(cfpaCompany.getZwh());
            company.setcTime(new Date());

            List<String> cList = new ArrayList<>();
            cList.add("00010009");
            int companyId = companyService.addCompany(2, company, cList);

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

                productService.addProduct(2, product);
            }
        }
    }
}

package com.viewol.task;

import com.alibaba.fastjson.JSONObject;
import com.viewol.pojo.Company;
import com.viewol.pojo.Product;
import com.viewol.service.ICompanyService;
import com.viewol.service.IInfoService;
import com.viewol.service.IProductService;
import com.viewol.vo.CompanyListResponse;
import com.viewol.vo.CompanyResponse;
import com.viewol.vo.ProductResponse;
import com.youguu.core.pojo.Response;
import com.youguu.core.util.HttpUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component("fireexpoTask")
public class FireexpoTask {

    @Resource
    private ICompanyService companyService;
    @Resource
    private IProductService productService;

    public static final String LOGO_URL_PREFIX = "http://61.161.226.197:8090/upload/";
    //展商列表
    public static final String COMPANY_LIST_URL = "http://61.161.226.197:8090//portalservice/zwjbxx/doFindQyZwNumDesc?callback=callback&zgList=&pageNum=1&pageSize=1000&gsmc=";
    //展商首页（包含展商和展品列表信息）
    public static final String COMPANY_INDEX = "http://61.161.226.197:8090/portalservice/qyjbxx/findQyjbjsVO?callback=callback&qyid=";
    //展品首页
    public static final String PRODUCT_INDEX = "http://61.161.226.197:8090/portalservice/qycpjs/doFindById?callback=callback&uuid=";

    public static final Set<String> productSet = new HashSet<>();
    public void execute() {
        //1、抓展商列表
        Response<String> response = HttpUtil.sendGet(COMPANY_LIST_URL, null, "UTF-8");
        String data = response.getT().substring(9, response.getT().length() - 1);
//        System.out.println(data);

        CompanyListResponse companyListResponse = JSONObject.parseObject(data, CompanyListResponse.class);
//        System.out.println(companyListResponse.getResult().getList().size());
        //2、遍历展商列表
        for (CompanyListResponse.ResultBean.ListBean bean : companyListResponse.getResult().getList()) {

            //3、抓展商主页+展品列表
            String qyid = bean.getQyid();//展商ID
            Response<String> companyRes = HttpUtil.sendGet(COMPANY_INDEX + qyid, null, "UTF-8");
            String json = companyRes.getT().substring(9, companyRes.getT().length() - 1);
            CompanyResponse companyResponse = JSONObject.parseObject(json, CompanyResponse.class);

            String gsmc = companyResponse.getResult().getZwgsmc();//公司名称
            String wz = companyResponse.getResult().getWz();//公司官网URL
            String qyjsSrc = companyResponse.getResult().getQyjsSrc();//公司LOGO
            String qyjj = companyResponse.getResult().getQyjj();//公司介绍

            Company company = new Company();
            company.setName(gsmc);
            company.setShortName(gsmc);
            company.setLogo(qyjsSrc);
            company.setContent(qyjj);
            company.setProductNum(100);
            company.setCanApply(1);
            company.setcTime(new Date());

            int companyId = companyService.addCompany(2, company, new ArrayList<String>());
            //4、遍历展品列表
            for (CompanyResponse.ResultBean.QycpjsVOsBean vOsBean : companyResponse.getResult().getQycpjsVOs()) {

                //5、抓取展品主页信息
                String uuid = vOsBean.getUuid();//展品ID
                Response<String> productRes = HttpUtil.sendGet(PRODUCT_INDEX + uuid, null, "UTF-8");
                String proJson = productRes.getT().substring(9, productRes.getT().length() - 1);
                ProductResponse productResponse = JSONObject.parseObject(proJson, ProductResponse.class);

                String cpjj = productResponse.getResult().getCpjj();//展品简介
                String cplxmc = productResponse.getResult().getCplxmc();//展品分类
                String src = productResponse.getResult().getSrc();//展品logo
                String zwgsmc = productResponse.getResult().getZwgsmc();//公司名称

                productSet.add(cplxmc);

                Product product = new Product();
                product.setCompanyId(companyId);
                product.setContent(cpjj);
                product.setImage(src);
                product.setCategoryId("9999");
                product.setStatus(0);//0上架
                product.setcTime(new Date());

                productService.addProduct(2, product);
//                System.out.println(proJson);
            }
//            System.out.println("--------------------");
        }

        System.out.println(JSONObject.toJSON(productSet));
    }
}

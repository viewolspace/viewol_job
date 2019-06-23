package com.viewol.task;

import com.alibaba.fastjson.JSONObject;
import com.viewol.vo.CompanyListResponse;
import com.viewol.vo.CompanyResponse;
import com.viewol.vo.ProductResponse;
import com.youguu.core.pojo.Response;
import com.youguu.core.util.HttpUtil;

public class FireexpoTask {

    //展商列表
    public static final String COMPANY_LIST_URL = "http://61.161.226.197:8090//portalservice/zwjbxx/doFindQyZwNumDesc?callback=callback&zgList=&pageNum=1&pageSize=1000&gsmc=";
    //展商首页（包含展商和展品列表信息）
    public static final String COMPANY_INDEX = "http://61.161.226.197:8090/portalservice/qyjbxx/findQyjbjsVO?callback=callback&qyid=";
    //展品首页
    public static final String PRODUCT_INDEX = "http://61.161.226.197:8090/portalservice/qycpjs/doFindById?callback=callback&uuid=";

    public static void main(String[] args) {
        //1、抓展商列表
        Response<String> response = HttpUtil.sendGet(COMPANY_LIST_URL, null, "UTF-8");
        String data = response.getT().substring(9, response.getT().length() - 1);
        System.out.println(data);

        CompanyListResponse companyListResponse = JSONObject.parseObject(data, CompanyListResponse.class);
        System.out.println(companyListResponse.getResult().getList().size());
        //2、遍历展商列表
        for (CompanyListResponse.ResultBean.ListBean bean : companyListResponse.getResult().getList()) {

            //3、抓展商主页+展品列表
            String qyid = bean.getQyid();//展商ID
            Response<String> companyRes = HttpUtil.sendGet(COMPANY_INDEX + qyid, null, "UTF-8");
            String json = companyRes.getT().substring(9, companyRes.getT().length() - 1);
            CompanyResponse companyResponse = JSONObject.parseObject(json, CompanyResponse.class);
            //4、遍历展品列表
            for (CompanyResponse.ResultBean.QycpjsVOsBean vOsBean : companyResponse.getResult().getQycpjsVOs()) {

                //5、抓取展品主页信息
                String uuid = vOsBean.getUuid();//展品ID
                Response<String> productRes = HttpUtil.sendGet(PRODUCT_INDEX + uuid, null, "UTF-8");
                String proJson = productRes.getT().substring(9, productRes.getT().length() - 1);
                ProductResponse productResponse = JSONObject.parseObject(proJson, ProductResponse.class);

                String cpjj = productResponse.getResult().getCpjj();//展品简介
                String cplxmc = productResponse.getResult().getCplxmc();//分类
                String src = productResponse.getResult().getSrc();//展品logo

                System.out.println(proJson);
            }
            System.out.println("--------------------");
        }

    }
}

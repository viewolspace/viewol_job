package com.viewol.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 访问地址：http://localhost:9999/mpAuth
 * 生产环境地址：http://47.93.25.129:9999/mpAuth
 */
public class MpServlet extends HttpServlet {

    public static final String URL = "http://mp.weixin.qq.com/profile?src=3&timestamp=1535508000&ver=1&signature=9AV2eAovXTrBvX85o*GkyJXuA2nRWBhn5VMC9eAcw-J5SDWupZ-YpYRx*dZjeJMmzhpC1woJ28Wb8wPnZkhyqg==";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=utf-8");

        resp.sendRedirect(URL);
    }

}
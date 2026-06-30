package com.nongxinle.filter;

import org.apache.shiro.session.Session;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 跨域过滤器
 *
 * @author Lining
 * @date 2017/12/12
 */
public class CORSFilter implements Filter {

    private List<Session> sessions = new ArrayList<>();


    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
//        System.out.println(req.get);
        System.out.println("CrosFilter de difang:------------------------------");


        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers",
                "Authorization, Origin, X-Requested-With, Content-Type, Accept, Access-Token, X-Platform-Market-Token");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        // 设置连接超时时间（10分钟 = 600秒 = 600000毫秒）
        // 注意：这个设置主要用于提示客户端，实际的超时时间主要由Tomcat的server.xml配置控制
        response.setHeader("Keep-Alive", "timeout=600");
        // 设置连接超时时间（单位：秒）
        if (request.getProtocol().startsWith("HTTP/1.1")) {
            response.setHeader("Connection", "keep-alive");
        }

        chain.doFilter(req, res);
    }


    @Override
    public void init(FilterConfig filterConfig) {
    }


    @Override
    public void destroy() {
    }

}

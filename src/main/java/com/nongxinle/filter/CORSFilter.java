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

        response.setHeader("Access-Control-Allow-Origin", request.getHeader("origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Authorization,Origin, X-Requested-With, Content-Type, Accept,Access-Token");


        chain.doFilter(req, res);
    }


    @Override
    public void init(FilterConfig filterConfig) {
    }


    @Override
    public void destroy() {
    }

}

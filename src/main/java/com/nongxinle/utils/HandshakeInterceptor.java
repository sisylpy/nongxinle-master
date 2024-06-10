package com.nongxinle.utils;


import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

public class HandshakeInterceptor extends HttpSessionHandshakeInterceptor {
//    private static final Logger logger = LoggerFactory.getLogger(HandshakeInterceptor.class);

    //会话开始
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//        attributes.put(Constant.WEBSOCKET_USERNAME,"LSH");
        System.out.println("dfadbeieieieiie");
        //  从session中获取用户
        /*if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
            HttpSession session = serverHttpRequest.getServletRequest().getSession(false);
            if (session!=null) {
                String username = (String) session.getAttribute(Constant.SESSION_USERNAME);
                attributes.put(Constant.WEBSOCKET_USERNAME,username);
            }
        }*/
        return true;
    }

    //会话结束
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
        System.out.println("After Handshake");
        super.afterHandshake(request, response, wsHandler, ex);
    }
}
package com.nongxinle.utils;
//import com.beesgame.sanguo.constant.Constant;
import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxCommunityUserEntity;
import com.nongxinle.service.NxCommunityService;
import com.nongxinle.service.NxCommunityUserService;
import com.nongxinle.service.NxRestrauntService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hackway on
 * Date: 2016/1/15.
 * Time:11:15
 */
public class WebSocketEndPoint extends TextWebSocketHandler {

    private static final ArrayList<WebSocketSession> users;
    private static final Logger logger;
    private static final List<Map<String,Object>> nxSocketUsers;

    @Autowired
    NxCommunityService nxCommunityService;
    @Autowired
    NxCommunityUserService nxCommunityUserService;
    static {
        users = new ArrayList<WebSocketSession>();
        nxSocketUsers = new ArrayList<>();
        logger = LoggerFactory.getLogger(WebSocketEndPoint.class);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        TextMessage returnMessage = new TextMessage(message.getPayload()+" received at server");
        session.sendMessage(returnMessage);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("connect to the websocket success......");
        String url = session.getUri().toString();
        String[] split = url.split("=");
        String userId = split[1];
        if(userId != null){
            for (Map<String,Object> map: nxSocketUsers) {
                String userId1 = (String)map.get("userId");
                if (userId.equals(userId1)){
                    nxSocketUsers.remove(map);
                }
            }

            NxCommunityUserEntity userEntity = nxCommunityUserService.queryObject(Integer.valueOf(userId));
            Integer communityId = userEntity.getNxCouCommunityId();
            NxCommunityEntity communityEntity = nxCommunityService.queryObject(communityId);
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("session", session);
            map.put("comId", communityId.toString() );
            nxSocketUsers.add(map);
            
            String userName = communityEntity.getNxCommunityName();
//            if (userName!=null) {
//                session.sendMessage(new TextMessage("用户："+userName));
//            }
        }


    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

        if (session.isOpen()) {
            session.close();
        }
        logger.info("websocket transport error, connection closed......");
        for (Map<String,Object> user :nxSocketUsers) {
            WebSocketSession session1 = (WebSocketSession)user.get("session");
            if (session1.equals(session)) {
                nxSocketUsers.remove(user);
                return;
            }
        }
//        users.remove(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("websocket connection closed......");
        for (Map<String,Object> user :nxSocketUsers) {
            WebSocketSession session1 = (WebSocketSession)user.get("session");
            if (session1.equals(session)) {
                nxSocketUsers.remove(user);
                return;
            }
        }
//        users.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return super.supportsPartialMessages();
    }

    // 发送信息给特定用户(LSH)
    public void sendMessageToUser(String username, TextMessage message) {
        for (WebSocketSession user:users) {
            if ("sisy".equals(username)) {
                try {
                    if (user.isOpen()) {
                        user.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
    // 发送信息给特定用户(LSH)
    public void sendWarnToComAllUser(String comId , String orderId) {
        for (Map<String,Object> user :nxSocketUsers) {
            String comId1 = (String)user.get("comId");
            if (comId1.equals(comId)) {
                try {
                    if (((WebSocketSession) user.get("session")).isOpen()) {
//                        ((WebSocketSession) user.get("session")).sendMessage(new TextMessage("有订单了"));
                        ((WebSocketSession) user.get("session")).sendMessage(new TextMessage(orderId));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                break;
            }
        }
    }


    //发送信息给所用用户，广播
    public void sendMessageToAllUser(TextMessage message) {
        for (WebSocketSession user:users) {
            try {
                if (user.isOpen()) {
                    user.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
package com.nongxinle.entity;

import java.io.Serializable;

/**
 * 企业微信会话消息实体
 * 
 * @author lpy
 * @date 2024-01-01
 */
public class QyWxMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息ID
     */
    private String msgid;
    
    /**
     * 动作 send/recall
     */
    private String action;
    
    /**
     * 发送者
     */
    private String from;
    
    /**
     * 群ID
     */
    private String roomid;
    
    /**
     * 消息时间戳
     */
    private Long msgtime;
    
    /**
     * 消息类型
     */
    private String msgtype;
    
    /**
     * 消息内容（JSON字符串）
     */
    private String content;

    /**
     * 设置：消息ID
     */
    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }
    
    /**
     * 获取：消息ID
     */
    public String getMsgid() {
        return msgid;
    }

    /**
     * 设置：动作
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    /**
     * 获取：动作
     */
    public String getAction() {
        return action;
    }

    /**
     * 设置：发送者
     */
    public void setFrom(String from) {
        this.from = from;
    }
    
    /**
     * 获取：发送者
     */
    public String getFrom() {
        return from;
    }

    /**
     * 设置：群ID
     */
    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }
    
    /**
     * 获取：群ID
     */
    public String getRoomid() {
        return roomid;
    }

    /**
     * 设置：消息时间戳
     */
    public void setMsgtime(Long msgtime) {
        this.msgtime = msgtime;
    }
    
    /**
     * 获取：消息时间戳
     */
    public Long getMsgtime() {
        return msgtime;
    }

    /**
     * 设置：消息类型
     */
    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }
    
    /**
     * 获取：消息类型
     */
    public String getMsgtype() {
        return msgtype;
    }

    /**
     * 设置：消息内容
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * 获取：消息内容
     */
    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "QyWxMessage{" +
                "msgid='" + msgid + '\'' +
                ", action='" + action + '\'' +
                ", from='" + from + '\'' +
                ", roomid='" + roomid + '\'' +
                ", msgtime=" + msgtime +
                ", msgtype='" + msgtype + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}

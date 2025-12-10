package com.nongxinle.dto;

import lombok.Data;

/**
 * 第一个回合请求参数
 */
@Data
public class Round1Request {
    private String raw_text;        // 订单原始文本，如"进口羊奶粉 1 瓶"
    private String distributor_id;  // 配送商ID
    private String order_id;        // 订单ID（可选，用于串联会话）
}


package com.nongxinle.community.yunguotuan.service;

import com.nongxinle.community.yunguotuan.entity.YgtChatMessageEntity;

import java.util.List;
import java.util.Map;

public interface YgtMessageService {
    List<YgtChatMessageEntity> queryMessages(Map<String, Object> params);

    Map<String, Object> parseMessage(Long messageId);
}

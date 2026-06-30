package com.nongxinle.community.yunguotuan.service.impl;

import com.nongxinle.dao.YgtChatMessageDao;
import com.nongxinle.community.yunguotuan.entity.YgtChatMessageEntity;
import com.nongxinle.community.yunguotuan.entity.YgtOrderCandidateEntity;
import com.nongxinle.community.yunguotuan.service.YgtMessageService;
import com.nongxinle.community.yunguotuan.service.YgtOrderCandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YgtMessageServiceImpl implements YgtMessageService {
    @Autowired
    private YgtChatMessageDao ygtChatMessageDao;

    @Autowired
    private YgtOrderCandidateService ygtOrderCandidateService;

    @Override
    public List<YgtChatMessageEntity> queryMessages(Map<String, Object> params) {
        return ygtChatMessageDao.queryList(params);
    }

    @Override
    public Map<String, Object> parseMessage(Long messageId) {
        YgtChatMessageEntity message = ygtChatMessageDao.queryObject(messageId);
        if (message == null) {
            throw new IllegalArgumentException("消息不存在");
        }
        if (!"text".equals(message.getYgtCmMsgType()) || isBlank(message.getYgtCmContent())) {
            message.setYgtCmParseStatus("IGNORED");
            message.setYgtCmParseError("非文本消息或内容为空");
            ygtChatMessageDao.updateParseStatus(message);
            Map<String, Object> ignored = new HashMap<>();
            ignored.put("status", "IGNORED");
            return ignored;
        }

        YgtOrderCandidateEntity candidate = ygtOrderCandidateService.createPlaceholderCandidate(message);
        message.setYgtCmParseStatus("CANDIDATE_CREATED");
        message.setYgtCmParseError(null);
        ygtChatMessageDao.updateParseStatus(message);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "CANDIDATE_CREATED");
        result.put("candidateId", candidate.getYgtOrderCandidateId());
        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

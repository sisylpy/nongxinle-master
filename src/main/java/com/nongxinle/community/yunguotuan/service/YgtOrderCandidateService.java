package com.nongxinle.community.yunguotuan.service;

import com.nongxinle.community.yunguotuan.entity.YgtChatMessageEntity;
import com.nongxinle.community.yunguotuan.entity.YgtOrderCandidateEntity;

import java.util.List;
import java.util.Map;

public interface YgtOrderCandidateService {
    YgtOrderCandidateEntity createPlaceholderCandidate(YgtChatMessageEntity message);

    List<YgtOrderCandidateEntity> queryCandidates(Map<String, Object> params);

    Map<String, Object> candidateDetail(Long id);

    Map<String, Object> editCandidate(Long id, Map<String, Object> body);

    Map<String, Object> ignoreCandidate(Long id, Map<String, Object> body);

    Map<String, Object> restoreCandidate(Long id);

    Map<String, Object> confirmCandidate(Long id, Map<String, Object> body);
}

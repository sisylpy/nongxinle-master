package com.nongxinle.community.yunguotuan.service;

import java.util.List;
import java.util.Map;

public interface YgtCampaignService {
    Map<String, Object> createCampaign(Map<String, Object> body);

    Map<String, Object> addGoods(Long campaignId, Map<String, Object> body);

    Map<String, Object> openCampaign(Long campaignId);

    Map<String, Object> closeCampaign(Long campaignId);

    List<Map<String, Object>> currentCampaigns(Long groupId, String corpId);

    List<Map<String, Object>> listCampaigns(Map<String, Object> params);

    Map<String, Object> campaignSummary(Long campaignId);

    Map<String, Object> createTestMessageForCandidate(Long groupId, String content);
}

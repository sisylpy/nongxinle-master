package com.nongxinle.dao;

import com.nongxinle.community.yunguotuan.entity.YgtGroupOrderEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface YgtGroupOrderDao extends BaseDao<YgtGroupOrderEntity> {
    YgtGroupOrderEntity queryByCandidateId(@Param("candidateId") Long candidateId);

    Map<String, Object> queryCampaignOrderSummary(@Param("campaignId") Long campaignId);
}

package com.nongxinle.dao;

import com.nongxinle.community.yunguotuan.entity.YgtOrderCandidateEntity;
import org.apache.ibatis.annotations.Param;

public interface YgtOrderCandidateDao extends BaseDao<YgtOrderCandidateEntity> {
    YgtOrderCandidateEntity queryObjectForUpdate(@Param("id") Long id);

    YgtOrderCandidateEntity queryByMessageId(@Param("messageId") Long messageId);

    int countByCampaignAndStatus(@Param("campaignId") Long campaignId, @Param("status") String status);
}

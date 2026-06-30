package com.nongxinle.dao;

import com.nongxinle.community.yunguotuan.entity.YgtGroupBuyCampaignEntity;
import org.apache.ibatis.annotations.Param;

public interface YgtGroupBuyCampaignDao extends BaseDao<YgtGroupBuyCampaignEntity> {
    YgtGroupBuyCampaignEntity queryActiveByGroupId(@Param("groupId") Long groupId);

    YgtGroupBuyCampaignEntity queryOpenByGroupId(@Param("groupId") Long groupId);
}

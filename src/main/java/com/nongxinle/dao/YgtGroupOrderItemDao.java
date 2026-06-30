package com.nongxinle.dao;

import com.nongxinle.community.yunguotuan.entity.YgtGroupOrderItemEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface YgtGroupOrderItemDao extends BaseDao<YgtGroupOrderItemEntity> {
    List<YgtGroupOrderItemEntity> queryByOrderId(@Param("orderId") Long orderId);

    List<Map<String, Object>> queryCampaignGoodsSummary(@Param("campaignId") Long campaignId);
}

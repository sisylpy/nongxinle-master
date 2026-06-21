package com.nongxinle.dao;

import com.nongxinle.entity.PlatformCouponTemplateEntity;

import java.util.List;
import java.util.Map;

public interface PlatformCouponTemplateDao {

    PlatformCouponTemplateEntity queryObject(Integer pctId);

    PlatformCouponTemplateEntity queryByMarketAndId(Map<String, Object> map);

    List<PlatformCouponTemplateEntity> queryList(Map<String, Object> map);

    int save(PlatformCouponTemplateEntity entity);

    int update(PlatformCouponTemplateEntity entity);

    int incrementIssueCount(Map<String, Object> map);
}

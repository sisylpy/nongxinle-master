package com.nongxinle.dao;

import com.nongxinle.entity.PlatformStoreCouponEntity;

import java.util.List;
import java.util.Map;

public interface PlatformStoreCouponDao {

    PlatformStoreCouponEntity queryObject(Integer pscId);

    PlatformStoreCouponEntity queryByMarketAndId(Map<String, Object> map);

    List<PlatformStoreCouponEntity> queryList(Map<String, Object> map);

    int save(PlatformStoreCouponEntity entity);

    int update(PlatformStoreCouponEntity entity);
}

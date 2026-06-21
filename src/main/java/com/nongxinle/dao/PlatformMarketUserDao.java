package com.nongxinle.dao;

import com.nongxinle.entity.PlatformMarketUserEntity;

import java.util.Map;

public interface PlatformMarketUserDao {

    PlatformMarketUserEntity queryObject(Integer pmuId);

    PlatformMarketUserEntity queryByMarketAndLoginAccount(Map<String, Object> map);

    int countByMarketId(Integer marketId);

    int save(PlatformMarketUserEntity entity);

    int update(PlatformMarketUserEntity entity);
}

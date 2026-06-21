package com.nongxinle.dao;

import com.nongxinle.entity.PlatformMarketUserSessionEntity;

public interface PlatformMarketUserSessionDao {

    PlatformMarketUserSessionEntity queryByToken(String token);

    int save(PlatformMarketUserSessionEntity entity);

    int deleteByToken(String token);

    int deleteByPmuId(Integer pmuId);
}

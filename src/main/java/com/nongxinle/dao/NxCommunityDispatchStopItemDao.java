package com.nongxinle.dao;

import com.nongxinle.entity.NxCommunityDispatchStopItemEntity;

import java.util.List;

public interface NxCommunityDispatchStopItemDao extends BaseDao<NxCommunityDispatchStopItemEntity> {

    List<NxCommunityDispatchStopItemEntity> queryByStopId(Integer stopId);

    int deleteByStopId(Integer stopId);
}

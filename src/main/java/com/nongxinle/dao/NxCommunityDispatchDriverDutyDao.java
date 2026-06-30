package com.nongxinle.dao;

import com.nongxinle.entity.NxCommunityDispatchDriverDutyEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityDispatchDriverDutyDao extends BaseDao<NxCommunityDispatchDriverDutyEntity> {

    List<NxCommunityDispatchDriverDutyEntity> queryOnDutyDrivers(Map<String, Object> map);
}

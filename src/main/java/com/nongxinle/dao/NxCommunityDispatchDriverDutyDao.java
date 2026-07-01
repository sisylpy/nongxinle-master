package com.nongxinle.dao;

import com.nongxinle.entity.NxCommunityDispatchDriverDutyEntity;
import com.nongxinle.entity.NxCommunityUserEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityDispatchDriverDutyDao extends BaseDao<NxCommunityDispatchDriverDutyEntity> {

    NxCommunityDispatchDriverDutyEntity queryByCommunityDriverDate(Map<String, Object> map);

    List<NxCommunityDispatchDriverDutyEntity> queryByCommunityDate(Map<String, Object> map);

    List<NxCommunityUserEntity> queryOnDutyDriverUsers(Map<String, Object> map);

    void upsertCheckIn(NxCommunityDispatchDriverDutyEntity entity);

    void upsertCheckOut(NxCommunityDispatchDriverDutyEntity entity);
}

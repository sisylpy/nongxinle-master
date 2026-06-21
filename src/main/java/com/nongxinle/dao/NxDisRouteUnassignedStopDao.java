package com.nongxinle.dao;

import com.nongxinle.entity.NxDisRouteUnassignedStopEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NxDisRouteUnassignedStopDao {

    List<NxDisRouteUnassignedStopEntity> queryByPlanId(Integer planId);

    void save(NxDisRouteUnassignedStopEntity entity);

    void deleteByPlanId(Integer planId);
}

package com.nongxinle.dao;

import com.nongxinle.entity.NxDisRouteStopOrderEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NxDisRouteStopOrderDao {

    List<NxDisRouteStopOrderEntity> queryByStopId(Integer stopId);

    List<NxDisRouteStopOrderEntity> queryByStopIds(@Param("stopIds") List<Integer> stopIds);

    void save(NxDisRouteStopOrderEntity entity);

    void deleteByStopIds(@Param("stopIds") List<Integer> stopIds);
}

package com.nongxinle.dao;

import com.nongxinle.entity.NxDisSandboxDayTimeWindowEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NxDisSandboxDayTimeWindowDao {

    NxDisSandboxDayTimeWindowEntity queryByDisRouteDep(@Param("disId") Integer disId,
                                                       @Param("routeDate") String routeDate,
                                                       @Param("depFatherId") Integer depFatherId);

    List<NxDisSandboxDayTimeWindowEntity> queryByDisRouteDate(@Param("disId") Integer disId,
                                                              @Param("routeDate") String routeDate);

    void upsert(NxDisSandboxDayTimeWindowEntity entity);

    /** 本趟送达/回沙盘后作废当日门店时间窗调整。 */
    int deleteByDisRouteDep(@Param("disId") Integer disId,
                            @Param("routeDate") String routeDate,
                            @Param("depFatherId") Integer depFatherId);
}

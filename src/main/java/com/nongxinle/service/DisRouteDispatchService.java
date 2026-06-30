package com.nongxinle.service;

import com.nongxinle.entity.NxDisRoutePlanEntity;
import com.nongxinle.entity.NxDistributerUserEntity;

import java.util.List;

public interface DisRouteDispatchService {

    NxDisRoutePlanEntity getPlan(Integer planId);

    List<NxDistributerUserEntity> listDrivers(Integer disId);
}

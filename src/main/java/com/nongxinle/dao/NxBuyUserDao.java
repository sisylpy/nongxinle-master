package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-29 10:22
 */

import com.nongxinle.entity.NxBuyUserEntity;
import com.nongxinle.entity.NxDistributerPurchaseBatchEntity;
import com.nongxinle.entity.NxRestrauntUserEntity;

import java.util.List;
import java.util.Map;


public interface NxBuyUserDao extends BaseDao<NxBuyUserEntity> {


    List<NxDistributerPurchaseBatchEntity> queryBuyerPurchaseBatchDayWork(Map<String, Object> mapZero);

    List<NxBuyUserEntity> queryAllNxBuyerUsers();
}

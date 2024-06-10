package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 11-30 15:31
 */

import com.nongxinle.entity.NxRestrauntEntity;

import java.util.List;


public interface NxRestrauntDao extends BaseDao<NxRestrauntEntity> {

    NxRestrauntEntity queryResInfo(Integer restaurantId);

    List<NxRestrauntEntity> queryPrepareDeliveryRestraunts(Integer comId);

    List<NxRestrauntEntity> queryDriverRestraunts(Integer userId);

    List<NxRestrauntEntity> queryChainRestrauntsByResId(Integer resId);
}

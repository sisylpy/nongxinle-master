package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-24 15:30:57
 */

import com.nongxinle.entity.NxStandardEntity;

import java.util.List;


public interface NxStandardDao extends BaseDao<NxStandardEntity> {


    List<NxStandardEntity> queryGoodsStandardListByGoodsId(Integer nxGoodsId);
}

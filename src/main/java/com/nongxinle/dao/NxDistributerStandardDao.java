package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 07-27 21:41
 */

import com.nongxinle.entity.NxDistributerStandardEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerStandardDao extends BaseDao<NxDistributerStandardEntity> {

    List<NxDistributerStandardEntity> queryDisStandardByDisGoodsId(Integer nxDdgDisGoodsId);

    List<NxDistributerStandardEntity> queryDisStandardByParams(Map<String, Object> map);
}

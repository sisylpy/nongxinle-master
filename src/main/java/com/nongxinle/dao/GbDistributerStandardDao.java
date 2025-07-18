package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import com.nongxinle.entity.GbDistributerStandardEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerStandardDao extends BaseDao<GbDistributerStandardEntity> {

    List<GbDistributerStandardEntity> queryDisStandardByDisGoodsIdGb(Integer disGoodsId);

    List<GbDistributerStandardEntity> queryDisStandardByParams(Map<String, Object> mapNx);
}

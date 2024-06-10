package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 06-18 21:32
 */

import com.nongxinle.entity.GbDistributerStandardEntity;

import java.util.List;


public interface GbDistributerStandardDao extends BaseDao<GbDistributerStandardEntity> {

    List<GbDistributerStandardEntity> queryDisStandardByDisGoodsIdGb(Integer disGoodsId);
}

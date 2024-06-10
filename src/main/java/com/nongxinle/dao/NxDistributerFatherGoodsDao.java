package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 07-27 17:38
 */

import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerFatherGoodsDao extends BaseDao<NxDistributerFatherGoodsEntity> {

    List<NxDistributerFatherGoodsEntity> queryDisGoodsCata(Integer disId);

    List<NxDistributerFatherGoodsEntity> queryHasDisFathersFather(Map<String, Object> map2);

    List<NxDistributerFatherGoodsEntity> queryDisFathersGoodsByParams(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryDisAll(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryDisGoodsCataWithGoods(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryDisGreatGrandList(Integer disId);

    List<NxDistributerFatherGoodsEntity> queryDisFathersGoodsByNxGoodsId(Integer nxGoodsId);

    Integer queryMaxIdForNow();

    NxDistributerFatherGoodsEntity queryLevelOneByNxGoodsId(Map<String, Object> mapG);

    List<NxDistributerFatherGoodsEntity> queryDisGoodsCataLinshi(Integer nxDistributerId);

    List<NxDistributerFatherGoodsEntity> queryFatherGoodsWithDisGoods(Map<String, Object> map1);


//    List<NxDistributerFatherGoodsEntity> queryHasGreatGrandGoods(Map<String, Object> map3);

}

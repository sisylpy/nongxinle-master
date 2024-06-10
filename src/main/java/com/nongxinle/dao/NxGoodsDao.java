package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import com.nongxinle.entity.NxGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxGoodsDao extends BaseDao<NxGoodsEntity> {

    List<NxGoodsEntity> queryQuickSearchNxGoods(Map<String, Object> map);

    List<NxGoodsEntity> queryQuickSearchNxCategoryGoods(Map<String, Object> map);

    List<NxGoodsEntity> queryNxGoodsByParams(Map<String, Object> map);


    List<NxGoodsEntity> getNxGoodsCateList();

    List<NxGoodsEntity> getNxFatherGoodsByFatherId(Integer fatherId);

    List<NxGoodsEntity> queryListWithFatherId(Map<String, Object> map);

    int queryTotalByFatherId(Integer fatherId);

    List<NxGoodsEntity> querySubNameByFatherId(Integer nxGoodsId);

    List<NxGoodsEntity> queryGoodsCataByType(Integer type);


    List<NxGoodsEntity> queryGoodsTree();


    List<NxGoodsEntity> queryIfHasSameGoods(Map<String, Object> map);


    List<NxGoodsEntity> queryNxGoodsOrderByGoodsId(Map<String, Object> map);

    List<NxGoodsEntity> queryQuickSearchNxCategoryGoodsWithNxDis(Map<String, Object> map);

    List<NxGoodsEntity> queryNxFatherGoods();

    List<NxGoodsEntity> queryQuickSearchAllGoodsWithNxDis(Map<String, Object> map);

    List<NxGoodsEntity> queryQuickSearchFatherGoods(Map<String, Object> map);

    int querySecondLevelMaxId();




//    List<NxGoodsEntity> queryCataNxDistribterWithPeisong(Map<String, Object> map);
}

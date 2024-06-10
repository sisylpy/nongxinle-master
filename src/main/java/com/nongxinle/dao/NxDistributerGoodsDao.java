package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 07-27 17:38
 */

import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerGoodsDao extends BaseDao<NxDistributerGoodsEntity> {

    List<NxDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> map);

    int queryDisGoodsTotal(Map<String, Object> map3);

    NxDistributerGoodsEntity queryDisGoodsDetail(Integer disGoodsId);

    List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStr(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisGoodsByNxGoodsId(Integer nxSGoodsId);

    List<NxDistributerGoodsEntity> queryDgSubNameByFatherId(Integer nxDistributerFatherGoodsId);

    List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStrWithDepOrders(Map<String, Object> map);

    List<NxDistributerEntity> queryMarketDistributerByNxGoodsId(Integer nxGoodsId);

    List<NxDistributerGoodsEntity> queryDisPurGoodsQuickSearchStr(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStrByFatherId(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryListGoodsAll();

    List<NxDistributerGoodsEntity> queryLinshiGoods(Integer disId);

    List<NxDistributerGoodsEntity> queryIfHasSameDisGoods(Map<String, Object> mapS);

    List<NxDistributerGoodsEntity>  queryDisGoodsByName(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisGoodsByNamePinyin(Map<String, Object> map);


    List<NxDistributerGoodsEntity> queryNxDepDisGrandGoodsByGreatId(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryNxDisGrandGoodsWithGbGoodsByGreatId(Map<String, Object> map);

//    List<NxDistributerGoodsEntity> queryDisGoodsWithGbGoodsByParams(Map<String, Object> map1);
//    List<NxDistributerGoodsEntity> queryAddDistributerNxGoods(Map<String, Object> map);
//List<NxGoodsEntity> queryDisGoodsGrandList(Map<String, Object> map);
//Integer querySubAmount(Integer nxGoodsId);
//List<NxDistributerGoodsEntity> queryDisGoodsFatherList(Map<String, Object> map);
//NxDistributerGoodsEntity queryDisGoodsWithStandards(Integer nxDdgDisGoodsId);
//List<NxDistributerGoodsEntity> queryIfHasDisGoods(Map<String, Object> map1);
//List<NxDistributerGoodsEntity> queryIfFatherHasOtherDisGoods(Integer nxDgDfgGoodsFatherId);
//List<NxDistributerFatherGoodsEntity> queryFatherDisGoodsByParams(Map<String, Object> map1);
//List<NxDistributerGoodsEntity> queryNxDisGrandGoodsByGreatId(Map<String, Object> map);
//List<NxDistributerGoodsEntity> querySelfAddGoods(Integer disId);
//List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStrForAdd(Map<String, Object> mapS);
//List<NxDistributerGoodsEntity> depQueryDisGoodsWithOrdersByFatherId(Map<String, Object> map);

    NxDistributerGoodsEntity queryOneGoodsAboutNxGoods(Map<String, Object> mapDepGoods);

    List<NxDistributerFatherGoodsEntity> querySupplierGrand(Map<String, Object> map);

    List<NxDistributerGoodsEntity> querySupplierGoodsByGreatId(Map<String, Object> map);

    List<NxGoodsEntity> querySupplierFather(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryAllLinshiGoods();

    List<NxDistributerGoodsEntity> querySupplierGoodsByFatherId(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryNxDepDisGrandGoodsByGreatIdAll(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryGbDisGrandGoodsByGreatId(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisGoodsByAlias(Map<String, Object> mapA);

    NxDistributerGoodsEntity queryDisGoodsDetailWithLinshi(Integer doDisGoodsId);

}

package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 07-27 17:38
 */

import com.nongxinle.entity.*;

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

    List<NxDistributerGoodsEntity> queryNxDepDisGrandGoodsByGreatIdSunHola(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryNxDisGrandGoodsWithGbGoodsByGreatId(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisGoodsWithGbGoodsByParams(Map<String, Object> map1);
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

    List<NxDistributerGoodsEntity> queryDisShelfGoodsQuickSearchStr(Map<String, Object> map);

    List<NxDistributerGoodsShelfGoodsEntity>  queryDisShelfGoods(Map<String, Object> map);

    List<NxDistributerGoodsShelfGoodsEntity> queryDisShelfGoodsWithNxGoodsId(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisLinshiGoodsQuickSearchStr(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisNxGoodsQuickSearchStrByGrandId(Map<String, Object> map);

    List<NxDistributerEntity> queryYishangByGoods(Map<String, Object> map);

    List<NxDistributerEntity> queryLinshiGoodsForNx();

    int queryLinshiGoodsAcount(Integer nxDistributerId);

    List<NxDistributerGoodsEntity> queryGbDisDisGrandGoodsByGreatId(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStrWithGbDepOrders(Map<String, Object> map);

    int queryNxGoodsSonsSortByParams(Map<String, Object> mapF);

    List<NxDistributerGoodsEntity> queryDisGoodsByLikeName(Map<String, Object> mapOne);

    NxDistributerGoodsEntity querySameGoodsWithOrders(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisGoodsByNameLikePinyin(Map<String, Object> mapTwo);

    List<NxDistributerGoodsEntity> queryDisGoodsByAliasLike(Map<String, Object> mapA);

    List<Integer> queryOnlyDepGoodsIds(Map<String, Object> map);

    List<Integer> queryOnlyDepGoodsIdsWithPurchaseType(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisUnshelfGoodsWithPage(Map<String, Object> map);

    int queryDisUnshelfGoodsTotal(Map<String, Object> map);

    List<String> queryDisGoodsBrand();

    List<NxDistributerGoodsEntity> queryUnShelfDisGoodsQuickSearchStr(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryUnShelfDisGoodsQuickSearchStrWithNxGoodsId(Map<String, Object> map);
}

package com.nongxinle.service;

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

public interface NxDistributerGoodsService {


	List<NxDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> map);

	int queryDisGoodsTotal(Map<String, Object> map3);

	NxDistributerGoodsEntity queryObject(Integer nxDistributerGoodsId);
	
	List<NxDistributerGoodsEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerGoodsEntity nxDistributerGoods);
	
	void update(NxDistributerGoodsEntity nxDistributerGoods);

	int delete(Integer nxDistributerGoodsId);
	
	void deleteBatch(Integer[] nxDistributerGoodsIds);

    NxDistributerGoodsEntity queryDisGoodsDetail(Integer disGoodsId);

    List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStr(Map<String, Object> map);

    List<NxDistributerGoodsEntity> querydisGoodsByNxGoodsId(Integer nxSGoodsId);

    List<NxDistributerGoodsEntity> queryDgSubNameByFatherId(Integer nxDistributerFatherGoodsId);

    List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStrWithDepOrders(Map<String, Object> map);

	List<NxDistributerEntity> queryMarketDistributerByNxGoodsId(Integer nxGoodsId);

	List<NxDistributerGoodsEntity> queryDisPurGoodsQuickSearchStr(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStrByFatherId(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryListGoodsAll();

    List<NxDistributerGoodsEntity> queryLinshiGoods(Integer disId);

    List<NxDistributerGoodsEntity> queryNxDepDisGrandGoodsByGreatId(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryIfHasSameDisGoods(Map<String, Object> mapS);

    List<NxDistributerGoodsEntity>  queryDisGoodsByName(Map<String, Object> map);

    List<NxDistributerGoodsEntity> queryDisGoodsByNamePinyin(Map<String, Object> map);

    List<NxDistributerFatherGoodsEntity> queryNxDisGrandGoodsWithGbGoodsByGreatId(Map<String, Object> map);

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

package com.nongxinle.service;

/**
 *
 *
 * @author lpy
 * @date 07-30 23:58
 */

import com.nongxinle.entity.*;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface NxDepartmentDisGoodsService {

	List<NxDepartmentEntity> queryDepartmentsByDisGoodsId(Integer disGoodsId);

	List<NxDistributerFatherGoodsEntity> depGetDepDisGoodsCata(Integer depId);

	List<NxDepartmentDisGoodsEntity> queryDepGoodsByFatherId(Map<String, Object> map);

	List<NxDepartmentDisGoodsEntity> queryAddDisDepGoods(Map<String, Object> map);

	int queryDepGoodsTotal(Map<String, Object> map3);

	TreeSet<NxDepartmentDisGoodsEntity> queryDepDisGoodsQuickSearchStr(Map<String, Object> map);

	void save(NxDepartmentDisGoodsEntity nxDepartmentDisGoods);

	void update(NxDepartmentDisGoodsEntity nxDepartmentDisGoods);

	NxDepartmentDisGoodsEntity queryObject(Integer nxDepartmentDisGoodsId);

    List<NxDepartmentDisGoodsEntity> queryDepDisSearchPinyin(Map<String, Object> map);

	void delete(Integer nxDepartmentDisGoodsId);

    List<NxDepartmentDisGoodsEntity> queryDepDisGoodsByParams(Map<String, Object> map);

	List<NxDistributerFatherGoodsEntity> disGetDepDisGoodsCata(Integer depFatherId);


//	/////


//	List<NxDistributerGoodsEntity> queryIfHasDisGoods(Map<String, Object> map);

//
//	List<NxDepartmentDisGoodsEntity> queryList(Map<String, Object> map);
//
//	int queryTotal(Map<String, Object> map);
//
//
//
//
	void deleteBatch(Integer[] nxDepartmentDisGoodsIds);


	List<NxDistributerFatherGoodsEntity> depQueryDepGoodsWithOrder(Map<String, Object> map);

	NxDepartmentEntity depFatherGetSubDepsGoods(Map<String, Object> map);

    List<NxDepartmentDisGoodsEntity> depGetDepsGoods(Map<String, Object> map);

	List<GbDepartmentEntity> queryGbDepartmentsByDisGoodsId(Integer disGoodsId);

    List<NxDistributerFatherGoodsEntity> queryDepDisGoodsWithOrders(Integer depFatherId);

    NxDepartmentDisGoodsEntity queryDepartmentGoods(Map<String, Object> mapDep);
}

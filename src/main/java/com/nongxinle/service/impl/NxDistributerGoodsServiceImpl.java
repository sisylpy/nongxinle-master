package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import com.nongxinle.service.NxDepartmentStandardService;
import com.nongxinle.service.NxDistributerStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerGoodsDao;
import com.nongxinle.service.NxDistributerGoodsService;



@Service("nxDistributerGoodsService")
public class NxDistributerGoodsServiceImpl implements NxDistributerGoodsService {
	@Autowired
	private NxDistributerGoodsDao nxDistributerGoodsDao;


	@Override
	public List<NxDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> map) {
		return nxDistributerGoodsDao.queryDisGoodsByParams(map);
	}

	@Override
	public int queryDisGoodsTotal(Map<String, Object> map3) {
		return nxDistributerGoodsDao.queryDisGoodsTotal(map3);
	}





//	//////////////////////




    @Override
	public NxDistributerGoodsEntity queryObject(Integer nxDistributerGoodsId){
		return nxDistributerGoodsDao.queryObject(nxDistributerGoodsId);
	}
	
	@Override
	public List<NxDistributerGoodsEntity> queryList(Map<String, Object> map){
		return nxDistributerGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerGoodsEntity nxDistributerGoods){
		nxDistributerGoodsDao.save(nxDistributerGoods);
	}
	
	@Override
	public void update(NxDistributerGoodsEntity nxDistributerGoods){
		nxDistributerGoodsDao.update(nxDistributerGoods);
	}
	
	@Override
	public int delete(Integer nxDistributerGoodsId){
		int delete = nxDistributerGoodsDao.delete(nxDistributerGoodsId);
		return delete;
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerGoodsIds){
		nxDistributerGoodsDao.deleteBatch(nxDistributerGoodsIds);
	}




	@Override
	public NxDistributerGoodsEntity queryDisGoodsDetail(Integer disGoodsId) {
		return nxDistributerGoodsDao.queryDisGoodsDetail(disGoodsId);
	}



    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStr(Map<String, Object> map) {
		return nxDistributerGoodsDao.queryDisGoodsQuickSearchStr(map);
    }


    @Override
    public List<NxDistributerGoodsEntity> querydisGoodsByNxGoodsId(Integer nxSGoodsId) {

		return nxDistributerGoodsDao.queryDisGoodsByNxGoodsId(nxSGoodsId);
    }
//
    @Override
    public List<NxDistributerGoodsEntity> queryDgSubNameByFatherId(Integer nxDistributerFatherGoodsId) {

		return nxDistributerGoodsDao.queryDgSubNameByFatherId(nxDistributerFatherGoodsId);
    }



    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStrWithDepOrders(Map<String, Object> map) {
        return nxDistributerGoodsDao.queryDisGoodsQuickSearchStrWithDepOrders(map);
    }

    @Override
    public List<NxDistributerEntity> queryMarketDistributerByNxGoodsId(Integer nxGoodsId) {

		return nxDistributerGoodsDao.queryMarketDistributerByNxGoodsId(nxGoodsId);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisPurGoodsQuickSearchStr(Map<String, Object> map) {
		return nxDistributerGoodsDao.queryDisPurGoodsQuickSearchStr(map);
    }

//    @Override
//    public NxDistributerGoodsEntity queryNxDisGoodsByNxGoodsId(Map<String, Object> map) {
//		return nxDistributerGoodsDao.queryNxDisGoodsByNxGoodsId(map);
//    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStrByFatherId(Map<String, Object> map) {
		return nxDistributerGoodsDao.queryDisGoodsQuickSearchStrByFatherId(map);
    }



//    @Override
//    public List<NxDistributerGoodsEntity> queryNxDisGrandGoodsByGreatId(Map<String, Object> map) {
//
//		return nxDistributerGoodsDao.queryNxDisGrandGoodsByGreatId(map);
//    }

    @Override
    public List<NxDistributerGoodsEntity> queryListGoodsAll() {
		return nxDistributerGoodsDao.queryListGoodsAll();
    }


    @Override
    public List<NxDistributerGoodsEntity> queryLinshiGoods(Integer disId) {
		return nxDistributerGoodsDao.queryLinshiGoods(disId);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryNxDepDisGrandGoodsByGreatId(Map<String, Object> map) {
		return nxDistributerGoodsDao.queryNxDepDisGrandGoodsByGreatId(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryNxDepDisGrandGoodsByGreatIdSunHola(Map<String, Object> map) {
		return nxDistributerGoodsDao.queryNxDepDisGrandGoodsByGreatIdSunHola(map);
    }


    @Override
    public List<NxDistributerGoodsEntity> queryIfHasSameDisGoods(Map<String, Object> mapS) {
		return nxDistributerGoodsDao.queryIfHasSameDisGoods(mapS);
    }

    @Override
    public List<NxDistributerGoodsEntity>  queryDisGoodsByName(Map<String, Object> map) {

		return nxDistributerGoodsDao.queryDisGoodsByName(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsByNamePinyin(Map<String, Object> map) {

	    return nxDistributerGoodsDao.queryDisGoodsByNamePinyin(map);
    }


    @Override
    public List<NxDistributerFatherGoodsEntity> queryNxDisGrandGoodsWithGbGoodsByGreatId(Map<String, Object> map) {

	    return nxDistributerGoodsDao.queryNxDisGrandGoodsWithGbGoodsByGreatId(map);
    }

    @Override
    public NxDistributerGoodsEntity queryOneGoodsAboutNxGoods(Map<String, Object> mapDepGoods) {

		return nxDistributerGoodsDao.queryOneGoodsAboutNxGoods(mapDepGoods);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> querySupplierGrand(Map<String, Object> map) {

		return nxDistributerGoodsDao.querySupplierGrand(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> querySupplierGoodsByGreatId(Map<String, Object> map) {

		return nxDistributerGoodsDao.querySupplierGoodsByGreatId(map);
    }

    @Override
    public List<NxGoodsEntity> querySupplierFather(Map<String, Object> map) {

		return nxDistributerGoodsDao.querySupplierFather(map);
    }

	@Override
	public List<NxDistributerGoodsEntity> queryAllLinshiGoods() {

		return nxDistributerGoodsDao.queryAllLinshiGoods();
	}

    @Override
    public List<NxDistributerGoodsEntity> querySupplierGoodsByFatherId(Map<String, Object> map) {

		return nxDistributerGoodsDao.querySupplierGoodsByFatherId(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryNxDepDisGrandGoodsByGreatIdAll(Map<String, Object> map) {

		return nxDistributerGoodsDao.queryNxDepDisGrandGoodsByGreatIdAll(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryGbDisGrandGoodsByGreatId(Map<String, Object> map) {

	    return nxDistributerGoodsDao.queryGbDisGrandGoodsByGreatId(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsByAlias(Map<String, Object> mapA) {

	    return nxDistributerGoodsDao.queryDisGoodsByAlias(mapA);
    }

    @Override
    public NxDistributerGoodsEntity queryDisGoodsDetailWithLinshi(Integer doDisGoodsId) {

	    return nxDistributerGoodsDao.queryDisGoodsDetailWithLinshi(doDisGoodsId);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsWithGbGoodsByParams(Map<String, Object> map) {

	    return nxDistributerGoodsDao.queryDisGoodsWithGbGoodsByParams(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisShelfGoodsQuickSearchStr(Map<String, Object> map) {

	    return nxDistributerGoodsDao.queryDisShelfGoodsQuickSearchStr(map);
    }

    @Override
    public List<NxDistributerGoodsShelfGoodsEntity> queryDisShelfGoods(Map<String, Object> map) {

	  return   nxDistributerGoodsDao.queryDisShelfGoods(map);
    }

    @Override
    public List<NxDistributerGoodsShelfGoodsEntity> queryDisShelfGoodsWithNxGoodsId(Map<String, Object> map) {
        return nxDistributerGoodsDao.queryDisShelfGoodsWithNxGoodsId(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisLinshiGoodsQuickSearchStr(Map<String, Object> map) {

	    return nxDistributerGoodsDao.queryDisLinshiGoodsQuickSearchStr(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisNxGoodsQuickSearchStrByGrandId(Map<String, Object> map) {

	    return nxDistributerGoodsDao.queryDisNxGoodsQuickSearchStrByGrandId(map);
    }

    @Override
    public List<NxDistributerEntity> queryYishangByGoods(Map<String, Object> map) {

	    return nxDistributerGoodsDao.queryYishangByGoods(map);
    }

    @Override
    public List<NxDistributerEntity> queryLinshiGoodsForNx() {

	    return nxDistributerGoodsDao.queryLinshiGoodsForNx();
    }

    @Override
    public int queryLinshiGoodsAcount(Integer nxDistributerId) {

	    return  nxDistributerGoodsDao.queryLinshiGoodsAcount(nxDistributerId);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryGbDisDisGrandGoodsByGreatId(Map<String, Object> map) {

	    return  nxDistributerGoodsDao.queryGbDisDisGrandGoodsByGreatId(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsQuickSearchStrWithGbDepOrders(Map<String, Object> map) {

	    return nxDistributerGoodsDao.queryDisGoodsQuickSearchStrWithGbDepOrders(map);
    }

    @Override
    public int queryNxGoodsSonsSortByParams(Map<String, Object> mapF) {

	    return nxDistributerGoodsDao.queryNxGoodsSonsSortByParams(mapF);
    }


    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsByLikeName(Map<String, Object> mapOne) {

	    return  nxDistributerGoodsDao.queryDisGoodsByLikeName(mapOne);
    }

    @Override
    public NxDistributerGoodsEntity querySameGoodsWithOrders(Map<String, Object> map) {

	    return nxDistributerGoodsDao.querySameGoodsWithOrders(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsByNameLikePinyin(Map<String, Object> mapTwo) {

	    return nxDistributerGoodsDao.queryDisGoodsByNameLikePinyin(mapTwo);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisGoodsByAliasLike(Map<String, Object> mapA) {

	    return nxDistributerGoodsDao.queryDisGoodsByAliasLike(mapA);
    }

    @Override
    public List<Integer> queryOnlyDepGoodsIds(Map<String, Object> map) {

	    return nxDistributerGoodsDao.queryOnlyDepGoodsIds(map);
    }

    @Override
    public List<Integer> queryOnlyDepGoodsIdsWithPurchaseType(Map<String, Object> map) {

	    return nxDistributerGoodsDao.queryOnlyDepGoodsIdsWithPurchaseType(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryDisUnshelfGoodsWithPage(Map<String, Object> map) {

        return nxDistributerGoodsDao.queryDisUnshelfGoodsWithPage(map);
    }

    @Override
    public int queryDisUnshelfGoodsTotal(Map<String, Object> map) {

        return nxDistributerGoodsDao.queryDisUnshelfGoodsTotal(map);
    }

    @Override
    public List<String> queryDisGoodsBrand() {

        return nxDistributerGoodsDao.queryDisGoodsBrand();
    }

    @Override
    public List<NxDistributerGoodsEntity> queryUnShelfDisGoodsQuickSearchStr(Map<String, Object> map) {

	    return nxDistributerGoodsDao.queryUnShelfDisGoodsQuickSearchStr(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryUnShelfDisGoodsQuickSearchStrWithNxGoodsId(Map<String, Object> map) {
        return nxDistributerGoodsDao.queryUnShelfDisGoodsQuickSearchStrWithNxGoodsId(map);
    }

    @Override
    public int updatePurchaseAutoByGreatGrandIds(Map<String, Object> map) {
        return nxDistributerGoodsDao.updatePurchaseAutoByGreatGrandIds(map);
    }


//    @Override
//    public List<NxDistributerFatherGoodsEntity> queryFatherDisGoodsByParams(Map<String, Object> map1) {
//
//		return nxDistributerGoodsDao.queryFatherDisGoodsByParams(map1);
//    }


}

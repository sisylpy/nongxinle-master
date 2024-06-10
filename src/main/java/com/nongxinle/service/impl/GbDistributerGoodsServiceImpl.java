package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDistributerEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerGoodsDao;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import com.nongxinle.service.GbDistributerGoodsService;



@Service("gbDistributerGoodsService")
public class GbDistributerGoodsServiceImpl implements GbDistributerGoodsService {
	@Autowired
	private GbDistributerGoodsDao gbDistributerGoodsDao;
	
	@Override
	public GbDistributerGoodsEntity queryObject(Integer gbDistributerGoodsId){
		return gbDistributerGoodsDao.queryObject(gbDistributerGoodsId);
	}
	
	@Override
	public List<GbDistributerGoodsEntity> queryList(Map<String, Object> map){
		return gbDistributerGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerGoodsEntity gbDistributerGoods){
		gbDistributerGoodsDao.save(gbDistributerGoods);
	}
	
	@Override
	public void update(GbDistributerGoodsEntity gbDistributerGoods){
		gbDistributerGoodsDao.update(gbDistributerGoods);
	}
	
	@Override
	public int delete(Integer gbDistributerGoodsId){
		gbDistributerGoodsDao.delete(gbDistributerGoodsId);
        return 1;
    }
	
	@Override
	public void deleteBatch(Integer[] gbDistributerGoodsIds){
		gbDistributerGoodsDao.deleteBatch(gbDistributerGoodsIds);
	}

    @Override
    public List<GbDistributerGoodsEntity> queryGoodsByParamsGb(Map<String, Object> map) {

		return gbDistributerGoodsDao.queryGoodsByParamsGb(map);
    }

    @Override
    public int queryGbGoodsTotal(Map<String, Object> map3) {

		return gbDistributerGoodsDao.queryGbGoodsTotal(map3);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsByParams(Map<String, Object> map) {

		return gbDistributerGoodsDao.queryDisGoodsByParams(map);
    }

	@Override
	public List<GbDistributerGoodsEntity> queryAddDistributerNxGoods(Map<String, Object> map) {
		return gbDistributerGoodsDao.queryAddDistributerNxGoods(map);
	}



    @Override
    public GbDistributerGoodsEntity queryGbDisGoodsDetail(Integer disGoodsId) {

		return gbDistributerGoodsDao.queryGbDisGoodsDetail(disGoodsId);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDgSubNameByFatherIdGb(Integer gbDistributerFatherGoodsId) {
        return gbDistributerGoodsDao.queryDgSubNameByFatherIdGb(gbDistributerFatherGoodsId);
    }


    @Override
    public List<GbDistributerGoodsEntity> queryGbDisGoodsQuickSearchStr(Map<String, Object> map) {

		return gbDistributerGoodsDao.queryGbDisGoodsQuickSearchStr(map);
    }

    @Override
    public int queryGbStockGoodsTotal(Map<String, Object> map3) {

		return gbDistributerGoodsDao.queryGbStockGoodsTotal(map3);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDisFatherGoodsByParams(Map<String, Object> map) {

		return gbDistributerGoodsDao.queryDisFatherGoodsByParams(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisShelfGoodsWithParams(Map<String, Object> map) {

		return gbDistributerGoodsDao.queryDisShelfGoodsWithParams(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryGbDisUnShlefGoodsQuickSearchStr(Map<String, Object> map) {

		return gbDistributerGoodsDao.queryGbDisUnShlefGoodsQuickSearchStr(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> querySubNameByFatherId(Integer gbDistributerFatherGoodsId) {

		return gbDistributerGoodsDao.querySubNameByFatherId(gbDistributerFatherGoodsId);
    }


    @Override
    public int queryDisGoodsCount(Map<String, Object> map) {

	    return gbDistributerGoodsDao.queryDisGoodsCount(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryPurchaserDisGoodsByParams(Map<String, Object> map) {

	    return gbDistributerGoodsDao.queryPurchaserDisGoodsByParams(map);
    }

    @Override
    public GbDistributerGoodsEntity queryDisGoodsWithDepDisGoods(Map<String, Object> map) {

	    return gbDistributerGoodsDao.queryDisGoodsWithDepDisGoods(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryUpdateGoodsByParams(Map<String, Object> map) {

	    return gbDistributerGoodsDao.queryUpdateGoodsByParams(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsWithShelfGoods(Integer depId) {

	    return gbDistributerGoodsDao.queryDisGoodsWithShelfGoods(depId);
    }

    @Override
    public List<GbDistributerGoodsEntity> querydisGoodsByNxGoodsId(Integer nxGoodsId) {

	    return gbDistributerGoodsDao.querydisGoodsByNxGoodsId(nxGoodsId);
    }

    @Override
    public List<GbDistributerEntity> queryGbDisByNxGoodsId(Integer nxGoodsId) {

	    return gbDistributerGoodsDao.queryGbDisByNxGoodsId(nxGoodsId);
    }

    @Override
    public GbDistributerGoodsEntity queryLinshiGoods(Integer lsGoodsId) {

	    return gbDistributerGoodsDao.queryLinshiGoods(lsGoodsId);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryDisGoodsQuickSearchStrWithDepOrdersGb(Map<String, Object> map) {
        return gbDistributerGoodsDao.queryDisGoodsQuickSearchStrWithDepOrdersGb(map);
    }


}

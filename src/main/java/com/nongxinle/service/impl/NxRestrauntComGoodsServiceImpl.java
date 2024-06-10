package com.nongxinle.service.impl;

import com.nongxinle.entity.NxCommunityFatherGoodsEntity;
import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.entity.NxRestrauntEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxRestrauntComGoodsDao;
import com.nongxinle.entity.NxRestrauntComGoodsEntity;
import com.nongxinle.service.NxRestrauntComGoodsService;



@Service("nxRestaruantComGoodsService")
public class NxRestrauntComGoodsServiceImpl implements NxRestrauntComGoodsService {
	@Autowired
	private NxRestrauntComGoodsDao nxRestrauntComGoodsDao;
	
	@Override
	public NxRestrauntComGoodsEntity queryObject(Integer nxRestrauntComGoodsId){
		return nxRestrauntComGoodsDao.queryObject(nxRestrauntComGoodsId);
	}
	
	@Override
	public List<NxRestrauntComGoodsEntity> queryList(Map<String, Object> map){
		return nxRestrauntComGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxRestrauntComGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(NxRestrauntComGoodsEntity nxRestaruantComGoods){
		nxRestrauntComGoodsDao.save(nxRestaruantComGoods);
	}
	
	@Override
	public void update(NxRestrauntComGoodsEntity nxRestaruantComGoods){
		nxRestrauntComGoodsDao.update(nxRestaruantComGoods);
	}
	
	@Override
	public void delete(Integer nxRestrauntComGoodsId){
		nxRestrauntComGoodsDao.delete(nxRestrauntComGoodsId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxRestrauntComGoodsIds){
		nxRestrauntComGoodsDao.deleteBatch(nxRestrauntComGoodsIds);
	}

    @Override
    public List<NxRestrauntComGoodsEntity> queryResComGoodsByParams(Map<String, Object> map) {

		return nxRestrauntComGoodsDao.queryResComGoodsByParams(map);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> resGetResComGoodsCata(Map<String, Object> map) {
        return nxRestrauntComGoodsDao.resGetResComGoodsCata(map);
    }

    @Override
    public List<NxRestrauntComGoodsEntity> queryResGoodsByFatherId(Map<String, Object> map) {

		return nxRestrauntComGoodsDao.queryResGoodsByFatherId(map);
    }

    @Override
    public int queryComGoodsTotal(Map<String, Object> map3) {

		return nxRestrauntComGoodsDao.queryComGoodsTotal(map3);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> queryHistoryGoods(Map<String, Object> map) {

		return nxRestrauntComGoodsDao.queryHistoryGoods(map);
    }

    @Override
    public List<NxRestrauntComGoodsEntity> queryHistoryGoodsQuickSearchStr(Map<String, Object> map1) {

		return nxRestrauntComGoodsDao.queryHistoryGoodsQuickSearchStr(map1);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> queryOrderUserGoods(Map<String, Object> map) {

		return nxRestrauntComGoodsDao.queryOrderUserGoods(map);
    }

	@Override
	public List<NxCommunityFatherGoodsEntity> queryOrderResGoods(Map<String, Object> map) {
		return nxRestrauntComGoodsDao.queryOrderResGoods(map);
	}

    @Override
    public List<NxRestrauntComGoodsEntity> orderUserQueryResComGoodsQuickSearchStr(Map<String, Object> map) {
        return nxRestrauntComGoodsDao.orderUserQueryResComGoodsQuickSearchStr(map);
    }

    @Override
    public List<NxRestrauntComGoodsEntity> ordreUserQueryHistoryGoodsQuickSearchStr(Map<String, Object> map1) {
        return nxRestrauntComGoodsDao.ordreUserQueryHistoryGoodsQuickSearchStr(map1);
    }

    @Override
    public void deleteResComGoods(Map<String, Object> map1) {
         nxRestrauntComGoodsDao.deleteResComGoods(map1);
    }

	@Override
	public List<NxRestrauntEntity> queryRestrantByResComGoodId(Map<String, Object> map1) {
		return nxRestrauntComGoodsDao.queryRestrantByResComGoodId(map1);
	}

    @Override
    public List<NxCommunityFatherGoodsEntity> queryResManagedGoods(Map<String, Object> map) {

        return nxRestrauntComGoodsDao.queryResManagedGoods(map);
    }

    @Override
    public List<NxRestrauntComGoodsEntity> queryResComGoodsWithComGoods(Integer resId) {

		return nxRestrauntComGoodsDao.queryResComGoodsWithComGoods(resId);
    }

    @Override
    public List<NxRestrauntEntity> queryChainResGoodsByParams(Map<String, Object> map) {

	    return nxRestrauntComGoodsDao.queryChainResGoodsByParams(map);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> queryCgManagedGoods(Map<String, Object> map) {

	    return nxRestrauntComGoodsDao.queryCgManagedGoods(map);
    }

    @Override
    public List<NxRestrauntComGoodsEntity> cgQueryCgMangementGoodsQuickSearchStr(Map<String, Object> map) {
        return nxRestrauntComGoodsDao.cgQueryCgMangementGoodsQuickSearchStr(map);
    }


}

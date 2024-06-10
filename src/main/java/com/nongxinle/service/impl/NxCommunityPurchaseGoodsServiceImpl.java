package com.nongxinle.service.impl;

import com.nongxinle.entity.NxCommunityFatherGoodsEntity;
import com.nongxinle.entity.NxDistributerPurchaseGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityPurchaseGoodsDao;
import com.nongxinle.entity.NxCommunityPurchaseGoodsEntity;
import com.nongxinle.service.NxCommunityPurchaseGoodsService;



@Service("nxCommunityPurchaseGoodsService")
public class NxCommunityPurchaseGoodsServiceImpl implements NxCommunityPurchaseGoodsService {
	@Autowired
	private NxCommunityPurchaseGoodsDao nxCommunityPurchaseGoodsDao;
	
	@Override
	public NxCommunityPurchaseGoodsEntity queryObject(Integer nxCommunityPurchaseGoodsId){
		return nxCommunityPurchaseGoodsDao.queryObject(nxCommunityPurchaseGoodsId);
	}
	
	@Override
	public List<NxCommunityPurchaseGoodsEntity> queryList(Map<String, Object> map){
		return nxCommunityPurchaseGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityPurchaseGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityPurchaseGoodsEntity nxCommunityPurchaseGoods){
		nxCommunityPurchaseGoodsDao.save(nxCommunityPurchaseGoods);
	}
	
	@Override
	public void update(NxCommunityPurchaseGoodsEntity nxCommunityPurchaseGoods){
		nxCommunityPurchaseGoodsDao.update(nxCommunityPurchaseGoods);
	}
	
	@Override
	public void delete(Integer nxCommunityPurchaseGoodsId){
		nxCommunityPurchaseGoodsDao.delete(nxCommunityPurchaseGoodsId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityPurchaseGoodsIds){
		nxCommunityPurchaseGoodsDao.deleteBatch(nxCommunityPurchaseGoodsIds);
	}

    @Override
    public List<NxCommunityPurchaseGoodsEntity> queryPurchaseForComGoods(Map<String, Object> map2) {

		return nxCommunityPurchaseGoodsDao.queryPurchaseForComGoods(map2);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> queryResOrdersByComPurchaseGoods(Map<String, Object> map2) {
        return nxCommunityPurchaseGoodsDao.queryResOrdersByComPurchaseGoods(map2);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> queryComPurchaseGoods(Map<String, Object> map) {

		return nxCommunityPurchaseGoodsDao.queryComPurchaseGoods(map);
    }

    @Override
    public List<NxCommunityPurchaseGoodsEntity> queryPurchaseGoodsByBathcId(Integer batchId) {

		return nxCommunityPurchaseGoodsDao.queryPurchaseGoodsByBathcId(batchId);
    }


}

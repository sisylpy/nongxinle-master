package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityFatherGoodsDao;
import com.nongxinle.entity.NxCommunityFatherGoodsEntity;
import com.nongxinle.service.NxCommunityFatherGoodsService;



@Service("nxCommunityFatherGoodsService")
public class NxCommunityFatherGoodsServiceImpl implements NxCommunityFatherGoodsService {
	@Autowired
	private NxCommunityFatherGoodsDao nxCommunityFatherGoodsDao;
	
	@Override
	public NxCommunityFatherGoodsEntity queryObject(Integer nxDfgId){
		return nxCommunityFatherGoodsDao.queryObject(nxDfgId);
	}
	
	@Override
	public List<NxCommunityFatherGoodsEntity> queryList(Map<String, Object> map){
		return nxCommunityFatherGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityFatherGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityFatherGoodsEntity nxCommunityFatherGoods){
		nxCommunityFatherGoodsDao.save(nxCommunityFatherGoods);
	}
	
	@Override
	public void update(NxCommunityFatherGoodsEntity nxCommunityFatherGoods){
		nxCommunityFatherGoodsDao.update(nxCommunityFatherGoods);
	}
	
	@Override
	public void delete(Integer nxDfgId){
		nxCommunityFatherGoodsDao.delete(nxDfgId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDfgIds){
		nxCommunityFatherGoodsDao.deleteBatch(nxDfgIds);
	}

    @Override
    public List<NxCommunityFatherGoodsEntity> queryFatherGoodsByFatherId(Integer dgGoodsFatherId) {

		return nxCommunityFatherGoodsDao.queryFatherGoods(dgGoodsFatherId);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> queryCataListByCommunityId(Integer communityId) {

		return nxCommunityFatherGoodsDao.queryCataListByCommunityId(communityId);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> queryHasComFathersFather(Map<String, Object> map2) {

		return nxCommunityFatherGoodsDao.queryHasComFathersFather(map2);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> queryComGoodsCata(Map<String, Object> map2) {

		return nxCommunityFatherGoodsDao.queryComGoodsCata(map2);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> queryRankFatherGoods(Integer comId) {

		return nxCommunityFatherGoodsDao.queryRankFatherGoods(comId);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> queryComFathersGoodsByParams(Map<String, Object> map) {

		return nxCommunityFatherGoodsDao.queryComFathersGoodsByParams(map);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> queryFatherWithGoods(Map<String, Object> map) {

		return nxCommunityFatherGoodsDao.queryFatherWithGoods(map);
    }

    @Override
    public List<NxCommunityFatherGoodsEntity> queryFatherWithGoodsPindan(Map<String, Object> map) {

		return nxCommunityFatherGoodsDao.queryFatherWithGoodsPindan(map);
    }

}

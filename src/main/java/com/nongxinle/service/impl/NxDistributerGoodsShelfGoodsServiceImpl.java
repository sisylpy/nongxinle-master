package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerGoodsShelfGoodsDao;
import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;
import com.nongxinle.service.NxDistributerGoodsShelfGoodsService;



@Service("nxDistributerGoodsShelfGoodsService")
public class NxDistributerGoodsShelfGoodsServiceImpl implements NxDistributerGoodsShelfGoodsService {
	@Autowired
	private NxDistributerGoodsShelfGoodsDao nxDistributerGoodsShelfGoodsDao;
	
	@Override
	public NxDistributerGoodsShelfGoodsEntity queryObject(Integer nxDistributerGoodsShelfGoodsId){
		return nxDistributerGoodsShelfGoodsDao.queryObject(nxDistributerGoodsShelfGoodsId);
	}
	
	@Override
	public List<NxDistributerGoodsShelfGoodsEntity> queryList(Map<String, Object> map){
		return nxDistributerGoodsShelfGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerGoodsShelfGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerGoodsShelfGoodsEntity nxDistributerGoodsShelfGoods){
		nxDistributerGoodsShelfGoodsDao.save(nxDistributerGoodsShelfGoods);
	}
	
	@Override
	public void update(NxDistributerGoodsShelfGoodsEntity nxDistributerGoodsShelfGoods){
		nxDistributerGoodsShelfGoodsDao.update(nxDistributerGoodsShelfGoods);
	}
	
	@Override
	public void delete(Integer nxDistributerGoodsShelfGoodsId){
		nxDistributerGoodsShelfGoodsDao.delete(nxDistributerGoodsShelfGoodsId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerGoodsShelfGoodsIds){
		nxDistributerGoodsShelfGoodsDao.deleteBatch(nxDistributerGoodsShelfGoodsIds);
	}

    @Override
    public List<NxDistributerGoodsShelfGoodsEntity> queryShelfGoodsByParams(Map<String, Object> map) {

		return nxDistributerGoodsShelfGoodsDao.queryShelfGoodsByParams(map);
    }

}

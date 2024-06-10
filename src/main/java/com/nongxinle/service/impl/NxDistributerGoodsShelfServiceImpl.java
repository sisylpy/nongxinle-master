package com.nongxinle.service.impl;

import com.nongxinle.entity.NxDistributerFatherGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerGoodsShelfDao;
import com.nongxinle.entity.NxDistributerGoodsShelfEntity;
import com.nongxinle.service.NxDistributerGoodsShelfService;



@Service("nxDistributerGoodsShelfService")
public class NxDistributerGoodsShelfServiceImpl implements NxDistributerGoodsShelfService {
	@Autowired
	private NxDistributerGoodsShelfDao nxDistributerGoodsShelfDao;
	
	@Override
	public NxDistributerGoodsShelfEntity queryObject(Integer nxDistributerGoodsShelfId){
		return nxDistributerGoodsShelfDao.queryObject(nxDistributerGoodsShelfId);
	}
	
	@Override
	public List<NxDistributerGoodsShelfEntity> queryList(Map<String, Object> map){
		return nxDistributerGoodsShelfDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerGoodsShelfDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerGoodsShelfEntity nxDistributerGoodsShelf){
		nxDistributerGoodsShelfDao.save(nxDistributerGoodsShelf);
	}
	
	@Override
	public void update(NxDistributerGoodsShelfEntity nxDistributerGoodsShelf){
		nxDistributerGoodsShelfDao.update(nxDistributerGoodsShelf);
	}
	
	@Override
	public void delete(Integer nxDistributerGoodsShelfId){
		nxDistributerGoodsShelfDao.delete(nxDistributerGoodsShelfId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerGoodsShelfIds){
		nxDistributerGoodsShelfDao.deleteBatch(nxDistributerGoodsShelfIds);
	}

	@Override
	public List<NxDistributerGoodsShelfEntity> queryShelfByParams(Map<String, Object> map) {
		return nxDistributerGoodsShelfDao.queryShelfByParams(map);


	}

    @Override
    public NxDistributerGoodsShelfEntity queryShelfGoodsByParams(Map<String, Object> map) {

		return nxDistributerGoodsShelfDao.queryShelfGoodsByParams(map);
    }

	@Override
	public List<NxDistributerFatherGoodsEntity> disGetUnPlanShelfPurchaseApplys(Map<String, Object> map) {
		return nxDistributerGoodsShelfDao.queryDisPlanShelfPurchaseApplys(map);
	}

    @Override
    public List<NxDistributerGoodsShelfEntity> queryShelfWithDetailByParams(Map<String, Object> map) {

		return nxDistributerGoodsShelfDao.queryShelfWithDetailByParams(map);
    }

}

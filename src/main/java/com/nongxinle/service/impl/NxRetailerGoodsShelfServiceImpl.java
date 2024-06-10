package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxRetailerGoodsShelfDao;
import com.nongxinle.entity.NxRetailerGoodsShelfEntity;
import com.nongxinle.service.NxRetailerGoodsShelfService;



@Service("nxRetailerGoodsShelfService")
public class NxRetailerGoodsShelfServiceImpl implements NxRetailerGoodsShelfService {
	@Autowired
	private NxRetailerGoodsShelfDao nxRetailerGoodsShelfDao;
	
	@Override
	public NxRetailerGoodsShelfEntity queryObject(Integer nxRetailerGoodsShelfId){
		return nxRetailerGoodsShelfDao.queryObject(nxRetailerGoodsShelfId);
	}
	
	@Override
	public List<NxRetailerGoodsShelfEntity> queryList(Map<String, Object> map){
		return nxRetailerGoodsShelfDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxRetailerGoodsShelfDao.queryTotal(map);
	}
	
	@Override
	public void save(NxRetailerGoodsShelfEntity nxRetailerGoodsShelf){
		nxRetailerGoodsShelfDao.save(nxRetailerGoodsShelf);
	}
	
	@Override
	public void update(NxRetailerGoodsShelfEntity nxRetailerGoodsShelf){
		nxRetailerGoodsShelfDao.update(nxRetailerGoodsShelf);
	}
	
	@Override
	public void delete(Integer nxRetailerGoodsShelfId){
		nxRetailerGoodsShelfDao.delete(nxRetailerGoodsShelfId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxRetailerGoodsShelfIds){
		nxRetailerGoodsShelfDao.deleteBatch(nxRetailerGoodsShelfIds);
	}

    @Override
    public List<NxRetailerGoodsShelfEntity> queryRetShelfByParams(Map<String, Object> map) {

		return nxRetailerGoodsShelfDao.queryRetShelfByParams(map);
    }

    @Override
    public List<NxRetailerGoodsShelfEntity> queryRetShelfWithPurGoodsByParams(Map<String, Object> map) {

		return nxRetailerGoodsShelfDao.queryRetShelfWithPurGoodsByParams(map);


    }

}

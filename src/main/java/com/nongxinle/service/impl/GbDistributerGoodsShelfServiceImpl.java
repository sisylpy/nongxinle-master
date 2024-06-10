package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDepartmentDisGoodsEntity;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerGoodsShelfDao;
import com.nongxinle.entity.GbDistributerGoodsShelfEntity;
import com.nongxinle.service.GbDistributerGoodsShelfService;



@Service("gbDistributerGoodsShelfService")
public class GbDistributerGoodsShelfServiceImpl implements GbDistributerGoodsShelfService {
	@Autowired
	private GbDistributerGoodsShelfDao gbDistributerGoodsShelfDao;
	
	@Override
	public GbDistributerGoodsShelfEntity queryObject(Integer gbDistributerGoodsShelfId){
		return gbDistributerGoodsShelfDao.queryObject(gbDistributerGoodsShelfId);
	}
	
	@Override
	public List<GbDistributerGoodsShelfEntity> queryList(Map<String, Object> map){
		return gbDistributerGoodsShelfDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerGoodsShelfDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerGoodsShelfEntity gbDistributerGoodsShelf){
		gbDistributerGoodsShelfDao.save(gbDistributerGoodsShelf);
	}
	
	@Override
	public void update(GbDistributerGoodsShelfEntity gbDistributerGoodsShelf){
		gbDistributerGoodsShelfDao.update(gbDistributerGoodsShelf);
	}
	
	@Override
	public void delete(Integer gbDistributerGoodsShelfId){
		gbDistributerGoodsShelfDao.delete(gbDistributerGoodsShelfId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerGoodsShelfIds){
		gbDistributerGoodsShelfDao.deleteBatch(gbDistributerGoodsShelfIds);
	}

	@Override
	public List<GbDistributerFatherGoodsEntity> queryShelfByParamsWithStock(Map<String, Object> map) {
		return gbDistributerGoodsShelfDao.queryShelfByParamsWithStock(map);
	}

	@Override
	public GbDistributerGoodsShelfEntity queryShelfGoodsByParams(Map<String, Object> map) {
		return gbDistributerGoodsShelfDao.queryShelfGoodsByParams(map);
	}

	@Override
	public List<GbDistributerFatherGoodsEntity> stockRoomGetShelfGoodsGb(Map<String, Object> map) {
		return gbDistributerGoodsShelfDao.stockRoomGetShelfGoodsGb(map);
	}

    @Override
    public List<GbDistributerGoodsEntity>  queryShelfInventoryGoodsByParams(Map<String, Object> map) {
		return gbDistributerGoodsShelfDao.queryShelfInventoryGoodsByParams(map);
    }

	@Override
	public List<GbDistributerGoodsShelfEntity> queryStockOrdersByParams(Map<String, Object> map) {
		return gbDistributerGoodsShelfDao.queryStockOrdersByParams(map);
	}

    @Override
    public List<GbDistributerFatherGoodsEntity> queryShelfInventoryDepGoodsByParams(Map<String, Object> map) {
        return gbDistributerGoodsShelfDao.queryShelfInventoryDepGoodsByParams(map);
    }

    @Override
    public List<GbDistributerGoodsShelfEntity> queryShelfList(Map<String, Object> map) {

		return gbDistributerGoodsShelfDao.queryShelfList(map);
    }

    @Override
    public List<GbDistributerGoodsShelfEntity> queryShelfGoodsWithPurOrder(Map<String, Object> map) {

		return gbDistributerGoodsShelfDao.queryShelfGoodsWithPurOrder(map);
    }



}

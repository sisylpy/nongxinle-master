package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerFatherGoodsDao;
import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.service.GbDistributerFatherGoodsService;



@Service("gbDistributerFatherGoodsService")
public class GbDistributerFatherGoodsServiceImpl implements GbDistributerFatherGoodsService {
	@Autowired
	private GbDistributerFatherGoodsDao gbDistributerFatherGoodsDao;
	
	@Override
	public GbDistributerFatherGoodsEntity queryObject(Integer gbDistributerFatherGoodsId){
		return gbDistributerFatherGoodsDao.queryObject(gbDistributerFatherGoodsId);
	}
	
	@Override
	public List<GbDistributerFatherGoodsEntity> queryList(Map<String, Object> map){
		return gbDistributerFatherGoodsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerFatherGoodsDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerFatherGoodsEntity gbDistributerFatherGoods){
		gbDistributerFatherGoodsDao.save(gbDistributerFatherGoods);
	}
	
	@Override
	public void update(GbDistributerFatherGoodsEntity gbDistributerFatherGoods){
		gbDistributerFatherGoodsDao.update(gbDistributerFatherGoods);
	}
	
	@Override
	public void delete(Integer gbDistributerFatherGoodsId){
		gbDistributerFatherGoodsDao.delete(gbDistributerFatherGoodsId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerFatherGoodsIds){
		gbDistributerFatherGoodsDao.deleteBatch(gbDistributerFatherGoodsIds);
	}


    @Override
    public List<GbDistributerFatherGoodsEntity> queryDisAll(Map<String, Object> map) {
		return gbDistributerFatherGoodsDao.queryDisAll(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> querySubFatherGoods(Integer goodsId) {

		return gbDistributerFatherGoodsDao.querySubFatherGoods(goodsId);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryHasDisFathersFather(Map<String, Object> map3) {

		return gbDistributerFatherGoodsDao.queryHasDisFathersFather(map3);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDisGoodsCata(Map<String, Object> map) {

		return gbDistributerFatherGoodsDao.queryDisGoodsCata(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDisStockOrdersFatherGoods(Map<String, Object> map) {

		return gbDistributerFatherGoodsDao.queryDisStockOrdersFatherGoods(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDisFathersGoodsByParamsGb(Map<String, Object> mapGrand) {

		return gbDistributerFatherGoodsDao.queryDisFathersGoodsByParamsGb(mapGrand);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDisGoodsCataWithGoods(Map<String, Object> map) {

		return gbDistributerFatherGoodsDao.queryDisGoodsCataWithGoods(map);
    }

    @Override
    public GbDistributerFatherGoodsEntity queryAppFatherGoods(Map<String, Object> map) {

		return gbDistributerFatherGoodsDao.queryAppFatherGoods(map);
    }

    @Override
    public List<GbDistributerFatherGoodsEntity> queryDisFathersGoodsByNxGoodsId(Integer nxGoodsId) {

		return gbDistributerFatherGoodsDao.queryDisFathersGoodsByNxGoodsId(nxGoodsId);
    }


}

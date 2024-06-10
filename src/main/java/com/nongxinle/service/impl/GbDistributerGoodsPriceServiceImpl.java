package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDistributerFatherGoodsEntity;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.GbDistributerGoodsPriceDao;
import com.nongxinle.entity.GbDistributerGoodsPriceEntity;
import com.nongxinle.service.GbDistributerGoodsPriceService;



@Service("gbDistributerGoodsPriceService")
public class GbDistributerGoodsPriceServiceImpl implements GbDistributerGoodsPriceService {
	@Autowired
	private GbDistributerGoodsPriceDao gbDistributerGoodsPriceDao;
	
	@Override
	public GbDistributerGoodsPriceEntity queryObject(Integer gbDistributerGoodsPriceId){
		return gbDistributerGoodsPriceDao.queryObject(gbDistributerGoodsPriceId);
	}
	
	@Override
	public List<GbDistributerGoodsPriceEntity> queryList(Map<String, Object> map){
		return gbDistributerGoodsPriceDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerGoodsPriceDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerGoodsPriceEntity gbDistributerGoodsPrice){
		gbDistributerGoodsPriceDao.save(gbDistributerGoodsPrice);
	}
	
	@Override
	public void update(GbDistributerGoodsPriceEntity gbDistributerGoodsPrice){
		gbDistributerGoodsPriceDao.update(gbDistributerGoodsPrice);
	}
	
	@Override
	public void delete(Integer gbDistributerGoodsPriceId){
		gbDistributerGoodsPriceDao.delete(gbDistributerGoodsPriceId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerGoodsPriceIds){
		gbDistributerGoodsPriceDao.deleteBatch(gbDistributerGoodsPriceIds);
	}

    @Override
    public List<GbDistributerGoodsPriceEntity> queryPriceGoodsListByParams(Map<String, Object> map) {

		return gbDistributerGoodsPriceDao.queryPriceGoodsListByParams(map);
    }

    @Override
    public Double queryPriceWhatTotal(Map<String, Object> map1) {

		return  gbDistributerGoodsPriceDao.queryPriceWhatTotal(map1);
    }

    @Override
    public int queryPriceWhatAmount(Map<String, Object> map1) {

		return gbDistributerGoodsPriceDao.queryPriceWhatAmount(map1);
    }

    @Override
    public TreeSet<GbDistributerFatherGoodsEntity> queryTreePriceGoodsList(Map<String, Object> map0) {

		return gbDistributerGoodsPriceDao.queryTreePriceGoodsList(map0);
    }

    @Override
    public Double queryPurTotal(Map<String, Object> map0) {

		return gbDistributerGoodsPriceDao.queryPurTotal(map0);
    }

    @Override
    public Double queryPriceHighestTotal(Map<String, Object> map0) {

		return gbDistributerGoodsPriceDao.queryPriceHighestTotal(map0);
    }

	@Override
	public Double queryPriceLowestTotal(Map<String, Object> map0) {

		return gbDistributerGoodsPriceDao.queryPriceLowestTotal(map0);
	}

    @Override
    public Integer queryDisPriceGoodsCount(Map<String, Object> map0) {

		return gbDistributerGoodsPriceDao.queryDisPriceGoodsCount(map0);
    }

    @Override
    public Double queryPriceWhatScale(Map<String, Object> map) {

		return gbDistributerGoodsPriceDao.queryPriceWhatScale(map);
    }

    @Override
    public List<GbDistributerGoodsEntity> queryTreeSetDisGoods(Map<String, Object> map) {

		return gbDistributerGoodsPriceDao.queryTreeSetDisGoods(map);
    }

    @Override
    public Double queryPricePurWeight(Map<String, Object> map1) {

		return gbDistributerGoodsPriceDao.queryPricePurWeight(map1);
    }

    @Override
    public GbDistributerGoodsPriceEntity queryItemByPurGoodsId(Integer purchaseGoodsId) {

		return gbDistributerGoodsPriceDao.queryItemByPurGoodsId(purchaseGoodsId);
    }


}

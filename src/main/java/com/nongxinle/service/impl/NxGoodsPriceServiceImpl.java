package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxGoodsPriceDao;
import com.nongxinle.entity.NxGoodsPriceEntity;
import com.nongxinle.service.NxGoodsPriceService;



@Service("nxGoodsPriceService")
public class NxGoodsPriceServiceImpl implements NxGoodsPriceService {
	@Autowired
	private NxGoodsPriceDao nxGoodsPriceDao;
	
	@Override
	public NxGoodsPriceEntity queryObject(Integer nxGoodsPriceId){
		return nxGoodsPriceDao.queryObject(nxGoodsPriceId);
	}
	
	@Override
	public List<NxGoodsPriceEntity> queryList(Map<String, Object> map){
		return nxGoodsPriceDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxGoodsPriceDao.queryTotal(map);
	}
	
	@Override
	public void save(NxGoodsPriceEntity nxGoodsPrice){
		nxGoodsPriceDao.save(nxGoodsPrice);
	}
	
	@Override
	public void update(NxGoodsPriceEntity nxGoodsPrice){
		nxGoodsPriceDao.update(nxGoodsPrice);
	}
	
	@Override
	public void delete(Integer nxGoodsPriceId){
		nxGoodsPriceDao.delete(nxGoodsPriceId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxGoodsPriceIds){
		nxGoodsPriceDao.deleteBatch(nxGoodsPriceIds);
	}

    @Override
    public NxGoodsPriceEntity queryPriceGoodsByParams(Map<String, Object> map) {

		return nxGoodsPriceDao.queryPriceGoodsByParams(map);
    }

    @Override
    public double queryLowestPriceByParams(Map<String, Object> mapL) {

		return nxGoodsPriceDao.queryLowestPriceByParams(mapL);
    }

    @Override
    public double queryHighestPriceByParams(Map<String, Object> mapL) {

		return nxGoodsPriceDao.queryHighestPriceByParams(mapL);
    }

    @Override
    public int queryPriceGoodsCount(Map<String, Object> mapL) {

		return nxGoodsPriceDao.queryPriceGoodsCount(mapL);
    }

}

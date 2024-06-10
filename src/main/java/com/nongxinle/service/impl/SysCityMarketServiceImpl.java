package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.SysCityMarketDao;
import com.nongxinle.entity.SysCityMarketEntity;
import com.nongxinle.service.SysCityMarketService;



@Service("sysCityMarketService")
public class SysCityMarketServiceImpl implements SysCityMarketService {
	@Autowired
	private SysCityMarketDao sysCityMarketDao;
	
	@Override
	public SysCityMarketEntity queryObject(Integer sysCityMarketId){
		return sysCityMarketDao.queryObject(sysCityMarketId);
	}
	
	@Override
	public List<SysCityMarketEntity> queryList(Map<String, Object> map){
		return sysCityMarketDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return sysCityMarketDao.queryTotal(map);
	}
	
	@Override
	public void save(SysCityMarketEntity sysCityMarket){
		sysCityMarketDao.save(sysCityMarket);
	}
	
	@Override
	public void update(SysCityMarketEntity sysCityMarket){
		sysCityMarketDao.update(sysCityMarket);
	}
	
	@Override
	public void delete(Integer sysCityMarketId){
		sysCityMarketDao.delete(sysCityMarketId);
	}
	
	@Override
	public void deleteBatch(Integer[] sysCityMarketIds){
		sysCityMarketDao.deleteBatch(sysCityMarketIds);
	}
	
}

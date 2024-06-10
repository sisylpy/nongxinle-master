package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerAliasDao;
import com.nongxinle.entity.GbDistributerAliasEntity;
import com.nongxinle.service.GbDistributerAliasService;



@Service("gbDistributerAliasService")
public class GbDistributerAliasServiceImpl implements GbDistributerAliasService {
	@Autowired
	private GbDistributerAliasDao gbDistributerAliasDao;
	
	@Override
	public GbDistributerAliasEntity queryObject(Integer gbDistributerAliasId){
		return gbDistributerAliasDao.queryObject(gbDistributerAliasId);
	}
	
	@Override
	public List<GbDistributerAliasEntity> queryList(Map<String, Object> map){
		return gbDistributerAliasDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerAliasDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerAliasEntity gbDistributerAlias){
		gbDistributerAliasDao.save(gbDistributerAlias);
	}
	
	@Override
	public void update(GbDistributerAliasEntity gbDistributerAlias){
		gbDistributerAliasDao.update(gbDistributerAlias);
	}
	
	@Override
	public void delete(Integer gbDistributerAliasId){
		gbDistributerAliasDao.delete(gbDistributerAliasId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerAliasIds){
		gbDistributerAliasDao.deleteBatch(gbDistributerAliasIds);
	}
	
}

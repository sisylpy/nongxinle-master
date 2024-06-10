package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerStandardDao;
import com.nongxinle.entity.GbDistributerStandardEntity;
import com.nongxinle.service.GbDistributerStandardService;



@Service("gbDistributerStandardService")
public class GbDistributerStandardServiceImpl implements GbDistributerStandardService {
	@Autowired
	private GbDistributerStandardDao gbDistributerStandardDao;
	
	@Override
	public GbDistributerStandardEntity queryObject(Integer gbDistributerStandardId){
		return gbDistributerStandardDao.queryObject(gbDistributerStandardId);
	}
	
	@Override
	public List<GbDistributerStandardEntity> queryList(Map<String, Object> map){
		return gbDistributerStandardDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerStandardDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerStandardEntity gbDistributerStandard){
		gbDistributerStandardDao.save(gbDistributerStandard);
	}
	
	@Override
	public void update(GbDistributerStandardEntity gbDistributerStandard){
		gbDistributerStandardDao.update(gbDistributerStandard);
	}
	
	@Override
	public void delete(Integer gbDistributerStandardId){
		gbDistributerStandardDao.delete(gbDistributerStandardId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerStandardIds){
		gbDistributerStandardDao.deleteBatch(gbDistributerStandardIds);
	}

    @Override
    public List<GbDistributerStandardEntity> queryDisStandardByDisGoodsIdGb(Integer disGoodsId) {

		return gbDistributerStandardDao.queryDisStandardByDisGoodsIdGb(disGoodsId);
    }

}

package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxECommerceDao;
import com.nongxinle.entity.NxECommerceEntity;
import com.nongxinle.service.NxECommerceService;



@Service("nxECommerceService")
public class NxECommerceServiceImpl implements NxECommerceService {
	@Autowired
	private NxECommerceDao nxECommerceDao;
	
	@Override
	public NxECommerceEntity queryObject(Integer nxECommerceId){
		return nxECommerceDao.queryObject(nxECommerceId);
	}
	
	@Override
	public List<NxECommerceEntity> queryList(Map<String, Object> map){
		return nxECommerceDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxECommerceDao.queryTotal(map);
	}
	
	@Override
	public void save(NxECommerceEntity nxECommerce){
		nxECommerceDao.save(nxECommerce);
	}
	
	@Override
	public void update(NxECommerceEntity nxECommerce){
		nxECommerceDao.update(nxECommerce);
	}
	
	@Override
	public void delete(Integer nxECommerceId){
		nxECommerceDao.delete(nxECommerceId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxECommerceIds){
		nxECommerceDao.deleteBatch(nxECommerceIds);
	}
	
}

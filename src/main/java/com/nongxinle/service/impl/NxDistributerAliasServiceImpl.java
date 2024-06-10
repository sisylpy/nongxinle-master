package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerAliasDao;
import com.nongxinle.entity.NxDistributerAliasEntity;
import com.nongxinle.service.NxDistributerAliasService;



@Service("nxDistributerAliasService")
public class NxDistributerAliasServiceImpl implements NxDistributerAliasService {
	@Autowired
	private NxDistributerAliasDao nxDistributerAliasDao;
	
	@Override
	public NxDistributerAliasEntity queryObject(Integer nxDistributerAliasId){
		return nxDistributerAliasDao.queryObject(nxDistributerAliasId);
	}
	
	@Override
	public List<NxDistributerAliasEntity> queryList(Map<String, Object> map){
		return nxDistributerAliasDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerAliasDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerAliasEntity nxDistributerAlias){
		nxDistributerAliasDao.save(nxDistributerAlias);
	}
	
	@Override
	public void update(NxDistributerAliasEntity nxDistributerAlias){
		nxDistributerAliasDao.update(nxDistributerAlias);
	}
	
	@Override
	public void delete(Integer nxDistributerAliasId){
		nxDistributerAliasDao.delete(nxDistributerAliasId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerAliasIds){
		nxDistributerAliasDao.deleteBatch(nxDistributerAliasIds);
	}
	
}

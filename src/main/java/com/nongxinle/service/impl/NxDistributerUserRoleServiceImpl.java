package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerUserRoleDao;
import com.nongxinle.entity.NxDistributerUserRoleEntity;
import com.nongxinle.service.NxDistributerUserRoleService;



@Service("nxDistributerUserRoleService")
public class NxDistributerUserRoleServiceImpl implements NxDistributerUserRoleService {
	@Autowired
	private NxDistributerUserRoleDao nxDistributerUserRoleDao;
	
	@Override
	public NxDistributerUserRoleEntity queryObject(Integer nxDistributerUserRoleId){
		return nxDistributerUserRoleDao.queryObject(nxDistributerUserRoleId);
	}
	
	@Override
	public List<NxDistributerUserRoleEntity> queryList(Map<String, Object> map){
		return nxDistributerUserRoleDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerUserRoleDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerUserRoleEntity nxDistributerUserRole){
		nxDistributerUserRoleDao.save(nxDistributerUserRole);
	}
	
	@Override
	public void update(NxDistributerUserRoleEntity nxDistributerUserRole){
		nxDistributerUserRoleDao.update(nxDistributerUserRole);
	}
	
	@Override
	public void delete(Integer nxDistributerUserRoleId){
		nxDistributerUserRoleDao.delete(nxDistributerUserRoleId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerUserRoleIds){
		nxDistributerUserRoleDao.deleteBatch(nxDistributerUserRoleIds);
	}
	
}

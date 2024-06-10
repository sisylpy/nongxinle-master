package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDepartmentStandardDao;
import com.nongxinle.entity.NxDepartmentStandardEntity;
import com.nongxinle.service.NxDepartmentStandardService;



@Service("nxDepartmentStandardService")
public class NxDepartmentStandardServiceImpl implements NxDepartmentStandardService {
	@Autowired
	private NxDepartmentStandardDao nxDepartmentStandardDao;
	
	@Override
	public NxDepartmentStandardEntity queryObject(Integer nxDepartmentStandardId){
		return nxDepartmentStandardDao.queryObject(nxDepartmentStandardId);
	}
	@Override
	public void save(NxDepartmentStandardEntity nxDepartmentStandard){
		nxDepartmentStandardDao.save(nxDepartmentStandard);
	}

	@Override
	public void update(NxDepartmentStandardEntity nxDepartmentStandard){
		nxDepartmentStandardDao.update(nxDepartmentStandard);
	}
	@Override
	public void delete(Integer nxDepartmentStandardId){
		nxDepartmentStandardDao.delete(nxDepartmentStandardId);
	}

	@Override
	public List<NxDepartmentStandardEntity> queryDepGoodsStandards(Integer depGoodsId) {

		return nxDepartmentStandardDao.queryDepGoodsStandards(depGoodsId);
	}
//
//
//	@Override
//	public List<NxDepartmentStandardEntity> queryList(Map<String, Object> map){
//		return nxDepartmentStandardDao.queryList(map);
//	}
//
//	@Override
//	public int queryTotal(Map<String, Object> map){
//		return nxDepartmentStandardDao.queryTotal(map);
//	}
//
//
//
//
//	@Override
//	public void deleteBatch(Integer[] nxDepartmentStandardIds){
//		nxDepartmentStandardDao.deleteBatch(nxDepartmentStandardIds);
//	}



}

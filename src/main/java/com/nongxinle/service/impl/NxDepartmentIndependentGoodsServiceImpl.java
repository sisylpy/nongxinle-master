package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDepartmentIndependentGoodsDao;
import com.nongxinle.entity.NxDepartmentIndependentGoodsEntity;
import com.nongxinle.service.NxDepartmentIndependentGoodsService;



@Service("nxDepartmentIndependentGoodsService")
public class NxDepartmentIndependentGoodsServiceImpl implements NxDepartmentIndependentGoodsService {
	@Autowired
	private NxDepartmentIndependentGoodsDao nxDepartmentIndependentGoodsDao;
	
	@Override
	public NxDepartmentIndependentGoodsEntity queryObject(Integer nxDepartmentIndependentGoodsId){
		return nxDepartmentIndependentGoodsDao.queryObject(nxDepartmentIndependentGoodsId);
	}
	
	@Override
	public List<NxDepartmentIndependentGoodsEntity> queryList(Map<String, Object> map){
		return nxDepartmentIndependentGoodsDao.queryList(map);
	}
	@Override
	public void save(NxDepartmentIndependentGoodsEntity nxDepartmentIndependentGoods){
		nxDepartmentIndependentGoodsDao.save(nxDepartmentIndependentGoods);
	}

	@Override
	public void update(NxDepartmentIndependentGoodsEntity nxDepartmentIndependentGoods){
		nxDepartmentIndependentGoodsDao.update(nxDepartmentIndependentGoods);
	}

	@Override
	public void delete(Integer nxDepartmentIndependentGoodsId){
		nxDepartmentIndependentGoodsDao.delete(nxDepartmentIndependentGoodsId);
	}
	@Override
	public List<NxDepartmentIndependentGoodsEntity> querySearchStr(Map<String, Object> map) {

		return nxDepartmentIndependentGoodsDao.querySearchStr(map);
	}

	
//	@Override
//	public int queryTotal(Map<String, Object> map){
//		return nxDepartmentIndependentGoodsDao.queryTotal(map);
//	}
	


//
//	@Override
//	public void deleteBatch(Integer[] nxDepartmentIndependentGoodsIds){
//		nxDepartmentIndependentGoodsDao.deleteBatch(nxDepartmentIndependentGoodsIds);
//	}



}

package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxJrdhUserAuthSupplierIdDao;
import com.nongxinle.entity.NxJrdhUserAuthSupplierIdEntity;
import com.nongxinle.service.NxJrdhUserAuthSupplierIdService;



@Service("nxJrdhUserAuthSupplierIdService")
public class NxJrdhUserAuthSupplierIdServiceImpl implements NxJrdhUserAuthSupplierIdService {
	@Autowired
	private NxJrdhUserAuthSupplierIdDao nxJrdhUserAuthSupplierIdDao;
	
	@Override
	public NxJrdhUserAuthSupplierIdEntity queryObject(Integer nxJrdhUserAuthSupplierId){
		return nxJrdhUserAuthSupplierIdDao.queryObject(nxJrdhUserAuthSupplierId);
	}
	
	@Override
	public List<NxJrdhUserAuthSupplierIdEntity> queryList(Map<String, Object> map){
		return nxJrdhUserAuthSupplierIdDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxJrdhUserAuthSupplierIdDao.queryTotal(map);
	}
	
	@Override
	public void save(NxJrdhUserAuthSupplierIdEntity nxJrdhUserAuthSupplierId){
		nxJrdhUserAuthSupplierIdDao.save(nxJrdhUserAuthSupplierId);
	}
	
	@Override
	public void update(NxJrdhUserAuthSupplierIdEntity nxJrdhUserAuthSupplierId){
		nxJrdhUserAuthSupplierIdDao.update(nxJrdhUserAuthSupplierId);
	}
	
	@Override
	public void delete(Integer nxJrdhUserAuthSupplierId){
		nxJrdhUserAuthSupplierIdDao.delete(nxJrdhUserAuthSupplierId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxJrdhUserAuthSupplierIds){
		nxJrdhUserAuthSupplierIdDao.deleteBatch(nxJrdhUserAuthSupplierIds);
	}

    @Override
    public NxJrdhUserAuthSupplierIdEntity queryAuthSupplierByIds(Map<String, Object> map) {

		return nxJrdhUserAuthSupplierIdDao.queryAuthSupplierByIds(map);
    }

}

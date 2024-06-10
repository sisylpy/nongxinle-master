package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxJrdhSupplierDao;
import com.nongxinle.entity.NxJrdhSupplierEntity;
import com.nongxinle.service.NxJrdhSupplierService;



@Service("nxJrdhSupplierService")
public class NxJrdhSupplierServiceImpl implements NxJrdhSupplierService {
	@Autowired
	private NxJrdhSupplierDao nxJrdhSupplierDao;
	
	@Override
	public NxJrdhSupplierEntity queryObject(Integer nxJrdhSupplierId){
		return nxJrdhSupplierDao.queryObject(nxJrdhSupplierId);
	}
	
	@Override
	public List<NxJrdhSupplierEntity> queryList(Map<String, Object> map){
		return nxJrdhSupplierDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxJrdhSupplierDao.queryTotal(map);
	}
	
	@Override
	public void save(NxJrdhSupplierEntity nxJrdhSupplier){
		nxJrdhSupplierDao.save(nxJrdhSupplier);
	}
	
	@Override
	public void update(NxJrdhSupplierEntity nxJrdhSupplier){
		nxJrdhSupplierDao.update(nxJrdhSupplier);
	}
	
	@Override
	public void delete(Integer nxJrdhSupplierId){
		nxJrdhSupplierDao.delete(nxJrdhSupplierId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxJrdhSupplierIds){
		nxJrdhSupplierDao.deleteBatch(nxJrdhSupplierIds);
	}

    @Override
    public List<NxJrdhSupplierEntity> queryJrdhSupplerByParams(Map<String, Object> map) {

		return nxJrdhSupplierDao.queryJrdhSupplerByParams(map);
    }

    @Override
    public NxJrdhSupplierEntity querySellUserSupplier(Map<String, Object> mapS) {

		return nxJrdhSupplierDao.querySellUserSupplier(mapS);
    }

    @Override
    public List<NxJrdhSupplierEntity> queryJrdhSupplerWithDisByUserId(Map<String, Object> mapS) {

		return nxJrdhSupplierDao.queryJrdhSupplerWithDisByUserId(mapS);
    }

    @Override
    public NxJrdhSupplierEntity querySupplierByUserId(Integer sellerId) {

		return nxJrdhSupplierDao.querySupplierByUserId(sellerId);
    }

}

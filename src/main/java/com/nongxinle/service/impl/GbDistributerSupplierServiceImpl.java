package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDepartmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerSupplierDao;
import com.nongxinle.entity.GbDistributerSupplierEntity;
import com.nongxinle.service.GbDistributerSupplierService;



@Service("gbDistributerSupplierService")
public class GbDistributerSupplierServiceImpl implements GbDistributerSupplierService {
	@Autowired
	private GbDistributerSupplierDao gbDistributerSupplierDao;
	
	@Override
	public GbDistributerSupplierEntity queryObject(Integer gbDistributerSupplierId){
		return gbDistributerSupplierDao.queryObject(gbDistributerSupplierId);
	}
	
	@Override
	public List<GbDistributerSupplierEntity> queryList(Map<String, Object> map){
		return gbDistributerSupplierDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerSupplierDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerSupplierEntity gbDistributerSupplier){
		gbDistributerSupplierDao.save(gbDistributerSupplier);
	}
	
	@Override
	public void update(GbDistributerSupplierEntity gbDistributerSupplier){
		gbDistributerSupplierDao.update(gbDistributerSupplier);
	}
	
	@Override
	public void delete(Integer gbDistributerSupplierId){
		gbDistributerSupplierDao.delete(gbDistributerSupplierId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerSupplierIds){
		gbDistributerSupplierDao.deleteBatch(gbDistributerSupplierIds);
	}

    @Override
    public List<GbDistributerSupplierEntity> querySupplierByParams(Map<String, Object> map) {

		return gbDistributerSupplierDao.querySupplierByParams(map);
    }

    @Override
    public List<GbDistributerSupplierEntity> queryDepartmentSupplier(Map<String, Object> map) {

		return gbDistributerSupplierDao.queryDepartmentSupplier(map);
    }

    @Override
    public List<GbDepartmentEntity> querySupplierDepartmentGroup(Map<String, Object> map) {

		return gbDistributerSupplierDao.querySupplierDepartmentGroup(map);
    }

    @Override
    public List<GbDistributerSupplierEntity> queryDepartmentAppointSupplier(Map<String, Object> map) {

		return gbDistributerSupplierDao.queryDepartmentAppointSupplier(map);
    }

    @Override
    public GbDistributerSupplierEntity queryAppointSupplierBySupplierId(Map<String, Object> map) {

		return gbDistributerSupplierDao.queryAppointSupplierBySupplierId(map);
    }


}

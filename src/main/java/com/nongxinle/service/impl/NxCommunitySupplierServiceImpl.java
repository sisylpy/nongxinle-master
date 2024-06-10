package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunitySupplierDao;
import com.nongxinle.entity.NxCommunitySupplierEntity;
import com.nongxinle.service.NxCommunitySupplierService;



@Service("nxCommunitySupplierService")
public class NxCommunitySupplierServiceImpl implements NxCommunitySupplierService {
	@Autowired
	private NxCommunitySupplierDao nxCommunitySupplierDao;
	
	@Override
	public NxCommunitySupplierEntity queryObject(Integer nxCommunitySupplierId){
		return nxCommunitySupplierDao.queryObject(nxCommunitySupplierId);
	}
	
	@Override
	public List<NxCommunitySupplierEntity> queryList(Map<String, Object> map){
		return nxCommunitySupplierDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunitySupplierDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunitySupplierEntity nxCommunitySupplier){
		nxCommunitySupplierDao.save(nxCommunitySupplier);
	}
	
	@Override
	public void update(NxCommunitySupplierEntity nxCommunitySupplier){
		nxCommunitySupplierDao.update(nxCommunitySupplier);
	}
	
	@Override
	public void delete(Integer nxCommunitySupplierId){
		nxCommunitySupplierDao.delete(nxCommunitySupplierId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunitySupplierIds){
		nxCommunitySupplierDao.deleteBatch(nxCommunitySupplierIds);
	}

    @Override
    public List<NxCommunitySupplierEntity> queryCommunitySupplierByParams(Map<String, Object> map) {

		return nxCommunitySupplierDao.queryCommunitySupplierByParams(map);
    }

}

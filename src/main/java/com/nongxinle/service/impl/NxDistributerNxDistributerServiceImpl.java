package com.nongxinle.service.impl;

import com.nongxinle.entity.NxDistributerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerNxDistributerDao;
import com.nongxinle.entity.NxDistributerNxDistributerEntity;
import com.nongxinle.service.NxDistributerNxDistributerService;



@Service("nxDistributerNxDistributerService")
public class NxDistributerNxDistributerServiceImpl implements NxDistributerNxDistributerService {
	@Autowired
	private NxDistributerNxDistributerDao nxDistributerNxDistributerDao;
	
	@Override
	public NxDistributerNxDistributerEntity queryObject(Integer nxDistributerNxDistributerId){
		return nxDistributerNxDistributerDao.queryObject(nxDistributerNxDistributerId);
	}
	
	@Override
	public List<NxDistributerNxDistributerEntity> queryList(Map<String, Object> map){
		return nxDistributerNxDistributerDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerNxDistributerDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerNxDistributerEntity nxDistributerNxDistributer){
		nxDistributerNxDistributerDao.save(nxDistributerNxDistributer);
	}
	
	@Override
	public void update(NxDistributerNxDistributerEntity nxDistributerNxDistributer){
		nxDistributerNxDistributerDao.update(nxDistributerNxDistributer);
	}
	
	@Override
	public void delete(Integer nxDistributerNxDistributerId){
		nxDistributerNxDistributerDao.delete(nxDistributerNxDistributerId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerNxDistributerIds){
		nxDistributerNxDistributerDao.deleteBatch(nxDistributerNxDistributerIds);
	}

	@Override
	public List<NxDistributerEntity> queryOfferNxDisByParams(Map<String, Object> map3) {
		return nxDistributerNxDistributerDao.queryOfferNxDisByParams(map3);
	}

	@Override
	public NxDistributerNxDistributerEntity queryByPartnerIds(Integer disId1, Integer disId2) {
		if (disId1 == null || disId2 == null) {
			return null;
		}
		return nxDistributerNxDistributerDao.queryByPartnerIds(disId1, disId2);
	}

}

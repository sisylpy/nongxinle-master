package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityStockSubDao;
import com.nongxinle.entity.NxCommunityStockSubEntity;
import com.nongxinle.service.NxCommunityStockSubService;



@Service("nxCommunityStockSubService")
public class NxCommunityStockSubServiceImpl implements NxCommunityStockSubService {
	@Autowired
	private NxCommunityStockSubDao nxCommunityStockSubDao;
	
	@Override
	public NxCommunityStockSubEntity queryObject(Integer nxCommunitySubStockId){
		return nxCommunityStockSubDao.queryObject(nxCommunitySubStockId);
	}
	
	@Override
	public List<NxCommunityStockSubEntity> queryList(Map<String, Object> map){
		return nxCommunityStockSubDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityStockSubDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityStockSubEntity nxCommunityStockSub){
		nxCommunityStockSubDao.save(nxCommunityStockSub);
	}
	
	@Override
	public void update(NxCommunityStockSubEntity nxCommunityStockSub){
		nxCommunityStockSubDao.update(nxCommunityStockSub);
	}
	
	@Override
	public void delete(Integer nxCommunitySubStockId){
		nxCommunityStockSubDao.delete(nxCommunitySubStockId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunitySubStockIds){
		nxCommunityStockSubDao.deleteBatch(nxCommunitySubStockIds);
	}
	
}

package com.nongxinle.service.impl;

import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.entity.NxCommunityOrdersSubEntity;
import com.nongxinle.service.NxCommunityOrdersSubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityOrdersSubDao;


@Service("nxOrdersSubService")
public class NxCommunityCommunityOrdersSubServiceImpl implements NxCommunityOrdersSubService {
	@Autowired
	private NxCommunityOrdersSubDao nxCommunityOrdersSubDao;
	
	@Override
	public NxCommunityOrdersSubEntity queryObject(Integer nxOrdersSubId){
		return nxCommunityOrdersSubDao.queryObject(nxOrdersSubId);
	}
	
	@Override
	public List<NxCommunityOrdersSubEntity> queryList(Map<String, Object> map){
		return nxCommunityOrdersSubDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityOrdersSubDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityOrdersSubEntity nxOrdersSub){
		nxCommunityOrdersSubDao.save(nxOrdersSub);
	}
	
	@Override
	public void update(NxCommunityOrdersSubEntity nxOrdersSub){
		nxCommunityOrdersSubDao.update(nxOrdersSub);
	}
	
	@Override
	public void delete(Integer nxOrdersSubId){
		nxCommunityOrdersSubDao.delete(nxOrdersSubId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxOrdersSubIds){
		nxCommunityOrdersSubDao.deleteBatch(nxOrdersSubIds);
	}



	@Override
	public List<NxCommunityOrdersSubEntity> querySubOrdersByCustomerUserId(Map<String, Object> map) {
		return nxCommunityOrdersSubDao.querySubOrdersByCustomerUserId(map);
	}

    @Override
    public List<NxCommunityOrdersEntity> queryOutGoodsByType(Map<String, Object> map) {
        return nxCommunityOrdersSubDao.queryOutGoodsByType(map);
    }

	@Override
	public List<NxCommunityOrdersSubEntity> querySubOrdersByParams(Map<String, Object> map) {
		return nxCommunityOrdersSubDao.querySubOrdersByParams(map);
	}




}

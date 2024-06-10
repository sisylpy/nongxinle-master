package com.nongxinle.service.impl;

import com.nongxinle.entity.NxCommunityEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxECommerceCommunityDao;
import com.nongxinle.entity.NxECommerceCommunityEntity;
import com.nongxinle.service.NxECommerceCommunityService;



@Service("nxECommerceCommunityService")
public class NxECommerceCommunityServiceImpl implements NxECommerceCommunityService {
	@Autowired
	private NxECommerceCommunityDao nxECommerceCommunityDao;
	
	@Override
	public NxECommerceCommunityEntity queryObject(Integer nxEccId){
		return nxECommerceCommunityDao.queryObject(nxEccId);
	}
	
	@Override
	public List<NxECommerceCommunityEntity> queryList(Map<String, Object> map){
		return nxECommerceCommunityDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxECommerceCommunityDao.queryTotal(map);
	}
	
	@Override
	public void save(NxECommerceCommunityEntity nxECommerceCommunity){
		nxECommerceCommunityDao.save(nxECommerceCommunity);
	}
	
	@Override
	public void update(NxECommerceCommunityEntity nxECommerceCommunity){
		nxECommerceCommunityDao.update(nxECommerceCommunity);
	}
	
	@Override
	public void delete(Integer nxEccId){
		nxECommerceCommunityDao.delete(nxEccId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxEccIds){
		nxECommerceCommunityDao.deleteBatch(nxEccIds);
	}

    @Override
    public List<NxCommunityEntity> queryCommunityByCommerceId(Integer commerceId) {

		return nxECommerceCommunityDao.queryCommunityByCommerceId(commerceId);

    }

}

package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityDao;
import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.service.NxCommunityService;



@Service("nxCommunityService")
public class NxCommunityServiceImpl implements NxCommunityService {
	@Autowired
	private NxCommunityDao nxCommunityDao;
	
	@Override
	public NxCommunityEntity queryObject(Integer nxCommunityId){
		return nxCommunityDao.queryObject(nxCommunityId);
	}
	
	@Override
	public List<NxCommunityEntity> queryList(Map<String, Object> map){
		return nxCommunityDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityEntity nxCommunity){
		nxCommunityDao.save(nxCommunity);
	}
	
	@Override
	public void update(NxCommunityEntity nxCommunity){
		nxCommunityDao.update(nxCommunity);
	}
	
	@Override
	public void delete(Integer nxCommunityId){
		nxCommunityDao.delete(nxCommunityId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityIds){
		nxCommunityDao.deleteBatch(nxCommunityIds);
	}


    @Override
    public List<NxCommunityEntity> queryDistributerCommunityList(Integer disId) {
        return nxCommunityDao.queryDistributerCommunityList(disId);
    }

}

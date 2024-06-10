package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityAdsenseDao;
import com.nongxinle.entity.NxCommunityAdsenseEntity;
import com.nongxinle.service.NxCommunityAdsenseService;



@Service("nxAdsenseService")
public class NxCommunityCommunityAdsenseServiceImpl implements NxCommunityAdsenseService {
	@Autowired
	private NxCommunityAdsenseDao nxCommunityAdsenseDao;
	
	@Override
	public NxCommunityAdsenseEntity queryObject(Integer nxAdsenseId){
		return nxCommunityAdsenseDao.queryObject(nxAdsenseId);
	}
	
	@Override
	public List<NxCommunityAdsenseEntity> queryList(Map<String, Object> map){
		return nxCommunityAdsenseDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityAdsenseDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityAdsenseEntity nxAdsense){
		nxCommunityAdsenseDao.save(nxAdsense);
	}
	
	@Override
	public void update(NxCommunityAdsenseEntity nxAdsense){
		nxCommunityAdsenseDao.update(nxAdsense);
	}
	
	@Override
	public void delete(Integer nxAdsenseId){
		nxCommunityAdsenseDao.delete(nxAdsenseId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxAdsenseIds){
		nxCommunityAdsenseDao.deleteBatch(nxAdsenseIds);
	}

    @Override
    public List<NxCommunityAdsenseEntity> getListByCommunityId(Integer communityId) {
        return nxCommunityAdsenseDao.getListByCommunityId(communityId);
    }

    @Override
    public List<NxCommunityAdsenseEntity> queryAdsenseByNxCommunityId(Integer communityId) {
        return nxCommunityAdsenseDao.queryAdsenseByNxCommunityId(communityId);
    }

}

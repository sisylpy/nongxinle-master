package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityPromoteDao;
import com.nongxinle.entity.NxCommunityPromoteEntity;
import com.nongxinle.service.NxCommunityPromoteService;



@Service("nxPromoteService")
public class NxCommunityCommunityPromoteServiceImpl implements NxCommunityPromoteService {
	@Autowired
	private NxCommunityPromoteDao nxCommunityPromoteDao;
	
	@Override
	public NxCommunityPromoteEntity queryObject(Integer nxPromoteId){
		return nxCommunityPromoteDao.queryObject(nxPromoteId);
	}
	
	@Override
	public List<NxCommunityPromoteEntity> queryList(Map<String, Object> map){
		return nxCommunityPromoteDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityPromoteDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityPromoteEntity nxPromote){
		nxCommunityPromoteDao.save(nxPromote);
	}
	
	@Override
	public void update(NxCommunityPromoteEntity nxPromote){
		nxCommunityPromoteDao.update(nxPromote);
	}
	
	@Override
	public void delete(Integer nxPromoteId){
		nxCommunityPromoteDao.delete(nxPromoteId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxPromoteIds){
		nxCommunityPromoteDao.deleteBatch(nxPromoteIds);
	}

    @Override
    public List<NxCommunityPromoteEntity> getListByCommunityId(Integer communityId) {
        return nxCommunityPromoteDao.getListByCommunityId(communityId);
    }

    @Override
    public List<NxCommunityPromoteEntity> queryPromoteByFatherId(Integer nxCommunityFatherGoodsId) {

		return nxCommunityPromoteDao.queryPromoteByFatherId(nxCommunityFatherGoodsId);
    }

}

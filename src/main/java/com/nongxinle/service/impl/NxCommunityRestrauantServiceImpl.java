package com.nongxinle.service.impl;

import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxRestrauntEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityRestrauantDao;
import com.nongxinle.entity.NxCommunityRestrauantEntity;
import com.nongxinle.service.NxCommunityRestrauantService;



@Service("nxCommunityRestrauantService")
public class NxCommunityRestrauantServiceImpl implements NxCommunityRestrauantService {
	@Autowired
	private NxCommunityRestrauantDao nxCommunityRestrauantDao;
	
	@Override
	public NxCommunityRestrauantEntity queryObject(Integer nxCommunityRestaruantId){
		return nxCommunityRestrauantDao.queryObject(nxCommunityRestaruantId);
	}
	
	@Override
	public List<NxCommunityRestrauantEntity> queryList(Map<String, Object> map){
		return nxCommunityRestrauantDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityRestrauantDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityRestrauantEntity nxCommunityRestrauant){
		nxCommunityRestrauantDao.save(nxCommunityRestrauant);
	}
	
	@Override
	public void update(NxCommunityRestrauantEntity nxCommunityRestrauant){
		nxCommunityRestrauantDao.update(nxCommunityRestrauant);
	}
	
	@Override
	public void delete(Integer nxCommunityRestaruantId){
		nxCommunityRestrauantDao.delete(nxCommunityRestaruantId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityRestaruantIds){
		nxCommunityRestrauantDao.deleteBatch(nxCommunityRestaruantIds);
	}

    @Override
    public List<NxRestrauntEntity> queryRestrauntsByComId(Integer comId) {

		return nxCommunityRestrauantDao.queryRestrauntsByComId(comId);
    }

    @Override
    public NxCommunityEntity queryCommunityByResId(Integer resId) {

		return nxCommunityRestrauantDao.queryCommunityByResId(resId);
    }

    @Override
    public List<NxRestrauntEntity> queryRestrauntsByParams(Map<String, Object> map) {

		return nxCommunityRestrauantDao.queryRestrauntsByParams(map);
    }

}

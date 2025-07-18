package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityDeskDao;
import com.nongxinle.entity.NxCommunityDeskEntity;
import com.nongxinle.service.NxCommunityDeskService;



@Service("nxCommunityDeskService")
public class NxCommunityDeskServiceImpl implements NxCommunityDeskService {
	@Autowired
	private NxCommunityDeskDao nxCommunityDeskDao;
	
	@Override
	public NxCommunityDeskEntity queryObject(Integer nxCommunityDeskId){
		return nxCommunityDeskDao.queryObject(nxCommunityDeskId);
	}
	
	@Override
	public List<NxCommunityDeskEntity> queryList(Map<String, Object> map){
		return nxCommunityDeskDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityDeskDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityDeskEntity nxCommunityDesk){
		nxCommunityDeskDao.save(nxCommunityDesk);
	}
	
	@Override
	public void update(NxCommunityDeskEntity nxCommunityDesk){
		nxCommunityDeskDao.update(nxCommunityDesk);
	}
	
	@Override
	public void delete(Integer nxCommunityDeskId){
		nxCommunityDeskDao.delete(nxCommunityDeskId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityDeskIds){
		nxCommunityDeskDao.deleteBatch(nxCommunityDeskIds);
	}

    @Override
    public List<NxCommunityDeskEntity> queryComDeskByParams(Map<String, Object> map) {
        return nxCommunityDeskDao.queryComDeskByParams(map);
    }

    @Override
    public NxCommunityDeskEntity queryDeskWithOrders(Map<String, Object> map) {

		return nxCommunityDeskDao.queryDeskWithOrders(map);
    }

}

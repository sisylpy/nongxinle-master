package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityCardDao;
import com.nongxinle.entity.NxCommunityCardEntity;
import com.nongxinle.service.NxCommunityCardService;



@Service("nxCommunityCardService")
public class NxCommunityCardServiceImpl implements NxCommunityCardService {
	@Autowired
	private NxCommunityCardDao nxCommunityCardDao;
	
	@Override
	public NxCommunityCardEntity queryObject(Integer nxCommunityCardId){
		return nxCommunityCardDao.queryObject(nxCommunityCardId);
	}
	
	@Override
	public List<NxCommunityCardEntity> queryList(Map<String, Object> map){
		return nxCommunityCardDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityCardDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityCardEntity nxCommunityCard){
		nxCommunityCardDao.save(nxCommunityCard);
	}
	
	@Override
	public void update(NxCommunityCardEntity nxCommunityCard){
		nxCommunityCardDao.update(nxCommunityCard);
	}
	
	@Override
	public void delete(Integer nxCommunityCardId){
		nxCommunityCardDao.delete(nxCommunityCardId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityCardIds){
		nxCommunityCardDao.deleteBatch(nxCommunityCardIds);
	}

    @Override
    public List<NxCommunityCardEntity> queryCardListByParams(Map<String, Object> map) {

		return nxCommunityCardDao.queryCardListByParams(map);
    }

}

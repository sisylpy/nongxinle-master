package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityGoodsSetPropertyDao;
import com.nongxinle.entity.NxCommunityGoodsSetPropertyEntity;
import com.nongxinle.service.NxCommunityGoodsSetPropertyService;



@Service("nxCommunityGoodsSetPropertyService")
public class NxCommunityGoodsSetPropertyServiceImpl implements NxCommunityGoodsSetPropertyService {
	@Autowired
	private NxCommunityGoodsSetPropertyDao nxCommunityGoodsSetPropertyDao;
	
	@Override
	public NxCommunityGoodsSetPropertyEntity queryObject(Integer nxCommunityGoodsSetPropertyId){
		return nxCommunityGoodsSetPropertyDao.queryObject(nxCommunityGoodsSetPropertyId);
	}
	
	@Override
	public List<NxCommunityGoodsSetPropertyEntity> queryList(Map<String, Object> map){
		return nxCommunityGoodsSetPropertyDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityGoodsSetPropertyDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityGoodsSetPropertyEntity nxCommunityGoodsSetProperty){
		nxCommunityGoodsSetPropertyDao.save(nxCommunityGoodsSetProperty);
	}
	
	@Override
	public void update(NxCommunityGoodsSetPropertyEntity nxCommunityGoodsSetProperty){
		nxCommunityGoodsSetPropertyDao.update(nxCommunityGoodsSetProperty);
	}
	
	@Override
	public void delete(Integer nxCommunityGoodsSetPropertyId){
		nxCommunityGoodsSetPropertyDao.delete(nxCommunityGoodsSetPropertyId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityGoodsSetPropertyIds){
		nxCommunityGoodsSetPropertyDao.deleteBatch(nxCommunityGoodsSetPropertyIds);
	}

    @Override
    public List<NxCommunityGoodsSetPropertyEntity> queryCgGoodsPropertyListByParams(Map<String, Object> mapP) {

		return nxCommunityGoodsSetPropertyDao.queryCgGoodsPropertyListByParams(mapP);
    }

}

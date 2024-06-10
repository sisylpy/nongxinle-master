package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityGoodsSetItemDao;
import com.nongxinle.entity.NxCommunityGoodsSetItemEntity;
import com.nongxinle.service.NxCommunityGoodsSetItemService;



@Service("nxCommunityGoodsSetItemService")
public class NxCommunityGoodsSetItemServiceImpl implements NxCommunityGoodsSetItemService {
	@Autowired
	private NxCommunityGoodsSetItemDao nxCommunityGoodsSetItemDao;
	
	@Override
	public NxCommunityGoodsSetItemEntity queryObject(Integer nxCommunityGoodsSetItemId){
		return nxCommunityGoodsSetItemDao.queryObject(nxCommunityGoodsSetItemId);
	}
	
	@Override
	public List<NxCommunityGoodsSetItemEntity> queryList(Map<String, Object> map){
		return nxCommunityGoodsSetItemDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityGoodsSetItemDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityGoodsSetItemEntity nxCommunityGoodsSetItem){
		nxCommunityGoodsSetItemDao.save(nxCommunityGoodsSetItem);
	}
	
	@Override
	public void update(NxCommunityGoodsSetItemEntity nxCommunityGoodsSetItem){
		nxCommunityGoodsSetItemDao.update(nxCommunityGoodsSetItem);
	}
	
	@Override
	public void delete(Integer nxCommunityGoodsSetItemId){
		nxCommunityGoodsSetItemDao.delete(nxCommunityGoodsSetItemId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityGoodsSetItemIds){
		nxCommunityGoodsSetItemDao.deleteBatch(nxCommunityGoodsSetItemIds);
	}


    @Override
    public List<NxCommunityGoodsSetItemEntity> queryCgGoodsSetListByParams(Map<String, Object> map) {

		return nxCommunityGoodsSetItemDao.queryCgGoodsSetListByParams(map);
    }

}

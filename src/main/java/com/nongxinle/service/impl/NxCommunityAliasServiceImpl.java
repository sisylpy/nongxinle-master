package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityAliasDao;
import com.nongxinle.entity.NxCommunityAliasEntity;
import com.nongxinle.service.NxCommunityAliasService;



@Service("nxCommunityAliasService")
public class NxCommunityAliasServiceImpl implements NxCommunityAliasService {
	@Autowired
	private NxCommunityAliasDao nxCommunityAliasDao;
	
	@Override
	public NxCommunityAliasEntity queryObject(Integer nxCommunityAliasId){
		return nxCommunityAliasDao.queryObject(nxCommunityAliasId);
	}
	
	@Override
	public List<NxCommunityAliasEntity> queryList(Map<String, Object> map){
		return nxCommunityAliasDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityAliasDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityAliasEntity nxCommunityAlias){
		nxCommunityAliasDao.save(nxCommunityAlias);
	}
	
	@Override
	public void update(NxCommunityAliasEntity nxCommunityAlias){
		nxCommunityAliasDao.update(nxCommunityAlias);
	}
	
	@Override
	public void delete(Integer nxCommunityAliasId){
		nxCommunityAliasDao.delete(nxCommunityAliasId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityAliasIds){
		nxCommunityAliasDao.deleteBatch(nxCommunityAliasIds);
	}

	@Override
	public List<NxCommunityAliasEntity> queryComAliasByComGoodsId(Integer comGoodsId) {
		return nxCommunityAliasDao.queryComAliasByComGoodsId(comGoodsId);
	}

}

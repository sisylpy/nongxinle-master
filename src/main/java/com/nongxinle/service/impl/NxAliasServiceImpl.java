package com.nongxinle.service.impl;

import com.nongxinle.entity.NxGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxAliasDao;
import com.nongxinle.entity.NxAliasEntity;
import com.nongxinle.service.NxAliasService;



@Service("nxAliasService")
public class NxAliasServiceImpl implements NxAliasService {
	@Autowired
	private NxAliasDao nxAliasDao;
	
	@Override
	public NxAliasEntity queryObject(Integer nxAliasId){
		return nxAliasDao.queryObject(nxAliasId);
	}
	
	@Override
	public List<NxAliasEntity> queryList(Map<String, Object> map){
		return nxAliasDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxAliasDao.queryTotal(map);
	}
	
	@Override
	public void save(NxAliasEntity nxAlias){
		nxAliasDao.save(nxAlias);
	}
	
	@Override
	public void update(NxAliasEntity nxAlias){
		nxAliasDao.update(nxAlias);
	}
	
	@Override
	public void delete(Integer nxAliasId){
		nxAliasDao.delete(nxAliasId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxAliasIds){
		nxAliasDao.deleteBatch(nxAliasIds);
	}

    @Override
    public List<NxAliasEntity> queryNxAliasList(Map<String, Object> map) {
		return nxAliasDao.queryNxAliasList(map);
    }

    @Override
    public List<NxGoodsEntity> queryNxGoodsByName(Map<String, Object> map) {

		return nxAliasDao.queryNxGoodsByName(map);
    }

}

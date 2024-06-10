package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerStandardDao;
import com.nongxinle.entity.NxDistributerStandardEntity;
import com.nongxinle.service.NxDistributerStandardService;



@Service("nxDistributerStandardService")
public class NxDistributerStandardServiceImpl implements NxDistributerStandardService {
	@Autowired
	private NxDistributerStandardDao nxDistributerStandardDao;


	@Override
	public List<NxDistributerStandardEntity> queryDisStandardByDisGoodsId(Integer nxDdgDisGoodsId) {

		return nxDistributerStandardDao.queryDisStandardByDisGoodsId(nxDdgDisGoodsId);
	}



	@Override
	public NxDistributerStandardEntity queryObject(Integer nxDistributerStandardId){
		return nxDistributerStandardDao.queryObject(nxDistributerStandardId);
	}
	
	@Override
	public List<NxDistributerStandardEntity> queryList(Map<String, Object> map){
		return nxDistributerStandardDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerStandardDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerStandardEntity nxDistributerStandard){
		nxDistributerStandardDao.save(nxDistributerStandard);
	}
	
	@Override
	public void update(NxDistributerStandardEntity nxDistributerStandard){
		nxDistributerStandardDao.update(nxDistributerStandard);
	}
	
	@Override
	public void delete(Integer nxDistributerStandardId){
		nxDistributerStandardDao.delete(nxDistributerStandardId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerStandardIds){
		nxDistributerStandardDao.deleteBatch(nxDistributerStandardIds);
	}

    @Override
    public List<NxDistributerStandardEntity> queryDisStandardByParams(Map<String, Object> map) {

		return nxDistributerStandardDao.queryDisStandardByParams(map);
    }


}

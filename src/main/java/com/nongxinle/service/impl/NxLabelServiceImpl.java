package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxLabelDao;
import com.nongxinle.entity.NxLabelEntity;
import com.nongxinle.service.NxLabelService;



@Service("nxLabelService")
public class NxLabelServiceImpl implements NxLabelService {
	@Autowired
	private NxLabelDao nxLabelDao;
	
	@Override
	public NxLabelEntity queryObject(Integer nxLabelId){
		return nxLabelDao.queryObject(nxLabelId);
	}
	
	@Override
	public List<NxLabelEntity> queryList(Map<String, Object> map){
		return nxLabelDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxLabelDao.queryTotal(map);
	}
	
	@Override
	public void save(NxLabelEntity nxLabel){
		nxLabelDao.save(nxLabel);
	}
	
	@Override
	public void update(NxLabelEntity nxLabel){
		nxLabelDao.update(nxLabel);
	}
	
	@Override
	public void delete(Integer nxLabelId){
		nxLabelDao.delete(nxLabelId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxLabelIds){
		nxLabelDao.deleteBatch(nxLabelIds);
	}
	
}

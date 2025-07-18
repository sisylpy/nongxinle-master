package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxAiForecastLogDao;
import com.nongxinle.entity.NxAiForecastLogEntity;
import com.nongxinle.service.NxAiForecastLogService;



@Service("nxAiForecastLogService")
public class NxAiForecastLogServiceImpl implements NxAiForecastLogService {
	@Autowired
	private NxAiForecastLogDao nxAiForecastLogDao;
	
	@Override
	public NxAiForecastLogEntity queryObject(Integer nxAiForecastLogId){
		return nxAiForecastLogDao.queryObject(nxAiForecastLogId);
	}
	
	@Override
	public List<NxAiForecastLogEntity> queryList(Map<String, Object> map){
		return nxAiForecastLogDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxAiForecastLogDao.queryTotal(map);
	}
	
	@Override
	public void save(NxAiForecastLogEntity nxAiForecastLog){
		nxAiForecastLogDao.save(nxAiForecastLog);
	}
	
	@Override
	public void update(NxAiForecastLogEntity nxAiForecastLog){
		nxAiForecastLogDao.update(nxAiForecastLog);
	}
	
	@Override
	public void delete(Integer nxAiForecastLogId){
		nxAiForecastLogDao.delete(nxAiForecastLogId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxAiForecastLogIds){
		nxAiForecastLogDao.deleteBatch(nxAiForecastLogIds);
	}
	
}

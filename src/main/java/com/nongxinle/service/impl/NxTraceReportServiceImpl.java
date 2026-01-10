package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxTraceReportDao;
import com.nongxinle.entity.NxTraceReportEntity;
import com.nongxinle.service.NxTraceReportService;



@Service("nxTraceReportService")
public class NxTraceReportServiceImpl implements NxTraceReportService {
	@Autowired
	private NxTraceReportDao nxTraceReportDao;
	
	@Override
	public NxTraceReportEntity queryObject(Integer nxTraceReportId){
		return nxTraceReportDao.queryObject(nxTraceReportId);
	}
	
	@Override
	public List<NxTraceReportEntity> queryList(Map<String, Object> map){
		return nxTraceReportDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxTraceReportDao.queryTotal(map);
	}
	
	@Override
	public void save(NxTraceReportEntity nxTraceReport){
		nxTraceReportDao.save(nxTraceReport);
	}
	
	@Override
	public void update(NxTraceReportEntity nxTraceReport){
		nxTraceReportDao.update(nxTraceReport);
	}
	
	@Override
	public void delete(Integer nxTraceReportId){
		nxTraceReportDao.delete(nxTraceReportId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxTraceReportIds){
		nxTraceReportDao.deleteBatch(nxTraceReportIds);
	}

	@Override
	public List<NxTraceReportEntity> queryTraceReportList(Map<String, Object> map) {
		return nxTraceReportDao.queryTraceReportList(map);
	}
}


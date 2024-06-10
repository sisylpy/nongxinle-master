package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbReportDao;
import com.nongxinle.entity.GbReportEntity;
import com.nongxinle.service.GbReportService;



@Service("gbReportService")
public class GbReportServiceImpl implements GbReportService {
	@Autowired
	private GbReportDao gbReportDao;
	
	@Override
	public GbReportEntity queryObject(Integer gbReportId){
		return gbReportDao.queryObject(gbReportId);
	}
	
	@Override
	public List<GbReportEntity> queryList(Map<String, Object> map){
		return gbReportDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbReportDao.queryTotal(map);
	}
	
	@Override
	public void save(GbReportEntity gbReport){
		gbReportDao.save(gbReport);
	}
	
	@Override
	public void update(GbReportEntity gbReport){
		gbReportDao.update(gbReport);
	}
	
	@Override
	public void delete(Integer gbReportId){
		gbReportDao.delete(gbReportId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbReportIds){
		gbReportDao.deleteBatch(gbReportIds);
	}

    @Override
    public List<GbReportEntity> queryReportList(Map<String, Object> map) {

		return gbReportDao.queryReportList(map);
    }

}

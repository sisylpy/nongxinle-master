package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepInventoryWeekDao;
import com.nongxinle.entity.GbDepInventoryWeekEntity;
import com.nongxinle.service.GbDepInventoryWeekService;



@Service("gbDepInventoryWeekService")
public class GbDepInventoryWeekServiceImpl implements GbDepInventoryWeekService {
	@Autowired
	private GbDepInventoryWeekDao gbDepInventoryWeekDao;
	
	@Override
	public GbDepInventoryWeekEntity queryObject(Integer gbInventoryWeekId){
		return gbDepInventoryWeekDao.queryObject(gbInventoryWeekId);
	}
	
	@Override
	public List<GbDepInventoryWeekEntity> queryList(Map<String, Object> map){
		return gbDepInventoryWeekDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepInventoryWeekDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepInventoryWeekEntity gbDepInventoryWeek){
		gbDepInventoryWeekDao.save(gbDepInventoryWeek);
	}
	
	@Override
	public void update(GbDepInventoryWeekEntity gbDepInventoryWeek){
		gbDepInventoryWeekDao.update(gbDepInventoryWeek);
	}
	
	@Override
	public void delete(Integer gbInventoryWeekId){
		gbDepInventoryWeekDao.delete(gbInventoryWeekId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbInventoryWeekIds){
		gbDepInventoryWeekDao.deleteBatch(gbInventoryWeekIds);
	}

    @Override
    public GbDepInventoryWeekEntity queryInventoryWeek(Map<String, Object> map1) {

		return gbDepInventoryWeekDao.queryInventoryWeek(map1);
    }

    @Override
    public List<GbDepInventoryWeekEntity> queryDepWeekList(Map<String, Object> map) {

		return gbDepInventoryWeekDao.queryDepWeekList(map);
    }

	@Override
	public Double queryInventoryWeekTotal(Map<String, Object> map) {

		return gbDepInventoryWeekDao.queryInventoryWeekTotal(map);
	}

    @Override
    public Double queryInventoryWeekWasteTotal(Map<String, Object> map) {

		return gbDepInventoryWeekDao.queryInventoryWeekWasteTotal(map);
    }

	@Override
	public Double queryInventoryWeekLossTotal(Map<String, Object> map) {

		return gbDepInventoryWeekDao.queryInventoryWeekLossTotal(map);
	}

    @Override
    public List<GbDepInventoryWeekEntity> queryInventoryWeekListByParams(Map<String, Object> map4) {

		return gbDepInventoryWeekDao.queryInventoryWeekListByParams(map4);
    }

    @Override
    public Double queryInventoryWeekReturnTotal(Map<String, Object> map) {

		return gbDepInventoryWeekDao.queryInventoryWeekReturnTotal(map);
    }


}

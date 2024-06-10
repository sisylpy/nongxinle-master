package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepInventoryMonthDao;
import com.nongxinle.entity.GbDepInventoryMonthEntity;
import com.nongxinle.service.GbDepInventoryMonthService;



@Service("gbDepInventoryMonthService")
public class GbDepInventoryMonthServiceImpl implements GbDepInventoryMonthService {
	@Autowired
	private GbDepInventoryMonthDao gbDepInventoryMonthDao;
	
	@Override
	public GbDepInventoryMonthEntity queryObject(Integer gbInventoryMonthId){
		return gbDepInventoryMonthDao.queryObject(gbInventoryMonthId);
	}
	
	@Override
	public List<GbDepInventoryMonthEntity> queryList(Map<String, Object> map){
		return gbDepInventoryMonthDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepInventoryMonthDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepInventoryMonthEntity gbDepInventoryMonth){
		gbDepInventoryMonthDao.save(gbDepInventoryMonth);
	}
	
	@Override
	public void update(GbDepInventoryMonthEntity gbDepInventoryMonth){
		gbDepInventoryMonthDao.update(gbDepInventoryMonth);
	}
	
	@Override
	public void delete(Integer gbInventoryMonthId){
		gbDepInventoryMonthDao.delete(gbInventoryMonthId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbInventoryMonthIds){
		gbDepInventoryMonthDao.deleteBatch(gbInventoryMonthIds);
	}

    @Override
    public GbDepInventoryMonthEntity queryInventoryMonth(Map<String, Object> map) {


		return gbDepInventoryMonthDao.queryInventoryMonth(map);
    }

    @Override
    public List<GbDepInventoryMonthEntity> queryDepMonthList(Map<String, Object> map) {

		return gbDepInventoryMonthDao.queryDepMonthList(map);
    }

    @Override
    public Double queryInventoryMonthTotal(Map<String, Object> map) {

		return gbDepInventoryMonthDao.queryInventoryMonthTotal(map);

    }

    @Override
    public Double queryInventoryMonthWasteTotal(Map<String, Object> map) {

		return gbDepInventoryMonthDao.queryInventoryMonthWasteTotal(map);
    }

	@Override
	public Double queryInventoryMonthLossTotal(Map<String, Object> map) {

		return gbDepInventoryMonthDao.queryInventoryMonthLossTotal(map);
	}

    @Override
    public List<GbDepInventoryMonthEntity> queryInventoryWeekListByParams(Map<String, Object> map5) {

		return gbDepInventoryMonthDao.queryInventoryWeekListByParams(map5);
    }

    @Override
    public Double queryInventoryMonthReturnTotal(Map<String, Object> map) {

		return gbDepInventoryMonthDao.queryInventoryMonthReturnTotal(map);
    }

}

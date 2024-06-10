package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepInventoryDailyDao;
import com.nongxinle.entity.GbDepInventoryDailyEntity;
import com.nongxinle.service.GbDepInventoryDailyService;



@Service("gbDepInventoryDailyService")
public class GbDepInventoryDailyServiceImpl implements GbDepInventoryDailyService {
	@Autowired
	private GbDepInventoryDailyDao gbDepInventoryDailyDao;
	
	@Override
	public GbDepInventoryDailyEntity queryObject(Integer gbInventoryDailyId){
		return gbDepInventoryDailyDao.queryObject(gbInventoryDailyId);
	}
	
	@Override
	public List<GbDepInventoryDailyEntity> queryList(Map<String, Object> map){
		return gbDepInventoryDailyDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepInventoryDailyDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepInventoryDailyEntity gbDepInventoryDaily){
		gbDepInventoryDailyDao.save(gbDepInventoryDaily);
	}
	
	@Override
	public void update(GbDepInventoryDailyEntity gbDepInventoryDaily){
		gbDepInventoryDailyDao.update(gbDepInventoryDaily);
	}
	
	@Override
	public void delete(Integer gbInventoryDailyId){
		gbDepInventoryDailyDao.delete(gbInventoryDailyId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbInventoryDailyIds){
		gbDepInventoryDailyDao.deleteBatch(gbInventoryDailyIds);
	}

    @Override
    public GbDepInventoryDailyEntity queryInventoryDaily(Map<String, Object> map) {

		return gbDepInventoryDailyDao.queryInventoryDaily(map);

    }

    @Override
    public List<GbDepInventoryDailyEntity> queryDepDailyList(Map<String, Object> map) {

		return gbDepInventoryDailyDao.queryDepDailyList(map);
    }

    @Override
    public Double queryInventoryDailyTotal(Map<String, Object> map) {

		return gbDepInventoryDailyDao.queryInventoryDailyTotal(map);
    }

    @Override
    public Double queryInventoryDailyWasteTotal(Map<String, Object> map) {

		return gbDepInventoryDailyDao.queryInventoryDailyWasteTotal(map);
    }

    @Override
    public Double queryInventoryDailyLossTotal(Map<String, Object> map) {

		return gbDepInventoryDailyDao.queryInventoryDailyLossTotal(map);
    }

    @Override
    public List<GbDepInventoryDailyEntity> queryInventoryDailyListByParams(Map<String, Object> map3) {

		return gbDepInventoryDailyDao.queryInventoryDailyListByParams(map3);
    }

    @Override
    public Double queryInventoryDailyReturnTotal(Map<String, Object> map) {

		return gbDepInventoryDailyDao.queryInventoryDailyReturnTotal(map);
    }

}

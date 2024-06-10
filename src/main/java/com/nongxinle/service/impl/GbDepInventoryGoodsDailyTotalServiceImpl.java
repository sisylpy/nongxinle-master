package com.nongxinle.service.impl;

import com.nongxinle.entity.GbDepartmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.dao.GbDepInventoryGoodsDailyTotalDao;
import com.nongxinle.entity.GbDepInventoryGoodsDailyTotalEntity;
import com.nongxinle.service.GbDepInventoryGoodsDailyTotalService;



@Service("gbDepInventoryGoodsDailyTotalService")
public class GbDepInventoryGoodsDailyTotalServiceImpl implements GbDepInventoryGoodsDailyTotalService {
	@Autowired
	private GbDepInventoryGoodsDailyTotalDao gbDepInventoryGoodsDailyTotalDao;
	
	@Override
	public GbDepInventoryGoodsDailyTotalEntity queryObject(Integer gbInventoryGoodsDailyTotalId){
		return gbDepInventoryGoodsDailyTotalDao.queryObject(gbInventoryGoodsDailyTotalId);
	}
	
	@Override
	public List<GbDepInventoryGoodsDailyTotalEntity> queryList(Map<String, Object> map){
		return gbDepInventoryGoodsDailyTotalDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepInventoryGoodsDailyTotalDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepInventoryGoodsDailyTotalEntity gbDepInventoryGoodsDailyTotal){
		gbDepInventoryGoodsDailyTotalDao.save(gbDepInventoryGoodsDailyTotal);
	}
	
	@Override
	public void update(GbDepInventoryGoodsDailyTotalEntity gbDepInventoryGoodsDailyTotal){
		gbDepInventoryGoodsDailyTotalDao.update(gbDepInventoryGoodsDailyTotal);
	}
	
	@Override
	public void delete(Integer gbInventoryGoodsDailyTotalId){
		gbDepInventoryGoodsDailyTotalDao.delete(gbInventoryGoodsDailyTotalId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbInventoryGoodsDailyTotalIds){
		gbDepInventoryGoodsDailyTotalDao.deleteBatch(gbInventoryGoodsDailyTotalIds);
	}

    @Override
    public GbDepInventoryGoodsDailyTotalEntity queryDailyTotalItemByParams(Map<String, Object> map) {

		return gbDepInventoryGoodsDailyTotalDao.queryDailyTotalItemByParams(map);
    }

    @Override
    public Integer queryDailyTotalAccount(Map<String, Object> map) {

		return gbDepInventoryGoodsDailyTotalDao.queryDailyTotalAccount(map);
    }

    @Override
    public List<GbDepInventoryGoodsDailyTotalEntity> queryDailyTotalListByParams(Map<String, Object> map) {

		return gbDepInventoryGoodsDailyTotalDao.queryDailyTotalListByParams(map);
    }

    @Override
    public Double queryDailyTotalProduceWeight(Map<String, Object> map) {

		return gbDepInventoryGoodsDailyTotalDao.queryDailyTotalProduceWeight(map);
    }

    @Override
    public TreeSet<GbDepartmentEntity> queryWhichDepsProduceWeight(Map<String, Object> mapDep) {

		return gbDepInventoryGoodsDailyTotalDao.queryWhichDepsProduceWeight(mapDep);
    }

    @Override
    public Double queryDailyTotalProduceWeightTotal(Map<String, Object> mapdepTotal) {

		return gbDepInventoryGoodsDailyTotalDao.queryDailyTotalProduceWeightTotal(mapdepTotal);
    }

    @Override
    public Double queryDailyTotalProfitTotal(Map<String, Object> map0) {

		return gbDepInventoryGoodsDailyTotalDao.queryDailyTotalProfitTotal(map0);
    }

    @Override
    public Double queryDailyTotalLossTotal(Map<String, Object> map) {

		return gbDepInventoryGoodsDailyTotalDao.queryDailyTotalLossTotal(map);
    }

    @Override
    public Double queryDailyTotalWasteTotal(Map<String, Object> map) {

		return gbDepInventoryGoodsDailyTotalDao.queryDailyTotalWasteTotal(map);
    }

    @Override
    public Double queryDailyTotalLossWeight(Map<String, Object> map) {

		return gbDepInventoryGoodsDailyTotalDao.queryDailyTotalLossWeight(map);
    }

	@Override
	public Double queryDailyTotalWasteWeight(Map<String, Object> map) {
		return gbDepInventoryGoodsDailyTotalDao.queryDailyTotalWasteWeight(map);
	}

}

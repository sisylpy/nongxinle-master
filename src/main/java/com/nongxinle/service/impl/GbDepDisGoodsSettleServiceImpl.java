package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDepDisGoodsSettleDao;
import com.nongxinle.entity.GbDepDisGoodsSettleEntity;
import com.nongxinle.service.GbDepDisGoodsSettleService;



@Service("gbDepDisGoodsSettleService")
public class GbDepDisGoodsSettleServiceImpl implements GbDepDisGoodsSettleService {
	@Autowired
	private GbDepDisGoodsSettleDao gbDepDisGoodsSettleDao;
	
	@Override
	public GbDepDisGoodsSettleEntity queryObject(Integer gbDepDisGoodsSettleId){
		return gbDepDisGoodsSettleDao.queryObject(gbDepDisGoodsSettleId);
	}
	
	@Override
	public List<GbDepDisGoodsSettleEntity> queryList(Map<String, Object> map){
		return gbDepDisGoodsSettleDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDepDisGoodsSettleDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDepDisGoodsSettleEntity gbDepDisGoodsSettle){
		gbDepDisGoodsSettleDao.save(gbDepDisGoodsSettle);
	}
	
	@Override
	public void update(GbDepDisGoodsSettleEntity gbDepDisGoodsSettle){
		gbDepDisGoodsSettleDao.update(gbDepDisGoodsSettle);
	}
	
	@Override
	public void delete(Integer gbDepDisGoodsSettleId){
		gbDepDisGoodsSettleDao.delete(gbDepDisGoodsSettleId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDepDisGoodsSettleIds){
		gbDepDisGoodsSettleDao.deleteBatch(gbDepDisGoodsSettleIds);
	}

    @Override
    public GbDepDisGoodsSettleEntity queryDisGoodsSettleByParams(Map<String, Object> mapGoodsSettle) {

		return gbDepDisGoodsSettleDao.queryDisGoodsSettleByParams(mapGoodsSettle);
    }

    @Override
    public List<GbDepDisGoodsSettleEntity> queryDepDisGoodsSettleByParams(Map<String, Object> map) {

		return gbDepDisGoodsSettleDao.queryDepDisGoodsSettleByParams(map);
    }

}

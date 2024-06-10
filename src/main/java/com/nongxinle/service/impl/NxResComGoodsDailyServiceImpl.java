package com.nongxinle.service.impl;

import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.entity.NxRestrauntComGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxResComGoodsDailyDao;
import com.nongxinle.entity.NxResComGoodsDailyEntity;
import com.nongxinle.service.NxResComGoodsDailyService;



@Service("nxResComGoodsDailyService")
public class NxResComGoodsDailyServiceImpl implements NxResComGoodsDailyService {
	@Autowired
	private NxResComGoodsDailyDao nxResComGoodsDailyDao;
	
	@Override
	public NxResComGoodsDailyEntity queryObject(Integer nxResComGoodsDailyId){
		return nxResComGoodsDailyDao.queryObject(nxResComGoodsDailyId);
	}
	
	@Override
	public List<NxResComGoodsDailyEntity> queryList(Map<String, Object> map){
		return nxResComGoodsDailyDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxResComGoodsDailyDao.queryTotal(map);
	}
	
	@Override
	public void save(NxResComGoodsDailyEntity nxResComGoodsDaily){
		nxResComGoodsDailyDao.save(nxResComGoodsDaily);
	}
	
	@Override
	public void update(NxResComGoodsDailyEntity nxResComGoodsDaily){
		nxResComGoodsDailyDao.update(nxResComGoodsDaily);
	}
	
	@Override
	public void delete(Integer nxResComGoodsDailyId){
		nxResComGoodsDailyDao.delete(nxResComGoodsDailyId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxResComGoodsDailyIds){
		nxResComGoodsDailyDao.deleteBatch(nxResComGoodsDailyIds);
	}

    @Override
    public NxResComGoodsDailyEntity queryDailyGoodsByParams(Map<String, Object> map) {

		return nxResComGoodsDailyDao.queryDailyGoodsByParams(map);
    }

    @Override
    public List<NxCommunityGoodsEntity> queryResDailyStatistics(Map<String, Object> map) {

		return nxResComGoodsDailyDao.queryResDailyStatistics(map);
    }

}

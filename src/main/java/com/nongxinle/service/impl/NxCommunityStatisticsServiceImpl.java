package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityStatisticsDao;
import com.nongxinle.entity.NxCommunityStatisticsEntity;
import com.nongxinle.service.NxCommunityStatisticsService;



@Service("nxCommunityStatisticsService")
public class NxCommunityStatisticsServiceImpl implements NxCommunityStatisticsService {
	@Autowired
	private NxCommunityStatisticsDao nxCommunityStatisticsDao;
	
	@Override
	public NxCommunityStatisticsEntity queryObject(Integer nxCommunityStatisticsId){
		return nxCommunityStatisticsDao.queryObject(nxCommunityStatisticsId);
	}
	
	@Override
	public List<NxCommunityStatisticsEntity> queryList(Map<String, Object> map){
		return nxCommunityStatisticsDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityStatisticsDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityStatisticsEntity nxCommunityStatistics){
		nxCommunityStatisticsDao.save(nxCommunityStatistics);
	}
	
	@Override
	public void update(NxCommunityStatisticsEntity nxCommunityStatistics){
		nxCommunityStatisticsDao.update(nxCommunityStatistics);
	}
	
	@Override
	public void delete(Integer nxCommunityStatisticsId){
		nxCommunityStatisticsDao.delete(nxCommunityStatisticsId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityStatisticsIds){
		nxCommunityStatisticsDao.deleteBatch(nxCommunityStatisticsIds);
	}

    @Override
    public List<NxCommunityStatisticsEntity>  queryStatisticsGoods(Map<String, Object> map1) {

		return nxCommunityStatisticsDao.queryStatisticsGoods(map1);
    }

	@Override
	public List<NxCommunityStatisticsEntity> querySt(Map<String, Object> map1) {
		return nxCommunityStatisticsDao.querySt(map1);
	}

	@Override
	public float queryStWeekProfitSum(Map<String, Object> map) {
		return nxCommunityStatisticsDao.queryStWeekProfitSum(map);
	}


}

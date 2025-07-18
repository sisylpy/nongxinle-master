package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerPayListDao;
import com.nongxinle.entity.GbDistributerPayListEntity;
import com.nongxinle.service.GbDistributerPayListService;



@Service("gbDistributerPayListService")
public class GbDistributerPayListServiceImpl implements GbDistributerPayListService {
	@Autowired
	private GbDistributerPayListDao gbDistributerPayListDao;
	
	@Override
	public GbDistributerPayListEntity queryObject(Integer gbDistributerPayListId){
		return gbDistributerPayListDao.queryObject(gbDistributerPayListId);
	}
	
	@Override
	public List<GbDistributerPayListEntity> queryList(Map<String, Object> map){
		return gbDistributerPayListDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerPayListDao.queryTotal(map);
	}
	
	@Override
	public void save(GbDistributerPayListEntity gbDistributerPayList){
		gbDistributerPayListDao.save(gbDistributerPayList);
	}
	
	@Override
	public void update(GbDistributerPayListEntity gbDistributerPayList){
		gbDistributerPayListDao.update(gbDistributerPayList);
	}
	
	@Override
	public void delete(Integer gbDistributerPayListId){
		gbDistributerPayListDao.delete(gbDistributerPayListId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerPayListIds){
		gbDistributerPayListDao.deleteBatch(gbDistributerPayListIds);
	}

    @Override
    public List<GbDistributerPayListEntity> queryPayListListByParams(Map<String, Object> map) {

		return gbDistributerPayListDao.queryPayListListByParams(map);
    }

    @Override
    public int queryDisPayListCount(Map<String, Object> mapCount) {

		return gbDistributerPayListDao.queryDisPayListCount(mapCount);
    }

}

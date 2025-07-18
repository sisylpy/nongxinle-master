package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunitySplicingOrdersDao;
import com.nongxinle.entity.NxCommunitySplicingOrdersEntity;
import com.nongxinle.service.NxCommunitySplicingOrdersService;



@Service("nxCommunitySplicingOrdersService")
public class NxCommunitySplicingOrdersServiceImpl implements NxCommunitySplicingOrdersService {
	@Autowired
	private NxCommunitySplicingOrdersDao nxCommunitySplicingOrdersDao;
	
	@Override
	public NxCommunitySplicingOrdersEntity queryObject(Integer nxCommunitySplicingOrdersId){
		return nxCommunitySplicingOrdersDao.queryObject(nxCommunitySplicingOrdersId);
	}
	
	@Override
	public List<NxCommunitySplicingOrdersEntity> queryList(Map<String, Object> map){
		return nxCommunitySplicingOrdersDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunitySplicingOrdersDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunitySplicingOrdersEntity nxCommunitySplicingOrders){
		nxCommunitySplicingOrdersDao.save(nxCommunitySplicingOrders);
	}
	
	@Override
	public void update(NxCommunitySplicingOrdersEntity nxCommunitySplicingOrders){
		nxCommunitySplicingOrdersDao.update(nxCommunitySplicingOrders);
	}
	
	@Override
	public void delete(Integer nxCommunitySplicingOrdersId){
		System.out.println("delelelelelsploeooeo");
		nxCommunitySplicingOrdersDao.delete(nxCommunitySplicingOrdersId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunitySplicingOrdersIds){
		nxCommunitySplicingOrdersDao.deleteBatch(nxCommunitySplicingOrdersIds);
	}

    @Override
    public NxCommunitySplicingOrdersEntity queryNewPindan(Map<String, Object> map) {

		return nxCommunitySplicingOrdersDao.queryNewPindan(map);
    }

    @Override
    public List<NxCommunitySplicingOrdersEntity> querySplicingListByParams(Map<String, Object> map) {

		return nxCommunitySplicingOrdersDao.querySplicingListByParams(map);
    }




}

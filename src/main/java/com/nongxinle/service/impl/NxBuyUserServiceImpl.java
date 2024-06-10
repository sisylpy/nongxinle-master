package com.nongxinle.service.impl;

import com.nongxinle.entity.NxDistributerPurchaseBatchEntity;
import com.nongxinle.entity.NxRestrauntUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxBuyUserDao;
import com.nongxinle.entity.NxBuyUserEntity;
import com.nongxinle.service.NxBuyUserService;



@Service("nxBuyUserService")
public class NxBuyUserServiceImpl implements NxBuyUserService {
	@Autowired
	private NxBuyUserDao nxBuyUserDao;
	
	@Override
	public NxBuyUserEntity queryObject(Integer nxBuyUserId){
		return nxBuyUserDao.queryObject(nxBuyUserId);
	}
	
	@Override
	public List<NxBuyUserEntity> queryList(Map<String, Object> map){
		return nxBuyUserDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxBuyUserDao.queryTotal(map);
	}
	
	@Override
	public void save(NxBuyUserEntity nxBuyUser){
		nxBuyUserDao.save(nxBuyUser);
	}
	
	@Override
	public void update(NxBuyUserEntity nxBuyUser){
		nxBuyUserDao.update(nxBuyUser);
	}
	
	@Override
	public void delete(Integer nxBuyUserId){
		nxBuyUserDao.delete(nxBuyUserId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxBuyUserIds){
		nxBuyUserDao.deleteBatch(nxBuyUserIds);
	}



    @Override
    public List<NxDistributerPurchaseBatchEntity> queryBuyerPurchaseBatchDayWork(Map<String, Object> mapZero) {

		return nxBuyUserDao.queryBuyerPurchaseBatchDayWork(mapZero);
    }

    @Override
    public List<NxBuyUserEntity> queryAllNxBuyerUsers() {

		return nxBuyUserDao.queryAllNxBuyerUsers();
    }

}

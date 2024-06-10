package com.nongxinle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxPrinterUserDao;
import com.nongxinle.entity.NxPrinterUserEntity;
import com.nongxinle.service.NxPrinterUserService;



@Service("nxPrinterUserService")
public class NxPrinterUserServiceImpl implements NxPrinterUserService {
	@Autowired
	private NxPrinterUserDao nxPrinterUserDao;
	
	@Override
	public NxPrinterUserEntity queryObject(Integer nxPrinterUserId){
		return nxPrinterUserDao.queryObject(nxPrinterUserId);
	}
	
	@Override
	public List<NxPrinterUserEntity> queryList(Map<String, Object> map){
		return nxPrinterUserDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxPrinterUserDao.queryTotal(map);
	}
	
	@Override
	public void save(NxPrinterUserEntity nxPrinterUser){
		nxPrinterUserDao.save(nxPrinterUser);
	}
	
	@Override
	public void update(NxPrinterUserEntity nxPrinterUser){
		nxPrinterUserDao.update(nxPrinterUser);
	}
	
	@Override
	public void delete(Integer nxPrinterUserId){
		nxPrinterUserDao.delete(nxPrinterUserId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxPrinterUserIds){
		nxPrinterUserDao.deleteBatch(nxPrinterUserIds);
	}

    @Override
    public NxPrinterUserEntity queryPrinterUserByOpenId(String openId) {

		return nxPrinterUserDao.queryPrinterUserByOpenId(openId);
    }

}

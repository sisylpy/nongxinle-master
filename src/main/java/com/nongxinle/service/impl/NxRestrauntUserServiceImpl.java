package com.nongxinle.service.impl;

import com.nongxinle.entity.NxRestrauntEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxRestrauntUserDao;
import com.nongxinle.entity.NxRestrauntUserEntity;
import com.nongxinle.service.NxRestrauntUserService;



@Service("nxRestrauntUserService")
public class NxRestrauntUserServiceImpl implements NxRestrauntUserService {
	@Autowired
	private NxRestrauntUserDao nxRestrauntUserDao;
	
	@Override
	public NxRestrauntUserEntity queryObject(Integer nxRestrauntUserId){
		return nxRestrauntUserDao.queryObject(nxRestrauntUserId);
	}
	
	@Override
	public List<NxRestrauntUserEntity> queryList(Map<String, Object> map){
		return nxRestrauntUserDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxRestrauntUserDao.queryTotal(map);
	}
	
	@Override
	public void save(NxRestrauntUserEntity nxRestrauntUser){
		nxRestrauntUserDao.save(nxRestrauntUser);
	}
	
	@Override
	public void update(NxRestrauntUserEntity nxRestrauntUser){
		nxRestrauntUserDao.update(nxRestrauntUser);
	}
	
	@Override
	public void delete(Integer nxRestrauntUserId){
		nxRestrauntUserDao.delete(nxRestrauntUserId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxRestrauntUserIds){
		nxRestrauntUserDao.deleteBatch(nxRestrauntUserIds);
	}

    @Override
    public NxRestrauntUserEntity queryResUserByOpenId(String openid) {

		return nxRestrauntUserDao.queryResUserByOpenId(openid);
    }

    @Override
    public NxRestrauntEntity queryAllResUsersByResId(Integer resId) {

		return nxRestrauntUserDao.queryAllResUsersByResId(resId);
    }

    @Override
    public NxRestrauntEntity queryAllRestrauntAndDepUsersByResId(Integer resId) {

		return nxRestrauntUserDao.queryAllRestrauntAndDepUsersByResId(resId);
    }

	@Override
	public NxRestrauntEntity queryChainRestrauntAndDepUsersByResId(Integer resId) {
		return nxRestrauntUserDao.queryChainRestrauntAndDepUsersByResId(resId);
	}

}

package com.nongxinle.service.impl;

import com.nongxinle.entity.NxBuyUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxJrdhUserDao;
import com.nongxinle.entity.NxJrdhUserEntity;
import com.nongxinle.service.NxJrdhUserService;



@Service("nxJrdhUserService")
public class NxJrdhUserServiceImpl implements NxJrdhUserService {
	@Autowired
	private NxJrdhUserDao nxJrdhUserDao;
	
	@Override
	public NxJrdhUserEntity queryObject(Integer nxJrdhUserId){
		return nxJrdhUserDao.queryObject(nxJrdhUserId);
	}
	
	@Override
	public List<NxJrdhUserEntity> queryList(Map<String, Object> map){
		return nxJrdhUserDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxJrdhUserDao.queryTotal(map);
	}
	
	@Override
	public void save(NxJrdhUserEntity nxJrdhUser){
		nxJrdhUserDao.save(nxJrdhUser);
	}
	
	@Override
	public void update(NxJrdhUserEntity nxJrdhUser){
		nxJrdhUserDao.update(nxJrdhUser);
	}
	
	@Override
	public void delete(Integer nxJrdhUserId){
		nxJrdhUserDao.delete(nxJrdhUserId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxJrdhUserIds){
		nxJrdhUserDao.deleteBatch(nxJrdhUserIds);
	}

    @Override
    public NxJrdhUserEntity queryWhichUserByOpenId(String openid) {

		return nxJrdhUserDao.queryWhichUserByOpenId(openid);
    }

    @Override
    public List<NxJrdhUserEntity> queryJrdhUserByParams(Map<String, Object> map) {

		return nxJrdhUserDao.queryJrdhUserByParams(map);
    }

    @Override
    public NxJrdhUserEntity queryJrdhUserById(Map<String, Object> map) {

		return nxJrdhUserDao.queryJrdhUserById(map);
    }

    @Override
    public NxJrdhUserEntity queryDisUserByOpenId(String openId) {

		return nxJrdhUserDao.queryDisUserByOpenId(openId);
    }

	@Override
	public NxJrdhUserEntity querySelluserByOpenId(String openId) {

		return nxJrdhUserDao.querySelluserByOpenId(openId);
	}

    @Override
    public NxJrdhUserEntity queryJrdhUserByAdmin(Map<String, Object> map) {

		return nxJrdhUserDao.queryJrdhUserByAdmin(map);
    }


}

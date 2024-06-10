package com.nongxinle.service.impl;

import com.nongxinle.dao.GbDistributerDao;
import com.nongxinle.entity.GbDistributerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.GbDistributerUserDao;
import com.nongxinle.entity.GbDistributerUserEntity;
import com.nongxinle.service.GbDistributerUserService;



@Service("gbDistributerUserService")
public class GbDistributerUserServiceImpl implements GbDistributerUserService {
	@Autowired
	private GbDistributerUserDao gbDistributerUserDao;
	@Autowired
	private GbDistributerDao gbDistributerDao;
	
	@Override
	public GbDistributerUserEntity queryObject(Integer gbDistributerUserId){
		return gbDistributerUserDao.queryObject(gbDistributerUserId);
	}
	
	@Override
	public List<GbDistributerUserEntity> queryList(Map<String, Object> map){
		return gbDistributerUserDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return gbDistributerUserDao.queryTotal(map);
	}
	
	@Override
	public Integer save(GbDistributerUserEntity gbDistributerUser){
		gbDistributerUserDao.save(gbDistributerUser);

		return gbDistributerUser.getGbDistributerUserId();
	}
	
	@Override
	public void update(GbDistributerUserEntity gbDistributerUser){
		gbDistributerUserDao.update(gbDistributerUser);
	}
	
	@Override
	public void delete(Integer gbDistributerUserId){
		gbDistributerUserDao.delete(gbDistributerUserId);
	}
	
	@Override
	public void deleteBatch(Integer[] gbDistributerUserIds){
		gbDistributerUserDao.deleteBatch(gbDistributerUserIds);
	}

    @Override
    public GbDistributerUserEntity queryDisUserByOpenIdGb(String openid) {

		return gbDistributerUserDao.queryDisUserByOpenIdGb(openid);
    }


	@Override
	public Map<String, Object> queryDisAndUserInfo(Integer resUserId) {

		//订货群信息
		GbDistributerUserEntity gbDistributerUserEntity = gbDistributerUserDao.queryUserInfo(resUserId);
		//用户信息
		Integer diuDistributerId = gbDistributerUserEntity.getGbDiuDistributerId();
		GbDistributerEntity gbDistributerEntity = gbDistributerDao.queryDisInfo(diuDistributerId);

		//返回
		Map<String, Object> map = new HashMap<>();
		map.put("userInfo", gbDistributerUserEntity);
		map.put("disInfo", gbDistributerEntity);
		return map;
	}

    @Override
    public GbDistributerUserEntity queryUserInfo(Integer userId) {

		return gbDistributerUserDao.queryUserInfo(userId);
    }

    @Override
    public List<GbDistributerUserEntity> queryUserByOpenId(String openid) {

		return gbDistributerUserDao.queryUserByOpenId(openid);
    }

	@Override
	public List<GbDistributerUserEntity> getAllUserByDisId(Integer disId) {

		return gbDistributerUserDao.getAllUserByDisId(disId);
	}

    @Override
    public List<GbDistributerUserEntity> queryGbUser(Integer disId) {

		return gbDistributerDao.queryGbUser(disId);

    }

    @Override
    public List<GbDistributerUserEntity> queryGbDisUserByParams(Map<String, Object> map) {

		return gbDistributerUserDao.queryGbDisUserByParams(map);
    }

    @Override
    public List<GbDistributerUserEntity> queryAllGbUsers() {

		return gbDistributerUserDao.queryAllGbUsers();
    }

}

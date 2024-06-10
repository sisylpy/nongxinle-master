package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDistributerUserRoleDao;
import com.nongxinle.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxDistributerUserDao;
import com.nongxinle.service.NxDistributerUserService;



@Service("nxDistributerUserService")
public class NxDistributerUserServiceImpl implements NxDistributerUserService {
	@Autowired
	private NxDistributerUserDao nxDistributerUserDao;

	@Autowired
	private NxDistributerUserRoleDao nxDistributerUserRoleDao;

	@Override
	public List<NxDistributerUserEntity> getAllUserByDisId(Integer disId) {
		return nxDistributerUserDao.queryAllUsersByDisId(disId);
	}

	@Override
	public NxDistributerUserEntity queryObject(Integer nxDistributerUserId){
		return nxDistributerUserDao.queryObject(nxDistributerUserId);
	}
	
	@Override
	public List<NxDistributerUserEntity> queryList(Map<String, Object> map){
		return nxDistributerUserDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxDistributerUserDao.queryTotal(map);
	}
	
	@Override
	public void save(NxDistributerUserEntity nxDistributerUser){

		nxDistributerUserDao.save(nxDistributerUser);
//		Integer nxDistributerUserId = nxDistributerUser.getNxDistributerUserId();
//		System.out.println(nxDistributerUserId + "iddididi");
//
//		List<NxDistributerUserRoleEntity> roleEntities = nxDistributerUser.getRoleEntities();
//		if(roleEntities != null){
//			for (NxDistributerUserRoleEntity role : roleEntities) {
//				Integer nxDurRoleId = role.getNxDurRoleId();
//				NxDistributerUserRoleEntity roleEntity = new NxDistributerUserRoleEntity();
//				roleEntity.setNxDurUserId(nxDistributerUserId);
//				roleEntity.setNxDurRoleId(nxDurRoleId);
//				nxDistributerUserRoleDao.save(roleEntity);
//			}
//		}

	}
	
	@Override
	public void update(NxDistributerUserEntity nxDistributerUser){
		nxDistributerUserDao.update(nxDistributerUser);
	}
	
	@Override
	public void delete(Integer nxDistributerUserId){
		nxDistributerUserDao.delete(nxDistributerUserId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxDistributerUserIds){
		nxDistributerUserDao.deleteBatch(nxDistributerUserIds);
	}

    @Override
    public List<NxDistributerUserEntity> queryUser(Integer disId) {

		return nxDistributerUserDao.queryUser(disId);
    }

	@Override
	public NxDistributerUserEntity queryUserInfo(Integer nxDistributerUserId) {
		return nxDistributerUserDao.queryUserInfo(nxDistributerUserId);
	}

    @Override
    public NxDistributerUserEntity queryUserByOpenId(String openid) {

		return nxDistributerUserDao.queryUserByOpenId(openid);
    }

    @Override
    public Map<String, Object> queryNxDisAndUserInfo(Integer userId) {
		//订货群信息
		NxDistributerUserEntity nxDistributerUserEntity = nxDistributerUserDao.queryUserInfo(userId);
		//用户信息
		Integer diuDistributerId = nxDistributerUserEntity.getNxDiuDistributerId();
		NxDistributerEntity nxDistributerEntity = nxDistributerUserDao.queryNxDisInfo(diuDistributerId);

		//返回
		Map<String, Object> map = new HashMap<>();
		map.put("userInfo", nxDistributerUserEntity);
		map.put("disInfo", nxDistributerEntity);
		return map;
    }

    @Override
    public List<NxDistributerUserEntity> getAllDisUsers() {

		return nxDistributerUserDao.getAllDisUsers();
    }

    @Override
    public List<NxDistributerUserEntity> queryRoleNxDisRoleUserList(Map<String, Object> map) {

		return nxDistributerUserDao.queryRoleNxDisRoleUserList(map);
    }

    @Override
    public NxDistributerUserEntity queryDisUserByRoleAndOpen(Map<String, Object> map1) {

		return nxDistributerUserDao.queryDisUserByRoleAndOpen(map1);
    }

    @Override
    public List<NxDistributerUserEntity> getAdminUserByParams(Map<String, Object> map) {

		return nxDistributerUserDao.getAdminUserByParams(map);
    }


}

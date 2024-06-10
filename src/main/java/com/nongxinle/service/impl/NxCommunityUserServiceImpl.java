package com.nongxinle.service.impl;

import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.entity.NxRestrauntEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityUserDao;
import com.nongxinle.entity.NxCommunityUserEntity;
import com.nongxinle.service.NxCommunityUserService;



@Service("nxCommunityUserService")
public class NxCommunityUserServiceImpl implements NxCommunityUserService {
	@Autowired
	private NxCommunityUserDao nxCommunityUserDao;
	
	@Override
	public NxCommunityUserEntity queryObject(Integer nxCommunityUserId){
		return nxCommunityUserDao.queryObject(nxCommunityUserId);
	}
	
	@Override
	public List<NxCommunityUserEntity> queryList(Map<String, Object> map){
		return nxCommunityUserDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map){
		return nxCommunityUserDao.queryTotal(map);
	}
	
	@Override
	public void save(NxCommunityUserEntity nxCommunityUser){
		nxCommunityUserDao.save(nxCommunityUser);
	}
	
	@Override
	public void update(NxCommunityUserEntity nxCommunityUser){
		nxCommunityUserDao.update(nxCommunityUser);
	}
	
	@Override
	public void delete(Integer nxCommunityUserId){
		nxCommunityUserDao.delete(nxCommunityUserId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxCommunityUserIds){
		nxCommunityUserDao.deleteBatch(nxCommunityUserIds);
	}

    @Override
    public NxCommunityUserEntity queryComUserByOpenId(Map<String, Object> map) {
        return nxCommunityUserDao.queryComUserByOpenId(map);
    }

	@Override
	public NxCommunityUserEntity queryComUserInfo(Map<String, Object> map) {
		return nxCommunityUserDao.queryComUserInfo(map);
	}

    @Override
    public List<NxCommunityUserEntity> queryCommunityRoleUsers(Map<String, Object> map) {

		return nxCommunityUserDao.queryCommunityRoleUsers(map);
    }

    @Override
    public List<NxRestrauntEntity> queryDeliveryRestrauntsByDriverId(Map<String, Object> map){

		return nxCommunityUserDao.queryDeliveryRestrauntsByDriverId(map);
    }

    @Override
    public List<NxCommunityUserEntity> getAdmainUserByComId(Integer comId) {

		return nxCommunityUserDao.getAdmainUserByComId(comId);
    }

    @Override
    public NxCommunityUserEntity queryUserByPhone(String nxCouWxPhone) {

		return nxCommunityUserDao.queryUserByPhone(nxCouWxPhone);
    }


}

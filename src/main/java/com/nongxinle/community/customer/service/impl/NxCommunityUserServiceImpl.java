package com.nongxinle.community.customer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.nongxinle.dao.NxCommunityUserDao;
import com.nongxinle.entity.NxCommunityUserEntity;
import com.nongxinle.community.promotion.service.NxCommunityUserPromotionEligibleService;
import com.nongxinle.community.customer.service.NxCommunityUserService;
import com.nongxinle.utils.CustomerReferralConstants;



@Service("nxCommunityUserService")
public class NxCommunityUserServiceImpl implements NxCommunityUserService {
	@Autowired
	private NxCommunityUserDao nxCommunityUserDao;
	@Autowired
	@Lazy
	private NxCommunityUserPromotionEligibleService nxCommunityUserPromotionEligibleService;
	
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
		if (nxCommunityUser.getNxCommunityUserId() != null && nxCommunityUser.getNxCouWorkingStatus() != null) {
			NxCommunityUserEntity old = nxCommunityUserDao.queryObject(nxCommunityUser.getNxCommunityUserId());
			if (old != null && old.getNxCouWorkingStatus() != null
					&& old.getNxCouWorkingStatus() == CustomerReferralConstants.COMMUNITY_USER_WORKING_ACTIVE
					&& !nxCommunityUser.getNxCouWorkingStatus().equals(CustomerReferralConstants.COMMUNITY_USER_WORKING_ACTIVE)) {
				nxCommunityUserPromotionEligibleService.deactivatePromotionAssets(
						nxCommunityUser.getNxCommunityUserId(), "工作人员停用或离职");
			}
		}
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
    public List<NxCommunityUserEntity> getAdmainUserByComId(Integer comId) {

		return nxCommunityUserDao.getAdmainUserByComId(comId);
    }

    @Override
    public NxCommunityUserEntity queryUserByPhone(String nxCouWxPhone) {

		return nxCommunityUserDao.queryUserByPhone(nxCouWxPhone);
    }


}

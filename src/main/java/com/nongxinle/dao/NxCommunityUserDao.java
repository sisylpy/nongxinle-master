package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 11-30 21:47
 */

import com.nongxinle.entity.NxCommunityUserEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.entity.NxRestrauntEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityUserDao extends BaseDao<NxCommunityUserEntity> {



    NxCommunityUserEntity queryComUserByOpenId(Map<String, Object> map);

    NxCommunityUserEntity queryComUserInfo(Map<String, Object> map);

    List<NxCommunityUserEntity> queryCommunityRoleUsers(Map<String, Object> map);

    List<NxRestrauntEntity> queryDeliveryRestrauntsByDriverId(Map<String, Object> map);

    List<NxCommunityUserEntity> getAdmainUserByComId(Integer comId);

    NxCommunityUserEntity queryUserByPhone(String nxCouWxPhone);
}

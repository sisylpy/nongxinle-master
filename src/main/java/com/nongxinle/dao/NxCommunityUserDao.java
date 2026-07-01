package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 11-30 21:47
 */

import com.nongxinle.entity.NxCommunityUserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface NxCommunityUserDao extends BaseDao<NxCommunityUserEntity> {



    NxCommunityUserEntity queryComUserByOpenId(Map<String, Object> map);

    NxCommunityUserEntity queryDriverByOpenId(@Param("openId") String openId);

    NxCommunityUserEntity queryComUserInfo(Map<String, Object> map);

    List<NxCommunityUserEntity> queryCommunityRoleUsers(Map<String, Object> map);

    List<NxCommunityUserEntity> getAdmainUserByComId(Integer comId);

    List<NxCommunityUserEntity> getDriverUsersByComId(Integer comId);

    NxCommunityUserEntity queryUserByPhone(String nxCouWxPhone);

}

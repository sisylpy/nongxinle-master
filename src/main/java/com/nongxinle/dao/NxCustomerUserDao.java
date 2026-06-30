package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-14 17:42
 */

import com.nongxinle.entity.NxCustomerUserEntity;

import java.util.List;
import java.util.Map;


public interface NxCustomerUserDao extends BaseDao<NxCustomerUserEntity> {



    NxCustomerUserEntity queryUserByOpenId(String openid);

    Map<String, Object> queryCustomerUserInfo(Integer gbDepartmentUserId);

    List<NxCustomerUserEntity> queryCustomerByParams(Map<String, Object> map);

    Integer queryCustomerUserCount(Map<String, Object> map);

    Integer queryCommerceCustomerUserCount(Map<String, Object> map);

    NxCustomerUserEntity queryUserWithAddress(Integer gbDepartmentUserId);

    NxCustomerUserEntity queryUserByOpenIdAndCommerceId(Map<String, Object> mapU);

    /** POS 会员搜索：本店注册或拥有本店券的会员 */
    List<NxCustomerUserEntity> posSearchMembersByKeyword(Map<String, Object> map);
}

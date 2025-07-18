package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 09-20 00:57
 */

import com.nongxinle.entity.NxCustomerUserAddressEntity;

import java.util.List;


public interface NxCustomerUserAddressDao extends BaseDao<NxCustomerUserAddressEntity> {

    List<NxCustomerUserAddressEntity> queryAddressByUserId(Integer userId);

    NxCustomerUserAddressEntity queryMainAddressByUserId(Integer nxCuaCustomerUserId);

}

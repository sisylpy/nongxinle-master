package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDistributerEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerDao extends BaseDao<NxDistributerEntity> {

    NxDistributerEntity gbDepQueryDistributerInfo(Map<String, Object> map);

    List<NxDistributerEntity> queryNxDisCustomerBySellerOpenId(String openId);

    NxDistributerEntity queryNxDisInfo(Integer id);

    List<NxDistributerEntity> queryAllTypeOne();

}

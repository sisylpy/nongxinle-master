package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 06-16 11:26
 */

import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDepartmentUserEntity;

import java.util.List;
import java.util.Map;


public interface NxDepartmentUserDao extends BaseDao<NxDepartmentUserEntity> {

    List<NxDepartmentUserEntity> queryAllUsersByDepId(Integer depId);

    NxDepartmentUserEntity queryDepUserByOpenId(String openId);

    List<NxDepartmentUserEntity> queryGroupAdminUserAmount(Integer nxDuDepartmentId);

    List<NxDepartmentEntity> queryMultiDepartmentByWxOpenId(String openId);

    List<NxDepartmentUserEntity> queryDepUsersByDepId(Integer depId);

    List<NxDepartmentUserEntity> queryAllDepUsers();

    List<NxDepartmentUserEntity> queryAllUsersByDepFatherId(Integer depId);

    NxDepartmentUserEntity queryDepUserInfo(Map<String, Object> map);

    NxDepartmentUserEntity queryDepUserByMachineId(String id);
}

package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 08-19 12:35
 */

import com.nongxinle.entity.SysBusinessTypeEntity;

import java.util.List;
import java.util.Map;


public interface SysBusinessTypeDao extends BaseDao<SysBusinessTypeEntity> {

    List<SysBusinessTypeEntity> queryTypeNxDistribterWithPeisong(Map<String, Object> map);
}

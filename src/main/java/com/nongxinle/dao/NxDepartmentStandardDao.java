package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 08-11 22:02
 */

import com.nongxinle.entity.NxDepartmentStandardEntity;

import java.util.List;


public interface NxDepartmentStandardDao extends BaseDao<NxDepartmentStandardEntity> {

    List<NxDepartmentStandardEntity> queryDepGoodsStandards(Integer depGoodsId);
}

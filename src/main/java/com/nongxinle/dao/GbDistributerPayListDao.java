package com.nongxinle.dao;

/**
 *
 *
 * @author lpy
 * @date 02-12 21:10
 */

import com.nongxinle.entity.GbDistributerPayListEntity;

import java.util.List;
import java.util.Map;


public interface GbDistributerPayListDao extends BaseDao<GbDistributerPayListEntity> {

    List<GbDistributerPayListEntity> queryPayListListByParams(Map<String, Object> map);

    int queryDisPayListCount(Map<String, Object> mapCount);

    int queryDisRecordSecondsTotal(Map<String, Object> map);
}

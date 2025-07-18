package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 08-19 12:35
 */

import com.nongxinle.entity.SysCityMarketEntity;

import java.util.List;
import java.util.Map;


public interface SysCityMarketDao extends BaseDao<SysCityMarketEntity> {

    List<SysCityMarketEntity> queryMarketByParams(Map<String, Object> map);

    List<SysCityMarketEntity> queryMarketNxDisByParams(Map<String, Object> map);
}

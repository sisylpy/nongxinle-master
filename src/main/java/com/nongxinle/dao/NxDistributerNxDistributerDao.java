package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 02-19 20:22
 */

import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDistributerNxDistributerEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface NxDistributerNxDistributerDao extends BaseDao<NxDistributerNxDistributerEntity> {

    List<NxDistributerEntity> queryOfferNxDisByParams(Map<String, Object> map3);

    /**
     * 根据两个配送商 id 查询协作关系（id 顺序不限）
     */
    NxDistributerNxDistributerEntity queryByPartnerIds(@Param("disId1") Integer disId1, @Param("disId2") Integer disId2);
}

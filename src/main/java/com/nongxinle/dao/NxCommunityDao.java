package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 2020-03-04 17:57:31
 */

import com.nongxinle.entity.NxCommunityEntity;
import com.nongxinle.entity.NxECommerceCommunityEntity;
import com.nongxinle.entity.NxECommerceEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface NxCommunityDao extends BaseDao<NxCommunityEntity> {

    NxCommunityEntity saveOne(NxCommunityEntity nxCommunity);

    NxECommerceEntity queryCommunityByECommerceId(Integer id);

    List<NxCommunityEntity> queryCommunityListByUserPoint(@Param("nxCuaLat") String nxCuaLat, @Param("nxCuaLng") String nxCuaLng, @Param("commerceId") Integer commerceId);
}

package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-07 09:33
 */

import com.nongxinle.entity.NxCommunityDeskEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityDeskDao extends BaseDao<NxCommunityDeskEntity> {

    List<NxCommunityDeskEntity> queryComDeskByParams(Map<String, Object> map);

    NxCommunityDeskEntity queryDeskWithOrders(Map<String, Object> map);

    NxCommunityDeskEntity queryDeskByCurrentOrderId(Integer currentOrderId);

    void bindCurrentOrder(@org.apache.ibatis.annotations.Param("deskId") Integer deskId,
                          @org.apache.ibatis.annotations.Param("orderId") Integer orderId);

    void releaseCurrentOrder(@org.apache.ibatis.annotations.Param("deskId") Integer deskId);
}

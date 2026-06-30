package com.nongxinle.dao;

import com.nongxinle.community.yunguotuan.entity.YgtMemberJoinSourceEntity;
import org.apache.ibatis.annotations.Param;

public interface YgtMemberJoinSourceDao extends BaseDao<YgtMemberJoinSourceEntity> {
    YgtMemberJoinSourceEntity queryByCustomerAndShareCode(@Param("customerUserId") Integer customerUserId,
                                                          @Param("shareCode") String shareCode);
}

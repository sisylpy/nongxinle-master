package com.nongxinle.dao;

import com.nongxinle.community.yunguotuan.entity.YgtMemberShareInviteEntity;
import org.apache.ibatis.annotations.Param;

public interface YgtMemberShareInviteDao extends BaseDao<YgtMemberShareInviteEntity> {
    YgtMemberShareInviteEntity queryByShareCode(@Param("shareCode") String shareCode);
}

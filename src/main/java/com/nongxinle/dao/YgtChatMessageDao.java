package com.nongxinle.dao;

import com.nongxinle.community.yunguotuan.entity.YgtChatMessageEntity;
import org.apache.ibatis.annotations.Param;

public interface YgtChatMessageDao extends BaseDao<YgtChatMessageEntity> {
    YgtChatMessageEntity queryByMsgId(@Param("corpId") String corpId, @Param("msgId") String msgId);

    YgtChatMessageEntity queryBySeq(@Param("corpId") String corpId, @Param("seq") Long seq);

    int updateParseStatus(YgtChatMessageEntity entity);
}

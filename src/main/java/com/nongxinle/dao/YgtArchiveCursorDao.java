package com.nongxinle.dao;

import com.nongxinle.community.yunguotuan.entity.YgtArchiveCursorEntity;
import org.apache.ibatis.annotations.Param;

public interface YgtArchiveCursorDao extends BaseDao<YgtArchiveCursorEntity> {
    YgtArchiveCursorEntity queryByCorpAndChatId(@Param("corpId") String corpId, @Param("chatId") String chatId);
}

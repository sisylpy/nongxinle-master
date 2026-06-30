package com.nongxinle.dao;

import com.nongxinle.community.yunguotuan.entity.YgtWecomGroupEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface YgtWecomGroupDao extends BaseDao<YgtWecomGroupEntity> {
    YgtWecomGroupEntity queryByCorpAndChatId(@Param("corpId") String corpId, @Param("chatId") String chatId);

    List<YgtWecomGroupEntity> queryEnabledGroups(Map<String, Object> map);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}

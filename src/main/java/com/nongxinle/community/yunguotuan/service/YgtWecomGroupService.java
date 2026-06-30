package com.nongxinle.community.yunguotuan.service;

import com.nongxinle.community.yunguotuan.entity.YgtWecomGroupEntity;

import java.util.List;
import java.util.Map;

public interface YgtWecomGroupService {
    Map<String, Object> syncGroups(Map<String, Object> params);

    List<YgtWecomGroupEntity> queryGroups(Map<String, Object> params);

    int enableGroup(Long id);

    int disableGroup(Long id);

    List<YgtWecomGroupEntity> queryEnabledGroups(String corpId);

    Map<String, Object> getGroupMembers(Long wecomGroupId);

    Map<String, Object> resolveGroupMemberIdentity(String corpId, String chatId, String wxCode,
                                                   String groupEnterEncrypted, String groupEnterIv);
}

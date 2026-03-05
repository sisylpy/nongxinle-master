package com.nongxinle.service;

import com.nongxinle.entity.NxDistributerInviteEntity;

import java.util.List;
import java.util.Map;

/**
 * 配送商邀请注册 Service
 *
 * @author lpy
 */
public interface NxDistributerInviteService {

    NxDistributerInviteEntity queryObject(Integer id);

    NxDistributerInviteEntity queryByInviteCode(String inviteCode);

    List<NxDistributerInviteEntity> queryList(Map<String, Object> map);

    int queryTotal(Map<String, Object> map);

    int save(NxDistributerInviteEntity entity);

    int update(NxDistributerInviteEntity entity);

    int delete(Integer id);

    /**
     * 生成唯一邀请码
     */
    String generateInviteCode();

    /**
     * 创建邀请（A 邀请 B）
     */
    NxDistributerInviteEntity createInvite(Integer inviterDisId, Integer inviteType, String inviteePhone);


}

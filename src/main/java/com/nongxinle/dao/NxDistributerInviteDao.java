package com.nongxinle.dao;

import com.nongxinle.entity.NxDistributerInviteEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 配送商邀请注册 Dao
 *
 * @author lpy
 */
public interface NxDistributerInviteDao {

    NxDistributerInviteEntity queryObject(Integer id);

    NxDistributerInviteEntity queryByInviteCode(String inviteCode);

    List<NxDistributerInviteEntity> queryList(Map<String, Object> map);

    int queryTotal(Map<String, Object> map);

    int save(NxDistributerInviteEntity entity);

    int update(NxDistributerInviteEntity entity);

    int delete(Integer id);
}

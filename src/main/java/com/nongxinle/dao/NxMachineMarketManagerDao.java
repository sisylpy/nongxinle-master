package com.nongxinle.dao;

import com.nongxinle.entity.NxMachineMarketManagerEntity;

import java.util.Map;

/**
 * 市场管理员Dao
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface NxMachineMarketManagerDao extends BaseDao<NxMachineMarketManagerEntity> {

    /**
     * 根据微信OpenID查询管理员
     * @param openid 微信OpenID
     * @return 管理员实体
     */
    NxMachineMarketManagerEntity queryByOpenid(String openid);

    /**
     * 根据手机号查询管理员
     * @param phone 手机号
     * @return 管理员实体
     */
    NxMachineMarketManagerEntity queryByPhone(String phone);

    /**
     * 根据市场ID查询管理员列表
     * @param map 包含marketId的参数map
     * @return 管理员列表
     */
    java.util.List<NxMachineMarketManagerEntity> queryByMarketId(Map<String, Object> map);

    /**
     * 更新最后登录时间
     * @param managerId 管理员ID
     */
    void updateLastLoginTime(Integer managerId);
}


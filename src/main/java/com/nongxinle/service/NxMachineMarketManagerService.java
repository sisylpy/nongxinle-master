package com.nongxinle.service;

import com.nongxinle.entity.NxMachineMarketManagerEntity;

import java.util.List;
import java.util.Map;

/**
 * 市场管理员Service
 *
 * @author lpy
 * @date 2025-10-14
 */
public interface NxMachineMarketManagerService {

    NxMachineMarketManagerEntity queryObject(Integer nxMmId);

    List<NxMachineMarketManagerEntity> queryList(Map<String, Object> map);

    int queryTotal(Map<String, Object> map);

    void save(NxMachineMarketManagerEntity nxMarketManager);

    void update(NxMachineMarketManagerEntity nxMarketManager);

    void delete(Integer nxMmId);

    void deleteBatch(Integer[] nxMmIds);

    /**
     * 根据微信OpenID查询管理员
     */
    NxMachineMarketManagerEntity queryByOpenid(String openid);

    /**
     * 根据手机号查询管理员
     * @param phone 手机号
     * @return 管理员实体
     */
    NxMachineMarketManagerEntity queryByPhone(String phone);

    /**
     * 打印软件登录（通过手机号）
     * @param phone 手机号
     * @return 登录结果（包含管理员信息和市场信息）
     */
    Map<String, Object> printSoftwareLogin(String phone);

    /**
     * 微信授权登录
     * @param code 微信授权码
     * @return 管理员实体（如果不存在则自动创建）
     */
    NxMachineMarketManagerEntity wxLogin(String code);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Integer managerId);

    /**
     * 根据市场ID查询管理员列表
     */
    List<NxMachineMarketManagerEntity> queryByMarketId(Integer marketId);
}


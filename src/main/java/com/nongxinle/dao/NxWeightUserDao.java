package com.nongxinle.dao;

/**
 * 称重员工Dao接口
 * @author lpy
 * @date 2025-01-XX
 */

import com.nongxinle.entity.NxWeightUserEntity;

import java.util.List;
import java.util.Map;

public interface NxWeightUserDao extends BaseDao<NxWeightUserEntity> {

    /**
     * 根据手机号查询称重员工
     * @param loginPhone 登录手机号
     * @return 称重员工实体
     */
    NxWeightUserEntity queryUserByLoginPhone(String loginPhone);

    /**
     * 根据微信openid查询称重员工
     * @param openId 微信openid
     * @return 称重员工实体
     */
    NxWeightUserEntity queryUserByOpenId(String openId);

    /**
     * 根据配送商ID查询称重员工列表
     * @param distributerId 配送商ID
     * @return 称重员工列表
     */
    List<NxWeightUserEntity> queryUsersByDistributerId(Integer distributerId);

    /**
     * 根据供货商ID查询称重员工列表
     * @param supplierId 供货商ID
     * @return 称重员工列表
     */
    List<NxWeightUserEntity> queryUsersBySupplierId(Integer supplierId);

    /**
     * 根据条件查询称重员工列表
     * @param map 查询条件
     * @return 称重员工列表
     */
    List<NxWeightUserEntity> queryUsersByParams(Map<String, Object> map);

    /**
     * 根据手机号和验证码查询称重员工（用于登录验证）
     * @param map 包含loginPhone和loginCode
     * @return 称重员工实体
     */
    NxWeightUserEntity queryUserByPhoneAndCode(Map<String, Object> map);
}


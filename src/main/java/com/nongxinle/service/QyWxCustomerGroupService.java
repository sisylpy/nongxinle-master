package com.nongxinle.service;

import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;

import java.util.List;
import java.util.Map;

/**
 * 企业微信客户群管理服务
 * 
 * @author lpy
 * @date 2024-01-01
 */
public interface QyWxCustomerGroupService {
    
    /**
     * 获取客户群列表
     * @param corpId 企业ID
     * @param statusFilter 群状态过滤 0-正常 1-跟进人离职 2-离职继承中 3-离职继承完成
     * @param offset 分页偏移量
     * @param limit 每页数量，最大1000
     * @return 客户群列表
     */
    List<Map<String, Object>> getCustomerGroupList(String corpId, Integer statusFilter, Integer offset, Integer limit);
    
    /**
     * 获取客户群详情
     * @param corpId 企业ID
     * @param chatId 客户群ID
     * @return 客户群详情
     */
    Map<String, Object> getCustomerGroupDetail(String corpId, String chatId);
    
    /**
     * 保存要监控的客户群
     * @param corpId 企业ID
     * @param chatId 客户群ID
     * @param chatName 客户群名称
     * @return 是否成功
     */
    boolean saveMonitoredGroup(String corpId, String chatId, String chatName);
    
    /**
     * 获取已监控的客户群列表
     * @param corpId 企业ID
     * @return 已监控的客户群列表
     */
    List<Map<String, Object>> getMonitoredGroups(String corpId);
    
    /**
     * 取消监控客户群
     * @param corpId 企业ID
     * @param chatId 客户群ID
     * @return 是否成功
     */
    boolean removeMonitoredGroup(String corpId, String chatId);
    
    /**
     * 获取access_token
     * @param corpId 企业ID
     * @return access_token
     */
    String getAccessToken(String corpId);
}


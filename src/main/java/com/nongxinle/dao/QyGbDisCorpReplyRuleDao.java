package com.nongxinle.dao;

import com.nongxinle.entity.QyGbDisCorpReplyRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 企业微信消息回复规则表
 * 
 * @author lpy
 * @date 2024-01-01
 */
@Mapper
public interface QyGbDisCorpReplyRuleDao extends BaseDao<QyGbDisCorpReplyRuleEntity> {
	
	/**
	 * 根据企业ID查询启用的回复规则（按优先级排序）
	 * @param corpId 企业微信企业ID
	 * @return 回复规则列表
	 */
	List<QyGbDisCorpReplyRuleEntity> queryEnabledRulesByCorpId(@Param("corpId") String corpId);
	
	/**
	 * 根据企业ID查询所有规则
	 * @param corpId 企业微信企业ID
	 * @return 回复规则列表
	 */
	List<QyGbDisCorpReplyRuleEntity> queryRulesByCorpId(@Param("corpId") String corpId);
}

package com.nongxinle.dao;

/**
 * 系统 Prompt Dao
 * 
 * @author lpy
 * @date 2025-01-XX
 */

import com.nongxinle.entity.NxPromptEntity;

import java.util.List;
import java.util.Map;


public interface NxPromptDao extends BaseDao<NxPromptEntity> {
	
	/**
	 * 根据 prompt_key 查询
	 */
	NxPromptEntity queryByKey(String promptKey);
	
	/**
	 * 根据 category 查询列表
	 */
	List<NxPromptEntity> queryListByCategory(String category);
	
	/**
	 * 根据 api_path 查询
	 */
	NxPromptEntity queryByApiPath(String apiPath);
	
	/**
	 * 根据 prompt_key 查询所有记录（不限制 status，用于查找历史记录）
	 */
	List<NxPromptEntity> queryListByKey(String promptKey);
	
	/**
	 * 将相同 prompt_key 的所有启用状态（status=1）的记录改为禁用（status=0）
	 */
	void disableByKey(String promptKey);
	
	/**
	 * 根据 prompt_key 删除所有记录（用于处理唯一约束冲突）
	 */
	void deleteByKey(String promptKey);
}


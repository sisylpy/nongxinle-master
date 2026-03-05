package com.nongxinle.service;

/**
 * 系统 Prompt Service
 * 
 * @author lpy
 * @date 2025-01-XX
 */

import com.nongxinle.entity.NxPromptEntity;

import java.util.List;
import java.util.Map;

public interface NxPromptService {
	
	NxPromptEntity queryObject(Integer nxPromptId);
	
	List<NxPromptEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxPromptEntity nxPrompt);
	
	void update(NxPromptEntity nxPrompt);
	
	void delete(Integer nxPromptId);
	
	void deleteBatch(Integer[] nxPromptIds);
	
	/**
	 * 根据 prompt_key 查询（只返回启用的）
	 */
	NxPromptEntity queryByKey(String promptKey);
	
	/**
	 * 根据 category 查询列表（只返回启用的）
	 */
	List<NxPromptEntity> queryListByCategory(String category);
	
	/**
	 * 根据 api_path 查询（只返回启用的）
	 */
	NxPromptEntity queryByApiPath(String apiPath);
	
	/**
	 * 获取系统 prompt 内容（如果不存在或未启用，返回 null）
	 */
	String getPromptContentByKey(String promptKey);
	
	/**
	 * 添加 prompt，如果存在相同 key 的启用记录，则将其设为禁用（作为历史记录）
	 * 新添加的记录状态设为启用（status=1）
	 * 使用事务保证原子性
	 * 
	 * @param nxPrompt 要添加的 prompt 实体
	 */
	void saveWithKeySwitch(NxPromptEntity nxPrompt);
	
	/**
	 * 设置 prompt 的状态（0=禁用，1=启用）
	 * 如果设置为启用（status=1），会将相同 key 的其他启用记录设为禁用，保证每个 key 只有一个启用记录
	 * 使用事务保证原子性
	 * 
	 * @param promptId prompt ID
	 * @param status 状态值（0 或 1）
	 */
	void updateStatus(Integer promptId, Integer status);
}


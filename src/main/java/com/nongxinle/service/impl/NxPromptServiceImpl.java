package com.nongxinle.service.impl;

import com.nongxinle.dao.NxPromptDao;
import com.nongxinle.entity.NxPromptEntity;
import com.nongxinle.service.NxPromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 系统 Prompt Service 实现
 * 
 * @author lpy
 * @date 2025-01-XX
 */
@Service("nxPromptService")
public class NxPromptServiceImpl implements NxPromptService {
	
	@Autowired
	private NxPromptDao nxPromptDao;
	
	@Override
	public NxPromptEntity queryObject(Integer nxPromptId) {
		return nxPromptDao.queryObject(nxPromptId);
	}
	
	@Override
	public List<NxPromptEntity> queryList(Map<String, Object> map) {
		return nxPromptDao.queryList(map);
	}
	
	@Override
	public int queryTotal(Map<String, Object> map) {
		return nxPromptDao.queryTotal(map);
	}
	
	@Override
	public void save(NxPromptEntity nxPrompt) {
		// 清洗 prompt 内容
		if (nxPrompt.getNxPromptContent() != null) {
			nxPrompt.setNxPromptContent(cleanPromptContent(nxPrompt.getNxPromptContent()));
		}
		
		// 设置默认值
		if (nxPrompt.getNxPromptStatus() == null) {
			nxPrompt.setNxPromptStatus(1); // 默认启用
		}
		if (nxPrompt.getNxPromptVersion() == null) {
			nxPrompt.setNxPromptVersion(1); // 默认版本1
		}
		if (nxPrompt.getNxPromptCreatedTime() == null) {
			nxPrompt.setNxPromptCreatedTime(new java.sql.Timestamp(System.currentTimeMillis()));
		}
		if (nxPrompt.getNxPromptLastUpdated() == null) {
			nxPrompt.setNxPromptLastUpdated(new java.sql.Timestamp(System.currentTimeMillis()));
		}
		nxPromptDao.save(nxPrompt);
	}
	
	@Override
	public void update(NxPromptEntity nxPrompt) {
		// 清洗 prompt 内容
		if (nxPrompt.getNxPromptContent() != null) {
			nxPrompt.setNxPromptContent(cleanPromptContent(nxPrompt.getNxPromptContent()));
		}
		
		// 更新时自动更新最后修改时间
		nxPrompt.setNxPromptLastUpdated(new java.sql.Timestamp(System.currentTimeMillis()));
		nxPromptDao.update(nxPrompt);
	}
	
	@Override
	public void delete(Integer nxPromptId) {
		nxPromptDao.delete(nxPromptId);
	}
	
	@Override
	public void deleteBatch(Integer[] nxPromptIds) {
		nxPromptDao.deleteBatch(nxPromptIds);
	}
	
	@Override
	public NxPromptEntity queryByKey(String promptKey) {
		return nxPromptDao.queryByKey(promptKey);
	}
	
	@Override
	public List<NxPromptEntity> queryListByCategory(String category) {
		return nxPromptDao.queryListByCategory(category);
	}
	
	@Override
	public NxPromptEntity queryByApiPath(String apiPath) {
		return nxPromptDao.queryByApiPath(apiPath);
	}
	
	@Override
	public String getPromptContentByKey(String promptKey) {
		NxPromptEntity prompt = queryByKey(promptKey);
		if (prompt != null && prompt.getNxPromptContent() != null) {
			return prompt.getNxPromptContent();
		}
		return null;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveWithKeySwitch(NxPromptEntity nxPrompt) {
		// 参数校验
		if (nxPrompt == null || nxPrompt.getNxPromptKey() == null || nxPrompt.getNxPromptKey().trim().isEmpty()) {
			throw new IllegalArgumentException("promptKey 不能为空");
		}
		
		String promptKey = nxPrompt.getNxPromptKey().trim();
		
		// 1. 查找是否存在相同 key 的记录
		List<NxPromptEntity> existingPrompts = nxPromptDao.queryListByKey(promptKey);
		
		// 2. 如果数据库有唯一约束（uk_prompt_key），需要先删除所有相同 key 的记录
		// 因为唯一约束不允许同一 key 有多条记录，所以无法保留历史记录
		// 注意：如果移除了唯一约束，可以改为只禁用旧记录，保留历史记录
		if (!existingPrompts.isEmpty()) {
			// 保存旧记录的版本号信息（用于计算新版本号）
			int maxVersion = 0;
			for (NxPromptEntity existing : existingPrompts) {
				if (existing.getNxPromptVersion() != null && existing.getNxPromptVersion() > maxVersion) {
					maxVersion = existing.getNxPromptVersion();
				}
			}
			
			// 删除所有相同 key 的记录（因为唯一约束不允许重复）
			nxPromptDao.deleteByKey(promptKey);
			
			// 设置新记录的版本号
			if (nxPrompt.getNxPromptVersion() == null) {
				nxPrompt.setNxPromptVersion(maxVersion + 1);
			}
		} else {
			// 如果没有旧记录，设置默认版本号
			if (nxPrompt.getNxPromptVersion() == null) {
				nxPrompt.setNxPromptVersion(1);
			}
		}
		
		// 3. 清洗 prompt 内容
		if (nxPrompt.getNxPromptContent() != null) {
			nxPrompt.setNxPromptContent(cleanPromptContent(nxPrompt.getNxPromptContent()));
		}
		
		// 4. 设置新记录的默认值
		nxPrompt.setNxPromptStatus(1); // 新记录设为启用
		if (nxPrompt.getNxPromptCreatedTime() == null) {
			nxPrompt.setNxPromptCreatedTime(new java.sql.Timestamp(System.currentTimeMillis()));
		}
		if (nxPrompt.getNxPromptLastUpdated() == null) {
			nxPrompt.setNxPromptLastUpdated(new java.sql.Timestamp(System.currentTimeMillis()));
		}
		
		// 5. 保存新记录
		nxPromptDao.save(nxPrompt);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateStatus(Integer promptId, Integer status) {
		// 参数校验
		if (promptId == null) {
			throw new IllegalArgumentException("promptId 不能为空");
		}
		if (status == null || (status != 0 && status != 1)) {
			throw new IllegalArgumentException("status 必须为 0 或 1");
		}
		
		// 1. 查询要更新的 prompt
		NxPromptEntity prompt = nxPromptDao.queryObject(promptId);
		if (prompt == null) {
			throw new IllegalArgumentException("未找到对应的 prompt，promptId: " + promptId);
		}
		
		// 2. 如果设置为启用（status=1），需要将相同 key 的其他启用记录设为禁用
		if (status == 1 && prompt.getNxPromptKey() != null) {
			// 先禁用相同 key 的所有启用记录
			nxPromptDao.disableByKey(prompt.getNxPromptKey());
		}
		
		// 3. 更新当前记录的 status
		prompt.setNxPromptStatus(status);
		prompt.setNxPromptLastUpdated(new java.sql.Timestamp(System.currentTimeMillis()));
		nxPromptDao.update(prompt);
	}
	
	/**
	 * 清洗 prompt 内容，使其更容易读和修改
	 * 
	 * 清洗规则：
	 * 1. 统一换行符（\r\n, \r 统一为 \n）
	 * 2. 去除行首行尾空白字符
	 * 3. 去除多余的空行（连续多个空行只保留一个）
	 * 4. 去除文档首尾的空白行
	 * 5. 保留必要的格式（分隔线、缩进等）
	 * 
	 * @param content 原始 prompt 内容
	 * @return 清洗后的 prompt 内容
	 */
	private String cleanPromptContent(String content) {
		if (content == null || content.trim().isEmpty()) {
			return content;
		}
		
		// 1. 统一换行符：\r\n 和 \r 统一为 \n
		content = content.replace("\r\n", "\n").replace("\r", "\n");
		
		// 2. 按行分割
		String[] lines = content.split("\n");
		StringBuilder cleaned = new StringBuilder();
		boolean lastLineWasEmpty = false;
		boolean hasContent = false;
		
		for (String line : lines) {
			// 3. 去除行首行尾空白字符
			String trimmedLine = line.trim();
			
			// 4. 处理空行：连续多个空行只保留一个
			if (trimmedLine.isEmpty()) {
				if (!lastLineWasEmpty && hasContent) {
					// 只在已有内容后添加一个空行
					cleaned.append("\n");
					lastLineWasEmpty = true;
				}
				// 跳过其他空行
				continue;
			}
			
			// 5. 保留非空行（保留原始缩进，但去除首尾空白）
			// 注意：这里保留行内的空白（用于缩进），只去除首尾空白
			if (hasContent || !cleaned.toString().isEmpty()) {
				cleaned.append("\n");
			}
			cleaned.append(trimmedLine);
			lastLineWasEmpty = false;
			hasContent = true;
		}
		
		// 6. 去除文档首尾的空白行
		String result = cleaned.toString();
		result = result.replaceAll("^\\n+", "");  // 去除开头的换行符
		result = result.replaceAll("\\n+$", "");  // 去除结尾的换行符
		
		return result;
	}
}


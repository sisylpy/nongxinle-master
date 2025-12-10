package com.nongxinle.service;

import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import com.nongxinle.entity.QyWxMessage;

import java.util.Date;

/**
 * 企业微信会话存档服务
 * 
 * @author lpy
 * @date 2024-01-01
 */
public interface QyGbDisCorpMsgAuditService {
	
	/**
	 * 根据企业ID查询配置
	 * @param corpId 企业微信企业ID
	 * @return 会话存档配置
	 */
	QyGbDisCorpMsgAuditEntity queryByCorpId(String corpId);
	
	/**
	 * 更新访问令牌
	 * @param corpId 企业ID
	 * @param accessToken 访问令牌
	 * @param expireTime 过期时间
	 * @return 更新行数
	 */
	int updateAccessToken(String corpId, String accessToken, Date expireTime);
	
	/**
	 * 保存配置
	 * @param config 会话存档配置
	 */
	void save(QyGbDisCorpMsgAuditEntity config);
	
	/**
	 * 更新配置
	 * @param config 会话存档配置
	 */
	void update(QyGbDisCorpMsgAuditEntity config);
	
	/**
	 * 获取会话内容
	 * @param corpId 企业ID
	 * @param msgId 消息ID
	 * @return 会话消息
	 */
	QyWxMessage getChatData(String corpId, String msgId);
	
	/**
	 * 解密消息内容
	 * @param encrypted 加密的消息内容
	 * @param corpId 企业ID
	 * @return 解密后的消息内容
	 */
	String decryptMessage(String encrypted, String corpId);
}

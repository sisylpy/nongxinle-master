package com.nongxinle.dao;

import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 企业微信会话存档配置表
 * 
 * @author lpy
 * @date 2024-01-01
 */
@Mapper
public interface QyGbDisCorpMsgAuditDao extends BaseDao<QyGbDisCorpMsgAuditEntity> {
	
	/**
	 * 根据企业ID查询配置
	 * @param corpId 企业微信企业ID
	 * @return 会话存档配置
	 */
	QyGbDisCorpMsgAuditEntity queryByCorpId(@Param("corpId") String corpId);
	
	/**
	 * 更新访问令牌
	 * @param corpId 企业ID
	 * @param accessToken 访问令牌
	 * @param expireTime 过期时间
	 * @return 更新行数
	 */
	int updateAccessToken(@Param("corpId") String corpId, 
						  @Param("accessToken") String accessToken, 
						  @Param("expireTime") java.util.Date expireTime);
}

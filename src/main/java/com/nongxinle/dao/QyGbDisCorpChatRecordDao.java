package com.nongxinle.dao;

import com.nongxinle.entity.QyGbDisCorpChatRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 企业微信聊天记录表
 * 
 * @author lpy
 * @date 2024-01-01
 */
@Mapper
public interface QyGbDisCorpChatRecordDao extends BaseDao<QyGbDisCorpChatRecordEntity> {
	
	/**
	 * 根据消息ID查询记录
	 * @param msgId 消息ID
	 * @return 聊天记录
	 */
	QyGbDisCorpChatRecordEntity queryByMsgId(@Param("msgId") String msgId);
	
	/**
	 * 根据群ID查询最近的聊天记录
	 * @param corpId 企业ID
	 * @param roomId 群ID
	 * @param limit 限制数量
	 * @return 聊天记录列表
	 */
	List<QyGbDisCorpChatRecordEntity> queryRecentRecordsByRoomId(@Param("corpId") String corpId, 
																  @Param("roomId") String roomId, 
																  @Param("limit") int limit);
	
	/**
	 * 更新回复状态
	 * @param msgId 消息ID
	 * @param replyContent 回复内容
	 * @return 更新行数
	 */
	int updateReplyStatus(@Param("msgId") String msgId, @Param("replyContent") String replyContent);
}

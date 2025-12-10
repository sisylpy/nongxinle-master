package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.service.QyWxMessageSendService;
import com.nongxinle.utils.QyWxTokenUtil;
import com.nongxinle.utils.WeChatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信消息发送服务实现
 * 
 * @author lpy
 * @date 2024-01-01
 */
@Service("qyWxMessageSendService")
public class QyWxMessageSendServiceImpl implements QyWxMessageSendService {
	
	@Autowired
	private QyWxTokenUtil qyWxTokenUtil;

	@Override
	public boolean sendTextToGroup(String corpId, String roomId, String content) {
		try {
			// 获取access_token
			String accessToken = qyWxTokenUtil.getAccessToken(corpId);
			if (accessToken == null) {
				System.err.println("获取access_token失败");
				return false;
			}

			// 构建消息体
			Map<String, Object> message = new HashMap<>();
			message.put("chatid", roomId);
			message.put("msgtype", "text");
			
			Map<String, Object> text = new HashMap<>();
			text.put("content", content);
			message.put("text", text);

			// 调用企业微信API发送消息
			String url = "https://qyapi.weixin.qq.com/cgi-bin/appchat/send?access_token=" + accessToken;
			String response = WeChatUtil.httpRequest(url, "POST", JSONObject.toJSONString(message));
			
			System.out.println("发送文本消息响应: " + response);
			
			JSONObject jsonObject = JSONObject.parseObject(response);
			if (jsonObject.getInteger("errcode") == 0) {
				System.out.println("文本消息发送成功");
				return true;
			} else {
				String errorMsg = "发送文本消息失败: " + jsonObject.getString("errmsg");
				System.err.println(errorMsg);
				return false;
			}
		} catch (Exception e) {
			System.err.println("发送文本消息异常: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean sendImageToGroup(String corpId, String roomId, String mediaId) {
		try {
			// 获取access_token
			String accessToken = qyWxTokenUtil.getAccessToken(corpId);
			if (accessToken == null) {
				System.err.println("获取access_token失败");
				return false;
			}

			// 构建消息体
			Map<String, Object> message = new HashMap<>();
			message.put("chatid", roomId);
			message.put("msgtype", "image");
			
			Map<String, Object> image = new HashMap<>();
			image.put("media_id", mediaId);
			message.put("image", image);

			// 调用企业微信API发送消息
			String url = "https://qyapi.weixin.qq.com/cgi-bin/appchat/send?access_token=" + accessToken;
			String response = WeChatUtil.httpRequest(url, "POST", JSONObject.toJSONString(message));
			
			System.out.println("发送图片消息响应: " + response);
			
			JSONObject jsonObject = JSONObject.parseObject(response);
			if (jsonObject.getInteger("errcode") == 0) {
				System.out.println("图片消息发送成功");
				return true;
			} else {
				String errorMsg = "发送图片消息失败: " + jsonObject.getString("errmsg");
				System.err.println(errorMsg);
				return false;
			}
		} catch (Exception e) {
			System.err.println("发送图片消息异常: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean sendFileToGroup(String corpId, String roomId, String mediaId) {
		try {
			// 获取access_token
			String accessToken = qyWxTokenUtil.getAccessToken(corpId);
			if (accessToken == null) {
				System.err.println("获取access_token失败");
				return false;
			}

			// 构建消息体
			Map<String, Object> message = new HashMap<>();
			message.put("chatid", roomId);
			message.put("msgtype", "file");
			
			Map<String, Object> file = new HashMap<>();
			file.put("media_id", mediaId);
			message.put("file", file);

			// 调用企业微信API发送消息
			String url = "https://qyapi.weixin.qq.com/cgi-bin/appchat/send?access_token=" + accessToken;
			String response = WeChatUtil.httpRequest(url, "POST", JSONObject.toJSONString(message));
			
			System.out.println("发送文件消息响应: " + response);
			
			JSONObject jsonObject = JSONObject.parseObject(response);
			if (jsonObject.getInteger("errcode") == 0) {
				System.out.println("文件消息发送成功");
				return true;
			} else {
				String errorMsg = "发送文件消息失败: " + jsonObject.getString("errmsg");
				System.err.println(errorMsg);
				return false;
			}
		} catch (Exception e) {
			System.err.println("发送文件消息异常: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
}



package com.nongxinle.utils;

import com.alibaba.fastjson.JSON;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 异常处理器
 * 
 */
@Component
public class RRExceptionHandler implements HandlerExceptionResolver {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 检查是否是客户端断开连接的异常
	 * 不直接依赖 Tomcat 的 ClientAbortException，而是通过异常类型和消息判断
	 */
	private boolean isClientAbortException(Exception ex) {
		if (ex == null) {
			return false;
		}
		
		// 检查异常类名（避免直接依赖 Tomcat 类）
		String className = ex.getClass().getName();
		if (className.contains("ClientAbortException")) {
			return true;
		}
		
		// 检查异常消息
		String message = ex.getMessage();
		if (message != null && (message.contains("Broken pipe") || message.contains("Connection reset"))) {
			return true;
		}
		
		// 检查 IOException 且消息包含 Broken pipe
		if (ex instanceof IOException && message != null && message.contains("Broken pipe")) {
			return true;
		}
		
		// 检查 cause
		Throwable cause = ex.getCause();
		if (cause != null) {
			String causeClassName = cause.getClass().getName();
			if (causeClassName.contains("ClientAbortException")) {
				return true;
			}
			String causeMessage = cause.getMessage();
			if (causeMessage != null && (causeMessage.contains("Broken pipe") || causeMessage.contains("Connection reset"))) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {
		R r = new R();
		try {
			// 检查是否是客户端断开连接的异常
			if (isClientAbortException(ex)) {
				// 客户端已断开连接，只记录日志，不尝试写入响应
				logger.warn("客户端已断开连接，跳过响应写入: {}", ex.getMessage());
				return new ModelAndView();
			}
			
			// 检查响应是否已经提交（流已被使用）
			if (response.isCommitted()) {
				logger.warn("响应已提交，无法写入异常响应: {}", ex.getMessage());
				// 只记录异常日志
				logger.error(ex.getMessage(), ex);
				return new ModelAndView();
			}
			
			response.setContentType("application/json;charset=utf-8");
			response.setCharacterEncoding("utf-8");
			
			if (ex instanceof RRException) {
				r.put("code", ((RRException) ex).getCode());
				r.put("msg", ((RRException) ex).getMessage());
			}else if(ex instanceof DuplicateKeyException){
				r = R.error("数据库中已存在该记录");
			}else if(ex instanceof AuthorizationException){
				r = R.error("没有权限，请联系管理员授权");
			}else{
				r = R.error();
			}
			
			//记录异常日志
			logger.error(ex.getMessage(), ex);
			
			String json = JSON.toJSONString(r);
			response.getWriter().print(json);
		} catch (IllegalStateException e) {
			// 响应流已被使用（getWriter() 或 getOutputStream() 已被调用）
			logger.warn("响应流已被使用，无法写入异常响应: {}", e.getMessage());
			logger.error("原始异常: {}", ex.getMessage(), ex);
		} catch (IOException e) {
			// IO 异常（可能包括 Broken pipe）
			if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
				logger.warn("客户端连接已断开: {}", e.getMessage());
			} else {
				logger.error("RRExceptionHandler 写入响应失败", e);
			}
		} catch (Exception e) {
			logger.error("RRExceptionHandler 异常处理失败", e);
		}
		return new ModelAndView();
	}
}

package com.nongxinle.listener;

import com.nongxinle.controller.OcrController;
import com.nongxinle.utils.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 应用关闭监听器
 * 用于在应用停止时优雅关闭线程池和清理资源
 * 
 * @author lpy
 * @date 2026-01-28
 */
public class ApplicationShutdownListener implements ServletContextListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationShutdownListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("[ApplicationShutdownListener] 应用启动，监听器已注册");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("[ApplicationShutdownListener] 应用正在停止，开始清理资源...");
        
        try {
            // 关闭 OCR Controller 的线程池
            if (SpringContextUtils.applicationContext != null) {
                try {
                    OcrController ocrController = SpringContextUtils.applicationContext.getBean(OcrController.class);
                    if (ocrController != null) {
                        logger.info("[ApplicationShutdownListener] 关闭 OCR 线程池...");
                        ocrController.shutdownThreadPool();
                        logger.info("[ApplicationShutdownListener] OCR 线程池已关闭");
                    }
                } catch (Exception e) {
                    logger.error("[ApplicationShutdownListener] 关闭 OCR 线程池时出错", e);
                }
            }
            
            // 等待一小段时间，确保 MySQL JDBC 清理线程有时间完成
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            logger.info("[ApplicationShutdownListener] 资源清理完成");
        } catch (Exception e) {
            logger.error("[ApplicationShutdownListener] 清理资源时发生异常", e);
        }
    }
}

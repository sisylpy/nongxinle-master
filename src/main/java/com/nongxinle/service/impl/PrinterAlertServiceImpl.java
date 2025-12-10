package com.nongxinle.service.impl;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印机提醒核心业务Service实现类
 *
 * @author lpy
 * @date 2025-10-14
 */
@Service("printerAlertService")
public class PrinterAlertServiceImpl implements PrinterAlertService {

    private static final Logger logger = LoggerFactory.getLogger(PrinterAlertServiceImpl.class);

    @Autowired
    private NxMachinePrinterDeviceService nxPrinterDeviceService;

    @Autowired
    private NxMachineAlertThresholdService nxAlertThresholdService;

    @Autowired
    private NxMachineDeviceManagerService nxDeviceManagerService;

    @Autowired
    private NxMachineAlertRecordService nxAlertRecordService;

    @Autowired
    private NxMachinePaperRefillRecordService nxPaperRefillRecordService;

    @Autowired
    private NxMachineMarketManagerService nxMarketManagerService;

    @Override
    @Transactional
    public Integer printAndCheckAlert(Integer deviceId) {
        return printAndCheckAlert(deviceId, 1);
    }

    @Override
    @Transactional
    public Integer printAndCheckAlert(Integer deviceId, Integer count) {
        logger.info("开始打印扣减，设备ID: {}, 扣减数量: {} 张", deviceId, count);
        
        // 1. 一次性扣减指定数量的纸张
        Integer currentPaperCount = nxPrinterDeviceService.reducePaperCount(deviceId, count);
        
        if (currentPaperCount == null) {
            logger.warn("打印扣减失败，纸张不足，设备ID: {}, 需要扣减: {} 张", deviceId, count);
            return null;
        }
        
        logger.info("打印扣减成功，扣减数量: {} 张，当前余量: {} 张", count, currentPaperCount);
        
        // 2. 检查并触发提醒
        checkAndTriggerAlert(deviceId, currentPaperCount);
        
        return currentPaperCount;
    }

    @Override
    @Transactional
    public void refillPaperAndClearAlert(Integer deviceId, Integer addCount, Integer operatorId, 
                                         String operatorName, Integer refillType, String remark) {
        logger.info("开始加纸操作，设备ID: {}, 增加数量: {} 张", deviceId, addCount);
        
        // 调用加纸服务（内部已包含清除提醒逻辑）
        nxPaperRefillRecordService.refillPaper(deviceId, addCount, operatorId, operatorName, refillType, remark);
        
        logger.info("加纸操作完成，已清除提醒记录");
    }

    @Override
    @Transactional
    public void calibratePaperCount(Integer deviceId, Integer paperCount, Integer operatorId, 
                                    String operatorName, String remark) {
        logger.info("开始校准纸张数量，设备ID: {}, 校准值: {} 张", deviceId, paperCount);
        
        // 1. 获取校准前的数量
        NxMachinePrinterDeviceEntity device = nxPrinterDeviceService.queryObject(deviceId);
        Integer beforeCount = device.getNxPdPaperCount();
        
        // 2. 设置新的纸张数量
        nxPrinterDeviceService.setPaperCount(deviceId, paperCount);
        
        // 3. 记录校准操作（作为加纸记录，类型为3-手动校准）
        Integer diff = paperCount - beforeCount;
        nxPaperRefillRecordService.refillPaper(deviceId, diff, operatorId, operatorName, 3, remark);
        
        logger.info("校准完成，从 {} 张 → {} 张", beforeCount, paperCount);
    }

    @Override
    public void checkAndTriggerAlert(Integer deviceId, Integer currentPaperCount) {
        logger.info("检查提醒阈值，设备ID: {}, 当前余量: {} 张", deviceId, currentPaperCount);
        
        // 1. 查询触发的阈值配置（按级别从高到低）
        List<NxMachineAlertThresholdEntity> triggeredThresholds = 
            nxAlertThresholdService.queryTriggeredThresholds(deviceId, currentPaperCount);
        
        if (triggeredThresholds == null || triggeredThresholds.isEmpty()) {
            logger.info("当前余量未触发任何阈值");
            return;
        }
        
        logger.info("触发了 {} 个阈值", triggeredThresholds.size());
        
        // 2. 遍历触发的阈值，检查是否需要发送提醒
        for (NxMachineAlertThresholdEntity threshold : triggeredThresholds) {
            Integer alertLevel = threshold.getNxAtLevel();
            
            // 3. 查询是否已有未清除的同级别提醒（防重复）
            NxMachineAlertRecordEntity unclearedRecord = 
                nxAlertRecordService.queryUnclearedRecord(deviceId, alertLevel);
            
            if (unclearedRecord != null) {
                logger.info("级别 {} 已有未清除提醒，跳过创建新记录（防重复）", alertLevel);
                
                // 检查是否需要重新发送微信消息
                // 条件：1) 之前发送失败 或 2) 测试模式（发送状态为2=待发送）
                if (unclearedRecord.getNxArSendStatus() == 0 || unclearedRecord.getNxArSendStatus() == 2) {
                    logger.info("级别 {} 需要发送微信消息，当前状态: {}", alertLevel, unclearedRecord.getNxArSendStatus());
                    try {
                        boolean sendSuccess = sendWxTemplateMessage(unclearedRecord.getNxArId());
                        if (sendSuccess) {
                            nxAlertRecordService.updateSendStatus(unclearedRecord.getNxArId(), 1);
                            logger.info("微信消息发送成功");
                        } else {
                            nxAlertRecordService.updateSendStatus(unclearedRecord.getNxArId(), 0);
                            logger.error("微信消息发送失败");
                        }
                    } catch (Exception e) {
                        logger.error("发送微信消息异常", e);
                        nxAlertRecordService.updateSendStatus(unclearedRecord.getNxArId(), 0);
                    }
                } else {
                    logger.info("级别 {} 微信消息已发送成功，跳过重复发送", alertLevel);
                }
                continue;
            }
            
            // 4. 查询该级别需要通知的责任人
            List<NxMachineDeviceManagerEntity> managers = 
                nxDeviceManagerService.queryNotifyManagers(deviceId, alertLevel);
            
            if (managers == null || managers.isEmpty()) {
                logger.warn("级别 {} 没有配置责任人，跳过", alertLevel);
                continue;
            }
            
            logger.info("级别 {} 需要通知 {} 个责任人", alertLevel, managers.size());
            
            // 5. 为每个责任人创建提醒记录
            for (NxMachineDeviceManagerEntity manager : managers) {
                // 构建提醒消息
                String message = buildAlertMessage(threshold, deviceId, currentPaperCount);
                
                // 创建提醒记录
                NxMachineAlertRecordEntity record = new NxMachineAlertRecordEntity();
                record.setNxArDeviceId(deviceId);
                record.setNxArManagerId(manager.getNxDmManagerId());
                record.setNxArAlertLevel(alertLevel);
                record.setNxArPaperCount(currentPaperCount);
                record.setNxArMessage(message);
                record.setNxArSendStatus(2); // 待发送
                record.setNxArIsCleared(0); // 未清除
                record.setNxArIsRead(0); // 未读
                
                nxAlertRecordService.save(record);
                
                logger.info("已创建提醒记录，ID: {}, 管理员: {}", record.getNxArId(), manager.getNxDmManagerId());
                
                // 6. 发送微信模板消息（异步）
                try {
                    boolean sendSuccess = sendWxTemplateMessage(record.getNxArId());
                    if (sendSuccess) {
                        nxAlertRecordService.updateSendStatus(record.getNxArId(), 1); // 发送成功
                    } else {
                        nxAlertRecordService.updateSendStatus(record.getNxArId(), 0); // 发送失败
                    }
                } catch (Exception e) {
                    logger.error("发送微信模板消息失败", e);
                    nxAlertRecordService.updateSendStatus(record.getNxArId(), 0); // 发送失败
                }
            }
        }
    }

    @Override
    public boolean sendWxTemplateMessage(Long recordId) {
        logger.info("发送微信模板消息，记录ID: {}", recordId);
        
        try {
            // 1. 获取提醒记录
            NxMachineAlertRecordEntity record = nxAlertRecordService.queryObject(recordId);
            if (record == null) {
                logger.error("提醒记录不存在，ID: {}", recordId);
                return false;
            }
            
            // 2. 获取管理员信息（获取openid）
            NxMachineMarketManagerEntity manager = nxMarketManagerService.queryObject(record.getNxArManagerId());
            if (manager == null || manager.getNxMmWxOpenid() == null) {
                logger.warn("管理员不存在或未绑定微信，管理员ID: {}", record.getNxArManagerId());
                return false;
            }
            

            
            // 4. 获取设备信息
            NxMachinePrinterDeviceEntity device = nxPrinterDeviceService.queryObject(record.getNxArDeviceId());
            if (device == null) {
                logger.error("设备不存在，ID: {}", record.getNxArDeviceId());
                return false;
            }
            
            // 5. 准备模板消息数据
            // 根据模板字段映射：设备名称(thing1), 申请人(thing6), 打印任务时间(time2), 备注(thing4)
            Map<String, TemplateData> templateData = new HashMap<>();
            templateData.put("thing1", new TemplateData(device.getNxPdDeviceName() != null ? 
                device.getNxPdDeviceName() : "打印设备")); // 设备名称
            templateData.put("thing6", new TemplateData(manager.getNxMmName() != null ? 
                manager.getNxMmName() : "管理员")); // 申请人（责任人）
            templateData.put("time2", new TemplateData(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(record.getNxArCreateTime()))); // 提醒时间
            templateData.put("thing4", new TemplateData(record.getNxArMessage())); // 备注（提醒内容）
            
            // 6. 调用现有的发送订阅消息方法
            // TODO: 需要先申请并配置打印机耗材提醒的模板ID
            // 暂时使用日志记录
            logger.info("准备发送订阅消息：openid={}, deviceName={}, paperCount={}", 
                manager.getNxMmWxOpenid(), device.getNxPdDeviceName(), device.getNxPdPaperCount());
            
            // 调用微信订阅消息发送（参考WeNoticeService的实现）
            boolean sendResult = sendPrinterAlertMessage(manager.getNxMmWxOpenid(), 
                "pages/device/deviceList", // 跳转到设备列表页面
                templateData);
            
            if (sendResult) {
                logger.info("微信模板消息发送成功");
                return true;
            } else {
                logger.error("微信模板消息发送失败");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("发送微信模板消息失败", e);
            return false;
        }
    }
    
    /**
     * 发送打印机耗材提醒订阅消息
     * 参考WeNoticeService的实现模式
     */
    private boolean sendPrinterAlertMessage(String openId, String page, Map<String, TemplateData> map) {
        logger.info("发送打印机耗材提醒订阅消息，openId: {}", openId);
        try {
            MyAPPIDConfig myAPPIDConfig = new MyAPPIDConfig();
            // 使用打印机耗材提醒小程序的AppID和Secret（对应您的openid）
            String appId = myAPPIDConfig.getPrinterAlertAppId();
            String secret = myAPPIDConfig.getPrinterAlertSecret();

            // 打印机耗材提醒模板ID
            String templateId = "KRuGq4DSsh7lBc6-hkzo1CQJhFHSifBQ-H9qaI5kNgg"; // 打印任务提醒模板
            
            String urlPhone = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", 
                appId, secret);
            String strPhone = WeChatUtil.httpRequest(urlPhone, "GET", null);
            logger.info("获取access_token响应: {}", strPhone);
            
            JSONObject jsonObjectPhone = JSONObject.parseObject(strPhone);
            String accessToken = jsonObjectPhone.getString("access_token");
            
            SubscribeMessage subscribeMessage = new SubscribeMessage();
            subscribeMessage.setAccess_token(accessToken);
            subscribeMessage.setTouser(openId);
            subscribeMessage.setTemplate_id(templateId);
            subscribeMessage.setPage(page);
            subscribeMessage.setData(map);
            
            String urlP = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;
            String body = HttpRequest.post(urlP)
                .body(JSONUtil.toJsonStr(subscribeMessage), ContentType.JSON.getValue())
                .execute()
                .body();
            logger.info("微信订阅消息发送响应: {}", body);
            
            // 解析微信API响应
            JSONObject responseJson = JSONObject.parseObject(body);
            Integer errcode = responseJson.getInteger("errcode");
            
            if (errcode != null && errcode == 0) {
                logger.info("微信订阅消息发送成功");
                return true;
            } else {
                String errmsg = responseJson.getString("errmsg");
                logger.error("微信订阅消息发送失败，errcode: {}, errmsg: {}", errcode, errmsg);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("发送微信订阅消息异常", e);
            return false;
        }
    }

    /**
     * 构建提醒消息
     */
    private String buildAlertMessage(NxMachineAlertThresholdEntity threshold, Integer deviceId, Integer paperCount) {
        // 获取设备信息
        NxMachinePrinterDeviceEntity device = nxPrinterDeviceService.queryObject(deviceId);
        
        String deviceName = device.getNxPdDeviceName() != null ? 
            device.getNxPdDeviceName() : "设备" + deviceId;
        
        String template = threshold.getNxAtMessage();
        if (template == null || template.isEmpty()) {
            template = "设备【{device_name}】纸张余量：{paper_count}张";
        }
        
        // 替换占位符
        String message = template
            .replace("{device_name}", deviceName)
            .replace("{paper_count}", String.valueOf(paperCount));
        
        return message;
    }
}


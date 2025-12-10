package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 提醒记录实体类
 * 
 * @author lpy
 * @date 2025-10-14
 */
@Setter
@Getter
@ToString
public class NxMachineAlertRecordEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private Long nxArId;

    /**
     * 设备ID（外键→nx_printer_device）
     */
    private Integer nxArDeviceId;

    /**
     * 接收管理员ID（外键→nx_market_manager）
     */
    private Integer nxArManagerId;

    /**
     * 提醒级别（1-4）
     */
    private Integer nxArAlertLevel;

    /**
     * 触发时的纸张数量
     */
    private Integer nxArPaperCount;

    /**
     * 提醒内容
     */
    private String nxArMessage;

    /**
     * 发送状态（0-失败 1-成功 2-待发送）
     */
    private Integer nxArSendStatus;

    /**
     * 发送时间
     */
    private Date nxArSendTime;

    /**
     * 是否已清除（0-未清除 1-已清除）【防重复关键字段】
     */
    private Integer nxArIsCleared;

    /**
     * 清除时间（加纸后）
     */
    private Date nxArClearTime;

    /**
     * 是否已读（0-未读 1-已读）
     */
    private Integer nxArIsRead;

    /**
     * 已读时间
     */
    private Date nxArReadTime;

    /**
     * 创建时间
     */
    private Date nxArCreateTime;

    /**
     * 关联设备实体
     */
    private NxMachinePrinterDeviceEntity nxPrinterDeviceEntity;

    /**
     * 关联管理员实体
     */
    private NxMachineMarketManagerEntity nxMarketManagerEntity;
}


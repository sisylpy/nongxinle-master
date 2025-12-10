package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 提醒阈值配置实体类
 * 
 * @author lpy
 * @date 2025-10-14
 */
@Setter
@Getter
@ToString
public class NxMachineAlertThresholdEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    private Integer nxAtId;

    /**
     * 设备ID（外键→nx_printer_device）
     */
    private Integer nxAtDeviceId;

    /**
     * 提醒级别（1-低 2-中 3-高 4-紧急）
     */
    private Integer nxAtLevel;

    /**
     * 阈值（剩余张数）
     */
    private Integer nxAtThreshold;

    /**
     * 提醒消息模板
     */
    private String nxAtMessage;

    /**
     * 是否启用（0-否 1-是）
     */
    private Integer nxAtEnable;

    /**
     * 创建时间
     */
    private Date nxAtCreateTime;

    /**
     * 关联设备实体
     */
    private NxMachinePrinterDeviceEntity nxPrinterDeviceEntity;
}


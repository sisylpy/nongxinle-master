package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备责任人绑定实体类
 * 
 * @author lpy
 * @date 2025-10-14
 */
@Setter
@Getter
@ToString
public class NxMachineDeviceManagerEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 绑定ID
     */
    private Integer nxDmId;

    /**
     * 设备ID（外键→nx_printer_device）
     */
    private Integer nxDmDeviceId;

    /**
     * 管理员ID（外键→nx_market_manager）
     */
    private Integer nxDmManagerId;

    /**
     * 通知级别（1-4，≥此级别才通知）
     */
    private Integer nxDmNotifyLevel;

    /**
     * 是否主要责任人（0-否 1-是，接收所有级别）
     */
    private Integer nxDmIsPrimary;

    /**
     * 是否启用（0-否 1-是）
     */
    private Integer nxDmEnable;

    /**
     * 绑定时间
     */
    private Date nxDmCreateTime;

    /**
     * 关联设备实体
     */
    private NxMachinePrinterDeviceEntity nxPrinterDeviceEntity;

    /**
     * 关联管理员实体
     */
    private NxMachineMarketManagerEntity nxMarketManagerEntity;
}


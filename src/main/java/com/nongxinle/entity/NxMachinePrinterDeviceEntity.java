package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 打印设备实体类
 * 
 * @author lpy
 * @date 2025-10-14
 */
@Setter
@Getter
@ToString
public class NxMachinePrinterDeviceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    private Integer nxPdId;

    /**
     * 所属市场ID（外键→sys_city_market）
     */
    private Integer nxPdMarketId;

    /**
     * 设备编号（唯一，格式：PD202410140001）
     */
    private String nxPdDeviceNo;

    /**
     * 设备名称（如"1号打印机"）
     */
    private String nxPdDeviceName;

    /**
     * 设备型号
     */
    private String nxPdModel;

    /**
     * 纸张类型（1-整张 2-半张 3-三分之一张）
     */
    private Integer nxPdPaperType;

    /**
     * 打印单据价格（元/张）
     */
    private java.math.BigDecimal nxPdPrintPrice;

    /**
     * 设备位置（如"北区出口"）
     */
    private String nxPdLocation;

    /**
     * 当前纸张数量（张）
     */
    private Integer nxPdPaperCount;

    /**
     * 纸张最大容量（张）
     */
    private Integer nxPdPaperMax;

    /**
     * 设备状态（0-离线 1-正常 2-故障 3-缺纸）
     */
    private Integer nxPdStatus;

    /**
     * 设备二维码URL
     */
    private String nxPdQrCode;

    /**
     * 安装日期
     */
    private Date nxPdInstallDate;

    /**
     * 余量最后更新时间
     */
    private Date nxPdLastUpdateTime;

    /**
     * 创建时间
     */
    private Date nxPdCreateTime;

    /**
     * 关联市场实体
     */
    private SysCityMarketEntity sysCityMarketEntity;

    /**
     * 设备的阈值配置列表
     */
    private List<NxMachineAlertThresholdEntity> alertThresholds;

    /**
     * 设备的责任人列表
     */
    private List<NxMachineDeviceManagerEntity> deviceManagers;
}


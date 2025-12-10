package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 加纸记录实体类
 * 
 * @author lpy
 * @date 2025-10-14
 */
@Setter
@Getter
@ToString
public class NxMachinePaperRefillRecordEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private Long nxPrrId;

    /**
     * 设备ID（外键→nx_printer_device）
     */
    private Integer nxPrrDeviceId;

    /**
     * 加纸前数量（张）
     */
    private Integer nxPrrBeforeCount;

    /**
     * 增加数量（张，校准时为负数）
     */
    private Integer nxPrrAddCount;

    /**
     * 加纸后数量（张）
     */
    private Integer nxPrrAfterCount;

    /**
     * 作废数量（张）：正常加纸=旧纸作废，手动校准=校准作废
     */
    private Integer nxPrrWasteCount;

    /**
     * 操作人ID（外键→nx_market_manager）
     */
    private Integer nxPrrOperatorId;

    /**
     * 操作人姓名
     */
    private String nxPrrOperatorName;

    /**
     * 加纸类型（1-正常加纸/更换 3-手动校准）
     */
    private Integer nxPrrRefillType;

    /**
     * 纸张类型（1-整张 2-半张 3-三分之一张）
     * 记录本次加纸时使用的纸张规格，用于历史追溯
     */
    private Integer nxPrrPaperType;

    /**
     * 备注
     */
    private String nxPrrRemark;

    /**
     * 操作时间
     */
    private Date nxPrrCreateTime;

    /**
     * 关联设备实体
     */
    private NxMachinePrinterDeviceEntity nxPrinterDeviceEntity;

    /**
     * 关联操作人实体
     */
    private NxMachineMarketManagerEntity nxMarketManagerEntity;
}


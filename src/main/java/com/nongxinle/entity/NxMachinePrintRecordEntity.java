package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 自助打印记录实体类
 * 
 * @author lpy
 * @date 2025-10-15
 */
@Setter
@Getter
@ToString
public class NxMachinePrintRecordEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 打印记录ID
     */
    private Integer nxPrId;

    /**
     * 单据ID（外键→nx_department_bill.nx_department_bill_id）
     */
    private Integer nxPrBillId;

    /**
     * 打印机设备ID（外键→nx_machine_printer_device.nx_pd_id）
     */
    private Integer nxPrDeviceId;

    /**
     * 市场ID
     */
    private Integer nxPrMarketId;

    /**
     * 配送商ID
     */
    private Integer nxPrDistributerId;

    /**
     * 打印时配送商状态快照（-1体验 0正式）
     * 记录打印时配送商的状态，用于区分体验打印和正式计费打印
     */
    private Integer nxPrDistributerType;

    /**
     * 纸张类型（1-整张 2-半张 3-三分之一张）
     */
    private Integer nxPrPaperType;

    /**
     * 消耗纸张数量（张）
     */
    private Integer nxPrPaperCount;

    /**
     * 打印时间
     */
    private Date nxPrPrintTime;

    /**
     * 打印状态（1-成功 0-失败）
     */
    private Integer nxPrPrintStatus;

    /**
     * 单据金额（便于统计总金额）
     */
    private BigDecimal nxPrBillTotal;

    /**
     * 单据日期（便于按日期统计）
     */
    private Date nxPrBillDate;

    /**
     * 单据流水号
     */
    private String nxPrBillTradeNo;

    /**
     * 操作人ID
     */
    private Integer nxPrOperatorId;

    /**
     * 操作人姓名
     */
    private String nxPrOperatorName;

    /**
     * 备注
     */
    private String nxPrRemark;

    /**
     * 创建时间
     */
    private Date nxPrCreateTime;

    /**
     * 关联的打印设备实体（用于查询）
     */
    private NxMachinePrinterDeviceEntity printerDevice;
}

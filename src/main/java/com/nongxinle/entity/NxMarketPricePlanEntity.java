package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 市场价格方案实体类
 * @author lpy
 * @date 2025-01-09
 */
@Setter
@Getter
@ToString
public class NxMarketPricePlanEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Integer nxMppId;

    /**
     * 市场ID（外键→sys_city_market）
     */
    private Integer nxMppMarketId;

    /**
     * 方案类型（0-流量 1-设备）
     */
    private Integer nxMppType;

    /**
     * 方案名称
     */
    private String nxMppPlanName;

    /**
     * 数量/规格（如：10万条、便携式打印机）
     */
    private String nxMppQuantity;

    /**
     * 价格（单位：分）
     */
    private Integer nxMppPrice;

    /**
     * 单价说明（如：1分/条、便携式小票打印机）
     */
    private String nxMppUnitPrice;

    /**
     * 方案描述
     */
    private String nxMppDescription;

    /**
     * 图片URL
     */
    private String nxMppImageUrl;

    /**
     * 排序（数字越小越靠前）
     */
    private Integer nxMppSortOrder;

    /**
     * 状态（1-启用 0-禁用）
     */
    private Integer nxMppStatus;

    /**
     * 创建时间
     */
    private LocalDateTime nxMppCreateTime;

    /**
     * 更新时间
     */
    private LocalDateTime nxMppUpdateTime;
}

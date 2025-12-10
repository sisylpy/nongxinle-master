package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 采购商品简化DTO（只包含前端显示需要的字段）
 * 用于采购商品分页接口，减少数据传输量
 * @author lpy
 */
@Setter
@Getter
@ToString
public class PurchaseGoodsSimpleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 采购商品ID
     */
    private Integer nxDistributerPurchaseGoodsId;

    /**
     * 配送商品ID
     */
    private Integer nxDpgDisGoodsId;

    /**
     * 采购数量
     */
    private String nxDpgQuantity;

    /**
     * 采购规格
     */
    private String nxDpgStandard;

    /**
     * 采购状态
     */
    private Integer nxDpgStatus;

    /**
     * 订单数量
     */
    private Integer nxDpgOrdersAmount;

    /**
     * 商品ID
     */
    private Integer nxDistributerGoodsId;

    /**
     * 商品名称
     */
    private String nxDgGoodsName;

    /**
     * 规格名称
     */
    private String nxDgGoodsStandardname;

    /**
     * 规格重量
     */
    private String nxDgGoodsStandardWeight;

    /**
     * 箱单位
     */
    private String nxDgCartonUnit;

    /**
     * 品牌
     */
    private String nxDgGoodsBrand;

    /**
     * 商品类别ID（grand级别）
     */
    private Integer nxDgDfgGoodsGrandId;

    /**
     * 曾祖父商品ID（great-grand级别）
     */
    private Integer nxDgDfgGoodsGreatGrandId;

    /**
     * 曾祖父商品名称（great-grand级别）
     */
    private String nxDgDfgGoodsGreatGrandName;

    /**
     * 曾祖父商品排序（great-grand级别）
     */
    private Integer nxDgDfgGoodsGreatGrandSort;

    /**
     * 采购自动标识（-1: 出库商品, 1: 采购商品）
     */
    private Integer nxDgPurchaseAuto;

    /**
     * 订单列表（简化版）
     */
    private List<PurchaseOrderSimpleDTO> orders;
}


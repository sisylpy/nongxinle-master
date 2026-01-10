package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 出库商品简化DTO（只包含前端显示需要的字段）
 * 用于出库商品分页接口，减少数据传输量
 * @author lpy
 */
@Setter
@Getter
@ToString
public class OutGoodsSimpleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 配送商品ID
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

    private Integer nxDgDfgGoodsFatherId;
    private Integer nxDgDfgGoodsGrandId;
    /**
     * 品牌
     */
    private String nxDgGoodsBrand;

    /**
     * 产地
     */
    private String nxDgGoodsPlace;

    /**
     * 商品详情
     */
    private String nxDgGoodsDetail;

    /**
     * 曾祖父商品ID（great-grand级别）
     */
    private Integer nxDgDfgGoodsGreatGrandId;

    /**
     * 曾祖父商品名称（great-grand级别）
     */
    private String nxDgDfgGoodsGreatGrandName;

    /**
     * 是否选中（用于前端选择商品）
     */
    private Boolean isSelected = false;

    /**
     * 订单列表（简化版）
     */
    private List<OutOrderSimpleDTO> nxDepartmentOrdersEntities;
}


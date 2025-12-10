package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 曾祖父商品简化DTO（只包含前端显示需要的字段）
 * 用于 disGetTypePrepareOutCata 接口，减少数据传输量
 * @author lpy
 */
@Setter
@Getter
@ToString
public class GreatGrandFatherGoodsSimpleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 曾祖父商品ID
     */
    private Integer nxDistributerFatherGoodsId;

    /**
     * 曾祖父商品名称
     */
    private String nxDfgFatherGoodsName;

    /**
     * 曾祖父商品排序
     */
    private Integer nxDfgFatherGoodsSort;

    /**
     * 曾祖父商品颜色
     */
    private String nxDfgFatherGoodsColor;

    /**
     * 曾祖父商品图片
     */
    private String nxDfgFatherGoodsImg;

    /**
     * 订单数量
     */
    private Integer newOrderCount;
}


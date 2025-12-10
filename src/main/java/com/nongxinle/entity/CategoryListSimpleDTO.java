package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 类别列表简化DTO（只包含类别列表需要的字段）
 * 用于 stockerGetCategoryListWithDepIds 接口，减少数据传输量
 * @author lpy
 */
@Setter
@Getter
@ToString
public class CategoryListSimpleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 类别ID（曾祖父级别）
     */
    private Integer nxDistributerFatherGoodsId;

    /**
     * 类别名称
     */
    private String nxDfgFatherGoodsName;

    /**
     * 类别排序
     */
    private Integer nxDfgFatherGoodsSort;

    /**
     * 类别颜色
     */
    private String nxDfgFatherGoodsColor;

    /**
     * 订单数量
     */
    private Integer newOrderCount;
}


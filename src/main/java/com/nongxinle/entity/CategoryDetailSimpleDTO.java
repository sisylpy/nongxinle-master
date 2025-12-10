package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 类别详情简化DTO（只包含前端显示需要的字段）
 * 用于类别商品接口，减少数据传输量
 * @author lpy
 */
@Setter
@Getter
@ToString
public class CategoryDetailSimpleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 类别ID（曾祖父级别 - Level 0）
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
     * 商品列表（简化版）
     */
    private List<CategoryGoodsSimpleDTO> goodsList;
}


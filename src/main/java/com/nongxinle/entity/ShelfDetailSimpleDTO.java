package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 货架详情简化DTO（只包含前端显示需要的字段）
 * @author lpy
 */
@Setter
@Getter
@ToString
public class ShelfDetailSimpleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 货架ID
     */
    private Integer nxDistributerGoodsShelfId;

    /**
     * 货架名称
     */
    private String nxDistributerGoodsShelfName;

    /**
     * 货架排序
     */
    private Integer nxDistributerGoodsShelfSort;

    /**
     * 分销商ID
     */
    private Integer nxDistributerGoodsShelfDisId;

    /**
     * 商品列表（简化版）
     */
    private List<ShelfGoodsShelfGoodsSimpleDTO> goodsList;
}


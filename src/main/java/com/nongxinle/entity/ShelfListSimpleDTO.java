package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 货架列表简化DTO（只包含货架列表需要的字段）
 * 用于 stockerGetShelfListWithDepIds 接口，减少数据传输量
 * @author lpy
 */
@Setter
@Getter
@ToString
public class ShelfListSimpleDTO implements Serializable {
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
     * 货架所属分销商ID
     */
    private Integer nxDistributerGoodsShelfDisId;

    /**
     * 订单数量
     */
    private Integer newOrderCount;
}


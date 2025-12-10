package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 货架商品关联简化DTO
 * @author lpy
 */
@Setter
@Getter
@ToString
public class ShelfGoodsShelfGoodsSimpleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 货架商品ID
     */
    private Integer nxDistributerGoodsShelfGoodsId;

    /**
     * 货架ID
     */
    private Integer nxDgsgShelfId;

    /**
     * 商品ID
     */
    private Integer nxDgsgDisGoodsId;

    /**
     * 排序
     */
    private Integer nxDgsgSort;

    /**
     * 货架排序
     */
    private Integer nxDgsgShelfSort;

    /**
     * 货架层级
     */
    private Integer nxDgsgShelfLayer;

    /**
     * 商品信息（简化版）
     */
    private ShelfGoodsSimpleDTO goods;

    /**
     * 同一商品在其他货架的信息（只包含货架名称）
     */
    private List<ShelfGoodsShelfGoodsSimpleDTO> sameShelfGoods;

    /**
     * 货架名称（用于 sameShelfGoods 中显示）
     */
    private String shelfName;
}


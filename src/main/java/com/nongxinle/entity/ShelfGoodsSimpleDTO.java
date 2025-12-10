package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 货架商品简化DTO（只包含前端显示需要的字段）
 * @author lpy
 */
@Setter
@Getter
@ToString
public class ShelfGoodsSimpleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

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
     * 订单列表（简化版）
     */
    private List<ShelfOrderSimpleDTO> orders;
}


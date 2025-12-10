package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 类别商品简化DTO（只包含前端显示需要的字段）
 * 用于类别商品接口，减少数据传输量
 * 注意：可以复用 ShelfGoodsSimpleDTO，但为了语义清晰，单独创建
 * @author lpy
 */
@Setter
@Getter
@ToString
public class CategoryGoodsSimpleDTO implements Serializable {
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
    private List<CategoryOrderSimpleDTO> orders;
}


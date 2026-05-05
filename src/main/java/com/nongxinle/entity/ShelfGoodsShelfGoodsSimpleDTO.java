package com.nongxinle.entity;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 货架商品关联简化DTO
 * @author lpy
 */
@JSONType(serialzeFeatures = {SerializerFeature.WriteMapNullValue})
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
     * 同一层内序号（从 1 递增）
     */
    private Integer nxDgsgShelfLayerSeq;

    /**
     * 是否层尾：1=该层最后一行，0=非层尾
     */
    private Integer nxDgsgShelfLayerLast;

    /**
     * 是否重复出现在多个货架：0 否，1 是
     */
    private Integer nxDgsgIsDuplicate;

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

    /**
     * 该货架商品的库存批次列表（用于 sameShelfGoods 中显示其他货架的库存，含生产日期、保质期、过期日期等）
     */
    private List<NxDistributerGoodsShelfStockEntity> nxDisGoodsShelfStockEntities;
}


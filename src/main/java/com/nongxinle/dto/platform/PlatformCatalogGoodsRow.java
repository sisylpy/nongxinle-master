package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 平台「全部类别」商品行：标准 nx 商品；可选附带当日平台待分配订单。
 */
@Getter
@Setter
@ToString
public class PlatformCatalogGoodsRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxGoodsId;
    private String nxGoodsName;
    private String nxGoodsStandardname;
    private String nxGoodsStandardWeight;
    private Integer nxGoodsGrandId;
    private Integer nxGoodsGreatGrandId;
    private String nxGoodsFile;
    private String nxGoodsBrand;
    private String nxGoodsItemsPerCarton;
    private String nxGoodsCartonUnit;

    /** 当日平台待分配订单（若有） */
    private Integer platformOrderId;
    private String platformOrderQuantity;
    private String platformOrderStandard;
}

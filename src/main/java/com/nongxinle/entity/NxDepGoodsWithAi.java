package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Setter
@Getter
@ToString
public class NxDepGoodsWithAi implements Serializable {

    private Integer nxDepartmentDisGoodsId;

    private Integer nxDdgDepartmentId;
    /**
     *
     */
    private Integer nxDdgDisGoodsId;

    private Integer nxDdgDisGoodsGrandId;

    private Integer nxDdgDisGoodsGreatId;

    private String nxDdgOrderPriceLevel;
    private String nxDdgOrderPrice;
    private String nxDdgOrderDate;
    private String nxDdgOrderRemark;
    private String nxDdgOrderQuantity;
    private String nxDdgOrderStandard;
    private String nxDdgGoodsPlace;
    private String nxDdgOrderCostPrice;
    private String nxDdgOrderGoodsName;
    private String nxDdgPickDetail;



}

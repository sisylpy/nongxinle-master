package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 出库订单简化DTO（只包含前端显示需要的字段）
 * @author lpy
 */
@Setter
@Getter
@ToString
public class OutOrderSimpleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Integer nxDepartmentOrdersId;

    /**
     * 订单数量
     */
    private String nxDoQuantity;

    /**
     * 订单规格
     */
    private String nxDoStandard;

    /**
     * 订单备注
     */
    private String nxDoRemark;

    /**
     * 商品类型
     */
    private Integer nxDoGoodsType;

    /**
     * 部门ID
     */
    private Integer nxDoDepartmentId;

    /**
     * 部门父ID
     */
    private Integer nxDoDepartmentFatherId;

    /**
     * 部门属性名称
     */
    private String nxDepartmentAttrName;

    /**
     * 父部门属性名称
     */
    private String fatherDepartmentAttrName;

    /**
     * GB部门ID
     */
    private Integer nxDoGbDepartmentId;

    /**
     * GB部门父ID
     */
    private Integer nxDoGbDepartmentFatherId;

    /**
     * GB部门名称
     */
    private String gbDepartmentName;

    /**
     * GB父部门名称
     */
    private String fatherGbDepartmentName;

    /**
     * 餐厅ID
     */
    private Integer nxDoNxCommRestrauntId;

    /**
     * 餐厅属性名称
     */
    private String nxRestrauntAttrName;

    /**
     * 是否选中（用于前端选择订单）
     */
    private Boolean purSelected = false;
}


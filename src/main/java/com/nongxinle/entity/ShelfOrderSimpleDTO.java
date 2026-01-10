package com.nongxinle.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 货架订单简化DTO（只包含前端显示需要的字段）
 * @author lpy
 */
@Setter
@Getter
@ToString
public class ShelfOrderSimpleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Integer nxDepartmentOrdersId;

    /**
     * 数量
     */
    private String nxDoQuantity;

    /**
     * 规格
     */
    private String nxDoStandard;

    /**
     * 重量
     */
    private String nxDoWeight;

    /**
     * 备注
     */
    private String nxDoRemark;

    /**
     * 打印规格（按货架显示时需要）
     */
    private String nxDoPrintStandard;

    /**
     * 部门名称（扁平化字符串，格式：父部门.部门名称 或 部门名称）
     */
    private String depName;

    /**
     * 部门属性名称（单独返回）
     */
    private String nxDepartmentAttrName;

    /**
     * 父部门属性名称（单独返回）
     */
    private String fatherDepartmentAttrName;

    /**
     * 部门订货代号
     */
    private String nxDepartmentOrderCode;

    /**
     * 父部门订货代号
     */
    private String fatherDepartmentOrderCode;

    /**
     * GB部门名称（扁平化字符串，格式：父GB部门.GB部门名称 或 GB部门名称）
     */
    private String gbDepName;

    /**
     * 餐厅名称
     */
    private String restrauntName;

    /**
     * 拣货详情（按货架显示时需要）
     */
    private String pickDetail;
}


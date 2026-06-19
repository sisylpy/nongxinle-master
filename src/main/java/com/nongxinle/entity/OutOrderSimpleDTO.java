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
     * 部门订货代号
     */
    private String nxDepartmentOrderCode;

    /**
     * 父部门订货代号
     */
    private String fatherDepartmentOrderCode;

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
     * 配送商ID
     */
    private Integer nxDoDistributerId;

    /**
     * 协作订单标识：-1=主订单，其他=协作订单（协作伙伴配送商id）
     */
    private Integer nxDoCollaborativeNxDisId;

    /**
     * 配送商名称（优先 nx_distributer_show_name）
     */
    private String nxDoDistributerName;

    /**
     * 协作商名称（协作订单时，nx_DO_collaborative_nx_dis_id 对应的配送商名称）
     */
    private String nxDoCollaborativeDistributerName;

    /**
     * 餐厅属性名称
     */
    private String nxRestrauntAttrName;

    /**
     * 是否选中（用于前端选择订单）
     */
    private Boolean purSelected = false;

    /**
     * 订单单价（展示用）
     */
    private String nxDoPrice;

    /**
     * 采购/出库状态
     */
    private Integer nxDoPurchaseStatus;

    /**
     * 平台订单：1 平台 / 0 自有
     */
    private Integer isPlatformOrder;

    /**
     * PLATFORM / OWN
     */
    private String orderSource;

    private Integer platformAssignId;

    private String platformFulfillmentStatus;

    /**
     * 平台行展示标签，固定「平台」
     */
    private String platformLabel;

    /**
     * 平台 ASSIGNED 单：实际价可改（相对期望价），期望价本身不在配送商端修改。
     */
    private Integer priceEditable;

    /**
     * 平台分配参考价 / 期望单价（同 expectPrice）
     */
    private String nxDoExpectPrice;

    /**
     * 前端推荐字段：期望价
     */
    private String expectPrice;

    /**
     * 实际单价（同 nxDoPrice）
     */
    private String actualPrice;

    /**
     * 实际价 - 期望价（同 priceDifferent）
     */
    private String nxDoPriceDifferent;

    /**
     * 前端推荐字段：差价
     */
    private String priceDifferent;

    /**
     * 平台价格展示提示
     */
    private String platformPriceLabel;

    /**
     * 排序：平台行 0，自有行 1
     */
    private Integer platformSort;
}


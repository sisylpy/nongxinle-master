package com.nongxinle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 粘贴搜索商品接口响应DTO
 * 只包含前端需要的字段，减少数据传输量
 * 
 * @author lpy
 */
@Setter
@Getter
@ToString
public class PasteSearchGoodsResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Integer nxDepartmentOrdersId;

    /**
     * 商品名称
     */
    private String nxDoGoodsName;

    /**
     * 原始商品名称（保存用户输入的原始商品名称）
     */
    private String nxDoGoodsNameOriginal;

    /**
     * 数量
     */
    private String nxDoQuantity;

    /**
     * 规格（默认值：斤）
     */
    private String nxDoStandard;

    /**
     * 备注
     */
    private String nxDoRemark;

    /**
     * 是否有备注（true表示有备注且不为空）
     */
    private Boolean nxDoAddRemark;

    /**
     * 订单状态
     */
    private Integer nxDoStatus;

    /**
     * 部门ID
     */
    private Integer nxDoDepartmentId;

    /**
     * 父部门ID
     */
    private Integer nxDoDepartmentFatherId;

    /**
     * 分销商商品ID
     */
    private Integer nxDoDisGoodsId;

    /**
     * 规格警告（默认值：0）
     */
    private Integer nxDoStandardWarn;

    /**
     * 商品名称警告（默认值：0）
     */
    private Integer goodsNameWarn;

    /**
     * 分销商ID
     */
    private Integer nxDoDistributerId;

    /**
     * 采购用户ID（默认值：-1）
     */
    private Integer nxDoPurchaseUserId;

    /**
     * 订单用户ID
     */
    private Integer nxDoOrderUserId;

    /**
     * 是否代理（默认值：-1）
     */
    private Integer nxDoIsAgent;

    /**
     * 今日订单序号
     */
    private Integer nxDoTodayOrder;

    /**
     * 分销商商品候选列表（当找到多个匹配的分销商商品时）
     */
    private List<DistributerGoodsCandidateDTO> nxDistributerGoodsEntityList;

    /**
     * 系统商品候选列表（当找到多个匹配的系统商品时）
     */
    private List<NxGoodsCandidateDTO> nxGoodsEntities;
}


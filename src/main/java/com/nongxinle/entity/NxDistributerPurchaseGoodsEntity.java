package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-24 11:45
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerPurchaseGoodsEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商采购商品id
	 */
	private Integer nxDistributerPurchaseGoodsId;
	/**
	 *  采购商品id
	 */
	private Integer nxDpgDisGoodsId;
	/**
	 *  采购父级商品id
	 */
	private Integer nxDpgDisGoodsFatherId;
	/**
	 *  采购数量
	 */
	private String nxDpgQuantity;
	/**
	 *  采购规格
	 */
	private String nxDpgStandard;
	/**
	 *  采购状态
	 */
	private Integer nxDpgStatus;
	/**
	 *  采购批发商id
	 */
	private Integer nxDpgDistributerId;
	/**
	 *  采购方式
	 */
	private Integer nxDpgPurchaseType;
	private Integer nxDpgDisGoodsGrandId;
	/**
	 *  采购时间
	 */
	private String nxDpgTime;

	private String nxDpgApplyDate;

	private Integer nxDpgBatchId;

	private Integer nxDpgBuyUserId;
	private Integer nxDpgPayType;

	private String nxDpgBuyPrice;

	private String nxDpgBuyQuantity;
	private Integer nxDpgNxWeightId;
	private Integer nxDpgFinishAmount;


	private Boolean isSelected;
	private Boolean isShowTools = false;


	private List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;
	private NxDepartmentOrdersEntity nxDepartmentOrdersEntity;

	private NxDistributerGoodsEntity nxDistributerGoodsEntity;

	private NxDistributerUserEntity nxPurchaserEntity;
	private Integer nxDpgOrdersAmount;
    private Integer nxDpgTypeAddUserId;
    private Integer nxDpgPurUserId;
    private Integer nxDpgSellUserId;

    private Integer nxDpgInputType;
    private Integer nxDpgCostLevel;
    private String nxDpgPurchaseDate;
    private String nxDpgBuySubtotal;
    private String nxDpgExpectPrice;
    private String nxDpgExpectSubtotal;
    private String nxDpgStockRestWeight;

    private NxJrdhUserEntity sellerUserEntity;
    private NxJrdhUserEntity buyerUserEntity;
    private NxJrdhSupplierEntity jrdhSupplierEntity;
	private Integer nxDpgApplyShelfId;
	/**
	 * 溯源报告ID
	 */
	private Integer nxDpgTraceReportId;
	/**
	 * 生产日期（如 2025-03-18）
	 */
	private String nxDpgProduceDate;
	/**
	 * 保质期（数值，如 3、7、15）
	 */
	private Integer nxDpgShelfLife;
	/**
	 * 保质期单位（天/月/年）
	 */
	private String nxDpgShelfLifeUnit;
	/**
	 * 过期日期（如 2025-03-21）
	 */
	private String nxDpgExpiryDate;

    private List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsEntities;
    private NxDistributerGoodsShelfEntity applyShelfEntity;

    // 库存批次列表（根据该采购商品入库的库存批次）
    private List<NxDistributerGoodsShelfStockEntity> nxDisGoodsShelfStockEntities;

    private NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity;
}

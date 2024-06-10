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

public class GbDistributerPurchaseGoodsEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商采购商品id
	 */
	private Integer gbDistributerPurchaseGoodsId;
	/**
	 *  采购商品id
	 */
	private Integer gbDpgDisGoodsId;
	/**
	 *  采购父级商品id
	 */
	private Integer gbDpgDisGoodsFatherId;
	/**
	 *  采购数量
	 */
	private String gbDpgQuantity;
	/**
	 *  采购规格
	 */
	private String gbDpgStandard;
	/**
	 *  采购状态
	 */
	private Integer gbDpgStatus;
	/**
	 *  采购批发商id
	 */
	private Integer gbDpgDistributerId;
	/**
	 *  采购方式
	 */
	private Integer gbDpgPurchaseType;
	private Integer gbDpgPurchaseNxDistributerId;
	/**
	 *  采购时间
	 */
	private String gbDpgTime;

	private String gbDpgApplyDate;

	private Integer gbDpgBatchId;

	private Integer gbDpgPurUserId;

	private String gbDpgBuyPrice;

	private String gbDpgBuyQuantity;

	private Boolean isSelected;

	private Integer gbDpgDisGoodsPriceId;
	private Integer gbDpgIsCheck;


	private Integer gbDpgOrdersAmount;
    private Integer gbDpgTypeAddUserId;

    private Integer gbDpgInputType;
    private Integer gbDpgPayType;
    private String gbDpgPurchaseDate;
    private String gbDpgBuySubtotal;
    private Integer gbDpgPurchaseDepartmentId;
    private String gbDpgPurchaseMonth;
    private String gbDpgPurchaseYear;
    private String gbDpgPurchaseFullTime;
    private String gbDpgPurchaseWeek;
    private String gbDpgPurchaseWeekYear;
    private String gbDpgBuyScaleQuantity;
    private String gbDpgBuyScalePrice;
    private String gbDpgBuyScale;
    private String gbDpgBuyPriceReason;
    private String gbDpgWarnFullTime;
    private String gbDpgWasteFullTime;
    private Integer gbDpgWeightId;
    private Integer gbDpgOrdersFinishAmount;
    private GbDistributerGoodsPriceEntity gbDistributerGoodsPriceEntity;


	private List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities;
	private GbDepartmentOrdersEntity gbDepartmentOrdersEntity;

	private GbDistributerGoodsEntity gbDistributerGoodsEntity;

	private GbDistributerUserEntity gbDistributerUserEntity;
	private GbDepartmentEntity purchaseDepartmentEntity;
	private GbDepartmentUserEntity purchaseDepartmentUser;
	private GbDistributerPurchaseBatchEntity gbDisPurchaseBatchEntity;
	private List<GbDepartmentEntity> wasteDepartmentEntities;

	private List<GbDepartmentGoodsStockEntity> gbDepartmentGoodsStockEntities;



}

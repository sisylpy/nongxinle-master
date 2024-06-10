package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-22 20:41
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxRetailerPurchaseGoodsEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  零售商采购商品id
	 */
	private Integer nxRetailerPurchaseGoodsId;
	/**
	 *  零售商品id
	 */
	private Integer nxRpgShelfGoodsId;
	/**
	 *  零售货架id
	 */
	private Integer nxRpgShelfId;
	/**
	 *  采购数量
	 */
	private String nxRpgQuantity;
	/**
	 *  采购规格
	 */
	private String nxRpgStandard;
	/**
	 *  采购状态
	 */
	private Integer nxRpgStatus;
	/**
	 *  采购批发商id
	 */
	private Integer nxRpgRetailerId;
	/**
	 *  采购方式：“1 订单采购”“2 添加采购”
	 */
	private Integer nxRpgPurchaseType;
	/**
	 *  采购时间
	 */
	private String nxRpgTime;
	/**
	 *  采购批次号
	 */
	private Integer nxRpgBatchId;
	/**
	 *  采购方式为“采购”的采购员id
	 */
	private Integer nxRpgBuyUserId;
	/**
	 *  采购单价
	 */
	private String nxRpgBuyPrice;
	/**
	 *  采购数量
	 */
	private String nxRpgBuyQuantity;
	/**
	 *  添加采购用户id
	 */
	private Integer nxRpgTypeAddUserId;
	/**
	 *  
	 */
	private String nxRpgApplyDate;
	/**
	 *  采购日期
	 */
	private String nxRpgPurchaseDate;
	/**
	 *  
	 */
	private Integer nxRpgInputType;
	/**
	 *  
	 */
	private String nxRpgBuySubtotal;
	private String nxRpgGoodsName;

	private Boolean isLongTap = false;

	private NxRetailerGoodsShelfGoodsEntity nxRetailerGoodsShelfGoodsEntity;

	private Boolean isSelected = false;

}

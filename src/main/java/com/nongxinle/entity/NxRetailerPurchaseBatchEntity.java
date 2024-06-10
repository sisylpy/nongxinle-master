package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-22 20:41
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxRetailerPurchaseBatchEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商进货批次id
	 */
	private Integer nxRetailerPurchaseBatchId;
	/**
	 *  批发商进货批次状态
	 */
	private Integer nxRpbStatus;
	/**
	 *  批发商进货批次类型
	 */
	private Integer nxRpbType;
	/**
	 *  批发商进货批次时间
	 */
	private String nxRpbTime;
	/**
	 *  批发商进货采购员id
	 */
	private Integer nxRpbPurUserId;
	/**
	 *  批发商id
	 */
	private Integer nxRpbRetailerId;
	/**
	 *  
	 */
	private String nxRpbDate;
	/**
	 *  
	 */
	private String nxRpbHour;
	/**
	 *  
	 */
	private String nxRpbMinute;
	/**
	 *  
	 */
	private String nxRpbSellSubtotal;

	private String nxRpbSellUserImg;
	private Integer nxRpbSellUserId;
	private Integer nxRpbBuyUserId;
	private NxBuyUserEntity nxBuyUserEntity;
	private NxSellUserEntity nxSellUserEntity;
	private NxJrdhUserEntity nxJrdhSellerEntity;
	private NxJrdhUserEntity nxJrdhBuyerEntity;
	private List<NxRetailerPurchaseGoodsEntity> nxRetPurchaseGoodsEntityList;

	private NxRetailerUserEntity nxRetailerUserEntity;

}

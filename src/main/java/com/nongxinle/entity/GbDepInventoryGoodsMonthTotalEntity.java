package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 02-18 14:22
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepInventoryGoodsMonthTotalEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbInventoryGoodsMonthTotalId;
	/**
	 *  
	 */
	private Integer gbIgmtDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbIgmtDepartmentId;
	/**
	 *  
	 */
	private Integer gbIgmtDistributerId;
	/**
	 *  
	 */
	private Integer gbIgmtDisGoodsId;
	/**
	 *  
	 */
	private Integer gbIgmtDisGoodsFatherId;
	/**
	 *  
	 */
	private String gbIgmtMonth;
	/**
	 *  
	 */
	private String gbIgmtWeight;
	/**
	 *  
	 */
	private String gbIgmtSubtotal;
	/**
	 *  
	 */
	private String gbIgmtYear;
	/**
	 *  
	 */
	private String gbIgmtFullTime;
	/**
	 *  
	 */
	private String gbIgmtWasteWeight;
	/**
	 *  
	 */
	private String gbIgmtWasteSubtotal;
	/**
	 *  
	 */
	private Integer gbIgmtStatus;
	/**
	 *  
	 */
	private String gbIgmtLossWeight;
	/**
	 *  
	 */
	private String gbIgmtLossSubtotal;
	/**
	 *  
	 */
	private String gbIgmtReturnWeight;
	/**
	 *  
	 */
	private String gbIgmtReturnSubtotal;
	/**
	 *  
	 */
	private Integer gbIgmtDepDisGoodsId;
	/**
	 *  
	 */
	private String gbIgmtProduceWeight;
	/**
	 *  
	 */
	private String gbIgmtProduceSubtotal;
	/**
	 *  
	 */
	private String gbIgmtProfitPrice;
	/**
	 *  
	 */
	private String gbIgmtProfitWeight;
	/**
	 *  
	 */
	private String gbIgmtProfitSubtotal;

}

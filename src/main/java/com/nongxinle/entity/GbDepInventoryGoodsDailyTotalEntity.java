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

public class GbDepInventoryGoodsDailyTotalEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbInventoryGoodsDailyTotalId;
	/**
	 *  
	 */
	private Integer gbIgdtDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbIgdtDepartmentId;
	/**
	 *  
	 */
	private Integer gbIgdtDistributerId;
	/**
	 *  
	 */
	private Integer gbIgdtDisGoodsId;
	/**
	 *  
	 */
	private Integer gbIgdtDisGoodsFatherId;
	/**
	 *  
	 */
	private String gbIgdtDate;
	/**
	 *  
	 */
	private Integer gbIgdtStatus;
	/**
	 *  
	 */
	private String gbIgdtWeight;
	/**
	 *  
	 */
	private String gbIgdtSubtotal;
	/**
	 *  
	 */
	private String gbIgdtFullTime;
	/**
	 *  
	 */
	private String gbIgdtWasteWeight;
	/**
	 *  
	 */
	private String gbIgdtWasteSubtotal;
	/**
	 *  
	 */
	private String gbIgdtLossWeight;
	/**
	 *  
	 */
	private String gbIgdtLossSubtotal;
	/**
	 *  
	 */
	private String gbIgdtReturnWeight;
	/**
	 *  
	 */
	private String gbIgdtReturnSubtotal;
	/**
	 *  
	 */
	private Integer gbIgdtDepDisGoodsId;
	/**
	 *  
	 */
	private String gbIgdtProduceWeight;
	/**
	 *  
	 */
	private String gbIgdtProduceSubtotal;
	/**
	 *  
	 */
	private String gbIgdtProfitPrice;
	/**
	 *  
	 */
	private String gbIgdtProfitSubtotal;
	/**
	 *  
	 */
	private String gbIgdtProfitWeight;

	private GbDistributerGoodsEntity distributerGoodsEntity;
	private GbDepartmentDisGoodsEntity departmentDisGoodsEntity;
	private GbDepartmentEntity departmentEntity;

}

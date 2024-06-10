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

public class GbDepInventoryGoodsWeekTotalEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbInventoryGoodsWeekTotalId;
	/**
	 *  
	 */
	private Integer gbIgwtDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbIgwtDepartmentId;
	/**
	 *  
	 */
	private Integer gbIgwtDistributerId;
	/**
	 *  
	 */
	private Integer gbIgwtDisGoodsId;
	/**
	 *  
	 */
	private Integer gbIgwtDisGoodsFatherId;
	/**
	 *  
	 */
	private String gbIgwtWeek;
	/**
	 *  
	 */
	private String gbIgwtWeight;
	/**
	 *  
	 */
	private String gbIgwtSubtotal;
	/**
	 *  
	 */
	private String gbIgwtYear;
	/**
	 *  
	 */
	private String gbIgwtFullTime;
	/**
	 *  
	 */
	private String gbIgwtWasteWeight;
	/**
	 *  
	 */
	private String gbIgwtWasteSubtotal;
	/**
	 *  
	 */
	private Integer gbIgwtStatus;
	/**
	 *  
	 */
	private String gbIgwtLossWeight;
	/**
	 *  
	 */
	private String gbIgwtLossSubtotal;
	/**
	 *  
	 */
	private String gbIgwtReturnWeight;
	/**
	 *  
	 */
	private String gbIgwtReturnSubtotal;
	/**
	 *  
	 */
	private Integer gbIgwtDepDisGoodsId;
	/**
	 *  
	 */
	private String gbIgwtProduceWeight;
	/**
	 *  
	 */
	private String gbIgwtProduceSubtotal;
	/**
	 *  
	 */
	private String gbIgwtProfitPrice;
	/**
	 *  
	 */
	private String gbIgwtProfitWeight;
	/**
	 *  
	 */
	private String gbIgwtProfitSubtotal;

}

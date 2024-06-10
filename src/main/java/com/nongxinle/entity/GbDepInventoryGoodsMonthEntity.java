package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 08-23 14:08
 */

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepInventoryGoodsMonthEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbInventoryGoodsMonthId;
	/**
	 *  
	 */
	private Integer gbIgmDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbIgmDepartmentId;
	/**
	 *  
	 */
	private Integer gbIgmDisGoodsId;
	private Integer gbIgmDisGoodsFatherId;
	/**
	 *  
	 */
	private String gbIgmMonth;
	/**
	 *  
	 */
	private String gbIgmWeight;
	/**
	 *  
	 */
	private String gbIgmSubtotal;

	private  Integer gbIgmGbDepStockId;
	private  Integer gbIgmGbDepStockRecordId;
	private Integer gbIgmDistributerId;
	private Integer gbIgmStatus;
	private String gbIgmFullTime;
	private String gbIgmWasteWeight;
	private String gbIgmWasteSubtotal;
	private String gbIgmYear;
	private String gbIgmLossWeight;
	private String gbIgmReturnWeight;
	private String gbIgmLossSubtotal;
	private String gbIgmReturnSubtotal;
	private String gbIgmProduceWeight;
	private String gbIgmProduceSubtotal;

	private Integer gbIgmDepSettleId;
	private Integer gbIgmDepDisGoodsId;



	private GbDistributerGoodsEntity gbDistributerGoodsEntity;
	private GbDepartmentGoodsStockEntity gbDepartmentGoodsStockEntity;
	private GbDepartmentEntity gbDepartmentEntity;


}

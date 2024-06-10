package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 08-23 14:08
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepInventoryGoodsWeekEntity implements Serializable,Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbInventoryGoodsWeekId;
	/**
	 *  
	 */
	private Integer gbIgwDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbIgwDepartmentId;
	/**
	 *  
	 */
	private Integer gbIgwDisGoodsId;
	private Integer gbIgwDisGoodsFatherId;
	/**
	 *  
	 */
	private String gbIgwWeek;
	/**
	 *  
	 */
	private String gbIgwWeight;
	/**
	 *  
	 */
	private String gbIgwSubtotal;

	private Integer gbIgwGbDepStockId;
	private Integer gbIgwGbDepStockRecordId;
	private Integer gbIgwDistributerId;

	private String gbIgwFullTime;
	private String gbIgwWasteWeight;
	private String gbIgwWasteSubtotal;
	private Integer gbIgwStatus;
	private String gbIgwYear;
	private String gbIgwLossWeight;
	private String gbIgwReturnWeight;
	private String gbIgwReturnSubtotal;
	private String gbIgwProduceWeight;
	private String gbIgwProduceSubtotal;

	private String gbIgwLossSubtotal;
	private Integer gbIgwDepSettleId;
	private Integer gbIgwDepDisGoodsId;


	private GbDistributerGoodsEntity gbDistributerGoodsEntity;

	private GbDepartmentGoodsStockEntity gbDepartmentGoodsStockEntity;

	private GbDepartmentEntity gbDepartmentEntity;

	@Override
	public int compareTo(Object o) {

		if (o instanceof GbDepInventoryGoodsWeekEntity) {
			GbDepInventoryGoodsWeekEntity e = (GbDepInventoryGoodsWeekEntity) o;
			return this.gbInventoryGoodsWeekId.compareTo(e.gbInventoryGoodsWeekId);
		}
		return 0;
	}
}

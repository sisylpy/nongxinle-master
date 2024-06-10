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

public class GbDepInventoryGoodsDailyEntity implements Serializable,Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbInventoryGoodsDailyId;
	/**
	 *  
	 */
	private Integer gbIgdDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbIgdDepartmentId;
	/**
	 *  
	 */
	private Integer gbIgdDisGoodsId;
	/**
	 *  
	 */
	private String gbIgdDate;
	/**
	 *  
	 */
	private String gbIgdWeight;

	private Integer gbIgdDistributerId;
	/**
	 *  
	 */
	private String gbIgdSubtotal;
	private Integer gbIgdGbDepStockId;
	private Integer gbIgdGbDepStockRecordId;

	private Integer gbIgdDisGoodsFatherId;
    private String 	gbIgdFullTime;
    private String gbIgdWasteWeight;
    private String gbIgdWasteSubtotal;
    private Integer gbIgdStatus;
    private String gbIgdLossWeight;
    private String gbIgdLossSubtotal;
    private String gbIgdReturnWeight;
    private String gbIgdReturnSubtotal;
    private String gbIgdProduceWeight;
    private String gbIgdProduceSubtotal;
    private String gbIgdProfitSubtotal;
    private Integer gbIgdDepSettleId;
    private Integer gbIgdDepDisGoodsId;


	private GbDistributerGoodsEntity gbDistributerGoodsEntity;
	private GbDepartmentGoodsStockEntity gbDepartmentGoodsStockEntity;

	private GbDepartmentEntity gbDepartmentEntity;

	@Override
	public int compareTo(Object o) {
		if (o instanceof GbDepInventoryGoodsDailyEntity) {
			GbDepInventoryGoodsDailyEntity e = (GbDepInventoryGoodsDailyEntity) o;
			return this.gbInventoryGoodsDailyId.compareTo(e.gbInventoryGoodsDailyId);
		}
		return 0;

	}
}

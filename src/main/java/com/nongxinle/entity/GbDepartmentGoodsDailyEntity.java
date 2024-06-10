package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-16 17:06
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepartmentGoodsDailyEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDepartmentGoodsDailyId;
	/**
	 *  
	 */
	private Integer gbDgdGbDistributerId;
	/**
	 *  
	 */
	private Integer gbDgdGbDepartmentId;
	/**
	 *  
	 */
	private Integer gbDgdGbDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbDgdGbDisGoodsId;
	/**
	 *  
	 */
	private Integer gbDgdGbDisGoodsFatherId;
	/**
	 *  
	 */
	private Integer gbDgdGbDepDisGoodsId;
	/**
	 *  执行废弃用户
	 */
	private String gbDgdWeight;
	/**
	 *  执行废弃用户
	 */
	private String gbDgdRestWeight;
	/**
	 *  执行废弃用户
	 */
	private String gbDgdSubtotal;
	/**
	 *  盘库日期
	 */
	private String gbDgdDate;
	/**
	 *  执行废弃用户
	 */
	private String gbDgdDay;
	/**
	 *  盘库周
	 */
	private String gbDgdWeek;
	/**
	 *  盘库月
	 */
	private String gbDgdMonth;
	/**
	 *  执行废弃时间
	 */
	private String gbDgdFullTime;
	/**
	 *  盘库月
	 */
	private String gbDgdYear;
	/**
	 *  执行废弃用户
	 */
	private String gbDgdProduceWeight;
	/**
	 *  执行废弃数量
	 */
	private String gbDgdProduceSubtotal;
	/**
	 *  执行废弃用户
	 */
	private String gbDgdLossWeight;
	/**
	 *  执行废弃数量
	 */
	private String gbDgdLossSubtotal;
	/**
	 *  执行废弃用户
	 */
	private String gbDgdWasteWeight;
	/**
	 *  执行废弃数量
	 */
	private String gbDgdWasteSubtotal;
	/**
	 *  执行废弃用户
	 */
	private String gbDgdReturnWeight;
	/**
	 *  执行废弃数量
	 */
	private String gbDgdReturnSubtotal;
	/**
	 *  执行废弃数量
	 */
	private String gbDgdProfitSubtotal;
	/**
	 *  执行废弃数量
	 */
	private String gbDgdSalesSubtotal;
	/**
	 *  执行废弃用户
	 */
	private String gbDgdAfterProfitSubtotal;
	private String gbDgdSellClearHour;
	private String gbDgdSellClearMinute;
	private String gbDgdLastWeight;
	private String gbDgdFreshRate;
	private String gbDgdTaskTime;
	private String gbDgdLastProduceWeight;

	private GbDistributerGoodsEntity gbDistributerGoodsEntity;
	private List<GbDepartmentGoodsStockEntity> gbDepartmentGoodsStockEntities;
	private List<GbDepartmentGoodsStockReduceEntity> wasteReduceList;

}

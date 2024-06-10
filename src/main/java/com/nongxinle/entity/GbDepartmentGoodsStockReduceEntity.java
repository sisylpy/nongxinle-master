package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 11-20 12:33
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepartmentGoodsStockReduceEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDepartmentGoodsStockReduceId;
	/**
	 *  
	 */
	private Integer gbDgsrGbDistributerId;
	/**
	 *  
	 */
	private Integer gbDgsrGbDepartmentId;
	/**
	 *  
	 */
	private Integer gbDgsrGbDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbDgsrGbDisGoodsId;
	/**
	 *  
	 */
	private Integer gbDgsrGbDepDisGoodsId;
	/**
	 *  
	 */
	private Integer gbDgsrGbDisGoodsFatherId;
	/**
	 *  批次采购商品id
	 */
	private Integer gbDgsrGbGoodsStockId;
	private Integer gbDgsrGbGoodsStockRecordId;
	/**
	 *  盘库日期
	 */
	private String gbDgsrDate;
	/**
	 *  盘库周
	 */
	private String gbDgsrWeek;
	/**
	 *  盘库月
	 */
	private String gbDgsrMonth;
	/**
	 *  执行废弃时间
	 */
	private String gbDgsrFullTime;
	/**
	 *  1,cost;2waste;3loass;4return
	 */
	private Integer gbDgsrType;
	/**
	 *  执行废弃用户
	 */
	private Integer gbDgsrDoUserId;
	private Integer gbDgsrStockNxDistribtuerId;
	/**
	 *  执行废弃用户
	 */
	private String gbDgsrCostWeight;
	/**
	 *  执行废弃数量
	 */
	private String gbDgsrCostSubtotal;
	/**
	 *  执行废弃用户
	 */
	private String gbDgsrWasteWeight;
	/**
	 *  执行废弃数量
	 */
	private String gbDgsrWasteSubtotal;
	/**
	 *  执行废弃用户
	 */
	private String gbDgsrLossWeight;
	/**
	 *  执行废弃数量
	 */
	private String gbDgsrLossSubtotal;
	/**
	 *  执行废弃用户
	 */
	private String gbDgsrReturnWeight;
	/**
	 *  执行废弃数量
	 */
	private String gbDgsrReturnSubtotal;
	private String gbDgsrProduceWeight;
	private String gbDgsrProduceSubtotal;
	private String gbDgsrProfitSubtotal;
	private String gbDgsrSalesSubtotal;

	private Integer gbDgsrGbGoodsInventoryType;
	private Integer gbDgsrStatus;
	private Integer gbDgsrDepSettleId;
	private Integer gbDgsrGbPurGoodsId;
	private Integer gbDgsrFromDepSettleId;
    private GbDepartmentEntity gbDepartmentEntity;
    private GbDistributerGoodsEntity gbDistributerGoodsEntity;
    private GbDepartmentUserEntity gbDepartmentUserEntity;
    private GbDistributerPurchaseGoodsEntity gbDisPurchaseGoodsEntity;
    private GbDepartmentGoodsStockReduceAttachmentEntity gbDeGoodsStockReduceAttachmentEntity;

	private GbDepartmentGoodsStockEntity gbDepartmentGoodsStockEntity;
	private GbDepartmentGoodsStockRecordEntity gbDepartmentGoodsStockRecordEntity;


}

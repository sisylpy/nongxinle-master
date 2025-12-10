package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-14 12:03
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerGoodsShelfStockReduceEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDistributerGoodsShelfStockReduceId;
	/**
	 *  
	 */
	private Integer nxDgssrNxDistributerId;
	/**
	 *  
	 */
	private Integer nxDgssrNxDisGoodsId;
	/**
	 *  
	 */
	private Integer nxDgssrNxDisGoodsFatherId;
	/**
	 *  批次采购商品id
	 */
	private Integer nxDgssrNxStockId;
	/**
	 *  盘库日期
	 */
	private String nxDgssrDate;
	/**
	 *  盘库周
	 */
	private String nxDgssrWeek;
	/**
	 *  盘库月
	 */
	private String nxDgssrMonth;
	/**
	 *  执行废弃时间
	 */
	private String nxDgssrFullTime;
	/**
	 *  1,cost;2waste;3loass;4return
	 */
	private Integer nxDgssrType;
	/**
	 *  执行废弃用户
	 */
	private Integer nxDgssrDoUserId;
	/**
	 *  执行废弃用户
	 */
	private String nxDgssrCostWeight;
	/**
	 *  执行废弃数量
	 */
	private String nxDgssrCostSubtotal;
	/**
	 *  执行废弃用户
	 */
	private String nxDgssrWasteWeight;
	/**
	 *  执行废弃数量
	 */
	private String nxDgssrWasteSubtotal;
	/**
	 *  执行废弃用户
	 */
	private String nxDgssrLossWeight;
	/**
	 *  执行废弃数量
	 */
	private String nxDgssrLossSubtotal;
	/**
	 *  执行废弃用户
	 */
	private String nxDgssrReturnWeight;
	/**
	 *  执行废弃数量
	 */
	private String nxDgssrReturnSubtotal;
	/**
	 *  执行废弃用户
	 */
	private String nxDgssrProduceWeight;
	/**
	 *  执行废弃数量
	 */
	private String nxDgssrProduceSubtotal;
	/**
	 *  批次采购商品id
	 */
	private Integer nxDgssrNxPurGoodsId;
	/**
	 *  批次采购商品id
	 */
	private Integer nxDgssrGoodsInventoryType;
	/**
	 *  批次采购商品id
	 */
	private Integer nxDgssrStatus;
	private Integer nxDgssrNxDepOrderId;

	// 关联订单和部门信息
	private String departmentName;  // 订货部门名称
	private String orderQuantity;   // 订货数量
	private String orderStandard;   // 订货规格
	private String doUserName;      // 支出人名字

}

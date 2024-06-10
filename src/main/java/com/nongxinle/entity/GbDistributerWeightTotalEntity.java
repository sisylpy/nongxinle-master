package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 11-29 07:10
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerWeightTotalEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  称重单id
	 */
	private Integer gbDistributerWeightTotalId;
	/**
	 *  称重用户id
	 */
	private Integer gbGwtUserId;
	/**
	 *  称重disid
	 */
	private Integer gbGwtDisId;
	/**
	 *  称重单总重量
	 */
	private String gbGwtWeightTotal;
	/**
	 *  称重单总日期
	 */
	private String gbGwtDate;
	/**
	 *  称重单总金额
	 */
	private String gbGwtSubtotal;
	/**
	 *  称重单状态
	 */
	private Integer gbGwtStatus;
	/**
	 *  称重单总金额
	 */
	private String gbGwtOrderNames;
	/**
	 *  称重disid
	 */
	private Integer gbGwtDepFatherId;
	private Integer gbGwtIsSelf;
	/**
	 *  称重单号
	 */
	private String gbGwtTradeNo;
	private String gbGwtOrderIds;
	/**
	 *  1 出库单 2 采购单
	 */
	private Integer gbGwtType;
	private String gbGwtDepDisGoodsIds;
	private String gbGwtOrderCount;
	private String gbGwtOrderFinishCount;
	private String gbGwtPrintTime;


}

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

public class NxDistributerGoodsShelfStockEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDistributerGoodsShelfStockId;
	/**
	 *  
	 */
	private Integer nxDgssNxDistributerId;
	/**
	 *  
	 */
	private Integer nxDgssNxDisGoodsId;
	/**
	 *  
	 */
	private Integer nxDgssNxDisGoodsFatherId;
	/**
	 *  批次数量
	 */
	private String nxDgssWeight;
	/**
	 *  批次单价
	 */
	private String nxDgssPrice;
	/**
	 *  销售单价
	 */
	private String nxDgssSellingPrice;
	/**
	 *  批次成本
	 */
	private String nxDgssSubtotal;
	/**
	 *  剩余数量
	 */
	private String nxDgssRestWeight;
	/**
	 *  剩余数量显示规格
	 */
	private String nxDgssRestWeightShowStandard;
	/**
	 *  批次剩余成本
	 */
	private String nxDgssRestSubtotal;
	/**
	 *  批次日期
	 */
	private String nxDgssDate;
	/**
	 *  盘库日期
	 */
	private String nxDgssFullTime;
	/**
	 *  出库日期
	 */
	private String nxDgssOutDate;
	/**
	 *  出库时间
	 */
	private String nxDgssOutFullTime;
	/**
	 *  出货小时
	 */
	private Integer nxDgssOutHour;
	/**
	 *  接收用户
	 */
	private Integer nxDgssReceiveUserId;
	/**
	 *  批次状态
	 */
	private Integer nxDgssStatus;
	/**
	 *  批次采购商品id
	 */
	private Integer nxDgssNxPurGoodsId;
	/**
	 *  批次周
	 */
	private String nxDgssWeek;
	/**
	 *  批次月
	 */
	private String nxDgssMonth;
	/**
	 *  批次年
	 */
	private String nxDgssYear;
	/**
	 *  时间戳
	 */
	private String nxDgssTimeStamp;
	/**
	 *  盘库日期
	 */
	private String nxDgssInventoryFullTime;
	/**
	 *  盘库日期
	 */
	private String nxDgssInventoryDate;
	/**
	 *  盘库周
	 */
	private String nxDgssInventoryWeek;
	/**
	 *  盘库月
	 */
	private String nxDgssInventoryMonth;
	/**
	 *  盘库月
	 */
	private String nxDgssInventoryYear;
	/**
	 *  退货数量
	 */
	private String nxDgssReturnWeight;
	/**
	 *  退货成本
	 */
	private String nxDgssReturnSubtotal;
	/**
	 *  制作数量
	 */
	private String nxDgssProduceWeight;
	/**
	 *  制作成本
	 */
	private String nxDgssProduceSubtotal;
	/**
	 *  损耗数量
	 */
	private String nxDgssLossWeight;
	/**
	 *  损耗成本
	 */
	private String nxDgssLossSubtotal;
	/**
	 *  制作数量
	 */
	private String nxDgssWasteWeight;
	/**
	 *  制作数量
	 */
	private String nxDgssWasteSubtotal;
	/**
	 *  利润小计
	 */
	private String nxDgssProfitSubtotal;
	/**
	 *  利润重量
	 */
	private String nxDgssProfitWeight;
	/**
	 *  销售小计
	 */
	private String nxDgssSellingSubtotal;
	/**
	 *  销售利润
	 */
	private String nxDgssAfterProfitSubtotal;
	/**
	 *  成本率
	 */
	private String nxDgssCostRate;
	/**
	 *  剩余数量显示规格
	 */
	private String nxDgssRestWeightShowStandardName;
	/**
	 *  销售小计
	 */
	private String nxDgssProduceSellingSubtotal;
	private Integer nxDgssNxShelfGoodsId;
	private String nxDgssInventoryWeight;

}

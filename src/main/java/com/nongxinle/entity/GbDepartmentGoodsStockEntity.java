package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 08-19 19:02
 */

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepartmentGoodsStockEntity implements Serializable,Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDepartmentGoodsStockId;
	/**
	 *  
	 */
	private Integer gbDgsGbDistributerId;
	/**
	 *  
	 */
	private Integer gbDgsGbDepartmentId;
	/**
	 *  
	 */
	private Integer gbDgsGbDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbDgsGbDisGoodsId;
	private Integer gbDgsGbDisGoodsFatherId;
	/**
	 *  
	 */
	private Integer gbDgsGbDepDisGoodsId;
	/**
	 *  
	 */
	private Integer gbDgsGbDepartmentOrderId;
	/**
	 *  批次数量
	 */
	private String gbDgsWeight;
	/**
	 *  剩余数量
	 */
	private String gbDgsRestWeight;
	/**
	 *  批次日期
	 */
	private String gbDgsDate;
	private String gbDgsTimeStamp;
	private String gbDgsWeek;
	private String gbDgsMonth;
	private String gbDgsYear;
	/**
	 *  接收用户
	 */
	private Integer gbDgsReceiveUserId;
	/**
	 *  批次状态
	 */
	private Integer gbDgsStatus;
	/**
	 *  批次采购商品id
	 */
	private Integer gbDgsGbPurGoodsId;
	/**
	 *  批次单价
	 */
	private String gbDgsPrice;
	/**
	 *  批次成本
	 */
	private String gbDgsSubtotal;
	/**
	 *  批次剩余成本
	 */
	private String gbDgsRestSubtotal;

	private Boolean isSelected = false;

//	private String gbDgsInventoryWeight = "-1";
	private String  gbDgsMyProduceWeight = "-1";
	private String  gbDgsMyWasteWeight = "-1";
	private String  gbDgsMyLossWeight = "-1";
	private String  gbDgsMyReturnWeight = "-1";
	private Integer gbDgsReturnUserId ;


	private Integer gbDgsGbGoodsStockId;
	private GbDepartmentGoodsStockEntity fromDepStockEntity;
	private GbDepartmentGoodsStockEntity toDepStockEntity;

	private GbDistributerGoodsEntity gbDistributerGoodsEntity;
	private Integer gbDgsGbFromDepartmentId;
	private GbDepartmentEntity gbDepartmentEntity;
	private String gbDgsInventoryDate;
	private String gbDgsInventoryWeek;
	private String gbDgsInventoryMonth;
	private String gbDgsInventoryYear;
	private String gbDgsFullTime;
	private String gbDgsWarnFullTime;
	private String gbDgsWasteFullTime;

	private String gbDgsDoWasteFullTime;
	private Integer gbDgsReduceWeightUserId;
	private String gbDgsLossWeight;
	private String gbDgsLossSubtotal;
	private String gbDgsReturnWeight;
	private String gbDgsReturnSubtotal;
	private String gbDgsProduceWeight;
	private String gbDgsProduceSubtotal;
	private String gbDgsProduceSellingSubtotal;

	private Integer gbDgsDepSettleId;
	private Integer gbDgsFromDepSettleId;

	private String gbStockWarnHours;
	private String gbStockWasetHours;

	private String gbDgsOutFullTime;
	private String gbDgsOutDate;
	private  Integer gbDgsOutHour;
	private  String gbDgsInventoryFullTime;
	private  String gbDgsWarnTimeQuantumName;
	private  String gbDgsWasteTimeQuantumName;
	private BigDecimal timeStockRestTotal;
	private String gbDgsInventoryWeight;
	private Integer gbDgsGbPriceGoodsId;
	private Integer gbDgsWeightGoodsId;
	private Integer gbDgsNxDistributerId;
	private String gbDgsGbPriceSubtotal;
	private String gbDgsGbPriceSubtotalScale;
	private String gbDgsRestWeightShowStandard;
	private String gbDgsRestWeightShowStandardName;
	private String gbDgsBetweenPrice;
	private String gbDgsProfitWeight;
	private String gbDgsProfitSubtotal;
	private String gbDgsSellingPrice;
	private String gbDgsSellingSubtotal;
	private String gbDgsWasteWeight;
	private String gbDgsWasteSubtotal;
	private String gbDgsAfterProfitSubtotal;
	private String gbDgsCostRate;

	private String outWeightTotal;

	private GbDepartmentOrdersEntity gbDepartmentOrdersEntity;
	private GbDepartmentUserEntity wasteUserEntity;
	private GbDepartmentUserEntity lossUserEntity;
	private GbDepartmentUserEntity stockUserEntity;
	private GbDistributerPurchaseGoodsEntity purchaseGoodsEntity;
	private GbDepartmentGoodsStockReduceEntity returnReduceEntity;
	private GbDepartmentGoodsStockReduceAttachmentEntity reduceAttachmentEntity;
	private List<GbDepartmentGoodsStockReduceEntity> goodsStockReduceEntityList;

	private  List<GbDepartmentEntity> wasteDepartmentEntities;
	private Double goodsWasteTotal;
	@Override
	public int compareTo(Object o) {
		if (o instanceof GbDepartmentGoodsStockEntity) {
			GbDepartmentGoodsStockEntity e = (GbDepartmentGoodsStockEntity) o;
			return this.getGbDepartmentGoodsStockId().compareTo(e.getGbDepartmentGoodsStockId());
		}
		return 0;

	}
}

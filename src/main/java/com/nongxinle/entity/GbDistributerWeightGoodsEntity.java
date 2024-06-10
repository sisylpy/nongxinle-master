package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 11-30 10:09
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerWeightGoodsEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  称重单id
	 */
	private Integer gbDistributerWeightGoodsId;
	/**
	 *  称重disid
	 */
	private Integer gbDwgWeightId;
	/**
	 *  称重单总重量
	 */
	private Integer gbDwgDepDisGoodsId;
	private Integer gbDwgDisGoodsId;
	private Integer gbDwgOrderAmount;
	private String gbDwgOrderFinishAmount;
	private String gbDwgDate;
	private String gbDwgLossWeight;
	private String gbDwgWasteWeight;
	private String gbDwgReturnWeight;
	private Integer gbDwgStatus;
	private Integer gbDwgSaveUserId;
	private Integer gbDwgDepId;
	private Integer gbDwgDepFatherId;
	/**
	 *  称重单总日期
	 */
	private String gbDwgPrepareWeight;
	private List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities;
	private List<GbDepartmentOrdersEntity> appendOrdersEntities;
	private GbDistributerWeightTotalEntity gbDisWeightTotalEntity;
	private GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity;
	private GbDepInventoryGoodsDailyTotalEntity gbDepInventoryGoodsDailyTotalEntity;


}

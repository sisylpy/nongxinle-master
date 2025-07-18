package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-21 20:42
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商id
	 */
	private Integer gbDistributerId;
	/**
	 *  批发商名称
	 */
	private String gbDistributerName;
	/**
	 *  批发商位置经度
	 */
	private String gbDistributerLan;
	/**
	 *  批发商位置纬度
	 */
	private String gbDistributerLun;
	/**
	 *  批发商商业类型
	 */
	private Integer gbDistributerBusinessType;
	/**
	 *  
	 */
	private String gbDistributerManager;
	/**
	 *  
	 */
	private String gbDistributerPhone;
	/**
	 *  
	 */
	private String gbDistributerAddress;
	private String gbDepartmentLatitude;
	private String gbDepartmentLongitude;
	private String gbDistributerPrintName;
	private String gbDistributerPickName;
	/**
	 *  
	 */
	private String gbDistributerImg;
	private String gbDistributerSettleDate;
	private String gbDistributerSettleWeek;
	private String gbDistributerSettleMonth;
	private String gbDistributerSettleYear;
	private String gbDistributerSettleFullTime;
	private String gbDistributerSettleTimes;
	private Integer gbDistributerTimeQuantum;
	private String gbDistributerBuyQuantity;

	private  Integer hasNotPriceCount;
	private  Integer hasPriceCount;
	private  Integer gbDistributerSysCityId;
	private  int orderDepCount;
	private  String disSubtotal;
	private  int goodsCount;
	private  int outStockCount;
	private  int orderGoodsCount;
	private  int orderCount;
	private  int linshiCout;
	private  int gbDistributerNxDisId;
	private NxDistributerEntity nxDistributerEntity;



	private GbDistributerUserEntity gbDistributerUserEntity;

	private SysUserEntity sysUserEntity;
	private GbDistributerModuleEntity gbDistributerModuleEntity;
	private SysBusinessTypeEntity sysBusinessTypeEntity;

	private List<GbDepartmentEntity> purDepartmentList;
	private List<GbDepartmentEntity> stockDepartmentList;
	private List<GbDepartmentEntity> franchiseeDepartmentList;
	private List<GbDepartmentEntity> mendianDepartmentList;
	private List<GbDepartmentEntity> peisongDepartmentList;
	private List<GbDepartmentEntity> kitchenDepartmentList;
	private List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities;
	private GbDistributerGoodsEntity linshiDisGoods;
	private GbDepartmentEntity appSupplierDepartment;

	private GbDepartmentUserEntity singleDepartmentUser;
	private GbDistributerFatherGoodsEntity linshiFather;

	private NxJrdhUserEntity nxDisBuyerUser;
	private NxJrdhUserEntity sellerUser;
	List<GbDistributerPurchaseBatchEntity> gbDisPurchaseBatchEntities;
	private Integer inviteNxDisId;
	private NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity;


}

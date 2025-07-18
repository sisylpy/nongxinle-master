package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 09-20 15:11
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepartmentBillEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDepartmentBillId;
	/**
	 *  
	 */
	private Integer gbDbDisId;
	/**
	 *  
	 */
	private Integer gbDbDepId;
	private Integer gbDbDepFatherId;
	/**
	 *  
	 */
	private String gbDbTotal;
	/**
	 *  
	 */
	private Integer gbDbStatus;
	/**
	 *  
	 */
	private String gbDbTime;
	/**
	 *  
	 */
	private Integer gbDbIssueUserId;
	/**
	 *  
	 */
	private String gbDbDate;

	private String gbDbWillPayDate;
	/**
	 *  
	 */
	private String gbDbMonth;
	private String gbDbYear;
	/**
	 *  
	 */
	private String gbDbWeek;
	/**
	 *  
	 */
	private String gbDbTradeNo;
	/**
	 *  
	 */
	private Integer gbDbPrintTimes;
	/**
	 *  星期
	 */
	private String gbDbDay;
	private Integer gbDbIssueOrderType;
	private Integer gbDbIssueDepId;
	private Integer gbDbOrderAmount;
	private Integer gbDbGbSupplierPaymentId;
//
	private Integer	gbDbConfirmGoodsUserId;
	private Integer	gbDbConfirmPriceUserId;
	private Integer	gbDbConfirmSettleUserId;
	private String	gbDbConfirmGoodsTime;
	private String	gbDbConfirmPriceTime;
	private String	gbDbConfirmSettleTime;
	private String	gbDbSellingTotal;
	private Integer gbDbDepSettleId;
	private Integer gbDbIssueNxDisId;
	private Integer gbDbSetAutoGoods;
	private Integer gbDbUserCouponId;
	private Integer gbDbReturnOrderId;
	private  String gbUserOpenId;
	private  String gbDbWxOutTradeNo;
	private  String gbDbPayTotal;
	private  String gbDbUserCouponTotal;
	private  String gbDbReturnTotal;
	private  String gbDbGreatCouponTotal;
	private  String gbDbChaTotal;


	private List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities;
	private List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;
	private List<GbDepartmentEntity> orderDepartments;
	private GbDepartmentEntity gbDepartmentEntity;
	private GbDepartmentEntity issueDepartmentEntity;
	private GbDepartmentUserEntity issueUserEntity;
	private NxDistributerCouponEntity nxDistributerCouponEntity;



}

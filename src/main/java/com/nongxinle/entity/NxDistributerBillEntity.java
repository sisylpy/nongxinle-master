package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 02-22 22:34
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerBillEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDistributerBillId;
	/**
	 *  
	 */
	private Integer nxDbdOrderDisId;
	/**
	 *  
	 */
	private Integer nxDbdOfferNxDisId;
	/**
	 *  
	 */
	private String nxDbdTotal;
	/**
	 *  
	 */
	private Integer nxDbdStatus;
	/**
	 *  
	 */
	private String nxDbdTime;
	/**
	 *  
	 */
	private Integer nxDbdIssueUserId;
	/**
	 *  
	 */
	private String nxDbdDate;
	/**
	 *  
	 */
	private String nxDbdMonth;
	/**
	 *  
	 */
	private String nxDbdWeek;
	/**
	 *  
	 */
	private String nxDbdTradeNo;
	/**
	 *  
	 */
	private Integer nxDbdPrintTimes;
	/**
	 *  星期
	 */
	private String nxDbdDay;
	/**
	 *  
	 */
	private String nxDbdProfitTotal;
	/**
	 *  
	 */
	private String nxDbdProfitScale;
	/**
	 *  支付现金
	 */
	private String nxDbdPayCash;
	/**
	 *  
	 */
	private String nxDbdCostTotal;
	/**
	 *  
	 */
	private String nxDbdWxOutTradeNo;
	/**
	 *  
	 */
	private String nxDbdYear;

	private NxDistributerEntity offerNxDistributer;

	private NxDistributerEntity orderNxDistributer;



}

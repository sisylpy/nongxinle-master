package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 12-13 09:47
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxRestrauntBillEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxRestrauntBillId;
	/**
	 *  
	 */
	private Integer nxRbComId;
	/**
	 *  
	 */
	private Integer nxRbRestrauntId;
	/**
	 *  
	 */
	private Integer nxRbDriverUserId;
	/**
	 *  
	 */
	private String nxRbTotal;
	/**
	 *  
	 */
	private Integer nxRbStatus;
	/**
	 *  
	 */
	private String nxRbProduceTime;
	/**
	 *  
	 */
	private Integer nxRbIssueUserId;
	/**
	 *  
	 */
	private String nxRbDate;
	private String nxRbDay;
	/**
	 *  
	 */
	private String nxRbMonth;
	/**
	 *  
	 */
	private String nxRbWeek;
	/**
	 *  
	 */
	private String nxRbTradeNo;
	private String nxRbApplyPayTime;
	private String nxRbPayTime;
	private String nxRbPayFullTime;

	private String nxRbGoodsTotal;
	private String nxRbDeliveryTotal;
	private String nxRbProfitTotal;
	private String nxRbProfitPercent;

	private String nxRbUserOpenId;

	private String nxRbRestrauntName;
	private String nxRbDeliveryDate;

	private List<NxRestrauntOrdersEntity> nxRestrauntOrdersEntities;
	private NxRestrauntEntity nxRestrauntEntity;

}

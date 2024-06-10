package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-11 17:01
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDepartmentBillEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDepartmentBillId;
	/**
	 *  
	 */
	private Integer nxDbDisId;
	/**
	 *  
	 */
	private Integer nxDbDepId;
	private Integer nxDbDepFatherId;
	/**
	 *  
	 */
	private String nxDbTotal;
	/**
	 *  
	 */
	private Integer nxDbStatus;
	/**
	 *  
	 */
	private String nxDbTime;
	private String nxDbDay;
	/**
	 *  
	 */
	private Integer nxDbIssueUserId;
	private String nxDbDate;
	private String nxDbWxOutTradeNo;

	private String nxDbMonth;
	private String nxDbWeek;
	private String nxDbTradeNo;
	private String nxDbProfitTotal;
	private String nxDbProfitScale;
	private String nxDbCostTotal;
	private Integer nxDbPrintTimes;
	private Integer nxDbGbDisId;
	private Integer nxDbGbDepId;
	private Integer nxDbNxCommunityId;
	private Integer nxDbNxRestrauntId;
    private String nxUserOpenId;
	private NxDistributerEntity distributerEntity;
	private String[] nxOrderIds;


	private List<NxDepartmentOrdersEntity>  nxDepartmentOrdersEntities;
	private GbDepartmentEntity gbDepartmentEntity;
	private NxRestrauntEntity nxRestrauntEntity;
	private NxDepartmentEntity nxDepartmentEntity;



}

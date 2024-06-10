package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-22 16:10
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepartmentSettleEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDepartmentSettleId;
	/**
	 *  
	 */
	private Integer gbDsDisId;
	/**
	 *  
	 */
	private Integer gbDsDepId;
	/**
	 *  
	 */
	private String gbDsCostArrTotal;
	/**
	 *  
	 */
	private Integer gbDsStatus;
	/**
	 *  
	 */
	private String gbDsTime;
	/**
	 *  
	 */
	private Integer gbDsSettleUserId;
	/**
	 *  
	 */
	private String gbDsDate;
	/**
	 *  
	 */
	private String gbDsMonth;
	/**
	 *  
	 */
	private String gbDsWeek;
	/**
	 *  星期
	 */
	private String gbDsDay;
	/**
	 *  
	 */
	private String gbDsWasteArrTotal;
	private String gbDsRestTotal;
	private String gbDsLastTotal;
	private String gbDsOutTotal;
	private String gbDsStockTotal;
	private String gbDsStartDate;
	private String gbDsStartTime;
	private String gbDsStopDate;
	private String gbDsStopTime;
	/**
	 *  
	 */
	private String gbDsLossArrTotal;
	private String gbDsYear;
	private String gbDsReturnArrTotal;


	private GbDepartmentEntity gbDepartmentEntity;
	private GbOutStockArrEntity gbOutStockArrEntity;




}

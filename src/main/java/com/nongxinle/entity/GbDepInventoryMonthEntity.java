package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 09-06 15:27
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepInventoryMonthEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbInventoryMonthId;
	/**
	 *  
	 */
	private Integer gbImDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbImDepartmentId;
	/**
	 *  
	 */
	private String gbImMonth;
	/**
	 *  
	 */
	private String gbImSubtotal;
	/**
	 *  
	 */
	private Integer gbImDistributerId;
	private String gbImWasteTotal;
	private String gbImYear;
	private String gbImLossTotal;
	private String gbImReturnTotal;
	private String gbImProduceTotal;
	private Integer gbImStatus;
	private Integer gbImDepSettleId;

	private GbDepartmentEntity departmentEntity;


}

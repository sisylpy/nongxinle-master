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

public class GbDepInventoryDailyEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbInventoryDailyId;
	/**
	 *  
	 */
	private Integer gbIdDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbIdDepartmentId;
	/**
	 *  
	 */
	private String gbIdDate;
	/**
	 *  
	 */
	private String gbIdTotal;
	/**
	 *  
	 */
	private Integer gbIdDistributerId;

	private String gbIdWasteTotal;
	private String gbIdWeek;
	private String gbIdYear;
	private String gbIdLossTotal;
	private Integer gbIdStatus;
	private Integer gbIdDepSettleId;
	private String gbIdReturnTotal;
	private String gbIdProduceTotal;


	private GbDepartmentEntity departmentEntity;

}

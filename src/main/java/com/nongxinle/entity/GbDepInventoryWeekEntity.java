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

public class GbDepInventoryWeekEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbInventoryWeekId;
	/**
	 *  
	 */
	private Integer gbDiwDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbDiwDepartmentId;
	/**
	 *  
	 */
	private String gbDiwWeek;
	/**
	 *  
	 */
	private String gbDiwSubtotal;
	/**
	 *  
	 */
	private Integer gbDiwDistributerId;
	private String gbDiwWasteTotal;
	private String gbDiwYear;
	private String gbDiwLossTotal;
	private String gbDiwReturnTotal;
	private String gbDiwProduceTotal;
	private Integer gbDiwStatus;
	private Integer gbDiwDepSettleId;

	private GbDepartmentEntity departmentEntity;

}

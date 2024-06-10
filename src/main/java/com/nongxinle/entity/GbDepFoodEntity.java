package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 03-27 21:55
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepFoodEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  供货商id
	 */
	private Integer gbDepFoodId;
	/**
	 *  供货商名称
	 */
	private Integer gbDfDepId;
	/**
	 *  gbDisid
	 */
	private Integer gbDfFoodId;
	/**
	 *  供货商名称
	 */
	private Integer gbDfDepFatherId;
	/**
	 *  gbDisid
	 */
	private Integer gbDfStatus;
	/**
	 *  
	 */
	private String gbDfFoodPrice;
	private String gbDfFoodSalesAmount;
	private Integer gbDfFoodSalesUserId;

	private GbDistributerFoodEntity gbDistributerFoodEntity;
	private GbDepartmentEntity gbDepartmentEntity;

}

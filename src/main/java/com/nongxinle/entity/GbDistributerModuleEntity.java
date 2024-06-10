package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-04 11:07
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerModuleEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDistributerModuleId;
	/**
	 *  
	 */
	private Integer gbDmFixedSupplierNumber;
	/**
	 *  
	 */
	private Integer gbDmPurchaseNumber;
	/**
	 *  
	 */
	private Integer gbDmStockNumber;
	/**
	 *  
	 */
	private Integer gbDmAppSupplierNumber;
	/**
	 *  
	 */
	private Integer gbDmCentralKitchenNumber;
	private Integer gbDmDirectSalesNumber;
	private Integer gbDmFranchiseeNumber;
	/**
	 *  
	 */
	private Integer gbDmDistributerId;

}

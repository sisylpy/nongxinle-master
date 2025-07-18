package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 02-12 21:10
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerPayListEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDistributerPayListId;
	/**
	 *  
	 */
	private Integer gbNdplGbDisId;
	/**
	 *  
	 */
	private String gbNdplPaySubtotal;
	/**
	 *  
	 */
	private String gbNdplPayTime;
	/**
	 *  
	 */
	private Integer gbNdplType;
	/**
	 *  
	 */
	private Integer gbNdplStatus;
	/**
	 *  
	 */
	private String gbNdplPayDate;
	/**
	 *  
	 */
	private Integer gbNdplGbPbId;
	/**
	 *  
	 */
	private String gbNdplPayMonth;
	/**
	 *  
	 */
	private String gbNdplPayYear;
	/**
	 *  
	 */
	private Integer gbNdplRestPoints;
	/**
	 *  
	 */
	private Integer gbNdplNxSupplierId;

	private NxJrdhSupplierEntity nxJrdhSupplierEntity;

}

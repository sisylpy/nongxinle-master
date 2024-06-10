package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-06 00:18
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityGoodsSetItemEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCommunityGoodsSetItemId;
	/**
	 *  
	 */
	private String nxCgsiItemName;
	/**
	 *  
	 */
	private Integer nxCgsiItemCgGoodsId;
	private Integer nxCgsiItemType;
	/**
	 *  
	 */
	private Integer nxCgsiCgPropertyId;
	private Integer nxCgsiItemSort;
	/**
	 *  
	 */
	private String nxCgsiItemPrice;
	/**
	 *  
	 */
	private String nxCgsiItemFilePath;
	private String nxCgsiItemQuantity;

}

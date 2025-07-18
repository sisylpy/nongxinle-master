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
	private Integer nxCgsiItemStatus;
	private Integer nxCgsiItemLimitCount;
	private Integer nxCgsiCgGoodsId;
	/**
	 *  
	 */
	private String nxCgsiItemPrice;
	private String nxCgsiItemHuaxianPrice;
	/**
	 *  
	 */
	private String nxCgsiItemFilePath;
	private String nxCgsiItemQuantity;
	
	private NxCommunityGoodsEntity nxCommunityGoodsEntity;

}

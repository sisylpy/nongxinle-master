package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-14 12:45
 */

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxOrderTemplateItemEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxOtItemId;
	/**
	 *  
	 */
	private Integer nxOtDisGoodsId;
	/**
	 *  
	 */
	private Float nxOtAmount;
	/**
	 *  
	 */
	private Integer nxOtCustomerUserId;
	/**
	 *
	 */

	private String nxOtDisGoodsColor;

	private Boolean isSelected;
	private Boolean isEdit;

	private Float nxOtOrderAmount;

	private Integer nxOtOrderTemplateId;

	private NxCommunityGoodsEntity nxCommunityGoodsEntity;

}

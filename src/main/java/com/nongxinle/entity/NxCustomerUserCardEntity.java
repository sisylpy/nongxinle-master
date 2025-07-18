package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-24 01:00
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCustomerUserCardEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCustomerUserCardId;
	/**
	 *  
	 */
	private Integer nxCucaCardId;
	/**
	 *  
	 */
	private Integer nxCucaCustomerUserId;
	/**
	 *  
	 */
	private Integer nxCucaCommunityId;
	/**
	 *  
	 */
	private Integer nxCucaStatus;
	/**
	 *  
	 */
	private Integer nxCucaType;
	private Integer nxCucaComOrderId;
	private Integer nxCucaIsSelected;
	private Integer nxCucaComSplicingOrderId;
	/**
	 *  
	 */
	private String nxCucaStartDate;
	/**
	 *  
	 */
	private String nxCucaStopDate;

	private NxCommunityCardEntity nxCommunityCardEntity;

}

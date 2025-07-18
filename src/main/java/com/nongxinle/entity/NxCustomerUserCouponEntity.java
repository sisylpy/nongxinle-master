package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-15 08:30
 */

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCustomerUserCouponEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCustomerUserCouponId;
	/**
	 *  
	 */
	private Integer nxCucCouponId;
	/**
	 *  
	 */
	private Integer nxCucCustomerUserId;
	/**
	 *  
	 */
	private Integer nxCucShareUserId;
	/**
	 *  
	 */
	private Integer nxCucCommunityId;
	/**
	 *  
	 */
	private Integer nxCucStatus;
	private Integer nxCucType;
	private Integer nxCucSubOrderId;
	private Integer nxCucFromShareUserId;
	private String nxCucShareTime;


	private NxCommunityCouponEntity nxCommunityCouponEntity;
	private NxCustomerUserEntity shareUser;

}

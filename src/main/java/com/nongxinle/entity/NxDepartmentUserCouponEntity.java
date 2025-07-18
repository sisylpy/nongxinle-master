package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-26 16:29
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDepartmentUserCouponEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDepartmentUserCouponId;
	/**
	 *  
	 */
	private Integer nxDucCouponId;
	/**
	 *  
	 */
	private Integer nxDucNxDepUserId;
	/**
	 *  
	 */
	private Integer nxDucShareUserId;
	/**
	 *  
	 */
	private Integer nxDucNxDistributerId;
	/**
	 *  
	 */
	private Integer nxDucStatus;
	/**
	 *  
	 */
	private Integer nxGducType;
	/**
	 *  
	 */
	private Integer nxDucUseOrderId;
	/**
	 *  
	 */
	private Integer nxDucFromShareUserId;
	/**
	 *  
	 */
	private String nxDucShareTime;
	/**
	 *  
	 */
	private Integer nxDucNxDepartmentId;
	/**
	 *  
	 */
	private String nxDucStartDate;
	/**
	 *  
	 */
	private String nxDucStopDate;
	/**
	 *  
	 */
	private Date nxDucStartTimeZone;
	/**
	 *  
	 */
	private Date nxDucStopTimeZone;

}

package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 03-28 18:56
 */

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxGbDistibuterUserCouponEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxGbDistributerUserCouponId;
	/**
	 *  
	 */
	private Integer nxGducCouponId;
	/**
	 *  
	 */
	private Integer nxGducGbDistibtuerUserId;
	/**
	 *  
	 */
	private Integer nxGducShareUserId;
	/**
	 *  
	 */
	private Integer nxGducNxDistributerId;
	/**
	 *  
	 */
	private Integer nxGducStatus;
	/**
	 *  
	 */
	private Integer nxGducType;
	/**
	 *  
	 */
	private Integer nxGducUseOrderId;
	/**
	 *  
	 */
	private Integer nxGducFromShareUserId;
	private Integer nxGducGbDistributerId;
	/**
	 *  
	 */
	private String nxGducShareTime;
	private String nxGducStartDate;
	private String nxGducStopDate;

	private LocalDateTime nxGducStartTimeZone;
	private LocalDateTime nxGducStopTimeZone;
	private NxDistributerCouponEntity nxDistributerCouponEntity;

}

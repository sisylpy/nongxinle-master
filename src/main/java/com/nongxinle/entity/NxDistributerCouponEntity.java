package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 03-28 18:43
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerCouponEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDistributerCouponId;
	/**
	 *  
	 */
	private String nxDistributerCouponName;
	/**
	 *  
	 */
	private String nxDcPrice;
	/**
	 *  
	 */
	private String nxDcPriceGreatGrandId;
	/**
	 *  
	 */
	private String nxDcStopTime;
	/**
	 *  
	 */
	private String nxDcStartTime;
	/**
	 *  
	 */
	private String nxDcWords;
	/**
	 *  
	 */
	private String nxDcFilePath;
	/**
	 *  
	 */
	private Integer nxDcDistributerId;
	/**
	 *  
	 */
	private Integer nxDcType;
	/**
	 *  
	 */
	private Integer nxDcStatus;
	/**
	 *  
	 */
	private Date nxDcStartTimeZone;
	/**
	 *  
	 */
	private Date nxDcStopTimeZone;
	/**
	 *  
	 */
	private String nxDcStartDate;
	/**
	 *  
	 */
	private String nxDcStopDate;
	/**
	 *  
	 */
	private Integer nxDcDownCount;
	/**
	 *  
	 */
	private Integer nxDcUseCount;
	private Integer nxDcCityId;
	private Integer nxDcMarketId;
	private Integer nxDcStartWhichDay;
	private Integer nxDcStopWhichDay;
	/**
	 *  
	 */
	private String nxDcSubtotalPrice;
	/**
	 *  
	 */
	private String nxDcPricePercent;

}

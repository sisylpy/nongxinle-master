package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-15 08:33
 */

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityCouponEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer  nxCommunityCouponId;
	/**
	 *  
	 */
	private Integer nxCpCgGoodsId;
	/**
	 *  
	 */
	private String nxCpOriginalPrice;
	private String nxCommunityCouponName;
	/**
	 *  
	 */
	private String nxCpPrice;
	/**
	 *  
	 */
	private String nxCpStandard;
	/**
	 *  
	 */
	private Integer nxCpQuantity;
	/**
	 *  
	 */
	private String nxCpStopTime;
	/**
	 *  
	 */
	private String nxCpStartTime;
	/**
	 *  
	 */
	private String nxCpWords;
	/**
	 *  
	 */
	private String nxCpRecommandGoods;
	private String nxCpStartDate;
	private String nxCpStopDate;
	/**
	 *  
	 */
	private String nxCpFilePath;

	private LocalDateTime nxCpStartTimeZone;

	private LocalDateTime nxCpStopTimeZone;
	/**
	 *  
	 */
	private Integer nxCpCommunityId;
	private Integer nxCpDownCount;
	private Integer nxCpUseCount;
	/**
	 *  
	 */
	private Integer nxPromoteCgFatherId;
	/**
	 *  
	 */
	private Integer nxCpType;
	/**
	 *  
	 */
	private Integer nxCpStatus;

	private NxCommunityGoodsEntity nxCommunityGoodsEntity;

}

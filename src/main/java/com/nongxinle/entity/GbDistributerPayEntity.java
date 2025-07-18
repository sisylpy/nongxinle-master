package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 01-08 12:27
 */

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerPayEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDistributerPayId;
	/**
	 *  
	 */
	private Integer gbGdpGbDisId;
	/**
	 *  
	 */
	private String gbGdpPaySubtotal;
	/**
	 *  
	 */
	private Date gbGdpFromTime;
	/**
	 *  
	 */
	private Date gbGdpStopTime;
	/**
	 *  
	 */
	private String gbGdpPayTime;
	/**
	 *  
	 */
	private Integer gbGdpType;
	/**
	 *  
	 */
	private Integer gbGdpStatus;
	/**
	 *  
	 */
	private String gbGdpTradeNo;
	/**
	 *  
	 */
	private Integer gbGdpGbNewDisId;
	/**
	 *  
	 */
	private String gbGdpBuyQuantity;

	private String payUserOpenId;
	private String gbGdpSellDetail;
	private String gbGdpImgUrl;

	private GbDistributerEntity newDistributer;


}

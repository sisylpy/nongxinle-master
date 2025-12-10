package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-18 12:14
 */

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerPayEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDistributerPayId;
	/**
	 * 配送商ID
	 */
	private Integer nxNdpNxDisId;
	
	/**
	 * 市场ID（外键→sys_city_market）
	 */
	private Integer nxNdpMarketId;
	
	/**
	 * 支付金额
	 */
	private String nxNdpPaySubtotal;
	/**
	 *  
	 */
	private LocalDateTime nxNdpFromTime;
	/**
	 *  
	 */
	private LocalDateTime nxNdpStopTime;
	/**
	 *  
	 */
	private String nxNdpPayTime;
	private String nxNdpTradeNo;
	/**
	 *  
	 */
	private Integer nxNdpType;
	/**
	 *  
	 */
	private Integer nxNdpStatus;
	private Integer nxNdpNxNewDisId;
	private String nxNdpBuyQuantity;
	private String perPrice;
	private String payUserOpenId;
	private Integer nxNdpOrderQuantity;
	private Boolean isSelected = false;
	private String nxNdpSellDetail;
	private String nxNdpImgUrl;


	private NxDistributerEntity newDistributer;

}

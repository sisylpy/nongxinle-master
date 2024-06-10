package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 03-28 17:06
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxResComGoodsMonthEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxResComGoodsMonthId;
	/**
	 *  
	 */
	private Integer nxRcgmRestrauntFatherId;
	/**
	 *  
	 */
	private Integer nxRcgmRestrauntId;
	/**
	 *  
	 */
	private Integer nxRcgmComGoodsId;
	/**
	 *  
	 */
	private String nxRcgmMonth;
	/**
	 *  
	 */
	private String nxRcgmWeight;
	/**
	 *  
	 */
	private String nxRcgmSubtotal;

}

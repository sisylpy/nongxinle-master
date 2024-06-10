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

public class NxResComGoodsDailyEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxResComGoodsDailyId;
	/**
	 *  
	 */
	private Integer nxRcgdRestrauntFatherId;
	/**
	 *  
	 */
	private Integer nxRcgdRestrauntId;
	/**
	 *  
	 */
	private Integer nxRcgdComGoodsId;
	/**
	 *  
	 */
	private String nxRcgdDate;
	/**
	 *  
	 */
	private String nxRcgdWeight;
	/**
	 *  
	 */
	private String nxRcgdSubtotal;

}

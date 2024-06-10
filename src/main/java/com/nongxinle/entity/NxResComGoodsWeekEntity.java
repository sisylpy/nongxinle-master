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

public class NxResComGoodsWeekEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxResComGoodsWeekId;
	/**
	 *  
	 */
	private Integer nxRcgwRestrauntFatherId;
	/**
	 *  
	 */
	private Integer nxRcgwRestrauntId;
	/**
	 *  
	 */
	private Integer nxRcgwComGoodsId;
	/**
	 *  
	 */
	private String nxRcgwWeek;
	/**
	 *  
	 */
	private String nxRcgwWeight;
	/**
	 *  
	 */
	private String nxRcgwSubtotal;

}

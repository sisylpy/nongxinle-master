package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 03-31 10:13
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepFatherGoodsSettleEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDepFatherGoodsSettleStaticsId;
	/**
	 *  
	 */
	private String gbDfgssFatherGoodsName;
	private Integer gbDfgssFatherGoodsId;
	/**
	 *  
	 */
	private Integer gbDfgssFathersFatherId;
	/**
	 *  
	 */
	private Integer gbDfgssFatherGoodsLevel;
	/**
	 *  
	 */
	private Integer gbDfgssDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbDfgssDistributerId;
	/**
	 *  
	 */
	private String gbDfgssOutStockSubtotal;

	/**
	 *  
	 */
	private Integer gbDfgssSettleId;
	private String gbDfgssSettleMonth;
	private String gbDfgssSettleYear;

	private Integer gbDfgssOutStockType;

}

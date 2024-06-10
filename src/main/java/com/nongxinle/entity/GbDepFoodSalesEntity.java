package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 03-27 21:55
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepFoodSalesEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  供货商id
	 */
	private Integer gbDepFoodSalesId;
	/**
	 *  供货商名称
	 */
	private Integer gbDfsDepId;
	/**
	 *  gbDisid
	 */
	private Integer gbDfsFoodId;
	/**
	 *  供货商名称
	 */
	private Integer gbDfsDepFatherId;
	/**
	 *  gbDisid
	 */
	private String gbDfsAmount;
	private String gbDfsSubtotal;
	/**
	 *  
	 */
	private Integer gbDfsSettleId;
	/**
	 *  gbDisid
	 */
	private String gbDfsMonth;
	private String gbDfsYear;
	/**
	 *  gbDisid
	 */
	private String gbDfsFullDate;
	/**
	 *  gbDisid
	 */
	private Integer gbDfsUserId;

	private GbDepFoodEntity gbDepFoodEntity;
	private GbDepFoodGoodsSalesEntity rawGoods;

}

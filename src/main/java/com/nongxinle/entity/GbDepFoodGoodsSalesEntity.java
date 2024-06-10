package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 03-27 21:55
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepFoodGoodsSalesEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  供货商id
	 */
	private Integer gbDepFoodGoodsSalesId;
	/**
	 *  供货商名称
	 */
	private Integer gbDfgsDepId;
	/**
	 *  供货商名称
	 */
	private Integer gbDfgsDepFatherId;
	/**
	 *  gbDisid
	 */
	private Integer gbDfgsFoodSalesId;
	/**
	 *  gbDisid
	 */
	private Integer gbDfgsFoodGoodsId;
	private Integer gbDfgsDisGoodsId;
	/**
	 *  gbDisid
	 */
	private String gbDfgsGoodsAmount;
	/**
	 *  
	 */
	private Integer gbDfgsSettleId;
	/**
	 *  gbDisid
	 */
	private String gbDfgsMonth;
	/**
	 *  gbDisid
	 */
	private String gbDfgsFullDate;

   private GbDistributerFoodGoodsEntity gbDistributerFoodGoodsEntity;
}

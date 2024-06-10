package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 03-27 10:50
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerFoodGoodsEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  供货商id
	 */
	private Integer gbDistributerFoodGoodsId;
	/**
	 *  父级id
	 */
	private Integer gbDfoodgFoodId;
	/**
	 *  gbDisid
	 */
	private Integer gbDfoodgGbDistributerId;
	/**
	 *  父级id
	 */
	private Integer gbDfoodgDisGoodsId;
	/**
	 *  父级id
	 */
	private String gbDfoodgGoodsAmount;
	/**
	 *  父级id
	 */
	private Integer gbDfoodgStatus;

	private String gbDfoodgGoodsStandardname;
	private String gbDfoodgGoodsName;
	private GbDistributerGoodsEntity distributerGoodsEntity;


}

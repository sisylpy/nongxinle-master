package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 03-26 18:31
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerFoodEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  供货商id
	 */
	private Integer gbDistributerFoodId;
	/**
	 *  供货商名称
	 */
	private String gbDistributerFoodName;
	private String gbDistributerFoodPrice;
	private String gbDistributerFoodImg;
	private String gbDistributerFoodMethod;
	/**
	 *  父级id
	 */
	private Integer gbDistributerFoodFatherId;
	/**
	 *  gbDisid
	 */
	private Integer gbDfGbDistributerId;

	private List<GbDistributerFoodGoodsEntity> gbdisFoodGoodsEntities;
	private List<GbDistributerFoodEntity> foodEntityList;
	private GbDepFoodEntity gbDepFoodEntity;
	private GbDistributerFoodGoodsEntity rawFoodGoods;
}

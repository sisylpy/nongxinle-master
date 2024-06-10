package com.nongxinle.entity;

/**
 * 用户与角色对应关系
 * @author lpy
 * @date 05-22 15:25
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxRetailerGoodsShelfGoodsEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  货架商品id
	 */
	private Integer nxRetailerGoodsShelfGoodsId;
	/**
	 *  批发商商品id
	 */
	private String nxRgsgGoodsName;
	/**
	 *  货架id
	 */
	private Integer nxRgsgShelfId;
	/**
	 *  货架商品排序
	 */
	private Integer nxRgsgSort;

	private NxRetailerPurchaseGoodsEntity nxRetailerPurchaseGoodsEntity;

}

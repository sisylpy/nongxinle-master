package com.nongxinle.entity;

/**
 * 用户与角色对应关系
 * @author lpy
 * @date 05-22 15:25
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxRetailerGoodsShelfEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  货架id
	 */
	private Integer nxRetailerGoodsShelfId;
	/**
	 *  货架名称
	 */
	private String nxRetailerGoodsShelfName;
	/**
	 *  货架排序
	 */
	private Integer nxRetailerGoodsShelfSort;
	/**
	 *  批发商id
	 */
	private Integer nxRetailerGoodsShelfRetailerId;

	private Boolean isSelected = false;
	private Integer shelfPurGoodsCount ;


	private List<NxRetailerGoodsShelfGoodsEntity> nxRetGoodsShelfGoodsEntityList;

}

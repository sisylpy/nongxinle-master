package com.nongxinle.entity;

/**
 * 用户与角色对应关系
 * @author lpy
 * @date 05-09 18:47
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerGoodsShelfEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  货架id
	 */
	private Integer nxDistributerGoodsShelfId;
	/**
	 *  货架名称
	 */
	private String nxDistributerGoodsShelfName;
	/**
	 *  货架排序
	 */
	private Integer nxDistributerGoodsShelfSort;
	/**
	 *  批发商id
	 */
	private Integer nxDistributerGoodsShelfDisId;
	/**
	 *  负责员工ID
	 */
	private Integer nxDistributerGoodsShelfUserId;
	private Boolean isSelected = false;

	private List<NxDistributerGoodsShelfGoodsEntity> nxDisGoodsShelfGoodsEntities;
	/**
	 * 负责员工信息
	 */
	private NxDistributerUserEntity responsibleUser;
	/**
	 * 货架商品数量
	 */
	private Integer goodsCount;
	/**
	 * 是否有商品（用于前端判断）
	 */
	private Boolean hasGoods;



}

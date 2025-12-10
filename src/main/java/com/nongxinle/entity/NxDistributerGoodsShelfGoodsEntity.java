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

public class
NxDistributerGoodsShelfGoodsEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *  货架商品id
	 */
	private Integer nxDistributerGoodsShelfGoodsId;
	/**
	 *  批发商商品id
	 */
	private Integer nxDgsgDisGoodsId;
	/**
	 *  货架id
	 */
	private Integer nxDgsgShelfId;
	private Integer nxDgsgSort;
	private Integer nxDgsgShelfSort;
	private Integer nxDgsgShelfLayer;
	/**
	 * 是否重复出现在多个货架
	 * 0 = 未重复（只出现在一个货架）
	 * 1 = 重复（出现在多个不同货架）
	 */
	private Integer nxDgsgIsDuplicate;

	private NxDistributerGoodsEntity nxDistributerGoodsEntity;
	private NxDistributerPurchaseGoodsEntity shelfPurGoods;
	private NxDistributerGoodsShelfEntity nxDistributerGoodsShelfEntity;

	private List<NxDistributerGoodsShelfStockEntity> nxDisGoodsShelfStockEntities;
	private List<NxDistributerGoodsShelfGoodsEntity> sameShelfGoods;

	/**
	 * 总库存重量（SQL计算）
	 */
	private String totalRestWeight;

	/**
	 * 货架名称（用于显示）
	 */
	private String shelfName;

}

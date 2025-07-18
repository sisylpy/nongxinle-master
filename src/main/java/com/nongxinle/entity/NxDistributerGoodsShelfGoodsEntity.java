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

	private NxDistributerGoodsEntity nxDistributerGoodsEntity;

	private List<NxDistributerGoodsShelfStockEntity> nxDisGoodsShelfStockEntities;

}

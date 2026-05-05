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
	/**
	 * 在本货架内的排序
	 */
	private Integer nxDgsgSort;
	/**
	 * 所在的货架的排序
	 */
	private Integer nxDgsgShelfSort;

	/**
	 * 所在的货架的层级
	 */
	private Integer nxDgsgShelfLayer;
	/**
	 * 同一层内序号：与 nxDgsgShelfLayer 配合，从 1 递增（该层第几行）
	 */
	private Integer nxDgsgShelfLayerSeq;
	/**
	 * 是否层尾：1=该层最后一行，0=非层尾；与 nxDgsgShelfLayer 配合，每层内仅一行应为 1
	 */
	private Integer nxDgsgShelfLayerLast;
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
	 * 该货架商品下有效库存批次的剩余金额合计（按库存金额排序等场景由 SQL 赋值）
	 */
	private String totalRestSubtotal;

	/**
	 * 按到期排序且按批次拆行时，与货架商品 ID 一起作为 MyBatis 行唯一键（值为该行的库存批次 ID）
	 */
	private Integer shelfGoodsListRowStockId;

	/**
	 * 临期排序用：距到期日的剩余天数（DATEDIFF），与 SQL 排序一致；非临期列表或未算出时为 null
	 */
	private Integer shelfGoodsRestDaysUntil;

	/**
	 * 货架名称（用于显示）
	 */
	private String shelfName;

	/**
	 * 非表字段：MyBatis 嵌套查询透传 nx_distributer_purchase_goods_id，用于按采购行精确加载库存批次
	 */
	private Integer nxDgsgForNestedPurGoodsId;

}

package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 12-02 20:50
 */

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityPurchaseGoodsEntity implements Serializable, Comparable  {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商采购商品id
	 */
	private Integer nxCommunityPurchaseGoodsId;
	/**
	 *  采购商品id
	 */
	private Integer nxCpgComGoodsId;
	/**
	 *  采购父级商品id
	 */
	private Integer nxCpgComGoodsFatherId;
	/**
	 *  采购数量
	 */
	private String nxCpgQuantity;
	/**
	 *  采购规格
	 */
	private String nxCpgStandard;
	/**
	 *  采购状态
	 */
	private Integer nxCpgStatus;
	/**
	 *  采购批发商id
	 */
	private Integer nxCpgCommunityId;

	/**
	 *  采购方式：“1 订单采购”“2 添加采购”
	 */
	private Integer nxCpgPurchaseType;
	/**
	 *  采购时间
	 */
	private String nxCpgTime;
	/**
	 *  采购批次号
	 */
	private Integer nxCpgBatchId;
	/**
	 *  采购方式为“采购”的采购员id
	 */
	private Integer nxCpgBuyUserId;
	/**
	 *  采购单价
	 */
	private String nxCpgBuyPrice;
	private String nxCpgBuySubtotal;
	/**
	 *  采购数量
	 */
	private String nxCpgBuyQuantity;
	/**
	 *  订单采购的订单数量
	 */
	private Integer nxCpgOrdersAmount;
	/**
	 *  添加采购用户id
	 */
	private Integer nxCpgTypeAddUserId;
	/**
	 *  
	 */
	private String nxCpgApplyDate;
	/**
	 *  采购日期
	 */
	private String nxCpgPurchaseDate;
	/**
	 *  
	 */
	private Integer nxCpgInputType;

	private NxCommunityGoodsEntity nxCommunityGoodsEntity;

	private List<NxRestrauntOrdersEntity> nxRestrauntOrdersEntityList;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxCommunityPurchaseGoodsEntity that = (NxCommunityPurchaseGoodsEntity) o;
		return Objects.equals(nxCommunityPurchaseGoodsId, that.nxCommunityPurchaseGoodsId) &&
				Objects.equals(nxCpgComGoodsId, that.nxCpgComGoodsId) &&
				Objects.equals(nxCpgComGoodsFatherId, that.nxCpgComGoodsFatherId) &&
				Objects.equals(nxCpgQuantity, that.nxCpgQuantity) &&
				Objects.equals(nxCpgStandard, that.nxCpgStandard) &&
				Objects.equals(nxCpgStatus, that.nxCpgStatus) &&
				Objects.equals(nxCpgCommunityId, that.nxCpgCommunityId) &&
				Objects.equals(nxCpgPurchaseType, that.nxCpgPurchaseType) &&
				Objects.equals(nxCpgTime, that.nxCpgTime) &&
				Objects.equals(nxCpgBatchId, that.nxCpgBatchId) &&
				Objects.equals(nxCpgBuyUserId, that.nxCpgBuyUserId) &&
				Objects.equals(nxCpgBuyPrice, that.nxCpgBuyPrice) &&
				Objects.equals(nxCpgBuyQuantity, that.nxCpgBuyQuantity) &&
				Objects.equals(nxCpgOrdersAmount, that.nxCpgOrdersAmount) &&
				Objects.equals(nxCpgTypeAddUserId, that.nxCpgTypeAddUserId) &&
				Objects.equals(nxCpgApplyDate, that.nxCpgApplyDate) &&
				Objects.equals(nxCpgPurchaseDate, that.nxCpgPurchaseDate) &&
				Objects.equals(nxCpgInputType, that.nxCpgInputType) &&
				Objects.equals(nxCommunityGoodsEntity, that.nxCommunityGoodsEntity) &&
				Objects.equals(nxRestrauntOrdersEntityList, that.nxRestrauntOrdersEntityList);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxCommunityPurchaseGoodsId, nxCpgComGoodsId, nxCpgComGoodsFatherId, nxCpgQuantity, nxCpgStandard, nxCpgStatus, nxCpgCommunityId, nxCpgPurchaseType, nxCpgTime, nxCpgBatchId, nxCpgBuyUserId, nxCpgBuyPrice, nxCpgBuyQuantity, nxCpgOrdersAmount, nxCpgTypeAddUserId, nxCpgApplyDate, nxCpgPurchaseDate, nxCpgInputType, nxCommunityGoodsEntity, nxRestrauntOrdersEntityList);
	}

	@Override
	public int compareTo(Object o) {

		if (o instanceof NxCommunityPurchaseGoodsEntity) {
			NxCommunityPurchaseGoodsEntity e = (NxCommunityPurchaseGoodsEntity) o;
			return this.nxCommunityPurchaseGoodsId.compareTo(e.nxCommunityPurchaseGoodsId);
		}
		return 0;
	}
}

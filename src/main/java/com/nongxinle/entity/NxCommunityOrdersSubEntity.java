package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 2020-03-22 18:07:28
 */

import java.io.Serializable;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityOrdersSubEntity implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  子订单id
	 */
	private Integer nxCommunityOrdersSubId;
	/**
	 *  子订单商品id
	 */
	private Integer nxCosNxGoodsId;
	/**
	 *  子订单father商品id
	 */
	private Integer nxCosCommunityGoodsFatherId;
	/**
	 *  子订单申请数量
	 */
	private String nxCosQuantity;
	/**
	 * 子订单申请规格
	 */
	private String nxCosStandard;
	/**
	 *  子订单申请商品单价
	 */
	private String nxCosPrice;

	/**
	 *  子订单申请备注
	 */
	private String nxCosRemark;
	private String nxCosHuaxianPrice;
	private String nxCosHuaxianDifferentPrice;
	/**
	 *  子订单申请商品称重
	 */
	private String nxCosWeight;
	/**
	 *  子订单申请商品小计
	 */
	private String nxCosSubtotal;
	/**
	 *  子订单申请商品状态
	 */
	private Integer nxCosStatus;
	private Integer nxCosType;
	/**
	 *  子订单商品称重用户id
	 */
	private Integer nxCosWeighUserId;
	/**
	 *  子订单商品输入单价用户id
	 */
	private Integer nxCosAccountUserId;
	/**
	 * 订单id
	 */
	private Integer nxCosOrdersId;

	private Integer nxCosGoodsType;
	private Integer nxCosGoodsIndex;

	/**
	 * shangpin
	 */


	private Integer nxCosDistributerId;

	private Integer nxCosCommunityGoodsId;

	private Integer nxCosBuyStatus;

	private Integer nxCosPurchaseUserId;

	private Integer nxCosOrderUserId;

	private Boolean hasItem;


  	private String nxCosSubWeight;

  	private Integer nxCosSubDistributerId;

  	private Integer nxCosCommunityId;

  	private Integer nxCosGoodsSellType;

  	private String nxCosGoodsSellStandardScale;


	private NxCommunityGoodsEntity nxCommunityGoodsEntity;
	private NxCommunityFatherGoodsEntity nxCommunityFatherGoodsEntity;


	private String nxCosStandardPrice;

	private NxCommunityEntity nxCommunityEntity;




	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxCommunityOrdersSubEntity subEntity = (NxCommunityOrdersSubEntity) o;
		return Objects.equals(nxCommunityOrdersSubId, subEntity.nxCommunityOrdersSubId) &&
				Objects.equals(nxCosNxGoodsId, subEntity.nxCosNxGoodsId) &&
				Objects.equals(nxCosCommunityGoodsFatherId, subEntity.nxCosCommunityGoodsFatherId) &&
				Objects.equals(nxCosQuantity, subEntity.nxCosQuantity) &&
				Objects.equals(nxCosStandard, subEntity.nxCosStandard) &&
				Objects.equals(nxCosPrice, subEntity.nxCosPrice) &&
				Objects.equals(nxCosRemark, subEntity.nxCosRemark) &&
				Objects.equals(nxCosWeight, subEntity.nxCosWeight) &&
				Objects.equals(nxCosSubtotal, subEntity.nxCosSubtotal) &&
				Objects.equals(nxCosStatus, subEntity.nxCosStatus) &&
				Objects.equals(nxCosWeighUserId, subEntity.nxCosWeighUserId) &&
				Objects.equals(nxCosAccountUserId, subEntity.nxCosAccountUserId) &&
				Objects.equals(nxCosOrdersId, subEntity.nxCosOrdersId) &&
				Objects.equals(nxCommunityGoodsEntity, subEntity.nxCommunityGoodsEntity) &&
				Objects.equals(nxCosDistributerId, subEntity.nxCosDistributerId) &&
				Objects.equals(nxCosCommunityGoodsId, subEntity.nxCosCommunityGoodsId) &&
				Objects.equals(nxCosBuyStatus, subEntity.nxCosBuyStatus) &&
				Objects.equals(nxCosPurchaseUserId, subEntity.nxCosPurchaseUserId) &&
				Objects.equals(nxCosOrderUserId, subEntity.nxCosOrderUserId) &&
				Objects.equals(hasItem, subEntity.hasItem) &&
				Objects.equals(nxCosSubWeight, subEntity.nxCosSubWeight) &&
				Objects.equals(nxCosDistributerId, subEntity.nxCosDistributerId) &&
				Objects.equals(nxCosCommunityId, subEntity.nxCosCommunityId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxCommunityOrdersSubId, nxCosNxGoodsId, nxCosCommunityGoodsFatherId, nxCosQuantity, nxCosStandard, nxCosPrice, nxCosRemark, nxCosWeight, nxCosSubtotal, nxCosStatus, nxCosWeighUserId, nxCosAccountUserId, nxCosOrdersId, nxCommunityGoodsEntity, nxCosDistributerId, nxCosCommunityGoodsId, nxCosBuyStatus, nxCosPurchaseUserId, nxCosOrderUserId, hasItem, nxCosSubWeight, nxCosDistributerId, nxCosCommunityId);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof NxCommunityOrdersSubEntity) {
			NxCommunityOrdersSubEntity e = (NxCommunityOrdersSubEntity) o;

			return this.nxCosCommunityGoodsId.compareTo(e.nxCosCommunityGoodsId);
		}
		return 0;
	}
}

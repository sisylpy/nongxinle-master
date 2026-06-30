package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityGoodsEntity implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;


	/**
	 *  批发商商品id
	 */
	private Integer nxCommunityGoodsId;
	/**
	 *  批发商id
	 */
	private Integer nxCgCommerceId;

	/**
	 * 社区id
	 */
	private Integer nxCgCommunityId;



	/**
	 *  商品状态
	 */
	private Integer nxCgGoodsStatus;
	private Integer nxCgIsOpenAdsense;

	/**
	 *  是否称重
	 */
	private Integer nxCgGoodsIsWeight;
	/**
	 * 价格
	 */
	private String nxCgGoodsPrice;
	private String nxCgGoodsPriceInteger;
	private String nxCgGoodsTwoPrice;
	private String nxCgGoodsThreePrice;
	/**
	 * 价格小数点
	 */
	private String nxCgGoodsPriceDecimal;

	private String nxCgNxGoodsFilePath;
	private Integer nxCgGoodsStandardType; // 0 选择商品规格；1，选择其他规格。

	private Integer nxCgCfgGoodsFatherId;


	/**
	 *  商品id
	 */
	private Integer nxCgNxGoodsId;
	/**
	 * 父类id
	 */
	private Integer nxCgNxFatherId;
	private String nxCgNxFatherName;
	/**
	 * image
	 */
	private String nxCgNxFatherImg;
	private String nxCgNxGoodsTopFilePath;
	/** 介绍视频相对路径（如 goodsVideo/xxx.mp4） */
	private String nxCgGoodsIntroVideo;
	private Integer nxCgNxGrandId;
	private String nxCgNxGrandName;
	private Integer nxCgNxGreatGrandId;
	private String nxCgNxGreatGrandName;

	private Integer nxCgGoodsTotalHits;
	/**
	 *  采购数量
	 */
	private String nxCgPurchaseQuantity;

	private Integer nxCgGoodsBuyType;
	private Integer nxCgBuyPurchaseUserId;

	private Integer nxCgBuyAppId;
	private Integer nxCgGoodsHuaxianQuantity;

	private Integer nxCgBuyStatus;
	private Integer nxCgSetSubNumber;
	private Integer nxCgSetSubLimitNumber;
	private Integer nxCgGoodsSort;

	private Integer nxCgDistributerId;
	private Integer nxCgDistributerGoodsId;
	private String nxCgBuyingPrice;
	private Integer nxCgGoodsType;

	private String nxCgGoodsName;

	private String nxCgGoodsDetail;

	private String nxCgGoodsStandardname;
	private String nxCgGoodsBrand;
	private String nxCgGoodsPlace;

	private String nxCgGoodsStock;

	private String nxCgGoodsPinyin;

	private String nxCgGoodsPy;
	private Integer nxCgSellType;
	private Integer nxCgCustomerPrice;
	private String nxCgGoodsPriceExchange;
	private String nxCgGoodsTwoPriceExchange;
	private String nxCgGoodsThreePriceExchange;
	private String nxCgGoodsPriceExchangeDate;
	private String nxCgGoodsTwoPriceExchangeDate;
	private String nxCgGoodsThreePriceExchangeDate;
	private String nxCgBuyingPriceExchange;
	private String nxCgBuyingPriceExchangeDate;
	private String nxCgPrintSn;
	private Integer nxCgIsSetMenu;
	private Integer nxCgCardId;
	private Integer nxCgPromotionType;


	////////


	/**
	 * 订货规格组
	 */


	private Boolean isSelected = false;
	private Boolean nxCgPrintSelected = true;


   private String nxCgGoodsStandardWeight;
   private String nxCgNxGoodsFatherColor;
   private Integer nxCgPullOff;
   private Integer nxCgCommunitySupplierId;

   private String nxCgExpectGrossProfit;
   private String nxCgRealityGrossProfit;
   private String nxCgGoodsHuaxianPrice;
   private String nxCgGoodsHuaxianPriceDifferent;
   private String nxCgGoodsGrossWeight;
   private String nxCgGoodsGrossPrice;
   private String nxCgGoodsNetWeight;
   private String nxCgGoodsNetPrice;


	private NxCommunityEntity nxCommunityEntity;

	private String couponStartDate;
	private String couponStartTime;
	private String couponStopDate;
	private String couponStopTime;
	private String nxCgStartTime;
	private String nxCgStartTimeZone;
	private String nxCgStopTimeZone;
	private String nxCgStopTime;
	private String nxCgAdsenseStartTime;
	private String nxCgAdsenseStopTime;
	private String nxCgAdsenseStartTimeZone;
	private String nxCgAdsenseStopTimeZone;
	private Integer nxCgAdsenseStockQuantity;
	private Integer nxCgAdsenseRestQuantity;
	/** Adsense 起订量 */
	private Integer nxCgAdsenseMinOrderQty;
	/** Adsense 购买倍数，默认 1 */
	private Integer nxCgAdsenseOrderMultiple;
	/** Adsense 每人限购 */
	private Integer nxCgAdsenseLimitPerCustomer;
	private Integer nxCgPromotionAmount;
	private Integer nxCgServiceType;
	private String nxCgPromotionPrice;
	private String nxCgPromotionWords;

	private NxCommunityCardEntity nxCommunityCardEntity;

	private List<NxCommunityStandardEntity> nxCommunityStandardEntities;
	private List<NxAliasEntity> nxAliasEntities;
	private List<NxCommunityAliasEntity> nxCommunityAliasEntities;

	private List<NxCommunityOrdersSubEntity> remarkMemberOrdersSubEntities;
	private List<NxCommunityOrdersSubEntity> remarkOrdersSubEntities;
	private List<NxCommunityOrdersSubEntity> itemSubEntities;
	private NxCommunityOrdersSubEntity ordersSubEntity;
	private NxCommunityOrdersSubEntity memberOrdersSubEntity;

	private List<NxCommunityGoodsSetItemEntity> nxCommunityGoodsSetItemEntities;

	private List<NxCommunityGoodsSetPropertyEntity> nxCommunityGoodsSetPropertyEntities;


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxCommunityGoodsEntity that = (NxCommunityGoodsEntity) o;
		return Objects.equals(nxCommunityGoodsId, that.nxCommunityGoodsId) &&
				Objects.equals(nxCgCommerceId, that.nxCgCommerceId) &&
				Objects.equals(nxCgCommunityId, that.nxCgCommunityId) &&
				Objects.equals(nxCgGoodsStatus, that.nxCgGoodsStatus) &&
				Objects.equals(nxCgGoodsIsWeight, that.nxCgGoodsIsWeight) &&
				Objects.equals(nxCgGoodsPrice, that.nxCgGoodsPrice) &&
				Objects.equals(nxCgGoodsPriceDecimal, that.nxCgGoodsPriceDecimal) &&
				Objects.equals(nxCgNxGoodsFilePath, that.nxCgNxGoodsFilePath) &&
				Objects.equals(nxCgGoodsStandardType, that.nxCgGoodsStandardType) &&
				Objects.equals(nxCgCfgGoodsFatherId, that.nxCgCfgGoodsFatherId) &&
				Objects.equals(nxCgNxGoodsId, that.nxCgNxGoodsId) &&
				Objects.equals(nxCgNxFatherId, that.nxCgNxFatherId) &&
				Objects.equals(nxCgNxFatherImg, that.nxCgNxFatherImg) &&
				Objects.equals(nxCgGoodsTotalHits, that.nxCgGoodsTotalHits) &&
				Objects.equals(nxCgPurchaseQuantity, that.nxCgPurchaseQuantity) &&
				Objects.equals(nxCgGoodsBuyType, that.nxCgGoodsBuyType) &&
				Objects.equals(nxCgBuyPurchaseUserId, that.nxCgBuyPurchaseUserId) &&
				Objects.equals(nxCgBuyAppId, that.nxCgBuyAppId) &&
				Objects.equals(nxCgBuyStatus, that.nxCgBuyStatus) &&
				Objects.equals(nxCgDistributerId, that.nxCgDistributerId) &&
				Objects.equals(nxCgBuyingPrice, that.nxCgBuyingPrice) &&
				Objects.equals(nxCgGoodsType, that.nxCgGoodsType) &&
				Objects.equals(nxCgGoodsName, that.nxCgGoodsName) &&
				Objects.equals(nxCgGoodsDetail, that.nxCgGoodsDetail) &&
				Objects.equals(nxCgGoodsStandardname, that.nxCgGoodsStandardname) &&
				Objects.equals(nxCgGoodsStock, that.nxCgGoodsStock) &&
				Objects.equals(nxCgGoodsPinyin, that.nxCgGoodsPinyin) &&
				Objects.equals(nxCgGoodsPy, that.nxCgGoodsPy) &&
				Objects.equals(isSelected, that.isSelected) &&
				Objects.equals(nxCgNxGrandId, that.nxCgNxGrandId) &&
				Objects.equals(nxCgNxGrandName, that.nxCgNxGrandName) &&
				Objects.equals(nxCgNxGreatGrandId, that.nxCgNxGreatGrandId) &&
				Objects.equals(nxCgNxGreatGrandName, that.nxCgNxGreatGrandName) &&
				Objects.equals(nxCommunityEntity, that.nxCommunityEntity) &&
				Objects.equals(nxCommunityStandardEntities, that.nxCommunityStandardEntities);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxCommunityGoodsId, nxCgCommerceId, nxCgCommunityId, nxCgGoodsStatus, nxCgGoodsIsWeight, nxCgGoodsPrice, nxCgGoodsPriceDecimal, nxCgNxGoodsFilePath, nxCgGoodsStandardType, nxCgCfgGoodsFatherId, nxCgNxGoodsId, nxCgNxFatherId, nxCgNxFatherImg, nxCgGoodsTotalHits, nxCgPurchaseQuantity, nxCgGoodsBuyType, nxCgBuyPurchaseUserId, nxCgBuyAppId, nxCgBuyStatus, nxCgDistributerId, nxCgBuyingPrice, nxCgGoodsType, nxCgGoodsName, nxCgGoodsDetail, nxCgGoodsStandardname, nxCgGoodsStock, nxCgGoodsPinyin, nxCgGoodsPy,  isSelected, nxCgNxGrandId, nxCgNxGrandName, nxCgNxGreatGrandId, nxCgNxGreatGrandName, nxCommunityEntity, nxCommunityStandardEntities);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof NxCommunityGoodsEntity) {
			NxCommunityGoodsEntity e = (NxCommunityGoodsEntity) o;
			return this.nxCommunityGoodsId.compareTo(e.nxCommunityGoodsId);
		}
		return 0;
	}
}

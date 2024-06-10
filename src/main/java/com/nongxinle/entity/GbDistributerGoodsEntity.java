package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-18 21:32
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerGoodsEntity implements Serializable,Comparable {
	private static final long serialVersionUID = 1L;

	/**
	 *  社区商品id
	 */
	private Integer gbDistributerGoodsId;
	/**
	 *  批发商父类商品id
	 */
	private Integer gbDgDfgGoodsFatherId;
	/**
	 *  批发商id
	 */
	private Integer gbDgDistributerId;
	/**
	 *  商品状态
	 */
	private Integer gbDgGoodsStatus;
	/**
	 *  是否称重
	 */
	private Integer gbDgGoodsIsWeight;
	/**
	 *  商品名称
	 */
	private String gbDgGoodsName;
	/**
	 *  商品详细
	 */
	private String gbDgGoodsDetail;
	/**
	 *  商品规格
	 */
	private String gbDgGoodsStandardname;
	/**
	 *  社区商品拼音
	 */
	private String gbDgGoodsPinyin;
	/**
	 *  社区商品拼音简拼
	 */
	private String gbDgGoodsPy;
	/**
	 *  gbGoodsId
	 */
	private Integer gbDgNxGoodsId;
	/**
	 *  进货方式
	 */
	private String gbDgNxFatherImg;
	private String gbDgNxFatherImgLarge;
	/**
	 *  gbGoodsFatherId
	 */
	private Integer gbDgNxFatherId;
	private String gbDgNxGrandName;
	private String gbDgNxGreatGrandName;
	private String gbDgNxFatherName;
	private String gbDgNxGoodsFatherImg;
	private Integer gbDgControlPrice;
	private Integer gbDgControlFresh;
	private String gbDgFreshWarnHour;
	private String gbDgFreshWasteHour;
    private Integer gbDgGoodsInventoryType;
    private Integer gbDgGbSupplierId;


	/**
	 *  gbGoodsGrandid
	 */
	private Integer gbDgNxGrandId;

	private Integer gbDgIsFranchisePrice;
	/**
	 *  gbGreatGrandid
	 */
	private Integer gbDgNxGreatGrandId;
	/**
	 *  是否下架
	 */
	private Integer gbDgPullOff;
	/**
	 *
	 */
	private String gbDgGoodsBrand;
	/**
	 *
	 */
	private String gbDgGoodsPlace;
	/**
	 *
	 */
	private String gbDgNxGoodsFatherColor;
	/**
	 *
	 */
	private String gbDgGoodsStandardWeight;

	/**
	 *
	 */
	private Integer gbDgGoodsType;
	private String gbDgGoodsPrice;
	private String gbDgGoodsLowestPrice;
	private String gbDgGoodsHighestPrice;
	private String gbDgSelfPrice;
	private String gbDgSellingPrice;


	private Integer gbDgNxDistributerId;
	private Integer gbDgNxDistributerGoodsId;
	private Integer gbDgGbDepartmentId;
	private Integer gbDgIsSelfControl;
	private Integer gbDgGoodsSort;
	private Integer gbDgGoodsSonsSort;
	private Integer gbDgGoodsIsHidden;

	private Boolean isSelected = false;

	private List<NxStandardEntity> nxStandardEntities;
	private List<GbDistributerAliasEntity> gbDistributerAliasEntities;
	private List<NxAliasEntity> nxAliasEntities;
	private List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities;
	private GbDepartmentOrdersEntity gbDepartmentOrdersEntity;
	private GbDistributerWeightGoodsEntity gbDistributerWeightGoodsEntity;

	private List<GbDistributerStandardEntity> gbDistributerStandardEntities;
	private NxDistributerEntity nxDistributerEntity;
	private GbDepartmentEntity gbDepartmentEntity;
	private NxDistributerGoodsEntity nxDistributerGoodsEntity;
	private List<GbDistributerPurchaseGoodsEntity>  unPurDisGoodsList;
	private List<GbDepartmentGoodsStockEntity> gbDepartmentGoodsStockEntities;
	private GbDepartmentGoodsStockEntity gbDepartmentGoodsStockEntity;
	private GbDistributerGoodsShelfEntity gbDistributerGoodsShelfEntity;
	private GbDistributerGoodsShelfGoodsEntity gbDistributerGoodsShelfGoodsEntity;
	private List<GbDistributerPurchaseGoodsEntity> wastePurGoodsEntities;
    private GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity;

	private  List<GbDepartmentEntity> wasteDepartmentEntities;
	private TreeSet<GbDepartmentEntity> stockDepartmentEntities;
	private TreeSet<GbDepartmentEntity> produceDepartmentEntities;
	private List<GbDepartmentOrdersEntity> prepareOrderEntities;
	private List<GbDepartmentOrdersEntity> weightedOrderEntities;
	private List<GbDepartmentOrdersEntity> deliveryOrderEntities;
	private GbDistributerWeightGoodsEntity prepareWeightGoods;
	private List<GbDistributerWeightGoodsEntity> printedWeightGoods;
	private List<GbDistributerWeightGoodsEntity> finishWeightGoods;
	private List<GbDistributerGoodsPriceEntity> gbDisGoodsPriceEntities;



	private Double goodsStockTotal = 0.0;
	private String goodsStockTotalString;
	private Double outStockTotal = 0.0;
	private String outStockTotalString;

	private Double goodsAverageStockTotal = 0.0;
	private String goodsAverageStockTotalString;


	private Double goodsPriceTotal = 0.0;
	private String goodsPriceTotalString;
	private Double goodsAveragePrice = 0.0;
	private String goodsAveragePriceString;
	private Integer goodsAveragePriceWhat = 0;

	private Double goodsAveragePricePercent = 0.0;
	private String goodsAveragePricePercentString;


	private Double goodsCostTotal = 0.0;
	private String goodsCostTotalString;

	private Double goodsCostWeightTotal = 0.0;
	private String goodsCostWeightTotalString;
	private Double goodsWeightTotal = 0.0;
	private String goodsWeightTotalString;


	private Double goodsWasteTotal = 0.0;
	private String goodsWasteTotalString;
	private Double goodsWasteWeightTotal = 0.0;
	private String goodsWasteWeightTotalString;

	private Double goodsLossTotal = 0.0;
	private String goodsLossTotalString;
	private Double goodsLossWeightTotal = 0.0;
	private String goodsLossWeightTotalString;

	private Double goodsProduceTotal = 0.0;
	private String goodsProduceTotalString;
	private Double goodsProfitTotal = 0.0;
	private String goodsProfitTotalString;

	private Double goodsProduceWeightTotal = 0.0;
	private String goodsProduceWeightTotalString;


	private Double goodsEveryWasteTotal = 0.0;
	private String goodsEveryWasteTotalString;
	private Double goodsEveryWasteWeightTotal = 0.0;
	private String goodsEveryWasteWeightTotalString;

	private Double goodsEveryLossTotal = 0.0;
	private String goodsEveryLossTotalString;
	private Double goodsEveryLossWeightTotal = 0.0;
	private String goodsEveryLossWeightTotalString;

	private Double goodsEveryProfitTotal = 0.0;
	private String goodsEveryProfitTotalString;
	private Double goodsEveryProduceTotal = 0.0;
	private String goodsEveryProduceTotalString;
	private Double goodsEveryProduceWeightTotal = 0.0;
	private String goodsEveryProduceWeightTotalString;
	private Double everyDayWeight;
	private String everyDayWeightString;
	private Double everyWeekWeight;
	private String everyWeekWeightString;
	private Double everyMonthWeight;
	private String everyMonthWeightString;
	private String averageManyTotal;
	private String goodsStockManyString;
	private Double goodsStockMany;

	private String gbDgFranchisePriceOne;
	private String gbDgFranchisePriceOneUpdate;
	private String gbDgFranchisePriceTwo;
	private String gbDgFranchisePriceTwoUpdate;
	private String gbDgFranchisePriceThree;
	private String gbDgFranchisePriceThreeUpdate;


	private Double goodsFreshRate;
	private String goodsFreshRateString;
	private String goodsClearTimeString;
	private Double goodsClearTime;
	private String goodsCostRateString;
	private Double goodsCostRate;
	private String goodsSalesRateString;
	private Double goodsSalesRate;

	private String goodsLossRateString;
	private Double goodsLossRate;

	private String goodsWasteRateString;
	private Double goodsWasteRate;
	private int goodsPurTotalCount;
	private String goodsPurTotalWeight;


	private GbDepartmentGoodsStockReduceEntity reduceEntity;



	private NxJrdhSupplierEntity gbDistributerAppointSupplierEntity;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GbDistributerGoodsEntity that = (GbDistributerGoodsEntity) o;
		return Objects.equals(gbDistributerGoodsId, that.gbDistributerGoodsId) &&
				Objects.equals(gbDgDfgGoodsFatherId, that.gbDgDfgGoodsFatherId) &&
				Objects.equals(gbDgDistributerId, that.gbDgDistributerId) &&
				Objects.equals(gbDgGoodsStatus, that.gbDgGoodsStatus) &&
				Objects.equals(gbDgGoodsIsWeight, that.gbDgGoodsIsWeight) &&
				Objects.equals(gbDgGoodsName, that.gbDgGoodsName) &&
				Objects.equals(gbDgGoodsDetail, that.gbDgGoodsDetail) &&
				Objects.equals(gbDgGoodsStandardname, that.gbDgGoodsStandardname) &&
				Objects.equals(gbDgGoodsPinyin, that.gbDgGoodsPinyin) &&
				Objects.equals(gbDgGoodsPy, that.gbDgGoodsPy) &&
				Objects.equals(gbDgNxGoodsId, that.gbDgNxGoodsId) &&
				Objects.equals(gbDgNxFatherImg, that.gbDgNxFatherImg) &&
				Objects.equals(gbDgNxFatherId, that.gbDgNxFatherId) &&
				Objects.equals(gbDgNxGrandName, that.gbDgNxGrandName) &&
				Objects.equals(gbDgNxGreatGrandName, that.gbDgNxGreatGrandName) &&
				Objects.equals(gbDgNxFatherName, that.gbDgNxFatherName) &&
				Objects.equals(gbDgNxGoodsFatherImg, that.gbDgNxGoodsFatherImg) &&
				Objects.equals(gbDgControlPrice, that.gbDgControlPrice) &&
				Objects.equals(gbDgControlFresh, that.gbDgControlFresh) &&
				Objects.equals(gbDgFreshWarnHour, that.gbDgFreshWarnHour) &&
				Objects.equals(gbDgFreshWasteHour, that.gbDgFreshWasteHour) &&
				Objects.equals(gbDgGoodsInventoryType, that.gbDgGoodsInventoryType) &&
				Objects.equals(gbDgNxGrandId, that.gbDgNxGrandId) &&
				Objects.equals(gbDgNxGreatGrandId, that.gbDgNxGreatGrandId) &&
				Objects.equals(gbDgPullOff, that.gbDgPullOff) &&
				Objects.equals(gbDgGoodsBrand, that.gbDgGoodsBrand) &&
				Objects.equals(gbDgGoodsPlace, that.gbDgGoodsPlace) &&
				Objects.equals(gbDgNxGoodsFatherColor, that.gbDgNxGoodsFatherColor) &&
				Objects.equals(gbDgGoodsStandardWeight, that.gbDgGoodsStandardWeight) &&
				Objects.equals(gbDgGoodsType, that.gbDgGoodsType) &&
				Objects.equals(gbDgGoodsPrice, that.gbDgGoodsPrice) &&
				Objects.equals(gbDgGoodsLowestPrice, that.gbDgGoodsLowestPrice) &&
				Objects.equals(gbDgGoodsHighestPrice, that.gbDgGoodsHighestPrice) &&
				Objects.equals(gbDgNxDistributerId, that.gbDgNxDistributerId) &&
				Objects.equals(gbDgNxDistributerGoodsId, that.gbDgNxDistributerGoodsId) &&
				Objects.equals(gbDgGbDepartmentId, that.gbDgGbDepartmentId) &&
				Objects.equals(isSelected, that.isSelected) &&
				Objects.equals(nxStandardEntities, that.nxStandardEntities) &&
				Objects.equals(gbDistributerAliasEntities, that.gbDistributerAliasEntities) &&
				Objects.equals(nxAliasEntities, that.nxAliasEntities) &&
				Objects.equals(gbDepartmentOrdersEntities, that.gbDepartmentOrdersEntities) &&
				Objects.equals(gbDepartmentOrdersEntity, that.gbDepartmentOrdersEntity) &&
				Objects.equals(gbDistributerStandardEntities, that.gbDistributerStandardEntities) &&
				Objects.equals(nxDistributerEntity, that.nxDistributerEntity) &&
				Objects.equals(gbDepartmentEntity, that.gbDepartmentEntity) &&
				Objects.equals(nxDistributerGoodsEntity, that.nxDistributerGoodsEntity) &&
				Objects.equals(unPurDisGoodsList, that.unPurDisGoodsList) &&
				Objects.equals(gbDepartmentGoodsStockEntities, that.gbDepartmentGoodsStockEntities) &&
				Objects.equals(gbDepartmentGoodsStockEntity, that.gbDepartmentGoodsStockEntity) &&
				Objects.equals(gbDistributerGoodsShelfEntity, that.gbDistributerGoodsShelfEntity) &&
				Objects.equals(gbDistributerGoodsShelfGoodsEntity, that.gbDistributerGoodsShelfGoodsEntity) &&
				Objects.equals(wasteDepartmentEntities, that.wasteDepartmentEntities) &&
				Objects.equals(goodsWasteTotal, that.goodsWasteTotal) &&
				Objects.equals(goodsWasteTotalString, that.goodsWasteTotalString)&&
				Objects.equals(goodsLossTotal, that.goodsLossTotal) &&
				Objects.equals(goodsPriceTotal, that.goodsPriceTotal) &&
				Objects.equals(goodsLossTotalString, that.goodsLossTotalString);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gbDistributerGoodsId,goodsPriceTotal, gbDgDfgGoodsFatherId, gbDgDistributerId, gbDgGoodsStatus, gbDgGoodsIsWeight, gbDgGoodsName, gbDgGoodsDetail, gbDgGoodsStandardname, gbDgGoodsPinyin, gbDgGoodsPy, gbDgNxGoodsId, gbDgNxFatherImg, gbDgNxFatherId, gbDgNxGrandName, gbDgNxGreatGrandName, gbDgNxFatherName, gbDgNxGoodsFatherImg, gbDgControlPrice, gbDgControlFresh, gbDgFreshWarnHour, gbDgFreshWasteHour, gbDgGoodsInventoryType, gbDgNxGrandId, gbDgNxGreatGrandId, gbDgPullOff, gbDgGoodsBrand, gbDgGoodsPlace, gbDgNxGoodsFatherColor, gbDgGoodsStandardWeight, gbDgGoodsType, gbDgGoodsPrice, gbDgGoodsLowestPrice, gbDgGoodsHighestPrice, gbDgNxDistributerId, gbDgNxDistributerGoodsId, gbDgGbDepartmentId, isSelected, nxStandardEntities, gbDistributerAliasEntities, nxAliasEntities, gbDepartmentOrdersEntities, gbDepartmentOrdersEntity, gbDistributerStandardEntities, nxDistributerEntity, gbDepartmentEntity, nxDistributerGoodsEntity, unPurDisGoodsList, gbDepartmentGoodsStockEntities, gbDepartmentGoodsStockEntity, gbDistributerGoodsShelfEntity, gbDistributerGoodsShelfGoodsEntity, wasteDepartmentEntities, goodsWasteTotal,goodsWasteTotalString, goodsLossTotal, goodsLossTotalString);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof GbDistributerGoodsEntity) {
			GbDistributerGoodsEntity e = (GbDistributerGoodsEntity) o;
			return this.gbDistributerGoodsId.compareTo(e.gbDistributerGoodsId);
		}
		return 0;
	}
}

package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-18 21:32
 */

import java.io.Serializable;
import java.util.*;
import java.util.Objects;

import com.alibaba.fastjson.annotation.JSONField;
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
    private Integer gbDgDfgGoodsGrandId;
    private Integer gbDgDfgGoodsGreatId;



	/**
	 *  gbGoodsGrandid
	 */
	private Integer gbDgNxGrandId;
	private Integer gbDgQuantityDays;

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
	private String gbDgNxDistributerGoodsPrice;


	private Integer gbDgNxDistributerId;
	private Integer gbDgNxDistributerGoodsId;
	private Integer gbDgGbDepartmentId;
	private Integer gbDgIsSelfControl;
	private Integer gbDgGoodsSort;
	private Integer gbDgGoodsSonsSort;
	private Integer gbDgGoodsIsHidden;

	private Boolean isSelected = false;

	@JSONField(serialize = false)
	private List<NxStandardEntity> nxStandardEntities;
	@JSONField(serialize = false)
	private List<GbDistributerAliasEntity> gbDistributerAliasEntities;
	@JSONField(serialize = false)
	private List<NxAliasEntity> nxAliasEntities;
	@JSONField(serialize = false)
	private List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities;
	@JSONField(serialize = false)
	private GbDepartmentOrdersEntity gbDepartmentOrdersEntity;
		@JSONField(serialize = false)
	private GbDistributerWeightGoodsEntity gbDistributerWeightGoodsEntity;

	@JSONField(serialize = false)
	private List<GbDistributerStandardEntity> gbDistributerStandardEntities;
	@JSONField(serialize = false)
	private NxDistributerEntity nxDistributerEntity;
	@JSONField(serialize = false)
	private GbDepartmentEntity gbDepartmentEntity;
	@JSONField(serialize = false)
	private NxDistributerGoodsEntity nxDistributerGoodsEntity;
	@JSONField(serialize = false)
	private List<GbDistributerPurchaseGoodsEntity>  unPurDisGoodsList;
	@JSONField(serialize = false)
	private List<GbDepartmentGoodsStockEntity> gbDepartmentGoodsStockEntities;
	@JSONField(serialize = false)
	private GbDepartmentGoodsStockEntity gbDepartmentGoodsStockEntity;
	@JSONField(serialize = false)
	private GbDistributerGoodsShelfEntity gbDistributerGoodsShelfEntity;
	@JSONField(serialize = false)
	private GbDistributerGoodsShelfGoodsEntity gbDistributerGoodsShelfGoodsEntity;
	@JSONField(serialize = false)
	private List<GbDistributerPurchaseGoodsEntity> wastePurGoodsEntities;
	@JSONField(serialize = false)
    private GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity;

	@JSONField(serialize = false)
	private  List<GbDepartmentEntity> wasteDepartmentEntities;
	@JSONField(serialize = false)
	private TreeSet<GbDepartmentEntity> stockDepartmentEntities;
	@JSONField(serialize = false)
	private TreeSet<GbDepartmentEntity> produceDepartmentEntities;
	@JSONField(serialize = false)
	private List<GbDepartmentOrdersEntity> prepareOrderEntities;
	@JSONField(serialize = false)
	private List<GbDepartmentOrdersEntity> weightedOrderEntities;
	@JSONField(serialize = false)
	private List<GbDepartmentOrdersEntity> deliveryOrderEntities;
	@JSONField(serialize = false)
	private GbDistributerWeightGoodsEntity prepareWeightGoods;
	@JSONField(serialize = false)
	private List<GbDistributerWeightGoodsEntity> printedWeightGoods;
	@JSONField(serialize = false)
	private List<GbDistributerWeightGoodsEntity> finishWeightGoods;
	@JSONField(serialize = false)
	private List<GbDistributerGoodsPriceEntity> gbDisGoodsPriceEntities;

	private Map<String, Object> purEveryDay;

	private Double goodsStockTotal = 0.0;
	private String goodsStockTotalString;
	private Double goodsStockWeightTotal = 0.0;
	private String goodsStockWeightTotalString = "0";

	private Double outStockTotal = 0.0;
	private String outStockTotalString  = "0";

	private Double goodsAverageStockTotal = 0.0;
	private String goodsAverageStockTotalString  = "0";


	private Double goodsPriceTotal = 0.0;
	private String goodsPriceTotalString;
	private Double goodsAveragePrice = 0.0;
	private String goodsAveragePriceString  = "0";
	private Integer goodsAveragePriceWhat = 0;

	private Double goodsAveragePricePercent = 0.0;
	private String goodsAveragePricePercentString  = "0";

	private String goodsAverageOrderTimes  = "0";
	private double goodsAverageStars ;
	private String goodsAverageStarsString  = "0";

	private int goodsStarGreen;
	private int goodsStarGray;
	private int goodsStarHalf;

	private Double goodsCostTotal = 0.0;
	private String goodsCostTotalString  = "0";

	private Double goodsCostWeightTotal = 0.0;
	private String goodsCostWeightTotalString  = "0";
	private Double goodsWeightTotal = 0.0;
	private String goodsWeightTotalString  = "0";



	private Double goodsWasteTotal = 0.0;
	private String goodsWasteTotalString  = "0";
	private Double goodsWasteWeightTotal = 0.0;
	private String goodsWasteWeightTotalString  = "0";
	private String goodsWastePercent  = "0";

	private Double goodsLossTotal = 0.0;
	private String goodsLossTotalString  = "0";
	private Double goodsLossWeightTotal = 0.0;
	private String goodsLossWeightTotalString  = "0";
	private String goodsLossPercent  = "0";

	private Double goodsProduceTotal = 0.0;
	private String goodsProduceTotalString  = "0";
	private Double goodsProfitTotal = 0.0;
	private String goodsProfitTotalString  = "0";


	private Double goodsProduceWeightTotal = 0.0;
	private String goodsProduceWeightTotalString;
	private String goodsProducePercent;

	private Double goodsReturnWeightTotal = 0.0;
	private String goodsReturnWeightTotalString  = "0";
	private Double goodsReturnTotal = 0.0;
	private String goodsReturnTotalString = "0";
	private String goodsReturnPercent = "0";

	private Double goodsEveryWasteTotal = 0.0;
	private String goodsEveryWasteTotalString = "0";
	private Double goodsEveryWasteWeightTotal = 0.0;
	private String goodsEveryWasteWeightTotalString = "0";

	private Double goodsEveryLossTotal = 0.0;
	private String goodsEveryLossTotalString = "0";
	private Double goodsEveryLossWeightTotal = 0.0;
	private String goodsEveryLossWeightTotalString = "0";

	private Double goodsEveryProfitTotal = 0.0;
	private String goodsEveryProfitTotalString = "0";
	private Double goodsEveryProduceTotal = 0.0;
	private String goodsEveryProduceTotalString = "0";
	private Double goodsEveryProduceWeightTotal = 0.0;
	private String goodsEveryProduceWeightTotalString = "0";
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
	private String goodsPurTotalSubtotal;

	private Map<String, Object> goodsData;


	@JSONField(serialize = false)
	private GbDepartmentGoodsStockReduceEntity reduceEntity;

	@JSONField(serialize = false)
	private NxJrdhSupplierEntity gbDistributerAppointSupplierEntity;

	private String gbTipText;
	private String aiOrderQuantity;

	/**
	 * AI建议订货单位
	 */
	private String aiOrderStandard;

	/**
	 * 长期日均用量
	 */
	private String aiDailyUsage;

	/**
	 * 最近7天平均用量
	 */
	private String aiRecentAvgUsage;

	/**
	 * 用量波动性(CV)
	 */
	private String aiUsageVariation;

	/**
	 * 安全库存
	 */
	private String aiSafetyStock;

	/**
	 * 再订货点
	 */
	private String aiReorderPoint;

	/**
	 * 当前库存
	 */
	private String aiCurrentStock;

	/**
	 * 当前库存单位
	 */
	private String aiCurrentStockUnit;

	/**
	 * 上次订货日期
	 */
	private String aiLastOrderDate;

	/**
	 * 上次订货量
	 */
	private String aiLastOrderQuantity;

	/**
	 * 上次订货单位
	 */
	private String aiLastOrderUnit;

	/**
	 * 距离上次订货天数
	 */
	private String aiDaysSinceLastOrder;

	/**
	 * 预计明天用量
	 */
	private String aiTomorrowNeed;

	private String aiAvailableDays;


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

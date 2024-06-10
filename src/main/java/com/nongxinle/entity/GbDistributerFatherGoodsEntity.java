package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-18 21:32
 */

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.TreeSet;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerFatherGoodsEntity implements Serializable,Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDistributerFatherGoodsId;
	/**
	 *  
	 */
	private String gbDfgFatherGoodsName;
	/**
	 *  
	 */
	private String gbDfgFatherGoodsImg;
	private String gbDfgFatherGoodsImgLarge;
	/**
	 *  
	 */
	private Integer gbDfgFatherGoodsSort;
	/**
	 *  
	 */
	private String gbDfgFatherGoodsColor;
	/**
	 *  
	 */
	private Integer gbDfgFathersFatherId;
	/**
	 *  
	 */
	private Integer gbDfgFatherGoodsLevel;
	/**
	 *  
	 */
	private Integer gbDfgDistributerId;
	/**
	 *  
	 */
	private Integer gbDfgGoodsAmount;

	private Integer gbDfgPriceAmount;
	private Integer gbDfgPriceTwoAmount;
	private Integer gbDfgPriceThreeAmount;
	private Integer gbDfgNxGoodsId;
	private List<GbDistributerGoodsEntity> gbDistributerGoodsEntities;

	private List<GbDistributerFatherGoodsEntity> fatherGoodsEntities;
	private List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities;
	private List<GbDepartmentDisGoodsEntity> gbDepartmentDisGoodsEntities;
	private List<GbDepartmentGoodsStockEntity> gbDepartmentGoodsStockEntities;
	private String GbDgGoodsSubNames;

	private Boolean isSelected = false;

	private BigDecimal purchaseSubTotal;
	private Double highestTotal ;
	private Double lowestTotal;
	private String fatherStockSubtotalString;
	private Double fatherStockSubtotal;
	private String fatherOutStockTotalString;
	private Double fatherOutStockTotal;
	private String fatherStockTotalPercent;
	private String fatherStockTotalString;
	private Double fatherStockTotal;
	private String fatherRestWeightTotalString;
	private Double fatherRestWeightTotal;
	private Double fatherProduceTotal;
	private String fatherProduceTotalString;

	private Double fatherProduceWeight;
	private String fatherProduceWeightString;
	private Double fatherCostWeight;
	private String fatherCostWeightString;

	private Double fatherCostSubtotal;
	private String fatherCostSubtotalString;
	private String fatherCostSubtotalPercentString;

	private Double fatherLossWeight;
	private String fatherLossWeightString;

	private Double fatherWasteWeight;
	private String fatherWasteWeightString;
	private String fatherCostTotal;
	private Double fatherProfitTotal;
	private String fatherProfitTotalString;
	private Double fatherSellingSubtotal;
	private String fatherSellingSubtotalString;

	private Double fatherLossTotal;
	private String fatherLossTotalString;
	private Double fatherWasteTotal;
	private String fatherWasteTotalString;
	private Double fatherRestTotal;
	private String fatherRestTotalString;
	private Double fatherReturnTotal;
	private String fatherReturnTotalString;
	private Double everyDayWeight;
	private String everyDayWeightString;

	private Double everyDayProfit;
	private String everyDayProfitString;
	private Double everyWeekWeight;
	private String everyWeekWeightString;
	private Double everyMonthWeight;
	private String everyMonthWeightString;
	private String averageManyTotal;


	private String goodsPriceLowestTotal;
	private String goodsPriceLowestTotalNormal;
	private String goodsPriceLowestTotalScale;
	private String goodsPriceLowestWhatTotal;

	private String goodsPriceHighestTotal;
	private String goodsPriceHighestTotalNormal;
	private String goodsPriceHighestTotalScale;
	private String goodsPriceHighestWhatTotal;

	private String goodsPriceWhatScale;
	private String goodsPriceWhat;
	private String goodsPriceWhatTotal;


	private String subGoodsCount;

	private String fatherStockManyString;
	private Double fatherStockMany;

	private String fatherFreshRateString;
	private Double fatherFreshRate;
	private String fatherClearTimeString;
	private Double fatherClearTime;
	private String fatherCostRateString;
	private Double fatherCostRate;
	private String fatherSalesRateString;
	private Double fatherSalesRate;

	private String fatherLossRateString;
	private Double fatherLossRate;

	private String fatherWasteRateString;
	private Double fatherWasteRate;
	@Override
	public int compareTo(Object o) {
		if (o instanceof GbDistributerFatherGoodsEntity) {
			GbDistributerFatherGoodsEntity e = (GbDistributerFatherGoodsEntity) o;
			return this.gbDistributerFatherGoodsId.compareTo(e.gbDistributerFatherGoodsId);
		}
		return 0;
	}
}

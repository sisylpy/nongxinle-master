package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-18 21:32
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepartmentEntity implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订货部门id
	 */
	private Integer gbDepartmentId;
	/**
	 *  订货部门名称
	 */
	private String gbDepartmentName;
	/**
	 *  订货部门上级id
	 */
	private Integer gbDepartmentFatherId;
	/**
	 *  订货部门类型
	 */
	private Integer gbDepartmentType;
	/**
	 *  订货部门子部门数量
	 */
	private Integer gbDepartmentSubAmount;
	/**
	 *  订货部门批发商id
	 */
	private Integer gbDepartmentDisId;
	/**
	 *  
	 */
	private String gbDepartmentFilePath;
	/**
	 *  是客户吗
	 */
	private Integer gbDepartmentIsGroupDep;
	/**
	 *  
	 */
	private String gbDepartmentPrintName;
	/**
	 *  
	 */
	private Integer gbDepartmentShowWeeks;
	/**
	 *  
	 */
	private Integer gbDepartmentSettleType;
	/**
	 *  客户简称
	 */
	private String gbDepartmentAttrName;
	private String gbDepartmentNamePy;

	private Integer gbDepartmentRouteId;

	private String gbDepartmentSettleFullTime;
	private String gbDepartmentSettleDate;
	private String gbDepartmentSettleWeek;
	private String gbDepartmentSettleMonth;
	private String gbDepartmentSettleYear;
	private String gbDepartmentSettleTimes;

	private String unPayTotal;
	private Integer gbDepartmentDepSettleId;
	private Integer gbDepartmentLevel;


	private GbDistributerUserEntity gbDistributerUserEntity;
	private GbDepartmentEntity fatherGbDepartmentEntity;

	private List<GbDepartmentEntity> gbDepartmentEntityList;

	private List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities;
	private List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;

	private GbDepartmentUserEntity gbDepartmentUserEntity;
	private GbDistributerEntity gbDistributerEntity;
	private List<GbDistributerUserEntity> gbDistributerUserEntities;
	private List<GbDepartmentUserEntity>  gbDepartmentUserEntities;
	private List<GbDepartmentDisGoodsEntity> gbDepartmentDisGoodsEntities;
	private List<GbDepartmentEntity> gbSubDepartments;
	private List<GbDistributerSupplierEntity> gbDistributerSupplierEntities;
	private List<GbDepartmentGoodsStockReduceEntity> gbDepGoodsStockReduceEntities;
	private List<GbDepartmentGoodsStockEntity> gbDepartmentGoodsStockEntities;
	private List<GbDistributerFatherGoodsEntity> fatherGoodsEntities;

    private List<NxDistributerGbDistributerEntity> appSupplierList;
    private GbDepartmentBillEntity unPayBill;
	private Boolean isSelected = false;

	private Double depWasteGoodsTotal = 0.0;
	private String depWasteGoodsTotalString;
	private Double depWasteGoodsWeightTotal = 0.0;
	private String depWasteGoodsWeightTotalString;

	private Double depReturnGoodsWeightTotal = 0.0;
	private String depReturnGoodsWeightTotalString;

	private Double depReturnGoodsTotal = 0.0;
	private String depReturnGoodsTotalString;
	private Double depLossGoodsTotal  = 0.0;
	private String depLossGoodsTotalString;
	private Double depLossGoodsWeightTotal  = 0.0;
	private String depLossGoodsWeightTotalString;

	private Double depRestGoodsTotal  = 0.0;
	private String depRestGoodsTotalString;
	private Double depRestGoodsWeightTotal  = 0.0;
	private String depRestGoodsWeightTotalString;

	private Double depProduceGoodsTotal  = 0.0;
	private String depProduceGoodsPercent;
	private String depProducePercent;
	private String depLossPercent;
	private String depWastePercent;
	private String depLossGoodsPercent;
	private String depWasteGoodsPercent;
	private String depProduceGoodsTotalString;
	private Double depCostGoodsTotal  = 0.0;
	private String depCostGoodsTotalString;
	private Double DepProduceGoodsWeightTotal;
	private String depProduceGoodsWeightTotalString;
	private Double depProfitGoodsTotal;
	private String depProfitGoodsTotalString;

	private Double depWasteGoodsEveryTotal  = 0.0;
	private String depWasteGoodsEveryTotalString;
	private Double depWasteGoodsEveryWeightTotal  = 0.0;
	private String depWasteGoodsEveryWeightTotalString;

	private Double DepLossGoodsEveryTotal  = 0.0;
	private String depLossGoodsEveryTotalString;
	private Double DepLossGoodsEveryWeightTotal  = 0.0;
	private String depLossGoodsEveryWeightTotalString;

	private Double DepProduceGoodsEveryTotal  = 0.0;
	private String depProduceGoodsEveryTotalString;
	private Double DepProduceGoodsEveryWeightTotal  = 0.0;
	private String depProduceGoodsEveryWeightTotalString;

	private String newOrderAmount ;
	private String prepareOrderAmount;
	private String hasWeightOrderAmount;
	private String updateSubtotal;
	private Integer gbDepartmentPrintSet;



	private String depFreshRateString;
	private Double depFreshRate;
	private String depClearTimeString;
	private Double depClearTime;
	private String depCostRateString;
	private Double depCostRate;
	private String depSalesRateString;
	private Double depSalesRate;

	private String depLossRateString;
	private Double depLossRate;

	private String depWasteRateString;
	private Double depWasteRate;


	private Double depCostUseStockTotal = 0.0;
	private Double depCostWasteStockTotal = 0.0;
	private Double depCostLossStockTotal = 0.0;
	private Double depPurchaseTotal = 0.0;
	private Double depPurchaseLowerTotal = 0.0;
	private Double depPurchaseHigherTotal = 0.0;

	private Double depStockSubtotal;
	private String depStockSubtotalString;
	private Double depStockWeightTotal;
	private String depStockWeightTotalString;
	private String depStockAverageSubtotal;
	private Double depStockMany;
	private String depStockManyString;

	private Map<String, Object>  totalMap;

	private List<String> dayData;
	private String fatherGoodsIds;
	private Integer cankaoDepId;

	private GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity;




	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GbDepartmentEntity that = (GbDepartmentEntity) o;
		return Objects.equals(gbDepartmentId, that.gbDepartmentId) &&
				Objects.equals(gbDepartmentName, that.gbDepartmentName) &&
				Objects.equals(gbDepartmentFatherId, that.gbDepartmentFatherId) &&
				Objects.equals(gbDepartmentType, that.gbDepartmentType) &&
				Objects.equals(gbDepartmentSubAmount, that.gbDepartmentSubAmount) &&
				Objects.equals(gbDepartmentDisId, that.gbDepartmentDisId) &&
				Objects.equals(gbDepartmentFilePath, that.gbDepartmentFilePath) &&
				Objects.equals(gbDepartmentIsGroupDep, that.gbDepartmentIsGroupDep) &&
				Objects.equals(gbDepartmentPrintName, that.gbDepartmentPrintName) &&
				Objects.equals(gbDepartmentShowWeeks, that.gbDepartmentShowWeeks) &&
				Objects.equals(gbDepartmentSettleType, that.gbDepartmentSettleType) &&
				Objects.equals(gbDepartmentAttrName, that.gbDepartmentAttrName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gbDepartmentId, gbDepartmentName, gbDepartmentFatherId, gbDepartmentType, gbDepartmentSubAmount, gbDepartmentDisId, gbDepartmentFilePath, gbDepartmentIsGroupDep, gbDepartmentPrintName, gbDepartmentShowWeeks, gbDepartmentSettleType, gbDepartmentAttrName);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof GbDepartmentEntity) {
			GbDepartmentEntity e = (GbDepartmentEntity) o;
			return this.gbDepartmentId.compareTo(e.gbDepartmentId);
		}
		return 0;
	}
}

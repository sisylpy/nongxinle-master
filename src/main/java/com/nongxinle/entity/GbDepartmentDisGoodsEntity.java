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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepartmentDisGoodsEntity implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDepartmentDisGoodsId;
	/**
	 *  
	 */
	private Integer gbDdgDepartmentFatherId;
	/**
	 *  
	 */
	private Integer gbDdgDepartmentId;
	/**
	 *  
	 */
	private Integer gbDdgDisGoodsId;
	/**
	 *  
	 */
	private Integer gbDdgDisGoodsFatherId;
	/**
	 *  
	 */
	private String gbDdgDepGoodsName;
	/**
	 *  
	 */
	private String gbDdgDepGoodsPinyin;
	/**
	 *  
	 */
	private String gbDdgDepGoodsPy;
	/**
	 *  
	 */
	private String gbDdgDepGoodsStandardname;
	/**
	 *  
	 */
	private String gbDdgDepGoodsDetail;
	/**
	 *  
	 */
	private String gbDdgDepGoodsBrand;
	/**
	 *  
	 */
	private String gbDdgDepGoodsPlace;
	/**
	 *  
	 */

	/**
	 *  
	 */
	private Integer gbDdgGoodsType;
	private Integer gbDdgNxDistributerId;
	private Integer gbDdgNxDistributerGoodsId;
	private Integer gbDdgGbDepartmentId;
	private Integer gbDdgGbSupplierId;
	private Integer gbDdgGbDisId;


	private String 	gbDdgInventoryDate;
	private String 	gbDdgInventoryFullTime;
	private String 	gbDdgStockTotalWeight;
	private String 	gbDdgStockTotalSubtotal;
	private String 	gbDdgPrepareTotalWeight;
	private String 	gbDdgShowStandardName;
	private Integer  gbDdgShowStandardId;
	private String 	gbDdgShowStandardWeight;
	private String 	gbDdgShowStandardScale;
	private String 	gbDdgLevelPrice;
	private String 	gbDdgSellingPrice;
	private String 	gbDdgOrderPrice;
	private String 	gbDdgOrderDate;
	private String 	gbDdgOrderRemark;
	private String 	gbDdgOrderQuantity;
	private String 	gbDdgOrderStandard;
	private String 	gbDdgOrderWeight;


	private Integer 	gbDdgPrepareStatus;


	private List<GbDepartmentGoodsStockEntity> gbDepartmentGoodsStockEntities;


	private GbDistributerGoodsEntity gbDistributerGoodsEntity;
	private GbDepartmentOrdersEntity gbDepartmentOrdersEntity;
	private List<GbDepartmentOrdersHistoryEntity> gbDepOrdersHistoryEntities;

	private List<GbDistributerStandardEntity> gbDistributerStandardEntities;
	private NxDistributerEntity nxDistributerEntity;
	private GbDepartmentEntity outStockDepartmentEntity;
	private GbDepartmentEntity gbGoodsDepartmentEntity;
	private NxDistributerGoodsEntity nxDistributerGoodsEntity;
	private GbDepInventoryGoodsDailyEntity gbDepInventoryGoodsDailyEntity;
	private GbDepInventoryGoodsWeekEntity gbDepInventoryGoodsWeekEntity;
	private GbDepInventoryGoodsMonthEntity gbDepInventoryGoodsMonthEntity;
    private GbDepartmentGoodsDailyEntity gbDepGoodsDailyEntity;
//
//	private List<GbDepInventoryGoodsDailyEntity> todayInventoryGoodsDailyEntities;
//	private List<GbDepInventoryGoodsWeekEntity> todayInventoryGoodsWeekEntities;
//	private List<GbDepInventoryGoodsMonthEntity> todayInventoryGoodsMonthEntities;



	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GbDepartmentDisGoodsEntity that = (GbDepartmentDisGoodsEntity) o;
		return Objects.equals(gbDepartmentDisGoodsId, that.gbDepartmentDisGoodsId) &&
				Objects.equals(gbDdgDepartmentFatherId, that.gbDdgDepartmentFatherId) &&
				Objects.equals(gbDdgDepartmentId, that.gbDdgDepartmentId) &&
				Objects.equals(gbDdgDisGoodsId, that.gbDdgDisGoodsId) &&
				Objects.equals(gbDdgDisGoodsFatherId, that.gbDdgDisGoodsFatherId) &&
				Objects.equals(gbDdgDepGoodsName, that.gbDdgDepGoodsName) &&
				Objects.equals(gbDdgDepGoodsPinyin, that.gbDdgDepGoodsPinyin) &&
				Objects.equals(gbDdgDepGoodsPy, that.gbDdgDepGoodsPy) &&
				Objects.equals(gbDdgDepGoodsStandardname, that.gbDdgDepGoodsStandardname) &&
				Objects.equals(gbDdgDepGoodsDetail, that.gbDdgDepGoodsDetail) &&
				Objects.equals(gbDdgDepGoodsBrand, that.gbDdgDepGoodsBrand) &&
				Objects.equals(gbDdgDepGoodsPlace, that.gbDdgDepGoodsPlace) &&
				Objects.equals(gbDdgGoodsType, that.gbDdgGoodsType) &&
				Objects.equals(gbDdgNxDistributerId, that.gbDdgNxDistributerId) &&
				Objects.equals(gbDdgNxDistributerGoodsId, that.gbDdgNxDistributerGoodsId) &&
				Objects.equals(gbDdgGbDepartmentId, that.gbDdgGbDepartmentId) &&
//				Objects.equals(gbDdgStockWeightTotal, that.gbDdgStockWeightTotal) &&
//				Objects.equals(gbDdgStockSubtotalTotal, that.gbDdgStockSubtotalTotal) &&
				Objects.equals(gbDepartmentGoodsStockEntities, that.gbDepartmentGoodsStockEntities) &&
				Objects.equals(gbDistributerGoodsEntity, that.gbDistributerGoodsEntity) &&
				Objects.equals(gbDepartmentOrdersEntity, that.gbDepartmentOrdersEntity) &&
				Objects.equals(gbDepOrdersHistoryEntities, that.gbDepOrdersHistoryEntities) &&
				Objects.equals(gbDistributerStandardEntities, that.gbDistributerStandardEntities) &&
				Objects.equals(nxDistributerEntity, that.nxDistributerEntity) &&
				Objects.equals(outStockDepartmentEntity, that.outStockDepartmentEntity) &&
				Objects.equals(nxDistributerGoodsEntity, that.nxDistributerGoodsEntity);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gbDepartmentDisGoodsId, gbDdgDepartmentFatherId, gbDdgDepartmentId, gbDdgDisGoodsId, gbDdgDisGoodsFatherId, gbDdgDepGoodsName, gbDdgDepGoodsPinyin, gbDdgDepGoodsPy, gbDdgDepGoodsStandardname, gbDdgDepGoodsDetail, gbDdgDepGoodsBrand, gbDdgDepGoodsPlace, gbDdgGoodsType, gbDdgNxDistributerId, gbDdgNxDistributerGoodsId, gbDdgGbDepartmentId, gbDepartmentGoodsStockEntities, gbDistributerGoodsEntity, gbDepartmentOrdersEntity, gbDepOrdersHistoryEntities, gbDistributerStandardEntities, nxDistributerEntity, outStockDepartmentEntity, nxDistributerGoodsEntity);
	}

	@Override
	public int compareTo(Object o) {

		if (o instanceof GbDepartmentDisGoodsEntity) {
			GbDepartmentDisGoodsEntity e = (GbDepartmentDisGoodsEntity) o;
			return this.gbDepartmentDisGoodsId.compareTo(e.gbDepartmentDisGoodsId);
		}
		return 0;

	}
}

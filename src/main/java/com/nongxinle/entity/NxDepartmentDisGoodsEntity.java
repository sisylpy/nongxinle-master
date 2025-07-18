package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 07-30 23:58
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDepartmentDisGoodsEntity implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDepartmentDisGoodsId;
	/**
	 *  
	 */
	private Integer nxDdgDepartmentFatherId;
	/**
	 *  
	 */
	private Integer nxDdgDepartmentId;
	/**
	 *  
	 */
	private Integer nxDdgDisGoodsId;
	private Integer nxDdgDisGoodsFatherId;

	/**
	 *  
	 */
	private String nxDdgDepGoodsName;
	/**
	 *  
	 */
	private String nxDdgDepGoodsPinyin;
	/**
	 *  
	 */
	private String nxDdgDepGoodsPy;
	/**
	 *  
	 */
	private Integer nxDdgDisGoodsGrandId;

	private Integer nxDdgDisGoodsGreatId;

	private String nxDdgOrderPriceLevel;
	/**
	 *  
	 */
	private String nxDdgDepGoodsStandardname;
	/**
	 *  
	 */
	private String nxDdgDepGoodsDetail;
	private String nxDdgDepGoodsBrand;
	private String nxDdgDepGoodsPlace;
	private String nxDdgOrderPrice;
	private String nxDdgOrderDate;
	private String nxDdgOrderRemark;
	private String nxDdgOrderQuantity;
	private String nxDdgOrderStandard;
	private String nxDdgGoodsPlace;
	private String nxDdgOrderCostPrice;
	private String nxDdgOrderGoodsName;
	private String nxDdgPickDetail;
	/**
	 *  
	 */

	private Integer isDownload;
	private Boolean isSelected = false;

	private Integer nxDdgIsGbDepartment;
	private Integer nxDdgGbDepartmentFatherId;
	private Integer nxDdgGbDepartmentId;
	private Integer nxDdgOrderSellerUserId;
	private Integer nxDdgOrderBuyerUserId;
	private Integer nxDdgNxDistributerId;
	private Integer nxDdgGbDistributerId;
	private NxDepartmentEntity nxDepartmentEntity;
	private GbDepartmentEntity gbDepartmentEntity;
	private String nxTipText;

	/**
	 * AI建议订货量
	 */
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

	private NxDistributerFatherGoodsEntity cataFather;

	private List<NxDepartmentStandardEntity> nxDepStandardEntities;
	private List<NxDistributerStandardEntity> nxDistributerStandardEntities;
	private String nxDisGoodsFile;
	private String nxDisGoodsFileLarge;
	private String nxDisStandardWeight;
	private String nxDgWillPriceOne;
	private String nxDgWillPriceOneWeight;
	private String nxDgWillPriceTwo;
	private String nxDgWillPriceTwoWeight;

	private String nxDgWillPriceThree;
	private String nxDgWillPriceThreeWeight;
	private String nxDgWillPriceOneStandard;
	private String nxDgWillPriceTwoStandard;
	private String nxDgWillPriceOneAboutPrice;
	private String nxDgWillPriceTwoAboutPrice;
	private String nxDgWillPriceThreeAboutPrice;

	private NxDistributerGoodsEntity nxDistributerGoodsEntity;
	private NxDepartmentOrdersEntity nxDepartmentOrdersEntity;
	private List<NxDepartmentOrdersEntity> depGoodsDepOrderList;
	private List<NxDepartmentOrdersHistoryEntity> nxDepOrdersHistoryEntities;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxDepartmentDisGoodsEntity that = (NxDepartmentDisGoodsEntity) o;
		return Objects.equals(nxDepartmentDisGoodsId, that.nxDepartmentDisGoodsId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxDepartmentDisGoodsId);
	}

	@Override
	public int compareTo(Object o) {

		if (o instanceof NxDepartmentDisGoodsEntity) {
			NxDepartmentDisGoodsEntity e = (NxDepartmentDisGoodsEntity) o;
			return this.nxDepartmentDisGoodsId.compareTo(e.nxDepartmentDisGoodsId);
		}
		return 0;
	}
}

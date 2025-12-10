package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 07-27 17:38
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerGoodsEntity implements Serializable, Comparable  {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  社区商品id
	 */
	private Integer nxDistributerGoodsId;
	/**
	 *  批发商id
	 */
	private Integer nxDgDistributerId;
	/**
	 *  商品状态
	 */
	private Integer nxDgGoodsStatus;
	private Integer nxDgQuantityDays;
	/**
	 *  是否称重
	 */
	private Integer nxDgGoodsIsWeight;
	/**
	 *  批发商商品父类id
	 */
	private Integer nxDgDfgGoodsFatherId;
	/**
	 *  购买热度
	 */
	private Integer nxDgNxGoodsId;
	/**
	 *  采购数量
	 */
	private Integer nxDgNxFatherId;
	private String nxDgNxFatherImg;
	private String nxDgNxFatherName;

	private Integer nxDgNxGrandId;
	private String nxDgNxGrandName;
	private Integer nxDgNxGreatGrandId;
	private String nxDgNxGreatGrandName;
	private String nxDgGoodsFileLarge;

	/**
	 *
	 */
	private String nxDgGoodsFile;
	/**
	 *  商品名称
	 */
	private String nxDgGoodsName;
	/**
	 *  商品详细
	 */
	private String nxDgGoodsDetail;
	private String nxDgGoodsBrand;
	private String nxDgGoodsPlace;
	/**
	 *  商品规格
	 */
	private String nxDgGoodsStandardname;

	private String nxDgGoodsStandardWeight;
	/**
	 *  社区商品拼音
	 */
	private String nxDgGoodsPinyin;
	/**
	 *  社区商品拼音简拼
	 */
	private String nxDgGoodsPy;


	private Integer nxDgPullOff;

	private String nxDgNxGoodsFatherColor;

	private String sellAmount;
	private String sellSubtotal;  // 采购总金额
	private Double goodsStockTotal;  // 商品库存总额
	private String goodsStockTotalString;  // 商品库存总额（字符串）
	private Double goodsStockWeightTotal;  // 商品库存总重量
	private String goodsStockWeightTotalString;  // 商品库存总重量（字符串）
	private Integer nxDgPurchaseAuto;
	private Integer nxDgGoodsSort;

	private String nxDgBuyingPrice;
	private String nxDgPriceProfitOne;
	private String nxDgPriceProfitTwo;
	private String nxDgPriceProfitThree;
	private Integer nxDgSupplierId;
	private String nxDgBuyingPriceUpdate;

	private String nxDgWillPrice;
	private String nxDgWillPriceOne;
	private String nxDgWillPriceOneWeight;
	private String nxDgWillPriceTwo;
	private String nxDgWillPriceTwoWeight;
	private String nxDgWillPriceThree;
	private String nxDgWillPriceThreeWeight;

	private String nxDgBuyingPriceOne;
	private String nxDgBuyingPriceOneUpdate;
	private String nxDgBuyingPriceTwo;
	private String nxDgBuyingPriceTwoUpdate;
	private String nxDgWillPriceUpdate;

	private String nxDgBuyingPriceThree;
	private String nxDgPriceFirstDay;
	private String nxDgPriceSecondDay;
	private String nxDgPriceThirdDay;
	private String nxDgBuyingPriceThreeUpdate;
	private Integer nxDgBuyingPriceIsGrade;
	private Integer nxDgDfgGoodsGrandId;
	private Integer nxDgIsOldestSon;
	private String  orderContent;
	private String  nxDgOutTotalWeight;
	private Integer orderSize;
	private Integer nxDgGoodsSonsSort;
	private Integer nxDgGoodsIsHidden;
	
	/**
	 * 外箱名称
	 */
	private String nxDgCartonUnit;
	/**
	 * 外箱装数量
	 */
	private Integer nxDgItemsPerCarton;
	
	private Integer gbDisGoodsId;
	private Integer gbDisGoodsFatherId;
	private Integer gbDisGoodsType;
	private Integer gbDisGoodsToDepId;
	private String perPrice;

	private NxGoodsEntity nxGoodsEntity;
	private NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity;
    private NxJrdhSupplierEntity nxJrdhSupplierEntity;
    private GbDistributerGoodsEntity  hasGbGoods;

    private GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity;
	private List<NxDistributerStandardEntity> nxDistributerStandardEntities;

	private List<NxStandardEntity> nxStandardEntities;
	private List<NxAliasEntity> nxAliasEntities;
	private List<NxDistributerAliasEntity> nxDistributerAliasEntities;

	private List<NxDepartmentStandardEntity> nxDepartmentStandardEntities;

	private Integer isDownload;

	private List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;
	private List<NxDepartmentOrdersEntity> histfyOrdersEntities;
	private List<NxDepartmentOrdersEntity> neetNotPurOrders;
	private NxDepartmentOrdersEntity nxDepartmentOrdersEntity;
	private List<NxRestrauntOrdersEntity> nxRestrauntOrdersEntities;

	private List<NxDepartmentOrdersHistoryEntity> nxDepartmentOrdersHistoryEntities;

	private NxDistributerEntity nxDistributerEntity;
	private NxDepartmentDisGoodsEntity departmentDisGoodsEntity;
	private NxCommunityGoodsEntity nxCommunityGoodsEntity;
	private List<NxDistributerPurchaseGoodsEntity>  unPurGoodsDisGoodsList;
	private List<NxDistributerPurchaseGoodsEntity>  unPurOrdersDisGoodsList;
	private NxDistributerGoodsEntity sonGoods;
	private List<NxDistributerGoodsEntity> allSons;
//	private GbDistributerGoodsEntity gbDistributerGoodsEntity;
	private NxDistributerPurchaseGoodsEntity shelfPurGoods;
	private NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity;

	private String nxDgWillPriceOneStandard;
	private String nxDgWillPriceTwoStandard;
	private String nxDgWillPriceThreeStandard;
	private String nxDgWillPriceOneAboutPrice;
	private String nxDgWillPriceTwoAboutPrice;
	private String nxDgWillPriceThreeAboutPrice;

	// 出货统计字段
	private Double goodsSalesTotal;   // 销售总额
	private Double goodsWasteTotal;   // 浪费总额
	private Double goodsLossTotal;    // 损耗总额
	private Double goodsReturnTotal;  // 退货总额

	// 采购记录集合
	private List<NxDistributerPurchaseGoodsEntity> nxDistributerPurchaseGoodsEntities;

	// 出货记录集合
	private List<NxDistributerGoodsShelfStockReduceEntity> nxDistributerGoodsShelfStockReduceEntities;

	// 库存批次集合
	private List<NxDistributerGoodsShelfStockEntity> nxDisGoodsShelfStockEntities;

	List<NxDistributerGoodsShelfGoodsEntity> nxDistributerGoodsShelfGoodsEntities;

	/**
	 * 货架名称（用于显示已上架商品的货架信息）
	 */
	private String shelfName;

	/**
	 * 商品最高价格
	 */
	private String nxDgGoodsHighestPrice;
	/**
	 * 商品最低价格
	 */
	private String nxDgGoodsLowestPrice;
	/**
	 * 价格差
	 */
	private String goodsPriceDiff;
	/**
	 * 价格波动（百分比）
	 */
	private String goodsPriceFluctuation;
	/**
	 * 采购总金额
	 */
	private String goodsPurTotalSubtotal;

	@Override
	public int compareTo(Object o) {
		if (o instanceof NxDistributerGoodsEntity) {
			NxDistributerGoodsEntity e = (NxDistributerGoodsEntity) o;
			return this.nxDistributerGoodsId.compareTo(e.nxDistributerGoodsId);
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxDistributerGoodsEntity that = (NxDistributerGoodsEntity) o;
		return Objects.equals(nxDistributerGoodsId, that.nxDistributerGoodsId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxDistributerGoodsId);
	}
}

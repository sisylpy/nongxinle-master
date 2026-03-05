package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 2020-02-10 19:43:11
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

public class NxDistributerEntity implements Serializable , Comparable{
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商id
	 */
	private Integer nxDistributerId;
	/**
	 *  批发商名称
	 */
	private String nxDistributerName;
	/**
	 *  批发商位置经度
	 */
	private String nxDistributerLan;
	/**
	 *  批发商位置纬度
	 */
	private String nxDistributerLun;
	/**
	 *  批发商商业类型
	 */
	private Integer nxDistributerBusinessTypeId;
	private String nxDistributerBuyQuantity;
	private Integer nxDistributerType;
	private Integer nxDistributerShelfQuantity;

	private String nxDistributerImg;
	private String nxDistributerAppId;

	private String nxDistributerManager;
	private String nxDistributerPayUrl;
	private String nxDistributerPhone;
	private String nxDistributerAddress;
	private String nxDistributerMarketName;
	private String distance;
	private double distanceValue;
	private String duration;
	private Boolean isSelected = false;

	private int linshiCount = 0;

	private Double purTimes = 0.0;
	private String purTimesString;
	private Double purGoodsTotal = 0.0;
	private String purGoodsTotalString;

	private Double stockGoodsTotal = 0.0;
	private String stockGoodsTotalString;
	private Double produceGoodsTotal = 0.0;
	private String produceGoodsTotalString;
	private Double wasteGoodsTotal = 0.0;
	private String wasteGoodsTotalString;

	private Double lossGoodsTotal = 0.0;
	private String lossGoodsTotalString;
	private Double returnGoodsTotal = 0.0;
	private String returnGoodsTotalString;
	private String nxDistributerShowName;

	private String total;
	private int billCount;

	private String freshStars;
	private int starGreen;
	private int starGray;
	private int starHalf;
	private Integer nxDistributerSysCityId;
	private Integer nxDistributerSysMarketId;

	private NxDistributerUserEntity nxDistributerUserEntity;

	private Map<String, Object> itemData;
	private List<NxDistributerUserEntity> nxDistributerUserEntities;
	private List<NxCommunityGoodsEntity> nxCommunityGoodsEntities;
	private List<GbDistributerGoodsEntity> gbDistributerGoodsEntities;
	private List<GbDepartmentOrdersEntity> peisongOrders;
	private List<NxDistributerPurchaseBatchEntity> nxDisPurchaseBatchEntities;


	private List<NxDistributerServiceCityEntity> nxDistributerServiceCityEntities;
	private SysBusinessTypeEntity sysBusinessTypeEntity;
	private SysCityMarketEntity sysCityMarketEntity;
	private GbDepartmentEntity gbDepartmentEntity;
	private List<NxGoodsEntity> nxGoodsEntities;

	private NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity;

	private NxDistributerGoodsEntity nxDistributerGoodsEntity;
	private QyNxDisCorpEntity qyNxDisCorpEntity;

	private NxJrdhUserEntity sellerUser;
	private NxJrdhUserEntity nxDisBuyerUser;

	private NxDistributerFatherGoodsEntity linshiFather;

	private List<NxDistributerPayEntity> orderPayList;
	private List<NxDistributerPayEntity> liuliangPayList;
	private List<NxDistributerPayEntity> machinePayList;

	private List<GbDistributerGoodsEntity> gbDistributerGoods;

	private List<NxDistributerGoodsEntity> nxDistributerGoodsEntities;
	private List<GbDistributerFatherGoodsEntity> gbFatherGoodsEntities;
	private List<GbDistributerPurchaseGoodsEntity> gbDistributerPurchaseGoodsEntities;
	private List<GbDistributerPurchaseBatchEntity> gbDistributerPurchaseBatchEntities;
	private List<GbDepartmentBillEntity> gbDepartmentBillEntities;
	private Map<String,Object> aaa;



	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxDistributerEntity that = (NxDistributerEntity) o;
		return nxDistributerId.equals(that.getNxDistributerId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxDistributerId);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof NxDistributerEntity) {
			NxDistributerEntity e = (NxDistributerEntity) o;
			return e.getNxDistributerId().compareTo(this.nxDistributerId);
		}
		return 0;
	}
}

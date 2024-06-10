package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 11-30 15:31
 */

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxRestrauntEntity implements Serializable, Comparable  {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订货部门id
	 */
	private Integer nxRestrauntId;
	/**
	 *  订货部门名称
	 */
	private String nxRestrauntName;
	/**
	 *  订货部门上级id
	 */
	private Integer nxRestrauntFatherId;
	/**
	 *  订货部门类型
	 */
	private String nxRestrauntType;
	/**
	 *  订货部门子部门数量
	 */
	private Integer nxRestrauntSubAmount;
	/**
	 *  订货部门批发商id
	 */
	private Integer nxRestrauntComId;
	/**
	 *  
	 */
	private String nxRestrauntFilePath;
	/**
	 *  是客户吗
	 */
	private Integer nxRestrauntIsGroupDep;
	/**
	 *  
	 */
	private String nxRestrauntPrintName;
	/**
	 *  
	 */
	private Integer nxRestrauntShowWeeks;
	/**
	 *  
	 */
	private Integer nxRestrauntSettleType;


	/**
	 *  客户简称
	 */
	private String nxRestrauntAttrName;
	private String nxRestrauntLat;
	private String nxRestrauntLng;
	private String nxRestrauntAddress;
	private String nxRestrauntNavigationAddress;
	private String nxRestrauntNumber;
	private Integer nxRestrauntServiceLevel;
	private Integer nxRestrauntDriverId;
	private Integer nxRestrauntOweBoxNumber;
	private Integer nxRestrauntDeliveryBoxNumber;
	private Integer nxRestrauntWorkingStatus;
	private String nxRestrauntMinTime;
	private String nxRestrauntMaxTime;
	private String nxRestrauntDeliveryCost;
	private String nxRestrauntDeliveryLimit;
	private Integer nxRestrauntClickCount;
	private Integer nxRestrauntAddCount;
	private String nxRestrauntDeliveryDate;

	private String nxRestrauntPayTotal;
	private String nxRestrauntProfitTotal;
	private String nxRestrauntProfitPercent;


	private String duration;
	private String distance;
	private NxRestrauntUserEntity nxRestrauntUserEntity;
	private List<NxRestrauntUserEntity> nxRestrauntUserEntities;

	private List<NxRestrauntEntity> nxRestrauntEntities;
	private List<NxRestrauntEntity> nxSubRestraunts;

	private NxRestrauntUserEntity fathernxRestraunt;
	private NxCommunityEntity nxCommunityEntity;
	private List<NxRestrauntOrdersEntity> nxRestrauntOrdersEntities;

	private List<NxCommunityGoodsEntity> nxCommunityGoodsEntities;
	private NxRestrauntComGoodsEntity nxRestrauntComGoodsEntity;

	private Boolean isSelected = false;
	private String nxRestrauntUnPayTotal;
	private Integer todayAmountTotal;
	private Integer zeroStatusAmount;
	private Integer oneStatusAmount;
	private Integer twoStatusAmount;
	private String twoStatusSubTotal;
	private String todayProfitScale;

	private NxCommunityUserEntity nxCommunityUserEntity;
	private List<NxCommunityFatherGoodsEntity> nxCommunityFatherGoodsEntities;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxRestrauntEntity that = (NxRestrauntEntity) o;
		return Objects.equals(nxRestrauntId, that.nxRestrauntId) &&
				Objects.equals(nxRestrauntName, that.nxRestrauntName) &&
				Objects.equals(nxRestrauntFatherId, that.nxRestrauntFatherId) &&
				Objects.equals(nxRestrauntType, that.nxRestrauntType) &&
				Objects.equals(nxRestrauntSubAmount, that.nxRestrauntSubAmount) &&
				Objects.equals(nxRestrauntComId, that.nxRestrauntComId) &&
				Objects.equals(nxRestrauntFilePath, that.nxRestrauntFilePath) &&
				Objects.equals(nxRestrauntIsGroupDep, that.nxRestrauntIsGroupDep) &&
				Objects.equals(nxRestrauntPrintName, that.nxRestrauntPrintName) &&
				Objects.equals(nxRestrauntShowWeeks, that.nxRestrauntShowWeeks) &&
				Objects.equals(nxRestrauntSettleType, that.nxRestrauntSettleType) &&
				Objects.equals(nxRestrauntAttrName, that.nxRestrauntAttrName) &&
				Objects.equals(nxRestrauntUserEntity, that.nxRestrauntUserEntity) &&
				Objects.equals(nxRestrauntEntities, that.nxRestrauntEntities) &&
				Objects.equals(fathernxRestraunt, that.fathernxRestraunt) &&
				Objects.equals(nxCommunityEntity, that.nxCommunityEntity) &&
				Objects.equals(nxRestrauntOrdersEntities, that.nxRestrauntOrdersEntities);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxRestrauntId, nxRestrauntName, nxRestrauntFatherId, nxRestrauntType, nxRestrauntSubAmount, nxRestrauntComId, nxRestrauntFilePath, nxRestrauntIsGroupDep, nxRestrauntPrintName, nxRestrauntShowWeeks, nxRestrauntSettleType, nxRestrauntAttrName, nxRestrauntUserEntity, nxRestrauntEntities, fathernxRestraunt, nxCommunityEntity, nxRestrauntOrdersEntities);
	}

	@Override
	public int compareTo(Object o) {

		if (o instanceof NxRestrauntEntity) {
			NxRestrauntEntity e = (NxRestrauntEntity) o;
			return this.nxRestrauntId.compareTo(e.nxRestrauntId);
		}
		return 0;
	}
}

package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 01-17 07:54
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityPurchaseBatchEntity implements Serializable,Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  区域商进货批次id
	 */
	private Integer nxCommunityPurchaseBatchId;
	/**
	 *  区域商进货批次状态
	 */
	private Integer nxCpbStatus;
	/**
	 *  区域商复制=2，打印=1
	 */
	private Integer nxCpbPurchaseType;
	/**
	 *  区域商进货批次时间
	 */
	private String nxCpbTime;
	/**
	 *  区域商进货采购员id
	 */
	private Integer nxCpbPurUserId;
	/**
	 *  区域商id
	 */
	private Integer nxCpbCommunityId;
	/**
	 *  日期
	 */
	private String nxCpbDate;
	/**
	 *  时间
	 */
	private String nxCpbHour;
	/**
	 *  分钟
	 */
	private String nxCpbMinute;
	private Integer nxCpbDistributerId;
	private Integer nxCpbComSellType;
	private String nxCpbSubtotal;
	private Integer nxCpbBuyUserId;
	private Integer nxCpbSellUserId;
	private Integer nxCpbComSupplierId;

	private NxCommunityPurchaseGoodsEntity nxCommunityPurchaseGoodsEntity;
	private List<NxCommunityPurchaseGoodsEntity> nxCommunityPurchaseGoodsEntities;
	private List<NxCommunityGoodsEntity> nxCommunityGoodsEntities;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxCommunityPurchaseBatchEntity that = (NxCommunityPurchaseBatchEntity) o;
		return Objects.equals(nxCommunityPurchaseBatchId, that.nxCommunityPurchaseBatchId) &&
				Objects.equals(nxCpbStatus, that.nxCpbStatus) &&
				Objects.equals(nxCpbPurchaseType, that.nxCpbPurchaseType) &&
				Objects.equals(nxCpbTime, that.nxCpbTime) &&
				Objects.equals(nxCpbPurUserId, that.nxCpbPurUserId) &&
				Objects.equals(nxCpbCommunityId, that.nxCpbCommunityId) &&
				Objects.equals(nxCpbDate, that.nxCpbDate) &&
				Objects.equals(nxCpbHour, that.nxCpbHour) &&
				Objects.equals(nxCpbMinute, that.nxCpbMinute) &&
				Objects.equals(nxCommunityPurchaseGoodsEntities, that.nxCommunityPurchaseGoodsEntities);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxCommunityPurchaseBatchId, nxCpbStatus, nxCpbPurchaseType, nxCpbTime, nxCpbPurUserId, nxCpbCommunityId, nxCpbDate, nxCpbHour, nxCpbMinute, nxCommunityPurchaseGoodsEntities);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof NxCommunityPurchaseBatchEntity) {
			NxCommunityPurchaseBatchEntity e = (NxCommunityPurchaseBatchEntity) o;
			return this.nxCommunityPurchaseBatchId.compareTo(e.nxCommunityPurchaseBatchId);
		}
		return 0;
	}
}

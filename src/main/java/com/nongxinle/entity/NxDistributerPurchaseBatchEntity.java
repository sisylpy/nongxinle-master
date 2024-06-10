package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-25 22:52
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import jdk.nashorn.internal.ir.IndexNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerPurchaseBatchEntity implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商进货批次id
	 */
	private Integer nxDistributerPurchaseBatchId;
	/**
	 *  批发商进货批次状态
	 */
	private Integer nxDpbStatus;
	/**
	 *  批发商进货批次类型
	 */
	private Integer nxDpbPayType;
	/**
	 *  批发商进货批次时间
	 */
	private String nxDpbTime;
	/**
	 *  批发商进货采购员id
	 */
	private Integer nxDpbPurUserId;

	private Integer nxDpbDistributerId;

	private String nxDpbDate;
	private String nxDpbNeedDate;
	private String nxDpbYear;
	private String nxDpbMonth;
	private String nxDpbSellSubtotal;
	private String nxDpbPayFullTime;

	private Integer nxDpbBuyUserId;
	private Integer nxDpbSellUserId;

	private String groupDate;
	private Integer nxDpbPurchaseType;

	private List<NxDistributerPurchaseGoodsEntity> nxDPGEntities;
	private NxDistributerUserEntity nxJrdhPurEntity;
	private NxJrdhUserEntity nxJrdhBuyerEntity;
	private NxJrdhUserEntity nxJrdhSellerEntity;
	private NxJrdhSupplierEntity nxJrdhSupplierEntity;
	private Integer nxDpbOrderIsNotice;
	private Integer nxDpbSupplierId;
	private String nxDpbPruchaseWeek;
	private String nxDpbBuyUserOpenId;
	private String nxDpbSellUserOpenId;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxDistributerPurchaseBatchEntity entity = (NxDistributerPurchaseBatchEntity) o;
		return Objects.equals(nxDistributerPurchaseBatchId, entity.nxDistributerPurchaseBatchId) &&
				Objects.equals(nxDpbStatus, entity.nxDpbStatus) &&
				Objects.equals(nxDpbPayType, entity.nxDpbPayType) &&
				Objects.equals(nxDpbTime, entity.nxDpbTime) &&
				Objects.equals(nxDpbPurUserId, entity.nxDpbPurUserId) &&
				Objects.equals(nxDpbDistributerId, entity.nxDpbDistributerId) &&
				Objects.equals(nxDpbDate, entity.nxDpbDate) &&
				Objects.equals(nxDpbYear, entity.nxDpbYear) &&
				Objects.equals(nxDpbMonth, entity.nxDpbMonth) &&
				Objects.equals(groupDate, entity.groupDate) &&
				Objects.equals(nxDPGEntities, entity.nxDPGEntities);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxDistributerPurchaseBatchId, nxDpbStatus, nxDpbPayType, nxDpbTime, nxDpbPurUserId, nxDpbDistributerId, nxDpbDate, nxDpbYear, nxDpbMonth, groupDate, nxDPGEntities);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof NxCommunityGoodsEntity) {
			NxDistributerPurchaseBatchEntity e = (NxDistributerPurchaseBatchEntity) o;
			return this.nxDistributerPurchaseBatchId.compareTo(e.nxDistributerPurchaseBatchId);
		}
		return 0;
	}
}

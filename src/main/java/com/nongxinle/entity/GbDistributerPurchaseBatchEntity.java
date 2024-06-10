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

public class GbDistributerPurchaseBatchEntity implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商进货批次id
	 */
	private Integer gbDistributerPurchaseBatchId;
	/**
	 *  批发商进货批次状态
	 */
	private Integer gbDpbStatus;
	/**
	 *  批发商进货批次类型
	 */
	private Integer gbDpbUserAdminType;
	/**
	 *  批发商进货批次时间
	 */
	private String gbDpbTime;
	/**
	 *  批发商进货采购员id
	 */
	private Integer gbDpbPurUserId;

	private Integer gbDpbDistributerId;

	private String gbDpbDate;
	private String gbDpbHour;
	private String gbDpbMinute;
	private String gbDpbSubtotal;

	private Integer gbDpbSupplierUserId;

	private String groupDate;
	private Integer gbDpbPurDepartmentId;
	private Integer gbDpbPayType;
	private String gbDpbPaySubtotal;
	private Integer gbDpbSupplierId;
	private String gbDpbPurchaseMonth;
	private String gbDpbPurchaseWeek;
	private String gbDpbPurchaseYear;
	private String gbDpbPurchaseFullTime;
	private String gbDpbSellerReplyFullTime;
	private String gbDpbFinishFullTime;
	private String gbDpbBuyUserOpenId;
	private String gbDpbSellUserOpenId;

	private Integer gbDpbGbSupplierPaymentId;
	private Integer gbDpbBuyUserId;
	private Integer gbDpbSellUserId;
	private Integer gbDpbPurchaseType;



	private List<GbDistributerPurchaseBatchEntity> gbDPBEntities1;
	private List<GbDistributerPurchaseGoodsEntity> gbDPGEntities;
	private GbDistributerEntity gbDistributerEntity;
	private GbDepartmentUserEntity nxJrdhPurEntity;
	private NxJrdhUserEntity nxJrdhBuyerEntity;
	private NxJrdhUserEntity nxJrdhSellerEntity;
	private NxJrdhSupplierEntity nxJrdhSupplierEntity;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GbDistributerPurchaseBatchEntity entity = (GbDistributerPurchaseBatchEntity) o;
		return Objects.equals(gbDistributerPurchaseBatchId, entity.gbDistributerPurchaseBatchId) &&
				Objects.equals(gbDpbStatus, entity.gbDpbStatus) &&
				Objects.equals(gbDpbUserAdminType, entity.gbDpbUserAdminType) &&
				Objects.equals(gbDpbTime, entity.gbDpbTime) &&
				Objects.equals(gbDpbPurUserId, entity.gbDpbPurUserId) &&
				Objects.equals(gbDpbDistributerId, entity.gbDpbDistributerId) &&
				Objects.equals(gbDpbDate, entity.gbDpbDate) &&
				Objects.equals(gbDpbHour, entity.gbDpbHour) &&
				Objects.equals(gbDpbMinute, entity.gbDpbMinute) &&
				Objects.equals(groupDate, entity.groupDate) &&
				Objects.equals(gbDPGEntities, entity.gbDPGEntities);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gbDistributerPurchaseBatchId, gbDpbStatus, gbDpbUserAdminType, gbDpbTime, gbDpbPurUserId, gbDpbDistributerId, gbDpbDate, gbDpbHour, gbDpbMinute, groupDate, gbDPGEntities);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof GbDistributerPurchaseBatchEntity) {
			GbDistributerPurchaseBatchEntity e = (GbDistributerPurchaseBatchEntity) o;
			return this.gbDistributerPurchaseBatchId.compareTo(e.gbDistributerPurchaseBatchId);
		}
		return 0;
	}
}

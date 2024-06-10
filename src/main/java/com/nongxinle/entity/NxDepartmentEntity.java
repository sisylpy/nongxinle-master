package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-16 11:26
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDepartmentEntity implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订货部门id
	 */
	private Integer nxDepartmentId;
	/**
	 *  订货部门名称
	 */
	private String nxDepartmentName;
	/**
	 *  订货部门上级id
	 */
	private Integer nxDepartmentFatherId;
	/**
	 *  订货部门类型
	 */
	private String nxDepartmentType;
	/**
	 *  订货部门子部门数量
	 */
	private Integer nxDepartmentSubAmount;

	private String nxDepartmentFilePath;

	private Boolean isSelected;


	private Integer nxDepUserId;

	private Integer nxDepartmentDisId;

	private Integer nxDepartmentIsGroupDep;

	private String  nxDepartmentPrintName;

	private Integer nxDepartmentShowWeeks;

	private Integer nxDepartmentSettleType;

	private String nxDepartmentAttrName;
	private Integer nxDepartmentPromotionGoodsId;

	private Integer nxDepartmentDisRouteId;

	private Integer nxDepartmentDriverId;
	private Integer nxDepartmentOweBoxNumber;
	private Integer nxDepartmentDeliveryBoxNumber;
	private Integer nxDepartmentWorkingStatus;
	private String nxDepartmentUnPayTotal;
	private Integer nxDepartmentAddCount;
	private Integer nxDepartmentPurOrderCount;
	private Integer nxDepartmentNeedNotPurOrderCount;
	private String nxDepartmentPayTotal;
	private String nxDepartmentProfitTotal;



	private NxDepartmentEntity fatherDepartmentEntity;

	private NxDepartmentUserEntity nxDepartmentUserEntity;
	
	private List<NxDepartmentUserEntity>  nxDepartmentUserEntities;

	private List<NxDepartmentEntity> nxDepartmentEntities;
	private List<NxDepartmentEntity> nxSubDepartments;

	private List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;

	private NxDistributerEntity nxDistributerEntity;
	private List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntities;
	private NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity;
	private List<NxDistributerFatherGoodsEntity>  nxDisFatherGoodsEntities;




	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxDepartmentEntity that = (NxDepartmentEntity) o;
		return Objects.equals(nxDepartmentId, that.nxDepartmentId) &&
				Objects.equals(nxDepartmentName, that.nxDepartmentName) &&
				Objects.equals(nxDepartmentFatherId, that.nxDepartmentFatherId) &&
				Objects.equals(nxDepartmentType, that.nxDepartmentType) &&
				Objects.equals(nxDepartmentSubAmount, that.nxDepartmentSubAmount) &&
				Objects.equals(nxDepartmentFilePath, that.nxDepartmentFilePath) &&
				Objects.equals(isSelected, that.isSelected) &&
				Objects.equals(nxDepUserId, that.nxDepUserId) &&
				Objects.equals(nxDepartmentDisId, that.nxDepartmentDisId) &&
				Objects.equals(fatherDepartmentEntity, that.fatherDepartmentEntity) &&
				Objects.equals(nxDepartmentUserEntity, that.nxDepartmentUserEntity) &&
				Objects.equals(nxDepartmentUserEntities, that.nxDepartmentUserEntities) &&
				Objects.equals(nxDepartmentEntities, that.nxDepartmentEntities) &&
				Objects.equals(nxDepartmentOrdersEntities, that.nxDepartmentOrdersEntities);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxDepartmentId, nxDepartmentName, nxDepartmentFatherId, nxDepartmentType, nxDepartmentSubAmount, nxDepartmentFilePath, isSelected, nxDepUserId, nxDepartmentDisId, fatherDepartmentEntity, nxDepartmentUserEntity, nxDepartmentUserEntities, nxDepartmentEntities, nxDepartmentOrdersEntities);
	}

	@Override
	public int compareTo(Object o) {

		if (o instanceof NxDepartmentEntity) {
			NxDepartmentEntity e = (NxDepartmentEntity) o;
			return this.nxDepartmentId.compareTo(e.nxDepartmentId);
		}
		return 0;
	}
}

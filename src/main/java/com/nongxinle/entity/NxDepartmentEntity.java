package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-16 11:26
 */

import java.io.Serializable;
import java.sql.Timestamp;
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
	private String distance;
	private String duration;
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
	private Integer nxDepartmentRecordMinutes;

	private Integer nxDepartmentDriverId;
	private Integer nxDepartmentOweBoxNumber;
	private Integer nxDepartmentDeliveryBoxNumber;
	private Integer nxDepartmentWorkingStatus;
	private String nxDepartmentUnPayTotal;
	private Integer nxDepartmentAddCount;
	private Integer nxDepartmentPurOrderCount;
	private Integer nxDepartmentNeedNotPurOrderCount;
	private Integer nxDepartmentOrderTotal;
	private String nxDepartmentPayTotal;
	private String nxDepartmentJoinDate;
	private String nxDepartmentProfitTotal;

	private String nxDepartmentPinyin;
	private String nxDepartmentAddress;
	private String nxDepartmentAppId;
	private String nxDepartmentPickName;
	/**
	 * 订货代号（用于对客户名称保密）
	 */
	private String nxDepartmentOrderCode;
	private String nxDepartmentLat;
	private String nxDepartmentLng;

	private Integer nxDepartmentEarliestDeliveryTime;
	private Integer nxDepartmentLatestDeliveryTime;
	private Integer nxDepartmentUnloadDuration;
	private Integer nxDepartmentGbDistributerId;
	/**
	 *  部门积分
	 */
	private String nxDepartmentPoints;
	/**
	 *  等待积分（客户货品卖出前的积分，卖出后加到积分字段）
	 */
	private String nxDepartmentWaitingPoints;
	
	/**
	 * OCR 修正指令 - 图片上传（用于存储用户的修正要求，针对图片识别）
	 */
	private String nxDepartmentOcrPromptImage;
	
	/**
	 * OCR 修正指令 - Excel上传（用于存储用户的修正要求，针对Excel识别）
	 */
	private String nxDepartmentOcrPromptExcel;
	
	/**
	 * OCR 修正指令 - 复制粘贴（用于存储用户的修正要求，针对粘贴识别）
	 */
	private String nxDepartmentOcrPromptPaste;

	private NxDepartmentEntity fatherDepartmentEntity;
	private NxDistributerPayEntity payEntity;

	private NxDepartmentUserEntity nxDepartmentUserEntity;
	
	private List<NxDepartmentUserEntity>  nxDepartmentUserEntities;

	private List<NxDepartmentEntity> nxDepartmentEntities;
	private List<NxDepartmentEntity> nxSubDepartments;

	private List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;

	private NxDistributerEntity nxDistributerEntity;
	private List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntities;
	private NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity;
	private List<NxDistributerFatherGoodsEntity>  nxDisFatherGoodsEntities;
	private List<NxDistributerLabelEntity> nxDistributerLabelEntities;

	private int taskCount;


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

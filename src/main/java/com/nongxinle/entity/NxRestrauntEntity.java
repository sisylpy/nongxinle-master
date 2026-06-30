package com.nongxinle.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Legacy Restraunt entity stub kept for MyBatis resultMap type aliases.
 */
@Setter
@Getter
@ToString
public class NxRestrauntEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer nxRestrauntId;
	private String nxRestrauntName;
	private Integer nxRestrauntFatherId;
	private String nxRestrauntType;
	private Integer nxRestrauntSubAmount;
	private Integer nxRestrauntComId;
	private String nxRestrauntFilePath;
	private Integer nxRestrauntIsGroupDep;
	private String nxRestrauntPrintName;
	private Integer nxRestrauntShowWeeks;
	private Integer nxRestrauntSettleType;
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
	private String nxRestrauntUnPayTotal;

	private NxCommunityEntity nxCommunityEntity;
	private NxCommunityUserEntity nxCommunityUserEntity;
}

package com.nongxinle.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Legacy Restraunt orders entity stub kept for MyBatis resultMap type aliases.
 */
@Setter
@Getter
@ToString
public class NxRestrauntOrdersEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer nxRestrauntOrdersId;
	private Integer nxRoNxGoodsId;
	private Integer nxRoNxGoodsFatherId;
	private Integer nxRoComGoodsId;
	private Integer nxRoComGoodsFatherId;
	private Integer nxRoResComGoodsId;
	private String nxRoResComGoodsPrice;
	private String nxRoQuantity;
	private String nxRoStandard;
	private String nxRoRemark;
	private String nxRoWeight;
	private String nxRoPrice;
	private String nxRoSubtotal;
	private Integer nxRoRestrauntId;
	private Integer nxRoRestrauntFatherId;
	private Integer nxRoCommunityId;
	private Integer nxRoPurchaseUserId;
	private Integer nxRoBillId;
	private Integer nxRoStatus;
	private Integer nxRoOrderUserId;
	private Integer nxRoPickUserId;
	private Integer nxRoAccountUserId;
	private Integer nxRoBuyStatus;
	private String nxRoApplyDate;
	private String nxRoArriveDate;
	private Integer nxRoPurchaseGoodsId;

	private NxCommunityGoodsEntity nxCommunityGoodsEntity;
	private NxRestrauntEntity nxRestrauntEntity;
}

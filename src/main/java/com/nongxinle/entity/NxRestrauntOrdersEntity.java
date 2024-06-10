package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 12-01 07:19
 */

import java.io.Serializable;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxRestrauntOrdersEntity implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  饭馆订单id
	 */
	private Integer nxRestrauntOrdersId;
	/**
	 *  饭馆订单nx商品id
	 */
	private Integer nxRoNxGoodsId;
	/**
	 *  饭馆订单商品父id
	 */
	private Integer nxRoNxGoodsFatherId;
	/**
	 *  饭馆区域商品id
	 */
	private Integer nxRoComGoodsId;
	/**
	 *  区域父级商品id
	 */
	private Integer nxRoComGoodsFatherId;
	/**
	 *  饭馆id
	 */
	private Integer nxRoResComGoodsId;
	/**
	 *  饭馆商品价格
	 */
	private String nxRoResComGoodsPrice;
	/**
	 *  部门订单申请数量
	 */
	private String nxRoQuantity;
	/**
	 *  部门订单申请规格
	 */
	private String nxRoStandard;
	/**
	 *  部门订单申请备注
	 */
	private String nxRoRemark;
	/**
	 *  部门订单重量
	 */
	private String nxRoWeight;
	/**
	 *  部门订单商品单价
	 */
	private String nxRoPrice;
	/**
	 *  部门订单申请商品小计
	 */
	private String nxRoSubtotal;
	/**
	 *  部门订单部门id
	 */
	private Integer nxRoRestrauntId;
	/**
	 *  
	 */
	private Integer nxRoRestrauntFatherId;
	/**
	 *  部门订单批发商id
	 */
	private Integer nxRoCommunityId;
	/**
	 *  部门商品采购员id
	 */
	private Integer nxRoPurchaseUserId;
	/**
	 *  部门订单账单id
	 */
	private Integer nxRoBillId;
	/**
	 *  部门订单申请商品状态
	 */
	private Integer nxRoStatus;
	/**
	 *  部门订单订货用户id
	 */
	private Integer nxRoOrderUserId;
	/**
	 *  部门订单商品称重用户id
	 */
	private Integer nxRoPickUserId;
	/**
	 *  部门订单商品输入单价用户id
	 */
	private Integer nxRoAccountUserId;
	/**
	 *  部门订单商品进货状态
	 */
	private Integer nxRoBuyStatus;
	/**
	 *  部门订单申请时间
	 */
	private String nxRoApplyDate;
	/**
	 *  部门订单送达时间
	 */
	private String nxRoArriveDate;
	/**
	 *  订单采购商品id
	 */
	private Integer nxRoPurchaseGoodsId;
	/**
	 *  
	 */
	private String nxRoArriveOnlyDate;
	/**
	 *  
	 */
	private String nxRoApplyFullTime;
	/**
	 *  
	 */
	private String nxRoOperationTime;
	/**
	 *  星期几
	 */
	private String nxRoArriveWhatDay;
	/**
	 *  
	 */
	private Integer nxRoIsAgent;
	/**
	 *  本年第几周
	 */
	private Integer nxRoArriveWeeksYear;
	/**
	 *  
	 */
	private String nxRoReceiveFullTime;

	private Integer nxRoSellType;
	private String nxRoExpectPrice;
	private String nxRoScale;
	private  String nxRoProfit;
	private String nxRoDifference;

	private String nxRoCostPrice;
	private String nxRoDeliveryDate;
	private Integer nxRoOrderRank;
	private Integer nxRoPrintTimes;
	private Integer nxRoComDistributerId;
	private Integer nxRoComDistributerGoodsId;
	private Integer nxRoComDistributerOrderId;
	private String nxRoCostSubtotal;
	private String nxRoCostPercent;


	private Integer nxRoComGoodsStandardType;
	private Integer nxRoComStandardId;
	private String nxRoComStandardName;
	private String nxRoComStandardScale;
	private String nxRoComStandardQuantity;
	private NxRestrauntUserEntity nxRestrauntUserEntity;
	private NxCommunityGoodsEntity nxCommunityGoodsEntity;
	private NxRestrauntComGoodsEntity nxRestrauntComGoodsEntity;
	private NxRestrauntEntity nxRestrauntEntity;
	private String nxRoOrderPrice;
	private String nxRoArriveMinTime;
	private String nxRoArriveMaxTime;

//	private NxRestrauntOrdersEntity nxRestrauntOrdersEntity;

	private Boolean showDate = true;

	private Boolean  isWeeks = true;
	private Boolean hasChoice =  false;
	private Boolean nxRoPrintSelected = true;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxRestrauntOrdersEntity that = (NxRestrauntOrdersEntity) o;
		return Objects.equals(nxRestrauntOrdersId, that.nxRestrauntOrdersId) &&
				Objects.equals(nxRoNxGoodsId, that.nxRoNxGoodsId) &&
				Objects.equals(nxRoNxGoodsFatherId, that.nxRoNxGoodsFatherId) &&
				Objects.equals(nxRoComGoodsId, that.nxRoComGoodsId) &&
				Objects.equals(nxRoComGoodsFatherId, that.nxRoComGoodsFatherId) &&
				Objects.equals(nxRoResComGoodsId, that.nxRoResComGoodsId) &&
				Objects.equals(nxRoResComGoodsPrice, that.nxRoResComGoodsPrice) &&
				Objects.equals(nxRoQuantity, that.nxRoQuantity) &&
				Objects.equals(nxRoStandard, that.nxRoStandard) &&
				Objects.equals(nxRoRemark, that.nxRoRemark) &&
				Objects.equals(nxRoWeight, that.nxRoWeight) &&
				Objects.equals(nxRoPrice, that.nxRoPrice) &&
				Objects.equals(nxRoSubtotal, that.nxRoSubtotal) &&
				Objects.equals(nxRoRestrauntId, that.nxRoRestrauntId) &&
				Objects.equals(nxRoRestrauntFatherId, that.nxRoRestrauntFatherId) &&
				Objects.equals(nxRoCommunityId, that.nxRoCommunityId) &&
				Objects.equals(nxRoPurchaseUserId, that.nxRoPurchaseUserId) &&
				Objects.equals(nxRoBillId, that.nxRoBillId) &&
				Objects.equals(nxRoStatus, that.nxRoStatus) &&
				Objects.equals(nxRoOrderUserId, that.nxRoOrderUserId) &&
				Objects.equals(nxRoPickUserId, that.nxRoPickUserId) &&
				Objects.equals(nxRoAccountUserId, that.nxRoAccountUserId) &&
				Objects.equals(nxRoBuyStatus, that.nxRoBuyStatus) &&
				Objects.equals(nxRoApplyDate, that.nxRoApplyDate) &&
				Objects.equals(nxRoArriveDate, that.nxRoArriveDate) &&
				Objects.equals(nxRoPurchaseGoodsId, that.nxRoPurchaseGoodsId) &&
				Objects.equals(nxRoArriveOnlyDate, that.nxRoArriveOnlyDate) &&
				Objects.equals(nxRoApplyFullTime, that.nxRoApplyFullTime) &&
				Objects.equals(nxRoOperationTime, that.nxRoOperationTime) &&
				Objects.equals(nxRoArriveWhatDay, that.nxRoArriveWhatDay) &&
				Objects.equals(nxRoIsAgent, that.nxRoIsAgent) &&
				Objects.equals(nxRoArriveWeeksYear, that.nxRoArriveWeeksYear) &&
				Objects.equals(nxRoReceiveFullTime, that.nxRoReceiveFullTime) &&
				Objects.equals(nxRoSellType, that.nxRoSellType) &&
				Objects.equals(nxRoExpectPrice, that.nxRoExpectPrice) &&
				Objects.equals(nxRoScale, that.nxRoScale) &&
				Objects.equals(nxRoProfit, that.nxRoProfit) &&
				Objects.equals(nxRoDifference, that.nxRoDifference) &&
				Objects.equals(nxRoCostPrice, that.nxRoCostPrice) &&
				Objects.equals(nxRestrauntUserEntity, that.nxRestrauntUserEntity) &&
				Objects.equals(nxCommunityGoodsEntity, that.nxCommunityGoodsEntity) &&
				Objects.equals(nxRestrauntComGoodsEntity, that.nxRestrauntComGoodsEntity) &&
				Objects.equals(nxRestrauntEntity, that.nxRestrauntEntity) &&
				Objects.equals(showDate, that.showDate) &&
				Objects.equals(isWeeks, that.isWeeks) &&
				Objects.equals(hasChoice, that.hasChoice);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxRestrauntOrdersId, nxRoNxGoodsId, nxRoNxGoodsFatherId, nxRoComGoodsId, nxRoComGoodsFatherId, nxRoResComGoodsId, nxRoResComGoodsPrice, nxRoQuantity, nxRoStandard, nxRoRemark, nxRoWeight, nxRoPrice, nxRoSubtotal, nxRoRestrauntId, nxRoRestrauntFatherId, nxRoCommunityId, nxRoPurchaseUserId, nxRoBillId, nxRoStatus, nxRoOrderUserId, nxRoPickUserId, nxRoAccountUserId, nxRoBuyStatus, nxRoApplyDate, nxRoArriveDate, nxRoPurchaseGoodsId, nxRoArriveOnlyDate, nxRoApplyFullTime, nxRoOperationTime, nxRoArriveWhatDay, nxRoIsAgent, nxRoArriveWeeksYear, nxRoReceiveFullTime, nxRoSellType, nxRoExpectPrice, nxRoScale, nxRoProfit, nxRoDifference, nxRoCostPrice, nxRestrauntUserEntity, nxCommunityGoodsEntity, nxRestrauntComGoodsEntity, nxRestrauntEntity, showDate, isWeeks, hasChoice);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof NxRestrauntOrdersEntity) {
			NxRestrauntOrdersEntity e = (NxRestrauntOrdersEntity) o;
			return this.nxRoApplyDate.compareTo(e.nxRoApplyDate);
		}
		return 0;
	}
}

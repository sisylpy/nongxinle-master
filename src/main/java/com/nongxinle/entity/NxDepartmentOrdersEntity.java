package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-21 21:51
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString
public class NxDepartmentOrdersEntity implements Serializable, Comparable{
	private static final long serialVersionUID = 1L;
	
	/**
	 *  部门订单id
	 */
	private Integer nxDepartmentOrdersId;
	/**
	 *  部门订单nx商品id
	 */
	private Integer nxDoNxGoodsId;
	/**
	 *  部门订单商品父id
	 */
	private Integer nxDoNxGoodsFatherId;
	/**
	 *  部门订单社区商品id
	 */
	private Integer nxDoDisGoodsId;
	private Integer nxDoDisGoodsFatherId;

	private  Integer nxDoDepDisGoodsId;
	private String  nxDoDepDisGoodsPrice;

	/**
	 *  部门订单申请数量
	 */
	private String nxDoQuantity;
	/**
	 *  部门订单申请规格
	 */


	private String nxDoStandard;
	/**
	 *  部门订单申请备注
	 */
	private String nxDoRemark;
	/**
	 *  部门订单重量
	 */
	private String nxDoWeight;

	/**
	 *  部门订单商品单价
	 */
	private String nxDoPrice;
	/**
	 *  部门订单申请商品小计
	 */
	private String nxDoSubtotal;
	/**
	 *  部门订单部门id
	 */
	private Integer nxDoDepartmentId;

	private Integer nxDoDepartmentFatherId;
	private Integer nxDoDisGoodsGrandId;
	/**
	 *  部门订单批发商id
	 */
	private Integer nxDoDistributerId;
	/**
	 *  部门订单账单id
	 */
	private Integer nxDoBillId;
	/**
	 *  部门订单申请商品状态
	 */
	private Integer nxDoStatus;
	/**
	 *  部门订单订货用户id
	 */
	private Integer nxDoOrderUserId;
	/**
	 *  部门订单商品称重用户id
	 */
	private Integer nxDoPickUserId;
	/**
	 *  部门订单商品输入单价用户id
	 */
	private Integer nxDoAccountUserId;
	/**
	 *  部门商品采购员id
	 */
	private Integer nxDoPurchaseUserId;
	/**
	 *  部门订单商品进货状态
	 */
	private Integer nxDoPurchaseStatus;
	/**
	 *  部门订单申请时间
	 */
	private String nxDoApplyDate;
	private String nxDoApplyFullTime;
	private String nxDoGoodsName;

	/**
	 *  部门订单送达时间
	 */
	private String nxDoArriveOnlyDate;
	private String nxDoArriveDate;
	private Integer nxDoArriveWeeksYear;

	/**
	 * 采购商品id
	 */
	private Integer nxDoPurchaseGoodsId;


	private String nxDoOperationTime;

	private String nxDoArriveWhatDay;

	private Integer nxDoIsAgent;

	private String nxDoApplyOnlyTime;
	private String nxDoCostPrice;
	private String nxDoCostSubtotal;
	private String nxDoPrintStandard;
	private String nxDoExpcetPrice;

	private String nxDoReturnWeight;
	private String nxDoReturnSubtotal;
	private Integer nxDoReturnBillId;
	private Integer nxDoReturnStatus;

	private Integer nxDoGbDistributerId;
	private Integer nxDoGbDepartmentId;
	private Integer nxDoGbDepartmentFatherId;
	private Integer nxDoWeightId;
	private Integer nxDoNxCommunityId;
	private Integer nxDoNxCommRestrauntId;
	private Integer nxDoNxCommRestrauntFatherId;
	private Integer nxDoGbDepartmentOrderId;
	private Integer nxDoNxRestrauntOrderId;
	private Integer nxDoGoodsType;
	private Integer nxDoTodayOrder;
	private String nxDoPriceDifferent;
	private String nxDoProfitSubtotal;
	private String nxDoProfitScale;
	private String nxDoCostPriceUpdate;
	private String nxDoCostPriceLevel;



	private NxGoodsEntity nxGoodsEntity;
	private NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity;
    private NxDistributerGoodsEntity nxDistributerGoodsEntity;
	private NxDepartmentIndependentGoodsEntity nxDepIndependentGoodsEntity;

	private NxDistributerPurchaseGoodsEntity orderPurGoods;
	private NxDistributerPurchaseGoodsEntity purchaseGoodsEntity;

	private NxDepartmentUserEntity nxDepartmentUserEntity;
	private NxDistributerUserEntity nxAgentUserEntity;

	private NxDepartmentEntity nxDepartmentEntity;
	private GbDepartmentEntity gbDepartmentEntity;
	private NxRestrauntEntity nxRestrauntEntity;
	private NxDistributerWeightEntity nxDistributerWeightEntity;
	private GbDepartmentOrdersEntity gbDepartmentOrdersEntity;
	private List<NxDistributerGoodsEntity> nxDistributerGoodsEntityList;

//	private NxDepartmentGoodsEntity nxDepartmentGoodsEntity;

	private Boolean onFocus;

	private Boolean hasChoice =  false;

	private Boolean isNotice = false ;

	private Boolean showDate = true;

	private Boolean  isWeeks = true;

	private Boolean purSelected = true;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxDepartmentOrdersEntity that = (NxDepartmentOrdersEntity) o;
		return nxDoArriveDate.equals(that.nxDoArriveDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxDoArriveDate);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof NxDepartmentOrdersEntity) {
			NxDepartmentOrdersEntity e = (NxDepartmentOrdersEntity) o;
			return e.nxDoArriveDate.compareTo(this.nxDoArriveDate);

		}
		return 0;
	}
}

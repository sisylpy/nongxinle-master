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
public class GbDepartmentOrdersEntity implements Serializable, Comparable{
	private static final long serialVersionUID = 1L;
	
	/**
	 *  部门订单id
	 */
	private Integer gbDepartmentOrdersId;
	/**
	 *  部门订单gb商品id
	 */
	private Integer gbDoNxGoodsId;
	/**
	 *  部门订单商品父id
	 */
	private Integer gbDoNxGoodsFatherId;
	/**
	 *  部门订单社区商品id
	 */
	private Integer gbDoDisGoodsId;
	private Integer gbDoDisGoodsFatherId;

	private  Integer gbDoDepDisGoodsId;

	/**
	 *  部门订单申请数量
	 */
	private String gbDoQuantity;
	/**
	 *  部门订单申请规格
	 */
	private String gbDoStandard;
	/**
	 *  部门订单申请备注
	 */
	private String gbDoRemark;
	/**
	 *  部门订单重量
	 */
	private String gbDoWeight;
	/**
	 *  部门订单商品单价
	 */
	private String gbDoPrice;
	/**
	 *  部门订单申请商品小计
	 */
	private String gbDoSubtotal;
	/**
	 *  部门订单部门id
	 */
	private Integer gbDoDepartmentId;

	private Integer gbDoDepartmentFatherId;
	/**
	 *  部门订单批发商id
	 */
	private Integer gbDoDistributerId;
	/**
	 *  部门订单账单id
	 */
	private Integer gbDoBillId;
	/**
	 *  部门订单申请商品状态
	 */
	private Integer gbDoStatus;
	/**
	 *  部门订单订货用户id
	 */
	private Integer gbDoOrderUserId;
	/**
	 *  部门订单商品称重用户id
	 */
	private Integer gbDoPickUserId;
	/**
	 *  部门订单商品输入单价用户id
	 */
	private Integer gbDoReceiveUserId;
	/**
	 *  部门商品采购员id
	 */
	private Integer gbDoPurchaseUserId;
	/**
	 *  部门订单商品进货状态
	 */
	private Integer gbDoBuyStatus;
	/**
	 *  部门订单申请时间
	 */
	private String gbDoApplyDate;
	private String gbDoApplyWhatDay;
	private String gbDoApplyArriveDate;
	private String gbDoApplyFullTime;
	private String gbDoSellingPrice;
	private String gbDoSellingSubtotal;

	/**
	 *  部门订单送达时间
	 */
	private String gbDoArriveOnlyDate;
	private String gbDoArriveDate;
	private Integer gbDoArriveWeeksYear;

	/**
	 * 采购商品id
	 */
	private Integer gbDoPurchaseGoodsId;

	private Integer gbDoGoodsType;

	private String gbDoOperationTime;

	private String gbDoArriveWhatDay;

	private Integer gbDoIsAgent;

	private String gbDoApplyOnlyTime;
	private String gbDoCostPrice;
	private String gbDoCostWeight;
	private String gbDoCostSubtotal;
	private String gbDoPriceDifferent;


	private Integer gbDoNxDistributerId;
	private Integer gbDoNxDistributerGoodsId;
	private Integer gbDoNxDepartmentOrderId;
	private Integer gbDoToDepartmentId;
	private Integer gbDoOrderType;
	private Integer gbDoReturnUserId;
	private Integer gbDoDgsrReturnId;
	private Integer gbDoDsStandardId;
	private Integer gbDoWeightTotalId;
	private Integer gbDoWeightGoodsId;
	private String gbDoDsStandardScale;
	private String gbDoScaleWeight;
	private String gbDoScalePrice;


	private NxGoodsEntity nxGoodsEntity;
    private GbDistributerGoodsEntity gbDistributerGoodsEntity;
    private GbDepartmentDisGoodsEntity gbDepartmentDisGoodsEntity;
    private GbDistributerPurchaseGoodsEntity gbDistributerPurchaseGoodsEntity;

	private GbDepartmentUserEntity gbDepartmentUserEntity;
	private GbDepartmentEntity gbDepartmentEntity;
	private GbDepartmentEntity orderDepartment;
	private GbDepartmentUserEntity receiveUserEntity;
	private GbDepartmentUserEntity pickerUserEntity;


	private Boolean onFocus;

	private Boolean hasChoice =  true;

	private Boolean isNotice = false ;

	private Boolean showDate = true;

	private Boolean  isWeeks = true;


	private GbDepartmentEntity stockDepartment;
	private List<GbDepartmentGoodsStockEntity> goodsStockEntityList;
	private List<GbDepartmentGoodsStockEntity> outGoodsStockEntityList;

	private GbDepartmentGoodsStockEntity selfControlStockEntity;
	private NxDepartmentOrdersEntity nxDepartmentOrdersEntity;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GbDepartmentOrdersEntity that = (GbDepartmentOrdersEntity) o;
		return gbDoArriveDate.equals(that.gbDoArriveDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gbDoArriveDate);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof GbDepartmentOrdersEntity) {
			GbDepartmentOrdersEntity e = (GbDepartmentOrdersEntity) o;
			return e.gbDoArriveDate.compareTo(this.gbDoArriveDate);

		}
		return 0;
	}
}

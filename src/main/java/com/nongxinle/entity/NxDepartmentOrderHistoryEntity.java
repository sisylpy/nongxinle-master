package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-19 23:55
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDepartmentOrderHistoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
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
	/**
	 *  批发商父级商品id
	 */
	private Integer nxDoDisGoodsFatherId;
	/**
	 *  
	 */
	private Integer nxDoDepDisGoodsId;
	/**
	 *  部门商品价格
	 */
	private String nxDoDepDisGoodsPrice;
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
	 *  部门订单Kg重量（用于将斤的重量转换为Kg）
	 */
	private String nxDoWeightKg;
	/**
	 *  部门订单商品单价
	 */
	private String nxDoPrice;
	/**
	 *  部门订单商品Kg单价（用于将斤的单价转换为Kg）
	 */
	private String nxDoPriceKg;
	/**
	 *  部门订单申请商品小计
	 */
	private String nxDoSubtotal;
	/**
	 *  部门订单部门id
	 */
	private Integer nxDoDepartmentId;
	/**
	 *  
	 */
	private Integer nxDoDepartmentFatherId;
	/**
	 *  部门订单批发商id
	 */
	private Integer nxDoDistributerId;
	/**
	 *  部门商品采购员id
	 */
	private Integer nxDoPurchaseUserId;
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
	 *  部门订单商品进货状态
	 */
	private Integer nxDoPurchaseStatus;
	/**
	 *  部门订单申请时间
	 */
	private String nxDoApplyDate;
	/**
	 *  部门订单送达时间
	 */
	private String nxDoArriveDate;
	/**
	 *  订单采购商品id
	 */
	private Integer nxDoPurchaseGoodsId;
	/**
	 *  
	 */
	private String nxDoArriveOnlyDate;
	/**
	 *  
	 */
	private String nxDoApplyFullTime;
	/**
	 *  配送商品0，自采购商品1
	 */
	private Integer nxDoGoodsType;
	/**
	 *  
	 */
	private String nxDoOperationTime;
	/**
	 *  星期几
	 */
	private String nxDoArriveWhatDay;
	/**
	 *  配送商用户 id
	 */
	private Integer nxDoIsAgent;
	/**
	 *  本年第几周
	 */
	private Integer nxDoArriveWeeksYear;
	/**
	 *  
	 */
	private String nxDoApplyOnlyTime;
	/**
	 *  进货单价
	 */
	private String nxDoCostPrice;
	/**
	 *  进货小计
	 */
	private String nxDoCostSubtotal;
	/**
	 *  连锁采购id
	 */
	private Integer nxDoGbDistributerId;
	/**
	 *  连锁采购客户订货部门id
	 */
	private Integer nxDoGbDepartmentId;
	/**
	 *  连锁采购客户订货部门id
	 */
	private Integer nxDoGbDepartmentFatherId;
	/**
	 *  退货数量
	 */
	private String nxDoReturnWeight;
	/**
	 *  退货金额
	 */
	private String nxDoReturnSubtotal;
	/**
	 *  退货单号
	 */
	private Integer nxDoReturnBillId;
	/**
	 *  退货状态
	 */
	private Integer nxDoReturnStatus;
	/**
	 *  称重单号
	 */
	private Integer nxDoWeightId;
	/**
	 *  利润小计
	 */
	private String nxDoProfitSubtotal;
	/**
	 *  利润率
	 */
	private String nxDoProfitScale;
	/**
	 *  连锁采购id
	 */
	private Integer nxDoNxCommunityId;
	/**
	 *  连锁采购客户订货部门id
	 */
	private Integer nxDoNxCommRestrauntId;
	/**
	 *  连锁采购客户订货部门id
	 */
	private Integer nxDoNxCommRestrauntFatherId;
	/**
	 *  连锁采购订单id
	 */
	private Integer nxDoGbDepartmentOrderId;
	/**
	 *  饭馆订单id
	 */
	private Integer nxDoNxRestrauntOrderId;
	/**
	 *  进货单价更新日期
	 */
	private String nxDoCostPriceUpdate;
	/**
	 *  进货单价级别精选，优选，普通，单一
	 */
	private String nxDoCostPriceLevel;
	/**
	 *  批发商父级商品id
	 */
	private Integer nxDoDisGoodsGrandId;
	/**
	 *  部门订单申请规格
	 */
	private String nxDoPrintStandard;
	/**
	 *  订货顺序
	 */
	private Integer nxDoTodayOrder;
	/**
	 *  部门订单申请规格
	 */
	private String nxDoPriceDifferent;
	/**
	 *  现金单价
	 */
	private String nxDoExpectPrice;
	/**
	 *  商品名称
	 */
	private String nxDoGoodsName;
	/**
	 *  
	 */
	private Integer nxDoGbDepDisGoodsId;


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
	private List<NxDistributerGoodsShelfStockEntity> outStockDisGoodsShelfStockEntities;
	private TreeSet<NxGoodsEntity> nxGoodsEntities;
	
	/**
	 * 溯源报告信息
	 */
	private NxTraceReportEntity nxTraceReportEntity;
	
	/**
	 * 货架库存信息
	 */
	private NxDistributerGoodsShelfStockEntity shelfStockEntity;

}

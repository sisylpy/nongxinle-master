package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-25 10:39
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunitySplicingOrdersEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订单id
	 */
	private Integer nxCommunitySplicingOrdersId;
	/**
	 *  批发商id
	 */
	private Integer nxCsoCoOrderId;
	/**
	 *  订单社区id
	 */
	private Integer nxCsoCommunityId;
	/**
	 *  订单客户id
	 */
	private Integer nxCsoCustomerId;
	/**
	 *  订单用户id
	 */
	private Integer nxCsoUserId;
	/**
	 *  订单日期
	 */
	private String nxCsoDate;
	/**
	 *  订单状态
	 */
	private Integer nxCsoStatus;
	/**
	 *  订单送达时间
	 */
	private String nxCsoService;
	/**
	 *  订单总金额
	 */
	private Float nxCsoAmount;
	/**
	 *  订单送到日期
	 */
	private String nxCsoServiceDate;
	/**
	 *  订单送到时间
	 */
	private String nxCsoServiceTime;
	/**
	 *  订单称重用户id
	 */
	private Integer nxCsoWeighUserId;
	/**
	 *  订单配送员工id
	 */
	private Integer nxCsoDeliveryUserId;
	/**
	 *  订单子商品数量
	 */
	private Integer nxCsoSubAmount;
	/**
	 *  订单子商品完成数量
	 */
	private Integer nxCsoSubFinished;
	/**
	 *  订单称重订单号
	 */
	private String nxCsoWeighNumber;
	/**
	 *  订单支付状态
	 */
	private Integer nxCsoPaymentStatus;
	/**
	 *  订单支付发送时间
	 */
	private String nxCsoPaymentSendTime;
	/**
	 *  订单支付时间
	 */
	private String nxCsoPaymentTime;
	/**
	 *  订单类型 0先付款1后付款
	 */
	private Integer nxCsoType;
	/**
	 *  订单支付时间
	 */
	private String nxCsoWxOutTradeNo;
	private String nxCsoYouhuiTotal;
	private int nxCoBuyMemberOrderTime;

	/**
	 *  订单类型 0先付款1后付款
	 */
	private String nxCsoTotal;

	private NxCustomerUserEntity otherUser;
	private NxCustomerUserEntity orderUser;
	private List<NxCommunityOrdersSubEntity> nxCommunityOrdersSubEntities;

}

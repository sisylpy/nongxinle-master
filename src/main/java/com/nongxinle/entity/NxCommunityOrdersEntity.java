package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 2020-03-22 18:07:28
 */

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityOrdersEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订单id
	 */
	private Integer nxCommunityOrdersId;
	/**
	 *  订单客户id
	 */
	private Integer nxCoCustomerId;
	/**
	 *  订单用户id
	 */
	private Integer nxCoUserId;
	/**
	 *  批发商id
	 */
	private Integer nxCoDistributerId;
	/**
	 *  订单日期
	 */
	private String nxCoDate;
	/**
	 *  订单状态
	 */
	private Integer nxCoStatus;
	/**
	 *  订单送达
	 */
	private String nxCoService;
	/**
	 *  订单送达riqi
	 */
	private String nxCoServiceDate;
	/**
	 *  订单送达时间
	 */
	private String nxCoServiceTime;
	private String nxCoWeighNumber;


	/**
	 *  订单总金额
	 */
	private Float nxCoAmount;

	private String nxCoTotal;
	private String nxCoYouhuiTotal;

	/**
	 * zidingdan
	 */
	private List<NxCommunityOrdersSubEntity> nxOrdersSubEntities;

	/**
	 * kehu
	 */
	private NxCustomerEntity nxCustomerEntity;

	private Boolean isSelected;

	private Integer nxCoWeighUserId;

	private Integer nxCoSubAmount;

	private Integer nxCoSubFinished;

	private Integer nxCoPaymentStatus;

	private String nxCoPaymentSendTime;

	private String nxCoPaymentTime;

	private Integer nxCoDeliveryUserId;


	private Integer nxCoType;

	private  Integer nxCoCommunityId;

	private String nxCoUserOpenId;
	private String nxCoWxOutTradeNo;
	private int nxCoBuyMemberOrderTime;

	private NxCustomerUserEntity orderUser;

	private NxCustomerUserEntity deliveryUser;
	private NxCommunitySplicingOrdersEntity orderUserSplicingOrder;
	private List<NxCommunitySplicingOrdersEntity> allSplicingOrders;




}

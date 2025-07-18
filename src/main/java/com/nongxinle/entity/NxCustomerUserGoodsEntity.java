package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-14 17:42
 */

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCustomerUserGoodsEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  客户用户商品id
	 */
	private Integer nxCugGoodsId;
	/**
	 *  客户用户id
	 */
	private Integer nxCugUserId;
	private Integer nxCugType;
	/**
	 *  社区商品id
	 */
	private Integer nxCugCommunityGoodsId;
	/**
	 *  客户用户第一次订货时间
	 */
	private String nxCugFirstOrderTime;
	/**
	 *  客户用户最后一次订货时间
	 */
	private String nxCugLastOrderTime;
	/**
	 *  客户用户订货总量
	 */
	private String nxCugOrderAmount;
	/**
	 *  客户用户订货次数
	 */
	private Integer nxCugOrderTimes;
	/**
	 *  客户用户最爱
	 */
	private Integer nxCugIsLove;

	/**
	 * 客户订货频率
	 */
	private Integer nxCugOrderRate;


	private NxCommunityGoodsEntity nxCommunityGoodsEntity;

	private String nxCugLastOrderQuantity;

	private String nxCugLastOrderStandard;

	private Integer nxCugJoinMyTemplate;

	private String nxCugOrderQuantity;

	private String nxCugOrderStandard;

	private String nxCugGoodsColor;

}

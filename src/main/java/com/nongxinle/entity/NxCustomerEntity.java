package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCustomerEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  客户id
	 */
	private Integer nxCustomerId;
	/**
	 *  客户名称
	 */
	private String nxCustomerName;
	/**
	 *  客户打印名称
	 */
	private String nxCustomerPrintLabel;
	/**
	 *  客户对外名称
	 */
	private String nxCustomerOutLabel;

	/**
	 * 地址
	 */
	private Byte nxCustomerType;

	/**
	 * 地址
	 */
	private String nxCustomerAddress;
	private String nxCustomerCardWasteDate;


	/**
	 * 地址
	 */
	private String nxCustomerPhone;
	/**
	 * 地址
	 */
	private Integer nxCustomerCall;

	private String nxCustomerLat;

	private String nxCustomerLng;


	private Integer nxCustomerDisId;

	private String code;

	private Integer nxCustomerCommunityId;

	private Date nxCustomerJoinDate;

	private Float nxCustomerOrderAmount;

	private Integer nxCustomerOrderTimes;

	private String nxCustomerDetailAddress;

	private NxCustomerUserEntity nxCustomerUserEntity;

	private List<NxCustomerUserEntity>  nxCustomerUserEntities;

	private NxCommunityEntity nxCommunityEntity;


}

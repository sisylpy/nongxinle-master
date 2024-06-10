package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-29 10:22
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxBuyUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订货用户id
	 */
	private Integer nxBuyUserId;
	/**
	 *  零售商id
	 */
	private Integer nxBuRetailerId;
	/**
	 *  订货用户微信头像
	 */
	private String nxBuWxAvartraUrl;
	/**
	 *  订货用户微信昵称
	 */
	private String nxBuWxNickName;
	/**
	 *  订货用户微信openid
	 */
	private String nxBuWxOpenId;
	/**
	 *  订货户微信手机号码
	 */
	private String nxBuWxPhone;
	/**
	 *  零售商用户加入日期
	 */
	private String nxBuJoinDate;

	private String nxBuCode;


	private Integer nxBuyerPurBatchId;
	private Integer nxBuNxDistributerId;
	private Integer nxBuNxDisUserId;
	private String nxBuyType;

	private NxDistributerEntity nxDistributerEntity;
	private NxRetailerEntity nxRetailerEntity;
	private NxDistributerUserEntity nxDistributerUserEntity;

	private List<NxDistributerPurchaseBatchEntity> nxDisPurBatchEntityList;

}

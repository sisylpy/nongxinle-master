package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-12 11:38
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxJrdhUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订货用户id
	 */
	private Integer nxJrdhUserId;
	/**
	 *  订货用户微信头像
	 */
	private String nxJrdhWxAvartraUrl;
	/**
	 *  订货用户微信昵称
	 */
	private String nxJrdhWxNickName;
	/**
	 *  订货用户微信openid
	 */
	private String nxJrdhWxOpenId;
	/**
	 *  订货户微信手机号码
	 */
	private String nxJrdhWxPhone;
	/**
	 *  零售商用户加入日期
	 */
	private String nxJrdhJoinDate;
	/**
	 *  批发商id
	 */
	private Integer nxJrdhNxDistributerId;
	/**
	 *  批发商用户id
	 */
	private Integer nxJrdhNxPurchaserUserId;
	/**
	 *  批发商id
	 */
	private Integer nxJrdhNxCommunityId;
	private Integer nxJrdhCommPurchaserUserId;

	/**
	 *  批发商用户id
	 */

	private Integer nxJrdhUrlChange;
	private Integer nxJrdhAdmin;

	private Integer nxJrdhGbDistributerId;
	private Integer nxJrdhGbDepartmentId;
	private Integer nxJrdhGbDepartmentUserId;

	private String nxJrdhCode;
	private String nxJrdhDeviceId;
	private String nxJrdhDevicePrintId;
	private NxDistributerUserEntity nxDistributerUserEntity;
	private GbDepartmentUserEntity gbDepartmentUserEntity;
	private List<NxDistributerGoodsEntity> autoPurchaseDisGoodsList;


	private List<NxJrdhSupplierEntity>  nxJrdhSupplierEntities;
	private List<NxDistributerEntity>   nxDistributerEntities;
	private List<GbDistributerEntity>  gbDistributerEntities;



}

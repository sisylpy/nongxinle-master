package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-29 10:21
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.omg.PortableInterceptor.INACTIVE;


@Setter@Getter@ToString

public class NxSellUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  卖货用户id
	 */
	private Integer nxSellUserId;
	/**
	 *  零售商id
	 */
	private Integer nxSuRetailerId;
	/**
	 *  卖货用户微信头像
	 */
	private String nxSuWxAvartraUrl;
	/**
	 *  卖货用户微信昵称
	 */
	private String nxSuWxNickName;
	/**
	 *  卖货用户微信openid
	 */
	private String nxSuWxOpenId;
	/**
	 *  卖货户微信手机号码
	 */
	private String nxSuWxPhone;
	/**
	 *  用户加入日期
	 */
	private String nxSuJoinDate;

	private String nxSuCode;
	private Integer nxSellerSellBatchId;
	private String nxSellerType;
	private Integer nxSuGbDisSupplierId;
	private Integer nxSuNxDisId;

	private GbDistributerSupplierEntity gbDistributerSupplierEntity;
	private List<NxDistributerPurchaseBatchEntity> nxDistributerPurchaseBatchEntityList;

}

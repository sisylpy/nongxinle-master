package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-25 15:21
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxPrinterUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订货用户id
	 */
	private Integer nxPrinterUserId;
	/**
	 *  订货用户微信头像
	 */
	private String nxPrinterWxAvartraUrl;
	/**
	 *  订货用户微信昵称
	 */
	private String nxPrinterWxNickName;
	/**
	 *  订货用户微信openid
	 */
	private String nxPrinterWxOpenId;
	/**
	 *  订货户微信手机号码
	 */
	private String nxPrinterWxPhone;
	/**
	 *  零售商用户加入日期
	 */
	private String nxPrinterJoinDate;
	/**
	 *  批发商id
	 */
	private Integer nxPrinterNxDistributerId;
	/**
	 *  批发商用户id
	 */
	private Integer nxPrinterNxPurchaserUserId;
	/**
	 *  批发商id
	 */
	private Integer nxPrinterNxCommunityId;
	/**
	 *  批发商用户id
	 */
	private Integer nxPrinterNxCommPurchaserUserId;
	/**
	 *  批发商用户id
	 */
	private Integer nxPrinterUrlChange;
	/**
	 *  0 seller, 1nxpurchaser 2 gbpurchaser 
	 */
	private Integer nxPrinterAdmin;
	/**
	 *  批发商id
	 */
	private Integer nxPrinterGbDistributerId;
	/**
	 *  批发商用户id
	 */
	private Integer nxPrinterGbDepartmentId;
	/**
	 *  批发商用户id
	 */
	private Integer nxPrinterGbDepartmentUserId;
	private String nxPrinterDeviceId;
	private String nxPrinterDeviceBillId;

	private NxDistributerEntity nxDistributerEntity;
	private GbDistributerEntity gbDistributerEntity;
	private NxCommunityEntity nxCommunityEntity;

}

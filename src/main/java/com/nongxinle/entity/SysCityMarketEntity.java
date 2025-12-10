package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 08-19 12:35
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class SysCityMarketEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer sysCityMarketId;
	/**
	 *  
	 */
	private Integer sysCmCityId;
	/**
	 *  
	 */
	private String sysCmMarketName;
	
	/**
	 * 配送商注册赠送试用点数
	 */
	private Integer sysCmRegisterGiftPoints;
	
	/**
	 * 一元兑换点数比例
	 */
	private Integer sysCmPointsPerYuan;
	
	/**
	 * 是否开通自助打印机器：0=关闭，1=开启
	 */
	private Integer sysCmSelfPrintEnabled;
	
	/**
	 * 市场管理人员手机号
	 */
	private String sysCmManagerPhone;
	
	/**
	 * 市场区域范围坐标（JSON格式存储多边形坐标点）
	 */
	private String sysCmAreaCoordinates;
	
	/**
	 * 市场中心点纬度
	 */
	private java.math.BigDecimal sysCmCenterLatitude;
	
	/**
	 * 市场中心点经度
	 */
	private java.math.BigDecimal sysCmCenterLongitude;
	
	/**
	 * 市场配送半径（米）
	 */
	private Integer sysCmDeliveryRadius;
	
	/**
	 * 市场支付配置类名（如：MyWxJjdhPayConfig）
	 */
	private String sysCmPayConfigClass;
	
	/**
	 * 市场小程序AppID（如：wx58ba279bc3d04c4a）
	 */
	private String sysCmMiniAppId;

	private List<NxDistributerEntity> nxDistributerEntities;

}

package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-21 22:15
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxWxOrdersEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  微信支付订单id
	 */
	private Integer nxWxOrdersId;
	/**
	 *  随机字符串32位
	 */
	private String nxWxOrdersOutTradeNo;
	/**
	 *  订单主体
	 */
	private String nxWxOrdersBody;
	/**
	 *  订单详细
	 */
	private String nxWxOrdersDetail;
	/**
	 *  附加数据，如“深圳分店”
	 */
	private String nxWxOrdersAttach;
	/**
	 *  支付金额单位“分”
	 */
	private Integer nxWxOrdersTotalFee;
	/**
	 *  支付api的机器ip
	 */
	private String nxWxOrdersSpbillCreateIp;

}

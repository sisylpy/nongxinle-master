package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 03-09 08:28
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Setter@Getter@ToString

public class NxGoodsPriceEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  商品价格id
	 */
	private Integer nxGoodsPriceId;
	/**
	 *  商品id
	 */
	private Integer nxGpNxGoodsId;
	/**
	 *  日期
	 */
	private String nxGpDate;
	/**
	 *  最低单价
	 */
	private String nxGpLowestPrice;
	/**
	 *  最低单价数量
	 */
	private String nxGpLowestWeight;
	/**
	 *  最低单价批发商
	 */
	private Integer nxGpLowestNxDistributerId;
	/**
	 *  最低单价菜商
	 */
	private Integer nxGpLowestJrdhSupplierId;
	/**
	 *  最高单价
	 */
	private String nxGpHighestPrice;
	/**
	 *  最高单价数量
	 */
	private String nxGpHighestWeight;
	/**
	 *  最高单价批发商
	 */
	private Integer nxGpHighestNxDistributerId;
	/**
	 *  最高单价菜商
	 */
	private Integer nxGpHighestJrdhSupplierId;
	/**
	 *  单价城市 id
	 */
	private Integer nxGpSysCityId;
	/**
	 *  单价批发市场 id
	 */
	private Integer nxGpSysMarketId;

	private NxJrdhSupplierEntity highestSupplier;
	private NxJrdhSupplierEntity lowestSupplier;

}

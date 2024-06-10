package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 2020-02-10 19:43:11
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;


@Setter@Getter@ToString

public class NxDistributerDayWorkEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商id
	 */
	private Integer nxDwDistributerId;

	private String nxDwDate;

	private List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities;
	private List<NxDistributerPurchaseBatchEntity> purchaseBatchEntities;




}

package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 01-14 21:23
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityStatisticsEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCommunityStatisticsId;
	/**
	 *  
	 */
	private Integer nxCsComGoodsId;
	/**
	 *  
	 */
	private Integer nxCsComFGoodsId;
	/**
	 *  
	 */
	private Integer nxCsComGfGoodsId;
	/**
	 *  
	 */
	private Integer nxCsComGgfGoodsId;

	private Date nxCsOrderDate;
	private Float nxCsComGoodsProfit;
	private Float nxCsComGoodsWeight;
	private Float nxCsPurchasePrice;
	private Integer nxCsOrderQuantity;
	private Integer nxCsComId;

	private NxCommunityGoodsEntity nxCommunityGoodsEntity;


}

package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-07 09:33
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityDeskEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCommunityDeskId;
	/**
	 *  
	 */
	private String nxCommunityDeskName;
	/**
	 *  
	 */
	private Integer nxCdAreaId;
	/**
	 *  
	 */
	private String nxCdChairNum;
	/**
	 *  
	 */
	private String nxCdCommunityId;
	/**
	 *  
	 */
	private Integer nxCdType;
	/**
	 *  
	 */
	private Integer nxCdStatus;
	/**
	 * 当前绑定待支付 POS 订单
	 */
	private Integer nxCdCurrentOrderId;

	private List<NxCommunityOrdersSubEntity> ordersSubEntities;

}

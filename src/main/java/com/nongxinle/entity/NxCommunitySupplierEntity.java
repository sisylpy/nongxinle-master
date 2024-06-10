package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-15 18:45
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunitySupplierEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  供货商id
	 */
	private Integer nxCommunitySupplierId;
	/**
	 *  供货商名称
	 */
	private String nxCommunitySupplierName;
	/**
	 *  nx_community_id
	 */
	private Integer nxCsNxCommunityId;
	/**
	 *  订单类型
	 */
	private Integer nxCsOrderType;
	/**
	 *  今日订货用户id
	 */
	private Integer nxCsJrdhSupplierUserId;
	/**
	 *  今日订货App用户id
	 */
	private Integer nxCsJrdhPurUserId;
	/**
	 *  nx_comm采购员id
	 */
	private Integer nxCsNxCommunityUserId;

	private NxJrdhUserEntity supplierUserEntity;


	private List<NxCommunityGoodsEntity> nxCommunityGoodsEntityList;


}

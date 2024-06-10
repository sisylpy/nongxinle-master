package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-06 00:18
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityGoodsSetPropertyEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCommunityGoodsSetPropertyId;
	/**
	 *  
	 */
	private String nxCgspName;
	/**
	 *  
	 */
	private Integer nxCgspSort;
	/**
	 *  
	 */
	private Integer nxCgspCgGoodsId;


	private List<NxCommunityGoodsSetItemEntity> nxCommunityGoodsSetItemEntities;

}

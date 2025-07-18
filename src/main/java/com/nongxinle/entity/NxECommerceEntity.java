package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 11-28 21:17
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxECommerceEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxECommerceId;
	private Integer nxECommerceGbDisId;
	/**
	 *  
	 */
	private String nxECommerceName;
	/**
	 *  
	 */
	private String nxECommerceImg;


	private List<NxECommerceCommunityEntity> nxECommerceCommunityEntities;
	private NxECommerceCommunityEntity nxECommerceCommunityEntity;


}

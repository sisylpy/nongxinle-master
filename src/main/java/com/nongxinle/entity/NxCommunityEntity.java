package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 2020-03-04 17:57:31
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCommunityId;
	/**
	 *  
	 */
	private String nxCommunityName;
	/**
	 *  
	 */
	private String nxCommunityLat;
	/**
	 *  
	 */
	private String nxCommunityLng;
	/**
	 *
	 */
	private Integer nxCommunityRouteId;

	private Integer nxCommunityCommerceId;

	private String nxCommunityPolygon;

	private String nxCommunityRegion;
	private String nxCommunityDeliveryAddress;

	private NxCommunityUserEntity nxCommunityUserEntity;


}

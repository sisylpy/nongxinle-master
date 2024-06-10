package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 11-28 21:17
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxECommerceCommunityEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商社区id
	 */
	private Integer nxEccId;
	/**
	 *  
	 */
	private Integer nxEccEId;
	/**
	 *  
	 */
	private Integer nxEccCommunityId;

}

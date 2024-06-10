package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 11-30 15:31
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityRestrauantEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCommunityRestaruantId;
	/**
	 *  
	 */
	private Integer nxCrCommunityId;
	/**
	 *  
	 */
	private Integer nxCrRestaruantId;

}

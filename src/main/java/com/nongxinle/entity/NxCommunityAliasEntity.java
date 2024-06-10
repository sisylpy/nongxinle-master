package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 12-05 18:56
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityAliasEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCommunityAliasId;
	/**
	 *  
	 */
	private Integer nxCaComGoodsId;
	/**
	 *  
	 */
	private String nxCaAliasName;

}

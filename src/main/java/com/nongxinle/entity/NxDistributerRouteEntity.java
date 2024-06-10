package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-14 20:17
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerRouteEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  线路id
	 */
	private Integer nxDistributerRouteId;
	/**
	 *  线路名称
	 */
	private String nxDistributerRouteName;
	/**
	 *  批发商id
	 */
	private Integer nxDistributerRouteDisId;

}

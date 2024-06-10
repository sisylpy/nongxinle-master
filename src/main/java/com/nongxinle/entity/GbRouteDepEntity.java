package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-20 21:43
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbRouteDepEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  线路id
	 */
	private Integer gbRouteDepId;
	/**
	 *  线路id
	 */
	private Integer gbRdRouteId;
	/**
	 *  商户id
	 */
	private Integer gbRdDepId;
	private Integer gbRdDisId;

}

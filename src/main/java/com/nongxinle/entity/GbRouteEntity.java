package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-18 21:32
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbRouteEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  线路id
	 */
	private Integer gbRouteId;
	/**
	 *  线路名称
	 */
	private String gbRouteName;

	private Integer gbRouteDisId;

	List<GbDepartmentEntity> departmentEntities;

}

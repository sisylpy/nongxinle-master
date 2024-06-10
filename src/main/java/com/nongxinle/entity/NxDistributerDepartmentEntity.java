package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-09 21:11
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerDepartmentEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDistributerDepId;
	/**
	 *  
	 */
	private Integer nxDdDistributerId;
	/**
	 *  
	 */
	private Integer nxDdDepartmentId;

	private NxDepartmentEntity nxDepartmentEntity;

}

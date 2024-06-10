package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 08-19 12:35
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class SysBusinessTypeEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer sysBusinessTypeId;
	private Integer sysByDisType;
	/**
	 *  
	 */
	private String sysBusinessTypeName;

	List<NxDistributerEntity> nxDistributerEntities;

}

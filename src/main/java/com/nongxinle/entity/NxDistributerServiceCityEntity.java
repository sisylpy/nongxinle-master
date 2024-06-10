package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 08-19 13:03
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerServiceCityEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDistributerServiceCityId;
	/**
	 *  
	 */
	private Integer nxDsCityId;
	/**
	 *  
	 */
	private Integer nxDsDisId;

	private String nxDsCityName;

	private SysCityEntity sysCityEntity;

}

package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 08-19 12:35
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class SysCityEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer sysCityId;
	/**
	 *  
	 */
	private Integer sysCityType;
	/**
	 *  
	 */
	private String sysCityName;
	/**
	 *  
	 */
	private Integer sysCityFatherId;
	/**
	 *  
	 */
	private String sysCityPy;
	/**
	 *  
	 */
	private String sysCityPinyin;

}

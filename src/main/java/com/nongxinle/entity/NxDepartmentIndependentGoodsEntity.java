package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 07-24 13:00
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDepartmentIndependentGoodsEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDepartmentIndependentGoodsId;
	/**
	 *  
	 */
	private String nxDigGoodsName;
	/**
	 *  
	 */
	private String nxDigGoodsStandardname;
	/**
	 *  
	 */
	private Integer nxDigDepartmentFatherId;
	/**
	 *  
	 */
	private Integer nxDigDepartmentId;
	/**
	 *  
	 */
	private Integer nxDigAlarmRate;
	/**
	 *  
	 */
	private String nxDigGoodsPinyin;
	/**
	 *  
	 */
	private String nxDigGoodsPy;

	private Boolean isShow;

}

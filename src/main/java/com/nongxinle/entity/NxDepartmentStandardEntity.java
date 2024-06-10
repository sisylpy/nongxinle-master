package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 08-11 22:02
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDepartmentStandardEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  部门规格id
	 */
	private Integer nxDepartmentStandardId;
	/**
	 *  部门商品id
	 */
	private Integer nxDdsDdsGoodsId;
	/**
	 *  部门规格名称
	 */
	private String nxDdsStandardName;
	/**
	 *  部门规格排序
	 */
	private Integer nxDdsStandardSort;

}

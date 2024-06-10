package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-18 21:32
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;


@Setter@Getter@ToString

public class GbDepartmentDay implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订货部门id
	 */
	private String  day;
	/**
	 *  订货部门名称
	 */
	private Double dayStockTotal;

}

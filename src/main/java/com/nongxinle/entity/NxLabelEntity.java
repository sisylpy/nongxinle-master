package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-27 12:56
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxLabelEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxLabelId;
	/**
	 *  
	 */
	private String nxLabelName;
	/**
	 *  
	 */
	private Integer nxLabelHot;
	/**
	 *  
	 */
	private Integer nxLabelTypeId;

}

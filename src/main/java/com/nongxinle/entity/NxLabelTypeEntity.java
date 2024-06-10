package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-27 12:56
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxLabelTypeEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxLabelTypeId;
	/**
	 *  
	 */
	private String nxLabelTypeName;
	/**
	 *  
	 */
	private Integer nxLabelAmount;
	/**
	 *  
	 */
	private Integer nxLabelTypeSort;

	private List<NxLabelEntity> nxLabelEntities;

}

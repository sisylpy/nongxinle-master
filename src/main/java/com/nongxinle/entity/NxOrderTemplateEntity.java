package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-14 12:45
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxOrderTemplateEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxOrderTemplateId;
	/**
	 *  
	 */
	private String nxOdFilePath;
	/**
	 *  
	 */
	private String nxOdName;
	/**
	 *  
	 */
	private Integer nxOdCustomerUserId;
	/**
	 *
	 */
	private Integer nxOdItemAmount;


	private List<NxOrderTemplateItemEntity> nxOrderTemplateItemEntity;

}

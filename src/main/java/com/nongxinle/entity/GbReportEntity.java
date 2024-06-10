package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 09-26 20:05
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbReportEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbReportId;
	/**
	 *  
	 */
	private String gbRepIds;
	/**
	 *  
	 */
	private String gbRepType;
	private String gbRepStartDate;
	private String gbRepStopDate;
	/**
	 *  
	 */
	private Integer gbRepDisUserId;

	private GbDepartmentEntity gbDepartmentEntity;

}

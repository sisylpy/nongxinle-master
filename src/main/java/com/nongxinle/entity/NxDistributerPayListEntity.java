package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-20 11:05
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerPayListEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDistributerPayListId;
	/**
	 *  
	 */
	private Integer nxNdplNxDisId;
	/**
	 *  
	 */
	private String nxNdplPaySubtotal;
	/**
	 *  
	 */
	private String nxNdplPayTime;
	/**
	 *  
	 */
	private Integer nxNdplType;
	/**
	 *  
	 */
	private Integer nxNdplStatus;
	/**
	 *  
	 */
	private String nxNdplPayDate;
	private String nxNdplPayMonth;
	private String nxNdplPayYear;
	/**
	 *  
	 */
	private String nxNdplRestPoints;
	private Integer nxNdplBuyPoints;

	private Integer nxNdplNxDbId;
	private Integer nxNdplGbDbId;
	private Integer nxNdplNxDepartmentFatherId;
	private Integer nxNdplGbDepartmentFatherId;
	private Integer nxNdplNxDepartmentId;
	private Integer nxNdplGbDepartmentId;

	private NxDepartmentEntity nxDepartmentEntity;
	private GbDepartmentEntity gbDepartmentEntity;

}

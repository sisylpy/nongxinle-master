package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-09 21:11
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerGbDistributerEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDistributerGbDistributerId;
	/**
	 *  
	 */
	private Integer nxDgdNxDistributerId;
	/**
	 *  
	 */
	private Integer nxDgdGbDistributerId;
	private Integer nxDgdGbDepId;
	private Integer nxDgdGbDepUserId;

	private GbDistributerEntity gbDistributerEntity;
	private NxDistributerEntity nxDistributerEntity;
	private GbDepartmentEntity gbDepartmentEntity;
	private GbDepartmentUserEntity gbDepartmentUserEntity;
	private Boolean isSelected = false;

	private Integer nxDgdGbPayMethod;
	private Integer nxDgdGbGoodsPrice;
	private Integer nxDgdStatus;
	private Integer nxDgdGbPayPeriodWeek;


}

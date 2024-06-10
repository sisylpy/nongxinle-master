package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 09-11 14:46
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxJrdhUserAuthSupplierIdEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  订货用户id
	 */
	private Integer nxJrdhUserAuthSupplierId;
	/**
	 *  批发商id
	 */
	private Integer nxJrdhasNxDistributerId;
	/**
	 *  批发商id
	 */
	private Integer nxJrdhasNxCommunityId;
	/**
	 *  批发商id
	 */
	private Integer nxJrdhasGbDistributerId;
	/**
	 *  批发商用户id
	 */
	private Integer nxJrdhasGbDepartmentId;
	/**
	 *  批发商id
	 */
	private Integer nxJrdhasSupplierId;
	/**
	 *  批发商id
	 */
	private Integer nxJrdhasUserId;

}

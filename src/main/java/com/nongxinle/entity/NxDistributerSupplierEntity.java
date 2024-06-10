package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-12 19:46
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerSupplierEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商供货商id
	 */
	private Integer nxDistributerSupplierId;
	/**
	 *  批发商id
	 */
	private Integer nxDsNxDistributerId;
	/**
	 *  供货商id
	 */
	private Integer nxDsJrdhUserId;
	/**
	 *  供货商yonghuid
	 */
	private Integer nxDsJrdhNxDisUserId;

	private String nxDistributerSupplierName;
	private String nxDistributerSupplierAddress;
	private NxJrdhUserEntity supplierUserEntity;

	private NxDistributerUserEntity disPurchaserUserEntity;
	private NxDistributerEntity nxDistributerEntity;


}

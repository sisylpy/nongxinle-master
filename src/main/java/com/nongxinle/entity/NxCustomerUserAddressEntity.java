package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 09-20 00:57
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCustomerUserAddressEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCustomerUserAddressId;
	/**
	 *  
	 */
	private Integer nxCuaCustomerUserId;
	/**
	 *  
	 */
	private Integer nxCuaCommunityId;
	/**
	 *  
	 */
	private Integer nxCuaStatus;
	/**
	 *  
	 */
	private Integer nxCuaType;
	/**
	 *  
	 */
	private String nxCuaUserName;
	/**
	 *  
	 */
	private String nxCuaUserPhone;
	/**
	 *  
	 */
	private String nxCuaAddressBuildingName;
	/**
	 *  
	 */
	private String nxCuaAddressDetail;
	private String nxCuaLocation;
	private String nxCuaLat;
	private String nxCuaLng;
	/**
	 *  
	 */
	private Integer nxCuaIsSelected;
	private Integer nxCuaSysDistrictId;
	private Integer nxCuaSysCityId;
	private Integer nxCuaSysProvinceId;
	private Integer nxCuaSysBusinessAreaId;




}

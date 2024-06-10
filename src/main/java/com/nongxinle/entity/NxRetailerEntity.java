package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-22 15:25
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxRetailerEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  零售商id
	 */
	private Integer nxRetailerId;
	/**
	 *  零售商名称
	 */
	private String nxRetailerName;
	/**
	 *  
	 */
	private String nxRetailerLat;
	/**
	 *  
	 */
	private String nxRetailerLng;
	/**
	 *  
	 */
	private String nxRetailerImg;
	/**
	 *  
	 */
	private String nxRetailerGoodsId;
	/**
	 *  
	 */
	private String nxRetailerDescribe;

	private NxRetailerUserEntity nxRetailerUserEntity;

}

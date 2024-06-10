package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-11 21:54
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxJrdhSupplierEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  供货商id
	 */
	private Integer nxJrdhSupplierId;
	/**
	 *  供货商名称
	 */
	private String nxJrdhsSupplierName;
	/**
	 *  gbDisid
	 */
	private Integer nxJrdhsGbDistributerId;
	/**
	 *  gbDepid
	 */
	private Integer nxJrdhsGbDepartmentId;
	/**
	 *  接单元id
	 */
	private Integer nxJrdhsUserId;
	/**
	 *  gbDisid
	 */
	private Integer nxJrdhsNxDistributerId;
	/**
	 *  gbDisid
	 */
	private Integer nxJrdhsNxCommunityId;

	private NxJrdhUserEntity jrdhUserEntity;
	private NxDistributerUserEntity nxPurUserEntity;
	private GbDepartmentUserEntity gbPurUserEntity;
	private NxCommunityUserEntity nxCommPurUserEntity;

	private Integer nxJrdhsNxPurUserId;
	private Integer nxJrdhsGbPurUserId;
	private Integer nxJrdhsCommPurUserId;
	private Integer nxJrdhsNxJrdhBuyUserId;
	private List<GbDistributerGoodsEntity> gbDistributerGoodsEntities;

	private NxJrdhUserEntity buyerUserEntity;
	private NxDistributerEntity nxDistributerEntity;
	private GbDistributerEntity gbDistributerEntity;



}

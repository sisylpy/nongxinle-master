package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 09-16 09:12
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerSupplierEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  供货商id
	 */
	private Integer gbDistributerSupplierId;
	/**
	 *  供货商名称
	 */
	private String gbDistributerSupplierName;
	/**
	 *  gbDisid
	 */
	private Integer gbDsGbDistributerId;
	private Integer gbDsGbDepartmentId;
	private Integer gbDistributerSupplierFatherId;
	private Integer gbDsSupplierIsGroup;
	private Integer gbDsOrderType;
	private Integer gbDsSupplierUserId;
	private Integer gbDsPurUserId;

	private GbDistributerSupplierUserEntity gbDisApplintSupplierUserEntity;
    private GbDepartmentUserEntity purUserEntity;
    private List<GbDistributerGoodsEntity> gbDistributerGoodsEntities;



}

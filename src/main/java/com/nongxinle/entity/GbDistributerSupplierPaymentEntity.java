package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-28 13:40
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerSupplierPaymentEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  供货商id
	 */
	private Integer gbDistributerSupplierPaymentId;
	/**
	 *  供货商名称
	 */
	private String gbDspDate;
	/**
	 *  父级id
	 */
	private Integer gbDspSupplierId;
	/**
	 *  gbDisid
	 */
	private Integer gbDspPayUserId;
	private Integer gbDspNxDistributerId;
	private String gbDspWxOutTradeNo;
	private Integer gbDspStatus;
	private String gbDspPayUserOpenId;
	/**
	 *  gbDepid
	 */
	private String gbDspPayTotal;
	private Integer gbDspDistributerId;
	private GbDistributerSupplierEntity gbDistributerSupplierEntity;
	private NxDistributerEntity nxDistributerEntity;

	private List<GbDistributerPurchaseBatchEntity> gbDisPurchaseBatchEntities;
	private List<GbDepartmentBillEntity> gbDepartmentBillEntities;

}

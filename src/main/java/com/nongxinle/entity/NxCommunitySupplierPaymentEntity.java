package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-15 18:45
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunitySupplierPaymentEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  供货商付款id
	 */
	private Integer nxCommuntiySupplierPaymentId;
	/**
	 *  付款日期
	 */
	private String nxCspDate;
	/**
	 *  供货商id
	 */
	private Integer nxCspSupplierId;
	/**
	 *  community_user付款人
	 */
	private Integer nxCspPayCommUserId;
	/**
	 *  付款总额
	 */
	private String nxCspPayTotal;
	/**
	 *  nxCommunityId
	 */
	private Integer nxCspNxCommunityId;

}

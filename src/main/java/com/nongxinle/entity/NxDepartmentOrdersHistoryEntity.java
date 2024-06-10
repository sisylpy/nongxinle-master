package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-08 19:09
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDepartmentOrdersHistoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  部门订单id
	 */
	private Integer nxDepartmentOrdersHistoryId;
	/**
	 *  部门id
	 */
	private Integer nxDohDepDisGoodsId;
	/**
	 *  部门订单申请数量
	 */
	private String nxDohQuantity;
	/**
	 *  部门订单申请规格
	 */
	private String nxDohStandard;
	/**
	 *  部门订单申请备注
	 */
	private String nxDohRemark;
	/**
	 *  部门订单部门id
	 */
	private Integer nxDohDepartmentId;
	/**
	 *  
	 */
	private Integer nxDohDepartmentFatherId;
	/**
	 *  部门订单订货用户id
	 */
	private Integer nxDohOrderUserId;
	/**
	 *  部门订单申请时间
	 */
	private String nxDohApplyDate;
	/**
	 *  出货方式0,日采;1,出库;2,供货商;3,加工
	 */
	private Integer nxDohSellType;
	private Integer nxDohOrder;
	private Integer nxDohOrderTimes;

}

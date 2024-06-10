package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-30 10:14
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepartmentOrdersHistoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  部门订单id
	 */
	private Integer gbDepartmentOrdersHistoryId;
	/**
	 *  部门id
	 */
	private Integer gbDohDepDisGoodsId;
	/**
	 *  部门订单申请数量
	 */
	private String gbDohQuantity;
	/**
	 *  部门订单申请规格
	 */
	private String gbDohStandard;
	/**
	 *  部门订单申请备注
	 */
	private String gbDohRemark;
	/**
	 *  部门订单部门id
	 */
	private Integer gbDohDepartmentId;
	/**
	 *  
	 */
	private Integer gbDohDepartmentFatherId;
	/**
	 *  部门订单订货用户id
	 */
	private Integer gbDohOrderUserId;
	/**
	 *  部门订单申请时间
	 */
	private String gbDohApplyDate;
	/**
	 *  出货方式0,日采;1,出库;2,供货商;3,加工
	 */
	private Integer gbDohSellType;

	private  Integer gbDohStandardId;
	private String gbDohStandardScale;

}

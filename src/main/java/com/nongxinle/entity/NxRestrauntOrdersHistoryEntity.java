package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-07 09:20
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxRestrauntOrdersHistoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  饭馆订单id
	 */
	private Integer nxRestrauntOrdersHistoryId;
	/**
	 *  饭馆id
	 */
	private Integer nxRohResComGoodsId;
	/**
	 *  部门订单申请数量
	 */
	private String nxRohQuantity;
	/**
	 *  部门订单申请规格
	 */
	private String nxRohStandard;
	/**
	 *  部门订单申请备注
	 */
	private String nxRohRemark;
	/**
	 *  部门订单部门id
	 */
	private Integer nxRohRestrauntId;
	/**
	 *  
	 */
	private Integer nxRohRestrauntFatherId;
	/**
	 *  部门订单订货用户id
	 */
	private Integer nxRohOrderUserId;
	/**
	 *  部门订单申请时间
	 */
	private String nxRohApplyDate;
	/**
	 *  出货方式0,日采;1,出库;2,供货商;3,加工
	 */
	private Integer nxRohSellType;

}

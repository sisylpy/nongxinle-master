package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-21 12:08
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerWeightEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  称重单id
	 */
	private Integer nxDistributerWeightId;
	/**
	 *  称重用户id
	 */
	private Integer nxDwUserId;
	/**
	 *  称重disid
	 */
	private Integer nxDwDisId;
	/**
	 *  称重单总重量
	 */
	private String nxDwWeightTotal;
	/**
	 *  称重单总日期
	 */
	private String nxDwDate;
	/**
	 *  称重单总金额
	 */
	private String nxDwSubtotal;
	/**
	 *  称重单状态
	 */
	private Integer nxDwStatus;
	/**
	 *  称重单总金额
	 */
	private String nxDwOrderNames;
	private Integer nxDwDepFatherId;
	private Integer nxDwGbDepFatherId;
	private Integer nxDwResFatherId;
	private Integer nxDwType;
	private Integer nxDwItemCount;
	private Integer nxDwItemFinishCount;
	private String nxDwOrderIds;
	private String nxDwPurGoodsIds;
	private String nxDwTradeNo;

	private List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntityList;

	private NxDistributerUserEntity weightUserEntity;

}

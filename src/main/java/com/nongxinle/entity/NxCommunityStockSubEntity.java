package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-08 09:22
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityStockSubEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  社区子库存id
	 */
	private Integer nxCommunitySubStockId;
	/**
	 *  社区子库存库存id
	 */
	private Integer nxCsId;
	/**
	 *  社区子社区商品id
	 */
	private Integer nxCssCgId;
	/**
	 *  社区子库存入库数量
	 */
	private String nxCssEntryAmount;
	/**
	 *  社区子库存入库单位
	 */
	private String nxCssEntryStandard;
	/**
	 *  社区子库存入库单价
	 */
	private String nxCssEntryPrice;
	/**
	 *  社区子库存入库小计
	 */
	private String nxCssEntrySubTotal;
	/**
	 *  社区子库存入库日期
	 */
	private String nxCssEntryDate;
	/**
	 *  社区子库存库存数量
	 */
	private String nxCssStockAmount;

}

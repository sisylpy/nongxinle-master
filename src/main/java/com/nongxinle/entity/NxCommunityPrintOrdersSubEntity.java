package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 2020-03-22 18:07:28
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;


@Setter@Getter@ToString

public class NxCommunityPrintOrdersSubEntity implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  子订单id
	 */
	private Integer nxCommunityOrdersPrintSubId;

	/**
	 *  子订单申请数量
	 */
	private String nxCospQuantity;
	/**
	 * 子订单申请规格
	 */
	private String nxCospStandard;
	/**
	 *  子订单申请商品单价
	 */
	private String nxCospPrice;
	private String nxCospSubtotal;

	/**
	 *  子订单申请备注
	 */
	private String nxCospRemark;

	private Integer nxCospStatus;
	private Integer nxCospType;

	private Integer nxCospOrdersId;

	private Integer nxCospGoodsType;

	private Integer nxCospCommunityGoodsId;

  	private String nxCospSubWeight;
  	private String nxCospPickUpCode;
  	private String nxCospService;
  	private String nxCospServiceDate;
  	private String nxCospServiceTime;


	private NxCommunityGoodsEntity nxCommunityGoodsEntity;





	@Override
	public int compareTo(Object o) {
		if (o instanceof NxCommunityPrintOrdersSubEntity) {
			NxCommunityPrintOrdersSubEntity e = (NxCommunityPrintOrdersSubEntity) o;

			return this.nxCospCommunityGoodsId.compareTo(e.nxCospCommunityGoodsId);
		}
		return 0;
	}
}

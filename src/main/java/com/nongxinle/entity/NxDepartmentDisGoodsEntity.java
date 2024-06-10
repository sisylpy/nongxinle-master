package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 07-30 23:58
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDepartmentDisGoodsEntity implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDepartmentDisGoodsId;
	/**
	 *  
	 */
	private Integer nxDdgDepartmentFatherId;
	/**
	 *  
	 */
	private Integer nxDdgDepartmentId;
	/**
	 *  
	 */
	private Integer nxDdgDisGoodsId;
	private Integer nxDdgDisGoodsFatherId;

	/**
	 *  
	 */
	private String nxDdgDepGoodsName;
	/**
	 *  
	 */
	private String nxDdgDepGoodsPinyin;
	/**
	 *  
	 */
	private String nxDdgDepGoodsPy;
	/**
	 *  
	 */
	private Integer nxDdgDisGoodsGrandId;
	/**
	 *  
	 */
	private String nxDdgDepGoodsStandardname;
	/**
	 *  
	 */
	private String nxDdgDepGoodsDetail;
	private String nxDdgDepGoodsBrand;
	private String nxDdgDepGoodsPlace;
	private String nxDdgOrderPrice;
	private String nxDdgOrderDate;
	private String nxDdgOrderRemark;
	private String nxDdgOrderQuantity;
	private String nxDdgOrderStandard;
	private String nxDdgGoodsPlace;
	private String nxDdgOrderCostPrice;
	/**
	 *  
	 */

	private Integer isDownload;
	private Boolean isSelected = false;

	private Integer nxDdgIsGbDepartment;
	private Integer nxDdgGbDepartmentFatherId;
	private Integer nxDdgGbDepartmentId;
	private Integer nxDdgOrderSellerUserId;
	private Integer nxDdgOrderBuyerUserId;



	private List<NxDepartmentStandardEntity> nxDepStandardEntities;

	private NxDistributerGoodsEntity nxDistributerGoodsEntity;
	private NxDepartmentOrdersEntity nxDepartmentOrdersEntity;
	private List<NxDepartmentOrdersEntity> depGoodsDepOrderList;
	private List<NxDepartmentOrdersHistoryEntity> nxDepOrdersHistoryEntities;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxDepartmentDisGoodsEntity that = (NxDepartmentDisGoodsEntity) o;
		return Objects.equals(nxDepartmentDisGoodsId, that.nxDepartmentDisGoodsId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxDepartmentDisGoodsId);
	}

	@Override
	public int compareTo(Object o) {

		if (o instanceof NxDepartmentDisGoodsEntity) {
			NxDepartmentDisGoodsEntity e = (NxDepartmentDisGoodsEntity) o;
			return this.nxDepartmentDisGoodsId.compareTo(e.nxDepartmentDisGoodsId);
		}
		return 0;
	}
}

package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 12-01 08:51
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxRestrauntComGoodsEntity implements Serializable,Comparable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxRestrauntComGoodsId;
	/**
	 *  
	 */
	private Integer nxRcgRestrauntFatherId;
	/**
	 *  
	 */
	private Integer nxRcgRestrauntId;
	/**
	 *  
	 */
	private Integer nxRcgComGoodsId;
	/**
	 *  
	 */
	private Integer nxRcgComGoodsFatherId;
	/**
	 *  
	 */
	private String nxRcgComGoodsName;
	/**
	 *  
	 */
	private String nxRcgComGoodsPinyin;
	/**
	 *  
	 */
	private String nxRcgComGoodsPy;
	/**
	 *  
	 */
	private String nxRcgComGoodsStandardname;
	/**
	 *  
	 */
	private String nxRcgComGoodsDetail;
	/**
	 *  
	 */
	private String nxRcgComGoodsBrand;
	/**
	 *  
	 */
	private String nxRcgComGoodsPlace;
	/**
	 *  
	 */
	private String nxRcgOrderPrice;
	/**
	 *  
	 */
	private String nxRcgOrderDate;
	/**
	 *  
	 */
	private String nxRcgOrderRemark;
	/**
	 *  
	 */
	private String nxRcgOrderQuantity;
	private Integer nxRcgOrderUserId;
	private Boolean isSelected;
	private String nxRcgResKnowDate;
	private String nxRcgComGoodsAbcExchange;
	private Integer nxRcgComGoodsUpOrDown;
	private String nxRcgResContractOrderQuantity;
	private String nxRcgResContractPrice;
	private String nxRcgResContractStopDate;


	/**
	 *  
	 */
	private String nxRcgOrderStandard;
	private NxCommunityGoodsEntity nxCommunityGoodsEntity;
	private NxRestrauntOrdersEntity nxRestrauntOrdersEntity;
	private List<NxCommunityStandardEntity> nxCommunityStandardEntities;
	private List<NxRestrauntOrdersEntity> nxRestrauntOrdersEntities;
	private List<NxRestrauntEntity> nxRestrauntEntities;
	private List<NxRestrauntOrdersHistoryEntity> nxResOrdersHistoryEntities;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxRestrauntComGoodsEntity that = (NxRestrauntComGoodsEntity) o;
		return Objects.equals(nxRestrauntComGoodsId, that.nxRestrauntComGoodsId) ;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxRestrauntComGoodsId);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof NxRestrauntComGoodsEntity) {
			NxRestrauntComGoodsEntity e = (NxRestrauntComGoodsEntity) o;
			return this.nxRestrauntComGoodsId.compareTo(e.nxRestrauntComGoodsId);
		}
		return 0;
	}
}

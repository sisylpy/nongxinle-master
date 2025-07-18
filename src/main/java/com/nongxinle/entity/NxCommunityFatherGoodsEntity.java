package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 2020-02-24 17:06:57
 */

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityFatherGoodsEntity implements Serializable, Comparable  {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCommunityFatherGoodsId;

	private String nxCfgFatherGoodsName;

	private String nxCfgFatherGoodsImg;

	private Integer nxCfgFatherGoodsSort;

	private String nxCfgFatherGoodsColor;

	private Integer nxCfgFathersFatherId;

	private Integer nxCfgFatherGoodsLevel;

	private Integer nxCfgCommunityId;

	private Integer nxCfgGoodsAmount;

	private Integer nxCfgNxGoodsId;
	private Integer nxCfgPriceAmount;
	private Integer nxCfgPriceTwoAmount;
	private Integer nxCfgPriceThreeAmount;
	private Integer nxCfgOrderRank;
	private Boolean nxCfgPrintSelected = true;



	List<NxCommunityFatherGoodsEntity> fatherGoodsEntities;

	List<NxCommunityGoodsEntity> nxCommunityGoodsEntities;

	private String cgGoodsSubNames;


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NxCommunityFatherGoodsEntity that = (NxCommunityFatherGoodsEntity) o;
		return Objects.equals(nxCommunityFatherGoodsId, that.nxCommunityFatherGoodsId) &&
				Objects.equals(nxCfgFatherGoodsName, that.nxCfgFatherGoodsName) &&
				Objects.equals(nxCfgFatherGoodsImg, that.nxCfgFatherGoodsImg) &&
				Objects.equals(nxCfgFatherGoodsSort, that.nxCfgFatherGoodsSort) &&
				Objects.equals(nxCfgFatherGoodsColor, that.nxCfgFatherGoodsColor) &&
				Objects.equals(nxCfgFathersFatherId, that.nxCfgFathersFatherId) &&
				Objects.equals(nxCfgFatherGoodsLevel, that.nxCfgFatherGoodsLevel) &&
				Objects.equals(nxCfgCommunityId, that.nxCfgCommunityId) &&
				Objects.equals(nxCfgGoodsAmount, that.nxCfgGoodsAmount) ;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nxCommunityFatherGoodsId, nxCfgFatherGoodsName, nxCfgFatherGoodsImg, nxCfgFatherGoodsSort, nxCfgFatherGoodsColor, nxCfgFathersFatherId, nxCfgFatherGoodsLevel, nxCfgCommunityId, nxCfgGoodsAmount);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof NxCommunityOrdersSubEntity) {
			NxCommunityFatherGoodsEntity e = (NxCommunityFatherGoodsEntity) o;

			return this.nxCommunityFatherGoodsId.compareTo(e.nxCommunityFatherGoodsId);
		}
		return 0;	}
}

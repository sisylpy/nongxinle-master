package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-25 08:54
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityPromoteEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 促销活动商品id
	 */
	private Integer nxPromoteId;
	/**
	 * 社区商品id
	 */
	private Integer nxPromoteCgId;
	/**
	 * 促销原价
	 */
	private String nxOrignalPrice;
	/**
	 * 促销价格
	 */
	private String nxPromotePrice;
	/**
	 * 促销规格
	 */
	private String nxPromoteStandard;
	/**
	 *  促销重量
	 */
	private String nxPromoteWeight;
	/**
	 *  保质期
	 */
	private String nxPromoteExpired;
	/**
	 *  存储条件
	 */
	private String nxPromoteStorage;
	/**
	 *  促销语句
	 */
	private String nxPromoteWords;
	/**
	 *  推荐商品idlist
	 */
	private String nxPromoteRecommandGoods;

	/**
	 *
	 */
	private String nxPromoteFilePath;

	/**
	 *
	 */
	private Integer nxPromoteCommunityId;
	/**
	 *
	 */
	private Integer nxPromoteCgFatherId;


	private NxCommunityGoodsEntity nxCommunityGoodsEntity;

	private List<NxCommunityGoodsEntity> nxCommunityGoodsEntities;

}

package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 08-14 22:10
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerGoodsLinshiEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  社区商品id
	 */
	private Integer nxDistributerGoodsLsId;
	/**
	 *  批发商父类商品id
	 */
	private Integer nxDgDfgGoodsFatherLsId;
	/**
	 *  批发商id
	 */
	private Integer nxDgDistributerLsId;
	/**
	 *  商品状态
	 */
	private Integer nxDgGoodsLsStatus;
	/**
	 *  商品名称
	 */
	private String nxDgGoodsName;
	/**
	 *  商品详细
	 */
	private String nxDgGoodsDetail;
	/**
	 *  商品规格
	 */
	private String nxDgGoodsStandardname;
	/**
	 *  社区商品拼音
	 */
	private String nxDgGoodsPinyin;
	/**
	 *  社区商品拼音简拼
	 */
	private String nxDgGoodsPy;
	/**
	 *  nxGoodsId
	 */
	private Integer nxDgNxGoodsId;
	/**
	 *  
	 */
	private String nxDgGoodsBrand;
	/**
	 *  
	 */
	private String nxDgGoodsPlace;
	/**
	 *  商品状态
	 */
	private Integer nxDgToNxDisGoodsId;

}

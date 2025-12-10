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
	private String nxDgGoodsLsName;
	/**
	 *  商品详细
	 */
	private String nxDgGoodsLsDetail;
	/**
	 *  商品规格
	 */
	private String nxDgGoodsLsStandardname;
	/**
	 *  社区商品拼音
	 */
	private String nxDgGoodsLsPinyin;
	/**
	 *  社区商品拼音简拼
	 */
	private String nxDgGoodsLsPy;

	/**
	 *  
	 */
	private String nxDgGoodsLsBrand;
	/**
	 *  
	 */
	private String nxDgGoodsLsPlace;
	private String nxDgApplyDate;
	private String nxDgGoodsLsFile;
	private String nxDgGoodsLsFileLarge;
	/**
	 *  商品状态
	 */
	private Integer nxDgToNxDisGoodsId;

	private NxDistributerGoodsEntity nxDistributerGoodsEntity;

}

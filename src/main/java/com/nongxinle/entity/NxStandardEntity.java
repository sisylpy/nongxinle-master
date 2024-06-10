package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 2020-02-24 15:30:57
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxStandardEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxStandardId;
	/**
	 *  规格名称
	 */
	private String nxStandardName;
	/**
	 *  goods商品id
	 */
	private Integer nxSGoodsId;
	/**
	 * 图片
	 */
	private String nxStandardFilePath;

	/**
	 * 规格比例
	 */
	private String nxStandardScale;

	/**
	 * 误差
	 */
	private String nxStandardError;

	/**
	 * 排序
	 */
	private Integer nxStandardSort;

	private Integer nxStandardWeight;

}

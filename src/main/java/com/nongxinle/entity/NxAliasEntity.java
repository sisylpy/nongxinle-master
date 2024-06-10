package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 07-30 18:51
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxAliasEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  别名id
	 */
	private Integer nxAliasId;
	/**
	 *  别名名称
	 */
	private String nxAliasName;
	/**
	 *  别名商品id
	 */
	private Integer nxAlsGoodsId;
	/**
	 *  别名排序
	 */
	private Integer nxAlsSort;

}

package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-26 16:23
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityAdsenseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  广告位id
	 */
	private Integer nxCommunityAdsenseId;
	/**
	 *  广告位图片
	 */
	private String nxCaFilePath;
	/**
	 *  广告位链接
	 */
	private String nxCaClick;
	/**
	 *  社区id
	 */
	private Integer nxCaCommunityId;
	/**
	 *  广告位排序
	 */
	private Integer nxCaSort;

}

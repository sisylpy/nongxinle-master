package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-23 14:26
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityCardEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCommunityCardId;
	/**
	 *  
	 */
	private String nxCommunityCardName;
	/**
	 *  
	 */
	private String nxCcPrice;

	/**
	 *  
	 */
	private String nxCcWords;
	/**
	 *  
	 */
	private String nxCcFilePath;
	/**
	 *  
	 */
	private Integer nxCcCommunityId;
	/**
	 *  
	 */
	private Integer nxCcType;
	/**
	 *  
	 */
	private Integer nxCcStatus;
	private Integer nxCcUserCount;

	/**
	 *  
	 */
	private String nxCcEffectiveDays;

	private List<NxCommunityGoodsEntity> nxCommunityGoodsEntityList;

}

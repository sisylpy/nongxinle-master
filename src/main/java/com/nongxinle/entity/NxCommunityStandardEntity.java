package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 04-30 06:45
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityStandardEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxCommunityStandardId;
	/**
	 *  
	 */
	private Integer nxCsCommGoodsId;
	/**
	 *  
	 */
	private String nxCsStandardName;
	/**
	 *  
	 */
	private String nxCsStandardFilePath;
	/**
	 *  
	 */
	private String nxCsStandardScale;
	/**
	 *  
	 */
	private String nxCsStandardError;
	/**
	 *  
	 */
	private Integer nxCsStandardSort;

	private Boolean isSelect;

}

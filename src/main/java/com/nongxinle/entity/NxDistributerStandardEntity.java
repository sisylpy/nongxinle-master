package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 07-27 21:41
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerStandardEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDistributerStandardId;
	/**
	 *  
	 */
	private Integer nxDsDisGoodsId;
	/**
	 *  
	 */
	private String nxDsStandardName;
	/**
	 *  
	 */
	private String nxDsStandardFilePath;
	/**
	 *  
	 */
	private String nxDsStandardScale;
	/**
	 *  
	 */
	private String nxDsStandardError;
	/**
	 *  
	 */
	private Integer nxDsStandardSort;
	private String nxDsStandardWeight;

}

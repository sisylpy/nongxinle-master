package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-18 21:32
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerStandardEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDistributerStandardId;
	/**
	 *  
	 */
	private Integer gbDsDisGoodsId;
	/**
	 *  
	 */
	private String gbDsStandardName;
	/**
	 *  
	 */
	private String gbDsStandardFilePath;
	/**
	 *  
	 */
	private String gbDsStandardScale;
	/**
	 *  
	 */
	private String gbDsStandardError;
	/**
	 *  
	 */
	private Integer gbDsStandardSort;
	/**
	 *  
	 */
	private String gbDsStandardWeight;

}

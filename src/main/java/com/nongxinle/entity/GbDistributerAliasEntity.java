package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-07 09:12
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerAliasEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDistributerAliasId;
	/**
	 *  
	 */
	private Integer gbDaDisGoodsId;
	/**
	 *  
	 */
	private String gbDaAliasName;

}

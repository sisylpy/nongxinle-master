package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-20 10:21
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;


@Setter@Getter@ToString

public class WxDistributerUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  批发商用户id
	 */
	private Integer wxDistributerUserId;
	/**
	 *  用户名
	 */
	private String wxDiuUserId;

	private String wxDiuOpenUserId;
	private String wxDiuCopId;



}

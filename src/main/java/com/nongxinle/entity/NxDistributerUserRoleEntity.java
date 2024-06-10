package com.nongxinle.entity;

/**
 * 用户与角色对应关系
 * @author lpy
 * @date 06-20 10:55
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerUserRoleEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxDistributerUserRoleId;
	/**
	 *  用户ID
	 */
	private Integer nxDurUserId;
	/**
	 *  角色ID
	 */
	private Integer nxDurRoleId;

}

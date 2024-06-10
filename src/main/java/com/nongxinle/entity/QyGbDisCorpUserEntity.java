package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-02 17:14
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class QyGbDisCorpUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer qyGbDisCorpUserId;
	/**
	 *  
	 */
	private String qyGbDisCorpUserName;
	/**
	 *  
	 */
	private String qyGbDisCorpUserUrl;
	/**
	 *  
	 */
	private String qyGbDisCorpOpenUserId;
	/**
	 *  
	 */
	private Integer qyGbDisCorpQyCorpId;
	/**
	 *  
	 */
	private Integer qyGbDistributerId;
	private String qyGbDisCorpSessionKey;
	private String qyGbDisCorpUserJoinDate;

}

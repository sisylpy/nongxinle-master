package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 07-16 12:25
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class QyNxDisCorpUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer qyNxDisCorpUserId;
	/**
	 *  
	 */
	private String qyNxDisCorpUserName;
	/**
	 *  
	 */
	private String qyNxDisCorpUserUrl;
	/**
	 *  
	 */
	private String qyNxDisCorpOpenUserId;
	/**
	 *  
	 */
	private Integer qyNxDisCorpQyCorpId;
	private Integer qyNxDistributerId;
	private String qyNxDisCorpSessionKey;
	private String qyNxDisCorpUserJoinDate;




	private QyNxDisCorpEntity qyNxDisCorpEntity;

}

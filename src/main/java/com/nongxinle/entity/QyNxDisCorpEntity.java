package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 07-15 15:52
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class QyNxDisCorpEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer qyNxDisCorpId;
	/**
	 *  
	 */
	private String qyNxDisCorpName;
	/**
	 *  
	 */
	private String qyNxDisCorpRoundLogoUrl;
	/**
	 *  
	 */
	private String qyNxDisCorpPermanentCode;
	/**
	 *  
	 */
	private String qyNxDisCorpAccessToken;
	/**
	 *  
	 */
	private String qyNxDisQyCorpId;
	private String qyNxDisCorpJoinDate;

}

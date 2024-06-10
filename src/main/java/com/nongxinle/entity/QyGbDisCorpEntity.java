package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-02 22:59
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class QyGbDisCorpEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer qyGbDisCorpId;
	/**
	 *  
	 */
	private String qyGbDisCorpName;
	/**
	 *  
	 */
	private String qyGbDisCorpRoundLogoUrl;
	/**
	 *  
	 */
	private String qyGbDisCorpPermanentCode;
	/**
	 *  
	 */
	private String qyGbDisCorpAccessToken;
	/**
	 *  
	 */
	private String qyGbDisQyCorpId;
	private String qyGbDisCorpJoinDate;

}

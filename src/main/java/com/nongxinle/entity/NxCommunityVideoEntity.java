package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 07-30 18:51
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Setter@Getter@ToString

public class NxCommunityVideoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	


	private String nxCommunityVideoUserName;
	/**
	 *  别名商品id
	 */
	private String nxCommunityVideoId;

}

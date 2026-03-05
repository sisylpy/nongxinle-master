package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 02-19 20:22
 */

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxDistributerNxDistributerEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键
	 */
	private Integer nxDistributerNxDistributerId;
	/**
	 * 协作伙伴1（存储时约定 id_1 < id_2）
	 * 兼容前端传 nxOrderNxDistributerId
	 */
	@JsonAlias("nxOrderNxDistributerId")
	private Integer nxDistributerId1;
	/**
	 * 协作伙伴2（存储时约定 id_1 < id_2）
	 * 兼容前端传 nxOfferNxDistributerId
	 */
	@JsonAlias("nxOfferNxDistributerId")
	private Integer nxDistributerId2;

	/**
	 * 邀请类型：1=申请成为供货商，2=申请采购我的商品
	 * 1：申请方能看到同意方商品；申请方不给同意方看自己的商品；同意方默认不看申请方商品
	 * 2：同意方不给申请方看商品；申请方不看同意方的商品
	 */
	private Integer inviteType;
	/**
	 * 发起邀请的配送商 id（谁建立的协作关系）
	 */
	private Integer inviterNxDistributerId;

}

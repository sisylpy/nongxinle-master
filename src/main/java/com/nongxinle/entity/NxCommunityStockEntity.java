package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 06-08 09:22
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxCommunityStockEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  社区库存id
	 */
	private Integer nxCommunityStockId;
	/**
	 *  社区库存社区id
	 */
	private Integer nxCsCommunityId;
	/**
	 *  社区库存数量
	 */
	private String nxStockTotal;
	/**
	 *  社区库存请求入库时间
	 */
	private String nxStockRequierDate;
	/**
	 *  社区库存请求入库用户
	 */
	private Integer nxStockRequierUserId;
	/**
	 *  社区库存状态
	 */
	private Integer nxStockStatus;
	/**
	 *  社区库存入库时间
	 */
	private String nxStockInDate;
	/**
	 *  社区库存入库用户
	 */
	private Integer nxStockInUserId;

	private List<NxCommunityStockSubEntity> entityList;

}

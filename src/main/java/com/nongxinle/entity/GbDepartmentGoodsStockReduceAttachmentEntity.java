package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-29 16:35
 */

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDepartmentGoodsStockReduceAttachmentEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer gbDepartmentGoodsStockReduceAttachId;
	/**
	 *  
	 */
	private Integer gbDgsraGbDgsrId;
	/**
	 *  
	 */
	private String gbDgsraContent;
	/**
	 *  
	 */
	private String gbDgsraFilePath;
	private String gbDgsraFileLargePath;
	private String gbDgsraType;
	private Integer gbDgsraStars;
	/**
	 *  
	 */
	private Integer gbDgsraStatus;
	private Integer gbDgsraNxSupplierId;
	private Integer gbDgsraNxDistributerId;

}

package com.nongxinle.entity;

/**
 * 溯源报告实体类
 * 
 * @author lpy
 * @date 2025-01-XX
 */

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxTraceReportEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 溯源报告ID
	 */
	private Integer nxTraceReportId;
	/**
	 * 采购批次ID（关联nx_distributer_purchase_batch）
	 */
	private Integer nxTrBatchId;
	/**
	 * 供应商ID（关联供应商表）
	 */
	private Integer nxTrSupplierId;
	/**
	 * 供应商名称（冗余字段，便于查询显示）
	 */
	private String nxTrSupplierName;
	/**
	 * 供应商联系方式
	 */
	private String nxTrSupplierContact;
	/**
	 * 采购日期
	 */
	private String nxTrPurchaseDate;
	/**
	 * 入库日期
	 */
	private String nxTrStockInDate;
	/**
	 * 报告有效期开始日期
	 */
	private String nxTrValidStartDate;
	/**
	 * 报告有效期结束日期
	 */
	private String nxTrValidEndDate;
	/**
	 * 报告类型（image/pdf/excel等）
	 */
	private String nxTrReportType;
	/**
	 * 报告文件路径（单个文件）
	 */
	private String nxTrFilePath;
	/**
	 * 报告文件路径（多个文件，JSON格式存储）
	 */
	private String nxTrFilePaths;
	/**
	 * 报告文件类型（MIME类型或扩展名）
	 */
	private String nxTrFileType;
	/**
	 * 配送商ID（关联配送商表）
	 */
	private Integer nxTrDistributerId;
	/**
	 * 创建用户ID
	 */
	private Integer nxTrCreateUserId;
	/**
	 * 创建时间
	 */
	private String nxTrCreateTime;
	/**
	 * 更新时间
	 */
	private String nxTrUpdateTime;
	/**
	 * 备注说明
	 */
	private String nxTrRemark;

}


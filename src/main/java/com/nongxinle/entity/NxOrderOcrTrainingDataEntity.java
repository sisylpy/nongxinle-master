package com.nongxinle.entity;

/**
 * 订单OCR训练数据实体类
 * 
 * @author lpy
 * @date 2025-01-XX
 */

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxOrderOcrTrainingDataEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  训练数据ID
	 */
	private Integer nxOtdId;
	/**
	 *  订单ID
	 */
	private Integer nxOtdOrderId;
	/**
	 *  部门ID
	 */
	private Integer nxOtdDepartmentId;
	/**
	 *  父级部门ID
	 */
	private Integer nxOtdDepartmentFatherId;
	/**
	 *  分销商ID
	 */
	private Integer nxOtdDistributerId;
	/**
	 *  商品ID（分销商商品ID）
	 */
	private Integer nxOtdDisGoodsId;
	/**
	 *  原始商品名称值
	 */
	private String nxOtdOriginalGoodsName;
	/**
	 *  商品名称是否手动标注（0=未标注，1=已标注）
	 */
	private Integer nxOtdIsNameManuallyAnnotated;
	/**
	 *  最终确认的商品名称值
	 */
	private String nxOtdFinalGoodsName;
	/**
	 *  DeepSeek 推荐的商品名称（纠错后的名称）
	 */
	private String nxOtdDeepseekRecommendedName;
	/**
	 *  原始订货数量值
	 */
	private String nxOtdOriginalQuantity;
	/**
	 *  订货数量是否手动标注（0=未标注，1=已标注）
	 */
	private Integer nxOtdIsQuantityManuallyAnnotated;
	/**
	 *  最终确认的订货数量值
	 */
	private String nxOtdFinalQuantity;
	/**
	 *  原始订货规格值
	 */
	private String nxOtdOriginalStandard;
	/**
	 *  订货规格是否手动标注（0=未标注，1=已标注）
	 */
	private Integer nxOtdIsStandardManuallyAnnotated;
	/**
	 *  最终确认的订货规格值
	 */
	private String nxOtdFinalStandard;
	/**
	 *  原始规格重量值
	 */
	private String nxOtdOriginalStandardWeight;
	/**
	 *  规格重量是否手动标注（0=未标注，1=已标注）
	 */
	private Integer nxOtdIsStandardWeightManuallyAnnotated;
	/**
	 *  最终确认的规格重量值
	 */
	private String nxOtdFinalStandardWeight;
	/**
	 *  原始备注值
	 */
	private String nxOtdOriginalRemark;
	/**
	 *  备注是否手动标注（0=未标注，1=已标注）
	 */
	private Integer nxOtdIsRemarkManuallyAnnotated;
	/**
	 *  最终确认的备注值
	 */
	private String nxOtdFinalRemark;
	/**
	 *  数据来源
	 */
	private String nxOtdDataSource;
	/**
	 *  创建时间
	 */
	private String nxOtdCreateDate;
	/**
	 *  更新时间
	 */
	private String nxOtdUpdateDate;
	/**
	 *  创建人ID
	 */
	private Integer nxOtdCreateUserId;
	/**
	 *  OCR原文（该商品对应的OCR文本行，清洗后）
	 */
	private String nxOtdOcrText;

}

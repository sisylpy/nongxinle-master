package com.nongxinle.entity;

/**
 * OCR任务实体类
 * 用于记录OCR识别订单的任务信息，包括图片存储、订单统计、用户信息等
 * 
 * @author lpy
 * @date 2025-01-24
 */

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class NxOcrTaskEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * OCR任务ID（主键）
	 */
	private Integer nxOcrTaskId;
	
	/**
	 * 文件名（原始文件名）
	 */
	private String nxOcrTaskFileName;
	
	/**
	 * 图片存储路径（相对路径，如：ocrImages/20250124/xxx.jpg）
	 */
	private String nxOcrTaskImagePath;
	
	/**
	 * 总订单条数（初步解析出来的所有订单的总数）
	 */
	private Integer nxOcrTaskTotalOrders;
	
	/**
	 * 已完成订单数（订单状态为0的订单数量）
	 */
	private Integer nxOcrTaskCompletedOrders;
	
	/**
	 * 未完成订单数（订单状态为-2的订单数量）
	 */
	private Integer nxOcrTaskPendingOrders;
	
	/**
	 * 上传时间（记录图片或文件被上传的时间点）
	 */
	private String nxOcrTaskUploadTime;
	
	/**
	 * 上传用户ID（负责上传图片的用户ID）
	 */
	private Integer nxOcrTaskUploadUserId;
	
	/**
	 * 上传用户名称（负责上传图片的用户名称）
	 */
	private String nxOcrTaskUploadUserName;
	
	/**
	 * 处理人ID（负责解析和处理这些订单的工作人员ID）
	 */
	private Integer nxOcrTaskProcessorUserId;
	
	/**
	 * 处理人名称（负责解析和处理这些订单的工作人员名称）
	 */
	private String nxOcrTaskProcessorUserName;
	
	/**
	 * 任务状态（0=处理中，1=已完成，2=部分完成）
	 */
	private Integer nxOcrTaskStatus;
	
	/**
	 * 创建时间
	 */
	private String nxOcrTaskCreateDate;
	
	/**
	 * 更新时间
	 */
	private String nxOcrTaskUpdateDate;
	
	/**
	 * 分销商ID
	 */
	private Integer nxOcrTaskDistributerId;
	
	/**
	 * 部门ID
	 */
	private Integer nxOcrTaskDepartmentId;
	
	/**
	 * 部门父ID
	 */
	private Integer nxOcrTaskDepartmentFatherId;
	
	/**
	 * 部门名称（查询时关联获取，不存储在任务表中）
	 */
	private String nxOcrTaskDepartmentName;
	
	/**
	 * OCR识别的原始文本内容（用于后续使用 DeepSeek 重新解析）
	 */
	private String nxOcrTaskOcrText;
	
	/**
	 * 任务类型（1=图片，2=Excel，3=文字）
	 */
	private Integer nxOcrTaskType;

}
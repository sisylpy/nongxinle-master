package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 05-29 06:59
 */

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.poi.hpsf.Decimal;


@Setter@Getter@ToString

public class NxAiForecastLogEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  
	 */
	private Integer nxAiForecastLogId;
	/**
	 *  
	 */
	private Integer nxDepartmentId;
	/**
	 *  
	 */
	private Integer nxDisGoodsId;
	/**
	 *  
	 */
	private String nxForecastDate;
	/**
	 *  
	 */
	private BigDecimal nxPredictedQuantity;
	/**
	 *  
	 */
	private BigDecimal nxActualQuantity;
	/**
	 *  
	 */
	private Integer nxDayOfWeek;
	/**
	 *  
	 */
	private BigDecimal nxLag1;
	/**
	 *  
	 */
	private BigDecimal nxLag2;
	/**
	 *  
	 */
	private BigDecimal nxLag3;
	/**
	 *  
	 */
	private Date nxCreatedAt;
	/**
	 *  
	 */
	private Integer nxCurrentDow;
	private Integer nxLag1Dow;
	/**
	 *  
	 */
	private Integer nxLag2Dow;
	/**
	 *  
	 */
	private Integer nxLag3Dow;
	/**
	 *  
	 */
	private BigDecimal nxAiApplyQuantity;
	private BigDecimal nxAiError;
	private BigDecimal nxAiErrorPct;
	/**
	 *  
	 */
	private String nxAiApplyStandard;

}

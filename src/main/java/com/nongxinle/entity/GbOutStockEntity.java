package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 10-22 16:10
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;


@Setter@Getter@ToString

public class GbOutStockEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String percent;
	private String showPercent;
	private String total;
	private List<GbDistributerFatherGoodsEntity> arr;




}

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

public class GbOutStockArrEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String lastDouble;
	private String outDouble;
	private String restDouble;
	private String stockDouble;
	List<GbOutStockEntity> cost;
	List<GbOutStockEntity> loss;
	List<GbOutStockEntity> waste;
	List<GbOutStockEntity> returnS;





}

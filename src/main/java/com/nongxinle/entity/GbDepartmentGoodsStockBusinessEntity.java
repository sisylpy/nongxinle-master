package com.nongxinle.entity;

/**
 * 
 * @author lpy
 * @date 08-19 19:02
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;


@Setter@Getter@ToString

public class GbDepartmentGoodsStockBusinessEntity implements Serializable,Comparable{
	private static final long serialVersionUID = 1L;

	private String gbBusinessFullTime;
	private String gbBusinessType;
	private String gbBusinessWeight;
	private BigDecimal gbBusinessResultWeight;
	private String gbApplyDepartment ;


	@Override
	public int compareTo(Object o) {
		if (o instanceof GbDepartmentGoodsStockBusinessEntity) {
			GbDepartmentGoodsStockBusinessEntity e = (GbDepartmentGoodsStockBusinessEntity) o;
			return e.getGbBusinessFullTime().compareTo(this.getGbBusinessFullTime());

		}
		return 0;
	}
}

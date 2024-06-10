package com.nongxinle.entity;

/**
 * 用户与角色对应关系
 * @author lpy
 * @date 08-19 09:57
 */

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter@Getter@ToString

public class GbDistributerGoodsShelfEntity implements Serializable ,Comparable{
	private static final long serialVersionUID = 1L;
	
	/**
	 *  货架id
	 */
	private Integer gbDistributerGoodsShelfId;
	/**
	 *  货架名称
	 */
	private String gbDistributerGoodsShelfName;
	/**
	 *  货架排序
	 */
	private Integer gbDistributerGoodsShelfSort;
	/**
	 *  批发商id
	 */
	private Integer gbDistributerGoodsShelfDisId;
	private Integer gbDistributerGoodsShelfDepId;

	private List<GbDistributerGoodsShelfGoodsEntity> gbDisGoodsShelfGoodsEntities;
	private TreeSet<GbDistributerGoodsShelfGoodsEntity> treeSet;
	private Boolean isSelected = false;


	@Override
	public int compareTo(Object o) {

		if (o instanceof GbDistributerGoodsShelfEntity) {
			GbDistributerGoodsShelfEntity e = (GbDistributerGoodsShelfEntity) o;
			return this.getGbDistributerGoodsShelfId().compareTo(e.getGbDistributerGoodsShelfId());
		}
		return 0;
	}
}

package com.nongxinle.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 社区商品媒体资源表(图片/视频)
 *
 * @author lpy
 * @date 2026-04-12 11:51:00
 */
@Setter@Getter@ToString
public class NxCommunityGoodsMediaEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	private Long nxCommGoodsMediaId;

	/**
	 * 关联的商品ID
	 */
	private Long nxCommGoodsId;

	/**
	 * 媒体类型: 1=主图/封面, 2=详情图, 3=视频
	 */
	private Integer nxCommGoodsMediaType;

	/**
	 * 图片/视频URL地址
	 */
	private String nxCommGoodsMediaUrl;

	/**
	 * 视频封面缩略图(仅视频类型使用)
	 */
	private String nxCommGoodsMediaThumbnailUrl;

	/**
	 * 排序权重，越小越靠前
	 */
	private Integer nxCommGoodsMediaSort;

	/**
	 * 是否主图: 0=否, 1=是
	 */
	private Integer nxCommGoodsMediaIsPrimary;

	/**
	 * 状态: 0=禁用, 1=启用
	 */
	private Integer nxCommGoodsMediaStatus;

	/**
	 * 文件大小(字节)
	 */
	private Long nxCommGoodsMediaFileSize;

	/**
	 * 原始文件名
	 */
	private String nxCommGoodsMediaFileName;

	/**
	 * 创建时间
	 */
	private Date nxCommGoodsMediaCreateTime;

	/**
	 * 更新时间
	 */
	private Date nxCommGoodsMediaUpdateTime;
}

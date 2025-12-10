package com.nongxinle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 系统商品候选DTO（只包含前端选择商品需要的字段）
 * 用于粘贴搜索商品接口的候选商品列表
 * 
 * @author lpy
 */
@Setter
@Getter
@ToString
public class NxGoodsCandidateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 系统商品ID
     */
    private Integer nxGoodsId;

    /**
     * 商品名称
     */
    private String nxGoodsName;

    /**
     * 规格名称
     */
    private String nxGoodsStandardname;

    /**
     * 规格重量
     */
    private String nxGoodsStandardWeight;

    /**
     * 箱单位
     */
    private String nxGoodsCartonUnit;

    /**
     * 品牌
     */
    private String nxGoodsBrand;

    /**
     * 商品图片
     */
    private String nxGoodsFile;

    /**
     * 父级ID
     */
    private Integer nxGoodsFatherId;
}











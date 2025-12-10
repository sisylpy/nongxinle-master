package com.nongxinle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 分销商商品候选DTO（只包含前端选择商品需要的字段）
 * 用于粘贴搜索商品接口的候选商品列表
 * 
 * @author lpy
 */
@Setter
@Getter
@ToString
public class DistributerGoodsCandidateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分销商商品ID
     */
    private Integer nxDistributerGoodsId;

    /**
     * 商品名称
     */
    private String nxDgGoodsName;

    /**
     * 规格名称
     */
    private String nxDgGoodsStandardname;

    /**
     * 规格重量
     */
    private String nxDgGoodsStandardWeight;

    /**
     * 箱单位
     */
    private String nxDgCartonUnit;

    /**
     * 品牌
     */
    private String nxDgGoodsBrand;

    /**
     * 商品图片
     */
    private String nxDgGoodsFile;

    /**
     * 系统商品ID
     */
    private Integer nxDgNxGoodsId;
}




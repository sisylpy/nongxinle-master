package com.nongxinle.dto;

import lombok.Data;

/**
 * 商品简化DTO（用于deepseek接口）
 * 只包含 id, goodname, fatherid
 */
@Data
public class NxGoodsSimpleDTO {
    private Integer id;        // 商品ID
    private String goodname;   // 商品名称
    private Integer fatherid;  // 父级ID

//    private Integer nxGoodsId;
//    /**
//     *  商品名称
//     */
//    private String nxGoodsName;
//    /**
//     *  商品规格
//     */
//    private String nxGoodsStandardname;
//    /**
//     *  商品品牌
//     */
//    private String nxGoodsBrand;
//    /**
//     *  商品产地
//     */
//    private String nxGoodsPlace;
//    private Integer nxGoodsFatherId;
//    private Integer nxGoodsLevel;
//    private Integer nxGoodsIsHidden;
//    //规格重量 例如 500克
//    private String nxGoodsStandardWeight;


}


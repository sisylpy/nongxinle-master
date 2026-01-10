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
}


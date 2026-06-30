package com.nongxinle.dto.platform.admin.coupon;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlatformCouponNxGoodsCategoryOptionDto {

    /** nx_goods 分类节点 id（一级/二级/三级均可） */
    private Integer nxGoodsId;
    private String nxGoodsName;
    /** 一级 / 二级 / 三级 */
    private String levelLabel;
    /** 展示路径，如 蔬菜 / 叶菜 / 白菜 */
    private String categoryPath;
}

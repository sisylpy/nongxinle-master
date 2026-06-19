package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformSupplierItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer disGoodsId;
    private Integer distributerId;
    private String distributerName;
    private Integer nxGoodsId;
    private String goodsName;
    private String standard;
    private String brand;
    private String place;
    /** 配送商当前销售报价（nx_dg_will_price_one / nx_dg_will_price，非进价） */
    private String currentQuotePrice;
    /** 该客户对该配送商商品的历史成交价，无则 null */
    private String customerHistoryPrice;
    private Integer isDefaultRecommend;
}

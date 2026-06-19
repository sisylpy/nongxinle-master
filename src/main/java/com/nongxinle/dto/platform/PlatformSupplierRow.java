package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;

/** MyBatis 映射：市场内标准商品可选配送商 */
@Setter
@Getter
public class PlatformSupplierRow {
    private Integer disGoodsId;
    private Integer distributerId;
    private String distributerName;
    private Integer nxGoodsId;
    private String goodsName;
    private String standard;
    private String brand;
    private String place;
    private String willPriceOne;
    private String willPrice;
    private String customerHistoryPrice;
    private Integer defaultDistributerId;
    private Integer defaultDisGoodsId;
}

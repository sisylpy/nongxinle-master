package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class PlatformCustomerGoodsCatalogListRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer marketId;
    /** 一级分类 nx_goods_id（great grand） */
    private Integer greatGrandId;
    private Integer departmentId;
    private Integer page;
    private Integer limit;
}

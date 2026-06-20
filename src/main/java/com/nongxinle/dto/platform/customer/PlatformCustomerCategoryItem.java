package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class PlatformCustomerCategoryItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer categoryId;
    private String categoryName;
    private String icon;
    private Integer goodsCount;
    private Integer supplierCount;
    private List<PlatformCustomerCategoryChildItem> children = new ArrayList<>();
}

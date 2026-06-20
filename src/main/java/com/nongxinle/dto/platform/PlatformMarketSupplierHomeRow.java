package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformMarketSupplierHomeRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer distributerId;
    private String distributerName;
    private String logo;
    private String address;
    private String mainCategories;
    private Integer goodsCount;
}

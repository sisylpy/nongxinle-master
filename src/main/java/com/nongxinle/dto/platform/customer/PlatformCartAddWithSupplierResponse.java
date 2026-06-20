package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class PlatformCartAddWithSupplierResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer nxDistributerId;
    private String supplierName;
    private Integer addedLineCount;
    private List<PlatformCartLineItem> lines;
}

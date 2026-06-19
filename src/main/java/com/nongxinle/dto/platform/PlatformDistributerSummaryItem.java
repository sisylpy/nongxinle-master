package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@ToString
public class PlatformDistributerSummaryItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer distributerId;
    private String distributerName;
    private Integer lineCount;
    private BigDecimal subtotalSum;
}

package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@ToString
public class PlatformPendingResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer marketId;
    private String applyDate;
    private Integer totalPendingLines;
    private List<PlatformPendingCustomerItem> customers;
}

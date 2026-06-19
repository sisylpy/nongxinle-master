package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@ToString
public class PlatformOrderDetailResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer marketId;
    private Integer departmentId;
    private String departmentName;
    private String applyDate;
    private List<PlatformOrderDetailLine> lines;
    private List<PlatformDistributerSummaryItem> distributerSummary;
}

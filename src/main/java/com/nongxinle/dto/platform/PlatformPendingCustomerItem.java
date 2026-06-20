package com.nongxinle.dto.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@ToString
public class PlatformPendingCustomerItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer departmentId;
    private String departmentName;
    private String departmentOrderCode;
    private Integer pendingLineCount;
    private Integer assignedLineCount;
    private Integer totalLineCount;
    private List<Integer> orderIds;
    private String firstPendingAt;
    private String lastPendingAt;
}

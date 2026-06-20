package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class PlatformCartListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer gbDepartmentId;
    private Integer lineCount;
    private List<PlatformCartLineItem> lines;
}

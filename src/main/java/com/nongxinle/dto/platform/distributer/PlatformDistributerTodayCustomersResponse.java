package com.nongxinle.dto.platform.distributer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class PlatformDistributerTodayCustomersResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<PlatformDistributerCustomerItem> customers = new ArrayList<>();
    private Integer customerCount;
    private Integer unDoTotal;
}

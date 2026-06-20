package com.nongxinle.dto.platform.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class PlatformCustomerHomeInitResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private PlatformCustomerMarketInfo market;
    private PlatformCustomerDepartmentInfo currentDepartment;
    private PlatformCustomerSearchInfo search;
    private PlatformCustomerHomeSummary summary;
    private Boolean hasOutstandingBill;
    private PlatformOutstandingBillInfo outstandingBill;
}

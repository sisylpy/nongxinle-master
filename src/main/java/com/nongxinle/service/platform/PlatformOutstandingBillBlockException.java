package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.customer.PlatformOutstandingBillInfo;

public class PlatformOutstandingBillBlockException extends RuntimeException {

    private final PlatformOutstandingBillInfo data;

    public PlatformOutstandingBillBlockException(String message, PlatformOutstandingBillInfo data) {
        super(message);
        this.data = data;
    }

    public PlatformOutstandingBillInfo getData() {
        return data;
    }
}

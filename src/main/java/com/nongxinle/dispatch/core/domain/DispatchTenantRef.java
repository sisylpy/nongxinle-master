package com.nongxinle.dispatch.core.domain;

import lombok.Getter;
import lombok.Setter;

/** 派单租户引用；core 不依赖 disId / communityId 等业务主键类型。 */
@Getter
@Setter
public class DispatchTenantRef {

    private DispatchTenantType tenantType;
    private Integer tenantId;
    /** 可选批次，如 Nx MORNING batch。 */
    private String batchCode;

    public static DispatchTenantRef of(DispatchTenantType tenantType, Integer tenantId) {
        DispatchTenantRef ref = new DispatchTenantRef();
        ref.setTenantType(tenantType);
        ref.setTenantId(tenantId);
        return ref;
    }
}

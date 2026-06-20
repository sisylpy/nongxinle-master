package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.BillCreationPartition;
import com.nongxinle.entity.GbDepartmentOrdersEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;

import java.util.List;

public interface GbBillCreationGuardService {

    BillCreationPartition partitionNxOrdersForLegacyBill(List<NxDepartmentOrdersEntity> nxOrders);

    void assertGbOrdersCanAttachToNewLegacyBill(List<GbDepartmentOrdersEntity> gbOrders);
}

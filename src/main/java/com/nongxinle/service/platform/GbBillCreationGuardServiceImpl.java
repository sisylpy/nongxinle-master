package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.BillCreationPartition;
import com.nongxinle.entity.GbDepartmentBillEntity;
import com.nongxinle.entity.GbDepartmentOrdersEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.service.GbDepartmentBillService;
import com.nongxinle.service.GbDepartmentOrdersService;
import com.nongxinle.utils.GbBillPlatformConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class GbBillCreationGuardServiceImpl implements GbBillCreationGuardService {

    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;

    @Override
    public BillCreationPartition partitionNxOrdersForLegacyBill(List<NxDepartmentOrdersEntity> nxOrders) {
        BillCreationPartition partition = new BillCreationPartition();
        if (nxOrders == null || nxOrders.isEmpty()) {
            return partition;
        }

        Set<Integer> platformBillIds = new LinkedHashSet<>();
        for (NxDepartmentOrdersEntity nxOrder : nxOrders) {
            GbDepartmentOrdersEntity gbOrder = loadGbOrder(nxOrder);
            if (gbOrder == null || gbOrder.getGbDoBillId() == null) {
                partition.getLegacyCreatableLines().add(nxOrder);
                continue;
            }

            GbDepartmentBillEntity bill = gbDepartmentBillService.queryObject(gbOrder.getGbDoBillId());
            if (bill != null && GbBillPlatformConstants.BILL_SOURCE_PLATFORM_CASH.equals(bill.getGbDbBillSource())) {
                partition.getPlatformCashLines().add(nxOrder);
                platformBillIds.add(bill.getGbDepartmentBillId());
            } else {
                partition.getLegacyCreatableLines().add(nxOrder);
            }
        }

        partition.setPlatformCashBillIds(platformBillIds);

        if (platformBillIds.size() > 1) {
            partition.getErrors().add("同一批次 NX 行关联多个 PLATFORM_CASH bill，拒绝自动合并: " + platformBillIds);
        }

        if (!partition.getPlatformCashLines().isEmpty() && !partition.getLegacyCreatableLines().isEmpty()) {
            partition.getWarnings().add("本批次含 "
                    + partition.getPlatformCashLines().size()
                    + " 条平台现金行已跳过 legacy bill 新建/计总额；"
                    + partition.getLegacyCreatableLines().size()
                    + " 条 legacy 行继续处理");
        }

        return partition;
    }

    @Override
    public void assertGbOrdersCanAttachToNewLegacyBill(List<GbDepartmentOrdersEntity> gbOrders) {
        if (gbOrders == null || gbOrders.isEmpty()) {
            return;
        }
        Set<Integer> existingBillIds = new HashSet<>();
        for (GbDepartmentOrdersEntity gbOrder : gbOrders) {
            if (gbOrder == null || gbOrder.getGbDoBillId() == null) {
                continue;
            }
            GbDepartmentBillEntity bill = gbDepartmentBillService.queryObject(gbOrder.getGbDoBillId());
            if (bill != null && GbBillPlatformConstants.BILL_SOURCE_PLATFORM_CASH.equals(bill.getGbDbBillSource())) {
                throw new IllegalStateException("商品行已属于平台现金 bill "
                        + gbOrder.getGbDoBillId() + "，不能通过 saveAccountBillGb 重复建单");
            }
            existingBillIds.add(gbOrder.getGbDoBillId());
        }
        if (existingBillIds.size() > 1) {
            throw new IllegalStateException("同一批次商品行混入多个已有 bill: " + existingBillIds);
        }
    }

    private GbDepartmentOrdersEntity loadGbOrder(NxDepartmentOrdersEntity nxOrder) {
        if (nxOrder.getNxDoGbDepartmentOrderId() == null) {
            return null;
        }
        return gbDepartmentOrdersService.queryObject(nxOrder.getNxDoGbDepartmentOrderId());
    }
}

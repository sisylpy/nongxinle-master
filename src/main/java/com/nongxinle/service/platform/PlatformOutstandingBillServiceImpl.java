package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.customer.PlatformOutstandingBillInfo;
import com.nongxinle.entity.GbDepartmentBillEntity;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.service.GbDepartmentBillService;
import com.nongxinle.service.NxDistributerService;
import com.nongxinle.utils.GbBillPlatformConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlatformOutstandingBillServiceImpl implements PlatformOutstandingBillService {

    private static final Logger log = LoggerFactory.getLogger(PlatformOutstandingBillServiceImpl.class);

    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private NxDistributerService nxDistributerService;

    @Override
    public GbDepartmentBillEntity findBlockingPlatformCashBill(Integer gbDepartmentId) {
        if (gbDepartmentId == null) {
            return null;
        }
        GbDepartmentBillEntity bill = gbDepartmentBillService.queryBlockingPlatformCashBillByDep(gbDepartmentId);
        if (bill == null || !GbBillPlatformConstants.blocksNewOrder(bill.getGbDbPayStatus())) {
            log.info("[platform/outstandingBill] gbDepartmentId={} blockingBill=null", gbDepartmentId);
            return null;
        }
        log.info("[platform/outstandingBill] gbDepartmentId={} blockingBillId={} payStatus={} billSource={} depId={} depFatherId={} knownTotal={} paidTotal={} supplementDue={} submitToken={}",
                gbDepartmentId,
                bill.getGbDepartmentBillId(),
                bill.getGbDbPayStatus(),
                bill.getGbDbBillSource(),
                bill.getGbDbDepId(),
                bill.getGbDbDepFatherId(),
                bill.getGbDbKnownTotal(),
                bill.getGbDbPaidTotal(),
                bill.getGbDbSupplementDue(),
                bill.getGbDbPlatformSubmitToken());
        return bill;
    }

    @Override
    public boolean hasOutstandingPlatformCashBill(Integer gbDepartmentId) {
        return findBlockingPlatformCashBill(gbDepartmentId) != null;
    }

    @Override
    public PlatformOutstandingBillInfo buildOutstandingInfo(GbDepartmentBillEntity bill) {
        if (bill == null) {
            return null;
        }
        NxDistributerEntity supplier = null;
        if (bill.getGbDbIssueNxDisId() != null) {
            supplier = nxDistributerService.queryObject(bill.getGbDbIssueNxDisId());
        }
        return PlatformBillPaySummaryHelper.toOutstandingInfo(bill, supplier);
    }

    @Override
    public PlatformOutstandingBillInfo findOutstandingInfo(Integer gbDepartmentId) {
        return buildOutstandingInfo(findBlockingPlatformCashBill(gbDepartmentId));
    }

    @Override
    public void assertNotBlockedForNewSubmit(Integer gbDepartmentId) {
        GbDepartmentBillEntity bill = findBlockingPlatformCashBill(gbDepartmentId);
        if (bill == null) {
            return;
        }
        NxDistributerEntity supplier = bill.getGbDbIssueNxDisId() == null
                ? null
                : nxDistributerService.queryObject(bill.getGbDbIssueNxDisId());
        PlatformOutstandingBillInfo data = PlatformBillPaySummaryHelper.toBlockedInfo(bill, supplier);
        String message = PlatformBillPaySummaryHelper.resolveMessage(bill.getGbDbPayStatus());
        log.warn("[platform/outstandingBill] block submit gbDepartmentId={} billId={} payStatus={} message={} data={}",
                gbDepartmentId, bill.getGbDepartmentBillId(), bill.getGbDbPayStatus(), message, data);
        throw new PlatformOutstandingBillBlockException(message, data);
    }
}

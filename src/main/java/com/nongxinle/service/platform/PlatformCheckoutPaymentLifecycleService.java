package com.nongxinle.service.platform;

import com.nongxinle.dao.PlatformCheckoutPaymentDao;
import com.nongxinle.entity.PlatformCheckoutPaymentEntity;
import com.nongxinle.utils.GbBillPlatformConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static com.nongxinle.utils.DateUtils.formatWhatYearDayTime;

/**
 * checkout payment 超时关闭 / 取消 / 锁定判定。
 */
@Service
public class PlatformCheckoutPaymentLifecycleService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${platform.checkout.payment.pending-timeout-minutes:15}")
    private int pendingTimeoutMinutes;

    @Autowired
    private PlatformCheckoutPaymentDao platformCheckoutPaymentDao;

    public int getPendingTimeoutMinutes() {
        return pendingTimeoutMinutes;
    }

    public String computeExpireAt() {
        return LocalDateTime.now().plusMinutes(pendingTimeoutMinutes).format(TIME_FMT);
    }

    public boolean isActiveLock(PlatformCheckoutPaymentEntity payment) {
        if (payment == null) {
            return false;
        }
        return GbBillPlatformConstants.PAYMENT_STATUS_PENDING.equals(payment.getPcpStatus())
                && !isExpiredByTime(payment);
    }

    public boolean isExpiredByTime(PlatformCheckoutPaymentEntity payment) {
        if (payment == null || StringUtils.isBlank(payment.getPcpExpireAt())) {
            return false;
        }
        try {
            LocalDateTime expireAt = LocalDateTime.parse(payment.getPcpExpireAt().trim(), TIME_FMT);
            return LocalDateTime.now().isAfter(expireAt);
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    public boolean isExpiredStatus(String status) {
        return GbBillPlatformConstants.PAYMENT_STATUS_EXPIRED.equals(status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void releaseExpiredPendingPayments(Integer gbDepartmentId) {
        if (gbDepartmentId == null) {
            return;
        }
        List<PlatformCheckoutPaymentEntity> pending =
                platformCheckoutPaymentDao.queryPendingByGbDepartmentId(gbDepartmentId);
        if (pending == null) {
            return;
        }
        for (PlatformCheckoutPaymentEntity payment : pending) {
            if (isExpiredByTime(payment)) {
                closeIfPending(payment.getPcpId(), GbBillPlatformConstants.PAYMENT_STATUS_EXPIRED);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public PlatformCheckoutPaymentEntity refreshAndCloseIfExpired(PlatformCheckoutPaymentEntity payment) {
        if (payment == null) {
            return null;
        }
        if (GbBillPlatformConstants.PAYMENT_STATUS_PENDING.equals(payment.getPcpStatus())
                && isExpiredByTime(payment)) {
            closeIfPending(payment.getPcpId(), GbBillPlatformConstants.PAYMENT_STATUS_EXPIRED);
            return platformCheckoutPaymentDao.queryObject(payment.getPcpId());
        }
        return payment;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean closeIfPending(Integer paymentId, String targetStatus) {
        if (paymentId == null || StringUtils.isBlank(targetStatus)) {
            return false;
        }
        PlatformCheckoutPaymentEntity patch = new PlatformCheckoutPaymentEntity();
        patch.setPcpId(paymentId);
        patch.setPcpStatus(targetStatus);
        patch.setPcpClosedAt(formatWhatYearDayTime(0));
        patch.setPcpUpdatedAt(formatWhatYearDayTime(0));
        return platformCheckoutPaymentDao.closeIfPending(patch) == 1;
    }
}

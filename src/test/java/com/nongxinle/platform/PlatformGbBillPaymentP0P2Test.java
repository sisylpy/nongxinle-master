package com.nongxinle.platform;

import com.nongxinle.dto.platform.LineAmountConfirmResult;
import com.nongxinle.entity.GbDepartmentBillEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.service.platform.GbBillPaymentRecalcServiceImpl;
import com.nongxinle.service.platform.PlatformLineAmountConfirmServiceImpl;
import com.nongxinle.utils.GbBillPlatformConstants;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;


public class PlatformGbBillPaymentP0P2Test {

    private final PlatformLineAmountConfirmServiceImpl lineConfirmService = new PlatformLineAmountConfirmServiceImpl();

    @Test
    public void greenCabbageThreeJinIsConfirmedEvenWhenGoodsIsWeight() {
        NxDistributerGoodsEntity goods = baseGoods();
        goods.setNxDgGoodsIsWeight(1);
        goods.setNxDgGoodsStandardname("斤");
        goods.setNxDgWillPrice("1.30");

        LineAmountConfirmResult result = lineConfirmService.isLineAmountConfirmable("3", "斤", goods);

        Assert.assertTrue(result.isConfirmable());
        Assert.assertEquals(GbBillPlatformConstants.PRICE_CONFIRM_CONFIRMED, result.getPriceConfirmStatus());
        Assert.assertEquals(new BigDecimal("3.90"), result.getLineSubtotal());
    }

    @Test
    public void radishOnePiecePricedByJinIsPending() {
        NxDistributerGoodsEntity goods = baseGoods();
        goods.setNxDgGoodsStandardname("斤");
        goods.setNxDgWillPrice("2.00");

        LineAmountConfirmResult result = lineConfirmService.isLineAmountConfirmable("1", "个", goods);

        Assert.assertFalse(result.isConfirmable());
        Assert.assertEquals(GbBillPlatformConstants.PRICE_CONFIRM_PENDING, result.getPriceConfirmStatus());
        Assert.assertEquals("NEED_WEIGH", result.getPendingReason());
    }

    @Test
    public void recalcAwaitFirstPayAndPartialPaidAndSupplement() {
        Assert.assertEquals(
                GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY,
                GbBillPaymentRecalcServiceImpl.resolvePayStatus(
                        bill(GbBillPlatformConstants.PAY_STATUS_NONE),
                        bd("12.50"), bd("0.00"), bd("12.50"), 2, bd("12.50")));

        Assert.assertEquals(
                GbBillPlatformConstants.PAY_STATUS_PARTIAL_PAID,
                GbBillPaymentRecalcServiceImpl.resolvePayStatus(
                        bill(GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY),
                        bd("12.50"), bd("12.50"), bd("12.50"), 1, bd("0.00")));

        Assert.assertEquals(
                GbBillPlatformConstants.PAY_STATUS_AWAIT_SUPPLEMENT,
                GbBillPaymentRecalcServiceImpl.resolvePayStatus(
                        bill(GbBillPlatformConstants.PAY_STATUS_PARTIAL_PAID),
                        bd("12.50"), bd("12.50"), bd("18.50"), 0, bd("6.00")));

        Assert.assertEquals(
                GbBillPlatformConstants.PAY_STATUS_PAID,
                GbBillPaymentRecalcServiceImpl.resolvePayStatus(
                        bill(GbBillPlatformConstants.PAY_STATUS_AWAIT_SUPPLEMENT),
                        bd("12.50"), bd("18.50"), bd("18.50"), 0, bd("0.00")));
    }

    @Test
    public void partialPaidDoesNotBlockNewOrder() {
        Assert.assertFalse(GbBillPlatformConstants.blocksNewOrder(GbBillPlatformConstants.PAY_STATUS_PARTIAL_PAID));
        Assert.assertFalse(GbBillPlatformConstants.blocksNewOrder(GbBillPlatformConstants.PAY_STATUS_NONE));
        Assert.assertTrue(GbBillPlatformConstants.blocksNewOrder(GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY));
        Assert.assertTrue(GbBillPlatformConstants.blocksNewOrder(GbBillPlatformConstants.PAY_STATUS_AWAIT_SUPPLEMENT));
    }

    @Test
    public void allConfirmedUnpaidIsAwaitFirstPay() {
        Assert.assertEquals(
                GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY,
                GbBillPaymentRecalcServiceImpl.resolvePayStatus(
                        bill(GbBillPlatformConstants.PAY_STATUS_NONE),
                        bd("3.90"), bd("0.00"), bd("3.90"), 0, bd("3.90")));
    }

    @Test
    public void recalcBlocksOnlyWhenDebtExists() {
        Assert.assertTrue(GbBillPaymentRecalcServiceImpl.hasOutstandingDebt(
                GbBillPlatformConstants.PAY_STATUS_AWAIT_FIRST_PAY, bd("12.50"), bd("0.00"), bd("12.50")));
        Assert.assertFalse(GbBillPaymentRecalcServiceImpl.hasOutstandingDebt(
                GbBillPlatformConstants.PAY_STATUS_PARTIAL_PAID, bd("12.50"), bd("12.50"), bd("0.00")));
    }

    private static NxDistributerGoodsEntity baseGoods() {
        NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();
        goods.setNxDistributerGoodsId(22151);
        goods.setNxDgGoodsName("绿甘蓝");
        return goods;
    }

    private static GbDepartmentBillEntity bill(String payStatus) {
        GbDepartmentBillEntity bill = new GbDepartmentBillEntity();
        bill.setGbDbPayStatus(payStatus);
        return bill;
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}

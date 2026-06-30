package com.nongxinle.platform.distributer.controller;

import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderIdRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderLinesRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderPriceRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerOrderWeightRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerTodayCustomersRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerTodayCustomersResponse;
import com.nongxinle.service.platform.PlatformDistributerCustomerService;
import com.nongxinle.service.platform.PlatformDistributerOrderService;
import com.nongxinle.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配送商端平台客户 / 平台订单接口（与 GB 饭店客户、自有 nx 客户列表隔离）。
 */
@RestController
@RequestMapping("api/platform/distributer")
public class PlatformDistributerController {

    private static final Logger log = LoggerFactory.getLogger(PlatformDistributerController.class);

    @Autowired
    private PlatformDistributerCustomerService platformDistributerCustomerService;
    @Autowired
    private PlatformDistributerOrderService platformDistributerOrderService;

    @RequestMapping(value = "/customers/today", method = RequestMethod.POST)
    @ResponseBody
    public R listTodayPlatformCustomers(@RequestBody PlatformDistributerTodayCustomersRequest request) {
        log.info("[platform/distributer/customers/today] 收到请求 request={}", request);
        try {
            PlatformDistributerTodayCustomersResponse data =
                    platformDistributerCustomerService.listTodayCustomers(request);
            log.info("[platform/distributer/customers/today] 响应 disId={} customerCount={}",
                    request != null ? request.getDisId() : null, data.getCustomerCount());
            return R.ok("success").put("data", data);
        } catch (IllegalArgumentException e) {
            log.warn("[platform/distributer/customers/today] 参数错误 request={} msg={}", request, e.getMessage());
            return R.error(4002, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/distributer/customers/today] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    /** 平台客户待处理订单行（勿用 phoneGetToFillDepOrders） */
    @RequestMapping(value = "/orders/lines", method = RequestMethod.POST)
    @ResponseBody
    public R listPlatformOrderLines(@RequestBody PlatformDistributerOrderLinesRequest request) {
        log.info("[platform/distributer/orders/lines] request={}", request);
        try {
            return R.ok("success").put("data", platformDistributerOrderService.listOrderLines(request));
        } catch (IllegalArgumentException e) {
            return R.error(4002, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/distributer/orders/lines] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    /** 仅保存出库重量（未完成出库） */
    @RequestMapping(value = "/orders/saveWeight", method = RequestMethod.POST)
    @ResponseBody
    public R savePlatformOrderWeight(@RequestBody PlatformDistributerOrderWeightRequest request) {
        log.info("[platform/distributer/orders/saveWeight] request={}", request);
        try {
            return R.ok("success").put("data", platformDistributerOrderService.saveWeight(request));
        } catch (IllegalArgumentException e) {
            return R.error(4002, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/distributer/orders/saveWeight] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    /** 录入重量并出库完成（同步 READY_FOR_PICKUP） */
    @RequestMapping(value = "/orders/finishOutbound", method = RequestMethod.POST)
    @ResponseBody
    public R finishPlatformOutbound(@RequestBody PlatformDistributerOrderWeightRequest request) {
        log.info("[platform/distributer/orders/finishOutbound] request={}", request);
        try {
            return R.ok("success").put("data", platformDistributerOrderService.finishOutbound(request));
        } catch (IllegalArgumentException e) {
            return R.error(4002, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/distributer/orders/finishOutbound] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    /** 修改平台单实际单价（期望价不变，重算差价/小计） */
    @RequestMapping(value = "/orders/updatePrice", method = RequestMethod.POST)
    @ResponseBody
    public R updatePlatformOrderPrice(@RequestBody PlatformDistributerOrderPriceRequest request) {
        log.info("[platform/distributer/orders/updatePrice] request={}", request);
        try {
            return R.ok("success").put("data", platformDistributerOrderService.updatePrice(request));
        } catch (IllegalArgumentException e) {
            return R.error(4002, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/distributer/orders/updatePrice] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    /** 取消出库（purchase 回退 + fulfillment READY→ASSIGNED） */
    @RequestMapping(value = "/orders/cancelOutbound", method = RequestMethod.POST)
    @ResponseBody
    public R cancelPlatformOutbound(@RequestBody PlatformDistributerOrderIdRequest request) {
        log.info("[platform/distributer/orders/cancelOutbound] request={}", request);
        try {
            return R.ok("success").put("data", platformDistributerOrderService.cancelOutbound(request));
        } catch (IllegalArgumentException e) {
            return R.error(4002, e.getMessage());
        } catch (Exception e) {
            log.error("[platform/distributer/orders/cancelOutbound] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }
}

package com.nongxinle.controller;

import com.nongxinle.dto.platform.customer.PlatformBillPayFirstRequest;
import com.nongxinle.dto.platform.customer.PlatformBillPaySupplementRequest;
import com.nongxinle.dto.platform.customer.PlatformCartAddWithSupplierResponse;
import com.nongxinle.dto.platform.customer.PlatformCartLineDeleteRequest;
import com.nongxinle.dto.platform.customer.PlatformCartLineUpdateRequest;
import com.nongxinle.dto.platform.customer.PlatformCartListRequest;
import com.nongxinle.dto.platform.customer.PlatformCartSubmitRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutConfirmRequest;
import com.nongxinle.dto.platform.customer.PlatformCheckoutPreviewRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerGoodsCatalogListRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerGoodsCatalogTreeRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerGoodsCategoriesRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerHomeInitRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerHomeInitResponse;
import com.nongxinle.dto.platform.customer.PlatformCustomerMarketSuppliersRequest;
import com.nongxinle.service.PlatformCustomerCatalogService;
import com.nongxinle.service.PlatformCustomerHomeService;
import com.nongxinle.dto.platform.customer.PlatformCustomerOrderDeleteRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerOrderListRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerOrderUpdateRequest;
import com.nongxinle.service.platform.PlatformCustomerOrderService;
import com.nongxinle.service.platform.PlatformCustomerSubmitLineService;
import com.nongxinle.dto.platform.PlatformSubmitLineRequest;
import com.nongxinle.service.platform.PlatformBillPaymentService;
import com.nongxinle.service.platform.PlatformCartCheckoutService;
import com.nongxinle.service.platform.PlatformCartSubmitService;
import com.nongxinle.service.platform.PlatformOutstandingBillBlockException;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 批发市场平台饭店端首页（与 Phase 2a/2b 订单分配出库链路隔离）
 */
@RestController
@RequestMapping("api/platform/customer")
public class PlatformCustomerController {

    private static final Logger log = LoggerFactory.getLogger(PlatformCustomerController.class);

    @Autowired
    private PlatformCustomerHomeService platformCustomerHomeService;
    @Autowired
    private PlatformCustomerCatalogService platformCustomerCatalogService;
    @Autowired
    private PlatformCustomerSubmitLineService platformCustomerSubmitLineService;
    @Autowired
    private PlatformCustomerOrderService platformCustomerOrderService;
    @Autowired
    private PlatformCartSubmitService platformCartSubmitService;
    @Autowired
    private PlatformCartCheckoutService platformCartCheckoutService;
    @Autowired
    private PlatformBillPaymentService platformBillPaymentService;

    @RequestMapping(value = "/home/init", method = RequestMethod.POST)
    @ResponseBody
    public R homeInit(@RequestBody PlatformCustomerHomeInitRequest request) {
        try {
            PlatformCustomerHomeInitResponse data = platformCustomerHomeService.homeInit(request);
            log.info("[platform/customer/home/init] marketId={} departmentId={} hasOutstandingBill={} outstandingBill={}",
                    request == null ? null : request.getMarketId(),
                    request == null ? null : request.getDepartmentId(),
                    data == null ? null : data.getHasOutstandingBill(),
                    data == null ? null : data.getOutstandingBill());
            return R.ok("success").put("data", data);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/market/suppliers", method = RequestMethod.POST)
    @ResponseBody
    public R marketSuppliers(@RequestBody PlatformCustomerMarketSuppliersRequest request) {
        try {
            return R.ok("success").put("data", platformCustomerHomeService.listMarketSuppliers(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/goods/categories", method = RequestMethod.POST)
    @ResponseBody
    public R goodsCategories(@RequestBody PlatformCustomerGoodsCategoriesRequest request) {
        try {
            return R.ok("success").put("data", platformCustomerHomeService.listGoodsCategories(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/goods/catalog/tree", method = RequestMethod.POST)
    @ResponseBody
    public R goodsCatalogTree(@RequestBody PlatformCustomerGoodsCatalogTreeRequest request) {
        try {
            log.info("[platform/catalog/tree] incoming request={}", request);
            Map<String, Object> data = platformCustomerCatalogService.buildCatalogTree(request);
            return R.ok("success").put("data", data);
        } catch (Exception e) {
            log.error("[platform/catalog/tree] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/goods/catalog/list", method = RequestMethod.POST)
    @ResponseBody
    public R goodsCatalogList(@RequestBody PlatformCustomerGoodsCatalogListRequest request) {
        try {
            log.info("[platform/catalog/list] incoming request={}", request);
            PageUtils page = platformCustomerCatalogService.listGoodsByGrandCategory(request);
            log.info("[platform/catalog/list] greatGrandId={} totalCount={} currPage={} listSize={}",
                    request == null ? null : request.getGreatGrandId(),
                    page == null ? null : page.getTotalCount(),
                    page == null ? null : page.getCurrPage(),
                    page == null || page.getList() == null ? 0 : page.getList().size());
            return R.ok("success").put("page", page);
        } catch (Exception e) {
            log.error("[platform/catalog/list] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/orders/listTodayLines", method = RequestMethod.POST)
    @ResponseBody
    public R listTodayLines(@RequestBody PlatformCustomerOrderListRequest request) {
        try {
            return R.ok("success").put("data", platformCustomerOrderService.listTodayLines(request));
        } catch (IllegalArgumentException ex) {
            return R.error(4002, ex.getMessage());
        } catch (Exception e) {
            log.error("[platform/customer/orders/listTodayLines] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/orders/updateLine", method = RequestMethod.POST)
    @ResponseBody
    public R updateOrderLine(@RequestBody PlatformCustomerOrderUpdateRequest request) {
        try {
            return R.ok("success").put("data", platformCustomerOrderService.updateLine(request));
        } catch (IllegalArgumentException ex) {
            return R.error(4002, ex.getMessage());
        } catch (Exception e) {
            log.error("[platform/customer/orders/updateLine] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/orders/deleteLine", method = RequestMethod.POST)
    @ResponseBody
    public R deleteOrderLine(@RequestBody PlatformCustomerOrderDeleteRequest request) {
        try {
            platformCustomerOrderService.deleteLine(request);
            return R.ok("success");
        } catch (IllegalArgumentException ex) {
            return R.error(4002, ex.getMessage());
        } catch (Exception e) {
            log.error("[platform/customer/orders/deleteLine] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/orders/submitLine", method = RequestMethod.POST)
    @ResponseBody
    public R customerSubmitLine(@RequestBody PlatformSubmitLineRequest request) {
        try {
            log.info("[platform/customer/orders/submitLine] request={}", request);
            return R.ok("success").put("data", platformCustomerSubmitLineService.submitLine(request));
        } catch (Exception e) {
            log.error("[platform/customer/orders/submitLine] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/cart/lines/list", method = RequestMethod.POST)
    @ResponseBody
    public R listCartLines(@RequestBody PlatformCartListRequest request) {
        try {
            return R.ok("success").put("data", platformCartCheckoutService.listCartLines(request));
        } catch (IllegalArgumentException ex) {
            return R.error(4002, ex.getMessage());
        } catch (Exception e) {
            log.error("[platform/cart/lines/list] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/cart/lines/update", method = RequestMethod.POST)
    @ResponseBody
    public R updateCartLine(@RequestBody PlatformCartLineUpdateRequest request) {
        try {
            return R.ok("success").put("data", platformCartCheckoutService.updateCartLine(request));
        } catch (IllegalArgumentException ex) {
            return R.error(4002, ex.getMessage());
        } catch (Exception e) {
            log.error("[platform/cart/lines/update] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/cart/lines/delete", method = RequestMethod.POST)
    @ResponseBody
    public R deleteCartLine(@RequestBody PlatformCartLineDeleteRequest request) {
        try {
            platformCartCheckoutService.deleteCartLine(request);
            return R.ok("success");
        } catch (IllegalArgumentException ex) {
            return R.error(4002, ex.getMessage());
        } catch (Exception e) {
            log.error("[platform/cart/lines/delete] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/cart/checkout/preview", method = RequestMethod.POST)
    @ResponseBody
    public R checkoutPreview(@RequestBody PlatformCheckoutPreviewRequest request) {
        try {
            return R.ok("success").put("data", platformCartCheckoutService.checkoutPreview(request));
        } catch (IllegalArgumentException ex) {
            return R.error(4002, ex.getMessage());
        } catch (Exception e) {
            log.error("[platform/cart/checkout/preview] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/cart/checkout/confirm", method = RequestMethod.POST)
    @ResponseBody
    public R checkoutConfirm(@RequestBody PlatformCheckoutConfirmRequest request) {
        try {
            return R.ok("success").put("data", platformCartCheckoutService.checkoutConfirm(request));
        } catch (PlatformOutstandingBillBlockException ex) {
            return R.error(4001, ex.getMessage())
                    .put("message", ex.getMessage())
                    .put("data", ex.getData());
        } catch (IllegalArgumentException ex) {
            return R.error(4002, ex.getMessage());
        } catch (Exception e) {
            log.error("[platform/cart/checkout/confirm] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/cart/submitBySupplier", method = RequestMethod.POST)
    @ResponseBody
    public R submitBySupplier(@RequestBody PlatformCartSubmitRequest request) {
        try {
            log.info("[platform/cart/submitBySupplier] incoming request={}", request);
            PlatformCartAddWithSupplierResponse data = platformCartSubmitService.submitBySupplier(request);
            log.info("[platform/cart/submitBySupplier] success nxDistributerId={} addedLineCount={}",
                    data == null ? null : data.getNxDistributerId(),
                    data == null ? null : data.getAddedLineCount());
            return R.ok("success").put("data", data);
        } catch (IllegalArgumentException ex) {
            log.warn("[platform/cart/submitBySupplier] bad request gbDepartmentId={} msg={}",
                    request == null ? null : request.getGbDepartmentId(), ex.getMessage());
            return R.error(4002, ex.getMessage());
        } catch (Exception e) {
            log.error("[platform/cart/submitBySupplier] failed request={}", request, e);
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/bill/payFirst", method = RequestMethod.POST)
    @ResponseBody
    public R payFirst(@RequestBody PlatformBillPayFirstRequest request) {
        try {
            return R.ok("success").put("data", platformBillPaymentService.payFirst(request));
        } catch (IllegalArgumentException ex) {
            return R.error(4002, ex.getMessage());
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/bill/paySupplement", method = RequestMethod.POST)
    @ResponseBody
    public R paySupplement(@RequestBody PlatformBillPaySupplementRequest request) {
        try {
            return R.ok("success").put("data", platformBillPaymentService.paySupplement(request));
        } catch (IllegalArgumentException ex) {
            return R.error(4002, ex.getMessage());
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}

package com.nongxinle.community.pos.controller;

import com.nongxinle.dto.pos.*;
import com.nongxinle.community.pos.service.NxCommunityPosService;
import com.nongxinle.utils.R;
import com.nongxinle.utils.WxPayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 社区 Electron POS 专用接口（与小程序订单链路隔离）
 */
@RestController
@RequestMapping("api/nxcommunitypos")
public class NxCommunityPosController {

    @Autowired
    private NxCommunityPosService nxCommunityPosService;

    @RequestMapping(value = "/auth/bootstrap", method = RequestMethod.POST)
    @ResponseBody
    public R bootstrap(@RequestBody PosBootstrapRequest request) {
        try {
            return R.ok().put("data", nxCommunityPosService.bootstrap(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/menu/list", method = RequestMethod.POST)
    @ResponseBody
    public R menuList(@RequestBody Map<String, Object> body) {
        try {
            Integer communityId = (Integer) body.get("communityId");
            return R.ok().put("data", nxCommunityPosService.menuList(communityId));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/order/save", method = RequestMethod.POST)
    @ResponseBody
    public R saveOrder(@RequestBody PosSaveOrderRequest request) {
        try {
            return R.ok().put("data", nxCommunityPosService.saveOrder(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/order/{orderId}", method = RequestMethod.GET)
    @ResponseBody
    public R orderDetail(@PathVariable Integer orderId,
                         @RequestParam(required = false) Integer customerUserId) {
        try {
            return R.ok().put("data", nxCommunityPosService.orderDetail(orderId, customerUserId));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/order/list/today", method = RequestMethod.GET)
    @ResponseBody
    public R orderListToday(@RequestParam Integer communityId,
                            @RequestParam(required = false) Integer deskId,
                            @RequestParam(required = false, defaultValue = "ALL") String status,
                            @RequestParam(required = false, defaultValue = "1") Integer page,
                            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        try {
            return R.ok().put("data", nxCommunityPosService.orderListToday(communityId, deskId, status, page, limit));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/table/activeOrder", method = RequestMethod.GET)
    @ResponseBody
    public R getActiveOrder(@RequestParam Integer communityId,
                          @RequestParam Integer deskId,
                          @RequestParam(required = false) Integer customerUserId) {
        try {
            return R.ok().put("data", nxCommunityPosService.getActiveOrderByDesk(communityId, deskId, customerUserId));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/table/transferDesk", method = RequestMethod.POST)
    @ResponseBody
    public R transferDesk(@RequestBody PosTransferDeskRequest request) {
        try {
            return R.ok().put("data", nxCommunityPosService.transferDesk(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/table/clearDesk", method = RequestMethod.POST)
    @ResponseBody
    public R clearDesk(@RequestBody PosClearDeskRequest request) {
        try {
            return R.ok().put("data", nxCommunityPosService.clearDesk(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/coupon/lookup", method = RequestMethod.POST)
    @ResponseBody
    public R couponLookup(@RequestBody PosCouponLookupRequest request) {
        try {
            return R.ok().put("data", nxCommunityPosService.couponLookup(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/member/search", method = RequestMethod.POST)
    @ResponseBody
    public R memberSearch(@RequestBody PosMemberSearchRequest request) {
        try {
            return R.ok().put("data", nxCommunityPosService.memberSearch(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/member/coupons", method = RequestMethod.POST)
    @ResponseBody
    public R memberCoupons(@RequestBody PosMemberCouponsRequest request) {
        try {
            return R.ok().put("data", nxCommunityPosService.memberCoupons(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/coupon/verify", method = RequestMethod.POST)
    @ResponseBody
    public R couponVerify(@RequestBody PosCouponVerifyRequest request) {
        try {
            return R.ok().put("data", nxCommunityPosService.couponVerify(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/order/applyCoupon", method = RequestMethod.POST)
    @ResponseBody
    public R applyCoupon(@RequestBody PosApplyCouponRequest request) {
        try {
            return R.ok().put("data", nxCommunityPosService.applyCoupon(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/payment/create", method = RequestMethod.POST)
    @ResponseBody
    public R createPayment(@RequestBody PosPaymentCreateRequest request) {
        try {
            return R.ok().put("data", nxCommunityPosService.createPayment(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/payment/settleZero", method = RequestMethod.POST)
    @ResponseBody
    public R settleZero(@RequestBody PosSettleZeroRequest request) {
        try {
            return R.ok().put("data", nxCommunityPosService.settleZero(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/payment/{paymentId}/status", method = RequestMethod.GET)
    @ResponseBody
    public R paymentStatus(@PathVariable Integer paymentId) {
        try {
            return R.ok().put("data", nxCommunityPosService.paymentStatus(paymentId));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/payment/cancel", method = RequestMethod.POST)
    @ResponseBody
    public R cancelPayment(@RequestBody PosPaymentCancelRequest request) {
        try {
            return R.ok().put("data", nxCommunityPosService.cancelPayment(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/payment/notify/wechat", method = RequestMethod.POST)
    @ResponseBody
    public String wechatNotify(HttpServletRequest request) {
        try {
            InputStream in = request.getInputStream();
            String xml = WxPayUtils.InputStream2String(in);
            return nxCommunityPosService.handleWechatNotify(xml);
        } catch (Exception e) {
            return "<xml><return_code><![CDATA[FAIL]]></return_code></xml>";
        }
    }

    @RequestMapping(value = "/payment/notify/alipay", method = RequestMethod.POST)
    @ResponseBody
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            params.put(name, request.getParameter(name));
        }
        return nxCommunityPosService.handleAlipayNotify(params);
    }
}

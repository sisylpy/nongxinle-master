package com.nongxinle.community.yunguotuan.controller;

import com.nongxinle.utils.R;
import com.nongxinle.community.yunguotuan.service.YgtGroupOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/ygt/admin/order")
public class YgtAdminOrderController {
    @Autowired
    private YgtGroupOrderService ygtGroupOrderService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public R list(@RequestParam(required = false) Long campaignId,
                  @RequestParam(required = false) Long groupId,
                  @RequestParam(required = false) String status,
                  @RequestParam(defaultValue = "0") Integer offset,
                  @RequestParam(defaultValue = "100") Integer limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("campaignId", campaignId);
        params.put("groupId", groupId);
        params.put("status", status);
        params.put("offset", offset);
        params.put("limit", limit);
        return R.ok().put("data", ygtGroupOrderService.queryOrders(params));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public R detail(@PathVariable Long id) {
        try {
            return R.ok().put("data", ygtGroupOrderService.orderDetail(id));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}

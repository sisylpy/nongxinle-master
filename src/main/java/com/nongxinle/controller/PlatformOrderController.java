package com.nongxinle.controller;

import com.nongxinle.dto.platform.PlatformAssignRequest;
import com.nongxinle.dto.platform.PlatformOrderDetailRequest;
import com.nongxinle.dto.platform.PlatformPendingRequest;
import com.nongxinle.dto.platform.PlatformSubmitLineRequest;
import com.nongxinle.dto.platform.PlatformUnassignRequest;
import com.nongxinle.dto.platform.PlatformSuppliersRequest;
import com.nongxinle.dto.platform.PlatformDistributerIdSchemaProbeResponse;
import com.nongxinle.service.PlatformGoodsSupplierService;
import com.nongxinle.service.PlatformOrderAssignService;
import com.nongxinle.service.platform.PlatformDistributerIdResolver;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 批发市场平台化订单接口（与旧配送商协作链、社区 POS 隔离）
 */
@RestController
@RequestMapping("api/platform/orders")
public class PlatformOrderController {

    @Autowired
    private PlatformOrderAssignService platformOrderAssignService;

    @Autowired
    private PlatformDistributerIdResolver platformDistributerIdResolver;

    @RequestMapping(value = "/submitLine", method = RequestMethod.POST)
    @ResponseBody
    public R submitLine(@RequestBody PlatformSubmitLineRequest request) {
        try {
            return R.ok().put("data", platformOrderAssignService.submitLine(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/pending", method = RequestMethod.POST)
    @ResponseBody
    public R pending(@RequestBody PlatformPendingRequest request) {
        try {
            return R.ok().put("data", platformOrderAssignService.listPending(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/detail", method = RequestMethod.POST)
    @ResponseBody
    public R detail(@RequestBody PlatformOrderDetailRequest request) {
        try {
            return R.ok().put("data", platformOrderAssignService.getDetail(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/assign", method = RequestMethod.POST)
    @ResponseBody
    public R assign(@RequestBody PlatformAssignRequest request) {
        try {
            return R.ok().put("data", platformOrderAssignService.assign(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/unassign", method = RequestMethod.POST)
    @ResponseBody
    public R unassign(@RequestBody PlatformUnassignRequest request) {
        try {
            return R.ok().put("data", platformOrderAssignService.unassign(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/schema/distributerIdNullable", method = RequestMethod.GET)
    @ResponseBody
    public R distributerIdNullable() {
        PlatformDistributerIdSchemaProbeResponse probe = platformDistributerIdResolver.probeSchema();
        if (probe.getNullable() == null) {
            return R.error(probe.getError() != null
                    ? probe.getError()
                    : "无法探测 nx_department_orders.nx_DO_distributer_id 是否允许 NULL")
                    .put("catalog", probe.getCatalog())
                    .put("tableExists", probe.getTableExists())
                    .put("errorCause", probe.getErrorCause())
                    .put("manualSql", "docs/sql/patches/check_nx_department_orders_distributer_id.sql");
        }
        return R.ok().put("data", probe);
    }
}

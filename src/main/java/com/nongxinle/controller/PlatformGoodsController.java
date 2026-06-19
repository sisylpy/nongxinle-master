package com.nongxinle.controller;

import com.nongxinle.dto.platform.PlatformSuppliersRequest;
import com.nongxinle.service.PlatformGoodsSupplierService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 批发市场平台化商品/供货查询（与旧配送商协作链隔离）
 */
@RestController
@RequestMapping("api/platform/goods")
public class PlatformGoodsController {

    @Autowired
    private PlatformGoodsSupplierService platformGoodsSupplierService;

    @RequestMapping(value = "/suppliers", method = RequestMethod.POST)
    @ResponseBody
    public R suppliers(@RequestBody PlatformSuppliersRequest request) {
        try {
            return R.ok().put("data", platformGoodsSupplierService.listSuppliers(request));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}

package com.nongxinle.controller;

/**
 * @author lpy
 * @date 09-16 09:12
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.GbDistributerPurchaseBatchEntity;
import com.nongxinle.service.GbDistributerPurchaseBatchService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.entity.GbDistributerSupplierEntity;
import com.nongxinle.service.GbDistributerSupplierService;
import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.GbTypeUtils.getGbDepUserAdminMendiancaigouyuan;


@RestController
@RequestMapping("api/gbdistributersupplier")
public class GbDistributerSupplierController {
    @Autowired
    private GbDistributerSupplierService gbDistributerSupplierService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDisPurchaseBatchService;



    @RequestMapping(value = "/mendianGetSupplier/{depId}")
    @ResponseBody
    public R mendianGetSupplier(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("isGroup", 0);
        map.put("depId", depId);
        List<GbDistributerSupplierEntity> supplierEntities = gbDistributerSupplierService.querySupplierByParams(map);
        return R.ok().put("data", supplierEntities);
    }



    @RequestMapping(value = "/purchaserGetAppointSupplier/{depId}")
    @ResponseBody
    public R purchaserGetAppointSupplier(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("admin", 0);
        List<GbDistributerSupplierEntity> supplierEntities = gbDistributerSupplierService.queryDepartmentAppointSupplier(map);
        return R.ok().put("data", supplierEntities);
    }


//    @RequestMapping(value = "/purchaserGetSupplier/{depId}")
//    @ResponseBody
//    public R purchaserGetSupplier(@PathVariable Integer depId) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", depId);
//        List<GbDistributerSupplierEntity> supplierEntities = gbDistributerSupplierService.queryDepartmentSupplier(map);
//        return R.ok().put("data", supplierEntities);
//    }


    @RequestMapping(value = "/purchaserGetSupplierWithCata/{depId}")
    @ResponseBody
    public R purchaserGetSupplierWithCata(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        List<GbDistributerSupplierEntity> supplierEntities = gbDistributerSupplierService.queryDepartmentSupplier(map);
        return R.ok().put("data", supplierEntities);
    }


    @RequestMapping(value = "/getDepartmentSupplier/{depId}")
    @ResponseBody
    public R getDepartmentSupplier(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        List<GbDistributerSupplierEntity> supplierEntities = gbDistributerSupplierService.querySupplierByParams(map);
        return R.ok().put("data", supplierEntities);
    }


    @RequestMapping(value = "/getDepSupplierList/{depId}")
    @ResponseBody
    public R getDepSupplierList(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        List<GbDistributerSupplierEntity> supplierEntities = gbDistributerSupplierService.querySupplierByParams(map);

        return R.ok().put("data", supplierEntities);
    }

    @RequestMapping(value = "/getSupplierList/{fatherId}")
    @ResponseBody
    public R getSupplierList(@PathVariable Integer fatherId) {
        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);
        List<GbDepartmentEntity> supplierEntities = gbDistributerSupplierService.querySupplierDepartmentGroup(map);
        return R.ok().put("data", supplierEntities);
    }


//    @RequestMapping(value = "/saveGbSupplier", method = RequestMethod.POST)
//    @ResponseBody
//    public R saveGbSupplier(@RequestBody GbDistributerSupplierEntity suppler) {
//        gbDistributerSupplierService.save(suppler);
//
//        return R.ok();
//    }


    @RequestMapping(value = "/deleteGbSupplierFather/{id}")
    @ResponseBody
    public R deleteGbSupplierFather(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", id);
        List<GbDistributerSupplierEntity> supplierEntities = gbDistributerSupplierService.querySupplierByParams(map);
        if(supplierEntities.size() > 0){
            return R.error(-1,"类别下有供货商，不能删除");
        }else{
            gbDistributerSupplierService.delete(id);
            return R.ok();
        }
    }
    @RequestMapping(value = "/deleteGbSupplier/{id}")
    @ResponseBody
    public R deleteGbSupplier(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", id);
        map.put("status", 4);
        List<GbDistributerPurchaseBatchEntity> entities = gbDisPurchaseBatchService.queryList(map);
        if(entities.size() > 0){
            return R.error(-1,"有未结账单");
        }else{
            gbDistributerSupplierService.delete(id);
            return R.ok();
        }
    }

    @RequestMapping(value = "/updateGbSupplier", method = RequestMethod.POST)
    @ResponseBody
    public R updateGbSupplier(@RequestBody GbDistributerSupplierEntity suppler) {
        gbDistributerSupplierService.update(suppler);
        return R.ok();
    }
    @RequestMapping(value = "/saveGbSupplierFather", method = RequestMethod.POST)
    @ResponseBody
    public R saveGbSupplierFather(@RequestBody GbDistributerSupplierEntity suppler) {
        suppler.setGbDistributerSupplierFatherId(0);
        gbDistributerSupplierService.save(suppler);

        return R.ok();
    }


    @RequestMapping(value = "/disGetSupplier/{disId}")
    @ResponseBody
    public R disGetSupplier(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("fatherId", 0);
        map.put("isGroup", 1);
        List<GbDistributerSupplierEntity> supplierEntities = gbDistributerSupplierService.querySupplierByParams(map);
        return R.ok().put("data", supplierEntities);
    }


}

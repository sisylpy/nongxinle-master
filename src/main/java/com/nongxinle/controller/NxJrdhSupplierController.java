package com.nongxinle.controller;

/**
 * @author lpy
 * @date 05-11 21:54
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;


@RestController
@RequestMapping("api/nxjrdhsupplier")
public class NxJrdhSupplierController {
    @Autowired
    private NxJrdhSupplierService nxJrdhSupplierService;

    @Autowired
    private NxDistributerPurchaseBatchService nxPurBatchService;
    @Autowired
    private GbDistributerPurchaseBatchService gbPurBatchService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepGoodsStockService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;





    @RequestMapping(value = "/updateJrdhSupplier", method = RequestMethod.POST)
    @ResponseBody
    public R updateJrdhSupplier (@RequestBody NxJrdhSupplierEntity supplier) {
        nxJrdhSupplierService.update(supplier);
        return R.ok();
    }

    @RequestMapping(value = "/gbPurchaserGetSupplier/{depId}")
    @ResponseBody
    public R gbPurchaserGetSupplier(@PathVariable Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("gbDepId", depId);
        List<NxJrdhSupplierEntity> supplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(map);
        return R.ok().put("data", supplierEntities);
    }


    @RequestMapping(value = "/saveJrdhSupplier", method = RequestMethod.POST)
    @ResponseBody
    public R saveJrdhSupplier(@RequestBody NxJrdhSupplierEntity suppler) {
        suppler.setNxJrdhsUserId(-1);
        suppler.setNxJrdhsSysCityId(5);
        suppler.setNxJrdhsSysMarketId(-1);
        nxJrdhSupplierService.save(suppler);
        return R.ok();
    }

    @RequestMapping(value = "/deleteGbDisSuppler/{id}")
    @ResponseBody
    public R deleteGbDisSuppler(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", id);
        map.put("status", 3);
        map.put("payType", 1);
        int i = gbPurBatchService.queryDisPurchaseBatchCount(map);

        Map<String, Object> mapST = new HashMap<>();
        mapST.put("supplierId",id);
        mapST.put("restWeight", 0);
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(mapST);

        if (i > 0 || integer > 0) {
            return R.error(-1, "有未结账账单或库存");
        } else {
            NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.queryObject(id);
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("supplierId", supplierEntity.getNxJrdhSupplierId());
            mapS.put("disId", supplierEntity.getNxJrdhsGbDistributerId());
            List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapS);
            if(gbDistributerGoodsEntities.size() > 0){
                for(GbDistributerGoodsEntity distributerGoodsEntity: gbDistributerGoodsEntities){
                    distributerGoodsEntity.setGbDgGbSupplierId(null);
                    distributerGoodsEntity.setGbDgNxDistributerId(-1);
                    distributerGoodsEntity.setGbDgNxDistributerGoodsId(-1);
                    gbDistributerGoodsService.update(distributerGoodsEntity);
                }
            }

            Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
            nxJrdhUserService.delete(nxJrdhsUserId);

            nxJrdhSupplierService.delete(id);

            return R.ok();

        }
    }

    @RequestMapping(value = "/deleteNxDisSuppler/{id}")
    @ResponseBody
    public R deleteNxDisSuppler(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", id);
        map.put("status", 3);
        map.put("payType", 1);
        int i = nxPurBatchService.queryDisPurchaseBatchCount(map);
        if (i > 0) {
            return R.error(-1, "有未结账账单");
        } else {
            NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.queryObject(id);
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("supplierId", supplierEntity.getNxJrdhSupplierId());
            List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = nxDistributerGoodsService.queryDisGoodsByParams(mapS);
            if(nxDistributerGoodsEntities.size() > 0){
                for(NxDistributerGoodsEntity distributerGoodsEntity: nxDistributerGoodsEntities){
                    distributerGoodsEntity.setNxDgSupplierId(null);
                    nxDistributerGoodsService.update(distributerGoodsEntity);
                }
            }
            map.put("status", 5);
            List<NxDistributerPurchaseBatchEntity> batchEntities = nxPurBatchService.queryDisPurchaseBatch(map);
            if(batchEntities.size() > 0){
                for(NxDistributerPurchaseBatchEntity batchEntity: batchEntities){
                    nxPurBatchService.delete(batchEntity.getNxDistributerPurchaseBatchId());
                }
            }

            Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
            nxJrdhUserService.delete(nxJrdhsUserId);

            nxJrdhSupplierService.delete(id);
            return R.ok();

        }
    }


    @RequestMapping(value = "/sellerGetAllDistributer/{sellId}")
    @ResponseBody
    public R sellerGetAllDistributer(@PathVariable String sellId) {

        List<NxDistributerEntity> nxDistributerEntities = nxPurBatchService.queryNxDistributerBySellerId(sellId);
        List<GbDistributerEntity> gbDistributerEntities = gbPurBatchService.queryGbDistributerBySellerId(sellId);

        Map<String, Object> map = new HashMap<>();
        map.put("gbArr", gbDistributerEntities);
        map.put("nxArr", nxDistributerEntities);

        return R.ok().put("data", map);


    }


    //disGetAllSellers
    @RequestMapping(value = "/nxDisGetAllSuppliers", method = RequestMethod.POST)
    @ResponseBody
    public R nxDisGetAllSuppliers(Integer nxDisId, Integer userId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("nxDisId", nxDisId);
        if(userId != 0){
            map3.put("notEqualUserId", userId);
        }
        System.out.println("map333333" + map3);

        List<NxJrdhSupplierEntity> nxJrdhSupplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(map3);

        return R.ok().put("data", nxJrdhSupplierEntities);
    }



//
    //disGetAllSellers
    @RequestMapping(value = "/gbDisGetAllSuppliers/{disId}")
    @ResponseBody
    public R gbDisGetAllSuppliers(@PathVariable Integer disId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("gbDisId", disId);
        List<NxJrdhSupplierEntity> nxJrdhSupplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(map3);

        return R.ok().put("data", nxJrdhSupplierEntities);
    }

    @RequestMapping(value = "/depGetSupplier/{depId}")
    @ResponseBody
    public R depGetSupplier(@PathVariable Integer depId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("gbDepId", depId);
        List<NxJrdhSupplierEntity> nxJrdhSupplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(map3);
        if(nxJrdhSupplierEntities.size() > 0){
            for(NxJrdhSupplierEntity supplierEntity: nxJrdhSupplierEntities){
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("supplierId",supplierEntity.getNxJrdhSupplierId());
                mapB.put("payType", 1);
                mapB.put("equalStatus", 3);
                Double aDouble = 0.0;
                Integer integer = gbPurBatchService.queryDisPurchaseBatchCount(mapB);
                if(integer > 0){
                    aDouble  = gbPurBatchService.querySupplierUnSettleSubtotal(mapB);
                }
                supplierEntity.setBillCount(integer);
                supplierEntity.setTotal(new BigDecimal(aDouble).setScale(1,BigDecimal.ROUND_HALF_UP).toString());
            }
        }

        return R.ok().put("data", nxJrdhSupplierEntities);
    }


}

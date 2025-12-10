package com.nongxinle.controller;

/**
 * @author lpy
 * @date 05-11 21:54
 */

import java.math.BigDecimal;
import java.util.ArrayList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("api/nxjrdhsupplier")
public class NxJrdhSupplierController {
    private static final Logger logger = LoggerFactory.getLogger(NxJrdhSupplierController.class);
    
    @Autowired
    private NxJrdhSupplierService nxJrdhSupplierService;

    @Autowired
    private NxDistributerPurchaseBatchService nxPurBatchService;
    @Autowired
    private GbDistributerPurchaseGoodsService purchaseGoodsService;
    @Autowired
    private GbDistributerPurchaseBatchService gbPurBatchService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;
    @Autowired
    private GbDistributerService gbDistributerService;
    @Autowired
    private NxGbDistibuterUserCouponService nxGbDistibuterUserCouponService;
    @Autowired
    private NxDistributerCouponService nxDistributerCouponService;




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

    @RequestMapping(value = "/addGbSupplierBlack/{id}")
    @ResponseBody
    public R addGbSupplierBlack(@PathVariable Integer id) {
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

//            Integer nxJrdhsNxDistributerId = supplierEntity.getNxJrdhsNxDistributerId();
//            if(nxJrdhsNxDistributerId != -1){
//                Map<String, Object> mapSN = new HashMap<>();
//                mapSN.put("nxDisId", nxJrdhsNxDistributerId);
//                mapSN.put("gbDisId",supplierEntity.getNxJrdhsGbDistributerId());
//                NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDistributerGbDistributerService.queryObjectByParams(mapSN);
//                if(nxDistributerGbDistributerEntity != null){
//                    nxDistributerGbDistributerService.delete(nxDistributerGbDistributerEntity.getNxDistributerGbDistributerId());
//                }

//                NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryDepInfoByGbDisId(supplierEntity.getNxJrdhsGbDistributerId());
//                nxDepartmentService.delete(nxDepartmentEntity.getNxDepartmentId());

//            }

        supplierEntity.setNxJrdhsGbDepartmentId(-1);
        supplierEntity.setNxJrdhsStatus(-1);
        nxJrdhSupplierService.update(supplierEntity);
        return R.ok();


    }

    @RequestMapping(value = "/deleteGbDisSuppler/{id}")
    @ResponseBody
    public R deleteGbDisSuppler(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("supplierId", id);
        map.put("status", 4);
        int i = gbPurBatchService.queryDisPurchaseBatchCount(map);

        if (i > 0 ) {
            return R.error(-1, "有未结账账单");
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

//            Integer nxJrdhsNxDistributerId = supplierEntity.getNxJrdhsNxDistributerId();
//            if(nxJrdhsNxDistributerId != -1){
//                Map<String, Object> mapSN = new HashMap<>();
//                mapSN.put("nxDisId", nxJrdhsNxDistributerId);
//                mapSN.put("gbDisId",supplierEntity.getNxJrdhsGbDistributerId());
//                NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = nxDistributerGbDistributerService.queryObjectByParams(mapSN);
//                if(nxDistributerGbDistributerEntity != null){
//                    nxDistributerGbDistributerService.delete(nxDistributerGbDistributerEntity.getNxDistributerGbDistributerId());
//                }

//                NxDepartmentEntity nxDepartmentEntity = nxDepartmentService.queryDepInfoByGbDisId(supplierEntity.getNxJrdhsGbDistributerId());
//                nxDepartmentService.delete(nxDepartmentEntity.getNxDepartmentId());

//            }

                supplierEntity.setNxJrdhsGbDepartmentId(-1);
            nxJrdhSupplierService.update(supplierEntity);

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
//    @RequestMapping(value = "/nxDisGetAllSuppliers", method = RequestMethod.POST)
//    @ResponseBody
//    public R nxDisGetAllSuppliers(Integer nxDisId, Integer userId) {
//        Map<String, Object> map3 = new HashMap<>();
//        map3.put("nxDisId", nxDisId);
//        if(userId != 0){
//            map3.put("notEqualUserId", userId);
//        }
//        System.out.println("map333333" + map3);
//
//        List<NxJrdhSupplierEntity> nxJrdhSupplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(map3);
//
//        return R.ok().put("data", nxJrdhSupplierEntities);
//    }

    @RequestMapping(value = "/nxDisGetAllSuppliers", method = RequestMethod.POST)
    @ResponseBody
    public R nxDisGetAllSuppliers(Integer nxDisId, Integer userId, String startDate, String stopDate) {
        logger.info("[nxDisGetAllSuppliers] 开始查询供应商列表，nxDisId={}, userId={}, startDate={}, stopDate={}", 
                nxDisId, userId, startDate, stopDate);
        
        Map<String, Object> map3 = new HashMap<>();
        map3.put("nxDisId", nxDisId);
        if(userId != null && userId != 0){
            map3.put("notEqualUserId", userId);
        }

        logger.debug("[nxDisGetAllSuppliers] 查询参数: {}", map3);
        List<NxJrdhSupplierEntity> nxJrdhSupplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(map3);
        logger.info("[nxDisGetAllSuppliers] 查询到{}个供应商", nxJrdhSupplierEntities.size());

        if(nxJrdhSupplierEntities.size() > 0 && startDate != null && stopDate != null){
            logger.info("[nxDisGetAllSuppliers] 日期参数有效，开始计算每个供应商的采购统计数据，startDate={}, stopDate={}", startDate, stopDate);
            for(NxJrdhSupplierEntity supplierEntity: nxJrdhSupplierEntities){
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("supplierId", supplierEntity.getNxJrdhSupplierId());
                mapS.put("disId", nxDisId);
                if(startDate != null && !startDate.isEmpty()){
                    mapS.put("startDate", startDate);
                }
                if(stopDate != null && !stopDate.isEmpty()){
                    mapS.put("stopDate", stopDate);
                }
                mapS.put("equalStatus", 3);  // 未结账状态
                mapS.put("notEqualPurchaseType", -1);  // 排除入库（purchaseType=-1）
                
                Double unPayOrderDouble = 0.0; // 未结账订单
                Double unPayReturn = 0.0; // 未结账退货
                Double havePayOrderDouble = 0.0; // 已结账订单
                Double havePayReturn = 0.0; // 已结账退货

                //未结账订单
                logger.debug("[nxDisGetAllSuppliers] 查询供应商{}未结账订单，参数: {}", 
                        supplierEntity.getNxJrdhSupplierId(), mapS);
                Integer unPayCount = nxPurBatchService.queryDisPurchaseBatchCount(mapS);
                if (unPayCount > 0) {
                    unPayOrderDouble = nxPurBatchService.querySupplierUnSettleSubtotal(mapS);
                    logger.debug("[nxDisGetAllSuppliers] 供应商{}未结账订单数量={}, 金额={}", 
                            supplierEntity.getNxJrdhSupplierId(), unPayCount, unPayOrderDouble);
                }
                
                //未结账退货（NX项目中可能需要根据实际退货类型调整）
                // 注意：GB项目使用 purchaseType=9 表示退货，NX项目中可能不同
                // 这里暂时注释，需要确认NX项目中退货的purchaseType值
                /*
                mapS.put("notEqualPurchaseType", null);
                mapS.put("purchaseType", 9);  // 需要确认NX项目中退货的purchaseType
                Integer unPayTuihuoCount = nxPurBatchService.queryDisPurchaseBatchCount(mapS);
                if (unPayTuihuoCount > 0) {
                    unPayReturn = nxPurBatchService.querySupplierUnSettleSubtotal(mapS);
                }
                */
                
                //已结账订单
                mapS.put("equalStatus", 4);  // 已结账状态
                mapS.put("notEqualPurchaseType", -1);  // 排除入库
                mapS.put("purchaseType", null);
                logger.debug("[nxDisGetAllSuppliers] 查询供应商{}已结账订单，参数: {}", 
                        supplierEntity.getNxJrdhSupplierId(), mapS);
                Integer havePayCount = nxPurBatchService.queryDisPurchaseBatchCount(mapS);
                if (havePayCount > 0) {
                    havePayOrderDouble = nxPurBatchService.querySupplierUnSettleSubtotal(mapS);
                    logger.debug("[nxDisGetAllSuppliers] 供应商{}已结账订单数量={}, 金额={}", 
                            supplierEntity.getNxJrdhSupplierId(), havePayCount, havePayOrderDouble);
                }

                // 已结账退货（同上，需要确认NX项目中退货的purchaseType）
                /*
                mapS.put("notEqualPurchaseType", null);
                mapS.put("purchaseType", 9);
                Integer havePayTuihuoCount = nxPurBatchService.queryDisPurchaseBatchCount(mapS);
                if (havePayTuihuoCount > 0) {
                    havePayReturn = nxPurBatchService.querySupplierUnSettleSubtotal(mapS);
                }
                */

                //计算结果:
                //订单数量
                int billCount = unPayCount + havePayCount;
                //订单金额
                double billTotal = unPayOrderDouble + havePayOrderDouble;

                //已结订单
                int havePayCountTotal = havePayCount;  // 暂时不加退货，因为上面注释了
                //已结金额
                double havePayTotl = havePayOrderDouble - havePayReturn;

                //实际未接金额
                double actPayTotal = unPayOrderDouble - unPayReturn;

                //实际订单数量
                int actPayCountTotal = unPayCount;  // 暂时不加退货，因为上面注释了

                Map<String, Object> mapDataOne = new HashMap<>();
                mapDataOne.put("billCount", billCount);
                mapDataOne.put("billTotal", String.format("%.1f", billTotal));

                mapDataOne.put("unPayCount", unPayCount);
                mapDataOne.put("unPayTotal", String.format("%.1f", unPayOrderDouble));

                mapDataOne.put("havePayCount", havePayCountTotal);
                mapDataOne.put("havePayTotal", String.format("%.1f", havePayTotl));

                mapDataOne.put("returnBillCount", 0);  // 暂时设为0，等确认退货类型后修改
                mapDataOne.put("returnPayTotal", String.format("%.1f", unPayReturn));

                mapDataOne.put("actBillCount", actPayCountTotal);
                mapDataOne.put("actPayTotal", String.format("%.1f", actPayTotal));

                logger.info("[nxDisGetAllSuppliers] 供应商{}（{}）统计数据计算完成，mapDataOne: {}", 
                        supplierEntity.getNxJrdhSupplierId(), 
                        supplierEntity.getNxJrdhsSupplierName() != null ? supplierEntity.getNxJrdhsSupplierName() : "未知",
                        mapDataOne);
                logger.info("[nxDisGetAllSuppliers] 供应商{}详细数据 - billCount={}, billTotal={}, unPayCount={}, unPayTotal={}, havePayCount={}, havePayTotal={}, returnBillCount={}, returnPayTotal={}, actBillCount={}, actPayTotal={}", 
                        supplierEntity.getNxJrdhSupplierId(),
                        billCount, billTotal, unPayCount, unPayOrderDouble, havePayCountTotal, havePayTotl, 
                        0, unPayReturn, actPayCountTotal, actPayTotal);
                
                supplierEntity.setItemData(mapDataOne);
                logger.info("[nxDisGetAllSuppliers] 供应商{}数据已设置到supplierEntity", supplierEntity.getNxJrdhSupplierId());
            }
            logger.info("[nxDisGetAllSuppliers] 供应商统计数据计算完成");
        } else {
            if(nxJrdhSupplierEntities.size() == 0) {
                logger.info("[nxDisGetAllSuppliers] 未找到供应商，跳过统计计算");
            } else if(startDate == null || stopDate == null) {
                logger.warn("[nxDisGetAllSuppliers] 日期参数为空，跳过采购统计计算。需要提供startDate和stopDate参数才能计算统计数据。startDate={}, stopDate={}", startDate, stopDate);
            }
        }

        logger.info("[nxDisGetAllSuppliers] 查询完成，返回{}个供应商", nxJrdhSupplierEntities.size());
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

    @RequestMapping(value = "/depGetSupplier", method = RequestMethod.POST)
    @ResponseBody
    public R depGetSupplier(Integer depId, String startDate, String stopDate) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("gbDepId", depId);
        List<NxJrdhSupplierEntity> nxJrdhSupplierEntities = purchaseGoodsService.queryDisPurGoodsSupplierList(map3);
        if(nxJrdhSupplierEntities.size() > 0){
            for(NxJrdhSupplierEntity supplierEntity: nxJrdhSupplierEntities){
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("supplierId",supplierEntity.getNxJrdhSupplierId());
                mapS.put("startDate", startDate);
                mapS.put("stopDate", stopDate);
                mapS.put("equalStatus", 3);
                mapS.put("notEqualPurchaseType", 9);
                Double unPayOrderDouble = 0.0; // 未结账订单
                Double unPayReturn = 0.0; // 未记账退货
                Double havePayOrderDouble = 0.0; // 已结账订单
                Double havePayReturn = 0.0; // 已结账退货

                //未结账订单
                Integer unPayCount = gbPurBatchService.queryDisPurchaseBatchCount(mapS);
                if (unPayCount > 0) {
                    unPayOrderDouble = gbPurBatchService.querySupplierUnSettleSubtotal(mapS);
                }
                // //未结账退货
                mapS.put("notEqualPurchaseType", null);
                mapS.put("purchaseType", 9);
                Integer unPayTuihuoCount = gbPurBatchService.queryDisPurchaseBatchCount(mapS);
                if (unPayTuihuoCount > 0) {
                    unPayReturn = gbPurBatchService.querySupplierUnSettleSubtotal(mapS);
                }
                //已结账订单
                mapS.put("equalStatus", 4);
                mapS.put("notEqualPurchaseType", 9);
                mapS.put("purchaseType", null);
                Integer havePayCount = gbPurBatchService.queryDisPurchaseBatchCount(mapS);
                if (havePayCount > 0) {
                    havePayOrderDouble = gbPurBatchService.querySupplierUnSettleSubtotal(mapS);
                }

                // 已结账退货
                mapS.put("notEqualPurchaseType", null);
                mapS.put("purchaseType", 9);
                Integer havePayTuihuoCount = gbPurBatchService.queryDisPurchaseBatchCount(mapS);
                if (havePayTuihuoCount > 0) {
                    havePayReturn = gbPurBatchService.querySupplierUnSettleSubtotal(mapS);
                }


                //计算结果:
                //订单数量
                int billCount = unPayCount + havePayCount;
                //订单金额
                double billTotal = unPayOrderDouble + havePayOrderDouble;

                //已结订单
                int havePayCountTotal = havePayCount + havePayTuihuoCount;

                //已结金额
                double havePayTotl = havePayOrderDouble - havePayReturn;

                //实际未接金额
                double actPayTotal = unPayOrderDouble - unPayReturn;

                //实际订单数量
                int actPayCountTotal = unPayCount + unPayTuihuoCount;

                Map<String, Object> mapDataOne = new HashMap<>();
                mapDataOne.put("billCount", billCount);
                mapDataOne.put("billTotal", String.format("%.1f", billTotal));

                mapDataOne.put("unPayCount", unPayCount);
                mapDataOne.put("unPayTotal", String.format("%.1f", unPayOrderDouble));

                mapDataOne.put("havePayCount", havePayCountTotal);
                mapDataOne.put("havePayTotal", String.format("%.1f", havePayTotl));

                mapDataOne.put("returnBillCount", unPayTuihuoCount);
                mapDataOne.put("returnPayTotal", String.format("%.1f", unPayReturn));

                mapDataOne.put("actBillCount", actPayCountTotal);
                mapDataOne.put("actPayTotal", String.format("%.1f", actPayTotal));



                supplierEntity.setItemData(mapDataOne);
                if(supplierEntity.getNxDistributerEntity() != null && supplierEntity.getNxDistributerEntity().getNxDistributerGbDistributerEntity().getNxDgdGbPayMethod() == 0){

                    Integer nxDisId = supplierEntity.getNxJrdhsNxDistributerId();
                    Integer gbDisId = supplierEntity.getNxJrdhsGbDistributerId();
                    GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(gbDisId);

                    Map<String, Object> map = new HashMap<>();
                    map.put("disId", nxDisId);
                    map.put("equalStatus", 0);
                    map.put("cityId", gbDistributerEntity.getGbDistributerSysCityId());
                    System.out.println("coupeoneoemap" + map);
                    List<NxDistributerCouponEntity> couponEntities = nxDistributerCouponService.queryLoadDownListByParams(map);
                    List<NxDistributerCouponEntity> resultCouList = new ArrayList<>();
                    if (couponEntities.size() > 0) {
                        for (NxDistributerCouponEntity couponEntity : couponEntities) {
                            Map<String, Object> mapG = new HashMap<>();
                            mapG.put("gbDisId", gbDisId);
                            mapG.put("nxDisId", nxDisId);
                            mapG.put("couponId", couponEntity.getNxDistributerCouponId());
                            System.out.println("c" + mapG);
                            List<NxGbDistibuterUserCouponEntity> userCouponEntities = nxGbDistibuterUserCouponService.queryGbCouponListByParams(mapG);
                            if (userCouponEntities.size() == 0) {
                                resultCouList.add(couponEntity);
                            }
                        }
                    }
                    supplierEntity.setCouponEntities(resultCouList);

                    //查询用户优惠卷
                    Map<String, Object> mapGU = new HashMap<>();
                    mapGU.put("gbDisId", gbDisId);
                    mapGU.put("nxDisId", nxDisId);
                    List<NxGbDistibuterUserCouponEntity> nxGbDistibuterUserCouponEntities = nxGbDistibuterUserCouponService.queryGbCouponListByParams(mapGU);
                    supplierEntity.setMyCouponEntities(nxGbDistibuterUserCouponEntities);
                }
            }
        }

        return R.ok().put("data", nxJrdhSupplierEntities);
    }

    @RequestMapping(value = "/depGetAllSupplier/{depId}")
    @ResponseBody
    public R depGetAllSupplier(@PathVariable  Integer depId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("gbDepId", depId);
        List<NxJrdhSupplierEntity> nxJrdhSupplierEntities = nxJrdhSupplierService.queryJrdhSupplerByParams(map3);


        return R.ok().put("data", nxJrdhSupplierEntities);
    }


}

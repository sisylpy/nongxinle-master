package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-21 21:51
 */

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.nongxinle.dto.NxDepartmentOrdersSimpleDTO;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.CommonUtils;
import com.nongxinle.utils.PageUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import com.nongxinle.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbPurchaseGoodsTypeForOrder;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.ParseObject.myRandom;
import static com.nongxinle.utils.PinYin4jUtils.*;


@RestController
@RequestMapping("api/nxdepartmentorders")
public class NxDepartmentOrdersController {
    private static final Logger logger = LoggerFactory.getLogger(NxDepartmentOrdersController.class);

    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDepartmentOrdersHistoryService nxDepartmentOrdersHistoryService;
    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDistributerPurchaseBatchService;
    @Autowired
    private NxDistributerWeightService nxDistributerWeightService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxDepartmentBillService nxDepartmentBillService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDistributerStandardService nxDistributerStandardService;
    @Autowired
    private NxDistributerGoodsShelfStockService nxDisGoodsShelfStockService;
    @Autowired
    private NxDistributerGoodsShelfStockReduceService nxDisGoodsShelfStockReduceService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;
    @Autowired
    private NxDepartmentOrderHistoryService historyService;
    @Autowired
    private NxDistributerAliasService nxDistributerAliasService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerPurchaseBatchService gbDistributerPurchaseBatchService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxDistributerGoodsShelfGoodsService shelfGoodsService;
    @Autowired
    private NxDistributerGoodsShelfService nxDistributerGoodsShelfService;
    @Autowired
    private NxOrderOcrTrainingDataService nxOrderOcrTrainingDataService;
    @Autowired
    private NxOcrTaskService nxOcrTaskService;
    @Autowired
    private NxDistributerGoodsLinshiService linshiService;
    @Autowired
    private NxDistributerBillService nxDistributerBillService;
    @Autowired
    private NxDistributerGoodsMergeService nxDistributerGoodsMergeService;

    @Value("${external.images.path:file:///opt/tomcat/latest/app-data/images/}")
    private String externalImagesPath;



    @RequestMapping(value = "/saveDepartmentOrdersToHistory", method = RequestMethod.POST)
    @ResponseBody
    public R saveDepartmentOrdersToHistory(Integer depFatherId, String date) {

        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("status", 3);
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);

        if (nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                orders.setNxDoApplyDate(date);
                orders.setNxDoArriveDate(date);

                Map<String, Object> mapDG = new HashMap<>();
                mapDG.put("disGoodsId", orders.getNxDoDisGoodsId());
                mapDG.put("depId", orders.getNxDoDepartmentId());
                System.out.println("dedigodo" + mapDG);
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(mapDG);
                NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(orders.getNxDoDisGoodsId());

                //1，配送商自己的客户
                if (nxDepartmentDisGoodsEntity != null) {
                    orders.setNxDoDepDisGoodsId(nxDepartmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                    if (orders.getNxDoTrainingDataId() != null) {
                        NxOrderOcrTrainingDataEntity dataEntity = nxOrderOcrTrainingDataService.queryObject(orders.getNxDoTrainingDataId());
                        nxDepartmentDisGoodsEntity.setNxDdgOrderGoodsName(dataEntity.getNxOtdOriginalGoodsName());
                    }
                    nxDepartmentDisGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                    if (orders.getNxDoGoodsName() != null) {
                        nxDepartmentDisGoodsEntity.setNxDdgDepGoodsName(orders.getNxDoGoodsName());
                    }

                    nxDepartmentDisGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
                    nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);

                } else {
                    System.out.println("new Depdiiddid");
                    NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
                    if (orders.getNxDoGoodsName() != null) {
                        disGoodsEntity.setNxDdgDepGoodsName(orders.getNxDoGoodsName());
                    }
                    if (orders.getNxDoTrainingDataId() != null) {
                        NxOrderOcrTrainingDataEntity dataEntity = nxOrderOcrTrainingDataService.queryObject(orders.getNxDoTrainingDataId());
                        disGoodsEntity.setNxDdgOrderGoodsName(dataEntity.getNxOtdOriginalGoodsName());
                    } else {
                        disGoodsEntity.setNxDdgOrderGoodsName(orders.getNxDoGoodsName());
                    }
                    disGoodsEntity.setNxDdgDisGoodsId(orders.getNxDoDisGoodsId());
                    disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
                    disGoodsEntity.setNxDdgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());

                    NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                    Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
                    disGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);

                    disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
                    disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
                    disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
                    disGoodsEntity.setNxDdgDepartmentId(orders.getNxDoDepartmentId());
                    disGoodsEntity.setNxDdgDepartmentFatherId(orders.getNxDoDepartmentFatherId());
                    disGoodsEntity.setNxDdgNxDistributerId(orders.getNxDoDistributerId());
                    //orderData
                    disGoodsEntity.setNxDdgOrderPrice(orders.getNxDoPrice());
                    disGoodsEntity.setNxDdgOrderCostPrice(orders.getNxDoCostPrice());
                    disGoodsEntity.setNxDdgOrderQuantity(orders.getNxDoQuantity());
                    disGoodsEntity.setNxDdgOrderRemark(orders.getNxDoRemark());
                    disGoodsEntity.setNxDdgOrderStandard(orders.getNxDoStandard());
                    disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
                    disGoodsEntity.setNxDdgGbDistributerId(orders.getNxDoGbDistributerId());
                    disGoodsEntity.setNxDdgGoodsNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
                    disGoodsEntity.setNxDdgOrderPriceLevel(orders.getNxDoCostPriceLevel());
                    nxDepartmentDisGoodsService.save(disGoodsEntity);
                    orders.setNxDoDepDisGoodsId(disGoodsEntity.getNxDepartmentDisGoodsId());
                }


                orders.setNxDoStatus(3);
                orders.setNxDoPurchaseStatus(getNxDisPurchaseGoodsFinishPay());
//                orders.setNxDoBillId(nxDepartmentBill.getNxDepartmentBillId());
                nxDepartmentOrdersService.update(orders);

                // 如果是任务订单，并且任务状态小于 3，则把任务状态更新为 3
                if (orders.getNxDoOcrTaskId() != null) {
                    NxOcrTaskEntity nxOcrTaskEntity = nxOcrTaskService.queryObject(orders.getNxDoOcrTaskId());
                    if (nxOcrTaskEntity != null && (nxOcrTaskEntity.getNxOcrTaskStatus() == null || nxOcrTaskEntity.getNxOcrTaskStatus() < 3)) {
                        nxOcrTaskEntity.setNxOcrTaskStatus(3);
                        nxOcrTaskService.update(nxOcrTaskEntity);
                    }
                }

                //迁移
                System.out.println("xunlianjieshu" + orders.getNxDoGoodsName());
                nxDepartmentOrdersService.moveOrderToHistory(orders);  // ✅ 迁移


            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/clearDisTestData/{id}")
    @ResponseBody
    public R clearDisTestData(@PathVariable Integer id) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", id);
        map.put("status", 3);
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : nxDepartmentOrdersEntities) {
                deleteOrderInternal(ordersEntity.getNxDepartmentOrdersId());
            }
        }

        List<NxDistributerPurchaseGoodsEntity> nxDistributerPurchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
        if (nxDistributerPurchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : nxDistributerPurchaseGoodsEntities) {
                nxDistributerPurchaseGoodsService.delete(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
            }
        }
        return R.ok().put("data", nxDepartmentOrdersEntities.size());
    }


    @RequestMapping(value = "/updateOrderPrint", method = RequestMethod.POST)
    @ResponseBody
    public R updateOrderPrint(Integer orderId, String printName) {
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(orderId);
        nxDepartmentOrdersEntity.setNxDoPrintStandard(printName);
        nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);

        if (nxDepartmentOrdersEntity.getNxDoTrainingDataId() != null) {
            NxOrderOcrTrainingDataEntity dataEntity = nxOrderOcrTrainingDataService.queryObject(nxDepartmentOrdersEntity.getNxDoTrainingDataId());
            dataEntity.setNxOtdFinalStandard(printName);
            dataEntity.setNxOtdIsStandardManuallyAnnotated(1);
            nxOrderOcrTrainingDataService.update(dataEntity);
        }
        return R.ok();
    }


    @RequestMapping(value = "/disGetLinshiOrders/{id}")
    @ResponseBody
    public R disGetLinshiOrders(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", id);
        map.put("equalStatus", -1);
        map.put("agent", 3);
        System.out.println("mapappa" + map);
        List<NxDepartmentEntity> nxDepartmentEntities = nxDepartmentOrdersService.queryDistributerTodayDepartments(map);
        return R.ok().put("data", nxDepartmentEntities);
    }

    /**
     * 配送商确定临时订单为临时商品
     *
     * @param
     * @return
     * @date 2026-01-10
     */
    @RequestMapping(value = "/confirmDepApplyGoods/{id}")
    @ResponseBody
    public R confirmDepApplyGoods(@PathVariable Integer id) {
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(id);
        nxDepartmentOrdersEntity.setNxDoStatus(0);
        nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);
        return R.ok();
    }

    /**
     * 订单换商品：删除老订单 + 保存新订单（复用 save 逻辑，价格由新商品决定）
     * 从老订单复制：订货数量、规格、备注、排序(todayOrder)
     */
    @RequestMapping(value = "/exchangeDepApplyGoods", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeDepApplyGoods(Integer orderId, Integer goodsId) {
        NxDepartmentOrdersEntity oldOrder = nxDepartmentOrdersService.queryObject(orderId);
        if (oldOrder == null) {
            return R.error("订单不存在，订单ID: " + orderId);
        }
        NxDistributerGoodsEntity newGoodsEntity = nxDistributerGoodsService.queryObject(goodsId);
        if (newGoodsEntity == null) {
            return R.error("商品不存在，商品ID: " + goodsId);
        }

        // 1. 构建新订单：复制老订单的订货数量、规格、备注、排序及必要字段
        NxDepartmentOrdersEntity newOrder = new NxDepartmentOrdersEntity();
        newOrder.setNxDoQuantity(oldOrder.getNxDoQuantity() != null ? oldOrder.getNxDoQuantity() : "");
        newOrder.setNxDoStandard(oldOrder.getNxDoStandard() != null ? oldOrder.getNxDoStandard() : "");
        newOrder.setNxDoRemark(oldOrder.getNxDoRemark());
        newOrder.setNxDoTodayOrder(oldOrder.getNxDoTodayOrder());
        newOrder.setNxDoDepartmentId(oldOrder.getNxDoDepartmentId());
        newOrder.setNxDoDepartmentFatherId(oldOrder.getNxDoDepartmentFatherId());
        newOrder.setNxDoDistributerId(oldOrder.getNxDoDistributerId());
        newOrder.setNxDoWeight(oldOrder.getNxDoWeight());
        newOrder.setStandardWeight(oldOrder.getStandardWeight());
        newOrder.setItemUnit(oldOrder.getItemUnit());
        newOrder.setItemsPerCarton(oldOrder.getItemsPerCarton());
        newOrder.setNxDoOcrTaskId(oldOrder.getNxDoOcrTaskId());
        newOrder.setNxDoTrainingDataId(oldOrder.getNxDoTrainingDataId());
        newOrder.setNxDoGoodsName(newGoodsEntity.getNxDgGoodsName());
        newOrder.setNxDoGoodsOriginalName(oldOrder.getNxDoGoodsOriginalName());
        newOrder.setNxDoOrderUserId(oldOrder.getNxDoOrderUserId());
        newOrder.setNxDoIsAgent(oldOrder.getNxDoIsAgent());
        newOrder.setNxDoDisGoodsId(goodsId);
        newOrder.setNxDepartmentOrdersId(null);
        newOrder.setNxDoStatus(0);

        // 2. 先保存新订单（价格由 saveOrderWithGoods 按新商品计算）
        newOrder = nxDepartmentOrdersService.saveOrderWithGoods(newOrder, newGoodsEntity);

        // 3. 再删除老订单（会处理采购商品回退等）
        boolean deleted = deleteOrderInternal(orderId);
        if (!deleted) {
            return R.error("换商品成功但删除老订单失败，请检查老订单状态");
        }
        return R.ok().put("data", newOrder);
    }

    @RequestMapping(value = "/editDepApplyGoods", method = RequestMethod.POST)
    @ResponseBody
    public R editDepApplyGoods(Integer orderId, Integer goodsId) {

        System.out.println("orderrid" + orderId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(orderId);
        Integer nxDoDisGoodsId = nxDepartmentOrdersEntity.getNxDoDisGoodsId();

        NxDistributerGoodsEntity lishiGoods = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        Integer nxDoPurchaseGoodsIldLinshi = nxDepartmentOrdersEntity.getNxDoPurchaseGoodsId();
        NxDistributerPurchaseGoodsEntity linshiPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsIldLinshi);

        System.out.println("purgidid" + linshiPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
        NxDistributerGoodsEntity toNxdisGoodsEnitity = nxDistributerGoodsService.queryObject(goodsId);
        Integer nxDpgOrdersAmount = linshiPurchaseGoodsEntity.getNxDpgOrdersAmount();
        if (nxDpgOrdersAmount == 1) {
            if (linshiPurchaseGoodsEntity.getNxDpgBatchId() == null) {
                nxDistributerPurchaseGoodsService.delete(linshiPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
            } else {
                System.out.println("updateeeeeee" + linshiPurchaseGoodsEntity);
                linshiPurchaseGoodsEntity.setNxDpgDisGoodsId(goodsId);
                linshiPurchaseGoodsEntity.setNxDpgDisGoodsFatherId(toNxdisGoodsEnitity.getGbDisGoodsFatherId());
                linshiPurchaseGoodsEntity.setNxDpgDisGoodsGrandId(toNxdisGoodsEnitity.getNxDgDfgGoodsGrandId());
                nxDistributerPurchaseGoodsService.update(linshiPurchaseGoodsEntity);
            }
        } else {
            linshiPurchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
            linshiPurchaseGoodsEntity.setNxDpgDisGoodsId(goodsId);
            linshiPurchaseGoodsEntity.setNxDpgDisGoodsFatherId(toNxdisGoodsEnitity.getGbDisGoodsFatherId());
            linshiPurchaseGoodsEntity.setNxDpgDisGoodsGrandId(toNxdisGoodsEnitity.getNxDgDfgGoodsGrandId());
            nxDistributerPurchaseGoodsService.update(linshiPurchaseGoodsEntity);
        }

        nxDistributerGoodsService.delete(lishiGoods.getNxDistributerGoodsId());


        Integer nxDoPurchaseGoodsId = nxDepartmentOrdersEntity.getNxDoPurchaseGoodsId();
        nxDistributerPurchaseGoodsService.delete(nxDoPurchaseGoodsId);

        nxDepartmentOrdersEntity.setNxDoDisGoodsId(toNxdisGoodsEnitity.getNxDistributerGoodsId());
        nxDepartmentOrdersEntity.setNxDoDisGoodsFatherId(toNxdisGoodsEnitity.getNxDgDfgGoodsFatherId());
        nxDepartmentOrdersEntity.setNxDoDisGoodsGrandId(toNxdisGoodsEnitity.getNxDgDfgGoodsGrandId());
        nxDepartmentOrdersEntity.setNxDoNxGoodsId(toNxdisGoodsEnitity.getNxDgNxGoodsId());
        nxDepartmentOrdersEntity.setNxDoNxGoodsFatherId(toNxdisGoodsEnitity.getNxDgNxFatherId());
        nxDepartmentOrdersEntity.setNxDoStatus(0);
        nxDepartmentOrdersEntity.setNxDoGoodsType(toNxdisGoodsEnitity.getNxDgPurchaseAuto());

        nxDepartmentOrdersService.processOrderPrice(nxDepartmentOrdersEntity, toNxdisGoodsEnitity);
        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrdersEntity.getNxDoDepartmentId());
        map.put("disGoodsId", toNxdisGoodsEnitity.getNxDistributerGoodsId());
        map.put("standard", nxDepartmentOrdersEntity.getNxDoStandard());
        System.out.println("depmapapmmdmmdd" + map);
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if (departmentDisGoodsEntity != null) {
            System.out.println("prinstnan" + departmentDisGoodsEntity.getNxDdgOrderStandard());
            // 判断是否使用了大包装单价，如果是则设置打印规格为大包装单位（支持智能匹配：件=箱）
            boolean isUsingCartonPrice = toNxdisGoodsEnitity.getNxDgCartonUnit() != null
                    && !toNxdisGoodsEnitity.getNxDgCartonUnit().trim().isEmpty()
                    && nxDepartmentOrdersEntity.getNxDoStandard() != null
                    && isStandardMatch(nxDepartmentOrdersEntity.getNxDoStandard().trim(), toNxdisGoodsEnitity.getNxDgCartonUnit().trim());

            if (isUsingCartonPrice) {
                nxDepartmentOrdersEntity.setNxDoPrintStandard(toNxdisGoodsEnitity.getNxDgCartonUnit());
                System.out.println("使用大包装单价，打印规格设置为: " + toNxdisGoodsEnitity.getNxDgCartonUnit());
            } else if (nxDepartmentOrdersEntity.getNxDoCostPriceLevel() == null || nxDepartmentOrdersEntity.getNxDoCostPriceLevel().equals("1")) {
                nxDepartmentOrdersEntity.setNxDoPrintStandard(toNxdisGoodsEnitity.getNxDgGoodsStandardname());
            } else {
                nxDepartmentOrdersEntity.setNxDoPrintStandard(toNxdisGoodsEnitity.getNxDgWillPriceTwoStandard());
            }

            nxDepartmentOrdersEntity.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
            //如果有重量和单价，则计算 subtotal
            if (nxDepartmentOrdersEntity.getNxDoWeight() != null && !nxDepartmentOrdersEntity.getNxDoWeight().trim().isEmpty()
                    && nxDepartmentOrdersEntity.getNxDoPrice() != null && !nxDepartmentOrdersEntity.getNxDoPrice().trim().isEmpty()) {
                try {
                    BigDecimal weight = new BigDecimal(nxDepartmentOrdersEntity.getNxDoWeight());
                    BigDecimal price = new BigDecimal(nxDepartmentOrdersEntity.getNxDoPrice());
                    BigDecimal subtotal = weight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                    nxDepartmentOrdersEntity.setNxDoSubtotal(subtotal.toString());
                } catch (NumberFormatException e) {
                    // 如果转换失败，不设置subtotal
                    logger.warn("[saveOneOrder] 计算subtotal失败，重量或单价格式错误: weight={}, price={}, error={}",
                            nxDepartmentOrdersEntity.getNxDoWeight(), nxDepartmentOrdersEntity.getNxDoPrice(), e.getMessage());
                }
            }
            nxDepartmentOrdersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
        } else {
            map.put("standard", null);
            System.out.println("depmapapapappapa" + map);
            NxDepartmentDisGoodsEntity departmentDisGoodsEntityO = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
            if (departmentDisGoodsEntityO != null) {
                nxDepartmentOrdersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
            }
        }
        nxDepartmentOrdersEntity.setNxDoPurchaseGoodsId(-1);
        nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);

        if (toNxdisGoodsEnitity.getNxDgPurchaseAuto() != -1) {
            nxDepartmentOrdersService.savePurGoodsAuto(nxDepartmentOrdersEntity, getNxPurchaseGoodsInputTypeOrder(), getNxPurchaseGoodsTypeForSelf());
        }


        return R.ok();
    }

    @RequestMapping(value = "/cancleDeliveryOrder/{id}")
    @ResponseBody
    public R cancleDeliveryOrder(@PathVariable Integer id) {
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(id);
        nxDepartmentOrdersEntity.setNxDoPurchaseStatus(4);
        nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);
        return R.ok();
    }


    @RequestMapping(value = "/deliveryOrder/{id}")
    @ResponseBody
    public R deliveryOrder(@PathVariable Integer id) {
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.queryObject(id);

        nxDepartmentOrdersEntity.setNxDoPurchaseStatus(5);
        nxDepartmentOrdersService.update(nxDepartmentOrdersEntity);
        return R.ok();
    }


    @RequestMapping(value = "/cancleGbOrderSx/{id}")
    @ResponseBody
    public R cancleGbOrderSx(@PathVariable Integer id) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(id);
        ordersEntity.setNxDoWeight(null);
        ordersEntity.setNxDoSubtotal("0");
        ordersEntity.setNxDoStatus(getNxOrderStatusNew());
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrdersService.update(ordersEntity);

        Integer gbDepartmentOrderId = ordersEntity.getNxDoGbDepartmentOrderId();
        GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrderId);
        gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusNew());
        gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderStatusProcurement());
        gbDepartmentOrdersEntity.setGbDoWeight("0");
        gbDepartmentOrdersEntity.setGbDoSubtotal("0");
        gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);

        Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
        purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
        purchaseGoodsEntity.setGbDpgBuySubtotal("0");
//        purchaseGoodsEntity.setGbDpgBuyQuantity("");
        gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);


        return R.ok();
    }

    @RequestMapping(value = "/cancleGbOrder/{id}")
    @ResponseBody
    public R cancleGbOrder(@PathVariable Integer id) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(id);
        ordersEntity.setNxDoWeight(null);
        ordersEntity.setNxDoSubtotal("0");
        ordersEntity.setNxDoStatus(getNxOrderStatusNew());
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrdersService.update(ordersEntity);

        Integer gbDepartmentOrderId = ordersEntity.getNxDoGbDepartmentOrderId();
        GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrderId);
        gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusNew());
        gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderStatusProcurement());
        gbDepartmentOrdersEntity.setGbDoWeight("0");
        gbDepartmentOrdersEntity.setGbDoSubtotal("0");
        gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);

        Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();
        GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
        purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
        purchaseGoodsEntity.setGbDpgBuySubtotal("0");
//        purchaseGoodsEntity.setGbDpgBuyQuantity(null);
        gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

        Integer gbDpgBatchId = purchaseGoodsEntity.getGbDpgBatchId();
        GbDistributerPurchaseBatchEntity batchEntity = gbDistributerPurchaseBatchService.queryObject(gbDpgBatchId);
        batchEntity.setGbDpbStatus(getGbDisPurchaseBatchSellerReply());
        batchEntity.setGbDpbSubtotal("0");
        gbDistributerPurchaseBatchService.update(batchEntity);

        return R.ok();
    }


    @RequestMapping(value = "/disUpdateBuyingPrice", method = RequestMethod.POST)
    @ResponseBody
    public R disUpdateBuyingPrice(@RequestBody NxDistributerGoodsEntity disGoods) {

        nxDistributerGoodsService.update(disGoods);

        //nxOrder
        Integer distributerGoodsId = disGoods.getNxDistributerGoodsId();
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", distributerGoodsId);
        map.put("equalStatus", 0);
        map.put("settleType", 0);
        System.out.println("senet");
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                if (ordersEntity.getNxDoWeight() != null && !ordersEntity.getNxDoWeight().trim().isEmpty()) {
                    BigDecimal orderWeight = CommonUtils.parseBigDecimalOrDefault(ordersEntity.getNxDoWeight(), BigDecimal.ZERO);
                    BigDecimal willPrice = BigDecimal.ZERO;
                    BigDecimal buyingPrice = CommonUtils.parseBigDecimalOrDefault(disGoods.getNxDgBuyingPrice(), BigDecimal.ZERO);
                    String buyingPriceLevel = "0";
                    String update = disGoods.getNxDgBuyingPriceUpdate();
                    if (CommonUtils.isStrictlyPositiveDecimalString(disGoods.getNxDgWillPriceOneWeight())) {
                        BigDecimal nxOneWeight = CommonUtils.parseBigDecimalOrDefault(disGoods.getNxDgWillPriceOneWeight(), BigDecimal.ZERO);
                        if (orderWeight.compareTo(nxOneWeight) < 1) {
                            willPrice = CommonUtils.parseBigDecimalOrDefault(disGoods.getNxDgWillPriceOne(), BigDecimal.ZERO);
                            buyingPriceLevel = "1";
                        } else {
                            if (CommonUtils.isStrictlyPositiveDecimalString(disGoods.getNxDgWillPriceTwoWeight())) {
                                BigDecimal nxTwoWeight = CommonUtils.parseBigDecimalOrDefault(disGoods.getNxDgWillPriceTwoWeight(), BigDecimal.ZERO);
                                if (orderWeight.compareTo(nxTwoWeight) < 1) {
                                    willPrice = CommonUtils.parseBigDecimalOrDefault(disGoods.getNxDgWillPriceTwo(), BigDecimal.ZERO);
                                    buyingPriceLevel = "2";
                                } else {
                                    if (CommonUtils.isStrictlyPositiveDecimalString(disGoods.getNxDgWillPriceThreeWeight())) {
                                        willPrice = CommonUtils.parseBigDecimalOrDefault(disGoods.getNxDgWillPriceThree(), BigDecimal.ZERO);
                                        buyingPriceLevel = "3";
                                    } else {
                                        willPrice = CommonUtils.parseBigDecimalOrDefault(disGoods.getNxDgWillPriceTwo(), BigDecimal.ZERO);
                                        buyingPriceLevel = "2";
                                    }
                                }
                            } else {
                                willPrice = CommonUtils.parseBigDecimalOrDefault(disGoods.getNxDgWillPriceOne(), BigDecimal.ZERO);
                                buyingPriceLevel = "1";
                            }

                        }
                    } else {
                        willPrice = CommonUtils.parseBigDecimalOrDefault(disGoods.getNxDgWillPrice(), BigDecimal.ZERO);
                    }

                    BigDecimal profitB = willPrice.subtract(buyingPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoCostPriceLevel(buyingPriceLevel);
                    ordersEntity.setNxDoCostPrice(buyingPrice.toString());
                    ordersEntity.setNxDoCostPriceUpdate(update);
                    ordersEntity.setNxDoPrice(willPrice.toString());

                    //profit
                    if (willPrice.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal scaleB = profitB.divide(willPrice, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                        ordersEntity.setNxDoProfitScale(scaleB.toString());
                    } else {
                        ordersEntity.setNxDoProfitScale("0");
                    }

                    if (ordersEntity.getNxDoStandard() != null
                            && disGoods.getNxDgGoodsStandardname() != null
                            && ordersEntity.getNxDoStandard().equals(disGoods.getNxDgGoodsStandardname())) {
                        BigDecimal w = CommonUtils.parseBigDecimalOrDefault(ordersEntity.getNxDoWeight(), BigDecimal.ZERO);
                        BigDecimal costSubtotalB = buyingPrice.multiply(w).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal profitSubtotal = profitB.multiply(w).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal orderSubtotal = willPrice.multiply(w).setScale(1, BigDecimal.ROUND_HALF_UP);
                        ordersEntity.setNxDoCostSubtotal(costSubtotalB.toString());
                        ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());
                        ordersEntity.setNxDoSubtotal(orderSubtotal.toString());

                    }
                } else {
                    System.out.println("updatepriceeieeiieeiie");
                    ordersEntity.setNxDoPrice(disGoods.getNxDgWillPrice());
                }

                nxDepartmentOrdersService.update(ordersEntity);

            }
        }


        //nxPurGoods
        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgBuyPrice(disGoods.getNxDgBuyingPrice());
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        return R.ok();
    }


    @RequestMapping(value = "/choiceGoodsForApply", method = RequestMethod.POST)
    @ResponseBody
    public R choiceGoodsForApply(@RequestBody NxDepartmentOrdersEntity ordersEntity) {
        logger.info("[choiceGoodsForApply] 开始处理商品选择申请，接收到的订单数据: {}", ordersEntity);

        NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsService.queryObject(ordersEntity.getNxDoDisGoodsId());
        System.out.println("orrdis" + disGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
        map.put("standardName", "个" );
        List<NxDistributerStandardEntity> nxDistributerStandardEntities = nxDistributerStandardService.queryDisStandardByParams(map);
        map.put("standardName", "颗" );
        List<NxDistributerStandardEntity> nxDistributerStandardEntitiesKe = nxDistributerStandardService.queryDisStandardByParams(map);

        if (ordersEntity.getNxDoStandard().equals("个")
                && nxDistributerStandardEntities.size() == 0
                && nxDistributerStandardEntitiesKe.size() == 0) {
            ordersEntity.setNxDoStandard(disGoodsEntity.getNxDgGoodsStandardname());
        }

        // 协作伙伴商品：完整保存协作订单（含价格处理、采购商品自动创建），并关联主订单
        nxDepartmentOrdersService.saveCollaborativeOrderWhenNeeded(ordersEntity, disGoodsEntity);

        NxDepartmentOrdersEntity aaa = nxDepartmentOrdersService.updateOneOrderForChoice(ordersEntity, disGoodsEntity);
        logger.info("[choiceGoodsForApply] 商品选择订单处理完成，订单ID: {}, 状态: {}",
                aaa.getNxDepartmentOrdersId(), aaa.getNxDoStatus());

        // 更新训练数据（如果订单有关联的训练数据）
        // 从数据库重新查询订单，确保获取最新的训练数据ID
        NxOcrTaskEntity nxOcrTaskEntity = null;
        if (aaa.getNxDepartmentOrdersId() != null) {
            if (ordersEntity.getNxDoTrainingDataId() != null) {
                NxOrderOcrTrainingDataEntity trainingData = nxOrderOcrTrainingDataService.queryObject(ordersEntity.getNxDoTrainingDataId());

                if (trainingData != null) {
                    // 手动标注标志设为 2（自动识别是 1，手动识别是 2）
                    trainingData.setNxOtdDisGoodsId(aaa.getNxDoDisGoodsId());
                    trainingData.setNxOtdOrderId(aaa.getNxDepartmentOrdersId());

                    // 填充最终确认字段（手动标注）
                    trainingData.setNxOtdFinalGoodsName(aaa.getNxDoGoodsName());
                    trainingData.setNxOtdFinalQuantity(aaa.getNxDoQuantity());
                    trainingData.setNxOtdFinalStandard(aaa.getNxDoStandard());
                    trainingData.setNxOtdFinalRemark(aaa.getNxDoRemark() != null ? aaa.getNxDoRemark() : "");

                    // 手动标注标志设为 2
                    trainingData.setNxOtdIsNameManuallyAnnotated(2);
                    trainingData.setNxOtdIsQuantityManuallyAnnotated(2);
                    trainingData.setNxOtdIsStandardManuallyAnnotated(2);
                    trainingData.setNxOtdIsStandardWeightManuallyAnnotated(2);
                    trainingData.setNxOtdIsRemarkManuallyAnnotated(2);

                    // 更新标准重量（如果有的话，从原始数据中获取）
                    if (trainingData.getNxOtdOriginalStandardWeight() != null) {
                        trainingData.setNxOtdFinalStandardWeight(trainingData.getNxOtdOriginalStandardWeight());
                    }

                    trainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));

                    // 更新训练数据
                    nxOrderOcrTrainingDataService.update(trainingData);

                    logger.info("[choiceGoodsForApply] 训练数据已更新（手动标注），训练数据ID: {}, 订单ID: {}, 商品ID: {}",
                            trainingData.getNxOtdId(), aaa.getNxDepartmentOrdersId(), aaa.getNxDoDisGoodsId());
                }
            }

            // 更新OCR任务（独立于训练数据更新，只要订单有OCR任务ID就更新）
            if (ordersEntity.getNxDoOcrTaskId() != null) {
                Integer nxDoOcrTaskId = ordersEntity.getNxDoOcrTaskId();
                logger.info("[choiceGoodsForApply] 开始更新OCR任务，任务ID: {}, 订单ID: {}", nxDoOcrTaskId, aaa.getNxDepartmentOrdersId());

                nxOcrTaskEntity = nxOcrTaskService.queryObject(nxDoOcrTaskId);
                if (nxOcrTaskEntity == null) {
                    logger.warn("[choiceGoodsForApply] OCR任务不存在，任务ID: {}", nxDoOcrTaskId);
                } else {
                    int oldCompletedOrders = nxOcrTaskEntity.getNxOcrTaskCompletedOrders() != null ? nxOcrTaskEntity.getNxOcrTaskCompletedOrders() : 0;
                    int oldPendingOrders = nxOcrTaskEntity.getNxOcrTaskPendingOrders() != null ? nxOcrTaskEntity.getNxOcrTaskPendingOrders() : 0;

                    logger.info("[choiceGoodsForApply] OCR任务更新前状态 - 任务ID: {}, 已完成订单: {}, 待修正订单: {}, 任务状态: {}",
                            nxDoOcrTaskId, oldCompletedOrders, oldPendingOrders, nxOcrTaskEntity.getNxOcrTaskStatus());

                    nxOcrTaskEntity.setNxOcrTaskCompletedOrders(oldCompletedOrders + 1);
                    nxOcrTaskEntity.setNxOcrTaskPendingOrders(oldPendingOrders - 1);

                    int newPendingOrders = nxOcrTaskEntity.getNxOcrTaskPendingOrders();
                    logger.info("[choiceGoodsForApply] OCR任务更新后 - 已完成订单: {}, 待修正订单: {}",
                            nxOcrTaskEntity.getNxOcrTaskCompletedOrders(), newPendingOrders);

                    // 如果待修正订单数为0，设置任务状态为已完成（状态1）
                    if (newPendingOrders == 0) {
                        nxOcrTaskEntity.setNxOcrTaskStatus(2);
                        logger.info("[choiceGoodsForApply] 所有订单已完成，设置OCR任务状态为1（已完成），任务ID: {}", nxDoOcrTaskId);
                    }

                    nxOcrTaskService.update(nxOcrTaskEntity);
                    logger.info("[choiceGoodsForApply] OCR任务更新完成，任务ID: {}, 更新结果: {}, 最终状态 - 已完成订单: {}, 待修正订单: {}, 任务状态: {}",
                            nxDoOcrTaskId,
                            nxOcrTaskEntity.getNxOcrTaskCompletedOrders(),
                            nxOcrTaskEntity.getNxOcrTaskPendingOrders(),
                            nxOcrTaskEntity.getNxOcrTaskStatus());
                }
            } else {
                logger.debug("[choiceGoodsForApply] 订单没有关联OCR任务，订单ID: {}", aaa.getNxDepartmentOrdersId());
            }

            nxDepartmentOrdersService.processOrderPrice(ordersEntity, disGoodsEntity);
            //
        }

        return R.ok().put("data", aaa)
                .put("task", nxOcrTaskEntity);
    }


    /**
     * 将 NxDepartmentOrdersEntity 转换为 PasteSearchGoodsResponseDTO
     * 只包含前端需要的字段，减少数据传输量
     *
     * @return DTO对象
     */
//    private PasteSearchGoodsResponseDTO convertToResponseDTO(NxDepartmentOrdersEntity order, String originalOrderJson, String originalGoodsName,
//                                                             String standardWeight, String itemUnit, String itemsPerCarton, String cartonUnit) {
//        PasteSearchGoodsResponseDTO dto = new PasteSearchGoodsResponseDTO();
//
//        dto.setNxDepartmentOrdersId(order.getNxDepartmentOrdersId());
//        dto.setNxDoGoodsName(order.getNxDoGoodsName());
//        // 设置原始商品名称（与商品名称相同）
//        dto.setNxDoGoodsNameOriginal(order.getNxDoGoodsName());
//        dto.setNxDoQuantity(order.getNxDoQuantity());
//        // 规格默认值为"斤"
//        dto.setNxDoStandard(order.getNxDoStandard() != null && !order.getNxDoStandard().trim().isEmpty()
//                ? order.getNxDoStandard() : "斤");
//        dto.setNxDoRemark(order.getNxDoRemark());
//        // 是否有备注：remark不为null且trim后不为空
//        dto.setNxDoAddRemark(order.getNxDoRemark() != null && !order.getNxDoRemark().trim().isEmpty());
//        dto.setNxDoStatus(order.getNxDoStatus());
//        dto.setNxDoDepartmentId(order.getNxDoDepartmentId());
//        dto.setNxDoDepartmentFatherId(order.getNxDoDepartmentFatherId());
//        dto.setNxDoDisGoodsId(order.getNxDoDisGoodsId());
//        // 默认值：0
//        dto.setNxDoStandardWarn(0);
//        dto.setGoodsNameWarn(0);
//        dto.setNxDoDistributerId(order.getNxDoDistributerId());
//        // 采购用户ID默认值为-1
//        dto.setNxDoPurchaseUserId(order.getNxDoPurchaseUserId() != null ? order.getNxDoPurchaseUserId() : -1);
//        dto.setNxDoOrderUserId(order.getNxDoOrderUserId());
//        // 是否代理默认值为-1
//        dto.setNxDoIsAgent(order.getNxDoIsAgent() != null ? order.getNxDoIsAgent() : -1);
//        // 今日订单序号？？？
//        dto.setNxDoTodayOrder(order.getNxDoTodayOrder());
//
//        // 设置包装结构字段（确保字段始终存在，即使为空字符串）
//        logger.info("[convertToResponseDTO] 设置包装结构字段: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                standardWeight, itemUnit, itemsPerCarton, cartonUnit);
//        dto.setStandardWeight(standardWeight != null ? standardWeight : "");
//        dto.setItemUnit(itemUnit != null ? itemUnit : "");
//        dto.setItemsPerCarton(itemsPerCarton != null ? itemsPerCarton : "");
//        dto.setCartonUnit(cartonUnit != null ? cartonUnit : "");
//        logger.info("[convertToResponseDTO] 设置后的DTO字段值: standardWeight={}, itemUnit={}, itemsPerCarton={}, cartonUnit={}",
//                dto.getStandardWeight(), dto.getItemUnit(), dto.getItemsPerCarton(), dto.getCartonUnit());
//
//        // 处理候选商品列表
//        // 分销商商品候选列表（优先显示）
//        if (order.getNxDistributerGoodsEntityList() != null && !order.getNxDistributerGoodsEntityList().isEmpty()) {
//            List<DistributerGoodsCandidateDTO> distributerGoodsList = new ArrayList<>();
//            for (NxDistributerGoodsEntity goods : order.getNxDistributerGoodsEntityList()) {
//                DistributerGoodsCandidateDTO candidateDTO = new DistributerGoodsCandidateDTO();
//                candidateDTO.setNxDistributerGoodsId(goods.getNxDistributerGoodsId());
//                candidateDTO.setNxDgGoodsName(goods.getNxDgGoodsName());
//                candidateDTO.setNxDgGoodsStandardname(goods.getNxDgGoodsStandardname());
//                candidateDTO.setNxDgGoodsStandardWeight(goods.getNxDgGoodsStandardWeight());
//                candidateDTO.setNxDgCartonUnit(goods.getNxDgCartonUnit());
//                candidateDTO.setNxDgGoodsBrand(goods.getNxDgGoodsBrand());
//                candidateDTO.setNxDgGoodsFile(goods.getNxDgGoodsFile());
//                candidateDTO.setNxDgNxGoodsId(goods.getNxDgNxGoodsId());
//                distributerGoodsList.add(candidateDTO);
//            }
//            dto.setNxDistributerGoodsEntityList(distributerGoodsList);
//
//        }
//
//        // 系统商品候选列表（即使有分销商商品候选列表，也要显示系统商品，但已去除重复的）
//        if (order.getNxGoodsEntities() != null && !order.getNxGoodsEntities().isEmpty()) {
//            List<NxGoodsCandidateDTO> nxGoodsList = new ArrayList<>();
//            for (NxGoodsEntity goods : order.getNxGoodsEntities()) {
//                NxGoodsCandidateDTO candidateDTO = new NxGoodsCandidateDTO();
//                candidateDTO.setNxGoodsId(goods.getNxGoodsId());
//                candidateDTO.setNxGoodsName(goods.getNxGoodsName());
//                candidateDTO.setNxGoodsStandardname(goods.getNxGoodsStandardname());
//                candidateDTO.setNxGoodsStandardWeight(goods.getNxGoodsStandardWeight());
//                candidateDTO.setNxGoodsCartonUnit(goods.getNxGoodsCartonUnit());
//                candidateDTO.setNxGoodsBrand(goods.getNxGoodsBrand());
//                candidateDTO.setNxGoodsFile(goods.getNxGoodsFile());
//                candidateDTO.setNxGoodsFatherId(goods.getNxGoodsFatherId());
//                nxGoodsList.add(candidateDTO);
//            }
//            dto.setNxGoodsEntities(nxGoodsList);
//        }
//
//        return dto;
//    }
//
//    private NxDepartmentOrdersEntity updateOneOrderForChoice(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
//        System.out.println("saveONeOrderereerereeqonenenneorere" + order.getNxDepartmentOrdersId());
//        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
//        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
//        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
//        order.setNxDoStatus(0);
//        order.setNxDoArriveDate(formatWhatDate(0));
//        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
//        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
//        order.setNxDoNxGoodsId(disGoodsEntity.getNxDgNxGoodsId());
//        order.setNxDoNxGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
//        order.setNxDoApplyDate(formatWhatDay(0));
//        order.setNxDoArriveOnlyDate(formatWhatDate(0));
//        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
//        order.setNxDoArriveDate(formatWhatDay(0));
//        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
//        order.setNxDoApplyOnlyTime(formatWhatTime(0));
//        order.setNxDoGbDistributerId(-1);
//        order.setNxDoGbDepartmentFatherId(-1);
//        order.setNxDoGbDepartmentId(-1);
//        order.setNxDoNxCommunityId(-1);
//        order.setNxDoNxCommRestrauntFatherId(-1);
//        order.setNxDoNxCommRestrauntId(-1);
//        order.setNxDoCollaborativeNxDisId(-1);
//        //todo
//        order.setNxDoArriveWhatDay(getWeek(0));
//        order.setNxDoCostPriceLevel("1");
//
//        //auto
//        Integer nxDepartmentOrdersId = order.getNxDepartmentOrdersId();
//        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(nxDepartmentOrdersId);
//
//        System.out.println("updattepurrrr" + disGoodsEntity.getNxDgPurchaseAuto());
//        System.out.println("ordersEntity.getNxDoPurchaseGoodsId()" + ordersEntity.getNxDoPurchaseGoodsId());
//        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
//
//            if (ordersEntity.getNxDoPurchaseGoodsId() != null && ordersEntity.getNxDoPurchaseGoodsId() != -1) {
//                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(ordersEntity.getNxDoPurchaseGoodsId());
//                System.out.println("updattepurrrr" + purchaseGoodsEntity.getNxDpgOrdersAmount());
//                if (purchaseGoodsEntity.getNxDpgOrdersAmount() == 1) {
//                    nxDistributerPurchaseGoodsService.delete(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
//                    nxDepartmentOrdersService.savePurGoodsAuto(order, getNxPurchaseGoodsInputTypeOrder(), getNxPurchaseGoodsTypeForSelf());
//                } else {
//                    BigDecimal bigDecimal = new BigDecimal(purchaseGoodsEntity.getNxDpgQuantity()).subtract(new BigDecimal(ordersEntity.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    purchaseGoodsEntity.setNxDpgQuantity(bigDecimal.toString());
//                    if (purchaseGoodsEntity.getNxDpgBuyQuantity() != null && purchaseGoodsEntity.getNxDpgBuyPrice() != null) {
//                        BigDecimal bigDecimal1 = bigDecimal.subtract(new BigDecimal(purchaseGoodsEntity.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                        purchaseGoodsEntity.setNxDpgBuyQuantity(bigDecimal.toString());
//                        purchaseGoodsEntity.setNxDpgBuySubtotal(bigDecimal1.toString());
//                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
//                    }
//                }
//            } else {
//                nxDepartmentOrdersService.savePurGoodsAuto(order, getNxPurchaseGoodsInputTypeOrder(), getNxPurchaseGoodsTypeForSelf());
//            }
//        } else {
//            order.setNxDoPurchaseGoodsId(-1);
//        }
//
//        // 使用完善的价格处理逻辑（平台型/非平台型分支、货架库存查询、规格匹配等）
//        nxDepartmentOrdersService.processOrderPrice(order, disGoodsEntity);
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("depId", order.getNxDoDepartmentId());
//        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
//        map.put("standard", order.getNxDoStandard());
//        System.out.println("depmapapmmdmmdd" + map);
//        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
//        if (departmentDisGoodsEntity != null) {
//            System.out.println("prinstnan" + departmentDisGoodsEntity.getNxDdgOrderStandard());
//            // 判断是否使用了大包装单价，如果是则设置打印规格为大包装单位（支持智能匹配：件=箱）
//            boolean isUsingCartonPrice = disGoodsEntity.getNxDgCartonUnit() != null
//                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
//                    && order.getNxDoStandard() != null
//                    && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim());
//
//            if (isUsingCartonPrice) {
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
//                System.out.println("使用大包装单价，打印规格设置为: " + disGoodsEntity.getNxDgCartonUnit());
//            } else if (order.getNxDoCostPriceLevel() == null || order.getNxDoCostPriceLevel().equals("1")) {
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
//            } else {
//                order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
//            }
//
//            order.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
//            //如果有重量和单价，则计算 subtotal
//            if (order.getNxDoWeight() != null && !order.getNxDoWeight().trim().isEmpty()
//                    && order.getNxDoPrice() != null && !order.getNxDoPrice().trim().isEmpty()) {
//                try {
//                    BigDecimal weight = new BigDecimal(order.getNxDoWeight());
//                    BigDecimal price = new BigDecimal(order.getNxDoPrice());
//                    BigDecimal subtotal = weight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    order.setNxDoSubtotal(subtotal.toString());
//                } catch (NumberFormatException e) {
//
//                }
//            }
//            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
//        } else {
//            map.put("standard", null);
//            System.out.println("depmapapapappapa" + map);
//            NxDepartmentDisGoodsEntity departmentDisGoodsEntityO = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
//            if (departmentDisGoodsEntityO != null) {
//                order.setNxDoDepDisGoodsId(departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
//            }
//        }
//
//
//        nxDepartmentOrdersService.update(order);
//
//        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
//        order.setNxDistributerGoodsEntity(distributerGoodsEntity);
//
//        // 确保返回的订单状态正确
//        logger.info("[saveOneOrder] 返回订单: 商品名称={}, 订单ID={}, 状态={}, 分销商商品ID={}",
//                order.getNxDoGoodsName(), order.getNxDepartmentOrdersId(),
//                order.getNxDoStatus(), order.getNxDoDisGoodsId());
//
//        // 找到商品后，设置为0（已保存），但如果订单状态是 -2（待修正），则保持 -2
//        if (order.getNxDoStatus() == null || (order.getNxDoStatus() != 0 && order.getNxDoStatus() != -2)) {
//            order.setNxDoStatus(0);
//        }
//
//        return order;
//    }


    //    private NxDepartmentOrdersEntity choiceGoodsOrder(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
//        logger.info("choiceGoodsOrderNw" , order.getNxDoGoodsName());
//        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
//        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
//        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
//        order.setNxDoStatus(0);
//        order.setNxDoArriveDate(formatWhatDate(0));
//        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
//        order.setNxDoNxGoodsId(disGoodsEntity.getNxDgNxGoodsId());
//        order.setNxDoNxGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
//        order.setNxDoApplyDate(formatWhatDay(0));
//        order.setNxDoArriveOnlyDate(formatWhatDate(0));
//        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
//        order.setNxDoArriveDate(formatWhatDay(0));
//        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
//        order.setNxDoApplyOnlyTime(formatWhatTime(0));
//        order.setNxDoGbDistributerId(-1);
//        order.setNxDoGbDepartmentFatherId(-1);
//        order.setNxDoGbDepartmentId(-1);
//        order.setNxDoNxCommunityId(-1);
//        order.setNxDoNxCommRestrauntFatherId(-1);
//        order.setNxDoNxCommRestrauntId(-1);
//        order.setNxDoPurchaseGoodsId(disGoodsEntity.getNxDgPurchaseAuto());
//
//        order.setNxDoArriveWhatDay(getWeek(0));
//
//        Integer nxDoDisGoodsId = order.getNxDoDisGoodsId();
//        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
//
//        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = updateOneOrderForChoice(order, nxDistributerGoodsEntity);
//
//        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
//        return order;
//    }
    // not good
    @RequestMapping(value = "/stokerGetToStockGoodsWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R stokerGetToStockGoodsWithDepIds(String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDisId);
        map.put("status", 3);

        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }
        map.put("purStatus", 4);

        Integer countDep = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        System.out.println("fatheridiidididi" + map);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("grandArr", greatGrandGoods);

        System.out.println("zahuishsshisisis" + map);
        mapR.put("depOrdersWait", count);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", nxDisId);
        mapFin.put("status", 3);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        mapR.put("depOrdersWait", count);
        mapR.put("idDepOrdersWait", countDep);

        mapFin.put("purStatus", 4);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapFin);
        mapFin.put("purStatus", null);
        mapFin.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapFin);
        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        return R.ok().put("data", mapR);

    }


    // not good
    @RequestMapping(value = "/pickerGetToStockGoodsWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R pickerGetToStockGoodsWithDepIds(String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDisId);
        map.put("status", 3);

        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }
        map.put("purStatus", 4);

        Integer countDep = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        System.out.println("fatheridiidididi" + map);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("grandArr", greatGrandGoods);

        System.out.println("zahuishsshisisis" + map);
        mapR.put("depOrdersWait", count);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", nxDisId);
        mapFin.put("status", 3);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        mapR.put("depOrdersWait", count);
        mapR.put("idDepOrdersWait", countDep);

        mapFin.put("purStatus", 4);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapFin);
        mapFin.put("purStatus", null);
        mapFin.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapFin);
        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        return R.ok().put("data", mapR);

    }


    //not good
    @RequestMapping(value = "/disGetToStockGoodsWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R disGetToStockGoodsWithDepIds(String nxDepIds, String gbDepIds, Integer nxDisId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();

        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }

        map.put("disId", nxDisId);
        map.put("goodsType", goodsType);
        System.out.println("fatheridiidididiwwgggggg" + map);
        map.put("purStatus", 4);
        Integer preOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("grandArr", greatGrandGoods);
        mapR.put("depOrdersWait", preOrders);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", nxDisId);
        mapFin.put("status", 3);
        mapFin.put("goodsType", goodsType);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", nxDisId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 2);
        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", nxDisId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        int wxCountAutoOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 0);
        int zicaiCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCount);
        mapR.put("wxCountOk", wxCountOk);
        mapR.put("preOrders", preOrders);
        return R.ok().put("data", mapR);

    }

    // not good
    @RequestMapping(value = "/disGetToStockNxDepGoodsFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetToStockNxDepGoodsFatherGoods(Integer fatherId, String nxDepIds, String gbDepIds, Integer disId, Integer goodsType) {

        Map<String, Object> map = new HashMap<>();


        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }
        Integer orderCount = 0;
        map.put("status", 3);
        map.put("fathersFatherId", fatherId);
        map.put("purStatus", 4);
        map.put("goodsType", goodsType);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);
        map.put("purStatus", 4);
        orderCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("status", 3);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapDep);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);


        Map<String, Object> mapR = new HashMap<>();
        mapR.put("grandArr", fatherGoodsEntities);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        mapR.put("orderCount", orderCount);

        return R.ok().put("data", mapR);

    }


    @RequestMapping("/downloadApplysExcel")
    @ResponseBody
    public void downloadApplysExcel(HttpServletResponse response, HttpServletRequest request) {
        String id = request.getParameter("id");
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            System.out.println(id);

            wb = toCreatNxDisControlGoodsPriceForm(id);


            String fileName = new String("导出商品.xls".getBytes("utf-8"), "iso8859-1");
            response.setHeader("content-Disposition", "attachment; filename =" + fileName);
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private HSSFWorkbook toCreatNxDisControlGoodsPriceForm(String id) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("depId", id);
        map.put("equalStatus", 0);
        System.out.println("mapamapapapExcelleleel" + map);
        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(Integer.valueOf(id));

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);

        if (ordersEntities.size() > 0) {
            HSSFSheet sheet = wb.createSheet(departmentEntity.getNxDepartmentName());
            //设置表头
            HSSFRow row1 = sheet.createRow(0);
            row1.createCell(0).setCellValue("序号");
            row1.createCell(1).setCellValue("商品名称");
            row1.createCell(2).setCellValue("规格");
            row1.createCell(3).setCellValue("订货");
            row1.createCell(4).setCellValue("备注");
            //设置表体
            HSSFRow goodsRow = null;
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                goodsRow.createCell(1).setCellValue(ordersEntity.getNxDistributerGoodsEntity().getNxDgGoodsName());
                goodsRow.createCell(2).setCellValue(ordersEntity.getNxDistributerGoodsEntity().getNxDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ordersEntity.getNxDoQuantity() + ordersEntity.getNxDoStandard());
                goodsRow.createCell(4).setCellValue(ordersEntity.getNxDoRemark());
            }
        }

        return wb;
    }

    @RequestMapping(value = "/getHaveNotOutCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getHaveNotOutCataGoods(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("goodsType", goodsType);
        System.out.println("mappapapapap" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        return R.ok().put("data", fatherGoodsEntities);
    }

    @RequestMapping(value = "/stokerHaveNotOutCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R stokerHaveNotOutCataGoods(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("dayuOrderStatus", -2);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        if (supplierId != null && supplierId != -1) {
            map.put("nxSupplierId", supplierId);
            map.put("purStatus", 2);
        }
        System.out.println("mappapapapap" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        Integer notInteger = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("purStatus", null);
        map.put("dayuPurStatus", 3);
        Integer haveInteger = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("haveCount", haveInteger);
        mapR.put("notCount", notInteger);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/stokerHaveNotCollOutCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R stokerHaveNotCollOutCataGoods(Integer collNxDisId, Integer nxDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("disId", nxDisId);
        map.put("collNxDisId", collNxDisId);

        System.out.println("mappapapapap" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        Integer notInteger = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("purStatus", null);
        map.put("dayuPurStatus", 3);
        Integer haveInteger = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("haveCount", haveInteger);
        mapR.put("notCount", notInteger);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/stockerGetHaveOutCollCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetHaveOutCollCataGoods(Integer collNxDisId, Integer nxDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDisId);
        map.put("status", 3);
        map.put("dayuPurStatus", 3);
        map.put("collNxDisId", collNxDisId);


        System.out.println("getHaveOutCataGoodsstockerGetHaveOutCataGoods" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("status", 3);
        mapCount.put("purStatus", 4);
        mapCount.put("collNxDisId", collNxDisId);

        Integer notInteger = nxDepartmentOrdersService.queryDepOrdersAcount(mapCount);
        mapCount.put("purStatus", null);
        mapCount.put("dayuPurStatus", 3);
        Integer haveInteger = nxDepartmentOrdersService.queryDepOrdersAcount(mapCount);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("haveCount", haveInteger);
        mapR.put("notCount", notInteger);
        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/stockerGetHaveOutCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetHaveOutCataGoods(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer supplierId, Integer batchSupplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("dayuPurStatus", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        if (supplierId != null && supplierId != -1) {
            map.put("nxSupplierId", supplierId);
            map.put("dayuPurStatus", 2);
        }


        System.out.println("getHaveOutCataGoodsstockerGetHaveOutCataGoods" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("status", 3);
        mapCount.put("purStatus", 4);
        mapCount.put("depFatherId", depFatherId);
        mapCount.put("resFatherId", resFatherId);
        mapCount.put("gbDepFatherId", gbDepFatherId);
        if (supplierId != null && supplierId != -1) {
            mapCount.put("nxSupplierId", supplierId);
            mapCount.put("purStatus", 2);
        }

        Integer notInteger = nxDepartmentOrdersService.queryDepOrdersAcount(mapCount);
        mapCount.put("purStatus", null);
        mapCount.put("dayuPurStatus", 3);
        Integer haveInteger = nxDepartmentOrdersService.queryDepOrdersAcount(mapCount);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("haveCount", haveInteger);
        mapR.put("notCount", notInteger);
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/getHaveOutCataGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getHaveOutCataGoods(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("dayuPurStatus", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("goodsType", goodsType);
        System.out.println("getHaveOutCataGoods" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        return R.ok().put("data", fatherGoodsEntities);
    }


    @RequestMapping(value = "/pickerGetToStockGoodsWithDepIdsKf", method = RequestMethod.POST)
    @ResponseBody
    public R pickerGetToStockGoodsWithDepIdsKf(String nxDepIds, String gbDepIds, Integer disId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();

        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }

        map.put("disId", disId);
        map.put("purStatus", 2);
        map.put("goodsType", goodsType);
        System.out.println("fatheridiidididi" + map);
        map.put("shelfId", 1);
        System.out.println("shelflfllfflfff111" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        map.put("shelfId", 0);
        System.out.println("shelflfllfflfff000" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities2 = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        List<NxDistributerGoodsShelfGoodsEntity> emptyList = new ArrayList<>();
        NxDistributerGoodsShelfEntity emptyShelf = new NxDistributerGoodsShelfEntity();
        if (shelfEntities2.size() > 0) {
            for (NxDistributerGoodsShelfEntity emptyEnity : shelfEntities2) {
                emptyList.addAll(emptyEnity.getNxDisGoodsShelfGoodsEntities());
            }
            emptyShelf.setNxDisGoodsShelfGoodsEntities(emptyList);
            shelfEntities.add(emptyShelf);
        }


        return R.ok().put("data", shelfEntities);

    }


    @RequestMapping(value = "/disGetToStockGoodsWithDepIdsKf", method = RequestMethod.POST)
    @ResponseBody
    public R disGetToStockGoodsWithDepIdsKf(String nxDepIds, String gbDepIds, Integer nxDisId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();

        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }

        map.put("disId", nxDisId);
        map.put("purStatus", 2);
        map.put("goodsType", goodsType);
        System.out.println("fatheridiidididi" + map);
        map.put("shelfId", 1);
        System.out.println("shelflfllfflfff111" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        map.put("shelfId", 0);
        System.out.println("shelflfllfflfff000" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities2 = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        List<NxDistributerGoodsShelfGoodsEntity> emptyList = new ArrayList<>();
        NxDistributerGoodsShelfEntity emptyShelf = new NxDistributerGoodsShelfEntity();
        if (shelfEntities2.size() > 0) {
            for (NxDistributerGoodsShelfEntity emptyEnity : shelfEntities2) {
                emptyList.addAll(emptyEnity.getNxDisGoodsShelfGoodsEntities());
            }
            emptyShelf.setNxDisGoodsShelfGoodsEntities(emptyList);
            shelfEntities.add(emptyShelf);
        }


        mapR.put("shelfArr", shelfEntities);


        System.out.println("zahuishsshisisis" + map);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        mapR.put("depOrdersWait", count);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", nxDisId);
        mapFin.put("status", 3);
        mapFin.put("goodsType", goodsType);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", nxDisId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 2);
        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", nxDisId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        int wxCountAutoOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 0);
        int zicaiCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCount);
        mapR.put("wxCountOk", wxCountOk);
        mapR.put("wxCountAuto", wxCountAuto);
        mapR.put("wxCountAutoOk", wxCountAutoOk);
        mapR.put("zicaiCount", zicaiCount);
        mapR.put("zicaiCountOk", zicaiCountOk);
        return R.ok().put("data", mapR);

    }



    @RequestMapping(value = "/stockerGetToStockGoodsWithCollDisId", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetToStockGoodsWithCollDisId(Integer collNxDisId, Integer nxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("collNxDisId", collNxDisId);
        params.put("purStatus", 4);
        params.put("dayuOrderStatus", -2);

        // 获取所有数据
        Map<String, Object> result = nxDepartmentOrdersService.queryStockGoodsData(params);

        return R.ok().put("data", result);
    }


    @RequestMapping(value = "/stockerGetToStockGoodsWithDepIdsKf", method = RequestMethod.POST)
    @ResponseBody
    @Cacheable(value = "stockGoods", key = "#nxDisId + '_' + #nxDepIds + '_' + #gbDepIds")
    public R stockerGetToStockGoodsWithDepIdsKf(String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("purStatus", 4);
        params.put("dayuOrderStatus", -2);
        // 处理部门ID
        List<String> idsGb = parseDepIds(gbDepIds);
        List<String> idsNx = parseDepIds(nxDepIds);

        if (!idsGb.isEmpty()) {
            params.put("gbDepIds", idsGb);
        }
        if (!idsNx.isEmpty()) {
            params.put("nxDepIds", idsNx);
        }
        // 获取所有数据
        Map<String, Object> result = nxDepartmentOrdersService.queryStockGoodsData(params);

        return R.ok().put("data", result);
    }


    /**
     * 解析部门 ID 列表。不传、"0"、"-1"、空串表示<strong>不按</strong> nx / gb 部门 father_id 筛选。
     * 逗号分隔时也会去掉其中的 0、-1；若去掉后无有效 ID，同样不筛选。
     */
    private List<String> parseDepIds(String depIds) {
        if (depIds == null) {
            return Collections.emptyList();
        }
        String t = depIds.trim();
        if (t.isEmpty() || "0".equals(t) || "-1".equals(t)) {
            return Collections.emptyList();
        }
        String[] parts = t.split("\\s*,\\s*");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            if (p == null) {
                continue;
            }
            String s = p.trim();
            if (s.isEmpty() || "0".equals(s) || "-1".equals(s)) {
                continue;
            }
            out.add(s);
        }
        return out;
    }

    /**
     * 获取货架列表（仅基本信息，不包含商品详情）
     *
     * @param nxDepIds 部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param gbDepIds GB部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param nxDisId  分销商ID
     * @return 包含货架列表、部门列表、统计数据的Map
     */
    @RequestMapping(value = "/stockerGetShelfListWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetShelfListWithDepIds(String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("status", 3);
        params.put("purStatus", 4);
        params.put("dayuOrderStatus", -2);

        // 处理部门ID
        List<String> idsGb = parseDepIds(gbDepIds);
        List<String> idsNx = parseDepIds(nxDepIds);

        if (!idsGb.isEmpty()) {
            params.put("gbDepIds", idsGb);
        }
        if (!idsNx.isEmpty()) {
            params.put("nxDepIds", idsNx);
        }

        // 获取货架列表和统计数据
        Map<String, Object> result = nxDepartmentOrdersService.queryShelfListWithDepIds(params);

        return R.ok().put("data", result);
    }

    /**
     * 获取指定货架的商品详情（包含订单信息）
     *
     * @param shelfId  货架ID
     * @param nxDepIds 部门 father_id 列表；"0"或"-1"或不传表示不按 nx 部门筛选，多个逗号分隔
     * @param gbDepIds GB 部门 father_id 列表；"0"或"-1"或不传表示不按 gb 部门筛选
     * @param nxDisId  分销商ID
     * @return 完整的货架对象（包含商品列表和订单）
     */
    @RequestMapping(value = "/stockerGetShelfGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetShelfGoodsDetail(Integer shelfId, String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("status", 3);
        params.put("purStatus", 4);
        params.put("shelfId", shelfId);
        params.put("dayuOrderStatus", -2);

        // 处理部门ID
        List<String> idsGb = parseDepIds(gbDepIds);
        List<String> idsNx = parseDepIds(nxDepIds);

        if (!idsGb.isEmpty()) {
            params.put("gbDepIds", idsGb);
        }
        if (!idsNx.isEmpty()) {
            params.put("nxDepIds", idsNx);
        }

        // 使用超简化版查询，字段扁平化，大幅减少数据传输量
        ShelfDetailSimpleDTO shelf = nxDepartmentOrdersService.queryShelfGoodsDetailUltraSimple(params);

        // 打印日志，检查 nxDepartmentAttrName、库存批次（含生产日期/保质期/过期日期）等字段
        if (shelf != null && shelf.getGoodsList() != null) {
            logger.info("========= stockerGetShelfGoodsDetail 返回数据检查 =========");
            logger.info("货架ID: {}, 货架名称: {}", shelf.getNxDistributerGoodsShelfId(), shelf.getNxDistributerGoodsShelfName());
            logger.info("商品数量: {}", shelf.getGoodsList().size());

            int orderCount = 0;
            int hasAttrNameCount = 0;
            int nullAttrNameCount = 0;
            int stockCount = 0;
            int sameShelfGoodsStockCount = 0;

            for (ShelfGoodsShelfGoodsSimpleDTO shelfGoods : shelf.getGoodsList()) {
                // 当前货架商品库存
                if (shelfGoods.getNxDisGoodsShelfStockEntities() != null) {
                    stockCount += shelfGoods.getNxDisGoodsShelfStockEntities().size();
                }
                // 其他货架商品及库存
                if (shelfGoods.getSameShelfGoods() != null) {
                    for (ShelfGoodsShelfGoodsSimpleDTO same : shelfGoods.getSameShelfGoods()) {
                        if (same.getNxDisGoodsShelfStockEntities() != null) {
                            sameShelfGoodsStockCount += same.getNxDisGoodsShelfStockEntities().size();
                        }
                    }
                }
                if (shelfGoods.getGoods() != null && shelfGoods.getGoods().getOrders() != null) {
                    for (ShelfOrderSimpleDTO order : shelfGoods.getGoods().getOrders()) {
                        orderCount++;
                        if (order.getNxDepartmentAttrName() != null && !order.getNxDepartmentAttrName().isEmpty()) {
                            hasAttrNameCount++;
                            logger.info("订单ID: {}, nxDepartmentAttrName: {}, depName: {}",
                                    order.getNxDepartmentOrdersId(),
                                    order.getNxDepartmentAttrName(),
                                    order.getDepName());
                        } else {
                            nullAttrNameCount++;
                            logger.warn("订单ID: {}, nxDepartmentAttrName 为 NULL 或空, depName: {}",
                                    order.getNxDepartmentOrdersId(),
                                    order.getDepName());
                        }
                    }
                }
            }

            logger.info("总订单数: {}, 有 nxDepartmentAttrName 的订单数: {}, nxDepartmentAttrName 为 NULL 的订单数: {}",
                    orderCount, hasAttrNameCount, nullAttrNameCount);
            logger.info("当前货架库存批次数: {}, 其他货架库存批次数: {}", stockCount, sameShelfGoodsStockCount);
            logger.info("========= stockerGetShelfGoodsDetail 数据检查完成 =========");
        } else {
            logger.warn("stockerGetShelfGoodsDetail 返回的 shelf 为 NULL 或 goodsList 为 NULL");
        }

        return R.ok().put("data", shelf);
    }

    /**
     * 获取统计数据（不包含货架和商品数据）
     *
     * @param nxDepIds 部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param gbDepIds GB部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param nxDisId  分销商ID
     * @return 包含部门列表和统计数据的Map
     */
    @RequestMapping(value = "/stockerGetShelfStatistics", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetShelfStatistics(String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("status", 3);
        params.put("purStatus", 4);

        // 处理部门ID
        List<String> idsGb = parseDepIds(gbDepIds);
        List<String> idsNx = parseDepIds(nxDepIds);

        if (!idsGb.isEmpty()) {
            params.put("gbDepIds", idsGb);
        }
        if (!idsNx.isEmpty()) {
            params.put("nxDepIds", idsNx);
        }

        // 获取统计数据
        Map<String, Object> result = nxDepartmentOrdersService.queryShelfStatistics(params);

        return R.ok().put("data", result);
    }

    /**
     * 获取类别列表（曾祖父级别，仅基本信息，不包含商品详情）
     *
     * @param nxDepIds 部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param gbDepIds GB部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param nxDisId  分销商ID
     * @return 包含类别列表、部门列表和统计数据的Map
     */
    @RequestMapping(value = "/stockerGetCategoryListWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetCategoryListWithDepIds(String nxDepIds, String gbDepIds, Integer nxDisId, Integer supplierId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("status", 3);
        params.put("purStatus", 4);
        params.put("dayuOrderStatus", -2);
        if (supplierId != null && supplierId != -1) {
            params.put("nxSupplierId", supplierId);
            params.put("purStatus", 2);
        }

        // 处理部门ID
        List<String> idsGb = parseDepIds(gbDepIds);
        List<String> idsNx = parseDepIds(nxDepIds);

        if (!idsGb.isEmpty()) {
            params.put("gbDepIds", idsGb);
        }
        if (!idsNx.isEmpty()) {
            params.put("nxDepIds", idsNx);
        }

        // 获取类别列表和统计数据
        Map<String, Object> result = nxDepartmentOrdersService.queryCategoryListWithDepIds(params);

        return R.ok().put("data", result);
    }


    @RequestMapping(value = "/stockerGetCategoryListWithCollNxDisId", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetCategoryListWithCollNxDisId(Integer collNxDisId, Integer nxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("collNxDisId", collNxDisId);
        params.put("status", 3);
        params.put("purStatus", 4);
        params.put("dayuOrderStatus", -2);

        // 获取类别列表和统计数据
        Map<String, Object> result = nxDepartmentOrdersService.queryCategoryListWithDepIds(params);

        return R.ok().put("data", result);
    }

    /**
     * 获取指定类别的商品详情（包含订单信息）
     *
     * @param categoryId 类别ID（Integer类型，必填）- 曾祖父级别ID
     * @param nxDepIds   部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param gbDepIds   GB部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param nxDisId    分销商ID
     * @return 完整的类别对象（包含商品列表和订单）
     */
    @RequestMapping(value = "/stockerGetCategoryGoodsDetail", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetCategoryGoodsDetail(Integer categoryId, String nxDepIds, String gbDepIds, Integer nxDisId, Integer supplierId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("status", 3);
        params.put("purStatus", 4);
        params.put("categoryId", categoryId);
        if (supplierId != null && supplierId != -1) {
            params.put("nxSupplierId", supplierId);
            params.put("purStatus", 2);
        }
        // 处理部门ID
        List<String> idsGb = parseDepIds(gbDepIds);
        List<String> idsNx = parseDepIds(nxDepIds);

        if (!idsGb.isEmpty()) {
            params.put("gbDepIds", idsGb);
        }
        if (!idsNx.isEmpty()) {
            params.put("nxDepIds", idsNx);
        }

        // 使用超简化版查询，字段扁平化，大幅减少数据传输量
        CategoryDetailSimpleDTO category = nxDepartmentOrdersService.queryCategoryGoodsDetailUltraSimple(params);

        return R.ok().put("data", category);
    }


    @RequestMapping(value = "/stockerGetCategoryGoodsDetailWithCooNxDisId", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetCategoryGoodsDetailWithCooNxDisId(Integer categoryId,  Integer nxDisId, Integer collNxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("collNxDisId", collNxDisId);
        params.put("disId", nxDisId);
        params.put("status", 3);
        params.put("purStatus", 4);
        params.put("categoryId", categoryId);


        // 使用超简化版查询，字段扁平化，大幅减少数据传输量
        CategoryDetailSimpleDTO category = nxDepartmentOrdersService.queryCategoryGoodsDetailUltraSimple(params);

        return R.ok().put("data", category);
    }
    /**
     * 获取统计数据（不包含类别和商品数据）
     *
     * @param nxDepIds 部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param gbDepIds GB部门ID列表（String类型），"0"表示全部，多个用逗号分隔
     * @param nxDisId  分销商ID
     * @return 包含部门列表和统计数据的Map
     */
    @RequestMapping(value = "/stockerGetCategoryStatistics", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetCategoryStatistics(String nxDepIds, String gbDepIds, Integer nxDisId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", nxDisId);
        params.put("status", 3);
        params.put("purStatus", 4);

        // 处理部门ID
        List<String> idsGb = parseDepIds(gbDepIds);
        List<String> idsNx = parseDepIds(nxDepIds);

        if (!idsGb.isEmpty()) {
            params.put("gbDepIds", idsGb);
        }
        if (!idsNx.isEmpty()) {
            params.put("nxDepIds", idsNx);
        }

        // 获取统计数据
        Map<String, Object> result = nxDepartmentOrdersService.queryCategoryStatistics(params);

        return R.ok().put("data", result);
    }


    @RequestMapping(value = "/disOutOrdersWithWeightFinish", method = RequestMethod.POST)
    @ResponseBody
    public R disOutOrdersWithWeightFinish(@RequestBody List<NxDepartmentOrdersEntity> ordersEntities) {

        for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
            Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
            NxDepartmentOrdersEntity oldOrderEntity = nxDepartmentOrdersService.queryObject(nxDepartmentOrdersId);

            oldOrderEntity.setNxDoWeight(ordersEntity.getNxDoWeight());
            BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());

            if (oldOrderEntity.getNxDoPrice() != null && !oldOrderEntity.getNxDoPrice().trim().isEmpty()) {
                BigDecimal priceB = new BigDecimal(oldOrderEntity.getNxDoPrice());
                BigDecimal decimal1 = priceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                oldOrderEntity.setNxDoStatus(getNxOrderStatusHasFinished());
                oldOrderEntity.setNxDoSubtotal(decimal1.toString());

            }

            BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
            BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
            oldOrderEntity.setNxDoCostSubtotal(decimal.toString());
            oldOrderEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
            nxDepartmentOrdersService.update(oldOrderEntity);
        }

        return R.ok();
    }

    @RequestMapping(value = "/disOutOrdersFinish/{ids}")
    @ResponseBody
    public R disOutOrdersFinish(@PathVariable String ids) {

        String[] arr = ids.split(",");
        if (arr.length > 0) {
            for (String id : arr) {
                NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(Integer.valueOf(id));
                ordersEntity.setNxDoWeight(ordersEntity.getNxDoQuantity());
                BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());

                if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                    BigDecimal priceB = new BigDecimal(ordersEntity.getNxDoPrice());
                    BigDecimal decimal1 = priceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoSubtotal(decimal1.toString());
                    ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
                }
                //cost
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoCostSubtotal(decimal.toString());
                ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
                nxDepartmentOrdersService.update(ordersEntity);

            }
        }


        return R.ok();
    }

    @RequestMapping(value = "/disGetStockGoodsOrdersFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoodsOrdersFatherGoods(Integer fatherId, Integer disId, Integer goodsType) {

        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", fatherId);
        map.put("equalPurStatus", 1);
        map.put("goodsType", goodsType);
        Map<String, Object> mapR = new HashMap<>();
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);
        mapR.put("grandArr", fatherGoodsEntities);
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("status", 3);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapDep);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapDep);

        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/disGetOutGoodsOrdersFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetOutGoodsOrdersFatherGoods(Integer fatherId, Integer disId) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("fathersFatherId", fatherId);
        map.put("purStatus", 4);
        map.put("dayuPurStatus", 1);
        map.put("purType", 1);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryDisGoodsForTodayOrders(map);
        if (fatherGoodsEntities.size() > 0) {
            mapR.put("grandArr", fatherGoodsEntities);

        } else {
            mapR.put("grandArr", new ArrayList<>());
        }
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("purStatus", 4);
        mapDep.put("status", 3);
        mapDep.put("purType", 1);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapDep);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", disId);
        mapFin.put("status", 3);
        mapFin.put("dayuPurStatus", 3);
        mapFin.put("purType", 1);
        List<NxDepartmentEntity> departmentEntitiesFinish = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntitiesFinish = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);


        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        mapR.put("finishDepNx", departmentEntitiesFinish);
        mapR.put("finishDepGb", gbDepartmentEntitiesFinish);
        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/disGetHaveOutGoodsOrdersFatherGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetHaveOutGoodsOrdersFatherGoods(Integer fatherId, Integer disId) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("fathersFatherId", fatherId);
        map.put("dayuPurStatus", 3);
        map.put("purType", 1);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryDisGoodsForTodayOrders(map);

        return R.ok().put("data", fatherGoodsEntities);

    }

    @RequestMapping(value = "/disGetToOutGoods/{disId}")
    @ResponseBody
    public R disGetToOutGoods(@PathVariable Integer disId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("dayuPurStatus", 1);
        map.put("purType", 1);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        List<NxDistributerFatherGoodsEntity> distributerGoodsEntities = new ArrayList<>();
        if (greatGrandGoods.size() > 0) {
            NxDistributerFatherGoodsEntity greatGrandsEntity = greatGrandGoods.get(0);
            Integer greatGarndGoodsId = greatGrandsEntity.getNxDistributerFatherGoodsId();
            map.put("fathersFatherId", greatGarndGoodsId);
            distributerGoodsEntities = nxDepartmentOrdersService.queryDisGoodsForTodayOrders(map);
            mapR.put("cataArr", greatGrandGoods);

        } else {
            mapR.put("cataArr", new ArrayList<>());
        }
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(map);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", disId);
        mapFin.put("status", 3);
        mapFin.put("dayuPurStatus", 3);
        mapFin.put("purType", 1);
        List<NxDepartmentEntity> departmentEntitiesFinish = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntitiesFinish = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);

        mapR.put("grandArr", distributerGoodsEntities);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        mapR.put("finishDepNx", departmentEntitiesFinish);
        mapR.put("finishDepGb", gbDepartmentEntitiesFinish);
        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("purStatus", 5);
        map111.put("purType", 0);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("purType", 1);
        map111.put("dayuPurStatus", 1);
        int purCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);


        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("purType", 0);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("purType", 1);
        int purCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("purCount", purCount);
        mapR.put("purCountOk", purCountOk);

        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/disGetHaveOutGoods/{disId}")
    @ResponseBody
    public R disGetHaveOutGoods(@PathVariable Integer disId) {
        System.out.println("didiiididdiid" + disId);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("dayuPurStatus", 3);
        map.put("purType", 1);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        List<NxDistributerFatherGoodsEntity> distributerGoodsEntities = new ArrayList<>();
        if (greatGrandGoods.size() > 0) {
            NxDistributerFatherGoodsEntity greatGrandsEntity = greatGrandGoods.get(0);
            Integer greatGarndGoodsId = greatGrandsEntity.getNxDistributerFatherGoodsId();
            map.put("fathersFatherId", greatGarndGoodsId);
            System.out.println("mapgranidididiidi" + map);
            distributerGoodsEntities = nxDepartmentOrdersService.queryDisGoodsForTodayOrders(map);
        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr", greatGrandGoods);
        mapR.put("grandArr", distributerGoodsEntities);
        System.out.println("mapapprpr" + mapR);

        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/stockerGetWaitStockGoodsDeps", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetWaitStockGoodsDeps(Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purStatus", 4);
        map.put("dayuOrderStatus", -2);
        System.out.println("mapappapa" + map);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        map.put("isSelfOrder", -1);
        // 1. 获取部门列表
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartmentSimple(map);
        List<GbDepartmentEntity> gbDepartmentEntityList = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        // 过滤掉 null 元素
        if (departmentEntities != null) {
            departmentEntities = departmentEntities.stream()
                    .filter(dept -> dept != null && dept.getNxDepartmentId() != null)
                    .collect(Collectors.toList());
        }
        if (gbDepartmentEntityList != null) {
            gbDepartmentEntityList = gbDepartmentEntityList.stream()
                    .filter(dept -> dept != null && dept.getGbDepartmentId() != null)
                    .collect(Collectors.toList());
        }

        // 2. 批量获取统计数据
        if (departmentEntities != null && !departmentEntities.isEmpty()) {
            List<Integer> depIds = departmentEntities.stream()
                    .map(NxDepartmentEntity::getNxDepartmentId)
                    .collect(Collectors.toList());

            // 一次性查询所有部门的统计数据
            Map<Integer, Map<String, Integer>> depStats = nxDepartmentOrdersService.batchQueryDepStats(depIds, null);

            // 设置统计数据
            for (NxDepartmentEntity dept : departmentEntities) {
                Map<String, Integer> stats = depStats.get(dept.getNxDepartmentId());
                if (stats != null) {
                    dept.setNxDepartmentAddCount(stats.get("count0"));
                    dept.setNxDepartmentPurOrderCount(stats.get("count1"));
                    dept.setNxDepartmentNeedNotPurOrderCount(stats.get("count2"));
                }
            }
        }

        // 3. 同样处理 GbDepartment
        if (gbDepartmentEntityList != null && !gbDepartmentEntityList.isEmpty()) {
            List<Integer> gbDepIds = gbDepartmentEntityList.stream()
                    .map(GbDepartmentEntity::getGbDepartmentId)
                    .collect(Collectors.toList());

            System.out.println("depididiisisississigbDepIds" + gbDepIds);

            Map<Integer, Map<String, Integer>> gbDepStats = nxDepartmentOrdersService.batchQueryGbDepStats(gbDepIds, null);

            for (GbDepartmentEntity dept : gbDepartmentEntityList) {
                Map<String, Integer> stats = gbDepStats.get(dept.getGbDepartmentId());
                if (stats != null) {
                    dept.setGbDepartmentAddCount(stats.get("count0"));
                    dept.setGbDepartmentPurOrderCount(stats.get("count1"));
                    dept.setGbDepartmentNeedNotPurOrderCount(stats.get("count2"));
                }
            }
        }


        //协作订单
        Map<String, Object> mapOffer = new HashMap<>();
        mapOffer.put("disId", disId);
        mapOffer.put("isSelfOrder", 1);
//        mapOffer.put("purStatus", 4);
        mapOffer.put("status", 3);
        System.out.println("enennenenennemapOffer" + mapOffer);
        List<NxDistributerEntity> offerNxDistributer = nxDepartmentOrdersService.queryOfferOrderNxDistributer(mapOffer);
        if(offerNxDistributer.size() > 0){
            for(NxDistributerEntity distributerEntity: offerNxDistributer){
                // 每个协作商独立查询，需传入 collNxDisId
                Map<String, Object> mapColl = new HashMap<>();
                mapColl.put("disId", disId);
                mapColl.put("isSelfOrder", 1);
                mapColl.put("status", 3);
                mapColl.put("collNxDisId", distributerEntity.getNxDistributerId());

                int orderCount = nxDepartmentOrdersService.queryOrderGoodsCount(mapColl);
                Map<String, Object> mapPick = new HashMap<>(mapColl);
                mapPick.put("equalPurStatus", 4);
                int pickCount = nxDepartmentOrdersService.queryOrderGoodsCount(mapPick);

                // totalCount、hasPrice、hasWeight、twoSubtotal 四个字段
                int totalCount = nxDepartmentOrdersService.queryDepOrdersAcount(mapColl);
                mapColl.put("hasPrice", 1);
                int hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(mapColl);
                mapColl.put("hasPrice", null);
                mapColl.put("hasWeight", 1);
                int hasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(mapColl);
                mapColl.put("hasWeight", null);
                Double subtotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapColl);
                String twoSubtotal = subtotal != null && subtotal > 0
                        ? new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString()
                        : "0";

                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("orderCount", orderCount);
                mapItem.put("pickCount", pickCount);
                mapItem.put("totalCount", totalCount);
                mapItem.put("twoSubtotal", twoSubtotal);
                mapItem.put("hasPrice", hasPrice);
                mapItem.put("hasWeight", hasWeight);
                distributerEntity.setAaa(mapItem);
            }
        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDep", departmentEntities);
        mapR.put("gbDep", gbDepartmentEntityList);
        mapR.put("depOrdersWait", count  );
        mapR.put("offerArr", offerNxDistributer);

        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/supplierGetWaitStockGoodsDeps", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGetWaitStockGoodsDeps(Integer disId, Integer supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purStatus", 2);
        map.put("nxSupplierId", supplierId);
        System.out.println("mapappapasuppleerr" + map);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        // 1. 获取部门列表
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartmentSimple(map);
        List<GbDepartmentEntity> gbDepartmentEntityList = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        // 过滤掉 null 元素
        if (departmentEntities != null) {
            departmentEntities = departmentEntities.stream()
                    .filter(dept -> dept != null && dept.getNxDepartmentId() != null)
                    .collect(Collectors.toList());
        }
        if (gbDepartmentEntityList != null) {
            gbDepartmentEntityList = gbDepartmentEntityList.stream()
                    .filter(dept -> dept != null && dept.getGbDepartmentId() != null)
                    .collect(Collectors.toList());
        }

        // 2. 批量获取统计数据
        if (departmentEntities != null && !departmentEntities.isEmpty()) {
            List<Integer> depIds = departmentEntities.stream()
                    .map(NxDepartmentEntity::getNxDepartmentId)
                    .collect(Collectors.toList());

            // 一次性查询所有部门的统计数据
            Map<Integer, Map<String, Integer>> depStats = nxDepartmentOrdersService.batchQueryDepStats(depIds, map);

            // 设置统计数据
            for (NxDepartmentEntity dept : departmentEntities) {
                Map<String, Integer> stats = depStats.get(dept.getNxDepartmentId());
                if (stats != null) {
                    dept.setNxDepartmentAddCount(stats.get("count0"));
                    dept.setNxDepartmentPurOrderCount(stats.get("count1"));
                    dept.setNxDepartmentNeedNotPurOrderCount(stats.get("count2"));
                }
            }
        }

        // 3. 同样处理 GbDepartment
        if (gbDepartmentEntityList != null && !gbDepartmentEntityList.isEmpty()) {
            List<Integer> gbDepIds = gbDepartmentEntityList.stream()
                    .map(GbDepartmentEntity::getGbDepartmentId)
                    .collect(Collectors.toList());

            System.out.println("depididiisisississigbDepIds" + gbDepIds);

            Map<Integer, Map<String, Integer>> gbDepStats = nxDepartmentOrdersService.batchQueryGbDepStats(gbDepIds, map);

            for (GbDepartmentEntity dept : gbDepartmentEntityList) {
                Map<String, Integer> stats = gbDepStats.get(dept.getGbDepartmentId());
                if (stats != null) {
                    dept.setGbDepartmentAddCount(stats.get("count0"));
                    dept.setGbDepartmentPurOrderCount(stats.get("count1"));
                    dept.setGbDepartmentNeedNotPurOrderCount(stats.get("count2"));
                }
            }
        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDep", departmentEntities);
        mapR.put("gbDep", gbDepartmentEntityList);
        mapR.put("depOrdersWait", count);


        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/stockerGetFinishStockGoodsDeps", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetFinishStockGoodsDeps(Integer disId, Integer supplierId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("isSelfOrder", -1);
        map.put("dayuOrderStatus", -2);
        if (supplierId != null && supplierId != -1) {
            map.put("nxSupplierId", supplierId);
            map.put("purStatus", 2);
        }

        // 1. 获取部门列表
        System.out.println("whhshshhwwh" + map);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartmentSimple(map);
        List<GbDepartmentEntity> gbDepartmentEntityList = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        // 2. 批量获取统计数据
        if (!departmentEntities.isEmpty()) {
            List<Integer> depIds = departmentEntities.stream()
                    .filter(Objects::nonNull)  // 过滤掉 null 元素
                    .map(NxDepartmentEntity::getNxDepartmentId)
                    .filter(Objects::nonNull)  // 过滤掉 null ID
                    .collect(Collectors.toList());

            // 一次性查询所有部门的统计数据
            Map<Integer, Map<String, Integer>> depStats = nxDepartmentOrdersService.batchQueryDepStats(depIds, null);

            // 设置统计数据
            for (NxDepartmentEntity dept : departmentEntities) {
                if (dept == null || dept.getNxDepartmentId() == null) {
                    continue;  // 跳过 null 元素或 null ID
                }
                Map<String, Integer> stats = depStats.get(dept.getNxDepartmentId());
                if (stats != null) {
                    dept.setNxDepartmentAddCount(stats.get("count0"));
                    dept.setNxDepartmentPurOrderCount(stats.get("count1"));
                    dept.setNxDepartmentNeedNotPurOrderCount(stats.get("count2"));
                }
            }
        }
        List<GbDepartmentEntity> resultGb = new ArrayList<>();

        // 3. 同样处理 GbDepartment
        if (!gbDepartmentEntityList.isEmpty()) {
            List<Integer> gbDepIds = gbDepartmentEntityList.stream()
                    .filter(Objects::nonNull)  // 过滤掉 null 元素
                    .map(GbDepartmentEntity::getGbDepartmentId)
                    .filter(Objects::nonNull)  // 过滤掉 null ID
                    .collect(Collectors.toList());

            System.out.println("depididiisisississigbDepIds" + gbDepIds);

            Map<Integer, Map<String, Integer>> gbDepStats = nxDepartmentOrdersService.batchQueryGbDepStats(gbDepIds, null);

            for (GbDepartmentEntity dept : gbDepartmentEntityList) {
                if (dept == null || dept.getGbDepartmentId() == null) {
                    continue;  // 跳过 null 元素或 null ID
                }
                Map<String, Integer> stats = gbDepStats.get(dept.getGbDepartmentId());
                if (stats != null) {
                    dept.setGbDepartmentAddCount(stats.get("count0"));
                    dept.setGbDepartmentPurOrderCount(stats.get("count1"));
                    dept.setGbDepartmentNeedNotPurOrderCount(stats.get("count2"));
                }
            }
        }

        // 过滤掉 null 元素，确保返回的列表不包含 null
        List<NxDepartmentEntity> filteredNxDep = departmentEntities.stream()
                .filter(Objects::nonNull)
                .filter(dept -> dept.getNxDepartmentId() != null)
                .collect(Collectors.toList());

        List<GbDepartmentEntity> filteredGbDep = gbDepartmentEntityList.stream()
                .filter(Objects::nonNull)
                .filter(dept -> dept.getGbDepartmentId() != null)
                .collect(Collectors.toList());

//协作订单
        Map<String, Object> mapOffer = new HashMap<>();
        mapOffer.put("disId", disId);
        mapOffer.put("isSelfOrder", 1);
        mapOffer.put("status", 3);
        System.out.println("enennenenennemapOffer" + mapOffer);
        List<NxDistributerEntity> offerNxDistributer = nxDepartmentOrdersService.queryOfferOrderNxDistributer(mapOffer);
        if(offerNxDistributer.size() > 0){
            for(NxDistributerEntity distributerEntity: offerNxDistributer){
                Map<String, Object> mapColl = new HashMap<>();
                mapColl.put("disId", disId);
                mapColl.put("isSelfOrder", 1);
                mapColl.put("status", 3);
                mapColl.put("collNxDisId", distributerEntity.getNxDistributerId());

                int orderCount = nxDepartmentOrdersService.queryOrderGoodsCount(mapColl);
                Map<String, Object> mapPick = new HashMap<>(mapColl);
                mapPick.put("equalPurStatus", 4);
                int pickCount = nxDepartmentOrdersService.queryOrderGoodsCount(mapPick);

                int totalCount = nxDepartmentOrdersService.queryDepOrdersAcount(mapColl);
                mapColl.put("hasPrice", 1);
                int hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(mapColl);
                mapColl.put("hasPrice", null);
                mapColl.put("hasWeight", 1);
                int hasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(mapColl);
                mapColl.put("hasWeight", null);
                Double subtotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapColl);
                String twoSubtotal = subtotal != null && subtotal > 0
                        ? new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString()
                        : "0";

                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("orderCount", orderCount);
                mapItem.put("pickCount", pickCount);
                mapItem.put("totalCount", totalCount);
                mapItem.put("twoSubtotal", twoSubtotal);
                mapItem.put("hasPrice", hasPrice);
                mapItem.put("hasWeight", hasWeight);
                distributerEntity.setAaa(mapItem);
            }
        }


        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDep", filteredNxDep);
        mapR.put("gbDep", filteredGbDep);
        mapR.put("offerArr", offerNxDistributer);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/disGetWaitStockGoodsDeps", method = RequestMethod.POST)
    @ResponseBody
    public R disGetWaitStockGoodsDeps(Integer disId, String goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("goodsType", goodsType);
        System.out.println("depeppepe" + map);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(map);
        List<GbDepartmentEntity> gbDepartmentEntityList = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("status", 3);
                mapDep.put("goodsType", goodsType);
                mapDep.put("depFatherId", departmentEntity.getNxDepartmentId());
                Integer count0 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                mapDep.put("purStatus", 4);
                Integer count1 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                mapDep.put("dayuPurStatus", 3);
                Integer count2 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                departmentEntity.setNxDepartmentAddCount(count0);
                departmentEntity.setNxDepartmentPurOrderCount(count1);
                departmentEntity.setNxDepartmentNeedNotPurOrderCount(count2);
            }
        }

        if (gbDepartmentEntityList.size() > 0) {
            for (GbDepartmentEntity gbDepartmentEntity : gbDepartmentEntityList) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("status", 3);
                mapDep.put("goodsType", goodsType);
                mapDep.put("gbDepFatherId", gbDepartmentEntity.getGbDepartmentId());
                Integer count0 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                mapDep.put("purStatus", 4);
                System.out.println("nenennen" + mapDep);
                Integer count1 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                mapDep.put("dayuPurStatus", 3);
                Integer count2 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                gbDepartmentEntity.setGbDepartmentAddCount(count0);
                gbDepartmentEntity.setGbDepartmentPurOrderCount(count1);
                gbDepartmentEntity.setGbDepartmentNeedNotPurOrderCount(count2);
            }
        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDep", departmentEntities);
        mapR.put("gbDep", gbDepartmentEntityList);
        System.out.println("返回data" + mapR);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/disGetWaitOutGoodsDeps", method = RequestMethod.POST)
    @ResponseBody
    public R disGetWaitOutGoodsDeps(Integer disId, String type) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purType", 1);
        if (type.equals("wait")) {
            map.put("dayuPurStatus", 1);
            map.put("purStatus", 4);
        }
        if (type.equals("finish")) {
            map.put("dayuPurStatus", 3);
        }
        System.out.println("akankana" + map);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(map);
        List<GbDepartmentEntity> gbDepartmentEntity = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);

        if (type.equals("finish")) {
            if (departmentEntities.size() > 0) {
                for (NxDepartmentEntity departmentEntity : departmentEntities) {
                    Map<String, Object> mapDep = new HashMap<>();
                    mapDep.put("status", 3);
                    mapDep.put("purType", 1);
                    mapDep.put("depFatherId", departmentEntity.getNxDepartmentId());
                    Integer count0 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                    mapDep.put("equalPurStatus", 3);
                    Integer count1 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                    mapDep.put("dayuPurStatus", 3);
                    Integer count2 = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);

                    departmentEntity.setNxDepartmentAddCount(count0);
                    departmentEntity.setNxDepartmentPurOrderCount(count1);
                    departmentEntity.setNxDepartmentNeedNotPurOrderCount(count2);

                }
            }
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDep", departmentEntities);
        mapR.put("gbDep", gbDepartmentEntity);
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/getNotWeightOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getNotWeightOrders(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depFatherId", depFatherId);
        map1.put("gbDepFatherId", gbDepFatherId);
        map1.put("resFatherId", resFatherId);
        map1.put("purGoodsId", -1);
        map1.put("status", 3);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map1);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("depFatherId", depFatherId);
        mapR.put("gbDepFatherId", gbDepFatherId);
        mapR.put("resFatherId", resFatherId);
        mapR.put("equalStatus", 0);
        mapR.put("weightId", 1);
        mapR.put("purGoodsId", -1);
        mapR.put("weightType", 1);
        System.out.println("laisiisisisiisis" + mapR);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(mapR);

        Map<String, Object> map = new HashMap<>();
        map.put("equalStatus", 0);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        map.put("purGoodsId", -1);
        map.put("weightId", 0);
        Integer count1 = nxDepartmentOrdersService.queryDepOrdersAcount(map);


        Map<String, Object> map11 = new HashMap<>();
        map11.put("arr", ordersEntities);
        map11.put("weightCount", count);
        map11.put("toPrintCount", count1);

        return R.ok().put("data", map11);
    }


    @RequestMapping(value = "/getBillReturnApplys/{billId}")
    @ResponseBody
    public R getBillReturnApplys(@PathVariable Integer billId) {
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryReturnOrdersByBillId(billId);
        return R.ok().put("data", ordersEntities);
    }

    /**
     * 根据tradeNo查询账单信息和订单列表（包含溯源报告信息）
     * 如果是货架商品，从库存批次关联溯源报告；如果不是货架商品，从采购商品关联溯源报告
     *
     * @param tradeNo 账单交易号（nx_DB_trade_no）
     * @return 账单信息和订单列表，每个订单包含下单用户、下单时间、溯源报告等信息
     */
    @RequestMapping(value = "/getOrdersByTradeNoWithTraceReport/{tradeNo}")
    @ResponseBody
    public R getOrdersByTradeNoWithTraceReport(@PathVariable String tradeNo, HttpServletRequest request) {
        System.out.println("[getOrdersByTradeNoWithTraceReport] 开始查询账单和订单溯源报告，tradeNo: " + tradeNo);

        if (tradeNo == null || tradeNo.trim().isEmpty()) {
            return R.error("tradeNo 不能为空");
        }

        // 通过 tradeNo 查询账单信息
        NxDepartmentBillEntity billEntity = nxDepartmentBillService.queryDepartBillByJustTradeNo(tradeNo);
        if (billEntity == null) {
            return R.error("账单不存在，tradeNo: " + tradeNo);
        }

        Integer departmentBillId = billEntity.getNxDepartmentBillId();
        if (departmentBillId == null) {
            return R.error("账单ID为空，tradeNo: " + tradeNo);
        }

        // 查询历史订单列表（包含溯源报告信息）
        Map<String, Object> map = new HashMap<>();
        map.put("billId", departmentBillId);

        List<NxDepartmentOrderHistoryEntity> ordersEntities = historyService.queryOrdersByBillIdWithTraceReport(map);

        System.out.println("[getOrdersByTradeNoWithTraceReport] 查询完成，tradeNo: " + tradeNo +
                ", 账单ID: " + departmentBillId + ", 订单数量: " + ordersEntities.size());

        // 构建基础URL（用于文件下载）
        String baseUrl = buildBaseUrl(request);

        // 处理溯源报告的下载地址
        for (NxDepartmentOrderHistoryEntity order : ordersEntities) {
            NxTraceReportEntity traceReport = order.getNxTraceReportEntity();
            if (traceReport != null) {
                // 处理单个文件路径
                String filePath = traceReport.getNxTrFilePath();
                if (filePath != null && !filePath.trim().isEmpty()) {
                    String downloadUrl = buildDownloadUrl(baseUrl, filePath);
                    traceReport.setNxTrFilePath(downloadUrl); // 将相对路径替换为完整URL
                }

                // 处理多个文件路径（JSON格式）
                String filePaths = traceReport.getNxTrFilePaths();
                if (filePaths != null && !filePaths.trim().isEmpty()) {
                    try {
                        // 解析JSON数组（fastjson 1.2.x 版本使用 parseArray）
                        JSONArray filePathsArray = JSONArray.parseArray(filePaths);
                        JSONArray downloadUrlsArray = new JSONArray();
                        for (int i = 0; i < filePathsArray.size(); i++) {
                            String path = filePathsArray.getString(i);
                            if (path != null && !path.trim().isEmpty()) {
                                String downloadUrl = buildDownloadUrl(baseUrl, path);
                                downloadUrlsArray.add(downloadUrl);
                            }
                        }
                        traceReport.setNxTrFilePaths(downloadUrlsArray.toJSONString());
                    } catch (Exception e) {
                        logger.warn("[getOrdersByTradeNoWithTraceReport] 解析文件路径JSON失败: {}, 错误: {}", filePaths, e.getMessage());
                        // 如果解析失败，保持原值
                    }
                }
            }


        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("bill", billEntity);
        result.put("orders", ordersEntities);

        return R.ok().put("data", result);
    }

    /**
     * 构建基础URL（协议 + 域名 + 端口 + 上下文路径）
     */
    private String buildBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme(); // http 或 https
        String serverName = request.getServerName(); // 服务器名称或IP
        int serverPort = request.getServerPort(); // 端口号
        String contextPath = request.getContextPath(); // 上下文路径，如 /nongxinle_master_war_exploded

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);

        // 如果是标准端口（80或443），不需要添加端口号
        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }

        baseUrl.append(contextPath);

        return baseUrl.toString();
    }

    /**
     * 构建文件下载URL
     *
     * @param baseUrl  基础URL（如 http://192.168.0.101:8080/nongxinle_master_war_exploded）
     * @param filePath 文件相对路径（如 traceReports/xxx.pdf）
     * @return 完整的下载URL
     */
    private String buildDownloadUrl(String baseUrl, String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }

        // 如果已经是完整URL，直接返回
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            return filePath;
        }

        // 确保路径以 / 开头
        String normalizedPath = filePath.startsWith("/") ? filePath : "/" + filePath;

        // 构建完整URL
        return baseUrl + normalizedPath;
    }

    /**
     * 批发商给客户添加申请
     *
     * @param depFatherId 客户id
     * @return 订单
     */
    @RequestMapping(value = "/disGetDepTodayOrders/{depFatherId}")
    @ResponseBody
    public R disGetDepTodayOrders(@PathVariable Integer depFatherId) {
        Map<String, Object> mapR = new HashMap<>();
        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
        if (departmentEntities.size() > 0) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                Map<String, Object> map = new HashMap<>();
                map.put("depId", departmentEntity.getNxDepartmentId());
                List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = nxDepartmentOrdersHistoryService.queryDepTodayOrder(map);
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depName", departmentEntity.getNxDepartmentName());
                mapDep.put("depId", departmentEntity.getNxDepartmentId());
                mapDep.put("list", nxDistributerGoodsEntities);
                list.add(mapDep);
            }
            mapR.put("arr", list);
            mapR.put("subDep", departmentEntities.size());

        } else {
            //今天的数据
            Map<String, Object> map = new HashMap<>();
            map.put("depId", depFatherId);
            List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = nxDepartmentOrdersHistoryService.queryDepTodayOrder(map);
            mapR.put("arr", nxDistributerGoodsEntities);
            mapR.put("subDep", 0);
        }


        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/disGetToPlanPurchaseGoodsSearch", method = RequestMethod.POST)
    @ResponseBody
    public R disGetToPlanPurchaseGoodsSearch(Integer disId, String searchStr) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        String pinyinString = searchStr;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchPinyin", pinyinString);
        map.put("searchStr", searchStr);
        System.out.println("idiiddiisisisidididididiids" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetUnPlanPurchaseApplysSearch(map);

        return R.ok().put("data", fatherGoodsEntities);
    }


    /**
     * 批发商获取未进货的订单
     *
     * @param disId 批发商id
     * @return 批发商父类商品
     */
    @RequestMapping(value = "/disGetToPlanPurchaseGoods/{disId}")
    @ResponseBody
    public R disGetToPlanPurchaseGoods(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("equalPurStatus", 0);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetUnPlanPurchaseApplysNew(map);

        if (fatherGoodsEntities.size() > 0) {
            for (NxDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                Map<String, Object> mapF = new HashMap<>();
                mapF.put("grandId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
                mapF.put("status", 3);
                mapF.put("equalPurStatus", 0);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(mapF);
                fatherGoodsEntity.setNewOrderCount(integer);
            }
        }

        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("purStatus", 3);
        map1.put("purType", -1);
        //新订单
        int newCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        // 出库
        map1.put("purStatus", 5);
        map1.put("purType", 0);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);


        // 订货
        map1.put("purType", 1);
        map1.put("inputType", 1);
        System.out.println("wxxxxxxx" + map1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        // 打印
        map1.put("inputType", 0);
        int printCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("purType", 0);
        mapOk.put("status", 3);
        mapOk.put("weight", 1);
        //出库完成
        System.out.println("mapoikkstocooutokookpk" + mapOk);
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        //订货完成
        mapOk.put("purType", 1);
        mapOk.put("inputType", 1);
        mapOk.put("weight", 1);
        mapOk.put("batchId", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        //打印
        mapOk.put("inputType", 0);
        mapOk.put("batchId", -1);
        mapOk.put("weightStatusEqual", 1);
        int printCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
//        //////************************************
        // map4 未发送或未打印
        Map<String, Object> map4 = new HashMap<>();

        map4.put("disId", disId);
        map4.put("status", 1);
        map4.put("weightId", -1);
        map4.put("batchId", -1);
        map4.put("equalInputType", 0);
        System.out.println("map444undododo" + map4);

        map1.put("purType", 1);
        map1.put("inputType", 1);
        map1.put("equalStatus", 0);
        Integer unDoCount = nxDepartmentOrdersService.queryDepOrdersAcount(map1);


        // map4 订货已发送
        map4.put("status", 2); //NX_DIS_PURCHASE_GOODS_IS_PURCHASE == 2 huifu
        map4.put("orderStatus", 3);
        map4.put("batchId", 1);
        map4.put("dayuPurStatus", 1);
        map4.put("purStatus", 3);
        Integer wxIsBatchCountUnReply = nxDistributerPurchaseGoodsService.queryPurOrderCount(map4);
        map4.put("status", 4);
        map4.put("dayuStatus", 1);
        Integer wxIsBatchCountHaveReply = nxDistributerPurchaseGoodsService.queryPurOrderCount(map4);


        //  map4 已打印
        map4.put("batchId", -1);
        map4.put("weightId", 1);
        map4.put("weightStatusEqual", 0);
        map4.put("orderStatus", 3);
        map4.put("status", 4);
        Integer isPrintCount = nxDistributerPurchaseGoodsService.queryPurOrderCount(map4);
        System.out.println("isprint444444" + map4);
        map4.put("weightStatusEqual", 1);
        Integer isPrintHaveWeightCount = nxDistributerPurchaseGoodsService.queryPurOrderCount(map4);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("newCount", newCount);
        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCount);
        mapR.put("printCount", printCount);
        mapR.put("wxCountOk", wxCountOk);
        mapR.put("printCountOk", printCountOk);

        mapR.put("unDoCount", unDoCount);
        mapR.put("isBatchCountUnRepaly", wxIsBatchCountUnReply);
        mapR.put("isBatchCountHaveRepaly", wxIsBatchCountHaveReply);
        mapR.put("isPrintCount", isPrintCount);
        mapR.put("havePrintCount", isPrintHaveWeightCount);

        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/disGetStockGoodsToPrint", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoodsToPrint(Integer disId, Integer goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("goodsType", goodsType);
        map.put("weightId", 0);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.queryDisGetPrintOrderGreatGrandGoods(map);
        return R.ok().put("data", fatherGoodsEntities);
    }

    @RequestMapping(value = "/disGetStockDepartmentToPrint", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockDepartmentToPrint(Integer disId, Integer goodsType) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("goodsType", goodsType);
        map.put("weightId", 0);
//        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryDistributerFatherGoodsTodayDepartments(map);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryDistributerTodayDepartments(map);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryOrderGbDepartmentList(map);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("nxDepArr", departmentEntities);
        mapR.put("gbDepArr", gbDepartmentEntities);
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/disGetStockGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoods(Integer disId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purStatus", 4);
        map.put("goodsType", goodsType);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("grandArr", greatGrandGoods);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", disId);
        mapFin.put("status", 3);
        mapFin.put("goodsType", goodsType);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("purStatus", 4);
        Integer preOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        map3.put("status", 3);
        Integer buyOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Map<String, Object> map3Ok = new HashMap<>();
        map3Ok.put("disId", disId);
        map3Ok.put("equalStatus", 2);
        Integer buyOrdersOk = nxDepartmentOrdersService.queryDepOrdersAcount(map3Ok);
        mapR.put("buyOrders", buyOrders);
        mapR.put("buyOrdersOk", buyOrdersOk);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        System.out.println("mapokk" + mapOk);
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        Map<String, Object> mapB = new HashMap<>();
        mapB.put("disId", disId);
        mapB.put("settleType", 0);
        mapB.put("equalStatus", 0);
        int unPayCount = nxDepartmentBillService.queryBillsCount(mapB);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCount);
        mapR.put("wxCountOk", wxCountOk);
        mapR.put("unPayCount", unPayCount);
        mapR.put("preOrders", preOrders);
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/stockerGetStockGoodsUnWeigth", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetStockGoodsUnWeigth(Integer disId) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);

        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(map);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(map);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        System.out.println("righthtmap" + map);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        mapR.put("depOrdersWait", count);
        map.put("equalPurStatus", 0);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("grandArr", greatGrandGoods);

        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map);
        map.put("purStatus", null);
        map.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map);
        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/stockerGetStockGoods", method = RequestMethod.POST)
    @ResponseBody
    public R stockerGetStockGoods(Integer disId) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        mapR.put("depOrdersWait", count);
        map.put("purStatus", 4);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        mapR.put("grandArr", greatGrandGoods);

        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map);
        map.put("purStatus", null);
        map.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map);
        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/disGetStockGoodsKf", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoodsKf(Integer disId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purStatus", 4);
        map.put("goodsType", goodsType);
        map.put("shelfId", 1);
        System.out.println("shelflfllfflfff" + map);

        List<NxDistributerGoodsShelfEntity> shelfEntities = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        System.out.println("shellld");
        map.put("shelfId", 0);
        System.out.println("shelflfllfflfff" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities2 = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        List<NxDistributerGoodsShelfGoodsEntity> emptyList = new ArrayList<>();
        NxDistributerGoodsShelfEntity emptyShelf = new NxDistributerGoodsShelfEntity();
        if (shelfEntities2.size() > 0) {
            for (NxDistributerGoodsShelfEntity emptyEnity : shelfEntities2) {
                emptyList.addAll(emptyEnity.getNxDisGoodsShelfGoodsEntities());
            }
            emptyShelf.setNxDisGoodsShelfGoodsEntities(emptyList);
            shelfEntities.add(emptyShelf);
        }


        System.out.println("shelrlrlrlr" + shelfEntities.size());
        mapR.put("shelfArr", shelfEntities);


        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", disId);
        mapFin.put("status", 3);
        mapFin.put("goodsType", goodsType);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("purStatus", 4);
        Integer preOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        map3.put("status", 3);
        Integer buyOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Map<String, Object> map3Ok = new HashMap<>();
        map3Ok.put("disId", disId);
        map3Ok.put("equalStatus", 2);
        Integer buyOrdersOk = nxDepartmentOrdersService.queryDepOrdersAcount(map3Ok);
        mapR.put("buyOrders", buyOrders);
        mapR.put("buyOrdersOk", buyOrdersOk);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 2);
        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        int wxCountPur = wxCount + wxCountAuto;

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        System.out.println("mapokk" + mapOk);
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        int wxCountOkAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        int wxCountPurOk = wxCountOk + wxCountOkAuto;


        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCountPur);
        mapR.put("wxCountOk", wxCountPurOk);
        mapR.put("preOrders", preOrders);
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/pickerGetStockGoodsKf", method = RequestMethod.POST)
    @ResponseBody
    public R pickerGetStockGoodsKf(Integer disId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purStatus", 4);
        map.put("goodsType", goodsType);
        map.put("shelfId", 1);
        System.out.println("shelflfllfflfffpickerGetStockGoodsKf" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        map.put("shelfId", 0);
        System.out.println("shelflfllfflfffpickerGetStockGoodsKf" + map);
        List<NxDistributerGoodsShelfEntity> shelfEntities2 = nxDepartmentOrdersService.queryShelfGoodsOrder(map);
        List<NxDistributerGoodsShelfGoodsEntity> emptyList = new ArrayList<>();
        NxDistributerGoodsShelfEntity emptyShelf = new NxDistributerGoodsShelfEntity();
        if (shelfEntities2.size() > 0) {
            for (NxDistributerGoodsShelfEntity emptyEnity : shelfEntities2) {
                emptyList.addAll(emptyEnity.getNxDisGoodsShelfGoodsEntities());
            }
            emptyShelf.setNxDisGoodsShelfGoodsEntities(emptyList);
            shelfEntities.add(emptyShelf);
        }


        System.out.println("dataArr=====" + shelfEntities);
        for (NxDistributerGoodsShelfEntity shelfEntity : shelfEntities) {
            List<NxDistributerGoodsShelfGoodsEntity> nxDisGoodsShelfGoodsEntities = shelfEntity.getNxDisGoodsShelfGoodsEntities();
            for (NxDistributerGoodsShelfGoodsEntity shelfGoodss : nxDisGoodsShelfGoodsEntities) {

            }
        }

        return R.ok().put("data", shelfEntities);

    }


    @RequestMapping("/disGetStockGoodsKfPage")
    @ResponseBody
    public R disGetStockGoodsKfPage(Integer disId, Integer page, Integer limit) {
        if (page == null || page < 1) page = 1;
        if (limit == null || limit < 1) limit = 15;

        // 1. 计算 offset
        int offset = (page - 1) * limit;

        // 2. 先做 count 查询——获取该分销商、该状态下的商品总数
        Map<String, Object> countParams = new HashMap<>();
        countParams.put("disId", disId);
//        countParams.put("status",   3);
//        countParams.put("purStatus",4);
        System.out.println("countntntntttaaaaa" + countParams);

        int total = shelfGoodsService.queryShelfGoodsCount(countParams);


        // 3. 再做带 offset/limit 的分页查询，直接返回当前页数据
        Map<String, Object> pageParams = new HashMap<>();
        pageParams.put("disId", disId);
//        pageParams.put("status",   3);
//        pageParams.put("purStatus",4);
        pageParams.put("offset", offset);
        pageParams.put("limit", limit);
        System.out.println("pagemmaididididi" + pageParams);
        List<NxDistributerGoodsShelfGoodsEntity> pageList =
                shelfGoodsService.queryShelfForGoodsWithOrders(pageParams);
        System.out.println("printpageListsize" + pageList.size());

        // 4. 构造 PageUtils 结果
        PageUtils pageUtil = new PageUtils(pageList, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping("/stockerGetStockGoodsKfPage")
    @ResponseBody
    public R stockerGetStockGoodsKfPage(Integer disId, Integer page, Integer limit) {
        if (page == null || page < 1) page = 1;
        if (limit == null || limit < 1) limit = 15;

        // 1. 计算 offset
        int offset = (page - 1) * limit;

        // 2. 先做 count 查询——获取该分销商、该状态下的商品总数
        Map<String, Object> countParams = new HashMap<>();
        countParams.put("disId", disId);
        countParams.put("status", 3);
        countParams.put("purStatus", 4);
        int total = shelfGoodsService.queryShelfGoodsCount(countParams);

        // 3. 再做带 offset/limit 的分页查询，直接返回当前页数据
        Map<String, Object> pageParams = new HashMap<>();
        pageParams.put("disId", disId);
        pageParams.put("status", 3);
        pageParams.put("purStatus", 4);
        pageParams.put("offset", offset);
        pageParams.put("limit", limit);
        System.out.println("pagemmaididididi" + pageParams);
        List<NxDistributerGoodsShelfGoodsEntity> pageList =
                shelfGoodsService.queryShelfForGoodsWithOrders(pageParams);
        // 4. 构造 PageUtils 结果
        PageUtils pageUtil = new PageUtils(pageList, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/stockerGetShelfList/{disId}")
    @ResponseBody
    public R stockerGetShelfList(@PathVariable Integer disId) {
        Map<String, Object> params = new HashMap<>();
        params.put("disId", disId);
        params.put("purStatus", 4);
        System.out.println("mapososososaaaa" + params);
        List<NxDistributerGoodsShelfEntity> allShelves = nxDistributerGoodsShelfService.queryStockShelf(params);

        return R.ok().put("data", allShelves);
    }


    @RequestMapping(value = "/disGetStockGoodsWeb", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoodsWeb(Integer disId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("purStatus", 4);
        map.put("goodsType", goodsType);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);

        if (greatGrandGoods.size() > 0) {

            for (NxDistributerFatherGoodsEntity greatGrand : greatGrandGoods) {

                List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = greatGrand.getNxDistributerGoodsEntities();
                if (nxDistributerGoodsEntities.size() > 0) {
                    for (NxDistributerGoodsEntity distributerGoodsEntity : nxDistributerGoodsEntities) {
                        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = distributerGoodsEntity.getNxDepartmentOrdersEntities();
                        if (nxDepartmentOrdersEntities.size() > 0) {
                            String content = "";
                            for (NxDepartmentOrdersEntity ordersEntity : nxDepartmentOrdersEntities) {
                                String depName = ordersEntity.getNxDepartmentEntity().getNxDepartmentName();
                                String order = ordersEntity.getNxDoQuantity() + ordersEntity.getNxDoStandard();
                                content = content + depName + " " + order + ", ";
                                System.out.println("contnennen" + content);
                                distributerGoodsEntity.setOrderContent(content);
                                distributerGoodsEntity.setOrderSize(content.length());
                            }
                        }
                    }
                }
            }
        }

        return R.ok().put("data", greatGrandGoods);
    }


    @RequestMapping(value = "/disGetStockGoodsNew", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoodsNew(Integer disId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
//        map.put("equalPurStatus", 1);
//        map.put("goodsType", goodsType);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
        List<NxDistributerFatherGoodsEntity> distributerGoodsEntities = new ArrayList<>();
        if (greatGrandGoods.size() > 0) {
            NxDistributerFatherGoodsEntity greatGrandsEntity = greatGrandGoods.get(0);
            Integer greatGarndGoodsId = greatGrandsEntity.getNxDistributerFatherGoodsId();
            map.put("fathersFatherId", greatGarndGoodsId);
            distributerGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);

            mapR.put("cataArr", greatGrandGoods);
            mapR.put("grandArr", distributerGoodsEntities);

        } else {
            mapR.put("cataArr", new ArrayList<>());
            mapR.put("grandArr", new ArrayList<>());
        }

        Map<String, Object> mapW = new HashMap<>();
        mapW.put("disId", disId);
        mapW.put("weightType", 3); //出库单 == 3
        mapW.put("status", 2);
        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(mapW);

        mapW.put("hasWeight", 1);
        Integer count1 = nxDepartmentOrdersService.queryDepOrdersAcount(mapW);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", disId);
        mapFin.put("status", 3);
        mapFin.put("dayuPurStatus", 3);
        mapFin.put("purType", 0);
        List<NxDepartmentEntity> departmentEntitiesFinish = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntitiesFinish = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);

        Map<String, Object> mapD = new HashMap<>();
        mapD.put("disId", disId);
        mapD.put("equalPurStatus", 1);
        mapD.put("purType", 0);
        mapD.put("weightId", 0);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapD);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapD);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);
        mapR.put("finishDepNx", departmentEntitiesFinish);
        mapR.put("finishDepGb", gbDepartmentEntitiesFinish);

        mapR.put("totalWeightCount", count);
        mapR.put("haveFinishCount", count1);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("purStatus", 5);
        map111.put("purType", 0);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("purType", 1);
        map111.put("dayuPurStatus", 1);
        int purCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("purType", 0);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("purType", 1);
        int purCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("purCount", purCount);
        mapR.put("purCountOk", purCountOk);

        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/disGetStockGoodsNewWithDepIds", method = RequestMethod.POST)
    @ResponseBody
    public R disGetStockGoodsNewWithDepIds(String nxDepIds, String gbDepIds, Integer nxDisId, Integer goodsType) {
        Map<String, Object> mapR = new HashMap<>();
        Map<String, Object> map = new HashMap<>();

        if (!gbDepIds.equals("0")) {
            List<String> idsGb = new ArrayList<>();
            String[] arrGb = gbDepIds.split(",");
            for (String idGb : arrGb) {
                idsGb.add(idGb);
                if (idsGb.size() > 0) {
                    map.put("gbDepIds", idsGb);
                } else {
                    map.put("gbDepIds", null);
                }
            }
        }
        if (!nxDepIds.equals("0")) {
            List<String> idsNx = new ArrayList<>();
            String[] arrNx = nxDepIds.split(",");
            for (String id : arrNx) {
                idsNx.add(id);
                if (idsNx.size() > 0) {
                    map.put("nxDepIds", idsNx);
                } else {
                    map.put("nxDepIds", null);
                }
            }
        }

        map.put("equalPurStatus", 1);
        map.put("goodsType", goodsType);
//        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        List<NxDistributerFatherGoodsEntity> greatGrandGoods = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoods(map);
//        mapR.put("grandArr", greatGrandGoods);
        List<NxDistributerFatherGoodsEntity> distributerGoodsEntities = new ArrayList<>();
        if (greatGrandGoods.size() > 0) {
            NxDistributerFatherGoodsEntity greatGrandsEntity = greatGrandGoods.get(0);
            Integer greatGarndGoodsId = greatGrandsEntity.getNxDistributerFatherGoodsId();
            map.put("fathersFatherId", greatGarndGoodsId);
            distributerGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);

            mapR.put("cataArr", greatGrandGoods);
            mapR.put("grandArr", distributerGoodsEntities);

        } else {
            mapR.put("cataArr", new ArrayList<>());
            mapR.put("grandArr", new ArrayList<>());
        }


        Integer count = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        mapR.put("depOrdersWait", count);

        Map<String, Object> mapFin = new HashMap<>();
        mapFin.put("disId", nxDisId);
        mapFin.put("status", 3);
        mapFin.put("goodsType", goodsType);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartment(mapFin);
        List<GbDepartmentEntity> gbDepartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapFin);
        mapR.put("waitDepNx", departmentEntities);
        mapR.put("waitDepGb", gbDepartmentEntities);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", nxDisId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", nxDisId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 0);
        int zicaiCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCount);
        mapR.put("wxCountOk", wxCountOk);
        mapR.put("zicaiCount", zicaiCount);
        mapR.put("zicaiCountOk", zicaiCountOk);
        return R.ok().put("data", mapR);


    }


    @RequestMapping(value = "/depGetDepDisGoodsCata")
    @ResponseBody
    public R depGetDepDisGoodsCata(Integer depId, Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("disId", disId);
        map.put("notLinshi", 1);
        System.out.println("cattaktktktkktk");
        List<NxDistributerFatherGoodsEntity> disGoodsEntities = nxDepartmentDisGoodsService.depGetDepDisGoodsCata(map);// 1. 获取总数

        List<Integer> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryOnlyDepGoodsIds(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr", disGoodsEntities);
        mapR.put("depGoodsArr", departmentDisGoodsEntities);

        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/disGetTypePrepareOrderPage")
    @ResponseBody
    public R disGetTypePrepareOrderPage(Integer limit, Integer page, Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("purType", 0);

        // 1. 获取总数
        int total = nxDepartmentDisGoodsService.queryDepGoodsCount(map);

        // 2. 获取当前页数据
        map.put("purStatus", 5);
        map.put("limit", limit);
        map.put("offset", (page - 1) * limit);
        System.out.println("");
        List<NxDistributerFatherGoodsEntity> currentPageList = nxDepartmentOrdersService.queryGrandGoodsOrder(map);

        // 5. 返回分页数据
        PageUtils pageUtil = new PageUtils(currentPageList, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/disGetTypePrepareOrderCata/{disId}")
    @ResponseBody
    public R disGetTypePrepareOrderCata(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("purType", 0);

        System.out.println("cattaktktktkktk");
        List<NxDistributerFatherGoodsEntity> disGoodsEntities = nxDepartmentOrdersService.queryGrandGoodsOrder(map);
        // 1. 获取总数

        List<Integer> disGoodsIds = nxDepartmentOrdersService.queryOnlyNxGoodsIds(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("cataArr", disGoodsEntities);
        mapR.put("depGoodsArr", disGoodsIds);


        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("status", 3);
        map3.put("purStatus", 4);
        Integer preOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Integer buyOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Map<String, Object> map3Ok = new HashMap<>();
        map3Ok.put("disId", disId);
        map3Ok.put("equalStatus", 2);
        Integer buyOrdersOk = nxDepartmentOrdersService.queryDepOrdersAcount(map3Ok);
        mapR.put("buyOrders", buyOrders);
        mapR.put("buyOrdersOk", buyOrdersOk);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 2);
        map111.put("purStatus", 4);

        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        int wxCountPur = wxCount + wxCountAuto;

        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        System.out.println("mapokk" + mapOk);
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        //订货完成
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        System.out.println("wxcoucncncncnccn" + mapOk);
        int wxCountOkAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        int wxCountPurOk = wxCountOk + wxCountOkAuto;
        Map<String, Object> mapB = new HashMap<>();
        mapB.put("disId", disId);
        mapB.put("settleType", 0);
        mapB.put("equalStatus", 0);
        int unPayCount = nxDepartmentBillService.queryBillsCount(mapB);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCountPur);
        mapR.put("wxCountOk", wxCountPurOk);
        mapR.put("unPayCount", unPayCount);
        mapR.put("preOrders", preOrders);

        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/disGetTypePrepareOrder/{disId}")
    @ResponseBody
    public R disGetTypePrepareOrder(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("purType", 0);
        System.out.println("aaoaoaooaoaaooa" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);
        //countOrderStatus
        fatherGoodsEntities = everyStockFatherGoodsOrderStatus(fatherGoodsEntities, disId, map);


        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("status", 3);
        map3.put("purStatus", 4);
        Integer preOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Integer buyOrders = nxDepartmentOrdersService.queryDepOrdersAcount(map3);
        Map<String, Object> map3Ok = new HashMap<>();
        map3Ok.put("disId", disId);
        map3Ok.put("equalStatus", 2);
        Integer buyOrdersOk = nxDepartmentOrdersService.queryDepOrdersAcount(map3Ok);
        mapR.put("buyOrders", buyOrders);
        mapR.put("buyOrdersOk", buyOrdersOk);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 2);
        map111.put("purStatus", 4);

        int wxCountAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        int wxCountPur = wxCount + wxCountAuto;

        //ok
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("dayuPurStatus", 3);
        //出库完成
        System.out.println("mapokk" + mapOk);
        int stockCountOK = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        //订货完成
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        System.out.println("wxcoucncncncnccn" + mapOk);
        int wxCountOkAuto = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(mapOk);
        int wxCountPurOk = wxCountOk + wxCountOkAuto;
        Map<String, Object> mapB = new HashMap<>();
        mapB.put("disId", disId);
        mapB.put("settleType", 0);
        mapB.put("equalStatus", 0);
        int unPayCount = nxDepartmentBillService.queryBillsCount(mapB);

        mapR.put("stockCount", stockCount);
        mapR.put("stockCountOk", stockCountOK);
        mapR.put("wxCount", wxCountPur);
        mapR.put("wxCountOk", wxCountPurOk);
        mapR.put("unPayCount", unPayCount);
        mapR.put("preOrders", preOrders);

        mapR.put("arr", fatherGoodsEntities);
        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/disGetTypePrepareOutPage", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTypePrepareOutPage(Integer disId,
                                      Integer page,
                                      Integer limit) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("dayuOrderStatus", -2);
        map.put("purStatus", 4);
        map.put("purType", 0);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        logger.info("[disGetTypePrepareOutPage] 查询条件 map: {}", map);
        logger.info("[disGetTypePrepareOutPage] 使用超简化版查询，只返回前端需要的字段");

        // 使用超简化版查询，只返回前端需要的字段，大幅减少数据传输量
        List<com.nongxinle.entity.OutGoodsSimpleDTO> outGoodsSimpleList = nxDepartmentOrdersService.queryOutGoodsWithOrdersUltraSimple(map);
        logger.info("[disGetTypePrepareOutPage] 查询结果数量: {}", outGoodsSimpleList != null ? outGoodsSimpleList.size() : 0);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("disId", disId);
        mapCount.put("status", 3);
        mapCount.put("purStatus", 4);
        mapCount.put("purType", 0);
        mapCount.put("type", -1);
        mapCount.put("dayuOrderStatus", -2);
        Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(mapCount);

        PageUtils pageUtil = new PageUtils(outGoodsSimpleList, integer, limit, page);

        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/disGetCollPrepareOutPage", method = RequestMethod.POST)
    @ResponseBody
    public R disGetCollPrepareOutPage(
                                      Integer disId,
                                      Integer page,
                                      Integer limit) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderDisId", disId);
        map.put("purStatus", 1);
        map.put("status", 3);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("hasCollOrder", 1);
        logger.info("[disGetCollPrepareOutPage] 查询条件 map: {}", map);
        logger.info("[disGetCollPrepareOutPage] 使用超简化版查询，只返回前端需要的字段");

        // 使用超简化版查询，只返回前端需要的字段，大幅减少数据传输量
        List<com.nongxinle.entity.OutGoodsSimpleDTO> outGoodsSimpleList = nxDepartmentOrdersService.queryOutGoodsWithOrdersUltraSimple(map);
        logger.info("[disGetTypePrepareOutPage] 查询结果数量: {}", outGoodsSimpleList != null ? outGoodsSimpleList.size() : 0);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("orderDisId", disId);
        mapCount.put("status", 3);
        mapCount.put("purStatus", 1);
        mapCount.put("hasCollOrder", 1);
        Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(mapCount);

        PageUtils pageUtil = new PageUtils(outGoodsSimpleList, integer, limit, page);

        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/disGetTypePrepareOutByDep", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTypePrepareOutByDep(Integer disId, Integer depId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depFatherId", depId);
        map.put("status", 3);
        map.put("dayuOrderStatus", -2);
        map.put("purStatus", 4);
        map.put("purType", 0);

        // 使用超简化版查询，只返回前端需要的字段，大幅减少数据传输量
        List<com.nongxinle.entity.OutGoodsSimpleDTO> outGoodsSimpleList = nxDepartmentOrdersService.disGetNxGoodsApplyUltraSimple(map);
        logger.info("[disGetTypePrepareOutByDep] 查询结果数量: {}", outGoodsSimpleList != null ? outGoodsSimpleList.size() : 0);
        return R.ok().put("data", outGoodsSimpleList);
    }

    @RequestMapping(value = "/disGetTypePrepareOutByCollDis", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTypePrepareOutByCollDis(Integer disId, Integer collDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("collNxDisId", collDisId);
        map.put("status", 3);
        map.put("dayuOrderStatus", -2);
        map.put("purStatus", 4);
        map.put("purType", 0);
        logger.info("[disGetTypePrepareOutByCollDis]{}" ,map);

        // 使用超简化版查询，只返回前端需要的字段，大幅减少数据传输量
        List<com.nongxinle.entity.OutGoodsSimpleDTO> outGoodsSimpleList = nxDepartmentOrdersService.disGetNxGoodsApplyUltraSimple(map);
        logger.info("[disGetTypePrepareOutByCollDis] 查询结果数量: {}", outGoodsSimpleList != null ? outGoodsSimpleList.size() : 0);
        return R.ok().put("data", outGoodsSimpleList);
    }


    @RequestMapping(value = "/disGetTypePrepareOutCata", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTypePrepareOutCata(Integer disId, Integer purType) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
//        map.put("status", 3);
        map.put("dayuOrderStatus", -2);
        map.put("purStatus", 1);
        map.put("orderGoodsType", purType);


        // 使用超简化版查询，只返回曾祖父对象基本信息，不包含嵌套对象，大幅减少数据传输量
        System.out.println("ormapapa" + map);
        List<com.nongxinle.entity.GreatGrandFatherGoodsSimpleDTO> greatGrandList = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoodsUltraSimple(map);
        logger.info("[disGetTypePrepareOutCata] 查询结果数量: {}", greatGrandList != null ? greatGrandList.size() : 0);

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("purStatus", 4);
        map111.put("dayuOrderStatus", -2);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
//
        int puringCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("batchId", 0);
        int unPurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("batchId", 1);
        int havePurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //协作订单数量
        Map<String, Object> mapcoll = new HashMap<>();
        mapcoll.put("disId", disId);
        mapcoll.put("purStatus", 4);
        mapcoll.put("status", 3);
        mapcoll.put("hasCollOrder", 1);

        Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(mapcoll);

        mapR.put("collCount", integer);
        mapR.put("stockCount", stockCount);
        mapR.put("unPurCount", unPurCount);
        mapR.put("havePurCount", havePurCount);
        mapR.put("puringCount", puringCount);
        mapR.put("arr", greatGrandList);
        System.out.println("grnalisiisis" + greatGrandList.size());
        logger.info("[disGetTypePrepareOutCata] 返回数据完成（使用简化DTO，数据传输量大幅减少）");
        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/disGetCollReplyOutPage", method = RequestMethod.POST)
    @ResponseBody
    public R disGetCollReplyOutPage(
            Integer disId,
            Integer page,
            Integer limit) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("hasCollOrder", 1);
        map.put("status", 3);
        map.put("dayuPurStatus", 0);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        logger.info("[disGetCollReplyOutPage] 查询条件 map: {}", map);

        List<NxDistributerEntity> nxDistributerEntities = nxDepartmentOrdersService.queryOfferOrderNxDistributerWithOrder(map);
        logger.info("[disGetCollReplyOutPage] 查询结果数量(协作伙伴数): {}", nxDistributerEntities != null ? nxDistributerEntities.size() : 0);

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("disId", disId);
        mapCount.put("hasCollOrder", 1);
        mapCount.put("status", 3);
        mapCount.put("dayuPurStatus", 0);
        Integer integer = nxDepartmentOrdersService.queryCollReplyPartnerCount(mapCount);

        PageUtils pageUtil = new PageUtils(nxDistributerEntities, integer, limit, page);

        return R.ok().put("page", pageUtil);
    }



    @RequestMapping(value = "/disGetCollNxPrepareOutCata", method = RequestMethod.POST)
    @ResponseBody
    public R disGetCollNxPrepareOutCata(Integer orderDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderDisId", orderDisId);
        map.put("status", 3);
        map.put("purStatus", 1);
        map.put("hasCollOrder", 1);

        // 使用超简化版查询，只返回曾祖父对象基本信息，不包含嵌套对象，大幅减少数据传输量
        System.out.println("ormapapadisGetCollNxPrepareOutCata" + map);
        List<com.nongxinle.entity.GreatGrandFatherGoodsSimpleDTO> greatGrandList = nxDepartmentOrdersService.queryGreatGrandOrderFatherGoodsUltraSimple(map);
        logger.info("[disGetTypePrepareOutCata] 查询结果数量: {}", greatGrandList != null ? greatGrandList.size() : 0);

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", orderDisId);
        map111.put("purStatus", 4);
        map111.put("dayuOrderStatus", -2);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
//
        int puringCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("batchId", 0);
        int unPurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("batchId", 1);
        int havePurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        //协作订单数量
        Map<String, Object> mapcoll = new HashMap<>();
        mapcoll.put("disId", orderDisId);
        mapcoll.put("purStatus", 4);
        mapcoll.put("status", 3);
        mapcoll.put("hasCollOrder", 1);

        Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(mapcoll);

        mapR.put("collCount", integer);
        mapR.put("stockCount", stockCount);
        mapR.put("unPurCount", unPurCount);
        mapR.put("havePurCount", havePurCount);
        mapR.put("puringCount", puringCount);
        mapR.put("arr", greatGrandList);

        Map<String, Object> mapUnPick = new HashMap<>();
        mapUnPick.put("disId", orderDisId);
        mapUnPick.put("purStatus", 1);
        mapUnPick.put("status", 3);
        mapUnPick.put("hasCollOrder", 1);
        Integer unPickCount = nxDepartmentOrdersService.queryOrderGoodsCount(mapUnPick);
        mapUnPick.put("purStatus", null);
        mapUnPick.put("dayuPurStatus", 0);
        System.out.println("mappapaunun" + mapUnPick);
        Integer havePickCount = nxDepartmentOrdersService.queryOrderGoodsCount(mapUnPick);

        mapR.put("unPickCount", unPickCount);
        mapR.put("havePickCount", havePickCount);

        System.out.println("grnalisiisis" + greatGrandList.size());
        logger.info("[disGetTypePrepareOutCata] 返回数据完成（使用简化DTO，数据传输量大幅减少）");
        return R.ok().put("data", mapR);

    }

    /**
     * 按部门查询准备出库/采购的部门列表
     * 只返回部门列表，不包含商品分类
     * 按部门订单数量排序
     * purType: 0-出库商品(purchaseAuto=-1), 1-采购商品(purchaseAuto=1)
     */
    @RequestMapping(value = "/disGetTypePrepareOutDepCata", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTypePrepareOutDepCata(Integer disId, Integer purType) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("dayuOrderStatus", -2);
        map.put("orderGoodsType", purType);
        map.put("isSelfOrder", -1);
        if (purType == 1) {
            map.put("batchId", 0);
        }

        // 1. 查询有订单的部门列表
        System.out.println("查询部门map" + map);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryPureOrderNxDepartmentSimple(map);


        //协作订单
        Map<String, Object> mapOffer = new HashMap<>();
        mapOffer.put("disId", disId);
        mapOffer.put("isSelfOrder", 1);
        mapOffer.put("status", 3);
        System.out.println("enennenenennemapOffer" + mapOffer);
        List<NxDistributerEntity> offerNxDistributer = nxDepartmentOrdersService.queryOfferOrderNxDistributer(mapOffer);

        // 统计每个协作配送商的订单数量等信息
        if (offerNxDistributer != null && !offerNxDistributer.isEmpty()) {
            for (NxDistributerEntity distributerEntity : offerNxDistributer) {
                Map<String, Object> mapColl = new HashMap<>();
                mapColl.put("disId", disId);
                mapColl.put("isSelfOrder", 1);
                mapColl.put("status", 3);
                mapColl.put("purStatus", 4);
                mapColl.put("dayuOrderStatus", -2);
                mapColl.put("collNxDisId", distributerEntity.getNxDistributerId());
                if (purType != null) {
                    mapColl.put("orderGoodsType", purType);
                    mapColl.put("goodsType", purType == 0 ? -1 : purType); // 0出库->-1, 1采购->1
                    if (purType == 1) {
                        mapColl.put("batchId", 0);
                    }
                }

                int orderCount = nxDepartmentOrdersService.queryOrderGoodsCount(mapColl);
//                Map<String, Object> mapPick = new HashMap<>(mapColl);
//                mapPick.put("equalPurStatus", 4);
//                int pickCount = nxDepartmentOrdersService.queryOrderGoodsCount(mapPick);
//                int totalCount = nxDepartmentOrdersService.queryDepOrdersAcount(mapColl);
//                mapColl.put("hasPrice", 1);
//                int hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(mapColl);
//                mapColl.put("hasPrice", null);
//                mapColl.put("hasWeight", 1);
//                int hasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(mapColl);
//                mapColl.put("hasWeight", null);
//                Double subtotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapColl);
//                String twoSubtotal = subtotal != null && subtotal > 0
//                        ? new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString()
//                        : "0";
//
//                Map<String, Object> mapUnDo = new HashMap<>(mapColl);
//                mapUnDo.put("equalStatus", -2);
//                int unDo = nxDepartmentOrdersService.queryDepOrdersAcount(mapUnDo);
//                Map<String, Object> mapFinish = new HashMap<>(mapColl);
//                mapFinish.put("equalStatus", 2);
//                mapFinish.put("dayuPurStatus", 3);
//                mapFinish.put("hasPrice", 1);
//                mapFinish.put("subtotal", 0);
//                int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(mapFinish);

                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("orderCount", orderCount);
//                mapItem.put("pickCount", pickCount);
//                mapItem.put("totalCount", totalCount);
//                mapItem.put("twoSubtotal", twoSubtotal);
//                mapItem.put("hasPrice", hasPrice);
//                mapItem.put("hasWeight", hasWeight);
//                mapItem.put("unDo", unDo);
//                mapItem.put("finishCount", finishCount);
                distributerEntity.setAaa(mapItem);
            }
        }

        // 2. 统计每个部门的订单数量，并按订单数量排序
        List<Map<String, Object>> departmentList = new ArrayList<>();
        for (NxDepartmentEntity department : departmentEntities) {
            Map<String, Object> depMap = new HashMap<>();
            depMap.put("depId", department.getNxDepartmentId());
            depMap.put("depName", department.getNxDepartmentName());
            depMap.put("depFatherId", department.getNxDepartmentFatherId());
            depMap.put("depAttrName", department.getNxDepartmentAttrName());
            depMap.put("depOrderCode", department.getNxDepartmentOrderCode());
            depMap.put("depPinyin", department.getNxDepartmentPinyin());

            // 统计该部门的订单数量
            Map<String, Object> countMap = new HashMap<>();
            countMap.put("disId", disId);
            countMap.put("status", 3);
            countMap.put("purStatus", 4);
            countMap.put("depFatherId", department.getNxDepartmentId());
            countMap.put("orderGoodsType", purType);

            // 根据 purType 设置 batchId，与主查询保持一致
            if (purType == 1) {
                countMap.put("batchId", 0); // 采购商品：只统计未批次的订单
            }
            System.out.println("orercount" + countMap);
            Integer orderCount = nxDepartmentOrdersService.queryDepOrdersAcount(countMap);
            depMap.put("orderCount", orderCount != null ? orderCount : 0);

            departmentList.add(depMap);
        }

        // 3. 按部门名称拼音排序
        departmentList.sort((a, b) -> {
            String pinyinA = (String) a.get("depPinyin");
            String pinyinB = (String) b.get("depPinyin");
            if (pinyinA == null && pinyinB == null) {
                return 0;
            }
            if (pinyinA == null) {
                return 1; // null值排在后面
            }
            if (pinyinB == null) {
                return -1; // null值排在后面
            }
            return pinyinA.compareTo(pinyinB); // 升序排序
        });

        // 4. 全局统计
        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("purStatus", 4);
        map111.put("dayuOrderStatus", -2);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        int puringCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("batchId", 0);
        int unPurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
        map111.put("batchId", 1);
        int havePurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);


        Map<String, Object> mapR = new HashMap<>();

        //协作订单数量
        Map<String, Object> mapcoll = new HashMap<>();
        mapcoll.put("disId", disId);
        mapcoll.put("purStatus", 4);
        mapcoll.put("status", 3);
        mapcoll.put("hasCollOrder", 1);

        Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(mapcoll);

        mapR.put("collCount", integer);
        mapR.put("stockCount", stockCount);
        mapR.put("unPurCount", unPurCount);
        mapR.put("havePurCount", havePurCount);
        mapR.put("puringCount", puringCount);
        mapR.put("arr", departmentList);
        mapR.put("offArr", offerNxDistributer);
        return R.ok().put("data", mapR);
    }

    /**
     * 根据部门ID查询该部门的商品分类、类别下的商品和订单
     * 返回商品分类列表，每个分类下包含商品和订单信息
     * purType: 0-出库商品(purchaseAuto=-1), 1-采购商品(purchaseAuto=1)
     */
    @RequestMapping(value = "/disGetTypePrepareOutDepGoodsPage", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTypePrepareOutDepGoodsPage(Integer disId, Integer depId, Integer purType) {
        logger.info("[disGetTypePrepareOutDepGoodsPage] 开始查询，参数: disId={}, depId={}, purType={}", disId, depId, purType);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depFatherId", depId);
        map.put("status", 3);
        map.put("dayuOrderStatus", -2);
        map.put("purStatus", 4);
        map.put("purType", purType);
        // 不设置 purchaseAuto，查询所有有采购商品的订单（包括 purchaseAuto=-1 但被添加了采购商品的）
        // 只根据 purType 设置 batchId
        if (purType != null && purType == 1) {
            map.put("batchId", 0);
        }


        logger.info("[disGetTypePrepareOutDepGoodsPage] 查询条件 map: {}", map);
        logger.info("[disGetTypePrepareOutDepGoodsPage] 注意：未设置 purchaseAuto，将查询所有有采购商品的订单");

        // 查询该部门下的商品分类（按大类sort排序），每个分类下包含商品和订单
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);

        // 统计订单状态
        fatherGoodsEntities = everyStockFatherGoodsOrderStatus(fatherGoodsEntities, disId, map);
        logger.info("[disGetTypePrepareOutDepGoodsPage] 统计订单状态后分类数量: {}", fatherGoodsEntities != null ? fatherGoodsEntities.size() : 0);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        logger.info("[disGetTypePrepareOutDepGoodsPage] 返回数据完成");
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/disGetTypePrepareOrderHaveStockOut/{disId}")
    @ResponseBody
    public R disGetTypePrepareOrderHaveStockOut(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("dayuPurStatus", 3);
        map.put("purType", 0);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDepartmentOrdersService.disGetOutStockGoodsApplyForStock(map);

        //countOrderStatus
        fatherGoodsEntities = everyStockFatherGoodsOrderStatus(fatherGoodsEntities, disId, map);


        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("purStatus", 3);
        map1.put("purType", -1);

        // 出库
        map1.put("purStatus", 5);
        map1.put("purType", 0);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        // 订货
        map1.put("purType", 1);
        map1.put("inputType", 1);
        int wxCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", fatherGoodsEntities);
        mapR.put("stockCount", stockCount);
        mapR.put("wxCount", wxCount);
        return R.ok().put("data", mapR);

    }


    private List<NxDistributerFatherGoodsEntity> everyStockFatherGoodsOrderStatus(List<NxDistributerFatherGoodsEntity> fatherGoodsList,
                                                                                  Integer disId, Map<String, Object> map) {
        if (fatherGoodsList == null || fatherGoodsList.isEmpty()) {
            return fatherGoodsList;
        }

        // 提取所有父级商品ID
        List<Integer> grandIds = fatherGoodsList.stream()
                .map(NxDistributerFatherGoodsEntity::getNxDistributerFatherGoodsId)
                .collect(Collectors.toList());

        // 构建查询参数
        Map<String, Object> queryParams = new HashMap<>(map);
        queryParams.put("disId", disId);
        queryParams.put("purStatus", 4);
        queryParams.put("dayuOrderStatus", -2);
        System.out.println("ididisisis" + grandIds);

        // 批量查询订单数量
        Map<Integer, Integer> orderCountMap = nxDepartmentOrdersService.batchQueryFatherGoodsOrderCount(grandIds, queryParams);

        // 设置订单数量
        for (NxDistributerFatherGoodsEntity fatherGoods : fatherGoodsList) {
            Integer orderCount = orderCountMap.get(fatherGoods.getNxDistributerFatherGoodsId());
            fatherGoods.setNewOrderCount(orderCount != null ? orderCount : 0);
        }

        return fatherGoodsList;
    }


    /**
     * 9-11
     * DISTRIBUTER
     * 保存订单的数量
     *
     * @param depOrders 订单
     * @return ok
     */
    @RequestMapping(value = "/saveToFillWeightAndPrice", method = RequestMethod.POST)
    @ResponseBody
    public R saveToFillWeightAndPrice(@RequestBody NxDepartmentOrdersEntity depOrders) {

        if (depOrders.getNxDoSubtotal() != null && new BigDecimal(depOrders.getNxDoSubtotal()).compareTo(BigDecimal.ZERO) == 1) {
            depOrders.setNxDoStatus(2);
        }
        //profit
        if (depOrders.getNxDoExpectPrice() != null && !depOrders.getNxDoExpectPrice().trim().isEmpty()) {

            BigDecimal expectPrice = new BigDecimal(depOrders.getNxDoExpectPrice());
            BigDecimal doPrice = new BigDecimal(depOrders.getNxDoPrice());
            BigDecimal subtract = doPrice.subtract(expectPrice);
            depOrders.setNxDoPriceDifferent(subtract.toString());
        }

        System.out.println("sorderss" + depOrders);
        nxDepartmentOrdersService.update(depOrders);

        return R.ok();
    }


    @RequestMapping(value = "/giveOrderWeightForPrint", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderWeightForPrint(Integer orderId, String weight) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoWeight(weight);
        String nxDoPrice = ordersEntity.getNxDoPrice();
        if (nxDoPrice != null) {
            BigDecimal decimal = new BigDecimal(nxDoPrice).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(decimal.toString());
        }
        //cost
        BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
        BigDecimal weightB = new BigDecimal(weight);
        BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxDoCostSubtotal(decimal.toString());
        nxDepartmentOrdersService.update(ordersEntity);
        //weight
        if (ordersEntity.getNxDoWeightId() != null) {
            NxDistributerWeightEntity weightEntity = nxDistributerWeightService.queryObject(ordersEntity.getNxDoWeightId());
            Integer nxDwItemCount = weightEntity.getNxDwItemCount();
            Integer nxDwItemFinishCount = weightEntity.getNxDwItemFinishCount();
            if (nxDwItemCount - nxDwItemFinishCount > 1) {
                weightEntity.setNxDwItemFinishCount(nxDwItemFinishCount + 1);
            } else {
                weightEntity.setNxDwItemFinishCount(nxDwItemCount);
                weightEntity.setNxDwStatus(1);
            }
            nxDistributerWeightService.update(weightEntity);
        }
        return R.ok();
    }

    @RequestMapping(value = "/giveOrderWeightForStockPrintAndFinish", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderWeightForStockPrintAndFinish(Integer orderId, String weight) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoWeight(weight);
        String nxDoPrice = ordersEntity.getNxDoPrice();
        if (nxDoPrice != null) {
            BigDecimal decimal = new BigDecimal(nxDoPrice).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(decimal.toString());
            ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
        }
        //cost
        BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
        BigDecimal weightB = new BigDecimal(weight);
        BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxDoCostSubtotal(decimal.toString());
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
        nxDepartmentOrdersService.update(ordersEntity);
        //weight
        if (ordersEntity.getNxDoWeightId() != null) {
            NxDistributerWeightEntity weightEntity = nxDistributerWeightService.queryObject(ordersEntity.getNxDoWeightId());
            Integer nxDwItemCount = weightEntity.getNxDwItemCount();
            Integer nxDwItemFinishCount = weightEntity.getNxDwItemFinishCount();
            if (nxDwItemCount - nxDwItemFinishCount > 1) {
                weightEntity.setNxDwItemFinishCount(nxDwItemFinishCount + 1);
            } else {
                weightEntity.setNxDwItemFinishCount(nxDwItemCount);
                weightEntity.setNxDwStatus(1);
            }
            nxDistributerWeightService.update(weightEntity);
        }
        return R.ok();
    }

    @RequestMapping(value = "/giveOrderWeight", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderWeight(Integer orderId, String weight) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoWeight(weight);
        String nxDoPrice = ordersEntity.getNxDoPrice();
        if (nxDoPrice != null) {
            BigDecimal decimal = new BigDecimal(nxDoPrice).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(decimal.toString());
        }
        //cost
        BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
        BigDecimal weightB = new BigDecimal(weight);
        BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxDoCostSubtotal(decimal.toString());
        nxDepartmentOrdersService.update(ordersEntity);

        return R.ok();
    }

    @RequestMapping(value = "/updateOrderWeight", method = RequestMethod.POST)
    @ResponseBody
    public R updateOrderWeight(Integer orderId, String weight) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoWeight(weight);
        if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
            String nxDoPrice = ordersEntity.getNxDoPrice();
            BigDecimal subtotalB = new BigDecimal(nxDoPrice).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotalB.toString());
            if (ordersEntity.getNxDoCostSubtotal() != null && !ordersEntity.getNxDoCostSubtotal().trim().isEmpty()
                    && ordersEntity.getNxDoCostPrice() != null && !ordersEntity.getNxDoCostPrice().trim().isEmpty()) {
                System.out.println("weidhht" + weight);
                BigDecimal weightB = new BigDecimal(weight);
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitB = subtotalB.subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                ordersEntity.setNxDoProfitScale(scaleB.toString());
                ordersEntity.setNxDoProfitSubtotal(profitB.toString());
            }

        }
        if (ordersEntity.getNxDoExpectPrice() != null && !ordersEntity.getNxDoExpectPrice().trim().isEmpty()) {
            BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
            BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
            BigDecimal subtract = doPrice.subtract(expectPrice);
            System.out.println("exxxxxxxx" + subtract);
            ordersEntity.setNxDoPriceDifferent(subtract.toString());

        }

        nxDepartmentOrdersService.update(ordersEntity);

        return R.ok();
    }


    @RequestMapping(value = "/updateOrderWeightGb", method = RequestMethod.POST)
    @ResponseBody
    public R updateOrderWeightGb(Integer orderId, String weight) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoWeight(weight);
        String nxDoPrice = ordersEntity.getNxDoPrice();
        if (nxDoPrice != null) {
            BigDecimal subtotalB = new BigDecimal(nxDoPrice).multiply(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotalB.toString());
            if (ordersEntity.getNxDoCostSubtotal() != null) {
                BigDecimal weightB = new BigDecimal(weight);
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitB = subtotalB.subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                ordersEntity.setNxDoProfitScale(scaleB.toString());
                ordersEntity.setNxDoProfitSubtotal(profitB.toString());
            }
        }

        nxDepartmentOrdersService.update(ordersEntity);

        System.out.println("xnordidiididid" + ordersEntity.getNxDoGbDepartmentOrderId());
        if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(ordersEntity.getNxDepartmentOrdersId());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusProcurement());
            gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());

            if (gbDepartmentOrdersEntity.getGbDoPrice() != null && !gbDepartmentOrdersEntity.getGbDoPrice().equals("0.0")) {
                BigDecimal decimal1 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoPrice());
                BigDecimal decimal2 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoWeight());
                BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                gbDepartmentOrdersEntity.setGbDoSubtotal(decimal3.toString());
            }
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
            System.out.println("xnordidiididid" + gbDepartmentOrdersEntity.getGbDepartmentOrdersId());


            Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();

            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", purchaseGoodsId);
            List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
            BigDecimal purWeight = new BigDecimal(0);
            if (gbDepartmentOrdersEntities.size() > 0) {
                for (GbDepartmentOrdersEntity gbOrder : gbDepartmentOrdersEntities) {
                    if (gbOrder.getGbDoWeight() != null) {
                        purWeight = purWeight.add(new BigDecimal(gbOrder.getGbDoWeight()));
                    }
                }
            }
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
            purchaseGoodsEntity.setGbDpgBuyQuantity(purWeight.toString());
            if (purchaseGoodsEntity.getGbDpgBuyPrice() != null) {
                BigDecimal decimal1 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                BigDecimal decimal2 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
                BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                purchaseGoodsEntity.setGbDpgBuySubtotal(decimal3.toString());
            }
//            purchaseGoodsEntity.setGbDpgStatus(getNxDisPurchaseGoodsIsPurchase());
            System.out.println("purpprur" + purchaseGoodsEntity);
            gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

        }

        return R.ok();
    }


    @RequestMapping(value = "/giveOrderPrice", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderPrice(Integer orderId, String price) {

        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoPrice(price);
        System.out.println("odididiididi==" + orderId + "priciiei===" + price);
        //try
        if (ordersEntity.getNxDoWeight() != null && !ordersEntity.getNxDoWeight().trim().isEmpty() && new BigDecimal(ordersEntity.getNxDoWeight()).compareTo(BigDecimal.ZERO) == 1) {
            BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());
            BigDecimal subtotalB = weightB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotalB.toString());
            ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());

            //profit
            if (ordersEntity.getNxDoExpectPrice() != null && !ordersEntity.getNxDoExpectPrice().trim().isEmpty() && !ordersEntity.getNxDoExpectPrice().isEmpty()) {
                BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
                BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                BigDecimal subtract = doPrice.subtract(expectPrice);
                System.out.println("exxxxxxxx" + subtract);
                ordersEntity.setNxDoPriceDifferent(subtract.toString());

            }
            System.out.println("cosrprice" + ordersEntity.getNxDoCostPrice());
            if (ordersEntity.getNxDoCostPrice() != null && !ordersEntity.getNxDoCostPrice().isEmpty()) {
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitB = subtotalB.subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                System.out.println("subsbsbbssbbsbbbbb" + subtotalB);
                BigDecimal scaleB = subtotalB.compareTo(BigDecimal.ZERO) != 0
                        ? profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                        : BigDecimal.ZERO;
                ordersEntity.setNxDoProfitScale(scaleB.toString());
                ordersEntity.setNxDoProfitSubtotal(profitB.toString());
            }
        }

        nxDepartmentOrdersService.update(ordersEntity);


        return R.ok();
    }

    @RequestMapping(value = "/giveOrderPriceColl", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderPriceColl(Integer orderId, String price) {

        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoPrice(price);
        System.out.println("odididiididi==" + orderId + "priciiei===" + price);
        //try
        if (ordersEntity.getNxDoWeight() != null && !ordersEntity.getNxDoWeight().trim().isEmpty() && new BigDecimal(ordersEntity.getNxDoWeight()).compareTo(BigDecimal.ZERO) == 1) {
            BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());
            BigDecimal subtotalB = weightB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotalB.toString());
            ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());

            //profit
            if (ordersEntity.getNxDoExpectPrice() != null && !ordersEntity.getNxDoExpectPrice().trim().isEmpty() && !ordersEntity.getNxDoExpectPrice().isEmpty()) {
                BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
                BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                BigDecimal subtract = doPrice.subtract(expectPrice);
                System.out.println("exxxxxxxx" + subtract);
                ordersEntity.setNxDoPriceDifferent(subtract.toString());

            }
            System.out.println("cosrprice" + ordersEntity.getNxDoCostPrice());
            if (ordersEntity.getNxDoCostPrice() != null && !ordersEntity.getNxDoCostPrice().isEmpty()) {
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitB = subtotalB.subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                System.out.println("subsbsbbssbbsbbbbb" + subtotalB);
                BigDecimal scaleB = subtotalB.compareTo(BigDecimal.ZERO) != 0
                        ? profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                        : BigDecimal.ZERO;
                ordersEntity.setNxDoProfitScale(scaleB.toString());
                ordersEntity.setNxDoProfitSubtotal(profitB.toString());
            }
        }

        nxDepartmentOrdersService.update(ordersEntity);

        //更新协作订单
        System.out.println("colllllmap" + ordersEntity.getNxDoCollaborativeNxDisId());
        if(ordersEntity.getNxDoCollaborativeNxDisId() != -1){
            Integer nxDoCollaborativeNxDisId = ordersEntity.getNxDoCollaborativeNxDisId();
            Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
            Map<String, Object> mapColl = new HashMap<>();
            mapColl.put("disId", nxDoCollaborativeNxDisId);
            mapColl.put("restrauntId", nxDepartmentOrdersId);
            System.out.println("colllllmap" + mapColl);
            NxDepartmentOrdersEntity collOrder =  nxDepartmentOrdersService.querycollOrder(mapColl);
            if(collOrder != null && collOrder.getNxDoStatus() < 2){
                collOrder.setNxDoCostPrice(ordersEntity.getNxDoPrice());
                if (collOrder.getNxDoWeight() != null && !collOrder.getNxDoWeight().trim().isEmpty() && new BigDecimal(collOrder.getNxDoWeight()).compareTo(BigDecimal.ZERO) == 1) {
                    BigDecimal weightB = new BigDecimal(collOrder.getNxDoWeight());
                    BigDecimal subtotalB = weightB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    collOrder.setNxDoCostSubtotal(subtotalB.toString());
                }
                nxDepartmentOrdersService.update(collOrder);
            }
        }

        return R.ok();
    }

    @RequestMapping(value = "/updateOrderPrice", method = RequestMethod.POST)
    @ResponseBody
    public R updateOrderPrice(Integer orderId, String price) {

        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoPrice(price);
        //try
        if (ordersEntity.getNxDoWeight() != null && !ordersEntity.getNxDoWeight().trim().isEmpty() && new BigDecimal(ordersEntity.getNxDoWeight()).compareTo(BigDecimal.ZERO) == 1) {
            BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());
            BigDecimal subtotalB = weightB.multiply(new BigDecimal(price)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotalB.toString());
            ordersEntity.setNxDoStatus(2);
            //profit
            if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
                BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice());
                BigDecimal decimal = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profitB = subtotalB.subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                ordersEntity.setNxDoProfitScale(scaleB.toString());
                ordersEntity.setNxDoProfitSubtotal(profitB.toString());
            }
        }
        nxDepartmentOrdersService.update(ordersEntity);
        return R.ok();
    }


    @RequestMapping(value = "/giveOrderWeightListForStockAndFinish", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderWeightListForStockAndFinish(@RequestBody List<NxDepartmentOrdersEntity> ordersEntityList) {
        for (NxDepartmentOrdersEntity ordersEntityOld : ordersEntityList) {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(ordersEntityOld.getNxDepartmentOrdersId());
            if (ordersEntityOld.getNxDoPickUserId() != null) {
                ordersEntity.setNxDoPickUserId(ordersEntityOld.getNxDoPickUserId());
            }
            ordersEntity.setNxDoWeight(ordersEntityOld.getNxDoWeight());
            System.out.println("oslsllslsls" + ordersEntityOld.getNxDoWeight());
            if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty() && !ordersEntity.getNxDoPrice().equals("0.1")) {
                BigDecimal subtotal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(ordersEntityOld.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoSubtotal(subtotal.toString());
                ordersEntity.setNxDoWeight(ordersEntityOld.getNxDoWeight());
                ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
            }

            ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
            nxDepartmentOrdersService.update(ordersEntity);

            Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
            if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                if (purchaseGoodsEntity != null) {
                    Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                    Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                    System.out.println("purGoodsss" + nxDpgFinishAmount);
                    if (nxDpgOrdersAmount != null && nxDpgFinishAmount != null) {
                        if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
//查询商品如有有采购批次nxDistributerPurchaseBatch
                            // 并且该批次下的全部采购商品nxDistributerGoods的 nxDpgStatus == getNxDisPurchaseGoodsFinishBuy 的数量
                            //是所有采购商品数量减去 1 ，那么就认为该采购批次也是完成。
                            if (purchaseGoodsEntity.getNxDpgBatchId() != null && purchaseGoodsEntity.getNxDpgBatchId() != -1) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("batchId", purchaseGoodsEntity.getNxDpgBatchId());
                                int count = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                                map.put("equalStatus", getNxDisPurchaseGoodsFinishBuy());
                                int finishCount = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                                if (count - finishCount == 1) {
                                    NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDistributerPurchaseBatchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());
                                    nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
                                    nxDistributerPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);
                                }
                            }
                        } else {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                        }
                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                } else {
                    System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
                }
            }


            System.out.println("xnordidiididid" + ordersEntity.getNxDoGbDepartmentOrderId());
            if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(ordersEntity.getNxDepartmentOrdersId());
                gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
                gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusProcurement());
                gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());

                if (gbDepartmentOrdersEntity.getGbDoPrice() != null && !gbDepartmentOrdersEntity.getGbDoPrice().trim().isEmpty() && !gbDepartmentOrdersEntity.getGbDoPrice().equals("0.0") && gbDepartmentOrdersEntity.getGbDoWeight() != null) {
                    BigDecimal decimal1 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoPrice());
                    BigDecimal decimal2 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoWeight());
                    BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                    gbDepartmentOrdersEntity.setGbDoSubtotal(decimal3.toString());
                }
                gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
                System.out.println("xnordidiididid" + gbDepartmentOrdersEntity.getGbDepartmentOrdersId());


                Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();

                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", purchaseGoodsId);
                List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
                BigDecimal purWeight = new BigDecimal(0);
                if (gbDepartmentOrdersEntities.size() > 0) {
                    for (GbDepartmentOrdersEntity gbOrder : gbDepartmentOrdersEntities) {
                        if (gbOrder.getGbDoWeight() != null) {
                            purWeight = purWeight.add(new BigDecimal(gbOrder.getGbDoWeight()));
                        }
                    }
                }
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
                purchaseGoodsEntity.setGbDpgBuyQuantity(purWeight.toString());
                if (purchaseGoodsEntity.getGbDpgBuyPrice() != null && !purchaseGoodsEntity.getGbDpgBuyPrice().trim().isEmpty()) {
                    BigDecimal decimal1 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                    BigDecimal decimal2 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
                    BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal3.toString());
                }
                purchaseGoodsEntity.setGbDpgStatus(getNxDisPurchaseGoodsIsPurchase());
                System.out.println("purpprur" + purchaseGoodsEntity);
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

            }

            //更新协作订单
            System.out.println("colllllmap" + ordersEntity.getNxDoCollaborativeNxDisId());

            if(ordersEntity.getNxDoCollaborativeNxDisId() != -1){
                Integer nxDoCollaborativeNxDisId = ordersEntity.getNxDoCollaborativeNxDisId();
                Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
                Map<String, Object> map = new HashMap<>();
                map.put("disId", nxDoCollaborativeNxDisId);
                map.put("restrauntId", nxDepartmentOrdersId);
                System.out.println("colllllmap" + map);
                NxDepartmentOrdersEntity collOrder =  nxDepartmentOrdersService.querycollOrder(map);
                if(collOrder != null && collOrder.getNxDoStatus() < 2){
                    collOrder.setNxDoPurchaseStatus(1);
                    collOrder.setNxDoWeight(ordersEntityOld.getNxDoWeight());
                    nxDepartmentOrdersService.update(collOrder);
                }
            }
        }

        return R.ok();
    }


    @RequestMapping(value = "/pickerGiveOrderWeight", method = RequestMethod.POST)
    @ResponseBody
    public R pickerGiveOrderWeight(Integer orderId, String orderWeight, Integer pickerUserId) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);

        ordersEntity.setNxDoPickUserId(pickerUserId);

        ordersEntity.setNxDoWeight(orderWeight);
        if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
            BigDecimal subtotal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(orderWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotal.toString());
            ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
        }
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
        nxDepartmentOrdersService.update(ordersEntity);

        Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
        if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
            if (purchaseGoodsEntity != null) {
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                System.out.println("purGoodsss" + nxDpgFinishAmount);
                if (nxDpgOrdersAmount != null && nxDpgFinishAmount != null) {
                    if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                        purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
//查询商品如有有采购批次nxDistributerPurchaseBatch
                        // 并且该批次下的全部采购商品nxDistributerGoods的 nxDpgStatus == getNxDisPurchaseGoodsFinishBuy 的数量
                        //是所有采购商品数量减去 1 ，那么就认为该采购批次也是完成。
                        if (purchaseGoodsEntity.getNxDpgBatchId() != null && purchaseGoodsEntity.getNxDpgBatchId() != -1) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("batchId", purchaseGoodsEntity.getNxDpgBatchId());
                            int count = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                            map.put("equalStatus", getNxDisPurchaseGoodsFinishBuy());
                            int finishCount = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                            if (count - finishCount == 1) {
                                NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDistributerPurchaseBatchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());
                                nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
                                nxDistributerPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);
                            }
                        }
                    } else {
                        purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                    }
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            } else {
                System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
            }
        }


        System.out.println("xnordidiididid" + ordersEntity.getNxDoGbDepartmentOrderId());
        if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(ordersEntity.getNxDepartmentOrdersId());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusProcurement());
            gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());

            if (gbDepartmentOrdersEntity.getGbDoPrice() != null && !gbDepartmentOrdersEntity.getGbDoPrice().trim().isEmpty() && !gbDepartmentOrdersEntity.getGbDoPrice().equals("0.0") && gbDepartmentOrdersEntity.getGbDoWeight() != null) {
                BigDecimal decimal1 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoPrice());
                BigDecimal decimal2 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoWeight());
                BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                gbDepartmentOrdersEntity.setGbDoSubtotal(decimal3.toString());
            }
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);

            Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();

            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", purchaseGoodsId);
            List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
            BigDecimal purWeight = new BigDecimal(0);
            if (gbDepartmentOrdersEntities.size() > 0) {
                for (GbDepartmentOrdersEntity gbOrder : gbDepartmentOrdersEntities) {
                    if (gbOrder.getGbDoWeight() != null) {
                        purWeight = purWeight.add(new BigDecimal(gbOrder.getGbDoWeight()));
                    }
                }
            }
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
            purchaseGoodsEntity.setGbDpgBuyQuantity(purWeight.toString());
            if (purchaseGoodsEntity.getGbDpgBuyPrice() != null && !purchaseGoodsEntity.getGbDpgBuyPrice().trim().isEmpty()) {
                BigDecimal decimal1 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                BigDecimal decimal2 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
                BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                purchaseGoodsEntity.setGbDpgBuySubtotal(decimal3.toString());
            }
            purchaseGoodsEntity.setGbDpgStatus(getNxDisPurchaseGoodsIsPurchase());
            System.out.println("purpprur" + purchaseGoodsEntity);
            gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

        }

        return R.ok();
    }


    @RequestMapping(value = "/pickerGiveOrderWeightUpdateShelfStock", method = RequestMethod.POST)
    @ResponseBody
    public R pickerGiveOrderWeightUpdateShelfStock(Integer orderId, String orderWeight, Integer pickerUserId, Integer shelfId) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orderId);
        ordersEntity.setNxDoPickUserId(pickerUserId);

        // 无论价格是否为空，都要设置正确的重量
        ordersEntity.setNxDoWeight(orderWeight);

        if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
            BigDecimal subtotal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(orderWeight)).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotal.toString());
            ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
            // 查询是否有库存（queryShelfStockListByParams：按生产日期 FIFO，无生产日期时按批次日期）
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", ordersEntity.getNxDoDisGoodsId());
            map.put("restWeight", 0);
            // 如果订单有出库货架ID，则只查询该货架的库存
            if (shelfId != -1) {
                map.put("shelfId", shelfId);
            }
            List<NxDistributerGoodsShelfStockEntity> stockEntities = nxDisGoodsShelfStockService.queryShelfStockListByParams(map);

            if (stockEntities.size() > 0) {
                // 查询商品信息，判断出库单位（需要先判断，才能正确转换数量）
                NxDistributerGoodsEntity disGoodsForOut = nxDistributerGoodsService.queryObject(ordersEntity.getNxDoDisGoodsId());
                boolean useCartonPrice = false;
                BigDecimal itemsPerCartonBdForOut = null;
                if (disGoodsForOut != null
                        && disGoodsForOut.getNxDgCartonUnit() != null
                        && !disGoodsForOut.getNxDgCartonUnit().trim().isEmpty()
                        && ordersEntity.getNxDoPrintStandard() != null
                        && ordersEntity.getNxDoPrintStandard().trim().equals(disGoodsForOut.getNxDgCartonUnit().trim())) {
                    itemsPerCartonBdForOut = CommonUtils.parseItemsPerCartonBigDecimal(disGoodsForOut.getNxDgItemsPerCarton());
                    if (itemsPerCartonBdForOut != null && itemsPerCartonBdForOut.compareTo(BigDecimal.ZERO) > 0) {
                        useCartonPrice = true;
                        System.out.println("✅ 出库单位与外包装单位匹配，使用外包装单价: " + disGoodsForOut.getNxDgCartonUnit()
                                + ", 每箱" + itemsPerCartonBdForOut.toPlainString() + "个");
                    } else {
                        System.out.println("⚠️ 外包装单位匹配但每箱数量无效");
                    }
                } else {
                    System.out.println("⚠️ 出库单位不匹配 - 订单规格: " + ordersEntity.getNxDoStandard()
                            + ", 商品外包装单位: " + (disGoodsForOut != null ? disGoodsForOut.getNxDgCartonUnit() : "null"));
                }

                // 需要出货的重量（使用确定的重量值）
                BigDecimal needWeightRaw = new BigDecimal(orderWeight);
                BigDecimal needWeight; // 转换为最小单位后的重量
                BigDecimal totalCost = BigDecimal.ZERO; // 总成本

                System.out.println("🔍 调试信息 - 前台提交的重量: " + needWeightRaw + ", 订单规格: " + ordersEntity.getNxDoStandard());
                System.out.println("🔍 调试信息 - useCartonPrice: " + useCartonPrice + ", itemsPerCartonBdForOut: " + itemsPerCartonBdForOut);

                // 如果使用大包装单位，需要将箱数转换为最小单位数量
                if (useCartonPrice && itemsPerCartonBdForOut != null) {
                    // 前台提交的是箱数，需要转换为最小单位数量
                    needWeight = needWeightRaw.multiply(itemsPerCartonBdForOut);
                    System.out.println("✅ 开始FIFO扣减库存 - 订单箱数: " + needWeightRaw + "箱, 转换为最小单位: " + needWeight + "个");
                } else {
                    // 前台提交的已经是最小单位数量
                    needWeight = needWeightRaw;
                    System.out.println("✅ 开始FIFO扣减库存 - 订单重量: " + needWeight + "个（未使用大包装单位）");
                }

                BigDecimal remainingWeight = needWeight; // 剩余需要扣减的重量（最小单位）
                System.out.println("🔍 调试信息 - remainingWeight（剩余需要扣减）: " + remainingWeight + "个");

                for (NxDistributerGoodsShelfStockEntity stockEntity : stockEntities) {
                    if (remainingWeight.compareTo(BigDecimal.ZERO) <= 0) {
                        break; // 已经扣减完毕
                    }

                    BigDecimal stockRestWeight = new BigDecimal(stockEntity.getNxDgssRestWeight());

                    // 计算本批次可以扣减的重量（最小单位）
                    BigDecimal deductWeight;
                    if (remainingWeight.compareTo(stockRestWeight) <= 0) {
                        // 本批次够用
                        deductWeight = remainingWeight;
                    } else {
                        // 本批次不够，全部扣减
                        deductWeight = stockRestWeight;
                    }

                    // 根据出库单位选择对应的单价和计算方式
                    BigDecimal stockPrice;
                    BigDecimal deductWeightForCalc; // 用于成本计算的扣减数量
                    if (useCartonPrice && stockEntity.getNxDgssPriceCarton() != null
                            && !stockEntity.getNxDgssPriceCarton().trim().isEmpty()
                            && itemsPerCartonBdForOut != null) {
                        // 使用外包装单价，需要将最小单位数量转换为箱数
                        deductWeightForCalc = deductWeight.divide(itemsPerCartonBdForOut, 4, BigDecimal.ROUND_HALF_UP);
                        stockPrice = new BigDecimal(stockEntity.getNxDgssPriceCarton());
                        System.out.println("批次ID: " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                ", 剩余: " + stockRestWeight + ", 扣减: " + deductWeight + "个(" + deductWeightForCalc + "箱)"
                                + ", 使用外包装单价: " + stockPrice + "元/箱");
                    } else {
                        // 使用最小单位单价
                        String priceStr = stockEntity.getNxDgssPrice();
                        stockPrice = (priceStr == null || priceStr.trim().isEmpty())
                                ? BigDecimal.ZERO
                                : new BigDecimal(priceStr);
                        deductWeightForCalc = deductWeight;
                        System.out.println("批次ID: " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                ", 剩余: " + stockRestWeight + ", 扣减: " + deductWeight
                                + ", 使用最小单位单价: " + stockPrice);
                    }

                    if (stockRestWeight.compareTo(BigDecimal.ZERO) <= 0) {
                        continue; // 跳过没有库存的批次
                    }

                    // 计算本批次的成本（使用对应的单价和数量）
                    BigDecimal batchCost = deductWeightForCalc.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    totalCost = totalCost.add(batchCost);

                    // 更新库存批次的剩余数量
                    BigDecimal newRestWeight = stockRestWeight.subtract(deductWeight);
                    stockEntity.setNxDgssRestWeight(newRestWeight.toString());
                    setFifoNewRestSubtotalAfterDeduct(stockEntity, stockRestWeight, newRestWeight);

                    // 保存库存批次更新
                    nxDisGoodsShelfStockService.update(stockEntity);

                    // 创建库存扣减记录
                    NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
                    reduceEntity.setNxDgssrNxDistributerId(ordersEntity.getNxDoDistributerId());
                    reduceEntity.setNxDgssrNxDisGoodsId(ordersEntity.getNxDoDisGoodsId());
                    reduceEntity.setNxDgssrNxDisGoodsFatherId(stockEntity.getNxDgssNxDisGoodsFatherId());
                    reduceEntity.setNxDgssrNxStockId(stockEntity.getNxDistributerGoodsShelfStockId());
                    reduceEntity.setNxDgssrDate(formatWhatDay(0));
                    reduceEntity.setNxDgssrWeek(getWeek(0));
                    reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
                    reduceEntity.setNxDgssrFullTime(formatWhatYearDayTime(0));
                    reduceEntity.setNxDgssrType(0); // 0=销售扣减
                    reduceEntity.setNxDgssrDoUserId(ordersEntity.getNxDoPickUserId());
                    reduceEntity.setNxDgssrCostWeight(deductWeight.toString());
                    reduceEntity.setNxDgssrCostSubtotal(batchCost.toString());
                    reduceEntity.setNxDgssrNxPurGoodsId(stockEntity.getNxDgssNxPurGoodsId());
                    reduceEntity.setNxDgssrNxDepOrderId(ordersEntity.getNxDepartmentOrdersId());
                    reduceEntity.setNxDgssrStatus(0);
                    reduceEntity.setNxDgssrProduceWeight(deductWeight.toString());
                    reduceEntity.setNxDgssrProduceSubtotal(batchCost.toString());
                    nxDisGoodsShelfStockReduceService.save(reduceEntity);

                    System.out.println("扣减批次 " + stockEntity.getNxDistributerGoodsShelfStockId() +
                            " - 扣减重量: " + deductWeight + ", 本批次成本: " + batchCost +
                            ", 新剩余: " + newRestWeight + ", 已保存扣减记录");

                    // 减少剩余需要扣减的重量
                    remainingWeight = remainingWeight.subtract(deductWeight);
                }

                // 计算加权平均成本价（保留1位小数）
                // 如果使用外包装单价，needWeight应该是箱数，avgCostPrice是每箱的平均成本价
                BigDecimal avgCostPrice = totalCost.divide(needWeight, 1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoCostPrice(avgCostPrice.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                ordersEntity.setNxDoCostSubtotal(totalCost.toString());

                // 计算利润
                BigDecimal sellPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                BigDecimal profit = sellPrice.subtract(avgCostPrice);
                BigDecimal profitSubtotal = profit.multiply(needWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());

            }
        }

        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
        nxDepartmentOrdersService.update(ordersEntity);


        Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
        if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
            if (purchaseGoodsEntity != null) {
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                System.out.println("purGoodsss" + nxDpgFinishAmount);
                if (nxDpgOrdersAmount != null && nxDpgFinishAmount != null) {
                    if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                        purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
//查询商品如有有采购批次nxDistributerPurchaseBatch
                        // 并且该批次下的全部采购商品nxDistributerGoods的 nxDpgStatus == getNxDisPurchaseGoodsFinishBuy 的数量
                        //是所有采购商品数量减去 1 ，那么就认为该采购批次也是完成。
                        if (purchaseGoodsEntity.getNxDpgBatchId() != null && purchaseGoodsEntity.getNxDpgBatchId() != -1) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("batchId", purchaseGoodsEntity.getNxDpgBatchId());
                            int count = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                            map.put("equalStatus", getNxDisPurchaseGoodsFinishBuy());
                            int finishCount = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                            if (count - finishCount == 1) {
                                NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDistributerPurchaseBatchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());
                                nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
                                nxDistributerPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);
                            }
                        }
                    } else {
                        purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                    }
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            } else {
                System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
            }
        }


        System.out.println("xnordidiididid" + ordersEntity.getNxDoGbDepartmentOrderId());
        if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(ordersEntity.getNxDepartmentOrdersId());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
            gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusProcurement());
            gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());

            if (gbDepartmentOrdersEntity.getGbDoPrice() != null && !gbDepartmentOrdersEntity.getGbDoPrice().trim().isEmpty() && !gbDepartmentOrdersEntity.getGbDoPrice().equals("0.0") && gbDepartmentOrdersEntity.getGbDoWeight() != null) {
                BigDecimal decimal1 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoPrice());
                BigDecimal decimal2 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoWeight());
                BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                gbDepartmentOrdersEntity.setGbDoSubtotal(decimal3.toString());
            }
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
            System.out.println("xnordidiididid" + gbDepartmentOrdersEntity.getGbDepartmentOrdersId());


            Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();

            Map<String, Object> map = new HashMap<>();
            map.put("purGoodsId", purchaseGoodsId);
            List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
            BigDecimal purWeight = new BigDecimal(0);
            if (gbDepartmentOrdersEntities.size() > 0) {
                for (GbDepartmentOrdersEntity gbOrder : gbDepartmentOrdersEntities) {
                    if (gbOrder.getGbDoWeight() != null) {
                        purWeight = purWeight.add(new BigDecimal(gbOrder.getGbDoWeight()));
                    }
                }
            }
            GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
            purchaseGoodsEntity.setGbDpgBuyQuantity(purWeight.toString());
            if (purchaseGoodsEntity.getGbDpgBuyPrice() != null && !purchaseGoodsEntity.getGbDpgBuyPrice().trim().isEmpty()) {
                BigDecimal decimal1 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                BigDecimal decimal2 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
                BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                purchaseGoodsEntity.setGbDpgBuySubtotal(decimal3.toString());
            }
            purchaseGoodsEntity.setGbDpgStatus(getNxDisPurchaseGoodsIsPurchase());
            System.out.println("purpprur" + purchaseGoodsEntity);
            gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

        }

        //更新协商订单
        //更新协作订单
        System.out.println("colllllmap" + ordersEntity.getNxDoCollaborativeNxDisId());

        if(ordersEntity.getNxDoCollaborativeNxDisId() != -1){
            Integer nxDoCollaborativeNxDisId = ordersEntity.getNxDoCollaborativeNxDisId();
            Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
            Map<String, Object> map = new HashMap<>();
            map.put("disId", nxDoCollaborativeNxDisId);
            map.put("restrauntId", nxDepartmentOrdersId);
            System.out.println("colllllmap" + map);
            NxDepartmentOrdersEntity collOrder =  nxDepartmentOrdersService.querycollOrder(map);
            if(collOrder != null && collOrder.getNxDoStatus() < 2){
                collOrder.setNxDoPurchaseStatus(1);
                collOrder.setNxDoWeight(ordersEntity.getNxDoWeight());
                nxDepartmentOrdersService.update(collOrder);
            }
        }

        return R.ok();
    }


    @RequestMapping(value = "/giveOrderWeightListForStockShelfGoods", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderWeightListForStockShelfGoods(@RequestBody List<NxDepartmentOrdersEntity> ordersEntityList) {
        List<Map<String, Object>> shortages = new ArrayList<>();
        for (NxDepartmentOrdersEntity ordersEntityOld : ordersEntityList) {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(ordersEntityOld.getNxDepartmentOrdersId());
            // 仅当前台传入拣货员 ID 时覆盖，避免 null 冲掉库里的 nx_DO_pick_user_id，且保证 reduce 的 nxDgssrDoUserId 与订单一致
            if (ordersEntityOld.getNxDoPickUserId() != null) {
                ordersEntity.setNxDoPickUserId(ordersEntityOld.getNxDoPickUserId());
            }

            // 确定使用的重量：优先使用前台传入的重量，如果为空则使用订单数量
            String weightToUse = null;
            if (ordersEntityOld.getNxDoWeight() != null && !ordersEntityOld.getNxDoWeight().trim().isEmpty()) {
                weightToUse = ordersEntityOld.getNxDoWeight();
                logger.info("[giveOrderWeightListForStockShelfGoods] 使用前台传入的重量: {}", weightToUse);
            } else if (ordersEntity.getNxDoQuantity() != null && !ordersEntity.getNxDoQuantity().trim().isEmpty()) {
                // 如果前台没有传入重量，使用订单数量作为重量
                weightToUse = ordersEntity.getNxDoQuantity();
                logger.warn("[giveOrderWeightListForStockShelfGoods] 前台未传入重量，使用订单数量作为重量: {}", weightToUse);
            } else {
                // 如果都没有，使用数据库中的重量（但记录警告）
                weightToUse = ordersEntity.getNxDoWeight();
                logger.warn("[giveOrderWeightListForStockShelfGoods] 前台未传入重量且订单数量为空，使用数据库中的重量: {} (订单ID: {})",
                        weightToUse, ordersEntity.getNxDepartmentOrdersId());
            }

            // 记录调试信息
            logger.info("[giveOrderWeightListForStockShelfGoods] 订单ID: {}, 订单数量: {}, 数据库重量: {}, 前台传入重量: {}, 最终使用重量: {}",
                    ordersEntity.getNxDepartmentOrdersId(),
                    ordersEntity.getNxDoQuantity(),
                    ordersEntity.getNxDoWeight(),
                    ordersEntityOld.getNxDoWeight(),
                    weightToUse);

            // 无论价格是否为空，都要设置正确的重量
            ordersEntity.setNxDoWeight(weightToUse);
            System.out.println("ordersEntityprriceeieiei123" + ordersEntity.getNxDoPrice());

            // 单价仍为占位 0.1 时：按旧逻辑重算小计、完成态与利润（与真实单价订单无关）
            if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty() && !ordersEntity.getNxDoPrice().trim().equals("0.1")) {
                String priceRaw = ordersEntity.getNxDoPrice();
                String weightRaw = weightToUse;
                try {
                    String priceTrim = priceRaw.trim();
                    String weightTrim = weightRaw == null ? null : weightRaw.trim();
                    BigDecimal subtotal = new BigDecimal(priceTrim).multiply(new BigDecimal(weightTrim)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoSubtotal(subtotal.toString());
                    ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
                    System.out.println("subtoatllslslsls" + subtotal);
                    if (ordersEntity.getNxDoCostPrice() != null && !ordersEntity.getNxDoCostPrice().trim().isEmpty()) {
                        BigDecimal nxDoCostPriceB = new BigDecimal(ordersEntity.getNxDoCostPrice().trim());
                        BigDecimal decimal = nxDoCostPriceB.multiply(new BigDecimal(weightTrim)).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal profitB = subtotal.subtract(decimal).setScale(1, BigDecimal.ROUND_HALF_UP);
                        System.out.println("subsbsbbssbbsbbbbb" + subtotal);
                        BigDecimal scaleB = profitB.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                        ordersEntity.setNxDoProfitScale(scaleB.toString());
                        ordersEntity.setNxDoProfitSubtotal(profitB.toString());
                    }
                    System.out.println("subsbbsbsbbsbs" + subtotal.toString());
                    System.out.println("subsbbsbsbbsbs" + ordersEntity.getNxDoCostPrice());
                } catch (Exception e) {
                    logger.error("[giveOrderWeightListForStockShelfGoods] 计算小计失败, orderId={}, priceRaw='{}'(len={}), weightRaw='{}'(len={}), error={}",
                            ordersEntity.getNxDepartmentOrdersId(),
                            priceRaw, priceRaw == null ? null : priceRaw.length(),
                            weightRaw, weightRaw == null ? null : weightRaw.length(),
                            e.getMessage(), e);
                }
            }

            // 货架 FIFO 扣减：与单价是否为 0.1 无关（原先误写在 0.1 分支内，导致正常价订单永不扣库存）
            if (ordersEntity.getNxDoDisGoodsId() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsId", ordersEntity.getNxDoDisGoodsId());
                map.put("restWeight", 0);

                if (ordersEntityOld.getOutShelfId() != null) {
                    map.put("shelfId", ordersEntityOld.getOutShelfId());
                    System.out.println("🔍 查询指定货架的库存 - 货架ID: " + ordersEntityOld.getOutShelfId());
                }
                List<NxDistributerGoodsShelfStockEntity> stockEntities = nxDisGoodsShelfStockService.queryShelfStockListByParams(map);

                if (stockEntities.size() > 0) {
                    NxDistributerGoodsEntity disGoodsForOut = nxDistributerGoodsService.queryObject(ordersEntity.getNxDoDisGoodsId());
                    boolean useCartonPrice = false;
                    BigDecimal itemsPerCartonBdForOut = null;
                    if (disGoodsForOut != null
                            && disGoodsForOut.getNxDgWillPriceTwoStandard() != null
                            && !disGoodsForOut.getNxDgWillPriceTwoStandard().trim().isEmpty()
                            && ordersEntity.getNxDoPrintStandard() != null
                            && ordersEntity.getNxDoPrintStandard().trim().equals(disGoodsForOut.getNxDgWillPriceTwoStandard().trim())) {
                        itemsPerCartonBdForOut = CommonUtils.parseItemsPerCartonBigDecimal(disGoodsForOut.getNxDgItemsPerCarton());
                        if (itemsPerCartonBdForOut != null && itemsPerCartonBdForOut.compareTo(BigDecimal.ZERO) > 0) {
                            useCartonPrice = true;
                            System.out.println("✅ 打印规格与二档销售规格一致，出库按箱折算最小单位扣减 FIFO: "
                                    + disGoodsForOut.getNxDgWillPriceTwoStandard()
                                    + ", 每件数 " + itemsPerCartonBdForOut.toPlainString());
                        } else {
                            System.out.println("⚠️ 二档规格匹配但每箱数量无效，按最小单位扣减");
                        }
                    } else {
                        System.out.println("⚠️ 非二档或大包装折算 - printStandard: " + ordersEntity.getNxDoPrintStandard()
                                + ", 二档规格: " + (disGoodsForOut != null ? disGoodsForOut.getNxDgWillPriceTwoStandard() : "null"));
                    }

                    // 需要出货的重量（使用确定的重量值）
                    BigDecimal needWeightRaw = new BigDecimal(weightToUse);
                    BigDecimal needWeight; // 转换为最小单位后的重量
                    BigDecimal totalCost = BigDecimal.ZERO; // 总成本

                    System.out.println("🔍 调试信息 - 前台提交的重量: " + needWeightRaw + ", 订单规格: " + ordersEntity.getNxDoStandard());
                    System.out.println("🔍 调试信息 - useCartonPrice: " + useCartonPrice + ", itemsPerCartonBdForOut: " + itemsPerCartonBdForOut);

                    // 如果使用大包装单位，需要将箱数转换为最小单位数量
                    if (useCartonPrice && itemsPerCartonBdForOut != null) {
                        // 前台提交的是箱数，需要转换为最小单位数量
                        needWeight = needWeightRaw.multiply(itemsPerCartonBdForOut);
                        System.out.println("✅ 开始FIFO扣减库存 - 订单箱数: " + needWeightRaw + "箱, 转换为最小单位: " + needWeight + "个");
                    } else {
                        // 前台提交的已经是最小单位数量
                        needWeight = needWeightRaw;
                        System.out.println("✅ 开始FIFO扣减库存 - 订单重量: " + needWeight + "个（未使用大包装单位）");
                    }

                    BigDecimal remainingWeight = needWeight; // 剩余需要扣减的重量（最小单位）
                    System.out.println("🔍 调试信息 - remainingWeight（剩余需要扣减）: " + remainingWeight + "个");

                    for (NxDistributerGoodsShelfStockEntity stockEntity : stockEntities) {
                        if (remainingWeight.compareTo(BigDecimal.ZERO) <= 0) {
                            break; // 已经扣减完毕
                        }

                        BigDecimal stockRestWeight = new BigDecimal(stockEntity.getNxDgssRestWeight());

                        // 计算本批次可以扣减的重量（最小单位）
                        BigDecimal deductWeight;
                        if (remainingWeight.compareTo(stockRestWeight) <= 0) {
                            // 本批次够用
                            deductWeight = remainingWeight;
                        } else {
                            // 本批次不够，全部扣减
                            deductWeight = stockRestWeight;
                        }

                        // 根据出库单位选择对应的单价和计算方式
                        BigDecimal stockPrice;
                        BigDecimal deductWeightForCalc; // 用于成本计算的扣减数量
                        if (useCartonPrice && stockEntity.getNxDgssPriceCarton() != null
                                && !stockEntity.getNxDgssPriceCarton().trim().isEmpty()
                                && itemsPerCartonBdForOut != null) {
                            // 使用外包装单价，需要将最小单位数量转换为箱数
                            deductWeightForCalc = deductWeight.divide(itemsPerCartonBdForOut, 4, BigDecimal.ROUND_HALF_UP);
                            stockPrice = new BigDecimal(stockEntity.getNxDgssPriceCarton());
                            System.out.println("批次ID: " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                    ", 剩余: " + stockRestWeight + ", 扣减: " + deductWeight + "个(" + deductWeightForCalc + "箱)"
                                    + ", 使用外包装单价: " + stockPrice + "元/箱");
                        } else {
                            // 使用最小单位单价
                            String priceStr = stockEntity.getNxDgssPrice();
                            stockPrice = (priceStr == null || priceStr.trim().isEmpty())
                                    ? BigDecimal.ZERO
                                    : new BigDecimal(priceStr);
                            deductWeightForCalc = deductWeight;
                            System.out.println("批次ID: " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                    ", 剩余: " + stockRestWeight + ", 扣减: " + deductWeight
                                    + ", 使用最小单位单价: " + stockPrice);
                        }

                        if (stockRestWeight.compareTo(BigDecimal.ZERO) <= 0) {
                            continue; // 跳过没有库存的批次
                        }

                        // 计算本批次的成本（使用对应的单价和数量）
                        BigDecimal batchCost = deductWeightForCalc.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        totalCost = totalCost.add(batchCost);

                        // 更新库存批次的剩余数量
                        BigDecimal newRestWeight = stockRestWeight.subtract(deductWeight);
                        stockEntity.setNxDgssRestWeight(newRestWeight.toString());
                        setFifoNewRestSubtotalAfterDeduct(stockEntity, stockRestWeight, newRestWeight);

                        // 保存库存批次更新
                        nxDisGoodsShelfStockService.update(stockEntity);

                        // 创建库存扣减记录
                        NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
                        reduceEntity.setNxDgssrNxDistributerId(ordersEntity.getNxDoDistributerId());
                        reduceEntity.setNxDgssrNxDisGoodsId(ordersEntity.getNxDoDisGoodsId());
                        reduceEntity.setNxDgssrNxDisGoodsFatherId(stockEntity.getNxDgssNxDisGoodsFatherId());
                        reduceEntity.setNxDgssrNxStockId(stockEntity.getNxDistributerGoodsShelfStockId());
                        reduceEntity.setNxDgssrDate(formatWhatDay(0));
                        reduceEntity.setNxDgssrWeek(getWeek(0));
                        reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
                        reduceEntity.setNxDgssrFullTime(formatWhatYearDayTime(0));
                        reduceEntity.setNxDgssrType(0); // 0=销售扣减
                        reduceEntity.setNxDgssrDoUserId(ordersEntity.getNxDoPickUserId());
                        reduceEntity.setNxDgssrCostWeight(deductWeight.toString());
                        reduceEntity.setNxDgssrCostSubtotal(batchCost.toString());
                        reduceEntity.setNxDgssrNxPurGoodsId(stockEntity.getNxDgssNxPurGoodsId());
                        reduceEntity.setNxDgssrNxDepOrderId(ordersEntity.getNxDepartmentOrdersId());
                        reduceEntity.setNxDgssrStatus(0);
                        reduceEntity.setNxDgssrProduceWeight(deductWeight.toString());
                        reduceEntity.setNxDgssrProduceSubtotal(batchCost.toString());
                        nxDisGoodsShelfStockReduceService.save(reduceEntity);

                        System.out.println("扣减批次 " + stockEntity.getNxDistributerGoodsShelfStockId() +
                                " - 扣减重量: " + deductWeight + ", 本批次成本: " + batchCost +
                                ", 新剩余: " + newRestWeight + ", 已保存扣减记录");

                        // 减少剩余需要扣减的重量
                        remainingWeight = remainingWeight.subtract(deductWeight);
                    }

                    // 计算加权平均成本价（保留1位小数）；needWeight 为换算后的最小单位总量
                    if (needWeight.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal avgCostPrice = totalCost.divide(needWeight, 1, BigDecimal.ROUND_HALF_UP);
                        ordersEntity.setNxDoCostPrice(avgCostPrice.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                        ordersEntity.setNxDoCostSubtotal(totalCost.toString());

                        if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                            BigDecimal sellPrice = new BigDecimal(ordersEntity.getNxDoPrice());
                            BigDecimal profit = sellPrice.subtract(avgCostPrice);
                            BigDecimal profitSubtotal = profit.multiply(needWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                            ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());
                            System.out.println("FIFO扣减完成 - 总成本: " + totalCost +
                                    ", 平均成本价: " + avgCostPrice +
                                    ", 利润小计: " + profitSubtotal);
                        } else {
                            System.out.println("FIFO扣减完成 - 总成本: " + totalCost +
                                    ", 平均成本价: " + avgCostPrice + "（无销售单价，未重算利润小计）");
                        }
                    } else {
                        logger.warn("[giveOrderWeightListForStockShelfGoods] needWeight 为 0，跳过平均成本 orderId={}",
                                ordersEntity.getNxDepartmentOrdersId());
                    }

                    if (remainingWeight.compareTo(BigDecimal.ZERO) > 0) {
                        System.out.println("警告：库存不足，还需要: " + remainingWeight);
                        Map<String, Object> shortage = new HashMap<>();
                        shortage.put("orderId", ordersEntity.getNxDepartmentOrdersId());
                        shortage.put("shortage", remainingWeight);
                        shortages.add(shortage);
                    }
                }
            }

            ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
            nxDepartmentOrdersService.update(ordersEntity);

            Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
            if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
                if (purchaseGoodsEntity != null) {
                    Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                    Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                    System.out.println("purGoodsss" + nxDpgFinishAmount);
                    if (nxDpgOrdersAmount != null && nxDpgFinishAmount != null) {
                        if (nxDpgOrdersAmount - nxDpgFinishAmount == 1) {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgOrdersAmount);
                            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                            //查询商品如有有采购批次nxDistributerPurchaseBatch
                            // 并且该批次下的全部采购商品nxDistributerGoods的 nxDpgStatus == getNxDisPurchaseGoodsFinishBuy 的数量
                            //是所有采购商品数量减去 1 ，那么就认为该采购批次也是完成。
                            System.out.println("gengixathciid" + purchaseGoodsEntity.getNxDpgBatchId());
                            if (purchaseGoodsEntity.getNxDpgBatchId() != null && purchaseGoodsEntity.getNxDpgBatchId() != -1) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("batchId", purchaseGoodsEntity.getNxDpgBatchId());
                                int count = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                                map.put("equalStatus", getNxDisPurchaseGoodsFinishBuy());
                                int finishCount = nxDistributerPurchaseGoodsService.queryPurchaseGoodsCount(map);
                                System.out.println("fincoundfdafdafadfat" + finishCount);
                                if (count - finishCount == 1) {
                                    NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDistributerPurchaseBatchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());
                                    nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
                                    nxDistributerPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);
                                }
                            }

                        } else {
                            purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount + 1);
                        }
                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                } else {
                    System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
                }
            }

            System.out.println("xnordidiididid" + ordersEntity.getNxDoGbDepartmentOrderId());
            if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(ordersEntity.getNxDepartmentOrdersId());
                gbDepartmentOrdersEntity.setGbDoBuyStatus(getGbOrderBuyStatusHasWeightAndPrice());
                gbDepartmentOrdersEntity.setGbDoStatus(getGbOrderStatusProcurement());
                gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());

                if (gbDepartmentOrdersEntity.getGbDoPrice() != null && !gbDepartmentOrdersEntity.getGbDoPrice().trim().isEmpty() && !gbDepartmentOrdersEntity.getGbDoPrice().equals("0.0") && gbDepartmentOrdersEntity.getGbDoWeight() != null) {
                    BigDecimal decimal1 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoPrice());
                    BigDecimal decimal2 = new BigDecimal(gbDepartmentOrdersEntity.getGbDoWeight());
                    BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                    gbDepartmentOrdersEntity.setGbDoSubtotal(decimal3.toString());
                }
                gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
                System.out.println("xnordidiididid" + gbDepartmentOrdersEntity.getGbDepartmentOrdersId());


                Integer purchaseGoodsId = gbDepartmentOrdersEntity.getGbDoPurchaseGoodsId();

                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", purchaseGoodsId);
                List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
                BigDecimal purWeight = new BigDecimal(0);
                if (gbDepartmentOrdersEntities.size() > 0) {
                    for (GbDepartmentOrdersEntity gbOrder : gbDepartmentOrdersEntities) {
                        if (gbOrder.getGbDoWeight() != null) {
                            purWeight = purWeight.add(new BigDecimal(gbOrder.getGbDoWeight()));
                        }
                    }
                }
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
                purchaseGoodsEntity.setGbDpgBuyQuantity(purWeight.toString());
                if (purchaseGoodsEntity.getGbDpgBuyPrice() != null && !purchaseGoodsEntity.getGbDpgBuyPrice().trim().isEmpty()) {
                    BigDecimal decimal1 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyPrice());
                    BigDecimal decimal2 = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
                    BigDecimal decimal3 = decimal1.multiply(decimal2).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal3.toString());
                }
                purchaseGoodsEntity.setGbDpgStatus(getNxDisPurchaseGoodsIsPurchase());
                System.out.println("purpprur" + purchaseGoodsEntity);
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

            }


            //更新协作订单
            System.out.println("colllllmap" + ordersEntity.getNxDoCollaborativeNxDisId());

            if(ordersEntity.getNxDoCollaborativeNxDisId() != -1){
                Integer nxDoCollaborativeNxDisId = ordersEntity.getNxDoCollaborativeNxDisId();
                Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
                Map<String, Object> map = new HashMap<>();
                map.put("disId", nxDoCollaborativeNxDisId);
                map.put("restrauntId", nxDepartmentOrdersId);
                System.out.println("colllllmap" + map);
                NxDepartmentOrdersEntity collOrder =  nxDepartmentOrdersService.querycollOrder(map);
                if(collOrder != null && collOrder.getNxDoStatus() < 2){
                    collOrder.setNxDoPurchaseStatus(1);
                    collOrder.setNxDoWeight(weightToUse);
                    //
                    if (collOrder.getNxDoPrice() != null && !collOrder.getNxDoPrice().trim().isEmpty()
                            && ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
                        BigDecimal costSubtotal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(weightToUse)).setScale(1, BigDecimal.ROUND_HALF_UP);

                        BigDecimal subtotal = new BigDecimal(collOrder.getNxDoPrice()).multiply(new BigDecimal(weightToUse)).setScale(1, BigDecimal.ROUND_HALF_UP);
                        collOrder.setNxDoCostPrice(ordersEntity.getNxDoPrice());
                        collOrder.setNxDoCostSubtotal(costSubtotal.toString());
                        collOrder.setNxDoSubtotal(subtotal.toString());
                        System.out.println("subtollllll=" + subtotal + "costsubtallalal==" + costSubtotal);
                        BigDecimal profitSubtotal = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);

                        collOrder.setNxDoProfitSubtotal(profitSubtotal.toString());
                        BigDecimal multiply = subtotal.compareTo(BigDecimal.ZERO) != 0
                                ? profitSubtotal.divide(subtotal, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP)
                                : BigDecimal.ZERO;
                        collOrder.setNxDoProfitScale(multiply.toString());

                    }
                        nxDepartmentOrdersService.update(collOrder);
                }
            }

        }

        return R.ok().put("shortages", shortages);
    }


    @RequestMapping(value = "/giveOrderWeightListForStock", method = RequestMethod.POST)
    @ResponseBody
    public R giveOrderWeightListForStock(@RequestBody NxDepartmentOrdersEntity ordersEntity) {
        List<NxDistributerGoodsShelfStockEntity> outStockDisGoodsShelfStockEntities = ordersEntity.getOutStockDisGoodsShelfStockEntities();
        BigDecimal costSubtotal = new BigDecimal(0);
        for (NxDistributerGoodsShelfStockEntity stockEntity : outStockDisGoodsShelfStockEntities) {
            String nxDgssInventoryWeight = stockEntity.getNxDgssInventoryWeight();
            String inventeryPrice = stockEntity.getNxDgssPrice();
            costSubtotal = costSubtotal.add(new BigDecimal(inventeryPrice).multiply(new BigDecimal(nxDgssInventoryWeight)).setScale(1, BigDecimal.ROUND_HALF_UP));
            System.out.println("restseee" + stockEntity.getNxDgssRestWeight() + "sub==" + stockEntity.getNxDgssRestSubtotal());
            nxDisGoodsShelfStockService.update(stockEntity);

            NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
            reduceEntity.setNxDgssrDate(formatWhatDay(0));
            reduceEntity.setNxDgssrNxDepOrderId(ordersEntity.getNxDepartmentOrdersId());
            reduceEntity.setNxDgssrCostWeight(stockEntity.getNxDgssInventoryWeight());
            reduceEntity.setNxDgssrCostSubtotal(costSubtotal.toString());
            reduceEntity.setNxDgssrNxDistributerId(ordersEntity.getNxDoDistributerId());
            reduceEntity.setNxDgssrNxDisGoodsId(stockEntity.getNxDgssNxDisGoodsId());
            reduceEntity.setNxDgssrGoodsInventoryType(0);
            reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
            reduceEntity.setNxDgssrFullTime(formatFullTime());
            reduceEntity.setNxDgssrNxStockId(stockEntity.getNxDistributerGoodsShelfStockId());
            reduceEntity.setNxDgssrType(0);// 0=销售扣减
            reduceEntity.setNxDgssrNxPurGoodsId(stockEntity.getNxDgssNxPurGoodsId());
            reduceEntity.setNxDgssrStatus(0);
            reduceEntity.setNxDgssrNxDisGoodsFatherId(stockEntity.getNxDgssNxDisGoodsFatherId());

            nxDisGoodsShelfStockReduceService.save(reduceEntity);
        }

        ordersEntity.setNxDoCostSubtotal(costSubtotal.toString());
        BigDecimal perPrice = costSubtotal.multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxDoCostPrice(perPrice.toString());
        if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty() && new BigDecimal(ordersEntity.getNxDoPrice()).compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal subtotal = new BigDecimal(ordersEntity.getNxDoPrice()).multiply(new BigDecimal(ordersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoSubtotal(subtotal.toString());
            ordersEntity.setNxDoStatus(getNxOrderStatusHasFinished());
            BigDecimal profitSubtotal = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
            ordersEntity.setNxDoProfitSubtotal(profitSubtotal.toString());
            BigDecimal multiply = profitSubtotal.divide(subtotal).setScale(2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            ordersEntity.setNxDoProfitScale(multiply.toString());

        } else {
            ordersEntity.setNxDoSubtotal("0");
            ordersEntity.setNxDoProfitSubtotal("0");
            ordersEntity.setNxDoProfitScale("0");
        }

        System.out.println("orderreeeequant" + ordersEntity.getNxDoWeight() + "sutl" + ordersEntity.getNxDoSubtotal());

        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishOut());
        nxDepartmentOrdersService.update(ordersEntity);

        return R.ok();
    }


    @RequestMapping(value = "/disGetToWeightOrders", method = RequestMethod.POST)
    @ResponseBody
    public R disGetToWeightOrders(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("equalStatus", 0);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        map.put("purGoodsId", -1);
        map.put("weightId", 0);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntitiesWeightun = nxDepartmentOrdersService.queryDepOrdersOrderFatherGoods(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("date", formatWhatDay(0));
        map3.put("type", 1);
        int count = nxDistributerWeightService.queryWeightCountByParams(map3);
        BigDecimal trade = new BigDecimal(count).add(new BigDecimal(1));
        String s = formatDayNumber(0) + "JHD" + trade;

        Map<String, Object> map11 = new HashMap<>();
        map11.put("tradeNo", s);
        map11.put("arr", fatherGoodsEntitiesWeightun);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("depFatherId", depFatherId);
        mapR.put("equalStatus", 0);
        int count1 = nxDistributerWeightService.queryWeightCountByParams(mapR);
        map11.put("weightCount", count1);
        return R.ok().put("data", map11);
    }


    @RequestMapping(value = "/getHaveWeightDepOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getHaveWeightDepOrders(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);

        return R.ok().put("data", ordersEntities);
    }

    /**
     * 9-11
     * DISTRIBUTER
     * 获取需要填写数量和价格的订单
     *
     * @param depFatherId 群id
     * @return 订单
     */
    @RequestMapping(value = "/getToFillDepOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getToFillDepOrders(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {
        System.out.println("getToFillDepOrdersgetToFillDepOrders" + depFatherId + gbDepFatherId + resFatherId + disId);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        System.out.println("abncnncnnnc" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        System.out.println("disnamme" + nxDistributerEntity.getNxDistributerName());
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        List<Map<String, Object>> mapList = new ArrayList<>();

        if (depFatherId != -1) {
            List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
            System.out.println("depdiidid" + entities.size());
            if (entities.size() > 0) {
                for (NxDepartmentEntity dep : entities) {
                    Map<String, Object> mapDep = new HashMap<>();
                    mapDep.put("disId", disId);
                    mapDep.put("depId", dep.getNxDepartmentId());
                    mapDep.put("depName", dep.getNxDepartmentName());
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("disId", disId);
                    map1.put("status", 3);
                    map1.put("depId", dep.getNxDepartmentId());
                    map1.put("orderBy", "time");
                    List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                    mapDep.put("depOrders", depOrders);
                    map1.put("subtotal", 0);
                    System.out.println("map111aaa" + map1);
                    Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                    Double sutotal = 0.0;
                    if (integer > 0) {
                        sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                    }
                    mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                    mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                    mapList.add(mapDep);
                }

                Map<String, Object> mapR = new HashMap<>();
                mapR.put("subAmount", entities.size());
                mapR.put("arr", mapList);
                mapR.put("tradeNo", s);
                double total = 0.0;
                map.put("subtotal", 0);
                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                if (twoTotal > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                }

                mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
                mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

                return R.ok().put("data", mapR);
            } else {
                Map<String, Object> mapR = new HashMap<>();
                double total = 0.0;
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("disId", disId);
                mapS.put("status", 3);
                mapS.put("depFatherId", depFatherId);
                mapS.put("gbDepFatherId", gbDepFatherId);
                mapS.put("resFatherId", resFatherId);
                mapS.put("subtotal", 0);
                System.out.println("tototototdddddddd" + mapS);
                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
                if (twoTotal > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapS);
                }
                mapR.put("subAmount", 0);
                mapR.put("arr", ordersEntities);
                mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
                mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapR.put("tradeNo", s);

                return R.ok().put("data", mapR);
            }


        } else if (gbDepFatherId != -1) {
            List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(gbDepFatherId);
            System.out.println("depdiididgbDepFatherIdgbDepFatherId" + entities.size());
            if (entities.size() > 0) {
                for (GbDepartmentEntity dep : entities) {
                    Map<String, Object> mapDep = new HashMap<>();
                    mapDep.put("disId", disId);
                    mapDep.put("gbDepId", dep.getGbDepartmentId());
                    mapDep.put("depName", dep.getGbDepartmentName());
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("disId", disId);
                    map1.put("status", 3);
                    map1.put("gbDepId", dep.getGbDepartmentId());
                    map1.put("orderBy", "time");
                    List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                    mapDep.put("depOrders", depOrders);
                    map1.put("subtotal", 0);
                    System.out.println("map111aaa" + map1);
                    Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                    Double sutotal = 0.0;
                    if (integer > 0) {
                        sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                    }
                    mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                    mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                    mapList.add(mapDep);
                }

                Map<String, Object> mapR = new HashMap<>();
                mapR.put("subAmount", entities.size());
                mapR.put("arr", mapList);
                mapR.put("tradeNo", s);
                double total = 0.0;
                map.put("subtotal", 0);
                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                if (twoTotal > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                }

                mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
                mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

                return R.ok().put("data", mapR);
            } else {
                Map<String, Object> mapR = new HashMap<>();
                double total = 0.0;
                Map<String, Object> mapS = new HashMap<>();
                mapS.put("disId", disId);
                mapS.put("status", 3);
                mapS.put("depFatherId", depFatherId);
                mapS.put("gbDepFatherId", gbDepFatherId);
                mapS.put("resFatherId", resFatherId);
                mapS.put("subtotal", 0);
                System.out.println("tototototdddddddd" + mapS);
                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
                if (twoTotal > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapS);
                }
                mapR.put("subAmount", 0);
                mapR.put("arr", ordersEntities);
                mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
                mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapR.put("tradeNo", s);

                return R.ok().put("data", mapR);
            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/getCollectionDisDepOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getCollectionDisDepOrders(Integer collDisId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("collNxDisId", collDisId);
        System.out.println("depmapapapppa" + map);
        List<NxDepartmentEntity> deps = nxDepartmentOrdersService.queryCollDisDeps(map);
        System.out.println("depmapapapppasize" + deps.size());

        // 优化：合并统计查询，减少数据库查询次数
        int totalCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        // 优化：合并统计查询，减少数据库查询次数
        map.put("hasPrice", 1);
        int hasPriceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("hasPrice", null);
        map.put("hasWeight", 1);
        int hasWeightCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        map.put("status", null);
        map.put("equalStatus", 2);
        map.put("dayuPurStatus", 3);
        int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();

        List<Map<String, Object>> mapList = new ArrayList<>();
        for (NxDepartmentEntity dep : deps) {
            Map<String, Object> mapDepQuery = new HashMap<>();
            mapDepQuery.put("depFatherId", dep.getNxDepartmentId());
            mapDepQuery.put("disId", disId);
            mapDepQuery.put("status", 3);
            mapDepQuery.put("collNxDisId", collDisId);
            // 使用简化版DTO查询，只查询必要字段
            System.out.println("queyryyrryryr" + mapDepQuery);
            List<NxDepartmentOrdersSimpleDTO> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersSimpleByParams(mapDepQuery);
            Map<String, Object> mapDep = new HashMap<>();

            mapDep.put("depOrders", ordersEntities);
            mapDep.put("depName", dep.getNxDepartmentOrderCode());
            mapDep.put("depId", dep.getNxDepartmentId());

            // 计算该部门的小计
            Map<String, Object> map1 = new HashMap<>();
            map1.put("status", 3);
            map1.put("depId", dep.getNxDepartmentId());
            map1.put("subtotal", 0);
            map1.put("collNxDisId", collDisId);
            Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
            Double sutotal = 0.0;
            if (integer > 0) {
                sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
            }
            mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
            mapList.add(mapDep);
        }

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("subAmount", deps.size());
        mapR.put("arr", mapList);
        mapR.put("tradeNo", s);
        double total = 0.0;
        map.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }

        mapR.put("totalCount", totalCount );
        mapR.put("finishCount", finishCount);
        mapR.put("hasPriceCount", hasPriceCount);
        mapR.put("hasWeightCount", hasWeightCount);
        mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

        return R.ok().put("data", mapR);

    }

  @RequestMapping(value = "/getCollectionDisOrders", method = RequestMethod.POST)
    @ResponseBody
    public R getCollectionDisOrders(Integer collDisId, Integer disId) {


        Map<String, Object> mapDepQuery = new HashMap<>();
        mapDepQuery.put("disId", disId);
        mapDepQuery.put("status", 3);
        mapDepQuery.put("collNxDisId", collDisId);
        // 使用简化版DTO查询，只查询必要字段
        System.out.println("queyryyrryryr" + mapDepQuery);
        List<NxDepartmentOrdersSimpleDTO> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersSimpleByParams(mapDepQuery);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("collNxDisId", collDisId);
        System.out.println("depmapapapppa" + map);

        // 优化：合并统计查询，减少数据库查询次数
        int totalCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        // 优化：合并统计查询，减少数据库查询次数
        map.put("hasPrice", 1);
        int hasPriceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("hasPrice", null);
        map.put("hasWeight", 1);
        int hasWeightCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        map.put("status", null);
        map.put("equalStatus", 2);
        map.put("dayuPurStatus", 3);
        int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        Map<String, Object> mapR = new HashMap<>();
        double total = 0.0;
        map.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }

        mapR.put("arr", ordersEntities );
        mapR.put("totalCount", totalCount );
        mapR.put("finishCount", finishCount);
        mapR.put("hasPriceCount", hasPriceCount);
        mapR.put("hasWeightCount", hasWeightCount);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/getCollectionDisDepOrdersFinish", method = RequestMethod.POST)
    @ResponseBody
    public R getCollectionDisDepOrdersFinish(Integer collDisId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("equalStatus", 2);
        map.put("equalPurStatus", 4);
        map.put("collNxDisId", collDisId);
        System.out.println("depmapapapppa" + map);

        List<NxDistributerGoodsEntity> goodsEntities = nxDepartmentOrdersService.queryOfferOrdersGoods(map);

        // 优化：合并统计查询，减少数据库查询次数
        int totalCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        // 优化：合并统计查询，减少数据库查询次数
        map.put("hasPrice", 1);
        int hasPriceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("hasPrice", null);
        map.put("hasWeight", 1);
        int hasWeightCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        map.put("status", null);
        map.put("equalStatus", 2);
        map.put("dayuPurStatus", 3);
        int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("tradeNo", s);
        double total = 0.0;
        map.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }

        mapR.put("arr", goodsEntities );
        mapR.put("totalCount", totalCount );
        mapR.put("finishCount", finishCount);
        mapR.put("hasPriceCount", hasPriceCount);
        mapR.put("hasWeightCount", hasWeightCount);
        mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

        return R.ok().put("data", mapR);

    }


    /**
     * 配送商获取客户订单
     *
     * @param
     * @return
     * @date 2026-01-10
     */
    @RequestMapping(value = "/phoneGetToFillDepOrders", method = RequestMethod.POST)
    @ResponseBody
    public R phoneGetToFillDepOrders(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderDisId", disId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);

        // 使用简化版DTO查询，只查询必要字段
        List<NxDepartmentOrdersSimpleDTO> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersSimpleByParams(map);
        System.out.println("mapaappaaaaa"+ordersEntities.size());

        // 优化：合并统计查询，减少数据库查询次数
        map.put("hasPrice", 1);
        System.out.println("haspricieiee" + map);
        int hasPriceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("hasPrice", null);
        map.put("hasWeight", 1);
        int hasWeightCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("hasWeight", null);

        Map<String, Object> mapFinish = new HashMap<>();
        mapFinish.put("orderDisId", disId);
        mapFinish.put("depFatherId", depFatherId);
        mapFinish.put("gbDepFatherId", gbDepFatherId);
        mapFinish.put("resFatherId", resFatherId);
        mapFinish.put("equalStatus", 2);
        mapFinish.put("dayuPurStatus", 3);
        int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(mapFinish);

        if (disId == null) {
            return R.error("disId 不能为空");
        }
        System.out.println("phoneGetToFillDepOrders ① 即将 queryObject disId=" + disId);
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        System.out.println("phoneGetToFillDepOrders ③ queryObject 成功 nxDistributerId=" + nxDistributerEntity.getNxDistributerId());
        String disNameForPinyin = nxDistributerEntity.getNxDistributerName();
        if (disNameForPinyin == null || disNameForPinyin.trim().isEmpty()) {
            disNameForPinyin = "D" + disId;
        }
        String headPinyin = getHeadStringByString(disNameForPinyin, true, null);
        System.out.println("phoneGetToFillDepOrderssisyabcdetest1233 ⑤ getHeadStringByString 完成 headPinyin=" + headPinyin);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();

        System.out.println("depfaidiabcccdddjdjdabc" + depFatherId);
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);

        System.out.println("querySubDepartmentsquerySubDepartmentsaa" + entities.size());
        if (entities.size() > 0) {
            System.out.println("querySubDepartmentsquerySubDepartmentsaa00000" + entities.size());

            // 优化：批量查询所有子部门的订单，避免N+1问题
            List<Integer> depIds = entities.stream()
                    .map(NxDepartmentEntity::getNxDepartmentId)
                    .collect(Collectors.toList());

            Map<String, Object> batchMap = new HashMap<>();
            batchMap.put("orderDisId", disId);
            batchMap.put("status", 3);
            batchMap.put("depIds", depIds);
            List<NxDepartmentOrdersSimpleDTO> allDepOrders = nxDepartmentOrdersService.queryDisOrdersSimpleByDepIds(batchMap);

            // 按部门分组订单
            Map<Integer, List<NxDepartmentOrdersSimpleDTO>> ordersByDep = allDepOrders.stream()
                    .collect(Collectors.groupingBy(NxDepartmentOrdersSimpleDTO::getNxDoDepartmentId));

            List<Map<String, Object>> mapList = new ArrayList<>();
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());

                // 从批量查询结果中获取该部门的订单
                List<NxDepartmentOrdersSimpleDTO> depOrders = ordersByDep.getOrDefault(dep.getNxDepartmentId(), new ArrayList<>());
                mapDep.put("depOrders", depOrders);

                // 计算该部门的小计
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("subtotal", 0);
                map1.put("isSelfOrder", 1);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                Double sutotal = 0.0;
                if (integer > 0) {
                    sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                }
                mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                mapList.add(mapDep);
            }

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("subAmount", entities.size());
            mapR.put("arr", mapList);
            mapR.put("tradeNo", s);
            double total = 0.0;
            map.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }

            mapR.put("totalCount", ordersEntities.size());
            mapR.put("finishCount", finishCount);
            mapR.put("hasPriceCount", hasPriceCount);
            mapR.put("hasWeightCount", hasWeightCount);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

            return R.ok().put("data", mapR);
        } else {
            Map<String, Object> mapR = new HashMap<>();
            double total = 0.0;
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("status", 3);
            mapS.put("depFatherId", depFatherId);
            mapS.put("gbDepFatherId", gbDepFatherId);
            mapS.put("resFatherId", resFatherId);
            mapS.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapS);
            }
            mapR.put("subAmount", 0);
            mapR.put("arr", ordersEntities);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapR.put("tradeNo", s);
            mapR.put("finishCount", finishCount);
            mapR.put("hasPriceCount", hasPriceCount);
            mapR.put("hasWeightCount", hasWeightCount);
            mapR.put("totalCount", ordersEntities.size());
            return R.ok().put("data", mapR);
        }
    }



    /**
     * 配送商获取客户订单
     *
     * @param
     * @return
     * @date 2026-01-10
     */
    @RequestMapping(value = "/phoneGetToFillRetailOrders", method = RequestMethod.POST)
    @ResponseBody
    public R phoneGetToFillRetailOrders(Integer depFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
//        map.put("orderDisId", disId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        System.out.println("map111111subsususososoentities" + map);
        List<NxDepartmentEntity> entities = nxDepartmentOrdersService.queryRetailOrderNxDepartment(map);
        System.out.println("map111111subsususososoentitiessiziziziiziaaa" + entities.size());

        if (entities.size() > 0) {
            // 优化：批量查询所有子部门的订单，避免N+1问题
            List<Integer> depIds = entities.stream()
                    .map(NxDepartmentEntity::getNxDepartmentId)
                    .collect(Collectors.toList());

            Map<String, Object> batchMap = new HashMap<>();
            batchMap.put("orderDisId", disId);
            batchMap.put("status", 3);
            batchMap.put("depIds", depIds);
            List<NxDepartmentOrdersSimpleDTO> allDepOrders = nxDepartmentOrdersService.queryDisOrdersSimpleByDepIds(batchMap);

            // 按部门分组订单
            Map<Integer, List<NxDepartmentOrdersSimpleDTO>> ordersByDep = allDepOrders.stream()
                    .collect(Collectors.groupingBy(NxDepartmentOrdersSimpleDTO::getNxDoDepartmentId));

            List<Map<String, Object>> mapList = new ArrayList<>();
            for (NxDepartmentEntity dep : entities) {
                NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
                String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
                String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();

                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());

                // 从批量查询结果中获取该部门的订单
                List<NxDepartmentOrdersSimpleDTO> depOrders = ordersByDep.getOrDefault(dep.getNxDepartmentId(), new ArrayList<>());
                mapDep.put("depOrders", depOrders);

                // 计算该部门的小计
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("subtotal", 0);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                Double sutotal = 0.0;
                System.out.println("map111111subsususososointegerinteger" + integer);
                if (integer > 0) {
                    System.out.println("map111111subsususososo" + map1);

                    sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                    System.out.println("map111111subsususosososutotalsutotal" + sutotal);

                }
                mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                mapDep.put("depTradeNo", s);
                mapList.add(mapDep);
            }

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("subAmount", entities.size());
            mapR.put("arr", mapList);
            double total = 0.0;
            map.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

            return R.ok().put("data", mapR);
        }

        return R.error(-1,"没有订单");


    }

    @RequestMapping(value = "/phoneGetToFillDepOrdersSunla", method = RequestMethod.POST)
    @ResponseBody
    public R phoneGetToFillDepOrdersSunla(Integer depFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("dayuStatus", -1);
        map.put("purType", -1);
        map.put("depFatherId", depFatherId);
        System.out.println("abncnncnnnc" + map);
        List<NxDepartmentOrdersEntity> ordersEntitiesFirst = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
        map.put("purType", -2);
        List<NxDepartmentOrdersEntity> ordersEntitiesTwo = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
        Map<String, Object> mapR = new HashMap<>();

        mapR.put("oneArr", ordersEntitiesFirst);
        mapR.put("twoArr", ordersEntitiesTwo);

        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        System.out.println("depdiidid" + entities.size());
        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                mapDep.put("depOrders", depOrders);
                map1.put("subtotal", 0);
                System.out.println("map111aaa" + map1);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                Double sutotal = 0.0;
                if (integer > 0) {
                    sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                }
                mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                mapList.add(mapDep);
            }

            mapR.put("subAmount", entities.size());
            mapR.put("arr", mapList);

            return R.ok().put("data", mapR);
        } else {

            return R.ok().put("data", mapR);
        }
    }

    /**
     * 获取需要填写数量和价格的订单（带Kg转换）
     * 借鉴 phoneGetToFillDepOrders 接口，添加Kg单价转换功能
     * 如果商品规格是"斤"，则将单价转换为Kg单价（1斤=0.5Kg，所以1Kg=2斤，Kg单价=斤单价×2）
     */
    @RequestMapping(value = "/phoneGetToFillDepOrdersWithKg", method = RequestMethod.POST)
    @ResponseBody
    public R phoneGetToFillDepOrdersWithKg(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderDisId", disId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);

        // 先查询Entity进行转换和保存（需要修改数据库）
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
        List<NxDepartmentOrdersEntity> convertedOrders = convertOrdersPriceToKg(ordersEntities);
        saveOrders(convertedOrders);

        // 转换为DTO用于返回（减少数据传输）
//        List<NxDepartmentOrdersSimpleDTO> ordersDTOs = convertEntitiesToSimpleDTOs(ordersEntities);
        List<NxDepartmentOrdersSimpleDTO> ordersDTOs = nxDepartmentOrdersService.queryNotWeightDisOrdersSimpleByParams(map);



        map.put("hasPrice", 1);
        int hasPriceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("hasPrice", null);
        map.put("hasWeight", 1);
        int hasWeightCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        map.put("status", null);
        map.put("equalStatus", 2);
        map.put("dayuPurStatus", 3);
        int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();

        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        if (entities.size() > 0) {
            // 收集所有子部门的订单Entity（用于转换和保存）
            List<NxDepartmentOrdersEntity> allDepOrdersEntities = new ArrayList<>();
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                allDepOrdersEntities.addAll(depOrders);
            }

            // 批量转换订单的Kg单价和重量
            List<NxDepartmentOrdersEntity> convertedDepOrders = convertOrdersPriceToKg(allDepOrdersEntities);
            saveOrders(convertedDepOrders);

            // 转换为DTO
//            List<NxDepartmentOrdersSimpleDTO> allDepOrdersDTOs = convertEntitiesToSimpleDTOs(allDepOrdersEntities);
            List<NxDepartmentOrdersSimpleDTO> allDepOrdersDTOs = nxDepartmentOrdersService.queryNotWeightDisOrdersSimpleByParams(map);

            // 按部门分组订单
            Map<Integer, List<NxDepartmentOrdersSimpleDTO>> ordersByDep = allDepOrdersDTOs.stream()
                    .collect(Collectors.groupingBy(NxDepartmentOrdersSimpleDTO::getNxDoDepartmentId));

            List<Map<String, Object>> mapList = new ArrayList<>();
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());

                // 从批量查询结果中获取该部门的订单
                List<NxDepartmentOrdersSimpleDTO> depOrders = ordersByDep.getOrDefault(dep.getNxDepartmentId(), new ArrayList<>());
                mapDep.put("depOrders", depOrders);

                // 计算该部门的小计
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("subtotal", 0);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                Double sutotal = 0.0;
                if (integer > 0) {
                    sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                }
                mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                mapList.add(mapDep);
            }

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("subAmount", entities.size());
            mapR.put("arr", mapList);
            mapR.put("tradeNo", s);
            double total = 0.0;
            map.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }

            mapR.put("totalCount", ordersDTOs.size());
            mapR.put("finishCount", finishCount);
            mapR.put("hasPriceCount", hasPriceCount);
            mapR.put("hasWeightCount", hasWeightCount);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

            return R.ok().put("data", mapR);
        } else {
            Map<String, Object> mapR = new HashMap<>();
            double total = 0.0;
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("status", 3);
            mapS.put("depFatherId", depFatherId);
            mapS.put("gbDepFatherId", gbDepFatherId);
            mapS.put("resFatherId", resFatherId);
            mapS.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapS);
            }
            mapR.put("subAmount", 0);
            mapR.put("arr", ordersDTOs);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapR.put("tradeNo", s);
            mapR.put("finishCount", finishCount);
            mapR.put("hasPriceCount", hasPriceCount);
            mapR.put("hasWeightCount", hasWeightCount);
            mapR.put("totalCount", ordersDTOs.size());
            return R.ok().put("data", mapR);
        }
    }


    /**
     * 将订单的单价和重量从公斤转换为斤
     * 直接修改 nxDoPrice 和 nxDoWeight 字段
     *
     * @param depFatherId   部门父ID
     * @param gbDepFatherId GB部门父ID
     * @param resFatherId   餐厅父ID
     * @param disId         批发商ID
     * @return
     */
    @RequestMapping(value = "/phoneGetToFillDepOrdersWithJin", method = RequestMethod.POST)
    @ResponseBody
    public R phoneGetToFillDepOrdersWithJin(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderDisId", disId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);

        // 先查询Entity进行转换和保存（需要修改数据库）
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
        List<NxDepartmentOrdersEntity> convertedOrders = convertOrdersPriceToJin(ordersEntities);
        saveOrders(convertedOrders);

        // 转换为DTO用于返回（减少数据传输）
//        List<NxDepartmentOrdersSimpleDTO> ordersDTOs = convertEntitiesToSimpleDTOs(ordersEntities);
        List<NxDepartmentOrdersSimpleDTO> ordersDTOs = nxDepartmentOrdersService.queryNotWeightDisOrdersSimpleByParams(map);

        map.put("hasPrice", 1);
        int hasPriceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("hasPrice", null);
        map.put("hasWeight", 1);
        int hasWeightCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        map.put("status", null);
        map.put("equalStatus", 2);
        map.put("dayuPurStatus", 3);
        int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();

        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        if (entities.size() > 0) {
            // 收集所有子部门的订单Entity（用于转换和保存）
            List<NxDepartmentOrdersEntity> allDepOrdersEntities = new ArrayList<>();
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                allDepOrdersEntities.addAll(depOrders);
            }

            // 批量转换订单的斤单价和重量
            List<NxDepartmentOrdersEntity> convertedDepOrders = convertOrdersPriceToJin(allDepOrdersEntities);
            saveOrders(convertedDepOrders);

            // 转换为DTO
//            List<NxDepartmentOrdersSimpleDTO> allDepOrdersDTOs = convertEntitiesToSimpleDTOs(convertedDepOrders);
            List<NxDepartmentOrdersSimpleDTO> allDepOrdersDTOs = nxDepartmentOrdersService.queryNotWeightDisOrdersSimpleByParams(map);


            // 按部门分组订单
            Map<Integer, List<NxDepartmentOrdersSimpleDTO>> ordersByDep = allDepOrdersDTOs.stream()
                    .collect(Collectors.groupingBy(NxDepartmentOrdersSimpleDTO::getNxDoDepartmentId));

            List<Map<String, Object>> mapList = new ArrayList<>();
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());

                // 从批量查询结果中获取该部门的订单
                List<NxDepartmentOrdersSimpleDTO> depOrders = ordersByDep.getOrDefault(dep.getNxDepartmentId(), new ArrayList<>());
                mapDep.put("depOrders", depOrders);

                // 计算该部门的小计
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("subtotal", 0);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                Double sutotal = 0.0;
                if (integer > 0) {
                    sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                }
                mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                mapList.add(mapDep);
            }

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("subAmount", entities.size());
            mapR.put("arr", mapList);
            mapR.put("tradeNo", s);
            double total = 0.0;
            map.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }

            mapR.put("totalCount", ordersDTOs.size());
            mapR.put("finishCount", finishCount);
            mapR.put("hasPriceCount", hasPriceCount);
            mapR.put("hasWeightCount", hasWeightCount);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

            return R.ok().put("data", mapR);
        } else {
            Map<String, Object> mapR = new HashMap<>();
            double total = 0.0;
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("status", 3);
            mapS.put("disId", disId);
            mapS.put("depFatherId", depFatherId);
            mapS.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapS);
            }
            mapR.put("subAmount", 0);
            mapR.put("arr", ordersDTOs);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapR.put("tradeNo", s);
            mapR.put("finishCount", finishCount);
            mapR.put("hasPriceCount", hasPriceCount);
            mapR.put("hasWeightCount", hasWeightCount);
            mapR.put("totalCount", ordersDTOs.size());
            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/phoneGetToFillDepOrdersGb", method = RequestMethod.POST)
    @ResponseBody
    public R phoneGetToFillDepOrdersGb(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        System.out.println("abncnncnnnc" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderGb(map);

        map.put("hasPrice", 1);
        System.out.println("hasprice=" + map);
        int hasPriceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        map.put("hasPrice", null);
        map.put("hasWeight", 1);
        int hasWeightCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        map.put("dayuPurStatus", 3);
        map.put("hasPrice", 1);
        System.out.println("finfidiidididid" + map);
        int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        System.out.println("disnamme" + nxDistributerEntity.getNxDistributerName());
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<GbDepartmentEntity> entities = gbDepartmentService.querySubDepartments(gbDepFatherId);
        System.out.println("depdiidid" + entities.size());
        if (entities.size() > 0) {
            for (GbDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("gbDepId", dep.getGbDepartmentId());
                mapDep.put("depName", dep.getGbDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("gbDepId", dep.getGbDepartmentId());
                map1.put("orderBy", "time");
                map1.put("disId", disId);
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDepWeightOrderGb(map1);
                mapDep.put("depOrders", depOrders);
                map1.put("subtotal", 0);
                System.out.println("map111aaa" + map1);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                Double sutotal = 0.0;
                if (integer > 0) {
                    sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                }
                mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                mapList.add(mapDep);
            }

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("subAmount", entities.size());
            mapR.put("arr", mapList);
            mapR.put("tradeNo", s);
            double total = 0.0;
            map.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }

            mapR.put("totalCount", ordersEntities.size());
            mapR.put("finishCount", finishCount);
            mapR.put("hasPriceCount", hasPriceCount);
            mapR.put("hasWeightCount", hasWeightCount);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

            return R.ok().put("data", mapR);
        } else {
            Map<String, Object> mapR = new HashMap<>();
            double total = 0.0;
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("status", 3);
            mapS.put("depFatherId", depFatherId);
            mapS.put("gbDepFatherId", gbDepFatherId);
            mapS.put("resFatherId", resFatherId);
            mapS.put("subtotal", 0);
            System.out.println("tototototdddddddd" + mapS);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapS);
            }
            mapR.put("subAmount", 0);
            mapR.put("arr", ordersEntities);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapR.put("tradeNo", s);
            mapR.put("finishCount", finishCount);
            mapR.put("hasPriceCount", hasPriceCount);
            mapR.put("hasWeightCount", hasWeightCount);
            mapR.put("totalCount", ordersEntities.size());
            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/printerGetPrintOrders", method = RequestMethod.POST)
    @ResponseBody
    public R printerGetPrintOrders(Integer depFatherId, Integer depId, Integer gbDepFatherId, Integer gbDepId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderDisId", disId);
        map.put("equalStatus", 2);
        map.put("depFatherId", depFatherId);
        if (!depFatherId.equals(depId)) {
            map.put("depId", depId);
        }

        map.put("gbDepFatherId", gbDepFatherId);
        if (!gbDepFatherId.equals(gbDepId)) {
            map.put("gbDepId", gbDepId);
        }
        map.put("resFatherId", resFatherId);
        map.put("orderBy", "time");
        System.out.println("mapps" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.disQueryDisOrdersByParams(map);
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        double total = 0.0;
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", ordersEntities);
        mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("tradeNo", s);
        mapR.put("distributer", nxDistributerEntity);

        System.out.println("rmamammam" + mapR);
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/getToFillDepOrdersReturn", method = RequestMethod.POST)
    @ResponseBody
    public R getToFillDepOrdersReturn(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderDisId", disId);
        map.put("returnStatus", 1);
        map.put("depFatherId", depFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("resFatherId", resFatherId);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);

        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(disId);
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("returnStatus", 1);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                mapDep.put("depOrders", depOrders);
                System.out.println("map111aaa" + map1);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                Double sutotal = 0.0;
                if (integer > 0) {
                    sutotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(map1);
                }
                mapDep.put("depSubtotal", new BigDecimal(sutotal).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapDep.put("depSubtotalHanzi", convertDoubleToChineseCurrency(sutotal));
                mapList.add(mapDep);
            }

            Map<String, Object> mapR = new HashMap<>();
            mapR.put("subAmount", entities.size());
            mapR.put("arr", mapList);
            mapR.put("tradeNo", s);
            double total = 0.0;
//            map.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }

            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
            return R.ok().put("data", mapR);
        } else {
            Map<String, Object> mapR = new HashMap<>();
            double total = 0.0;
            Map<String, Object> mapS = new HashMap<>();
            mapS.put("returnStatus", 1);
            mapS.put("depFatherId", depFatherId);
            mapS.put("gbDepFatherId", gbDepFatherId);
            mapS.put("resFatherId", resFatherId);
//            mapS.put("subtotal", 0);
            System.out.println("tototototdddddddd" + mapS);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(mapS);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapS);
            }
            mapR.put("subAmount", 0);
            mapR.put("arr", ordersEntities);
            mapR.put("totalHanzi", convertDoubleToChineseCurrency(total));
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapR.put("tradeNo", s);
            System.out.println("ababdbbdbdbdbdbbdb====" + mapR);
            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/getOrderPageSearch", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPageSearch(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, String searchStr) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        String pinyinString = searchStr;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchPinyin", pinyinString);
        map.put("searchStr", searchStr);
        System.out.println("arrserchas" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderSearch(map);

        return R.ok().put("data", ordersEntities);
    }

    @RequestMapping(value = "/webGetOrderPage", method = RequestMethod.POST)
    @ResponseBody
    public R webGetOrderPage(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId,
                             Integer limit, Integer page, Integer nxDisId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDisId);
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
        System.out.println("orsisisisis" + ordersEntities.size());
        //查询列表数据
        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
        double total = 0.0;

        map.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/webGetOrderPageReturn", method = RequestMethod.POST)
    @ResponseBody
    public R webGetOrderPageReturn(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId,
                                   String orderBy, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("returnStatus", 1);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
        System.out.println("orsisisisis" + ordersEntities.size());
        //查询列表数据
        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
        double total = 0.0;

        map.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/webGetSubDepOrderPageSubDep", method = RequestMethod.POST)
    @ResponseBody
    public R webGetSubDepOrderPageSubDep(Integer depFatherId, Integer depId, Integer gbDepId, Integer resFatherId,
                                         String orderBy, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depId", depId);
        map.put("gbDepId", gbDepId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
        System.out.println("orsisisisis" + ordersEntities.size());
        //查询列表数据
        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
        double total = 0.0;

        map.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        return R.ok().put("data", mapR);
    }


    @RequestMapping(value = "/webGetSubDepOrderPageSubDepReturn", method = RequestMethod.POST)
    @ResponseBody
    public R webGetSubDepOrderPageSubDepReturn(Integer depFatherId, Integer depId, Integer gbDepFatherId, Integer resFatherId,
                                               String orderBy, Integer limit, Integer page) {
        Map<String, Object> map = new HashMap<>();
        map.put("returnStatus", 1);
        map.put("depId", depId);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);

        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
        System.out.println("orsisisisis" + ordersEntities.size());
        //查询列表数据
        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
        double total = 0.0;

        map.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
        }
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("page", pageUtil);
        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/webGetSubDepOrderPageReturn", method = RequestMethod.POST)
    @ResponseBody
    public R webGetSubDepOrderPageReturn(Integer depFatherId, Integer depId, Integer gbDepFatherId, Integer resFatherId,
                                         Integer limit, Integer page) {
        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
        List<Map<String, Object>> retrunList = new ArrayList<>();
        if (departmentEntities.size() > 0) {

            for (NxDepartmentEntity subDep : departmentEntities) {

                Map<String, Object> map = new HashMap<>();
                map.put("returnStatus", 1);
                map.put("depId", subDep.getNxDepartmentId());
                map.put("resFatherId", resFatherId);
                map.put("gbDepFatherId", gbDepFatherId);
                map.put("offset", (page - 1) * limit);
                map.put("limit", limit);
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
                System.out.println("orsisisisis" + ordersEntities.size());

                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                System.out.println("wokkkk" + integer);

                double total = 0.0;
                map.put("offset", null);
                map.put("limit", null);


                map.put("subtotal", 0);
                Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                if (twoTotal > 0) {
                    total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                }
                //查询列表数据

                PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);

                Map<String, Object> mapR = new HashMap<>();
                mapR.put("page", pageUtil);
                mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
                mapR.put("depName", subDep.getNxDepartmentName());

                retrunList.add(mapR);
            }

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("returnStatus", 1);
            map.put("depFatherId", depFatherId);
            map.put("resFatherId", resFatherId);
            map.put("gbDepFatherId", gbDepFatherId);
            map.put("offset", (page - 1) * limit);
            map.put("limit", limit);

            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
            System.out.println("orsisisisis" + ordersEntities.size());
            //查询列表数据
            Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

            PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
            double total = 0.0;
            map.put("offset", null);
            map.put("limit", null);
            map.put("subtotal", 0);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }
            Map<String, Object> mapR = new HashMap<>();
            mapR.put("page", pageUtil);
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

            retrunList.add(mapR);
        }

        return R.ok().put("data", retrunList);
    }

    @RequestMapping(value = "/webGetSubDepOrderPage", method = RequestMethod.POST)
    @ResponseBody
    public R webGetSubDepOrderPage(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId,
                                   Integer limit, Integer page, Integer disId) {

        int depAmount = 0;
        List<Map<String, Object>> retrunList = new ArrayList<>();

        if (depFatherId != -1) {
            List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
            if (departmentEntities.size() > 0) {
                depAmount = departmentEntities.size();
                for (NxDepartmentEntity subDep : departmentEntities) {

                    Map<String, Object> map = new HashMap<>();
                    map.put("disId", disId);
                    map.put("status", 3);
                    map.put("depId", subDep.getNxDepartmentId());
                    map.put("resFatherId", resFatherId);
                    map.put("gbDepFatherId", gbDepFatherId);
                    map.put("offset", (page - 1) * limit);
                    map.put("limit", limit);
                    List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
                    System.out.println("orsisisisis" + ordersEntities.size());

                    Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                    System.out.println("wokkkk" + integer);

                    double total = 0.0;
                    map.put("offset", null);
                    map.put("limit", null);


                    map.put("subtotal", 0);
                    Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                    if (twoTotal > 0) {
                        total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                    }
                    //查询列表数据

                    PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);

                    Map<String, Object> mapR = new HashMap<>();
                    mapR.put("page", pageUtil);
                    mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
                    mapR.put("depName", subDep.getNxDepartmentName());

                    retrunList.add(mapR);
                }
            }
        } else {
            if (gbDepFatherId != -1) {
                List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(gbDepFatherId);
                if (departmentEntities.size() > 0) {
                    depAmount = departmentEntities.size();
                    for (GbDepartmentEntity subDep : departmentEntities) {

                        Map<String, Object> map = new HashMap<>();
                        map.put("disId", disId);
                        map.put("status", 3);
                        map.put("depFatherId", depFatherId);
                        map.put("resFatherId", resFatherId);
                        map.put("gbDepId", subDep.getGbDepartmentId());
                        map.put("offset", (page - 1) * limit);
                        map.put("limit", limit);
                        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
                        System.out.println("orsisisisis" + ordersEntities.size());

                        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                        System.out.println("wokkkkGGGGGGG" + integer);

                        double total = 0.0;
                        map.put("offset", null);
                        map.put("limit", null);


                        map.put("subtotal", 0);
                        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
                        if (twoTotal > 0) {
                            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
                        }
                        //查询列表数据

                        PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
                        Map<String, Object> mapR = new HashMap<>();
                        mapR.put("page", pageUtil);
                        mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
                        mapR.put("depName", subDep.getGbDepartmentName());
                        retrunList.add(mapR);
                    }
                }
            }
        }


        if (depAmount == 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", disId);
            map.put("status", 3);
            map.put("depFatherId", depFatherId);
            map.put("resFatherId", resFatherId);
            map.put("gbDepFatherId", gbDepFatherId);
            map.put("offset", (page - 1) * limit);
            map.put("limit", limit);

            System.out.println("orroormriappap" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryPrintDepOrder(map);
            System.out.println("orsisisisis" + ordersEntities.size());
            //查询列表数据
            Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map);

            PageUtils pageUtil = new PageUtils(ordersEntities, integer, limit, page);
            double total = 0.0;
            map.put("offset", null);
            map.put("limit", null);
            map.put("subtotal", 0);
            System.out.println("sussusososossoso" + map);
            Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map);
            if (twoTotal > 0) {
                total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map);
            }
            Map<String, Object> mapR = new HashMap<>();
            mapR.put("page", pageUtil);
            mapR.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));

            retrunList.add(mapR);
        }

        return R.ok().put("data", retrunList);
    }

    @RequestMapping(value = "/getOrderPageToOutWeightByDis", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPageToOutWeightByDis(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, String orderBy, Integer disId) {
        Map<String, Object> mapResult = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("orderDisId", disId);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("orderBy", orderBy);
        map.put("dayuOrderStatus", -2);

        NxDepartmentEntity fatherDepartment = nxDepartmentService.queryObject(depFatherId);
        List<NxDepartmentEntity> departmentEntities = new ArrayList<>();
        System.out.println("newMehodtocheckDepart" + fatherDepartment.getNxDepartmentSettleType() );
        if(fatherDepartment.getNxDepartmentSettleType() == 2){
            departmentEntities = nxDepartmentOrdersService.queryRetailOrderNxDepartment(map);
        }else{
            departmentEntities  = nxDepartmentService.querySubDepartments(depFatherId);
        }


        List<Map<String, Object>> list = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                map.put("depId", departmentEntity.getNxDepartmentId());
                map.put("purchaseStatus", 4);
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", departmentEntity.getNxDepartmentId());
                mapDep.put("depName", departmentEntity.getNxDepartmentName());
                mapDep.put("list", ordersEntities);
                list.add(mapDep);
            }

            mapResult.put("arr", list);
            mapResult.put("depHasSubs", departmentEntities.size());
        } else {
            map.put("purchaseStatus", 4);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
            mapResult.put("arr", ordersEntities);
            mapResult.put("depHasSubs", 0);
        }

        mapResult.put("subDep", departmentEntities.size());
        return R.ok().put("data", mapResult);
    }
    @RequestMapping(value = "/getOrderPageToOutWeight", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPageToOutWeight(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, String orderBy) {
        Map<String, Object> mapResult = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("orderBy", orderBy);
        map.put("dayuOrderStatus", -2);

        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
        List<Map<String, Object>> list = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                map.put("depId", departmentEntity.getNxDepartmentId());
                map.put("purchaseStatus", 4);
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", departmentEntity.getNxDepartmentId());
                mapDep.put("depName", departmentEntity.getNxDepartmentName());
                mapDep.put("list", ordersEntities);
                list.add(mapDep);
            }

            mapResult.put("arr", list);
            mapResult.put("depHasSubs", departmentEntities.size());
        } else {
            map.put("purchaseStatus", 4);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
            mapResult.put("arr", ordersEntities);
            mapResult.put("depHasSubs", 0);
        }

        mapResult.put("subDep", departmentEntities.size());
        return R.ok().put("data", mapResult);
    }


    @RequestMapping(value = "/getOrderPageToOutWeightGb", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPageToOutWeightGb(Integer disId, Integer gbDepFatherId, Integer resFatherId, String orderBy) {
        Map<String, Object> mapResult = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("disId", disId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("orderBy", orderBy);

        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(gbDepFatherId);
        List<Map<String, Object>> list = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity departmentEntity : departmentEntities) {
                map.put("disId", disId);
                map.put("gbDepId", departmentEntity.getGbDepartmentId());
                map.put("purchaseStatus", 4);
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderGb(map);
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("gbDepId", departmentEntity.getGbDepartmentId());
                mapDep.put("depName", departmentEntity.getGbDepartmentName());
                mapDep.put("list", ordersEntities);
                list.add(mapDep);
            }

            mapResult.put("arr", list);
            mapResult.put("depHasSubs", departmentEntities.size());
        } else {
            map.put("purchaseStatus", 4);
            System.out.println("aodoodododoodododood0000000" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderGb(map);
            mapResult.put("arr", ordersEntities);
            mapResult.put("depHasSubs", 0);
        }

        mapResult.put("subDep", departmentEntities.size());
        return R.ok().put("data", mapResult);
    }

    @RequestMapping(value = "/getOrderPage", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPage(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, String orderBy, Integer disId) {
        Map<String, Object> mapResult = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("orderDisId", disId);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("orderBy", orderBy);
        map.put("dayuOrderStatus", -2);

        NxDepartmentEntity fatherDepartment = nxDepartmentService.queryObject(depFatherId);
        List<NxDepartmentEntity> departmentEntities = new ArrayList<>();
        System.out.println("newMehodtocheckDepart" + fatherDepartment.getNxDepartmentSettleType() );
        if(fatherDepartment.getNxDepartmentSettleType() == 2){
            departmentEntities = nxDepartmentOrdersService.queryRetailOrderNxDepartment(map);
        }else{
            departmentEntities  = nxDepartmentService.querySubDepartments(depFatherId);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                map.put("depId", departmentEntity.getNxDepartmentId());
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", departmentEntity.getNxDepartmentId());
                mapDep.put("depName", departmentEntity.getNxDepartmentName());
                mapDep.put("list", ordersEntities);
                list.add(mapDep);
            }
            mapResult.put("arr", list);
            mapResult.put("depHasSubs", departmentEntities.size());
        } else {

            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
            mapResult.put("arr", ordersEntities);
            mapResult.put("depHasSubs", 0);
        }

        mapResult.put("subDep", departmentEntities.size());
        return R.ok().put("data", mapResult);
    }


    @RequestMapping(value = "/getOrderPageByDis", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPageByDis(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, String orderBy, Integer disId) {
        Map<String, Object> mapResult = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("disId", disId);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        map.put("orderBy", orderBy);
        map.put("dayuOrderStatus", -2);

        NxDepartmentEntity fatherDepartment = nxDepartmentService.queryObject(depFatherId);
        List<NxDepartmentEntity> departmentEntities = new ArrayList<>();
        System.out.println("newMehodtocheckDepart" + fatherDepartment.getNxDepartmentSettleType() );
        if(fatherDepartment.getNxDepartmentSettleType() == 2){
            departmentEntities = nxDepartmentOrdersService.queryRetailOrderNxDepartment(map);
        }else{
            departmentEntities  = nxDepartmentService.querySubDepartments(depFatherId);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                map.put("depId", departmentEntity.getNxDepartmentId());
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", departmentEntity.getNxDepartmentId());
                mapDep.put("depName", departmentEntity.getNxDepartmentName());
                mapDep.put("list", ordersEntities);
                list.add(mapDep);
            }
            mapResult.put("arr", list);
            mapResult.put("depHasSubs", departmentEntities.size());
        } else {

            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepWeightOrder(map);
            mapResult.put("arr", ordersEntities);
            mapResult.put("depHasSubs", 0);
        }

        mapResult.put("subDep", departmentEntities.size());
        return R.ok().put("data", mapResult);
    }


    @RequestMapping(value = "/getOrderPagePicture", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPagePicture(Integer depFatherId, String orderBy) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        List<NxDepartmentOrdersEntity> ordersEntities = new ArrayList<>();
        if (orderBy.equals("time") || orderBy.equals("sort")) {
            map.put("orderBy", orderBy);
            ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderGb(map);
        }

        return R.ok().put("data", ordersEntities);
    }

    /**
     * 根据depFatherId查询订单列表（包含溯源报告信息）
     * 如果是货架商品，从库存批次关联溯源报告；如果不是货架商品，从采购商品关联溯源报告
     */
    @RequestMapping(value = "/getOrderPageWithTraceReport", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPageWithTraceReport(Integer depFatherId) {
        System.out.println("[getOrderPageWithTraceReport] 开始查询订单溯源报告，depFatherId: " + depFatherId);
        Map<String, Object> mapResult = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);

        List<NxDepartmentEntity> departmentEntities = nxDepartmentService.querySubDepartments(depFatherId);
        List<Map<String, Object>> list = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                map.put("depId", departmentEntity.getNxDepartmentId());
                List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepOrdersWithTraceReport(map);
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", departmentEntity.getNxDepartmentId());
                mapDep.put("depName", departmentEntity.getNxDepartmentName());
                mapDep.put("list", ordersEntities);
                list.add(mapDep);
            }
            mapResult.put("arr", list);
            mapResult.put("depHasSubs", departmentEntities.size());
        } else {
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDepOrdersWithTraceReport(map);
            System.out.println("[getOrderPageWithTraceReport] 无子部门，订单数量: " + ordersEntities.size());

            // 打印每个订单的溯源报告信息
            for (NxDepartmentOrdersEntity order : ordersEntities) {
                NxTraceReportEntity traceReport = order.getNxTraceReportEntity();
                NxDistributerGoodsShelfStockEntity shelfStock = order.getShelfStockEntity();
                // 查询商品信息，检查商品表中的溯源报告ID
                if (order.getNxDoDisGoodsId() != null) {
                    NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(order.getNxDoDisGoodsId());
                    System.out.println("[getOrderPageWithTraceReport] 订单ID: " + order.getNxDepartmentOrdersId() +
                            ", 商品ID: " + order.getNxDoDisGoodsId() +
                            ", 商品名称: " + order.getNxDoGoodsName() +
                            ", 商品表中的溯源报告ID: " + (goods != null ? goods.getNxDgTraceReportId() : "null") +
                            ", 订单对象中的溯源报告: " + (traceReport != null ? "存在(ID:" + traceReport.getNxTraceReportId() + ")" : "null"));
                } else {
                    System.out.println("[getOrderPageWithTraceReport] 订单ID: " + order.getNxDepartmentOrdersId() +
                            ", 商品名称: " + order.getNxDoGoodsName() +
                            ", 订单对象中的溯源报告: " + (traceReport != null ? "存在(ID:" + traceReport.getNxTraceReportId() + ")" : "null"));
                }
                if (traceReport != null) {
                    System.out.println("[getOrderPageWithTraceReport]   溯源报告详情 - 供应商: " + traceReport.getNxTrSupplierName() +
                            ", 采购日期: " + traceReport.getNxTrPurchaseDate());
                }
            }
            mapResult.put("arr", ordersEntities);
            mapResult.put("depHasSubs", 0);
        }

        mapResult.put("subDep", departmentEntities.size());
        System.out.println("[getOrderPageWithTraceReport] 查询完成");
        return R.ok().put("data", mapResult);
    }

    @RequestMapping(value = "/getOrderPageGb", method = RequestMethod.POST)
    @ResponseBody
    public R getOrderPageGb(Integer depFatherId, Integer gbDepFatherId, Integer resFatherId, String orderBy) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depFatherId", depFatherId);
        map.put("resFatherId", resFatherId);
        map.put("gbDepFatherId", gbDepFatherId);
        List<NxDepartmentOrdersEntity> ordersEntities = new ArrayList<>();
        if (orderBy.equals("time") || orderBy.equals("sort")) {
            map.put("orderBy", orderBy);
            System.out.println("oririir" + map);
            ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderGb(map);
        }


        Map<String, Object> mapPPP = new HashMap<>();

        if (orderBy.equals("orderPrice")) {
            Map<String, Object> mapP = new HashMap<>();
            mapP.put("status", 3);
            mapP.put("depFatherId", depFatherId);
            mapP.put("resFatherId", resFatherId);
            mapP.put("gbDepFatherId", gbDepFatherId);
            ordersEntities = nxDepartmentOrdersService.queryDepWeightOrderGb(mapP);

            mapP.put("purType", 0); // stock
            List<NxDepartmentOrdersEntity> ordersEntitiesStock = nxDepartmentOrdersService.queryDepWeightOrderGb(mapP);
            mapP.put("purType", 1);
            mapP.put("inputType", 0);
            List<NxDepartmentOrdersEntity> ordersEntitiesZicai = nxDepartmentOrdersService.queryDepWeightOrderGb(mapP);
            mapP.put("inputType", 1);
            List<NxDepartmentOrdersEntity> ordersEntitiesWx = nxDepartmentOrdersService.queryDepWeightOrderGb(mapP);

            mapPPP.put("stock", ordersEntitiesStock);
            mapPPP.put("zicai", ordersEntitiesZicai);
            mapPPP.put("wx", ordersEntitiesWx);

        }


        Map<String, Object> map2 = new HashMap<>();
        map2.put("status", 3);
        map2.put("depFatherId", depFatherId);
        map2.put("resFatherId", resFatherId);
        map2.put("gbDepFatherId", gbDepFatherId);
        map2.put("subtotal", 0);
        Integer twoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map2);
        Double total = 0.0;
        Double profitTotal = 0.0;
        String scale = "";
        if (twoTotal > 0) {
            total = nxDepartmentOrdersService.queryDepOrdersSubtotal(map2);
//            profitTotal = nxDepartmentOrdersService.queryDepOrdersProfitSubtotal(map2);
            BigDecimal scaleBig = new BigDecimal(profitTotal).divide(new BigDecimal(total), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            scale = scaleBig.toString();
        }

        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("arr", ordersEntities);
        mapResult.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("profit", new BigDecimal(profitTotal).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapResult.put("scale", scale);


        map.put("hasPrice", 1);
        Integer priceCount = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        mapResult.put("priceCount", priceCount);
        mapResult.put("mapP", mapPPP);

        return R.ok().put("data", mapResult);
    }

    @RequestMapping(value = "/depGetApplyDesk/{depId}")
    @ResponseBody
    public R depGetApplyDesk(@PathVariable Integer depId) {
        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depId);
        map3.put("orderBy", "time");
        map3.put("status", 3);
        map3.put("dayuStatus", -1);
        List<NxDepartmentOrdersEntity> ordersEntities3 = nxDepartmentOrdersService.queryDisOrdersByParams(map3);

        return R.ok().put("data", ordersEntities3);
    }


    @RequestMapping(value = "/depGetApplyAi/{depFatherId}")
    @ResponseBody
    public R depGetApplyAi(@PathVariable Integer depFatherId) {

        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        System.out.println("depdiidid" + entities.size());
        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                mapDep.put("depOrders", depOrders);

                Map<String, Object> mapD = new HashMap<>();
                mapD.put("depId", dep.getNxDepartmentId());
                int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);
                mapDep.put("depGoodsCount", i);
                System.out.println("newwnnwenddepdinofoffo" + depFatherId);
                mapDep.put("depInfo", nxDepartmentService.queryDepInfoAll(dep.getNxDepartmentId()));

                mapList.add(mapDep);
            }

            System.out.println("Arekrjekjkre" + mapR);
            mapR.put("arr", mapList);

            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depFatherId);

            if (departmentEntity.getNxDepartmentSettleType() == 0) {
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("depFatherId", depFatherId);
                mapB.put("status", 1);
                System.out.println("debebiiii" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
                if (billEntityList.size() > 0) {
                    mapR.put("bill", billEntityList.get(0));
                } else {
                    mapR.put("bill", -1);
                }
            } else {
                mapR.put("bill", -1);
            }
            return R.ok().put("data", mapR);

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 3);
            map.put("depFatherId", depFatherId);
            System.out.println("abncnncnnnc" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
            Map<String, Object> mapD = new HashMap<>();
            mapD.put("depId", depFatherId);
            int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);

            mapR.put("depGoodsCount", i);
            mapR.put("arr", ordersEntities);
            mapR.put("depInfo", nxDepartmentService.queryDepInfoAll(depFatherId));

            System.out.println("whwhwhwwwwkw");
            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depFatherId);

            if (departmentEntity.getNxDepartmentSettleType() == 0) {
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("depFatherId", depFatherId);
                mapB.put("status", 1);
                System.out.println("debebiiii" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
                if (billEntityList.size() > 0) {
                    mapR.put("bill", billEntityList.get(0));
                } else {
                    mapR.put("bill", -1);
                }
            } else {
                mapR.put("bill", -1);
            }
            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/depGetApplyAiByTime/{depFatherId}")
    @ResponseBody
    public R depGetApplyAiByTime(@PathVariable Integer depFatherId) {

        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        System.out.println("depdiidid" + entities.size());
        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
//                map1.put("dayuOrderStatus", -2);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                mapDep.put("depOrders", depOrders);

                Map<String, Object> mapD = new HashMap<>();
                mapD.put("depId", dep.getNxDepartmentId());
//                mapD.put("dayuOrderStatus", -2);
                int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);
                mapDep.put("depGoodsCount", i);
                System.out.println("newwnnwenddepdinofoffo" + depFatherId);
                mapDep.put("depInfo", nxDepartmentService.queryDepInfoAll(dep.getNxDepartmentId()));

                mapList.add(mapDep);
            }

            System.out.println("Arekrjekjkre" + mapR);
            mapR.put("arr", mapList);

            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depFatherId);

            if (departmentEntity.getNxDepartmentSettleType() == 0) {
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("depFatherId", depFatherId);
                mapB.put("status", 1);
                System.out.println("debebiiii" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
                if (billEntityList.size() > 0) {
                    mapR.put("bill", billEntityList.get(0));
                } else {
                    mapR.put("bill", -1);
                }
            } else {
                mapR.put("bill", -1);
            }
            return R.ok().put("data", mapR);

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 3);
//            map.put("dayuOrderStatus", -2);
            map.put("depFatherId", depFatherId);
            System.out.println("abncnncnnnc" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
            Map<String, Object> mapD = new HashMap<>();
            mapD.put("depId", depFatherId);
            int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);

            mapR.put("depGoodsCount", i);
            mapR.put("arr", ordersEntities);
            mapR.put("depInfo", nxDepartmentService.queryDepInfoAll(depFatherId));

            System.out.println("whwhwhwwwwkw");
            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depFatherId);

            if (departmentEntity.getNxDepartmentSettleType() == 0) {
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("depFatherId", depFatherId);
                mapB.put("status", 1);
                System.out.println("debebiiii" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
                if (billEntityList.size() > 0) {
                    mapR.put("bill", billEntityList.get(0));
                } else {
                    mapR.put("bill", -1);
                }
            } else {
                mapR.put("bill", -1);
            }
            return R.ok().put("data", mapR);
        }
    }


    @RequestMapping(value = "/depGetApplyAiByTimeSunla/{depFatherId}")
    @ResponseBody
    public R depGetApplyAiByTimeSunla(@PathVariable Integer depFatherId) {

        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        System.out.println("depdiidid" + entities.size());
        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("dayuStatus", -1);
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDepartmentOrdersEntity> depOrders = nxDepartmentOrdersService.queryDisOrdersByParams(map1);
                mapDep.put("depOrders", depOrders);

                Map<String, Object> mapD = new HashMap<>();
                mapD.put("depId", dep.getNxDepartmentId());
                int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);
                mapDep.put("depGoodsCount", i);
                System.out.println("newwnnwenddepdinofoffo" + depFatherId);
                mapDep.put("depInfo", nxDepartmentService.queryDepInfoAll(dep.getNxDepartmentId()));

                mapList.add(mapDep);
            }

            System.out.println("Arekrjekjkre" + mapR);
            mapR.put("arr", mapList);

            return R.ok().put("data", mapR);

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 3);
            map.put("dayuStatus", -1);
            map.put("depFatherId", depFatherId);
            map.put("isPurType", true);
            System.out.println("abncnncnnncaaaa" + map);
            List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
            mapR.put("arr", ordersEntities);
            return R.ok().put("data", mapR);
        }
    }

    @RequestMapping(value = "/depSearchTodayOrders", method = RequestMethod.POST)
    @ResponseBody
    public R depSearchTodayOrders(Integer depFatherId, Integer depId, String searchStr) {

        Map<String, Object> map = new HashMap<>();

        if (depFatherId != -1) {
            map.put("depFatherId", depFatherId);
        }
        if (depId != -1) {
            map.put("depId", depId);
        }


        String pinyinString = "";
        boolean hasChinese = false;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                hasChinese = true;
                break;
            }
        }
        if (hasChinese) {
            pinyinString = hanziToPinyin(searchStr);

        } else {
            // 全是拼音或英文
            pinyinString = searchStr;
            searchStr = null;
        }
        map.put("searchPinyin", pinyinString);
        map.put("searchStr", searchStr);
        map.put("status", 3);

        System.out.println("searchchhchc1111" + map);
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentOrdersService.queryDepWeightOrderSearch(map);
        System.out.println("orarrrr" + nxDepartmentOrdersEntities.size());
        return R.ok().put("data", nxDepartmentOrdersEntities);
    }

    @RequestMapping(value = "/depGetApplyAiFather/{depFatherId}")
    @ResponseBody
    public R depGetApplyAiFather(@PathVariable Integer depFatherId) {

        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<NxDepartmentEntity> entities = nxDepartmentService.querySubDepartments(depFatherId);
        System.out.println("depdiidid" + entities.size());
        if (entities.size() > 0) {
            for (NxDepartmentEntity dep : entities) {
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("depId", dep.getNxDepartmentId());
                mapDep.put("depName", dep.getNxDepartmentName());
                Map<String, Object> map1 = new HashMap<>();
                map1.put("status", 3);
                map1.put("depId", dep.getNxDepartmentId());
                map1.put("orderBy", "time");
                List<NxDistributerFatherGoodsEntity> nxDistributerFatherGoodsEntities = nxDepartmentOrdersService.queryGrandGoodsOrder(map1);
                mapDep.put("depOrders", nxDistributerFatherGoodsEntities);

                Map<String, Object> mapD = new HashMap<>();
                mapD.put("depId", dep.getNxDepartmentId());
                int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);
                mapDep.put("depGoodsCount", i);
                System.out.println("newwnnwenddepdinofoffo" + depFatherId);
                mapDep.put("depInfo", nxDepartmentService.queryDepInfoAll(dep.getNxDepartmentId()));

                mapList.add(mapDep);
            }

            System.out.println("Arekrjekjkre" + mapR);
            mapR.put("arr", mapList);

            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depFatherId);

            if (departmentEntity.getNxDepartmentSettleType() == 0) {
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("depFatherId", depFatherId);
                mapB.put("status", 1);
                System.out.println("debebiiii" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
                if (billEntityList.size() > 0) {
                    mapR.put("bill", billEntityList.get(0));
                } else {
                    mapR.put("bill", -1);
                }
            } else {
                mapR.put("bill", -1);
            }
            return R.ok().put("data", mapR);

        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 3);
            map.put("depFatherId", depFatherId);
            List<NxDistributerFatherGoodsEntity> nxDistributerFatherGoodsEntities = nxDepartmentOrdersService.queryGrandGoodsOrder(map);

            mapR.put("arr", nxDistributerFatherGoodsEntities);
            mapR.put("depInfo", nxDepartmentService.queryDepInfoAll(depFatherId));

            System.out.println("whwhwhwwwwkw");
            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depFatherId);

            if (departmentEntity.getNxDepartmentSettleType() == 0) {
                Map<String, Object> mapB = new HashMap<>();
                mapB.put("depFatherId", depFatherId);
                mapB.put("status", 1);
                System.out.println("debebiiii" + mapB);
                List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
                if (billEntityList.size() > 0) {
                    mapR.put("bill", billEntityList.get(0));
                } else {
                    mapR.put("bill", -1);
                }
            } else {
                mapR.put("bill", -1);
            }
            return R.ok().put("data", mapR);
        }
    }


    @RequestMapping(value = "/subDepGetApplyAi/{depId}")
    @ResponseBody
    public R subDepGetApplyAi(@PathVariable Integer depId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depId", depId);
        System.out.println("abncnncnnncSubsusb" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
        Map<String, Object> mapD = new HashMap<>();
        mapD.put("depId", depId);
        int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);

        mapR.put("depGoodsCount", i);
        mapR.put("arr", ordersEntities);
        mapR.put("depInfo", nxDepartmentService.queryDepInfoAll(depId));

        System.out.println("whwhwhwwwwkw");
        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);

        if (departmentEntity.getNxDepartmentSettleType() == 0) {
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("depFatherId", departmentEntity.getNxDepartmentFatherId());
            mapB.put("status", 1);
            System.out.println("debebiiii" + mapB);
            List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
            if (billEntityList.size() > 0) {
                mapR.put("bill", billEntityList.get(0));
            } else {
                mapR.put("bill", -1);
            }
        } else {
            mapR.put("bill", -1);
        }
        return R.ok().put("data", mapR);

    }


    @RequestMapping(value = "/subDepGetApplyAiByTime/{depId}")
    @ResponseBody
    public R subDepGetApplyAiByTime(@PathVariable Integer depId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depId", depId);
        System.out.println("abncnncnnncSubsusb" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryNotWeightDisOrdersByParams(map);
        Map<String, Object> mapD = new HashMap<>();
        mapD.put("depId", depId);
        int i = nxDepartmentDisGoodsService.queryDepGoodsCount(mapD);

        mapR.put("depGoodsCount", i);
        mapR.put("arr", ordersEntities);
        mapR.put("depInfo", nxDepartmentService.queryDepInfoAll(depId));

        System.out.println("whwhwhwwwwkw");
        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);

        if (departmentEntity.getNxDepartmentSettleType() == 0) {
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("depFatherId", departmentEntity.getNxDepartmentFatherId());
            mapB.put("status", 1);
            System.out.println("debebiiii" + mapB);
            List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
            if (billEntityList.size() > 0) {
                mapR.put("bill", billEntityList.get(0));
            } else {
                mapR.put("bill", -1);
            }
        } else {
            mapR.put("bill", -1);
        }
        return R.ok().put("data", mapR);

    }

    @RequestMapping(value = "/subDepGetApplyAiFather/{depId}")
    @ResponseBody
    public R subDepGetApplyAiFather(@PathVariable Integer depId) {

        Map<String, Object> mapR = new HashMap<>();

        Map<String, Object> map = new HashMap<>();
        map.put("status", 3);
        map.put("depId", depId);

        List<NxDistributerFatherGoodsEntity> nxDistributerFatherGoodsEntities = nxDepartmentOrdersService.queryGrandGoodsOrder(map);

        mapR.put("arr", nxDistributerFatherGoodsEntities);
        mapR.put("depInfo", nxDepartmentService.queryDepInfoAll(depId));

        System.out.println("whwhwhwwwwkw");
        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);

        if (departmentEntity.getNxDepartmentSettleType() == 0) {
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("depFatherId", departmentEntity.getNxDepartmentFatherId());
            mapB.put("status", 1);
            System.out.println("debebiiii" + mapB);
            List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(mapB);
            if (billEntityList.size() > 0) {
                mapR.put("bill", billEntityList.get(0));
            } else {
                mapR.put("bill", -1);
            }
        } else {
            mapR.put("bill", -1);
        }
        return R.ok().put("data", mapR);

    }


    /**
     * dh
     *
     * @param depId
     * @return
     */
    @RequestMapping(value = "/depGetApply/{depId}")
    @ResponseBody
    public R depGetApply(@PathVariable Integer depId) {

        Map<String, Object> map3 = new HashMap<>();
        map3.put("depId", depId);
        map3.put("orderBy", "time");
        map3.put("status", 3);
//        map3.put("purStatus", 4);
        map3.put("dayuStatus", -1);
        List<NxDepartmentOrdersEntity> ordersEntities3 = nxDepartmentOrdersService.queryDisOrdersByParams(map3);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", ordersEntities3);
        mapR.put("depInfo", nxDepartmentService.queryObject(depId));

        NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(depId);

        if (departmentEntity.getNxDepartmentSettleType() == 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("depId", depId);
            map.put("status", 1);
            List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryBillsByParams(map);
            if (billEntityList.size() > 0) {
                mapR.put("bill", billEntityList.get(0));
            } else {
                mapR.put("bill", -1);
            }
        } else {
            mapR.put("bill", -1);
        }
        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/printerNxDisGetTodayOrderCustomer/{disId}")
    @ResponseBody
    public R printerNxDisGetTodayOrderCustomer(@PathVariable Integer disId) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("equalStatus", 2);
        System.out.println("dapapfpa" + map1);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);
        List<NxDepartmentEntity> resultData = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                map1.put("status", null);
                map1.put("equalStatus", 2);
                map1.put("depFatherId", departmentEntity.getNxDepartmentId());
                Integer finish = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                map1.put("equalStatus", null);
                map1.put("status", 3);
//                System.out.println("dkdfajdlfalfdz" +  map1);

                Integer total = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
                System.out.println("dkdfajdlfalfdzininnininin" + total + "finsi=" + finish);
                if (total == finish) {
                    System.out.println("dkdfajdlfalfdzininnininin" + total + "finsi=" + finish);
                    resultData.add(departmentEntity);
                }
            }
        }
        return R.ok().put("data", resultData);

    }

    @RequestMapping(value = "/webNxDisGetTodayOrderCustomer/{disId}")
    @ResponseBody
    public R webNxDisGetTodayOrderCustomer(@PathVariable String disId) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("equalStatus", 2);
        map1.put("dayuPurStatus", 3);
        System.out.println("dapapfpa" + map1);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);
        System.out.println("eneneeneisisisisisiss" + departmentEntities.size());
        List<NxDepartmentEntity> result = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                Integer departmentId = departmentEntity.getNxDepartmentId();
                Map<String, Object> mapDep = new HashMap<>();
                mapDep.put("orderDisId", disId);
                mapDep.put("depFatherId", departmentId);
                mapDep.put("status", 3);
                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                mapDep.put("equalStatus", 2);
                mapDep.put("dayuPurStatus", 3);
                System.out.println("depmapappaaforoofofofo" + mapDep);
                Integer integerFinish = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                System.out.println("reususlslt" + departmentEntity.getNxDepartmentAttrName() + "d" + integer + "fins" + integerFinish);

                if (integer.equals(integerFinish) && integer > 0) {

                    if (departmentEntity.getNxDepartmentEntities().size() > 0) {
                        System.out.println("susususuuususuususokkokkkk" + departmentEntity.getNxDepartmentName());
                        List<NxDepartmentEntity> subList = new ArrayList<>();
                        for (NxDepartmentEntity sub : departmentEntity.getNxDepartmentEntities()) {

                            mapDep.put("depFatherId", departmentId);
                            mapDep.put("depId", sub.getNxDepartmentId());
                            mapDep.put("equalStatus", 2);
                            mapDep.put("dayuPurStatus", 3);
                            System.out.println("depmapappaasusuusussuusu" + mapDep);
                            System.out.println("depmapappaasusuusussuusu" + sub.getNxDepartmentName());
                            Integer integerFinishsub = nxDepartmentOrdersService.queryDepOrdersAcount(mapDep);
                            if (integerFinishsub > 0) {
                                subList.add(sub);
                                System.out.println("sulisiis.elel" + subList.size());
                            }
                        }
                        departmentEntity.setNxDepartmentEntities(subList);
                        result.add(departmentEntity);

                    } else {
                        result.add(departmentEntity);
                    }

                }
            }
        }

        //GbData
        Map<String, Object> mapPB = new HashMap<>();
        mapPB.put("nxDisId", disId);
        mapPB.put("equalStatus", 1);
        System.out.println("enennenenenne" + mapPB);
        List<GbDistributerPurchaseBatchEntity> entities = gbDistributerPurchaseBatchService.queryDisPurchaseBatch(mapPB);

        Map<String, Object> map = new HashMap<>();
        map.put("nxArr", result);
        map.put("gbArr", new ArrayList<>());
        map.put("gbBatchArr", entities);

        Map<String, Object> mapTask = new HashMap<>();
        mapTask.put("disId", disId);
        mapTask.put("xiaoyuStatus", 3);
        System.out.println("maptakksksksksaaaa" + mapTask + result.size());
        int total = nxOcrTaskService.queryTotalByDepartmentAndStatus(mapTask);
        return R.ok().put("data", map)
                .put("taskCount", total);

    }


    @RequestMapping(value = "/webNxDisGetTodayReturnCustomer/{disId}")
    @ResponseBody
    public R webNxDisGetTodayReturnCustomer(@PathVariable Integer disId) {
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("returnStatus", 1);
        System.out.println("dapapfpa" + map1);
        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);

        return R.ok().put("data", departmentEntities);

    }


    /**
     * 配送商获取订货客户
     *
     * @param
     * @return
     * @date 2026-01-10
     */
    @RequestMapping(value = "/disGetTodayOrderCustomer/{disId}")
    @ResponseBody
    public R disGetTodayOrderCustomer(@PathVariable Integer disId) {
        Map<String, Object> returnData = new HashMap<>();
        //今天订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("isSelfOrder", -1);

        List<NxDepartmentEntity> departmentEntities = nxDepartmentOrdersService.queryOrderDepartmentList(map1);
        System.out.println("mapapapapapddiidididi" + map1);
        List<GbDistributerEntity> distributerEntitiesAA = nxDepartmentOrdersService.queryOrderGbDistributerList(map1);
        System.out.println("gbbgbgbgbbgbbgbgb" + distributerEntitiesAA.size());
        Map<String, Object> mapData = new HashMap<>();
        List<Map<String, Object>> resultNx = new ArrayList<>();
        if (departmentEntities.size() > 0) {
            System.out.println("depsisiisisisiiis" + departmentEntities.size());
            // 提取所有部门ID，过滤掉null值
            List<Integer> depFatherIds = departmentEntities.stream()
                    .filter(dept -> dept != null)
                    .map(dept -> dept.getNxDepartmentId())
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            // 批量查询所有部门的统计数据
            Map<Integer, Map<String, Object>> statsMap = nxDepartmentOrdersService.batchQueryDepartmentOrderStats(depFatherIds);

            for (NxDepartmentEntity departmentEntity : departmentEntities) {
                // 跳过null的部门实体
                if (departmentEntity == null) {
                    continue;
                }
                Integer depId = departmentEntity.getNxDepartmentId();
                // 跳过部门ID为null的情况
                if (depId == null) {
                    continue;
                }
                Map<String, Object> stats = statsMap.get(depId);

                if (stats != null) {
                    Integer unDo = (Integer) stats.get("unDo");
                    Integer hasPrice = (Integer) stats.get("hasPrice");
                    Integer hasWeight = (Integer) stats.get("hasWeight");
                    Integer totalCount = (Integer) stats.get("totalCount");
                    Integer finishCount = (Integer) stats.get("finishCount");
                    Object totalSubtotal = stats.get("totalSubtotal");

                    Double total = 0.0;
                    if (totalSubtotal != null) {
                        total = ((Number) totalSubtotal).doubleValue();
                    }

                    Map<String, Object> mapDep = new HashMap<>();
                    mapDep.put("dep", departmentEntity);
                    mapDep.put("totalCount", totalCount);
                    mapDep.put("finishCount", finishCount);
                    mapDep.put("hasPrice", hasPrice);
                    mapDep.put("hasWeight", hasWeight);
                    mapDep.put("unDo", unDo);
                    mapDep.put("twoSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                    resultNx.add(mapDep);
                }
            }
            mapData.put("nxDep", resultNx);
        } else {
            mapData.put("nxDep", new ArrayList<>());
        }

        List<Map<String, Object>> gbList = new ArrayList<>();

        if (distributerEntitiesAA.size() > 0) {
            // 提取所有分销商ID和部门ID
            List<Integer> gbDisIds = new ArrayList<>();
            List<Integer> gbDepIds = new ArrayList<>();
            Map<Integer, List<GbDepartmentEntity>> gbDisToDepMap = new HashMap<>();

            for (GbDistributerEntity gbDis : distributerEntitiesAA) {
                gbDisIds.add(gbDis.getGbDistributerId());

                Map<String, Object> mapOrderDep = new HashMap<>();
                mapOrderDep.put("gbDisId", gbDis.getGbDistributerId());
                mapOrderDep.put("orderDisId", disId);
                System.out.println("查询部门参数: " + mapOrderDep);
                List<GbDepartmentEntity> gbpartmentEntities = nxDepartmentOrdersService.queryPureOrderGbDepartment(mapOrderDep);
                System.out.println("商家 " + gbDis.getGbDistributerId() + " 关联的部门数量: " + gbpartmentEntities.size());
                if (gbpartmentEntities.size() > 0) {
                    System.out.println("部门列表: " + gbpartmentEntities.stream().map(GbDepartmentEntity::getGbDepartmentName).collect(Collectors.toList()));
                }
                gbDisToDepMap.put(gbDis.getGbDistributerId(), gbpartmentEntities);
                for (GbDepartmentEntity gbDep : gbpartmentEntities) {
                    gbDepIds.add(gbDep.getGbDepartmentId());
                }
            }

            // 批量查询所有统计数据

            Map<String, Map<String, Object>> statsMap = nxDepartmentOrdersService.batchQueryGbDistributerDepartmentStats(gbDisIds, gbDepIds);


            for (GbDistributerEntity gbDis : distributerEntitiesAA) {
                List<Map<String, Object>> gbDisDepArrMap = new ArrayList<>();

                Map<String, Object> mapDis = new HashMap<>();
                mapDis.put("orderDisId", gbDis);
                mapDis.put("gbDisId", gbDis.getGbDistributerId());

                List<GbDepartmentEntity> gbpartmentEntities = gbDisToDepMap.get(gbDis.getGbDistributerId());

                System.out.println("=== 开始处理商家: " + gbDis.getGbDistributerId() + " ===");
                System.out.println("商家名称: " + gbDis.getGbDistributerName());
                System.out.println("关联部门数量: " + (gbpartmentEntities != null ? gbpartmentEntities.size() : "null"));

                for (GbDepartmentEntity gbDepartmentEntity : gbpartmentEntities) {
                    String key = gbDis.getGbDistributerId() + "_" + gbDepartmentEntity.getGbDepartmentId();
                    Map<String, Object> stats = statsMap.get(key);

                    if (stats != null) {
                        Integer newOrder = (Integer) stats.get("newOrder");
                        System.out.println("newOrder值: " + newOrder);

                        if (newOrder > 0) {
                            Integer jinhuoOrder = (Integer) stats.get("jinhuoOrder");
                            Integer chukuOrder = (Integer) stats.get("chukuOrder");
                            Integer hasNotWeight = (Integer) stats.get("hasNotWeight");
                            Integer jinhuoHasWeight = (Integer) stats.get("jinhuoHasWeight");
                            Integer chukuHasWeight = (Integer) stats.get("chukuHasWeight");
                            Integer hasPrice = (Integer) stats.get("hasPrice");
                            Integer hasNotPrice = (Integer) stats.get("hasNotPrice");
                            Integer twoTotal = (Integer) stats.get("twoTotal");
                            Object totalObj = stats.get("total");

                            Double total = 0.0;
                            if (totalObj != null) {
                                total = ((Number) totalObj).doubleValue();
                            }

                            Integer jinhuoFinished = 0; // 这个字段在原来的代码中似乎没有计算

                            Map<String, Object> mapDep = new HashMap<>();
                            mapDep.put("gbDep", gbDepartmentEntity);
                            mapDep.put("newOrder", newOrder);
                            mapDep.put("hasNotWeight", hasNotWeight);
                            mapDep.put("jinhuoOrder", jinhuoOrder);
                            mapDep.put("jinhuoHasWeight", jinhuoHasWeight);
                            mapDep.put("jinhuoFinished", jinhuoFinished);

                            mapDep.put("chukuOrder", chukuOrder);
                            mapDep.put("chukuHasWeight", chukuHasWeight);
                            mapDep.put("chukuFinished", jinhuoFinished);

                            mapDep.put("hasPrice", hasPrice);
                            mapDep.put("hasNotPrice", hasNotPrice);

                            mapDep.put("twoSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            gbDisDepArrMap.add(mapDep);
                            System.out.println("*** 添加部门数据到arr ***");
                        } else {
                            System.out.println("*** newOrder <= 0，跳过该部门 ***");
                        }
                    } else {
                        System.out.println("*** stats为null，跳过该部门 ***");
                    }
                }

                System.out.println("最终arr大小: " + gbDisDepArrMap.size());
                System.out.println("arr内容: " + gbDisDepArrMap);
                mapDis.put("arr", gbDisDepArrMap);
                gbList.add(mapDis);
                System.out.println("=== 商家处理完成 ===\n");
            }

            mapData.put("gbDisArrApp", gbList);

        } else {
            mapData.put("gbDisArrApp", new ArrayList<>());
        }


        System.out.println("gbDisArrapp" + mapData);

        //协作订单
        Map<String, Object> mapOffer = new HashMap<>();
        mapOffer.put("disId", disId);
        mapOffer.put("isSelfOrder", 1);
        mapOffer.put("status", 3);
        System.out.println("enennenenennemapOffer" + mapOffer);
        List<NxDistributerEntity> offerNxDistributer = nxDepartmentOrdersService.queryOfferOrderNxDistributer(mapOffer);
        if(offerNxDistributer.size() > 0){
            for(NxDistributerEntity distributerEntity: offerNxDistributer){
                Map<String, Object> mapColl = new HashMap<>();
                mapColl.put("disId", disId);
                mapColl.put("isSelfOrder", 1);
                mapColl.put("status", 3);
                mapColl.put("collNxDisId", distributerEntity.getNxDistributerId());

                int orderCount = nxDepartmentOrdersService.queryOrderGoodsCount(mapColl);
                Map<String, Object> mapPick = new HashMap<>(mapColl);
                mapPick.put("equalPurStatus", 4);
                int pickCount = nxDepartmentOrdersService.queryOrderGoodsCount(mapPick);

                int totalCount = nxDepartmentOrdersService.queryDepOrdersAcount(mapColl);
                mapColl.put("hasPrice", 1);
                int hasPrice = nxDepartmentOrdersService.queryDepOrdersAcount(mapColl);
                mapColl.put("hasPrice", null);
                mapColl.put("hasWeight", 1);
                int hasWeight = nxDepartmentOrdersService.queryDepOrdersAcount(mapColl);
                mapColl.put("hasWeight", null);
                Double subtotal = nxDepartmentOrdersService.queryDepOrdersSubtotal(mapColl);
                String twoSubtotal = subtotal != null && subtotal > 0
                        ? new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString()
                        : "0";

                // unDo: 待修正订单数 (status=-2)
                Map<String, Object> mapUnDo = new HashMap<>(mapColl);
                mapUnDo.put("equalStatus", -2);
                int unDo = nxDepartmentOrdersService.queryDepOrdersAcount(mapUnDo);
                // finishCount: 已完成订单数 (status=2, purchase_status>3, 有价格, 有小计)
                Map<String, Object> mapFinish = new HashMap<>(mapColl);
                mapFinish.put("equalStatus", 2);
                mapFinish.put("dayuPurStatus", 3);
                mapFinish.put("hasPrice", 1);
                mapFinish.put("subtotal", 0);
                int finishCount = nxDepartmentOrdersService.queryDepOrdersAcount(mapFinish);

                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("orderCount", orderCount);
                mapItem.put("pickCount", pickCount);
                mapItem.put("totalCount", totalCount);
                mapItem.put("twoSubtotal", twoSubtotal);
                mapItem.put("hasPrice", hasPrice);
                mapItem.put("hasWeight", hasWeight);
                mapItem.put("unDo", unDo);
                mapItem.put("finishCount", finishCount);
                distributerEntity.setAaa(mapItem);
            }
        }

//        //GbData
//        Map<String, Object> mapPB = new HashMap<>();
//        mapPB.put("nxDisId", disId);
//        mapPB.put("status", 3);
//        System.out.println("enennenenenne" + mapPB);
//        List<GbDistributerPurchaseBatchEntity> entities = gbDistributerPurchaseBatchService.queryDisPurchaseBatch(mapPB);
//
//        if (entities.size() > 0) {
//
//            for (GbDistributerPurchaseBatchEntity batchEntity : entities) {
//
//                Map<String, Object> mapDis = new HashMap<>();
//                mapDis.put("nxDisId", disId);
//                mapDis.put("disId", batchEntity.getGbDpbDistributerId());
//                mapDis.put("status", 3);
//                System.out.println("whwhwhwhhwhwhwhpururur000" + mapDis);
//                Integer goodsCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapDis);
//                mapDis.put("hasPrice", -1);
//                System.out.println("whwhwhwhhwhwhwhpururur111" + mapDis);
//                Integer hasNotPrice = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapDis);
//                mapDis.put("hasPrice", 1);
//                System.out.println("whwhwhwhhwhwhwhpururur222" + mapDis);
//                Integer hasPriceCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(mapDis);
//
//            }
//
//            mapData.put("gbDisArr", entities);
//
//        } else {
//            mapData.put("gbDisArr", new ArrayList<>());
//        }

        Map<String, Object> mapNx = new HashMap<>();
        mapNx.put("orderDisId", disId);
        mapNx.put("equalStatus", 0);
        System.out.println("mapnxxnnxnxnx" + mapNx);
        Integer nxBillCount = nxDistributerBillService.queryDisPurchaseBatchCount(mapNx);
        mapNx.put("orderDisId", null);
        mapNx.put("offerDisId", disId);
        mapNx.put("equalStatus", 2);
        System.out.println("mapnxxnnxnxnx" + mapNx);
        Integer nxBillCount2 = nxDistributerBillService.queryDisPurchaseBatchCount(mapNx);

        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("purStatus", 4);
        map111.put("dayuOrderStatus", -2);
        // 出库
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);

        map111.put("goodsType", 1);
        System.out.println("purirnirinrmap11111" + map111);
//        map111.put("batchId", 0);
//        int unPurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);
//        map111.put("batchId", 1);
        int puringCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map111);


        Map<String, Object> mapB = new HashMap<>();
        mapB.put("disId", disId);
        mapB.put("settleType", 0);
        mapB.put("equalStatus", 0);
        int unPayCount = nxDepartmentBillService.queryBillsCount(mapB);

        //return
        Map<String, Object> mapReturn = new HashMap<>();
        mapReturn.put("disId", disId);
        mapReturn.put("equalStatus", -1);
        System.out.println("rututtntnmaappa" + mapReturn);
        List<NxDepartmentBillEntity> billEntityList = nxDepartmentBillService.queryReturnBill(mapReturn);
//        Map<String, Object> mapGb = new HashMap<>();
//        mapGb.put("disId", disId);
//        mapGb.put("gbDepFatherIdNotEqual", -1);
//        mapGb.put("status", 3);
//        System.out.println("usnbdidiid" + mapGb);
//        int i = nxDepartmentBillService.queryBillsCount(mapGb);

        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("disId", disId);
        queryMap.put("xiaoyuStatus", 3);
        System.out.println("queryMapqueryMapqueryMap" + queryMap);

        List<NxOcrTaskEntity> nxOcrTaskEntities = nxOcrTaskService.queryTasksByDepartmentAndStatus(queryMap);

        Map<String, Object> map7 = new HashMap<>();
        map7.put("disId", disId);
        map7.put("status", -1);
        map7.put("agent", 1);
        int unDoTotal = nxDepartmentOrdersService.queryDepOrdersAcount(map7);

        Map<String, Object> mapLinshi = new HashMap<>();
        mapLinshi.put("disId", disId);
        mapLinshi.put("isLinshi", 1);
        System.out.println("linsshsisiss" + mapLinshi);
        int wxCountAuto1 = nxDistributerGoodsService.queryDisGoodsTotal(mapLinshi);

        //协作订单数量
        Map<String, Object> mapcoll = new HashMap<>();
        mapcoll.put("disId", disId);
        mapcoll.put("purStatus", 4);
        mapcoll.put("status", 3);
        mapcoll.put("hasCollOrder", 1);

        Integer integer = nxDepartmentOrdersService.queryOrderGoodsCount(mapcoll);


        returnData.put("collCount", integer);
        returnData.put("stockCount", stockCount);
        returnData.put("puringCount", puringCount);
        returnData.put("unPayCount", unPayCount);
        returnData.put("deps", mapData);
        returnData.put("disInfo", nxDistributerService.queryDistributerInfo(disId));
        returnData.put("returnList", billEntityList);
        returnData.put("unDoTotal", unDoTotal);
        returnData.put("linshiTotal", wxCountAuto1);
        returnData.put("taskArr", nxOcrTaskEntities);
        returnData.put("offerArr", offerNxDistributer);
        returnData.put("nxBillCount", nxBillCount + nxBillCount2);
        return R.ok().put("data", returnData);

    }


    @RequestMapping(value = "/getBooks")
    @ResponseBody
    public R getBooks() {
        List<NxGoodsEntity> books = nxGoodsService.queryNumberGoods();

        System.out.println("getBooksgetBooksgetBooks");
        return R.ok().put("data", books);
    }


    @RequestMapping(value = "/nxDisGetGbBatchOrders/{id}")
    @ResponseBody
    public R nxDisGetGbBatchOrders(@PathVariable Integer id) {
        GbDistributerPurchaseBatchEntity batchEntity = gbDistributerPurchaseBatchService.queryObject(id);

        Map<String, Object> mapG = new HashMap<>();
        mapG.put("batchId", id);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(mapG);

        List<NxDepartmentOrdersEntity> list = new ArrayList<>();

        if (purchaseGoodsEntities.size() > 0) {
            for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                Integer purchaseGoodsId = purchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", purchaseGoodsId);
                List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
                if (gbDepartmentOrdersEntities.size() > 0) {
                    for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
                        Integer gbDoNxDepartmentOrderId = gbDepartmentOrdersEntity.getGbDoNxDepartmentOrderId();
                        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
                        list.add(ordersEntity);
                    }
                }
            }
        }
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(batchEntity.getGbDpbNxDistributerId());
        String headPinyin = getHeadStringByString(nxDistributerEntity.getNxDistributerName(), true, null);
        String s = headPinyin + "-" + formatDayNumber(0) + "-" + myRandom();
        Map<String, Object> map = new HashMap<>();
        map.put("batchId", id);
        Double aDouble = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", list);
        mapR.put("totalHanzi", convertDoubleToChineseCurrency(aDouble));
        mapR.put("total", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP));
        mapR.put("tradeNo", s);
        mapR.put("distributer", nxDistributerEntity);

        return R.ok().put("data", mapR);
    }

    @RequestMapping(value = "/nxDisGetGbBatchOrdersUnOut/{id}")
    @ResponseBody
    public R nxDisGetGbBatchOrdersUnOut(@PathVariable Integer id) {
//        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByBatchId(id);
        Map<String, Object> mapG = new HashMap<>();
        mapG.put("batchId", id);
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(mapG);
        List<NxDepartmentOrdersEntity> list = new ArrayList<>();

        if (purchaseGoodsEntities.size() > 0) {
            for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                Integer purchaseGoodsId = purchaseGoodsEntity.getGbDistributerPurchaseGoodsId();
                Map<String, Object> map = new HashMap<>();
                map.put("purGoodsId", purchaseGoodsId);
                List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(map);
                if (gbDepartmentOrdersEntities.size() > 0) {
                    for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
                        Integer gbDoNxDepartmentOrderId = gbDepartmentOrdersEntity.getGbDoNxDepartmentOrderId();
                        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(gbDoNxDepartmentOrderId);
                        if (ordersEntity.getNxDoPurchaseStatus() < 4) {
                            list.add(ordersEntity);
                        }

                    }
                }
            }
        }
        return R.ok().put("data", list);
    }

    @RequestMapping(value = "/disInitOrderStatus", method = RequestMethod.POST)
    @ResponseBody
    public R disInitOrderStatus(@RequestBody NxDepartmentOrdersEntity ordersEntity) {

        Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
        if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
            if (purchaseGoodsEntity != null) {
                Integer nxDpgBatchId = purchaseGoodsEntity.getNxDpgBatchId();
                if (nxDpgBatchId == null) {
                    BigDecimal orderCount = new BigDecimal(0);
                    if (purchaseGoodsEntity.getNxDpgOrdersAmount() != null && purchaseGoodsEntity.getNxDpgOrdersAmount() > 0) {
                        orderCount = new BigDecimal(purchaseGoodsEntity.getNxDpgOrdersAmount()).subtract(new BigDecimal(1));
                    }
                    if (orderCount.compareTo(BigDecimal.ZERO) == 0) {
                        nxDistributerPurchaseGoodsService.delete(nxDoPurchaseGoodsId);
                    } else {
                        purchaseGoodsEntity.setNxDpgOrdersAmount(Integer.valueOf(orderCount.toString()));
                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                } else {
                    return R.error(-1, "请采购员先删除对外订货");
                }
            } else {
                System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
            }
        }

        ordersEntity.setNxDoPurchaseGoodsId(-1);
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        ordersEntity.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrdersService.update(ordersEntity);

        if (ordersEntity.getNxDoGbDepartmentOrderId() != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(ordersEntity.getNxDoGbDepartmentOrderId());
            gbDepartmentOrdersEntity.setGbDoStatus(0);
            gbDepartmentOrdersEntity.setGbDoBuyStatus(0);
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }

        return R.ok();
    }

    @RequestMapping(value = "/cancelOutOrder", method = RequestMethod.POST)
    @ResponseBody
    public R cancelOutOrder(@RequestBody NxDepartmentOrdersEntity ordersEntity) {
        Integer nxDepartmentOrderId = ordersEntity.getNxDepartmentOrdersId();
        NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObject(nxDepartmentOrderId);

        ordersEntity1.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        ordersEntity1.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrdersService.update(ordersEntity1);


        Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
        if (nxDoPurchaseGoodsId != null && nxDoPurchaseGoodsId != -1) {
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
            if (purchaseGoodsEntity != null) {
                Integer nxDpgFinishAmount = purchaseGoodsEntity.getNxDpgFinishAmount();
                System.out.println("purGoodsss" + nxDpgFinishAmount);
                if (nxDpgFinishAmount != null && nxDpgFinishAmount > 0) {
                    purchaseGoodsEntity.setNxDpgFinishAmount(nxDpgFinishAmount - 1);
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

                    //查询采购批次如果是订货批次purchaseType= 2，那么就修改订货批次为getNxDisPurchaseBatchSellerReply。
                    if (purchaseGoodsEntity.getNxDpgBatchId() != null && purchaseGoodsEntity.getNxDpgBatchId() != -1) {

                        NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = nxDistributerPurchaseBatchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());
                        if (nxDistributerPurchaseBatchEntity.getNxDpbStatus() == getNxDisPurchaseBatchDisUserFinish())
                            nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchSellerReply());
                        nxDistributerPurchaseBatchService.update(nxDistributerPurchaseBatchEntity);
                    }
                }
            } else {
                System.out.println("警告：采购商品不存在，ID: " + nxDoPurchaseGoodsId);
            }
        }
        if (ordersEntity1.getNxDoGbDepartmentOrderId() != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(ordersEntity.getNxDoGbDepartmentOrderId());
            gbDepartmentOrdersEntity.setGbDoStatus(0);
            gbDepartmentOrdersEntity.setGbDoBuyStatus(0);
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }


        //查询该订单的库存扣减记录，恢复库存并删除扣减记录
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", ordersEntity.getNxDepartmentOrdersId());
        List<NxDistributerGoodsShelfStockReduceEntity> reduceEntities = nxDisGoodsShelfStockReduceService.queryReduceListByParams(map);
        if (reduceEntities.size() > 0) {
            System.out.println("开始恢复库存 - 找到 " + reduceEntities.size() + " 条扣减记录");
            for (NxDistributerGoodsShelfStockReduceEntity reduceEntity : reduceEntities) {
                Integer nxDgssrNxStockId = reduceEntity.getNxDgssrNxStockId();
                NxDistributerGoodsShelfStockEntity stockEntity = nxDisGoodsShelfStockService.queryObject(nxDgssrNxStockId);

                // 恢复库存数量和成本
                BigDecimal restWeight = new BigDecimal(stockEntity.getNxDgssRestWeight()).add(new BigDecimal(reduceEntity.getNxDgssrCostWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal restSubtotal = new BigDecimal(stockEntity.getNxDgssRestSubtotal()).add(new BigDecimal(reduceEntity.getNxDgssrCostSubtotal())).setScale(1, BigDecimal.ROUND_HALF_UP);
                stockEntity.setNxDgssRestWeight(restWeight.toString());
                stockEntity.setNxDgssRestSubtotal(restSubtotal.toString());
                nxDisGoodsShelfStockService.update(stockEntity);

                System.out.println("恢复批次 " + nxDgssrNxStockId + " - 恢复重量: " + reduceEntity.getNxDgssrCostWeight() +
                        ", 新剩余: " + restWeight);

                // 删除扣减记录
                nxDisGoodsShelfStockReduceService.delete(reduceEntity.getNxDistributerGoodsShelfStockReduceId());
            }
            System.out.println("库存恢复完成");
        }

        //更新协作订单
        System.out.println("colllllmap" + ordersEntity1.getNxDoCollaborativeNxDisId());

        if(ordersEntity1.getNxDoCollaborativeNxDisId() != -1){
            Integer nxDoCollaborativeNxDisId = ordersEntity1.getNxDoCollaborativeNxDisId();
            Integer nxDepartmentOrdersId = ordersEntity1.getNxDepartmentOrdersId();
            Map<String, Object> mapColl = new HashMap<>();
            mapColl.put("disId", nxDoCollaborativeNxDisId);
            mapColl.put("restrauntId", nxDepartmentOrdersId);
            System.out.println("colllllmap" + mapColl);
            NxDepartmentOrdersEntity collOrder =  nxDepartmentOrdersService.querycollOrder(mapColl);
            if(collOrder != null && collOrder.getNxDoStatus() < 2){
                collOrder.setNxDoPurchaseStatus(0);
                collOrder.setNxDoWeight(null);
                nxDepartmentOrdersService.update(collOrder);
            }
        }

        return R.ok();
    }

    @RequestMapping(value = "/cancelOutOrderAdmin", method = RequestMethod.POST)
    @ResponseBody
    public R cancelOutOrderAdmin(@RequestBody NxDepartmentOrdersEntity ordersEntity) {
        Integer nxDepartmentOrderId = ordersEntity.getNxDepartmentOrdersId();
        NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObject(nxDepartmentOrderId);

        ordersEntity1.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        ordersEntity1.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrdersService.update(ordersEntity1);


        Map<String, Object> map = new HashMap<>();
        map.put("orderId", ordersEntity.getNxDepartmentOrdersId());
        List<NxDistributerGoodsShelfStockReduceEntity> reduceEntities = nxDisGoodsShelfStockReduceService.queryReduceListByParams(map);
        if (reduceEntities.size() > 0) {
            for (NxDistributerGoodsShelfStockReduceEntity reduceEntity : reduceEntities) {
                Integer nxDgssrNxStockId = reduceEntity.getNxDgssrNxStockId();
                NxDistributerGoodsShelfStockEntity stockEntity = nxDisGoodsShelfStockService.queryObject(nxDgssrNxStockId);

                BigDecimal restWeight = new BigDecimal(stockEntity.getNxDgssRestWeight()).add(new BigDecimal(reduceEntity.getNxDgssrCostWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal restSubtotal = new BigDecimal(stockEntity.getNxDgssRestSubtotal()).add(new BigDecimal(reduceEntity.getNxDgssrCostSubtotal())).setScale(1, BigDecimal.ROUND_HALF_UP);
                stockEntity.setNxDgssRestWeight(restWeight.toString());
                stockEntity.setNxDgssRestSubtotal(restSubtotal.toString());
                nxDisGoodsShelfStockService.update(stockEntity);

                nxDisGoodsShelfStockReduceService.delete(reduceEntity.getNxDistributerGoodsShelfStockReduceId());
            }
        }

        if (ordersEntity1.getNxDoGbDepartmentOrderId() != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(ordersEntity.getNxDoGbDepartmentOrderId());
            gbDepartmentOrdersEntity.setGbDoStatus(0);
            gbDepartmentOrdersEntity.setGbDoBuyStatus(0);
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }

        return R.ok();
    }

    /**
     * ORDER
     * 修改申请
     *
     * @param
     * @return ok
     */
    @ResponseBody
    @RequestMapping(value = "/updateOrder", method = RequestMethod.POST)
    public R updateOrder(Integer id, String weight, String standard, String remark, String printStandard, String priceLevel) {
        System.out.println("updatedoorpr" + weight);

        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(id);

        System.out.println("depdisididiidididdiid" + ordersEntity.getNxDoDepDisGoodsId());
        String oldNxDoQuantity = ordersEntity.getNxDoQuantity();

        // 检查规格是否变化：如果新规格和旧规格不一样，单价和小计设置为null
        String oldStandard = ordersEntity.getNxDoStandard();
        boolean standardChanged = false;
        if (oldStandard != null && standard != null && !oldStandard.trim().equals(standard.trim())) {
            System.out.println("⚠️ 规格已变化: 旧规格[" + oldStandard + "] -> 新规格[" + standard + "]，单价和小计将设置为null");
            standardChanged = true;
            ordersEntity.setNxDoPrice(null);
            ordersEntity.setNxDoSubtotal(null);
        } else if (oldStandard == null && standard != null) {
            System.out.println("⚠️ 规格从null变为[" + standard + "]，单价和小计将设置为null");
            standardChanged = true;
            ordersEntity.setNxDoPrice(null);
            ordersEntity.setNxDoSubtotal(null);
        } else if (oldStandard != null && standard == null) {
            System.out.println("⚠️ 规格从[" + oldStandard + "]变为null，单价和小计将设置为null");
            standardChanged = true;
            ordersEntity.setNxDoPrice(null);
            ordersEntity.setNxDoSubtotal(null);
        } else {
            System.out.println("✅ 规格未变化: [" + oldStandard + "] = [" + standard + "]");
        }

        //自动添加重量和单价小计
        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetailWithLinshi(doDisGoodsId);
        String nxDgGoodsStandardname = distributerGoodsEntity.getNxDgGoodsStandardname();
        System.out.println("willPriceTwowillPriceTwowillPriceTwo==" + priceLevel);

        // 单价、成本价、打印规格、小计、利润统一走 service 入口，避免控制器与新增订单逻辑分叉
        System.out.println("[updateOrder] 统一走 processOrderPrice，priceLevel仅作前端参数保留，不在控制器内直接改价");


        //purGoods
        if (ordersEntity.getNxDoPurchaseGoodsId() != -1) {
            Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();

            NxDistributerPurchaseGoodsEntity oldPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
            //standard changed
            if (!standard.equals(oldPurchaseGoodsEntity.getNxDpgStandard())) {
                if (oldPurchaseGoodsEntity.getNxDpgOrdersAmount() == 1) {
                    oldPurchaseGoodsEntity.setNxDpgStandard(standard);
                    oldPurchaseGoodsEntity.setNxDpgQuantity(weight);
                    nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);
                } else {

                    BigDecimal purQuantity = new BigDecimal(oldPurchaseGoodsEntity.getNxDpgQuantity());
                    BigDecimal add = purQuantity.subtract(new BigDecimal(ordersEntity.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    System.out.println("updidddidid" + add);
                    oldPurchaseGoodsEntity.setNxDpgQuantity(add.toString());
                    oldPurchaseGoodsEntity.setNxDpgOrdersAmount(oldPurchaseGoodsEntity.getNxDpgOrdersAmount() - 1);
                    NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
                    if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                        BigDecimal totaoWeight = new BigDecimal(weight);
                        oldPurchaseGoodsEntity.setNxDpgBuyQuantity(add.toString());
                        // Check if buy price is not null or empty before calculating
                        String buyPrice = oldPurchaseGoodsEntity.getNxDpgBuyPrice();
                        if (buyPrice != null && !buyPrice.trim().isEmpty()) {
                            BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(buyPrice)).setScale(1, BigDecimal.ROUND_HALF_UP);
                            purchaseGoodsEntity.setNxDpgBuyQuantity(weight);
                            purchaseGoodsEntity.setNxDpgBuySubtotal(decimal2.toString());
                        } else {
                            purchaseGoodsEntity.setNxDpgBuyQuantity(weight);
                            purchaseGoodsEntity.setNxDpgBuySubtotal("0");
                        }
                    }

                    nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);

                    purchaseGoodsEntity.setNxDpgDistributerId(distributerGoodsEntity.getNxDgDistributerId());
                    purchaseGoodsEntity.setNxDpgDisGoodsFatherId(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
                    purchaseGoodsEntity.setNxDpgDisGoodsGrandId(distributerGoodsEntity.getNxDgDfgGoodsGrandId());
                    purchaseGoodsEntity.setNxDpgDistributerId(distributerGoodsEntity.getNxDgDistributerId());
                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
                    purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
                    purchaseGoodsEntity.setNxDpgOrdersAmount(1);
                    purchaseGoodsEntity.setNxDpgFinishAmount(0);
                    purchaseGoodsEntity.setNxDpgPurchaseType(getGbPurchaseGoodsTypeForOrder());
                    purchaseGoodsEntity.setNxDpgExpectPrice(distributerGoodsEntity.getNxDgBuyingPrice());
                    purchaseGoodsEntity.setNxDpgBuyPrice(distributerGoodsEntity.getNxDgBuyingPrice());
                    purchaseGoodsEntity.setNxDpgDisGoodsId(doDisGoodsId);
                    purchaseGoodsEntity.setNxDpgInputType(distributerGoodsEntity.getNxDgPurchaseAuto());
                    purchaseGoodsEntity.setNxDpgStandard(standard);
                    purchaseGoodsEntity.setNxDpgQuantity(ordersEntity.getNxDoQuantity());

                    nxDistributerPurchaseGoodsService.save(purchaseGoodsEntity);
                    Integer nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
                    ordersEntity.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
                }

            } else {
                System.out.println("hehehehrhereer===" + nxDgGoodsStandardname);
                BigDecimal purQuantity = new BigDecimal(oldPurchaseGoodsEntity.getNxDpgQuantity());
                BigDecimal add = purQuantity.subtract(new BigDecimal(oldNxDoQuantity)).add(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
                oldPurchaseGoodsEntity.setNxDpgQuantity(add.toString());
                oldPurchaseGoodsEntity.setNxDpgBuyQuantity(add.toString());
                if (standard.equals(nxDgGoodsStandardname)) {
                    System.out.println("hehehehrhereer===" + nxDgGoodsStandardname);
                    // Check if buy price is not null or empty before calculating
                    String buyPrice = oldPurchaseGoodsEntity.getNxDpgBuyPrice();
                    if (buyPrice != null && !buyPrice.trim().isEmpty()) {
                        BigDecimal decimal = new BigDecimal(buyPrice).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
                        oldPurchaseGoodsEntity.setNxDpgBuySubtotal(decimal.toString());
                    } else {
                        // If buy price is empty, set subtotal to 0
                        oldPurchaseGoodsEntity.setNxDpgBuySubtotal("0");
                    }
                }
                nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);

            }
        }

        ordersEntity.setNxDoQuantity(weight);
        ordersEntity.setNxDoStandard(standard);
        ordersEntity.setNxDoRemark(remark);

        // 仅保留部门商品ID绑定，价格计算统一交给 service
        Map<String, Object> depGoodsMap = new HashMap<>();
        depGoodsMap.put("depId", ordersEntity.getNxDoDepartmentId());
        depGoodsMap.put("disGoodsId", distributerGoodsEntity.getNxDistributerGoodsId());
        depGoodsMap.put("standard", standard);
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(depGoodsMap);
        if (departmentDisGoodsEntity != null) {
            ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
        }
        else {
            depGoodsMap.put("standard", null);
            NxDepartmentDisGoodsEntity departmentDisGoodsEntityO = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(depGoodsMap);
            if (departmentDisGoodsEntityO != null) {
                ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
            }
        }

        nxDepartmentOrdersService.processOrderPrice(ordersEntity, distributerGoodsEntity);
        nxDepartmentOrdersService.update(ordersEntity);

        ordersEntity.setNxDistributerGoodsEntity(distributerGoodsEntity);
        return R.ok().put("data", ordersEntity);
    }



//    @ResponseBody
//    @RequestMapping(value = "/updateOrder", method = RequestMethod.POST)
//    public R updateOrder(Integer id, String weight, String standard, String remark, String printStandard, String priceLevel) {
//        System.out.println("updatedoorpr" + weight);
//
//        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(id);
//
//        System.out.println("depdisididiidididdiid" + ordersEntity.getNxDoDepDisGoodsId());
//        String oldNxDoQuantity = ordersEntity.getNxDoQuantity();
//
//        // 检查规格是否变化：如果新规格和旧规格不一样，单价和小计设置为null
//        String oldStandard = ordersEntity.getNxDoStandard();
//        boolean standardChanged = false;
//        if (oldStandard != null && standard != null && !oldStandard.trim().equals(standard.trim())) {
//            System.out.println("⚠️ 规格已变化: 旧规格[" + oldStandard + "] -> 新规格[" + standard + "]，单价和小计将设置为null");
//            standardChanged = true;
//            ordersEntity.setNxDoPrice(null);
//            ordersEntity.setNxDoSubtotal(null);
//        } else if (oldStandard == null && standard != null) {
//            System.out.println("⚠️ 规格从null变为[" + standard + "]，单价和小计将设置为null");
//            standardChanged = true;
//            ordersEntity.setNxDoPrice(null);
//            ordersEntity.setNxDoSubtotal(null);
//        } else if (oldStandard != null && standard == null) {
//            System.out.println("⚠️ 规格从[" + oldStandard + "]变为null，单价和小计将设置为null");
//            standardChanged = true;
//            ordersEntity.setNxDoPrice(null);
//            ordersEntity.setNxDoSubtotal(null);
//        } else {
//            System.out.println("✅ 规格未变化: [" + oldStandard + "] = [" + standard + "]");
//        }
//
//        //自动添加重量和单价小计
//        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
//        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetailWithLinshi(doDisGoodsId);
//        String nxDgGoodsStandardname = distributerGoodsEntity.getNxDgGoodsStandardname();
//        System.out.println("willPriceTwowillPriceTwowillPriceTwo==" + priceLevel);
//
//        if (priceLevel.equals("1")) {
//
//            Integer nxDoDepartmentId = ordersEntity.getNxDoDepartmentId();
//            Map<String, Object> map = new HashMap<>();
//            map.put("depId", nxDoDepartmentId);
//            map.put("standard", ordersEntity.getNxDoStandard());
//            map.put("disGoodsId", distributerGoodsEntity.getNxDistributerGoodsId());
//            System.out.println("zahusimdkd" + map);
//            NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
//
//            // 如果规格变化了，不重新设置价格，保持为null
//            // 必须确保部门商品的规格(ddgOrderStandard)和订单规格(standard)一致，才能给单价赋值
//            if (nxDepartmentDisGoodsEntity != null && !standardChanged) {
//                String departmentStandard = nxDepartmentDisGoodsEntity.getNxDdgOrderStandard();
//                // 检查部门商品规格和订单规格是否一致
//                boolean standardMatch = (departmentStandard == null && standard == null)
//                        || (departmentStandard != null && standard != null
//                        && departmentStandard.trim().equals(standard.trim()));
//                if (standardMatch) {
//                    ordersEntity.setNxDoPrice(nxDepartmentDisGoodsEntity.getNxDdgOrderPrice());
//                    System.out.println("✅ 部门商品规格匹配，设置价格: " + nxDepartmentDisGoodsEntity.getNxDdgOrderPrice());
//                } else {
//                    System.out.println("⚠️ 部门商品规格不匹配，跳过价格设置。部门商品规格: [" + departmentStandard
//                            + "], 订单规格: [" + standard + "]");
//                }
//            } else if (standardChanged) {
//                System.out.println("⚠️ 规格已变化，跳过价格设置，保持nxDoPrice为null");
//            }
//
//            ordersEntity.setNxDoCostPrice(distributerGoodsEntity.getNxDgBuyingPriceOne());
//            ordersEntity.setNxDoCostPriceUpdate(distributerGoodsEntity.getNxDgBuyingPriceOneUpdate());
//
//            System.out.println("priririeieiieieieiiei0000nnnn" + ordersEntity.getNxDoPrice());
//
//            // 参考 saveOneOrder 的逻辑：检查订单规格是否等于商品标准规格，或者订单规格是否匹配大包装单位（智能匹配：件=箱）
//            boolean isStandardMatch = standard != null && standard.equals(nxDgGoodsStandardname);
//            boolean isCartonMatch = distributerGoodsEntity.getNxDgCartonUnit() != null
//                    && !distributerGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
//                    && standard != null
//                    && isStandardMatch(standard.trim(), distributerGoodsEntity.getNxDgCartonUnit().trim());
//
//            if (isStandardMatch || isCartonMatch) {
//                System.out.println("订单规格匹配: isStandardMatch=" + isStandardMatch + ", isCartonMatch=" + isCartonMatch);
//                ordersEntity.setNxDoWeight(weight);
//                // 检查成本价格是否为有效数字
//                String costPrice = ordersEntity.getNxDoCostPrice();
//                if (costPrice == null || costPrice.trim().isEmpty()) {
//                    costPrice = "0";
//                }
//                try {
//                    // 参考 saveOneOrder 的逻辑：判断是否使用外包装单价计算（支持智能匹配：件=箱）
//                    BigDecimal doQuantity = new BigDecimal(weight);
//                    boolean useCartonPriceForCalc = false;
//                    BigDecimal itemsPerCartonBdForCalc = null;
//                    if (distributerGoodsEntity.getNxDgCartonUnit() != null
//                            && !distributerGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
//                            && standard != null
//                            && isStandardMatch(standard.trim(), distributerGoodsEntity.getNxDgCartonUnit().trim())) {
//                        itemsPerCartonBdForCalc = CommonUtils.parseItemsPerCartonBigDecimal(distributerGoodsEntity.getNxDgItemsPerCarton());
//                        if (itemsPerCartonBdForCalc != null && itemsPerCartonBdForCalc.compareTo(BigDecimal.ZERO) > 0) {
//                            useCartonPriceForCalc = true;
//                            System.out.println("订单规格匹配外包装单位（智能匹配），计算时将数量转换为箱数: " + doQuantity + "个 ÷ " + itemsPerCartonBdForCalc + " = " + doQuantity.divide(itemsPerCartonBdForCalc, 4, BigDecimal.ROUND_HALF_UP) + "箱");
//                        }
//                    }
//
//                    // 计算成本小计
//                    BigDecimal costSubtotalCalc = null;
//                    if (useCartonPriceForCalc && itemsPerCartonBdForCalc != null) {
//                        // 使用外包装成本价：需要将数量转换为箱数
//                        BigDecimal cartonCount = doQuantity.divide(itemsPerCartonBdForCalc, 4, BigDecimal.ROUND_HALF_UP);
//                        BigDecimal costPriceBD = new BigDecimal(costPrice);
//                        costSubtotalCalc = cartonCount.multiply(costPriceBD).setScale(1, BigDecimal.ROUND_HALF_UP);
//
//                    } else {
//                        // 使用最小单位成本价：直接相乘
//                        BigDecimal costPriceBD = new BigDecimal(costPrice);
//                        costSubtotalCalc = doQuantity.multiply(costPriceBD).setScale(1, BigDecimal.ROUND_HALF_UP);
//
//                    }
//                    ordersEntity.setNxDoCostSubtotal(costSubtotalCalc.toString());
//
//                    System.out.println("priririeieiieieieiiei1111" + ordersEntity.getNxDoPrice());
//                    if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
//                        // 计算销售小计
//                        BigDecimal willPrice = new BigDecimal(ordersEntity.getNxDoPrice());
//                        BigDecimal subtotal;
//                        if (useCartonPriceForCalc && itemsPerCartonBdForCalc != null) {
//                            // 使用外包装单价：需要将数量转换为箱数
//                            BigDecimal cartonCount = doQuantity.divide(itemsPerCartonBdForCalc, 4, BigDecimal.ROUND_HALF_UP);
//                            subtotal = cartonCount.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//
//                        } else {
//                            // 使用最小单位单价：直接相乘
//                            subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//
//                        }
//
//                        ordersEntity.setNxDoSubtotal(subtotal.toString());
//
//                        //profit - 参考 saveOneOrder 的逻辑，使用已计算好的 subtotal 和 costSubtotal
//                        if (costSubtotalCalc != null && subtotal != null) {
//                            BigDecimal profitB = subtotal.subtract(costSubtotalCalc).setScale(1, BigDecimal.ROUND_HALF_UP);
//                            BigDecimal scaleB = BigDecimal.ZERO;
//                            if (subtotal.compareTo(BigDecimal.ZERO) != 0) {
//                                scaleB = profitB.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
//                            }
//                            ordersEntity.setNxDoProfitScale(scaleB.toString());
//                            ordersEntity.setNxDoProfitSubtotal(profitB.toString());
//
//                            if (ordersEntity.getNxDoExpectPrice() != null && !ordersEntity.getNxDoExpectPrice().trim().isEmpty()) {
//                                BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
//                                BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
//                                BigDecimal subtract = doPrice.subtract(expectPrice);
//                                ordersEntity.setNxDoPriceDifferent(subtract.toString());
//                            }
//                        }
//                    }
//                } catch (NumberFormatException e) {
//                    ordersEntity.setNxDoCostSubtotal("0");
//                    ordersEntity.setNxDoProfitScale("0");
//                    ordersEntity.setNxDoProfitSubtotal("0");
//                    ordersEntity.setNxDoPriceDifferent("0");
//                }
//            } else {
//                ordersEntity.setNxDoWeight(null);
//                ordersEntity.setNxDoSubtotal(null);
//                ordersEntity.setNxDoCostSubtotal("0");
//                ordersEntity.setNxDoProfitScale("0");
//                ordersEntity.setNxDoProfitSubtotal("0");
//                ordersEntity.setNxDoPriceDifferent("0");
//
//            }
//        } else if (priceLevel.equals("2")){
//
//            // 检查价格是否为有效数字
//            String willPriceTwo = distributerGoodsEntity.getNxDgWillPriceTwo();
//            System.out.println("willPriceTwowillPriceTwowillPriceTwo==" + willPriceTwo);
//            if (willPriceTwo == null || willPriceTwo.trim().isEmpty()) {
//                willPriceTwo = "0";
//            }
//
//            try {
//                ordersEntity.setNxDoPrice(willPriceTwo);
//                BigDecimal bigDecimal = new BigDecimal(weight).multiply(new BigDecimal(willPriceTwo)).setScale(1, BigDecimal.ROUND_HALF_UP);
//                ordersEntity.setNxDoWeight(weight);
//
//                Integer nxDoDepartmentId = ordersEntity.getNxDoDepartmentId();
//                Map<String, Object> map = new HashMap<>();
//                map.put("depId", nxDoDepartmentId);
//                map.put("standard", ordersEntity.getNxDoStandard());
//                map.put("disGoodsId", distributerGoodsEntity.getNxDistributerGoodsId());
//                System.out.println("zahusimdkd" + map);
//                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
//                // 如果规格变化了，不重新设置价格，保持为null
//                // 必须确保部门商品的规格(ddgOrderStandard)和订单规格(standard)一致，才能给单价赋值
//                if (nxDepartmentDisGoodsEntity != null && !standardChanged) {
//                    String departmentStandard = nxDepartmentDisGoodsEntity.getNxDdgOrderStandard();
//                    // 检查部门商品规格和订单规格是否一致
//                    boolean standardMatch = (departmentStandard == null && standard == null)
//                            || (departmentStandard != null && standard != null
//                            && departmentStandard.trim().equals(standard.trim()));
//                    if (standardMatch) {
//                        ordersEntity.setNxDoPrice(nxDepartmentDisGoodsEntity.getNxDdgOrderPrice());
//                        System.out.println("✅ 部门商品规格匹配，设置价格: " + nxDepartmentDisGoodsEntity.getNxDdgOrderPrice());
//                    } else {
//                        System.out.println("⚠️ 部门商品规格不匹配，跳过价格设置。部门商品规格: [" + departmentStandard
//                                + "], 订单规格: [" + standard + "]");
//                    }
//                } else if (standardChanged) {
//                    System.out.println("⚠️ 规格已变化，跳过价格设置，保持nxDoPrice为null");
//                }
//                // 如果规格变化了，小计也设置为null
//                if (!standardChanged) {
//                    ordersEntity.setNxDoSubtotal(bigDecimal.toString());
//                } else {
//                    ordersEntity.setNxDoSubtotal(null);
//                }
//                ordersEntity.setNxDoCostPrice(distributerGoodsEntity.getNxDgBuyingPriceTwo());
//                ordersEntity.setNxDoCostPriceUpdate(distributerGoodsEntity.getNxDgBuyingPriceTwoUpdate());
//
//                // 检查成本价格是否为有效数字
//                String costPriceTwo = ordersEntity.getNxDoCostPrice();
//                if (costPriceTwo == null || costPriceTwo.trim().isEmpty()) {
//                    costPriceTwo = "0";
//                }
//                BigDecimal decimal3 = new BigDecimal(weight).multiply(new BigDecimal(costPriceTwo)).setScale(1, BigDecimal.ROUND_HALF_UP);
//                ordersEntity.setNxDoCostSubtotal(decimal3.toString());
//                BigDecimal weightB = new BigDecimal(ordersEntity.getNxDoWeight());
//
//                System.out.println("priciie" + ordersEntity.getNxDoPrice());
//                if (ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
//                    BigDecimal decimal2 = new BigDecimal(ordersEntity.getNxDoPrice());
//                    BigDecimal subtotalB = weightB.multiply(decimal2);
//
//                    BigDecimal nxDoCostPriceB = new BigDecimal(costPriceTwo);  // 使用已验证的 costPriceTwo
//                    BigDecimal decimal1 = nxDoCostPriceB.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    BigDecimal profitB = subtotalB.subtract(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    BigDecimal scaleB = profitB.divide(subtotalB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
//                    ordersEntity.setNxDoProfitScale(scaleB.toString());
//                    ordersEntity.setNxDoProfitSubtotal(profitB.toString());
//
//                    if (ordersEntity.getNxDoExpectPrice() != null && !ordersEntity.getNxDoExpectPrice().trim().isEmpty()) {
//                        BigDecimal expectPrice = new BigDecimal(ordersEntity.getNxDoExpectPrice());
//                        BigDecimal doPrice = new BigDecimal(ordersEntity.getNxDoPrice());
//                        BigDecimal subtract = doPrice.subtract(expectPrice);
//                        ordersEntity.setNxDoPriceDifferent(subtract.toString());
//                    }
//                }
//            } catch (NumberFormatException e) {
//                System.err.println("Invalid number format for price or cost: willPriceTwo=" + willPriceTwo + ", costPriceTwo=" + ", weight=" + weight);
//                ordersEntity.setNxDoSubtotal("0");
//                ordersEntity.setNxDoCostSubtotal("0");
//                ordersEntity.setNxDoProfitScale("0");
//                ordersEntity.setNxDoProfitSubtotal("0");
//                ordersEntity.setNxDoPriceDifferent("0");
//            }
//        }
//
//
//        //purGoods
//        if (ordersEntity.getNxDoPurchaseGoodsId() != -1) {
//            Integer nxDoPurchaseGoodsId = ordersEntity.getNxDoPurchaseGoodsId();
//
//            NxDistributerPurchaseGoodsEntity oldPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(nxDoPurchaseGoodsId);
//            //standard changed
//            if (!standard.equals(oldPurchaseGoodsEntity.getNxDpgStandard())) {
//                if (oldPurchaseGoodsEntity.getNxDpgOrdersAmount() == 1) {
//                    oldPurchaseGoodsEntity.setNxDpgStandard(standard);
//                    oldPurchaseGoodsEntity.setNxDpgQuantity(weight);
//                    nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);
//                } else {
//
//                    BigDecimal purQuantity = new BigDecimal(oldPurchaseGoodsEntity.getNxDpgQuantity());
//                    BigDecimal add = purQuantity.subtract(new BigDecimal(ordersEntity.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    System.out.println("updidddidid" + add);
//                    oldPurchaseGoodsEntity.setNxDpgQuantity(add.toString());
//                    oldPurchaseGoodsEntity.setNxDpgOrdersAmount(oldPurchaseGoodsEntity.getNxDpgOrdersAmount() - 1);
//                    NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
//                    if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
//                        BigDecimal totaoWeight = new BigDecimal(weight);
//                        oldPurchaseGoodsEntity.setNxDpgBuyQuantity(add.toString());
//                        // Check if buy price is not null or empty before calculating
//                        String buyPrice = oldPurchaseGoodsEntity.getNxDpgBuyPrice();
//                        if (buyPrice != null && !buyPrice.trim().isEmpty()) {
//                            BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(buyPrice)).setScale(1, BigDecimal.ROUND_HALF_UP);
//                            purchaseGoodsEntity.setNxDpgBuyQuantity(weight);
//                            purchaseGoodsEntity.setNxDpgBuySubtotal(decimal2.toString());
//                        } else {
//                            purchaseGoodsEntity.setNxDpgBuyQuantity(weight);
//                            purchaseGoodsEntity.setNxDpgBuySubtotal("0");
//                        }
//                    }
//
//                    nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);
//
//                    purchaseGoodsEntity.setNxDpgDistributerId(distributerGoodsEntity.getNxDgDistributerId());
//                    purchaseGoodsEntity.setNxDpgDisGoodsFatherId(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
//                    purchaseGoodsEntity.setNxDpgDisGoodsGrandId(distributerGoodsEntity.getNxDgDfgGoodsGrandId());
//                    purchaseGoodsEntity.setNxDpgDistributerId(distributerGoodsEntity.getNxDgDistributerId());
//                    purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
//                    purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
//                    purchaseGoodsEntity.setNxDpgOrdersAmount(1);
//                    purchaseGoodsEntity.setNxDpgFinishAmount(0);
//                    purchaseGoodsEntity.setNxDpgPurchaseType(getGbPurchaseGoodsTypeForOrder());
//                    purchaseGoodsEntity.setNxDpgExpectPrice(distributerGoodsEntity.getNxDgBuyingPrice());
//                    purchaseGoodsEntity.setNxDpgBuyPrice(distributerGoodsEntity.getNxDgBuyingPrice());
//                    purchaseGoodsEntity.setNxDpgDisGoodsId(doDisGoodsId);
//                    purchaseGoodsEntity.setNxDpgInputType(distributerGoodsEntity.getNxDgPurchaseAuto());
//                    purchaseGoodsEntity.setNxDpgStandard(standard);
//                    purchaseGoodsEntity.setNxDpgQuantity(ordersEntity.getNxDoQuantity());
//
//                    nxDistributerPurchaseGoodsService.save(purchaseGoodsEntity);
//                    Integer nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
//                    ordersEntity.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
//                }
//
//            } else {
//                System.out.println("hehehehrhereer===" + nxDgGoodsStandardname);
//                BigDecimal purQuantity = new BigDecimal(oldPurchaseGoodsEntity.getNxDpgQuantity());
//                BigDecimal add = purQuantity.subtract(new BigDecimal(oldNxDoQuantity)).add(new BigDecimal(weight)).setScale(1, BigDecimal.ROUND_HALF_UP);
//                oldPurchaseGoodsEntity.setNxDpgQuantity(add.toString());
//                oldPurchaseGoodsEntity.setNxDpgBuyQuantity(add.toString());
//                if (standard.equals(nxDgGoodsStandardname)) {
//                    System.out.println("hehehehrhereer===" + nxDgGoodsStandardname);
//                    // Check if buy price is not null or empty before calculating
//                    String buyPrice = oldPurchaseGoodsEntity.getNxDpgBuyPrice();
//                    if (buyPrice != null && !buyPrice.trim().isEmpty()) {
//                        BigDecimal decimal = new BigDecimal(buyPrice).multiply(add).setScale(1, BigDecimal.ROUND_HALF_UP);
//                        oldPurchaseGoodsEntity.setNxDpgBuySubtotal(decimal.toString());
//                    } else {
//                        // If buy price is empty, set subtotal to 0
//                        oldPurchaseGoodsEntity.setNxDpgBuySubtotal("0");
//                    }
//                }
//                nxDistributerPurchaseGoodsService.update(oldPurchaseGoodsEntity);
//
//            }
//        }
//
//        ordersEntity.setNxDoQuantity(weight);
//        ordersEntity.setNxDoStandard(standard);
//        ordersEntity.setNxDoRemark(remark);
//
//        // 智能匹配打印规格：如果订单规格匹配大包装单位，则设置为大包装单位
//        // 支持同义词匹配：件=箱，盒=箱等
//        System.out.println("======== 修改订单-设置打印规格(printStandard)开始 ========");
//        System.out.println("订单ID: " + ordersEntity.getNxDepartmentOrdersId());
//        System.out.println("订单规格(standard): " + standard);
//        System.out.println("前端传入打印规格(printStandard参数): " + printStandard);
//        System.out.println("商品大包装单位(nxDgCartonUnit): " + distributerGoodsEntity.getNxDgCartonUnit());
//        System.out.println("商品标准规格(nxDgGoodsStandardname): " + distributerGoodsEntity.getNxDgGoodsStandardname());
//        System.out.println("设置前 printStandard: " + ordersEntity.getNxDoPrintStandard());
//
//        if (distributerGoodsEntity.getNxDgCartonUnit() != null
//                && !distributerGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
//                && standard != null
//                && isStandardMatch(standard.trim(), distributerGoodsEntity.getNxDgCartonUnit().trim())) {
//            ordersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgCartonUnit());
//            System.out.println("✅ [printStandard] 已设置为大包装单位: " + distributerGoodsEntity.getNxDgCartonUnit());
//        } else if (printStandard != null && !printStandard.trim().isEmpty()) {
//            // 如果前端传入了打印规格，使用前端传入的值
//            ordersEntity.setNxDoPrintStandard(printStandard);
//            System.out.println("✅ [printStandard] 已设置为前端传入值: " + printStandard);
//        } else {
//            // 否则使用商品的标准规格名称
//            ordersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgGoodsStandardname());
//            System.out.println("✅ [printStandard] 已设置为商品标准规格: " + distributerGoodsEntity.getNxDgGoodsStandardname());
//        }
//        System.out.println("设置后 printStandard: " + ordersEntity.getNxDoPrintStandard());
//        System.out.println("======== 修改订单-设置打印规格(printStandard)结束 ========");
//
//        // 参考 saveOneOrder 的逻辑：从部门商品中查找价格（如果前面没有设置价格，或者规格变化后需要重新查找）
//        System.out.println("======== 从部门商品查找价格开始 ========");
//        Map<String, Object> depGoodsMap = new HashMap<>();
//        depGoodsMap.put("depId", ordersEntity.getNxDoDepartmentId());
//        depGoodsMap.put("disGoodsId", distributerGoodsEntity.getNxDistributerGoodsId());
//        depGoodsMap.put("standard", standard);  // 使用新的规格
//        System.out.println("查询部门商品参数: " + depGoodsMap);
//
//        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(depGoodsMap);
//        if (departmentDisGoodsEntity != null) {
//            System.out.println("找到部门商品，规格: " + departmentDisGoodsEntity.getNxDdgOrderStandard());
//            System.out.println("部门商品价格: " + departmentDisGoodsEntity.getNxDdgOrderPrice());
//
//            // 如果订单价格为null（规格变化后），或者需要更新价格，则从部门商品获取
//            // 必须确保部门商品的规格(ddgOrderStandard)和订单规格(standard)一致，才能给单价赋值
//            if (ordersEntity.getNxDoPrice() == null || standardChanged) {
//                String departmentStandard = departmentDisGoodsEntity.getNxDdgOrderStandard();
//                // 检查部门商品规格和订单规格是否一致
//                boolean standardMatch = (departmentStandard == null && standard == null)
//                        || (departmentStandard != null && standard != null
//                        && departmentStandard.trim().equals(standard.trim()));
//                if (standardMatch) {
//                    ordersEntity.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
//                    System.out.println("✅ 部门商品规格匹配，从部门商品设置价格: " + departmentDisGoodsEntity.getNxDdgOrderPrice());
//                } else {
//                    System.out.println("⚠️ 部门商品规格不匹配，跳过价格设置。部门商品规格: [" + departmentStandard
//                            + "], 订单规格: [" + standard + "]");
//                }
//            }
//
//            // 判断是否使用了大包装单价，如果是则设置打印规格为大包装单位（支持智能匹配：件=箱）
//            boolean isUsingCartonPrice = distributerGoodsEntity.getNxDgCartonUnit() != null
//                    && !distributerGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
//                    && standard != null
//                    && isStandardMatch(standard.trim(), distributerGoodsEntity.getNxDgCartonUnit().trim());
//
//            if (isUsingCartonPrice) {
//                ordersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgCartonUnit());
//                System.out.println("使用大包装单价，打印规格设置为: " + distributerGoodsEntity.getNxDgCartonUnit());
//            } else if (ordersEntity.getNxDoCostPriceLevel() == null || ordersEntity.getNxDoCostPriceLevel().equals("1")) {
//                ordersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgGoodsStandardname());
//            } else {
//                ordersEntity.setNxDoPrintStandard(distributerGoodsEntity.getNxDgWillPriceTwoStandard());
//            }
//
//            if (ordersEntity.getNxDoWeight() != null && !ordersEntity.getNxDoWeight().trim().isEmpty()
//                    && ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().trim().isEmpty()) {
//                try {
//                    BigDecimal weightBD = new BigDecimal(ordersEntity.getNxDoWeight());
//                    BigDecimal priceBD = new BigDecimal(ordersEntity.getNxDoPrice());
//                    BigDecimal subtotal = weightBD.multiply(priceBD).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    ordersEntity.setNxDoSubtotal(subtotal.toString());
//                    System.out.println("重新计算小计: " + weightBD + " × " + priceBD + " = " + subtotal);
//                } catch (NumberFormatException e) {
//                    System.err.println("计算subtotal失败，重量或单价格式错误: weight=" + ordersEntity.getNxDoWeight()
//                            + ", price=" + ordersEntity.getNxDoPrice() + ", error=" + e.getMessage());
//                }
//            }
//
//            ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
//            System.out.println("✅ 部门商品ID已设置: " + departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
//        } else {
//            // 如果没有找到对应规格的部门商品，尝试查找标准为null的部门商品
//            depGoodsMap.put("standard", null);
//            System.out.println("未找到对应规格的部门商品，尝试查找标准为null的部门商品: " + depGoodsMap);
//            NxDepartmentDisGoodsEntity departmentDisGoodsEntityO = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(depGoodsMap);
//            if (departmentDisGoodsEntityO != null) {
//                ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
//                System.out.println("✅ 找到标准为null的部门商品，ID: " + departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
//            } else {
//                System.out.println("⚠️ 未找到部门商品");
//            }
//        }
//        System.out.println("======== 从部门商品查找价格结束 ========");
//
//        ordersEntity.setNxDoCostPriceLevel(priceLevel);
//        nxDepartmentOrdersService.update(ordersEntity);
//
//        ordersEntity.setNxDistributerGoodsEntity(distributerGoodsEntity);
//        return R.ok().put("data", ordersEntity);
//    }

    @ResponseBody
    @RequestMapping(value = "/updateOrderReturn", method = RequestMethod.POST)
    public R updateOrderReturn(Integer id, String weight, String subtotal) {

        NxDepartmentOrderHistoryEntity orders = historyService.queryObject(id);

        orders.setNxDoReturnWeight(weight);
        orders.setNxDoReturnSubtotal(subtotal);
        orders.setNxDoReturnStatus(0);
        historyService.update(orders);
        return R.ok().put("data", orders);
    }

    /**
     * ORDER
     * 删除申请
     *
     * @param nxDepartmentOrdersId 订货申请id
     * @return ok
     */
    @ResponseBody
    /**
     * 删除单个订单的内部逻辑
     *
     * @param nxDepartmentOrdersId 订单ID
     * @return 是否删除成功
     */
    private boolean deleteOrderInternal(Integer nxDepartmentOrdersId) {
        return deleteOrderInternal(nxDepartmentOrdersId, new HashSet<>());
    }

    private boolean deleteOrderInternal(Integer nxDepartmentOrdersId, Set<Integer> processedIds) {
        if (processedIds.contains(nxDepartmentOrdersId)) {
            logger.warn("[deleteOrderInternal] 检测到协作订单循环引用，跳过订单ID: {}", nxDepartmentOrdersId);
            return true;
        }
        processedIds.add(nxDepartmentOrdersId);
        try {
            NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(nxDepartmentOrdersId);
            if (ordersEntity == null) {
                logger.warn("[deleteOrderInternal] 订单不存在，订单ID: {}", nxDepartmentOrdersId);
                return false;
            }
            System.out.println("ordosos" + ordersEntity.getNxDepartmentOrdersId());
            if (ordersEntity.getNxDoStatus() > 2) {
                return false;
            }


            // 删除训练数据
            if (ordersEntity.getNxDoTrainingDataId() != null) {
                logger.info("[deleteOrderInternal] 删除训练数据，训练数据ID: {}", ordersEntity.getNxDoTrainingDataId());
                NxOrderOcrTrainingDataEntity nxOrderOcrTrainingDataEntity = nxOrderOcrTrainingDataService.queryObject(ordersEntity.getNxDoTrainingDataId());
                if (nxOrderOcrTrainingDataEntity != null) {
                    if (nxOrderOcrTrainingDataEntity.getNxOtdOrderId() == null) {
                        nxOrderOcrTrainingDataService.delete(ordersEntity.getNxDoTrainingDataId());
                    } else {
                        Integer nxOtdOrderId = nxOrderOcrTrainingDataEntity.getNxOtdOrderId();
                        logger.info("[deleteOrderInternal] 删除训练数据shcnh" + nxOtdOrderId + "grid" + nxDepartmentOrdersId);
                        if (nxOtdOrderId.equals(nxDepartmentOrdersId)) {
                            nxOrderOcrTrainingDataService.delete(ordersEntity.getNxDoTrainingDataId());
                        }
                    }
                }
            }

//            //更新训练数据（查询不到OCR任务时跳过，继续向下执行）
            if (ordersEntity.getNxDoOcrTaskId() != null) {
                NxOcrTaskEntity nxOcrTaskEntity = nxOcrTaskService.queryObject(ordersEntity.getNxDoOcrTaskId());
                if (nxOcrTaskEntity != null) {
                    logger.info("[nxOcrTaskEntity]nxOcrTaskEntity");
                    if (nxOcrTaskEntity.getNxOcrTaskTotalOrders() == 1) {
                        nxOcrTaskService.delete(ordersEntity.getNxDoOcrTaskId());
                    } else {
                        nxOcrTaskEntity.setNxOcrTaskTotalOrders(nxOcrTaskEntity.getNxOcrTaskTotalOrders() - 1);
                        if (ordersEntity.getNxDoStatus() == -2) {
                            nxOcrTaskEntity.setNxOcrTaskPendingOrders(nxOcrTaskEntity.getNxOcrTaskPendingOrders() - 1);
                        } else {
                            nxOcrTaskEntity.setNxOcrTaskCompletedOrders(nxOcrTaskEntity.getNxOcrTaskCompletedOrders() - 1);
                        }
                        int newPendingOrders = nxOcrTaskEntity.getNxOcrTaskPendingOrders();
                        logger.info("[choiceGoodsForApply] OCR任务更新后 - 已完成订单: {}, 待修正订单: {}",
                                nxOcrTaskEntity.getNxOcrTaskCompletedOrders(), newPendingOrders);

                        // 如果待修正订单数为0，设置任务状态为已完成（状态1）
                        if (newPendingOrders == 0) {
                            nxOcrTaskEntity.setNxOcrTaskStatus(2);
                        }
                        nxOcrTaskService.update(nxOcrTaskEntity);
                    }
                }
            }

            // 处理采购商品（与回退订单共用逻辑）
            revertPurchaseGoodsForOrder(ordersEntity);

            // 处理协作订单（与回退订单共用逻辑，使用 processedIds 防止循环引用）
            deleteCollaborationOrderIfExists(ordersEntity, processedIds);
            // 删除订单
            nxDepartmentOrdersService.delete(nxDepartmentOrdersId);

            return true;
        } catch (Exception e) {
            logger.error("[deleteOrderInternal] 删除订单失败，订单ID: {}, 错误: {}", nxDepartmentOrdersId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 撤销订单关联的采购商品（减数量、更新小计或删除采购记录）
     * 供删除订单、回退订单共用，不修改订单本身
     *
     * @param ordersEntity 订单实体
     */
    private void revertPurchaseGoodsForOrder(NxDepartmentOrdersEntity ordersEntity) {
        if (ordersEntity.getNxDoPurchaseGoodsId() == null || ordersEntity.getNxDoPurchaseGoodsId() == -1) {
            return;
        }
        NxDistributerPurchaseGoodsEntity nxDistributerPurchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(ordersEntity.getNxDoPurchaseGoodsId());
        if (nxDistributerPurchaseGoodsEntity == null) {
            return;
        }
        Integer nxDpgOrdersAmount = nxDistributerPurchaseGoodsEntity.getNxDpgOrdersAmount();
        if (nxDpgOrdersAmount > 1) {
            nxDistributerPurchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
            Integer nxDoDisGoodsId = ordersEntity.getNxDoDisGoodsId();
            if (nxDoDisGoodsId != null) {
                NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
                if (nxDistributerGoodsEntity != null
                        && nxDistributerPurchaseGoodsEntity.getNxDpgStandard() != null
                        && nxDistributerPurchaseGoodsEntity.getNxDpgStandard().equals(nxDistributerGoodsEntity.getNxDgGoodsStandardname())) {
                    if (nxDistributerPurchaseGoodsEntity.getNxDpgBuyQuantity() != null && ordersEntity.getNxDoQuantity() != null) {
                        BigDecimal decimal = new BigDecimal(nxDistributerPurchaseGoodsEntity.getNxDpgBuyQuantity()).subtract(new BigDecimal(ordersEntity.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal multiply = decimal.multiply(new BigDecimal(nxDistributerPurchaseGoodsEntity.getNxDpgBuyPrice()));
                        nxDistributerPurchaseGoodsEntity.setNxDpgBuySubtotal(multiply.toString());
                        nxDistributerPurchaseGoodsEntity.setNxDpgBuyQuantity(decimal.toString());
                        nxDistributerPurchaseGoodsEntity.setNxDpgQuantity(decimal.toString());
                    }
                }
            }
            nxDistributerPurchaseGoodsService.update(nxDistributerPurchaseGoodsEntity);
        } else {
            Integer nxDpgBatchId = nxDistributerPurchaseGoodsEntity.getNxDpgBatchId();
            List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByBatchId(nxDpgBatchId);
            if (purchaseGoodsEntities.size() == 1) {
                nxDistributerPurchaseBatchService.delete(nxDpgBatchId);
            }
            nxDistributerPurchaseGoodsService.delete(nxDistributerPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
        }
    }

    /**
     * 若订单存在协作订单则删除（递归调用 deleteOrderInternal，会处理协作订单的采购商品等）
     * 供删除订单、回退订单共用
     *
     * @param ordersEntity 主订单实体
     * @param processedIds 已处理订单ID集合，防止循环引用
     */
    private void deleteCollaborationOrderIfExists(NxDepartmentOrdersEntity ordersEntity, Set<Integer> processedIds) {
        if (ordersEntity.getNxDoNxRestrauntOrderId() == null || ordersEntity.getNxDoNxRestrauntOrderId() <= 0) {
            return;
        }
        NxDepartmentOrdersEntity xiezuoOrder = nxDepartmentOrdersService.queryObjectNew(ordersEntity.getNxDoNxRestrauntOrderId());
        if (xiezuoOrder != null) {
            deleteOrderInternal(xiezuoOrder.getNxDepartmentOrdersId(), processedIds);
        }
    }

    @RequestMapping(value = "/deleteTaskData/{taskId}")
    @ResponseBody
    public R deleteTaskData(@PathVariable Integer taskId) {

        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = nxDepartmentOrdersService.queryListByOcrTaskId(taskId);

        for (NxDepartmentOrdersEntity ordersEntity : nxDepartmentOrdersEntities) {
            deleteOrderInternal(ordersEntity.getNxDepartmentOrdersId());
        }

        nxOcrTaskService.delete(taskId);

        return R.ok();
    }


    /**
     * 删除单个订单
     *
     * @param nxDepartmentOrdersId 订单ID
     * @return 删除结果
     */
    @RequestMapping("/delete/{nxDepartmentOrdersId}")
    public R delete(@PathVariable Integer nxDepartmentOrdersId) {
        boolean success = deleteOrderInternal(nxDepartmentOrdersId);
        if (success) {
            return R.ok();
        } else {
            return R.error("删除订单失败，订单ID: " + nxDepartmentOrdersId);
        }
    }


    /**
     * 回退订单：将订单恢复到初始状态（撤销已匹配商品），供用户重新选择
     * 会处理：协作订单删除、采购商品回退、字段恢复、OCR任务统计
     *
     * @param nxDepartmentOrdersId 订单ID
     * @return 回退后的订单（含推荐商品）
     */
    @RequestMapping("/revertTaskOrder/{nxDepartmentOrdersId}")
    public R revertTaskOrder(@PathVariable Integer nxDepartmentOrdersId) {
            logger.info("[revertTaskOrder] 收到回退请求，订单ID: {}", nxDepartmentOrdersId);

        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(nxDepartmentOrdersId);
        if (ordersEntity == null) {
            return R.error("订单不存在，订单ID: " + nxDepartmentOrdersId);
        }
        if (ordersEntity.getNxDoStatus() != null && ordersEntity.getNxDoStatus() > 2) {
            return R.error("订单状态不允许回退，订单ID: " + nxDepartmentOrdersId);
        }
        Integer statusBeforeRevert = ordersEntity.getNxDoStatus();

        // 先检查是否有协作订单，有的话才处理
        if (ordersEntity.getNxDoNxRestrauntOrderId() != null && ordersEntity.getNxDoNxRestrauntOrderId() > 0) {
            Set<Integer> processedIds = new HashSet<>();
            processedIds.add(nxDepartmentOrdersId);  // 主订单加入已处理集合，防止协作订单循环引用时误删主订单
            // 1. 先删除协作订单（会处理协作订单的采购商品等）
            deleteCollaborationOrderIfExists(ordersEntity, processedIds);
            // 2. 回退主订单的采购商品
            revertPurchaseGoodsForOrder(ordersEntity);
        }

        // 3. 从训练数据恢复字段
        Integer nxDoTrainingDataId = ordersEntity.getNxDoTrainingDataId();
        if (nxDoTrainingDataId != null) {
            NxOrderOcrTrainingDataEntity dataEntity = nxOrderOcrTrainingDataService.queryObject(nxDoTrainingDataId);
            if (dataEntity != null) {
                ordersEntity.setNxDoStandard(dataEntity.getNxOtdOriginalStandard());
                ordersEntity.setStandardWeight(dataEntity.getNxOtdOriginalStandardWeight());
                ordersEntity.setNxDoQuantity(dataEntity.getNxOtdOriginalQuantity());
                ordersEntity.setNxDoRemark(dataEntity.getNxOtdOriginalRemark());
                ordersEntity.setNxDoGoodsName(dataEntity.getNxOtdOriginalGoodsName());
            }
        }

        ordersEntity.setNxDoDisGoodsId(null);
        ordersEntity.setNxDoNxRestrauntOrderId(null);
        ordersEntity.setNxDoPurchaseGoodsId(null);
        ordersEntity.setNxDoStatus(-2);
        ordersEntity.setNxDoDepDisGoodsId(null);
        ordersEntity.setNxDoWeight(null);
        ordersEntity.setNxDoPrice(null);
        ordersEntity.setNxDoSubtotal(null);

        // 4. 更新OCR任务统计（与 choiceGoodsForApply 反向：原为已完成则 completed -1, pending +1）
        // 注意：仅在订单原状态非-2时更新，避免重复更新；且必须在 addCommentsGoodsForOrder 之前更新，否则任务统计会不准
        if (ordersEntity.getNxDoOcrTaskId() != null && (statusBeforeRevert == null || statusBeforeRevert != -2)) {
            NxOcrTaskEntity nxOcrTaskEntity = nxOcrTaskService.queryObject(ordersEntity.getNxDoOcrTaskId());
            if (nxOcrTaskEntity != null) {
                int oldCompleted = nxOcrTaskEntity.getNxOcrTaskCompletedOrders() != null ? nxOcrTaskEntity.getNxOcrTaskCompletedOrders() : 0;
                int oldPending = nxOcrTaskEntity.getNxOcrTaskPendingOrders() != null ? nxOcrTaskEntity.getNxOcrTaskPendingOrders() : 0;
                nxOcrTaskEntity.setNxOcrTaskCompletedOrders(Math.max(0, oldCompleted - 1));
                nxOcrTaskEntity.setNxOcrTaskPendingOrders(oldPending + 1);
                nxOcrTaskService.update(nxOcrTaskEntity);
            }
        }

        // 5. 添加推荐商品并更新订单（合并为一次 update，避免订单被更新 2 次）
        NxDepartmentOrdersEntity orderWithComments = nxDepartmentOrdersService.addCommentsGoodsForOrder(ordersEntity);
        System.out.println("orderWithComments" + orderWithComments.getNxDistributerGoodsEntityList());
        return R.ok().put("data", orderWithComments);
    }

    @RequestMapping("/deleteTaskOrder/{nxDepartmentOrdersId}")
    public R deleteTaskOrder(@PathVariable Integer nxDepartmentOrdersId) {
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(nxDepartmentOrdersId);
        NxOcrTaskEntity nxOcrTaskEntity = null;
//        if (ordersEntity.getNxDoOcrTaskId() != null) {
//            nxOcrTaskEntity = nxOcrTaskService.queryObject(ordersEntity.getNxDoOcrTaskId());
//            if (nxOcrTaskEntity.getNxOcrTaskTotalOrders() == 1) {
//                nxOcrTaskService.delete(ordersEntity.getNxDoOcrTaskId());
//            } else {
//                nxOcrTaskEntity.setNxOcrTaskTotalOrders(nxOcrTaskEntity.getNxOcrTaskTotalOrders() - 1);
//                if (ordersEntity.getNxDoStatus() == -2) {
//                    nxOcrTaskEntity.setNxOcrTaskPendingOrders(nxOcrTaskEntity.getNxOcrTaskPendingOrders() - 1);
//                } else {
//                    nxOcrTaskEntity.setNxOcrTaskCompletedOrders(nxOcrTaskEntity.getNxOcrTaskCompletedOrders() - 1);
//                }
//                int newPendingOrders = nxOcrTaskEntity.getNxOcrTaskPendingOrders();
//                logger.info("[choiceGoodsForApply] OCR任务更新后 - 已完成订单: {}, 待修正订单: {}",
//                        nxOcrTaskEntity.getNxOcrTaskCompletedOrders(), newPendingOrders);
//
//                // 如果待修正订单数为0，设置任务状态为已完成（状态1）
//                if (newPendingOrders == 0) {
//                    nxOcrTaskEntity.setNxOcrTaskStatus(2);
//                }
//                nxOcrTaskService.update(nxOcrTaskEntity);
//            }
//
//        }

        boolean success = deleteOrderInternal(nxDepartmentOrdersId);
        if (success) {
            nxOcrTaskEntity = nxOcrTaskService.queryObject(ordersEntity.getNxDoOcrTaskId());
            return R.ok().put("task", nxOcrTaskEntity);
        } else {
            return R.error("删除订单失败，订单ID: " + nxDepartmentOrdersId);
        }
    }

    /**
     * 批量删除订单
     *
     * @param request 请求体，包含订单ID列表，格式：{"orderIds": [1, 2, 3, ...]}
     * @return 删除结果，包含成功和失败的统计信息
     */
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.POST)
    @ResponseBody
    public R deleteBatch(@RequestBody Map<String, Object> request) {
        try {
            // 获取订单ID列表
            Object orderIdsObj = request.get("orderIds");
            Object taskId = request.get("taskId");
            if (orderIdsObj == null) {
                return R.error("订单ID列表不能为空");
            }

            if (taskId != null) {
                Integer taskIdInteger = null;
                // 将 Object 转换为 Integer
                if (taskId instanceof Number) {
                    taskIdInteger = ((Number) taskId).intValue();
                } else if (taskId instanceof String) {
                    try {
                        taskIdInteger = Integer.parseInt((String) taskId);
                    } catch (NumberFormatException e) {
                        logger.warn("[deleteBatch] taskId 格式错误: {}", taskId);
                        return R.error("任务ID格式错误");
                    }
                } else {
                    try {
                        taskIdInteger = Integer.valueOf(taskId.toString());
                    } catch (NumberFormatException e) {
                        logger.warn("[deleteBatch] taskId 无法转换为 Integer: {}", taskId);
                        return R.error("任务ID格式错误");
                    }
                }

                NxOcrTaskEntity nxOcrTaskEntity = nxOcrTaskService.queryObject(taskIdInteger);
                String nxOcrTaskImagePath = nxOcrTaskEntity.getNxOcrTaskImagePath();
                if (nxOcrTaskImagePath != null && !nxOcrTaskImagePath.trim().isEmpty()) {
                    // 将相对路径转换为绝对路径
                    String absolutePath = getAbsoluteImagePath(nxOcrTaskImagePath);
                    if (absolutePath != null) {
                        File file = new File(absolutePath);
                        if (file.exists()) {
                            boolean deleted = file.delete();
                            if (deleted) {
                                logger.info("[deleteBatch] 成功删除OCR任务图片文件，任务ID: {}, 文件路径: {}", taskIdInteger, absolutePath);
                            } else {
                                logger.warn("[deleteBatch] 删除OCR任务图片文件失败，任务ID: {}, 文件路径: {}", taskIdInteger, absolutePath);
                            }
                        } else {
                            logger.warn("[deleteBatch] OCR任务图片文件不存在，任务ID: {}, 文件路径: {}", taskIdInteger, absolutePath);
                        }
                    } else {
                        logger.warn("[deleteBatch] 无法获取OCR任务图片的绝对路径，任务ID: {}, 相对路径: {}", taskIdInteger, nxOcrTaskImagePath);
                    }
                }
                nxOcrTaskService.delete(taskIdInteger);
            }

            List<Integer> orderIds = new ArrayList<>();
            if (orderIdsObj instanceof List) {
                List<?> list = (List<?>) orderIdsObj;
                for (Object item : list) {
                    if (item instanceof Number) {
                        orderIds.add(((Number) item).intValue());
                    } else if (item instanceof String) {
                        try {
                            orderIds.add(Integer.parseInt((String) item));
                        } catch (NumberFormatException e) {
                            logger.warn("[deleteBatch] 无效的订单ID格式: {}", item);
                        }
                    }
                }
            } else if (orderIdsObj instanceof Integer[]) {
                Integer[] array = (Integer[]) orderIdsObj;
                orderIds = Arrays.asList(array);
            } else {
                return R.error("订单ID列表格式不正确，应为数组格式");
            }

            if (orderIds.isEmpty()) {
                return R.error("订单ID列表为空");
            }

            logger.info("[deleteBatch] 开始批量删除订单，订单数量: {}", orderIds.size());

            // 执行批量删除
            List<Integer> successIds = new ArrayList<>();
            List<Integer> failedIds = new ArrayList<>();
            Map<Integer, String> failedReasons = new HashMap<>();

            for (Integer orderId : orderIds) {
                boolean success = deleteOrderInternal(orderId);
                if (success) {
                    successIds.add(orderId);
                } else {
                    failedIds.add(orderId);
                    failedReasons.put(orderId, "删除失败");
                }
            }

            logger.info("[deleteBatch] 批量删除完成，成功: {} 个，失败: {} 个", successIds.size(), failedIds.size());

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("total", orderIds.size());
            result.put("successCount", successIds.size());
            result.put("failedCount", failedIds.size());
            result.put("successIds", successIds);
            result.put("failedIds", failedIds);
            result.put("failedReasons", failedReasons);
            return R.ok().put("data", result);
        } catch (Exception e) {
            logger.error("[deleteBatch] 批量删除订单异常: {}", e.getMessage(), e);
            return R.error("批量删除订单异常: " + e.getMessage());
        }
    }


    @ResponseBody
    @RequestMapping("/saveCash")
    public R saveCash(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {
        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
        nxDepartmentOrders.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxDepartmentOrders.setNxDoGbDistributerId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
        nxDepartmentOrders.setNxDoNxCommunityId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
        nxDepartmentOrders.setNxDoCollaborativeNxDisId(-1);
        nxDepartmentOrders.setNxDoPriceDifferent("0.0");

        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("status", 3);
        int order = nxDepartmentOrdersService.queryDepOrdersAcount(map);
        nxDepartmentOrders.setNxDoTodayOrder(order + 1);

        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.saveOrderWithGoods(nxDepartmentOrders, nxDistributerGoodsEntity);

        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
        return R.ok().put("data", nxDepartmentOrdersEntity);

    }


    @ResponseBody
    @RequestMapping("/saveCashBefore")
    public R saveCashBefore(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {
        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
        //临时用 purUserid 赋值 前面 order 的 id
        Integer nxDoPurchaseUserId = nxDepartmentOrders.getNxDoPurchaseUserId();
        NxDepartmentOrdersEntity beforOrder = nxDepartmentOrdersService.queryObject(nxDoPurchaseUserId);
        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxDepartmentOrders.setNxDoGbDistributerId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
        nxDepartmentOrders.setNxDoNxCommunityId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
        nxDepartmentOrders.setNxDoCollaborativeNxDisId(-1);
        nxDepartmentOrders.setNxDoPriceDifferent("0.0");
        // 获取beforOrder的todayOrder值，如果为null则使用0
        Integer beforTodayOrder = beforOrder.getNxDoTodayOrder() != null ? beforOrder.getNxDoTodayOrder() : 0;
        nxDepartmentOrders.setNxDoTodayOrder(beforTodayOrder);
        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.saveOrderWithGoods(nxDepartmentOrders, nxDistributerGoodsEntity);
//


        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("todayOrder", beforTodayOrder);
        map.put("status", 3);
        map.put("orderBy", "time");
        System.out.println("maporodoerer" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            System.out.println("orooro" + ordersEntities.size());
            for (int i = 0; i < ordersEntities.size(); i++) {
                NxDepartmentOrdersEntity ordersEntity = ordersEntities.get(i);
                int i1 = beforTodayOrder + i + 2;
                ordersEntity.setNxDoTodayOrder(i1);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

        beforOrder.setNxDoTodayOrder(beforTodayOrder + 1);
        nxDepartmentOrdersService.update(beforOrder);

        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
        return R.ok().put("data", nxDepartmentOrdersEntity);
    }

    @ResponseBody
    @RequestMapping("/saveCashBeforeTask")
    public R saveCashBeforeTask(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {


        Integer nxDoOcrTaskId = nxDepartmentOrders.getNxDoOcrTaskId();
        NxOcrTaskEntity nxOcrTaskEntity = nxOcrTaskService.queryObject(nxDoOcrTaskId);
        nxOcrTaskEntity.setNxOcrTaskTotalOrders(nxOcrTaskEntity.getNxOcrTaskTotalOrders() + 1);
        nxOcrTaskEntity.setNxOcrTaskCompletedOrders(nxOcrTaskEntity.getNxOcrTaskCompletedOrders() + 1);
        nxOcrTaskService.update(nxOcrTaskEntity);

        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
        //临时用 purUserid 赋值 前面 order 的 id
        Integer nxDoPurchaseUserId = nxDepartmentOrders.getNxDoPurchaseUserId();
        NxDepartmentOrdersEntity beforOrder = nxDepartmentOrdersService.queryObject(nxDoPurchaseUserId);
        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxDepartmentOrders.setNxDoGbDistributerId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
        nxDepartmentOrders.setNxDoNxCommunityId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
        nxDepartmentOrders.setNxDoPriceDifferent("0.0");
        // 获取beforOrder的todayOrder值，如果为null则使用0
        Integer beforTodayOrder = beforOrder.getNxDoTodayOrder() != null ? beforOrder.getNxDoTodayOrder() : 0;
        nxDepartmentOrders.setNxDoTodayOrder(beforTodayOrder);
        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.saveOrderWithGoods(nxDepartmentOrders, nxDistributerGoodsEntity);
//


        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("todayOrder", beforTodayOrder);
        map.put("status", 3);
        map.put("orderBy", "time");
        System.out.println("maporodoerer" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            System.out.println("orooro" + ordersEntities.size());
            for (int i = 0; i < ordersEntities.size(); i++) {
                NxDepartmentOrdersEntity ordersEntity = ordersEntities.get(i);
                int i1 = beforTodayOrder + i + 2;
                ordersEntity.setNxDoTodayOrder(i1);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

        beforOrder.setNxDoTodayOrder(beforTodayOrder + 1);
        nxDepartmentOrdersService.update(beforOrder);

        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
        return R.ok().put("data", nxDepartmentOrdersEntity)
                .put("task", nxOcrTaskEntity);
    }


    /**
     * ORDER,DISTRIBUTER
     * 添加订货申请
     *
     * @param nxDepartmentOrders 订货申请
     * @return ok
     */
    @ResponseBody
    @RequestMapping("/save")
    public R save(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {

        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
        nxDepartmentOrders.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxDepartmentOrders.setNxDoGbDistributerId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
        nxDepartmentOrders.setNxDoNxCommunityId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
        nxDepartmentOrders.setNxDoCollaborativeNxDisId(-1);

        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        if (nxDistributerGoodsEntity == null) {
            return R.error("商品不存在或已下架，请检查商品ID：" + nxDoDisGoodsId);
        }
        // 协作伙伴商品判别与保存已统一在 saveOrderWithGoods 中处理

        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("status", 3);
        System.out.println("ordierireirei" + map);
        int order = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        nxDepartmentOrders.setNxDoTodayOrder(order + 1);

        if (nxDistributerGoodsEntity.getNxDgGoodsStandardWeight() != null) {
            nxDepartmentOrders.setStandardWeight(nxDistributerGoodsEntity.getNxDgGoodsStandardWeight());
        } else {
            nxDepartmentOrders.setStandardWeight("");
        }
        if (nxDistributerGoodsEntity.getNxDgItemsPerCarton() != null
                && !nxDistributerGoodsEntity.getNxDgItemsPerCarton().trim().isEmpty()) {
            nxDepartmentOrders.setItemsPerCarton(nxDistributerGoodsEntity.getNxDgItemsPerCarton().trim());
        } else {
            nxDepartmentOrders.setItemsPerCarton("");
        }
        if (nxDistributerGoodsEntity.getNxDgCartonUnit() != null) {
            nxDepartmentOrders.setCartonUnit(nxDistributerGoodsEntity.getNxDgCartonUnit());
        } else {
            nxDepartmentOrders.setCartonUnit("");
        }

        nxDepartmentOrders.setItemUnit(nxDistributerGoodsEntity.getNxDgGoodsStandardname());

        nxDepartmentOrders = nxDepartmentOrdersService.saveOrderWithGoods(nxDepartmentOrders, nxDistributerGoodsEntity);
        nxDepartmentOrders.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);


        return R.ok().put("data", nxDepartmentOrders);
    }

    @ResponseBody
    @RequestMapping("/nxDepSaveApply")
    public R nxDepSaveApply(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {

        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
        nxDepartmentOrders.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxDepartmentOrders.setNxDoGbDistributerId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
        nxDepartmentOrders.setNxDoNxCommunityId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
        nxDepartmentOrders.setNxDoCollaborativeNxDisId(-1);
        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("status", 3);
        System.out.println("ordierireirei" + map);
        int order = nxDepartmentOrdersService.queryDepOrdersAcount(map);

        nxDepartmentOrders.setNxDoTodayOrder(order + 1);

        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        if (nxDistributerGoodsEntity == null) {
            return R.error("商品不存在或已下架，请检查商品ID：" + nxDoDisGoodsId);
        }
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.saveOrderWithGoods(nxDepartmentOrders, nxDistributerGoodsEntity);
        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
        return R.ok().put("data", nxDepartmentOrdersEntity);
    }

    /**
     * ORDER,DISTRIBUTER
     * 添加订货申请
     *
     * @param nxDepartmentOrders 订货申请
     * @return ok
     */
    @ResponseBody
    @RequestMapping("/saveBefore")
    public R saveBefore(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {
        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
        //临时用 purUserid 赋值 前面 order 的 id
        Integer nxDoPurchaseUserId = nxDepartmentOrders.getNxDoPurchaseUserId();
        NxDepartmentOrdersEntity beforOrder = nxDepartmentOrdersService.queryObject(nxDoPurchaseUserId);
        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxDepartmentOrders.setNxDoGbDistributerId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
        nxDepartmentOrders.setNxDoNxCommunityId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
        nxDepartmentOrders.setNxDoCollaborativeNxDisId(-1);
        // 获取beforOrder的todayOrder值，如果为null则使用0
        Integer beforTodayOrder = beforOrder.getNxDoTodayOrder() != null ? beforOrder.getNxDoTodayOrder() : 0;
        System.out.println("befororrordidd" + beforTodayOrder);
        nxDepartmentOrders.setNxDoTodayOrder(beforTodayOrder);

        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.saveOrderWithGoods(nxDepartmentOrders, nxDistributerGoodsEntity);


        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("todayOrder", beforTodayOrder);
        map.put("status", 3);
        map.put("orderBy", "time");
        System.out.println("maporodoerer" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            System.out.println("orooro" + ordersEntities.size());
            for (int i = 0; i < ordersEntities.size(); i++) {
                NxDepartmentOrdersEntity ordersEntity = ordersEntities.get(i);
                int i1 = beforTodayOrder + i + 2;
                System.out.println("whisisisisisisisis====" + i1);
                ordersEntity.setNxDoTodayOrder(i1);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

        beforOrder.setNxDoTodayOrder(beforTodayOrder + 1);
        nxDepartmentOrdersService.update(beforOrder);

        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);
        return R.ok().put("data", nxDepartmentOrdersEntity);
    }


    /**
     * ORDER,DISTRIBUTER
     * 添加订货申请
     *
     * @param nxDepartmentOrders 订货申请
     * @return ok
     */
    @ResponseBody
    @RequestMapping("/saveOrderBeforeTask")
    public R saveOrderBeforeTask(@RequestBody NxDepartmentOrdersEntity nxDepartmentOrders) {

        Integer nxDoOcrTaskId = nxDepartmentOrders.getNxDoOcrTaskId();
        NxOcrTaskEntity nxOcrTaskEntity = nxOcrTaskService.queryObject(nxDoOcrTaskId);
        nxOcrTaskEntity.setNxOcrTaskTotalOrders(nxOcrTaskEntity.getNxOcrTaskTotalOrders() + 1);
        nxOcrTaskEntity.setNxOcrTaskCompletedOrders(nxOcrTaskEntity.getNxOcrTaskCompletedOrders() + 1);
        nxOcrTaskService.update(nxOcrTaskEntity);

        nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
        nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
        //临时用 purUserid 赋值 前面 order 的 id
        Integer nxDoPurchaseUserId = nxDepartmentOrders.getNxDoPurchaseUserId();
        NxDepartmentOrdersEntity beforOrder = nxDepartmentOrdersService.queryObject(nxDoPurchaseUserId);
        nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
        nxDepartmentOrders.setNxDoGbDistributerId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentFatherId(-1);
        nxDepartmentOrders.setNxDoGbDepartmentId(-1);
        nxDepartmentOrders.setNxDoNxCommunityId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntFatherId(-1);
        nxDepartmentOrders.setNxDoNxCommRestrauntId(-1);
        nxDepartmentOrders.setNxDoCollaborativeNxDisId(-1);
        // 获取beforOrder的todayOrder值，如果为null则使用0
        Integer beforTodayOrder = beforOrder.getNxDoTodayOrder() != null ? beforOrder.getNxDoTodayOrder() : 0;
        System.out.println("befororrordidd" + beforTodayOrder);
        nxDepartmentOrders.setNxDoTodayOrder(beforTodayOrder);

        Integer nxDoDisGoodsId = nxDepartmentOrders.getNxDoDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(nxDoDisGoodsId);
        NxDepartmentOrdersEntity nxDepartmentOrdersEntity = nxDepartmentOrdersService.saveOrderWithGoods(nxDepartmentOrders, nxDistributerGoodsEntity);


        Map<String, Object> map = new HashMap<>();
        map.put("depId", nxDepartmentOrders.getNxDoDepartmentId());
        map.put("todayOrder", beforTodayOrder);
        map.put("status", 3);
        map.put("orderBy", "time");
        System.out.println("maporodoerer" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            System.out.println("orooro" + ordersEntities.size());
            for (int i = 0; i < ordersEntities.size(); i++) {
                NxDepartmentOrdersEntity ordersEntity = ordersEntities.get(i);
                int i1 = beforTodayOrder + i + 2;
                System.out.println("whisisisisisisisis====" + i1);
                ordersEntity.setNxDoTodayOrder(i1);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

        beforOrder.setNxDoTodayOrder(beforTodayOrder + 1);
        nxDepartmentOrdersService.update(beforOrder);

        nxDepartmentOrdersEntity.setNxDistributerGoodsEntity(nxDistributerGoodsEntity);

        return R.ok().put("data", nxDepartmentOrdersEntity)
                .put("task", nxOcrTaskEntity);
    }


    @RequestMapping(value = "/disSaveLinshiToNxGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disSaveLinshiToNxGoods(Integer lsGoodsId, Integer nxGoodsId) {
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsMergeService.mergeLinshiToNxGoods(lsGoodsId, nxGoodsId);
        return R.ok().put("data", nxDistributerGoodsEntity);
    }

    /**
     * 临时商品合并到正式商品，并将临时商品名称添加为别名
     * 合并逻辑与 disSaveLinshiToNxGoods 相同（mergeLinshiToNxGoods），仅多一步：保存别名
     */
    @RequestMapping(value = "/disSaveLinshiToAlias", method = RequestMethod.POST)
    @ResponseBody
    public R disSaveLinshiToAlias(Integer lsGoodsId, Integer nxGoodsId, Integer disId) {
        NxDistributerGoodsEntity linshiGoods = nxDistributerGoodsService.queryObject(lsGoodsId);
        if (linshiGoods == null) {
            return R.error(-1, "临时商品不存在");
        }
        String aliasName = linshiGoods.getNxDgGoodsName();
        if (aliasName != null && !aliasName.trim().isEmpty()) {
            NxDistributerAliasEntity aliasEntity = new NxDistributerAliasEntity();
            aliasEntity.setNxDaAliasName(aliasName);
            aliasEntity.setNxDaAliasPinyin(hanziToPinyin(aliasName));
            aliasEntity.setNxDaAliasPy(getHeadStringByString(aliasName, false, null));
            aliasEntity.setNxDaDisGoodsId(nxGoodsId);
            nxDistributerAliasService.save(aliasEntity);
        }
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsMergeService.mergeLinshiToNxGoods(lsGoodsId, nxGoodsId);
        return R.ok().put("data", nxDistributerGoodsEntity);
    }



    private boolean convertOrderPriceToKg(NxDepartmentOrdersEntity order) {
        if (order == null) {
            return false;
        }

        System.out.println("========= convertOrderPriceToKg 开始 =========");
        System.out.println("订单ID: " + order.getNxDepartmentOrdersId());

        // 判断商品的规格是否为"斤"，而不是订单的规格
        NxDistributerGoodsEntity distributerGoodsEntity = order.getNxDistributerGoodsEntity();
        if (distributerGoodsEntity == null) {
            return false;
        }

        String goodsStandard = distributerGoodsEntity.getNxDgGoodsStandardname();

        if (goodsStandard == null || goodsStandard.trim().isEmpty()) {
            return false;
        }

        String goodsStandardTrim = goodsStandard.trim();
        // 检查商品规格是否包含"斤"（支持"斤"、"市斤"等）
        boolean goodsContainsJin = goodsStandardTrim.contains("斤");
        if (!goodsContainsJin) {

            return false;
        }

        // 检查订单规格，如果已经是"Kg"，说明已经转换过，避免重复转换
        String printStandard = order.getNxDoPrintStandard();
        if (printStandard != null && !printStandard.trim().isEmpty()) {
            String orderStandardTrim = printStandard.trim().toLowerCase();
            boolean orderContainsKg = orderStandardTrim.contains("kg");
        }

        // 订单规格是"斤"，需要转换
        String price = order.getNxDoPrice();
        String weight = order.getNxDoWeight();

        boolean hasConversion = false;

        // 转换单价（直接修改 nxDoPrice）
        if (price != null && !price.trim().isEmpty()) {
            try {
                BigDecimal priceDecimal = new BigDecimal(price.trim());
                System.out.println("单价转换计算: " + priceDecimal + " × 2 = ?");
                // 1斤 = 0.5Kg，所以1Kg = 2斤，Kg单价 = 斤单价 × 2
                BigDecimal priceKg = priceDecimal.multiply(new BigDecimal("2")).setScale(1, BigDecimal.ROUND_HALF_UP);
                order.setNxDoPrice(priceKg.toString());
                hasConversion = true;
            } catch (NumberFormatException e) {
            }
        }

        // 转换重量（直接修改 nxDoWeight）
        if (weight != null && !weight.trim().isEmpty()) {
            try {
                BigDecimal weightDecimal = new BigDecimal(weight.trim());
                System.out.println("重量转换计算: " + weightDecimal + " ÷ 2 = ?");
                // 1斤 = 0.5Kg，所以Kg重量 = 斤重量 / 2
                BigDecimal weightKg = weightDecimal.divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP);
                System.out.println("重量转换结果: " + weightDecimal + " ÷ 2 = " + weightKg);
                order.setNxDoWeight(weightKg.toString());
                hasConversion = true;
            } catch (NumberFormatException e) {
            }
        }

        // 如果进行了转换，更新订单规格为"Kg"，避免重复转换
        if (hasConversion) {
            // 将订单规格从"斤"改为"Kg"
            String newStandard = printStandard.replace("斤", "Kg").replace("市斤", "Kg");
            order.setNxDoPrintStandard(newStandard);
        }

        return hasConversion;
    }

    /**
     * 转换订单的斤单价和重量（直接修改 nxDoPrice 和 nxDoWeight）
     * 如果商品规格是"Kg"或"kg"，则将单价和重量转换为斤
     * 单价转换：1Kg = 2斤，所以斤单价 = Kg单价 / 2
     * 重量转换：1Kg = 2斤，所以斤重量 = Kg重量 × 2
     * 转换后会更新订单规格为"斤"，避免重复转换
     * @param order 订单实体
     * @return 是否进行了转换
     */
    /**
     * 将订单从公斤转换为斤（只处理商品规格是"Kg"的订单）
     * 修改3个字段：nxDoPrice（单价）、nxDoWeight（重量）、nxDoPrintStandard（打印规格）
     * 注意：nxDoStandard（客户订货规格）不能修改
     * 单价转换：1Kg = 2斤，所以斤单价 = Kg单价 / 2
     * 重量转换：1Kg = 2斤，所以斤重量 = Kg重量 × 2
     */
    private boolean convertOrderPriceToJin(NxDepartmentOrdersEntity order) {
        if (order == null) {
            return false;
        }

        System.out.println("========= convertOrderPriceToJin 开始 =========");
        System.out.println("订单ID: " + order.getNxDepartmentOrdersId());

        // 判断商品的规格是否为"Kg"
        NxDistributerGoodsEntity distributerGoodsEntity = order.getNxDistributerGoodsEntity();
        if (distributerGoodsEntity == null) {
            System.out.println("❌ 商品信息为空，跳过转换");
            return false;
        }

        String goodsStandard = distributerGoodsEntity.getNxDgGoodsStandardname();
        if (goodsStandard == null || goodsStandard.trim().isEmpty()) {
            System.out.println("❌ 商品规格为空，跳过转换");
            return false;
        }

        String goodsStandardTrim = goodsStandard.trim();
        System.out.println("商品规格（去除空格后）: [" + goodsStandardTrim + "]");

        // 检查商品规格是否包含"斤"（支持"斤"、"市斤"等）
        boolean goodsContainsJin = goodsStandardTrim.contains("斤");
        if (!goodsContainsJin) {
            System.out.println("❌ 商品规格不包含斤，跳过转换");
            return false;
        }

        // 检查订单打印规格，如果已经是"斤"，说明已经转换过，避免重复转换
        String printStandard = order.getNxDoPrintStandard();
        if (printStandard != null && !printStandard.trim().isEmpty()) {
            String printStandardTrim = printStandard.trim();
            if (printStandardTrim.contains("斤")) {
                System.out.println("❌ 订单打印规格已经是斤，已转换过，跳过");
                return false;
            }
        }

        // 从公斤转换为斤：修改 nxDoPrice、nxDoWeight、nxDoPrintStandard
        String price = order.getNxDoPrice();
        String weight = order.getNxDoWeight();

        boolean hasConversion = false;

        // 转换单价（直接修改 nxDoPrice）
        if (price != null && !price.trim().isEmpty()) {
            try {
                BigDecimal priceDecimal = new BigDecimal(price.trim());
                System.out.println("单价转换计算: " + priceDecimal + " ÷ 2 = ?");
                // 1Kg = 2斤，所以斤单价 = Kg单价 / 2
                BigDecimal priceJin = priceDecimal.divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP);
                System.out.println("单价转换结果: " + priceDecimal + " ÷ 2 = " + priceJin);
                order.setNxDoPrice(priceJin.toString());
                hasConversion = true;
            } catch (NumberFormatException e) {
                System.out.println("❌ 单价格式错误，跳过转换: " + e.getMessage());
            }
        }

        // 转换重量（直接修改 nxDoWeight）
        if (weight != null && !weight.trim().isEmpty()) {
            try {
                BigDecimal weightDecimal = new BigDecimal(weight.trim());
                System.out.println("重量转换计算: " + weightDecimal + " × 2 = ?");
                // 1Kg = 2斤，所以斤重量 = Kg重量 × 2
                BigDecimal weightJin = weightDecimal.multiply(new BigDecimal("2")).setScale(2, BigDecimal.ROUND_HALF_UP);
                System.out.println("重量转换结果: " + weightDecimal + " × 2 = " + weightJin);
                order.setNxDoWeight(weightJin.toString());
                hasConversion = true;
            } catch (NumberFormatException e) {
                System.out.println("❌ 重量格式错误，跳过转换: " + e.getMessage());
            }
        }

        // 如果进行了转换，更新订单打印规格为"斤"，避免重复转换
        if (hasConversion) {
            // 将订单打印规格从"Kg"改为"斤"
            String oldPrintStandard = printStandard != null ? printStandard : "";
            String newPrintStandard = oldPrintStandard.replaceAll("(?i)kg", "斤");
            order.setNxDoPrintStandard(newPrintStandard);
            System.out.println("✅ 转换完成，打印规格已更新: [" + oldPrintStandard + "] -> [" + newPrintStandard + "]");
        } else {
            System.out.println("❌ 没有进行任何转换");
        }

        return hasConversion;
    }

    /**
     * 批量转换订单列表的Kg单价
     * @param orders 订单列表
     */
    /**
     * 批量转换订单列表的Kg单价和重量
     *
     * @param orders 订单列表
     * @return 被转换的订单列表（只包含真正被转换的订单）
     */
    private List<NxDepartmentOrdersEntity> convertOrdersPriceToKg(List<NxDepartmentOrdersEntity> orders) {
        List<NxDepartmentOrdersEntity> convertedOrders = new ArrayList<>();
        if (orders == null || orders.isEmpty()) {
            return convertedOrders;
        }
        System.out.println("========= 批量转换开始（斤转公斤），订单总数: " + orders.size() + " =========");
        int convertedCount = 0;
        for (NxDepartmentOrdersEntity order : orders) {
            boolean converted = convertOrderPriceToKg(order);
            if (converted) {
                convertedOrders.add(order);
                convertedCount++;
            }
        }
        System.out.println("========= 批量转换结束，实际转换数量: " + convertedCount + "/" + orders.size() + " =========");
        return convertedOrders;
    }

    /**
     * 批量转换订单列表的斤单价和重量
     *
     * @param orders 订单列表
     * @return 被转换的订单列表（只包含真正被转换的订单）
     */
    private List<NxDepartmentOrdersEntity> convertOrdersPriceToJin(List<NxDepartmentOrdersEntity> orders) {
        List<NxDepartmentOrdersEntity> convertedOrders = new ArrayList<>();
        if (orders == null || orders.isEmpty()) {
            return convertedOrders;
        }
        System.out.println("========= 批量转换开始（公斤转斤），订单总数: " + orders.size() + " =========");
        int convertedCount = 0;
        for (NxDepartmentOrdersEntity order : orders) {
            boolean converted = convertOrderPriceToJin(order);
            if (converted) {
                convertedOrders.add(order);
                convertedCount++;
            }
        }
        System.out.println("========= 批量转换结束，实际转换数量: " + convertedCount + "/" + orders.size() + " =========");
        return convertedOrders;
    }

    /**
     * 批量保存订单到数据库（只保存被转换的订单）
     *
     * @param orders 订单列表（只包含被转换的订单）
     */
    private void saveOrders(List<NxDepartmentOrdersEntity> orders) {
        if (orders == null || orders.isEmpty()) {
            System.out.println("没有需要保存的订单（没有订单被转换）");
            return;
        }
        System.out.println("========= 开始保存订单到数据库，数量: " + orders.size() + " =========");
        for (NxDepartmentOrdersEntity order : orders) {
            try {
                nxDepartmentOrdersService.update(order);
                System.out.println("✅ 订单ID: " + order.getNxDepartmentOrdersId() +
                        " 已保存到数据库");
            } catch (Exception e) {
                System.out.println("❌ 保存订单ID: " + order.getNxDepartmentOrdersId() +
                        " 失败: " + e.getMessage());
            }
        }
        System.out.println("========= 订单保存完成 =========");
    }

    /**
     * 将Entity列表转换为SimpleDTO列表
     * 用于减少数据传输量
     */
//    private List<NxDepartmentOrdersSimpleDTO> convertEntitiesToSimpleDTOs(List<NxDepartmentOrdersEntity> entities) {
//        if (entities == null || entities.isEmpty()) {
//            return new ArrayList<>();
//        }
//        List<NxDepartmentOrdersSimpleDTO> dtos = new ArrayList<>();
//        for (NxDepartmentOrdersEntity entity : entities) {
//            NxDepartmentOrdersSimpleDTO dto = convertEntityToSimpleDTO(entity);
//            dtos.add(dto);
//        }
//        return dtos;
//    }

    /**
     * 将单个Entity转换为SimpleDTO
     */
    private NxDepartmentOrdersSimpleDTO convertEntityToSimpleDTO(NxDepartmentOrdersEntity entity) {
        if (entity == null) {
            return null;
        }
        NxDepartmentOrdersSimpleDTO dto = new NxDepartmentOrdersSimpleDTO();
        dto.setNxDepartmentOrdersId(entity.getNxDepartmentOrdersId());
        dto.setNxDoGoodsName(entity.getNxDoGoodsName());
        dto.setNxDoQuantity(entity.getNxDoQuantity());
        dto.setNxDoStandard(entity.getNxDoStandard());
        dto.setNxDoRemark(entity.getNxDoRemark());
        dto.setNxDoStatus(entity.getNxDoStatus());
        dto.setNxDoWeight(entity.getNxDoWeight());
        dto.setNxDoPrice(entity.getNxDoPrice());
        dto.setNxDoSubtotal(entity.getNxDoSubtotal());
        dto.setNxDoPrintStandard(entity.getNxDoPrintStandard());
        dto.setNxDoPurchaseStatus(entity.getNxDoPurchaseStatus());
        dto.setNxDoDisGoodsId(entity.getNxDoDisGoodsId());
        dto.setNxDoDepartmentId(entity.getNxDoDepartmentId());
        dto.setNxDoIsAgent(entity.getNxDoIsAgent());

        // 转换商品信息
        if (entity.getNxDistributerGoodsEntity() != null) {
            NxDistributerGoodsEntity goods = entity.getNxDistributerGoodsEntity();
            NxDepartmentOrdersSimpleDTO.DistributerGoodsSimpleDTO goodsDTO = new NxDepartmentOrdersSimpleDTO.DistributerGoodsSimpleDTO();
            goodsDTO.setNxDistributerGoodsId(goods.getNxDistributerGoodsId());
            goodsDTO.setNxDgGoodsName(goods.getNxDgGoodsName());
            goodsDTO.setNxDgGoodsBrand(goods.getNxDgGoodsBrand());
            goodsDTO.setNxDgGoodsPlace(goods.getNxDgGoodsPlace());
            goodsDTO.setNxDgGoodsDetail(goods.getNxDgGoodsDetail());
            goodsDTO.setNxDgGoodsStandardname(goods.getNxDgGoodsStandardname());
            goodsDTO.setNxDgGoodsStandardWeight(goods.getNxDgGoodsStandardWeight());
            goodsDTO.setNxDgCartonUnit(goods.getNxDgCartonUnit());
            goodsDTO.setNxDgItemsPerCarton(goods.getNxDgItemsPerCarton());
            goodsDTO.setNxDgWillPriceTwoStandard(goods.getNxDgWillPriceTwoStandard());
            goodsDTO.setNxDgWillPriceThreeStandard(goods.getNxDgWillPriceThreeStandard());
            goodsDTO.setNxDgWillPriceTwoAboutPrice(goods.getNxDgWillPriceTwoAboutPrice());
            goodsDTO.setNxDgWillPriceThreeAboutPrice(goods.getNxDgWillPriceThreeAboutPrice());
            goodsDTO.setNxDgWillPriceTwoWeight(goods.getNxDgWillPriceTwoWeight());
            goodsDTO.setNxDgWillPriceThreeWeight(goods.getNxDgWillPriceThreeWeight());
            goodsDTO.setNxDgWillPriceOne(goods.getNxDgWillPriceOne());
            goodsDTO.setNxDgWillPriceTwo(goods.getNxDgWillPriceTwo());
            goodsDTO.setNxDgWillPriceThree(goods.getNxDgWillPriceThree());
            goodsDTO.setNxDgBuyingPriceOneUpdate(goods.getNxDgBuyingPriceOneUpdate());
            goodsDTO.setNxDgBuyingPriceTwoUpdate(goods.getNxDgBuyingPriceTwoUpdate());
            goodsDTO.setNxDgBuyingPriceThreeUpdate(goods.getNxDgBuyingPriceThreeUpdate());
            goodsDTO.setNxDgNxGoodsFatherColor(goods.getNxDgNxGoodsFatherColor());
            if (goods.getNxDistributerStandardEntities() != null && !goods.getNxDistributerStandardEntities().isEmpty()) {
                goodsDTO.setNxDistributerStandardEntities(new ArrayList<>(goods.getNxDistributerStandardEntities()));
            }
            dto.setNxDistributerGoodsEntity(goodsDTO);
        }

        // 转换部门商品信息
        if (entity.getNxDepartmentDisGoodsEntity() != null) {
            NxDepartmentDisGoodsEntity depGoods = entity.getNxDepartmentDisGoodsEntity();
            NxDepartmentOrdersSimpleDTO.DepartmentDisGoodsSimpleDTO depGoodsDTO = new NxDepartmentOrdersSimpleDTO.DepartmentDisGoodsSimpleDTO();
            depGoodsDTO.setNxDdgOrderGoodsName(depGoods.getNxDdgOrderGoodsName());
            dto.setNxDepartmentDisGoodsEntity(depGoodsDTO);
        }

        return dto;
    }

    /**
     * 智能匹配单位规格
     * 支持同义词匹配，例如：件=箱，盒=箱等
     *
     * @param orderStandard 订单规格（客户输入的规格，如"件"）
     * @param cartonUnit    大包装单位（商品的外包装单位，如"箱"）
     * @return 是否匹配
     */
    private boolean isStandardMatch(String orderStandard, String cartonUnit) {
        System.out.println("======== 智能匹配单位规格开始 ========");
        System.out.println("订单规格: [" + orderStandard + "]");
        System.out.println("包装单位: [" + cartonUnit + "]");

        if (orderStandard == null || cartonUnit == null) {
            System.out.println("❌ 匹配失败: 订单规格或包装单位为null");
            return false;
        }

        // 去除空格并转为小写进行比较
        String orderStd = orderStandard.trim();
        String carton = cartonUnit.trim();

        System.out.println("去除空格后 - 订单规格: [" + orderStd + "], 包装单位: [" + carton + "]");

        // 1. 完全匹配
        if (orderStd.equals(carton)) {
            System.out.println("✅ 完全匹配成功: [" + orderStd + "] == [" + carton + "]");
            return true;
        }

        // 2. 同义词映射：定义常见的单位同义词
        // 件 = 箱（订货"件"视为大包装"箱"）
        Map<String, Set<String>> synonymMap = new HashMap<>();
        synonymMap.put("箱", new HashSet<>(Arrays.asList("件")));
        synonymMap.put("件", new HashSet<>(Arrays.asList("箱")));

        System.out.println("开始检查同义词匹配...");

        // 3. 检查同义词匹配
        // 如果订单规格的同义词组包含包装单位，或者包装单位的同义词组包含订单规格，则认为匹配
        Set<String> orderSynonyms = synonymMap.get(orderStd);
        Set<String> cartonSynonyms = synonymMap.get(carton);

        if (orderSynonyms != null && orderSynonyms.contains(carton)) {
            System.out.println("✅ 智能匹配成功: 订单规格[" + orderStd + "] 的同义词组包含包装单位[" + carton + "]");
            System.out.println("订单规格[" + orderStd + "] 的同义词组: " + orderSynonyms);
            return true;
        }

        if (cartonSynonyms != null && cartonSynonyms.contains(orderStd)) {
            System.out.println("✅ 智能匹配成功: 包装单位[" + carton + "] 的同义词组包含订单规格[" + orderStd + "]");
            System.out.println("包装单位[" + carton + "] 的同义词组: " + cartonSynonyms);
            return true;
        }

        System.out.println("❌ 匹配失败: 订单规格[" + orderStd + "] 与 包装单位[" + carton + "] 不匹配");
        System.out.println("======== 智能匹配单位规格结束 ========");
        return false;
    }

    /**
     * 获取OCR图片的基础目录（自动适配开发和生产环境）
     * 与 OcrController 中的 getOcrImageDirectory 方法保持一致
     *
     * @return OCR图片基础目录路径
     */
    private String getOcrImageDirectory() {
        // 1. 尝试使用配置文件中的路径（生产环境）
        if (externalImagesPath != null && !externalImagesPath.trim().isEmpty()) {
            // 移除 file:// 前缀（如果存在）
            String cleanPath = externalImagesPath.replace("file://", "").trim();
            if (!cleanPath.endsWith("/")) {
                cleanPath += "/";
            }
            String productionPath = cleanPath + "ocrImages/";

            File prodDir = new File(productionPath);
            // 检查父目录是否存在（如果父目录存在，说明可能是生产环境）
            File parentDir = prodDir.getParentFile();
            if (parentDir != null && parentDir.exists()) {
                logger.info("[getOcrImageDirectory] 使用生产环境路径: {}", productionPath);
                return productionPath;
            }
        }

        // 2. 尝试使用项目根目录（开发环境）
        // 获取项目根目录（通过类路径推断）
        String projectRoot = System.getProperty("user.dir");
        if (projectRoot != null) {
            String devPath = projectRoot + "/ocrImages/";
            File devDir = new File(devPath);
            // 尝试创建目录，如果成功则使用
            if (devDir.exists() || devDir.mkdirs()) {
                logger.info("[getOcrImageDirectory] 使用开发环境路径: {}", devPath);
                return devPath;
            }
        }

        // 3. 备用方案：使用用户主目录下的临时目录
        String userHome = System.getProperty("user.home");
        String fallbackPath = userHome + "/nongxinle_ocrImages/";
        logger.info("[getOcrImageDirectory] 使用备用路径: {}", fallbackPath);
        return fallbackPath;
    }

    /**
     * 将相对路径转换为绝对路径
     * 数据库存储的是相对路径（如：ocrImages/20250124/xxx.jpg）
     * 需要转换为实际保存的绝对路径
     *
     * @param relativePath 相对路径（如：ocrImages/20250124/xxx.jpg）
     * @return 绝对路径，如果转换失败返回 null
     */
    private String getAbsoluteImagePath(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }

        try {
            // 移除开头的 "ocrImages/" 前缀（如果存在）
            String pathWithoutPrefix = relativePath;
            if (pathWithoutPrefix.startsWith("ocrImages/")) {
                pathWithoutPrefix = pathWithoutPrefix.substring("ocrImages/".length());
            }

            // 获取基础目录
            String baseDir = getOcrImageDirectory();

            // 拼接完整路径
            String absolutePath = baseDir + pathWithoutPrefix;

            logger.debug("[getAbsoluteImagePath] 相对路径: {}, 绝对路径: {}", relativePath, absolutePath);
            return absolutePath;

        } catch (Exception e) {
            logger.error("[getAbsoluteImagePath] 转换路径失败，相对路径: {}", relativePath, e);
            return null;
        }
    }

    /**
     * FIFO 扣减后写回批次剩余成本：按扣减前「nx_dgss_rest_subtotal / 剩余数量」比例推算新剩余小计。
     * 不能用「新剩余数量 × nx_dgss_price」重算，因为账面剩余小计往往不等于 数量×单价（入库四舍五入、历史调整等）。
     *
     * @param stockEntity           当前批次（尚未 update 前，nxDgssRestSubtotal 仍为扣减前值）
     * @param restWeightBeforeDeduct 本批次扣减前的剩余数量
     * @param newRestWeight         扣减后的剩余数量
     */
    private void setFifoNewRestSubtotalAfterDeduct(NxDistributerGoodsShelfStockEntity stockEntity,
                                                   BigDecimal restWeightBeforeDeduct,
                                                   BigDecimal newRestWeight) {
        String restSubStr = stockEntity.getNxDgssRestSubtotal();
        if (restSubStr != null && !restSubStr.trim().isEmpty()
                && restWeightBeforeDeduct.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal oldRestSubtotal = new BigDecimal(restSubStr);
            BigDecimal newRestSubtotal = oldRestSubtotal.multiply(newRestWeight)
                    .divide(restWeightBeforeDeduct, 4, BigDecimal.ROUND_HALF_UP)
                    .setScale(1, BigDecimal.ROUND_HALF_UP);
            stockEntity.setNxDgssRestSubtotal(newRestSubtotal.toPlainString());
        } else {
            String priceStrForRest = stockEntity.getNxDgssPrice();
            BigDecimal stockPriceForRest = (priceStrForRest == null || priceStrForRest.trim().isEmpty())
                    ? BigDecimal.ZERO
                    : new BigDecimal(priceStrForRest);
            BigDecimal newRestSubtotal = newRestWeight.multiply(stockPriceForRest).setScale(1, BigDecimal.ROUND_HALF_UP);
            stockEntity.setNxDgssRestSubtotal(newRestSubtotal.toPlainString());
        }
    }

}
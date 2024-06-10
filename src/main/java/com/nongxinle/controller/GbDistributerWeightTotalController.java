package com.nongxinle.controller;

/**
 * @author lpy
 * @date 11-29 07:10
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

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.GbTypeUtils.*;


@RestController
@RequestMapping("api/gbdistributerweighttotal")
public class GbDistributerWeightTotalController {
    @Autowired
    private GbDistributerWeightTotalService gbDisWeightTotalService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDistributerWeightGoodsService gbDisWeightGoodsService;
    @Autowired
    private GbDistributerPurchaseGoodsService purchaseGoodsService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerGoodsPriceService gbDistributerGoodsPriceService;



    @RequestMapping(value = "/depDeleteWeight/{id}")
    @ResponseBody
    public R depDeleteWeight(@PathVariable Integer id) {

        Map<String, Object> map = new HashMap<>();
        map.put("weightId", id);
        GbDistributerWeightTotalEntity gbDistributerWeightTotalEntity = gbDisWeightTotalService.queryObject(id);
       //出库
        if(gbDistributerWeightTotalEntity.getGbGwtType().equals(getGbOrderTypeChuKu()) || gbDistributerWeightTotalEntity.getGbGwtType().equals(getGbOrderTypeKitchen())){
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
            if (ordersEntities.size() > 0) {
                for (GbDepartmentOrdersEntity order : ordersEntities) {
                    order.setGbDoWeightTotalId(null);
                    order.setGbDoBuyStatus(getGbOrderBuyStatusNew());
                    gbDepartmentOrdersService.update(order);
                }
            }

            List<GbDistributerWeightGoodsEntity> weightGoodsEntities =  gbDisWeightGoodsService.queryWeightGoodsByParams(map);
            if(weightGoodsEntities.size() > 0){
                for (GbDistributerWeightGoodsEntity weightGoodsEntity: weightGoodsEntities){
                    weightGoodsEntity.setGbDwgWeightId(null);
                    weightGoodsEntity.setGbDwgStatus(getGbWeightGoodsStatusPrepare());
                    gbDisWeightGoodsService.update(weightGoodsEntity);
                }
            }
        }

        //采购
        if(gbDistributerWeightTotalEntity.getGbGwtType() == 2){
            List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
            if (ordersEntities.size() > 0) {
                for (GbDepartmentOrdersEntity order : ordersEntities) {
                    order.setGbDoWeightTotalId(null);
                    order.setGbDoBuyStatus(0);
                    gbDepartmentOrdersService.update(order);
                }
            }
            List<GbDistributerPurchaseGoodsEntity> purchaseGoodsByParams = purchaseGoodsService.queryPurchaseGoodsByParams(map);
            if(purchaseGoodsByParams.size() > 0){
                for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity: purchaseGoodsByParams){
                    Integer gbDpgDisGoodsPriceId = purchaseGoodsEntity.getGbDpgDisGoodsPriceId();
                    if(gbDpgDisGoodsPriceId != null){
                        gbDistributerGoodsPriceService.delete(gbDpgDisGoodsPriceId);
                    }
                    purchaseGoodsEntity.setGbDpgWeightId(null);
                    purchaseGoodsEntity.setGbDpgBatchId(null);
                    purchaseGoodsEntity.setGbDpgStatus(0);
                    purchaseGoodsEntity.setGbDpgPurchaseType(null);
                    purchaseGoodsEntity.setGbDpgPayType(null);
                    purchaseGoodsEntity.setGbDpgTime(null);
                    purchaseGoodsEntity.setGbDpgPurchaseDate(null);
                    purchaseGoodsEntity.setGbDpgPurchaseType(null);
                    purchaseGoodsEntity.setGbDpgPurchaseMonth(null);
                    purchaseGoodsEntity.setGbDpgPurchaseYear(null);
                    purchaseGoodsEntity.setGbDpgPurchaseWeek(null);
                    purchaseGoodsEntity.setGbDpgPurchaseFullTime(null);
                    purchaseGoodsEntity.setGbDpgPurchaseWeekYear(null);
                    purchaseGoodsEntity.setGbDpgPurUserId(null);
                    purchaseGoodsEntity.setGbDpgWarnFullTime(null);
                    purchaseGoodsEntity.setGbDpgWasteFullTime(null);
                    purchaseGoodsService.update(purchaseGoodsEntity);
                }
            }
        }

		gbDisWeightTotalService.delete(id);

        return R.ok();
    }


    /**
     *
     * @param weightTotal
     * @return
     */
    @RequestMapping(value = "/depPrintWeightOrders", method = RequestMethod.POST)
    @ResponseBody
    public R depPrintWeightOrders(@RequestBody GbDistributerWeightTotalEntity weightTotal) {


        System.out.println("typepepepepep" + weightTotal.getGbGwtType());
        weightTotal.setGbGwtDate(formatWhatDay(0));
        weightTotal.setGbGwtPrintTime(formatWhatTime(0));
        gbDisWeightTotalService.save(weightTotal);

        Integer gbDistributerWeightTotalId = weightTotal.getGbDistributerWeightTotalId();
        String nxDwOrderIds = weightTotal.getGbGwtOrderIds();
        String[] split = nxDwOrderIds.split(",");


        if(weightTotal.getGbGwtType().equals(getGbOrderTypeChuKu()) || weightTotal.getGbGwtType().equals(getGbOrderTypeKitchen())){
            for (String orderId : split) {
                GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(Integer.valueOf(orderId));
                ordersEntity.setGbDoBuyStatus(getGbOrderBuyStatusPrepareing());
                ordersEntity.setGbDoWeightTotalId(gbDistributerWeightTotalId);
                gbDepartmentOrdersService.update(ordersEntity);
            }
            String gbGwtDepDisGoodsIds = weightTotal.getGbGwtDepDisGoodsIds();
            System.out.println("ididsiisisis" + weightTotal.getGbGwtDepDisGoodsIds());
            String[] splitDep = gbGwtDepDisGoodsIds.split(",");
            for (String id : splitDep) {
                System.out.println("i======" + id);
                GbDistributerWeightGoodsEntity weightGoodsEntity = gbDisWeightGoodsService.queryObject(Integer.valueOf(id));
                weightGoodsEntity.setGbDwgStatus(getGbWeightGoodsStatusPrinted());
                weightGoodsEntity.setGbDwgWeightId(gbDistributerWeightTotalId);
                gbDisWeightGoodsService.update(weightGoodsEntity);
            }
        }else{
            for (String orderId : split) {
                GbDepartmentOrdersEntity ordersEntity = gbDepartmentOrdersService.queryObject(Integer.valueOf(orderId));
                ordersEntity.setGbDoWeightTotalId(gbDistributerWeightTotalId);
                ordersEntity.setGbDoBuyStatus(getGbOrderBuyStatusProcurement());
                gbDepartmentOrdersService.update(ordersEntity);
            }
            String gbGwtDepDisGoodsIds = weightTotal.getGbGwtDepDisGoodsIds();
            String[] splitDep = gbGwtDepDisGoodsIds.split(",");
            for (String id : splitDep) {
                GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = purchaseGoodsService.queryObject(Integer.valueOf(id));
                purchaseGoodsEntity.setGbDpgBatchId(-1);
                purchaseGoodsEntity.setGbDpgStatus(1);
                purchaseGoodsEntity.setGbDpgPayType(0);
                purchaseGoodsEntity.setGbDpgTime(formatWhatTime(0));
                purchaseGoodsEntity.setGbDpgPurchaseDate(formatWhatDay(0));
                purchaseGoodsEntity.setGbDpgPurchaseMonth(formatWhatMonth(0));
                purchaseGoodsEntity.setGbDpgPurchaseYear(formatWhatYear(0));
                purchaseGoodsEntity.setGbDpgPurchaseWeek(getWeek(0));
                purchaseGoodsEntity.setGbDpgPurUserId(weightTotal.getGbGwtUserId());
                purchaseGoodsEntity.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
                purchaseGoodsEntity.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                purchaseGoodsEntity.setGbDpgWeightId(gbDistributerWeightTotalId);
                //判断是否有保鲜时间参数
                GbDistributerGoodsEntity gbDisGoodsEntity = gbDistributerGoodsService.queryObject(purchaseGoodsEntity.getGbDpgDisGoodsId());
                if (gbDisGoodsEntity.getGbDgControlFresh() != null && gbDisGoodsEntity.getGbDgControlFresh() == 1) {
                    int warnHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWarnHour());
                    int wasteHour = Integer.parseInt(gbDisGoodsEntity.getGbDgFreshWasteHour());
                    purchaseGoodsEntity.setGbDpgWarnFullTime(formatWhatFullTime(warnHour));
                    purchaseGoodsEntity.setGbDpgWasteFullTime(formatWhatFullTime(wasteHour));
                }
                purchaseGoodsService.update(purchaseGoodsEntity);
            }
        }



        return R.ok();
    }

    @RequestMapping(value = "/depGetWeightTotal", method = RequestMethod.POST)
    @ResponseBody
    public R depGetWeightTotal (Integer depId, Integer isSelf, String startDate, String stopDate) {
        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("isSelf", isSelf);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);

        List<GbDistributerWeightTotalEntity> weightTotalEntities = gbDisWeightTotalService.queryDepWeightListByParams(map);

        return R.ok().put("data", weightTotalEntities);
    }

    @RequestMapping(value = "/depGetWeightingList", method = RequestMethod.POST)
    @ResponseBody
    public R depGetWeightingList(Integer depId, Integer isSelf, Integer status) {

        Map<String, Object> map = new HashMap<>();
        map.put("depId", depId);
        map.put("equalStatus", status);
        map.put("isSelf", isSelf);
        if(status == 1){
            map.put("startDate", formatWhatDay(-2));
            map.put("stopDate", formatWhatDay(0));
        }
        System.out.println("0000000000" + map);
        List<GbDistributerWeightTotalEntity> weightTotalEntities = gbDisWeightTotalService.queryDepWeightListByParams(map);

        return R.ok().put("data", weightTotalEntities);
    }


}

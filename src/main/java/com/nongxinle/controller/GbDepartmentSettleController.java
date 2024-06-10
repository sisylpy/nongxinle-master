package com.nongxinle.controller;

/**
 * @author lpy
 * @date 10-22 16:10
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.R;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.DateUtils.formatWhatMonth;
import static com.nongxinle.utils.GbTypeUtils.getGbDepartmentTypeKufang;


@RestController
@RequestMapping("api/gbdepartmentsettle")
public class GbDepartmentSettleController {
    @Autowired
    private GbDepartmentSettleService gbDepartmentSettleService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepGoodsStockService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepGoodsStockReduceService;
    @Autowired
    private GbDepInventoryGoodsDailyService gbDepInventoryGoodsDailyService;
    @Autowired
    private GbDepInventoryGoodsWeekService gbDepInventoryGoodsWeekService;
    @Autowired
    private GbDepInventoryGoodsMonthService gbDepInventoryGoodsMonthService;
    @Autowired
    private GbDepInventoryDailyService gbDepInventoryDailyService;
    @Autowired
    private GbDepInventoryWeekService gbDepInventoryWeekService;
    @Autowired
    private GbDepInventoryMonthService gbDepInventoryMonthService;
    @Autowired
    private GbDepartmentGoodsStockRecordService gbDepGoodsStockRecordService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private GbDepFatherGoodsSettleService gbDepFatherGoodsSettleService;
    @Autowired
    private GbDepDisGoodsSettleService gbDepDisGoodsSettleService;
    @Autowired
    private GbDepFoodGoodsSalesService gbDepFoodGoodsSalesService;
    @Autowired
    private GbDepFoodSalesService gbDepFoodSalesService;



    @RequestMapping(value = "/getDepSettle/{disId}")
    @ResponseBody
    public R getDepSettle(@PathVariable Integer disId) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("equalStatus", 0);
        List<GbDepartmentSettleEntity> settleEntities = gbDepartmentSettleService.queryDepartmentSettlesByParams(map);
        return R.ok().put("data", settleEntities);
    }

    @RequestMapping(value = "/settleMendian", method = RequestMethod.POST)
    @ResponseBody
    public R settleMendian(@RequestBody GbDepartmentSettleEntity settle) {
        System.out.println(settle);
        settle.setGbDsDate(formatWhatDate(0));
        settle.setGbDsDay(formatWhatDay(0));
        settle.setGbDsMonth(formatWhatMonth(0));
        settle.setGbDsWeek(getWeekOfYear(0).toString());
        settle.setGbDsTime(formatWhatTime(0));
        settle.setGbDsYear(formatWhatYear(0));
        settle.setGbDsStatus(0);
        gbDepartmentSettleService.save(settle);
        Integer gbDepartmentSettleId = settle.getGbDepartmentSettleId(); //结算id

        //添加fatherGoodsSettle
        GbOutStockArrEntity outStockArrEntity = settle.getGbOutStockArrEntity();

        //cost
        List<GbOutStockEntity> costArr = outStockArrEntity.getCost();
        for (GbOutStockEntity outStock : costArr) {
            List<GbDistributerFatherGoodsEntity> arr = outStock.getArr();
            for (GbDistributerFatherGoodsEntity greatGrandFather : arr) {
                // greatGrandFather
                GbDepFatherGoodsSettleEntity greatGrandGoodsSettle = new GbDepFatherGoodsSettleEntity();
                greatGrandGoodsSettle.setGbDfgssOutStockSubtotal(greatGrandFather.getFatherProduceTotalString());
                greatGrandGoodsSettle.setGbDfgssOutStockType(1);
                greatGrandGoodsSettle.setGbDfgssFatherGoodsId(greatGrandFather.getGbDistributerFatherGoodsId());
                greatGrandGoodsSettle.setGbDfgssFathersFatherId(greatGrandFather.getGbDfgFathersFatherId());
                greatGrandGoodsSettle.setGbDfgssFatherGoodsLevel(greatGrandFather.getGbDfgFatherGoodsLevel());
                greatGrandGoodsSettle.setGbDfgssDepartmentFatherId(settle.getGbDsDepId());
                greatGrandGoodsSettle.setGbDfgssDistributerId(settle.getGbDsDisId());
                greatGrandGoodsSettle.setGbDfgssFatherGoodsName(greatGrandFather.getGbDfgFatherGoodsName());
                greatGrandGoodsSettle.setGbDfgssSettleId(gbDepartmentSettleId);
                greatGrandGoodsSettle.setGbDfgssSettleMonth(formatWhatMonth(0));
                greatGrandGoodsSettle.setGbDfgssSettleYear(formatWhatYear(0));
                gbDepFatherGoodsSettleService.save(greatGrandGoodsSettle);

                //grandFather
                List<GbDistributerFatherGoodsEntity> grandFatherGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandFatherGoodsEntities) {
                    // greatGrandFather
                    GbDepFatherGoodsSettleEntity grandGoodsSettle = new GbDepFatherGoodsSettleEntity();
                    grandGoodsSettle.setGbDfgssOutStockSubtotal(grandFather.getFatherProduceTotalString());
                    grandGoodsSettle.setGbDfgssOutStockType(1);
                    grandGoodsSettle.setGbDfgssFatherGoodsId(grandFather.getGbDistributerFatherGoodsId());
                    grandGoodsSettle.setGbDfgssFathersFatherId(grandFather.getGbDfgFathersFatherId());
                    grandGoodsSettle.setGbDfgssFatherGoodsLevel(grandFather.getGbDfgFatherGoodsLevel());
                    grandGoodsSettle.setGbDfgssDepartmentFatherId(settle.getGbDsDepId());
                    grandGoodsSettle.setGbDfgssDistributerId(settle.getGbDsDisId());
                    grandGoodsSettle.setGbDfgssFatherGoodsName(grandFather.getGbDfgFatherGoodsName());
                    grandGoodsSettle.setGbDfgssSettleId(gbDepartmentSettleId);
                    grandGoodsSettle.setGbDfgssSettleMonth(formatWhatMonth(0));
                    grandGoodsSettle.setGbDfgssSettleYear(formatWhatYear(0));
                    gbDepFatherGoodsSettleService.save(grandGoodsSettle);

                    //fatherFather
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                        GbDepFatherGoodsSettleEntity fatherGoodsSettle = new GbDepFatherGoodsSettleEntity();
                        fatherGoodsSettle.setGbDfgssOutStockSubtotal(fatherGoodsEntity.getFatherProduceTotalString());
                        fatherGoodsSettle.setGbDfgssOutStockType(1);
                        fatherGoodsSettle.setGbDfgssFatherGoodsId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
                        fatherGoodsSettle.setGbDfgssFathersFatherId(fatherGoodsEntity.getGbDfgFathersFatherId());
                        fatherGoodsSettle.setGbDfgssFatherGoodsLevel(fatherGoodsEntity.getGbDfgFatherGoodsLevel());
                        fatherGoodsSettle.setGbDfgssDepartmentFatherId(settle.getGbDsDepId());
                        fatherGoodsSettle.setGbDfgssDistributerId(settle.getGbDsDisId());
                        fatherGoodsSettle.setGbDfgssFatherGoodsName(fatherGoodsEntity.getGbDfgFatherGoodsName());
                        fatherGoodsSettle.setGbDfgssSettleId(gbDepartmentSettleId);
                        fatherGoodsSettle.setGbDfgssSettleMonth(formatWhatMonth(0));
                        fatherGoodsSettle.setGbDfgssSettleYear(formatWhatYear(0));
                        gbDepFatherGoodsSettleService.save(fatherGoodsSettle);
                    }
                }
            }

        }

        //loss
        List<GbOutStockEntity> lossArr = outStockArrEntity.getLoss();
        for (GbOutStockEntity outStock : lossArr) {
            List<GbDistributerFatherGoodsEntity> arr = outStock.getArr();
            for (GbDistributerFatherGoodsEntity greatGrandFather : arr) {
                // greatGrandFather
                GbDepFatherGoodsSettleEntity greatGrandGoodsSettle = new GbDepFatherGoodsSettleEntity();
                greatGrandGoodsSettle.setGbDfgssOutStockSubtotal(greatGrandFather.getFatherLossTotalString());
                greatGrandGoodsSettle.setGbDfgssOutStockType(2);
                greatGrandGoodsSettle.setGbDfgssFatherGoodsId(greatGrandFather.getGbDistributerFatherGoodsId());
                greatGrandGoodsSettle.setGbDfgssFathersFatherId(greatGrandFather.getGbDfgFathersFatherId());
                greatGrandGoodsSettle.setGbDfgssFatherGoodsLevel(greatGrandFather.getGbDfgFatherGoodsLevel());
                greatGrandGoodsSettle.setGbDfgssDepartmentFatherId(settle.getGbDsDepId());
                greatGrandGoodsSettle.setGbDfgssDistributerId(settle.getGbDsDisId());
                greatGrandGoodsSettle.setGbDfgssFatherGoodsName(greatGrandFather.getGbDfgFatherGoodsName());
                greatGrandGoodsSettle.setGbDfgssSettleId(gbDepartmentSettleId);
                greatGrandGoodsSettle.setGbDfgssSettleMonth(formatWhatMonth(0));
                greatGrandGoodsSettle.setGbDfgssSettleYear(formatWhatYear(0));
                gbDepFatherGoodsSettleService.save(greatGrandGoodsSettle);

                //grandFather
                List<GbDistributerFatherGoodsEntity> grandFatherGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandFatherGoodsEntities) {
                    // greatGrandFather
                    GbDepFatherGoodsSettleEntity grandGoodsSettle = new GbDepFatherGoodsSettleEntity();
                    grandGoodsSettle.setGbDfgssOutStockSubtotal(grandFather.getFatherLossTotalString());
                    grandGoodsSettle.setGbDfgssOutStockType(2);
                    grandGoodsSettle.setGbDfgssFatherGoodsId(grandFather.getGbDistributerFatherGoodsId());
                    grandGoodsSettle.setGbDfgssFathersFatherId(grandFather.getGbDfgFathersFatherId());
                    grandGoodsSettle.setGbDfgssFatherGoodsLevel(grandFather.getGbDfgFatherGoodsLevel());
                    grandGoodsSettle.setGbDfgssDepartmentFatherId(settle.getGbDsDepId());
                    grandGoodsSettle.setGbDfgssDistributerId(settle.getGbDsDisId());
                    grandGoodsSettle.setGbDfgssFatherGoodsName(grandFather.getGbDfgFatherGoodsName());
                    grandGoodsSettle.setGbDfgssSettleId(gbDepartmentSettleId);
                    grandGoodsSettle.setGbDfgssSettleMonth(formatWhatMonth(0));
                    grandGoodsSettle.setGbDfgssSettleYear(formatWhatYear(0));
                    gbDepFatherGoodsSettleService.save(grandGoodsSettle);

                    //fatherFather
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                        GbDepFatherGoodsSettleEntity fatherGoodsSettle = new GbDepFatherGoodsSettleEntity();
                        fatherGoodsSettle.setGbDfgssOutStockSubtotal(fatherGoodsEntity.getFatherLossTotalString());
                        fatherGoodsSettle.setGbDfgssOutStockType(2);
                        fatherGoodsSettle.setGbDfgssFatherGoodsId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
                        fatherGoodsSettle.setGbDfgssFathersFatherId(fatherGoodsEntity.getGbDfgFathersFatherId());
                        fatherGoodsSettle.setGbDfgssFatherGoodsLevel(fatherGoodsEntity.getGbDfgFatherGoodsLevel());
                        fatherGoodsSettle.setGbDfgssDepartmentFatherId(settle.getGbDsDepId());
                        fatherGoodsSettle.setGbDfgssDistributerId(settle.getGbDsDisId());
                        fatherGoodsSettle.setGbDfgssFatherGoodsName(fatherGoodsEntity.getGbDfgFatherGoodsName());
                        fatherGoodsSettle.setGbDfgssSettleId(gbDepartmentSettleId);
                        fatherGoodsSettle.setGbDfgssSettleMonth(formatWhatMonth(0));
                        fatherGoodsSettle.setGbDfgssSettleYear(formatWhatYear(0));
                        gbDepFatherGoodsSettleService.save(fatherGoodsSettle);
                    }
                }
            }

        }

        //waste
        List<GbOutStockEntity> wasteArr = outStockArrEntity.getWaste();
        for (GbOutStockEntity outStock : wasteArr) {
            List<GbDistributerFatherGoodsEntity> arr = outStock.getArr();
            for (GbDistributerFatherGoodsEntity greatGrandFather : arr) {
                // greatGrandFather
                GbDepFatherGoodsSettleEntity greatGrandGoodsSettle = new GbDepFatherGoodsSettleEntity();
                greatGrandGoodsSettle.setGbDfgssOutStockSubtotal(greatGrandFather.getFatherWasteTotalString());
                greatGrandGoodsSettle.setGbDfgssOutStockType(3);
                greatGrandGoodsSettle.setGbDfgssFatherGoodsId(greatGrandFather.getGbDistributerFatherGoodsId());
                greatGrandGoodsSettle.setGbDfgssFathersFatherId(greatGrandFather.getGbDfgFathersFatherId());
                greatGrandGoodsSettle.setGbDfgssFatherGoodsLevel(greatGrandFather.getGbDfgFatherGoodsLevel());
                greatGrandGoodsSettle.setGbDfgssDepartmentFatherId(settle.getGbDsDepId());
                greatGrandGoodsSettle.setGbDfgssDistributerId(settle.getGbDsDisId());
                greatGrandGoodsSettle.setGbDfgssFatherGoodsName(greatGrandFather.getGbDfgFatherGoodsName());
                greatGrandGoodsSettle.setGbDfgssSettleId(gbDepartmentSettleId);
                greatGrandGoodsSettle.setGbDfgssSettleMonth(formatWhatMonth(0));
                greatGrandGoodsSettle.setGbDfgssSettleYear(formatWhatYear(0));
                gbDepFatherGoodsSettleService.save(greatGrandGoodsSettle);

                //grandFather
                List<GbDistributerFatherGoodsEntity> grandFatherGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandFatherGoodsEntities) {
                    // greatGrandFather
                    GbDepFatherGoodsSettleEntity grandGoodsSettle = new GbDepFatherGoodsSettleEntity();
                    grandGoodsSettle.setGbDfgssOutStockSubtotal(grandFather.getFatherWasteTotalString());
                    grandGoodsSettle.setGbDfgssOutStockType(3);
                    grandGoodsSettle.setGbDfgssFatherGoodsId(grandFather.getGbDistributerFatherGoodsId());
                    grandGoodsSettle.setGbDfgssFathersFatherId(grandFather.getGbDfgFathersFatherId());
                    grandGoodsSettle.setGbDfgssFatherGoodsLevel(grandFather.getGbDfgFatherGoodsLevel());
                    grandGoodsSettle.setGbDfgssDepartmentFatherId(settle.getGbDsDepId());
                    grandGoodsSettle.setGbDfgssDistributerId(settle.getGbDsDisId());
                    grandGoodsSettle.setGbDfgssFatherGoodsName(grandFather.getGbDfgFatherGoodsName());
                    grandGoodsSettle.setGbDfgssSettleId(gbDepartmentSettleId);
                    grandGoodsSettle.setGbDfgssSettleMonth(formatWhatMonth(0));
                    grandGoodsSettle.setGbDfgssSettleYear(formatWhatYear(0));
                    gbDepFatherGoodsSettleService.save(grandGoodsSettle);

                    //fatherFather
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                        GbDepFatherGoodsSettleEntity fatherGoodsSettle = new GbDepFatherGoodsSettleEntity();
                        fatherGoodsSettle.setGbDfgssOutStockSubtotal(fatherGoodsEntity.getFatherWasteTotalString());
                        fatherGoodsSettle.setGbDfgssOutStockType(3);
                        fatherGoodsSettle.setGbDfgssFatherGoodsId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
                        fatherGoodsSettle.setGbDfgssFathersFatherId(fatherGoodsEntity.getGbDfgFathersFatherId());
                        fatherGoodsSettle.setGbDfgssFatherGoodsLevel(fatherGoodsEntity.getGbDfgFatherGoodsLevel());
                        fatherGoodsSettle.setGbDfgssDepartmentFatherId(settle.getGbDsDepId());
                        fatherGoodsSettle.setGbDfgssDistributerId(settle.getGbDsDisId());
                        fatherGoodsSettle.setGbDfgssFatherGoodsName(fatherGoodsEntity.getGbDfgFatherGoodsName());
                        fatherGoodsSettle.setGbDfgssSettleId(gbDepartmentSettleId);
                        fatherGoodsSettle.setGbDfgssSettleMonth(formatWhatMonth(0));
                        fatherGoodsSettle.setGbDfgssSettleYear(formatWhatYear(0));
                        gbDepFatherGoodsSettleService.save(fatherGoodsSettle);
                    }
                }
            }

        }

        //return
        List<GbOutStockEntity> returnArr = outStockArrEntity.getReturnS();
        for (GbOutStockEntity outStock : returnArr) {
            List<GbDistributerFatherGoodsEntity> arr = outStock.getArr();
            for (GbDistributerFatherGoodsEntity greatGrandFather : arr) {
                // greatGrandFather
                GbDepFatherGoodsSettleEntity greatGrandGoodsSettle = new GbDepFatherGoodsSettleEntity();
                greatGrandGoodsSettle.setGbDfgssOutStockSubtotal(greatGrandFather.getFatherReturnTotalString());
                greatGrandGoodsSettle.setGbDfgssOutStockType(4);
                greatGrandGoodsSettle.setGbDfgssFatherGoodsId(greatGrandFather.getGbDistributerFatherGoodsId());
                greatGrandGoodsSettle.setGbDfgssFathersFatherId(greatGrandFather.getGbDfgFathersFatherId());
                greatGrandGoodsSettle.setGbDfgssFatherGoodsLevel(greatGrandFather.getGbDfgFatherGoodsLevel());
                greatGrandGoodsSettle.setGbDfgssDepartmentFatherId(settle.getGbDsDepId());
                greatGrandGoodsSettle.setGbDfgssDistributerId(settle.getGbDsDisId());
                greatGrandGoodsSettle.setGbDfgssFatherGoodsName(greatGrandFather.getGbDfgFatherGoodsName());
                greatGrandGoodsSettle.setGbDfgssSettleId(gbDepartmentSettleId);
                greatGrandGoodsSettle.setGbDfgssSettleMonth(formatWhatMonth(0));
                greatGrandGoodsSettle.setGbDfgssSettleYear(formatWhatYear(0));
                gbDepFatherGoodsSettleService.save(greatGrandGoodsSettle);

                //grandFather
                List<GbDistributerFatherGoodsEntity> grandFatherGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandFatherGoodsEntities) {
                    // greatGrandFather
                    GbDepFatherGoodsSettleEntity grandGoodsSettle = new GbDepFatherGoodsSettleEntity();
                    grandGoodsSettle.setGbDfgssOutStockSubtotal(grandFather.getFatherReturnTotalString());
                    grandGoodsSettle.setGbDfgssOutStockType(4);
                    grandGoodsSettle.setGbDfgssFatherGoodsId(grandFather.getGbDistributerFatherGoodsId());
                    grandGoodsSettle.setGbDfgssFathersFatherId(grandFather.getGbDfgFathersFatherId());
                    grandGoodsSettle.setGbDfgssFatherGoodsLevel(grandFather.getGbDfgFatherGoodsLevel());
                    grandGoodsSettle.setGbDfgssDepartmentFatherId(settle.getGbDsDepId());
                    grandGoodsSettle.setGbDfgssDistributerId(settle.getGbDsDisId());
                    grandGoodsSettle.setGbDfgssFatherGoodsName(grandFather.getGbDfgFatherGoodsName());
                    grandGoodsSettle.setGbDfgssSettleId(gbDepartmentSettleId);
                    grandGoodsSettle.setGbDfgssSettleMonth(formatWhatMonth(0));
                    grandGoodsSettle.setGbDfgssSettleYear(formatWhatYear(0));
                    gbDepFatherGoodsSettleService.save(grandGoodsSettle);

                    //fatherFather
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                        GbDepFatherGoodsSettleEntity fatherGoodsSettle = new GbDepFatherGoodsSettleEntity();
                        fatherGoodsSettle.setGbDfgssOutStockSubtotal(fatherGoodsEntity.getFatherReturnTotalString());
                        fatherGoodsSettle.setGbDfgssOutStockType(4);
                        fatherGoodsSettle.setGbDfgssFatherGoodsId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
                        fatherGoodsSettle.setGbDfgssFathersFatherId(fatherGoodsEntity.getGbDfgFathersFatherId());
                        fatherGoodsSettle.setGbDfgssFatherGoodsLevel(fatherGoodsEntity.getGbDfgFatherGoodsLevel());
                        fatherGoodsSettle.setGbDfgssDepartmentFatherId(settle.getGbDsDepId());
                        fatherGoodsSettle.setGbDfgssDistributerId(settle.getGbDsDisId());
                        fatherGoodsSettle.setGbDfgssFatherGoodsName(fatherGoodsEntity.getGbDfgFatherGoodsName());
                        fatherGoodsSettle.setGbDfgssSettleId(gbDepartmentSettleId);
                        fatherGoodsSettle.setGbDfgssSettleMonth(formatWhatMonth(0));
                        fatherGoodsSettle.setGbDfgssSettleYear(formatWhatYear(0));
                        gbDepFatherGoodsSettleService.save(fatherGoodsSettle);
                    }
                }
            }

        }

        settleMendianStockData(gbDepartmentSettleId, settle.getGbDsDepId(), settle.getGbDsMonth(), settle.getGbDsYear());
        Integer gbDsDepId = settle.getGbDsDepId();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(gbDsDepId);
        return R.ok().put("data", departmentEntity);
    }


    public void settleMendianStockData(Integer gbDepartmentSettleId, Integer depFatherId ,String  settleMonth, String settleYear) {

        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depFatherId);
        departmentEntity.setGbDepartmentSettleFullTime(formatWhatFullTime(0));
        departmentEntity.setGbDepartmentSettleDate(formatWhatDay(0));
        departmentEntity.setGbDepartmentSettleWeek(getWeekOfYear(0).toString());
        departmentEntity.setGbDepartmentSettleMonth(formatWhatMonth(0));
        departmentEntity.setGbDepartmentSettleYear(formatWhatYear(0));
        BigDecimal add = new BigDecimal(departmentEntity.getGbDepartmentSettleTimes()).add(new BigDecimal(1));
        departmentEntity.setGbDepartmentSettleTimes(add.toString());
        departmentEntity.setGbDepartmentDepSettleId(gbDepartmentSettleId); //更新结算id
        gbDepartmentService.update(departmentEntity);

        Integer gbDepartmentSubAmount = departmentEntity.getGbDepartmentSubAmount();
        if (gbDepartmentSubAmount > 0) {
            List<GbDepartmentEntity> departmentEntities = gbDepartmentService.querySubDepartments(depFatherId);
            for (GbDepartmentEntity subDep : departmentEntities) {
                subDep.setGbDepartmentSettleFullTime(formatWhatFullTime(0));
                subDep.setGbDepartmentSettleDate(formatWhatDay(0));
                subDep.setGbDepartmentSettleWeek(getWeekOfYear(0).toString());
                subDep.setGbDepartmentSettleMonth(formatWhatMonth(0));
                subDep.setGbDepartmentSettleYear(formatWhatYear(0));
                BigDecimal add1 = new BigDecimal(departmentEntity.getGbDepartmentSettleTimes()).add(new BigDecimal(1));
                subDep.setGbDepartmentSettleTimes(add1.toString());
                subDep.setGbDepartmentDepSettleId(gbDepartmentSettleId); //更新结算id
                gbDepartmentService.update(subDep);
            }
        }

        //2,udpate goods_stock
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryStockListByParams(depFatherId);

        for (GbDepartmentGoodsStockEntity stock : stockEntities) {

            //1，添加stockRecord
            GbDepartmentGoodsStockRecordEntity recordEntity = new GbDepartmentGoodsStockRecordEntity();
            recordEntity.setGbDgscDate(stock.getGbDgsDate());
            recordEntity.setGbDgscDoWasteFullTime(stock.getGbDgsDoWasteFullTime());
            recordEntity.setGbDgscDoWasteUserId(stock.getGbDgsReduceWeightUserId());
            recordEntity.setGbDgscFullTime(stock.getGbDgsFullTime());
            recordEntity.setGbDgscGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            recordEntity.setGbDgscGbDepartmentId(stock.getGbDgsGbDepartmentId());
            recordEntity.setGbDgscGbDepartmentOrderId(stock.getGbDgsGbDepartmentOrderId());
            recordEntity.setGbDgscGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
            recordEntity.setGbDgscWarnFullTime(stock.getGbDgsWarnFullTime());
            recordEntity.setGbDgscGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            recordEntity.setGbDgscGbFromDepartmentId(stock.getGbDgsGbFromDepartmentId());
            recordEntity.setGbDgscGbPurGoodsId(stock.getGbDgsGbPurGoodsId());
            recordEntity.setGbDgscWeek(stock.getGbDgsWeek());
            recordEntity.setGbDgscMonth(stock.getGbDgsMonth());
            recordEntity.setGbDgscYear(stock.getGbDgsYear());
            recordEntity.setGbDgscTimeStamp(stock.getGbDgsTimeStamp());
            recordEntity.setGbDgscInventoryDate(stock.getGbDgsInventoryDate());
            recordEntity.setGbDgscInventoryWeek(stock.getGbDgsInventoryWeek());
            recordEntity.setGbDgscInventoryMonth(stock.getGbDgsInventoryMonth());
            recordEntity.setGbDgscGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
            recordEntity.setGbDgscWeight(stock.getGbDgsWeight());
            recordEntity.setGbDgscSubtotal(stock.getGbDgsSubtotal());
            recordEntity.setGbDgscPrice(stock.getGbDgsPrice());
            recordEntity.setGbDgscRestWeight(stock.getGbDgsRestWeight());
            recordEntity.setGbDgscRestSubtotal(stock.getGbDgsRestSubtotal());
            recordEntity.setGbDgscReceiveUserId(stock.getGbDgsReceiveUserId());
            recordEntity.setGbDgscGbGoodsStockId(stock.getGbDgsGbGoodsStockId());
            recordEntity.setGbDgscLossWeight(stock.getGbDgsLossWeight());
            recordEntity.setGbDgscLossSubtotal(stock.getGbDgsLossSubtotal());
            recordEntity.setGbDgscDepSettleId(gbDepartmentSettleId);
            recordEntity.setGbDgscStatus(0);
            recordEntity.setGbDgscFromGbGoodsStockId(stock.getGbDepartmentGoodsStockId());
            gbDepGoodsStockRecordService.save(recordEntity);


            //1.1.1
            Map<String, Object> mapGoodsSettle = new HashMap<>();
            mapGoodsSettle.put("depId", stock.getGbDgsGbDepartmentId());
            mapGoodsSettle.put("month", settleMonth);
            mapGoodsSettle.put("disGoodsId", stock.getGbDgsGbDisGoodsId());
            BigDecimal useWeight = new BigDecimal(stock.getGbDgsWeight()).subtract(new BigDecimal(stock.getGbDgsRestWeight()));
            BigDecimal useSubtotal = new BigDecimal(stock.getGbDgsSubtotal()).subtract(new BigDecimal(stock.getGbDgsRestSubtotal()));
            GbDepDisGoodsSettleEntity settleEntity = gbDepDisGoodsSettleService.queryDisGoodsSettleByParams(mapGoodsSettle);
            if(settleEntity != null){
                BigDecimal settleOldWeight = new BigDecimal(settleEntity.getGbDdgsSettleAmount());
                BigDecimal settleNewWeight = useWeight.add(settleOldWeight).setScale(2,BigDecimal.ROUND_HALF_UP);
                BigDecimal settleOldSubtotal = new BigDecimal(settleEntity.getGbDdgsSettleSubtotal());
                BigDecimal settleNewSubtotal = useSubtotal.add(settleOldSubtotal).setScale(2, BigDecimal.ROUND_HALF_UP);
                settleEntity.setGbDdgsSettleAmount(settleNewWeight.toString());
                settleEntity.setGbDdgsSettleSubtotal(settleNewSubtotal.toString());
                settleEntity.setGbDdgsSettleId(gbDepartmentSettleId);
                gbDepDisGoodsSettleService.update(settleEntity);
            }else{
                GbDepDisGoodsSettleEntity newGoodsSettleEntity = new GbDepDisGoodsSettleEntity();
                newGoodsSettleEntity.setGbDdgsSettleSubtotal(useSubtotal.toString());
                newGoodsSettleEntity.setGbDdgsSettleAmount(useWeight.toString());
                newGoodsSettleEntity.setGbDdgsSettleId(gbDepartmentSettleId);
                newGoodsSettleEntity.setGbDdgsDfgGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
                newGoodsSettleEntity.setGbDdgsDisControlFresh(stock.getGbDistributerGoodsEntity().getGbDgControlFresh());
                newGoodsSettleEntity.setGbDdgsDisControlPrice(stock.getGbDistributerGoodsEntity().getGbDgControlPrice());
                newGoodsSettleEntity.setGbDdgsDisFreshWarnHour(stock.getGbDistributerGoodsEntity().getGbDgFreshWarnHour());
                newGoodsSettleEntity.setGbDdgsDisFreshWasteHour(stock.getGbDistributerGoodsEntity().getGbDgFreshWasteHour());
                newGoodsSettleEntity.setGbDdgsDisGoodsPrice(stock.getGbDistributerGoodsEntity().getGbDgGoodsPrice());
                newGoodsSettleEntity.setGbDdgsDisGoodsLowestPrice(stock.getGbDistributerGoodsEntity().getGbDgGoodsLowestPrice());
                newGoodsSettleEntity.setGbDdgsDisGoodsHighestPrice(stock.getGbDistributerGoodsEntity().getGbDgGoodsHighestPrice());
                newGoodsSettleEntity.setGbDdgsDisGoodsId(stock.getGbDistributerGoodsEntity().getGbDistributerGoodsId());
                newGoodsSettleEntity.setGbDdgsDisGoodsInventoryDepId(stock.getGbDistributerGoodsEntity().getGbDgGbDepartmentId());
                newGoodsSettleEntity.setGbDdgsDisGoodsInventoryType(stock.getGbDistributerGoodsEntity().getGbDgGoodsInventoryType());
                newGoodsSettleEntity.setGbDdgsDisGoodsName(stock.getGbDistributerGoodsEntity().getGbDgGoodsName());
                newGoodsSettleEntity.setGbDdgsDisGoodsStandardname(stock.getGbDistributerGoodsEntity().getGbDgGoodsStandardname());
                newGoodsSettleEntity.setGbDdgsDisGoodsStandardWeight(stock.getGbDistributerGoodsEntity().getGbDgGoodsStandardWeight());
                newGoodsSettleEntity.setGbDdgsDisGoodsType(stock.getGbDistributerGoodsEntity().getGbDgGoodsType());
                newGoodsSettleEntity.setGbDdgsSettleDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
                newGoodsSettleEntity.setGbDdgsSettleDepartmentId(stock.getGbDgsGbDepartmentId());
                newGoodsSettleEntity.setGbDdgsDistributerId(stock.getGbDgsGbDistributerId());
                newGoodsSettleEntity.setGbDdgsSettleMonth(settleMonth);
                newGoodsSettleEntity.setGbDdgsSettleYear(settleYear);
                newGoodsSettleEntity.setGbDdgsSalesAmount("0");
                newGoodsSettleEntity.setGbDdgsStatus(0);
                gbDepDisGoodsSettleService.save(newGoodsSettleEntity);

            }

            //2.1.1.1update food-goods-sales
            Map<String, Object> mapFoodGoodsSales = new HashMap<>();
            mapFoodGoodsSales.put("depFatherId", depFatherId);
            mapFoodGoodsSales.put("settleId", -1 );
            List<GbDepFoodGoodsSalesEntity> salesEntities =  gbDepFoodGoodsSalesService.queryDepFoodGoodsSalesByParams(mapFoodGoodsSales);
            for (GbDepFoodGoodsSalesEntity foodGoodsSalesEntity: salesEntities) {
                foodGoodsSalesEntity.setGbDfgsSettleId(gbDepartmentSettleId);
                gbDepFoodGoodsSalesService.update(foodGoodsSalesEntity);
            }

            //2.1.2.1update food-sales
            Map<String, Object> mapFoodSales = new HashMap<>();
            mapFoodSales.put("depFatherId", depFatherId);
            mapFoodSales.put("settleId", -1 );
            List<GbDepFoodSalesEntity> foodSalesEtities =  gbDepFoodSalesService.queryDepFoodSalesByParams(mapFoodSales);
            for (GbDepFoodSalesEntity foodSalesEntity: foodSalesEtities) {
                foodSalesEntity.setGbDfsSettleId(gbDepartmentSettleId);
                gbDepFoodSalesService.update(foodSalesEntity);
            }





            //2，更新stock
            if (stock.getGbDgsRestWeight().equals("0") || stock.getGbDgsRestWeight().equals("0.0") || stock.getGbDgsRestWeight().equals("0.00")) {
                //删除stock
                Integer gbDepartmentGoodsStockId = stock.getGbDepartmentGoodsStockId();
                gbDepGoodsStockService.delete(gbDepartmentGoodsStockId);
            } else {
                //更新stock 未起始数据
                stock.setGbDgsWeight(stock.getGbDgsRestWeight());
                stock.setGbDgsSubtotal(stock.getGbDgsRestSubtotal());
                stock.setGbDgsRestSubtotal(stock.getGbDgsSubtotal());
                stock.setGbDgsRestWeight(stock.getGbDgsRestWeight());
                stock.setGbDgsLossWeight("0");
                stock.setGbDgsLossSubtotal("0");
                stock.setGbDgsReturnWeight("0");
                stock.setGbDgsReturnSubtotal("0");
                stock.setGbDgsProduceWeight("0");
                stock.setGbDgsProduceSubtotal("0");
                stock.setGbDgsWasteWeight("0");
                stock.setGbDgsWasteSubtotal("0");
                stock.setGbDgsDepSettleId(gbDepartmentSettleId);
                gbDepGoodsStockService.update(stock);
            }
        }

        //3,update goods_daily
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("status", 1);
        List<GbDepInventoryGoodsDailyEntity> dailyEntities = gbDepInventoryGoodsDailyService.queryDailyStockListByParams(map);
        if (dailyEntities.size() > 0) {
            for (GbDepInventoryGoodsDailyEntity daily : dailyEntities) {
                daily.setGbIgdStatus(1);
                daily.setGbIgdDepSettleId(gbDepartmentSettleId);
                gbDepInventoryGoodsDailyService.update(daily);
            }
        }

        //4, update goods_week
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depFatherId", depFatherId);
        map1.put("status", 1);
        List<GbDepInventoryGoodsWeekEntity> weekEntities = gbDepInventoryGoodsWeekService.queryWeekStockListByParams(map1);
        if (weekEntities.size() > 0) {
            for (GbDepInventoryGoodsWeekEntity weekEntity : weekEntities) {
                weekEntity.setGbIgwStatus(1);
                weekEntity.setGbIgwDepSettleId(gbDepartmentSettleId);
                gbDepInventoryGoodsWeekService.update(weekEntity);
            }
        }

        //5, update goods_month
        Map<String, Object> map2 = new HashMap<>();
        map2.put("depFatherId", depFatherId);
        map2.put("status", 1);
        List<GbDepInventoryGoodsMonthEntity> monthEntities = gbDepInventoryGoodsMonthService.queryMonthStockListByParams(map2);
        if (monthEntities.size() > 0) {
            for (GbDepInventoryGoodsMonthEntity monthEntity : monthEntities) {
                monthEntity.setGbIgmStatus(1);
                monthEntity.setGbIgmDepSettleId(gbDepartmentSettleId);
                gbDepInventoryGoodsMonthService.update(monthEntity);
            }
        }

        //6, update inventory_daily
        Map<String, Object> map3 = new HashMap<>();
        map3.put("depFatherId", depFatherId);
        map3.put("status", 1);
        List<GbDepInventoryDailyEntity> inventoryDailyEntities = gbDepInventoryDailyService.queryInventoryDailyListByParams(map3);
        if (inventoryDailyEntities.size() > 0) {
            for (GbDepInventoryDailyEntity inventoryDailyEntity : inventoryDailyEntities) {
                inventoryDailyEntity.setGbIdStatus(1);
                inventoryDailyEntity.setGbIdDepSettleId(gbDepartmentSettleId);
                gbDepInventoryDailyService.update(inventoryDailyEntity);
            }
        }

        //7, update inventory_week
        Map<String, Object> map4 = new HashMap<>();
        map4.put("depFatherId", depFatherId);
        map4.put("status", 1);
        List<GbDepInventoryWeekEntity> inventoryWeekEntities = gbDepInventoryWeekService.queryInventoryWeekListByParams(map4);
        if (inventoryWeekEntities.size() > 0) {
            for (GbDepInventoryWeekEntity inventoryWeekEntity : inventoryWeekEntities) {
                inventoryWeekEntity.setGbDiwStatus(1);
                inventoryWeekEntity.setGbDiwDepSettleId(gbDepartmentSettleId);
                gbDepInventoryWeekService.update(inventoryWeekEntity);
            }
        }

        //8, update_inventory_month
        Map<String, Object> map5 = new HashMap<>();
        map5.put("depFatherId", depFatherId);
        map5.put("status", 1);
        List<GbDepInventoryMonthEntity> inventoryMonthEntities = gbDepInventoryMonthService.queryInventoryWeekListByParams(map5);
        if (inventoryMonthEntities.size() > 0) {
            for (GbDepInventoryMonthEntity inventoryWeekEntity : inventoryMonthEntities) {
                inventoryWeekEntity.setGbImStatus(1);
                inventoryWeekEntity.setGbImDepSettleId(gbDepartmentSettleId);
                gbDepInventoryMonthService.update(inventoryWeekEntity);
            }
        }


        //10, update department_bill
        //本月的账单
        Map<String, Object> mapBill = new HashMap<>();
        mapBill.put("depId", depFatherId);
        mapBill.put("settleId", -1);

        List<GbDepartmentBillEntity> billEntityList = gbDepartmentBillService.queryBillsByParamsGb(mapBill);
        for (GbDepartmentBillEntity bill : billEntityList) {
            bill.setGbDbConfirmSettleTime(formatFullTime());
            bill.setGbDbDepSettleId(gbDepartmentSettleId);
            gbDepartmentBillService.update(bill);
        }

        //12, update stock_reduce
        Map<String, Object> mapRed = new HashMap<>();
        mapRed.put("depFatherId", depFatherId);
        mapRed.put("settleId", -1);
        List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepGoodsStockReduceService.queryStockReduceListByParams(mapRed);
        for (GbDepartmentGoodsStockReduceEntity reduce : reduceEntities) {
            reduce.setGbDgsrDepSettleId(gbDepartmentSettleId);
            gbDepGoodsStockReduceService.update(reduce);
        }
    }


    @RequestMapping(value = "/settleKufang", method = RequestMethod.POST)
    @ResponseBody
    public R settleKufang(@RequestBody GbDepartmentSettleEntity settle) {
        //0
        settle.setGbDsDate(formatWhatDate(0));
        settle.setGbDsDay(formatWhatDay(0));
        settle.setGbDsMonth(formatWhatMonth(0));
        settle.setGbDsWeek(getWeekOfYear(0).toString());
        settle.setGbDsTime(formatWhatTime(0));
        settle.setGbDsYear(formatWhatYear(0));
        settle.setGbDsStatus(0);
        gbDepartmentSettleService.save(settle);
        Integer gbDepartmentSettleId = settle.getGbDepartmentSettleId(); //结算id

        //1,update Dep
        Integer depFatherId = settle.getGbDsDepId();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(depFatherId);
        Integer gbDepartmentType = departmentEntity.getGbDepartmentType();
        departmentEntity.setGbDepartmentSettleFullTime(formatWhatFullTime(0));
        departmentEntity.setGbDepartmentSettleDate(formatWhatDay(0));
        departmentEntity.setGbDepartmentSettleWeek(getWeekOfYear(0).toString());
        departmentEntity.setGbDepartmentSettleMonth(formatWhatMonth(0));
        departmentEntity.setGbDepartmentSettleYear(formatWhatYear(0));
        BigDecimal add = new BigDecimal(departmentEntity.getGbDepartmentSettleTimes()).add(new BigDecimal(1));
        departmentEntity.setGbDepartmentSettleTimes(add.toString());
        departmentEntity.setGbDepartmentDepSettleId(gbDepartmentSettleId); //更新结算id
        gbDepartmentService.update(departmentEntity);


        //2,udpate goods_stock
        List<GbDepartmentGoodsStockEntity> stockEntities = gbDepGoodsStockService.queryStockListByParams(depFatherId);

        for (GbDepartmentGoodsStockEntity stock : stockEntities) {
            //1，添加stockRecord
            GbDepartmentGoodsStockRecordEntity recordEntity = new GbDepartmentGoodsStockRecordEntity();
            recordEntity.setGbDgscDate(stock.getGbDgsDate());
            recordEntity.setGbDgscDoWasteFullTime(stock.getGbDgsDoWasteFullTime());
            recordEntity.setGbDgscDoWasteUserId(stock.getGbDgsReduceWeightUserId());
            recordEntity.setGbDgscFullTime(stock.getGbDgsFullTime());
            recordEntity.setGbDgscGbDepartmentFatherId(stock.getGbDgsGbDepartmentFatherId());
            recordEntity.setGbDgscGbDepartmentId(stock.getGbDgsGbDepartmentId());
            recordEntity.setGbDgscGbDepartmentOrderId(stock.getGbDgsGbDepartmentOrderId());
            recordEntity.setGbDgscGbDepDisGoodsId(stock.getGbDgsGbDepDisGoodsId());
            recordEntity.setGbDgscWarnFullTime(stock.getGbDgsWarnFullTime());
            recordEntity.setGbDgscGbDisGoodsFatherId(stock.getGbDgsGbDisGoodsFatherId());
            recordEntity.setGbDgscGbFromDepartmentId(stock.getGbDgsGbFromDepartmentId());
            recordEntity.setGbDgscGbPurGoodsId(stock.getGbDgsGbPurGoodsId());
            recordEntity.setGbDgscWeek(stock.getGbDgsWeek());
            recordEntity.setGbDgscMonth(stock.getGbDgsMonth());
            recordEntity.setGbDgscYear(stock.getGbDgsYear());
            recordEntity.setGbDgscTimeStamp(stock.getGbDgsTimeStamp());
            recordEntity.setGbDgscInventoryDate(stock.getGbDgsInventoryDate());
            recordEntity.setGbDgscInventoryWeek(stock.getGbDgsInventoryWeek());
            recordEntity.setGbDgscInventoryMonth(stock.getGbDgsInventoryMonth());
            recordEntity.setGbDgscGbDisGoodsId(stock.getGbDgsGbDisGoodsId());
            recordEntity.setGbDgscWeight(stock.getGbDgsWeight());
            recordEntity.setGbDgscSubtotal(stock.getGbDgsSubtotal());
            recordEntity.setGbDgscPrice(stock.getGbDgsPrice());
            recordEntity.setGbDgscRestWeight(stock.getGbDgsRestWeight());
            recordEntity.setGbDgscRestSubtotal(stock.getGbDgsRestSubtotal());
            recordEntity.setGbDgscReceiveUserId(stock.getGbDgsReceiveUserId());
            recordEntity.setGbDgscGbGoodsStockId(stock.getGbDgsGbGoodsStockId());
            recordEntity.setGbDgscLossWeight(stock.getGbDgsLossWeight());
            recordEntity.setGbDgscLossSubtotal(stock.getGbDgsLossSubtotal());
            recordEntity.setGbDgscDepSettleId(gbDepartmentSettleId);
            recordEntity.setGbDgscStatus(0);
            gbDepGoodsStockRecordService.save(recordEntity);

            //2，更新stock
            if (stock.getGbDgsRestWeight().equals("0") || stock.getGbDgsRestWeight().equals("0.0")) {
                //删除stock
                Integer gbDepartmentGoodsStockId = stock.getGbDepartmentGoodsStockId();
                gbDepGoodsStockService.delete(gbDepartmentGoodsStockId);

            } else {
                //更新stock 未起始数据
                stock.setGbDgsWeight(stock.getGbDgsRestWeight());
                stock.setGbDgsSubtotal(stock.getGbDgsRestSubtotal());
                stock.setGbDgsRestSubtotal(stock.getGbDgsSubtotal());
                stock.setGbDgsRestWeight(stock.getGbDgsRestWeight());
                stock.setGbDgsLossWeight("0");
                stock.setGbDgsLossSubtotal("0");
                stock.setGbDgsReturnWeight("0");
                stock.setGbDgsReturnSubtotal("0");
                stock.setGbDgsProduceWeight("0");
                stock.setGbDgsProduceSubtotal("0");
                stock.setGbDgsWasteWeight("0");
                stock.setGbDgsWasteSubtotal("0");
                stock.setGbDgsDepSettleId(gbDepartmentSettleId);
                stock.setGbDgsStatus(1);
                gbDepGoodsStockService.update(stock);
            }
        }

        //3,update goods_daily
        Map<String, Object> map = new HashMap<>();
        map.put("depFatherId", depFatherId);
        map.put("status", 1);
        List<GbDepInventoryGoodsDailyEntity> dailyEntities = gbDepInventoryGoodsDailyService.queryDailyStockListByParams(map);
        if (dailyEntities.size() > 0) {
            for (GbDepInventoryGoodsDailyEntity daily : dailyEntities) {
                daily.setGbIgdStatus(1);
                daily.setGbIgdDepSettleId(gbDepartmentSettleId);
                gbDepInventoryGoodsDailyService.update(daily);
            }
        }


        //4, update goods_week
        Map<String, Object> map1 = new HashMap<>();
        map1.put("depFatherId", depFatherId);
        map1.put("status", 1);
        List<GbDepInventoryGoodsWeekEntity> weekEntities = gbDepInventoryGoodsWeekService.queryWeekStockListByParams(map1);
        if (weekEntities.size() > 0) {
            for (GbDepInventoryGoodsWeekEntity weekEntity : weekEntities) {
                weekEntity.setGbIgwStatus(1);
                weekEntity.setGbIgwDepSettleId(gbDepartmentSettleId);
                gbDepInventoryGoodsWeekService.update(weekEntity);
            }
        }

        //5, update goods_month
        Map<String, Object> map2 = new HashMap<>();
        map2.put("depFatherId", depFatherId);
        map2.put("status", 1);
        List<GbDepInventoryGoodsMonthEntity> monthEntities = gbDepInventoryGoodsMonthService.queryMonthStockListByParams(map2);
        if (monthEntities.size() > 0) {
            for (GbDepInventoryGoodsMonthEntity monthEntity : monthEntities) {
                monthEntity.setGbIgmStatus(1);
                monthEntity.setGbIgmDepSettleId(gbDepartmentSettleId);
                gbDepInventoryGoodsMonthService.update(monthEntity);
            }
        }

        //6, update inventory_daily
        Map<String, Object> map3 = new HashMap<>();
        map3.put("depFatherId", depFatherId);
        map3.put("status", 1);
        List<GbDepInventoryDailyEntity> inventoryDailyEntities = gbDepInventoryDailyService.queryInventoryDailyListByParams(map3);
        if (inventoryDailyEntities.size() > 0) {
            for (GbDepInventoryDailyEntity inventoryDailyEntity : inventoryDailyEntities) {
                inventoryDailyEntity.setGbIdStatus(1);
                inventoryDailyEntity.setGbIdDepSettleId(gbDepartmentSettleId);
                gbDepInventoryDailyService.update(inventoryDailyEntity);
            }
        }

        //7, update inventory_week
        Map<String, Object> map4 = new HashMap<>();
        map4.put("depFatherId", depFatherId);
        map4.put("status", 1);
        List<GbDepInventoryWeekEntity> inventoryWeekEntities = gbDepInventoryWeekService.queryInventoryWeekListByParams(map4);
        if (inventoryWeekEntities.size() > 0) {
            for (GbDepInventoryWeekEntity inventoryWeekEntity : inventoryWeekEntities) {
                inventoryWeekEntity.setGbDiwStatus(1);
                inventoryWeekEntity.setGbDiwDepSettleId(gbDepartmentSettleId);
                gbDepInventoryWeekService.update(inventoryWeekEntity);
            }
        }

        //8, update_inventory_month
        Map<String, Object> map5 = new HashMap<>();
        map5.put("depFatherId", depFatherId);
        map5.put("status", 1);
        List<GbDepInventoryMonthEntity> inventoryMonthEntities = gbDepInventoryMonthService.queryInventoryWeekListByParams(map5);
        if (inventoryMonthEntities.size() > 0) {
            for (GbDepInventoryMonthEntity inventoryWeekEntity : inventoryMonthEntities) {
                inventoryWeekEntity.setGbImStatus(1);
                inventoryWeekEntity.setGbImDepSettleId(gbDepartmentSettleId);
                gbDepInventoryMonthService.update(inventoryWeekEntity);
            }
        }

        //9,
        if (gbDepartmentType.equals(getGbDepartmentTypeKufang())) {
            //  update 出库settleId
            Map<String, Object> map6 = new HashMap<>();
            map6.put("fromDepId", depFatherId);
            map6.put("fromSettleId", -1);

            int count = gbDepGoodsStockService.queryGoodsStockCount(map6);
            if (count > 0) {
                List<GbDepartmentGoodsStockEntity> gbDepartmentGoodsStockEntities = gbDepGoodsStockService.queryGoodsStockByParams(map6);
                for (GbDepartmentGoodsStockEntity goodsStockEntity : gbDepartmentGoodsStockEntities) {
                    goodsStockEntity.setGbDgsFromDepSettleId(gbDepartmentSettleId);
                    gbDepGoodsStockService.update(goodsStockEntity);
                }
            }

            //  update 出库settleId

            List<GbDepartmentGoodsStockRecordEntity> gbDepartmentGoodsStockRecordEntities = gbDepGoodsStockRecordService.queryGoodsStockListByParams(map6);
            if (gbDepartmentGoodsStockRecordEntities.size() > 0) {
                for (GbDepartmentGoodsStockRecordEntity goodsStockEntity : gbDepartmentGoodsStockRecordEntities) {
                    goodsStockEntity.setGbDgscFromDepSettleId(gbDepartmentSettleId);
                    gbDepGoodsStockRecordService.update(goodsStockEntity);
                }
            }

//            List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepGoodsStockReduceService.queryStockReduceListByParams(map6);
//            if(reduceEntities.size() > 0){
//                for (GbDepartmentGoodsStockReduceEntity reduce : reduceEntities) {
//                    reduce.setGbDgsrDepSettleId(gbDepartmentSettleId);
//                    gbDepGoodsStockReduceService.update(reduce);
//                }
//            }


        }


        //10, update department_bill
        //本月的账单
        Map<String, Object> mapBill = new HashMap<>();
        mapBill.put("depId", depFatherId);
        mapBill.put("settleId", -1);

        List<GbDepartmentBillEntity> billEntityList = gbDepartmentBillService.queryBillsByParamsGb(mapBill);
        for (GbDepartmentBillEntity bill : billEntityList) {
            bill.setGbDbStatus(3);
            bill.setGbDbConfirmSettleTime(formatFullTime());
            bill.setGbDbDepSettleId(gbDepartmentSettleId);
            gbDepartmentBillService.update(bill);
        }

        //12, update stock_reduce
        Map<String, Object> mapRed = new HashMap<>();
        mapRed.put("depFatherId", depFatherId);
        mapRed.put("settleId", -1);
        List<GbDepartmentGoodsStockReduceEntity> reduceEntities = gbDepGoodsStockReduceService.queryStockReduceListByParams(mapRed);
        for (GbDepartmentGoodsStockReduceEntity reduce : reduceEntities) {
            reduce.setGbDgsrDepSettleId(gbDepartmentSettleId);
            gbDepGoodsStockReduceService.update(reduce);
        }

        return R.ok();
    }


}

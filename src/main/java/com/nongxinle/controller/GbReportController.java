package com.nongxinle.controller;

/**
 * @author lpy
 * @date 09-26 20:05
 */

import java.math.BigDecimal;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.sun.tools.internal.xjc.reader.dtd.bindinfo.BIAttribute;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.omg.PortableInterceptor.INACTIVE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.nongxinle.utils.DateUtils.afterWhatDay;
import static com.nongxinle.utils.DateUtils.getHowManyDaysInPeriod;
import static com.nongxinle.utils.GbTypeUtils.*;


@RestController
@RequestMapping("api/gbreport")
public class GbReportController {
    @Autowired
    private GbReportService gbReportService;
    @Autowired
    private GbDepartmentGoodsStockService gbDepGoodsStockService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDistributerFatherGoodsService fatherGoodsService;
    @Autowired
    private GbDepartmentGoodsDailyService gbDepGoodsDailyService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerUserService gbDistributerUserService;
    @Autowired
    private GbDistributerGoodsPriceService gbDistributerGoodsPriceService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepartmentStockReduceService;


    @RequestMapping(value = "/delteReport/{id}")
    @ResponseBody
    public R delteReport(@PathVariable Integer id) {
        gbReportService.delete(id);
        return R.ok();
    }


    @RequestMapping(value = "/saveReportCost", method = RequestMethod.POST)
    @ResponseBody
    public R saveReportCost(@RequestBody GbReportEntity reportEntity) {
        gbReportService.save(reportEntity);
        return R.ok();
    }

    @RequestMapping(value = "/getDisUserReports")
    @ResponseBody
    public R getDisUserReports(Integer userId) {

        Map<String, Object> map1 = new HashMap<>();
        map1.put("userId", userId);
        List<GbReportEntity> reportEntities = gbReportService.queryReportList(map1);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (GbReportEntity report : reportEntities) {
            String gbRepType = report.getGbRepType();
            if (gbRepType.equals("nxDisControlPrice")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("nxDisId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaNxDisControlGoodsPriceTotal(map);
                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
                NxDistributerEntity fatherGoodsEntity = nxDistributerService.queryObject(gbRepIds);
                stringObjectMap.put("name", fatherGoodsEntity.getNxDistributerName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "价控商品统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("disControlPrice")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaDisControlGoodsPriceTotal(map);
                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
                GbDepartmentEntity fatherGoodsEntity = gbDepartmentService.queryObject(gbRepIds);
                stringObjectMap.put("name", fatherGoodsEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "价控商品统计");
                resultList.add(stringObjectMap);
            }


            if (gbRepType.equals("nxDisPurchaseCost")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("nxDisId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaNxDisPurchaseCost(map);
                NxDistributerEntity departmentEntity = nxDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getNxDistributerName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "门店成本统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("disPurchaseCost")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("fromDepId", report.getGbRepIds());
                map.put("purDepId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                map.put("notEqualStockId", -1);
                Map<String, Object> stringObjectMap = aaaDisPurchaseCost(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "门店成本统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("disPurchaseCostPur")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("fromDepId", report.getGbRepIds());
                map.put("purDepId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                map.put("stockId", -1);
                Map<String, Object> stringObjectMap = aaaDisPurchaseCost(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "集采门店成本统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("nxDisOutGoodsFresh")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("nxDisId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaNxDisOutGoodsFreshTotal(map);
                NxDistributerEntity departmentEntity = nxDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getNxDistributerName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "保鲜商品统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("disOutGoodsFresh")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("toDepId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaDisOutGoodsFreshTotal(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "保鲜商品统计");
                resultList.add(stringObjectMap);
            }


            if (gbRepType.equals("disOutWeightAndSubtotal")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("fromDepId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
//                map.put("notEqualStockId", -1);
                Map<String, Object> stringObjectMap = aaaDisOutWeightAndSubtotal(map);

                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "出货商品统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("disOutWeightAndSubtotalPur")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("fromDepId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                map.put("stockId", -1);
                System.out.println("zhbeebebbeebbeb" + map);
                Map<String, Object> stringObjectMap = aaaDisOutWeightAndSubtotal(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "集采出货商品统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("nxDisOutWeightAndSubtotal")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("nxDisId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaNxDisOutWeightAndSubtotalTotal(map);

                NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", nxDistributerEntity.getNxDistributerName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "出货商品统计");
                resultList.add(stringObjectMap);
            }

            if (gbRepType.equals("depSalesSubtotal")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaDepSalesSubtotalTotal(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "销售统计");
                resultList.add(stringObjectMap);
            }

            if (gbRepType.equals("depSalesWeight")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaDepSalesWeightTotal(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "销售统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("depParameter")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                Map<String, Object> stringObjectMap = aaaDepParameterTotal(map, departmentEntity.getGbDepartmentDisId());
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "日鲜率统计");
                resultList.add(stringObjectMap);
            }

            if (gbRepType.equals("depProfit")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaDepProfitTotal(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "利润统计");
                resultList.add(stringObjectMap);
            }

            if (gbRepType.equals("depBusiness")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaDepBusinessData(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "部门商品");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("depStockNow")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaDepStockTotalNow(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "库存商品统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("depCost")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaDepCost(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "门店成本统计");
                resultList.add(stringObjectMap);
            }

            if (gbRepType.equals("depLoss")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaDepLoss(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "商品损耗");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("depWaste")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaDepWaste(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "商品废弃");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("depReturn")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaDepReturn(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "商品退货");
                resultList.add(stringObjectMap);
            }


            if (gbRepType.equals("goodsSales")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaGoodsSalesTotal(map);
                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
                GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(gbRepIds);
                stringObjectMap.put("name", fatherGoodsEntity.getGbDfgFatherGoodsName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "商品销售统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("goodsProfit")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaGoodsProfitTotal(map);
                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
                GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(gbRepIds);
                stringObjectMap.put("name", fatherGoodsEntity.getGbDfgFatherGoodsName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "商品利润统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("goodsCost")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaGoodsCostTotal(map);
                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
                GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(gbRepIds);
                stringObjectMap.put("name", fatherGoodsEntity.getGbDfgFatherGoodsName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "商品成本统计");
                resultList.add(stringObjectMap);
            }

            if (gbRepType.equals("goodsFresh")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = aaaGoodsFreshTotal(map);
                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
                GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(gbRepIds);
                stringObjectMap.put("name", fatherGoodsEntity.getGbDfgFatherGoodsName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "保鲜商品统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("goodsPrice")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                System.out.println("zahuidhsishisdifas" + map);
                Map<String, Object> stringObjectMap = aaaGoodsPriceTotal(map);
                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
                GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(gbRepIds);
                stringObjectMap.put("name", fatherGoodsEntity.getGbDfgFatherGoodsName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "价控商品统计");
                resultList.add(stringObjectMap);
            }

            if (gbRepType.equals("goodsByDepartment")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsFatherId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                System.out.println("zahuidhsishisdifas" + map);
                Map<String, Object> stringObjectMap = aaaGoodsCostTotalByDepartment(map);
                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
                GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(gbRepIds);
                stringObjectMap.put("name", fatherGoodsEntity.getGbDfgFatherGoodsName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "部门商品统计");
                resultList.add(stringObjectMap);
            }
        }

        return R.ok().put("data", resultList);

    }

    //

    private Map<String, Object> aaaDepReturn(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        map.put("return", 0);
        TreeSet<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepGoodsDailyService.queryDepDisGoodsTreeByParams(map);


        if (departmentDisGoodsEntities.size() > 0) {
            double doutbleProduce = 0;
            double doutbleLoss = 0;
            double doutbleWaste = 0;
            double doutbleReturn = 0;

            for (GbDepartmentDisGoodsEntity depGoods : departmentDisGoodsEntities) {
                map.put("return", null);
                map.put("disGoodsId", depGoods.getGbDdgDisGoodsId());
                Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                Double returnSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyReturnSubtotal(map);
                doutbleProduce = doutbleProduce + produceSubtotal;
                doutbleLoss = doutbleLoss + lossSubtotal;
                doutbleWaste = doutbleWaste + wasteSubtotal;
                doutbleReturn = doutbleReturn + returnSubtotal;

            }

            BigDecimal add = new BigDecimal(doutbleProduce).add(new BigDecimal(doutbleLoss)).add(new BigDecimal(doutbleWaste));
            System.out.println("abdbdbf" + add);

            mapResult.put("totalDepCost", add.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalLoss", new BigDecimal(doutbleReturn).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            BigDecimal multiply = new BigDecimal(doutbleReturn).divide(add, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            mapResult.put("percent", multiply.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("lossTotal", departmentDisGoodsEntities.size());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }

    private Map<String, Object> aaaDepWaste(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        map.put("waste", 0);
        TreeSet<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepGoodsDailyService.queryDepDisGoodsTreeByParams(map);


        if (departmentDisGoodsEntities.size() > 0) {
            double doutbleProduce = 0;
            double doutbleLoss = 0;
            double doutbleWaste = 0;

            for (GbDepartmentDisGoodsEntity depGoods : departmentDisGoodsEntities) {
                map.put("waste", null);
                map.put("disGoodsId", depGoods.getGbDdgDisGoodsId());
                Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                doutbleProduce = doutbleProduce + produceSubtotal;
                doutbleLoss = doutbleLoss + lossSubtotal;
                doutbleWaste = doutbleWaste + wasteSubtotal;

            }

            BigDecimal add = new BigDecimal(doutbleProduce).add(new BigDecimal(doutbleLoss)).add(new BigDecimal(doutbleWaste));
            System.out.println("abdbdbf" + add);

            mapResult.put("totalDepCost", add.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalLoss", new BigDecimal(doutbleWaste).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            BigDecimal multiply = new BigDecimal(doutbleWaste).divide(add, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            mapResult.put("percent", multiply.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("lossTotal", departmentDisGoodsEntities.size());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }

    private Map<String, Object> aaaDepLoss(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        map.put("loss", 0);
        TreeSet<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepGoodsDailyService.queryDepDisGoodsTreeByParams(map);


        if (departmentDisGoodsEntities.size() > 0) {
            double doutbleProduce = 0;
            double doutbleLoss = 0;
            double doutbleWaste = 0;

            for (GbDepartmentDisGoodsEntity depGoods : departmentDisGoodsEntities) {
                map.put("loss", null);
                map.put("disGoodsId", depGoods.getGbDdgDisGoodsId());
                Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                doutbleProduce = doutbleProduce + produceSubtotal;
                doutbleLoss = doutbleLoss + lossSubtotal;
                doutbleWaste = doutbleWaste + wasteSubtotal;

            }

            BigDecimal add = new BigDecimal(doutbleProduce).add(new BigDecimal(doutbleLoss)).add(new BigDecimal(doutbleWaste));
            System.out.println("abdbdbf" + add);

            mapResult.put("totalDepCost", add.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalLoss", new BigDecimal(doutbleLoss).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            BigDecimal multiply = new BigDecimal(doutbleLoss).divide(add, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            mapResult.put("percent", multiply.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("lossTotal", departmentDisGoodsEntities.size());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }
//


    private Map<String, Object> aaaNxDisPurchaseCost(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        map.put("dayuStatus", -1);
//        TreeSet<GbDistributerFatherGoodsEntity> greatGrandFatherGoods = new TreeSet<>();
        List<GbDistributerFatherGoodsEntity> greatGrandFatherGoods = new ArrayList<>();
        List<GbDistributerFatherGoodsEntity> resultFatherGoodsList = new ArrayList<>();

        double doutbleCost = 0;
        double doutbleCostV = 0;
        Double aDoubleZeroTotal = 0.0;
        Double aDoubleZero = 0.0;
        Double aDoubleOne = 0.0;
        Double aDoubleZeroTotalWeight = 0.0;
        Integer integer0 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
        if (integer0 > 0) {
            aDoubleZeroTotal = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
            aDoubleZeroTotalWeight = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(map);
        }

        map.put("payType", 0);
        Integer integer1 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
        if (integer1 > 0) {
            aDoubleZero = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
        }

        map.put("payType", 1);
        Integer integer2 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
        if (integer2 > 0) {
            aDoubleOne = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
        }

        if (integer0 > 0) {

            greatGrandFatherGoods = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandFatherGoods) {
                double greatGrandTotalCost = 0;
                double greatGrandTotalCostV = 0;
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    double grandDoubleCost = 0;
                    double grandDoubleCostV = 0;
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        map.put("payType", null);
                        System.out.println("ifnifahthhtnullllll" + map);
                        Integer integerFather = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
                        Double fatherDoubleCost = 0.0;
                        Double fatherDoubleCostV = 0.0;
                        if (integerFather > 0) {
                            System.out.println("-------3--3--3--3---3--");
                            fatherDoubleCost = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(map);
                            fatherDoubleCostV = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
                            System.out.println("ziahsuidufidfiasfadsfas afdf afa");
                        }
                        grandDoubleCostV = grandDoubleCostV + fatherDoubleCostV;
                        greatGrandTotalCostV = greatGrandTotalCostV + fatherDoubleCostV;
                        grandDoubleCost = grandDoubleCost + fatherDoubleCost;
                        greatGrandTotalCost = greatGrandTotalCost + fatherDoubleCost;

                        father.setFatherCostWeightString(new BigDecimal(fatherDoubleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        father.setFatherCostSubtotalString(new BigDecimal(fatherDoubleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        resultFatherGoodsList.add(father);

                    }
                    grandFather.setFatherCostWeightString(new BigDecimal(grandDoubleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    grandFather.setFatherCostSubtotalString(new BigDecimal(grandDoubleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());


                }
                greatGrandFather.setFatherCostWeightString(new BigDecimal(greatGrandTotalCostV).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                greatGrandFather.setFatherCostSubtotalString(new BigDecimal(greatGrandTotalCost).setScale(2, BigDecimal.ROUND_HALF_UP).toString());


                doutbleCost = doutbleCost + greatGrandTotalCost;
                doutbleCostV = doutbleCostV + greatGrandTotalCostV;

            }


            mapResult.put("payZero", new BigDecimal(aDoubleZero).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("payOne", new BigDecimal(aDoubleOne).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            //分店总成本
            mapResult.put("arr", resultFatherGoodsList);
            mapResult.put("totalCost", new BigDecimal(aDoubleZeroTotalWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCostSubtotal", new BigDecimal(aDoubleZeroTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }


    private Map<String, Object> aaaDisPurchaseCost(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        map.put("dayuStatus", -1);
//        TreeSet<GbDistributerFatherGoodsEntity> greatGrandFatherGoods = new TreeSet<>();
        List<GbDistributerFatherGoodsEntity> greatGrandFatherGoods = new ArrayList<>();
        List<GbDistributerFatherGoodsEntity> resultFatherGoodsList = new ArrayList<>();

        double doutbleCost = 0;
        double doutbleCostV = 0;
        Double aDoubleZeroTotal = 0.0;
        Double aDoubleZero = 0.0;
        Double aDoubleOne = 0.0;
        Double aDoubleZeroTotalWeight = 0.0;
        Integer integer0 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
        if (integer0 > 0) {
            aDoubleZeroTotal = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
            aDoubleZeroTotalWeight = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(map);

        }

        map.put("payType", 0);
        Integer integer1 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
        if (integer1 > 0) {
            aDoubleZero = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);

        }

        map.put("payType", 1);
        Integer integer2 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
        if (integer2 > 0) {
            aDoubleOne = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
        }
        System.out.println("mappapappaa" + map);

        if (integer0 > 0) {
            greatGrandFatherGoods = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandFatherGoods) {
                double greatGrandTotalRest = 0;
                double greatGrandTotalRestV = 0;
                double greatGrandTotalCost = 0;
                double greatGrandTotalCostV = 0;
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    double grandDoubleRest = 0;
                    double grandDoubleRestV = 0;
                    double grandDoubleCost = 0;
                    double grandDoubleCostV = 0;
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        map.put("payType", null);
                        System.out.println("fathieieiie" + map);
                        Integer integerFather = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
                        Double fatherDoubleCost = 0.0;
                        Double fatherDoubleCostV = 0.0;
                        if (integerFather > 0) {
                            fatherDoubleCost = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(map);
                            fatherDoubleCostV = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
                        }

                        grandDoubleCostV = grandDoubleCostV + fatherDoubleCostV;
                        greatGrandTotalCostV = greatGrandTotalCostV + fatherDoubleCostV;
                        grandDoubleCost = grandDoubleCost + fatherDoubleCost;
                        greatGrandTotalCost = greatGrandTotalCost + fatherDoubleCost;
                        father.setFatherCostWeightString(new BigDecimal(fatherDoubleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        father.setFatherCostSubtotalString(new BigDecimal(fatherDoubleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        resultFatherGoodsList.add(father);

                    }
                    grandFather.setFatherRestWeightTotalString(new BigDecimal(grandDoubleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    grandFather.setFatherRestTotalString(new BigDecimal(grandDoubleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    grandFather.setFatherCostWeightString(new BigDecimal(grandDoubleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    grandFather.setFatherCostSubtotalString(new BigDecimal(grandDoubleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());


                }
                greatGrandFather.setFatherRestWeightTotalString(new BigDecimal(greatGrandTotalRestV).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                greatGrandFather.setFatherRestTotalString(new BigDecimal(greatGrandTotalRest).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                greatGrandFather.setFatherCostWeightString(new BigDecimal(greatGrandTotalCostV).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                greatGrandFather.setFatherCostSubtotalString(new BigDecimal(greatGrandTotalCost).setScale(2, BigDecimal.ROUND_HALF_UP).toString());


                doutbleCost = doutbleCost + greatGrandTotalCost;
                doutbleCostV = doutbleCostV + greatGrandTotalCostV;
//                doutbleRest = doutbleRest + greatGrandTotalRest;
//                doutbleRestV = doutbleRestV + greatGrandTotalRestV;

            }
            //


            mapResult.put("payZero", new BigDecimal(aDoubleZero).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("payOne", new BigDecimal(aDoubleOne).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            //分店总成本
            mapResult.put("arr", resultFatherGoodsList);
            mapResult.put("totalCost", new BigDecimal(aDoubleZeroTotalWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCostSubtotal", new BigDecimal(aDoubleZeroTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }


    private Map<String, Object> aaaDepCost(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        map.put("dayuStatus", -1);

        System.out.println("depcosoostot" + map);
        double doutbleSubtotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map);
        double doutbleLossV = 0;
        double doutbleWasteV = 0;
        double doutbleProduceV = 0;
        double doutbleReturnV = 0;

        map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
        Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerProduce > 0) {
            doutbleProduceV = gbDepartmentStockReduceService.queryReduceProduceTotal(map);
        } else {
            doutbleProduceV = 0;
        }
        map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
        Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerLoss > 0) {
            doutbleLossV = gbDepartmentStockReduceService.queryReduceLossTotal(map);
        } else {
            doutbleLossV = 0;
        }
        map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerWaste > 0) {
            doutbleWasteV = gbDepartmentStockReduceService.queryReduceWasteTotal(map);
        } else {
            doutbleWasteV = 0;
        }
        map.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
        Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerReturn > 0) {
            doutbleReturnV = gbDepartmentStockReduceService.queryReduceReturnTotal(map);
        } else {
            doutbleReturnV = 0;
        }
        double costTotal = doutbleProduceV + doutbleLossV + doutbleWasteV;
        double doutbleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(map);


        Object startDdate = map.get("startDate");
        Object stopDate = map.get("stopDate");
        Integer howManyDaysInPeriod = getHowManyDaysInPeriod((String) stopDate, (String) startDdate);

        double v = costTotal / (howManyDaysInPeriod + 1);
        mapResult.put("perCost", new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapResult.put("totalSubtotal", new BigDecimal(doutbleSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapResult.put("totalCost", new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapResult.put("totalProduceSubtotal", new BigDecimal(doutbleProduceV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapResult.put("totalReturnSubtotal", new BigDecimal(doutbleReturnV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapResult.put("totalLossSubtotal", new BigDecimal(doutbleLossV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapResult.put("totalWasteSubtotal", new BigDecimal(doutbleWasteV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        mapResult.put("totalRestSubtotal", new BigDecimal(doutbleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());


//        TreeSet<GbDistributerFatherGoodsEntity> greatGrandFatherGoods = new TreeSet<>();
        List<GbDistributerFatherGoodsEntity> greatGrandFatherGoods = new ArrayList<>();
        List<GbDistributerFatherGoodsEntity> resultFatherGoodsList = new ArrayList<>();
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);

        if (integer > 0) {

            greatGrandFatherGoods = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandFatherGoods) {
                double greatGrandTotalCost = 0;
                double greatGrandTotalCostV = 0;
                double greatGrandTotalRest = 0;
                double greatGrandTotalRestV = 0;
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    double grandDoubleCost = 0;
                    double grandDoubleCostV = 0;
                    double grandDoubleRest = 0;
                    double grandDoubleRestV = 0;

                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        Double doutbleProduceWeightDep = 0.0;
                        Double doutbleProduceVDep = 0.0;
                        Double doutbleLossWeightDep = 0.0;
                        Double doutbleLossVDep = 0.0;
                        Double doutbleWasteWeightDep = 0.0;
                        Double doutbleWasteVDep = 0.0;

                        map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                        System.out.println("coprororo" + map);
                        Integer integerProduceDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
                        if (integerProduceDep > 0) {
                            doutbleProduceVDep = gbDepartmentStockReduceService.queryReduceProduceTotal(map);
                            doutbleProduceWeightDep = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(map);
                            System.out.println("prooodododdododo" + doutbleProduceVDep);
                        }

                        map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                        Integer integerLossDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
                        if (integerLossDep > 0) {
                            doutbleLossVDep = gbDepartmentStockReduceService.queryReduceLossTotal(map);
                            doutbleLossWeightDep = gbDepartmentStockReduceService.queryReduceLossWeightTotal(map);
                        }
                        map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                        Integer integerWasteDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
                        if (integerWasteDep > 0) {
                            doutbleWasteVDep = gbDepartmentStockReduceService.queryReduceWasteTotal(map);
                            doutbleWasteWeightDep = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(map);
                        }

                        double fatherDoubleCost = doutbleProduceWeightDep + doutbleLossWeightDep + doutbleWasteWeightDep;
                        double fatherDoubleCostV = doutbleProduceVDep + doutbleLossVDep + doutbleWasteVDep;


                        Double fatherDoubleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
                        Double fatherDoubleRestV = gbDepGoodsStockService.queryDepStockRestSubtotal(map);

                        grandDoubleCostV = grandDoubleCostV + fatherDoubleCostV;
                        grandDoubleRestV = grandDoubleRestV + fatherDoubleRestV;
                        greatGrandTotalCostV = greatGrandTotalCostV + fatherDoubleCostV;
                        greatGrandTotalRestV = greatGrandTotalRestV + fatherDoubleRestV;

                        grandDoubleCost = grandDoubleCost + fatherDoubleCost;
                        grandDoubleRest = grandDoubleRest + fatherDoubleRest;

                        greatGrandTotalCost = greatGrandTotalCost + fatherDoubleCost;
                        greatGrandTotalRest = greatGrandTotalRest + fatherDoubleRest;

                        father.setFatherCostWeightString(new BigDecimal(fatherDoubleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        father.setFatherCostSubtotalString(new BigDecimal(fatherDoubleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        father.setFatherRestWeightTotalString(new BigDecimal(fatherDoubleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        father.setFatherRestTotalString(new BigDecimal(fatherDoubleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

//                        resultFatherGoodsList.add(father);

                    }
                    grandFather.setFatherCostWeightString(new BigDecimal(grandDoubleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    grandFather.setFatherCostSubtotalString(new BigDecimal(grandDoubleCostV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    BigDecimal decimal = new BigDecimal(grandDoubleCostV).divide(new BigDecimal(costTotal), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    grandFather.setFatherCostSubtotalPercentString(decimal.toString());
                    System.out.println("dkafdaksf;asfas" + decimal);
                    grandFather.setFatherRestWeightTotalString(new BigDecimal(grandDoubleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    grandFather.setFatherRestTotalString(new BigDecimal(grandDoubleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        resultFatherGoodsList.add(grandFather);

                }
                greatGrandFather.setFatherCostWeightString(new BigDecimal(greatGrandTotalCostV).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                greatGrandFather.setFatherCostSubtotalString(new BigDecimal(greatGrandTotalCost).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                BigDecimal decimal = new BigDecimal(greatGrandTotalCost).divide(new BigDecimal(costTotal), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                greatGrandFather.setFatherCostSubtotalPercentString(decimal.toString());
                greatGrandFather.setFatherRestWeightTotalString(new BigDecimal(greatGrandTotalRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                greatGrandFather.setFatherRestTotalString(new BigDecimal(greatGrandTotalRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            }


            mapResult.put("arr", resultFatherGoodsList);
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }


    private Map<String, Object> aaaDepStockTotalNow(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        map.put("dayuStatus", -1);
        map.put("restWeight", 0);
//        TreeSet<GbDistributerFatherGoodsEntity> greatGrandFatherGoods = new TreeSet<>();
        List<GbDistributerFatherGoodsEntity> greatGrandFatherGoods = new ArrayList<>();
        List<GbDistributerFatherGoodsEntity> resultFatherGoodsList = new ArrayList<>();

        double doutbleRest = 0;
        double doutbleRestV = 0;
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);

        if (integer > 0) {

            greatGrandFatherGoods = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandFatherGoods) {
                double greatGrandTotalRest = 0;
                double greatGrandTotalRestV = 0;
                List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrandFather.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity grandFather : grandGoodsEntities) {
                    double grandDoubleRest = 0;
                    double grandDoubleRestV = 0;
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grandFather.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        Integer gbDistributerFatherGoodsId = father.getGbDistributerFatherGoodsId();
                        map.put("disGoodsFatherId", gbDistributerFatherGoodsId);
                        Double fatherDoubleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
                        Double fatherDoubleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
                        grandDoubleRestV = grandDoubleRestV + fatherDoubleRestV;
                        greatGrandTotalRestV = greatGrandTotalRestV + fatherDoubleRestV;

                        grandDoubleRest = grandDoubleRest + fatherDoubleRest;
                        greatGrandTotalRest = greatGrandTotalRest + fatherDoubleRest;
                        father.setFatherRestTotalString(new BigDecimal(fatherDoubleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        father.setFatherRestWeightTotalString(new BigDecimal(fatherDoubleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        resultFatherGoodsList.add(father);

                    }
                    grandFather.setFatherRestWeightTotalString(new BigDecimal(grandDoubleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    grandFather.setFatherRestTotalString(new BigDecimal(grandDoubleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                }
                greatGrandFather.setFatherRestWeightTotalString(new BigDecimal(greatGrandTotalRestV).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                greatGrandFather.setFatherRestTotalString(new BigDecimal(greatGrandTotalRest).setScale(2, BigDecimal.ROUND_HALF_UP).toString());


                doutbleRest = doutbleRest + greatGrandTotalRest;
                doutbleRestV = doutbleRestV + greatGrandTotalRestV;

            }
            //分店总成本
            mapResult.put("arr", resultFatherGoodsList);
            mapResult.put("totalRest", new BigDecimal(doutbleRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalRestWeight", new BigDecimal(doutbleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }

    private Map<String, Object> aaaDepBusinessData(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        map.put("dayuStatus", -1);


        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        System.out.println("depBusinessssssss");

        if (integer > 0) {
            double doutbleCost = 0;
            double doutbleSell = 0;
            double doutbleProduce = 0;
            double doutbleWaste = 0;
            double doutbleLoss = 0;
            double doutbleReturn = 0;
            double doutbleProfit = 0;
            doutbleSell = gbDepGoodsDailyService.queryDepGoodsDailySalesSubtotal(map);
            doutbleCost = gbDepGoodsDailyService.queryDepGoodsDailySubtotal(map);
            doutbleProduce = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
            doutbleLoss = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
            doutbleWaste = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
            doutbleReturn = gbDepGoodsDailyService.queryDepGoodsDailyReturnSubtotal(map);
            doutbleProfit = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map);
            Double aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
            BigDecimal add = new BigDecimal(doutbleProduce).add(new BigDecimal(doutbleLoss)).add(new BigDecimal(doutbleWaste));


            mapResult.put("totalSell", new BigDecimal(doutbleSell).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCost", new BigDecimal(doutbleCost).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalDepCost", add.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalProduce", new BigDecimal(doutbleProduce).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalWaste", new BigDecimal(doutbleWaste).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalLoss", new BigDecimal(doutbleLoss).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalReturn", new BigDecimal(doutbleReturn).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalProfit", new BigDecimal(doutbleProfit).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalRest", new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            BigDecimal multiply = new BigDecimal(doutbleProfit).divide(new BigDecimal(doutbleCost), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

            mapResult.put("percent", multiply.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }


    private Map<String, Object> aaaDepProfitTotal(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integer > 0) {

            Double fatherDoubleWeight = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map);
            Double fatherDoubleProduce = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map);
            Double fatherDoubleWaste = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
            Double fatherDoubleLoss = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
            double fatherDoubleRest = 0.0;

            mapResult.put("totalProduce", new BigDecimal(fatherDoubleProduce).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalWaste", new BigDecimal(fatherDoubleWaste).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalLoss", new BigDecimal(fatherDoubleLoss).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalRest", new BigDecimal(fatherDoubleRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCost", new BigDecimal(fatherDoubleWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);

        }


        return mapResult;
    }


    private Map<String, Object> aaaNxDisOutWeightAndSubtotalTotal(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();
        double totalWeight = 0.0;
        double total = 0.0;

        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            totalWeight = gbDepGoodsStockService.queryDepStockWeightTotal(map);
            total = gbDepGoodsStockService.queryDepGoodsSubtotal(map);
            mapResult.put("totalWeight", new BigDecimal(totalWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }
        return mapResult;
    }


    private Map<String, Object> aaaDisOutWeightAndSubtotal(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();
        double totalWeight = 0.0;
        double total = 0.0;
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            totalWeight = gbDepGoodsStockService.queryDepStockWeightTotal(map);
            total = gbDepGoodsStockService.queryDepGoodsSubtotal(map);
            mapResult.put("totalWeight", new BigDecimal(totalWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalSubtotal", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }
        return mapResult;
    }


    private Map<String, Object> aaaDepParameterTotal(Map<String, Object> map, Integer disId) {
        Map<String, Object> mapResult = new HashMap<>();

        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disId", disId);
        mapG.put("fresh", 1);
        double averTotal = 0;
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapG);
        int totalCount = 0;
        if (distributerGoodsEntities.size() > 0) {
            double totalF = 0;
            for (GbDistributerGoodsEntity goods : distributerGoodsEntities) {
                Integer gbDistributerGoodsId = goods.getGbDistributerGoodsId();
                map.put("disGoodsId", gbDistributerGoodsId);
                map.put("produce", 0);
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                if (integer > 0) {
                    double v = gbDepGoodsDailyService.queryDepGoodsFreshRate(map);
                    double v1 = v / integer;
                    totalF = totalF + v1;
                    totalCount = totalCount + 1;
                }
            }
            averTotal = totalF / totalCount;
        }

        //time
        String time = "-:-";
        map.put("clear", 1);
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integer > 0) {
            int clearHour = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(map);
            int clearMinute = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(map);

            int hourTF = clearHour * 60;
            int totalMinuteF = (hourTF + clearMinute) / integer;
            int hourF = totalMinuteF / 60;
            int minTF = totalMinuteF % 60;

            String minString = "";
            if (minTF < 10) {
                minString = "0" + minTF;
            } else {
                minString = Integer.toString(minTF);
            }
            time = hourF + ":" + minString;
        }

        if (distributerGoodsEntities.size() > 0 && integer > 0) {
            mapResult.put("code", -1);
        } else {
            mapResult.put("totalGoods", totalCount);
            mapResult.put("totalRate", new BigDecimal(averTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalFinish", integer);
            mapResult.put("finishTime", time);
            mapResult.put("code", 0);
        }

        return mapResult;
    }


    private Map<String, Object> aaaDepSalesSubtotalTotal(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        map.put("dayuStatus", -1);
        System.out.println("subtlldldldllddld");
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integer > 0) {
            Double produceSubtotal = gbDepGoodsDailyService.queryDepGoodsDailySalesSubtotal(map);
            Double lossSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
            Double wasteSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
            Double returnSubtotal = gbDepGoodsDailyService.queryDepGoodsDailyReturnSubtotal(map);
            Double afterProfit = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map);
            Double profit = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map);


            mapResult.put("profit", new BigDecimal(profit).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("afterProfit", new BigDecimal(afterProfit).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalProduce", new BigDecimal(produceSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalLoss", new BigDecimal(lossSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalWaste", new BigDecimal(wasteSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalReturn", new BigDecimal(returnSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }
        return mapResult;
    }

    private Map<String, Object> aaaDepSalesWeightTotal(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        Double doutbleProduceWeightDep = 0.0;
        Double doutbleLossWeightDep = 0.0;
        Double doutbleWasteWeightDep = 0.0;
        Double doutbleReturnWeightDep = 0.0;
        double totalWeight = 0.0;

        map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
        Integer integerProduceDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerProduceDep > 0) {
            doutbleProduceWeightDep = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(map);
        }

        map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
        Integer integerLossDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerLossDep > 0) {
            doutbleLossWeightDep = gbDepartmentStockReduceService.queryReduceLossWeightTotal(map);
        }
        map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        Integer integerWasteDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerWasteDep > 0) {
            doutbleWasteWeightDep = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(map);
        }

        map.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
        Integer integerReturnDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerReturnDep > 0) {
            doutbleReturnWeightDep = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(map);
        }

        totalWeight = doutbleProduceWeightDep + doutbleLossWeightDep + doutbleWasteWeightDep;

        if (totalWeight > 0) {
            mapResult.put("totalProduce", new BigDecimal(doutbleProduceWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalWaste", new BigDecimal(doutbleWasteWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalLoss", new BigDecimal(doutbleLossWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCost", new BigDecimal(totalWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalReturn", new BigDecimal(doutbleReturnWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("code", 0);

        } else {
            mapResult.put("code", -1);
        }
        return mapResult;
    }


    private Map<String, Object> aaaGoodsProfitTotal(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();
        map.put("dayuStatus", -1);
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integer > 0) {
            Double totalProfit = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map);
            Double fatherDoubleProduce = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map);
            Double fatherDoubleWaste = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
            Double fatherDoubleLoss = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);

            Double costTotalP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
            Double dcostTotalL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
            Double dcostTotalW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
            double costTotal = costTotalP + dcostTotalL + dcostTotalW;
            BigDecimal decimal = new BigDecimal(totalProfit).divide(new BigDecimal(costTotal), 4, BigDecimal.ROUND_HALF_UP);

            mapResult.put("costTotal", new BigDecimal(costTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("costTotalPercent", decimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalProduce", new BigDecimal(fatherDoubleProduce).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalWaste", new BigDecimal(fatherDoubleWaste).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalLoss", new BigDecimal(fatherDoubleLoss).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCost", new BigDecimal(totalProfit).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }


    private Map<String, Object> aaaGoodsCostTotal(Map<String, Object> map) {
        System.out.println("cosootototootottoaaaGoodsCostTotal" + map);
        Map<String, Object> mapResult = new HashMap<>();
        Double doutbleProduceWeightDep = 0.0;
        Double doutbleLossWeightDep = 0.0;
        Double doutbleWasteWeightDep = 0.0;
        Double doutbleReturnWeightDep = 0.0;
        double totalWeight = 0.0;
        double restWeight = 0.0;


        Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer1 > 0) {
            restWeight = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
        }

        map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
        Integer integerLossDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerLossDep > 0) {
            doutbleLossWeightDep = gbDepartmentStockReduceService.queryReduceLossTotal(map);
        }
        map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        Integer integerWasteDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerWasteDep > 0) {
            doutbleWasteWeightDep = gbDepartmentStockReduceService.queryReduceWasteTotal(map);
        }

        map.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
        Integer integerReturnDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerReturnDep > 0) {
            doutbleReturnWeightDep = gbDepartmentStockReduceService.queryReduceReturnTotal(map);
        }

        Integer integer = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
        if (integer > 0) {
            totalWeight = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
        }

        // 只查询门店销售
        map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
        map.put("depType", getGbDepartmentTypeMendian());
        Integer integerProduceDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerProduceDep > 0) {
            doutbleProduceWeightDep = gbDepartmentStockReduceService.queryReduceProduceTotal(map);
        }

        if (totalWeight > 0) {
            mapResult.put("totalProduceSubtotal", new BigDecimal(doutbleProduceWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalWasteSubtotal", new BigDecimal(doutbleWasteWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalLossSubtotal", new BigDecimal(doutbleLossWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCostSubtotal", new BigDecimal(totalWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalReturnSubtotal", new BigDecimal(doutbleReturnWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalRestSubtotal", new BigDecimal(restWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("code", 0);

        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }


    private Map<String, Object> aaaGoodsCostTotalByDepartment(Map<String, Object> map) {
        System.out.println("cosootototootottoaaaGoodsCostTotal" + map);
        Map<String, Object> mapResult = new HashMap<>();
        Double doutbleProduceWeightDep = 0.0;
        Double doutbleLossWeightDep = 0.0;
        Double doutbleWasteWeightDep = 0.0;
        Double doutbleReturnWeightDep = 0.0;
        double totalWeight = 0.0;
        double restWeight = 0.0;


        Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer1 > 0) {
            restWeight = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
        }

        map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
        Integer integerLossDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerLossDep > 0) {
            doutbleLossWeightDep = gbDepartmentStockReduceService.queryReduceLossTotal(map);
        }
        map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        Integer integerWasteDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerWasteDep > 0) {
            doutbleWasteWeightDep = gbDepartmentStockReduceService.queryReduceWasteTotal(map);
        }

        map.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
        Integer integerReturnDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerReturnDep > 0) {
            doutbleReturnWeightDep = gbDepartmentStockReduceService.queryReduceReturnTotal(map);
        }

        Integer integer = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
        if (integer > 0) {
            totalWeight = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
        }

        // 只查询门店销售
        map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
        map.put("depType", getGbDepartmentTypeMendian());
        Integer integerProduceDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerProduceDep > 0) {
            doutbleProduceWeightDep = gbDepartmentStockReduceService.queryReduceProduceTotal(map);
        }

        if (totalWeight > 0) {
            mapResult.put("totalProduceSubtotal", new BigDecimal(doutbleProduceWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalWasteSubtotal", new BigDecimal(doutbleWasteWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalLossSubtotal", new BigDecimal(doutbleLossWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCostSubtotal", new BigDecimal(totalWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalReturnSubtotal", new BigDecimal(doutbleReturnWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalRestSubtotal", new BigDecimal(restWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("code", 0);

        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }

    private Map<String, Object> aaaNxDisOutGoodsFreshTotal(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();
        map.put("dayuStatus", -1);
        map.put("controlFresh", 1);
        map.put("produce", 0);
        System.out.println("hdhafdafdafasaaaaaa???maaaoaoao" + map);
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integer > 0) {

            TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
            System.out.println("dfadfa" + distributerGoodsEntities.size());
            Double freshRate = gbDepGoodsDailyService.queryDepGoodsFreshRate(map);
            BigDecimal divide = new BigDecimal(freshRate).divide(new BigDecimal(integer), 2, BigDecimal.ROUND_HALF_UP);

            Double restWeight = gbDepGoodsStockService.queryDepGoodsRestWeightTotal(map);
            BigDecimal rest = new BigDecimal(restWeight).divide(new BigDecimal(integer), 1, BigDecimal.ROUND_HALF_UP);
            String time = "";
            int clearHour = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(map);
            int clearMinute = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(map);

            int hourTF = clearHour * 60;
            int totalMinuteF = (hourTF + clearMinute) / integer;
            int hourF = totalMinuteF / 60;
            int minTF = totalMinuteF % 60;

            String minString = "";
            if (minTF < 10) {
                minString = "0" + minTF;
            } else {
                minString = Integer.toString(minTF);
            }
            time = hourF + ":" + minString;


            mapResult.put("freshRate", divide.toString() + "%");
            mapResult.put("restWeight", rest.toString());
            mapResult.put("clearTime", time);
            mapResult.put("goods", distributerGoodsEntities.size());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }
        return mapResult;
    }


    private Map<String, Object> aaaDisOutGoodsFreshTotal(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();
        map.put("dayuStatus", -1);
        map.put("controlFresh", 1);
        map.put("produce", 0);
        System.out.println("hdhafdafdafas");
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integer > 0) {

            TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
            System.out.println("dfadfa" + distributerGoodsEntities.size());
            Double freshRate = gbDepGoodsDailyService.queryDepGoodsFreshRate(map);
            System.out.println("shisbshis280" + freshRate);
            BigDecimal divide = new BigDecimal(freshRate).divide(new BigDecimal(integer), 2, BigDecimal.ROUND_HALF_UP);

            Double restWeight = gbDepGoodsStockService.queryDepGoodsRestWeightTotal(map);
            BigDecimal rest = new BigDecimal(restWeight).divide(new BigDecimal(integer), 1, BigDecimal.ROUND_HALF_UP);
            String time = "";
            int clearHour = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(map);
            int clearMinute = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(map);

            int hourTF = clearHour * 60;
            int totalMinuteF = (hourTF + clearMinute) / integer;
            int hourF = totalMinuteF / 60;
            int minTF = totalMinuteF % 60;

            String minString = "";
            if (minTF < 10) {
                minString = "0" + minTF;
            } else {
                minString = Integer.toString(minTF);
            }
            time = hourF + ":" + minString;


            mapResult.put("freshRate", divide.toString() + "%");
            mapResult.put("restWeight", rest.toString());
            mapResult.put("clearTime", time);
            mapResult.put("goods", distributerGoodsEntities.size());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }
        return mapResult;
    }


    private Map<String, Object> aaaGoodsFreshTotal(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();
        map.put("dayuStatus", -1);
        map.put("controlFresh", 1);
        map.put("produce", 0);
        System.out.println("hdhafdafdafas");
        Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        if (integer > 0) {

            TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
            System.out.println("dfadfa" + distributerGoodsEntities.size());
            Double freshRate = gbDepGoodsDailyService.queryDepGoodsFreshRate(map);
            System.out.println("shisbshis280" + freshRate);
            BigDecimal divide = new BigDecimal(freshRate).divide(new BigDecimal(integer), 2, BigDecimal.ROUND_HALF_UP);

            Double restWeight = gbDepGoodsStockService.queryDepGoodsRestWeightTotal(map);

            BigDecimal rest = new BigDecimal(restWeight).divide(new BigDecimal(integer), 1, BigDecimal.ROUND_HALF_UP);
            String time = "";
            int clearHour = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(map);
            int clearMinute = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(map);

            int hourTF = clearHour * 60;
            int totalMinuteF = (hourTF + clearMinute) / integer;
            int hourF = totalMinuteF / 60;
            int minTF = totalMinuteF % 60;

            String minString = "";
            if (minTF < 10) {
                minString = "0" + minTF;
            } else {
                minString = Integer.toString(minTF);
            }
            time = hourF + ":" + minString;


            mapResult.put("freshRate", divide.toString() + "%");
            mapResult.put("restWeight", rest.toString());
            mapResult.put("clearTime", time);
            mapResult.put("goods", distributerGoodsEntities.size());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }
        return mapResult;
    }


    private Map<String, Object> aaaNxDisControlGoodsPriceTotal(Map<String, Object> map) {
        System.out.println("cosootototootottoaaaNxDisControlGoodsPriceTotal" + map);
        Map<String, Object> mapResult = new HashMap<>();
        map.put("dayuStatus", -1);
        Integer integer = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
        if (integer > 0) {
            map.put("purWhat", 1);
            System.out.println("111111" + map);
            int integerH = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
            if (integerH > 0) {
                Double highestTotal = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                Double whatScale = gbDistributerGoodsPriceService.queryPriceWhatScale(map);
                BigDecimal divide = new BigDecimal(whatScale).divide(new BigDecimal(integerH), 4, BigDecimal.ROUND_HALF_UP);
                BigDecimal multiply = divide.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                mapResult.put("highestTotal", new BigDecimal(highestTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                mapResult.put("highest", integerH);
                mapResult.put("highPercent", multiply + "%");

            } else {
                mapResult.put("highestTotal", "0");
                mapResult.put("highest", 0);
                mapResult.put("highPercent", 0 + "%");
            }

            map.put("purWhat", -1);
            System.out.println("------------111111" + map);
            int integerL = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
            if (integerL > 0) {
                Double lowerTotal = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                Double whatScaleL = gbDistributerGoodsPriceService.queryPriceWhatScale(map);
                BigDecimal divideL = new BigDecimal(whatScaleL).divide(new BigDecimal(integerL), 4, BigDecimal.ROUND_HALF_UP);
                BigDecimal multiplyL = divideL.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                mapResult.put("lowerTotal", new BigDecimal(lowerTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                mapResult.put("lower", integerL);
                mapResult.put("lowPercent", multiplyL + "%");
            } else {
                mapResult.put("lowerTotal", "0");
                mapResult.put("lower", 0);
                mapResult.put("lowPercent", 0 + "%");
            }

            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }

    private Map<String, Object> aaaDisControlGoodsPriceTotal(Map<String, Object> map) {
        System.out.println("cosootototootottoaaaDisControlGoodsPriceTotal" + map);
        Map<String, Object> mapResult = new HashMap<>();
        map.put("dayuStatus", -1);
        Integer integer = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
        if (integer > 0) {
            map.put("purWhat", 1);
            System.out.println("111111" + map);
            int integerH = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
            if (integerH > 0) {
                Double highestTotal = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                Double whatScale = gbDistributerGoodsPriceService.queryPriceWhatScale(map);
                BigDecimal divide = new BigDecimal(whatScale).divide(new BigDecimal(integerH), 4, BigDecimal.ROUND_HALF_UP);
                BigDecimal multiply = divide.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                mapResult.put("highestTotal", new BigDecimal(highestTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                mapResult.put("highest", integerH);
                mapResult.put("highPercent", multiply + "%");

            } else {
                mapResult.put("highestTotal", "0");
                mapResult.put("highest", 0);
                mapResult.put("highPercent", 0 + "%");
            }

            map.put("purWhat", -1);
            System.out.println("------------111111" + map);
            int integerL = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
            if (integerL > 0) {
                Double lowerTotal = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                Double whatScaleL = gbDistributerGoodsPriceService.queryPriceWhatScale(map);
                BigDecimal divideL = new BigDecimal(whatScaleL).divide(new BigDecimal(integerL), 4, BigDecimal.ROUND_HALF_UP);
                BigDecimal multiplyL = divideL.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                mapResult.put("lowerTotal", new BigDecimal(lowerTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                mapResult.put("lower", integerL);
                mapResult.put("lowPercent", multiplyL + "%");
            } else {
                mapResult.put("lowerTotal", "0");
                mapResult.put("lower", 0);
                mapResult.put("lowPercent", 0 + "%");
            }

            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }

    private Map<String, Object> aaaGoodsPriceTotal(Map<String, Object> map) {
        System.out.println("cosootototoototto" + map);
        Map<String, Object> mapResult = new HashMap<>();
        map.put("dayuStatus", -1);
        Integer integer = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
        if (integer > 0) {
            map.put("purWhat", 1);
            System.out.println("111111" + map);
            int integerH = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
            if (integerH > 0) {
                Double highestTotal = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                Double whatScale = gbDistributerGoodsPriceService.queryPriceWhatScale(map);
                BigDecimal divide = new BigDecimal(whatScale).divide(new BigDecimal(integerH), 4, BigDecimal.ROUND_HALF_UP);
                BigDecimal multiply = divide.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                mapResult.put("highestTotal", new BigDecimal(highestTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                mapResult.put("highest", integerH);
                mapResult.put("highPercent", multiply + "%");

            } else {
                mapResult.put("highestTotal", "0");
                mapResult.put("highest", 0);
                mapResult.put("highPercent", 0 + "%");
            }

            map.put("purWhat", -1);
            System.out.println("------------111111" + map);
            int integerL = gbDistributerGoodsPriceService.queryPriceWhatAmount(map);
            if (integerL > 0) {
                Double lowerTotal = gbDistributerGoodsPriceService.queryPriceWhatTotal(map);
                Double whatScaleL = gbDistributerGoodsPriceService.queryPriceWhatScale(map);
                BigDecimal divideL = new BigDecimal(whatScaleL).divide(new BigDecimal(integerL), 4, BigDecimal.ROUND_HALF_UP);
                BigDecimal multiplyL = divideL.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                mapResult.put("lowerTotal", new BigDecimal(lowerTotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                mapResult.put("lower", integerL);
                mapResult.put("lowPercent", multiplyL + "%");
            } else {
                mapResult.put("lowerTotal", "0");
                mapResult.put("lower", 0);
                mapResult.put("lowPercent", 0 + "%");
            }

            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }


    private Map<String, Object> aaaGoodsSalesTotal(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();
        Double doutbleProduceWeightDep = 0.0;
        Double doutbleLossWeightDep = 0.0;
        Double doutbleWasteWeightDep = 0.0;
        Double doutbleReturnWeightDep = 0.0;
        double totalWeight = 0.0;
        double restWeight = 0.0;


        map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
        Integer integerLossDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerLossDep > 0) {
            doutbleLossWeightDep = gbDepartmentStockReduceService.queryReduceLossWeightTotal(map);
        }
        map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
        Integer integerWasteDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerWasteDep > 0) {
            doutbleWasteWeightDep = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(map);
        }

        map.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
        Integer integerReturnDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerReturnDep > 0) {
            doutbleReturnWeightDep = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(map);
        }

        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            restWeight = gbDepGoodsStockService.queryDepGoodsRestWeightTotal(map);
        }


        System.out.println("prodceiieieiieiieie" + map);
        int wxCountAuto = gbDistributerPurchaseGoodsService.queryPurchaseGoodsAmount(map);
        if (wxCountAuto > 0) {
            totalWeight = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(map);
        }


        map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
        map.put("depType", getGbDepartmentTypeMendian()); //销售只查询门店
        Integer integerProduceDep = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (integerProduceDep > 0) {
            doutbleProduceWeightDep = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(map);
        }


        if (totalWeight > 0) {
            mapResult.put("totalProduce", new BigDecimal(doutbleProduceWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalWaste", new BigDecimal(doutbleWasteWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalLoss", new BigDecimal(doutbleLossWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalReturn", new BigDecimal(doutbleReturnWeightDep).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalCost", new BigDecimal(totalWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
            mapResult.put("totalRest", new BigDecimal(restWeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            mapResult.put("code", 0);

        } else {
            mapResult.put("code", -1);
        }


        return mapResult;
    }

    @RequestMapping("/downloadReportExcelGb")
    @ResponseBody
    public void downloadReportExcelGb(HttpServletResponse response, HttpServletRequest request) {
        String id = request.getParameter("id");
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            System.out.println(id);

            GbReportEntity reportEntity = gbReportService.queryObject(Integer.valueOf(id));


            if (reportEntity.getGbRepType().equals("nxDisControlPrice")) {
                wb = toCreatNxDisControlGoodsPriceForm(reportEntity);
            }

            if (reportEntity.getGbRepType().equals("disControlPrice")) {
                wb = toCreatDisControlGoodsPriceForm(reportEntity);
            }

            if (reportEntity.getGbRepType().equals("nxDisPurchaseCost")) {
                wb = toCreatNxDisPurchaseCostForm(reportEntity);
            }

            if (reportEntity.getGbRepType().equals("disPurchaseCost")) {
                wb = toCreatDisPurchaseCostForm(reportEntity);
            }

            if (reportEntity.getGbRepType().equals("disPurchaseCostPur")) {
                wb = toCreatDisPurchaseCostFormPur(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("nxDisOutGoodsFresh")) {
                wb = toCreatNxDisOutGoodsFreshForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("disOutGoodsFresh")) {
                wb = toCreatDisOutGoodsFreshForm(reportEntity);
            }

            if (reportEntity.getGbRepType().equals("nxDisOutWeightAndSubtotal")) {
                wb = toCreatNxDisOutWeightAndSubtotalForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("disOutWeightAndSubtotal")) {
                wb = toCreatDisOutWeightForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("disOutWeightAndSubtotalPur")) {
                wb = toCreatDisOutWeightFormPur(reportEntity);
            }

            if (reportEntity.getGbRepType().equals("depSalesWeight")) {
                wb = toCreatDepSalesWeightForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("depSalesSubtotal")) {
                wb = toCreatDepSalesSubtotalForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("depParameter")) {
                wb = toCreatDepParameter(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("depProfit")) {
                wb = toCreatDepProfitForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("depBusiness")) {
                wb = toCreatDepBusinessForm(reportEntity);
            }

            if (reportEntity.getGbRepType().equals("depStockNow")) {
                wb = toCreatDepStockNowForm(reportEntity);
            }

            if (reportEntity.getGbRepType().equals("depCost")) {
                wb = toCreatDepCostForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("depLoss")) {
                wb = toCreatDepLossForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("depWaste")) {
                wb = toCreatDepWasteForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("depReturn")) {
                wb = toCreatDepReturnForm(reportEntity);
            }

            if (reportEntity.getGbRepType().equals("goodsSales")) {
                wb = toCreatGoodsSalesForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("goodsCost")) {
                wb = toCreatGoodsCostForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("goodsProfit")) {
                wb = toCreatGoodsProfitForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("goodsFresh")) {
                wb = toCreatGoodsFreshForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("goodsPrice")) {
                wb = toCreatGoodsPriceForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("goodsByDepartment")) {
                wb = toCreatGoodsCostByDepartmentForm(reportEntity);
            }


            String fileName = new String("导出商品.xls".getBytes("utf-8"), "iso8859-1");
            response.setHeader("content-Disposition", "attachment; filename =" + fileName);
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private HSSFWorkbook toCreatGoodsCostForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("disGoodsFatherId", reportEntity.getGbRepIds());
        System.out.println("mapamapapapExcelleleel" + map);
        TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);

        GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(Integer.valueOf(reportEntity.getGbRepIds()));


        if (distributerGoodsEntities.size() > 0) {
            HSSFSheet sheet = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName());
            //设置表头
            HSSFRow row1 = sheet.createRow(0);
            row1.createCell(0).setCellValue("序号");
            row1.createCell(1).setCellValue("商品名称");
            row1.createCell(2).setCellValue("规格");
            row1.createCell(3).setCellValue("品牌");
            row1.createCell(4).setCellValue("详细");
            row1.createCell(5).setCellValue("销售成本");
            row1.createCell(6).setCellValue("损耗成本");
            row1.createCell(7).setCellValue("废弃成本");
            row1.createCell(8).setCellValue("退货成本");
            row1.createCell(9).setCellValue("库存总额");
            row1.createCell(10).setCellValue("采购总数量");
            row1.createCell(11).setCellValue("采购总成本");
            row1.createCell(12).setCellValue("采购均价");
            //设置表体
            HSSFRow goodsRow = null;
            for (GbDistributerGoodsEntity ckGoodsEntity : distributerGoodsEntities) {
                goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                //5 totalWeight
                Map<String, Object> disGoodsMap = new HashMap<>();
                disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());

                double aDoubleP = 0.0;
                double aDoubleL = 0.0;
                double aDoubleW = 0.0;
                double aDoubleR = 0.0;

                Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                if (integer > 0) {
                    aDoubleL = gbDepartmentStockReduceService.queryReduceLossTotal(disGoodsMap);
                    aDoubleW = gbDepartmentStockReduceService.queryReduceWasteTotal(disGoodsMap);
                    aDoubleR = gbDepartmentStockReduceService.queryReduceReturnTotal(disGoodsMap);
                }

                //6 pur
                double weight = 0.0;
                Double subtotal = 0.0;
                int wxCountAuto = gbDistributerPurchaseGoodsService.queryPurchaseGoodsAmount(disGoodsMap);
                if (wxCountAuto > 0) {
                    subtotal = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(disGoodsMap);
                    weight = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(disGoodsMap);
                }
                BigDecimal divide = new BigDecimal(subtotal).divide(new BigDecimal(weight), 2, BigDecimal.ROUND_HALF_UP);

                double aDoubleRS = 0.0;
                Integer integerS = gbDepGoodsStockService.queryGoodsStockCount(disGoodsMap);
                if (integerS > 0) {
                    aDoubleRS = gbDepGoodsStockService.queryDepGoodsRestTotal(disGoodsMap);
                }

                //只出现门店销售
                disGoodsMap.put("depType", getGbDepartmentTypeMendian());
                Integer integerP = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                if (integerP > 0) {
                    aDoubleP = gbDepartmentStockReduceService.queryReduceProduceTotal(disGoodsMap);
                }

                goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleRS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsRow.createCell(10).setCellValue(new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsRow.createCell(11).setCellValue(new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsRow.createCell(12).setCellValue(divide.toString());
            }

        }

        return wb;
    }


    private HSSFWorkbook toCreatNxDisOutGoodsFreshForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("nxDisId", reportEntity.getGbRepIds());
        map.put("controlFresh", 1);
        System.out.println("fathehreh" + map);
        List<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map);
        for (GbDistributerFatherGoodsEntity greatGrand : greatGrandGoodsEntities) {

            List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrand.getFatherGoodsEntities();

            for (GbDistributerFatherGoodsEntity grand : grandGoodsEntities) {
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {

                    System.out.println("fahhhg-namemememmememefatherGoodsEntity-=======" + fatherGoodsEntity.getGbDfgFatherGoodsName());
//                    GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(fatherGoods.getGbDistributerFatherGoodsId());

                    if (fatherGoodsEntities.size() > 0) {
                        HSSFSheet sheet = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("天数");
                        row1.createCell(6).setCellValue("平均日鲜率(%)");
                        row1.createCell(7).setCellValue("最高日鲜率");
                        row1.createCell(8).setCellValue("最低日鲜率");
                        row1.createCell(9).setCellValue("平均每日剩余数量");
                        row1.createCell(10).setCellValue("平均沽清时间");

                        map.put("disGoodsFatherId", fatherGoodsEntity.getGbDistributerFatherGoodsId());
                        TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (GbDistributerGoodsEntity ckGoodsEntity : distributerGoodsEntities) {
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMap);
                            if (integer > 0) {
                                double freshRate = gbDepGoodsDailyService.queryDepGoodsFreshRate(disGoodsMap);
                                int clearHour = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(disGoodsMap);
                                int clearMinute = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(disGoodsMap);
                                String time = "";
                                int hourTF = clearHour * 60;
                                int totalMinuteF = (hourTF + clearMinute) / integer;
                                int hourF = totalMinuteF / 60;
                                int minTF = totalMinuteF % 60;

                                String minString = "";
                                if (minTF < 10) {
                                    minString = "0" + minTF;
                                } else {
                                    minString = Integer.toString(minTF);
                                }
                                time = hourF + ":" + minString;

                                goodsRow.createCell(5).setCellValue(integer);
                                BigDecimal divide = new BigDecimal(freshRate).divide(new BigDecimal(integer), 2, BigDecimal.ROUND_HALF_UP);
                                System.out.println("abc" + freshRate);
                                System.out.println("deff" + integer);
                                System.out.println("reeeee" + divide);
                                double highestFreshRate = gbDepGoodsDailyService.queryDepGoodsDailyHighestFreshRate(disGoodsMap);
                                double lowestFreshRate = gbDepGoodsDailyService.queryDepGoodsDailyLowestFreshRate(disGoodsMap);

                                goodsRow.createCell(6).setCellValue(divide.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                                goodsRow.createCell(7).setCellValue(new BigDecimal(highestFreshRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                                goodsRow.createCell(8).setCellValue(new BigDecimal(lowestFreshRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                Double restWeight = gbDepGoodsStockService.queryDepGoodsRestWeightTotal(disGoodsMap);

                                System.out.println("rererereree" + restWeight);
                                BigDecimal divide1 = new BigDecimal(restWeight).divide(new BigDecimal(integer), 2, BigDecimal.ROUND_HALF_UP);
                                goodsRow.createCell(9).setCellValue(divide1.toString());
                                goodsRow.createCell(10).setCellValue(time);

                            }

                        }


                        HSSFSheet sheet2 = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName() + "明细");
                        //设置表头
                        HSSFRow row2 = sheet2.createRow(0);
                        row2.createCell(0).setCellValue("序号");
                        row2.createCell(1).setCellValue("商品名称");
                        row2.createCell(2).setCellValue("规格");
                        row2.createCell(3).setCellValue("品牌");
                        row2.createCell(4).setCellValue("详细");
                        row2.createCell(5).setCellValue("日期");
                        row2.createCell(6).setCellValue("店铺");
                        row2.createCell(7).setCellValue("日鲜率");
                        row2.createCell(8).setCellValue("剩余数量");
                        row2.createCell(9).setCellValue("沽清时间");
                        HSSFRow goodsRow2 = null;
                        int m = 1;

                        for (GbDistributerGoodsEntity distributerGoodsEntity : distributerGoodsEntities) {

                            goodsRow2 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                            goodsRow2.createCell(0).setCellValue(m);
                            m = m + 1;
                            goodsRow2.createCell(1).setCellValue(distributerGoodsEntity.getGbDgGoodsName());
                            goodsRow2.createCell(2).setCellValue(distributerGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow2.createCell(3).setCellValue(distributerGoodsEntity.getGbDgGoodsBrand());
                            goodsRow2.createCell(4).setCellValue(distributerGoodsEntity.getGbDgGoodsDetail());
                            //5 totalWeight

                            Integer howManyDaysInPeriod = 0;
                            if (!reportEntity.getGbRepStartDate().equals(reportEntity.getGbRepStopDate())) {
                                howManyDaysInPeriod = getHowManyDaysInPeriod(reportEntity.getGbRepStopDate(), reportEntity.getGbRepStartDate());
                            }
                            System.out.println("hoammdmmd" + howManyDaysInPeriod);
                            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                                String whichDay = "";
                                if (i == 0) {
                                    whichDay = reportEntity.getGbRepStartDate();
                                } else {
                                    whichDay = afterWhatDay(reportEntity.getGbRepStartDate(), i);
                                }

                                Map<String, Object> disGoodsMap = new HashMap<>();
                                disGoodsMap.put("date", whichDay);
                                disGoodsMap.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());
                                double highInteger = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMap);
                                if (highInteger > 0) {
                                    List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListByParams(disGoodsMap);

                                    HSSFRow goodsRow3 = null;
                                    for (GbDepartmentGoodsDailyEntity dailyEntity : departmentGoodsDailyEntities) {
                                        goodsRow3 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                                        goodsRow3.createCell(5).setCellValue(whichDay);
                                        Integer gbDgdGbDepartmentId = dailyEntity.getGbDgdGbDepartmentId();
                                        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(gbDgdGbDepartmentId);

                                        goodsRow3.createCell(6).setCellValue(gbDepartmentEntity.getGbDepartmentName());
                                        goodsRow3.createCell(7).setCellValue(dailyEntity.getGbDgdFreshRate() + "%");
                                        goodsRow3.createCell(8).setCellValue(dailyEntity.getGbDgdRestWeight() + distributerGoodsEntity.getGbDgGoodsStandardname());
                                        goodsRow3.createCell(9).setCellValue(dailyEntity.getGbDgdSellClearHour() + ":" + dailyEntity.getGbDgdSellClearMinute());
                                    }

                                }

                            }
                        }


                    }
                }

            }


        }


        return wb;
    }


    //

    private HSSFWorkbook toCreatGoodsCostByDepartmentForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("disGoodsFatherId", reportEntity.getGbRepIds());
        List<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map);
        for (GbDistributerFatherGoodsEntity greatGrand : greatGrandGoodsEntities) {

            List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrand.getFatherGoodsEntities();

            for (GbDistributerFatherGoodsEntity grand : grandGoodsEntities) {
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                    if (fatherGoodsEntities.size() > 0) {
                        HSSFSheet sheet = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("总采购金额");
                        row1.createCell(6).setCellValue("总制作成本");
                        row1.createCell(7).setCellValue("总损耗成本");
                        row1.createCell(8).setCellValue("总废弃成本");
                        row1.createCell(9).setCellValue("总退货金额");
                        row1.createCell(10).setCellValue("总库存金额");

                        map.put("disGoodsFatherId", fatherGoodsEntity.getGbDistributerFatherGoodsId());
                        TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (GbDistributerGoodsEntity ckGoodsEntity : distributerGoodsEntities) {
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());

                            map.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            Double doutbleProduceWeightM = 0.0;
                            Double doutbleLossWeightM = 0.0;
                            Double doutbleWasteWeightM = 0.0;
                            Double doutbleReturnWeightM = 0.0;
                            double totalWeightM = 0.0;
                            double restWeightM = 0.0;


                            Integer integerM = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                            if (integerM > 0) {

                                Integer integer2 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
                                if (integer2 > 0) {
                                    totalWeightM = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);
                                }
                                goodsRow.createCell(5).setCellValue(new BigDecimal(totalWeightM).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                                Integer integerLossM = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                                if (integerLossM > 0) {
                                    doutbleLossWeightM = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
                                }
                                goodsRow.createCell(7).setCellValue(new BigDecimal(doutbleLossWeightM).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                                Integer integerWasteM = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                                if (integerWasteM > 0) {
                                    doutbleWasteWeightM = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
                                }
                                goodsRow.createCell(8).setCellValue(new BigDecimal(doutbleWasteWeightM).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                map.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                                Integer integerReturnM = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                                if (integerReturnM > 0) {
                                    doutbleReturnWeightM = gbDepGoodsDailyService.queryDepGoodsDailyReturnSubtotal(map);
                                }
                                goodsRow.createCell(9).setCellValue(new BigDecimal(doutbleReturnWeightM).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(map);
                                if (integer1 > 0) {
                                    restWeightM = gbDepGoodsStockService.queryDepStockRestSubtotal(map);
                                }
                                goodsRow.createCell(10).setCellValue(new BigDecimal(restWeightM).setScale(2, BigDecimal.ROUND_HALF_UP).toString());


                                map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                                map.put("depType", getGbDepartmentTypeMendian());
                                Integer integerProduceM = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
                                if (integerProduceM > 0) {
                                    doutbleProduceWeightM = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
                                }
                                goodsRow.createCell(6).setCellValue(doutbleProduceWeightM);

                            }

                        }


                        HSSFSheet sheet2 = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName() + "明细");
                        //设置表头
                        HSSFRow row2 = sheet2.createRow(0);
                        row2.createCell(0).setCellValue("序号");
                        row2.createCell(1).setCellValue("商品名称");
                        row2.createCell(2).setCellValue("规格");
                        row2.createCell(3).setCellValue("品牌");
                        row2.createCell(4).setCellValue("详细");
                        row2.createCell(5).setCellValue("总采购金额");
                        row2.createCell(6).setCellValue("总制作成本");
                        row2.createCell(7).setCellValue("总损耗成本");
                        row2.createCell(8).setCellValue("总废弃成本");
                        row2.createCell(9).setCellValue("总退货金额");
                        row2.createCell(10).setCellValue("总库存金额");
                        row2.createCell(11).setCellValue("部门");
                        row2.createCell(12).setCellValue("部门制作成本");
                        row2.createCell(13).setCellValue("部门损耗成本");
                        row2.createCell(14).setCellValue("部门废弃成本");
                        row2.createCell(15).setCellValue("部门退货金额");
                        row2.createCell(16).setCellValue("部门库存金额");
                        HSSFRow goodsRow2 = null;
                        int m = 1;

                        for (GbDistributerGoodsEntity distributerGoodsEntity : distributerGoodsEntities) {

                            goodsRow2 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                            goodsRow2.createCell(0).setCellValue(m);
                            m = m + 1;
                            goodsRow2.createCell(1).setCellValue(distributerGoodsEntity.getGbDgGoodsName());
                            goodsRow2.createCell(2).setCellValue(distributerGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow2.createCell(3).setCellValue(distributerGoodsEntity.getGbDgGoodsBrand());
                            goodsRow2.createCell(4).setCellValue(distributerGoodsEntity.getGbDgGoodsDetail());


                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            Double doutbleProduceWeight = 0.0;
                            Double doutbleLossWeight = 0.0;
                            Double doutbleWasteWeight = 0.0;
                            Double doutbleReturnWeight = 0.0;
                            double totalWeight = 0.0;
                            double restWeight = 0.0;
                            Integer integerG = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMap);
                            if (integerG > 0) {

                                Integer integer2 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(disGoodsMap);
                                if (integer2 > 0) {
                                    totalWeight = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(disGoodsMap);
                                }
                                goodsRow2.createCell(5).setCellValue(new BigDecimal(totalWeight).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                                Integer integerLossDep = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMap);
                                if (integerLossDep > 0) {
                                    doutbleLossWeight = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(disGoodsMap);
                                }
                                goodsRow2.createCell(7).setCellValue(new BigDecimal(doutbleLossWeight).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                                Integer integerWasteDep = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMap);
                                if (integerWasteDep > 0) {
                                    doutbleWasteWeight = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(disGoodsMap);
                                }
                                goodsRow2.createCell(8).setCellValue(new BigDecimal(doutbleWasteWeight).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                                Integer integerReturnDep = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMap);
                                if (integerReturnDep > 0) {
                                    doutbleReturnWeight = gbDepGoodsDailyService.queryDepGoodsDailyReturnSubtotal(disGoodsMap);
                                }
                                goodsRow2.createCell(9).setCellValue(new BigDecimal(doutbleReturnWeight).setScale(2, BigDecimal.ROUND_HALF_UP).toString());


                                Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(disGoodsMap);
                                if (integer1 > 0) {
                                    restWeight = gbDepGoodsStockService.queryDepStockRestSubtotal(disGoodsMap);
                                }
                                goodsRow2.createCell(10).setCellValue(new BigDecimal(restWeight).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                            }

                            TreeSet<GbDepartmentEntity> departmentEntities = gbDepGoodsDailyService.queryWhichDepsHasProduceDepGoodsDaily(disGoodsMap);

                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                            disGoodsMap.put("depType", getGbDepartmentTypeMendian());
                            Integer integerProduceDep = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMap);
                            if (integerProduceDep > 0) {
                                doutbleProduceWeight= gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(disGoodsMap);
                            }
                            goodsRow2.createCell(6).setCellValue(doutbleProduceWeight);

                            System.out.println("depdsssss" + departmentEntities.size());
                            HSSFRow goodsRow3 = null;
                            for (GbDepartmentEntity departmentEntity : departmentEntities) {
                                goodsRow3 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                                Integer gbDgdGbDepartmentId = departmentEntity.getGbDepartmentId();
                                GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(gbDgdGbDepartmentId);

                                Map<String, Object> disGoodsMapDep = new HashMap<>();
                                disGoodsMapDep.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());
                                disGoodsMapDep.put("startDate", reportEntity.getGbRepStartDate());
                                disGoodsMapDep.put("stopDate", reportEntity.getGbRepStopDate());
                                goodsRow3.createCell(11).setCellValue(gbDepartmentEntity.getGbDepartmentName());
                                disGoodsMapDep.put("depFatherId", gbDgdGbDepartmentId);
                                Integer integerd = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMapDep);
                                Double doutbleProduceWeightDep = 0.0;
                                Double doutbleLossWeightDep = 0.0;
                                Double doutbleWasteWeightDep = 0.0;
                                Double doutbleReturnWeightDep = 0.0;
                                double restWeightDep = 0.0;
                                if (integerd > 0) {

                                    disGoodsMapDep.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                                    System.out.println("disgoododdooddoddododoodood----" + disGoodsMapDep);

                                    Integer integerLossDep = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMapDep);
                                    if (integerLossDep > 0) {
                                        doutbleLossWeightDep = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(disGoodsMapDep);
                                    }
                                    goodsRow3.createCell(13).setCellValue(new BigDecimal(doutbleLossWeightDep).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                    disGoodsMapDep.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                                    Integer integerWasteDep = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMapDep);
                                    if (integerWasteDep > 0) {
                                        doutbleWasteWeightDep = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(disGoodsMapDep);
                                    }
                                    goodsRow3.createCell(14).setCellValue(new BigDecimal(doutbleWasteWeightDep).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                    disGoodsMapDep.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                                    Integer integerReturnDep = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMapDep);
                                    if (integerReturnDep > 0) {
                                        doutbleReturnWeightDep = gbDepGoodsDailyService.queryDepGoodsDailyReturnSubtotal(disGoodsMapDep);
                                    }
                                    goodsRow3.createCell(15).setCellValue(new BigDecimal(doutbleReturnWeightDep).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                    Integer integer1 = gbDepGoodsStockService.queryGoodsStockCount(disGoodsMapDep);
                                    if (integer1 > 0) {
                                        restWeightDep = gbDepGoodsStockService.queryDepStockRestSubtotal(disGoodsMapDep);
                                    }
                                    goodsRow3.createCell(16).setCellValue(new BigDecimal(restWeightDep).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                    disGoodsMapDep.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                                    disGoodsMapDep.put("depType", getGbDepartmentTypeMendian());
                                    Integer integerProduceDe = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMapDep);
                                    if (integerProduceDe > 0) {
                                        doutbleProduceWeightDep = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(disGoodsMapDep);
                                    }
                                    goodsRow3.createCell(12).setCellValue(doutbleProduceWeightDep);

                                }

                            }

                        }


                    }
                }

            }


        }


        return wb;
    }

    private HSSFWorkbook toCreatDisOutGoodsFreshForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("toDepId", reportEntity.getGbRepIds());
        map.put("controlFresh", 1);
        System.out.println("fathehreh" + map);
        List<GbDistributerFatherGoodsEntity> greatGrandGoodsEntities = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map);
        for (GbDistributerFatherGoodsEntity greatGrand : greatGrandGoodsEntities) {

            List<GbDistributerFatherGoodsEntity> grandGoodsEntities = greatGrand.getFatherGoodsEntities();

            for (GbDistributerFatherGoodsEntity grand : grandGoodsEntities) {
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                for (GbDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {

                    System.out.println("fahhhg-namemememmememefatherGoodsEntity-=======" + fatherGoodsEntity.getGbDfgFatherGoodsName());
//                    GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(fatherGoods.getGbDistributerFatherGoodsId());

                    if (fatherGoodsEntities.size() > 0) {
                        HSSFSheet sheet = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("天数");
                        row1.createCell(6).setCellValue("平均日鲜率(%)");
                        row1.createCell(7).setCellValue("最高日鲜率");
                        row1.createCell(8).setCellValue("最低日鲜率");
                        row1.createCell(9).setCellValue("平均每日剩余数量");
                        row1.createCell(10).setCellValue("平均沽清时间");

                        map.put("disGoodsFatherId", fatherGoodsEntity.getGbDistributerFatherGoodsId());
                        TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (GbDistributerGoodsEntity ckGoodsEntity : distributerGoodsEntities) {
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMap);
                            if (integer > 0) {
                                double freshRate = gbDepGoodsDailyService.queryDepGoodsFreshRate(disGoodsMap);
                                int clearHour = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(disGoodsMap);
                                int clearMinute = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(disGoodsMap);
                                String time = "";
                                int hourTF = clearHour * 60;
                                int totalMinuteF = (hourTF + clearMinute) / integer;
                                int hourF = totalMinuteF / 60;
                                int minTF = totalMinuteF % 60;

                                String minString = "";
                                if (minTF < 10) {
                                    minString = "0" + minTF;
                                } else {
                                    minString = Integer.toString(minTF);
                                }
                                time = hourF + ":" + minString;

                                goodsRow.createCell(5).setCellValue(integer);
                                BigDecimal divide = new BigDecimal(freshRate).divide(new BigDecimal(integer), 2, BigDecimal.ROUND_HALF_UP);
                                System.out.println("abc" + freshRate);
                                System.out.println("deff" + integer);
                                System.out.println("reeeee" + divide);
                                double highestFreshRate = gbDepGoodsDailyService.queryDepGoodsDailyHighestFreshRate(disGoodsMap);
                                double lowestFreshRate = gbDepGoodsDailyService.queryDepGoodsDailyLowestFreshRate(disGoodsMap);

                                goodsRow.createCell(6).setCellValue(divide.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                                goodsRow.createCell(7).setCellValue(new BigDecimal(highestFreshRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                                goodsRow.createCell(8).setCellValue(new BigDecimal(lowestFreshRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                                Double restWeight = gbDepGoodsStockService.queryDepGoodsRestWeightTotal(disGoodsMap);

                                System.out.println("rererereree" + restWeight);
                                BigDecimal divide1 = new BigDecimal(restWeight).divide(new BigDecimal(integer), 2, BigDecimal.ROUND_HALF_UP);
                                goodsRow.createCell(9).setCellValue(divide1.toString());
                                goodsRow.createCell(10).setCellValue(time);

                            }

                        }


                        HSSFSheet sheet2 = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName() + "明细");
                        //设置表头
                        HSSFRow row2 = sheet2.createRow(0);
                        row2.createCell(0).setCellValue("序号");
                        row2.createCell(1).setCellValue("商品名称");
                        row2.createCell(2).setCellValue("规格");
                        row2.createCell(3).setCellValue("品牌");
                        row2.createCell(4).setCellValue("详细");
                        row2.createCell(5).setCellValue("日期");
                        row2.createCell(6).setCellValue("店铺");
                        row2.createCell(7).setCellValue("日鲜率");
                        row2.createCell(8).setCellValue("剩余数量");
                        row2.createCell(9).setCellValue("沽清时间");
                        HSSFRow goodsRow2 = null;
                        int m = 1;

                        for (GbDistributerGoodsEntity distributerGoodsEntity : distributerGoodsEntities) {

                            goodsRow2 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                            goodsRow2.createCell(0).setCellValue(m);
                            m = m + 1;
                            goodsRow2.createCell(1).setCellValue(distributerGoodsEntity.getGbDgGoodsName());
                            goodsRow2.createCell(2).setCellValue(distributerGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow2.createCell(3).setCellValue(distributerGoodsEntity.getGbDgGoodsBrand());
                            goodsRow2.createCell(4).setCellValue(distributerGoodsEntity.getGbDgGoodsDetail());
                            //5 totalWeight

                            Integer howManyDaysInPeriod = 0;
                            if (!reportEntity.getGbRepStartDate().equals(reportEntity.getGbRepStopDate())) {
                                howManyDaysInPeriod = getHowManyDaysInPeriod(reportEntity.getGbRepStopDate(), reportEntity.getGbRepStartDate());
                            }
                            System.out.println("hoammdmmd" + howManyDaysInPeriod);
                            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                                String whichDay = "";
                                if (i == 0) {
                                    whichDay = reportEntity.getGbRepStartDate();
                                } else {
                                    whichDay = afterWhatDay(reportEntity.getGbRepStartDate(), i);
                                }

                                Map<String, Object> disGoodsMap = new HashMap<>();
                                disGoodsMap.put("date", whichDay);
                                disGoodsMap.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());
                                double highInteger = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMap);
                                if (highInteger > 0) {
                                    List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListByParams(disGoodsMap);

                                    HSSFRow goodsRow3 = null;
                                    for (GbDepartmentGoodsDailyEntity dailyEntity : departmentGoodsDailyEntities) {
                                        goodsRow3 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                                        goodsRow3.createCell(5).setCellValue(whichDay);
                                        Integer gbDgdGbDepartmentId = dailyEntity.getGbDgdGbDepartmentId();
                                        GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(gbDgdGbDepartmentId);

                                        goodsRow3.createCell(6).setCellValue(gbDepartmentEntity.getGbDepartmentName());
                                        goodsRow3.createCell(7).setCellValue(dailyEntity.getGbDgdFreshRate() + "%");
                                        goodsRow3.createCell(8).setCellValue(dailyEntity.getGbDgdRestWeight() + distributerGoodsEntity.getGbDgGoodsStandardname());
                                        goodsRow3.createCell(9).setCellValue(dailyEntity.getGbDgdSellClearHour() + ":" + dailyEntity.getGbDgdSellClearMinute());
                                    }

                                }

                            }
                        }


                    }
                }

            }


        }


        return wb;
    }


    private HSSFWorkbook toCreatGoodsFreshForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("disGoodsFatherId", reportEntity.getGbRepIds());
        map.put("controlFresh", 1);
        TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);

        GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(Integer.valueOf(reportEntity.getGbRepIds()));


        if (distributerGoodsEntities.size() > 0) {
            HSSFSheet sheet = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName());
            //设置表头
            HSSFRow row1 = sheet.createRow(0);
            row1.createCell(0).setCellValue("序号");
            row1.createCell(1).setCellValue("商品名称");
            row1.createCell(2).setCellValue("规格");
            row1.createCell(3).setCellValue("品牌");
            row1.createCell(4).setCellValue("详细");
            row1.createCell(5).setCellValue("天数");
            row1.createCell(6).setCellValue("平均日鲜率(%)");
            row1.createCell(7).setCellValue("最高日鲜率");
            row1.createCell(8).setCellValue("最低日鲜率");
            row1.createCell(9).setCellValue("平均每日剩余数量");
            row1.createCell(10).setCellValue("平均沽清时间");
            //设置表体
            HSSFRow goodsRow = null;
            for (GbDistributerGoodsEntity ckGoodsEntity : distributerGoodsEntities) {
                goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                //5 totalWeight
                Map<String, Object> disGoodsMap = new HashMap<>();
                disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                disGoodsMap.put("produce", 0);
                Integer integer = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMap);
                if (integer > 0) {
                    double freshRate = gbDepGoodsDailyService.queryDepGoodsFreshRate(disGoodsMap);
                    int clearHour = gbDepGoodsDailyService.queryDepGoodsDailyClearHour(disGoodsMap);
                    int clearMinute = gbDepGoodsDailyService.queryDepGoodsDailyClearMinute(disGoodsMap);
                    String time = "";
                    int hourTF = clearHour * 60;
                    int totalMinuteF = (hourTF + clearMinute) / integer;
                    int hourF = totalMinuteF / 60;
                    int minTF = totalMinuteF % 60;

                    String minString = "";
                    if (minTF < 10) {
                        minString = "0" + minTF;
                    } else {
                        minString = Integer.toString(minTF);
                    }
                    time = hourF + ":" + minString;

                    goodsRow.createCell(5).setCellValue(integer);
                    BigDecimal divide = new BigDecimal(freshRate).divide(new BigDecimal(integer), 2, BigDecimal.ROUND_HALF_UP);
                    System.out.println("abc" + freshRate);
                    System.out.println("deff" + integer);
                    System.out.println("reeeee" + divide);
                    double highestFreshRate = gbDepGoodsDailyService.queryDepGoodsDailyHighestFreshRate(disGoodsMap);
                    double lowestFreshRate = gbDepGoodsDailyService.queryDepGoodsDailyLowestFreshRate(disGoodsMap);

                    goodsRow.createCell(6).setCellValue(divide.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    goodsRow.createCell(7).setCellValue(new BigDecimal(highestFreshRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    goodsRow.createCell(8).setCellValue(new BigDecimal(lowestFreshRate).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

                    Double restWeight = gbDepGoodsStockService.queryDepGoodsRestWeightTotal(disGoodsMap);

                    System.out.println("rererereree" + restWeight);
                    BigDecimal divide1 = new BigDecimal(restWeight).divide(new BigDecimal(integer), 2, BigDecimal.ROUND_HALF_UP);
                    goodsRow.createCell(9).setCellValue(divide1.toString());
                    goodsRow.createCell(10).setCellValue(time);

                }

            }


            HSSFSheet sheet2 = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName() + "明细");
            //设置表头
            HSSFRow row2 = sheet2.createRow(0);
            row2.createCell(0).setCellValue("序号");
            row2.createCell(1).setCellValue("商品名称");
            row2.createCell(2).setCellValue("规格");
            row2.createCell(3).setCellValue("品牌");
            row2.createCell(4).setCellValue("详细");
            row2.createCell(5).setCellValue("日期");
            row2.createCell(6).setCellValue("店铺");
            row2.createCell(7).setCellValue("日鲜率");
            row2.createCell(8).setCellValue("剩余数量");
            row2.createCell(9).setCellValue("沽清时间");
            HSSFRow goodsRow2 = null;
            int m = 1;

            for (GbDistributerGoodsEntity distributerGoodsEntity : distributerGoodsEntities) {

                goodsRow2 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                goodsRow2.createCell(0).setCellValue(m);
                m = m + 1;
                goodsRow2.createCell(1).setCellValue(distributerGoodsEntity.getGbDgGoodsName());
                goodsRow2.createCell(2).setCellValue(distributerGoodsEntity.getGbDgGoodsStandardname());
                goodsRow2.createCell(3).setCellValue(distributerGoodsEntity.getGbDgGoodsBrand());
                goodsRow2.createCell(4).setCellValue(distributerGoodsEntity.getGbDgGoodsDetail());
                //5 totalWeight

                Integer howManyDaysInPeriod = 0;
                if (!reportEntity.getGbRepStartDate().equals(reportEntity.getGbRepStopDate())) {
                    howManyDaysInPeriod = getHowManyDaysInPeriod(reportEntity.getGbRepStopDate(), reportEntity.getGbRepStartDate());
                }
                System.out.println("hoammdmmd" + howManyDaysInPeriod);
                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                    String whichDay = "";
                    if (i == 0) {
                        whichDay = reportEntity.getGbRepStartDate();
                    } else {
                        whichDay = afterWhatDay(reportEntity.getGbRepStartDate(), i);
                    }

                    Map<String, Object> disGoodsMap = new HashMap<>();
                    disGoodsMap.put("date", whichDay);
                    disGoodsMap.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());
                    double highInteger = gbDepGoodsDailyService.queryDepGoodsDailyCount(disGoodsMap);
                    if (highInteger > 0) {
                        List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListByParams(disGoodsMap);

                        HSSFRow goodsRow3 = null;
                        for (GbDepartmentGoodsDailyEntity dailyEntity : departmentGoodsDailyEntities) {
                            goodsRow3 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                            goodsRow3.createCell(5).setCellValue(whichDay);
                            Integer gbDgdGbDepartmentId = dailyEntity.getGbDgdGbDepartmentId();
                            GbDepartmentEntity gbDepartmentEntity = gbDepartmentService.queryObject(gbDgdGbDepartmentId);

                            goodsRow3.createCell(6).setCellValue(gbDepartmentEntity.getGbDepartmentName());
                            goodsRow3.createCell(7).setCellValue(dailyEntity.getGbDgdFreshRate() + "%");
                            goodsRow3.createCell(8).setCellValue(dailyEntity.getGbDgdRestWeight() + distributerGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow3.createCell(9).setCellValue(dailyEntity.getGbDgdSellClearHour() + ":" + dailyEntity.getGbDgdSellClearMinute());
                        }

                    }

                }
            }


        }


        return wb;
    }


    private HSSFWorkbook toCreatNxDisControlGoodsPriceForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("nxDisId", reportEntity.getGbRepIds());
        System.out.println("mapamapapapExcelleleel" + map);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDistributerGoodsPriceService.queryTreeSetDisGoods(map);

        GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(Integer.valueOf(reportEntity.getGbRepIds()));


        if (distributerGoodsEntities.size() > 0) {
            HSSFSheet sheet = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName());
            //设置表头
            HSSFRow row1 = sheet.createRow(0);
            row1.createCell(0).setCellValue("序号");
            row1.createCell(1).setCellValue("商品名称");
            row1.createCell(2).setCellValue("规格");
            row1.createCell(3).setCellValue("品牌");
            row1.createCell(4).setCellValue("详细");
            row1.createCell(5).setCellValue("高出次数");
            row1.createCell(6).setCellValue("高出百分比(%)");
            row1.createCell(7).setCellValue("高出总金额");
//            row1.createCell(8).setCellValue("销售利润");
            row1.createCell(9).setCellValue("低于次数");
            row1.createCell(10).setCellValue("低于百分比(%)");
            row1.createCell(11).setCellValue("低于总金额");
            row1.createCell(12).setCellValue("采购成本");
//            row1.createCell(13).setCellValue("平均单价");
            //设置表体
            HSSFRow goodsRow = null;
            for (GbDistributerGoodsEntity ckGoodsEntity : distributerGoodsEntities) {
                goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                //5 totalWeight
                Map<String, Object> disGoodsMap = new HashMap<>();
                disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());

                //6 totalWeight

                disGoodsMap.put("purWhat", 1);
                double highInteger = gbDistributerGoodsPriceService.queryDisPriceGoodsCount(disGoodsMap);
                if (highInteger > 0) {
                    double scale = gbDistributerGoodsPriceService.queryPriceWhatScale(disGoodsMap);
                    double v = scale / highInteger;
                    BigDecimal decimal = new BigDecimal(v).setScale(4, BigDecimal.ROUND_HALF_UP);
                    BigDecimal multiply = decimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                    double total = gbDistributerGoodsPriceService.queryPriceWhatTotal(disGoodsMap);
                    goodsRow.createCell(5).setCellValue(highInteger);
                    goodsRow.createCell(6).setCellValue(multiply.toString() + "%");
                    goodsRow.createCell(7).setCellValue(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    goodsRow.createCell(5).setCellValue(0);
                    goodsRow.createCell(6).setCellValue(0 + "%");
                    goodsRow.createCell(7).setCellValue(0);
                }
                disGoodsMap.put("purWhat", -1);
                double lowInteger = gbDistributerGoodsPriceService.queryDisPriceGoodsCount(disGoodsMap);
                if (lowInteger > 0) {
                    double scale = gbDistributerGoodsPriceService.queryPriceWhatScale(disGoodsMap);
                    double v = scale / lowInteger;
                    BigDecimal decimal = new BigDecimal(v).setScale(4, BigDecimal.ROUND_HALF_UP);
                    BigDecimal multiply = decimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    double total = gbDistributerGoodsPriceService.queryPriceWhatTotal(disGoodsMap);
                    goodsRow.createCell(9).setCellValue(lowInteger);
                    goodsRow.createCell(10).setCellValue(multiply.toString() + "%");
                    goodsRow.createCell(11).setCellValue(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    goodsRow.createCell(9).setCellValue(0);
                    goodsRow.createCell(10).setCellValue(0 + "%");
                    goodsRow.createCell(11).setCellValue(0);
                }

            }


            HSSFSheet sheet2 = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName() + "明细");
            //设置表头
            HSSFRow row2 = sheet2.createRow(0);
            row2.createCell(0).setCellValue("序号");
            row2.createCell(1).setCellValue("商品名称");
            row2.createCell(2).setCellValue("规格");
            row2.createCell(3).setCellValue("品牌");
            row2.createCell(4).setCellValue("详细");
            row2.createCell(5).setCellValue("日期");
            row2.createCell(6).setCellValue("超低");
            row2.createCell(7).setCellValue("百分比");
            row2.createCell(8).setCellValue("采购单价");
            row2.createCell(9).setCellValue("价控最低单价");
            row2.createCell(10).setCellValue("价控最高单价");
            row2.createCell(11).setCellValue("相差总额");
            HSSFRow goodsRow2 = null;
            for (int j = 0; j < distributerGoodsEntities.size(); j++) {
                goodsRow2 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                goodsRow2.createCell(0).setCellValue(j + 1);
                goodsRow2.createCell(1).setCellValue(distributerGoodsEntities.get(j).getGbDgGoodsName());
                goodsRow2.createCell(2).setCellValue(distributerGoodsEntities.get(j).getGbDgGoodsStandardname());
                goodsRow2.createCell(3).setCellValue(distributerGoodsEntities.get(j).getGbDgGoodsBrand());
                goodsRow2.createCell(4).setCellValue(distributerGoodsEntities.get(j).getGbDgGoodsDetail());
                //5 totalWeight

                Integer howManyDaysInPeriod = 0;
                if (!reportEntity.getGbRepStartDate().equals(reportEntity.getGbRepStopDate())) {
                    howManyDaysInPeriod = getHowManyDaysInPeriod(reportEntity.getGbRepStopDate(), reportEntity.getGbRepStartDate());
                }
                System.out.println("hoammdmmd" + howManyDaysInPeriod);
                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                    String whichDay = "";
                    if (i == 0) {
                        whichDay = reportEntity.getGbRepStartDate();
                    } else {
                        whichDay = afterWhatDay(reportEntity.getGbRepStartDate(), i);
                    }

                    Map<String, Object> disGoodsMap = new HashMap<>();
                    disGoodsMap.put("date", whichDay);
                    disGoodsMap.put("disGoodsId", distributerGoodsEntities.get(j).getGbDistributerGoodsId());
                    System.out.println("disgodmamd" + disGoodsMap);
                    double highInteger = gbDistributerGoodsPriceService.queryDisPriceGoodsCount(disGoodsMap);
                    if (highInteger > 0) {
                        System.out.println("youzhizhizzz" + highInteger);

                        List<GbDistributerGoodsPriceEntity> entities = gbDistributerGoodsPriceService.queryPriceGoodsListByParams(disGoodsMap);
                        HSSFRow goodsRow3 = null;

                        for (GbDistributerGoodsPriceEntity priceGoods : entities) {
                            goodsRow3 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                            goodsRow3.createCell(5).setCellValue(whichDay);
                            Integer gbDgpPurWhat = priceGoods.getGbDgpPurWhat();
                            if (gbDgpPurWhat == 1) {
                                goodsRow3.createCell(6).setCellValue("高");
                            } else {
                                goodsRow3.createCell(6).setCellValue("低");
                            }
                            BigDecimal decimal = new BigDecimal(priceGoods.getGbDgpPurScale()).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                            goodsRow3.createCell(7).setCellValue(decimal + "%");
                            goodsRow3.createCell(8).setCellValue(priceGoods.getGbDgpPurPrice());
                            goodsRow3.createCell(9).setCellValue(priceGoods.getGbDgpGoodsLowestPrice());
                            goodsRow3.createCell(10).setCellValue(priceGoods.getGbDgpGoodsHighestPrice());
                            goodsRow3.createCell(11).setCellValue(priceGoods.getGbDgpPurWhatTotal());
                        }

                    }

                }


            }


        }

        return wb;
    }


    private HSSFWorkbook toCreatDisControlGoodsPriceForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depId", reportEntity.getGbRepIds());
        System.out.println("mapamapapapExcelleleel" + map);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDistributerGoodsPriceService.queryTreeSetDisGoods(map);

        GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(Integer.valueOf(reportEntity.getGbRepIds()));


        if (distributerGoodsEntities.size() > 0) {
            HSSFSheet sheet = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName());
            //设置表头
            HSSFRow row1 = sheet.createRow(0);
            row1.createCell(0).setCellValue("序号");
            row1.createCell(1).setCellValue("商品名称");
            row1.createCell(2).setCellValue("规格");
            row1.createCell(3).setCellValue("品牌");
            row1.createCell(4).setCellValue("详细");
            row1.createCell(5).setCellValue("高出次数");
            row1.createCell(6).setCellValue("高出百分比(%)");
            row1.createCell(7).setCellValue("高出总金额");
//            row1.createCell(8).setCellValue("销售利润");
            row1.createCell(9).setCellValue("低于次数");
            row1.createCell(10).setCellValue("低于百分比(%)");
            row1.createCell(11).setCellValue("低于总金额");
            row1.createCell(12).setCellValue("采购成本");
//            row1.createCell(13).setCellValue("平均单价");
            //设置表体
            HSSFRow goodsRow = null;
            for (GbDistributerGoodsEntity ckGoodsEntity : distributerGoodsEntities) {
                goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                //5 totalWeight
                Map<String, Object> disGoodsMap = new HashMap<>();
                disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());

                //6 totalWeight

                disGoodsMap.put("purWhat", 1);
                double highInteger = gbDistributerGoodsPriceService.queryDisPriceGoodsCount(disGoodsMap);
                if (highInteger > 0) {
                    double scale = gbDistributerGoodsPriceService.queryPriceWhatScale(disGoodsMap);
                    double v = scale / highInteger;
                    BigDecimal decimal = new BigDecimal(v).setScale(4, BigDecimal.ROUND_HALF_UP);
                    BigDecimal multiply = decimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                    double total = gbDistributerGoodsPriceService.queryPriceWhatTotal(disGoodsMap);
                    goodsRow.createCell(5).setCellValue(highInteger);
                    goodsRow.createCell(6).setCellValue(multiply.toString() + "%");
                    goodsRow.createCell(7).setCellValue(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    goodsRow.createCell(5).setCellValue(0);
                    goodsRow.createCell(6).setCellValue(0 + "%");
                    goodsRow.createCell(7).setCellValue(0);
                }
                disGoodsMap.put("purWhat", -1);
                double lowInteger = gbDistributerGoodsPriceService.queryDisPriceGoodsCount(disGoodsMap);
                if (lowInteger > 0) {
                    double scale = gbDistributerGoodsPriceService.queryPriceWhatScale(disGoodsMap);
                    double v = scale / lowInteger;
                    BigDecimal decimal = new BigDecimal(v).setScale(4, BigDecimal.ROUND_HALF_UP);
                    BigDecimal multiply = decimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    double total = gbDistributerGoodsPriceService.queryPriceWhatTotal(disGoodsMap);
                    goodsRow.createCell(9).setCellValue(lowInteger);
                    goodsRow.createCell(10).setCellValue(multiply.toString() + "%");
                    goodsRow.createCell(11).setCellValue(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    goodsRow.createCell(9).setCellValue(0);
                    goodsRow.createCell(10).setCellValue(0 + "%");
                    goodsRow.createCell(11).setCellValue(0);
                }

            }


            HSSFSheet sheet2 = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName() + "明细");
            //设置表头
            HSSFRow row2 = sheet2.createRow(0);
            row2.createCell(0).setCellValue("序号");
            row2.createCell(1).setCellValue("商品名称");
            row2.createCell(2).setCellValue("规格");
            row2.createCell(3).setCellValue("品牌");
            row2.createCell(4).setCellValue("详细");
            row2.createCell(5).setCellValue("日期");
            row2.createCell(6).setCellValue("超低");
            row2.createCell(7).setCellValue("百分比");
            row2.createCell(8).setCellValue("采购单价");
            row2.createCell(9).setCellValue("价控最低单价");
            row2.createCell(10).setCellValue("价控最高单价");
            row2.createCell(11).setCellValue("相差总额");
            HSSFRow goodsRow2 = null;
            for (int j = 0; j < distributerGoodsEntities.size(); j++) {
                goodsRow2 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                goodsRow2.createCell(0).setCellValue(j + 1);
                goodsRow2.createCell(1).setCellValue(distributerGoodsEntities.get(j).getGbDgGoodsName());
                goodsRow2.createCell(2).setCellValue(distributerGoodsEntities.get(j).getGbDgGoodsStandardname());
                goodsRow2.createCell(3).setCellValue(distributerGoodsEntities.get(j).getGbDgGoodsBrand());
                goodsRow2.createCell(4).setCellValue(distributerGoodsEntities.get(j).getGbDgGoodsDetail());
                //5 totalWeight

                Integer howManyDaysInPeriod = 0;
                if (!reportEntity.getGbRepStartDate().equals(reportEntity.getGbRepStopDate())) {
                    howManyDaysInPeriod = getHowManyDaysInPeriod(reportEntity.getGbRepStopDate(), reportEntity.getGbRepStartDate());
                }
                System.out.println("hoammdmmd" + howManyDaysInPeriod);
                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                    String whichDay = "";
                    if (i == 0) {
                        whichDay = reportEntity.getGbRepStartDate();
                    } else {
                        whichDay = afterWhatDay(reportEntity.getGbRepStartDate(), i);
                    }

                    Map<String, Object> disGoodsMap = new HashMap<>();
                    disGoodsMap.put("date", whichDay);
                    disGoodsMap.put("disGoodsId", distributerGoodsEntities.get(j).getGbDistributerGoodsId());
                    System.out.println("disgodmamd" + disGoodsMap);
                    double highInteger = gbDistributerGoodsPriceService.queryDisPriceGoodsCount(disGoodsMap);
                    if (highInteger > 0) {
                        System.out.println("youzhizhizzz" + highInteger);

                        List<GbDistributerGoodsPriceEntity> entities = gbDistributerGoodsPriceService.queryPriceGoodsListByParams(disGoodsMap);
                        HSSFRow goodsRow3 = null;

                        for (GbDistributerGoodsPriceEntity priceGoods : entities) {
                            goodsRow3 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                            goodsRow3.createCell(5).setCellValue(whichDay);
                            Integer gbDgpPurWhat = priceGoods.getGbDgpPurWhat();
                            if (gbDgpPurWhat == 1) {
                                goodsRow3.createCell(6).setCellValue("高");
                            } else {
                                goodsRow3.createCell(6).setCellValue("低");
                            }
                            BigDecimal decimal = new BigDecimal(priceGoods.getGbDgpPurScale()).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                            goodsRow3.createCell(7).setCellValue(decimal + "%");
                            goodsRow3.createCell(8).setCellValue(priceGoods.getGbDgpPurPrice());
                            goodsRow3.createCell(9).setCellValue(priceGoods.getGbDgpGoodsLowestPrice());
                            goodsRow3.createCell(10).setCellValue(priceGoods.getGbDgpGoodsHighestPrice());
                            goodsRow3.createCell(11).setCellValue(priceGoods.getGbDgpPurWhatTotal());
                        }

                    }

                }


            }


        }

        return wb;
    }

    private HSSFWorkbook toCreatGoodsPriceForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("disGoodsFatherId", reportEntity.getGbRepIds());
        System.out.println("mapamapapapExcelleleel" + map);
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDistributerGoodsPriceService.queryTreeSetDisGoods(map);

        GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(Integer.valueOf(reportEntity.getGbRepIds()));


        if (distributerGoodsEntities.size() > 0) {
            HSSFSheet sheet = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName());
            //设置表头
            HSSFRow row1 = sheet.createRow(0);
            row1.createCell(0).setCellValue("序号");
            row1.createCell(1).setCellValue("商品名称");
            row1.createCell(2).setCellValue("规格");
            row1.createCell(3).setCellValue("品牌");
            row1.createCell(4).setCellValue("详细");
            row1.createCell(5).setCellValue("高出次数");
            row1.createCell(6).setCellValue("高出百分比(%)");
            row1.createCell(7).setCellValue("高出总金额");
//            row1.createCell(8).setCellValue("销售利润");
            row1.createCell(9).setCellValue("低于次数");
            row1.createCell(10).setCellValue("低于百分比(%)");
            row1.createCell(11).setCellValue("低于总金额");
            row1.createCell(12).setCellValue("采购成本");
//            row1.createCell(13).setCellValue("平均单价");
            //设置表体
            HSSFRow goodsRow = null;
            for (GbDistributerGoodsEntity ckGoodsEntity : distributerGoodsEntities) {
                goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                //5 totalWeight
                Map<String, Object> disGoodsMap = new HashMap<>();
                disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());

                //6 totalWeight

                disGoodsMap.put("purWhat", 1);
                double highInteger = gbDistributerGoodsPriceService.queryDisPriceGoodsCount(disGoodsMap);
                if (highInteger > 0) {
                    double scale = gbDistributerGoodsPriceService.queryPriceWhatScale(disGoodsMap);
                    double v = scale / highInteger;
                    BigDecimal decimal = new BigDecimal(v).setScale(4, BigDecimal.ROUND_HALF_UP);
                    BigDecimal multiply = decimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                    double total = gbDistributerGoodsPriceService.queryPriceWhatTotal(disGoodsMap);
                    goodsRow.createCell(5).setCellValue(highInteger);
                    goodsRow.createCell(6).setCellValue(multiply.toString() + "%");
                    goodsRow.createCell(7).setCellValue(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    goodsRow.createCell(5).setCellValue(0);
                    goodsRow.createCell(6).setCellValue(0 + "%");
                    goodsRow.createCell(7).setCellValue(0);
                }
                disGoodsMap.put("purWhat", -1);
                double lowInteger = gbDistributerGoodsPriceService.queryDisPriceGoodsCount(disGoodsMap);
                if (lowInteger > 0) {
                    double scale = gbDistributerGoodsPriceService.queryPriceWhatScale(disGoodsMap);
                    double v = scale / lowInteger;
                    BigDecimal decimal = new BigDecimal(v).setScale(4, BigDecimal.ROUND_HALF_UP);
                    BigDecimal multiply = decimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    double total = gbDistributerGoodsPriceService.queryPriceWhatTotal(disGoodsMap);
                    goodsRow.createCell(9).setCellValue(lowInteger);
                    goodsRow.createCell(10).setCellValue(multiply.toString() + "%");
                    goodsRow.createCell(11).setCellValue(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                } else {
                    goodsRow.createCell(9).setCellValue(0);
                    goodsRow.createCell(10).setCellValue(0 + "%");
                    goodsRow.createCell(11).setCellValue(0);
                }

            }


            HSSFSheet sheet2 = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName() + "明细");
            //设置表头
            HSSFRow row2 = sheet2.createRow(0);
            row2.createCell(0).setCellValue("序号");
            row2.createCell(1).setCellValue("商品名称");
            row2.createCell(2).setCellValue("规格");
            row2.createCell(3).setCellValue("品牌");
            row2.createCell(4).setCellValue("详细");
            row2.createCell(5).setCellValue("日期");
            row2.createCell(6).setCellValue("超低");
            row2.createCell(7).setCellValue("百分比");
            row2.createCell(8).setCellValue("采购单价");
            row2.createCell(9).setCellValue("价控最低单价");
            row2.createCell(10).setCellValue("价控最高单价");
            row2.createCell(11).setCellValue("相差总额");
            HSSFRow goodsRow2 = null;
            for (int j = 0; j < distributerGoodsEntities.size(); j++) {
                goodsRow2 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                goodsRow2.createCell(0).setCellValue(j + 1);
                goodsRow2.createCell(1).setCellValue(distributerGoodsEntities.get(j).getGbDgGoodsName());
                goodsRow2.createCell(2).setCellValue(distributerGoodsEntities.get(j).getGbDgGoodsStandardname());
                goodsRow2.createCell(3).setCellValue(distributerGoodsEntities.get(j).getGbDgGoodsBrand());
                goodsRow2.createCell(4).setCellValue(distributerGoodsEntities.get(j).getGbDgGoodsDetail());
                //5 totalWeight

                Integer howManyDaysInPeriod = 0;
                if (!reportEntity.getGbRepStartDate().equals(reportEntity.getGbRepStopDate())) {
                    howManyDaysInPeriod = getHowManyDaysInPeriod(reportEntity.getGbRepStopDate(), reportEntity.getGbRepStartDate());
                }
                System.out.println("hoammdmmd" + howManyDaysInPeriod);
                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                    String whichDay = "";
                    if (i == 0) {
                        whichDay = reportEntity.getGbRepStartDate();
                    } else {
                        whichDay = afterWhatDay(reportEntity.getGbRepStartDate(), i);
                    }

                    Map<String, Object> disGoodsMap = new HashMap<>();
                    disGoodsMap.put("date", whichDay);
                    disGoodsMap.put("disGoodsId", distributerGoodsEntities.get(j).getGbDistributerGoodsId());
                    System.out.println("disgodmamd" + disGoodsMap);
                    double highInteger = gbDistributerGoodsPriceService.queryDisPriceGoodsCount(disGoodsMap);
                    if (highInteger > 0) {
                        System.out.println("youzhizhizzz" + highInteger);

                        List<GbDistributerGoodsPriceEntity> entities = gbDistributerGoodsPriceService.queryPriceGoodsListByParams(disGoodsMap);
                        HSSFRow goodsRow3 = null;

                        for (GbDistributerGoodsPriceEntity priceGoods : entities) {
                            goodsRow3 = sheet2.createRow(sheet2.getLastRowNum() + 1);
                            goodsRow3.createCell(5).setCellValue(whichDay);
                            Integer gbDgpPurWhat = priceGoods.getGbDgpPurWhat();
                            if (gbDgpPurWhat == 1) {
                                goodsRow3.createCell(6).setCellValue("高");
                            } else {
                                goodsRow3.createCell(6).setCellValue("低");
                            }
                            BigDecimal decimal = new BigDecimal(priceGoods.getGbDgpPurScale()).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                            goodsRow3.createCell(7).setCellValue(decimal + "%");
                            goodsRow3.createCell(8).setCellValue(priceGoods.getGbDgpPurPrice());
                            goodsRow3.createCell(9).setCellValue(priceGoods.getGbDgpGoodsLowestPrice());
                            goodsRow3.createCell(10).setCellValue(priceGoods.getGbDgpGoodsHighestPrice());
                            goodsRow3.createCell(11).setCellValue(priceGoods.getGbDgpPurWhatTotal());
                        }

                    }

                }


            }


        }

        return wb;
    }

    private HSSFWorkbook toCreatGoodsProfitForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("disGoodsFatherId", reportEntity.getGbRepIds());
        System.out.println("mapamapapapExcelleleel" + map);
        TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);

        GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(Integer.valueOf(reportEntity.getGbRepIds()));


        if (distributerGoodsEntities.size() > 0) {
            HSSFSheet sheet = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName());
            //设置表头
            HSSFRow row1 = sheet.createRow(0);
            row1.createCell(0).setCellValue("序号");
            row1.createCell(1).setCellValue("商品名称");
            row1.createCell(2).setCellValue("规格");
            row1.createCell(3).setCellValue("品牌");
            row1.createCell(4).setCellValue("详细");
            row1.createCell(5).setCellValue("总成本");
            row1.createCell(6).setCellValue("总利润百分比(%)");
            row1.createCell(7).setCellValue("总利润");
            row1.createCell(8).setCellValue("销售利润");
            row1.createCell(9).setCellValue("损耗成本");
            row1.createCell(10).setCellValue("废弃成本");
            row1.createCell(11).setCellValue("采购总数量");
            row1.createCell(12).setCellValue("采购成本");
            row1.createCell(13).setCellValue("平均单价");
            //设置表体
            HSSFRow goodsRow = null;
            for (GbDistributerGoodsEntity ckGoodsEntity : distributerGoodsEntities) {
                goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                //5 totalWeight
                Map<String, Object> disGoodsMap = new HashMap<>();
                disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());

                //6 totalWeight

                double doubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(disGoodsMap);
                double doubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(disGoodsMap);
                double doubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(disGoodsMap);
                double aDoubleT = doubleP + doubleL + doubleW;
                goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleT).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                double aDouble = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(disGoodsMap);
                BigDecimal aDoublePer = new BigDecimal(aDouble).divide(new BigDecimal(aDoubleT), 4, BigDecimal.ROUND_HALF_UP);
                goodsRow.createCell(6).setCellValue(aDoublePer.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");
                goodsRow.createCell(7).setCellValue(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());


                Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(disGoodsMap);
                goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(disGoodsMap);
                goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(disGoodsMap);
                goodsRow.createCell(10).setCellValue(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                Double subtotal = gbDepGoodsDailyService.queryDepGoodsDailySubtotal(disGoodsMap);
                Double weight = gbDepGoodsDailyService.queryDepGoodsDailyWeight(disGoodsMap);

                BigDecimal divide = new BigDecimal(subtotal).divide(new BigDecimal(weight), 2, BigDecimal.ROUND_HALF_UP);

                goodsRow.createCell(11).setCellValue(new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsRow.createCell(12).setCellValue(new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsRow.createCell(13).setCellValue(divide.toString());

            }


        }

        return wb;
    }

    private HSSFWorkbook toCreatGoodsSalesForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("disGoodsFatherId", reportEntity.getGbRepIds());
        System.out.println("mapamapapapExcelleleel" + map);//
        TreeSet<GbDistributerGoodsEntity> distributerGoodsEntities = gbDepGoodsDailyService.queryDisGoodsTreesetByParams(map);

        GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(Integer.valueOf(reportEntity.getGbRepIds()));

        if (distributerGoodsEntities.size() > 0) {
            HSSFSheet sheet = wb.createSheet(fatherGoodsEntity.getGbDfgFatherGoodsName());
            //设置表头
            HSSFRow row1 = sheet.createRow(0);
            row1.createCell(0).setCellValue("序号");
            row1.createCell(1).setCellValue("商品名称");
            row1.createCell(2).setCellValue("规格");
            row1.createCell(3).setCellValue("品牌");
            row1.createCell(4).setCellValue("详细");
            row1.createCell(5).setCellValue("成本总数量");
            row1.createCell(6).setCellValue("销售数量");
            row1.createCell(7).setCellValue("损耗数量");
            row1.createCell(8).setCellValue("废弃数量");
            row1.createCell(9).setCellValue("退货数量");
            row1.createCell(10).setCellValue("库存数量");
            row1.createCell(11).setCellValue("采购总数量");
            row1.createCell(12).setCellValue("采购总成本");
            row1.createCell(13).setCellValue("平均单价");
            //设置表体
            HSSFRow goodsRow = null;
            for (GbDistributerGoodsEntity ckGoodsEntity : distributerGoodsEntities) {
                goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                //5 totalWeight
                Map<String, Object> disGoodsMap = new HashMap<>();
                disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());

                //6 totalWeight
                Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(disGoodsMap);
                goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(disGoodsMap);
                goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                double aDoubleT = gbDepGoodsStockService.queryDepStockReturnWeightTotal(disGoodsMap);
                goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleT).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                double aDoubleR = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                goodsRow.createCell(10).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());


                double subtotal = 0.0;
                double weight = 0.0;
                BigDecimal divide = new BigDecimal(0);
                if (ckGoodsEntity.getGbDgIsSelfControl() == 0) {
                    subtotal = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(disGoodsMap);
                    weight = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(disGoodsMap);
                    divide = new BigDecimal(subtotal).divide(new BigDecimal(weight), 2, BigDecimal.ROUND_HALF_UP);
                }


                goodsRow.createCell(11).setCellValue(new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsRow.createCell(12).setCellValue(new BigDecimal(subtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                goodsRow.createCell(13).setCellValue(divide.toString());


                disGoodsMap.put("depType", getGbDepartmentTypeMendian());
                Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(disGoodsMap);
                goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                double aDouble = aDoubleL + aDoubleW + aDoubleP;
                goodsRow.createCell(5).setCellValue(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

            }

        }

        return wb;
    }

    //
    private HSSFWorkbook toCreatDepReturnForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depFatherId", departmentEntity.getGbDepartmentId());
        map.put("return", 0);
        System.out.println("mapamapapapExcelleleel" + map);
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map);

        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        System.out.println("roororor---------------------" + father.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("退货成本");
                        row1.createCell(6).setCellValue("废弃成本");
                        row1.createCell(7).setCellValue("销售成本");
                        row1.createCell(8).setCellValue("损耗成本");
                        row1.createCell(9).setCellValue("总成本");
                        row1.createCell(10).setCellValue("零售总金额");
                        row1.createCell(11).setCellValue("利润总额");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());

                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("depFatherId", departmentEntity.getGbDepartmentId());
//                            disGoodsMap.put("loss", 0);
                            Double aDoubleRL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(disGoodsMap);
                            Double aDoubleRP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(disGoodsMap);
                            Double aDoubleRW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(disGoodsMap);
                            Double aDoublePro = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(disGoodsMap);
                            Double aDoubleRS = gbDepGoodsDailyService.queryDepGoodsDailySalesSubtotal(disGoodsMap);
                            Double aDoubleRR = gbDepGoodsDailyService.queryDepGoodsDailyReturnSubtotal(disGoodsMap);
                            double total = aDoubleRP + aDoubleRL + aDoubleRW;

                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleRR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleRW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleRP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleRL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(9).setCellValue(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(10).setCellValue(new BigDecimal(aDoubleRS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(11).setCellValue(new BigDecimal(aDoublePro).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                    }

                }
            }

        }

        return wb;
    }

    private HSSFWorkbook toCreatDepWasteForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depFatherId", departmentEntity.getGbDepartmentId());
        map.put("waste", 0);
        System.out.println("mapamapapapExcelleleel" + map);
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map);

        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        System.out.println("roororor---------------------" + father.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("废弃成本");
                        row1.createCell(6).setCellValue("销售成本");
                        row1.createCell(7).setCellValue("损耗成本");
                        row1.createCell(8).setCellValue("总成本");
                        row1.createCell(9).setCellValue("零售总金额");
                        row1.createCell(10).setCellValue("利润总额");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());

                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("depFatherId", departmentEntity.getGbDepartmentId());
//                            disGoodsMap.put("loss", 0);
                            Double aDoubleRL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(disGoodsMap);
                            Double aDoubleRP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(disGoodsMap);
                            Double aDoubleRW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(disGoodsMap);
                            Double aDoublePro = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(disGoodsMap);
                            Double aDoubleRS = gbDepGoodsDailyService.queryDepGoodsDailySalesSubtotal(disGoodsMap);
                            double total = aDoubleRP + aDoubleRL + aDoubleRW;

                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleRW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleRP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleRL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(8).setCellValue(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleRS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(10).setCellValue(new BigDecimal(aDoublePro).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                    }

                }
            }

        }

        return wb;
    }

    private HSSFWorkbook toCreatDepLossForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depFatherId", departmentEntity.getGbDepartmentId());
        map.put("loss", 0);
        System.out.println("mapamapapapExcelleleel" + map);
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map);

        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        System.out.println("roororor---------------------" + father.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("损耗成本");
                        row1.createCell(6).setCellValue("销售成本");
                        row1.createCell(7).setCellValue("废弃成本");
                        row1.createCell(8).setCellValue("总成本");
                        row1.createCell(9).setCellValue("零售总金额");
                        row1.createCell(10).setCellValue("利润总额");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());

                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("depFatherId", departmentEntity.getGbDepartmentId());
//                            disGoodsMap.put("loss", 0);
                            Double aDoubleRL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(disGoodsMap);
                            Double aDoubleRP = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(disGoodsMap);
                            Double aDoubleRW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(disGoodsMap);
                            Double aDoublePro = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(disGoodsMap);
                            Double aDoubleRS = gbDepGoodsDailyService.queryDepGoodsDailySalesSubtotal(disGoodsMap);
                            double total = aDoubleRP + aDoubleRL + aDoubleRW;

                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleRL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleRP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleRW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(8).setCellValue(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleRS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(10).setCellValue(new BigDecimal(aDoublePro).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                    }

                }
            }

        }

        return wb;
    }


    //
    private HSSFWorkbook toCreatNxDisPurchaseCostForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject((Integer.valueOf(reportEntity.getGbRepIds())));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("nxDisId", nxDistributerEntity.getNxDistributerId());
        map.put("dayuStatus", -1);
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        System.out.println("roororor---------------------" + father.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("成本总量");
                        row1.createCell(6).setCellValue("成本总金额");
                        row1.createCell(7).setCellValue("库存总量");
                        row1.createCell(8).setCellValue("库存总金额");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());

                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("nxDisId", nxDistributerEntity.getNxDistributerId());
                            Double aDoubleR = gbDepGoodsStockService.queryDepStockWeightTotal(disGoodsMap);
                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            Double aDoubleRT = gbDepGoodsStockService.queryDepGoodsSubtotal(disGoodsMap);
                            goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleRT).setScale(1, BigDecimal.ROUND_HALF_UP).toString());


                            Double aDoubleS = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            Double aDoubleST = gbDepGoodsStockService.queryDepGoodsRestTotal(disGoodsMap);
                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleST).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                    }

                }
            }

        }

        return wb;
    }


    private HSSFWorkbook toCreatDisPurchaseCostFormPur(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("fromDepId", departmentEntity.getGbDepartmentId());
//        map.put("stockId", -1);
        System.out.println("purrururmap" + map);
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        System.out.println("roororor---------------------" + father.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("采购总量");
                        row1.createCell(6).setCellValue("采购总金额");
                        row1.createCell(7).setCellValue("平均单价");
                        row1.createCell(8).setCellValue("最高单价");
                        row1.createCell(9).setCellValue("最低单价");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("fromDepId", departmentEntity.getGbDepartmentId());
                            Integer integer = gbDepGoodsStockService.queryGoodsStockCount(disGoodsMap);
                            if (integer > 0) {
                                goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                                goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                                Double aDoubleR = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(disGoodsMap);
                                goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                Double aDoubleRT = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(disGoodsMap);
                                goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleRT).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                double v = aDoubleRT / aDoubleR;
                                goodsRow.createCell(7).setCellValue(new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                                String s = gbDistributerPurchaseGoodsService.queryPurGoodsMaxPrice(disGoodsMap);
                                goodsRow.createCell(8).setCellValue(new BigDecimal(s).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                String aDoubleST = gbDistributerPurchaseGoodsService.queryPurGoodsMinPrice(disGoodsMap);
                                goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleST).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            }

                        }
                    }

                }
            }

        }

        return wb;
    }

    private HSSFWorkbook toCreatDisPurchaseCostForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("fromDepId", departmentEntity.getGbDepartmentId());
        map.put("notEqualStockId", -1);
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        System.out.println("roororor---------------------" + father.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("采购总量");
                        row1.createCell(6).setCellValue("采购总金额");
                        row1.createCell(7).setCellValue("平均单价");
                        row1.createCell(8).setCellValue("最高单价");
                        row1.createCell(9).setCellValue("最低单价");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("depFatherId", departmentEntity.getGbDepartmentId());
                            Integer integer = gbDepGoodsStockService.queryGoodsStockCount(disGoodsMap);
                            if (integer > 0) {
                                goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                                goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                                Double aDoubleR = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(disGoodsMap);
                                goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                Double aDoubleRT = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(disGoodsMap);
                                goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleRT).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                double v = aDoubleRT / aDoubleR;
                                goodsRow.createCell(7).setCellValue(new BigDecimal(v).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                                String s = gbDistributerPurchaseGoodsService.queryPurGoodsMaxPrice(disGoodsMap);
                                goodsRow.createCell(8).setCellValue(new BigDecimal(s).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                String aDoubleST = gbDistributerPurchaseGoodsService.queryPurGoodsMinPrice(disGoodsMap);
                                goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleST).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            }

                        }
                    }

                }
            }

        }

        return wb;
    }

    private HSSFWorkbook toCreatDepCostForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depFatherId", departmentEntity.getGbDepartmentId());
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        System.out.println("roororor---------------------" + father.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("成本数量");
                        row1.createCell(6).setCellValue("销售数量");
                        row1.createCell(7).setCellValue("损耗数量");
                        row1.createCell(8).setCellValue("废弃数量");
                        row1.createCell(9).setCellValue("退货数量");
                        row1.createCell(10).setCellValue("总成本");
                        row1.createCell(11).setCellValue("销售成本");
                        row1.createCell(12).setCellValue("损耗成本");
                        row1.createCell(13).setCellValue("废弃成本");
                        row1.createCell(14).setCellValue("退货成本");
                        row1.createCell(15).setCellValue("库存数量");
                        row1.createCell(16).setCellValue("库存成本");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());

                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("depFatherId", departmentEntity.getGbDepartmentId());

                            Double aDoubleRT = 0.0;
                            Double aDoubleRTV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                            Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerProduce > 0) {
                                aDoubleRT = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(disGoodsMap);
                                aDoubleRTV = gbDepartmentStockReduceService.queryReduceProduceTotal(disGoodsMap);
                            }
                            goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleRT).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(11).setCellValue(new BigDecimal(aDoubleRTV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleS = 0.0;
                            Double aDoubleSV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                            Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerLoss > 0) {
                                aDoubleS = gbDepartmentStockReduceService.queryReduceLossWeightTotal(disGoodsMap);
                                aDoubleSV = gbDepartmentStockReduceService.queryReduceLossTotal(disGoodsMap);
                            }
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(12).setCellValue(new BigDecimal(aDoubleSV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleST = 0.0;
                            Double aDoubleSTV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                            Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerWaste > 0) {
                                aDoubleST = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(disGoodsMap);
                                aDoubleSTV = gbDepartmentStockReduceService.queryReduceWasteTotal(disGoodsMap);
                            }
                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleST).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(13).setCellValue(new BigDecimal(aDoubleSTV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleRTW = 0.0;
                            Double aDoubleRTWV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                            Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerReturn > 0) {
                                aDoubleRTW = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(disGoodsMap);
                                aDoubleRTWV = gbDepartmentStockReduceService.queryReduceReturnTotal(disGoodsMap);
                            }
                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleRTW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(14).setCellValue(new BigDecimal(aDoubleRTWV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            double aDoubleRV = aDoubleRTV + aDoubleSV + aDoubleSTV;
                            double aDoubleR = aDoubleRT + aDoubleS + aDoubleST;
                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(10).setCellValue(new BigDecimal(aDoubleRV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleRRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                            goodsRow.createCell(15).setCellValue(new BigDecimal(aDoubleRRest).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            Double aDoubleRRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(disGoodsMap);
                            goodsRow.createCell(16).setCellValue(new BigDecimal(aDoubleRRestV).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                    }

                }
            }

        }

        return wb;
    }

    private HSSFWorkbook toCreatDepStockNowForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depFatherId", departmentEntity.getGbDepartmentId());
        map.put("restWeight", 0);
        System.out.println("mapamapapapExcelleleel" + map);
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        System.out.println("roororor---------------------" + father.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("库存总量");
                        row1.createCell(6).setCellValue("库存总金额");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());

                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("depFatherId", departmentEntity.getGbDepartmentId());
                            disGoodsMap.put("restWeight", 0);
                            Double aDoubleR = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            Double aDoubleRT = gbDepGoodsStockService.queryDepGoodsRestTotal(disGoodsMap);
                            goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleRT).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                    }

                }
            }

        }

        return wb;
    }


    private HSSFWorkbook toCreatDepBusinessForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depFatherId", departmentEntity.getGbDepartmentId());
        System.out.println("mapamapapapExcelleleelFormmmmm" + map);
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());

                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        System.out.println("roororor---------------------" + father.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("库存总量");
                        row1.createCell(6).setCellValue("库存总金额");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        System.out.println("sisiisissizesizes========" + goodsEntities.size());
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());

                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("depFatherId", departmentEntity.getGbDepartmentId());

                            Double aDoubleR = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            Double aDoubleRT = gbDepGoodsStockService.queryDepGoodsRestTotal(disGoodsMap);
                            goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleRT).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                    }

                }
            }

        }

        return wb;
    }


    private HSSFWorkbook toCreatDepProfitForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depFatherId", departmentEntity.getGbDepartmentId());
        System.out.println("mapamapapapExcelleleel" + map);
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        HSSFSheet sheet = wb.createSheet(reportEntity.getGbRepStartDate() + "-" + reportEntity.getGbRepStopDate());
        HSSFRow row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("序号");
        row0.createCell(1).setCellValue("净利润(元)");
        row0.createCell(2).setCellValue("净利润百分比");
        row0.createCell(3).setCellValue("销售利润(元)");
        row0.createCell(4).setCellValue("销售利润百分比");
        row0.createCell(5).setCellValue("损耗成本(元)");
        row0.createCell(6).setCellValue("损耗百分比");
        row0.createCell(7).setCellValue("废弃成本(元)");
        row0.createCell(8).setCellValue("废弃成本百分比");
        row0.createCell(9).setCellValue("总成本(元)");

        HSSFRow row10 = sheet.createRow(sheet.getLastRowNum() + 1);
        row10.createCell(0).setCellValue(sheet.getLastRowNum());
        //00Cost
        Double aDoubleT = gbDepGoodsDailyService.queryDepGoodsDailySubtotal(map); //送货金额
        double aDoubleReT = gbDepGoodsDailyService.queryDepGoodsDailyReturnSubtotal(map); //退货金额
        BigDecimal costTotalT = new BigDecimal(aDoubleT).subtract(new BigDecimal(aDoubleReT)).setScale(1, BigDecimal.ROUND_HALF_UP);

        Double profitSubtotal = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(map);
        BigDecimal dividePT = new BigDecimal(profitSubtotal).divide(costTotalT, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
        row10.createCell(1).setCellValue(new BigDecimal(profitSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        row10.createCell(2).setCellValue(dividePT.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");

        Double fatherDoubleProduce = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(map);
        BigDecimal dividePTS = new BigDecimal(fatherDoubleProduce).divide(costTotalT, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
        row10.createCell(3).setCellValue(new BigDecimal(fatherDoubleProduce).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        row10.createCell(4).setCellValue(dividePTS.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");

        Double fatherDoubleWaste = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
        BigDecimal dividePTW = new BigDecimal(fatherDoubleWaste).divide(costTotalT, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
        row10.createCell(5).setCellValue(new BigDecimal(fatherDoubleWaste).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        row10.createCell(6).setCellValue(dividePTW.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");

        Double fatherDoubleLoss = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
        BigDecimal dividePTL = new BigDecimal(fatherDoubleLoss).divide(costTotalT, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
        row10.createCell(7).setCellValue(new BigDecimal(fatherDoubleLoss).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        row10.createCell(8).setCellValue(dividePTL.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");
        row10.createCell(9).setCellValue(costTotalT.toString());

        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        System.out.println("roororor---------------------" + father.getGbDfgFatherGoodsName());
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("净利润");
                        row1.createCell(6).setCellValue("净利润百分比");
                        row1.createCell(7).setCellValue("销售利润");
                        row1.createCell(8).setCellValue("销售利润百分比");
                        row1.createCell(9).setCellValue("损耗金额");
                        row1.createCell(10).setCellValue("损耗百分比");
                        row1.createCell(11).setCellValue("废弃金额");
                        row1.createCell(12).setCellValue("废弃百分比");
                        row1.createCell(13).setCellValue("库存金额");
                        row1.createCell(14).setCellValue("总成本");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("depFatherId", departmentEntity.getGbDepartmentId());

                            //00Cost
                            Double aDouble = gbDepGoodsDailyService.queryDepGoodsDailySubtotal(disGoodsMap); //送货金额
                            double aDoubleRe = gbDepGoodsDailyService.queryDepGoodsDailyReturnSubtotal(disGoodsMap); //退货金额
                            BigDecimal costTotal = new BigDecimal(aDouble).subtract(new BigDecimal(aDoubleRe));

                            //6 净利润
                            Double aDoubleProfit = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(disGoodsMap);
                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleProfit).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            BigDecimal divideP = new BigDecimal(aDoubleProfit).divide(costTotal, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                            goodsRow.createCell(6).setCellValue(divideP.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");

                            //7 销售利润
                            Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(disGoodsMap);
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            BigDecimal divideS = new BigDecimal(aDoubleProfit).divide(costTotal, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                            goodsRow.createCell(8).setCellValue(divideS.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");

                            //损耗金额
                            Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(disGoodsMap);
                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            BigDecimal divideL = new BigDecimal(aDoubleL).divide(costTotal, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                            goodsRow.createCell(10).setCellValue(divideL.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");

                            //废弃金额
                            Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(disGoodsMap);
                            goodsRow.createCell(11).setCellValue(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            BigDecimal divideW = new BigDecimal(aDoubleW).divide(costTotal, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                            goodsRow.createCell(12).setCellValue(divideW.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");

                            //库存金额
                            double aDoubleR = gbDepGoodsStockService.queryDepGoodsRestTotal(disGoodsMap);
                            goodsRow.createCell(13).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            //总成本
                            goodsRow.createCell(14).setCellValue(costTotal.setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                    }

                }
            }

        }

        return wb;
    }


    private HSSFWorkbook toCreatNxDisOutWeightAndSubtotalForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        String gbRepIds = reportEntity.getGbRepIds();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("nxDisId", gbRepIds);
        System.out.println("nxdidiid" + map);
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("总数量");
                        row1.createCell(6).setCellValue("销售总量");
                        row1.createCell(7).setCellValue("损耗总量");
                        row1.createCell(8).setCellValue("废弃总量");
                        row1.createCell(9).setCellValue("库存总量");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        System.out.println("sisiisissizesizes========" + goodsEntities.size());
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("nxDisId", gbRepIds);

                            //6 totalWeight
                            Double aDoubleP = 0.0;
                            map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                            Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(map);
                            if (integerProduce > 0) {
                                aDoubleP = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(map);
                            }
                            goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleL = 0.0;
                            map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                            Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(map);
                            if (integerLoss > 0) {
                                aDoubleL = gbDepartmentStockReduceService.queryReduceLossWeightTotal(map);
                            }
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleW = 0.0;
                            map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                            Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(map);
                            if (integerWaste > 0) {
                                aDoubleW = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(map);
                            }
                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());


                            Double aDoubleR = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            double aDouble = aDoubleP + aDoubleW + aDoubleL + aDoubleR;
                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                    }

                }
            }

        }

        return wb;
    }

//
//    private HSSFWorkbook toCreatDisOutSubtotalForm(GbReportEntity reportEntity) {
//        HSSFWorkbook wb = new HSSFWorkbook();
//        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
//        Map<String, Object> map = new HashMap<>();
//        map.put("startDate", reportEntity.getGbRepStartDate());
//        map.put("stopDate", reportEntity.getGbRepStopDate());
//        map.put("fromDepId", departmentEntity.getGbDepartmentId());
//        TreeSet<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);
//
//        if (distributerFatherGoodsEntities.size() > 0) {
//            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
//                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
//                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
//                    //设置表头
//                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
//                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
//                        //设置表头
//                        HSSFRow row1 = sheet.createRow(0);
//                        row1.createCell(0).setCellValue("序号");
//                        row1.createCell(1).setCellValue("商品名称");
//                        row1.createCell(2).setCellValue("规格");
//                        row1.createCell(3).setCellValue("品牌");
//                        row1.createCell(4).setCellValue("详细");
//                        row1.createCell(5).setCellValue("总金额");
//                        row1.createCell(6).setCellValue("销售金额");
//                        row1.createCell(7).setCellValue("损耗金额");
//                        row1.createCell(8).setCellValue("废弃金额");
//                        row1.createCell(9).setCellValue("库存金额");
//
//                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
//                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
//                        System.out.println("sisiisissizesizes========" + goodsEntities.size());
//                        //设置表体
//                        HSSFRow goodsRow = null;
//                        for (int i = 0; i < goodsEntities.size(); i++) {
//                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
//                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
//                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
//                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
//                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
//                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
//                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
//                            //5 totalWeight
//                            Map<String, Object> disGoodsMap = new HashMap<>();
//                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
//                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
//                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
//                            disGoodsMap.put("fromDepId", departmentEntity.getGbDepartmentId());
//
//                            //6 totalWeight
//                            Double aDoubleP = gbDepGoodsStockService.queryDepStockProduceSubtotal(disGoodsMap);
//                            goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                            Double aDoubleL = gbDepGoodsStockService.queryDepStockLossSubtotal(disGoodsMap);
//                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                            Double aDoubleW = gbDepGoodsStockService.queryDepStockWasteSubtotal(disGoodsMap);
//                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//
//                            Double aDoubleR = gbDepGoodsStockService.queryDepStockRestSubtotal(disGoodsMap);
//                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                            double aDouble = aDoubleP + aDoubleW + aDoubleL + aDoubleR;
//                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
//
//                        }
//                    }
//
//                }
//            }
//
//        }
//
//        return wb;
//    }

    private HSSFWorkbook toCreatDisOutWeightFormPur(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("fromDepId", departmentEntity.getGbDepartmentId());
        map.put("stockId", -1);
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);
        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("总数量");
                        row1.createCell(6).setCellValue("销售总量");
                        row1.createCell(7).setCellValue("损耗总量");
                        row1.createCell(8).setCellValue("废弃总量");
                        row1.createCell(9).setCellValue("退货总量");
                        row1.createCell(10).setCellValue("库存总量");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        System.out.println("sisiisissizesizes========" + goodsEntities.size());
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("fromDepId", departmentEntity.getGbDepartmentId());
                            disGoodsMap.put("stockId", -1);

                            //6 totalWeight
                            Double aDoubleP = 0.0;
                            map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                            Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(map);
                            if (integerProduce > 0) {
                                aDoubleP = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(map);
                            }
                            goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleL = 0.0;
                            map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                            Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(map);
                            if (integerLoss > 0) {
                                aDoubleL = gbDepartmentStockReduceService.queryReduceLossWeightTotal(map);
                            }
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleW = 0.0;
                            map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                            Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(map);
                            if (integerWaste > 0) {
                                aDoubleW = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(map);
                            }
                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleRET = 0.0;
                            map.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                            System.out.println("tuihguodmap" + map);
                            Integer integerReturn = gbDepGoodsStockService.queryGoodsStockCount(map);
                            if (integerReturn > 0) {
                                aDoubleRET = gbDepGoodsStockService.queryDepStockReturnWeightTotal(map);
                            }
                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleRET).setScale(1, BigDecimal.ROUND_HALF_UP).toString());


                            Double aDoubleR = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                            goodsRow.createCell(10).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            double aDouble = gbDepGoodsStockService.queryDepStockWeightTotal(disGoodsMap);

                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                    }

                }
            }

        }

        return wb;
    }

    private HSSFWorkbook toCreatDisOutWeightForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("fromDepId", departmentEntity.getGbDepartmentId());
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);
        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("进货数量");
                        row1.createCell(6).setCellValue("进货金额");
                        row1.createCell(7).setCellValue("出货数量");
                        row1.createCell(8).setCellValue("出货金额");
                        row1.createCell(9).setCellValue("库存数量");
                        row1.createCell(10).setCellValue("库存金额");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                            //5 totalWeight
                            Map<String, Object> disGoodsMapP = new HashMap<>();
                            disGoodsMapP.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMapP.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMapP.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMapP.put("depId", departmentEntity.getGbDepartmentId());

                            //5 pur
                            Double aDoubleP = 0.0;
                            Double aDoublePS = 0.0;
                            Integer integers = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(disGoodsMapP);
                            if (integers > 0) {
                                aDoubleP = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(disGoodsMapP);
                                aDoublePS = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(disGoodsMapP);
                            }
                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(6).setCellValue(new BigDecimal(aDoublePS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());


                            //6 totalWeight
                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("fromDepId", departmentEntity.getGbDepartmentId());
                            Double aDoubleW = 0.0;
                            Double aDoubleSub = 0.0;
                            Integer integer = gbDepGoodsStockService.queryGoodsStockCount(disGoodsMap);
                            if (integer > 0) {
                                aDoubleW = gbDepGoodsStockService.queryDepStockWeightTotal(disGoodsMap);
                                aDoubleSub = gbDepGoodsStockService.queryDepStockSubtotal(disGoodsMap);
                            }
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleSub).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            //5 totalWeight
                            Map<String, Object> disGoodsMapSt = new HashMap<>();
                            disGoodsMapSt.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMapSt.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMapSt.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMapSt.put("depFatherId", departmentEntity.getGbDepartmentId());
                            Double aDoubleWS = 0.0;
                            Double aDoubleSubS = 0.0;
                            Integer integerS = gbDepGoodsStockService.queryGoodsStockCount(disGoodsMapSt);
                            if (integerS > 0) {
                                aDoubleWS = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMapSt);
                                aDoubleSubS = gbDepGoodsStockService.queryDepStockRestSubtotal(disGoodsMapSt);
                            }
                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleWS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            goodsRow.createCell(10).setCellValue(new BigDecimal(aDoubleSubS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());


                        }
                    }

                }
            }

        }

        return wb;
    }


    private HSSFWorkbook toCreatDepParameter(GbReportEntity reportEntity) {
        Integer gbRepDisUserId = reportEntity.getGbRepDisUserId();
        GbDistributerUserEntity gbDistributerUserEntity = gbDistributerUserService.queryUserInfo(gbRepDisUserId);
        Integer diuDistributerId = gbDistributerUserEntity.getGbDiuDistributerId();
        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disId", diuDistributerId);
        mapG.put("fresh", 1);

        HSSFWorkbook wb = new HSSFWorkbook();
        List<GbDistributerGoodsEntity> distributerGoodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapG);

        if (distributerGoodsEntities.size() > 0) {
            for (GbDistributerGoodsEntity distributerGoodsEntity : distributerGoodsEntities) {

                GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
                Map<String, Object> map = new HashMap<>();
                map.put("startDate", reportEntity.getGbRepStartDate());
                map.put("stopDate", reportEntity.getGbRepStopDate());
                map.put("depFatherId", departmentEntity.getGbDepartmentId());
                map.put("disGoodsId", distributerGoodsEntity.getGbDistributerGoodsId());
                map.put("produce", 0);
                List<GbDepartmentGoodsDailyEntity> departmentGoodsDailyEntities = gbDepGoodsDailyService.queryDepGoodsDailyListByParams(map);
                if (departmentGoodsDailyEntities.size() > 0) {
                    HSSFSheet sheet = wb.createSheet(distributerGoodsEntity.getGbDgGoodsName());
                    //设置表头
                    //设置表头
                    HSSFRow row1 = sheet.createRow(0);
                    row1.createCell(0).setCellValue("序号");
                    row1.createCell(1).setCellValue("日期");
                    row1.createCell(2).setCellValue("陈货数量");
                    row1.createCell(3).setCellValue("销售数量");
                    row1.createCell(4).setCellValue("日鲜率");
                    row1.createCell(5).setCellValue("损耗数量");
                    row1.createCell(6).setCellValue("废弃总量");
                    row1.createCell(7).setCellValue("退货总量");
                    row1.createCell(8).setCellValue("进货总量");
                    row1.createCell(9).setCellValue("剩余总量");
                    row1.createCell(10).setCellValue("沽清时间");

                    //设置表体
                    HSSFRow goodsRow = null;
                    String standardname = distributerGoodsEntity.getGbDgGoodsStandardname();
                    for (int i = 0; i < departmentGoodsDailyEntities.size(); i++) {
                        GbDepartmentGoodsDailyEntity dailyEntity = departmentGoodsDailyEntities.get(i);
                        goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                        goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                        goodsRow.createCell(1).setCellValue(dailyEntity.getGbDgdDate());
                        goodsRow.createCell(2).setCellValue(dailyEntity.getGbDgdLastWeight() + standardname);
                        goodsRow.createCell(3).setCellValue(dailyEntity.getGbDgdProduceWeight() + standardname);
                        goodsRow.createCell(4).setCellValue(dailyEntity.getGbDgdFreshRate() + "%");
                        goodsRow.createCell(5).setCellValue(dailyEntity.getGbDgdLossWeight() + standardname);
                        goodsRow.createCell(6).setCellValue(dailyEntity.getGbDgdWasteWeight() + standardname);
                        goodsRow.createCell(7).setCellValue(dailyEntity.getGbDgdReturnWeight() + standardname);
                        goodsRow.createCell(8).setCellValue(dailyEntity.getGbDgdWeight() + standardname);
                        goodsRow.createCell(9).setCellValue(dailyEntity.getGbDgdRestWeight() + standardname);
                        goodsRow.createCell(10).setCellValue(dailyEntity.getGbDgdSellClearHour() + ":" + dailyEntity.getGbDgdSellClearMinute());

                    }

                }

            }

        }


        return wb;
    }


    private HSSFWorkbook toCreatDepSalesSubtotalForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depFatherId", departmentEntity.getGbDepartmentId());

        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsDailyService.queryDepDailyGoodsFatherTypeByParams(map);
        if (distributerFatherGoodsEntities.size() > 0) {
            //总表
            //todo
            HSSFSheet sheet0 = wb.createSheet(reportEntity.getGbRepStartDate() + "-" + reportEntity.getGbRepStopDate());
            HSSFRow row0 = sheet0.createRow(0);
            row0.createCell(0).setCellValue("");
            row0.createCell(1).setCellValue("金额");
            row0.createCell(2).setCellValue("销售百分比");

            Map<String, Object> disGoodsMap = new HashMap<>();
            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
            disGoodsMap.put("depFatherId", departmentEntity.getGbDepartmentId());

            HSSFRow salesRow = null;
            salesRow = sheet0.createRow(sheet0.getLastRowNum() + 1);
            salesRow.createCell(0).setCellValue("销售额");
            Double weightPTSales = gbDepGoodsDailyService.queryDepGoodsDailySalesSubtotal(disGoodsMap);
            salesRow.createCell(1).setCellValue(new BigDecimal(weightPTSales).setScale(1, BigDecimal.ROUND_HALF_UP).toString() + "元");

            HSSFRow lossRow = null;
            lossRow = sheet0.createRow(sheet0.getLastRowNum() + 1);
            lossRow.createCell(0).setCellValue("损耗成本");
            Double weightLossT = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(disGoodsMap);
            lossRow.createCell(1).setCellValue(new BigDecimal(weightLossT).setScale(1, BigDecimal.ROUND_HALF_UP).toString() + "元");
            BigDecimal decimal = new BigDecimal(weightLossT).divide(new BigDecimal(weightPTSales), 3, BigDecimal.ROUND_HALF_UP);
            BigDecimal decimal1 = decimal.multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
            lossRow.createCell(2).setCellValue(decimal1.toString() + "%");

            HSSFRow wasteRow = null;
            wasteRow = sheet0.createRow(sheet0.getLastRowNum() + 1);
            wasteRow.createCell(0).setCellValue("废弃成本");
            Double weightPT = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(disGoodsMap);
            wasteRow.createCell(1).setCellValue(new BigDecimal(weightPT).setScale(1, BigDecimal.ROUND_HALF_UP).toString() + "元");
            BigDecimal decimalW = new BigDecimal(weightPT).divide(new BigDecimal(weightPTSales), 3, BigDecimal.ROUND_HALF_UP);
            BigDecimal decimal1W = decimalW.multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
            wasteRow.createCell(2).setCellValue(decimal1W.toString() + "%");

            HSSFRow profitRow = null;
            profitRow = sheet0.createRow(sheet0.getLastRowNum() + 1);
            profitRow.createCell(0).setCellValue("销售利润");
            Double weightPTProfit = gbDepGoodsDailyService.queryDepGoodsDailyProfitSubtotal(disGoodsMap);
            profitRow.createCell(1).setCellValue(new BigDecimal(weightPTProfit).setScale(1, BigDecimal.ROUND_HALF_UP).toString() + "元");
            BigDecimal decimalP = new BigDecimal(weightPTProfit).divide(new BigDecimal(weightPTSales), 3, BigDecimal.ROUND_HALF_UP);
            BigDecimal decimal1P = decimalP.multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
            profitRow.createCell(2).setCellValue(decimal1P.toString() + "%");

            HSSFRow profitRowBet = null;
            profitRowBet = sheet0.createRow(sheet0.getLastRowNum() + 1);
            profitRowBet.createCell(0).setCellValue("纯利润");
            Double weightPTProfitBet = gbDepGoodsDailyService.queryDepGoodsDailySalesProfitSubtotal(disGoodsMap);
            profitRowBet.createCell(1).setCellValue(new BigDecimal(weightPTProfitBet).setScale(1, BigDecimal.ROUND_HALF_UP).toString() + "元");
            BigDecimal decimalWAP = new BigDecimal(weightPTProfitBet).divide(new BigDecimal(weightPTSales), 3, BigDecimal.ROUND_HALF_UP);
            BigDecimal decimal1WAP = decimalWAP.multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
            profitRowBet.createCell(2).setCellValue(decimal1WAP.toString() + "%");


            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("入库数量");
                        row1.createCell(6).setCellValue("销售数量");
                        row1.createCell(7).setCellValue("销售额");
                        row1.createCell(8).setCellValue("损耗数量");
                        row1.createCell(9).setCellValue("损耗成本");
                        row1.createCell(10).setCellValue("废弃数量");
                        row1.createCell(11).setCellValue("废弃成本");
                        row1.createCell(12).setCellValue("退货数量");
                        row1.createCell(13).setCellValue("退货成本");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        TreeSet<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepGoodsDailyService.queryDepDisGoodsTreeByParams(map);

                        //设置表体
                        HSSFRow goodsRow = null;
                        int count = 0;
                        for (GbDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                            System.out.println("-----=====--------" + departmentDisGoodsEntity.getGbDistributerGoodsEntity());
                            count = count + 1;
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            System.out.println(" nanfdmafdnaf " + departmentDisGoodsEntity.getGbDistributerGoodsEntity().getGbDgGoodsName());
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(departmentDisGoodsEntity.getGbDistributerGoodsEntity().getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(departmentDisGoodsEntity.getGbDistributerGoodsEntity().getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(departmentDisGoodsEntity.getGbDistributerGoodsEntity().getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(departmentDisGoodsEntity.getGbDistributerGoodsEntity().getGbDgGoodsDetail());

                            disGoodsMap.put("disGoodsId", departmentDisGoodsEntity.getGbDdgDisGoodsId());
                            //5 totalWeight
                            Double weight = gbDepGoodsDailyService.queryDepGoodsDailyWeight(disGoodsMap);
                            goodsRow.createCell(5).setCellValue(new BigDecimal(weight).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            //6 totalWeight
                            Double weightP = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(disGoodsMap);
                            goodsRow.createCell(6).setCellValue(new BigDecimal(weightP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailySalesSubtotal(disGoodsMap);
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(disGoodsMap);
                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleLS = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(disGoodsMap);
                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleLS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(disGoodsMap);
                            goodsRow.createCell(10).setCellValue(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            Double aDoubleLWS = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(disGoodsMap);
                            goodsRow.createCell(11).setCellValue(new BigDecimal(aDoubleLWS).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleR = gbDepGoodsDailyService.queryDepGoodsDailyReturnWeight(disGoodsMap);
                            goodsRow.createCell(12).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                            Double aDoubleLWRW = gbDepGoodsDailyService.queryDepGoodsDailyReturnSubtotal(disGoodsMap);
                            goodsRow.createCell(13).setCellValue(new BigDecimal(aDoubleLWRW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                    }

                }
            }
        }

        return wb;
    }

    private HSSFWorkbook toCreatDepSalesWeightForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depFatherId", departmentEntity.getGbDepartmentId());
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        if (distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    HSSFSheet sheet = wb.createSheet(grand.getGbDfgFatherGoodsName());
                    //设置表头
                    List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = grand.getFatherGoodsEntities();
                    for (GbDistributerFatherGoodsEntity father : fatherGoodsEntities) {
                        //设置表头
                        HSSFRow row1 = sheet.createRow(0);
                        row1.createCell(0).setCellValue("序号");
                        row1.createCell(1).setCellValue("商品名称");
                        row1.createCell(2).setCellValue("规格");
                        row1.createCell(3).setCellValue("品牌");
                        row1.createCell(4).setCellValue("详细");
                        row1.createCell(5).setCellValue("总数量");
                        row1.createCell(6).setCellValue("销售总量");
                        row1.createCell(7).setCellValue("损耗总量");
                        row1.createCell(8).setCellValue("废弃总量");
                        row1.createCell(9).setCellValue("剩余总量");

                        map.put("disGoodsFatherId", father.getGbDistributerFatherGoodsId());
                        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                        System.out.println("sisiisissizesizes========" + goodsEntities.size());
                        //设置表体
                        HSSFRow goodsRow = null;
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("depFatherId", departmentEntity.getGbDepartmentId());

                            //6 totalWeight
                            Double aDoubleP = gbDepGoodsDailyService.queryDepGoodsDailyProduceWeight(disGoodsMap);
                            goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleL = gbDepGoodsDailyService.queryDepGoodsDailyLossWeight(disGoodsMap);
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleL).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleW = gbDepGoodsDailyService.queryDepGoodsDailyWasteWeight(disGoodsMap);
                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleW).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            double aDouble = aDoubleP + aDoubleW + aDoubleL;
                            goodsRow.createCell(5).setCellValue(new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                            Double aDoubleR = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleR).setScale(1, BigDecimal.ROUND_HALF_UP).toString());

                        }
                    }

                }
            }

        }

        return wb;
    }


}

package com.nongxinle.controller;

/**
 * @author lpy
 * @date 09-26 20:05
 */

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.*;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private GbDistributerService gbDistributerService;
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
    private GbDistributerPurchaseBatchService gbDistributerPurchaseBatchService;
    @Autowired
    private GbDepartmentGoodsStockReduceService gbDepartmentStockReduceService;
    @Autowired
    private GbDepartmentBillService gbDepartmentBillService;
    @Autowired
    private NxJrdhSupplierService jrdhSupplierService;
    @Autowired
    private GbDepartmentUserService gbDepartmentUserService;


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


//    @RequestMapping(value = "/getDisUserReports")
//    @ResponseBody
//    public R getDisUserReports(Integer userId) {
//
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("userId", userId);
//        List<GbReportEntity> reportEntities = gbReportService.queryReportList(map1);
//        List<Map<String, Object>> resultList = new ArrayList<>();
//        for (GbReportEntity report : reportEntities) {
//            String gbRepType = report.getGbRepType();
//
//            if (gbRepType.equals("disControlPrice")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDisControlGoodsPriceTotal(map);
//                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
//                GbDepartmentEntity fatherGoodsEntity = gbDepartmentService.queryObject(gbRepIds);
//                stringObjectMap.put("name", fatherGoodsEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "价控商品统计");
//                resultList.add(stringObjectMap);
//            }
//
//
//            if (gbRepType.equals("nxDisPurchaseCost")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("nxDisId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaNxDisPurchaseCost(map);
//                NxDistributerEntity departmentEntity = nxDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getNxDistributerName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "门店成本统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("disPurchaseCost")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("fromDepId", report.getGbRepIds());
//                map.put("depFatherIdNotEqual", report.getGbRepIds());
//                map.put("purDepId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                map.put("notEqualStockId", -1);
//                Map<String, Object> stringObjectMap = aaaDisPurchaseCost(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "门店成本统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("disPurchaseCostPur")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("fromDepId", report.getGbRepIds());
//                map.put("depFatherIdNotEqual", report.getGbRepIds());
//                map.put("purDepId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                map.put("stockId", -1);
//                Map<String, Object> stringObjectMap = aaaDisPurchaseCost(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "集采门店成本统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("nxDisOutGoodsFresh")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("nxDisId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaNxDisOutGoodsFreshTotal(map);
//                NxDistributerEntity departmentEntity = nxDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getNxDistributerName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "保鲜商品统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("disOutGoodsFresh")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("toDepId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDisOutGoodsFreshTotal(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "保鲜商品统计");
//                resultList.add(stringObjectMap);
//            }
//
//
//            if (gbRepType.equals("disOutWeightAndSubtotal")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("fromDepId", report.getGbRepIds());
//                map.put("depFatherIdNotEqual", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
////                map.put("notEqualStockId", -1);
//                Map<String, Object> stringObjectMap = aaaDisOutWeightAndSubtotal(map);
//
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "出货商品统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("disOutWeightAndSubtotalPur")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("fromDepId", report.getGbRepIds());
//                map.put("depFatherIdNotEqual", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                map.put("stockId", -1);
//                System.out.println("zhbeebebbeebbeb" + map);
//                Map<String, Object> stringObjectMap = aaaDisOutWeightAndSubtotal(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "集采出货商品统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("nxDisOutWeightAndSubtotal")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("nxDisId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaNxDisOutWeightAndSubtotalTotal(map);
//
//                NxDistributerEntity nxDistributerEntity = nxDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", nxDistributerEntity.getNxDistributerName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "出货商品统计");
//                resultList.add(stringObjectMap);
//            }
//
//            if (gbRepType.equals("depSalesSubtotal")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depFatherId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepSalesSubtotalTotal(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "销售统计");
//                resultList.add(stringObjectMap);
//            }
//
//            if (gbRepType.equals("depSalesWeight")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depFatherId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepSalesWeightTotal(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "销售统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("depParameter")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depFatherId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                Map<String, Object> stringObjectMap = aaaDepParameterTotal(map, departmentEntity.getGbDepartmentDisId());
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "日鲜率统计");
//                resultList.add(stringObjectMap);
//            }
//
//            if (gbRepType.equals("depProfit")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depFatherId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepProfitTotal(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "利润统计");
//                resultList.add(stringObjectMap);
//            }
//
//            if (gbRepType.equals("depBusiness")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depFatherId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepBusinessData(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "部门商品");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("subDepStockNow")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depId", report.getGbRepIds());
//                System.out.println("suussuuss" + map);
//                Map<String, Object> stringObjectMap = aaaDepStockTotalNow(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "库存商品统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("depStockNow")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depFatherId", report.getGbRepIds());
//                Map<String, Object> stringObjectMap = aaaDepStockTotalNow(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "库存商品统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("disStockNow")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disId", report.getGbRepIds());
//                Map<String, Object> stringObjectMap = aaaDepStockTotalNow(map);
//                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", gbDistributerEntity.getGbDistributerName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "库存商品统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("subDepCost")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepCost(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "门店成本统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("depCost")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depFatherId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepCost(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "门店成本统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("disCost")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepCost(map);
//                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", gbDistributerEntity.getGbDistributerName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "门店成本统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("disPurGoods")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepPurGoods(map);
//                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", gbDistributerEntity.getGbDistributerName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "采购商品统计");
//                resultList.add(stringObjectMap);
//            }
//
//            if (gbRepType.equals("disPurSupplier")) {
//                System.out.println("disPurSupplierdisPurSupplier");
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepPurGoodsSupplier(map);
//                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", gbDistributerEntity.getGbDistributerName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "采购供货商统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("purDepUser")) {
//                System.out.println("purDepUserpurDepUserpurDepUser");
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("purUserId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDisPurUser(map);
//                GbDepartmentUserEntity departmentUserEntity = gbDepartmentUserService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentUserEntity.getGbDuWxNickName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "采购员统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("disPurSelf")) {
//                System.out.println("disPurSelfdisPurSelf");
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepPurGoodsSelf(map);
//                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", gbDistributerEntity.getGbDistributerName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "自采统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("disPurNxDistributer")) {
//                System.out.println("disPurNxDistributer");
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("nxDisId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepPurGoodsNxdistributer(map);
//                NxDistributerEntity gbDistributerEntity = nxDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", gbDistributerEntity.getNxDistributerName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "京京采购统计");
//                resultList.add(stringObjectMap);
//            }
//
//            if (gbRepType.equals("disLoss")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepLoss(map);
//                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", gbDistributerEntity.getGbDistributerName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "商品损耗");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("depLoss")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depFatherId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepLoss(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "商品损耗");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("disWaste")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepWaste(map);
//                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", gbDistributerEntity.getGbDistributerName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "商品废弃");
//                resultList.add(stringObjectMap);
//            }
//
//            if (gbRepType.equals("depWaste")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depFatherId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepWaste(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "商品废弃");
//                resultList.add(stringObjectMap);
//            }
//
//            if (gbRepType.equals("disReturn")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepReturn(map);
//                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", gbDistributerEntity.getGbDistributerName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "商品退货");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("depReturn")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("depFatherId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaDepReturn(map);
//                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
//                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "商品退货");
//                resultList.add(stringObjectMap);
//            }
//
//
//            if (gbRepType.equals("goodsSales")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disGoodsGrandId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaGoodsSalesTotal(map);
//                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
//                GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(gbRepIds);
//                stringObjectMap.put("name", fatherGoodsEntity.getGbDfgFatherGoodsName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "商品销售统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("goodsProfit")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disGoodsGrandId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaGoodsProfitTotal(map);
//                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
//                GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(gbRepIds);
//                stringObjectMap.put("name", fatherGoodsEntity.getGbDfgFatherGoodsName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "商品利润统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("goodsCost")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disGoodsGrandId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaGoodsCostTotal(map);
//                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
//                GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(gbRepIds);
//                stringObjectMap.put("name", fatherGoodsEntity.getGbDfgFatherGoodsName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "商品成本统计");
//                resultList.add(stringObjectMap);
//            }
//
//            if (gbRepType.equals("goodsFresh")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disGoodsGrandId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                Map<String, Object> stringObjectMap = aaaGoodsFreshTotal(map);
//                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
//                GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(gbRepIds);
//                stringObjectMap.put("name", fatherGoodsEntity.getGbDfgFatherGoodsName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "保鲜商品统计");
//                resultList.add(stringObjectMap);
//            }
//            if (gbRepType.equals("goodsPrice")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disGoodsGrandId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                System.out.println("zahuidhsishisdifas" + map);
//                Map<String, Object> stringObjectMap = aaaGoodsPriceTotal(map);
//                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
//                GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(gbRepIds);
//                stringObjectMap.put("name", fatherGoodsEntity.getGbDfgFatherGoodsName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "价控商品统计");
//                resultList.add(stringObjectMap);
//            }
//
//            if (gbRepType.equals("goodsByDepartment")) {
//                //获取表数据
//                Map<String, Object> map = new HashMap<>();
//                map.put("disGoodsGrandId", report.getGbRepIds());
//                map.put("startDate", report.getGbRepStartDate());
//                map.put("stopDate", report.getGbRepStopDate());
//                System.out.println("zahuidhsishisdifas" + map);
//                Map<String, Object> stringObjectMap = aaaGoodsCostTotalByDepartment(map);
//                Integer gbRepIds = Integer.valueOf(report.getGbRepIds());
//                GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsService.queryObject(gbRepIds);
//                stringObjectMap.put("name", fatherGoodsEntity.getGbDfgFatherGoodsName());
//                stringObjectMap.put("report", report);
//                stringObjectMap.put("type", "部门商品统计");
//                resultList.add(stringObjectMap);
//            }
//        }
//
//        return R.ok().put("data", resultList);
//
//    }

    @RequestMapping(value = "/getDisUserReportsPurchase/{userId}")
    @ResponseBody
    public R getDisUserReportsPurchase(@PathVariable Integer userId) {

        Map<String, Object> map1 = new HashMap<>();
        map1.put("userId", userId);
        String typesStr = "purSupplier,purDepUser";
        map1.put("types", Arrays.asList(typesStr.split(",")));
        System.out.println("mapapapappa" + map1);
        List<GbReportEntity> reportEntities = gbReportService.queryReportList(map1);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (GbReportEntity report : reportEntities) {
            String gbRepType = report.getGbRepType();
            if (gbRepType.equals("purSupplier")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("supplierId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = bbbDisPurSupplier(map);
                NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", supplierEntity.getNxJrdhsSupplierName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "供货商采购统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("purDepUser")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("purUserId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = bbbDisPurUser(map);
                GbDepartmentUserEntity departmentUserEntity = gbDepartmentUserService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentUserEntity.getGbDuWxNickName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "采购员统计");
                resultList.add(stringObjectMap);
            }
        }
        return R.ok().put("data", resultList);

    }


    @RequestMapping(value = "/getDisUserReportsCost/{userId}")
    @ResponseBody
    public R getDisUserReportsCost(@PathVariable Integer userId) {

        Map<String, Object> map1 = new HashMap<>();
        map1.put("userId", userId);
        String typesStr = "disCost,subDepCost";
        map1.put("types", Arrays.asList(typesStr.split(",")));
        System.out.println("mapapapappa" + map1);
        List<GbReportEntity> reportEntities = gbReportService.queryReportList(map1);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (GbReportEntity report : reportEntities) {
            String gbRepType = report.getGbRepType();
            if (gbRepType.equals("disCost")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("disId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = bbbDisCost(map);
                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", gbDistributerEntity.getGbDistributerName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "门店成本统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("subDepCost")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = bbbSubDepCost(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "部门成本统计");
                resultList.add(stringObjectMap);
            }
        }
        return R.ok().put("data", resultList);

    }


    @RequestMapping(value = "/getDisUserReportsBusiness/{userId}")
    @ResponseBody
    public R getDisUserReportsBusiness(@PathVariable Integer userId) {

        Map<String, Object> map1 = new HashMap<>();
        map1.put("userId", userId);
        String typesStr = "disBusiness,subDepBusiness";
        map1.put("types", Arrays.asList(typesStr.split(",")));
        System.out.println("mapapapappaBUsinesss" + map1);
        List<GbReportEntity> reportEntities = gbReportService.queryReportList(map1);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (GbReportEntity report : reportEntities) {
            String gbRepType = report.getGbRepType();
            if (gbRepType.equals("disBusiness")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("disId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = bbbDisBusiness(map);
                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", gbDistributerEntity.getGbDistributerName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "门店成本统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("subDepBusiness")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depId", report.getGbRepIds());
                map.put("startDate", report.getGbRepStartDate());
                map.put("stopDate", report.getGbRepStopDate());
                Map<String, Object> stringObjectMap = bbbSubDepBusiness(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "部门成本统计");
                resultList.add(stringObjectMap);
            }
        }
        return R.ok().put("data", resultList);

    }

    @RequestMapping(value = "/getDisUserReportsStock/{userId}")
    @ResponseBody
    public R getDisUserReportsStock(@PathVariable Integer userId) {

        Map<String, Object> map1 = new HashMap<>();
        map1.put("userId", userId);
        String typesStr = "disStockNow,subDepStockNow";
        map1.put("types", Arrays.asList(typesStr.split(",")));
        System.out.println("mapapapappa" + map1);
        List<GbReportEntity> reportEntities = gbReportService.queryReportList(map1);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (GbReportEntity report : reportEntities) {
            String gbRepType = report.getGbRepType();
            if (gbRepType.equals("disStockNow")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("disId", report.getGbRepIds());
                Map<String, Object> stringObjectMap = aaaDepStockTotalNow(map);
                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", gbDistributerEntity.getGbDistributerName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "库存商品统计");
                resultList.add(stringObjectMap);
            }
            if (gbRepType.equals("subDepStockNow")) {
                //获取表数据
                Map<String, Object> map = new HashMap<>();
                map.put("depId", report.getGbRepIds());
                Map<String, Object> stringObjectMap = aaaSubDepStockTotalNow(map);
                GbDepartmentEntity departmentEntity = gbDepartmentService.queryObject(Integer.valueOf(report.getGbRepIds()));
                stringObjectMap.put("name", departmentEntity.getGbDepartmentName());
                stringObjectMap.put("report", report);
                stringObjectMap.put("type", "库存商品统计");
                resultList.add(stringObjectMap);
            }
        }
        return R.ok().put("data", resultList);

    }


    private Map<String, Object> bbbDisCost(Map<String, Object> map) {

        map.put("isGroup", 0);
        System.out.println("niamamamammamamCCCCC" + map);
        List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentService.queryDepsByDisId(map);


        Map<String, Object> mapResult = new HashMap<>();

        map.put("dayuStatus", -1);

        Integer stockCount = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (stockCount > 0) {

            System.out.println("depcosoostot" + map);
            double doutbleSubtotal = 0;
            double doutbleLossV = 0;
            double doutbleWasteV = 0;
            double doutbleProduceV = 0;
            double doutbleReturnV = 0;

            map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
            Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(map);
            if (integerProduce > 0) {
                doutbleProduceV = gbDepartmentStockReduceService.queryReduceProduceTotal(map);
                doutbleSubtotal = doutbleSubtotal + doutbleProduceV;
            } else {
                doutbleProduceV = 0;
            }
            map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
            Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(map);
            if (integerLoss > 0) {
                doutbleLossV = gbDepartmentStockReduceService.queryReduceLossTotal(map);
                doutbleSubtotal = doutbleSubtotal + doutbleLossV;
            } else {
                doutbleLossV = 0;
            }
            map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
            Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(map);
            if (integerWaste > 0) {
                doutbleWasteV = gbDepartmentStockReduceService.queryReduceWasteTotal(map);
                doutbleSubtotal = doutbleSubtotal + doutbleWasteV;
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
            mapResult.put("perCost", new BigDecimal(v).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalSubtotal", new BigDecimal(doutbleSubtotal).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalCost", new BigDecimal(costTotal).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalProduceSubtotal", new BigDecimal(doutbleProduceV).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalReturnSubtotal", new BigDecimal(doutbleReturnV).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalLossSubtotal", new BigDecimal(doutbleLossV).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalWasteSubtotal", new BigDecimal(doutbleWasteV).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalRestSubtotal", new BigDecimal(doutbleRestV).setScale(1, RoundingMode.HALF_UP).toString());

            List<GbDistributerFatherGoodsEntity> greatGrandFatherGoods = new ArrayList<>();
            System.out.println("44444depdididiid" + map);
            Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(map);

            if (integer > 0) {

                greatGrandFatherGoods = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);
                for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandFatherGoods) {
                    double greatGrandTotalCost = 0;
                    double greatGrandTotalCostV = 0;
                    map.put("disGoodsGreatId", greatGrandFather.getGbDistributerFatherGoodsId());
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

                    greatGrandTotalCostV = doutbleProduceWeightDep + doutbleLossWeightDep + doutbleWasteWeightDep;
                    greatGrandTotalCost = doutbleProduceVDep + doutbleLossVDep + doutbleWasteVDep;

                    greatGrandFather.setFatherCostWeightString(new BigDecimal(greatGrandTotalCostV).setScale(2, RoundingMode.HALF_UP).toString());
                    greatGrandFather.setFatherCostSubtotalString(new BigDecimal(greatGrandTotalCost).setScale(2, RoundingMode.HALF_UP).toString());
                    BigDecimal decimal = new BigDecimal(0);
                    if (costTotal > 0) {
                        decimal = new BigDecimal(greatGrandTotalCost).divide(new BigDecimal(costTotal), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
                    }
                    greatGrandFather.setFatherCostSubtotalPercentString(decimal.toString());

                }

                if (gbDepartmentEntities.size() > 1) {
                    Double doutbleCostDis = 0.0;
                    for (GbDepartmentEntity gbDepartmentEntity : gbDepartmentEntities) {
                        Map<String, Object> mapDep = new HashMap<>();
                        mapDep.put("depId", gbDepartmentEntity.getGbDepartmentId());

                        Double doutbleProduceVDep = 0.0;
                        Double doutbleLossVDep = 0.0;
                        Double doutbleWasteVDep = 0.0;
                        Double doutbleCostDep = 0.0;
                        mapDep.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                        System.out.println("coprororo" + map);
                        Integer integerProduceDep = gbDepartmentStockReduceService.queryReduceTypeCount(mapDep);
                        if (integerProduceDep > 0) {
                            doutbleProduceVDep = gbDepartmentStockReduceService.queryReduceProduceTotal(mapDep);
                        }

                        mapDep.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                        System.out.println("coprororo" + map);
                        Integer integerLossDep = gbDepartmentStockReduceService.queryReduceTypeCount(mapDep);
                        if (integerLossDep > 0) {
                            doutbleLossVDep = gbDepartmentStockReduceService.queryReduceLossTotal(mapDep);
                        }
                        mapDep.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                        System.out.println("coprororo" + map);
                        Integer integerWasteDep = gbDepartmentStockReduceService.queryReduceTypeCount(mapDep);
                        if (integerWasteDep > 0) {
                            doutbleWasteVDep = gbDepartmentStockReduceService.queryReduceWasteTotal(mapDep);
                        }
                        doutbleCostDep = doutbleProduceVDep + doutbleLossVDep + doutbleWasteVDep;
                        doutbleCostDis = doutbleCostDis + doutbleCostDep;
                        gbDepartmentEntity.setDepProduceGoodsTotalString(new BigDecimal(doutbleProduceVDep).setScale(1, RoundingMode.HALF_UP).toString());
                        gbDepartmentEntity.setDepLossGoodsTotalString(new BigDecimal(doutbleLossVDep).setScale(1, RoundingMode.HALF_UP).toString());
                        gbDepartmentEntity.setDepWasteGoodsTotalString(new BigDecimal(doutbleWasteVDep).setScale(1, RoundingMode.HALF_UP).toString());
                        gbDepartmentEntity.setDepCostGoodsTotalString(new BigDecimal(doutbleCostDep).setScale(1, RoundingMode.HALF_UP).toString());

                    }
                }

                mapResult.put("arr", greatGrandFatherGoods);
                mapResult.put("depArr", gbDepartmentEntities);
                mapResult.put("code", 0);
            } else {
                mapResult.put("code", -1);
            }

        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }

    private Map<String, Object> bbbSubDepCost(Map<String, Object> map) {

        Map<String, Object> mapResult = new HashMap<>();
        map.put("dayuStatus", -1);
        Integer stockCount = gbDepartmentStockReduceService.queryReduceTypeCount(map);
        if (stockCount > 0) {
            System.out.println("depcosoostot" + map);
            double doutbleSubtotal = 0;
            double doutbleLossV = 0;
            double doutbleWasteV = 0;
            double doutbleProduceV = 0;
            double doutbleReturnV = 0;

            map.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
            Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(map);
            if (integerProduce > 0) {
                doutbleProduceV = gbDepartmentStockReduceService.queryReduceProduceTotal(map);
                doutbleSubtotal = doutbleSubtotal + doutbleProduceV;
            } else {
                doutbleProduceV = 0;
            }
            map.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
            Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(map);
            if (integerLoss > 0) {
                doutbleLossV = gbDepartmentStockReduceService.queryReduceLossTotal(map);
                doutbleSubtotal = doutbleSubtotal + doutbleLossV;
            } else {
                doutbleLossV = 0;
            }
            map.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
            Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(map);
            if (integerWaste > 0) {
                doutbleWasteV = gbDepartmentStockReduceService.queryReduceWasteTotal(map);
                doutbleSubtotal = doutbleSubtotal + doutbleWasteV;
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
            mapResult.put("perCost", new BigDecimal(v).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalSubtotal", new BigDecimal(doutbleSubtotal).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalCost", new BigDecimal(costTotal).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalProduceSubtotal", new BigDecimal(doutbleProduceV).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalReturnSubtotal", new BigDecimal(doutbleReturnV).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalLossSubtotal", new BigDecimal(doutbleLossV).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalWasteSubtotal", new BigDecimal(doutbleWasteV).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalRestSubtotal", new BigDecimal(doutbleRestV).setScale(1, RoundingMode.HALF_UP).toString());

            List<GbDistributerFatherGoodsEntity> greatGrandFatherGoods = new ArrayList<>();
            map.put("equalType", null);
            System.out.println("44444depdididiidsub" + map);
            Integer integer = gbDepartmentStockReduceService.queryReduceTypeCount(map);

            if (integer > 0) {

                greatGrandFatherGoods = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);
                for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandFatherGoods) {
                    double greatGrandTotalCost = 0;
                    double greatGrandTotalCostV = 0;
                    map.put("disGoodsGreatId", greatGrandFather.getGbDistributerFatherGoodsId());
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

                    greatGrandTotalCostV = doutbleProduceWeightDep + doutbleLossWeightDep + doutbleWasteWeightDep;
                    greatGrandTotalCost = doutbleProduceVDep + doutbleLossVDep + doutbleWasteVDep;

                    greatGrandFather.setFatherCostWeightString(new BigDecimal(greatGrandTotalCostV).setScale(2, RoundingMode.HALF_UP).toString());
                    greatGrandFather.setFatherCostSubtotalString(new BigDecimal(greatGrandTotalCost).setScale(2, RoundingMode.HALF_UP).toString());
                    BigDecimal decimal = new BigDecimal(0);
                    if (costTotal > 0) {
                        decimal = new BigDecimal(greatGrandTotalCost).divide(new BigDecimal(costTotal), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
                    }
                    greatGrandFather.setFatherCostSubtotalPercentString(decimal.toString());
                }
                mapResult.put("arr", greatGrandFatherGoods);
                mapResult.put("code", 0);
            } else {
                mapResult.put("code", -1);
            }

        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }

    private Map<String, Object> bbbDisPurUser(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        map.put("dayuStatus", 1);
        Integer stockCount = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(map);
        if (stockCount > 0) {
            System.out.println("depcosoostotsuppppooliiieieiriri" + map);
            System.out.println("suplieriirpurrr" + map);
            double supplierSubtotal = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(map);

            Object startDdate = map.get("startDate");
            Object stopDate = map.get("stopDate");
            Integer howManyDaysInPeriod = getHowManyDaysInPeriod((String) stopDate, (String) startDdate);

            double v = supplierSubtotal / (howManyDaysInPeriod + 1);
            mapResult.put("perCost", new BigDecimal(v).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalSubtotal", new BigDecimal(supplierSubtotal).setScale(1, RoundingMode.HALF_UP).toString());
            map.put("typeNotEqual", 9);
            map.put("supplierBuy", -1);
            map.put("dayuStatus", 2);
            map.put("offset", 0);
            map.put("limit", 100);
            List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDistributerPurchaseGoodsService.queryDisTreeGoodsWithPurList(map);
            mapResult.put("arr", gbDistributerGoodsEntities);
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }


        return mapResult;
    }

    private Map<String, Object> bbbDisPurSupplier(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        map.put("dayuStatus", 1);
        Integer stockCount = gbDistributerPurchaseBatchService.queryDisPurchaseBatchCount(map);
        if (stockCount > 0) {
            double supplierSubtotal = 0;
            double tuihuoSubtotal = 0;
            map.put("notEqualPurchaseType", 9);
            System.out.println("suplieriirpurrr22222" + map);
            int count = gbDistributerPurchaseBatchService.queryDisPurchaseBatchCount(map);
            if (count > 0) {
                supplierSubtotal = gbDistributerPurchaseBatchService.querySupplierUnSettleSubtotal(map);
            }

            map.put("notEqualPurchaseType", null);
            map.put("purchaseType", 9);
            System.out.println("suplieriirpurrr333333" + map);
            int tuihuoCount = gbDistributerPurchaseBatchService.queryDisPurchaseBatchCount(map);
            if (tuihuoCount > 0) {
                tuihuoSubtotal = gbDistributerPurchaseBatchService.querySupplierUnSettleSubtotal(map);
            }
            double v1 = supplierSubtotal - tuihuoSubtotal;

            Object startDdate = map.get("startDate");
            Object stopDate = map.get("stopDate");
            Integer howManyDaysInPeriod = getHowManyDaysInPeriod((String) stopDate, (String) startDdate);

            double v = v1 / (howManyDaysInPeriod + 1);
            mapResult.put("perCost", new BigDecimal(v).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("allTotalSubtotal", new BigDecimal(supplierSubtotal).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("tuihuoSubtotal", new BigDecimal(tuihuoSubtotal).setScale(1, RoundingMode.HALF_UP).toString());

            double unPaySupplierTotal = 0;
            double unPayTuihuoTotal = 0;
            double havePaySupplierTotalPay = 0;
            double havePayTuihuoTotalPay = 0;
            map.put("status", 4);
            map.put("purchaseType", null);
            map.put("notEqualPurchaseType", 9);
            Integer integer = gbDistributerPurchaseBatchService.queryDisPurchaseBatchCount(map);
            if (integer > 0) {
                unPaySupplierTotal = gbDistributerPurchaseBatchService.querySupplierUnSettleSubtotal(map);
            }
            map.put("purchaseType", 9);
            map.put("notEqualPurchaseType", null);
            Integer integerTui = gbDistributerPurchaseBatchService.queryDisPurchaseBatchCount(map);
            if (integerTui > 0) {
                unPayTuihuoTotal = gbDistributerPurchaseBatchService.querySupplierUnSettleSubtotal(map);
            }

            map.put("status", null);
            map.put("equalStatus", 4);
            Integer integer2 = gbDistributerPurchaseBatchService.queryDisPurchaseBatchCount(map);
            if (integer2 > 0) {
                havePaySupplierTotalPay = gbDistributerPurchaseBatchService.querySupplierUnSettleSubtotal(map);
            }

            double v2 = unPaySupplierTotal - unPayTuihuoTotal;

            mapResult.put("unPaySubtotal", new BigDecimal(v2).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("havePaySubtotal", new BigDecimal(havePaySupplierTotalPay).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }


        return mapResult;
    }

    private Map<String, Object> aaaDepStockTotalNow(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();
        map.put("isGroup", 0);
        System.out.println("niamamamammamam" + map);
        List<GbDepartmentEntity> gbDepartmentEntities = gbDepartmentService.queryDepsByDisId(map);

        map.put("dayuStatus", -1);
        map.put("restWeight", 0);
        System.out.println("sotodiididnaoodosoaaaDepStockTotalNow" + map);
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
                    Integer gbDistributerFatherGoodsId = grandFather.getGbDistributerFatherGoodsId();
                    map.put("disGoodsGrandId", gbDistributerFatherGoodsId);
                    Double fatherDoubleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
                    Double fatherDoubleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
                    grandDoubleRestV = grandDoubleRestV + fatherDoubleRestV;
                    greatGrandTotalRestV = greatGrandTotalRestV + fatherDoubleRestV;

                    grandDoubleRest = grandDoubleRest + fatherDoubleRest;
                    greatGrandTotalRest = greatGrandTotalRest + fatherDoubleRest;
                    grandFather.setFatherRestWeightTotalString(new BigDecimal(grandDoubleRest).setScale(1, RoundingMode.HALF_UP).toString());
                    grandFather.setFatherRestTotalString(new BigDecimal(grandDoubleRestV).setScale(1, RoundingMode.HALF_UP).toString());
                    resultFatherGoodsList.add(grandFather);
                }
                greatGrandFather.setFatherRestWeightTotalString(new BigDecimal(greatGrandTotalRestV).setScale(2, RoundingMode.HALF_UP).toString());
                greatGrandFather.setFatherRestTotalString(new BigDecimal(greatGrandTotalRest).setScale(2, RoundingMode.HALF_UP).toString());


                doutbleRest = doutbleRest + greatGrandTotalRest;
                doutbleRestV = doutbleRestV + greatGrandTotalRestV;

            }

            if (gbDepartmentEntities.size() > 1) {
                for (GbDepartmentEntity gbDepartmentEntity : gbDepartmentEntities) {
                    map.put("depId", gbDepartmentEntity.getGbDepartmentId());
                    map.put("disGoodsGrandId", null);
                    System.out.println("couanmapa[pa" + map);
                    int count = gbDepGoodsStockService.queryGoodsStockCount(map);
                    Double fatherDoubleRest = 0.0;
                    Double fatherDoubleRestV = 0.0;
                    if (count > 0) {
                        fatherDoubleRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
                        fatherDoubleRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
                    }

                    gbDepartmentEntity.setDepStockSubtotalString(new BigDecimal(fatherDoubleRestV).setScale(1, RoundingMode.HALF_UP).toString());
                    gbDepartmentEntity.setDepStockWeightTotalString(new BigDecimal(fatherDoubleRest).setScale(1, RoundingMode.HALF_UP).toString());
                }
            }
            //分店总成本
            mapResult.put("depArr", gbDepartmentEntities);
            mapResult.put("arr", resultFatherGoodsList);
            mapResult.put("totalRest", new BigDecimal(doutbleRestV).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalRestWeight", new BigDecimal(doutbleRest).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }

    private Map<String, Object> aaaSubDepStockTotalNow(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();
        map.put("dayuStatus", -1);
        map.put("restWeight", 0);
        System.out.println("sotodiididnaoodosoaaaSuubsubDepStockTotalNow" + map);
        List<GbDistributerFatherGoodsEntity> greatGrandFatherGoods = new ArrayList<>();
        double doutbleRest = 0;
        double doutbleRestV = 0;
        Integer integer = gbDepGoodsStockService.queryGoodsStockCount(map);
        if (integer > 0) {
            greatGrandFatherGoods = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);
            for (GbDistributerFatherGoodsEntity greatGrandFather : greatGrandFatherGoods) {
                map.put("disGoodsGreatId", greatGrandFather.getGbDistributerFatherGoodsId());
                double greatGrandTotalRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(map);
                double greatGrandTotalRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
                greatGrandFather.setFatherRestWeightTotalString(new BigDecimal(greatGrandTotalRest).setScale(2, RoundingMode.HALF_UP).toString());
                greatGrandFather.setFatherRestTotalString(new BigDecimal(greatGrandTotalRestV).setScale(2, RoundingMode.HALF_UP).toString());

                doutbleRest = doutbleRest + greatGrandTotalRest;
                doutbleRestV = doutbleRestV + greatGrandTotalRestV;

            }


            //分店总成本
            mapResult.put("arr", greatGrandFatherGoods);
            mapResult.put("totalRest", new BigDecimal(doutbleRestV).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalRestWeight", new BigDecimal(doutbleRest).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }

    private Map<String, Object> bbbSubDepBusiness(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        Object startDate = map.get("startDate");
        Object stopDate = map.get("stopDate");

        map.put("dayuStatus", -1);

        Integer integer = gbDepGoodsStockService.queryDisStockGoodsCount(map);
        System.out.println("depBusinessssssss");

        if (integer > 0) {

            //dis采购
            double purchaseTotal = 0.0;
            Integer stockCount = gbDepGoodsStockService.queryGoodsStockCount(map);
            if (stockCount > 0) {
                purchaseTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map);
            }

            //dis 支出
            double doutbleCost = 0;
            double doutbleProduce = 0;
            double doutbleWaste = 0;
            double doutbleLoss = 0;
            double doutbleReturn = 0;

            doutbleProduce = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
            doutbleLoss = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
            doutbleWaste = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
            doutbleCost = doutbleProduce + doutbleLoss + doutbleWaste;

            //dis 本期库存
            Double aDouble = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
            map.put("startDate", null);
            map.put("stopDate", null);
            // dis 总库存
            Double aDoubleAll = gbDepGoodsStockService.queryDepGoodsRestTotal(map);

            double producePercent = doutbleProduce / doutbleCost * 100;
            double lossPercent = doutbleLoss / doutbleCost * 100;
            double wastePercent = doutbleWaste / doutbleCost * 100;

            //dis退货
            doutbleReturn = gbDepGoodsDailyService.queryDepGoodsDailyReturnSubtotal(map);


            // 本期支出
            double costTotalPur = 0.0;
            map.put("startPurchaseDate", startDate);
            map.put("stopDate", stopDate);
            System.out.println("mappouuuruu333" + map);
            Integer integer1 = gbDepartmentStockReduceService.queryReduceTypeCount(map);
            if(integer1 > 0){
                costTotalPur =  gbDepartmentStockReduceService.queryReduceCostSubtotal(map);
            }


            mapResult.put("purchaseTotal", new BigDecimal(purchaseTotal).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("costTotalPur", new BigDecimal(costTotalPur).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalCost", new BigDecimal(doutbleCost).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalProduce", new BigDecimal(doutbleProduce).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("producePercent", new BigDecimal(producePercent).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalWaste", new BigDecimal(doutbleWaste).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("wastePercent", new BigDecimal(wastePercent).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalLoss", new BigDecimal(doutbleLoss).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("lossPercent", new BigDecimal(lossPercent).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalReturn", new BigDecimal(doutbleReturn).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalRest", new BigDecimal(aDouble).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalRestAll", new BigDecimal(aDoubleAll).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("code", 0);
        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }

    private Map<String, Object> bbbDisBusiness(Map<String, Object> map) {
        Map<String, Object> mapResult = new HashMap<>();

        Object startDate = map.get("startDate");
        Object stopDate = map.get("stopDate");

        map.put("dayuStatus", -1);

        Integer integer = gbDepGoodsStockService.queryDisStockGoodsCount(map);
        Integer integer3 = gbDepGoodsDailyService.queryDepGoodsDailyCount(map);
        System.out.println("depBusinessssssss");

        if (integer > 0 || integer3 > 0) {

            //dis采购
            double purchaseTotal = 0.0;
            Integer stockCount = gbDepGoodsStockService.queryGoodsStockCount(map);
            if (stockCount > 0) {
                purchaseTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(map);
            }

            //dis 支出
            double doutbleCost = 0;
            double doutbleProduce = 0;
            double doutbleWaste = 0;
            double doutbleLoss = 0;
            //dis 退货
            double doutbleReturn = 0;
            // 本期退货
            double doutbleReturnPur = 0;
            //本期库存
            double aDoubleStockPur = 0.0;
            //总库存
            double aDoubleStock = 0.0;

            // 本期支出
            double costTotalPur = 0.0;

            doutbleProduce = gbDepGoodsDailyService.queryDepGoodsDailyProduceSubtotal(map);
            doutbleLoss = gbDepGoodsDailyService.queryDepGoodsDailyLossSubtotal(map);
            doutbleWaste = gbDepGoodsDailyService.queryDepGoodsDailyWasteSubtotal(map);
            doutbleCost = doutbleProduce + doutbleLoss + doutbleWaste;

            double producePercent = doutbleProduce / doutbleCost * 100;
            double lossPercent = doutbleLoss / doutbleCost * 100;
            double wastePercent = doutbleWaste / doutbleCost * 100;

            //dis总退货
            map.put("equalType", 4);
            System.out.println("dis总退货dis总退货" + map);
            Integer integer2 = gbDepartmentStockReduceService.queryReduceTypeCount(map);
            if(integer2 > 0){
                doutbleReturn = gbDepartmentStockReduceService.queryReduceReturnTotal(map);
            }

            // 本期退货
            map.put("startDate", null);
            map.put("startPurchaseDate", startDate);
            System.out.println("本期退货本期退货" + map);
            Integer integerPur = gbDepartmentStockReduceService.queryReduceTypeCount(map);
            if(integerPur > 0){
                doutbleReturnPur = gbDepartmentStockReduceService.queryReduceReturnTotal(map);
            }
            double lastReturn = doutbleReturn - doutbleReturnPur;

            //dis 本期库存

            System.out.println("本期库存本期库存" + map);
            map.put("equalType",null);
            map.put("startPurchaseDate",null);
            map.put("startDate",startDate);
            Integer stockCount1 = gbDepGoodsStockService.queryGoodsStockCount(map);
            if(stockCount1 > 0){
                aDoubleStockPur = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
            }

            map.put("startDate", null);
            map.put("stopDate", null);
            // dis 总库存
            Integer stockCountall = gbDepGoodsStockService.queryGoodsStockCount(map);
            if(stockCountall > 0){
                aDoubleStock = gbDepGoodsStockService.queryDepGoodsRestTotal(map);
            }
            double lastRest = aDoubleStock - aDoubleStockPur;

            map.put("startPurchaseDate", startDate);
            map.put("stopDate", stopDate);
            System.out.println("mappouuuruu333" + map);
            Integer integer1 = gbDepartmentStockReduceService.queryReduceTypeCount(map);
            if(integer1 > 0){
                costTotalPur =  gbDepartmentStockReduceService.queryReduceCostSubtotal(map);
            }

            double lastCost = doutbleCost - costTotalPur;

            mapResult.put("purchaseTotal", new BigDecimal(purchaseTotal).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("costTotalPur", new BigDecimal(costTotalPur).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalCost", new BigDecimal(doutbleCost).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("lastCost", new BigDecimal(lastCost).setScale(1, RoundingMode.HALF_UP).toString());

            mapResult.put("totalProduce", new BigDecimal(doutbleProduce).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("producePercent", new BigDecimal(producePercent).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalWaste", new BigDecimal(doutbleWaste).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("wastePercent", new BigDecimal(wastePercent).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalLoss", new BigDecimal(doutbleLoss).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("lossPercent", new BigDecimal(lossPercent).setScale(1, RoundingMode.HALF_UP).toString());

            mapResult.put("totalReturn", new BigDecimal(doutbleReturn).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalReturnPur", new BigDecimal(doutbleReturnPur).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("lastReturn", new BigDecimal(lastReturn).setScale(1, RoundingMode.HALF_UP).toString());

            mapResult.put("totalRest", new BigDecimal(aDoubleStockPur).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("totalRestAll", new BigDecimal(aDoubleStock).setScale(1, RoundingMode.HALF_UP).toString());
            mapResult.put("lastRest", new BigDecimal(lastRest).setScale(1, RoundingMode.HALF_UP).toString());

            mapResult.put("code", 0);

        } else {
            mapResult.put("code", -1);
        }

        return mapResult;
    }


    @RequestMapping("/downloadReportExcelGb")
    @ResponseBody
    public void downloadReportExcelGb(HttpServletResponse response, HttpServletRequest request) {
        System.out.println("=== 开始Excel下载流程 ===");
        String id = request.getParameter("id");
        System.out.println("请求时间: " + new java.util.Date());

        HSSFWorkbook wb = null;
        try {
            System.out.println("下载报表ID: " + id);

            GbReportEntity reportEntity = gbReportService.queryObject(Integer.valueOf(id));

            // 初始化workbook
            wb = new HSSFWorkbook();

            if (reportEntity.getGbRepType().equals("subDepStockNow")) {
                System.out.println("生成类型: subDepStockNow");
                wb = toCreatSubDepStockNowForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("disStockNow")) {
                System.out.println("生成类型: disStockNow");
                wb = toCreatDisStockNowForm(reportEntity);
            }

            if (reportEntity.getGbRepType().equals("depCost")) {
                System.out.println("生成类型: depCost");
                wb = toCreatDepCostForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("disCost")) {
                System.out.println("生成类型: disCost");
                wb = toCreatDisCostForm(reportEntity);
            }

            if (reportEntity.getGbRepType().equals("disBusiness")) {
                System.out.println("生成类型: disCost");
                wb = toCreatDisBusinessForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("subDepBusiness")) {
                System.out.println("生成类型: disCost");
                wb = toCreatSubDepBusinessForm(reportEntity);
            }

            if (reportEntity.getGbRepType().equals("purSupplier")) {
                System.out.println("生成类型: purSupplier");
                wb = toCreatSupplierPurGoodsForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("subDepCost")) {
                System.out.println("生成类型: subDepCost");
                wb = toCreatSubDepCostForm(reportEntity);
            }
            if (reportEntity.getGbRepType().equals("purDepUser")) {
                System.out.println("生成类型: purDepUser");
                wb = toCreatPurDepUserForm(reportEntity);
            }

            // 设置响应头
            String fileName = URLEncoder.encode("导出商品.xls", "UTF-8");
            System.out.println("设置文件名: " + fileName);

            // 设置正确的Content-Type
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            System.out.println("开始写入Excel数据到响应流...");
            wb.write(response.getOutputStream());
            System.out.println("Excel数据写入完成");

        } catch (Exception e) {
            System.err.println("Excel下载过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Excel文件生成失败");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            // 确保资源被正确释放
            if (wb != null) {
                try {
                    wb.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 辅助方法：安全格式化数字，避免除零和精度问题
    private String formatDecimal(Double value) {
        if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
            return "0.0";
        }
        return new BigDecimal(value).setScale(1, RoundingMode.HALF_UP).toString();
    }


    private HSSFWorkbook toCreatSupplierPurGoodsForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("supplierId", reportEntity.getGbRepIds());
        map.put("typeNotEqual", 9);
        map.put("dayuStatus", 2);
        // 调试信息
        sheetCreatPurchase(wb, map, reportEntity);
        return wb;

    }

    private HSSFWorkbook toCreatDisBusinessForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("disId", Integer.valueOf(reportEntity.getGbRepIds()));

        //第一个 sheet 是采购列表
        wb = sheetCreatPurchaseSingle(wb, map, reportEntity);

        //第二个 sheet 是支出列表，制作，损耗，废弃
        wb = sheetCreatCostSingle(wb, map, reportEntity);

        // 第三个sheet 是库存列表
        map.put("disGoodsGrandId", null);
        wb = sheetCreatStockSingle(wb, map, reportEntity);

        //第四个 sheet 是采购列表
        map.put("restWeight", null);
        wb = sheetCreatPurchaseTuihuoSingle(wb, map, reportEntity);

        return wb;
    }
    private HSSFWorkbook toCreatSubDepBusinessForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depId", Integer.valueOf(reportEntity.getGbRepIds()));

        //第一个 sheet 是采购列表
        wb = sheetCreatPurchaseSingle(wb, map, reportEntity);

        //第二个 sheet 是支出列表，制作，损耗，废弃
        wb = sheetCreatCostSingle(wb, map, reportEntity);

        // 第三个sheet 是库存列表
        map.put("disGoodsGrandId", null);
        wb = sheetCreatStockSingle(wb, map, reportEntity);

        //第四个 sheet 是采购列表
        System.out.println("tuithuiutiutit" );
        map.put("restWeight", null);
        wb = sheetCreatPurchaseTuihuoSingle(wb, map, reportEntity);

        return wb;
    }

    private HSSFWorkbook sheetCreatCost(HSSFWorkbook wb, Map<String, Object> map, GbReportEntity reportEntity) {
        System.out.println("creatCostSheetcreatCostSheet");
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        if (distributerFatherGoodsEntities != null && distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    String sheetName = grand.getGbDfgFatherGoodsName() != null ? grand.getGbDfgFatherGoodsName() : "未命名工作表";
                    if (sheetName.length() > 31) {
                        sheetName = sheetName.substring(0, 31);
                    }
                    HSSFSheet sheet = wb.createSheet(sheetName);
                    //设置表头
                    HSSFRow row1 = sheet.createRow(0);
                    row1.createCell(0).setCellValue("序号");
                    row1.createCell(1).setCellValue("商品名称");
                    row1.createCell(2).setCellValue("规格");
                    row1.createCell(3).setCellValue("品牌");
                    row1.createCell(4).setCellValue("详细");
                    row1.createCell(5).setCellValue("总成本数量");
                    row1.createCell(6).setCellValue("制作数量");
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

                    map.put("disGoodsGrandId", grand.getGbDistributerFatherGoodsId());
                    List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                    //设置表体
                    HSSFRow goodsRow = null;
                    if (goodsEntities != null && goodsEntities.size() > 0) {
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

                            Double aDoubleRT = 0.0;
                            Double aDoubleRTV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                            Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerProduce != null && integerProduce > 0) {
                                aDoubleRT = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(disGoodsMap);
                                aDoubleRTV = gbDepartmentStockReduceService.queryReduceProduceTotal(disGoodsMap);
                            }
                            goodsRow.createCell(6).setCellValue(formatDecimal(aDoubleRT));
                            goodsRow.createCell(11).setCellValue(new BigDecimal(aDoubleRTV).setScale(1, RoundingMode.HALF_UP).toString());

                            Double aDoubleS = 0.0;
                            Double aDoubleSV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                            Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerLoss > 0) {
                                aDoubleS = gbDepartmentStockReduceService.queryReduceLossWeightTotal(disGoodsMap);
                                aDoubleSV = gbDepartmentStockReduceService.queryReduceLossTotal(disGoodsMap);
                            }
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleS).setScale(1, RoundingMode.HALF_UP).toString());
                            goodsRow.createCell(12).setCellValue(new BigDecimal(aDoubleSV).setScale(1, RoundingMode.HALF_UP).toString());

                            Double aDoubleST = 0.0;
                            Double aDoubleSTV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                            Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerWaste > 0) {
                                aDoubleST = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(disGoodsMap);
                                aDoubleSTV = gbDepartmentStockReduceService.queryReduceWasteTotal(disGoodsMap);
                            }
                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleST).setScale(1, RoundingMode.HALF_UP).toString());
                            goodsRow.createCell(13).setCellValue(new BigDecimal(aDoubleSTV).setScale(1, RoundingMode.HALF_UP).toString());

                            Double aDoubleRTW = 0.0;
                            Double aDoubleRTWV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                            Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerReturn > 0) {
                                aDoubleRTW = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(disGoodsMap);
                                aDoubleRTWV = gbDepartmentStockReduceService.queryReduceReturnTotal(disGoodsMap);
                            }
                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleRTW).setScale(1, RoundingMode.HALF_UP).toString());
                            goodsRow.createCell(14).setCellValue(new BigDecimal(aDoubleRTWV).setScale(1, RoundingMode.HALF_UP).toString());

                            double aDoubleRV = aDoubleRTV + aDoubleSV + aDoubleSTV;
                            double aDoubleR = aDoubleRT + aDoubleS + aDoubleST;
                            goodsRow.createCell(5).setCellValue(formatDecimal(aDoubleR));
                            goodsRow.createCell(10).setCellValue(new BigDecimal(aDoubleRV).setScale(1, RoundingMode.HALF_UP).toString());

                            Double aDoubleRRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                            goodsRow.createCell(15).setCellValue(new BigDecimal(aDoubleRRest).setScale(1, RoundingMode.HALF_UP).toString());
                            Double aDoubleRRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(disGoodsMap);
                            goodsRow.createCell(16).setCellValue(new BigDecimal(aDoubleRRestV).setScale(1, RoundingMode.HALF_UP).toString());

                        }
                    }
                }
            }
        }


        return wb;

    }

    // 备份原始方法 - 为每个商品分类创建多个sheet
    private HSSFWorkbook sheetCreatCostOriginal(HSSFWorkbook wb, Map<String, Object> map, GbReportEntity reportEntity) {
        System.out.println("creatCostSheetcreatCostSheet");
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        if (distributerFatherGoodsEntities != null && distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    String sheetName = grand.getGbDfgFatherGoodsName() != null ? grand.getGbDfgFatherGoodsName() : "未命名工作表";
                    if (sheetName.length() > 31) {
                        sheetName = sheetName.substring(0, 31);
                    }
                    HSSFSheet sheet = wb.createSheet(sheetName);
                    //设置表头
                    HSSFRow row1 = sheet.createRow(0);
                    row1.createCell(0).setCellValue("序号");
                    row1.createCell(1).setCellValue("商品名称");
                    row1.createCell(2).setCellValue("规格");
                    row1.createCell(3).setCellValue("品牌");
                    row1.createCell(4).setCellValue("详细");
                    row1.createCell(5).setCellValue("总成本数量");
                    row1.createCell(6).setCellValue("制作数量");
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

                    map.put("disGoodsGrandId", grand.getGbDistributerFatherGoodsId());
                    List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                    //设置表体
                    HSSFRow goodsRow = null;
                    if (goodsEntities != null && goodsEntities.size() > 0) {
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

                            Double aDoubleRT = 0.0;
                            Double aDoubleRTV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                            Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerProduce != null && integerProduce > 0) {
                                aDoubleRT = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(disGoodsMap);
                                aDoubleRTV = gbDepartmentStockReduceService.queryReduceProduceTotal(disGoodsMap);
                            }
                            goodsRow.createCell(6).setCellValue(formatDecimal(aDoubleRT));
                            goodsRow.createCell(11).setCellValue(new BigDecimal(aDoubleRTV).setScale(1, RoundingMode.HALF_UP).toString());

                            Double aDoubleS = 0.0;
                            Double aDoubleSV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                            Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerLoss > 0) {
                                aDoubleS = gbDepartmentStockReduceService.queryReduceLossWeightTotal(disGoodsMap);
                                aDoubleSV = gbDepartmentStockReduceService.queryReduceLossTotal(disGoodsMap);
                            }
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleS).setScale(1, RoundingMode.HALF_UP).toString());
                            goodsRow.createCell(12).setCellValue(new BigDecimal(aDoubleSV).setScale(1, RoundingMode.HALF_UP).toString());

                            Double aDoubleST = 0.0;
                            Double aDoubleSTV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                            Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerWaste > 0) {
                                aDoubleST = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(disGoodsMap);
                                aDoubleSTV = gbDepartmentStockReduceService.queryReduceWasteTotal(disGoodsMap);
                            }
                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleST).setScale(1, RoundingMode.HALF_UP).toString());
                            goodsRow.createCell(13).setCellValue(new BigDecimal(aDoubleSTV).setScale(1, RoundingMode.HALF_UP).toString());

                            Double aDoubleRTW = 0.0;
                            Double aDoubleRTWV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                            Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerReturn > 0) {
                                aDoubleRTW = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(disGoodsMap);
                                aDoubleRTWV = gbDepartmentStockReduceService.queryReduceReturnTotal(disGoodsMap);
                            }
                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleRTW).setScale(1, RoundingMode.HALF_UP).toString());
                            goodsRow.createCell(14).setCellValue(new BigDecimal(aDoubleRTWV).setScale(1, RoundingMode.HALF_UP).toString());

                            double aDoubleRV = aDoubleRTV + aDoubleSV + aDoubleSTV;
                            double aDoubleR = aDoubleRT + aDoubleS + aDoubleST;
                            goodsRow.createCell(5).setCellValue(formatDecimal(aDoubleR));
                            goodsRow.createCell(10).setCellValue(new BigDecimal(aDoubleRV).setScale(1, RoundingMode.HALF_UP).toString());

                            Double aDoubleRRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                            goodsRow.createCell(15).setCellValue(new BigDecimal(aDoubleRRest).setScale(1, RoundingMode.HALF_UP).toString());
                            Double aDoubleRRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(disGoodsMap);
                            goodsRow.createCell(16).setCellValue(new BigDecimal(aDoubleRRestV).setScale(1, RoundingMode.HALF_UP).toString());

                        }
                    }
                }
            }
        }

        return wb;
    }

    // 新方法 - 创建单个统一的支出列表sheet
    private HSSFWorkbook sheetCreatCostSingle(HSSFWorkbook wb, Map<String, Object> map, GbReportEntity reportEntity) {
        System.out.println("creatCostSheetSingle - 创建单个支出列表sheet");
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        // 创建一个统一的支出列表sheet
        HSSFSheet sheet = wb.createSheet("支出列表");

        //设置表头
        HSSFRow row1 = sheet.createRow(0);
        row1.createCell(0).setCellValue("序号");
        row1.createCell(1).setCellValue("商品名称");
        row1.createCell(2).setCellValue("规格");
        row1.createCell(3).setCellValue("品牌");
        row1.createCell(4).setCellValue("详细");
        row1.createCell(5).setCellValue("总成本数量");
        row1.createCell(6).setCellValue("制作数量");
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

        int rowIndex = 1; // 从第2行开始填充数据

        if (distributerFatherGoodsEntities != null && distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    map.put("disGoodsGrandId", grand.getGbDistributerFatherGoodsId());
                    List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);

                    //设置表体
                    if (goodsEntities != null && goodsEntities.size() > 0) {
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            HSSFRow goodsRow = sheet.createRow(rowIndex++);
                            goodsRow.createCell(0).setCellValue(rowIndex - 1);
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());

                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());

                            Double aDoubleRT = 0.0;
                            Double aDoubleRTV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
                            Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerProduce != null && integerProduce > 0) {
                                aDoubleRT = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(disGoodsMap);
                                aDoubleRTV = gbDepartmentStockReduceService.queryReduceProduceTotal(disGoodsMap);
                            }
                            goodsRow.createCell(6).setCellValue(formatDecimal(aDoubleRT));
                            goodsRow.createCell(11).setCellValue(new BigDecimal(aDoubleRTV).setScale(1, RoundingMode.HALF_UP).toString());

                            Double aDoubleS = 0.0;
                            Double aDoubleSV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
                            Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerLoss > 0) {
                                aDoubleS = gbDepartmentStockReduceService.queryReduceLossWeightTotal(disGoodsMap);
                                aDoubleSV = gbDepartmentStockReduceService.queryReduceLossTotal(disGoodsMap);
                            }
                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleS).setScale(1, RoundingMode.HALF_UP).toString());
                            goodsRow.createCell(12).setCellValue(new BigDecimal(aDoubleSV).setScale(1, RoundingMode.HALF_UP).toString());

                            Double aDoubleST = 0.0;
                            Double aDoubleSTV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
                            Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerWaste > 0) {
                                aDoubleST = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(disGoodsMap);
                                aDoubleSTV = gbDepartmentStockReduceService.queryReduceWasteTotal(disGoodsMap);
                            }
                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleST).setScale(1, RoundingMode.HALF_UP).toString());
                            goodsRow.createCell(13).setCellValue(new BigDecimal(aDoubleSTV).setScale(1, RoundingMode.HALF_UP).toString());

                            Double aDoubleRTW = 0.0;
                            Double aDoubleRTWV = 0.0;
                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
                            Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                            if (integerReturn > 0) {
                                aDoubleRTW = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(disGoodsMap);
                                aDoubleRTWV = gbDepartmentStockReduceService.queryReduceReturnTotal(disGoodsMap);
                            }
                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleRTW).setScale(1, RoundingMode.HALF_UP).toString());
                            goodsRow.createCell(14).setCellValue(new BigDecimal(aDoubleRTWV).setScale(1, RoundingMode.HALF_UP).toString());

                            double aDoubleRV = aDoubleRTV + aDoubleSV + aDoubleSTV;
                            double aDoubleR = aDoubleRT + aDoubleS + aDoubleST;
                            goodsRow.createCell(5).setCellValue(formatDecimal(aDoubleR));
                            goodsRow.createCell(10).setCellValue(new BigDecimal(aDoubleRV).setScale(1, RoundingMode.HALF_UP).toString());

                            Double aDoubleRRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                            goodsRow.createCell(15).setCellValue(new BigDecimal(aDoubleRRest).setScale(1, RoundingMode.HALF_UP).toString());
                            Double aDoubleRRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(disGoodsMap);
                            goodsRow.createCell(16).setCellValue(new BigDecimal(aDoubleRRestV).setScale(1, RoundingMode.HALF_UP).toString());

                        }
                    }
                }
            }
        }

        return wb;
    }

    private HSSFWorkbook toCreatDisCostForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("disId", Integer.valueOf(reportEntity.getGbRepIds()));
        wb = sheetCreatCost(wb, map, reportEntity);
        System.out.println("toCreatDisCostFormtoCreatDisCostForm");
        return wb;
    }

    private HSSFWorkbook toCreatDepCostForm(GbReportEntity reportEntity) {
        System.out.println("cres");
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depFatherId", departmentEntity.getGbDepartmentId());
        wb = sheetCreatCost(wb, map, reportEntity);

        return wb;
    }

    private HSSFWorkbook toCreatSubDepCostForm(GbReportEntity reportEntity) {
        System.out.println("=== 开始生成子部门成本分析Excel ===");
        HSSFWorkbook wb = new HSSFWorkbook();

        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depId", Integer.valueOf(reportEntity.getGbRepIds()));
        wb = sheetCreatCost(wb, map, reportEntity);
        System.out.println("查询参数toCreatSubDepCostForm: " + map);
        return wb;
    }

    private HSSFWorkbook toCreatPurDepUserForm(GbReportEntity reportEntity) {
        System.out.println("=== 开始生成采购员统计Excel ===");
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("purUserId", reportEntity.getGbRepIds());
        map.put("typeNotEqual", 9);
        map.put("dayuStatus", 2);

        sheetCreatPurchase(wb, map, reportEntity);

        return wb;
    }

    private HSSFWorkbook toCreatDisStockNowForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("disId", Integer.valueOf(reportEntity.getGbRepIds()));
        map.put("restWeight", 0);

        wb = sheetCreatStock(wb, map, reportEntity);
        return wb;
    }

    private HSSFWorkbook toCreatSubDepStockNowForm(GbReportEntity reportEntity) {
        HSSFWorkbook wb = new HSSFWorkbook();
        GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", reportEntity.getGbRepStartDate());
        map.put("stopDate", reportEntity.getGbRepStopDate());
        map.put("depId", departmentEntity.getGbDepartmentId());
        map.put("restWeight", 0);
        wb = sheetCreatStock(wb, map, reportEntity);

        return wb;
    }

    private HSSFWorkbook sheetCreatPurchase(HSSFWorkbook wb, Map<String, Object> map, GbReportEntity reportEntity) {
        // 调试信息
        System.out.println("toCreatDepuseerrPurGoodsForm - 查询参数: " + map);
        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDistributerPurchaseGoodsService.queryDisTreeGoods(map);
        System.out.println("toCreatSupplierPurGoodsForm - 查询到的商品数量: " + (gbDistributerGoodsEntities != null ? gbDistributerGoodsEntities.size() : "null"));

        if (gbDistributerGoodsEntities != null && gbDistributerGoodsEntities.size() > 0) {
            System.out.println("toCreatSupplierPurGoodsForm - 开始处理 " + gbDistributerGoodsEntities.size() + " 个商品");

            // 创建主工作表
            HSSFSheet sheet = wb.createSheet("采购员统计");

            // 设置表头
            HSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("序号");
            headerRow.createCell(1).setCellValue("商品名称");
            headerRow.createCell(2).setCellValue("规格");
            headerRow.createCell(3).setCellValue("品牌");
            headerRow.createCell(4).setCellValue("详细");
            headerRow.createCell(5).setCellValue("供货商总额");
            headerRow.createCell(6).setCellValue("供货商数量");
            headerRow.createCell(7).setCellValue("供货商单价");
            headerRow.createCell(8).setCellValue("供货商退货总额");
            headerRow.createCell(9).setCellValue("供货商退货数量");
            headerRow.createCell(10).setCellValue("供货商退货单价");

            int rowIndex = 1; // 从第2行开始填充数据

            for (GbDistributerGoodsEntity ckGoodsEntity : gbDistributerGoodsEntities) {
                System.out.println("toCreatSupplierPurGoodsForm - 处理商品: " + ckGoodsEntity.getGbDgGoodsName());
                //设置表体
                HSSFRow goodsRow = sheet.createRow(rowIndex);
                goodsRow.createCell(0).setCellValue(rowIndex);
                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                goodsRow.createCell(5).setCellValue(0.0);
                goodsRow.createCell(6).setCellValue(0.0);
                goodsRow.createCell(7).setCellValue(0.0);
                goodsRow.createCell(8).setCellValue(0.0);
                goodsRow.createCell(9).setCellValue(0.0);
                goodsRow.createCell(10).setCellValue(0.0);

                //5 totalWeight
                Map<String, Object> disGoodsMap = new HashMap<>();
                disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                disGoodsMap.put("dayuStatus", 1);
                disGoodsMap.put("typeNotEqual", 9);
                disGoodsMap.put("purUserId", reportEntity.getGbRepIds());
                Double aDoubleTotal = 0.0;
                Double aDoubleWeight = 0.0;
                Double aDoublePerPrice = 0.0;
                Double aDoubleSupplier = 0.0;
                Double aDoubleSupplierTui = 0.0;
                Double aDoubleSupplieWeight = 0.0;
                Double aDoubleSupplieWeightTui = 0.0;
                Double aDoubleSuppliePerPrice = 0.0;
                Double aDoubleSuppliePerPriceTui = 0.0;
                // 查询商品统计数据
                System.out.println("toCreatSupplierPurGoodsForm - 查询商品统计参数: " + disGoodsMap);
                Integer integerProduce = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(disGoodsMap);
                System.out.println("toCreatSupplierPurGoodsForm - 商品统计数量: " + integerProduce);
                if (integerProduce != null && integerProduce > 0) {
                    Double totalResult = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(disGoodsMap);
                    Double weightResult = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(disGoodsMap);
                    aDoubleTotal = totalResult != null ? totalResult : 0.0;
                    aDoubleWeight = weightResult != null ? weightResult : 0.0;
                    aDoublePerPrice = aDoubleWeight != 0 ? aDoubleTotal / aDoubleWeight : 0.0;
                    System.out.println("toCreatSupplierPurGoodsForm - 商品统计结果 - 总额:" + aDoubleTotal + ", 重量:" + aDoubleWeight + ", 单价:" + aDoublePerPrice);
                    Integer integer2 = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(disGoodsMap);
                    if (integer2 != null && integer2 > 0) {
                        Double supplierResult = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(disGoodsMap);
                        Double supplierWeightResult = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(disGoodsMap);
                        aDoubleSupplier = supplierResult != null ? supplierResult : 0.0;
                        aDoubleSupplieWeight = supplierWeightResult != null ? supplierWeightResult : 0.0;
                        aDoubleSuppliePerPrice = aDoubleSupplieWeight != 0 ? aDoubleSupplier / aDoubleSupplieWeight : 0.0;
                        System.out.println("toCreatSupplierPurGoodsForm - 供货商统计结果 - 总额:" + aDoubleSupplier + ", 重量:" + aDoubleSupplieWeight + ", 单价:" + aDoubleSuppliePerPrice);
                    }
                    // 调试信息已移除
                    Integer integerSupTui = gbDistributerPurchaseGoodsService.queryGbPurchaseGoodsCount(disGoodsMap);
                    if (integerSupTui != null && integerSupTui > 0) {
                        Double supplierTuiResult = gbDistributerPurchaseGoodsService.queryPurchaseGoodsSubTotal(disGoodsMap);
                        aDoubleSupplierTui = supplierTuiResult != null ? supplierTuiResult : 0.0;
                        double absoluteValue = Math.abs(aDoubleSupplierTui); // 取绝对值
                        Double supplierWeightTuiResult = gbDistributerPurchaseGoodsService.queryPurchaseGoodsWeightTotal(disGoodsMap);
                        aDoubleSupplieWeightTui = supplierWeightTuiResult != null ? supplierWeightTuiResult : 0.0;
                        aDoubleSuppliePerPriceTui = aDoubleSupplieWeightTui != 0 ? absoluteValue / aDoubleSupplieWeightTui : 0.0;
                        System.out.println("toCreatSupplierPurGoodsForm - 供货商退货统计结果 - 总额:" + aDoubleSupplierTui + ", 重量:" + aDoubleSupplieWeightTui + ", 单价:" + aDoubleSuppliePerPriceTui);
                    }

                }

                goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleSupplier).setScale(1, RoundingMode.HALF_UP).toString());
                goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleSupplieWeight).setScale(1, RoundingMode.HALF_UP).toString());
                goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleSuppliePerPrice).setScale(1, RoundingMode.HALF_UP).toString());

                goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleSupplierTui).setScale(1, RoundingMode.HALF_UP).toString());
                goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleSupplieWeightTui).setScale(1, RoundingMode.HALF_UP).toString());
                goodsRow.createCell(10).setCellValue(new BigDecimal(aDoubleSuppliePerPriceTui).setScale(1, RoundingMode.HALF_UP).toString());

                rowIndex++; // 移动到下一行
            }
        } else {
            System.out.println("toCreatSupplierPurGoodsForm - 没有查询到商品数据");
        }

        System.out.println("toCreatSupplierPurGoodsForm - 完成处理，工作表数量: " + wb.getNumberOfSheets());
        return wb;

    }

    // 新方法 - 创建单个统一的采购列表sheet（用于业务表单）
    private HSSFWorkbook sheetCreatPurchaseSingle(HSSFWorkbook wb, Map<String, Object> map, GbReportEntity reportEntity) {
        // 调试信息
        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
        if (gbDistributerGoodsEntities != null && gbDistributerGoodsEntities.size() > 0) {
            System.out.println("sheetCreatPurchaseneewnwnwnwwn - 开始处理 " + gbDistributerGoodsEntities.size() + " 个商品");

            // 创建主工作表
            HSSFSheet sheet = wb.createSheet("采购列表");

            // 设置表头
            HSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("序号");
            headerRow.createCell(1).setCellValue("商品名称");
            headerRow.createCell(2).setCellValue("规格");
            headerRow.createCell(3).setCellValue("品牌");
            headerRow.createCell(4).setCellValue("详细");
            headerRow.createCell(5).setCellValue("采购总额");
            headerRow.createCell(6).setCellValue("采购数量");

            int rowIndex = 1; // 从第2行开始填充数据

            for (GbDistributerGoodsEntity ckGoodsEntity : gbDistributerGoodsEntities) {
                System.out.println("sheetCreaNNNNNNN - 处理商品: " + ckGoodsEntity.getGbDgGoodsName());
                //设置表体
                HSSFRow goodsRow = sheet.createRow(rowIndex);
                goodsRow.createCell(0).setCellValue(rowIndex);
                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                goodsRow.createCell(5).setCellValue(0.0);
                goodsRow.createCell(6).setCellValue(0.0);

                //5 totalWeight
                Map<String, Object> disGoodsMap = new HashMap<>();
                disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                Double aDoubleTotal = 0.0;
                Double aDoubleWeight = 0.0;
                // 查询商品统计数据
                System.out.println("sheetCreatPurchaseSingletotalWeight - 查询商品统计参数: " + disGoodsMap);
                Integer integerProduce = gbDepGoodsStockService.queryGoodsStockCount(disGoodsMap);
                System.out.println("sheetCreatPurchaseSingletotalWeight - 商品统计数量: " + integerProduce);
                if (integerProduce != null && integerProduce > 0) {
                     aDoubleTotal = gbDepGoodsStockService.queryDepGoodsSubtotal(disGoodsMap);
                     aDoubleWeight = gbDepGoodsStockService.queryDepStockWeightTotal(disGoodsMap);
                }
                goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleTotal).setScale(1, RoundingMode.HALF_UP).toString());
                goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleWeight).setScale(1, RoundingMode.HALF_UP).toString());

                rowIndex++; // 移动到下一行
            }
        } else {
            System.out.println("sheetCreatPurchaseSingle - 没有查询到商品数据");
        }

        System.out.println("sheetCreatPurchaseSingle - 完成处理，工作表数量: " + wb.getNumberOfSheets());
        return wb;
    }

    // 新方法 - 创建单个统一的采购列表sheet（用于业务表单）
    private HSSFWorkbook sheetCreatPurchaseTuihuoSingle(HSSFWorkbook wb, Map<String, Object> map, GbReportEntity reportEntity) {
        // 调试信息
        System.out.println("dpeTut - 查询参数: " + map);
        map.put("equalType", 4);
        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDepartmentStockReduceService.queryGoodsStockRecordListByParams(map);
        System.out.println("sheetCreatPurchaseSingle - 查询到的退货商品数量: " + (gbDistributerGoodsEntities != null ? gbDistributerGoodsEntities.size() : "null"));

        if (gbDistributerGoodsEntities != null && gbDistributerGoodsEntities.size() > 0) {
            System.out.println("sheetCreatPurchaseSingle - 开始处理 " + gbDistributerGoodsEntities.size() + " 个商品");

            // 创建主工作表
            HSSFSheet sheet = wb.createSheet("退货商品列表");

            // 设置表头
            HSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("序号");
            headerRow.createCell(1).setCellValue("商品名称");
            headerRow.createCell(2).setCellValue("规格");
            headerRow.createCell(3).setCellValue("品牌");
            headerRow.createCell(4).setCellValue("详细");
            headerRow.createCell(5).setCellValue("退货总额");
            headerRow.createCell(6).setCellValue("退货数量");

            int rowIndex = 1; // 从第2行开始填充数据

            for (GbDistributerGoodsEntity ckGoodsEntity : gbDistributerGoodsEntities) {
                System.out.println("sheetCreatPurchaseSingle - 处理商品: " + ckGoodsEntity.getGbDgGoodsName());
                //设置表体
                HSSFRow goodsRow = sheet.createRow(rowIndex);
                goodsRow.createCell(0).setCellValue(rowIndex);
                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                goodsRow.createCell(5).setCellValue(0.0);
                goodsRow.createCell(6).setCellValue(0.0);

                //5 totalWeight
                Map<String, Object> disGoodsMap = new HashMap<>();
                disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                disGoodsMap.put("equalType", 4);
                Double aDoubleTotal = 0.0;
                Double aDoubleWeight = 0.0;
                // 查询商品统计数据
                System.out.println("sheetCreatPurchaseSingle - 查询商品统计参数: " + disGoodsMap);
                Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
                if (integerProduce != null && integerProduce > 0) {
                    System.out.println("sheetCreatPurchaseSingle - 商品统计数量退货Subtitoall: " + disGoodsMap);
                    aDoubleTotal = gbDepartmentStockReduceService.queryReduceReturnTotal(disGoodsMap);
                    System.out.println("sheetCreatPurchaseSingle - 商品统计数量退货Weeight: " + disGoodsMap);
                    aDoubleWeight = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(disGoodsMap);
                }
                goodsRow.createCell(5).setCellValue(new BigDecimal(aDoubleTotal).setScale(1, RoundingMode.HALF_UP).toString());
                goodsRow.createCell(6).setCellValue(new BigDecimal(aDoubleWeight).setScale(1, RoundingMode.HALF_UP).toString());

                rowIndex++; // 移动到下一行
            }
        } else {
            System.out.println("sheetCreatPurchaseSingle - 没有查询到商品数据");
        }

        System.out.println("sheetCreatPurchaseSingle - 完成处理，工作表数量: " + wb.getNumberOfSheets());
        return wb;
    }


    private HSSFWorkbook sheetCreatStock(HSSFWorkbook wb, Map<String, Object> map, GbReportEntity reportEntity) {

        System.out.println("mapamapapapExcelleleel" + map);
        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);

        if (distributerFatherGoodsEntities != null && distributerFatherGoodsEntities.size() > 0) {
            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
                    String sheetName = grand.getGbDfgFatherGoodsName() != null ? grand.getGbDfgFatherGoodsName() : "未命名工作表";
                    if (sheetName.length() > 31) {
                        sheetName = sheetName.substring(0, 31);
                    }
                    HSSFSheet sheet = wb.createSheet(sheetName);
                    //设置表头
                    HSSFRow row1 = sheet.createRow(0);
                    row1.createCell(0).setCellValue("序号");
                    row1.createCell(1).setCellValue("商品名称");
                    row1.createCell(2).setCellValue("规格");
                    row1.createCell(3).setCellValue("品牌");
                    row1.createCell(4).setCellValue("详细");
                    row1.createCell(5).setCellValue("库存总量");
                    row1.createCell(6).setCellValue("库存总金额");

                    map.put("disGoodsGrandId", grand.getGbDistributerFatherGoodsId());
                    List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
                    //设置表体
                    HSSFRow goodsRow = null;
                    if (goodsEntities != null && goodsEntities.size() > 0) {
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
                            GbDepartmentEntity departmentEntity = gbDepartmentService.queryDepInfoGb(Integer.valueOf(reportEntity.getGbRepIds()));

                            //5 totalWeight
                            Map<String, Object> disGoodsMap = new HashMap<>();
                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                            disGoodsMap.put("depFatherId", departmentEntity.getGbDepartmentId());
                            disGoodsMap.put("restWeight", 0);
                            Double aDoubleR = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                            goodsRow.createCell(5).setCellValue(formatDecimal(aDoubleR));
                            Double aDoubleRT = gbDepGoodsStockService.queryDepGoodsRestTotal(disGoodsMap);
                            goodsRow.createCell(6).setCellValue(formatDecimal(aDoubleRT));

                        }
                    }
                }

            }
        }

        return wb;
    }

    // 新方法 - 创建单个统一的库存列表sheet（用于业务表单）
    private HSSFWorkbook sheetCreatStockSingle(HSSFWorkbook wb, Map<String, Object> map, GbReportEntity reportEntity) {

        System.out.println("sheetCreatStockSingle - 创建单个库存列表sheet: " + map);
        map.put("restWeight", 0);
        List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);

        // 创建一个统一的库存列表sheet
        HSSFSheet sheet = wb.createSheet("库存列表");

        //设置表头
        HSSFRow row1 = sheet.createRow(0);
        row1.createCell(0).setCellValue("序号");
        row1.createCell(1).setCellValue("商品名称");
        row1.createCell(2).setCellValue("规格");
        row1.createCell(3).setCellValue("品牌");
        row1.createCell(4).setCellValue("详细");
        row1.createCell(5).setCellValue("库存总量");
        row1.createCell(6).setCellValue("库存总金额");

        int rowIndex = 1; // 从第2行开始填充数据

        //设置表体
        if (goodsEntities != null && goodsEntities.size() > 0) {
            for (int i = 0; i < goodsEntities.size(); i++) {
                GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
                HSSFRow goodsRow = sheet.createRow(rowIndex++);
                goodsRow.createCell(0).setCellValue(rowIndex - 1);
                goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
                goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
                goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
                goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());

                //5 totalWeight
                Map<String, Object> disGoodsMap = new HashMap<>();
                disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
                disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
                disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
                disGoodsMap.put("restWeight", 0);
                Double aDoubleR = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
                goodsRow.createCell(5).setCellValue(formatDecimal(aDoubleR));
                Double aDoubleRT = gbDepGoodsStockService.queryDepGoodsRestTotal(disGoodsMap);
                goodsRow.createCell(6).setCellValue(formatDecimal(aDoubleRT));

            }
        }
        return wb;
    }


}


//    private HSSFWorkbook toCreatSubDepCostForm(GbReportEntity reportEntity) {
//        System.out.println("=== 开始生成子部门成本分析Excel ===");
//        HSSFWorkbook wb = new HSSFWorkbook();
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("startDate", reportEntity.getGbRepStartDate());
//        map.put("stopDate", reportEntity.getGbRepStopDate());
//        map.put("depId", Integer.valueOf(reportEntity.getGbRepIds()));
//
//        System.out.println("查询参数: " + map);
//        List<GbDistributerFatherGoodsEntity> distributerFatherGoodsEntities = gbDepGoodsStockService.queryDepStockTreeFatherGoodsByParams(map);
//        System.out.println("查询到的商品分类数量: " + (distributerFatherGoodsEntities != null ? distributerFatherGoodsEntities.size() : 0));
//
//        if (distributerFatherGoodsEntities != null && distributerFatherGoodsEntities.size() > 0) {
//            for (GbDistributerFatherGoodsEntity greatGrandFather : distributerFatherGoodsEntities) {
//                for (GbDistributerFatherGoodsEntity grand : greatGrandFather.getFatherGoodsEntities()) {
//                    String sheetName = grand.getGbDfgFatherGoodsName() != null ? grand.getGbDfgFatherGoodsName() : "未命名工作表";
//                    if (sheetName.length() > 31) {
//                        sheetName = sheetName.substring(0, 31);
//                    }
//                    HSSFSheet sheet = wb.createSheet(sheetName);
//
//                    //设置表头
//                    HSSFRow row1 = sheet.createRow(0);
//                    row1.createCell(0).setCellValue("序号");
//                    row1.createCell(1).setCellValue("商品名称");
//                    row1.createCell(2).setCellValue("规格");
//                    row1.createCell(3).setCellValue("品牌");
//                    row1.createCell(4).setCellValue("详细");
//                    row1.createCell(5).setCellValue("成本数量");
//                    row1.createCell(6).setCellValue("销售数量");
//                    row1.createCell(7).setCellValue("损耗数量");
//                    row1.createCell(8).setCellValue("废弃数量");
//                    row1.createCell(9).setCellValue("退货数量");
//                    row1.createCell(10).setCellValue("总成本");
//                    row1.createCell(11).setCellValue("销售成本");
//                    row1.createCell(12).setCellValue("损耗成本");
//                    row1.createCell(13).setCellValue("废弃成本");
//                    row1.createCell(14).setCellValue("退货成本");
//                    row1.createCell(15).setCellValue("库存数量");
//                    row1.createCell(16).setCellValue("库存成本");
//
//                    map.put("disGoodsGrandId", grand.getGbDistributerFatherGoodsId());
//                    List<GbDistributerGoodsEntity> goodsEntities = gbDepGoodsStockService.queryDisGoodsStockByParams(map);
//                    System.out.println("商品分类 [" + sheetName + "] 下的商品数量: " + (goodsEntities != null ? goodsEntities.size() : 0));
//
//                    //设置表体
//                    HSSFRow goodsRow = null;
//                    if (goodsEntities != null && goodsEntities.size() > 0) {
//                        for (int i = 0; i < goodsEntities.size(); i++) {
//                            GbDistributerGoodsEntity ckGoodsEntity = goodsEntities.get(i);
//                            goodsRow = sheet.createRow(sheet.getLastRowNum() + 1);
//                            goodsRow.createCell(0).setCellValue(sheet.getLastRowNum());
//                            goodsRow.createCell(1).setCellValue(ckGoodsEntity.getGbDgGoodsName());
//                            goodsRow.createCell(2).setCellValue(ckGoodsEntity.getGbDgGoodsStandardname());
//                            goodsRow.createCell(3).setCellValue(ckGoodsEntity.getGbDgGoodsBrand());
//                            goodsRow.createCell(4).setCellValue(ckGoodsEntity.getGbDgGoodsDetail());
//
//                            // 查询各种成本数据
//                            Map<String, Object> disGoodsMap = new HashMap<>();
//                            disGoodsMap.put("startDate", reportEntity.getGbRepStartDate());
//                            disGoodsMap.put("stopDate", reportEntity.getGbRepStopDate());
//                            disGoodsMap.put("disGoodsId", ckGoodsEntity.getGbDistributerGoodsId());
//                            disGoodsMap.put("depId", Integer.valueOf(reportEntity.getGbRepIds()));
//
//                            // 生产/销售数量
//                            Double aDoubleRT = 0.0;
//                            Double aDoubleRTV = 0.0;
//                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeProduce());
//                            System.out.println("searchRedduce" + disGoodsMap);
//                            Integer integerProduce = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
//                            if (integerProduce != null && integerProduce > 0) {
//                                aDoubleRT = gbDepartmentStockReduceService.queryReduceProduceWeightTotal(disGoodsMap);
//                                aDoubleRTV = gbDepartmentStockReduceService.queryReduceProduceTotal(disGoodsMap);
//                            }
//                            goodsRow.createCell(6).setCellValue(formatDecimal(aDoubleRT));
//                            goodsRow.createCell(11).setCellValue(new BigDecimal(aDoubleRTV).setScale(1, RoundingMode.HALF_UP).toString());
//
//                            // 损耗数量
//                            Double aDoubleS = 0.0;
//                            Double aDoubleSV = 0.0;
//                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeLoss());
//                            Integer integerLoss = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
//                            if (integerLoss > 0) {
//                                aDoubleS = gbDepartmentStockReduceService.queryReduceLossWeightTotal(disGoodsMap);
//                                aDoubleSV = gbDepartmentStockReduceService.queryReduceLossTotal(disGoodsMap);
//                            }
//                            goodsRow.createCell(7).setCellValue(new BigDecimal(aDoubleS).setScale(1, RoundingMode.HALF_UP).toString());
//                            goodsRow.createCell(12).setCellValue(new BigDecimal(aDoubleSV).setScale(1, RoundingMode.HALF_UP).toString());
//
//                            // 废弃数量
//                            Double aDoubleST = 0.0;
//                            Double aDoubleSTV = 0.0;
//                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeWaste());
//                            Integer integerWaste = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
//                            if (integerWaste > 0) {
//                                aDoubleST = gbDepartmentStockReduceService.queryReduceWasteWeightTotal(disGoodsMap);
//                                aDoubleSTV = gbDepartmentStockReduceService.queryReduceWasteTotal(disGoodsMap);
//                            }
//                            goodsRow.createCell(8).setCellValue(new BigDecimal(aDoubleST).setScale(1, RoundingMode.HALF_UP).toString());
//                            goodsRow.createCell(13).setCellValue(new BigDecimal(aDoubleSTV).setScale(1, RoundingMode.HALF_UP).toString());
//
//                            // 退货数量
//                            Double aDoubleRTW = 0.0;
//                            Double aDoubleRTWV = 0.0;
//                            disGoodsMap.put("equalType", getGbDepartGoodsStockReduceTypeReturn());
//                            Integer integerReturn = gbDepartmentStockReduceService.queryReduceTypeCount(disGoodsMap);
//                            if (integerReturn > 0) {
//                                aDoubleRTW = gbDepartmentStockReduceService.queryReduceReturnWeightTotal(disGoodsMap);
//                                aDoubleRTWV = gbDepartmentStockReduceService.queryReduceReturnTotal(disGoodsMap);
//                            }
//                            goodsRow.createCell(9).setCellValue(new BigDecimal(aDoubleRTW).setScale(1, RoundingMode.HALF_UP).toString());
//                            goodsRow.createCell(14).setCellValue(new BigDecimal(aDoubleRTWV).setScale(1, RoundingMode.HALF_UP).toString());
//
//                            // 总计
//                            double aDoubleRV = aDoubleRTV + aDoubleSV + aDoubleSTV;
//                            double aDoubleR = aDoubleRT + aDoubleS + aDoubleST;
//                            goodsRow.createCell(5).setCellValue(formatDecimal(aDoubleR));
//                            goodsRow.createCell(10).setCellValue(new BigDecimal(aDoubleRV).setScale(1, RoundingMode.HALF_UP).toString());
//
//                            // 库存
//                            Double aDoubleRRest = gbDepGoodsStockService.queryDepStockRestWeightTotal(disGoodsMap);
//                            goodsRow.createCell(15).setCellValue(new BigDecimal(aDoubleRRest).setScale(1, RoundingMode.HALF_UP).toString());
//                            Double aDoubleRRestV = gbDepGoodsStockService.queryDepGoodsRestTotal(disGoodsMap);
//                            goodsRow.createCell(16).setCellValue(new BigDecimal(aDoubleRRestV).setScale(1, RoundingMode.HALF_UP).toString());
//                        }
//                    } else {
//                        // 如果没有数据，添加一行提示
//                        HSSFRow emptyRow = sheet.createRow(1);
//                        emptyRow.createCell(0).setCellValue("暂无数据");
//                    }
//                }
//            }
//        } else {
//            // 如果没有分类数据，创建一个默认工作表
//            HSSFSheet sheet = wb.createSheet("无数据");
//            HSSFRow row = sheet.createRow(0);
//            row.createCell(0).setCellValue("该时间段内没有找到相关数据");
//            row.createCell(1).setCellValue("报表ID: " + reportEntity.getGbReportId());
//            row = sheet.createRow(1);
//            row.createCell(0).setCellValue("开始日期: " + reportEntity.getGbRepStartDate());
//            row.createCell(1).setCellValue("结束日期: " + reportEntity.getGbRepStopDate());
//        }
//
//        System.out.println("=== 子部门成本分析Excel生成完成 ===");
//        return wb;
//    }

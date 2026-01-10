package com.nongxinle.controller;

/**
 * @author lpy
 * @date 07-27 17:38
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.Constant;
import com.nongxinle.utils.UploadFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPurchaseGoodsWithBatch;
import static com.nongxinle.utils.PinYin4jUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.getXiegang;


@RestController
@RequestMapping("api/nxdistributergoods")
public class NxDistributerGoodsController {
    private static final Logger logger = LoggerFactory.getLogger(NxDistributerGoodsController.class);

    @Autowired
    private NxDistributerGoodsService dgService;
    @Autowired
    private NxDepartmentOrdersService depOrdersService;
    @Autowired
    private NxDepartmentOrdersHistoryService nxDepartmentOrdersHistoryService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxDistributerFatherGoodsService dgfService;
    @Autowired
    private NxDistributerStandardService dsService;
    @Autowired
    private NxDistributerAliasService disAliasService;
    @Autowired
    private NxAliasService nxAliasService;
    @Autowired
    private NxStandardService nxStandardService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepDisGoodsService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDisPurchaseGoodsService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDepartmentOrderHistoryService nxDepartmentOrderHistoryService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDPBService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;
    @Autowired
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepDisGoodsService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerFatherGoodsService gbDgfService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private NxDistributerGoodsShelfGoodsService nxDistributerGoodsShelfGoodsService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private GbDistributerPurchaseGoodsService gbDistributerPurchaseGoodsService;
    @Autowired
    private NxJrdhSupplierService jrdhSupplierService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;
    @Autowired
    private GbDistributerPurchaseBatchService getGbDPBService;
    @Autowired
    private NxDistributerGbDistributerService nxDisGbDisService;
    @Autowired
    private GbDistributerGoodsService gbDgService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private GbDistributerService gbDistributerService;

    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private NxDistributerGoodsShelfStockService nxDistributerGoodsShelfStockService;
    @Autowired
    private NxDistributerGoodsShelfStockReduceService nxDistributerGoodsShelfStockReduceService;
    @Autowired
    private NxDistributerGoodsShelfService nxDistributerGoodsShelfService;
    @Autowired
    private com.nongxinle.service.AsyncGoodsDownloadService asyncGoodsDownloadService;
    @Autowired
    private NxTraceReportService nxTraceReportService;


    private static final String KEY = "C5HBZ-KEIW2-JXXUJ-COLGS-FQO47-WWFAK";


    @RequestMapping(value = "/getBrandForPrompts")
    @ResponseBody
    public R getBrandForPrompts() {
        List<String> brandList = nxGoodsService.queryNxBrand();
        List<String> disBrandList = dgService.queryDisGoodsBrand();

        LinkedHashSet<String> uniqueBrands = new LinkedHashSet<>();
        if (brandList != null) {
            for (String brand : brandList) {
                String normalized = normalizeBrand(brand);
                if (normalized != null) {
                    uniqueBrands.add(normalized);
                }
            }
        }
        if (disBrandList != null) {
            for (String brand : disBrandList) {
                String normalized = normalizeBrand(brand);
                if (normalized != null) {
                    uniqueBrands.add(normalized);
                }
            }
        }

        return R.ok().put("data", new ArrayList<>(uniqueBrands));
    }

    @RequestMapping(value = "/cancelSupplierGoods", method = RequestMethod.POST)
    @ResponseBody
    public R cancelSupplierGoods(String ids) {
        System.out.println("ids" + ids);
        String[] split = ids.split(",");
        if (split.length > 0) {
            for (String id : split) {
                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(Integer.valueOf(id));
                distributerGoodsEntity.setNxDgSupplierId(null);
                dgService.update(distributerGoodsEntity);
            }
        }
        return R.ok();
    }

    @RequestMapping(value = "/updateFatherId")
    @ResponseBody
    public R updateFatherId() {

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryWenti();
        System.out.println("deiisissisissiis" + departmentDisGoodsEntities.size());
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                if (nxDistributerGoodsEntity != null) {

                    System.out.println("depgoods111" + nxDistributerGoodsEntity.getNxDgGoodsName());
                    System.out.println("depgoods111ffffff" + nxDistributerGoodsEntity.getNxDgNxFatherId());
                    NxGoodsEntity nxFather = nxGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgNxFatherId());
                    NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
                    if (nxFather != null && fatherGoodsEntity == null) {
                        NxDistributerFatherGoodsEntity goodsEntity = new NxDistributerFatherGoodsEntity();

                        goodsEntity.setNxDistributerFatherGoodsId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
                        goodsEntity.setNxDfgFatherGoodsName(nxFather.getNxGoodsName());
                        goodsEntity.setNxDfgFathersFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                        goodsEntity.setNxDfgDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
                        NxDistributerFatherGoodsEntity grandEnttitty = nxDistributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());

                        if (grandEnttitty != null && grandEnttitty.getNxDfgFatherGoodsSort() != null) {

                            Integer nxDgNxGreatGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
                            int sort = nxDistributerFatherGoodsService.queryMaxSortByFatherId(nxDgNxGreatGrandId);
                            goodsEntity.setNxDfgFatherGoodsSort(sort + 1);
                            goodsEntity.setNxDfgFatherGoodsLevel(2);
                            goodsEntity.setNxDfgGoodsAmount(1);
                            NxDistributerFatherGoodsEntity grand = dgfService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                            goodsEntity.setNxDfgFatherGoodsColor(grand.getNxDfgFatherGoodsColor());
                            goodsEntity.setNxDfgFatherGoodsImg("goodsImage/logo.jpg");
                            goodsEntity.setNxDfgNxGoodsId(nxFather.getNxGoodsId());

                            nxDistributerFatherGoodsService.save(goodsEntity);
                        }

                    }
                }


            }
        }

        return R.ok();
    }

    @RequestMapping(value = "/supplierHaveNotNxGoods/{id}")
    @ResponseBody
    public R supplierHaveNotNxGoods(@PathVariable Integer id) {
        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(id);
        distributerGoodsEntity.setNxDgPurchaseAuto(-1);
        distributerGoodsEntity.setNxDgSupplierId(null);
        distributerGoodsEntity.setNxDgBuyingPrice("0.1");
        dgService.update(distributerGoodsEntity);
        return R.ok();
    }

    @RequestMapping(value = "/nxDisSaveGbPurchaserBatch", method = RequestMethod.POST)
    @ResponseBody
    public R nxDisSaveGbPurchaserBatch(Integer nxDisId, Integer batchId) {

        GbDistributerPurchaseBatchEntity batchEntity = getGbDPBService.queryBatchWithOrders(batchId);
        batchEntity.setGbDpbNxDistributerId(nxDisId);
        batchEntity.setGbDpbStatus(getGbDisPurchaseBatchHaveRead());
        getGbDPBService.update(batchEntity);

        Integer gbDisId = batchEntity.getGbDpbDistributerId();
        Map<String, Object> map = new HashMap<>();
        map.put("nxDisId", nxDisId);
        map.put("gbDisId", gbDisId);
        NxDistributerGbDistributerEntity nxDistributerGbDistributer = nxDisGbDisService.queryObjectByParams(map);
        if (nxDistributerGbDistributer == null) {
            Map<String, Object> mapB = new HashMap<>();
            mapB.put("disId", gbDisId);
            mapB.put("type", getGbDepartmentTypeAppSupplier());
            List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(mapB);
            System.out.println("denennee" + departmentEntities.size());
            if (departmentEntities.size() > 0) {
                NxDistributerGbDistributerEntity nxDistributerGbDistributerEntity = new NxDistributerGbDistributerEntity();
                nxDistributerGbDistributerEntity.setNxDgdGbPayPeriodWeek(4);
                nxDistributerGbDistributerEntity.setNxDgdFromNxDepId(-1);
                nxDistributerGbDistributerEntity.setNxDgdStatus(0);
                nxDistributerGbDistributerEntity.setNxDgdGbGoodsPrice(0);
                nxDistributerGbDistributerEntity.setNxDgdGbPayMethod(1);
                nxDistributerGbDistributerEntity.setNxDgdGbDepId(departmentEntities.get(0).getGbDepartmentId());
                nxDistributerGbDistributerEntity.setNxDgdGbDistributerId(gbDisId);
                nxDistributerGbDistributerEntity.setNxDgdNxDistributerId(nxDisId);
                nxDistributerGbDistributerEntity.setNxDgdNxSupplierId(batchEntity.getGbDpbSupplierId());
                nxDisGbDisService.save(nxDistributerGbDistributerEntity);


                Integer gbDpbSupplierId = batchEntity.getGbDpbSupplierId();
                NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDpbSupplierId);
                supplierEntity.setNxJrdhsNxDistributerId(nxDisId);
                jrdhSupplierService.update(supplierEntity);


                GbDistributerEntity gbDistributerEntity = gbDistributerService.queryDistributerInfo(gbDisId);

                NxDepartmentEntity departmentEntity = new NxDepartmentEntity();
                departmentEntity.setNxDepartmentName(gbDistributerEntity.getGbDistributerName());
                departmentEntity.setNxDepartmentDisId(nxDisId);
                departmentEntity.setNxDepartmentGbDistributerId(gbDisId);
                departmentEntity.setNxDepartmentSettleType(nxDistributerGbDistributerEntity.getNxDgdGbPayMethod());
                departmentEntity.setNxDepartmentFatherId(0);
                departmentEntity.setNxDepartmentSubAmount(0);
                departmentEntity.setNxDepartmentIsGroupDep(1);
                departmentEntity.setNxDepartmentPrintName("ApplyFiftyPanel");
                departmentEntity.setNxDepartmentSettleType(0);
                departmentEntity.setNxDepartmentType("unFixed");
                departmentEntity.setNxDepartmentShowWeeks(1);
                departmentEntity.setNxDepartmentWorkingStatus(-1);
                departmentEntity.setNxDepartmentOweBoxNumber(0);
                departmentEntity.setNxDepartmentDeliveryBoxNumber(0);
                departmentEntity.setNxDepartmentUnPayTotal("0");
                departmentEntity.setNxDepartmentAddCount(0);
                departmentEntity.setNxDepartmentPayTotal("0");
                departmentEntity.setNxDepartmentProfitTotal("0");
                departmentEntity.setNxDepartmentAttrName(gbDistributerEntity.getGbDistributerName());
                departmentEntity.setNxDepartmentOrderCode(gbDistributerEntity.getGbDistributerName());
                departmentEntity.setNxDepartmentRecordMinutes(30);
                departmentEntity.setNxDepartmentJoinDate(formatWhatDay(0));
                departmentEntity.setNxDepartmentOrderTotal(0);
                nxDepartmentService.saveJustDepartment(departmentEntity);
            }

        } else {
            if (nxDistributerGbDistributer.getNxDgdNxSupplierId() == null) {
                nxDistributerGbDistributer.setNxDgdNxSupplierId(batchEntity.getGbDpbSupplierId());
                nxDisGbDisService.update(nxDistributerGbDistributer);
                Integer gbDpbSupplierId = batchEntity.getGbDpbSupplierId();
                NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDpbSupplierId);
                supplierEntity.setNxJrdhsNxDistributerId(nxDisId);
                jrdhSupplierService.update(supplierEntity);

            }
        }

        List<GbDistributerPurchaseGoodsEntity> gbDPGEntities = batchEntity.getGbDPGEntities();
        if (gbDPGEntities.size() > 0) {
            for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : gbDPGEntities) {

                purchaseGoodsEntity.setGbDpgBuySubtotal("0");
                purchaseGoodsEntity.setGbDpgPurchaseType(5);
                purchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(nxDisId);
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

                Map<String, Object> mapG = new HashMap<>();
                mapG.put("purGoodsId", purchaseGoodsEntity.getGbDistributerPurchaseGoodsId());
                List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(mapG);

//                List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purchaseGoodsEntity.getGbDepartmentOrdersEntities();
                if (gbDepartmentOrdersEntities.size() > 0) {
                    for (GbDepartmentOrdersEntity ordersEntity : gbDepartmentOrdersEntities) {
                        Integer gbDoDisGoodsId = ordersEntity.getGbDoDisGoodsId();
                        GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryObject(gbDoDisGoodsId);
                        checkIfHasNxGoods(gbDistributerGoodsEntity, nxDisId, purchaseGoodsEntity.getGbDistributerPurchaseGoodsId());

                        if (batchEntity.getGbDpbPurchaseType() == 21) {
                            if (gbDistributerGoodsEntity.getGbDgNxGoodsId() != null) {
                                Integer nxGoodsId = gbDistributerGoodsEntity.getGbDgNxGoodsId();
                                Map<String, Object> mapNG = new HashMap<>();
                                mapNG.put("nxGoodsId", nxGoodsId);
                                mapNG.put("disId", nxDisId);
                                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryOneGoodsAboutNxGoods(mapNG);
                                if (distributerGoodsEntity != null) {
                                    gbDistributerGoodsEntity.setGbDgNxDistributerId(nxDisId);
                                    gbDistributerGoodsEntity.setGbDgNxDistributerGoodsId(distributerGoodsEntity.getNxDistributerGoodsId());
                                    gbDistributerGoodsService.update(gbDistributerGoodsEntity);
                                }
                            }
                        }

                    }
                }

            }
        }

        return R.ok();
    }


    private void checkIfHasNxGoods(GbDistributerGoodsEntity goodsEntity, Integer nxDisId, Integer purGoodsId) {
        //不是临时添加
        if (goodsEntity.getGbDgNxGoodsId() != null) {
            //nxDisyou
            Map<String, Object> map = new HashMap<>();
            map.put("nxGoodsId", goodsEntity.getGbDgNxGoodsId());
            map.put("disId", nxDisId);
            NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(goodsEntity.getGbDgNxGoodsId());

            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryOneGoodsAboutNxGoods(map);
            if (distributerGoodsEntity != null) {
                //设置 gbDisGoods给 nxDis
                gbOrderToSaveNxOrder(goodsEntity.getGbDistributerGoodsId(), distributerGoodsEntity.getNxDistributerGoodsId(), nxDisId, purGoodsId);
            } else {
                //下载 nxGoods
                NxDistributerGoodsEntity distributerGoodsEntity1 = downLoadNxGoodsForGbDisGoods(goodsEntity.getGbDistributerGoodsId(), nxGoodsEntity, nxDisId);
                gbOrderToSaveNxOrder(goodsEntity.getGbDistributerGoodsId(), distributerGoodsEntity1.getNxDistributerGoodsId(), nxDisId, purGoodsId);

            }

        } else {
            //linshitianjai
            //给 nxDis 添加临时 goods
            NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();
            Map<String, Object> map = new HashMap<>();
            map.put("disId", nxDisId);
            map.put("nxGoodsId", -1);
            map.put("goodsLevel", 2);
            System.out.println("linshsisssisisiisiissi" + nxDisId);
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);

            NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
            goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
            goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
            goods.setNxDgPurchaseAuto(-1);
            goods.setNxDgGoodsName(goodsEntity.getGbDgGoodsName());
            String pinyin = hanziToPinyin(goodsEntity.getGbDgGoodsName());
            String headPinyin = getHeadStringByString(goodsEntity.getGbDgGoodsName(), false, null);
            goods.setNxDgGoodsPinyin(pinyin);
            goods.setNxDgGoodsPy(headPinyin);
            goods.setNxDgDistributerId(nxDisId);
            goods.setNxDgBuyingPriceIsGrade(0);
            goods.setNxDgBuyingPrice("0.1");
            goods.setNxDgWillPrice("0.1");
            goods.setNxDgBuyingPriceOne("0.1");
            goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
            goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
            goods.setNxDgWillPriceOne("0.1");
            goods.setNxDgWillPriceOneAboutPrice("0.1");
            goods.setNxDgGoodsIsHidden(0);
            goods.setNxDgGoodsStandardname(goodsEntity.getGbDgGoodsStandardname());
            goods.setNxDgGoodsDetail(goodsEntity.getGbDgGoodsDetail());
            goods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
            System.out.println("savegoogog" + goods);
            goods.setNxDgOutTotalWeight("0");
            dgService.save(goods);

            Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
            fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
            dgfService.update(fatherGoodsEntity);

            ////设置 gbDisGoods给 nxDis
//            setGbDistribuerGoodsToNxDisWithOrder(goodsEntity.getGbDistributerGoodsId(), goods);
            gbOrderToSaveNxOrder(goodsEntity.getGbDistributerGoodsId(), goods.getNxDistributerGoodsId(), nxDisId, purGoodsId);

        }

    }


    private void gbOrderToSaveNxOrder(Integer gbDisGoodsId, Integer nxDisGoodsId, Integer nxDisId, Integer purGoodsId) {

        //修改 gbOrder
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", gbDisGoodsId);
        map.put("status", 3);
        map.put("purGoodsId", purGoodsId);
        System.out.println("changeorord" + map);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        if (ordersEntities.size() > 0) {
            for (GbDepartmentOrdersEntity gbDepartmentOrders : ordersEntities) {

                gbDepartmentOrders.setGbDoNxDistributerId(nxDisId);
                gbDepartmentOrders.setGbDoNxDistributerGoodsId(nxDisGoodsId);

                NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxDisGoodsId);
                String gbDoQuantity = gbDepartmentOrders.getGbDoQuantity();
                String gbDoStandard = gbDepartmentOrders.getGbDoStandard();
                String gbDoRemark = gbDepartmentOrders.getGbDoRemark();
                gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
                gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
                gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
                gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
                gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
                String gbDoApplyArriveDate = gbDepartmentOrders.getGbDoApplyArriveDate();
                Integer gbDoDepartmentId = gbDepartmentOrders.getGbDoDepartmentId();
                Integer gbDoDistributerId = gbDepartmentOrders.getGbDoDistributerId();
                Integer gbDoDepartmentFatherId = gbDepartmentOrders.getGbDoDepartmentFatherId();
                Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
                Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
                //
                NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
                ordersEntity.setNxDoDistributerId(nxDisId);
                ordersEntity.setNxDoDisGoodsId(nxDisGoodsId);
                ordersEntity.setNxDoQuantity(gbDoQuantity);
                ordersEntity.setNxDoStandard(gbDoStandard);
                ordersEntity.setNxDoRemark(gbDoRemark);

                if (ordersEntity.getNxDoStandard().equals(nxDistributerGoodsEntity.getNxDgWillPriceOneStandard())) {
                    BigDecimal willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPrice());
                    BigDecimal orderWeight = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
                    BigDecimal subtotal = orderWeight.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);

                    ordersEntity.setNxDoPrice(nxDistributerGoodsEntity.getNxDgWillPrice());
                    ordersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
                    ordersEntity.setNxDoSubtotal(subtotal.toString());
                    gbDepartmentOrders.setGbDoPrice(nxDistributerGoodsEntity.getNxDgWillPrice());
                    gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                    gbDepartmentOrders.setGbDoSubtotal(subtotal.toString());


                    ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPrice());
                    ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceUpdate());
                    BigDecimal buyPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPrice());
                    BigDecimal buySutotal = buyPrice.multiply(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoCostSubtotal(buySutotal.toString());
                    BigDecimal profit = subtotal.subtract(buySutotal).setScale(1, BigDecimal.ROUND_HALF_UP);

                    ordersEntity.setNxDoProfitSubtotal(profit.toString());

                    BigDecimal decimal = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoProfitScale(decimal.toString());

                } else {
                    ordersEntity.setNxDoCostSubtotal("0");
                    ordersEntity.setNxDoProfitSubtotal("0");
                    ordersEntity.setNxDoCostPrice("0");
                }


                ordersEntity.setNxDoApplyDate(formatWhatDay(0));
                ordersEntity.setNxDoArriveOnlyDate(formatWhatDay(0));
                ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
                ordersEntity.setNxDoApplyFullTime(formatWhatYearDayTime(0));
                ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
                ordersEntity.setNxDoArriveDate(gbDoApplyArriveDate);
                ordersEntity.setNxDoGbDistributerId(gbDoDistributerId);
                ordersEntity.setNxDoGbDepartmentId(gbDoDepartmentId);
                ordersEntity.setNxDoGbDepartmentFatherId(gbDoDepartmentFatherId);
                ordersEntity.setNxDoDepartmentId(-1);
                ordersEntity.setNxDoDepartmentFatherId(-1);
                ordersEntity.setNxDoNxCommunityId(-1);
                ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
                ordersEntity.setNxDoNxCommRestrauntId(-1);
                ordersEntity.setNxDoNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
                ordersEntity.setNxDoNxGoodsFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoDepDisGoodsId(-1);
                ordersEntity.setNxDoArriveWhatDay(getWeek(0));
                ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
                ordersEntity.setNxDoGoodsType(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
                ordersEntity.setNxDoIsAgent(-1);
                ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
                ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPrice());
                ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceUpdate());
//                ordersEntity.setNxDoCostPriceLevel("0");
                ordersEntity.setNxDoPurchaseUserId(-1);
                ordersEntity.setNxDoGbDepartmentOrderId(gbDepartmentOrders.getGbDepartmentOrdersId());
                if (nxDistributerGoodsEntity.getNxDgPurchaseAuto() == -1) {
                    ordersEntity.setNxDoPurchaseGoodsId(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
                } else {
                    savePurGoodsAuto(ordersEntity);
                }

                ordersEntity.setNxDoStatus(getNxOrderStatusNew());
                depOrdersService.saveForGb(ordersEntity);
                Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
                gbDepartmentOrders.setGbDoNxDepartmentOrderId(nxDepartmentOrdersId);
                gbDepartmentOrdersService.update(gbDepartmentOrders);

            }
        }

        // 添加 nxOrder

    }


    @RequestMapping(value = "/addAppGoodsWithOrder", method = RequestMethod.POST)
    @ResponseBody
    public R addAppGoodsWithOrder(String ids, Integer nxDisId) {
        System.out.println("ids" + ids);
        String[] split = ids.split(",");
        if (split.length > 0) {
            for (String id : split) {
                System.out.println("id======" + id);
                GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(Integer.valueOf(id));
                //不是临时添加
                if (goodsEntity.getGbDgNxGoodsId() != null) {
                    //nxDisyou
                    Map<String, Object> map = new HashMap<>();
                    map.put("nxGoodsId", goodsEntity.getGbDgNxGoodsId());
                    map.put("disId", nxDisId);
                    NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(goodsEntity.getGbDgNxGoodsId());

                    NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryOneGoodsAboutNxGoods(map);
                    if (distributerGoodsEntity != null) {
                        //设置 gbDisGoods给 nxDis
                        setGbDistribuerGoodsToNxDisWithOrder(Integer.valueOf(id), distributerGoodsEntity);

                    } else {
                        //下载 nxGoods
                        NxDistributerGoodsEntity distributerGoodsEntity1 = downLoadNxGoodsForGbDisGoods(Integer.valueOf(id), nxGoodsEntity, nxDisId);

                        ////设置 gbDisGoods给 nxDis
                        setGbDistribuerGoodsToNxDisWithOrder(Integer.valueOf(id), distributerGoodsEntity1);

                    }

                } else {
                    //linshitianjai
                    //给 nxDis 添加临时 goods
                    NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();
                    Map<String, Object> map = new HashMap<>();
                    map.put("disId", nxDisId);
                    map.put("nxGoodsId", -1);
                    map.put("goodsLevel", 2);
                    System.out.println("linshsisssisisiisiissi" + nxDisId);
                    List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);

                    NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
                    goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
                    goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
                    goods.setNxDgPurchaseAuto(1);
                    goods.setNxDgGoodsName(goodsEntity.getGbDgGoodsName());
                    String pinyin = hanziToPinyin(goodsEntity.getGbDgGoodsName());
                    String headPinyin = getHeadStringByString(goodsEntity.getGbDgGoodsName(), false, null);
                    goods.setNxDgGoodsPinyin(pinyin);
                    goods.setNxDgGoodsPy(headPinyin);
                    goods.setNxDgDistributerId(nxDisId);
                    goods.setNxDgBuyingPriceIsGrade(0);
                    goods.setNxDgBuyingPrice("0.1");
                    goods.setNxDgWillPrice("0.1");
                    goods.setNxDgGoodsIsHidden(0);
                    goods.setNxDgGoodsStandardname(goodsEntity.getGbDgGoodsStandardname());
                    goods.setNxDgGoodsDetail(goodsEntity.getGbDgGoodsDetail());
                    goods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
                    goods.setNxDgBuyingPriceOne("0.1");
                    goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
                    goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
                    goods.setNxDgWillPriceOne("0.1");
                    goods.setNxDgWillPriceOneAboutPrice("0.1");
                    System.out.println("savegoogog" + goods);
                    goods.setNxDgOutTotalWeight("0");
                    dgService.save(goods);

                    Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
                    fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
                    dgfService.update(fatherGoodsEntity);

                    ////设置 gbDisGoods给 nxDis
                    setGbDistribuerGoodsToNxDisWithOrder(goodsEntity.getGbDistributerGoodsId(), goods);

                }

            }
        }

        return R.ok();
    }


//    @RequestMapping(value = "/addAppGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R addAppGoods(String ids, Integer nxDisId) {
//        System.out.println("ids" + ids);
//        String[] split = ids.split(",");
//        if (split.length > 0) {
//            for (String id : split) {
//                System.out.println("id======" + id);
//                GbDistributerGoodsEntity goodsEntity = gbDistributerGoodsService.queryObject(Integer.valueOf(id));
//
//                //不是临时添加
//                if (goodsEntity.getGbDgNxGoodsId() != null) {
//                    //nxDisyou
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("nxGoodsId", goodsEntity.getGbDgNxGoodsId());
//                    map.put("disId", nxDisId);
//                    NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(goodsEntity.getGbDgNxGoodsId());
//
//                    NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryOneGoodsAboutNxGoods(map);
//                    if (distributerGoodsEntity != null) {
//                        //设置 gbDisGoods给 nxDis
//                        setGbDistribuerGoodsToNxDis(Integer.valueOf(id), distributerGoodsEntity);
//
//                    } else {
//                        //下载 nxGoods
//                        NxDistributerGoodsEntity distributerGoodsEntity1 = downLoadNxGoodsForGbDisGoods(Integer.valueOf(id), nxGoodsEntity, nxDisId);
//
//                        ////设置 gbDisGoods给 nxDis
//                        setGbDistribuerGoodsToNxDis(Integer.valueOf(id), distributerGoodsEntity1);
//                    }
//
//                } else {
//                    //linshitianjai
//                    //给 nxDis 添加临时 goods
//                    NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("disId", nxDisId);
//                    map.put("nxGoodsId", -1);
//                    map.put("goodsLevel", 2);
//                    System.out.println("linshsisssisisiisiissi" + nxDisId);
//                    List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
//
//                    NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
//                    goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
//                    goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
//                    goods.setNxDgPurchaseAuto(1);
//                    goods.setNxDgGoodsName(goodsEntity.getGbDgGoodsName());
//                    String pinyin = hanziToPinyin(goodsEntity.getGbDgGoodsName());
//                    String headPinyin = getHeadStringByString(goodsEntity.getGbDgGoodsName(), false, null);
//                    goods.setNxDgGoodsPinyin(pinyin);
//                    goods.setNxDgGoodsPy(headPinyin);
//                    goods.setNxDgDistributerId(nxDisId);
//                    goods.setNxDgBuyingPriceIsGrade(0);
//                    goods.setNxDgBuyingPrice("0.1");
//                    goods.setNxDgWillPrice("0.1");
//                    goods.setNxDgBuyingPriceOne("0.1");
//                    goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
//                    goods.setNxDgWillPriceOneWeight(goods.getNxDgGoodsStandardname());
//                    goods.setNxDgWillPriceOne("0.1");
//                    goods.setNxDgWillPriceOneAboutPrice("0.1");
//                    goods.setNxDgGoodsIsHidden(0);
//                    goods.setNxDgGoodsStandardname(goodsEntity.getGbDgGoodsStandardname());
//                    goods.setNxDgGoodsDetail(goodsEntity.getGbDgGoodsDetail());
//                    goods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
//                    System.out.println("savegoogog" + goods);
//                    goods.setNxDgOutTotalWeight("0");
//                    dgService.save(goods);
//
//                    Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
//                    fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
//                    dgfService.update(fatherGoodsEntity);
//
//                    ////设置 gbDisGoods给 nxDis
//                    setGbDistribuerGoodsToNxDis(goodsEntity.getGbDistributerGoodsId(), goods);
//
//                }
//
//            }
//        }
//
//        return R.ok();
//    }

    private void setGbDistribuerGoodsToNxDis(Integer gbDisGoodsId, NxDistributerGoodsEntity distributerGoodsEntity) {

        GbDistributerGoodsEntity gbDoodsEntity = gbDistributerGoodsService.queryObject(gbDisGoodsId);
        gbDoodsEntity.setGbDgNxDistributerGoodsId(distributerGoodsEntity.getNxDistributerGoodsId());
        gbDoodsEntity.setGbDgNxDistributerId(distributerGoodsEntity.getNxDgDistributerId());

        Integer gbDgDistributerId = gbDoodsEntity.getGbDgDistributerId();
        Map<String, Object> mapD = new HashMap<>();
        mapD.put("disId", gbDgDistributerId);
        mapD.put("type", getGbDepartmentTypeAppSupplier());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(mapD);
        gbDoodsEntity.setGbDgGbDepartmentId(departmentEntities.get(0).getGbDepartmentId());
        gbDoodsEntity.setGbDgGoodsType(5);
        gbDoodsEntity.setGbDgGbSupplierId(-1);
        gbDistributerGoodsService.update(gbDoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", gbDisGoodsId);
        map.put("status", 3);

        //gbDepGoods
        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);

        if (departmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                departmentDisGoodsEntity.setGbDdgNxDistributerId(distributerGoodsEntity.getNxDgDistributerId());
                departmentDisGoodsEntity.setGbDdgNxDistributerGoodsId(distributerGoodsEntity.getNxDistributerGoodsId());
                departmentDisGoodsEntity.setGbDdgGoodsType(5);
                gbDepDisGoodsService.update(departmentDisGoodsEntity);
            }

        }
        //gbpurgoods


    }


    private void setGbDistribuerGoodsToNxDisWithOrder(Integer gbDisGoodsId, NxDistributerGoodsEntity distributerGoodsEntity) {

        GbDistributerGoodsEntity gbGoodsEntity = gbDistributerGoodsService.queryObject(gbDisGoodsId);
        gbGoodsEntity.setGbDgNxDistributerGoodsId(distributerGoodsEntity.getNxDistributerGoodsId());
        gbGoodsEntity.setGbDgNxDistributerId(distributerGoodsEntity.getNxDgDistributerId());
        gbGoodsEntity.setGbDgGoodsType(5);
        gbGoodsEntity.setGbDgGbSupplierId(-1);
        Integer distributerId = gbGoodsEntity.getGbDgDistributerId();
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", distributerId);
        mapDep.put("type", getGbDepartmentTypeAppSupplier());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(mapDep);
        GbDepartmentEntity appDepartment = departmentEntities.get(0);
        gbGoodsEntity.setGbDgGbDepartmentId(appDepartment.getGbDepartmentId());
        gbDistributerGoodsService.update(gbGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", gbDisGoodsId);
        map.put("status", 3);

        //gbpurgoods
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryOnlyPurGoods(map);
        if (purchaseGoodsEntities.size() > 0) {
            for (GbDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setGbDpgBuyPrice(distributerGoodsEntity.getNxDgWillPrice());
                if (purchaseGoodsEntity.getGbDpgStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                    BigDecimal decimal = new BigDecimal(purchaseGoodsEntity.getGbDpgQuantity()).multiply(new BigDecimal(distributerGoodsEntity.getNxDgWillPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    purchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());
                }
                purchaseGoodsEntity.setGbDpgPurchaseDepartmentId(appDepartment.getGbDepartmentId());
                purchaseGoodsEntity.setGbDpgPurchaseType(5);
                purchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
                purchaseGoodsEntity.setGbDpgPurchaseDate(formatWhatDay(0));
                purchaseGoodsEntity.setGbDpgPurchaseMonth(formatWhatMonth(0));
                purchaseGoodsEntity.setGbDpgPurchaseYear(formatWhatYear(0));
                purchaseGoodsEntity.setGbDpgPurchaseWeek(getWeek(0));
                purchaseGoodsEntity.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
                purchaseGoodsEntity.setGbDpgPurchaseWeekYear(getWeekOfYear(0).toString());
                purchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(distributerGoodsEntity.getNxDgDistributerId());
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        changeGbOrderToNxOrder(gbDisGoodsId, distributerGoodsEntity.getNxDistributerGoodsId(), distributerGoodsEntity.getNxDgDistributerId(), appDepartment.getGbDepartmentId());
    }


    private void changeGbOrderToNxOrder(Integer gbDisGoodsId, Integer nxDisGoodsId, Integer nxDisId, Integer appDepId) {

        //修改 gbOrder
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", gbDisGoodsId);
        map.put("status", 3);
        System.out.println("changeorord" + map);
        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(map);
        if (ordersEntities.size() > 0) {
            for (GbDepartmentOrdersEntity gbDepartmentOrders : ordersEntities) {

                gbDepartmentOrders.setGbDoNxDistributerId(nxDisId);
                gbDepartmentOrders.setGbDoNxDistributerGoodsId(nxDisGoodsId);
                gbDepartmentOrders.setGbDoToDepartmentId(appDepId);
                gbDepartmentOrders.setGbDoGoodsType(5);
                gbDepartmentOrders.setGbDoOrderType(5);

                NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxDisGoodsId);

                String gbDoQuantity = gbDepartmentOrders.getGbDoQuantity();
                String gbDoStandard = gbDepartmentOrders.getGbDoStandard();
                String gbDoRemark = gbDepartmentOrders.getGbDoRemark();
                gbDepartmentOrders.setGbDoApplyDate(formatWhatDay(0));
                gbDepartmentOrders.setGbDoApplyFullTime(formatWhatYearDayTime(0));
                gbDepartmentOrders.setGbDoApplyOnlyTime(formatWhatTime(0));
                gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDate(0));
                gbDepartmentOrders.setGbDoArriveWeeksYear(getWeekOfYear(0));
                String gbDoApplyArriveDate = gbDepartmentOrders.getGbDoApplyArriveDate();
                Integer gbDoDepartmentId = gbDepartmentOrders.getGbDoDepartmentId();
                Integer gbDoDistributerId = gbDepartmentOrders.getGbDoDistributerId();
                Integer gbDoDepartmentFatherId = gbDepartmentOrders.getGbDoDepartmentFatherId();
                Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
                Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
                //
                NxDepartmentOrdersEntity ordersEntity = new NxDepartmentOrdersEntity();
                ordersEntity.setNxDoDistributerId(nxDisId);
                ordersEntity.setNxDoDisGoodsId(nxDisGoodsId);
                ordersEntity.setNxDoQuantity(gbDoQuantity);
                ordersEntity.setNxDoStandard(gbDoStandard);
                ordersEntity.setNxDoRemark(gbDoRemark);


                if (ordersEntity.getNxDoStandard().equals(nxDistributerGoodsEntity.getNxDgGoodsStandardname())) {
                    BigDecimal willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                    BigDecimal orderWeight = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
                    BigDecimal subtotal = orderWeight.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);

                    ordersEntity.setNxDoPrice(nxDistributerGoodsEntity.getNxDgWillPriceOne());
                    ordersEntity.setNxDoCostPriceLevel("1");
                    ordersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
                    ordersEntity.setNxDoSubtotal(subtotal.toString());
                    gbDepartmentOrders.setGbDoPrice(nxDistributerGoodsEntity.getNxDgWillPrice());

                    System.out.println("heeehere" + nxDistributerGoodsEntity.getNxDgWillPrice());
                    System.out.println("heeehere" + gbDepartmentOrders.getGbDoPrice());
                    gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                    gbDepartmentOrders.setGbDoSubtotal(subtotal.toString());

                    ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPriceOne());
                    ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceOneUpdate());
                    BigDecimal buyPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPriceOne());
                    BigDecimal buySutotal = buyPrice.multiply(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoCostSubtotal(buySutotal.toString());
                    BigDecimal profit = subtotal.subtract(buySutotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoProfitSubtotal(profit.toString());
                    BigDecimal decimal = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP);
                    ordersEntity.setNxDoProfitScale(decimal.toString());
//

                } else {
                    ordersEntity.setNxDoCostSubtotal("0");
                    ordersEntity.setNxDoProfitSubtotal("0");
                    ordersEntity.setNxDoCostPrice("0");

                    ordersEntity.setNxDoCostPriceLevel("1");
                    ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPriceOne());
                    ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceOneUpdate());
                    if (nxDistributerGoodsEntity.getNxDgWillPriceTwoStandard() != null) {
                        if (gbDepartmentOrders.getGbDoStandard().equals(nxDistributerGoodsEntity.getNxDgWillPriceTwoStandard())) {
                            BigDecimal willPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                            BigDecimal orderWeight = new BigDecimal(gbDepartmentOrders.getGbDoQuantity());
                            BigDecimal subtotal = orderWeight.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);

                            ordersEntity.setNxDoPrice(nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                            ordersEntity.setNxDoCostPriceLevel("2");
                            ordersEntity.setNxDoWeight(gbDepartmentOrders.getGbDoQuantity());
                            ordersEntity.setNxDoSubtotal(subtotal.toString());
                            gbDepartmentOrders.setGbDoPrice(nxDistributerGoodsEntity.getNxDgWillPriceTwo());

                            System.out.println("heeehere" + nxDistributerGoodsEntity.getNxDgWillPriceTwo());
                            System.out.println("heeehere" + gbDepartmentOrders.getGbDoPrice());
                            gbDepartmentOrders.setGbDoWeight(gbDepartmentOrders.getGbDoQuantity());
                            gbDepartmentOrders.setGbDoSubtotal(subtotal.toString());

                            ordersEntity.setNxDoCostPrice(nxDistributerGoodsEntity.getNxDgBuyingPriceTwo());
                            ordersEntity.setNxDoCostPriceUpdate(nxDistributerGoodsEntity.getNxDgBuyingPriceTwoUpdate());
                            BigDecimal buyPrice = new BigDecimal(nxDistributerGoodsEntity.getNxDgBuyingPriceTwo());
                            BigDecimal buySutotal = buyPrice.multiply(orderWeight).setScale(1, BigDecimal.ROUND_HALF_UP);
                            ordersEntity.setNxDoCostSubtotal(buySutotal.toString());
                            BigDecimal profit = subtotal.subtract(buySutotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                            ordersEntity.setNxDoProfitSubtotal(profit.toString());
                            BigDecimal decimal = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP);
                            ordersEntity.setNxDoProfitScale(decimal.toString());
                        }
                    }
                }

                ordersEntity.setNxDoApplyDate(formatWhatDay(0));
                ordersEntity.setNxDoArriveOnlyDate(formatWhatDay(0));
                ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
                ordersEntity.setNxDoApplyFullTime(formatWhatYearDayTime(0));
                ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
                ordersEntity.setNxDoArriveDate(gbDoApplyArriveDate);
                ordersEntity.setNxDoGbDistributerId(gbDoDistributerId);
                ordersEntity.setNxDoGbDepartmentId(gbDoDepartmentId);
                ordersEntity.setNxDoGbDepartmentFatherId(gbDoDepartmentFatherId);
                ordersEntity.setNxDoDepartmentId(-1);
                ordersEntity.setNxDoDepartmentFatherId(-1);
                ordersEntity.setNxDoNxCommunityId(-1);
                ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
                ordersEntity.setNxDoNxCommRestrauntId(-1);
                ordersEntity.setNxDoNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
                ordersEntity.setNxDoNxGoodsFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoDepDisGoodsId(-1);
                ordersEntity.setNxDoArriveWhatDay(getWeek(0));
                ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
                ordersEntity.setNxDoGoodsType(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
                ordersEntity.setNxDoIsAgent(-1);
                ordersEntity.setNxDoPrintStandard(nxDistributerGoodsEntity.getNxDgGoodsStandardname());

//                ordersEntity.setNxDoCostPriceLevel("0");
                ordersEntity.setNxDoPurchaseUserId(-1);
                ordersEntity.setNxDoGbDepartmentOrderId(gbDepartmentOrders.getGbDepartmentOrdersId());
                if (nxDistributerGoodsEntity.getNxDgPurchaseAuto() == -1) {
                    ordersEntity.setNxDoPurchaseGoodsId(nxDistributerGoodsEntity.getNxDgPurchaseAuto());
                } else {
                    savePurGoodsAuto(ordersEntity);
                }

                ordersEntity.setNxDoStatus(getNxOrderStatusNew());
                depOrdersService.saveForGb(ordersEntity);
                Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();

                gbDepartmentOrders.setGbDoNxDepartmentOrderId(nxDepartmentOrdersId);
                gbDepartmentOrdersService.update(gbDepartmentOrders);


            }
        }

        // 添加 nxOrder

    }

//    private void savePurGoodsAuto(NxDepartmentOrdersEntity ordersEntity) {
//
//
//        Integer nxDistributerPurchaseGoodsId = 0;
//        //判断是否有已经分的
//
//        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
//        NxDistributerGoodsEntity disGoods = dgService.queryObject(doDisGoodsId);
//        Map<String, Object> map = new HashMap<>();
//        map.put("disGoodsId", doDisGoodsId);
//        map.put("status", 1);
//        System.out.println("purgogogo" + map);
//        NxDistributerPurchaseGoodsEntity havePurGoods = nxDisPurchaseGoodsService.queryIfHavePurGoods(map);
//        if (havePurGoods != null) {
//            havePurGoods.setNxDpgOrdersAmount(havePurGoods.getNxDpgOrdersAmount() + 1);
//            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
//            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
//                if (havePurGoods.getNxDpgBuyQuantity() != null) {
//                    BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgQuantity());
//                    BigDecimal decimal1 = new BigDecimal(ordersEntity.getNxDoQuantity());
//                    BigDecimal totaoWeight = decimal.add(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(havePurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    havePurGoods.setNxDpgQuantity(totaoWeight.toString());
//                    havePurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
//                    havePurGoods.setNxDpgBuySubtotal(decimal2.toString());
//                }
//
//            }
//
//            nxDisPurchaseGoodsService.update(havePurGoods);
//            nxDistributerPurchaseGoodsId = havePurGoods.getNxDistributerPurchaseGoodsId();
//
//        } else {
//
//            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
//            purchaseGoodsEntity.setNxDpgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
//            purchaseGoodsEntity.setNxDpgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
//            purchaseGoodsEntity.setNxDpgDistributerId(disGoods.getNxDgDistributerId());
//            purchaseGoodsEntity.setNxDpgApplyDate(formatWhatDay(0));
//            purchaseGoodsEntity.setNxDpgCostLevel(disGoods.getNxDgBuyingPriceIsGrade());
//            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
//            purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
//            purchaseGoodsEntity.setNxDpgOrdersAmount(1);
//            purchaseGoodsEntity.setNxDpgFinishAmount(0);
//            purchaseGoodsEntity.setNxDpgPurchaseType(1);
//            purchaseGoodsEntity.setNxDpgExpectPrice(disGoods.getNxDgBuyingPrice());
//            purchaseGoodsEntity.setNxDpgBuyPrice(disGoods.getNxDgBuyingPrice());
//            purchaseGoodsEntity.setNxDpgDisGoodsId(doDisGoodsId);
//            purchaseGoodsEntity.setNxDpgInputType(disGoods.getNxDgPurchaseAuto());
//            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
//            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
//                purchaseGoodsEntity.setNxDpgQuantity(ordersEntity.getNxDoQuantity());
//                BigDecimal totaoWeight = new BigDecimal(ordersEntity.getNxDoQuantity());
//                purchaseGoodsEntity.setNxDpgStandard(ordersEntity.getNxDoStandard());
//                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(purchaseGoodsEntity.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
//                purchaseGoodsEntity.setNxDpgQuantity(totaoWeight.toString());
//                purchaseGoodsEntity.setNxDpgBuyQuantity(totaoWeight.toString());
//                purchaseGoodsEntity.setNxDpgBuySubtotal(decimal2.toString());
//            }
//            nxDisPurchaseGoodsService.save(purchaseGoodsEntity);
//            nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
//
//            //给autoBatch更新gbDepartmentOrderid
//            if (disGoods.getNxDgSupplierId() != null) {
//                //
//                Map<String, Object> mapBatch = new HashMap<>();
//                Integer gbDgGbSupplierId = disGoods.getNxDgSupplierId();
//                mapBatch.put("supplierId", gbDgGbSupplierId);
//                mapBatch.put("status", 1);
//                mapBatch.put("purchaseType", 2);
//                List<NxDistributerPurchaseBatchEntity> entities = nxDPBService.queryDisPurchaseBatch(mapBatch);
//
//                if (entities.size() == 0) {
//                    //
//                    NxDistributerPurchaseBatchEntity batchEntity = new NxDistributerPurchaseBatchEntity();
//                    batchEntity.setNxDpbDate(formatWhatDay(0));
//                    batchEntity.setNxDpbTime(formatWhatTime(0));
//                    batchEntity.setNxDpbMonth(formatWhatMonth(0));
//                    batchEntity.setNxDpbPruchaseWeek(getWeek(0));
//                    batchEntity.setNxDpbYear(formatWhatYear(0));
//                    batchEntity.setNxDpbPayFullTime(formatWhatYearDayTime(0));
//                    batchEntity.setNxDpbStatus(-1);
//                    batchEntity.setNxDpbPurchaseType(2);
//                    batchEntity.setNxDpbSupplierId(gbDgGbSupplierId);
//                    NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDgGbSupplierId);
//                    batchEntity.setNxDpbSellUserId(supplierEntity.getNxJrdhsUserId());
//                    batchEntity.setNxDpbDistributerId(ordersEntity.getNxDoDistributerId());
//                    batchEntity.setNxDpbPurUserId(supplierEntity.getNxJrdhsNxPurUserId());
//                    batchEntity.setNxDpbBuyUserId(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
//                    NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
//                    batchEntity.setNxDpbBuyUserOpenId(nxJrdhUserEntity.getNxJrdhWxOpenId());
//                    nxDPBService.save(batchEntity);
//
//                    purchaseGoodsEntity.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
//                    purchaseGoodsEntity.setNxDpgStatus(getGbPurchaseGoodsStatusProcurement());
//                    purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
//                    purchaseGoodsEntity.setNxDpgTime(formatWhatYearDayTime(0));
//                    purchaseGoodsEntity.setNxDpgDistributerId(batchEntity.getNxDpbDistributerId());
//                    nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
//                } else {
//                    NxDistributerPurchaseBatchEntity batchEntity = entities.get(0);
//                    purchaseGoodsEntity.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
//                    purchaseGoodsEntity.setNxDpgStatus(getGbPurchaseGoodsStatusProcurement());
//                    nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
//                }
//            }
//        }
//        ordersEntity.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
//        nxDepartmentOrdersService.update(ordersEntity);
//
//    }

    private void savePurGoodsAuto(NxDepartmentOrdersEntity ordersEntity) {

        NxDistributerPurchaseGoodsEntity resultPurGoods = new NxDistributerPurchaseGoodsEntity();

        //判断是否有已经分的
        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity disGoods = dgService.queryObject(doDisGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", doDisGoodsId);
        map.put("status", 1);
        map.put("standard", ordersEntity.getNxDoStandard());
        System.out.println("purgogogo" + map);
        NxDistributerPurchaseGoodsEntity havePurGoods = nxDisPurchaseGoodsService.queryIfHavePurGoods(map);
        if (havePurGoods != null) {
            resultPurGoods = havePurGoods;
            havePurGoods.setNxDpgOrdersAmount(resultPurGoods.getNxDpgOrdersAmount() + 1);
            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                if (havePurGoods.getNxDpgBuyQuantity() != null) {
                    BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgQuantity());
                    BigDecimal decimal1 = new BigDecimal(ordersEntity.getNxDoQuantity());
                    BigDecimal totaoWeight = decimal.add(decimal1).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(havePurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                    resultPurGoods.setNxDpgQuantity(totaoWeight.toString());
                    resultPurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
                    resultPurGoods.setNxDpgBuySubtotal(decimal2.toString());
                }

            }
//            else {
//                BigDecimal decimal = new BigDecimal(resultPurGoods.getNxDpgQuantity());
//                BigDecimal purQuantity = new BigDecimal(resultPurGoods.getNxDpgQuantity());
//                BigDecimal add = decimal.add(purQuantity);
//                resultPurGoods.setNxDpgQuantity(add.toString());
//                resultPurGoods.setNxDpgBuyQuantity(add.toString());
//            }
            nxDisPurchaseGoodsService.update(resultPurGoods);

        } else {
            resultPurGoods.setNxDpgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
            resultPurGoods.setNxDpgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
            resultPurGoods.setNxDpgDistributerId(disGoods.getNxDgDistributerId());
            resultPurGoods.setNxDpgApplyDate(formatWhatDay(0));
//            resultPurGoods.setNxDpgCostLevel(disGoods.getNxDgBuyingPriceIsGrade());
            resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
            resultPurGoods.setNxDpgApplyDate(formatWhatYearDayTime(0));
            resultPurGoods.setNxDpgOrdersAmount(1);
            resultPurGoods.setNxDpgFinishAmount(0);
            resultPurGoods.setNxDpgPurchaseType(getGbPurchaseGoodsTypeForOrder());
            resultPurGoods.setNxDpgExpectPrice(disGoods.getNxDgBuyingPrice());
            resultPurGoods.setNxDpgBuyPrice(disGoods.getNxDgBuyingPrice());
            resultPurGoods.setNxDpgDisGoodsId(doDisGoodsId);
            resultPurGoods.setNxDpgInputType(disGoods.getNxDgPurchaseAuto());
            resultPurGoods.setNxDpgStandard(ordersEntity.getNxDoStandard());
            NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(doDisGoodsId);
            if (ordersEntity.getNxDoStandard().equals(distributerGoodsEntity.getNxDgGoodsStandardname())) {
                BigDecimal totaoWeight = new BigDecimal(ordersEntity.getNxDoQuantity());
                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(resultPurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                resultPurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
                resultPurGoods.setNxDpgQuantity(totaoWeight.toString());
                resultPurGoods.setNxDpgBuySubtotal(decimal2.toString());
            }
            nxDisPurchaseGoodsService.save(resultPurGoods);

        }

        NxDistributerPurchaseBatchEntity batchEntity = new NxDistributerPurchaseBatchEntity();

        //给autoBatch更新gbDepartmentOrderid
        System.out.println("suplieridid" + disGoods.getNxDgGoodsName() + disGoods.getNxDgSupplierId());
        if (disGoods.getNxDgSupplierId() != null) {
            //
            Map<String, Object> mapBatch = new HashMap<>();
            Integer gbDgGbSupplierId = disGoods.getNxDgSupplierId();
            mapBatch.put("supplierId", gbDgGbSupplierId);
            mapBatch.put("status", 1);
            mapBatch.put("purchaseType", 2);

            List<NxDistributerPurchaseBatchEntity> entities = nxDPBService.queryDisPurchaseBatch(mapBatch);
            if (entities.size() == 0) {
                //
                batchEntity.setNxDpbDate(formatWhatDay(0));
                batchEntity.setNxDpbTime(formatWhatTime(0));
                batchEntity.setNxDpbMonth(formatWhatMonth(0));
                batchEntity.setNxDpbPruchaseWeek(getWeek(0));
                batchEntity.setNxDpbYear(formatWhatYear(0));
                batchEntity.setNxDpbPayFullTime(formatWhatYearDayTime(0));
                batchEntity.setNxDpbStatus(-1);
                batchEntity.setNxDpbPurchaseType(2);
                batchEntity.setNxDpbSupplierId(gbDgGbSupplierId);
                NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(gbDgGbSupplierId);
                batchEntity.setNxDpbSellUserId(supplierEntity.getNxJrdhsUserId());
                batchEntity.setNxDpbDistributerId(ordersEntity.getNxDoDistributerId());
                batchEntity.setNxDpbPurUserId(supplierEntity.getNxJrdhsNxPurUserId());
                batchEntity.setNxDpbBuyUserId(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                batchEntity.setNxDpbBuyUserOpenId(nxJrdhUserEntity.getNxJrdhWxOpenId());
                nxDPBService.save(batchEntity);

                resultPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                resultPurGoods.setNxDpgPurchaseDate(formatWhatDay(0));
                resultPurGoods.setNxDpgTime(formatWhatYearDayTime(0));
                resultPurGoods.setNxDpgDistributerId(batchEntity.getNxDpbDistributerId());
                resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
                nxDisPurchaseGoodsService.update(resultPurGoods);
            } else {
                batchEntity = entities.get(0);
                resultPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                nxDisPurchaseGoodsService.update(resultPurGoods);
            }

            Map<String, TemplateData> mapNotice = new HashMap<>();
            mapNotice.put("thing1", new TemplateData(disGoods.getNxDgGoodsName()));
            mapNotice.put("time6", new TemplateData(formatWhatDay(0)));
            Integer nxDpbDistributerId = batchEntity.getNxDpbDistributerId();
            NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDpbDistributerId);
            mapNotice.put("thing7", new TemplateData(distributerEntity.getNxDistributerName()));
            mapNotice.put("thing8", new TemplateData(distributerEntity.getNxDistributerPhone()));
//
            StringBuilder pathBuilder = new StringBuilder("pages/txs/disOrderBatch/disOrderBatch");
            pathBuilder.append("?batchId=").append(batchEntity.getNxDistributerPurchaseBatchId());
            pathBuilder.append("&retName=").append(nxDistributerService.queryObject(batchEntity.getNxDpbDistributerId()).getNxDistributerName());
            pathBuilder.append("&disId=").append(batchEntity.getNxDpbDistributerId());
            pathBuilder.append("&buyUserId=").append(batchEntity.getNxDpbBuyUserId());
            pathBuilder.append("&purUserId=").append(batchEntity.getNxDpbPurUserId());
            pathBuilder.append("&fromBuyer=1");
            String path = pathBuilder.toString();
            System.out.println("paththhththt" + path);

            NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(disGoods.getNxDgSupplierId());
            Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);
            System.out.println("suppsleir" + path);
            WeNoticeService.nxSupplierOrderSave(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);

            ordersEntity.setNxDoPurchaseStatus(getNxDisPurchaseGoodsIsPurchase());
        }

        ordersEntity.setNxDoPurchaseGoodsId(resultPurGoods.getNxDistributerPurchaseGoodsId());
        nxDepartmentOrdersService.update(ordersEntity);

    }

    /**
     * 删除订单关联的采购商品
     * 如果采购商品只有一个订单，则删除采购商品；否则减少订单数量
     */
    private void deletePurchaseGoodsForOrder(NxDepartmentOrdersEntity orderEntity) {
        Integer purchaseGoodsId = orderEntity.getNxDoPurchaseGoodsId();
        if (purchaseGoodsId == null || purchaseGoodsId == -1) {
            return;
        }

        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDisPurchaseGoodsService.queryObject(purchaseGoodsId);
        if (purchaseGoodsEntity == null) {
            return;
        }

        Integer ordersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
        if (ordersAmount == null || ordersAmount <= 0) {
            return;
        }

        if (ordersAmount > 1) {
            // 减少订单数量并更新数量
            purchaseGoodsEntity.setNxDpgOrdersAmount(ordersAmount - 1);

            // 减少数量
            if (orderEntity.getNxDoStandard() != null &&
                    orderEntity.getNxDoStandard().equals(purchaseGoodsEntity.getNxDpgStandard()) &&
                    purchaseGoodsEntity.getNxDpgQuantity() != null) {
                try {
                    BigDecimal currentQuantity = new BigDecimal(purchaseGoodsEntity.getNxDpgQuantity());
                    BigDecimal orderQuantity = new BigDecimal(orderEntity.getNxDoQuantity());
                    BigDecimal newQuantity = currentQuantity.subtract(orderQuantity).setScale(1, BigDecimal.ROUND_HALF_UP);

                    purchaseGoodsEntity.setNxDpgQuantity(newQuantity.toString());
                    purchaseGoodsEntity.setNxDpgBuyQuantity(newQuantity.toString());

                    // 更新小计
                    if (purchaseGoodsEntity.getNxDpgBuyPrice() != null) {
                        BigDecimal price = new BigDecimal(purchaseGoodsEntity.getNxDpgBuyPrice());
                        BigDecimal newSubtotal = newQuantity.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                        purchaseGoodsEntity.setNxDpgBuySubtotal(newSubtotal.toString());
                    }
                } catch (Exception e) {
                    logger.error("计算采购商品数量时出错", e);
                }
            }

            nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
        } else {
            // 只有一个订单，删除采购商品
            Integer batchId = purchaseGoodsEntity.getNxDpgBatchId();

            // 如果有关联的批次，检查批次中是否只有这一个商品
            if (batchId != null) {
                Map<String, Object> mapBatch = new HashMap<>();
                mapBatch.put("batchId", batchId);
                List<NxDistributerPurchaseGoodsEntity> batchGoodsList = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(mapBatch);
                if (batchGoodsList != null && batchGoodsList.size() == 1) {
                    // 批次中只有这一个商品，删除批次
                    nxDPBService.delete(batchId);
                }
            }

            // 删除采购商品
            nxDisPurchaseGoodsService.delete(purchaseGoodsId);
        }
    }

    private NxDistributerGoodsEntity downLoadNxGoodsForGbDisGoods(Integer gbDisGoodsId, NxGoodsEntity nxGoodsEntity, Integer nxdisId) {
        System.out.println("downLoadNxGoodsForGbDisGoods-----");
        NxDistributerGoodsEntity cgnGoods = new NxDistributerGoodsEntity();
        cgnGoods.setNxDgDistributerId(nxdisId);
        cgnGoods.setNxDgNxGoodsId(nxGoodsEntity.getNxGoodsId());
        cgnGoods.setNxDgGoodsName(nxGoodsEntity.getNxGoodsName());
        cgnGoods.setNxDgNxFatherId(nxGoodsEntity.getNxGoodsFatherId());

        Integer nxGoodsFatherId = nxGoodsEntity.getNxGoodsFatherId();
        NxGoodsEntity fatherNxGoods = nxGoodsService.queryObject(nxGoodsFatherId);

        cgnGoods.setNxDgNxFatherImg(fatherNxGoods.getNxGoodsFile());
        cgnGoods.setNxDgNxFatherName(fatherNxGoods.getNxGoodsName());
        cgnGoods.setNxDgGoodsDetail(nxGoodsEntity.getNxGoodsDetail());
        cgnGoods.setNxDgGoodsPlace(nxGoodsEntity.getNxGoodsPlace());
        cgnGoods.setNxDgGoodsBrand(nxGoodsEntity.getNxGoodsBrand());
        cgnGoods.setNxDgGoodsStandardname(nxGoodsEntity.getNxGoodsStandardname());
        cgnGoods.setNxDgGoodsStandardWeight(nxGoodsEntity.getNxGoodsStandardWeight());
        cgnGoods.setNxDgGoodsPy(nxGoodsEntity.getNxGoodsPy());
        cgnGoods.setNxDgGoodsPinyin(nxGoodsEntity.getNxGoodsPinyin());
        cgnGoods.setNxDgPullOff(0);
        cgnGoods.setNxDgGoodsStatus(0);
        cgnGoods.setNxDgPurchaseAuto(-1);

        NxDistributerGoodsEntity distributerGoodsEntity = saveDisGoods(cgnGoods);

        //2，保存dis规格bieming
        Integer nxDistributerGoodsId = cgnGoods.getNxDistributerGoodsId();
        //2.1
        Integer nxDgNxGoodsId = cgnGoods.getNxDgNxGoodsId();
        List<NxStandardEntity> nxStandardEntities = nxStandardService.queryGoodsStandardListByGoodId(nxDgNxGoodsId);
        if (nxStandardEntities.size() > 0) {
            for (NxStandardEntity standard : nxStandardEntities) {
                NxDistributerStandardEntity disStandards = new NxDistributerStandardEntity();
                disStandards.setNxDsDisGoodsId(nxDistributerGoodsId);
                disStandards.setNxDsStandardName(standard.getNxStandardName());
                disStandards.setNxDsStandardError(standard.getNxStandardError());
                disStandards.setNxDsStandardScale(standard.getNxStandardScale());
                disStandards.setNxDsStandardFilePath(standard.getNxStandardFilePath());
                disStandards.setNxDsStandardSort(standard.getNxStandardSort());
                dsService.save(disStandards);
            }
        }

//        2.2

        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", nxDgNxGoodsId);
        List<NxAliasEntity> aliasEntities = nxAliasService.queryNxAliasList(map);
        if (aliasEntities.size() > 0) {
            for (NxAliasEntity aliasEntity : aliasEntities) {
                NxDistributerAliasEntity disAlias = new NxDistributerAliasEntity();
                disAlias.setNxDaDisGoodsId(nxDistributerGoodsId);
                disAlias.setNxDaAliasName(aliasEntity.getNxAliasName());
                disAliasService.save(disAlias);
            }
        }


        return distributerGoodsEntity;
    }


    @RequestMapping(value = "/getLinshiAll")
    @ResponseBody
    public R getLinshiAll() {

//        List<NxDistributerEntity> goodsEntities = dgService.queryLinshiGoodsForNx();

        List<NxDistributerEntity> nxDistributerEntities = nxDistributerService.queryAllTypeOne();
        if (nxDistributerEntities.size() > 0) {
            for (NxDistributerEntity distributerEntity : nxDistributerEntities) {
                int count = dgService.queryLinshiGoodsAcount(distributerEntity.getNxDistributerId());
                distributerEntity.setLinshiCount(count);
            }
        }

        List<GbDistributerEntity> gbDistributerEntities = gbDistributerService.queryListAll();
        if (gbDistributerEntities.size() > 0) {
            for (GbDistributerEntity distributerEntity : gbDistributerEntities) {
                int count = gbDgService.queryLinshiGoodsAcount(distributerEntity.getGbDistributerId());
                distributerEntity.setLinshiCout(count);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("nxArr", nxDistributerEntities);
        map.put("gbArr", gbDistributerEntities);
        return R.ok().put("data", map);

    }


    @RequestMapping(value = "/helpSaveLinshi/{id}")
    @ResponseBody
    public R helpSaveLinshi(@PathVariable Integer id) {
        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(id);
        distributerGoodsEntity.setNxDgGoodsStatus(-1);
        dgService.update(distributerGoodsEntity);


        return R.ok();
    }


    @RequestMapping(value = "/disSaveSelfGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disSaveSelfGoods(@RequestBody NxDistributerGoodsEntity goods) {
        if (goods.getNxDgDfgGoodsGrandId() != null) {
            Integer nxDgDfgGoodsGrandId = goods.getNxDgDfgGoodsGrandId();
            //update behindFatherSort
            Map<String, Object> mapSrot = new HashMap<>();
            mapSrot.put("fathersFatherId", nxDgDfgGoodsGrandId);
            mapSrot.put("dayuSort", goods.getNxDgGoodsSort());
            System.out.println("sororomdai" + mapSrot);
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(mapSrot);
            if (fatherGoodsEntities.size() > 0) {
                for (NxDistributerFatherGoodsEntity sortFather : fatherGoodsEntities) {
                    System.out.println("ssofoorfafeeh=======" + sortFather.getNxDfgFatherGoodsName() + "fan==" + sortFather.getNxDfgFatherGoodsSort());
                    sortFather.setNxDfgFatherGoodsSort(sortFather.getNxDfgFatherGoodsSort() + 1);
                    nxDistributerFatherGoodsService.update(sortFather);
                }
            }
            //update behindGoodsSort
            Map<String, Object> mapSrotG = new HashMap<>();
            mapSrotG.put("dayuSort", goods.getNxDgGoodsSort());
            mapSrotG.put("grandId", nxDgDfgGoodsGrandId);
            System.out.println("sororogggggg" + mapSrotG);
            List<NxDistributerGoodsEntity> sortGoods = dgService.queryDisGoodsByParams(mapSrotG);
            if (sortGoods.size() > 0) {
                for (NxDistributerGoodsEntity sortEntity : sortGoods) {
                    System.out.println("sorenenenennenenne" + sortEntity.getNxDgGoodsName() + "sort=" + sortEntity.getNxDgGoodsSort());
                    sortEntity.setNxDgGoodsSort(sortEntity.getNxDgGoodsSort() + 1);
                    dgService.update(sortEntity);
                }
            }

            NxDistributerFatherGoodsEntity grandGoods = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsGrandId);
            //add-fatherGoods
            NxDistributerFatherGoodsEntity fatherGoodsEntity = new NxDistributerFatherGoodsEntity();
            fatherGoodsEntity.setNxDfgFathersFatherId(nxDgDfgGoodsGrandId);
            fatherGoodsEntity.setNxDfgFatherGoodsName(goods.getNxDgGoodsName());
            fatherGoodsEntity.setNxDfgDistributerId(grandGoods.getNxDfgDistributerId());
            fatherGoodsEntity.setNxDfgGoodsAmount(1);
            fatherGoodsEntity.setNxDfgNxGoodsId(-1);
            fatherGoodsEntity.setNxDfgFatherGoodsLevel(2);
            fatherGoodsEntity.setNxDfgFatherGoodsSort(goods.getNxDgGoodsSort());
            fatherGoodsEntity.setNxDfgFatherGoodsColor(grandGoods.getNxDfgFatherGoodsColor());
            nxDistributerFatherGoodsService.save(fatherGoodsEntity);


            //update-Data
            Integer linshiId = goods.getNxDgNxGoodsId();
            NxDistributerGoodsEntity linshiGoodsEntity = dgService.queryObject(linshiId);

            //add-goods
            linshiGoodsEntity.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
            linshiGoodsEntity.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
            linshiGoodsEntity.setNxDgPurchaseAuto(-1);
            linshiGoodsEntity.setNxDgWillPrice("0.1");
            linshiGoodsEntity.setNxDgBuyingPrice("0.1");
            linshiGoodsEntity.setNxDgGoodsIsHidden(0);
            linshiGoodsEntity.setNxDgNxFatherImg("goodsImage/logo.jpg");
            linshiGoodsEntity.setNxDgNxFatherId(-1);
            linshiGoodsEntity.setNxDgNxGrandId(-1);
            linshiGoodsEntity.setNxDgNxGreatGrandId(-1);
            linshiGoodsEntity.setNxDgIsOldestSon(1);
            linshiGoodsEntity.setNxDgGoodsSonsSort(1);
            linshiGoodsEntity.setNxDgNxGoodsId(null);
            dgService.update(linshiGoodsEntity);

            updateLinshiGoodsData(linshiId, goods.getNxDistributerGoodsId(), goods.getNxDgDistributerId());
            return R.ok();
        } else {
            return R.error(-1, "必填项内容不全");
        }

    }

    private void updateLinshiGoodsData(Integer lsGoodsId, Integer nxGoodsId, Integer disId) {

        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(lsGoodsId);
        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();

        Map<String, Object> linshiMap = new HashMap<>();
        linshiMap.put("disGoodsId", lsGoodsId);
        linshiMap.put("disId", disId);
        System.out.println("dadmlisisosopsspsp" + linshiMap);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(linshiMap);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoNxGoodsId(-1);
                ordersEntity.setNxDoNxGoodsFatherId(-1);
                depOrdersService.update(ordersEntity);
            }
        }

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(linshiMap);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                depGoodsEntity.setNxDdgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                depGoodsEntity.setNxDdgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                Integer greatFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
                depGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);

                nxDepartmentDisGoodsService.update(depGoodsEntity);
            }
        }

        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(linshiMap);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        List<NxDistributerGoodsShelfGoodsEntity> entities = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(linshiMap);
        if (entities.size() > 0) {
            for (NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity : entities) {
                shelfGoodsEntity.setNxDgsgDisGoodsId(nxGoodsId);
                nxDistributerGoodsShelfGoodsService.update(shelfGoodsEntity);
            }
        }


        System.out.println("lishsisi" + lsGoodsId);
        List<GbDistributerEntity> distributerEntities = gbDistributerGoodsService.queryGbDisByNxGoodsId(lsGoodsId);
        if (distributerEntities.size() > 0) {
            for (GbDistributerEntity gbDistributerEntity : distributerEntities) {
                updateGbDisGoods(lsGoodsId, nxDistributerGoodsEntity.getNxDistributerGoodsId(), disId, gbDistributerEntity);
            }
        }

//        dgService.delete(lsGoodsId);

    }


    @RequestMapping(value = "/disGetUpdatePriceGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetUpdatePriceGoods(Integer disId, String type, Integer limit, Integer page) {
        if (type.equals("sell")) {
            Map<String, Object> mapPrice = new HashMap<>();
            mapPrice.put("disId", disId);
            mapPrice.put("willPrice", 0.1);
            mapPrice.put("offset", (page - 1) * limit);
            mapPrice.put("limit", limit);
            List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisGoodsByParams(mapPrice);

            // 检查是否有重复的商品ID
            Set<Integer> goodsIds = new HashSet<>();
            List<Integer> duplicateIds = new ArrayList<>();
            for (NxDistributerGoodsEntity goods : distributerGoodsEntities) {
                if (goods.getNxDistributerGoodsId() != null) {
                    if (goodsIds.contains(goods.getNxDistributerGoodsId())) {
                        duplicateIds.add(goods.getNxDistributerGoodsId());
                    } else {
                        goodsIds.add(goods.getNxDistributerGoodsId());
                    }
                }
            }
            if (!duplicateIds.isEmpty()) {
                logger.warn("发现重复的商品ID: {}", duplicateIds);
            }
            logger.info("查询返回商品数量: {}, 唯一商品数量: {}", distributerGoodsEntities.size(), goodsIds.size());

            // 查询总数
            Map<String, Object> mapTotal = new HashMap<>();
            mapTotal.put("disId", disId);
            mapTotal.put("willPrice", 0.1);
            int total = dgService.queryDisGoodsTotal(mapTotal);
            PageUtils pageUtil = new PageUtils(distributerGoodsEntities, total, limit, page);
            return R.ok().put("page", pageUtil);


        } else if (type.equals("buyingPrice")) {
            Map<String, Object> mapBuyPrice = new HashMap<>();
            mapBuyPrice.put("disId", disId);
            mapBuyPrice.put("buyPrice", 0.1);
            mapBuyPrice.put("offset", (page - 1) * limit);
            mapBuyPrice.put("limit", limit);
            List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisGoodsByParams(mapBuyPrice);

            // 检查是否有重复的商品ID
            Set<Integer> goodsIds2 = new HashSet<>();
            List<Integer> duplicateIds2 = new ArrayList<>();
            for (NxDistributerGoodsEntity goods : distributerGoodsEntities) {
                if (goods.getNxDistributerGoodsId() != null) {
                    if (goodsIds2.contains(goods.getNxDistributerGoodsId())) {
                        duplicateIds2.add(goods.getNxDistributerGoodsId());
                    } else {
                        goodsIds2.add(goods.getNxDistributerGoodsId());
                    }
                }
            }
            if (!duplicateIds2.isEmpty()) {
                logger.warn("发现重复的商品ID: {}", duplicateIds2);
            }
            logger.info("查询返回商品数量: {}, 唯一商品数量: {}", distributerGoodsEntities.size(), goodsIds2.size());

            // 查询总数
            Map<String, Object> mapTotal = new HashMap<>();
            mapTotal.put("disId", disId);
            mapTotal.put("buyPrice", 0.1);
            int total = dgService.queryDisGoodsTotal(mapTotal);
            PageUtils pageUtil = new PageUtils(distributerGoodsEntities, total, limit, page);
            return R.ok().put("page", pageUtil);
        }
        return R.ok();
    }


    @RequestMapping(value = "/abcdefgh")
    @ResponseBody
    public R abcdefg() {

//           for(int i = 1; i < 13; i++){
//               Map<String, Object> map = new HashMap<>();
//               map.put("disId", 1);
//               map.put("nxGreatGrandFatherId", i);
//               List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = dgService.queryDisGoodsByParams(map);
//               if(nxDistributerGoodsEntities.size() > 0){
//                   for(NxDistributerGoodsEntity distributerGoodsEntity: nxDistributerGoodsEntities){
//                       Map<String, Object> mapG = new HashMap<>();
//                       mapG.put("disGoodsId", distributerGoodsEntity.getNxDistributerGoodsId());
//                       List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapG);
//                       if(ordersEntities.size() == 0){
//                           dgService.delete(distributerGoodsEntity.getNxDistributerGoodsId());
////                           Integer nxDgDfgGoodsFatherId = distributerGoodsEntity.getNxDgDfgGoodsFatherId();
////                           nxDistributerFatherGoodsService.delete(nxDgDfgGoodsFatherId);
//                       }
//                   }
//               }
//           }


        Map<String, Object> map = new HashMap<>();
        map.put("goodsLevel", 2);
        map.put("disId", 1);

        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);

        if (fatherGoodsEntities.size() > 0) {
            for (NxDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                Map<String, Object> mapD = new HashMap<>();
                mapD.put("dgFatherId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
                List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = dgService.queryDisGoodsByParams(mapD);
                if (nxDistributerGoodsEntities.size() == 0) {
                    nxDistributerFatherGoodsService.delete(fatherGoodsEntity.getNxDistributerFatherGoodsId());
                }
            }
        }

        return R.ok();
    }


    @RequestMapping(value = "/saveSupplierGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveSupplierGoods(String ids, Integer supplierId) {
        String[] arr = ids.split(",");
        if (arr.length > 0) {
            for (String id : arr) {
                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(Integer.valueOf(id));
                distributerGoodsEntity.setNxDgSupplierId(supplierId);
                distributerGoodsEntity.setNxDgPurchaseAuto(1);
                dgService.update(distributerGoodsEntity);
            }
        }

        return R.ok();
    }


    @RequestMapping(value = "/saveShelfGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveShelfGoods(String ids, Integer shelfId) {

        Map<String, Object> map = new HashMap<>();
        NxDistributerGoodsShelfEntity shelfEntity = nxDistributerGoodsShelfService.queryObject(shelfId);

        map.put("shelfId", shelfId);
        int count = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsCount(map);
        String[] arr = ids.split(",");
        if (arr.length > 0) {
            for (int i = 0; i < arr.length; i++) {
                Integer id = Integer.valueOf(arr[i]);
                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(id);
                distributerGoodsEntity.setNxDgPurchaseAuto(-1);
                dgService.update(distributerGoodsEntity);

                NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity = new NxDistributerGoodsShelfGoodsEntity();
                shelfGoodsEntity.setNxDgsgDisGoodsId(Integer.valueOf(id));
                shelfGoodsEntity.setNxDgsgShelfId(shelfId);
                shelfGoodsEntity.setNxDgsgSort(count + i + 1);
                shelfGoodsEntity.setNxDgsgShelfSort(shelfEntity.getNxDistributerGoodsShelfSort());
                nxDistributerGoodsShelfGoodsService.save(shelfGoodsEntity);
            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/saveAutoPurchase", method = RequestMethod.POST)
    @ResponseBody
    public R saveAutoPurchase(String ids, Integer purchaseType) {
        String[] arr = ids.split(",");
        if (arr.length > 0) {
            for (int i = 0; i < arr.length; i++) {
                Integer id = Integer.valueOf(arr[i]);
                NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(id);
                distributerGoodsEntity.setNxDgPurchaseAuto(purchaseType);
                dgService.update(distributerGoodsEntity);
            }
        }
        return R.ok();
    }


    @RequestMapping(value = "/supplierGetDisGoodsFather", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGetDisGoodsFather(Integer disId, Integer supplierId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("supplierId", supplierId);
        map.put("autoType", 1);
        System.out.println("damfdanfas" + map);
        List<NxGoodsEntity> fatherGoodsEntities = dgService.querySupplierFather(map);
        return R.ok().put("data", fatherGoodsEntities);
    }


    @RequestMapping(value = "/supplierGetDisGoodsCata", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGetDisGoodsCata(Integer disId, Integer supplierId) {

//        NxJrdhSupplierEntity supplierEntity = nxJrdhSupplierService.querySupplierByUserId(sellerId);
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("supplierId", supplierId);
        map.put("autoType", 1);
        map.put("isHidden", 0);
        System.out.println("suppscata" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgService.querySupplierGrand(map);
        System.out.println("fathheeee" + fatherGoodsEntities.size());
        return R.ok().put("data", fatherGoodsEntities);
    }


    @RequestMapping(value = "/supplierGetGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGetGoodsListByFatherId(Integer fatherId, Integer supplierId,
                                            Integer limit, Integer page) {


        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("grandId", fatherId);
        map.put("supplierId", supplierId);
        map.put("autoType", 1);
        map.put("isHidden", 0);
        List<NxDistributerGoodsEntity> goodsEntities1 = dgService.queryDisGoodsByParams(map);


        Map<String, Object> map3 = new HashMap<>();
        map3.put("grandId", fatherId);
        map3.put("supplierId", supplierId);
        map3.put("isHidden", 0);
        map3.put("autoType", 1);
        System.out.println(map3 + "map3333");
        int total = dgService.queryDisGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/saveNxDisGoodsForCash", method = RequestMethod.POST)
    @ResponseBody
    public R saveNxDisGoodsForCash(@RequestBody NxDistributerGoodsEntity goods) {

        String goodsName = goods.getNxDgGoodsName();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", goods.getNxDgDistributerId());
        map.put("nxGoodsId", -1);
        map.put("goodsLevel", 2);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
        goods.setNxDgPurchaseAuto(-1);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        goods.setNxDgGoodsPinyin(pinyin);
        goods.setNxDgGoodsPy(headPinyin);
        goods.setNxDgBuyingPrice("0.1");
        goods.setNxDgWillPrice("0.1");
        goods.setNxDgBuyingPriceOne("0.1");
        goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
        goods.setNxDgWillPriceOne("0.1");
        goods.setNxDgWillPriceOneAboutPrice("0.1");
        goods.setNxDgGoodsIsHidden(0);
        goods.setNxDgOutTotalWeight("0");
        dgService.save(goods);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
        fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
        dgfService.update(fatherGoodsEntity);

        return R.ok().put("data", goods);
    }


    @ResponseBody
    @RequestMapping(value = "/updateFatherNx", produces = "text/html;charset=UTF-8")
    public R updateFatherNx(@RequestParam("file") MultipartFile file,
                            @RequestParam("goodsName") String goodsName,
                            @RequestParam("id") Integer id,
                            HttpSession session) {

        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(id);
        String oldPath = distributerGoodsEntity.getNxDgGoodsFile();
        if (oldPath != null && !oldPath.trim().isEmpty()) {
            String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
            File file1 = new File(oldAbsolutePath);
            if (file1.exists()) {
                file1.delete();
            }
        }


        //1,上传图片
        String newUploadName = "goodsImage";
        String originalName = goodsName;
        originalName = originalName.replaceAll("[\\\\/:*?\"<>|]", "");
        String englishKuohao = getEnglishKuohao(originalName);
        String lastFileName = originalName + formatFullTime();
        String pinyin = hanziToPinyin(englishKuohao);
        String headPinyin = getHeadStringByString(englishKuohao, false, null);
        distributerGoodsEntity.setNxDgGoodsPinyin(pinyin);
        distributerGoodsEntity.setNxDgGoodsPy(headPinyin);
        String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + lastFileName + ".jpg";
        distributerGoodsEntity.setNxDgGoodsFile(filePath);
        distributerGoodsEntity.setNxDgGoodsName(englishKuohao);
        dgService.update(distributerGoodsEntity);

        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/updateFatherBigNx", produces = "text/html;charset=UTF-8")
    public R updateFatherBigNx(@RequestParam("file") MultipartFile file,
                               @RequestParam("goodsName") String goodsName,
                               @RequestParam("id") Integer id,
                               HttpSession session) {

        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(id);
        String oldPath = distributerGoodsEntity.getNxDgGoodsFileLarge();
        if (oldPath != null && !oldPath.trim().isEmpty()) {
            String oldAbsolutePath = Constant.EXTERNAL_IMAGE_DIR + oldPath;
            File file1 = new File(oldAbsolutePath);
            if (file1.exists()) {
                file1.delete();
            }
        }


        //1,上传图片
        String newUploadName = "goodsImage";
        String originalName = goodsName;
        originalName = originalName.replaceAll("[\\\\/:*?\"<>|]", "");
        String englishKuohao = getEnglishKuohao(originalName);
        String lastFileName = originalName + formatFullTime() + "large";
        String pinyin = hanziToPinyin(englishKuohao);
        String headPinyin = getHeadStringByString(englishKuohao, false, null);
        distributerGoodsEntity.setNxDgGoodsPinyin(pinyin);
        distributerGoodsEntity.setNxDgGoodsPy(headPinyin);
        String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + lastFileName + ".jpg";
        System.out.println("biddiidid" + lastFileName);
        distributerGoodsEntity.setNxDgGoodsFileLarge(filePath);
        distributerGoodsEntity.setNxDgGoodsName(englishKuohao);
        dgService.update(distributerGoodsEntity);

        return R.ok();
    }


    @ResponseBody
    @RequestMapping(value = "/saveLinshiGoods", produces = "text/html;charset=UTF-8")
    public NxDistributerGoodsEntity saveFather(@RequestParam("file") MultipartFile file,
                                               @RequestParam("goodsName") String goodsName,
                                               @RequestParam("standard") String standard,
                                               @RequestParam("detail") String detail,
                                               @RequestParam("disId") Integer disId,
                                               HttpSession session) {

        //1,上传图片
        String newUploadName = "goodsImage";
        String originalName = goodsName;
        originalName = originalName.replaceAll("[\\\\/:*?\"<>|]", "");
        String englishKuohao = getEnglishKuohao(originalName);
        String lastFileName = getXiegang(englishKuohao) + formatFullTime();

        String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
        String filename = file.getOriginalFilename();
        String filePath = newUploadName + "/" + lastFileName + ".jpg";

        NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("nxGoodsId", -1);
        map.put("goodsLevel", 2);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
        goods.setNxDgPurchaseAuto(-1);
        goods.setNxDgDistributerId(disId);
        goods.setNxDgGoodsFile(filePath);
        goods.setNxDgGoodsFileLarge(filePath);
        goods.setNxDgGoodsName(goodsName);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        goods.setNxDgGoodsPinyin(pinyin);
        goods.setNxDgGoodsPy(headPinyin);
        goods.setNxDgDistributerId(disId);
        goods.setNxDgBuyingPriceIsGrade(0);
        goods.setNxDgBuyingPrice("0.1");
        goods.setNxDgWillPrice("0.1");
        goods.setNxDgBuyingPriceOne("0.1");
        goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
        goods.setNxDgWillPriceOne("0.1");
        goods.setNxDgWillPriceOneAboutPrice("0.1");
        goods.setNxDgGoodsIsHidden(0);
        goods.setNxDgGoodsStandardname(standard);
        goods.setNxDgGoodsDetail(detail);
        goods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
        System.out.println("savegoogog" + goods);
        goods.setNxDgOutTotalWeight("0");
        goods.setNxDgGoodsStatus(0);
        dgService.save(goods);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
        fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
        dgfService.update(fatherGoodsEntity);

        return goods;
    }

    //disChangeLinshiToSub

    @RequestMapping(value = "/disChangeLinshiToSub", method = RequestMethod.POST)
    @ResponseBody
    public R disChangeLinshiToSub(Integer lsGoodsId, Integer nxGoodsId, Integer disId) {


        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxGoodsId);

        String goodsName = nxDistributerGoodsEntity.getNxDgGoodsName();
        String nxGoodsDetail = nxDistributerGoodsEntity.getNxDgGoodsDetail();
        String nxGoodsBrand = nxDistributerGoodsEntity.getNxDgGoodsBrand();
        String nxDgGoodsStandardname = nxDistributerGoodsEntity.getNxDgGoodsStandardname();
        String nxDgGoodsStandardWeight = nxDistributerGoodsEntity.getNxDgGoodsStandardWeight();

        Map<String, Object> mapS = new HashMap<>();
        mapS.put("goodsName", goodsName);
        mapS.put("goodsStandard", nxDgGoodsStandardname);
        mapS.put("goodsDetail", nxGoodsDetail);
        mapS.put("goodsBrand", nxGoodsBrand);
        mapS.put("standardWeight", nxDgGoodsStandardWeight);
        mapS.put("disId", nxDistributerGoodsEntity.getNxDgDistributerId());
        mapS.put("fatherId", nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
        System.out.println("mapaidididpdppdpdpdpdpdpdpdp" + mapS);
        List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = dgService.queryIfHasSameDisGoods(mapS);
        if (nxDistributerGoodsEntities.size() > 1) {

            return R.error(-1, "已有相同商品");

        } else {
            Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
            Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
            Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();
            Integer nxDgNxFatherId = nxDistributerGoodsEntity.getNxDgNxFatherId();

            NxDistributerGoodsEntity linshiGoods = dgService.queryObject(lsGoodsId);
            linshiGoods.setNxDgDfgGoodsFatherId(nxDgDfgGoodsFatherId);
            linshiGoods.setNxDgDfgGoodsGrandId(nxDgDfgGoodsGrandId);
            linshiGoods.setNxDgNxFatherId(nxDgNxFatherId);
            linshiGoods.setNxDgNxGoodsId(null);
            linshiGoods.setNxDgIsOldestSon(0);
            linshiGoods.setNxDgGoodsStatus(1);
            linshiGoods.setNxDgNxGrandId(nxDistributerGoodsEntity.getNxDgNxGrandId());
            linshiGoods.setNxDgNxGreatGrandId(nxDistributerGoodsEntity.getNxDgNxGreatGrandId());
            linshiGoods.setNxDgGoodsSort(nxDistributerGoodsEntity.getNxDgGoodsSort());
            Map<String, Object> map = new HashMap<>();
            map.put("fatherId", nxDgDfgGoodsFatherId);
            int wxCountAuto = dgService.queryDisGoodsTotal(map);
            linshiGoods.setNxDgGoodsSonsSort(wxCountAuto + 1);
            dgService.update(linshiGoods);

            //update FatherGoods
            NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsFatherId);
            fatherGoodsEntity.setNxDfgGoodsAmount(fatherGoodsEntity.getNxDfgGoodsAmount() + 1);
            nxDistributerFatherGoodsService.update(fatherGoodsEntity);

            Map<String, Object> linshiMap = new HashMap<>();
            linshiMap.put("disGoodsId", lsGoodsId);
            List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(linshiMap);
            if (ordersEntities.size() > 0) {
                for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                    ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                    ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                    depOrdersService.update(ordersEntity);
                }
            }

            Map<String, Object> linshiMapH = new HashMap<>();
            linshiMapH.put("disGoodsId", lsGoodsId);
            List<NxDepartmentOrderHistoryEntity> ordersEntitiesH = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(linshiMapH);
            if (ordersEntitiesH.size() > 0) {
                for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesH) {
                    ordersEntity.setNxDoDisGoodsId(nxGoodsId);
                    ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                    ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                    nxDepartmentOrderHistoryService.update(ordersEntity);
                }
            }
            List<NxDepartmentOrdersHistoryEntity> linshiMapHHS = nxDepartmentOrdersHistoryService.queryDepHistoryOrdersByParams(linshiMapH);
            if (linshiMapHHS.size() > 0) {
                for (NxDepartmentOrdersHistoryEntity ordersEntity : linshiMapHHS) {
                    ordersEntity.setNxDohDisGoodsId(nxGoodsId);
                    nxDepartmentOrdersHistoryService.update(ordersEntity);
                }
            }

            List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(linshiMap);
            if (departmentDisGoodsEntities.size() > 0) {
                for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                    depGoodsEntity.setNxDdgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    depGoodsEntity.setNxDdgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    NxDistributerFatherGoodsEntity grand = nxDistributerFatherGoodsService.queryObject(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                    Integer greatFatherId = grand.getNxDfgFathersFatherId();
                    depGoodsEntity.setNxDdgDisGoodsGreatId(greatFatherId);

                    nxDepartmentDisGoodsService.update(depGoodsEntity);
                }
            }


            List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(linshiMap);
            if (purchaseGoodsEntities.size() > 0) {
                for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                    purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
                }
            }

            return R.ok();

        }

    }


    //todo test


    //todo test

    @RequestMapping(value = "/exchangeDisGoods", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeDisGoods(Integer lsGoodsId, Integer nxGoodsId, Integer disId) {

        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disId", disId);
        mapG.put("nxGoodsId", nxGoodsId);
        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryOneGoodsAboutNxGoods(mapG);
        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
        Integer distributerGoodsId = nxDistributerGoodsEntity.getNxDistributerGoodsId();
        Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();
        Integer nxDgNxFatherId = nxDistributerGoodsEntity.getNxDgNxFatherId();

        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", lsGoodsId);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsId(distributerGoodsId);
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                depOrdersService.update(ordersEntity);
                nxDistributerGoodsEntity.setNxDgWillPrice(ordersEntity.getNxDoPrice());
                nxDistributerGoodsEntity.setNxDgBuyingPriceOne(ordersEntity.getNxDoPrice());
                nxDistributerGoodsEntity.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
                dgService.update(nxDistributerGoodsEntity);
            }
        }

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                depGoodsEntity.setNxDdgDisGoodsId(distributerGoodsId);
                depGoodsEntity.setNxDdgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                depGoodsEntity.setNxDdgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                NxDistributerFatherGoodsEntity grandEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsGrandId);
                Integer nxDfgGreatId = grandEntity.getNxDfgFathersFatherId();
                depGoodsEntity.setNxDdgDisGoodsGreatId(nxDfgGreatId);

                nxDepartmentDisGoodsService.update(depGoodsEntity);
            }
        }

        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(map);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsId(distributerGoodsId);
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        List<NxDistributerGoodsShelfGoodsEntity> entities = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(map);
        if (entities.size() > 0) {
            for (NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity : entities) {
                shelfGoodsEntity.setNxDgsgDisGoodsId(distributerGoodsId);
                nxDistributerGoodsShelfGoodsService.update(shelfGoodsEntity);
            }
        }

        System.out.println("lishsisi" + lsGoodsId);
//        List<GbDistributerEntity> distributerEntities = gbDistributerGoodsService.queryGbDisByNxGoodsId(lsGoodsId);
//        if (distributerEntities.size() > 0) {
//            for (GbDistributerEntity gbDistributerEntity : distributerEntities) {
//                updateGbDisGoods(lsGoodsId, nxDistributerGoodsEntity.getNxDistributerGoodsId(), disId, gbDistributerEntity);
//
//            }
//        }

        Map<String, Object> mapGb = new HashMap<>();
        mapGb.put("nxDisGoodsId", lsGoodsId);
        List<GbDistributerGoodsEntity> goodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapGb);
        if (goodsEntities.size() > 0) {
            for (GbDistributerGoodsEntity distributerGoodsEntity : goodsEntities) {
                distributerGoodsEntity.setGbDgNxDistributerGoodsId(distributerGoodsId);
                gbDistributerGoodsService.update(distributerGoodsEntity);
            }
        }


        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("nxGoodsId", lsGoodsId);
        List<GbDepartmentDisGoodsEntity> gbDepartmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapDep);
        if (gbDepartmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity disGoodsEntity : gbDepartmentDisGoodsEntities) {
                disGoodsEntity.setGbDdgNxDistributerGoodsId(distributerGoodsId);
                gbDepartmentDisGoodsService.update(disGoodsEntity);
            }
        }


        dgService.delete(lsGoodsId);

        return R.ok();
    }

    private void updateGbDisGoods(Integer lsGoodsId, Integer nxGoodsId, Integer disId, GbDistributerEntity distributerEntity) {

        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxGoodsId);

        GbDistributerGoodsEntity cgnGoods = new GbDistributerGoodsEntity();
        cgnGoods.setGbDgControlFresh(0);
        cgnGoods.setGbDgControlPrice(0);
        cgnGoods.setGbDgGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
        cgnGoods.setGbDgGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
        cgnGoods.setGbDgGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
        cgnGoods.setGbDgGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
        cgnGoods.setGbDgGoodsStandardWeight(nxDistributerGoodsEntity.getNxDgGoodsStandardWeight());
        cgnGoods.setGbDgGoodsDetail(nxDistributerGoodsEntity.getNxDgGoodsDetail());
        cgnGoods.setGbDgGoodsBrand(nxDistributerGoodsEntity.getNxDgGoodsBrand());
        cgnGoods.setGbDgGoodsPlace(nxDistributerGoodsEntity.getNxDgGoodsPlace());
        cgnGoods.setGbDgGoodsSort(nxDistributerGoodsEntity.getNxDgGoodsSort());

        cgnGoods.setGbDgNxDistributerId(disId);
        cgnGoods.setGbDgGoodsStatus(0);
        cgnGoods.setGbDgGoodsIsWeight(0);
        cgnGoods.setGbDgNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
        cgnGoods.setGbDgNxFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
        cgnGoods.setGbDgNxGrandId(nxDistributerGoodsEntity.getNxDgNxGrandId());
        cgnGoods.setGbDgNxGreatGrandId(nxDistributerGoodsEntity.getNxDgNxGreatGrandId());
        cgnGoods.setGbDgPullOff(0);
        cgnGoods.setGbDgGoodsType(5);
        cgnGoods.setGbDgNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
        cgnGoods.setGbDgNxDistributerGoodsId(nxGoodsId);
        cgnGoods.setGbDgControlFresh(0);
        cgnGoods.setGbDgControlPrice(0);
        cgnGoods.setGbDgGoodsInventoryType(1);
        cgnGoods.setGbDgIsFranchisePrice(0);
        cgnGoods.setGbDgIsSelfControl(0);
        cgnGoods.setGbDgDistributerId(distributerEntity.getGbDistributerId());

        Integer distributerId = distributerEntity.getGbDistributerId();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", distributerId);
        map.put("type", 5);
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
        Integer gbDepartmentId = departmentEntities.get(0).getGbDepartmentId();
        cgnGoods.setGbDgGbDepartmentId(gbDepartmentId);
        GbDistributerGoodsEntity disGoods = saveDisGoodsForNx(cgnGoods);

        Map<String, Object> mapDepGoods = new HashMap<>();
        mapDepGoods.put("nxGoodsId", lsGoodsId);
        System.out.println("depGodoodoasodaf" + mapDepGoods);

        List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(mapDepGoods);
        if (departmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity departmentDisGoods : departmentDisGoodsEntities) {
                GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepDisGoodsService.queryObject(departmentDisGoods.getGbDepartmentDisGoodsId());
                departmentDisGoodsEntity.setGbDdgNxDistributerGoodsId(nxGoodsId);
                departmentDisGoodsEntity.setGbDdgDisGoodsId(disGoods.getGbDistributerGoodsId());
                departmentDisGoodsEntity.setGbDdgDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
                departmentDisGoodsEntity.setGbDdgDisGoodsGrandId(disGoods.getGbDgDfgGoodsGrandId());
                departmentDisGoodsEntity.setGbDdgDisGoodsGreatId(disGoods.getGbDgDfgGoodsGreatId());
                gbDepDisGoodsService.update(departmentDisGoodsEntity);
            }
        }

        List<GbDepartmentOrdersEntity> ordersEntities = gbDepartmentOrdersService.queryDisOrdersListByParams(mapDepGoods);
        if (ordersEntities.size() > 0) {
            for (GbDepartmentOrdersEntity order : ordersEntities) {
                GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryObject(order.getGbDepartmentOrdersId());
                gbDepartmentOrdersEntity.setGbDoDisGoodsId(disGoods.getGbDistributerGoodsId());
                gbDepartmentOrdersEntity.setGbDoDisGoodsFatherId(disGoods.getGbDgDfgGoodsFatherId());
                gbDepartmentOrdersEntity.setGbDoDisGoodsGrandId(disGoods.getGbDgDfgGoodsGrandId());
                gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
            }
        }

        gbDistributerGoodsService.delete(lsGoodsId);

    }


    @RequestMapping(value = "/exchangeNewGoodsDis", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeNewGoodsDis(Integer changeId, Integer toGoodsId) {

        System.out.println("echchagnee" + changeId + "toiccic" + toGoodsId);
        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(changeId);
        NxDistributerGoodsEntity toNxGoods = dgService.queryObject(toGoodsId);
        //order
        Map<String, Object> mapSearch = new HashMap<>();
        mapSearch.put("disGoodsId", changeId);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapSearch);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                ordersEntity.setNxDoDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                ordersEntity.setNxDoDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                ordersEntity.setNxDoNxGoodsId(toNxGoods.getNxDgNxGoodsId());
                ordersEntity.setNxDoNxGoodsFatherId(toNxGoods.getNxDgNxFatherId());
                if (toNxGoods.getNxDgWillPriceTwoStandard() != null && ordersEntity.getNxDoStandard().equals(toNxGoods.getNxDgWillPriceTwoStandard())) {
                    ordersEntity.setNxDoCostPriceLevel("2");
                }
                depOrdersService.update(ordersEntity);
            }
        }

        // history
        List<NxDepartmentOrderHistoryEntity> historyEntities = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(mapSearch);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrderHistoryEntity ordersEntity : historyEntities) {
                ordersEntity.setNxDoDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                ordersEntity.setNxDoDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                ordersEntity.setNxDoDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                ordersEntity.setNxDoNxGoodsId(toNxGoods.getNxDgNxGoodsId());
                ordersEntity.setNxDoNxGoodsFatherId(toNxGoods.getNxDgNxFatherId());
                if (toNxGoods.getNxDgWillPriceTwoStandard() != null && ordersEntity.getNxDoStandard().equals(toNxGoods.getNxDgWillPriceTwoStandard())) {
                    ordersEntity.setNxDoCostPriceLevel("2");
                }
                nxDepartmentOrderHistoryService.update(ordersEntity);
            }
        }


        //depGoods
        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapSearch);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity linshiDepGoodsEntity : departmentDisGoodsEntities) {
                Integer nxDdgDepartmentId = linshiDepGoodsEntity.getNxDdgDepartmentId();
                Map<String, Object> map = new HashMap<>();
                map.put("depId", nxDdgDepartmentId);
                map.put("disGoodsId", toGoodsId);
                System.out.println("depdisgoenen" + map);
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
                if (nxDepartmentDisGoodsEntity != null) {
                    System.out.println("hasdepgoods" + nxDepartmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsName(toNxGoods.getNxDgGoodsName());
                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsStandardname(toNxGoods.getNxDgGoodsStandardname());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderStandard(toNxGoods.getNxDgGoodsStandardname());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderPrice(linshiDepGoodsEntity.getNxDdgOrderPrice());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderGoodsName(linshiDepGoodsEntity.getNxDdgOrderGoodsName());
                    if (toNxGoods.getNxDgWillPriceTwoStandard() != null && linshiDepGoodsEntity.getNxDdgDepGoodsStandardname().equals(toNxGoods.getNxDgWillPriceTwoStandard())) {
                        nxDepartmentDisGoodsEntity.setNxDdgOrderPriceLevel("2");
                    }

                    nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);
                    nxDepartmentDisGoodsService.delete(linshiDepGoodsEntity.getNxDepartmentDisGoodsId());

                } else {
                    linshiDepGoodsEntity.setNxDdgDisGoodsId(toGoodsId);
                    linshiDepGoodsEntity.setNxDdgDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                    linshiDepGoodsEntity.setNxDdgDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                    NxDistributerFatherGoodsEntity grandEntity = nxDistributerFatherGoodsService.queryObject(toNxGoods.getNxDgDfgGoodsGrandId());
                    Integer nxDfgGreatId = grandEntity.getNxDfgFathersFatherId();
                    linshiDepGoodsEntity.setNxDdgDisGoodsGreatId(nxDfgGreatId);

                    linshiDepGoodsEntity.setNxDdgDepGoodsName(toNxGoods.getNxDgGoodsName());
                    linshiDepGoodsEntity.setNxDdgOrderGoodsName(toNxGoods.getNxDgGoodsName());
                    linshiDepGoodsEntity.setNxDdgOrderStandard(toNxGoods.getNxDgGoodsStandardname());
                    linshiDepGoodsEntity.setNxDdgDepGoodsStandardname(toNxGoods.getNxDgGoodsStandardname());
                    if (toNxGoods.getNxDgWillPriceTwoStandard() != null && linshiDepGoodsEntity.getNxDdgDepGoodsStandardname().equals(toNxGoods.getNxDgWillPriceTwoStandard())) {
                        linshiDepGoodsEntity.setNxDdgOrderPriceLevel("2");
                    }
                    nxDepartmentDisGoodsService.update(linshiDepGoodsEntity);
                }
            }
        }
//

        //purGoods
        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(mapSearch);

        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        List<NxDistributerGoodsShelfGoodsEntity> entities = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(mapSearch);
        if (entities.size() > 0) {
            for (NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity : entities) {
                nxDistributerGoodsShelfGoodsService.delete(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
            }
        }


//         ordersEntity.setNxDoNxGoodsId(toNxGoods.getNxDgNxGoodsId());
//                ordersEntity.setNxDoNxGoodsFatherId(toNxGoods.getNxDgNxFatherId());
        Map<String, Object> mapGb = new HashMap<>();
        mapGb.put("nxDisGoodsId", changeId);
        System.out.println("gbdidiididid" + mapGb);
        List<GbDistributerGoodsEntity> goodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapGb);
        if (goodsEntities.size() > 0) {
            for (GbDistributerGoodsEntity gbDistributerGoodsEntity : goodsEntities) {
                gbDistributerGoodsEntity.setGbDgNxDistributerGoodsId(toGoodsId);
                gbDistributerGoodsEntity.setGbDgNxGoodsId(toNxGoods.getNxDgNxGoodsId());
                gbDistributerGoodsEntity.setGbDgNxFatherId(toNxGoods.getNxDgNxFatherId());
                gbDistributerGoodsEntity.setGbDgNxGrandId(toNxGoods.getNxDgNxGrandId());
                gbDistributerGoodsEntity.setGbDgNxGreatGrandId(toNxGoods.getNxDgNxGreatGrandId());
                gbDistributerGoodsService.update(gbDistributerGoodsEntity);
            }
        }


        List<GbDepartmentOrdersEntity> gbOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(mapGb);
        if (gbOrdersEntities.size() > 0) {
            for (GbDepartmentOrdersEntity ordersEntity : gbOrdersEntities) {
                ordersEntity.setGbDoNxDistributerGoodsId(toGoodsId);
                gbDepartmentOrdersService.update(ordersEntity);
            }
        }


        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("nxGoodsId", changeId);
        List<GbDepartmentDisGoodsEntity> gbDepartmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapDep);
        if (gbDepartmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity disGoodsEntity : gbDepartmentDisGoodsEntities) {
                gbDepartmentDisGoodsService.delete(disGoodsEntity.getGbDepartmentDisGoodsId());
            }
        }


        Map<String, Object> map1 = new HashMap<>();
        map1.put("dgFatherId", distributerGoodsEntity.getNxDgDfgGoodsFatherId());
        //搜索fatherId下有几个disGoods
        List<NxDistributerGoodsEntity> totalDisGoods = dgService.queryDisGoodsByParams(map1);
        //如果disGoods的父类只有一个商品
        if (totalDisGoods.size() == 1) {
            //父类Entity
            NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
            //disGoods的grandId
            Integer grandId = nxDistributerFatherGoodsEntity.getNxDfgFathersFatherId();
            Map<String, Object> mapGrand = new HashMap<>();
            mapGrand.put("fathersFatherId", grandId);
            //搜索grand有几个兄弟
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(mapGrand);
            if (fatherGoodsEntities.size() == 1) {
                Integer nxDfgFathersFatherId = fatherGoodsEntities.get(0).getNxDfgFathersFatherId();
                NxDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(nxDfgFathersFatherId);
                Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
                Map<String, Object> map = new HashMap<>();
                map.put("fathersFatherId", greatGrandId);
                List<NxDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);

                //如果grandFather也是只有一个，则删除greatGrandFather
                if (grandGoodsEntities.size() == 1) {
                    dgfService.delete(greatGrandId);
                }
                dgfService.delete(grandId);
            }
            dgfService.delete(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
        } else {
            //父类商品数量减去1
            NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
            Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
            nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount - 1);
            dgfService.update(nxDistributerFatherGoodsEntity);
        }

        dgService.delete(changeId);

        return R.ok();
    }


    @RequestMapping(value = "/exchangeNewGoodsDisWithLevel", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeNewGoodsDisWithLevel(Integer changeId, Integer toGoodsId, Integer level) {

        System.out.println("echchagnee" + changeId + "toiccic" + toGoodsId);
        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(changeId);
        NxDistributerGoodsEntity toNxGoods = dgService.queryObject(toGoodsId);
        String standard = "";
        String price = "";
        if (level == 2) {
            standard = toNxGoods.getNxDgWillPriceTwoStandard();
            price = toNxGoods.getNxDgWillPriceTwo();
        }
        //order
        Map<String, Object> mapSearch = new HashMap<>();
        mapSearch.put("disGoodsId", changeId);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapSearch);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                ordersEntity.setNxDoDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                ordersEntity.setNxDoDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                ordersEntity.setNxDoNxGoodsId(toNxGoods.getNxDgNxGoodsId());
                ordersEntity.setNxDoNxGoodsFatherId(toNxGoods.getNxDgNxFatherId());
                depOrdersService.update(ordersEntity);
            }
        }
        List<NxDepartmentOrderHistoryEntity> historyEntities = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(mapSearch);
        if (historyEntities.size() > 0) {
            for (NxDepartmentOrderHistoryEntity ordersEntity : historyEntities) {
                ordersEntity.setNxDoDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                ordersEntity.setNxDoDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                ordersEntity.setNxDoDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                ordersEntity.setNxDoNxGoodsId(toNxGoods.getNxDgNxGoodsId());
                ordersEntity.setNxDoNxGoodsFatherId(toNxGoods.getNxDgNxFatherId());
                nxDepartmentOrderHistoryService.update(ordersEntity);
            }
        }

        List<NxDepartmentOrdersHistoryEntity> linshiMapHHS = nxDepartmentOrdersHistoryService.queryDepHistoryOrdersByParams(mapSearch);
        if (linshiMapHHS.size() > 0) {
            for (NxDepartmentOrdersHistoryEntity ordersEntity : linshiMapHHS) {
                ordersEntity.setNxDohDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                nxDepartmentOrdersHistoryService.update(ordersEntity);
            }
        }

        //depGoods
        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapSearch);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity linshiDepGoodsEntity : departmentDisGoodsEntities) {
                Integer nxDdgDepartmentId = linshiDepGoodsEntity.getNxDdgDepartmentId();
                Map<String, Object> map = new HashMap<>();
                map.put("depId", nxDdgDepartmentId);
                map.put("disGoodsId", toGoodsId);
                System.out.println("depdisgoenen" + map);
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
                if (nxDepartmentDisGoodsEntity != null) {
                    System.out.println("hasdepgoods" + nxDepartmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsName(toNxGoods.getNxDgGoodsName());
                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsStandardname(standard);
                    nxDepartmentDisGoodsEntity.setNxDdgOrderStandard(standard);
                    nxDepartmentDisGoodsEntity.setNxDdgOrderPrice(price);
                    nxDepartmentDisGoodsEntity.setNxDdgOrderGoodsName(toNxGoods.getNxDgGoodsName());
                    nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);
                    nxDepartmentDisGoodsService.delete(linshiDepGoodsEntity.getNxDepartmentDisGoodsId());

                } else {
                    linshiDepGoodsEntity.setNxDdgDisGoodsId(toGoodsId);
                    linshiDepGoodsEntity.setNxDdgDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                    linshiDepGoodsEntity.setNxDdgDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                    NxDistributerFatherGoodsEntity grandEntity = nxDistributerFatherGoodsService.queryObject(toNxGoods.getNxDgDfgGoodsGrandId());
                    Integer nxDfgGreatId = grandEntity.getNxDfgFathersFatherId();
                    linshiDepGoodsEntity.setNxDdgDisGoodsGreatId(nxDfgGreatId);
                    linshiDepGoodsEntity.setNxDdgDepGoodsName(toNxGoods.getNxDgGoodsName());
                    linshiDepGoodsEntity.setNxDdgOrderGoodsName(toNxGoods.getNxDgGoodsName());
                    linshiDepGoodsEntity.setNxDdgOrderStandard(standard);
                    linshiDepGoodsEntity.setNxDdgDepGoodsStandardname(standard);
                    nxDepartmentDisGoodsService.update(linshiDepGoodsEntity);
                }
            }
        }
//

        //purGoods
        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(mapSearch);

        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsId(toNxGoods.getNxDistributerGoodsId());
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(toNxGoods.getNxDgDfgGoodsGrandId());
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(toNxGoods.getNxDgDfgGoodsFatherId());
                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        List<NxDistributerGoodsShelfGoodsEntity> entities = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(mapSearch);
        if (entities.size() > 0) {
            for (NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity : entities) {
                nxDistributerGoodsShelfGoodsService.delete(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
            }
        }


        Map<String, Object> mapGb = new HashMap<>();
        mapGb.put("nxDisGoodsId", changeId);
        System.out.println("gbdidiididid" + mapGb);
        List<GbDistributerGoodsEntity> goodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapGb);
        if (goodsEntities.size() > 0) {
            for (GbDistributerGoodsEntity gbDistributerGoodsEntity : goodsEntities) {
                gbDistributerGoodsEntity.setGbDgNxDistributerGoodsId(toGoodsId);
                gbDistributerGoodsService.update(gbDistributerGoodsEntity);
            }
        }


        List<GbDepartmentOrdersEntity> gbOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(mapGb);
        if (gbOrdersEntities.size() > 0) {
            for (GbDepartmentOrdersEntity ordersEntity : gbOrdersEntities) {
                ordersEntity.setGbDoNxDistributerGoodsId(toGoodsId);
                gbDepartmentOrdersService.update(ordersEntity);
            }
        }


        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("nxGoodsId", changeId);
        List<GbDepartmentDisGoodsEntity> gbDepartmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapDep);
        if (gbDepartmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity disGoodsEntity : gbDepartmentDisGoodsEntities) {
                gbDepartmentDisGoodsService.delete(disGoodsEntity.getGbDepartmentDisGoodsId());
            }
        }


        Map<String, Object> map1 = new HashMap<>();
        map1.put("dgFatherId", distributerGoodsEntity.getNxDgDfgGoodsFatherId());
        //搜索fatherId下有几个disGoods
        List<NxDistributerGoodsEntity> totalDisGoods = dgService.queryDisGoodsByParams(map1);
        //如果disGoods的父类只有一个商品
        if (totalDisGoods.size() == 1) {
            //父类Entity
            NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
            //disGoods的grandId
            Integer grandId = nxDistributerFatherGoodsEntity.getNxDfgFathersFatherId();
            Map<String, Object> mapGrand = new HashMap<>();
            mapGrand.put("fathersFatherId", grandId);
            //搜索grand有几个兄弟
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(mapGrand);
            if (fatherGoodsEntities.size() == 1) {
                Integer nxDfgFathersFatherId = fatherGoodsEntities.get(0).getNxDfgFathersFatherId();
                NxDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(nxDfgFathersFatherId);
                Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
                Map<String, Object> map = new HashMap<>();
                map.put("fathersFatherId", greatGrandId);
                List<NxDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);

                //如果grandFather也是只有一个，则删除greatGrandFather
                if (grandGoodsEntities.size() == 1) {
                    dgfService.delete(greatGrandId);
                }
                dgfService.delete(grandId);
            }
            dgfService.delete(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
        } else {
            //父类商品数量减去1
            NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(distributerGoodsEntity.getNxDgDfgGoodsFatherId());
            Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
            nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount - 1);
            dgfService.update(nxDistributerFatherGoodsEntity);
        }

        dgService.delete(changeId);

        return R.ok();
    }


    @RequestMapping(value = "/moveGoods", method = RequestMethod.POST)
    @ResponseBody
    public R moveGoods(Integer toFatherId, Integer moveId) {
        // 0， 新 nxGoods
        NxGoodsEntity fatherGoods = nxGoodsService.queryObject(toFatherId);

        NxGoodsEntity moveGoods = nxGoodsService.queryObject(moveId);

        Integer nxGoodsFatherId = moveGoods.getNxGoodsFatherId();
        int total = nxGoodsService.queryTotalByFatherId(nxGoodsFatherId);

        if (total == 1) {
            NxGoodsEntity father = nxGoodsService.queryObject(nxGoodsFatherId);
            nxGoodsService.delete(father.getNxGoodsId());
        }

        moveGoods.setNxGoodsFatherId(fatherGoods.getNxGoodsId());
        moveGoods.setNxGoodsGrandId(fatherGoods.getNxGoodsFatherId());
        moveGoods.setNxGoodsGreatGrandId(fatherGoods.getNxGoodsGrandId());
        System.out.println("firstmovveieieiieiieiei" + moveGoods);
        nxGoodsService.update(moveGoods);


        System.out.println("stopeoneeeoeoeo");
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.querydisGoodsByNxGoodsId(moveId);

        if (distributerGoodsEntities.size() > 0) {

            for (NxDistributerGoodsEntity delNxGoods : distributerGoodsEntities) {

                NxDistributerGoodsEntity toNxDisGoods = saveDisGoods(delNxGoods);

                Map<String, Object> mapSearch = new HashMap<>();
                mapSearch.put("disGoodsId", delNxGoods.getNxDistributerGoodsId());
                System.out.println("mapseredisGoodsId1111" + mapSearch);
                List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapSearch);
                if (ordersEntities.size() > 0) {
                    for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                        ordersEntity.setNxDoDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                        ordersEntity.setNxDoDisGoodsGrandId(toNxDisGoods.getNxDgDfgGoodsGrandId());
                        ordersEntity.setNxDoDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                        ordersEntity.setNxDoNxGoodsFatherId(fatherGoods.getNxGoodsId());
                        depOrdersService.update(ordersEntity);

                    }
                }

                List<NxDepartmentOrderHistoryEntity> ordersEntitiesHis = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(mapSearch);
                if (ordersEntitiesHis.size() > 0) {
                    for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesHis) {
                        ordersEntity.setNxDoDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                        ordersEntity.setNxDoDisGoodsGrandId(toNxDisGoods.getNxDgDfgGoodsGrandId());
                        ordersEntity.setNxDoDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                        ordersEntity.setNxDoNxGoodsFatherId(fatherGoods.getNxGoodsId());
                        nxDepartmentOrderHistoryService.update(ordersEntity);
                    }
                }

                //depGoods
                System.out.println("drpGHooddsossooss" + mapSearch);
                List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapSearch);
                if (departmentDisGoodsEntities.size() > 0) {
                    for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("depId", depGoodsEntity.getNxDdgDepartmentId());
                        map.put("disGoodsId", toNxDisGoods.getNxDistributerGoodsId());
                        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
                        if (departmentDisGoodsEntity != null) {

                            Map<String, Object> mapDepOrder = new HashMap<>();
                            mapDepOrder.put("depDisGoodsId", depGoodsEntity.getNxDepartmentDisGoodsId());
                            System.out.println("mapseredisGoodsId22222" + mapSearch);
                            List<NxDepartmentOrdersEntity> ordersEntitiesDep = depOrdersService.queryDisOrdersByParams(mapDepOrder);
                            if (ordersEntitiesDep.size() > 0) {
                                for (NxDepartmentOrdersEntity ordersEntity : ordersEntitiesDep) {
                                    ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                                    depOrdersService.update(ordersEntity);
                                }
                            }

                            System.out.println("mapseredisGoodsI3333333" + mapSearch);
                            List<NxDepartmentOrderHistoryEntity> ordersEntitiesDepHis = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(mapDepOrder);
                            if (ordersEntitiesDep.size() > 0) {
                                for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesDepHis) {
                                    ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                                    nxDepartmentOrderHistoryService.update(ordersEntity);
                                }
                            }

                            nxDepartmentDisGoodsService.delete(depGoodsEntity.getNxDepartmentDisGoodsId());

                        } else {
                            depGoodsEntity.setNxDdgDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                            depGoodsEntity.setNxDdgDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                            nxDepartmentDisGoodsService.update(depGoodsEntity);
                        }

                    }

                    //purGoods
                    System.out.println("purgodosssss" + mapSearch);
                    List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(mapSearch);
                    if (purchaseGoodsEntities.size() > 0) {
                        for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                            purchaseGoodsEntity.setNxDpgDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                            purchaseGoodsEntity.setNxDpgDisGoodsGrandId(toNxDisGoods.getNxDgDfgGoodsGrandId());
                            purchaseGoodsEntity.setNxDpgDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                            nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
                        }
                    }


                    //gbDisGoods
                    Map<String, Object> mapG = new HashMap<>();
                    mapG.put("nxDisGoodsId", delNxGoods.getNxDistributerGoodsId());
                    List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapG);
                    if (gbDistributerGoodsEntities.size() > 0) {
                        for (GbDistributerGoodsEntity gbDistributerGoodsEntity : gbDistributerGoodsEntities) {
                            gbDistributerGoodsEntity.setGbDgNxDistributerGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                            gbDistributerGoodsService.update(gbDistributerGoodsEntity);
                        }
                    }

                    // gbDepOrders
                    List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(mapG);
                    if (gbDepartmentOrdersEntities.size() > 0) {
                        for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
                            gbDepartmentOrdersEntity.setGbDoNxDistributerGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
                        }
                    }
                }


                //lastt
                dgService.delete(delNxGoods.getNxDistributerGoodsId());
            }

        }

        return R.ok();
    }

    //todo test
    @RequestMapping(value = "/exchangeNewGoods", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeNewGoods(Integer deleteId, Integer toGoodsId) {

        // 0， 新 nxGoods
        NxGoodsEntity toGoods = nxGoodsService.queryObject(toGoodsId);
        // 1,获取所有要删除 nxgoodsId 的 nxDisttributerGoods
        System.out.println("stopeoneeeoeoeo");
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.querydisGoodsByNxGoodsId(deleteId);

        if (distributerGoodsEntities.size() > 0) {

            for (NxDistributerGoodsEntity delNxGoods : distributerGoodsEntities) {

                Map<String, Object> mapDepGoods = new HashMap<>();
                mapDepGoods.put("nxGoodsId", toGoodsId);
                mapDepGoods.put("disId", delNxGoods.getNxDgDistributerId());
                System.out.println("queyrdidididiiididiid" + mapDepGoods);
                NxDistributerGoodsEntity toNxDisGoods = dgService.queryOneGoodsAboutNxGoods(mapDepGoods);
                //转移的商品如果有
                if (toNxDisGoods != null) {

                    toNxDisGoods.setNxDgPurchaseAuto(delNxGoods.getNxDgPurchaseAuto());
                    toNxDisGoods.setNxDgGoodsFile(toGoods.getNxGoodsFile());
                    toNxDisGoods.setNxDgGoodsFileLarge(toGoods.getNxGoodsFileBig());
                    toNxDisGoods.setNxDgCartonUnit(toGoods.getNxGoodsCartonUnit());
                    toNxDisGoods.setNxDgItemsPerCarton(toGoods.getNxGoodsItemsPerCarton());

                    if (delNxGoods.getNxDgWillPriceOne() != null) {
                        toNxDisGoods.setNxDgWillPriceOne(delNxGoods.getNxDgWillPrice());
                        toNxDisGoods.setNxDgWillPriceOneStandard(delNxGoods.getNxDgWillPriceOneStandard());
                        toNxDisGoods.setNxDgBuyingPriceOneUpdate(delNxGoods.getNxDgBuyingPriceOneUpdate());
                        if (delNxGoods.getNxDgWillPriceTwo() != null) {
                            toNxDisGoods.setNxDgWillPriceTwo(delNxGoods.getNxDgWillPrice());
                            toNxDisGoods.setNxDgWillPriceTwoStandard(delNxGoods.getNxDgWillPriceTwoStandard());
                            toNxDisGoods.setNxDgWillPriceTwoAboutPrice(delNxGoods.getNxDgWillPriceTwoAboutPrice());
                            toNxDisGoods.setNxDgWillPriceTwoWeight(delNxGoods.getNxDgWillPriceTwoWeight());
                            toNxDisGoods.setNxDgBuyingPriceTwoUpdate(delNxGoods.getNxDgBuyingPriceTwoUpdate());
                        }

                        if (delNxGoods.getNxDgSupplierId() != null) {
                            toNxDisGoods.setNxDgSupplierId(delNxGoods.getNxDgSupplierId());
                        }
                        dgService.update(toNxDisGoods);
                    }

                    Map<String, Object> mapSearch = new HashMap<>();
                    mapSearch.put("disGoodsId", delNxGoods.getNxDistributerGoodsId());
                    System.out.println("mapseredisGoodsId1111" + mapSearch);
                    List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapSearch);
                    if (ordersEntities.size() > 0) {
                        for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                            ordersEntity.setNxDoDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                            ordersEntity.setNxDoDisGoodsGrandId(toNxDisGoods.getNxDgDfgGoodsGrandId());
                            ordersEntity.setNxDoDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                            ordersEntity.setNxDoNxGoodsId(toGoods.getNxGoodsId());
                            ordersEntity.setNxDoNxGoodsFatherId(toGoods.getNxGoodsFatherId());
                            depOrdersService.update(ordersEntity);

                        }
                    }

                    List<NxDepartmentOrderHistoryEntity> ordersEntitiesHis = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(mapSearch);
                    if (ordersEntitiesHis.size() > 0) {
                        for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesHis) {
                            ordersEntity.setNxDoDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                            ordersEntity.setNxDoDisGoodsGrandId(toNxDisGoods.getNxDgDfgGoodsGrandId());
                            ordersEntity.setNxDoDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                            ordersEntity.setNxDoNxGoodsId(toGoods.getNxGoodsId());
                            ordersEntity.setNxDoNxGoodsFatherId(toGoods.getNxGoodsFatherId());
                            nxDepartmentOrderHistoryService.update(ordersEntity);
                        }
                    }

                    //depGoods
                    System.out.println("drpGHooddsossooss" + mapSearch);
                    List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapSearch);
                    if (departmentDisGoodsEntities.size() > 0) {
                        for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("depId", depGoodsEntity.getNxDdgDepartmentId());
                            map.put("disGoodsId", toNxDisGoods.getNxDistributerGoodsId());
                            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
                            if (departmentDisGoodsEntity != null) {

                                Map<String, Object> mapDepOrder = new HashMap<>();
                                mapDepOrder.put("depDisGoodsId", depGoodsEntity.getNxDepartmentDisGoodsId());
                                System.out.println("mapseredisGoodsId22222" + mapSearch);
                                List<NxDepartmentOrdersEntity> ordersEntitiesDep = depOrdersService.queryDisOrdersByParams(mapDepOrder);
                                if (ordersEntitiesDep.size() > 0) {
                                    for (NxDepartmentOrdersEntity ordersEntity : ordersEntitiesDep) {
                                        ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                                        depOrdersService.update(ordersEntity);
                                    }
                                }

                                System.out.println("mapseredisGoodsI3333333" + mapSearch);
                                List<NxDepartmentOrderHistoryEntity> ordersEntitiesDepHis = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(mapDepOrder);
                                if (ordersEntitiesDep.size() > 0) {
                                    for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesDepHis) {
                                        ordersEntity.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                                        nxDepartmentOrderHistoryService.update(ordersEntity);
                                    }
                                }

                                nxDepartmentDisGoodsService.delete(depGoodsEntity.getNxDepartmentDisGoodsId());

                            } else {
                                depGoodsEntity.setNxDdgDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                                depGoodsEntity.setNxDdgDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                                nxDepartmentDisGoodsService.update(depGoodsEntity);
                            }

                        }

                        //purGoods
                        System.out.println("purgodosssss" + mapSearch);
                        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(mapSearch);
                        if (purchaseGoodsEntities.size() > 0) {
                            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                                purchaseGoodsEntity.setNxDpgDisGoodsId(toNxDisGoods.getNxDistributerGoodsId());
                                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(toNxDisGoods.getNxDgDfgGoodsGrandId());
                                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(toNxDisGoods.getNxDgDfgGoodsFatherId());
                                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
                            }
                        }


                        //gbDisGoods
                        Map<String, Object> mapG = new HashMap<>();
                        mapG.put("nxDisGoodsId", delNxGoods.getNxDistributerGoodsId());
                        List<GbDistributerGoodsEntity> gbDistributerGoodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapG);
                        if (gbDistributerGoodsEntities.size() > 0) {
                            for (GbDistributerGoodsEntity gbDistributerGoodsEntity : gbDistributerGoodsEntities) {
                                gbDistributerGoodsEntity.setGbDgNxDistributerGoodsId(toGoodsId);
                                gbDistributerGoodsService.update(gbDistributerGoodsEntity);
                            }
                        }

                        // gbDepOrders
                        List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(mapG);
                        if (gbDepartmentOrdersEntities.size() > 0) {
                            for (GbDepartmentOrdersEntity gbDepartmentOrdersEntity : gbDepartmentOrdersEntities) {
                                gbDepartmentOrdersEntity.setGbDoNxDistributerGoodsId(toGoodsId);
                                gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
                            }
                        }
                    }


                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("dgFatherId", delNxGoods.getNxDgDfgGoodsFatherId());
                    //搜索fatherId下有几个disGoods
                    List<NxDistributerGoodsEntity> totalDisGoods = dgService.queryDisGoodsByParams(map1);
                    //如果disGoods的父类只有一个商品
                    System.out.println("totalDisGoodstotalDisGoodstotalDisGoods" + totalDisGoods.size());
                    if (totalDisGoods.size() == 1) {
//                    //父类Entity
                        NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(delNxGoods.getNxDgDfgGoodsFatherId());
                        //disGoods的grandId
                        Integer grandId = nxDistributerFatherGoodsEntity.getNxDfgFathersFatherId();
                        Map<String, Object> mapGrand = new HashMap<>();
                        mapGrand.put("fathersFatherId", grandId);
                        //搜索grand有几个兄弟
                        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(mapGrand);
                        if (fatherGoodsEntities.size() == 1) {
                            Integer nxDfgFathersFatherId = fatherGoodsEntities.get(0).getNxDfgFathersFatherId();
                            NxDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(nxDfgFathersFatherId);
                            Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
                            Map<String, Object> map = new HashMap<>();
                            map.put("fathersFatherId", greatGrandId);
                            List<NxDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);

                            //如果grandFather也是只有一个，则删除greatGrandFather
                            if (grandGoodsEntities.size() == 1) {
                                dgfService.delete(greatGrandId);
                            }
                            dgfService.delete(grandId);
                        }

                        dgfService.delete(nxDistributerFatherGoodsEntity.getNxDistributerFatherGoodsId());

                    } else {
                        //父类商品数量减去1
                        NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(delNxGoods.getNxDgDfgGoodsFatherId());
                        Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
                        nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount - 1);
                        dgfService.update(nxDistributerFatherGoodsEntity);
                    }

                    dgService.delete(delNxGoods.getNxDistributerGoodsId());

                } else {

                    System.out.println("updatneExchangeee");
                    delNxGoods.setNxDgNxGoodsId(toGoodsId);
                    delNxGoods.setNxDgNxFatherId(toGoods.getNxGoodsFatherId());
                    delNxGoods.setNxDgNxGrandId(toGoods.getNxGoodsGrandId());
                    delNxGoods.setNxDgGoodsName(toGoods.getNxGoodsName());
                    delNxGoods.setNxDgGoodsBrand(toGoods.getNxGoodsBrand());
                    delNxGoods.setPerPrice(toGoods.getNxGoodsPlace());
                    delNxGoods.setNxDgGoodsDetail(toGoods.getNxGoodsDetail());
                    delNxGoods.setNxDgGoodsStandardname(toGoods.getNxGoodsStandardname());
                    delNxGoods.setNxDgGoodsStandardWeight(toGoods.getNxGoodsStandardWeight());
                    System.out.println("upadtttetteeeeee" + delNxGoods);
                    delNxGoods.setNxDgGoodsFile(toGoods.getNxGoodsFile());
                    delNxGoods.setNxDgGoodsFileLarge(toGoods.getNxGoodsFileBig());
                    delNxGoods.setNxDgCartonUnit(toGoods.getNxGoodsCartonUnit());
                    delNxGoods.setNxDgItemsPerCarton(toGoods.getNxGoodsItemsPerCarton());
                    dgService.update(delNxGoods);

                    Integer nxDgDfgGoodsFatherId = delNxGoods.getNxDgDfgGoodsFatherId();
                    NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsFatherId);
                    fatherGoodsEntity.setNxDfgNxGoodsId(toGoods.getNxGoodsFatherId());
                    nxDistributerFatherGoodsService.update(fatherGoodsEntity);

                    Integer nxDgDfgGoodsGrandId = delNxGoods.getNxDgDfgGoodsGrandId();
                    NxDistributerFatherGoodsEntity grandGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsGrandId);
                    grandGoodsEntity.setNxDfgNxGoodsId(toGoods.getNxGoodsGrandId());
                    nxDistributerFatherGoodsService.update(grandGoodsEntity);


                    Integer nxDfgGreatId = grandGoodsEntity.getNxDfgFathersFatherId();

                    NxDistributerFatherGoodsEntity greatGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDfgGreatId);
                    grandGoodsEntity.setNxDfgNxGoodsId(toGoods.getNxGoodsGreatGrandId());
                    nxDistributerFatherGoodsService.update(greatGoodsEntity);


                    Map<String, Object> mapSearch = new HashMap<>();
                    mapSearch.put("disGoodsId", delNxGoods.getNxDistributerGoodsId());
                    System.out.println("mapseredisGoodsId" + mapSearch);
                    List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapSearch);
                    if (ordersEntities.size() > 0) {
                        for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                            ordersEntity.setNxDoNxGoodsId(toGoods.getNxGoodsId());
                            ordersEntity.setNxDoNxGoodsFatherId(toGoods.getNxGoodsFatherId());
                            ordersEntity.setNxDoDepDisGoodsId(-1);
                            depOrdersService.update(ordersEntity);
                        }
                    }

                    //depGoods
                    System.out.println("drpGHooddsossooss" + mapSearch);
                    List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(mapSearch);
                    if (departmentDisGoodsEntities.size() > 0) {
                        for (NxDepartmentDisGoodsEntity depGoodsEntity : departmentDisGoodsEntities) {

                            depGoodsEntity.setNxDdgDepGoodsName(toGoods.getNxGoodsName());
                            depGoodsEntity.setNxDdgDepGoodsStandardname(toGoods.getNxGoodsStandardname());
                            nxDepartmentDisGoodsService.update(depGoodsEntity);
                        }
                    }

                }
            }

        }

        NxGoodsEntity delGoodsEntity = nxGoodsService.queryObject(deleteId);
        Integer nxGoodsFatherId = delGoodsEntity.getNxGoodsFatherId();
        int total = nxGoodsService.queryTotalByFatherId(nxGoodsFatherId);

        if (delGoodsEntity.getNxGoodsIsOldestSon() == 1) {
            if (total > 1) {
                return R.error(-1, "不能删除根商品");
            }
        }

        if (total == 1) {
            NxGoodsEntity father = nxGoodsService.queryObject(nxGoodsFatherId);
            nxGoodsService.delete(father.getNxGoodsId());
        }

        nxGoodsService.delete(deleteId);

        return R.ok();
    }

    @RequestMapping(value = "/exchangeNxGoodsFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeNxGoodsFatherId(Integer changeId, Integer toFatherId) {
        logger.info("[exchangeNxGoodsFatherId] 开始修改商品父ID，changeId={}, toFatherId={}", changeId, toFatherId);

        //1,把 changeId 的nxGoods 的fatheId 换成 fatherId
        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(changeId);
        if (nxGoodsEntity == null) {
            logger.error("[exchangeNxGoodsFatherId] 商品不存在，changeId={}", changeId);
            return R.error(-1, "商品不存在");
        }

        Integer nxGoodsFatherId = nxGoodsEntity.getNxGoodsFatherId();
        logger.info("[exchangeNxGoodsFatherId] 当前商品父ID={}, 商品名称={}", nxGoodsFatherId, nxGoodsEntity.getNxGoodsName());

        int total = nxGoodsService.queryTotalByFatherId(nxGoodsFatherId);
        logger.info("[exchangeNxGoodsFatherId] 原父商品下共有{}个子商品", total);

        if (total == 1) {
            NxGoodsEntity father = nxGoodsService.queryObject(nxGoodsFatherId);
            if (father != null) {
                logger.info("[exchangeNxGoodsFatherId] 原父商品下只有一个子商品，删除原父商品，fatherId={}, fatherName={}",
                        nxGoodsFatherId, father.getNxGoodsName());
                nxGoodsService.delete(father.getNxGoodsId());
            }
        }

        // 获取目标父级商品信息
        NxGoodsEntity toFatherGoods = nxGoodsService.queryObject(toFatherId);
        if (toFatherGoods == null) {
            logger.error("[exchangeNxGoodsFatherId] 目标父级商品不存在，toFatherId={}", toFatherId);
            return R.error(-1, "目标父级商品不存在");
        }

        // 正确获取 grandId 和 greatGrandId：应该从 toFatherId 的父级获取
        // toGrandId = toFatherId 的 fatherId（level=2的grandId等于它的fatherId）
        Integer toGrandId = toFatherGoods.getNxGoodsFatherId();
        // toGreatGrandId = toFatherId 的父级的父级ID
        Integer toGreatGrandId = null;
        if (toGrandId != null) {
            NxGoodsEntity grandGoods = nxGoodsService.queryObject(toGrandId);
            if (grandGoods != null) {
                toGreatGrandId = grandGoods.getNxGoodsFatherId();
            }
        }

        // nxGoodsSort 应该等于父级的 sort（即 toFatherGoods 的 sort）
        Integer toGoodsSort = toFatherGoods.getNxGoodsSort();

        // 查询该父级下已有商品的最大 sonsSort，用于设置转移商品的 sonsSort
        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.querySubNameByFatherId(toFatherId);
        int maxSonsSort = 0;
        if (nxGoodsEntities != null && !nxGoodsEntities.isEmpty()) {
            for (NxGoodsEntity existing : nxGoodsEntities) {
                if (existing.getNxGoodsSonsSort() != null && existing.getNxGoodsSonsSort() > maxSonsSort) {
                    maxSonsSort = existing.getNxGoodsSonsSort();
                }
            }
        }
        Integer newSort = maxSonsSort + 1;

        // 更新商品信息
        nxGoodsEntity.setNxGoodsGrandId(toGrandId);
        nxGoodsEntity.setNxGoodsGreatGrandId(toGreatGrandId);
        nxGoodsEntity.setNxGoodsSort(toGoodsSort);

        logger.info("[exchangeNxGoodsFatherId] 更新商品父ID，changeId={}, 从{}改为{}, maxSonsSort={}, newSort={}",
                changeId, nxGoodsFatherId, toFatherId, maxSonsSort, newSort);
        nxGoodsEntity.setNxGoodsFatherId(toFatherId);

        nxGoodsEntity.setNxGoodsSonsSort(newSort);
        nxGoodsEntity.setNxGoodsIsOldestSon(0);
        nxGoodsService.update(nxGoodsEntity);

        //2，把配送商的商品的 nxFatherrId 换成 fatherId，
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.querydisGoodsByNxGoodsId(changeId);
        logger.info("[exchangeNxGoodsFatherId] 查询到{}个分销商商品需要更新", distributerGoodsEntities.size());

        if (distributerGoodsEntities.size() > 0) {
            int updateCount = 0;
            for (NxDistributerGoodsEntity nxChangeGoods : distributerGoodsEntities) {
                logger.debug("[exchangeNxGoodsFatherId] 更新分销商商品父ID，disGoodsId={}, 从{}改为{}",
                        nxChangeGoods.getNxDistributerGoodsId(), nxChangeGoods.getNxDgNxFatherId(), toFatherId);
                nxChangeGoods.setNxDgNxFatherId(toFatherId);
                nxChangeGoods.setNxDgNxGrandId(toGrandId);
                nxChangeGoods.setNxDgNxGreatGrandId(toGreatGrandId);
                nxChangeGoods.setNxDgGoodsSort(toGoodsSort);

                //修改 nxDistributerGoodsFather 的 nxGoodsId
                Integer disId = nxChangeGoods.getNxDgDistributerId();

                //1，查询该分销商是否已有 toFatherId 对应的 NxDistributerFatherGoodsEntity
                Map<String, Object> map = new HashMap<>();
                map.put("disId", disId);
                map.put("nxGoodsId", toFatherId);
                List<NxDistributerFatherGoodsEntity> existingFatherGoods = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);

                NxDistributerFatherGoodsEntity targetFatherGoods = null;
                if (existingFatherGoods != null && !existingFatherGoods.isEmpty()) {
                    // 如果已有，直接使用
                    targetFatherGoods = existingFatherGoods.get(0);
                    logger.debug("[exchangeNxGoodsFatherId] 找到已存在的分销商父商品，disGoodsId={}, fatherGoodsId={}",
                            nxChangeGoods.getNxDistributerGoodsId(), targetFatherGoods.getNxDistributerFatherGoodsId());
                } else {
                    //2，如果没有，则创建新的 NxDistributerFatherGoodsEntity（按照 saveDisGoods 的逻辑）
                    // toFatherGoods 已经在方法开始处定义，直接使用
                    if (toFatherGoods == null) {
                        logger.error("[exchangeNxGoodsFatherId] 目标父商品不存在，toFatherId={}", toFatherId);
                        continue;
                    }

                    // 获取 grand 和 greatGrand 信息（使用已定义的 toFatherGoods）
                    NxGoodsEntity fatherEntity = toFatherGoods;
                    Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
                    NxGoodsEntity grandEntity = grandFatherId != null ? nxGoodsService.queryObject(grandFatherId) : null;
                    Integer greatGrandFatherId = grandEntity != null ? grandEntity.getNxGoodsFatherId() : null;
                    String grandName = grandEntity != null ? grandEntity.getNxGoodsName() : null;
                    String greatGrandName = greatGrandFatherId != null ? nxGoodsService.queryObject(greatGrandFatherId).getNxGoodsName() : null;

                    // 获取颜色（根据 greatGrandId）
                    String fatherColor = null;
                    if (greatGrandFatherId != null) {
                        if (greatGrandFatherId.equals(1)) {
                            fatherColor = "#20afb8";
                        } else if (greatGrandFatherId.equals(2)) {
                            fatherColor = "#f5c832";
                        } else if (greatGrandFatherId.equals(3)) {
                            fatherColor = "#3cc36e";
                        } else if (greatGrandFatherId.equals(4)) {
                            fatherColor = "#f09628";
                        } else if (greatGrandFatherId.equals(5)) {
                            fatherColor = "#1ebaee";
                        } else if (greatGrandFatherId.equals(6)) {
                            fatherColor = "#f05a32";
                        } else if (greatGrandFatherId.equals(7)) {
                            fatherColor = "#c0a6dd";
                        } else if (greatGrandFatherId.equals(8)) {
                            fatherColor = "#969696";
                        } else if (greatGrandFatherId.equals(9)) {
                            fatherColor = "#318666";
                        } else if (greatGrandFatherId.equals(10)) {
                            fatherColor = "#026bc2";
                        } else if (greatGrandFatherId.equals(11)) {
                            fatherColor = "#06eb6d";
                        } else if (greatGrandFatherId.equals(12)) {
                            fatherColor = "#0690eb";
                        }
                    }

                    // 创建 father (Level 2)
                    targetFatherGoods = new NxDistributerFatherGoodsEntity();
                    targetFatherGoods.setNxDfgDistributerId(disId);
                    targetFatherGoods.setNxDfgFatherGoodsName(toFatherGoods.getNxGoodsName());
                    targetFatherGoods.setNxDfgFatherGoodsLevel(2);
                    targetFatherGoods.setNxDfgGoodsAmount(1);
                    targetFatherGoods.setNxDfgFatherGoodsImg(toFatherGoods.getNxGoodsFile());
                    targetFatherGoods.setNxDfgFatherGoodsImgLarge(toFatherGoods.getNxGoodsFileBig());
                    targetFatherGoods.setNxDfgFatherGoodsColor(fatherColor);
                    targetFatherGoods.setNxDfgFatherGoodsSort(toFatherGoods.getNxGoodsSort());
                    targetFatherGoods.setNxDfgNxGoodsId(toFatherId);
                    nxDistributerFatherGoodsService.save(targetFatherGoods);

                    // 处理 grand (Level 1)
                    if (grandName != null && grandFatherId != null) {
                        Map<String, Object> grandMap = new HashMap<>();
                        grandMap.put("disId", disId);
                        grandMap.put("fathersFatherName", grandName);
                        List<NxDistributerFatherGoodsEntity> existingGrandGoods = nxDistributerFatherGoodsService.queryHasDisFathersFather(grandMap);

                        if (existingGrandGoods != null && !existingGrandGoods.isEmpty()) {
                            // 如果已有 grand，直接关联
                            targetFatherGoods.setNxDfgFathersFatherId(existingGrandGoods.get(0).getNxDistributerFatherGoodsId());
                            nxDistributerFatherGoodsService.update(targetFatherGoods);
                            logger.debug("[exchangeNxGoodsFatherId] 找到已存在的分销商祖父商品，grandGoodsId={}",
                                    existingGrandGoods.get(0).getNxDistributerFatherGoodsId());
                        } else {
                            // 创建 grand
                            NxDistributerFatherGoodsEntity grandGoods = new NxDistributerFatherGoodsEntity();
                            grandGoods.setNxDfgDistributerId(disId);
                            grandGoods.setNxDfgFatherGoodsName(grandName);
                            grandGoods.setNxDfgFatherGoodsLevel(1);
                            grandGoods.setNxDfgGoodsAmount(1);
                            grandGoods.setNxDfgFatherGoodsColor(fatherColor);
                            grandGoods.setNxDfgNxGoodsId(grandFatherId);
                            grandGoods.setNxDfgFatherGoodsImg(grandEntity.getNxGoodsFile());
                            grandGoods.setNxDfgFatherGoodsImgLarge(grandEntity.getNxGoodsFileBig());
                            grandGoods.setNxDfgFatherGoodsSort(grandEntity.getNxGoodsSort());
                            nxDistributerFatherGoodsService.save(grandGoods);

                            targetFatherGoods.setNxDfgFathersFatherId(grandGoods.getNxDistributerFatherGoodsId());
                            nxDistributerFatherGoodsService.update(targetFatherGoods);
                            logger.debug("[exchangeNxGoodsFatherId] 创建新的分销商祖父商品，grandGoodsId={}", grandGoods.getNxDistributerFatherGoodsId());

                            // 处理 greatGrand (Level 0)
                            if (greatGrandName != null && greatGrandFatherId != null) {
                                Map<String, Object> greatGrandMap = new HashMap<>();
                                greatGrandMap.put("disId", disId);
                                greatGrandMap.put("fathersFatherName", greatGrandName);
                                List<NxDistributerFatherGoodsEntity> existingGreatGrandGoods = nxDistributerFatherGoodsService.queryHasDisFathersFather(greatGrandMap);

                                if (existingGreatGrandGoods != null && !existingGreatGrandGoods.isEmpty()) {
                                    // 如果已有 greatGrand，直接关联
                                    grandGoods.setNxDfgFathersFatherId(existingGreatGrandGoods.get(0).getNxDistributerFatherGoodsId());
                                    nxDistributerFatherGoodsService.update(grandGoods);
                                    logger.debug("[exchangeNxGoodsFatherId] 找到已存在的分销商曾祖父商品，greatGrandGoodsId={}",
                                            existingGreatGrandGoods.get(0).getNxDistributerFatherGoodsId());
                                } else {
                                    // 创建 greatGrand（按照 saveDisGoods 的逻辑）
                                    NxGoodsEntity greatGrandEntity = nxGoodsService.queryObject(greatGrandFatherId);
                                    NxDistributerFatherGoodsEntity greatGrandGoods = new NxDistributerFatherGoodsEntity();
                                    greatGrandGoods.setNxDfgFatherGoodsName(greatGrandName);
                                    greatGrandGoods.setNxDfgDistributerId(disId);
                                    greatGrandGoods.setNxDfgFatherGoodsLevel(0);
                                    greatGrandGoods.setNxDfgFatherGoodsColor(fatherColor);
                                    greatGrandGoods.setNxDfgNxGoodsId(greatGrandFatherId);
                                    // 按照 saveDisGoods 的逻辑，使用 grandEntity.getFatherGoods().getNxGoodsFile()
                                    // 因为 grandEntity.getFatherGoods() 就是 greatGrand
                                    greatGrandGoods.setNxDfgFatherGoodsImg(grandEntity.getFatherGoods() != null ? grandEntity.getFatherGoods().getNxGoodsFile() : greatGrandEntity.getNxGoodsFile());
                                    greatGrandGoods.setNxDfgFatherGoodsSort(greatGrandEntity.getNxGoodsSort());
                                    greatGrandGoods.setNxDfgGoodsAmount(1);
                                    nxDistributerFatherGoodsService.save(greatGrandGoods);

                                    grandGoods.setNxDfgFathersFatherId(greatGrandGoods.getNxDistributerFatherGoodsId());
                                    nxDistributerFatherGoodsService.update(grandGoods);
                                    logger.debug("[exchangeNxGoodsFatherId] 创建新的分销商曾祖父商品，greatGrandGoodsId={}", greatGrandGoods.getNxDistributerFatherGoodsId());
                                }
                            }
                        }
                    }

                    logger.info("[exchangeNxGoodsFatherId] 创建新的分销商父商品，disGoodsId={}, fatherGoodsId={}, toFatherId={}",
                            nxChangeGoods.getNxDistributerGoodsId(), targetFatherGoods.getNxDistributerFatherGoodsId(), toFatherId);
                }

                // 更新 nxChangeGoods 的 nxDgDfgGoodsFatherId 和 nxDgDfgGoodsGrandId
                if (targetFatherGoods != null) {
                    nxChangeGoods.setNxDgDfgGoodsFatherId(targetFatherGoods.getNxDistributerFatherGoodsId());
                    if (targetFatherGoods.getNxDfgFathersFatherId() != null) {
                        nxChangeGoods.setNxDgDfgGoodsGrandId(targetFatherGoods.getNxDfgFathersFatherId());
                    }

                    // 查询该配送商下该父级目录的最大 sonsSort，用于设置转移商品的 sonsSort
                    Map<String, Object> disMaxSortMap = new HashMap<>();
                    disMaxSortMap.put("dgFatherId", targetFatherGoods.getNxDistributerFatherGoodsId());
                    int disMaxSonsSort = dgService.queryNxGoodsSonsSortByParams(disMaxSortMap);
                    int disNewSonsSort = disMaxSonsSort + 1;
                    nxChangeGoods.setNxDgGoodsSonsSort(disNewSonsSort);

                    logger.debug("[exchangeNxGoodsFatherId] 更新分销商商品的父商品ID，disGoodsId={}, nxDgDfgGoodsFatherId={}, nxDgDfgGoodsGrandId={}, sonsSort={}",
                            nxChangeGoods.getNxDistributerGoodsId(), targetFatherGoods.getNxDistributerFatherGoodsId(),
                            targetFatherGoods.getNxDfgFathersFatherId(), disNewSonsSort);
                }

                dgService.update(nxChangeGoods);

                // 更新该配送商商品相关的订单和部门商品
                if (targetFatherGoods != null) {
                    Integer disGoodsId = nxChangeGoods.getNxDistributerGoodsId();
                    Integer newFatherId = targetFatherGoods.getNxDistributerFatherGoodsId();
                    Integer newGrandId = targetFatherGoods.getNxDfgFathersFatherId(); // 可能为null

                    // 更新 nxDepartmentOrder 中的 fatherId, grandId
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("disGoodsId", disGoodsId);
                    List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(orderMap);
                    if (ordersEntities != null && !ordersEntities.isEmpty()) {
                        for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                            ordersEntity.setNxDoDisGoodsFatherId(newFatherId);
                            if (newGrandId != null) {
                                ordersEntity.setNxDoDisGoodsGrandId(newGrandId);
                            }
                            depOrdersService.update(ordersEntity);
                        }
                        logger.debug("[exchangeNxGoodsFatherId] 已更新{}个订单记录，disGoodsId={}", 
                                ordersEntities.size(), disGoodsId);
                    }

                    // 更新 nxDepartmentOrderHistory 中的 fatherId, grandId
                    List<NxDepartmentOrderHistoryEntity> historyOrdersEntities = 
                            nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(orderMap);
                    if (historyOrdersEntities != null && !historyOrdersEntities.isEmpty()) {
                        for (NxDepartmentOrderHistoryEntity historyOrderEntity : historyOrdersEntities) {
                            historyOrderEntity.setNxDoDisGoodsFatherId(newFatherId);
                            if (newGrandId != null) {
                                historyOrderEntity.setNxDoDisGoodsGrandId(newGrandId);
                            }
                            nxDepartmentOrderHistoryService.update(historyOrderEntity);
                        }
                        logger.debug("[exchangeNxGoodsFatherId] 已更新{}个历史订单记录，disGoodsId={}", 
                                historyOrdersEntities.size(), disGoodsId);
                    }

                    // 更新 nxDepartDisGoods 中的 fatherId
                    List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = 
                            nxDepartmentDisGoodsService.queryDepDisGoodsByParams(orderMap);
                    if (departmentDisGoodsEntities != null && !departmentDisGoodsEntities.isEmpty()) {
                        for (NxDepartmentDisGoodsEntity depDisGoodsEntity : departmentDisGoodsEntities) {
                            depDisGoodsEntity.setNxDdgDisGoodsFatherId(newFatherId);
                            nxDepartmentDisGoodsService.update(depDisGoodsEntity);
                        }
                        logger.debug("[exchangeNxGoodsFatherId] 已更新{}个部门商品记录，disGoodsId={}", 
                                departmentDisGoodsEntities.size(), disGoodsId);
                    }
                }

                updateCount++;

            }
            logger.info("[exchangeNxGoodsFatherId] 成功更新{}个分销商商品的父ID", updateCount);
        }

        logger.info("[exchangeNxGoodsFatherId] 修改商品父ID完成，changeId={}, toFatherId={}", changeId, toFatherId);
        return R.ok();
    }


    @RequestMapping(value = "/exchangeNxGoodsToGrandId", method = RequestMethod.POST)
    @ResponseBody
    public R exchangeNxGoodsToGrandId(Integer changeId, Integer toGrandId, Integer sort1) {
        logger.info("[exchangeNxGoodsToGrandId] 开始修改商品祖父级ID，changeId={}, toGrandId={}", changeId, toGrandId);

        // 1. 通过商品ID找到父级（三级分类）
        NxGoodsEntity changeGoods = nxGoodsService.queryObject(changeId);
        if (changeGoods == null) {
            logger.error("[exchangeNxGoodsToGrandId] 商品不存在，changeId={}", changeId);
            return R.error(-1, "商品不存在");
        }

        Integer fatherId = changeGoods.getNxGoodsFatherId();
        if (fatherId == null) {
            logger.error("[exchangeNxGoodsToGrandId] 商品没有父级，changeId={}", changeId);
            return R.error(-1, "商品没有父级");
        }

        NxGoodsEntity fatherEntity = nxGoodsService.queryObject(fatherId);
        if (fatherEntity == null) {
            logger.error("[exchangeNxGoodsToGrandId] 父级不存在，fatherId={}", fatherId);
            return R.error(-1, "父级不存在");
        }

        Integer oldGrandId = fatherEntity.getNxGoodsGrandId();
        logger.info("[exchangeNxGoodsToGrandId] 父级ID={}, 原祖父级ID={}, 新祖父级ID={}, 父级名称={}",
                fatherId, oldGrandId, toGrandId, fatherEntity.getNxGoodsName());

        // 2. 更新该父级（三级分类）本身的 nxGoodsFatherId、nxGoodsGrandId 和排序
        // 获取新二级分类的父级（一级分类）
        NxGoodsEntity newGrandEntity = nxGoodsService.queryObject(toGrandId);
        if (newGrandEntity == null) {
            logger.error("[exchangeNxGoodsToGrandId] 新祖父级不存在，toGrandId={}", toGrandId);
            return R.error(-1, "新祖父级不存在");
        }
        Integer newGreatGrandId = newGrandEntity.getNxGoodsFatherId();

        // 更新父级（三级分类）的关联关系
        fatherEntity.setNxGoodsFatherId(toGrandId);  // 父级ID指向新的二级分类
        fatherEntity.setNxGoodsGrandId(toGrandId);  // 祖父级ID应该等于父级ID（level=2的grandId等于它的fatherId）
        fatherEntity.setNxGoodsGreatGrandId(newGreatGrandId);  // 曾祖父级ID指向新二级分类的父级（一级分类）
        if (sort1 != null) {
            fatherEntity.setNxGoodsSort(sort1);
            logger.info("[exchangeNxGoodsToGrandId] 设置父级排序，fatherId={}, sort={}", fatherId, sort1);

            // 更新 sort 之后的其他父级商品的 sort（都加 1）
            // 查询同一个 grand（toGrandId）下，level=2 的商品，sort >= sort1 且不是当前 fatherId 的商品
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("fatherId", toGrandId);  // 查询 toGrandId 下的所有子商品（level=2）
            List<NxGoodsEntity> otherFatherGoods = nxGoodsService.queryListWithFatherId(queryMap);
            if (otherFatherGoods != null && !otherFatherGoods.isEmpty()) {
                int updateCount = 0;
                for (NxGoodsEntity otherFather : otherFatherGoods) {
                    // 排除当前商品，且 sort >= sort1 的商品
                    if (!otherFather.getNxGoodsId().equals(fatherId)
                            && otherFather.getNxGoodsSort() != null
                            && otherFather.getNxGoodsSort() >= sort1) {
                        otherFather.setNxGoodsSort(otherFather.getNxGoodsSort() + 1);
                        nxGoodsService.update(otherFather);
                        updateCount++;

                        // 同时更新该父级下所有子商品（level=3）的 sort
                        Map<String, Object> childMap = new HashMap<>();
                        childMap.put("fatherId", otherFather.getNxGoodsId());
                        List<NxGoodsEntity> childGoods = nxGoodsService.queryListWithFatherId(childMap);
                        if (childGoods != null && !childGoods.isEmpty()) {
                            for (NxGoodsEntity child : childGoods) {
                                child.setNxGoodsSort(otherFather.getNxGoodsSort());
                                nxGoodsService.update(child);
                            }
                        }

                        // 更新配送商相关的 sort
                        Map<String, Object> disFatherMap = new HashMap<>();
                        disFatherMap.put("nxGoodsId", otherFather.getNxGoodsId());
                        List<NxDistributerFatherGoodsEntity> disFatherGoodsList = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(disFatherMap);
                        if (disFatherGoodsList != null && !disFatherGoodsList.isEmpty()) {
                            for (NxDistributerFatherGoodsEntity disFatherGoods : disFatherGoodsList) {
                                disFatherGoods.setNxDfgFatherGoodsSort(otherFather.getNxGoodsSort());
                                nxDistributerFatherGoodsService.update(disFatherGoods);
                            }
                        }
                    }
                }
                logger.info("[exchangeNxGoodsToGrandId] 已更新{}个其他父级商品的sort（+1）", updateCount);
            }
        }
        nxGoodsService.update(fatherEntity);
        logger.info("[exchangeNxGoodsToGrandId] 已更新父级的关联关系，fatherId={}, newFatherId={}, newGrandId={}",
                fatherId, toGrandId, newGreatGrandId);

        // 3. 查询该父级下的所有商品（Level 3），更新它们的 nxGoodsGrandId 和 nxGoodsGreatGrandId
        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);
        List<NxGoodsEntity> goodsList = nxGoodsService.queryListWithFatherId(map);
        logger.info("[exchangeNxGoodsToGrandId] 父级下共有{}个商品需要更新", goodsList.size());

        // 获取父级的 sort（用于更新子商品的 nxGoodsSort）
        Integer fatherSort = sort1 != null ? sort1 : fatherEntity.getNxGoodsSort();

        // 查询该父级下已有商品的最大 sonsSort（包括 goodsList 中的商品）
        // 用于设置转移商品的 sonsSort（排在最后）
        int maxSonsSort = 0;
        for (NxGoodsEntity existing : goodsList) {
            if (existing.getNxGoodsSonsSort() != null && existing.getNxGoodsSonsSort() > maxSonsSort) {
                maxSonsSort = existing.getNxGoodsSonsSort();
            }
        }
        // 还需要查询该父级下其他商品的最大 sonsSort（如果有的话）
        Map<String, Object> maxSortMap = new HashMap<>();
        maxSortMap.put("fatherId", fatherId);
        List<NxGoodsEntity> allGoodsInFather = nxGoodsService.queryListWithFatherId(maxSortMap);
        if (allGoodsInFather != null && !allGoodsInFather.isEmpty()) {
            for (NxGoodsEntity existing : allGoodsInFather) {
                if (existing.getNxGoodsSonsSort() != null && existing.getNxGoodsSonsSort() > maxSonsSort) {
                    maxSonsSort = existing.getNxGoodsSonsSort();
                }
            }
        }
        int newSonsSort = maxSonsSort + 1;
        logger.info("[exchangeNxGoodsToGrandId] 父级下最大sonsSort={}, 转移商品的sonsSort从{}开始", maxSonsSort, newSonsSort);

        for (NxGoodsEntity goods : goodsList) {
            goods.setNxGoodsGrandId(toGrandId);  // 更新为新的二级分类ID
            goods.setNxGoodsGreatGrandId(newGreatGrandId);  // 更新为新的一级分类ID
            goods.setNxGoodsSort(fatherSort);  // nxGoodsSort 应该等于父级的 sort
            goods.setNxGoodsSonsSort(newSonsSort);  // sonsSort 设置为新父级下的最后一位
            newSonsSort++;  // 每个商品递增
            nxGoodsService.update(goods);
        }
        logger.info("[exchangeNxGoodsToGrandId] 已更新父级下所有商品的祖父级ID、曾祖父级ID和排序");

        // 4. 获取新一级分类的信息（用于后续创建目录）
        NxGoodsEntity newGreatGrandEntity = newGreatGrandId != null ? nxGoodsService.queryObject(newGreatGrandId) : null;
        String newGrandName = newGrandEntity.getNxGoodsName();
        String newGreatGrandName = newGreatGrandEntity != null ? newGreatGrandEntity.getNxGoodsName() : null;

        // 获取颜色（根据新一级分类ID）
        String fatherColor = null;
        if (newGreatGrandId != null) {
            if (newGreatGrandId.equals(1)) {
                fatherColor = "#20afb8";
            } else if (newGreatGrandId.equals(2)) {
                fatherColor = "#f5c832";
            } else if (newGreatGrandId.equals(3)) {
                fatherColor = "#3cc36e";
            } else if (newGreatGrandId.equals(4)) {
                fatherColor = "#f09628";
            } else if (newGreatGrandId.equals(5)) {
                fatherColor = "#1ebaee";
            } else if (newGreatGrandId.equals(6)) {
                fatherColor = "#f05a32";
            } else if (newGreatGrandId.equals(7)) {
                fatherColor = "#c0a6dd";
            } else if (newGreatGrandId.equals(8)) {
                fatherColor = "#969696";
            } else if (newGreatGrandId.equals(9)) {
                fatherColor = "#318666";
            } else if (newGreatGrandId.equals(10)) {
                fatherColor = "#026bc2";
            } else if (newGreatGrandId.equals(11)) {
                fatherColor = "#06eb6d";
            } else if (newGreatGrandId.equals(12)) {
                fatherColor = "#0690eb";
            }
        }

        // 5. 对于每个已下载该父级下商品的配送商，更新目录结构
        // 5.1 查询所有使用该父级下商品的配送商商品
        Set<Integer> distributerIds = new HashSet<>();
        for (NxGoodsEntity goods : goodsList) {
            List<NxDistributerGoodsEntity> distributerGoodsList = dgService.querydisGoodsByNxGoodsId(goods.getNxGoodsId());
            for (NxDistributerGoodsEntity disGoods : distributerGoodsList) {
                distributerIds.add(disGoods.getNxDgDistributerId());
            }
        }
        logger.info("[exchangeNxGoodsToGrandId] 共有{}个配送商需要更新目录结构", distributerIds.size());

        // 5.2 对于每个配送商，补齐分类并更新目录
        for (Integer disId : distributerIds) {
            logger.debug("[exchangeNxGoodsToGrandId] 处理配送商，disId={}", disId);

            // 5.2.1 检查是否有新祖父级（二级分类）的目录
            Map<String, Object> grandMap = new HashMap<>();
            grandMap.put("disId", disId);
            grandMap.put("fathersFatherName", newGrandName);
            List<NxDistributerFatherGoodsEntity> existingGrandGoods = nxDistributerFatherGoodsService.queryHasDisFathersFather(grandMap);

            NxDistributerFatherGoodsEntity targetGrandGoods = null;
            if (existingGrandGoods != null && !existingGrandGoods.isEmpty()) {
                // 如果已有二级分类目录，直接使用
                targetGrandGoods = existingGrandGoods.get(0);
                logger.debug("[exchangeNxGoodsToGrandId] 找到已存在的二级分类目录，disId={}, grandGoodsId={}",
                        disId, targetGrandGoods.getNxDistributerFatherGoodsId());
            } else {
                // 如果没有，创建二级分类目录（Level 1）
                targetGrandGoods = new NxDistributerFatherGoodsEntity();
                targetGrandGoods.setNxDfgDistributerId(disId);
                targetGrandGoods.setNxDfgFatherGoodsName(newGrandName);
                targetGrandGoods.setNxDfgFatherGoodsLevel(1);
                targetGrandGoods.setNxDfgGoodsAmount(0); // 先设为0，后面会更新
                targetGrandGoods.setNxDfgFatherGoodsColor(fatherColor);
                targetGrandGoods.setNxDfgNxGoodsId(toGrandId);
                targetGrandGoods.setNxDfgFatherGoodsImg(newGrandEntity.getNxGoodsFile());
                targetGrandGoods.setNxDfgFatherGoodsImgLarge(newGrandEntity.getNxGoodsFileBig());
                targetGrandGoods.setNxDfgFatherGoodsSort(newGrandEntity.getNxGoodsSort());
                nxDistributerFatherGoodsService.save(targetGrandGoods);
                logger.debug("[exchangeNxGoodsToGrandId] 创建新的二级分类目录，disId={}, grandGoodsId={}",
                        disId, targetGrandGoods.getNxDistributerFatherGoodsId());

                // 检查是否有一级分类目录
                if (newGreatGrandName != null && newGreatGrandId != null) {
                    Map<String, Object> greatGrandMap = new HashMap<>();
                    greatGrandMap.put("disId", disId);
                    greatGrandMap.put("fathersFatherName", newGreatGrandName);
                    List<NxDistributerFatherGoodsEntity> existingGreatGrandGoods = nxDistributerFatherGoodsService.queryHasDisFathersFather(greatGrandMap);

                    if (existingGreatGrandGoods != null && !existingGreatGrandGoods.isEmpty()) {
                        // 如果已有一级分类目录，直接关联
                        targetGrandGoods.setNxDfgFathersFatherId(existingGreatGrandGoods.get(0).getNxDistributerFatherGoodsId());
                        nxDistributerFatherGoodsService.update(targetGrandGoods);
                        logger.debug("[exchangeNxGoodsToGrandId] 找到已存在的一级分类目录，disId={}, greatGrandGoodsId={}",
                                disId, existingGreatGrandGoods.get(0).getNxDistributerFatherGoodsId());
                    } else {
                        // 如果没有，创建一级分类目录（Level 0）
                        NxDistributerFatherGoodsEntity greatGrandGoods = new NxDistributerFatherGoodsEntity();
                        greatGrandGoods.setNxDfgFatherGoodsName(newGreatGrandName);
                        greatGrandGoods.setNxDfgDistributerId(disId);
                        greatGrandGoods.setNxDfgFatherGoodsLevel(0);
                        greatGrandGoods.setNxDfgFatherGoodsColor(fatherColor);
                        greatGrandGoods.setNxDfgNxGoodsId(newGreatGrandId);
                        greatGrandGoods.setNxDfgFatherGoodsImg(newGreatGrandEntity.getNxGoodsFile());
                        greatGrandGoods.setNxDfgFatherGoodsSort(newGreatGrandEntity.getNxGoodsSort());
                        greatGrandGoods.setNxDfgGoodsAmount(0);
                        nxDistributerFatherGoodsService.save(greatGrandGoods);

                        targetGrandGoods.setNxDfgFathersFatherId(greatGrandGoods.getNxDistributerFatherGoodsId());
                        nxDistributerFatherGoodsService.update(targetGrandGoods);
                        logger.debug("[exchangeNxGoodsToGrandId] 创建新的一级分类目录，disId={}, greatGrandGoodsId={}",
                                disId, greatGrandGoods.getNxDistributerFatherGoodsId());
                    }
                }
            }

            // 5.2.2 检查是否有新父级（三级分类）的目录
            Map<String, Object> fatherMap = new HashMap<>();
            fatherMap.put("disId", disId);
            fatherMap.put("nxGoodsId", fatherId);
            List<NxDistributerFatherGoodsEntity> existingFatherGoods = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(fatherMap);

            NxDistributerFatherGoodsEntity targetFatherGoods = null;
            if (existingFatherGoods != null && !existingFatherGoods.isEmpty()) {
                // 如果已有三级分类目录，更新其关联和排序
                targetFatherGoods = existingFatherGoods.get(0);
                targetFatherGoods.setNxDfgFathersFatherId(targetGrandGoods.getNxDistributerFatherGoodsId());
                // 同步更新排序，确保与 nxGoods 一致
                if (sort1 != null) {
                    targetFatherGoods.setNxDfgFatherGoodsSort(sort1);
                } else {
                    targetFatherGoods.setNxDfgFatherGoodsSort(fatherEntity.getNxGoodsSort());
                }
                nxDistributerFatherGoodsService.update(targetFatherGoods);
                logger.debug("[exchangeNxGoodsToGrandId] 更新已存在的三级分类目录，disId={}, fatherGoodsId={}, sort={}",
                        disId, targetFatherGoods.getNxDistributerFatherGoodsId(),
                        sort1 != null ? sort1 : fatherEntity.getNxGoodsSort());
            } else {
                // 如果没有，创建三级分类目录（Level 2）
                targetFatherGoods = new NxDistributerFatherGoodsEntity();
                targetFatherGoods.setNxDfgDistributerId(disId);
                targetFatherGoods.setNxDfgFatherGoodsName(fatherEntity.getNxGoodsName());
                targetFatherGoods.setNxDfgFatherGoodsLevel(2);
                targetFatherGoods.setNxDfgGoodsAmount(0); // 先设为0，后面会更新
                targetFatherGoods.setNxDfgFatherGoodsImg(fatherEntity.getNxGoodsFile());
                targetFatherGoods.setNxDfgFatherGoodsImgLarge(fatherEntity.getNxGoodsFileBig());
                targetFatherGoods.setNxDfgFatherGoodsColor(fatherColor);
                // 同步设置排序，确保与 nxGoods 一致
                if (sort1 != null) {
                    targetFatherGoods.setNxDfgFatherGoodsSort(sort1);
                } else {
                    targetFatherGoods.setNxDfgFatherGoodsSort(fatherEntity.getNxGoodsSort());
                }
                targetFatherGoods.setNxDfgNxGoodsId(fatherId);
                targetFatherGoods.setNxDfgFathersFatherId(targetGrandGoods.getNxDistributerFatherGoodsId());
                nxDistributerFatherGoodsService.save(targetFatherGoods);
                logger.debug("[exchangeNxGoodsToGrandId] 创建新的三级分类目录，disId={}, fatherGoodsId={}, sort={}",
                        disId, targetFatherGoods.getNxDistributerFatherGoodsId(),
                        sort1 != null ? sort1 : fatherEntity.getNxGoodsSort());
            }

            // 5.2.3 更新所有相关配送商商品的目录关联和排序
            // 查询该配送商下该父级目录的最大 sonsSort
            Map<String, Object> disMaxSortMap = new HashMap<>();
            disMaxSortMap.put("dgFatherId", targetFatherGoods.getNxDistributerFatherGoodsId());
            int disMaxSonsSort = dgService.queryNxGoodsSonsSortByParams(disMaxSortMap);
            int disNewSonsSort = disMaxSonsSort + 1;
            logger.debug("[exchangeNxGoodsToGrandId] 配送商disId={}下父级目录最大sonsSort={}, 新商品的sonsSort={}",
                    disId, disMaxSonsSort, disNewSonsSort);

            for (NxGoodsEntity goods : goodsList) {
                List<NxDistributerGoodsEntity> distributerGoodsList = dgService.querydisGoodsByNxGoodsId(goods.getNxGoodsId());
                for (NxDistributerGoodsEntity disGoods : distributerGoodsList) {
                    if (disGoods.getNxDgDistributerId().equals(disId)) {
                        // 更新配送商商品的关联信息
                        disGoods.setNxDgNxGrandId(toGrandId);
                        disGoods.setNxDgNxGrandName(newGrandName);
                        if (newGreatGrandId != null) {
                            disGoods.setNxDgNxGreatGrandId(newGreatGrandId);
                            disGoods.setNxDgNxGreatGrandName(newGreatGrandName);
                            disGoods.setNxDgNxGoodsFatherColor(fatherColor);
                        }
                        disGoods.setNxDgDfgGoodsFatherId(targetFatherGoods.getNxDistributerFatherGoodsId());
                        disGoods.setNxDgDfgGoodsGrandId(targetGrandGoods.getNxDistributerFatherGoodsId());
                        // 同步更新排序：nxDgGoodsSort 应该等于父级的 sort（即 fatherSort）
                        disGoods.setNxDgGoodsSort(fatherSort);
                        // sonsSort 设置为新父级目录下的最后一位
                        disGoods.setNxDgGoodsSonsSort(disNewSonsSort);
                        disNewSonsSort++;  // 每个商品递增
                        dgService.update(disGoods);

                        // 更新该配送商商品相关的订单和部门商品
                        Integer disGoodsId = disGoods.getNxDistributerGoodsId();
                        Integer newFatherId = targetFatherGoods.getNxDistributerFatherGoodsId();
                        Integer newGrandId = targetGrandGoods.getNxDistributerFatherGoodsId();

                        // 6.1 更新 nxDepartmentOrder 中的 fatherId, grandId
                        Map<String, Object> orderMap = new HashMap<>();
                        orderMap.put("disGoodsId", disGoodsId);
                        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(orderMap);
                        if (ordersEntities != null && !ordersEntities.isEmpty()) {
                            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                                ordersEntity.setNxDoDisGoodsFatherId(newFatherId);
                                ordersEntity.setNxDoDisGoodsGrandId(newGrandId);
                                depOrdersService.update(ordersEntity);
                            }
                            logger.debug("[exchangeNxGoodsToGrandId] 已更新{}个订单记录，disGoodsId={}", 
                                    ordersEntities.size(), disGoodsId);
                        }

                        // 6.2 更新 nxDepartmentOrderHistory 中的 fatherId, grandId
                        List<NxDepartmentOrderHistoryEntity> historyOrdersEntities = 
                                nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(orderMap);
                        if (historyOrdersEntities != null && !historyOrdersEntities.isEmpty()) {
                            for (NxDepartmentOrderHistoryEntity historyOrderEntity : historyOrdersEntities) {
                                historyOrderEntity.setNxDoDisGoodsFatherId(newFatherId);
                                historyOrderEntity.setNxDoDisGoodsGrandId(newGrandId);
                                nxDepartmentOrderHistoryService.update(historyOrderEntity);
                            }
                            logger.debug("[exchangeNxGoodsToGrandId] 已更新{}个历史订单记录，disGoodsId={}", 
                                    historyOrdersEntities.size(), disGoodsId);
                        }

                        // 6.3 更新 nxDepartDisGoods 中的 fatherId
                        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = 
                                nxDepartmentDisGoodsService.queryDepDisGoodsByParams(orderMap);
                        if (departmentDisGoodsEntities != null && !departmentDisGoodsEntities.isEmpty()) {
                            for (NxDepartmentDisGoodsEntity depDisGoodsEntity : departmentDisGoodsEntities) {
                                depDisGoodsEntity.setNxDdgDisGoodsFatherId(newFatherId);
                                nxDepartmentDisGoodsService.update(depDisGoodsEntity);
                            }
                            logger.debug("[exchangeNxGoodsToGrandId] 已更新{}个部门商品记录，disGoodsId={}", 
                                    departmentDisGoodsEntities.size(), disGoodsId);
                        }

                        // 更新 GB 项目中 gbDistributerGoods,gbDistributerFatherGoods, gbDepartmentOrder, gbDepart
                    }
                }
            }

            // 5.2.4 更新目录的商品数量
            int goodsCount = 0;
            for (NxGoodsEntity goods : goodsList) {
                List<NxDistributerGoodsEntity> distributerGoodsList = dgService.querydisGoodsByNxGoodsId(goods.getNxGoodsId());
                for (NxDistributerGoodsEntity disGoods : distributerGoodsList) {
                    if (disGoods.getNxDgDistributerId().equals(disId)) {
                        goodsCount++;
                    }
                }
            }
            targetFatherGoods.setNxDfgGoodsAmount(goodsCount);
            nxDistributerFatherGoodsService.update(targetFatherGoods);
            logger.debug("[exchangeNxGoodsToGrandId] 更新三级分类目录的商品数量，disId={}, goodsCount={}", disId, goodsCount);
        }

        logger.info("[exchangeNxGoodsToGrandId] 修改商品祖父级ID完成，changeId={}, toGrandId={}", changeId, toGrandId);

        return R.ok();
    }


    @RequestMapping(value = "/getDisLinshiGoods/{disId}")
    @ResponseBody
    public R getDisLinshiGoods(@PathVariable Integer disId) {
        System.out.println("linshissinsish" + disId);
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryLinshiGoods(disId);
        return R.ok().put("data", goodsEntities);
    }

    @RequestMapping(value = "/supplierUpdateDisGoodsWillPrice", method = RequestMethod.POST)
    @ResponseBody
    public R supplierUpdateDisGoodsWillPrice(@RequestBody NxDistributerGoodsEntity goodsEntity) {
        System.out.println("goodoss" + goodsEntity);
        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(goodsEntity.getNxDistributerGoodsId());
        nxDistributerGoodsEntity.setNxDgWillPrice(goodsEntity.getNxDgWillPrice());
        nxDistributerGoodsEntity.setNxDgBuyingPrice(goodsEntity.getNxDgBuyingPrice());
        nxDistributerGoodsEntity.setNxDgBuyingPriceUpdate(formatWhatDay(0));

        nxDistributerGoodsEntity.setNxDgWillPriceOne(goodsEntity.getNxDgWillPriceOne());
        nxDistributerGoodsEntity.setNxDgWillPriceOneStandard(goodsEntity.getNxDgWillPriceOneStandard());
        nxDistributerGoodsEntity.setNxDgWillPriceOneAboutPrice(goodsEntity.getNxDgWillPriceOneAboutPrice());
        nxDistributerGoodsEntity.setNxDgBuyingPriceOne(goodsEntity.getNxDgBuyingPriceOne());
        nxDistributerGoodsEntity.setNxDgBuyingPriceOneUpdate(formatWhatDay(0));

        nxDistributerGoodsEntity.setNxDgWillPriceTwo(goodsEntity.getNxDgWillPriceTwo());
        nxDistributerGoodsEntity.setNxDgWillPriceTwoWeight(goodsEntity.getNxDgWillPriceTwoWeight());
        nxDistributerGoodsEntity.setNxDgWillPriceTwoStandard(goodsEntity.getNxDgWillPriceTwoStandard());
        nxDistributerGoodsEntity.setNxDgWillPriceTwoAboutPrice(goodsEntity.getNxDgWillPriceTwoAboutPrice());
        nxDistributerGoodsEntity.setNxDgBuyingPriceTwo(goodsEntity.getNxDgBuyingPriceTwo());
        nxDistributerGoodsEntity.setNxDgBuyingPriceTwoUpdate(formatWhatDay(0));

        nxDistributerGoodsEntity.setNxDgWillPriceThree(goodsEntity.getNxDgWillPriceThree());
        nxDistributerGoodsEntity.setNxDgWillPriceThreeWeight(goodsEntity.getNxDgWillPriceThreeWeight());
        nxDistributerGoodsEntity.setNxDgWillPriceThreeStandard(goodsEntity.getNxDgWillPriceThreeStandard());
        nxDistributerGoodsEntity.setNxDgWillPriceThreeAboutPrice(goodsEntity.getNxDgWillPriceThreeAboutPrice());
        nxDistributerGoodsEntity.setNxDgBuyingPriceThree(goodsEntity.getNxDgBuyingPriceThree());
        nxDistributerGoodsEntity.setNxDgBuyingPriceThreeUpdate(formatWhatDay(0));

        dgService.update(nxDistributerGoodsEntity);

        //update PurGoods
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", goodsEntity.getNxDistributerGoodsId());
        map.put("status", 2);
        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(map);
//        if (purchaseGoodsEntities.size() > 0) {
//            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
//                purchaseGoodsEntity.setNxDpgBuyPrice(buyPrice);
//                if (purchaseGoodsEntity.getNxDpgBuyQuantity() != null) {
//                    BigDecimal quantity = new BigDecimal(purchaseGoodsEntity.getNxDpgBuyQuantity());
//                    BigDecimal decimal = new BigDecimal(buyPrice).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
//                    purchaseGoodsEntity.setNxDpgBuySubtotal(decimal.toString());
//                }
//                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
//            }
//        }

        System.out.println("ordoroeoeoeoeoeooe" + map);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map);

//        if (ordersEntities.size() > 0) {
//            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
//                BigDecimal orderPrice = new BigDecimal(ordersEntity.getNxDoPrice());
//                BigDecimal different = new BigDecimal(value).subtract(orderPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
//                BigDecimal orderWeight = new BigDecimal(ordersEntity.getNxDoWeight());
//                BigDecimal newCostPrice = new BigDecimal(buyPrice);
//                BigDecimal newPrice = new BigDecimal(value);
//                BigDecimal newSubtotal = orderWeight.multiply(newPrice);
//                BigDecimal newScale = newPrice.subtract(newCostPrice).divide(newPrice, 2, BigDecimal.ROUND_HALF_UP);
//                BigDecimal newCostSubtotal = orderWeight.multiply(newCostPrice);
//                BigDecimal newProfit = newSubtotal.subtract(newCostSubtotal);
//
//                ordersEntity.setNxDoPrice(value);
//                ordersEntity.setNxDoSubtotal(newSubtotal.toString());
//                ordersEntity.setNxDoCostPrice(buyPrice);
//                ordersEntity.setNxDoCostPriceUpdate(formatWhatDay(0));
//                ordersEntity.setNxDoCostSubtotal(newCostSubtotal.toString());
//                ordersEntity.setNxDoProfitSubtotal(newProfit.toString());
//                ordersEntity.setNxDoProfitScale(newScale.toString());
//                ordersEntity.setNxDoPriceDifferent(different.toString());
//                depOrdersService.update(ordersEntity);
//
//                if (ordersEntity.getNxDoGbDepartmentOrderId() != -1) {
//                    Integer gbDepartmentOrderId = ordersEntity.getNxDoGbDepartmentOrderId();
//                    GbDepartmentOrdersEntity gbDepOrdersEntity = gbDepartmentOrdersService.queryObject(gbDepartmentOrderId);
//                    Integer purchaseGoodsId = gbDepOrdersEntity.getGbDoPurchaseGoodsId();
//                    GbDistributerPurchaseGoodsEntity purchaseGoodsEntity = gbDistributerPurchaseGoodsService.queryObject(purchaseGoodsId);
//                    purchaseGoodsEntity.setGbDpgBuyPrice(value);
//                    if (purchaseGoodsEntity.getGbDpgBuyQuantity() != null) {
//                        BigDecimal quantity = new BigDecimal(purchaseGoodsEntity.getGbDpgBuyQuantity());
//                        BigDecimal decimal = new BigDecimal(value).multiply(quantity).setScale(1, BigDecimal.ROUND_HALF_UP);
//                        purchaseGoodsEntity.setGbDpgBuySubtotal(decimal.toString());
//                        gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
//                    }
//                }
//
//            }
//        }

        return R.ok();
    }

    @RequestMapping(value = "/openGoodsToOrder/{id}")
    @ResponseBody
    public R openGoodsToOrder(@PathVariable Integer id) {
        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(id);
        System.out.println("whwhwhwhwhw" + distributerGoodsEntity.getNxDgGoodsIsHidden());
        if (distributerGoodsEntity.getNxDgGoodsIsHidden() == 1) {
            distributerGoodsEntity.setNxDgGoodsIsHidden(0);
        } else {
            distributerGoodsEntity.setNxDgGoodsIsHidden(1);
        }

        dgService.update(distributerGoodsEntity);
        return R.ok().put("data", distributerGoodsEntity);
    }


//    @RequestMapping(value = "/disUpdateDisGoodsWillPrice", method = RequestMethod.POST)
//    @ResponseBody
//    public R disUpdateDisGoodsWillPrice(Integer goodsId, String buyingPrice,
//                                        String willPrice, String weight,
//                                        Integer level, String profit) {

//        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(goodsId);
//        if(level == 1){
//            nxDistributerGoodsEntity.setNxDgPriceProfitOne(profit);
//            nxDistributerGoodsEntity.setNxDgBuyingPriceOne(buyingPrice);
//            nxDistributerGoodsEntity.setNxDgWillPriceOne(willPrice);
//            nxDistributerGoodsEntity.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
//            nxDistributerGoodsEntity.setNxDgWillPriceOneWeight(weight);
//        }
//        if(level == 2){
//            nxDistributerGoodsEntity.setNxDgPriceProfitTwo(profit);
//            nxDistributerGoodsEntity.setNxDgBuyingPriceTwo(buyingPrice);
//            nxDistributerGoodsEntity.setNxDgWillPriceTwo(willPrice);
//            nxDistributerGoodsEntity.setNxDgBuyingPriceTwoUpdate(formatWhatDate(0));
//            nxDistributerGoodsEntity.setNxDgWillPriceTwoWeight(weight);
//        }
//        if(level == 3){
//            nxDistributerGoodsEntity.setNxDgPriceProfitThree(profit);
//            nxDistributerGoodsEntity.setNxDgBuyingPriceThree(buyingPrice);
//            nxDistributerGoodsEntity.setNxDgWillPriceThree(willPrice);
//            nxDistributerGoodsEntity.setNxDgBuyingPriceThreeUpdate(formatWhatDate(0));
//            nxDistributerGoodsEntity.setNxDgWillPriceThreeWeight(weight);
//        }
//
//        dgService.update(nxDistributerGoodsEntity);
//        return R.ok().put("data", nxDistributerGoodsEntity);
//    }

    @RequestMapping(value = "/saveNxDisLinshiGoodsForApply", method = RequestMethod.POST)
    @ResponseBody
    public R saveNxDisLinshiGoodsForApply(@RequestBody NxDistributerGoodsEntity goods) {


        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disId", goods.getNxDgDistributerId());
        mapG.put("searchStr", goods.getNxDgGoodsName());
        mapG.put("standard", goods.getNxDgGoodsStandardname());

        if (goods.getNxDgGoodsStandardWeight() != null && !goods.getNxDgGoodsStandardWeight().equals("-1")) {
            mapG.put("standardWeight", goods.getNxDgGoodsStandardWeight());
        } else {
            goods.setNxDgGoodsStandardWeight(null);
        }
        if (goods.getNxDgGoodsBrand() != null && !goods.getNxDgGoodsBrand().equals("-1")) {
            mapG.put("brand", goods.getNxDgGoodsBrand());
        } else {
            goods.setNxDgGoodsBrand(null);
        }
        if (goods.getNxDgGoodsPlace() != null && !goods.getNxDgGoodsPlace().equals("-1")) {
            mapG.put("place", goods.getNxDgGoodsPlace());
        } else {
            goods.setNxDgGoodsPlace(null);
        }
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisGoodsByName(mapG);
        if (distributerGoodsEntities.size() > 0) {
            return R.error(-1, "商品已存在");
        } else {
            String goodsName = goods.getNxDgGoodsName();
            Map<String, Object> map = new HashMap<>();
            map.put("disId", goods.getNxDgDistributerId());
            map.put("nxGoodsId", -1);
            map.put("goodsLevel", 2);
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
            NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
            goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
            goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
            goods.setNxDgPurchaseAuto(-1);
            goods.setNxDgWillPrice("0.1");
            goods.setNxDgBuyingPrice("0.1");
            goods.setNxDgBuyingPriceOne("0.1");
            goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
            goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
            goods.setNxDgWillPriceOne("0.1");
            goods.setNxDgWillPriceOneAboutPrice("0.1");
            goods.setNxDgGoodsIsHidden(0);
            String englishKuohao = getEnglishKuohao(goodsName);
            String pinyin = hanziToPinyin(englishKuohao);
            String headPinyin = getHeadStringByString(englishKuohao, false, null);
            goods.setNxDgGoodsPinyin(pinyin);
            goods.setNxDgGoodsPy(headPinyin);
            goods.setNxDgOutTotalWeight("0");
            dgService.save(goods);

            Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
            fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
            dgfService.update(fatherGoodsEntity);

            Integer orderId = goods.getOrderSize();
            NxDepartmentOrdersEntity ordersEntity = depOrdersService.queryObject(orderId);
            ordersEntity.setNxDoNxGoodsId(-1);
            ordersEntity.setNxDoNxGoodsFatherId(-1);
            ordersEntity.setNxDoDisGoodsId(goods.getNxDistributerGoodsId());
            ordersEntity.setNxDoDisGoodsFatherId(goods.getNxDgDfgGoodsFatherId());
            ordersEntity.setNxDoDisGoodsGrandId(goods.getNxDgDfgGoodsGrandId());
            ordersEntity.setNxDoDepDisGoodsId(-1);
            ordersEntity.setNxDoStatus(0);
            ordersEntity.setNxDoPurchaseGoodsId(-1);
            ordersEntity.setNxDoGoodsType(1);
            ordersEntity.setNxDoCostPrice("0.1");
            ordersEntity.setNxDoCostSubtotal("0.1");
            ordersEntity.setNxDoGoodsName(null);
            ordersEntity.setNxDoPrintStandard(goods.getNxDgGoodsStandardname());
            depOrdersService.update(ordersEntity);

            return R.ok().put("data", goods);
        }
    }

    @RequestMapping(value = "/saveNxDisLinshiGoods", method = RequestMethod.POST)
    @ResponseBody
    public R saveNxDisLinshiGoods(@RequestBody NxDistributerGoodsEntity goods) {

        Map<String, Object> mapG = new HashMap<>();
        mapG.put("disId", goods.getNxDgDistributerId());
        mapG.put("searchStr", goods.getNxDgGoodsName());
        mapG.put("standard", goods.getNxDgGoodsStandardname());
        if (goods.getNxDgGoodsStandardWeight() != null && !goods.getNxDgGoodsStandardWeight().equals("-1")) {
            mapG.put("standardWeight", goods.getNxDgGoodsStandardWeight());
        } else {
            goods.setNxDgGoodsStandardWeight(null);
        }
        if (goods.getNxDgGoodsBrand() != null && !goods.getNxDgGoodsBrand().equals("-1")) {
            mapG.put("brand", goods.getNxDgGoodsBrand());
        } else {
            goods.setNxDgGoodsBrand(null);
        }
        if (goods.getNxDgGoodsPlace() != null && !goods.getNxDgGoodsPlace().equals("-1")) {
            mapG.put("place", goods.getNxDgGoodsPlace());
        } else {
            goods.setNxDgGoodsPlace(null);
        }
        System.out.println("smappgpgpgpg" + mapG);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisGoodsByName(mapG);
        if (distributerGoodsEntities.size() > 0) {
            return R.error(-1, "商品已存在");
        } else {
            String goodsName = goods.getNxDgGoodsName();
            Map<String, Object> map = new HashMap<>();
            map.put("disId", goods.getNxDgDistributerId());
            map.put("nxGoodsId", -1);
            map.put("goodsLevel", 2);
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
            NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
            goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
            goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
            goods.setNxDgPurchaseAuto(-1);
            goods.setNxDgWillPrice("0.1");
            goods.setNxDgBuyingPrice("0.1");

            goods.setNxDgBuyingPriceOne("0.1");
            goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
            goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
            goods.setNxDgWillPriceOne("0.1");
            goods.setNxDgWillPriceOneAboutPrice("0.1");
            goods.setNxDgGoodsIsHidden(0);
            String englishKuohao = getEnglishKuohao(goodsName);
            String pinyin = hanziToPinyin(englishKuohao);
            String headPinyin = getHeadStringByString(englishKuohao, false, null);
            goods.setNxDgGoodsPinyin(pinyin);
            goods.setNxDgGoodsPy(headPinyin);
            goods.setNxDgOutTotalWeight("0");
            dgService.save(goods);

            Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
            fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
            dgfService.update(fatherGoodsEntity);

            return R.ok().put("data", goods);
        }
    }


    @RequestMapping(value = "/saveNxDisGoodsAndDown", method = RequestMethod.POST)
    @ResponseBody
    public R saveNxDisGoodsAndDown(@RequestBody NxDepartmentOrdersEntity ordersEntity) {
        NxDistributerGoodsEntity goods = ordersEntity.getNxDistributerGoodsEntity();

        Map<String, Object> map = new HashMap<>();
        map.put("disId", 1); //固定 nxDisId==1
        map.put("nxGoodsId", -1);
        map.put("goodsLevel", 2);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
        goods.setNxDgPurchaseAuto(-1);
        goods.setNxDgOutTotalWeight("0");
        goods.setNxDgBuyingPriceOne("0.1");
        goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
        goods.setNxDgWillPriceOne("0.1");
        goods.setNxDgWillPriceOneAboutPrice("0.1");
        dgService.save(goods);

        downDisGoods(goods, ordersEntity);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
        fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
        dgfService.update(fatherGoodsEntity);
        return R.ok().put("data", goods);

    }


    private void downDisGoods(NxDistributerGoodsEntity nxDistributerGoodsEntity, NxDepartmentOrdersEntity ordersEntity) {
        GbDistributerGoodsEntity cgnGoods = new GbDistributerGoodsEntity();
        cgnGoods.setGbDgGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
        cgnGoods.setGbDgGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
        cgnGoods.setGbDgGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
        cgnGoods.setGbDgGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
        cgnGoods.setGbDgGoodsStandardWeight(nxDistributerGoodsEntity.getNxDgGoodsStandardWeight());
        cgnGoods.setGbDgGoodsDetail(nxDistributerGoodsEntity.getNxDgGoodsDetail());
        cgnGoods.setGbDgGoodsBrand(nxDistributerGoodsEntity.getNxDgGoodsBrand());
        cgnGoods.setGbDgGoodsPlace(nxDistributerGoodsEntity.getNxDgGoodsPlace());

        cgnGoods.setGbDgDistributerId(ordersEntity.getNxDoGbDistributerId());
        cgnGoods.setGbDgGoodsStatus(0);
        cgnGoods.setGbDgGoodsIsWeight(0);
        cgnGoods.setGbDgNxGoodsId(nxDistributerGoodsEntity.getNxDgNxGoodsId());
        cgnGoods.setGbDgNxFatherId(nxDistributerGoodsEntity.getNxDgNxFatherId());
        cgnGoods.setGbDgNxGrandId(nxDistributerGoodsEntity.getNxDgNxGrandId());
        cgnGoods.setGbDgNxGreatGrandId(nxDistributerGoodsEntity.getNxDgNxGreatGrandId());
        cgnGoods.setGbDgPullOff(0);
        cgnGoods.setGbDgGoodsType(5);
        cgnGoods.setGbDgNxDistributerId(nxDistributerGoodsEntity.getNxDgDistributerId());
        cgnGoods.setGbDgNxDistributerGoodsId(nxDistributerGoodsEntity.getNxDistributerGoodsId());
        cgnGoods.setGbDgGbDepartmentId(ordersEntity.getNxDoGbDepartmentId());
        cgnGoods.setGbDgControlFresh(0);
        cgnGoods.setGbDgControlPrice(0);
        cgnGoods.setGbDgGoodsInventoryType(1);
        cgnGoods.setGbDgIsFranchisePrice(0);
        cgnGoods.setGbDgIsSelfControl(0);
        gbDistributerGoodsService.save(cgnGoods);

        //添加部门商品
        GbDepartmentDisGoodsEntity disGoodsEntity = new GbDepartmentDisGoodsEntity();
        disGoodsEntity.setGbDdgDepGoodsName(cgnGoods.getGbDgGoodsName());
        disGoodsEntity.setGbDdgDisGoodsId(cgnGoods.getGbDistributerGoodsId());
        disGoodsEntity.setGbDdgDisGoodsFatherId(cgnGoods.getGbDgDfgGoodsFatherId());
        disGoodsEntity.setGbDdgDisGoodsGrandId(cgnGoods.getGbDgDfgGoodsGrandId());
        disGoodsEntity.setGbDdgDisGoodsGreatId(cgnGoods.getGbDgDfgGoodsGreatId());
        disGoodsEntity.setGbDdgDepGoodsPinyin(cgnGoods.getGbDgGoodsPinyin());
        disGoodsEntity.setGbDdgDepGoodsPy(cgnGoods.getGbDgGoodsPy());
        disGoodsEntity.setGbDdgDepGoodsStandardname(cgnGoods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgDepartmentId(cgnGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgDepartmentFatherId(cgnGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDepartmentId(cgnGoods.getGbDgGbDepartmentId());
        disGoodsEntity.setGbDdgGbDisId(cgnGoods.getGbDgDistributerId());
        disGoodsEntity.setGbDdgGoodsType(cgnGoods.getGbDgGoodsType());
        disGoodsEntity.setGbDdgStockTotalWeight("0.0");
        disGoodsEntity.setGbDdgStockTotalSubtotal("0.0");
        disGoodsEntity.setGbDdgShowStandardId(-1);
        disGoodsEntity.setGbDdgShowStandardName(cgnGoods.getGbDgGoodsStandardname());
        disGoodsEntity.setGbDdgShowStandardScale("-1");
        disGoodsEntity.setGbDdgShowStandardWeight(null);
        disGoodsEntity.setGbDdgNxDistributerGoodsId(cgnGoods.getGbDgNxDistributerGoodsId());
        gbDepDisGoodsService.save(disGoodsEntity);


        //添加给门店
        //如果是餐饮商品，自动给门店添加部门商品
        Map<String, Object> map = new HashMap<>();
        map.put("disId", cgnGoods.getGbDgDistributerId());
        map.put("type", getGbDepartmentTypeMendian());
        List<GbDepartmentEntity> departmentEntities = gbDepartmentService.queryDepByDepType(map);
        if (departmentEntities.size() > 0) {
            for (GbDepartmentEntity dep : departmentEntities) {
                GbDepartmentDisGoodsEntity disGoodsEntityDep = new GbDepartmentDisGoodsEntity();
                disGoodsEntityDep.setGbDdgDepGoodsName(cgnGoods.getGbDgGoodsName());
                disGoodsEntityDep.setGbDdgDisGoodsId(cgnGoods.getGbDistributerGoodsId());
                disGoodsEntityDep.setGbDdgDisGoodsFatherId(cgnGoods.getGbDgDfgGoodsFatherId());
                disGoodsEntityDep.setGbDdgDisGoodsGrandId(cgnGoods.getGbDgDfgGoodsGrandId());
                disGoodsEntityDep.setGbDdgDisGoodsGreatId(cgnGoods.getGbDgDfgGoodsGreatId());
                disGoodsEntityDep.setGbDdgDepGoodsPinyin(cgnGoods.getGbDgGoodsPinyin());
                disGoodsEntityDep.setGbDdgDepGoodsPy(cgnGoods.getGbDgGoodsPy());
                disGoodsEntityDep.setGbDdgDepGoodsStandardname(cgnGoods.getGbDgGoodsStandardname());
                disGoodsEntityDep.setGbDdgDepartmentId(dep.getGbDepartmentId());
                disGoodsEntityDep.setGbDdgDepartmentFatherId(dep.getGbDepartmentId());
                disGoodsEntityDep.setGbDdgGbDepartmentId(cgnGoods.getGbDgGbDepartmentId());
                disGoodsEntityDep.setGbDdgGbDisId(cgnGoods.getGbDgDistributerId());
                disGoodsEntityDep.setGbDdgGoodsType(cgnGoods.getGbDgGoodsType());
                disGoodsEntityDep.setGbDdgStockTotalWeight("0.0");
                disGoodsEntityDep.setGbDdgStockTotalSubtotal("0.0");
                disGoodsEntityDep.setGbDdgShowStandardId(-1);
                disGoodsEntityDep.setGbDdgShowStandardName(cgnGoods.getGbDgGoodsStandardname());
                disGoodsEntityDep.setGbDdgShowStandardScale("-1");
                disGoodsEntityDep.setGbDdgShowStandardWeight(null);
                disGoodsEntityDep.setGbDdgNxDistributerGoodsId(cgnGoods.getGbDgNxDistributerGoodsId());
                gbDepDisGoodsService.save(disGoodsEntityDep);
            }
        }
    }


    private GbDistributerGoodsEntity saveDisGoodsForNx(GbDistributerGoodsEntity cgnGoods) {

        System.out.println("saeeeforNxxxnnxnxnxnxnxnnxn");

        Integer gbDgDistributerId = cgnGoods.getGbDgDistributerId();
        Integer gbDgNxGrandId = cgnGoods.getGbDgNxGrandId(); //nxGrand 是gb的father 101

        //queryGrandFatherId
        NxGoodsEntity fatherEntity = nxGoodsService.queryObject(gbDgNxGrandId); //叶花菜
        Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
        NxGoodsEntity grandEntity = nxGoodsService.queryObject(grandFatherId);
        cgnGoods.setGbDgNxGrandName(grandEntity.getNxGoodsName());
        cgnGoods.setGbDgNxFatherName(fatherEntity.getNxGoodsName());

        Map<String, Object> map = new HashMap<>();
        map.put("color", "#187e6e");
        map.put("disId", gbDgDistributerId);
        ;
        GbDistributerFatherGoodsEntity greatGrand = gbDgfService.queryAppFatherGoods(map);
        cgnGoods.setGbDgNxGreatGrandId(greatGrand.getGbDistributerFatherGoodsId());
        cgnGoods.setGbDgNxGreatGrandName(greatGrand.getGbDfgFatherGoodsName());

        // 3， 查询父类
        Map<String, Object> map11 = new HashMap<>();
        map11.put("nxGoodsId", fatherEntity.getNxGoodsId());
        map11.put("disId", gbDgDistributerId);
        List<GbDistributerFatherGoodsEntity> fatherGoodsEntities1 = gbDgfService.queryDisFathersGoodsByParamsGb(map11);

        if (fatherGoodsEntities1.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            GbDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities1.get(0);
            Integer nxDfgGoodsAmount = fatherGoodsEntity.getGbDfgGoodsAmount();
            fatherGoodsEntity.setGbDfgGoodsAmount(nxDfgGoodsAmount + 1);
            gbDgfService.update(fatherGoodsEntity);

            //2，保存disId商品
            cgnGoods.setGbDgDfgGoodsFatherId(fatherGoodsEntity.getGbDistributerFatherGoodsId());
            cgnGoods.setGbDgDfgGoodsGrandId(fatherGoodsEntity.getGbDfgFathersFatherId());
            GbDistributerFatherGoodsEntity grandFather = gbDgfService.queryObject(fatherGoodsEntity.getGbDfgFathersFatherId());
            cgnGoods.setGbDgDfgGoodsGreatId(grandFather.getGbDfgFathersFatherId());


            //1 ，先保存disGoods
            gbDistributerGoodsService.save(cgnGoods);
            //
        } else {
            //添加fatherGoods的第一个级别 是nxGrand
            GbDistributerFatherGoodsEntity dgf = new GbDistributerFatherGoodsEntity();
            dgf.setGbDfgDistributerId(cgnGoods.getGbDgDistributerId());
            dgf.setGbDfgFatherGoodsName(fatherEntity.getNxGoodsName());
            dgf.setGbDfgFatherGoodsLevel(2);
            dgf.setGbDfgGoodsAmount(1);
            dgf.setGbDfgPriceAmount(0);
            dgf.setGbDfgPriceTwoAmount(0);
            dgf.setGbDfgPriceThreeAmount(0);
            dgf.setGbDfgNxGoodsId(cgnGoods.getGbDgNxGrandId());
            dgf.setGbDfgFatherGoodsImg(fatherEntity.getNxGoodsFile());
            dgf.setGbDfgFatherGoodsSort(fatherEntity.getNxGoodsSort());
            gbDgfService.save(dgf);
            //更新disGoods的fatherGoodsId
            Integer distributerFatherGoodsId = dgf.getGbDistributerFatherGoodsId();
            cgnGoods.setGbDgDfgGoodsFatherId(distributerFatherGoodsId);
            cgnGoods.setGbDgDfgGoodsGrandId(dgf.getGbDfgFathersFatherId());
            GbDistributerFatherGoodsEntity grandFather = gbDgfService.queryObject(dgf.getGbDfgFathersFatherId());
            cgnGoods.setGbDgDfgGoodsGreatId(grandFather.getGbDfgFathersFatherId());

            gbDistributerGoodsService.save(cgnGoods);
            //继续查询是否有GrandFather
            String fatherName = cgnGoods.getGbDgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", gbDgDistributerId);
            map2.put("fathersFatherName", fatherName);
            map2.put("goodsLevel", 1);
            List<GbDistributerFatherGoodsEntity> grandGoodsFather = gbDgfService.queryHasDisFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                GbDistributerFatherGoodsEntity gbDistributerFatherGoodsEntity = grandGoodsFather.get(0);
                dgf.setGbDfgFathersFatherId(gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId());
                Map<String, Object> mapFather = new HashMap<>();
                mapFather.put("fathersFatherId", gbDistributerFatherGoodsEntity.getGbDistributerFatherGoodsId());
                List<GbDistributerFatherGoodsEntity> fatherGoodsEntities = gbDgfService.queryDisFathersGoodsByParamsGb(mapFather);
                gbDgfService.update(dgf);
            } else {
                //tianjiaGrand
                GbDistributerFatherGoodsEntity grand = new GbDistributerFatherGoodsEntity();
                grand.setGbDfgFatherGoodsName(grandEntity.getNxGoodsName());
                grand.setGbDfgDistributerId(gbDgDistributerId);
                grand.setGbDfgFatherGoodsLevel(1);
                grand.setGbDfgFatherGoodsColor(grandEntity.getColor());
                grand.setGbDfgFatherGoodsImg(grandEntity.getNxGoodsFile());
                grand.setGbDfgFatherGoodsSort(grandEntity.getNxGoodsSort());
                grand.setGbDfgNxGoodsId(grandEntity.getNxGoodsId());
                Map<String, Object> mapApp = new HashMap<>();
                mapApp.put("color", "#187e6e");
                mapApp.put("disId", gbDgDistributerId);
                GbDistributerFatherGoodsEntity app = gbDgfService.queryAppFatherGoods(mapApp);
                grand.setGbDfgFathersFatherId(app.getGbDistributerFatherGoodsId());
                gbDgfService.save(grand);
                dgf.setGbDfgFathersFatherId(grand.getGbDistributerFatherGoodsId());
                gbDgfService.update(dgf);

            }
        }


        return cgnGoods;
    }


    @RequestMapping(value = "/delNxDisGoods/{id}")
    @ResponseBody
    public R delNxDisGoods(@PathVariable Integer id) {
        logger.info("[delNxDisGoods] 开始删除商品，商品ID: {}", id);

        try {
            // 1. 检查是否有客户使用此商品
            Map<String, Object> map4 = new HashMap<>();
            map4.put("disGoodsId", id);
            map4.put("status", 3);
            List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map4);
            if (ordersEntities.size() > 0) {
                logger.warn("[delNxDisGoods] 商品ID: {} 有客户使用，无法删除", id);
                return R.error(-1, "有客户使用此商品");
            }

            // 2. 检查是否有采购数据
            Map<String, Object> purchaseMap = new HashMap<>();
            purchaseMap.put("disGoodsId", id);
            System.out.println("kkkkkkkkpurggogo" + purchaseMap);
            List<NxDistributerPurchaseGoodsEntity> purchaseGoodsList = nxDisPurchaseGoodsService.queryPurchaseGoodsByDisGoodsIdAndDate(purchaseMap);
            if (purchaseGoodsList != null && purchaseGoodsList.size() > 0) {
                logger.warn("[delNxDisGoods] 商品ID: {} 有采购数据，无法删除，采购记录数: {}", id, purchaseGoodsList.size());
                return R.error(-1, "该商品有采购数据，无法删除");
            }

            // 3. 查询并删除货架商品
            Map<String, Object> shelfMap = new HashMap<>();
            shelfMap.put("disGoodsId", id);
            List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsList = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(shelfMap);
            if (shelfGoodsList != null && shelfGoodsList.size() > 0) {
                logger.info("[delNxDisGoods] 商品ID: {} 是货架商品，开始删除货架商品，数量: {}", id, shelfGoodsList.size());
                for (NxDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsList) {
                    nxDistributerGoodsShelfGoodsService.delete(shelfGoods.getNxDistributerGoodsShelfGoodsId());
                }
            }

            // 4. 查询库存批次并删除相关的支出记录
            Map<String, Object> stockMap = new HashMap<>();
            stockMap.put("disGoodsId", id);
            List<NxDistributerGoodsShelfStockEntity> stockList = nxDistributerGoodsShelfStockService.queryStockByDisGoodsIdAndDate(stockMap);
            if (stockList != null && stockList.size() > 0) {
                logger.info("[delNxDisGoods] 商品ID: {} 有库存批次，开始删除相关支出记录，批次数量: {}", id, stockList.size());
                for (NxDistributerGoodsShelfStockEntity stock : stockList) {
                    // 查询该批次的所有支出记录
                    Map<String, Object> reduceMap = new HashMap<>();
                    reduceMap.put("stockId", stock.getNxDistributerGoodsShelfStockId());
                    List<NxDistributerGoodsShelfStockReduceEntity> reduceList = nxDistributerGoodsShelfStockReduceService.queryReduceListByParams(reduceMap);
                    if (reduceList != null && reduceList.size() > 0) {
                        logger.info("[delNxDisGoods] 批次ID: {} 有支出记录，开始删除，数量: {}",
                                stock.getNxDistributerGoodsShelfStockId(), reduceList.size());
                        for (NxDistributerGoodsShelfStockReduceEntity reduce : reduceList) {
                            nxDistributerGoodsShelfStockReduceService.delete(reduce.getNxDistributerGoodsShelfStockReduceId());
                        }
                    }
                }
            }

            // 5. 删除客户商品
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", id);
            List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepDisGoodsService.queryDepDisGoodsByParams(map);
            if (departmentDisGoodsEntities.size() > 0) {
                logger.info("[delNxDisGoods] 删除客户商品，数量: {}", departmentDisGoodsEntities.size());
                for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                    nxDepDisGoodsService.delete(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                }
            }

            // 6. 删除历史订单
            List<NxDepartmentOrdersHistoryEntity> nxDepartmentOrdersHistoryEntities = nxDepartmentOrdersHistoryService.queryDepHistoryOrdersByParams(map);
            if (nxDepartmentOrdersHistoryEntities.size() > 0) {
                logger.info("[delNxDisGoods] 删除历史订单，数量: {}", nxDepartmentOrdersHistoryEntities.size());
                for (NxDepartmentOrdersHistoryEntity historyEntity : nxDepartmentOrdersHistoryEntities) {
                    nxDepartmentOrdersHistoryService.delete(historyEntity.getNxDepartmentOrdersHistoryId());
                }
            }

            // 7. 删除商品
            int i = dgService.delete(id);
            if (i == 1) {
                logger.info("[delNxDisGoods] 商品ID: {} 删除成功", id);
                return R.ok();
            } else {
                logger.error("[delNxDisGoods] 商品ID: {} 删除失败", id);
                return R.error(-1, "删除失败");
            }
        } catch (Exception e) {
            logger.error("[delNxDisGoods] 删除商品异常，商品ID: {}", id, e);
            return R.error(-1, "删除失败：" + e.getMessage());
        }
    }


    @RequestMapping(value = "/changeDisGoodsFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R changeDisGoodsFatherId(Integer disGoodsId, Integer fatherId, Integer grandId) {

        //old
        NxDistributerGoodsEntity goodsEntity = dgService.queryObject(disGoodsId);
        Integer oldfgGoodsFatherId1 = goodsEntity.getNxDgDfgGoodsFatherId();
        NxDistributerFatherGoodsEntity fatherGoodsEntity = dgfService.queryObject(oldfgGoodsFatherId1);
        fatherGoodsEntity.setNxDfgGoodsAmount(fatherGoodsEntity.getNxDfgGoodsAmount() - 1);
        dgfService.update(fatherGoodsEntity);

        //new
        NxDistributerFatherGoodsEntity fatherGoodsEntity1 = dgfService.queryObject(fatherId);
        fatherGoodsEntity1.setNxDfgGoodsAmount(fatherGoodsEntity1.getNxDfgGoodsAmount() + 1);
        dgfService.update(fatherGoodsEntity1);

        goodsEntity.setNxDgDfgGoodsFatherId(fatherId);
        goodsEntity.setNxDgDfgGoodsGrandId(grandId);
        dgService.update(goodsEntity);

        return R.ok();
    }


    @RequestMapping(value = "/getGoodsSubNamesByFatherIdNx/{fatherId}")
    @ResponseBody
    public R getGoodsSubNamesByFatherIdNx(@PathVariable Integer fatherId) {


        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", fatherId);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);

        List<NxDistributerFatherGoodsEntity> newList = new ArrayList<>();

//        for (NxDistributerFatherGoodsEntity fatherGoods : fatherGoodsEntities) {
//            StringBuilder builder = new StringBuilder();
//
//            Map<String, Object> map1 = new HashMap<>();
//            map1.put("dgFatherId", fatherGoods.getNxDistributerFatherGoodsId());
//            List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsByParams(map1);
//            for (NxDistributerGoodsEntity goods : goodsEntities) {
//                String nxGoodsName = goods.getNxDgGoodsName();
//                builder.append(nxGoodsName);
//                builder.append(',');
//            }
//            fatherGoods.setDgGoodsSubNames(builder.toString());
//            newList.add(fatherGoods);
//        }

        return R.ok().put("data", fatherGoodsEntities);
    }


    @RequestMapping(value = "/getNxDistributerGoodsDetail/{disId}")
    @ResponseBody
    public R getNxDistributerGoodsDetail(@PathVariable Integer disId) {

        List<NxDistributerFatherGoodsEntity> greatGrandGoodsEntities = nxDistributerFatherGoodsService.queryDisGoodsCata(disId);
        if (greatGrandGoodsEntities.size() > 0) {
            for (NxDistributerFatherGoodsEntity greatGrandGoodsEntity : greatGrandGoodsEntities) {
                if (greatGrandGoodsEntity.getFatherGoodsEntities().size() > 0) {
                    for (NxDistributerFatherGoodsEntity grandGoodsEntity : greatGrandGoodsEntity.getFatherGoodsEntities()) {
                        System.out.println("grandGoodsEntity==" + grandGoodsEntity.getNxDfgFatherGoodsName());
                        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = grandGoodsEntity.getFatherGoodsEntities();
                        if (fatherGoodsEntities.size() > 0) {
                            for (NxDistributerFatherGoodsEntity fatherGoods : fatherGoodsEntities) {
                                Integer distributerFatherGoodsId = fatherGoods.getNxDistributerFatherGoodsId();

//                                Map<String, Object> map1 = new HashMap<>();
//                                System.out.println("grandGoodsEntity.getNxDistributerFatherGoodsId()" +fatherGoods.getNxDistributerFatherGoodsId());
//                                map.put("dgFatherId", distributerFatherGoodsId);
                                List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDgSubNameByFatherId(distributerFatherGoodsId);
                                fatherGoods.setNxDistributerGoodsEntities(distributerGoodsEntities);
                                System.out.println(fatherGoodsEntities.size() + "sisziiziziz");
                                System.out.println("fatherNameaaaa==" + fatherGoods.getNxDfgFatherGoodsName());
                            }
                        }
                    }
                }
            }

        }

        return R.ok().put("data", greatGrandGoodsEntities);
    }


    @RequestMapping(value = "/getMarketNxGoodsDistributers", method = RequestMethod.POST)
    @ResponseBody
    public R getMarketNxGoodsDistributers(Integer nxGoodsId, String fromLat, String fromLng) {


        List<NxDistributerEntity> distributerEntities = dgService.queryMarketDistributerByNxGoodsId(nxGoodsId);
        List<NxDistributerEntity> newDistributerEntities = new ArrayList<>();

        if (distributerEntities.size() > 0) {

            //获取出发点坐标
//            StringBuilder stringBuilder = new StringBuilder();
//            for (NxDistributerEntity distributerEntity : distributerEntities) {
//                String nxDistributerLan = distributerEntity.getNxDistributerLan();
//                String nxDistributerLun = distributerEntity.getNxDistributerLun();
//                System.out.println("dinanmme" + distributerEntity.getNxDistributerName());
//                System.out.println("dinanmme" + distributerEntity.getNxDistributerLan());
//                System.out.println("dinanmme" + distributerEntity.getNxDistributerLun());
//                String item = nxDistributerLan + "," + nxDistributerLun;
//                System.out.println("itemmee" + item);
//                stringBuilder.append(item + ";");
//            }
            StringBuilder stringBuilder = new StringBuilder();

// 检查 distributerEntities 是否为 null
            if (distributerEntities == null) {
                System.out.println("distributerEntities is null");

            }

            for (NxDistributerEntity distributerEntity : distributerEntities) {
                // 检查 distributerEntity 是否为 null
                if (distributerEntity == null) {
                    System.out.println("distributerEntity is null");
                    continue;
                } else {
                    newDistributerEntities.add(distributerEntity);

                }

                String nxDistributerLan = distributerEntity.getNxDistributerLan();
                String nxDistributerLun = distributerEntity.getNxDistributerLun();

                // 检查字段是否为 null
                if (nxDistributerLan == null || nxDistributerLun == null) {
                    System.out.println("One of the fields is null for distributer: " + distributerEntity.getNxDistributerName());
                    continue; // 跳过当前循环
                }

                String item = nxDistributerLan + "," + nxDistributerLun;
                System.out.println("item: " + item);
                stringBuilder.append(item + ";");
            }

            String substring = stringBuilder.substring(0, stringBuilder.length() - 1);
            String from = fromLat + "," + fromLng;
            String urlString = "http://apis.map.qq.com/ws/distance/v1/optimal_order?mode=driving&from="
                    + from + "&to=" + substring + "&key=" + KEY;
            // 发送请求，返回Json字符串
            String result = "";
            try {
                URL url = new URL(urlString);
                System.out.println(url);
                System.out.println("----");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                // 腾讯地图使用GET
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                // 获取地址解析结果
                System.out.println(in);
                while ((line = in.readLine()) != null) {
                    result += line + "\n";
                }
                in.close();
            } catch (Exception e) {
                e.getMessage();
            }


//		// 转JSON格式
            JSONObject jsonObject = JSONObject.parseObject(result);
            System.out.println(result);
            System.out.println("resutltltltltl");
            String optimal_order = (String) jsonObject.getString("result");

            //获取排序
            String order = JSONObject.parseObject(optimal_order).getString("optimal_order");

            System.out.println(order + "  ====thisisiorder");
            String substring3 = order.substring(1, order.length() - 1);
            System.out.println(substring3 + "  ==stustring33333");

            String[] split = substring3.split(",");

            TreeSet<NxDistributerEntity> treeSet = new TreeSet<>();

            String elements = JSONObject.parseObject(optimal_order).getString("elements");
            List<NxDistributerEntity> list = JSONObject.parseArray(elements, NxDistributerEntity.class);

            System.out.println(list + "  ====list");

            for (int i = 0; i < split.length; i++) {
                System.out.println(split[i] + "spiidititiiti");
                Integer integer = Integer.valueOf(split[i]);
                System.out.println(integer + "    === inteterrerereggggg");

                NxDistributerEntity nxDistributerEntity = newDistributerEntities.get(integer - 1);
                NxDistributerEntity listEnitity = list.get(i);

                System.out.println(listEnitity.getDistance() + "distancececicicicici");
                System.out.println(listEnitity.getDuration() + "durationtitonttino");
                String distance = listEnitity.getDistance();
                String duration = listEnitity.getDuration();
                nxDistributerEntity.setDistance(distance);
                nxDistributerEntity.setDistanceValue(new BigDecimal(distance).doubleValue());
                nxDistributerEntity.setDuration(duration);
                Map<String, Object> map = new HashMap<>();
                map.put("disId", nxDistributerEntity.getNxDistributerId());
                map.put("nxGoodsId", nxGoodsId);
                NxDistributerGoodsEntity nxGoods = dgService.queryOneGoodsAboutNxGoods(map);
                nxDistributerEntity.setPurGoodsTotalString(nxGoods.getNxDgOutTotalWeight());
                treeSet.add(nxDistributerEntity);
            }


            return R.ok().put("data", abcNxDistributerDistance(treeSet));
        } else {
            return R.error(-1, "没有订单");
        }
    }


    private TreeSet<NxDistributerEntity> abcNxDistributerDistance(TreeSet<NxDistributerEntity> newDistributerEntities) {

        TreeSet<NxDistributerEntity> ts = new TreeSet<>(new Comparator<NxDistributerEntity>() {
            @Override
            public int compare(NxDistributerEntity o1, NxDistributerEntity o2) {
                int result;
                if (o2.getDistanceValue() - o1.getDistanceValue() < 0) {
                    result = 1;
                } else if (o2.getDistanceValue() - o1.getDistanceValue() > 0) {
                    result = -1;
                } else {
                    result = 1;
                }

                return result;
            }
        });

        ts.addAll(newDistributerEntities);

        return ts;

    }

    /**
     * 批发商商品列表
     *
     * @param fatherId 父类id
     * @return 批发商商品列表
     */
    @RequestMapping(value = "/disGetDisTypeGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDisTypeGoodsListByFatherId(Integer fatherId, Integer goodsType,
                                              Integer limit, Integer page, Integer hasCartonUnit, Integer hasTraceReport) {
        System.out.println(goodsType + "fatherididiid");

        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("grandId", fatherId);
        if (goodsType != 99) {
            map.put("autoType", goodsType);
            if (goodsType == 11) {
                map.put("autoType", 1);
                map.put("hasSupplier", 1);
            }
        }
        // 添加外包装查询条件：1-有外包装，0-无外包装，null-不筛选
        if (hasCartonUnit != null) {
            map.put("hasCartonUnit", hasCartonUnit);
        }
        // 添加溯源查询条件：1-有溯源，0-无溯源，null-不筛选
        if (hasTraceReport != null) {
            map.put("hasTraceReport", hasTraceReport);
        }

        List<NxDistributerGoodsEntity> goodsEntities1;
        if (goodsType.equals(11)) {
            goodsEntities1 = dgService.querySupplierGoodsByFatherId(map);

        } else {
            goodsEntities1 = dgService.queryDisGoodsByParams(map);
        }

        // 打印货架商品日志
        logger.info("查询到的商品数量: {}", goodsEntities1 != null ? goodsEntities1.size() : 0);
        if (goodsEntities1 != null && goodsEntities1.size() > 0) {
            for (NxDistributerGoodsEntity goods : goodsEntities1) {
                logger.info("商品ID: {}, 商品名称: {}", goods.getNxDistributerGoodsId(), goods.getNxDgGoodsName());
                if (goods.getNxDistributerGoodsShelfGoodsEntities() != null) {
                    logger.info("  货架商品数量: {}", goods.getNxDistributerGoodsShelfGoodsEntities().size());
                    for (int i = 0; i < goods.getNxDistributerGoodsShelfGoodsEntities().size(); i++) {
                        NxDistributerGoodsShelfGoodsEntity shelfGoods = goods.getNxDistributerGoodsShelfGoodsEntities().get(i);
                        logger.info("    货架商品[{}]: ID={}, 货架ID={}, 货架名称={}, 库存数量={}",
                                i,
                                shelfGoods.getNxDistributerGoodsShelfGoodsId(),
                                shelfGoods.getNxDgsgShelfId(),
                                shelfGoods.getShelfName(),
                                shelfGoods.getNxDisGoodsShelfStockEntities() != null ? shelfGoods.getNxDisGoodsShelfStockEntities().size() : 0);
                    }
                } else {
                    logger.info("  该商品没有货架商品数据");
                }
            }
        }

        Map<String, Object> map3 = new HashMap<>();
        map3.put("grandId", fatherId);
        if (goodsType != 99) {
            map3.put("autoType", goodsType);
        }
        // 传递外包装查询条件到总数查询
        if (hasCartonUnit != null) {
            map3.put("hasCartonUnit", hasCartonUnit);
        }
        // 传递溯源查询条件到总数查询
        if (hasTraceReport != null) {
            map3.put("hasTraceReport", hasTraceReport);
        }

        System.out.println(map3 + "map3333");
        int total = dgService.queryDisGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }


    @RequestMapping(value = "/getDgCataGoodsWithSubNames/{disId}")
    @ResponseBody
    public R getDgCataGoodsWithSubNames(@PathVariable Integer disId) {

        List<NxDistributerFatherGoodsEntity> goodsEntities1 = dgfService.queryDisGoodsCata(disId);
        if (goodsEntities1.size() > 0) {
            for (NxDistributerFatherGoodsEntity greatGrandGoods : goodsEntities1) {
                for (NxDistributerFatherGoodsEntity grandGoods : greatGrandGoods.getFatherGoodsEntities()) {
                    for (NxDistributerFatherGoodsEntity fatherGoods : grandGoods.getFatherGoodsEntities()) {
                        StringBuilder builder = new StringBuilder();
                        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDgSubNameByFatherId(fatherGoods.getNxDistributerFatherGoodsId());
                        for (int i = 0; i < goodsEntities.size(); i++) {
                            String nxGoodsName = goodsEntities.get(i).getNxDgGoodsName();
                            if (i == 0) {
                                builder.append(nxGoodsName);
                                builder.append(",");
                            } else {
                                String lastName = goodsEntities.get(i - 1).getNxDgGoodsName();
                                if (!lastName.equals(nxGoodsName)) {
                                    builder.append(nxGoodsName);
                                    builder.append(",");
                                }
                            }
                        }
                        fatherGoods.setDgGoodsSubNames(builder.toString());
                    }
                }
            }
            return R.ok().put("data", goodsEntities1);
        } else {
            return R.error(-1, "没有商品");
        }
    }


    @RequestMapping(value = "/canclePostDgnGoods", method = RequestMethod.POST)
    @ResponseBody
    public R canclePostDgnGoods(Integer disGoodsId, Integer disGoodsFatherId, Integer disId) {
        //判断此商品下是否有客户

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disGoodsId", disGoodsId);
        map4.put("disId", disId);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map4);

        List<NxDepartmentOrderHistoryEntity> historyEntities = nxDepartmentOrderHistoryService.queryDisHistoryOrdersByParams(map4);
        if (ordersEntities.size() > 0 && historyEntities.size() > 0) {
            return R.error(-1, "有客户使用此商品");
        } else {

            Map<String, Object> map1 = new HashMap<>();
            map1.put("disId", disId);
            map1.put("dgFatherId", disGoodsFatherId);
            //搜索fatherId下有几个disGoods
            List<NxDistributerGoodsEntity> totalDisGoods = dgService.queryDisGoodsByParams(map1);
            //如果disGoods的父类只有一个商品
            if (totalDisGoods.size() == 1) {
                //父类Entity
                NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(disGoodsFatherId);
                //disGoods的grandId
                Integer grandId = nxDistributerFatherGoodsEntity.getNxDfgFathersFatherId();
                Map<String, Object> mapGrand = new HashMap<>();
                mapGrand.put("fathersFatherId", grandId);
                mapGrand.put("disId", disId);
                //搜索grand有几个兄弟
                List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = dgfService.queryDisFathersGoodsByParams(mapGrand);
                if (fatherGoodsEntities.size() == 1) {
                    Integer nxDfgFathersFatherId = fatherGoodsEntities.get(0).getNxDfgFathersFatherId();
                    NxDistributerFatherGoodsEntity grandEntity = dgfService.queryObject(nxDfgFathersFatherId);
                    Integer greatGrandId = grandEntity.getNxDfgFathersFatherId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("disId", disId);
                    map.put("fathersFatherId", greatGrandId);
                    List<NxDistributerFatherGoodsEntity> grandGoodsEntities = dgfService.queryDisFathersGoodsByParams(map);

                    //如果grandFather也是只有一个，则删除greatGrandFather
                    if (grandGoodsEntities.size() == 1) {
                        dgfService.delete(greatGrandId);
                    }
                    dgfService.delete(grandId);
                }
                dgfService.delete(disGoodsFatherId);
            } else {
                //父类商品数量减去1
                NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(disGoodsFatherId);
                Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
                nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount - 1);
                dgfService.update(nxDistributerFatherGoodsEntity);
            }

            //删除客户商品
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", disGoodsId);
            map.put("disId", disId);
            List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepDisGoodsService.queryDepDisGoodsByParams(map);
            if(departmentDisGoodsEntities.size() > 0){
                for(NxDepartmentDisGoodsEntity departmentDisGoodsEntity: departmentDisGoodsEntities){
                    nxDepDisGoodsService.delete(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                }
            }

            List<NxDepartmentOrdersHistoryEntity> nxDepartmentOrdersHistoryEntities = nxDepartmentOrdersHistoryService.queryDepHistoryOrdersByParams(map);
            if(nxDepartmentOrdersHistoryEntities.size() > 0){
                for(NxDepartmentOrdersHistoryEntity historyEntity: nxDepartmentOrdersHistoryEntities){
                    nxDepartmentOrdersHistoryService.delete(historyEntity.getNxDepartmentOrdersHistoryId());
                }
            }

            //删除订货单位
            List<NxDistributerStandardEntity> standardEntities = dsService.queryDisStandardByDisGoodsId(disGoodsId);
            if (standardEntities.size() > 0) {
                for (NxDistributerStandardEntity disStandard : standardEntities) {
                    dsService.delete(disStandard.getNxDistributerStandardId());
                }
            }



            //删除别名
            Map<String, Object> mapA = new HashMap<>();
            mapA.put("nxId", disGoodsId);
            List<NxDistributerAliasEntity> aliasEntities = disAliasService.queryAliasByParmas(mapA);
            if (aliasEntities.size() > 0) {
                for (NxDistributerAliasEntity aliasEntity : aliasEntities) {
                    disAliasService.delete(aliasEntity.getNxDistributerAliasId());
                }
            }

            Map<String, Object> mapShelf = new HashMap<>();
            mapShelf.put("disGoodsId", disGoodsId);
            System.out.println("shelldldldlldserarrra"  + mapShelf);
            List<NxDistributerGoodsShelfGoodsEntity> nxDistributerGoodsShelfGoodsEntities = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(mapShelf);
            if(nxDistributerGoodsShelfGoodsEntities.size() > 0){
                for(NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity : nxDistributerGoodsShelfGoodsEntities){
                    nxDistributerGoodsShelfGoodsService.delete(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
                }
            }

            int i = dgService.delete(disGoodsId);
            if (i == 1) {
                return R.ok();
            } else {
                return R.error(-1, "删除失败");
            }
        }
    }


    /**
     * 添加批发商商品
     *
     * @param cgnGoods 批发商商品
     * @return ok
     */
    @RequestMapping(value = "/postDgnGoods", method = RequestMethod.POST)
    @ResponseBody
    public R postDgnGoods(@RequestBody NxDistributerGoodsEntity cgnGoods) {
        return downloadGoodsForDistributer(cgnGoods);
    }

    /**
     * 下载商品到配送商（公共方法，供Service调用）
     *
     * @param cgnGoods 配送商商品实体
     * @return 结果
     */
    public R downloadGoodsForDistributer(NxDistributerGoodsEntity cgnGoods) {
        //判断是否已经下载
        Integer nxDgNxGoodsId = cgnGoods.getNxDgNxGoodsId();
        Integer nxDgDistributerId1 = cgnGoods.getNxDgDistributerId();
        Map<String, Object> map7 = new HashMap<>();
        map7.put("disId", nxDgDistributerId1);
        map7.put("goodsId", nxDgNxGoodsId);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisGoodsByParams(map7);

        if (distributerGoodsEntities.size() > 0) {
            return R.error(-1, "已经下载");
        } else {
            return downloadGoodsForDistributerWithoutCheck(cgnGoods);
        }
    }

    /**
     * 下载商品到配送商（跳过检查，用于全量下载）
     * 注意：此方法不检查商品是否已下载，直接下载。适用于新注册配送商的全量下载场景。
     *
     * @param cgnGoods 配送商商品实体
     * @return 结果
     */
    public R downloadGoodsForDistributerWithoutCheck(NxDistributerGoodsEntity cgnGoods) {
        Integer nxDgNxGoodsId = cgnGoods.getNxDgNxGoodsId();

        // 直接调用saveDisGoods保存商品（不检查是否已下载）
        NxDistributerGoodsEntity nxDistributerGoodsEntity = saveDisGoods(cgnGoods);

        //2，保存dis规格bieming
        // 使用保存后返回的实体ID（saveDisGoods会设置ID）
        Integer nxCgGoodsId = nxDistributerGoodsEntity.getNxDistributerGoodsId();
        //2.1 根据nxGoodsId查询规格列表
        List<NxStandardEntity> ncsEntities = nxStandardService.queryList(nxDgNxGoodsId);
        if (ncsEntities != null && ncsEntities.size() > 0) {
            for (NxStandardEntity standard : ncsEntities) {
                NxDistributerStandardEntity disStandards = new NxDistributerStandardEntity();
                disStandards.setNxDsDisGoodsId(nxCgGoodsId);
                disStandards.setNxDsStandardName(standard.getNxStandardName());
                disStandards.setNxDsStandardError(standard.getNxStandardError());
                disStandards.setNxDsStandardScale(standard.getNxStandardScale());
                disStandards.setNxDsStandardFilePath(standard.getNxStandardFilePath());
                disStandards.setNxDsStandardSort(standard.getNxStandardSort());
                dsService.save(disStandards);
            }
        }


        //添加大包装
        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(nxDgNxGoodsId);
        System.out.println("cuuruurururr" + nxGoodsEntity.getNxGoodsCartonUnit());
        if (nxGoodsEntity.getNxGoodsCartonUnit() != null && !nxGoodsEntity.getNxGoodsCartonUnit().trim().isEmpty()) {
            String nxGoodsCartonUnit = nxGoodsEntity.getNxGoodsCartonUnit();
            NxDistributerStandardEntity disStandards = new NxDistributerStandardEntity();
            disStandards.setNxDsDisGoodsId(nxCgGoodsId);
            disStandards.setNxDsStandardName(nxGoodsCartonUnit);
            disStandards.setNxDsStandardScale(nxGoodsEntity.getNxGoodsItemsPerCarton().toString());
            disStandards.setNxDsStandardSort(1);
            dsService.save(disStandards);
        }


        //2.2 根据nxGoodsId查询别名列表
        Map<String, Object> aliasMap = new HashMap<>();
        aliasMap.put("goodsId", nxDgNxGoodsId);
        List<NxAliasEntity> aliasEntities = nxAliasService.queryNxAliasList(aliasMap);
        if (aliasEntities != null && aliasEntities.size() > 0) {
            for (NxAliasEntity aliasEntity : aliasEntities) {
                NxDistributerAliasEntity disAlias = new NxDistributerAliasEntity();
                disAlias.setNxDaDisGoodsId(nxCgGoodsId);
                disAlias.setNxDaAliasName(aliasEntity.getNxAliasName());
                disAliasService.save(disAlias);
            }
        }

        return R.ok().put("data", nxDistributerGoodsEntity);
    }


    private NxDistributerGoodsEntity saveDisGoods(NxDistributerGoodsEntity cgnGoods) {
        Integer nxDgNxGoodsId = cgnGoods.getNxDgNxGoodsId();
        System.out.println("nxgoidsisissi" + cgnGoods.getNxDgNxGoodsId());
        NxGoodsEntity nxGoodsEntity = nxGoodsService.queryObject(nxDgNxGoodsId);
        cgnGoods.setNxDgGoodsName(nxGoodsEntity.getNxGoodsName());
        cgnGoods.setNxDgNxFatherImg(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setNxDgGoodsStandardname(nxGoodsEntity.getNxGoodsStandardname());
        cgnGoods.setNxDgGoodsDetail(nxGoodsEntity.getNxGoodsDetail());
        cgnGoods.setNxDgGoodsPlace(nxGoodsEntity.getNxGoodsPlace());
        cgnGoods.setNxDgGoodsBrand(nxGoodsEntity.getNxGoodsBrand());
        cgnGoods.setNxDgGoodsStandardWeight(nxGoodsEntity.getNxGoodsStandardWeight());
        cgnGoods.setNxDgGoodsPinyin(nxGoodsEntity.getNxGoodsPinyin());
        cgnGoods.setNxDgGoodsPy(nxGoodsEntity.getNxGoodsPy());
        cgnGoods.setNxDgPullOff(0);
        cgnGoods.setNxDgGoodsStatus(1);
        cgnGoods.setNxStandardEntities(nxGoodsEntity.getNxGoodsStandardEntities());
        cgnGoods.setNxDistributerAliasEntities(nxGoodsEntity.getNxDistributerAliasEntities());
        cgnGoods.setNxDgNxFatherId(nxGoodsEntity.getNxGoodsFatherId());
        cgnGoods.setNxDgNxFatherName(nxGoodsEntity.getFatherGoods().getNxGoodsName());
        cgnGoods.setNxDgNxFatherImg(nxGoodsEntity.getFatherGoods().getNxGoodsFile());
        cgnGoods.setNxDgNxGrandName(nxGoodsEntity.getGrandGoods().getNxGoodsName());
        cgnGoods.setNxDgNxGrandId(nxGoodsEntity.getGrandGoods().getNxGoodsId());
        cgnGoods.setNxDgGoodsFile(nxGoodsEntity.getNxGoodsFile());
        cgnGoods.setNxDgGoodsFileLarge(nxGoodsEntity.getNxGoodsFileBig());
        cgnGoods.setNxDgIsOldestSon(nxGoodsEntity.getNxGoodsIsOldestSon());
        cgnGoods.setNxDgGoodsSort(nxGoodsEntity.getNxGoodsSort());
        cgnGoods.setNxDgGoodsSonsSort(nxGoodsEntity.getNxGoodsSonsSort());
        cgnGoods.setNxDgGoodsIsHidden(0);
        cgnGoods.setNxDgBuyingPriceIsGrade(0);
        cgnGoods.setNxDgPurchaseAuto(-1);
        cgnGoods.setNxDgQuantityDays(nxGoodsEntity.getNxGoodsQuantityDays());
        cgnGoods.setNxDgItemsPerCarton(nxGoodsEntity.getNxGoodsItemsPerCarton());
        cgnGoods.setNxDgCartonUnit(nxGoodsEntity.getNxGoodsCartonUnit());

        cgnGoods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
        cgnGoods.setNxDgWillPrice("0.1");
        cgnGoods.setNxDgBuyingPrice("0.1");
        cgnGoods.setNxDgBuyingPriceOne("0.1");
        cgnGoods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        cgnGoods.setNxDgWillPriceOneStandard(cgnGoods.getNxDgGoodsStandardname());
        cgnGoods.setNxDgWillPriceOne("0.1");
        cgnGoods.setNxDgWillPriceOneAboutPrice("0.1");


        //queryGrandFatherId
        NxGoodsEntity fatherEntity = nxGoodsService.queryObject(cgnGoods.getNxDgNxFatherId());
        Integer grandFatherId = fatherEntity.getNxGoodsFatherId();
        cgnGoods.setNxDgNxGrandId(grandFatherId);
        NxGoodsEntity grandEntity = nxGoodsService.queryObject(grandFatherId);
        cgnGoods.setNxDgNxGrandName(grandEntity.getNxGoodsName());


        //queryGreatGrandFatherId
        Integer greatGrandFatherId = grandEntity.getNxGoodsFatherId();
        if (greatGrandFatherId.equals(1)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#20afb8");
        }
        if (greatGrandFatherId.equals(2)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#f5c832");
        }
        if (greatGrandFatherId.equals(3)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#3cc36e");
        }
        if (greatGrandFatherId.equals(4)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#f09628");
        }
        if (greatGrandFatherId.equals(5)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#1ebaee");
        }
        if (greatGrandFatherId.equals(6)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#f05a32");
        }
        if (greatGrandFatherId.equals(7)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#c0a6dd");
        }
        if (greatGrandFatherId.equals(8)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#969696");
        }
        if (greatGrandFatherId.equals(9)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#318666");
        }
        if (greatGrandFatherId.equals(10)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#026bc2");
        }
        if (greatGrandFatherId.equals(11)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#06eb6d");
        }
        if (greatGrandFatherId.equals(12)) {
            cgnGoods.setNxDgNxGoodsFatherColor("#0690eb");
        }

        cgnGoods.setNxDgNxGreatGrandId(greatGrandFatherId);
        cgnGoods.setNxDgNxGreatGrandName(nxGoodsService.queryObject(greatGrandFatherId).getNxGoodsName());

        Integer nxDgDistributerId = cgnGoods.getNxDgDistributerId();

        // 3， 查询父类
        Integer nxDgNxFatherId = cgnGoods.getNxDgNxFatherId();
        Integer nxDgNxGreatGrandId = cgnGoods.getNxDgNxGreatGrandId();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDgDistributerId);
        map.put("nxFatherId", nxDgNxFatherId);
        // 添加曾祖父ID限制，避免不同曾祖父分类下的同名商品混淆（如"蔬菜"下的"菌菇"和"蔬菜调理"下的"香菇"）
        if (nxDgNxGreatGrandId != null) {
            map.put("nxGreatGrandFatherId", nxDgNxGreatGrandId);
        }
        List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = dgService.queryDisGoodsByParams(map);

        if (nxDistributerGoodsEntities.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsEntities.get(0);
            Integer nxDgDfgGoodsFatherId1 = disGoodsEntity.getNxDgDfgGoodsFatherId();
            Integer nxDgDfgGoodsGrandId = disGoodsEntity.getNxDgDfgGoodsGrandId();

            // 检查父级商品是否存在，如果不存在则抛出异常
            if (nxDgDfgGoodsFatherId1 != null) {
                NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(nxDgDfgGoodsFatherId1);
                if (nxDistributerFatherGoodsEntity == null) {
                    throw new RuntimeException("父级商品不存在，ID: " + nxDgDfgGoodsFatherId1 + ", 配送商品ID: " + disGoodsEntity.getNxDistributerGoodsId());
                }
                Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
                nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount + 1);
                dgfService.update(nxDistributerFatherGoodsEntity);
            } else {
                throw new RuntimeException("配送商品没有关联父级商品ID，配送商品ID: " + disGoodsEntity.getNxDistributerGoodsId());
            }

            //2，保存disId商品
            Integer nxDgDfgGoodsFatherId = disGoodsEntity.getNxDgDfgGoodsFatherId();
            cgnGoods.setNxDgDfgGoodsFatherId(nxDgDfgGoodsFatherId);
            cgnGoods.setNxDgDfgGoodsGrandId(nxDgDfgGoodsGrandId);

            cgnGoods.setNxDgOutTotalWeight("0");
            //1 ，先保存disGoods
            dgService.save(cgnGoods);
            //
        } else {
            //添加fatherGoods的第一个级别
            NxDistributerFatherGoodsEntity dgf = new NxDistributerFatherGoodsEntity();
            dgf.setNxDfgDistributerId(cgnGoods.getNxDgDistributerId());
            dgf.setNxDfgFatherGoodsName(cgnGoods.getNxDgNxFatherName());
            dgf.setNxDfgFatherGoodsLevel(2);
            dgf.setNxDfgGoodsAmount(1);
            dgf.setNxDfgFatherGoodsImg(cgnGoods.getNxDgNxFatherImg());
            dgf.setNxDfgFatherGoodsColor(cgnGoods.getNxDgNxGoodsFatherColor());
            dgf.setNxDfgFatherGoodsSort(nxGoodsEntity.getFatherGoods().getNxGoodsSort());
            dgf.setNxDfgNxGoodsId(cgnGoods.getNxDgNxFatherId());
            dgfService.save(dgf);

            //继续查询是否有GrandFather
            String grandName = cgnGoods.getNxDgNxGrandName();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("disId", nxDgDistributerId);
            map2.put("fathersFatherName", grandName);
            map2.put("fatherGoodsLevel", 1); // grand是level 1
            map2.put("nxGoodsId", cgnGoods.getNxDgNxGrandId()); // 添加nxGoodsId限制，避免同名但不同分类的grand混淆
            List<NxDistributerFatherGoodsEntity> grandGoodsFather = dgfService.queryHasDisFathersFather(map2);
            if (grandGoodsFather.size() > 0) {
                NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = grandGoodsFather.get(0);
                dgf.setNxDfgFathersFatherId(nxDistributerFatherGoodsEntity.getNxDistributerFatherGoodsId());
                dgfService.update(dgf);
                //更新disGoods的fatherGoodsId
                Integer distributerFatherGoodsId = dgf.getNxDistributerFatherGoodsId();
                Integer nxDfgFathersFatherId = dgf.getNxDfgFathersFatherId();
                cgnGoods.setNxDgDfgGoodsFatherId(distributerFatherGoodsId);
                cgnGoods.setNxDgDfgGoodsGrandId(nxDfgFathersFatherId);
                cgnGoods.setNxDgOutTotalWeight("0");
                dgService.save(cgnGoods);

            } else {
                //tianjiaGrand
                NxDistributerFatherGoodsEntity grand = new NxDistributerFatherGoodsEntity();
                String nxCgGrandFatherName = cgnGoods.getNxDgNxGrandName();
                grand.setNxDfgFatherGoodsName(nxCgGrandFatherName);
                grand.setNxDfgDistributerId(cgnGoods.getNxDgDistributerId());
                grand.setNxDfgFatherGoodsLevel(1);
                grand.setNxDfgGoodsAmount(1);
                grand.setNxDfgFatherGoodsColor(cgnGoods.getNxDgNxGoodsFatherColor());
                grand.setNxDfgNxGoodsId(cgnGoods.getNxDgNxGrandId());
                grand.setNxDfgFatherGoodsSort(nxGoodsEntity.getGrandGoods().getNxGoodsSort());
                dgfService.save(grand);

                dgf.setNxDfgFathersFatherId(grand.getNxDistributerFatherGoodsId());
                dgfService.update(dgf);
                //更新disGoods的fatherGoodsId
                Integer distributerFatherGoodsId = dgf.getNxDistributerFatherGoodsId();
                Integer nxDfgFathersFatherId = dgf.getNxDfgFathersFatherId();
                cgnGoods.setNxDgDfgGoodsFatherId(distributerFatherGoodsId);
                cgnGoods.setNxDgDfgGoodsGrandId(nxDfgFathersFatherId);
                cgnGoods.setNxDgOutTotalWeight("0");
                dgService.save(cgnGoods);
                //查询是否有greatGrand
                String greatGrandName = cgnGoods.getNxDgNxGreatGrandName();
                Map<String, Object> map3 = new HashMap<>();
                map3.put("disId", nxDgDistributerId);
                map3.put("fathersFatherName", greatGrandName);
                map3.put("fatherGoodsLevel", 0); // greatGrand是level 0
                map3.put("nxGoodsId", cgnGoods.getNxDgNxGreatGrandId()); // 添加nxGoodsId限制，避免同名但不同分类的greatGrand混淆
                List<NxDistributerFatherGoodsEntity> greatGrandGoodsFather = dgfService.queryHasDisFathersFather(map3);
                if (greatGrandGoodsFather.size() > 0) {
                    NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = greatGrandGoodsFather.get(0);
                    Integer disFatherId = nxDistributerFatherGoodsEntity.getNxDistributerFatherGoodsId();
                    grand.setNxDfgFathersFatherId(disFatherId);
                    dgfService.update(grand);
                } else {
                    NxDistributerFatherGoodsEntity greatGrand = new NxDistributerFatherGoodsEntity();
                    String greatGrandName1 = cgnGoods.getNxDgNxGreatGrandName();
                    greatGrand.setNxDfgFatherGoodsName(greatGrandName1);
                    greatGrand.setNxDfgDistributerId(cgnGoods.getNxDgDistributerId());
                    greatGrand.setNxDfgFatherGoodsLevel(0);
                    greatGrand.setNxDfgFatherGoodsColor(cgnGoods.getNxDgNxGoodsFatherColor());
                    greatGrand.setNxDfgNxGoodsId(cgnGoods.getNxDgNxGreatGrandId());
                    greatGrand.setNxDfgFatherGoodsImg(grandEntity.getFatherGoods().getNxGoodsFile());
                    greatGrand.setNxDfgFatherGoodsSort(grandEntity.getFatherGoods().getNxGoodsSort());
                    greatGrand.setNxDfgGoodsAmount(1);
                    dgfService.save(greatGrand);
                    grand.setNxDfgFathersFatherId(greatGrand.getNxDistributerFatherGoodsId());
                    dgfService.update(grand);
                }
            }
        }
        return cgnGoods;
    }


    /**
     * 批发商商品列表
     *
     * @param fatherId 父类id
     * @return 批发商商品列表
     */
    @RequestMapping(value = "/disGetDisGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R disGetDisGoodsListByFatherId(Integer fatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("grandId", fatherId);
//        map.put("disId", disId);
        List<NxDistributerGoodsEntity> goodsEntities1 = dgService.queryDisGoodsByParams(map);
        return R.ok().put("data", goodsEntities1);
    }

    @RequestMapping(value = "/disGetNxDisGoodsListByFatherId", method = RequestMethod.POST)
    @ResponseBody
    public R disGetNxDisGoodsListByFatherId(Integer fatherId, Integer disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("grandId", fatherId);
        map.put("disId", disId);
        map.put("nxGoodsIdNotEqual", -1);
        List<NxDistributerGoodsEntity> goodsEntities1 = dgService.queryDisGoodsByParams(map);
        return R.ok().put("data", goodsEntities1);
    }


    /**
     * ibook获取含有批发商信息的商品列表
     *
     * @param limit    每页商品数量
     * @param page     第几页
     * @param fatherId 商品父级id
     * @param disId    批发商id
     * @return ibook商品列表
     */
    @RequestMapping(value = "/disGetIbookGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disGetIbookGoods(Integer limit, Integer page, Integer fatherId, Integer disId) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("offset", (page - 1) * limit);
        map1.put("limit", limit);
        map1.put("fatherId", fatherId);
//        map1.put("isHidden", 0);
        List<NxGoodsEntity> nxGoodsEntities1 = nxGoodsService.queryNxGoodsByParams(map1);

        List<NxGoodsEntity> goodsEntities = new ArrayList<>();

        for (NxGoodsEntity goods : nxGoodsEntities1) {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", disId);
            map.put("nxGoodsId", goods.getNxGoodsId());
            NxDistributerGoodsEntity dgGoods = dgService.queryOneGoodsAboutNxGoods(map);

            if (dgGoods != null) {
                goods.setIsDownload(1);
                goods.setNxDistributerGoodsEntity(dgGoods);
                goodsEntities.add(goods);
            } else {
                goods.setIsDownload(0);
                goodsEntities.add(goods);
            }
        }

        int total = nxGoodsService.queryTotalByFatherId(fatherId);
        PageUtils pageUtil = new PageUtils(goodsEntities, total, limit, page);
        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/getDisGoodsOrdersHistory", method = RequestMethod.POST)
    @ResponseBody
    public R getDisGoodsOrdersHistory(Integer disGoodsId, String startDate, String stopDate) {
        System.out.println("abcck");

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        List<Map<String, Object>> orderList = new ArrayList<>();

        List<Map<String, Object>> list = new ArrayList<>();
        if (howManyDaysInPeriod > 0) {
            for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                // dateList
                String whichDay = "";
                if (i == 0) {
                    whichDay = startDate;
                } else {
                    whichDay = afterWhatDay(startDate, i);
                }

                double total = 0;
                Map<String, Object> mapDisGoods = new HashMap<>();
                mapDisGoods.put("disGoodsId", disGoodsId);
                mapDisGoods.put("arriveDate", whichDay);
                System.out.println("abdbdbfaf" + mapDisGoods);
                List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryOrdersForDisGoods(mapDisGoods);

                if (ordersEntities.size() > 0) {
                    total = depOrdersService.queryDisGoodsOrderWeightTotal(mapDisGoods);
                    Map<String, Object> mapthree = new HashMap<>();
                    mapthree.put("date", whichDay);
                    mapthree.put("order", ordersEntities);
                    mapthree.put("total", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP));
                    orderList.add(mapthree);
                }
            }
        }
        return R.ok().put("data", orderList);
    }


    /**
     * 批发商商品详细
     *
     * @param disGoodsId 批发商商品id
     * @return 含有客户的商品
     */
    @RequestMapping(value = "/disGetGoods/{disGoodsId}")
    @ResponseBody
    public R disGetGoods(@PathVariable Integer disGoodsId) {
        //商品信息
        NxDistributerGoodsEntity disGoods = dgService.queryDisGoodsDetail(disGoodsId);

        return R.ok().put("data", disGoods);
    }

    /**
     * 批发商商品详细
     *
     * @param disGoodsId 批发商商品id
     * @return 含有客户的商品
     */
    @RequestMapping(value = "/disGetGoodsDetail/{disGoodsId}")
    @ResponseBody
    public R disGetGoodsDetail(@PathVariable Integer disGoodsId) {

        //商品信息
        NxDistributerGoodsEntity disGoods = dgService.queryDisGoodsDetail(disGoodsId);

        //3ri订单
        List<Map<String, Object>> orderList = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disGoodsId", disGoodsId);
        map1.put("arriveDate", formatWhatDay(0));

        List<NxDepartmentOrdersEntity> departmentOrdersEntities = depOrdersService.queryOrdersForDisGoods(map1);
        map1.put("hasWeight", 1);
        Integer integer = depOrdersService.queryDepOrdersAcount(map1);
        double weightTotal = 0.0;
        if (integer > 0) {
            weightTotal = depOrdersService.queryDisGoodsOrderWeightTotal(map1);
        }
        Map<String, Object> mapone = new HashMap<>();
        mapone.put("date", formatWhatDayString(0));
        mapone.put("order", departmentOrdersEntities);
        mapone.put("total", new BigDecimal(weightTotal).setScale(1, BigDecimal.ROUND_HALF_UP));
        orderList.add(mapone);
        map1.put("arriveDate", formatWhatDay(-1));
        List<NxDepartmentOrdersEntity> departmentOrdersEntities2 = depOrdersService.queryOrdersForDisGoods(map1);
        Integer integer2 = depOrdersService.queryDepOrdersAcount(map1);
        double weightTotalTwo = 0.0;
        if (integer2 > 0) {
            weightTotalTwo = depOrdersService.queryDisGoodsOrderWeightTotal(map1);
        }

        Map<String, Object> maptwo = new HashMap<>();
        maptwo.put("date", formatWhatDayString(-1));
        maptwo.put("order", departmentOrdersEntities2);
        maptwo.put("total", new BigDecimal(weightTotalTwo).setScale(1, BigDecimal.ROUND_HALF_UP));
        orderList.add(maptwo);
        map1.put("arriveDate", formatWhatDay(-2));
        List<NxDepartmentOrdersEntity> departmentOrdersEntities3 = depOrdersService.queryOrdersForDisGoods(map1);
        double weightTotal3 = 0.0;
        Integer integer3 = depOrdersService.queryDepOrdersAcount(map1);
        if (integer3 > 0) {
            weightTotal3 = depOrdersService.queryDisGoodsOrderWeightTotal(map1);
        }
        Map<String, Object> mapthree = new HashMap<>();
        mapthree.put("date", formatWhatDayString(-2));
        mapthree.put("order", departmentOrdersEntities3);
        mapthree.put("total", new BigDecimal(weightTotal3).setScale(1, BigDecimal.ROUND_HALF_UP));
        orderList.add(mapthree);

        //进货
        Map<String, Object> map2 = new HashMap<>();
        map2.put("disGoodsId", disGoodsId);
//        map2.put("dayuStatus", 1);
        List<NxDistributerPurchaseGoodsEntity> disPurchaseGoods = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(map2);

        //客户
        List<NxDepartmentEntity> entities = nxDepDisGoodsService.queryDepartmentsByDisGoodsId(disGoodsId);
        List<GbDepartmentEntity> gbEntities = nxDepDisGoodsService.queryGbDepartmentsByDisGoodsId(disGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("orderArr", orderList);
        map.put("purchaseArr", disPurchaseGoods);
        map.put("goodsInfo", disGoods);
        map.put("departmentArr", entities);
        map.put("gbDepartmentArr", gbEntities);

        return R.ok().put("data", map);
    }

    @RequestMapping(value = "/queryDisGoodsByQuickSearchWithDepIdDis", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisGoodsByQuickSearchWithDepIdDis(String searchStr, Integer disId, Integer depId) {


        logger.info("[queryDisGoodsByQuickSearchWithDepId] 搜索关键词: {}, disId: {}, depId: {}", searchStr, disId, depId);

        // 优化拼音转换逻辑：如果包含汉字才转换，否则直接用原字符串
        String pinyinString = searchStr;
        if (searchStr.matches(".*[\u4E00-\u9FFF]+.*")) {
            pinyinString = hanziToPinyin(searchStr);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depId", depId);
        map.put("searchStr", searchStr);
        map.put("searchPinyin", pinyinString);

        logger.info("[queryDisGoodsByQuickSearchWithDepIdDis] 查询参数: {}", map);

        // 同时查询配送商商品和标准商品库
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStrWithDepOrders(map);
        if (goodsEntities.size() < 100) {
            return R.ok().put("data", goodsEntities);
        } else {
            return R.error(-1, "请继续输入");
        }
    }


    @RequestMapping(value = "/queryDisGoodsByQuickSearchWithDepId", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisGoodsByQuickSearchWithDepId(String searchStr, Integer disId, Integer depId) {
        // 参数校验
        if (searchStr == null || searchStr.trim().isEmpty()) {
            return R.error("搜索关键词不能为空");
        }
        if (disId == null) {
            return R.error("配送商ID不能为空");
        }
        if (depId == null) {
            return R.error("部门ID不能为空");
        }

        logger.info("[queryDisGoodsByQuickSearchWithDepId] 搜索关键词: {}, disId: {}, depId: {}", searchStr, disId, depId);

        // 优化拼音转换逻辑：如果包含汉字才转换，否则直接用原字符串
        String pinyinString = searchStr;
        if (searchStr.matches(".*[\u4E00-\u9FFF]+.*")) {
            pinyinString = hanziToPinyin(searchStr);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depId", depId);
        map.put("searchStr", searchStr);
        map.put("searchPinyin", pinyinString);
        map.put("name", searchStr);

        System.out.println("wokkdeppd" + map);
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if (departmentDisGoodsEntity != null) {
            Integer nxDdgDisGoodsId = departmentDisGoodsEntity.getNxDdgDisGoodsId();
            NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxDdgDisGoodsId);
            List<NxDistributerGoodsEntity> list = new ArrayList<>();
            list.add(nxDistributerGoodsEntity);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("hasDisGoods", 1);
            resultMap.put("disArr", list.size() > 0 ? list : new ArrayList<>());
            resultMap.put("hasNxGoods", false);
            resultMap.put("nxArr", new ArrayList<>());

            return R.ok().put("data", resultMap);

        }

        logger.info("[queryDisGoodsByQuickSearchWithDepId] 查询参数: {}", map);
        // 同时查询配送商商品和标准商品库
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStrWithDepOrders(map);
        // 对结果进行排序：完全匹配的商品名称放在最前面
        if (goodsEntities != null && !goodsEntities.isEmpty()) {
            final String searchStrFinal = searchStr.trim();
            goodsEntities.sort((g1, g2) -> {
                boolean g1ExactMatch = g1.getNxDgGoodsName() != null && g1.getNxDgGoodsName().equals(searchStrFinal);
                boolean g2ExactMatch = g2.getNxDgGoodsName() != null && g2.getNxDgGoodsName().equals(searchStrFinal);
                if (g1ExactMatch && !g2ExactMatch) {
                    return -1; // g1排在前面
                } else if (!g1ExactMatch && g2ExactMatch) {
                    return 1; // g2排在前面
                }
                return 0; // 保持原有顺序
            });
        }
        map.put("level", 3);
        System.out.println("ddkdd" + map);
        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);
        // 过滤掉 level 不等于 3 的商品
        if (nxGoodsEntities != null && !nxGoodsEntities.isEmpty()) {
            List<NxGoodsEntity> filteredByLevel = new ArrayList<>();
            for (NxGoodsEntity goods : nxGoodsEntities) {
                if (goods.getNxGoodsLevel() != null && goods.getNxGoodsLevel() == 3) {
                    filteredByLevel.add(goods);
                }
            }
            nxGoodsEntities = filteredByLevel;
        }

        logger.info("[queryDisGoodsByQuickSearchWithDepId] 配送商商品查询结果数量: {}, 标准商品库查询结果数量: {}",
                goodsEntities.size(), nxGoodsEntities.size());

        // 如果配送商商品结果超过100条，提示用户继续输入
        if (goodsEntities.size() >= 100) {
            return R.error("配送商商品搜索结果过多，请继续输入关键词以缩小搜索范围");
        }

        // 从配送商商品中提取已下载的系统商品ID（配送商品是从系统商品下载的）
        Set<Integer> downloadedNxGoodsIds = new HashSet<>();
        for (NxDistributerGoodsEntity disGoods : goodsEntities) {
            if (disGoods.getNxDgNxGoodsId() != null) {
                downloadedNxGoodsIds.add(disGoods.getNxDgNxGoodsId());
            }
        }

        // 从标准商品库中过滤掉已经在配送商品中存在的商品
        List<NxGoodsEntity> filteredNxGoodsEntities = new ArrayList<>();
        for (NxGoodsEntity nxGoods : nxGoodsEntities) {
            if (nxGoods.getNxGoodsId() != null && !downloadedNxGoodsIds.contains(nxGoods.getNxGoodsId())) {
                filteredNxGoodsEntities.add(nxGoods);
            }
        }

        logger.info("[queryDisGoodsByQuickSearchWithDepId] 已下载的系统商品ID数量: {}, 过滤后的标准商品数量: {}",
                downloadedNxGoodsIds.size(), filteredNxGoodsEntities.size());

        // 如果过滤后的标准商品库结果超过100条，提示用户继续输入
        if (filteredNxGoodsEntities.size() >= 100) {
            return R.error("标准商品库搜索结果过多，请继续输入关键词以缩小搜索范围");
        }

        // 返回两个查询结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("hasDisGoods", goodsEntities.size() > 0);
        resultMap.put("disArr", goodsEntities.size() > 0 ? goodsEntities : new ArrayList<>());
        resultMap.put("hasNxGoods", filteredNxGoodsEntities.size() > 0);
        resultMap.put("nxArr", filteredNxGoodsEntities.size() > 0 ? filteredNxGoodsEntities : new ArrayList<>());

        logger.info("[queryDisGoodsByQuickSearchWithDepId] 返回结果 - 配送商商品: {}条, 标准商品(已去重): {}条",
                goodsEntities.size(), filteredNxGoodsEntities.size());
        return R.ok().put("data", resultMap);
    }


    @RequestMapping(value = "/queryDisShelfGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisShelfGoodsByQuickSearch(String searchStr, String disId) {
        System.out.println(searchStr);
        Map<String, Object> map = new HashMap<>();
        String pinyinString = searchStr;

        map.put("disId", disId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }

        map.put("searchStr", searchStr);
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisShelfGoodsQuickSearchStr(map);
        map.put("searchPinyin", pinyinString);
        map.put("searchStr", null);

        List<NxDistributerGoodsEntity> goodsEntitiesPiyin = dgService.queryDisShelfGoodsQuickSearchStr(map);
        goodsEntitiesPiyin.removeAll(goodsEntities);
        List<NxDistributerGoodsEntity> all = new ArrayList<>();
        all.addAll(goodsEntities);
        all.addAll(goodsEntitiesPiyin);
        return R.ok().put("data", all);
    }

//    @RequestMapping(value = "/queryDisGoodsByQuickSearch", method = RequestMethod.POST)
//    @ResponseBody
//    public R queryDisGoodsByQuickSearch(String searchStr, String disId) {
//        System.out.println(searchStr);
//        Map<String, Object> map = new HashMap<>();
//        String pinyinString = searchStr;
//
//        map.put("disId", disId);
//        for (int i = 0; i < searchStr.length(); i++) {
//            String str = searchStr.substring(i, i + 1);
//            if (str.matches("[\u4E00-\u9FFF]")) {
//                pinyinString = hanziToPinyin(searchStr);
//            }
//        }
//        map.put("searchStr", searchStr);
//        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStr(map);
//        map.put("searchPinyin", pinyinString);
//        map.put("searchStr", null);
//
//        List<NxDistributerGoodsEntity> goodsEntitiesPiyin = dgService.queryDisGoodsQuickSearchStr(map);
//        goodsEntitiesPiyin.removeAll(goodsEntities);
//        List<NxDistributerGoodsEntity> all = new ArrayList<>();
//        all.addAll(goodsEntities);
//        all.addAll(goodsEntitiesPiyin);
//        return R.ok().put("data", all);
//    }

    /**
     * @param searchStr 搜索字符串
     * @param disId     批发商id
     * @return 搜索结果
     */
//    @RequestMapping(value = "/queryDisGoodsByQuickSearch1", method = RequestMethod.POST)
//    @ResponseBody
//    public R queryDisGoodsByQuickSearch1(String searchStr, String disId, String depId) {
//        System.out.println(searchStr);
//        Map<String, Object> map = new HashMap<>();
//
//        map.put("disId", disId);
//        for (int i = 0; i < searchStr.length(); i++) {
//            String str = searchStr.substring(i, i + 1);
//
//            if (str.matches("[\u4E00-\u9FFF]")) {
//                String pinyin = hanziToPinyin(searchStr);
//                map.put("searchStr", searchStr);
//                map.put("searchPinyin", pinyin);
//            } else {
//                map.put("searchStr", searchStr);
//                map.put("searchPinyin", searchStr);
//            }
//        }
//        System.out.println("mapappaSSSS" + map);
//        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStr(map);
//
//        return R.ok().put("data", goodsEntities);
//    }
    @RequestMapping(value = "/queryDisFatherGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisFatherGoodsByQuickSearch(String searchStr, String disId, Integer fatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("fatherId", fatherId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }
        System.out.println("fafas" + map);
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStrByFatherId(map);
        return R.ok().put("data", goodsEntities);
    }

    @RequestMapping(value = "/queryDisExchangeGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisExchangeGoodsByQuickSearch(String searchStr, String disId, Integer fatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("grandId", fatherId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }
        System.out.println("fafas" + map);
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisNxGoodsQuickSearchStrByGrandId(map);
        return R.ok().put("data", goodsEntities);
    }


    @RequestMapping(value = "/queryDisGoodsAndNxGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisGoodsAndNxGoodsByQuickSearch(String searchStr, String disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        String pinyinString = searchStr;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchStr", searchStr);
        map.put("searchPinyin", pinyinString);
        System.out.println("fafnbiibii11111" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisGoodsQuickSearchStr(map);
        map.put("searchStr", null);
        map.put("searchPinyin", pinyinString);
        System.out.println("fafnbiibii" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities1 = dgService.queryDisGoodsQuickSearchStr(map);
        distributerGoodsEntities1.removeAll(distributerGoodsEntities);
        List<NxDistributerGoodsEntity> all = new ArrayList<>();
        all.addAll(distributerGoodsEntities);
        all.addAll(distributerGoodsEntities1);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disArr", all);
        if (all.size() > 0) {
            System.out.println("onnenenenneene" + map);
            map.put("searchStr", searchStr);
            List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);
            // 过滤掉 level 不等于 3 的商品
            if (nxGoodsEntities != null && !nxGoodsEntities.isEmpty()) {
                List<NxGoodsEntity> filteredByLevel = new ArrayList<>();
                for (NxGoodsEntity goods : nxGoodsEntities) {
                    if (goods.getNxGoodsLevel() != null && goods.getNxGoodsLevel() == 3) {
                        filteredByLevel.add(goods);
                    }
                }
                nxGoodsEntities = filteredByLevel;
            }
            if (nxGoodsEntities != null && nxGoodsEntities.size() > 0 && nxGoodsEntities.size() < 100) {
                // 1. 收集所有已存在的商品ID（避免重复）
                Set<Integer> existingGoodsIds = new HashSet<>();
                for (NxDistributerGoodsEntity disGoods : all) {
                    if (disGoods.getNxDgNxGoodsId() != null) {
                        existingGoodsIds.add(disGoods.getNxDgNxGoodsId());
                    }
                }

                // 2. 使用Iterator安全删除重复的商品
                Iterator<NxGoodsEntity> iterator = nxGoodsEntities.iterator();
                while (iterator.hasNext()) {
                    NxGoodsEntity goods = iterator.next();
                    if (existingGoodsIds.contains(goods.getNxGoodsId())) {
                        iterator.remove();
                    }
                }

                map3.put("nxArr", nxGoodsEntities);
            } else {
                map3.put("nxArr", new ArrayList<>());
            }
        } else {
            map3.put("disArr", new ArrayList<>());
            map.put("searchStr", searchStr);
            System.out.println("nxgoods" + map);
            List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);
            // 过滤掉 level 不等于 3 的商品
//            if (nxGoodsEntities != null && !nxGoodsEntities.isEmpty()) {
//                List<NxGoodsEntity> filteredList = new ArrayList<>();
//                for (NxGoodsEntity goods : nxGoodsEntities) {
//                    if (goods.getNxGoodsLevel() != null && goods.getNxGoodsLevel() == 3) {
//                        filteredList.add(goods);
//                    }
//                }
//                nxGoodsEntities = filteredList;
//            }
            if (nxGoodsEntities != null && nxGoodsEntities.size() > 0 && nxGoodsEntities.size() < 100) {
                map3.put("nxArr", nxGoodsEntities);
            } else {
                map3.put("nxArr", new ArrayList<>());
            }

        }

        return R.ok().put("data", map3);
    }

    @RequestMapping(value = "/queryLinshiGoodsAndNxGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryLinshiGoodsAndNxGoodsByQuickSearch(String searchStr, String disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        String pinyinString = searchStr;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchStr", searchStr);
        map.put("searchPinyin", null);
        map.put("nxGoodsId", -1);
        System.out.println("oneoeoneneoeneoee" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisLinshiGoodsQuickSearchStr(map);
        map.put("searchPinyin", pinyinString);
        System.out.println("oneoeoneneoeneoee1111" + map);
        List<NxDistributerGoodsEntity> distributerGoodsEntities1 = dgService.queryDisLinshiGoodsQuickSearchStr(map);
        distributerGoodsEntities1.removeAll(distributerGoodsEntities);
        List<NxDistributerGoodsEntity> all = new ArrayList<>();
        all.addAll(distributerGoodsEntities);
        all.addAll(distributerGoodsEntities1);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disArr", all);
        System.out.println("oneoeoneneoeneoee22222");
        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);

        // 过滤掉 level 不等于 3 的商品
        if (nxGoodsEntities != null && !nxGoodsEntities.isEmpty()) {
            List<NxGoodsEntity> filteredByLevel = new ArrayList<>();
            for (NxGoodsEntity goods : nxGoodsEntities) {
                if (goods.getNxGoodsLevel() != null && goods.getNxGoodsLevel() == 3) {
                    filteredByLevel.add(goods);
                }
            }
            nxGoodsEntities = filteredByLevel;
        }

        if (all.size() > 0) {
            // 1. 收集所有已存在的商品ID（避免重复）
            Set<Integer> existingGoodsIds = new HashSet<>();
            for (NxDistributerGoodsEntity disGoods : all) {
                if (disGoods.getNxDgNxGoodsId() != null) {
                    existingGoodsIds.add(disGoods.getNxDgNxGoodsId());
                }
            }

            // 2. 使用Iterator安全删除重复的商品
            Iterator<NxGoodsEntity> iterator = nxGoodsEntities.iterator();
            while (iterator.hasNext()) {
                NxGoodsEntity goods = iterator.next();
                if (existingGoodsIds.contains(goods.getNxGoodsId())) {
                    iterator.remove();
                }
            }
        }

        map3.put("nxArr", nxGoodsEntities);

        return R.ok().put("data", map3);
    }

//    @RequestMapping(value = "/queryLinshiGoodsAndNxGoodsByQuickSearch", method = RequestMethod.POST)
//    @ResponseBody
//    public R queryLinshiGoodsAndNxGoodsByQuickSearch(String searchStr, String disId) {
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", disId);
//        String pinyinString = searchStr;
//        for (int i = 0; i < searchStr.length(); i++) {
//            String str = searchStr.substring(i, i + 1);
//            if (str.matches("[\u4E00-\u9FFF]")) {
//                pinyinString = hanziToPinyin(searchStr);
//            }
//        }
//        map.put("searchStr", searchStr);
//        map.put("searchPinyin", null);
//        map.put("nxGoodsId", -1);
//        System.out.println("oneoeoneneoeneoee" + map);
//        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisLinshiGoodsQuickSearchStr(map);
//        map.put("searchStr", null);
//        map.put("searchPinyin", pinyinString);
//        System.out.println("oneoeoneneoeneoee1111" + map);
//        List<NxDistributerGoodsEntity> distributerGoodsEntities1 = dgService.queryDisLinshiGoodsQuickSearchStr(map);
//        distributerGoodsEntities1.removeAll(distributerGoodsEntities);
//        List<NxDistributerGoodsEntity> all = new ArrayList<>();
//        all.addAll(distributerGoodsEntities);
//        all.addAll(distributerGoodsEntities1);
//
//        Map<String, Object> map3 = new HashMap<>();
//        map3.put("disArr", all);
//        System.out.println("oneoeoneneoeneoee22222");
//
//        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);
//
//        if (all.size() > 0) {
//            for (NxDistributerGoodsEntity disGoods : all) {
//                if (disGoods.getNxDgNxGoodsId() != null) {
//                    Integer nxDgNxGoodsId = disGoods.getNxDgNxGoodsId();
//                    for (int i = 0; i < nxGoodsEntities.size(); i++) {
//                        Integer nxGoodsId = nxGoodsEntities.get(i).getNxGoodsId();
//                        if (nxDgNxGoodsId.equals(nxGoodsId)) {
//                            nxGoodsEntities.remove(i);
//                        }
//                    }
//                }
//            }
//        }
//
//        map3.put("nxArr", nxGoodsEntities);
//
//        return R.ok().put("data", map3);
//    }

    /**
     * @param searchStr 搜索字符串
     * @param disId     批发商id
     * @return 搜索结果
     */
    @RequestMapping(value = "/queryDepDisGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryDepDisGoodsByQuickSearch(String searchStr, Integer disId, String depId) {

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> map1 = new HashMap<>();
        map.put("disId", disId);
        map1.put("depId", depId);
        map.put("depId", depId);
        map.put("isHidden", 0);
        map1.put("isHidden", 0);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map1.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
                map1.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
                map1.put("searchPinyin", searchStr);
            }
        }

        System.out.println("shaiqntgkkkdkdk" + map);

        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStrWithDepOrders(map);
        TreeSet<NxDepartmentDisGoodsEntity> disGoodsEntityTreeSet = nxDepartmentDisGoodsService.queryDepDisGoodsQuickSearchStr(map1);
        Map<String, Object> map3 = new HashMap<>();
        if (goodsEntities.size() > 0) {
            map3.put("dis", goodsEntities);
        } else {
            map3.put("dis", new ArrayList<>());
        }

        if (goodsEntities.size() > 0) {
            map3.put("dep", disGoodsEntityTreeSet);
        } else {
            map3.put("dep", new ArrayList<>());
        }
        return R.ok().put("data", map3);
    }


    /**
     * @param searchStr 搜索字符串
     * @param disId     批发商id
     * @return 搜索结果 queryGbDepartmentGoodsByQuickSearchGb
     */
    @RequestMapping(value = "/queryDepDisGoodsByQuickSearchJj", method = RequestMethod.POST)
    @ResponseBody
    public R queryDepDisGoodsByQuickSearchJj(String searchStr, Integer disId, String depId, Integer gbDisId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("gbDisId", gbDisId);
        map.put("gbDepId", depId);
        map.put("isHidden", 0);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }

        System.out.println("shaiqntgkkkdkdk" + map);
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStrWithGbDepOrders(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", gbDisId);
        map1.put("depId", depId);
        map1.put("searchStr", searchStr);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map1.put("searchStr", searchStr);
                map1.put("searchPinyin", pinyin);
            } else {
                map1.put("searchPinyin", searchStr);
            }
        }
        map1.put("status", 4);
        map1.put("date", formatWhatDay(0));
        map1.put("pull", 0);
        map1.put("notLinshi", 1);
        System.out.println("depserardkddkdkdk" + map);
        TreeSet<GbDepartmentDisGoodsEntity> disGoodsEntityTreeSet = gbDepartmentDisGoodsService.queryDepDisGoodsQuickSearchStrGb(map1);
        Map<String, Object> map3 = new HashMap<>();
        if (goodsEntities.size() > 0) {
            map3.put("dis", goodsEntities);
        } else {
            map3.put("dis", new ArrayList<>());
        }

        if (goodsEntities.size() > 0) {
            map3.put("dep", disGoodsEntityTreeSet);
        } else {
            map3.put("dep", new ArrayList<>());
        }
        return R.ok().put("data", map3);
    }


    @ResponseBody
    @RequestMapping("/disGoodsUpdate")
    public R update(@RequestBody NxDistributerGoodsEntity nxDistributerGoods) {


        Integer distributerGoodsId = nxDistributerGoods.getNxDistributerGoodsId();
        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(distributerGoodsId);
        Integer oldPurchaseAuto = distributerGoodsEntity.getNxDgPurchaseAuto();
        Integer newPurchaseAuto = nxDistributerGoods.getNxDgPurchaseAuto();

        if (!oldPurchaseAuto.equals(newPurchaseAuto)) {
            Map<String, Object> mapR = new HashMap<>();
            mapR.put("disGoodsId", distributerGoodsId);
            mapR.put("status", 3);
            List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapR);
            if (ordersEntities.size() > 0) {
                // 处理 purchaseAuto 改变的业务逻辑
                // 从出库（-1）改为采购（1）
                if (oldPurchaseAuto == -1 && newPurchaseAuto == 1) {
                    // 遍历所有订单，为每个订单创建或更新采购商品
                    for (NxDepartmentOrdersEntity orderEntity : ordersEntities) {
                        savePurGoodsAuto(orderEntity);
                        depOrdersService.update(orderEntity);
                    }
                }
                // 从采购（1）改为出库（-1）
                else if (oldPurchaseAuto == 1 && newPurchaseAuto == -1) {
                    // 遍历所有订单，删除采购商品并清空订单的采购商品ID
                    for (NxDepartmentOrdersEntity orderEntity : ordersEntities) {
                        Integer purchaseGoodsId = orderEntity.getNxDoPurchaseGoodsId();
                        if (purchaseGoodsId != null && purchaseGoodsId != -1) {
                            // 删除或更新采购商品
                            deletePurchaseGoodsForOrder(orderEntity);
                            // 清空订单的采购商品ID
                            orderEntity.setNxDoPurchaseGoodsId(-1);
                            orderEntity.setNxDoGoodsType(-1);
                            depOrdersService.update(orderEntity);
                        }
                    }
                }

                // 更新商品信息
                String pinyin = hanziToPinyin(nxDistributerGoods.getNxDgGoodsName());
                String headPinyin = getHeadStringByString(nxDistributerGoods.getNxDgGoodsName(), false, null);
                nxDistributerGoods.setNxDgGoodsPinyin(pinyin);
                nxDistributerGoods.setNxDgGoodsPy(headPinyin);
                dgService.update(nxDistributerGoods);

                // 更新GB商品信息
                GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryGbGoodsByNxGoodsId(nxDistributerGoods.getNxDistributerGoodsId());
                if (gbDistributerGoodsEntity != null) {
                    gbDistributerGoodsEntity.setGbDgGoodsName(nxDistributerGoods.getNxDgGoodsName());
                    gbDistributerGoodsEntity.setGbDgGoodsBrand(nxDistributerGoods.getNxDgGoodsBrand());
                    gbDistributerGoodsEntity.setGbDgGoodsStandardname(nxDistributerGoods.getNxDgGoodsStandardname());
                    gbDistributerGoodsEntity.setGbDgGoodsPy(nxDistributerGoods.getNxDgGoodsPy());
                    gbDistributerGoodsEntity.setGbDgGoodsPinyin(nxDistributerGoods.getNxDgGoodsPinyin());
                    gbDistributerGoodsEntity.setGbDgGoodsDetail(nxDistributerGoods.getNxDgGoodsDetail());
                    gbDistributerGoodsEntity.setGbDgGoodsPlace(nxDistributerGoods.getNxDgGoodsPlace());
                    gbDistributerGoodsEntity.setGbDgGoodsStandardWeight(nxDistributerGoods.getNxDgGoodsStandardWeight());
                    gbDistributerGoodsService.update(gbDistributerGoodsEntity);
                    Map<String, Object> map = new HashMap<>();
                    map.put("disGoodsId", gbDistributerGoodsEntity.getGbDistributerGoodsId());
                    List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
                    if (departmentDisGoodsEntities.size() > 0) {
                        for (GbDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                            departmentDisGoodsEntity.setGbDdgDepGoodsName(nxDistributerGoods.getNxDgGoodsName());
                            departmentDisGoodsEntity.setGbDdgDepGoodsBrand(nxDistributerGoods.getNxDgGoodsBrand());
                            departmentDisGoodsEntity.setGbDdgShowStandardName(nxDistributerGoods.getNxDgGoodsStandardname());
                            departmentDisGoodsEntity.setGbDdgDepGoodsDetail(nxDistributerGoods.getNxDgGoodsDetail());
                            departmentDisGoodsEntity.setGbDdgDepGoodsPlace(nxDistributerGoods.getNxDgGoodsPlace());
                            departmentDisGoodsEntity.setGbDdgShowStandardWeight(nxDistributerGoods.getNxDgGoodsStandardWeight());
                            gbDepDisGoodsService.update(departmentDisGoodsEntity);
                        }
                    }
                }

                return R.ok().put("data", nxDistributerGoods);
            } else {
                String pinyin = hanziToPinyin(nxDistributerGoods.getNxDgGoodsName());
                String headPinyin = getHeadStringByString(nxDistributerGoods.getNxDgGoodsName(), false, null);
                nxDistributerGoods.setNxDgGoodsPinyin(pinyin);
                nxDistributerGoods.setNxDgGoodsPy(headPinyin);
                dgService.update(nxDistributerGoods);


                GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryGbGoodsByNxGoodsId(nxDistributerGoods.getNxDistributerGoodsId());
                if (gbDistributerGoodsEntity != null) {
                    gbDistributerGoodsEntity.setGbDgGoodsName(nxDistributerGoods.getNxDgGoodsName());
                    gbDistributerGoodsEntity.setGbDgGoodsBrand(nxDistributerGoods.getNxDgGoodsBrand());
                    gbDistributerGoodsEntity.setGbDgGoodsStandardname(nxDistributerGoods.getNxDgGoodsStandardname());
                    gbDistributerGoodsEntity.setGbDgGoodsPy(nxDistributerGoods.getNxDgGoodsPy());
                    gbDistributerGoodsEntity.setGbDgGoodsPinyin(nxDistributerGoods.getNxDgGoodsPinyin());
                    gbDistributerGoodsEntity.setGbDgGoodsDetail(nxDistributerGoods.getNxDgGoodsDetail());
                    gbDistributerGoodsEntity.setGbDgGoodsPlace(nxDistributerGoods.getNxDgGoodsPlace());
                    gbDistributerGoodsEntity.setGbDgGoodsStandardWeight(nxDistributerGoods.getNxDgGoodsStandardWeight());
                    gbDistributerGoodsService.update(gbDistributerGoodsEntity);
                    Map<String, Object> map = new HashMap<>();
                    map.put("disGoodsId", gbDistributerGoodsEntity.getGbDistributerGoodsId());
                    List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
                    if (departmentDisGoodsEntities.size() > 0) {
                        for (GbDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                            departmentDisGoodsEntity.setGbDdgDepGoodsName(nxDistributerGoods.getNxDgGoodsName());
                            departmentDisGoodsEntity.setGbDdgDepGoodsBrand(nxDistributerGoods.getNxDgGoodsBrand());
                            departmentDisGoodsEntity.setGbDdgShowStandardName(nxDistributerGoods.getNxDgGoodsStandardname());
                            departmentDisGoodsEntity.setGbDdgDepGoodsDetail(nxDistributerGoods.getNxDgGoodsDetail());
                            departmentDisGoodsEntity.setGbDdgDepGoodsPlace(nxDistributerGoods.getNxDgGoodsPlace());
                            departmentDisGoodsEntity.setGbDdgShowStandardWeight(nxDistributerGoods.getNxDgGoodsStandardWeight());

                            gbDepDisGoodsService.update(departmentDisGoodsEntity);
                        }
                    }

                }

                return R.ok().put("data", nxDistributerGoods);
            }

        } else {

            String pinyin = hanziToPinyin(nxDistributerGoods.getNxDgGoodsName());
            String headPinyin = getHeadStringByString(nxDistributerGoods.getNxDgGoodsName(), false, null);
            nxDistributerGoods.setNxDgGoodsPinyin(pinyin);
            nxDistributerGoods.setNxDgGoodsPy(headPinyin);
            dgService.update(nxDistributerGoods);


            GbDistributerGoodsEntity gbDistributerGoodsEntity = gbDistributerGoodsService.queryGbGoodsByNxGoodsId(nxDistributerGoods.getNxDistributerGoodsId());
            if (gbDistributerGoodsEntity != null) {
                gbDistributerGoodsEntity.setGbDgGoodsName(nxDistributerGoods.getNxDgGoodsName());
                gbDistributerGoodsEntity.setGbDgGoodsBrand(nxDistributerGoods.getNxDgGoodsBrand());
                gbDistributerGoodsEntity.setGbDgGoodsStandardname(nxDistributerGoods.getNxDgGoodsStandardname());
                gbDistributerGoodsEntity.setGbDgGoodsPy(nxDistributerGoods.getNxDgGoodsPy());
                gbDistributerGoodsEntity.setGbDgGoodsPinyin(nxDistributerGoods.getNxDgGoodsPinyin());
                gbDistributerGoodsEntity.setGbDgGoodsDetail(nxDistributerGoods.getNxDgGoodsDetail());
                gbDistributerGoodsEntity.setGbDgGoodsPlace(nxDistributerGoods.getNxDgGoodsPlace());
                gbDistributerGoodsEntity.setGbDgGoodsStandardWeight(nxDistributerGoods.getNxDgGoodsStandardWeight());
                gbDistributerGoodsService.update(gbDistributerGoodsEntity);
                Map<String, Object> map = new HashMap<>();
                map.put("disGoodsId", gbDistributerGoodsEntity.getGbDistributerGoodsId());
                List<GbDepartmentDisGoodsEntity> departmentDisGoodsEntities = gbDepDisGoodsService.queryGbDepDisGoodsByParams(map);
                if (departmentDisGoodsEntities.size() > 0) {
                    for (GbDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                        departmentDisGoodsEntity.setGbDdgDepGoodsName(nxDistributerGoods.getNxDgGoodsName());
                        departmentDisGoodsEntity.setGbDdgDepGoodsBrand(nxDistributerGoods.getNxDgGoodsBrand());
                        departmentDisGoodsEntity.setGbDdgShowStandardName(nxDistributerGoods.getNxDgGoodsStandardname());
                        departmentDisGoodsEntity.setGbDdgDepGoodsDetail(nxDistributerGoods.getNxDgGoodsDetail());
                        departmentDisGoodsEntity.setGbDdgDepGoodsPlace(nxDistributerGoods.getNxDgGoodsPlace());
                        departmentDisGoodsEntity.setGbDdgShowStandardWeight(nxDistributerGoods.getNxDgGoodsStandardWeight());
                        gbDepDisGoodsService.update(departmentDisGoodsEntity);
                    }
                }
            }

            return R.ok().put("data", nxDistributerGoods);

        }

    }


    @RequestMapping(value = "/disSaveApplyGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disSaveApplyGoods(@RequestBody NxDistributerGoodsEntity applyGoods) {

        //保存nxgoods
        NxGoodsEntity nxGoodsEntity = new NxGoodsEntity();
        nxGoodsEntity.setNxGoodsName(applyGoods.getNxDgGoodsName());
        nxGoodsEntity.setNxGoodsDetail(applyGoods.getNxDgGoodsDetail());
        nxGoodsEntity.setNxGoodsBrand(applyGoods.getNxDgGoodsBrand());
        nxGoodsEntity.setNxGoodsPlace(applyGoods.getNxDgGoodsPlace());
        nxGoodsEntity.setNxGoodsStandardname(applyGoods.getNxDgGoodsStandardname());
        nxGoodsEntity.setNxGoodsStandardWeight(applyGoods.getNxDgGoodsStandardWeight());
        String pinyin = hanziToPinyin(applyGoods.getNxDgGoodsName());
        String headPinyin = getHeadStringByString(applyGoods.getNxDgGoodsName(), false, null);
        nxGoodsEntity.setNxGoodsPinyin(pinyin);
        nxGoodsEntity.setNxGoodsPy(headPinyin);
        nxGoodsEntity.setNxGoodsFatherId(-1);
        nxGoodsEntity.setNxGoodsApplyNxDistributerId(applyGoods.getNxDgDistributerId());
        nxGoodsService.save(nxGoodsEntity);
        return R.ok().put("data", nxGoodsEntity.getNxGoodsId());
    }


    @ResponseBody
    @RequestMapping("/disSaveDisGoods")
    public R disSaveDisGoods(@RequestBody NxDistributerGoodsEntity nxDistributerGoods) {

        String goodsName = nxDistributerGoods.getNxDgGoodsName();
        String nxGoodsDetail = nxDistributerGoods.getNxDgGoodsDetail();
        String nxGoodsBrand = nxDistributerGoods.getNxDgGoodsBrand();
        String nxDgGoodsStandardname = nxDistributerGoods.getNxDgGoodsStandardname();
        String nxDgGoodsStandardWeight = nxDistributerGoods.getNxDgGoodsStandardWeight();

        Map<String, Object> map = new HashMap<>();
        map.put("goodsName", goodsName);
        map.put("goodsStandard", nxDgGoodsStandardname);
        map.put("goodsDetail", nxGoodsDetail);
        map.put("goodsBrand", nxGoodsBrand);
        map.put("standardWeight", nxDgGoodsStandardWeight);
        map.put("disId", nxDistributerGoods.getNxDgDistributerId());
        map.put("fatherId", nxDistributerGoods.getNxDgDfgGoodsFatherId());
        System.out.println("mapaidididpdppdpdpdpdpdpdpdp" + map);
        List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = dgService.queryIfHasSameDisGoods(map);
        if (nxDistributerGoodsEntities.size() > 1) {

            return R.error(-1, "已有相同商品");

        } else {

            nxDistributerGoods.setNxDgGoodsStatus(1);
            String pinyin = hanziToPinyin(goodsName);
            String englishKuohao = getEnglishKuohao(goodsName);
            String headPinyin = getHeadStringByString(goodsName, false, null);
            nxDistributerGoods.setNxDgGoodsPy(headPinyin);
            nxDistributerGoods.setNxDgGoodsPinyin(pinyin);
            nxDistributerGoods.setNxDgGoodsName(englishKuohao);

            nxDistributerGoods.setNxDgGoodsFile("goodsImage/logo.jpg");
            nxDistributerGoods.setNxDgGoodsFileLarge("goodsImage/logo.jpg");

            Integer nxDgDfgGoodsFatherId = nxDistributerGoods.getNxDgDfgGoodsFatherId();
            Map<String, Object> mapF = new HashMap<>();
            mapF.put("dgFatherId", nxDgDfgGoodsFatherId);
            System.out.println("kkkkkkkkkkzaaaaaa" + mapF);
            int maxSort = dgService.queryNxGoodsSonsSortByParams(mapF);
            nxDistributerGoods.setNxDgGoodsSonsSort(maxSort + 1);
            nxDistributerGoods.setNxDgIsOldestSon(0);
            nxDistributerGoods.setNxDgGoodsIsHidden(0);
            nxDistributerGoods.setNxDgWillPrice("0.1");
            nxDistributerGoods.setNxDgBuyingPrice("0.1");
            nxDistributerGoods.setNxDgBuyingPriceIsGrade(0);
            nxDistributerGoods.setNxDgBuyingPriceOne("0.1");
            nxDistributerGoods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
            nxDistributerGoods.setNxDgWillPriceOneStandard(nxDistributerGoods.getNxDgGoodsStandardname());
            nxDistributerGoods.setNxDgWillPriceOne("0.1");
            nxDistributerGoods.setNxDgWillPriceOneAboutPrice("0.1");
            nxDistributerGoods.setNxDgOutTotalWeight("0");
            dgService.save(nxDistributerGoods);
            NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsFatherId);

            fatherGoodsEntity.setNxDfgGoodsAmount(fatherGoodsEntity.getNxDfgGoodsAmount() + 1);
            nxDistributerFatherGoodsService.update(fatherGoodsEntity);

//          自动添加nxGoods
            Integer nxDfgNxGoodsId = fatherGoodsEntity.getNxDfgNxGoodsId();
            NxGoodsEntity nxGoodsEntity = new NxGoodsEntity();
            nxGoodsEntity.setNxGoodsName(englishKuohao);
            nxGoodsEntity.setNxGoodsPinyin(pinyin);
            nxGoodsEntity.setNxGoodsPy(headPinyin);
            nxGoodsEntity.setNxGoodsName(goodsName);
            nxGoodsEntity.setNxGoodsFatherId(nxDfgNxGoodsId);
            nxGoodsEntity.setNxGoodsDetail(nxGoodsDetail);
            nxGoodsEntity.setNxGoodsBrand(nxGoodsBrand);
            nxGoodsEntity.setNxGoodsStandardname(nxDgGoodsStandardname);
            nxGoodsEntity.setNxGoodsStandardWeight(nxDgGoodsStandardWeight);
            nxGoodsEntity.setNxGoodsSort(nxDistributerGoods.getNxDgGoodsSort());
            nxGoodsEntity.setNxGoodsSonsSort(nxDistributerGoods.getNxDgGoodsSonsSort());
            nxGoodsEntity.setNxGoodsGrandId(nxDistributerGoods.getNxDgNxGrandId());
            nxGoodsEntity.setNxGoodsGreatGrandId(nxDistributerGoods.getNxDgNxGreatGrandId());
            nxGoodsEntity.setNxGoodsLevel(3);
            nxGoodsEntity.setNxGoodsIsHidden(0);

            Map<String, Object> mapS = new HashMap<>();
            mapS.put("fatherId", nxDfgNxGoodsId);
            List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryNxGoodsByParams(mapS);
            if (nxGoodsEntities.size() == 0) {
                nxGoodsEntity.setNxGoodsIsOldestSon(1);
            } else {
                nxGoodsEntity.setNxGoodsIsOldestSon(0);
            }

            System.out.println("nxGoodseneyeyeyyee====" + nxGoodsEntity);
            nxGoodsService.save(nxGoodsEntity);

            nxDistributerGoods.setNxDgNxGoodsId(nxGoodsEntity.getNxGoodsId());
            dgService.update(nxDistributerGoods);
            nxDistributerGoods.setNxGoodsEntity(nxGoodsEntity);
            System.out.println("nxgoods" + nxDistributerGoods.getNxGoodsEntity());

            return R.ok().put("data", nxDistributerGoods);

        }

    }

    @RequestMapping(value = "/getFatherGoods/{id}")
    @ResponseBody
    public R getFatherGoods(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("fathersFatherId", id);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
//        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisGoodsCataLinshi(id);
        System.out.println("heeeke" + fatherGoodsEntities);
        return R.ok().put("data", fatherGoodsEntities);
    }

    @RequestMapping(value = "/getGoodsListByFatherIdNx", method = RequestMethod.POST)
    @ResponseBody
    public R getGoodsListByFatherIdNx(Integer fatherId, Integer limit, Integer page) {

        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("dgFatherId", fatherId);
        List<NxDistributerGoodsEntity> goodsEntities1 = dgService.queryDisGoodsByParams(map);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("fatherId", fatherId);
        int total = dgService.queryDisGoodsTotal(map3);
        PageUtils pageUtil = new PageUtils(goodsEntities1, total, limit, page);
        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/getGoodsSubNamesByFatherId/{fatherId}")
    @ResponseBody
    public R getGoodsSubNamesByFatherId(@PathVariable Integer fatherId) {

        Map<String, Object> map = new HashMap<>();
        map.put("fatherId", fatherId);
        System.out.println("fathehrididiidididi" + map);
        List<NxGoodsEntity> goodsEntities1 = nxGoodsService.queryNxGoodsOrderByGoodsId(map);

        List<NxGoodsEntity> newList = new ArrayList<>();

        for (NxGoodsEntity fatherGoods : goodsEntities1) {
            StringBuilder builder = new StringBuilder();

            List<NxGoodsEntity> goodsEntities = nxGoodsService.querySubNameByFatherId(fatherGoods.getNxGoodsId());

            for (int i = 0; i < goodsEntities.size(); i++) {
                String nxGoodsName = goodsEntities.get(i).getNxGoodsName();
                if (i == 0) {
                    builder.append(nxGoodsName);
                    builder.append(",");
                } else {
                    String lastName = goodsEntities.get(i - 1).getNxGoodsName();
                    if (!lastName.equals(nxGoodsName)) {
                        builder.append(nxGoodsName);
                        builder.append(",");
                    }
                }

            }


            fatherGoods.setNxGoodsSubNames(builder.toString());
            newList.add(fatherGoods);
        }


        return R.ok().put("data", newList);
    }

    private String normalizeBrand(String brand) {
        if (brand == null) {
            return null;
        }
        String trimmed = brand.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 上传Excel导入配送商商品（全部存为临时商品）
     * 优化版本：支持大批量导入，使用批量插入和分批处理
     *
     * @param file  Excel文件
     * @param disId 配送商ID
     * @return 处理结果
     */
    @RequestMapping(value = "/uploadGoodsExcel", method = RequestMethod.POST)
    @ResponseBody
    public R uploadGoodsExcel(@RequestParam("file") MultipartFile file,
                              @RequestParam("disId") Integer disId) {
        System.out.println("[uploadGoodsExcel] 请求导入配送商商品 disId=" + disId
                + ", fileName=" + (file == null ? "null" : file.getOriginalFilename()));

        if (disId == null) {
            return R.error(-1, "参数错误: 缺少 disId");
        }
        if (file == null || file.isEmpty()) {
            return R.error(-1, "文件不能为空");
        }

        DataFormatter formatter = new DataFormatter();
        List<Map<String, Object>> failItems = new ArrayList<>();
        int total = 0;
        int success = 0;

        // 批量处理配置
        final int BATCH_SIZE = 100; // 每批处理100条

        // 缓存父级分类信息（避免重复查询）
        NxDistributerFatherGoodsEntity cachedFatherGoodsEntity = null;
        int currentSort = 0; // 当前排序号计数器

        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(file.getInputStream());

            // 预先查询并缓存父级分类信息
            Map<String, Object> map = new HashMap<>();
            map.put("disId", disId);
            map.put("nxGoodsId", -1);
            map.put("goodsLevel", 2);
            List<NxDistributerFatherGoodsEntity> fatherGoodsEntities =
                    nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);

            if (fatherGoodsEntities == null || fatherGoodsEntities.isEmpty()) {
                return R.error(-1, "未找到临时父级分类");
            }

            cachedFatherGoodsEntity = fatherGoodsEntities.get(0);

            // 获取初始排序号
            Map<String, Object> sortMap = new HashMap<>();
            sortMap.put("dgFatherId", cachedFatherGoodsEntity.getNxDistributerFatherGoodsId());
            currentSort = dgService.queryNxGoodsSonsSortByParams(sortMap);

            // 批量保存的商品列表
            List<NxDistributerGoodsEntity> batchGoodsList = new ArrayList<>();

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                if (sheet == null) {
                    continue;
                }
                int lastRowNum = sheet.getLastRowNum();
                // 从第3行开始读取数据（第1行是提示，第2行是表头，第3行可能是示例）
                for (int rowIndex = 2; rowIndex <= lastRowNum; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        continue;
                    }

                    String goodsName = getCellString(row, 0, formatter);
                    String standard = getCellString(row, 1, formatter);
                    // 如果商品名称和规格都为空，跳过这一行
                    if (isBlank(goodsName) && isBlank(standard)) {
                        continue;
                    }

                    total++;
                    Map<String, Object> resultItem = new HashMap<>();
                    resultItem.put("row", rowIndex + 1);
                    resultItem.put("goodsName", goodsName);
                    resultItem.put("standard", standard);

                    try {
                        // 验证必填项
                        if (isBlank(goodsName)) {
                            throw new IllegalArgumentException("缺少商品名称");
                        }
                        if (isBlank(standard)) {
                            throw new IllegalArgumentException("缺少商品规格");
                        }

                        // 读取可选项
                        String standardWeight = getCellString(row, 2, formatter);
                        String brand = getCellString(row, 3, formatter);
                        String cartonUnit = getCellString(row, 4, formatter);
                        String itemsPerCartonStr = getCellString(row, 5, formatter);

                        // 创建临时商品
                        NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();

                        // 使用缓存的父级分类信息
                        goods.setNxDgDfgGoodsFatherId(cachedFatherGoodsEntity.getNxDistributerFatherGoodsId());
                        goods.setNxDgDfgGoodsGrandId(cachedFatherGoodsEntity.getNxDfgFathersFatherId());

                        // 设置基本信息
                        goods.setNxDgDistributerId(disId);
                        goods.setNxDgGoodsName(goodsName.trim());
                        goods.setNxDgGoodsStandardname(standard.trim());

                        // 设置可选项
                        if (!isBlank(standardWeight)) {
                            goods.setNxDgGoodsStandardWeight(standardWeight.trim());
                        }
                        if (!isBlank(brand)) {
                            goods.setNxDgGoodsBrand(brand.trim());
                        }
                        if (!isBlank(cartonUnit)) {
                            goods.setNxDgCartonUnit(cartonUnit.trim());
                        }
                        if (!isBlank(itemsPerCartonStr)) {
                            try {
                                Integer itemsPerCarton = Integer.parseInt(itemsPerCartonStr.trim());
                                goods.setNxDgItemsPerCarton(itemsPerCarton);
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("外包装数量格式错误，应为整数");
                            }
                        }

                        // 生成拼音和简拼
                        String pinyin = hanziToPinyin(goodsName.trim());
                        String headPinyin = getHeadStringByString(goodsName.trim(), false, null);
                        goods.setNxDgGoodsPinyin(pinyin);
                        goods.setNxDgGoodsPy(headPinyin);

                        // 设置默认值
                        goods.setNxDgPurchaseAuto(-1);
                        goods.setNxDgBuyingPriceIsGrade(0);
                        goods.setNxDgBuyingPrice("0.1");
                        goods.setNxDgWillPrice("0.1");
                        goods.setNxDgBuyingPriceOne("0.1");
                        goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
                        goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
                        goods.setNxDgWillPriceOne("0.1");
                        goods.setNxDgWillPriceOneAboutPrice("0.1");
                        goods.setNxDgGoodsIsHidden(0);
                        goods.setNxDgGoodsStatus(0);
                        goods.setNxDgPullOff(0);
                        goods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
                        goods.setNxDgOutTotalWeight("0");
                        // 使用递增的排序号（避免每次查询数据库）
                        currentSort++;
                        goods.setNxDgGoodsSonsSort(currentSort);
                        goods.setNxDgIsOldestSon(0);

                        // 添加到批量列表
                        batchGoodsList.add(goods);

                        // 当达到批量大小时，执行批量插入
                        if (batchGoodsList.size() >= BATCH_SIZE) {
                            batchInsertGoods(batchGoodsList);
                            success += batchGoodsList.size();
                            batchGoodsList.clear();
                            System.out.println("[uploadGoodsExcel] 已批量插入 " + BATCH_SIZE + " 条商品，当前成功数: " + success);
                        }

                    } catch (Exception e) {
                        resultItem.put("error", e.getMessage());
                        failItems.add(resultItem);
                        System.err.println("[uploadGoodsExcel] 第" + (rowIndex + 1) + "行导入失败: " + e.getMessage());
                    }
                }
            }

            // 处理剩余的批量数据
            if (!batchGoodsList.isEmpty()) {
                batchInsertGoods(batchGoodsList);
                success += batchGoodsList.size();
                System.out.println("[uploadGoodsExcel] 最后批量插入 " + batchGoodsList.size() + " 条商品");
            }

            // 最后统一更新父级分类的商品数量（只更新一次，而不是每条都更新）
            if (success > 0) {
                Integer goodsAmount = cachedFatherGoodsEntity.getNxDfgGoodsAmount();
                if (goodsAmount == null) {
                    goodsAmount = 0;
                }
                cachedFatherGoodsEntity.setNxDfgGoodsAmount(goodsAmount + success);
                nxDistributerFatherGoodsService.update(cachedFatherGoodsEntity);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("success", success);
            result.put("fail", failItems.size());
            result.put("failItems", failItems);

            System.out.println("[uploadGoodsExcel] 导入完成: 总计=" + total + ", 成功=" + success + ", 失败=" + failItems.size());

            return R.ok().put("data", result);

        } catch (Exception e) {
            System.err.println("[uploadGoodsExcel] 处理异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("导入失败: " + e.getMessage());
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    System.err.println("[uploadGoodsExcel] 关闭工作簿异常: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 批量插入商品（逐条插入，但减少数据库连接开销）
     * 注意：MyBatis 默认不支持真正的批量插入，这里使用循环插入但优化了事务管理
     */
    private void batchInsertGoods(List<NxDistributerGoodsEntity> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }

        for (NxDistributerGoodsEntity goods : goodsList) {
            try {
                dgService.save(goods);
            } catch (Exception e) {
                System.err.println("[batchInsertGoods] 插入商品失败: " + goods.getNxDgGoodsName() + ", 错误: " + e.getMessage());
                throw e; // 抛出异常，让外层处理
            }
        }
    }

    /**
     * 辅助方法：读取Excel单元格字符串值
     */
    private String getCellString(Row row, int cellIndex, DataFormatter formatter) {
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell);
        return value == null ? null : value.trim();
    }

    /**
     * 辅助方法：判断字符串是否为空
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 批量更新曾祖父商品下的所有商品的 nxDgPurchaseAuto 字段
     * <p>
     * 请求体格式：
     * {
     * "greatGrandIds": [1, 2, 3],
     * "type": -1  // -1 表示出库商品，1 表示采购商品
     * }
     *
     * @param greatGrandIds（曾祖父ID列表）和 type（类型）
     * @return 更新结果
     */
    @RequestMapping(value = "/updatePurchaseAutoByGreatGrandIds", method = RequestMethod.POST)
    @ResponseBody
    public R updatePurchaseAutoByGreatGrandIds(@RequestParam String greatGrandIds, @RequestParam Integer type) {
        System.out.println("[updatePurchaseAutoByGreatGrandIds] 请求批量更新 purchaseAuto, greatGrandIds=" + greatGrandIds + ", type=" + type);

        try {
            // 验证参数
            if (greatGrandIds == null || greatGrandIds.trim().isEmpty()) {
                return R.error(-1, "参数错误: 缺少 greatGrandIds");
            }

            if (type == null) {
                return R.error(-1, "参数错误: 缺少 type");
            }

            // 解析逗号分隔的 ID 字符串
            List<Integer> greatGrandIdsList = new ArrayList<>();
            String[] idsArray = greatGrandIds.split(",");
            for (String idStr : idsArray) {
                try {
                    Integer id = Integer.parseInt(idStr.trim());
                    greatGrandIdsList.add(id);
                } catch (NumberFormatException e) {
                    return R.error(-1, "参数错误: greatGrandIds 格式不正确，包含非数字: " + idStr);
                }
            }

            if (greatGrandIdsList.isEmpty()) {
                return R.error(-1, "参数错误: greatGrandIds 不能为空");
            }

            if (type != -1 && type != 1) {
                return R.error(-1, "参数错误: type 必须为 -1（出库商品）或 1（采购商品）");
            }

            // 执行更新
            Map<String, Object> map = new HashMap<>();
            map.put("greatGrandIds", greatGrandIdsList);
            map.put("type", type);

            int updatedCount = dgService.updatePurchaseAutoByGreatGrandIds(map);

            System.out.println("[updatePurchaseAutoByGreatGrandIds] 更新完成，共更新 " + updatedCount + " 条记录");

            Map<String, Object> result = new HashMap<>();
            result.put("updatedCount", updatedCount);
            result.put("type", type);
            result.put("typeName", type == -1 ? "出库商品" : "采购商品");
            result.put("greatGrandIds", greatGrandIdsList);

            return R.ok().put("data", result);

        } catch (Exception e) {
            System.err.println("[updatePurchaseAutoByGreatGrandIds] 更新异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("更新失败: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/downLoadByGreatGrandIds", method = RequestMethod.POST)
    @ResponseBody
    public R downLoadByGreatGrandIds(@RequestParam String greatGrandIds, @RequestParam Integer disId) {
        System.out.println("[updatePurchaseAutoByGreatGrandIds] 请求批量下载 , greatGrandIds=" + greatGrandIds + ", disId=" + disId);

        try {

            // 解析逗号分隔的 ID 字符串
            List<Integer> greatGrandIdsList = new ArrayList<>();
            String[] idsArray = greatGrandIds.split(",");
            for (String idStr : idsArray) {
                try {
                    Integer id = Integer.parseInt(idStr.trim());
                    greatGrandIdsList.add(id);
                } catch (NumberFormatException e) {
                    return R.error(-1, "参数错误: greatGrandIds 格式不正确，包含非数字: " + idStr);
                }
            }

            // 异步下载商品，不阻塞注册接口返回
            asyncGoodsDownloadService.downloadGoodsByGreatGrandIdsAsync(disId, greatGrandIdsList);

            return R.ok();

        } catch (Exception e) {
            System.err.println("[updatePurchaseAutoByGreatGrandIds] 更新异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 下载配送商商品导入Excel模板
     *
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    @RequestMapping(value = "/downloadGoodsTemplate", method = RequestMethod.GET)
    public void downloadGoodsTemplate(HttpServletResponse response) throws IOException {
        System.out.println("[downloadGoodsTemplate] 请求下载配送商商品导入模板");

        HSSFWorkbook workbook = new HSSFWorkbook();
        try {
            HSSFSheet sheet = workbook.createSheet("配送商商品导入模板");
            sheet.setDefaultColumnWidth(20);

            HSSFCellStyle headerStyle = workbook.createCellStyle();
            HSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            int rowIndex = 0;
            // 提示行
            HSSFRow tipRow = sheet.createRow(rowIndex++);
            tipRow.createCell(0).setCellValue("提示：商品名称和商品规格为必填项，其他字段为可选项。");

            // 表头
            HSSFRow headerRow = sheet.createRow(rowIndex++);
            String[] headers = new String[]{
                    "商品名称(必填)",
                    "商品规格(必填)",
                    "规格重量",
                    "品牌",
                    "外包装名称",
                    "外包装数量"
            };
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 示例行（帮助用户理解格式）
            HSSFRow exampleRow = sheet.createRow(rowIndex++);
            exampleRow.createCell(0).setCellValue("示例：菌伯乐鹿茸菌");
            exampleRow.createCell(1).setCellValue("桶");
            exampleRow.createCell(2).setCellValue("5kg");
            exampleRow.createCell(3).setCellValue("菌伯乐");
            exampleRow.createCell(4).setCellValue("箱");
            exampleRow.createCell(5).setCellValue("24");

            String fileName = "配送商商品导入模板.xls";
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            workbook.write(response.getOutputStream());
            response.flushBuffer();
            System.out.println("[downloadGoodsTemplate] 模板生成完成，已输出响应");
        } catch (IOException e) {
            System.err.println("[downloadGoodsTemplate] 生成模板异常: " + e.getMessage());
            throw new RuntimeException("生成模板失败: " + e.getMessage(), e);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                System.err.println("[downloadGoodsTemplate] 关闭工作簿异常: " + e.getMessage());
            }
        }
    }

    /**
     * 为分销商商品添加溯源报告
     * 支持上传图片或PDF格式的溯源报告文件
     * 创建新的溯源报告，并将报告ID更新到商品表中
     */
    @RequestMapping(value = "/addTraceReportToGoods", method = RequestMethod.POST)
    @ResponseBody
    public R addTraceReportToGoods(
            @RequestParam("nxDistributerGoodsId") String nxDistributerGoodsId,
            @RequestParam(value = "traceReportFile", required = false) MultipartFile traceReportFile,
            // 溯源报告相关参数
            @RequestParam(value = "nxTrSupplierId", required = false) String nxTrSupplierId,
            @RequestParam(value = "nxTrSupplierName", required = false) String nxTrSupplierName,
            @RequestParam(value = "nxTrSupplierContact", required = false) String nxTrSupplierContact,
            @RequestParam(value = "nxTrPurchaseDate", required = false) String nxTrPurchaseDate,
            @RequestParam(value = "nxTrStockInDate", required = false) String nxTrStockInDate,
            @RequestParam(value = "nxTrValidStartDate", required = false) String nxTrValidStartDate,
            @RequestParam(value = "nxTrValidEndDate", required = false) String nxTrValidEndDate,
            @RequestParam(value = "nxTrRemark", required = false) String nxTrRemark,
            @RequestParam(value = "createUserId", required = false) String createUserId,
            HttpSession session) {
        
        System.out.println("[addTraceReportToGoods] 开始为商品添加溯源报告，商品ID: " + nxDistributerGoodsId);
        
        try {
            // 验证商品ID
            Integer goodsId = Integer.parseInt(nxDistributerGoodsId);
            NxDistributerGoodsEntity goodsEntity = dgService.queryObject(goodsId);
            if (goodsEntity == null) {
                return R.error("商品不存在，ID: " + goodsId);
            }
            
            // 验证文件是否上传
            if (traceReportFile == null || traceReportFile.isEmpty()) {
                return R.error("请上传溯源报告文件");
            }
            
            // 处理溯源报告文件上传
            System.out.println("[addTraceReportToGoods] 开始上传溯源报告文件");
            System.out.println("[addTraceReportToGoods] 文件名: " + traceReportFile.getOriginalFilename());
            System.out.println("[addTraceReportToGoods] 文件大小: " + traceReportFile.getSize() + " bytes");
            System.out.println("[addTraceReportToGoods] Content-Type: " + traceReportFile.getContentType());

            // 获取文件扩展名
            String originalFilename = traceReportFile.getOriginalFilename();
            String fileExtension = "";
            String reportType = "";
            
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            }
            
            // 判断报告类型（image 或 pdf）
            if ("jpg".equals(fileExtension) || "jpeg".equals(fileExtension) || 
                "png".equals(fileExtension) || "gif".equals(fileExtension) || 
                "bmp".equals(fileExtension)) {
                reportType = "image";
            } else if ("pdf".equals(fileExtension)) {
                reportType = "pdf";
            } else {
                return R.error("不支持的文件格式，仅支持图片（jpg/png/gif/bmp）和PDF文件");
            }

            // 上传文件到服务器
            String uploadDir = "traceReports";
            String absolutePath = UploadFile.upload(session, uploadDir, traceReportFile);
            System.out.println("[addTraceReportToGoods] 文件上传到: " + absolutePath);

            // 获取文件路径（相对路径）
            String filePath = uploadDir + "/" + originalFilename;

            // 创建溯源报告实体
            NxTraceReportEntity traceReportEntity = new NxTraceReportEntity();
            
            // 设置基本信息
            if (nxTrSupplierId != null && !nxTrSupplierId.trim().isEmpty()) {
                traceReportEntity.setNxTrSupplierId(Integer.parseInt(nxTrSupplierId));
            }
            if (nxTrSupplierName != null) {
                traceReportEntity.setNxTrSupplierName(nxTrSupplierName);
            }
            if (nxTrSupplierContact != null) {
                traceReportEntity.setNxTrSupplierContact(nxTrSupplierContact);
            }
            
            // 设置日期信息
            if (nxTrPurchaseDate != null && !nxTrPurchaseDate.trim().isEmpty()) {
                traceReportEntity.setNxTrPurchaseDate(nxTrPurchaseDate);
            } else {
                traceReportEntity.setNxTrPurchaseDate(formatWhatDay(0)); // 默认今天
            }
            if (nxTrStockInDate != null && !nxTrStockInDate.trim().isEmpty()) {
                traceReportEntity.setNxTrStockInDate(nxTrStockInDate);
            } else {
                traceReportEntity.setNxTrStockInDate(formatWhatDay(0)); // 默认今天
            }
            if (nxTrValidStartDate != null && !nxTrValidStartDate.trim().isEmpty()) {
                traceReportEntity.setNxTrValidStartDate(nxTrValidStartDate);
            } else {
                traceReportEntity.setNxTrValidStartDate(formatWhatDay(0)); // 默认今天开始
            }
            if (nxTrValidEndDate != null && !nxTrValidEndDate.trim().isEmpty()) {
                traceReportEntity.setNxTrValidEndDate(nxTrValidEndDate);
            }
            
            // 设置文件信息
            traceReportEntity.setNxTrReportType(reportType);
            traceReportEntity.setNxTrFilePath(filePath);
            traceReportEntity.setNxTrFileType(fileExtension);
            
            // 设置关联信息
            traceReportEntity.setNxTrDistributerId(goodsEntity.getNxDgDistributerId());
            if (createUserId != null && !createUserId.trim().isEmpty()) {
                traceReportEntity.setNxTrCreateUserId(Integer.parseInt(createUserId));
            }
            traceReportEntity.setNxTrCreateTime(formatWhatYearDayTime(0));
            traceReportEntity.setNxTrUpdateTime(formatWhatYearDayTime(0));
            
            if (nxTrRemark != null) {
                traceReportEntity.setNxTrRemark(nxTrRemark);
            }

            // 保存溯源报告
            nxTraceReportService.save(traceReportEntity);
            Integer traceReportId = traceReportEntity.getNxTraceReportId();
            System.out.println("[addTraceReportToGoods] 溯源报告保存成功，ID: " + traceReportId);

            // 更新商品，关联溯源报告ID
            goodsEntity.setNxDgTraceReportId(traceReportId);
            ;
            dgService.update(goodsEntity);
            System.out.println("[addTraceReportToGoods] 商品已关联溯源报告ID: " + traceReportId);

            return R.ok().put("traceReportId", traceReportId).put("message", "溯源报告添加成功");

        } catch (NumberFormatException e) {
            System.err.println("[addTraceReportToGoods] 参数格式错误: " + e.getMessage());
            return R.error("参数格式错误: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[addTraceReportToGoods] 处理失败: " + e.getMessage());
            e.printStackTrace();
            return R.error("添加溯源报告失败: " + e.getMessage());
        }
    }

}


//sisy


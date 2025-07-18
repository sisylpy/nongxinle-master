package com.nongxinle.controller;

/**
 * @author lpy
 * @date 07-27 17:38
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.GbTypeUtils.getGbPurchaseGoodsStatusProcurement;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPurchaseGoodsWithBatch;
import static com.nongxinle.utils.PinYin4jUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.getXiegang;


@RestController
@RequestMapping("api/nxdistributergoods")
public class NxDistributerGoodsController {
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
    private NxDistributerStandardService nxDistributerStandardService;
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

    private static final String KEY = "C5HBZ-KEIW2-JXXUJ-COLGS-FQO47-WWFAK";


    @RequestMapping(value = "/updateFatherId1")
    @ResponseBody
    public R updateFatherId1() {

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryWenti();
        System.out.println("deiisissisissiis" + departmentDisGoodsEntities.size());
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                if (nxDistributerGoodsEntity != null) {

                    System.out.println("depgoods111" + nxDistributerGoodsEntity.getNxDgGoodsName());
                    departmentDisGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
                    nxDepartmentDisGoodsService.update(departmentDisGoodsEntity);

                }
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
        distributerGoodsEntity.setNxDgPurchaseAuto(1);
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
                purchaseGoodsEntity.setGbDpgPurchaseNxDistributerId(nxDisId);
                gbDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

                List<GbDepartmentOrdersEntity> gbDepartmentOrdersEntities = purchaseGoodsEntity.getGbDepartmentOrdersEntities();
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
            goods.setNxDgBuyingPriceOne("0.1");
            goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
            goods.setNxDgWillPriceOneWeight(goods.getNxDgGoodsStandardname());
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
                gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDayTime(0));
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
//                    gbPurchaseGoodsEntity.setGbDpgBuyPrice(willPrice.toString());
//                    gbPurchaseGoodsEntity.setGbDpgStatus(getGbPurchaseGoodsStatusProcurement());
//                    gbPurchaseGoodsEntity.setGbDpgBuySubtotal(subtotal.toString());
//                    gbPurchaseGoodsEntity.setGbDpgBuyQuantity(gbPurchaseGoodsEntity.getGbDpgQuantity());
//                    gbPurchaseGoodsEntity.setGbDpgPayType(1);
//                    gbPurchaseGoodsEntity.setGbDpgTime(formatWhatTime(0));
//                    gbPurchaseGoodsEntity.setGbDpgPurchaseDate(formatWhatDay(0));
//                    gbPurchaseGoodsEntity.setGbDpgPurchaseMonth(formatWhatMonth(0));
//                    gbPurchaseGoodsEntity.setGbDpgPurchaseYear(formatWhatYear(0));
//                    gbPurchaseGoodsEntity.setGbDpgPurchaseWeek(getWeek(0));
//                    gbPurchaseGoodsEntity.setGbDpgPurchaseFullTime(formatWhatYearDayTime(0));
//
//                    gbDistributerPurchaseGoodsService.update(gbPurchaseGoodsEntity);

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
                    goods.setNxDgWillPriceOneWeight(goods.getNxDgGoodsStandardname());
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


    @RequestMapping(value = "/addAppGoods", method = RequestMethod.POST)
    @ResponseBody
    public R addAppGoods(String ids, Integer nxDisId) {
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
                        setGbDistribuerGoodsToNxDis(Integer.valueOf(id), distributerGoodsEntity);

                    } else {
                        //下载 nxGoods
                        NxDistributerGoodsEntity distributerGoodsEntity1 = downLoadNxGoodsForGbDisGoods(Integer.valueOf(id), nxGoodsEntity, nxDisId);

                        ////设置 gbDisGoods给 nxDis
                        setGbDistribuerGoodsToNxDis(Integer.valueOf(id), distributerGoodsEntity1);
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
                    goods.setNxDgBuyingPriceOne("0.1");
                    goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
                    goods.setNxDgWillPriceOneWeight(goods.getNxDgGoodsStandardname());
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
                    setGbDistribuerGoodsToNxDis(goodsEntity.getGbDistributerGoodsId(), goods);

                }

            }
        }

        return R.ok();
    }

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
        List<GbDistributerPurchaseGoodsEntity> purchaseGoodsEntities = gbDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
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
                gbDepartmentOrders.setGbDoArriveOnlyDate(formatWhatDayTime(0));
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
            resultPurGoods.setNxDpgPurchaseType(1);
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
            pathBuilder.append("&buyerUserId=").append(batchEntity.getNxDpbBuyUserId());
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
        cgnGoods.setNxDgPurchaseAuto(1);

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
            linshiGoodsEntity.setNxDgPurchaseAuto(1);
            linshiGoodsEntity.setNxDgWillPrice("0.1");
            linshiGoodsEntity.setNxDgBuyingPrice("0.1");
            linshiGoodsEntity.setNxDgGoodsIsHidden(0);
            linshiGoodsEntity.setNxDgNxFatherImg("goodsImage/logo.jpg");
            linshiGoodsEntity.setNxDgNxFatherId(-1);
            linshiGoodsEntity.setNxDgNxGrandId(-1);
            linshiGoodsEntity.setNxDgNxGreatGrandId(-1);
            linshiGoodsEntity.setNxDgIsOldestSon(1);
            linshiGoodsEntity.setNxDgGoodsSonsSort(1);
            linshiGoodsEntity.setNxDgNxGoodsId(-1);
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
    public R disGetUpdatePriceGoods(Integer disId, String type) {
        if (type.equals("sell")) {
            Map<String, Object> mapPrice = new HashMap<>();
            mapPrice.put("disId", disId);
            mapPrice.put("willPrice", 0.1);
            List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisGoodsByParams(mapPrice);
            return R.ok().put("data", distributerGoodsEntities);


        } else if (type.equals("buyingPrice")) {
            Map<String, Object> mapBuyPrice = new HashMap<>();
            mapBuyPrice.put("disId", disId);
            mapBuyPrice.put("buyPrice", 0.1);
            List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisGoodsByParams(mapBuyPrice);
            return R.ok().put("data", distributerGoodsEntities);
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
        goods.setNxDgPurchaseAuto(1);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        goods.setNxDgGoodsPinyin(pinyin);
        goods.setNxDgGoodsPy(headPinyin);
        goods.setNxDgBuyingPrice("0.1");
        goods.setNxDgWillPrice("0.1");
        goods.setNxDgBuyingPriceOne("0.1");
        goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        goods.setNxDgWillPriceOneWeight(goods.getNxDgGoodsStandardname());
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
        goods.setNxDgPurchaseAuto(1);
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
        goods.setNxDgWillPriceOneWeight(goods.getNxDgGoodsStandardname());
        goods.setNxDgWillPriceOne("0.1");
        goods.setNxDgWillPriceOneAboutPrice("0.1");
        goods.setNxDgGoodsIsHidden(0);
        goods.setNxDgGoodsStandardname(standard);
        goods.setNxDgGoodsDetail(detail);
        goods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
        System.out.println("savegoogog" + goods);
        goods.setNxDgOutTotalWeight("0");
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
            linshiGoods.setNxDgNxGoodsId(-1);
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
    @RequestMapping(value = "/disSaveLinshiToNxGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disSaveLinshiToNxGoods(Integer lsGoodsId, Integer nxGoodsId, Integer disId) {

        System.out.println("sadisSaveLinshiToNxGoodsdisSaveLinshiToNxGoods");
        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxGoodsId);
        NxDistributerGoodsEntity linshiGoods = dgService.queryObject(lsGoodsId);

        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();

        Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();
        Integer nxDgNxFatherId = nxDistributerGoodsEntity.getNxDgNxFatherId();

        Map<String, Object> linshiMap = new HashMap<>();
        linshiMap.put("disGoodsId", lsGoodsId);
        System.out.println("linsihsimappsa "+ linshiMap );
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(linshiMap);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsId(nxGoodsId);
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                depOrdersService.update(ordersEntity);
            }
        }

        Map<String, Object> linshiMapH = new HashMap<>();
        linshiMapH.put("disGoodsId", lsGoodsId);
        System.out.println("linsihsimappslinshiMapHlinshiMapHa "+ linshiMapH );
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
//        List<NxDepartmentOrdersHistoryEntity> linshiMapHHS = nxDepartmentOrdersHistoryService.queryDepHistoryOrdersByParams(linshiMapH);
//        if (linshiMapHHS.size() > 0) {
//            for (NxDepartmentOrdersHistoryEntity ordersEntity : linshiMapHHS) {
//                ordersEntity.setNxDohDisGoodsId(nxGoodsId);
//                nxDepartmentOrdersHistoryService.update(ordersEntity);
//            }
//        }

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(linshiMap);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity linshiDepGoodsEntity : departmentDisGoodsEntities) {
                Integer nxDdgDepartmentId = linshiDepGoodsEntity.getNxDdgDepartmentId();
                Map<String, Object> map = new HashMap<>();
                map.put("depId", nxDdgDepartmentId);
                map.put("disGoodsId", nxGoodsId);
                System.out.println("depdisgoenen" + map);
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
                if (nxDepartmentDisGoodsEntity != null) {
                    System.out.println("hasdepgoods" + nxDepartmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsStandardname(linshiGoods.getNxDgGoodsStandardname());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderStandard(linshiGoods.getNxDgGoodsStandardname());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderPrice(linshiDepGoodsEntity.getNxDdgOrderPrice());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderGoodsName(linshiGoods.getNxDgGoodsName());
                    nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);
                    nxDepartmentDisGoodsService.delete(linshiDepGoodsEntity.getNxDepartmentDisGoodsId());

                } else {
                    linshiDepGoodsEntity.setNxDdgDisGoodsId(nxGoodsId);
                    linshiDepGoodsEntity.setNxDdgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    linshiDepGoodsEntity.setNxDdgDisGoodsGrandId(nxDgDfgGoodsGrandId);

                    NxDistributerFatherGoodsEntity grandEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsGrandId);
                    Integer nxDfgGreatId = grandEntity.getNxDfgFathersFatherId();
                    linshiDepGoodsEntity.setNxDdgDisGoodsGreatId(nxDfgGreatId);

                    linshiDepGoodsEntity.setNxDdgDepGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
                    linshiDepGoodsEntity.setNxDdgOrderGoodsName(linshiGoods.getNxDgGoodsName());
                    linshiDepGoodsEntity.setNxDdgOrderStandard(linshiGoods.getNxDgGoodsStandardname());
                    linshiDepGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
                    nxDepartmentDisGoodsService.update(linshiDepGoodsEntity);
                }
            }
        }

        Map<String, Object> mapNx = new HashMap<>();
        mapNx.put("disGoodsId", nxGoodsId);
        int same = 0;
        List<NxDistributerStandardEntity> nxStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapNx);
        if (nxStandardEntities.size() > 0) {
            for (NxDistributerStandardEntity nxDistributerStandardEntity : nxStandardEntities) {
                if (linshiGoods.getNxDgGoodsStandardname().equals(nxDistributerStandardEntity.getNxDsStandardName())) {
                    same = same + 1;
                }
            }
            if (nxDistributerGoodsEntity.getNxDgGoodsStandardname().equals(linshiGoods.getNxDgGoodsStandardname())) {
                same = same + 1;
            }
        }
        String nxDgGoodsStandardname = nxDistributerGoodsEntity.getNxDgGoodsStandardname();
        String nxDgGoodsStandardname1 = linshiGoods.getNxDgGoodsStandardname();
        if (nxDgGoodsStandardname.equals(nxDgGoodsStandardname1)) {
            same = same + 1;
        }
        if (same == 0) {
            NxDistributerStandardEntity nxDistributerStandardEntity = new NxDistributerStandardEntity();
            nxDistributerStandardEntity.setNxDsStandardName(linshiGoods.getNxDgGoodsStandardname());
            nxDistributerStandardEntity.setNxDsDisGoodsId(nxGoodsId);
            nxDistributerStandardEntity.setNxDsStandardSort(nxStandardEntities.size());
            nxDistributerStandardService.save(nxDistributerStandardEntity);
        }

        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(linshiMap);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsId(nxDgNxGoodsId);
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }


        List<NxDistributerGoodsShelfGoodsEntity> entities = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(linshiMap);
        if (entities.size() > 0) {
            for (NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity : entities) {
                nxDistributerGoodsShelfGoodsService.delete(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
            }
        }


        Map<String, Object> mapGb = new HashMap<>();
        mapGb.put("nxDisGoodsId", lsGoodsId);
        System.out.println("gbdidiididid" + mapGb);
        List<GbDistributerGoodsEntity> goodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapGb);
        if (goodsEntities.size() > 0) {
            for (GbDistributerGoodsEntity distributerGoodsEntity : goodsEntities) {
                distributerGoodsEntity.setGbDgNxDistributerGoodsId(nxGoodsId);
                gbDistributerGoodsService.update(distributerGoodsEntity);
            }
        }


        List<GbDepartmentOrdersEntity> gbOrdersEntities = gbDepartmentOrdersService.queryDisOrdersByParams(mapGb);
        if (gbOrdersEntities.size() > 0) {
            for (GbDepartmentOrdersEntity ordersEntity : gbOrdersEntities) {
                ordersEntity.setGbDoNxDistributerGoodsId(nxGoodsId);
                gbDepartmentOrdersService.update(ordersEntity);
            }
        }


        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("nxGoodsId", lsGoodsId);
        List<GbDepartmentDisGoodsEntity> gbDepartmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapDep);
        if (gbDepartmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity disGoodsEntity : gbDepartmentDisGoodsEntities) {
                gbDepartmentDisGoodsService.delete(disGoodsEntity.getGbDepartmentDisGoodsId());
            }
        }


        dgService.delete(lsGoodsId);

        return R.ok();
    }

    //todo test
    @RequestMapping(value = "/disSaveLinshiToAlias", method = RequestMethod.POST)
    @ResponseBody
    public R disSaveLinshiToAlias(Integer lsGoodsId, Integer nxGoodsId, Integer disId) {

        NxDistributerAliasEntity aliasEntity = new NxDistributerAliasEntity();
        NxDistributerGoodsEntity linshiGoods = dgService.queryObject(lsGoodsId);
        aliasEntity.setNxDaAliasName(linshiGoods.getNxDgGoodsName());
        String pinyin = hanziToPinyin(linshiGoods.getNxDgGoodsName());
        String headPinyin = getHeadStringByString(linshiGoods.getNxDgGoodsName(), false, null);
        aliasEntity.setNxDaAliasPy(headPinyin);
        aliasEntity.setNxDaAliasPinyin(pinyin);
        aliasEntity.setNxDaDisGoodsId(nxGoodsId);
        disAliasService.save(aliasEntity);

        NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(nxGoodsId);
        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
        Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();
        Integer nxDgNxFatherId = nxDistributerGoodsEntity.getNxDgNxFatherId();

        Map<String, Object> linshiMap = new HashMap<>();
        linshiMap.put("disGoodsId", lsGoodsId);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(linshiMap);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsId(nxGoodsId);
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

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(linshiMap);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity linshiDepGoodsEntity : departmentDisGoodsEntities) {
                Integer nxDdgDepartmentId = linshiDepGoodsEntity.getNxDdgDepartmentId();
                Map<String, Object> map = new HashMap<>();
                map.put("depId", nxDdgDepartmentId);
                map.put("disGoodsId", nxGoodsId);
                System.out.println("depdisgoenen" + map);
                NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
                if (nxDepartmentDisGoodsEntity != null) {

                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsName(nxDistributerGoodsEntity.getNxDgGoodsName());
                    nxDepartmentDisGoodsEntity.setNxDdgDepGoodsStandardname(linshiGoods.getNxDgGoodsStandardname());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderStandard(linshiGoods.getNxDgGoodsStandardname());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderPrice(linshiDepGoodsEntity.getNxDdgOrderPrice());
                    nxDepartmentDisGoodsEntity.setNxDdgOrderGoodsName(linshiGoods.getNxDgGoodsName());


                    nxDepartmentDisGoodsService.update(nxDepartmentDisGoodsEntity);
                    nxDepartmentDisGoodsService.delete(linshiDepGoodsEntity.getNxDepartmentDisGoodsId());

                } else {
                    linshiDepGoodsEntity.setNxDdgDisGoodsId(nxGoodsId);
                    linshiDepGoodsEntity.setNxDdgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                    linshiDepGoodsEntity.setNxDdgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                    NxDistributerFatherGoodsEntity grandEntity = nxDistributerFatherGoodsService.queryObject(nxDgDfgGoodsGrandId);
                    Integer nxDfgGreatId = grandEntity.getNxDfgFathersFatherId();
                    linshiDepGoodsEntity.setNxDdgDisGoodsGreatId(nxDfgGreatId);
                    nxDepartmentDisGoodsService.update(linshiDepGoodsEntity);
                }
            }
        }


        Map<String, Object> mapNx = new HashMap<>();
        mapNx.put("disGoodsId", nxGoodsId);
        int same = 0;
        List<NxDistributerStandardEntity> nxStandardEntities = nxDistributerStandardService.queryDisStandardByParams(mapNx);
        if (nxStandardEntities.size() > 0) {
            for (NxDistributerStandardEntity nxDistributerStandardEntity : nxStandardEntities) {
                if (linshiGoods.getNxDgGoodsStandardname().equals(nxDistributerStandardEntity.getNxDsStandardName())) {
                    same = same + 1;
                }
            }
            if (nxDistributerGoodsEntity.getNxDgGoodsStandardname().equals(linshiGoods.getNxDgGoodsStandardname())) {
                same = same + 1;
            }
        }

        String nxDgGoodsStandardname = nxDistributerGoodsEntity.getNxDgGoodsStandardname();
        String nxDgGoodsStandardname1 = linshiGoods.getNxDgGoodsStandardname();
        if (nxDgGoodsStandardname.equals(nxDgGoodsStandardname1)) {
            same = same + 1;
        }
        if (same == 0) {
            NxDistributerStandardEntity nxDistributerStandardEntity = new NxDistributerStandardEntity();
            nxDistributerStandardEntity.setNxDsStandardName(linshiGoods.getNxDgGoodsStandardname());
            nxDistributerStandardEntity.setNxDsDisGoodsId(nxGoodsId);
            nxDistributerStandardEntity.setNxDsStandardSort(nxStandardEntities.size());
            nxDistributerStandardService.save(nxDistributerStandardEntity);
        }

        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurchaseGoodsService.queryPurchaseGoodsByParams(linshiMap);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsId(nxDgNxGoodsId);
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                nxDisPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }


        List<NxDistributerGoodsShelfGoodsEntity> entities = nxDistributerGoodsShelfGoodsService.queryShelfForGoodsByParams(linshiMap);
        if (entities.size() > 0) {
            for (NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity : entities) {
//                shelfGoodsEntity.setNxDgsgDisGoodsId(nxGoodsId);
                nxDistributerGoodsShelfGoodsService.delete(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
            }
        }

        Map<String, Object> mapGb = new HashMap<>();
        mapGb.put("nxDisGoodsId", lsGoodsId);
        List<GbDistributerGoodsEntity> goodsEntities = gbDistributerGoodsService.queryDisGoodsByParams(mapGb);
        if (goodsEntities.size() > 0) {
            for (GbDistributerGoodsEntity distributerGoodsEntity : goodsEntities) {
                distributerGoodsEntity.setGbDgNxDistributerGoodsId(nxGoodsId);
                gbDistributerGoodsService.update(distributerGoodsEntity);
            }
        }


        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("nxGoodsId", lsGoodsId);
        List<GbDepartmentDisGoodsEntity> gbDepartmentDisGoodsEntities = gbDepartmentDisGoodsService.queryGbDepDisGoodsByParams(mapDep);
        if (gbDepartmentDisGoodsEntities.size() > 0) {
            for (GbDepartmentDisGoodsEntity disGoodsEntity : gbDepartmentDisGoodsEntities) {
                gbDepartmentDisGoodsService.delete(disGoodsEntity.getGbDepartmentDisGoodsId());
            }
        }

        dgService.delete(lsGoodsId);

        return R.ok();
    }

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
                shelfGoodsEntity.setNxDgsgDisGoodsId(nxGoodsId);
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
        nxDistributerGoodsEntity.setNxDgWillPriceOneWeight(goodsEntity.getNxDgWillPriceOneWeight());
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
            goods.setNxDgPurchaseAuto(1);
            goods.setNxDgWillPrice("0.1");
            goods.setNxDgBuyingPrice("0.1");
            goods.setNxDgBuyingPriceOne("0.1");
            goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
            goods.setNxDgWillPriceOneWeight(goods.getNxDgGoodsStandardname());
            goods.setNxDgWillPriceOne("0.1");
            goods.setNxDgWillPriceOneAboutPrice("0.1");
            goods.setNxDgGoodsIsHidden(0);
            goods.setNxDgPurchaseAuto(1);
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
            ordersEntity.setNxDoGoodsType(-1);
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
            goods.setNxDgPurchaseAuto(1);
            goods.setNxDgWillPrice("0.1");
            goods.setNxDgBuyingPrice("0.1");

            goods.setNxDgBuyingPriceOne("0.1");
            goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
            goods.setNxDgWillPriceOneWeight(goods.getNxDgGoodsStandardname());
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
        goods.setNxDgPurchaseAuto(1);
        goods.setNxDgOutTotalWeight("0");
        goods.setNxDgBuyingPriceOne("0.1");
        goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        goods.setNxDgWillPriceOneWeight(goods.getNxDgGoodsStandardname());
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
        Map<String, Object> map4 = new HashMap<>();
        map4.put("disGoodsId", id);
        map4.put("status", 3);
        List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(map4);
        if (ordersEntities.size() > 0) {
            return R.error(-1, "有客户使用此商品");
        } else {
            //删除客户商品
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", id);
            List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepDisGoodsService.queryDepDisGoodsByParams(map);
            if (departmentDisGoodsEntities.size() > 0) {
                System.out.println("deisisiisis" + departmentDisGoodsEntities.size());
                for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : departmentDisGoodsEntities) {
                    nxDepDisGoodsService.delete(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
                }
            }

            List<NxDepartmentOrdersHistoryEntity> nxDepartmentOrdersHistoryEntities = nxDepartmentOrdersHistoryService.queryDepHistoryOrdersByParams(map);
            if (nxDepartmentOrdersHistoryEntities.size() > 0) {
                System.out.println("ordeheididiiifsi" + nxDepartmentOrdersHistoryEntities.size());
                for (NxDepartmentOrdersHistoryEntity historyEntity : nxDepartmentOrdersHistoryEntities) {
                    nxDepartmentOrdersHistoryService.delete(historyEntity.getNxDepartmentOrdersHistoryId());
                }
            }
            int i = dgService.delete(id);
            if (i == 1) {
                return R.ok();
            } else {
                return R.error(-1, "删除失败");
            }
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
    public R disGetDisTypeGoodsListByFatherId(Integer fatherId, Integer type,
                                              Integer limit, Integer page) {
        System.out.println(fatherId + "fatherididiid");

        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("dgFatherId", fatherId);
        map.put("type", type);
        List<NxDistributerGoodsEntity> goodsEntities1;
        if (type.equals(4)) {
//            goodsEntities1 = dgService.queryDisGoodsWithSupplierByParams(map);
            goodsEntities1 = dgService.queryDisGoodsByParams(map);

        } else {
            goodsEntities1 = dgService.queryDisGoodsByParams(map);
        }


        Map<String, Object> map3 = new HashMap<>();
        map3.put("fatherId", fatherId);
        map3.put("type", type);
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
        if (ordersEntities.size() > 0) {
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
//            Map<String, Object> map = new HashMap<>();
//            map.put("disGoodsId", disGoodsId);
//            map.put("disId", disId);
//            List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepDisGoodsService.queryDepDisGoodsByParams(map);
//            if(departmentDisGoodsEntities.size() > 0){
//                for(NxDepartmentDisGoodsEntity departmentDisGoodsEntity: departmentDisGoodsEntities){
//                    nxDepDisGoodsService.delete(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
//                }
//            }
//
//            List<NxDepartmentOrdersHistoryEntity> nxDepartmentOrdersHistoryEntities = nxDepartmentOrdersHistoryService.queryDepHistoryOrdersByParams(map);
//            if(nxDepartmentOrdersHistoryEntities.size() > 0){
//                for(NxDepartmentOrdersHistoryEntity historyEntity: nxDepartmentOrdersHistoryEntities){
//                    nxDepartmentOrdersHistoryService.delete(historyEntity.getNxDepartmentOrdersHistoryId());
//                }
//            }

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

            NxDistributerGoodsEntity nxDistributerGoodsEntity = saveDisGoods(cgnGoods);

            //2，保存dis规格bieming
            Integer nxCgGoodsId = cgnGoods.getNxDistributerGoodsId();
            //2.1
            List<NxStandardEntity> ncsEntities = cgnGoods.getNxStandardEntities();
            if (ncsEntities.size() > 0) {
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

            //2.2
            List<NxAliasEntity> aliasEntities = cgnGoods.getNxAliasEntities();
            if (aliasEntities.size() > 0) {
                for (NxAliasEntity aliasEntity : aliasEntities) {
                    NxDistributerAliasEntity disAlias = new NxDistributerAliasEntity();
                    disAlias.setNxDaDisGoodsId(nxCgGoodsId);
                    disAlias.setNxDaAliasName(aliasEntity.getNxAliasName());
                    disAliasService.save(disAlias);
                }
            }

            return R.ok().put("data", nxDistributerGoodsEntity);
        }
    }


    private NxDistributerGoodsEntity saveDisGoods(NxDistributerGoodsEntity cgnGoods) {
        Integer nxDgNxGoodsId = cgnGoods.getNxDgNxGoodsId();
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
        cgnGoods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
        cgnGoods.setNxDgWillPrice("0.1");
        cgnGoods.setNxDgBuyingPrice("0.1");
        cgnGoods.setNxDgPurchaseAuto(1);
        cgnGoods.setNxDgBuyingPriceOne("0.1");
        cgnGoods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        cgnGoods.setNxDgWillPriceOneWeight(cgnGoods.getNxDgGoodsStandardname());
        cgnGoods.setNxDgWillPriceOne("0.1");
        cgnGoods.setNxDgWillPriceOneAboutPrice("0.1");
        cgnGoods.setNxDgQuantityDays(nxGoodsEntity.getNxGoodsQuantityDays());

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
        Map<String, Object> map = new HashMap<>();
        map.put("disId", nxDgDistributerId);
        map.put("nxFatherId", nxDgNxFatherId);
        List<NxDistributerGoodsEntity> nxDistributerGoodsEntities = dgService.queryDisGoodsByParams(map);

        if (nxDistributerGoodsEntities.size() > 0) {
            //直接加disGoods和disStandard,不需要加disFatherGoods
            //1，给父类商品的字段商品数量加1
            NxDistributerGoodsEntity disGoodsEntity = nxDistributerGoodsEntities.get(0);
            Integer nxDgDfgGoodsFatherId1 = disGoodsEntity.getNxDgDfgGoodsFatherId();
            Integer nxDgDfgGoodsGrandId = disGoodsEntity.getNxDgDfgGoodsGrandId();

            NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity = dgfService.queryObject(nxDgDfgGoodsFatherId1);
            Integer nxDfgGoodsAmount = nxDistributerFatherGoodsEntity.getNxDfgGoodsAmount();
            nxDistributerFatherGoodsEntity.setNxDfgGoodsAmount(nxDfgGoodsAmount + 1);
            dgfService.update(nxDistributerFatherGoodsEntity);

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
        map1.put("isHidden", 0);
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
        List<NxDistributerPurchaseGoodsEntity> disPurchaseGoods = nxDisPurchaseGoodsService.queryForDisGoods(map2);

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

    @RequestMapping(value = "/queryDisGoodsByQuickSearchWithDepId", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisGoodsByQuickSearchWithDepId(String searchStr, Integer disId, Integer depId) {
        System.out.println(searchStr);
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("depId", depId);
        map.put("searchStr", searchStr);
//        map.put("isHidden", 0);

        List<NxDistributerGoodsEntity> all = new ArrayList<>();

        String pinyinString = searchStr;
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchPinyin", pinyinString);
        System.out.println("sttuutnxdididisisiisisisisiisisi" + map);
        List<NxDistributerGoodsEntity> goodsEntities = dgService.queryDisGoodsQuickSearchStrWithDepOrders(map);
        System.out.println("goennennenssss" + goodsEntities.size());
        if (goodsEntities.size() < 100) {
            Map<String, Object> mapR = new HashMap<>();
            if (goodsEntities.size() == 0) {
                List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);
                if (nxGoodsEntities.size() < 100) {
                    if (nxGoodsEntities.size() == 0) {
                        mapR.put("nxArr", -2);
                    } else {
                        mapR.put("nxArr", nxGoodsEntities);
                    }
                }
            } else {
                mapR.put("nxArr", -1);
                mapR.put("disArr", goodsEntities);
            }
            return R.ok().put("data", mapR);
        } else {
            return R.error(-1, "jixu");
        }

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
            List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);
            if (nxGoodsEntities.size() > 0 && nxGoodsEntities.size() < 100) {
                for (NxDistributerGoodsEntity disGoods : all) {
                    if (disGoods.getNxDgNxGoodsId() != null) {
                        Integer nxDgNxGoodsId = disGoods.getNxDgNxGoodsId();
                        for (int i = 0; i < nxGoodsEntities.size(); i++) {
                            Integer nxGoodsId = nxGoodsEntities.get(i).getNxGoodsId();
                            if (nxDgNxGoodsId.equals(nxGoodsId)) {
                                nxGoodsEntities.remove(i);
                            }
                        }
                    }

                }
                map3.put("nxArr", nxGoodsEntities);
            } else {
                map3.put("nxArr", new ArrayList<>());
            }
        } else {
            map3.put("disArr", new ArrayList<>());
            List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);
            if (nxGoodsEntities.size() > 0 && nxGoodsEntities.size() < 100) {
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
        List<NxDistributerGoodsEntity> distributerGoodsEntities = dgService.queryDisLinshiGoodsQuickSearchStr(map);
        map.put("searchStr", null);
        map.put("searchPinyin", pinyinString);
        List<NxDistributerGoodsEntity> distributerGoodsEntities1 = dgService.queryDisLinshiGoodsQuickSearchStr(map);
        distributerGoodsEntities1.removeAll(distributerGoodsEntities);
        List<NxDistributerGoodsEntity> all = new ArrayList<>();
        all.addAll(distributerGoodsEntities);
        all.addAll(distributerGoodsEntities1);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("disArr", all);
        List<NxGoodsEntity> nxGoodsEntities = nxGoodsService.queryQuickSearchNxGoods(map);

        if (all.size() > 0) {
            for (NxDistributerGoodsEntity disGoods : all) {
                if (disGoods.getNxDgNxGoodsId() != null) {
                    Integer nxDgNxGoodsId = disGoods.getNxDgNxGoodsId();
                    for (int i = 0; i < nxGoodsEntities.size(); i++) {
                        Integer nxGoodsId = nxGoodsEntities.get(i).getNxGoodsId();
                        if (nxDgNxGoodsId.equals(nxGoodsId)) {
                            nxGoodsEntities.remove(i);
                        }
                    }
                }
            }
        }

        map3.put("nxArr", nxGoodsEntities);

        return R.ok().put("data", map3);
    }

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
        if (!distributerGoodsEntity.getNxDgPurchaseAuto().equals(nxDistributerGoods.getNxDgPurchaseAuto())) {
            Map<String, Object> mapR = new HashMap<>();
            mapR.put("disGoodsId", distributerGoodsId);
            mapR.put("status", 3);
            List<NxDepartmentOrdersEntity> ordersEntities = depOrdersService.queryDisOrdersByParams(mapR);
            if (ordersEntities.size() > 0) {
                return R.error(-1, "有未处理完订单");
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

//        return R.ok();


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
            nxDistributerGoods.setNxDgWillPriceOneWeight(nxDistributerGoods.getNxDgGoodsStandardname());
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

}


//sisy


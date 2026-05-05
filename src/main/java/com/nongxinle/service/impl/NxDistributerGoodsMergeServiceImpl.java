package com.nongxinle.service.impl;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxPurchaseGoodsInputTypeOrder;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxPurchaseGoodsTypeForSelf;

/**
 * 临时商品合并到正式商品服务实现
 */
@Service
public class NxDistributerGoodsMergeServiceImpl implements NxDistributerGoodsMergeService {
    private static final Logger logger = LoggerFactory.getLogger(NxDistributerGoodsMergeServiceImpl.class);

    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;
    @Autowired
    private NxDepartmentOrderHistoryService historyService;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;
    @Autowired
    private NxDistributerStandardService nxDistributerStandardService;
    @Autowired
    private NxDistributerGoodsShelfStockService nxDisGoodsShelfStockService;
    @Autowired
    private NxDistributerGoodsShelfGoodsService shelfGoodsService;
    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private NxDistributerGoodsLinshiService linshiService;
    @Autowired
    private NxOrderOcrTrainingDataService nxOrderOcrTrainingDataService;

    @Override
    public NxDistributerGoodsEntity mergeLinshiToNxGoods(Integer lsGoodsId, Integer nxGoodsId) {
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDistributerGoodsService.queryObject(nxGoodsId);
        NxDistributerGoodsEntity linshiGoods = nxDistributerGoodsService.queryObject(lsGoodsId);

        Map<String, Object> mapPur = new HashMap<>();
        mapPur.put("disGoodsId", lsGoodsId);
        List<NxDistributerPurchaseGoodsEntity> nxDistributerPurchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(mapPur);
        if (nxDistributerPurchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : nxDistributerPurchaseGoodsEntities) {
                Integer nxDpgOrdersAmount = purchaseGoodsEntity.getNxDpgOrdersAmount();
                if (nxDpgOrdersAmount == 1) {
                    if (purchaseGoodsEntity.getNxDpgBatchId() == null) {
                        nxDistributerPurchaseGoodsService.delete(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
                    } else {
                        purchaseGoodsEntity.setNxDpgDisGoodsId(nxGoodsId);
                        purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDistributerGoodsEntity.getGbDisGoodsFatherId());
                        purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                        nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                    }
                } else {
                    purchaseGoodsEntity.setNxDpgOrdersAmount(nxDpgOrdersAmount - 1);
                    purchaseGoodsEntity.setNxDpgDisGoodsId(nxGoodsId);
                    purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDistributerGoodsEntity.getGbDisGoodsFatherId());
                    purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId());
                    nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);

                    if (nxDistributerGoodsEntity.getNxDgBuyingPriceOne() != null && nxDistributerGoodsEntity.getNxDgBuyingPriceOne().equals("0.1")) {
                        nxDistributerGoodsEntity.setNxDgBuyingPriceOne(purchaseGoodsEntity.getNxDpgBuyPrice());
                        nxDistributerGoodsEntity.setNxDgBuyingPrice(purchaseGoodsEntity.getNxDpgBuyPrice());
                        nxDistributerGoodsEntity.setNxDgBuyingPriceUpdate(formatWhatDay(0));
                        nxDistributerGoodsService.update(nxDistributerGoodsEntity);
                    }
                }
            }
        }

        Integer nxDgDfgGoodsFatherId = nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId();
        Integer nxDgDfgGoodsGrandId = nxDistributerGoodsEntity.getNxDgDfgGoodsGrandId();
        Integer nxDgNxGoodsId = nxDistributerGoodsEntity.getNxDgNxGoodsId();
        Integer nxDgNxFatherId = nxDistributerGoodsEntity.getNxDgNxFatherId();

        Map<String, Object> linshiMap = new HashMap<>();
        linshiMap.put("disGoodsId", lsGoodsId);
        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(linshiMap);
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : ordersEntities) {
                ordersEntity.setNxDoDisGoodsId(nxGoodsId);
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                if (ordersEntity.getNxDoStatus() != null && ordersEntity.getNxDoStatus() == -2) {
                    ordersEntity.setNxDoStatus(0);
                }
                nxDepartmentOrdersService.update(ordersEntity);

                if (nxDistributerGoodsEntity.getNxDgPurchaseAuto() != null && nxDistributerGoodsEntity.getNxDgPurchaseAuto() != -1) {
                    nxDepartmentOrdersService.savePurGoodsAuto(ordersEntity, getNxPurchaseGoodsInputTypeOrder(), getNxPurchaseGoodsTypeForSelf());
                }
            }
        }

        Map<String, Object> linshiMapH = new HashMap<>();
        linshiMapH.put("disGoodsId", lsGoodsId);
        List<NxDepartmentOrderHistoryEntity> ordersEntitiesH = historyService.queryDisHistoryOrdersByParams(linshiMapH);
        if (ordersEntitiesH.size() > 0) {
            for (NxDepartmentOrderHistoryEntity ordersEntity : ordersEntitiesH) {
                ordersEntity.setNxDoDisGoodsId(nxGoodsId);
                ordersEntity.setNxDoDisGoodsFatherId(nxDgDfgGoodsFatherId);
                ordersEntity.setNxDoDisGoodsGrandId(nxDgDfgGoodsGrandId);
                ordersEntity.setNxDoNxGoodsId(nxDgNxGoodsId);
                ordersEntity.setNxDoNxGoodsFatherId(nxDgNxFatherId);
                historyService.update(ordersEntity);
            }
        }

        List<NxDepartmentDisGoodsEntity> departmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(linshiMap);
        if (departmentDisGoodsEntities.size() > 0) {
            for (NxDepartmentDisGoodsEntity linshiDepGoodsEntity : departmentDisGoodsEntities) {
                Integer nxDdgDepartmentId = linshiDepGoodsEntity.getNxDdgDepartmentId();
                Map<String, Object> map = new HashMap<>();
                map.put("depId", nxDdgDepartmentId);
                map.put("disGoodsId", nxGoodsId);
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
                if (normalizeStandardNameForComparison(linshiGoods.getNxDgGoodsStandardname())
                        .equals(normalizeStandardNameForComparison(nxDistributerStandardEntity.getNxDsStandardName()))) {
                    same = same + 1;
                }
            }
            if (normalizeStandardNameForComparison(nxDistributerGoodsEntity.getNxDgGoodsStandardname())
                    .equals(normalizeStandardNameForComparison(linshiGoods.getNxDgGoodsStandardname()))) {
                same = same + 1;
            }
        }
        String nxDgGoodsStandardname = nxDistributerGoodsEntity.getNxDgGoodsStandardname();
        String nxDgGoodsStandardname1 = linshiGoods.getNxDgGoodsStandardname();
        if (nxDgGoodsStandardname != null && nxDgGoodsStandardname1 != null
                && normalizeStandardNameForComparison(nxDgGoodsStandardname).equals(normalizeStandardNameForComparison(nxDgGoodsStandardname1))) {
            same = same + 1;
        }
        if (same == 0) {
            NxDistributerStandardEntity nxDistributerStandardEntity = new NxDistributerStandardEntity();
            nxDistributerStandardEntity.setNxDsStandardName(linshiGoods.getNxDgGoodsStandardname());
            nxDistributerStandardEntity.setNxDsDisGoodsId(nxGoodsId);
            nxDistributerStandardEntity.setNxDsStandardSort(nxStandardEntities != null ? nxStandardEntities.size() : 0);
            nxDistributerStandardService.save(nxDistributerStandardEntity);
        }

        List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(linshiMap);
        if (purchaseGoodsEntities.size() > 0) {
            for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntities) {
                purchaseGoodsEntity.setNxDpgDisGoodsId(nxDgNxGoodsId);
                purchaseGoodsEntity.setNxDpgDisGoodsFatherId(nxDgDfgGoodsFatherId);
                purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDgDfgGoodsGrandId);
                nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
            }
        }

        Map<String, Object> stockMap = new HashMap<>();
        stockMap.put("disGoodsId", lsGoodsId);
        List<NxDistributerGoodsShelfStockEntity> stockList = nxDisGoodsShelfStockService.queryShelfStockListByParams(stockMap);
        if (stockList != null && !stockList.isEmpty()) {
            Map<String, Object> nxGoodsShelfMap = new HashMap<>();
            nxGoodsShelfMap.put("disGoodsId", nxGoodsId);
            List<NxDistributerGoodsShelfGoodsEntity> nxGoodsShelfGoodsList = shelfGoodsService.queryShelfForGoodsByParams(nxGoodsShelfMap);
            NxDistributerGoodsShelfGoodsEntity nxGoodsShelfGoods = null;
            if (nxGoodsShelfGoodsList != null && !nxGoodsShelfGoodsList.isEmpty()) {
                nxGoodsShelfGoods = nxGoodsShelfGoodsList.stream()
                        .filter(sg -> sg.getNxDgsgShelfId() != null && sg.getNxDgsgShelfId() == 1)
                        .findFirst()
                        .orElse(nxGoodsShelfGoodsList.get(0));
            }

            for (NxDistributerGoodsShelfStockEntity stock : stockList) {
                stock.setNxDgssNxDisGoodsId(nxGoodsId);
                stock.setNxDgssNxDisGoodsFatherId(nxDgDfgGoodsFatherId);
                if (nxGoodsShelfGoods != null && nxGoodsShelfGoods.getNxDistributerGoodsShelfGoodsId() != null) {
                    stock.setNxDgssNxShelfGoodsId(nxGoodsShelfGoods.getNxDistributerGoodsShelfGoodsId());
                }
                nxDisGoodsShelfStockService.update(stock);
            }
        }

        List<NxDistributerGoodsShelfGoodsEntity> entities = shelfGoodsService.queryShelfForGoodsByParams(linshiMap);
        if (entities.size() > 0) {
            for (NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity : entities) {
                shelfGoodsEntity.setNxDgsgDisGoodsId(nxGoodsId);
                shelfGoodsService.update(shelfGoodsEntity);
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

        NxDistributerGoodsLinshiEntity linshiEntity = linshiService.queryLinshiByFromGoodsId(lsGoodsId);
        if (linshiEntity != null) {
            linshiEntity.setNxDgGoodsLsStatus(-1);
            linshiEntity.setNxDgToNxDisGoodsId(nxGoodsId);
            linshiService.update(linshiEntity);
        } else {
            linshiEntity = new NxDistributerGoodsLinshiEntity();
            linshiEntity.setNxDgDistributerLsId(linshiGoods.getNxDgDistributerId());
            linshiEntity.setNxDgGoodsLsName(linshiGoods.getNxDgGoodsName());
            linshiEntity.setNxDgGoodsLsStandardname(linshiGoods.getNxDgGoodsStandardname());
            linshiEntity.setNxDgDfgGoodsFatherLsId(linshiGoods.getNxDgDfgGoodsFatherId());
            linshiEntity.setNxDgGoodsLsDetail(linshiGoods.getNxDgGoodsDetail());
            linshiEntity.setNxDgGoodsLsStatus(-1);
            linshiEntity.setNxDgToNxDisGoodsId(nxGoodsId);
            linshiEntity.setNxDgApplyDate(linshiGoods.getNxDgBuyingPriceUpdate());
            linshiEntity.setNxDgGoodsLsFile(linshiGoods.getNxDgGoodsFile());
            linshiEntity.setNxDgGoodsLsFileLarge(linshiGoods.getNxDgGoodsFileLarge());
            linshiService.save(linshiEntity);
        }

        Map<String, Object> mapT = new HashMap<>();
        mapT.put("disGoodsId", lsGoodsId);
        List<NxOrderOcrTrainingDataEntity> nxOrderOcrTrainingDataEntities = nxOrderOcrTrainingDataService.queryTrainingDataList(mapT);
        if (nxOrderOcrTrainingDataEntities != null && !nxOrderOcrTrainingDataEntities.isEmpty() && nxDgNxGoodsId != null) {
            for (NxOrderOcrTrainingDataEntity dataEntity : nxOrderOcrTrainingDataEntities) {
                Integer trainingDataId = dataEntity.getNxOtdId();
                if (trainingDataId != null) {
                    dataEntity.setNxOtdDisGoodsId(nxGoodsId);
                    nxOrderOcrTrainingDataService.update(dataEntity);
                }
            }
        }

        nxDistributerGoodsService.delete(lsGoodsId);

        return nxDistributerGoodsEntity;
    }

    private String normalizeStandardNameForComparison(String name) {
        if (name == null) return "";
        return name.replace("代", "袋").replace("课", "颗").replace("棵", "颗");
    }
}

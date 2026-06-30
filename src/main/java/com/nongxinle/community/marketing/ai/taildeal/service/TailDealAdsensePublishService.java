package com.nongxinle.community.marketing.ai.taildeal.service;

import com.nongxinle.community.marketing.ai.taildeal.TailDealAdsenseDefaults;

import com.nongxinle.community.catalog.service.NxCommunityGoodsService;
import com.nongxinle.community.marketing.ai.taildeal.draft.TailDealAdsenseDraft;
import com.nongxinle.community.marketing.ai.taildeal.gate.TailDealAdsenseConfirmSafetyGate;
import com.nongxinle.community.marketing.service.NxCommunityAdsenseService;
import com.nongxinle.entity.NxCommunityAdsenseEntity;
import com.nongxinle.entity.NxCommunityGoodsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;

@Service
public class TailDealAdsensePublishService {

    private static final Logger log = LoggerFactory.getLogger(TailDealAdsensePublishService.class);

    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;

    @Autowired
    private NxCommunityAdsenseService nxCommunityAdsenseService;

    @Transactional
    public Map<String, Object> publish(TailDealAdsenseDraft draft, String normalizedConfirmAction,
                                       Integer selectedGoodsId, Integer selectedFatherGoodsId,
                                       Map<String, Object> override) {
        boolean publishVisible = TailDealAdsenseConfirmSafetyGate.isPublishAction(normalizedConfirmAction);
        int nxCaStatus = publishVisible ? 0 : 1;
        log.info("[TailDealAI] publishStart action={}, publishVisible={}, nxCaStatus={}",
                normalizedConfirmAction, publishVisible, nxCaStatus);

        NxCommunityGoodsEntity goods = resolveGoods(draft, selectedGoodsId, selectedFatherGoodsId);
        log.info("[TailDealAI] publishGoodsResolved goodsId={}, goodsName={}, planAction={}",
                goods.getNxCommunityGoodsId(), goods.getNxCgGoodsName(),
                draft.getCommunityGoodsPlan() == null ? null : draft.getCommunityGoodsPlan().get("action"));
        applySellTimePlan(goods, draft.getAdsensePlan());
        applyAdsensePlan(goods, draft.getAdsensePlan(), override);
        applyPriceSplit(goods);

        Map<String, Object> adsenseQuery = new HashMap<>();
        adsenseQuery.put("goodsId", goods.getNxCommunityGoodsId());
        NxCommunityAdsenseEntity existingAdsense = nxCommunityAdsenseService.queryGoodsAdsenseByParams(adsenseQuery);

        if (existingAdsense == null) {
            log.info("[TailDealAI] publishCreateAdsense goodsId={}, nxCaStatus={}", goods.getNxCommunityGoodsId(), nxCaStatus);
            saveNewAdsense(goods, nxCaStatus);
        } else {
            log.info("[TailDealAI] publishUpdateAdsense goodsId={}, adsenseId={}, nxCaStatus={}",
                    goods.getNxCommunityGoodsId(), existingAdsense.getNxCommunityAdsenseId(), nxCaStatus);
            syncAdsenseFromGoods(goods, existingAdsense, nxCaStatus);
            nxCommunityAdsenseService.update(existingAdsense);
            nxCommunityGoodsService.update(goods);
        }

        NxCommunityAdsenseEntity adsense = queryAdsense(goods.getNxCommunityGoodsId());
        log.info("[TailDealAI] publishDone goodsId={}, adsenseId={}, nxCaStatus={}, stock={}, rest={}",
                goods.getNxCommunityGoodsId(),
                adsense == null ? null : adsense.getNxCommunityAdsenseId(),
                adsense == null ? nxCaStatus : adsense.getNxCaStatus(),
                goods.getNxCgAdsenseStockQuantity(), goods.getNxCgAdsenseRestQuantity());
        return buildResult(goods, adsense, publishVisible, draft);
    }

    private NxCommunityGoodsEntity resolveGoods(TailDealAdsenseDraft draft, Integer selectedGoodsId,
                                                 Integer selectedFatherGoodsId) {
        Map<String, Object> plan = draft.getCommunityGoodsPlan();
        String action = plan == null ? null : (String) plan.get("action");

        if (selectedGoodsId != null) {
            NxCommunityGoodsEntity existing = nxCommunityGoodsService.queryObject(selectedGoodsId);
            if (existing == null) {
                throw new IllegalArgumentException("selectedGoodsId not found");
            }
            return existing;
        }

        if ("CREATE_NEW".equals(action)) {
            Map<String, Object> proposed = (Map<String, Object>) plan.get("proposedGoods");
            NxCommunityGoodsEntity entity = mapToGoodsEntity(proposed, draft);
            if (selectedFatherGoodsId != null) {
                entity.setNxCgCfgGoodsFatherId(selectedFatherGoodsId);
            }
            if (entity.getNxCgCfgGoodsFatherId() == null) {
                entity.setNxCgCfgGoodsFatherId(TailDealAdsenseDefaults.DEFAULT_FATHER_GOODS_ID);
            }
            prepareNewGoodsDefaults(entity);
            nxCommunityGoodsService.save(entity);
            log.info("[TailDealAI] publishCreateGoods goodsId={}, name={}",
                    entity.getNxCommunityGoodsId(), entity.getNxCgGoodsName());
            return entity;
        }

        Integer matchedId = plan == null ? null : (Integer) plan.get("matchedGoodsId");
        if (matchedId == null) {
            throw new IllegalArgumentException("goodsId unresolved");
        }
        return nxCommunityGoodsService.queryObject(matchedId);
    }

    private NxCommunityGoodsEntity mapToGoodsEntity(Map<String, Object> proposed, TailDealAdsenseDraft draft) {
        NxCommunityGoodsEntity entity = new NxCommunityGoodsEntity();
        entity.setNxCgCommunityId(draft.getCommunityId());
        entity.setNxCgCommerceId(draft.getCommerceId());
        entity.setNxCgGoodsName((String) proposed.get("nxCgGoodsName"));
        entity.setNxCgGoodsStandardname((String) proposed.get("nxCgGoodsStandardname"));
        entity.setNxCgGoodsPrice((String) proposed.get("nxCgGoodsPrice"));
        entity.setNxCgGoodsType(proposed.get("nxCgGoodsType") != null ? (Integer) proposed.get("nxCgGoodsType") : 0);
        entity.setNxCgCfgGoodsFatherId((Integer) proposed.get("nxCgCfgGoodsFatherId"));
        entity.setNxCgGoodsDetail((String) proposed.get("nxCgGoodsDetail"));
        if (proposed.get("nxCgGoodsStandardWeight") != null) {
            entity.setNxCgGoodsStandardWeight(proposed.get("nxCgGoodsStandardWeight").toString());
        }
        if (proposed.get("nxCgGoodsGrossWeight") != null) {
            entity.setNxCgGoodsGrossWeight(proposed.get("nxCgGoodsGrossWeight").toString());
        }
        if (proposed.get("nxCgGoodsGrossPrice") != null) {
            entity.setNxCgGoodsGrossPrice(proposed.get("nxCgGoodsGrossPrice").toString());
        }
        if (proposed.get("nxCgGoodsNetWeight") != null) {
            entity.setNxCgGoodsNetWeight(proposed.get("nxCgGoodsNetWeight").toString());
        }
        if (proposed.get("nxCgGoodsNetPrice") != null) {
            entity.setNxCgGoodsNetPrice(proposed.get("nxCgGoodsNetPrice").toString());
        }
        return entity;
    }

    private void prepareNewGoodsDefaults(NxCommunityGoodsEntity entity) {
        entity.setNxCgGoodsStatus(0);
        entity.setNxCgPullOff(TailDealAdsenseDefaults.DEFAULT_PULL_OFF_ON);
        entity.setNxCgIsOpenAdsense(0);
        entity.setNxCgPromotionType(0);
        entity.setNxCgNxGoodsId(-1);
        entity.setNxCgNxFatherId(-1);
        entity.setNxCgNxGrandId(-1);
        entity.setNxCgNxGreatGrandId(-1);
        String goodsName = entity.getNxCgGoodsName();
        entity.setNxCgGoodsPy(getHeadStringByString(goodsName, false, null));
        entity.setNxCgGoodsPinyin(hanziToPinyin(goodsName));
        entity.setNxCgServiceType(TailDealAdsenseDefaults.DEFAULT_SERVICE_TYPE_TAKEOUT);
        entity.setNxCgSellType(TailDealAdsenseDefaults.DEFAULT_SELL_TYPE_PART_TIME);
        if (entity.getNxCommunityGoodsSetItemEntities() == null) {
            entity.setNxCommunityGoodsSetItemEntities(new ArrayList<>());
        }
    }

    private void applySellTimePlan(NxCommunityGoodsEntity goods, Map<String, Object> adsensePlan) {
        goods.setNxCgServiceType(TailDealAdsenseDefaults.DEFAULT_SERVICE_TYPE_TAKEOUT);
        goods.setNxCgSellType(TailDealAdsenseDefaults.DEFAULT_SELL_TYPE_PART_TIME);
        if (adsensePlan == null) {
            return;
        }
        String start = (String) adsensePlan.get("nxCgAdsenseStartTime");
        String stop = (String) adsensePlan.get("nxCgAdsenseStopTime");
        if (start != null && !start.isEmpty()) {
            goods.setNxCgStartTime(start);
            goods.setNxCgStartTimeZone(toMinuteZone(start));
        }
        if (stop != null && !stop.isEmpty()) {
            goods.setNxCgStopTime(stop);
            goods.setNxCgStopTimeZone(toMinuteZone(stop));
        }
        log.info("[TailDealAI] applySellTime serviceType=1, sellType=1, start={}, stop={}",
                goods.getNxCgStartTime(), goods.getNxCgStopTime());
    }

    @SuppressWarnings("unchecked")
    private void applyAdsensePlan(NxCommunityGoodsEntity goods, Map<String, Object> adsensePlan,
                                  Map<String, Object> override) {
        if (override != null) {
            if (override.get("dealPrice") != null) {
                adsensePlan.put("nxCgGoodsPrice", String.valueOf(override.get("dealPrice")));
                adsensePlan.put("nxCgPromotionPrice", adsensePlan.get("nxCgGoodsPrice"));
            }
            if (override.get("totalStock") != null) {
                adsensePlan.put("nxCgAdsenseStockQuantity", override.get("totalStock"));
                adsensePlan.put("nxCgAdsenseRestQuantity", override.get("totalStock"));
            }
        }

        goods.setNxCgIsOpenAdsense(1);
        goods.setNxCgAdsenseStartTime((String) adsensePlan.get("nxCgAdsenseStartTime"));
        goods.setNxCgAdsenseStopTime((String) adsensePlan.get("nxCgAdsenseStopTime"));
        goods.setNxCgAdsenseStartTimeZone(toMinuteZone(goods.getNxCgAdsenseStartTime()));
        goods.setNxCgAdsenseStopTimeZone(toMinuteZone(goods.getNxCgAdsenseStopTime()));

        if (adsensePlan.get("nxCgAdsenseStockQuantity") != null) {
            goods.setNxCgAdsenseStockQuantity((Integer) adsensePlan.get("nxCgAdsenseStockQuantity"));
        }
        if (adsensePlan.get("nxCgAdsenseRestQuantity") != null) {
            goods.setNxCgAdsenseRestQuantity((Integer) adsensePlan.get("nxCgAdsenseRestQuantity"));
        }
        if (adsensePlan.get("nxCgGoodsPrice") != null) {
            goods.setNxCgGoodsPrice(adsensePlan.get("nxCgGoodsPrice").toString());
        }
        if (adsensePlan.get("nxCgPromotionPrice") != null) {
            goods.setNxCgPromotionPrice(adsensePlan.get("nxCgPromotionPrice").toString());
        }
        if (adsensePlan.get("nxCgGoodsHuaxianPrice") != null) {
            goods.setNxCgGoodsHuaxianPrice(adsensePlan.get("nxCgGoodsHuaxianPrice").toString());
        }
        if (adsensePlan.get("nxCgAdsenseMinOrderQty") != null) {
            goods.setNxCgAdsenseMinOrderQty((Integer) adsensePlan.get("nxCgAdsenseMinOrderQty"));
        }
        if (adsensePlan.get("nxCgAdsenseOrderMultiple") != null) {
            goods.setNxCgAdsenseOrderMultiple((Integer) adsensePlan.get("nxCgAdsenseOrderMultiple"));
        }
        if (adsensePlan.get("nxCgAdsenseLimitPerCustomer") != null) {
            goods.setNxCgAdsenseLimitPerCustomer((Integer) adsensePlan.get("nxCgAdsenseLimitPerCustomer"));
        }
        applyPriceSplit(goods);
    }

    private void applyPriceSplit(NxCommunityGoodsEntity goods) {
        if (goods.getNxCgGoodsPrice() == null) {
            return;
        }
        BigDecimal goodsPrice = new BigDecimal(goods.getNxCgGoodsPrice());
        BigDecimal fractionalPart = goodsPrice.subtract(goodsPrice.setScale(0, RoundingMode.DOWN))
                .multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP);
        BigDecimal integerPart = goodsPrice.setScale(0, RoundingMode.DOWN);
        goods.setNxCgGoodsPriceInteger(integerPart.toString());
        goods.setNxCgGoodsPriceDecimal(fractionalPart.toString());
        if (goods.getNxCgGoodsHuaxianPrice() != null && goods.getNxCgGoodsHuaxianPrice().length() > 0) {
            BigDecimal huaxianPrice = new BigDecimal(goods.getNxCgGoodsHuaxianPrice());
            BigDecimal difDec = huaxianPrice.subtract(goodsPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            goods.setNxCgGoodsHuaxianPriceDifferent(difDec.toString());
        }
    }

    private void saveNewAdsense(NxCommunityGoodsEntity goods, int nxCaStatus) {
        NxCommunityAdsenseEntity adsenseEntity = new NxCommunityAdsenseEntity();
        adsenseEntity.setNxCaStatus(nxCaStatus);
        adsenseEntity.setNxCaCommunityId(goods.getNxCgCommunityId());
        adsenseEntity.setNxCommunityAdsenseName(goods.getNxCgGoodsName());
        adsenseEntity.setNxCaClickTo(buildClickPath(goods));
        adsenseEntity.setNxCaCgGoodsId(goods.getNxCommunityGoodsId());
        adsenseEntity.setNxCaFilePath(goods.getNxCgNxGoodsTopFilePath());
        adsenseEntity.setNxCaStartTimeZone(goods.getNxCgAdsenseStartTimeZone());
        adsenseEntity.setNxCaStopTimeZone(goods.getNxCgAdsenseStopTimeZone());
        adsenseEntity.setNxCaStartTime(goods.getNxCgAdsenseStartTime());
        adsenseEntity.setNxCaStopTime(goods.getNxCgAdsenseStopTime());
        nxCommunityAdsenseService.save(adsenseEntity);
        nxCommunityGoodsService.update(goods);
    }

    private void syncAdsenseFromGoods(NxCommunityGoodsEntity goods, NxCommunityAdsenseEntity adsense, int nxCaStatus) {
        adsense.setNxCaStatus(nxCaStatus);
        adsense.setNxCommunityAdsenseName(goods.getNxCgGoodsName());
        adsense.setNxCaClickTo(buildClickPath(goods));
        adsense.setNxCaStartTimeZone(goods.getNxCgAdsenseStartTimeZone());
        adsense.setNxCaStopTimeZone(goods.getNxCgAdsenseStopTimeZone());
        adsense.setNxCaStartTime(goods.getNxCgAdsenseStartTime());
        adsense.setNxCaStopTime(goods.getNxCgAdsenseStopTime());
        if (goods.getNxCgNxGoodsTopFilePath() != null) {
            adsense.setNxCaFilePath(goods.getNxCgNxGoodsTopFilePath());
        }
    }

    private NxCommunityAdsenseEntity queryAdsense(Integer goodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goodsId);
        return nxCommunityAdsenseService.queryGoodsAdsenseByParams(map);
    }

    private String buildClickPath(NxCommunityGoodsEntity goods) {
        Integer nxCgGoodsType = goods.getNxCgGoodsType() != null ? goods.getNxCgGoodsType() : 0;
        Integer nxCgCardId = goods.getNxCgCardId();
        String path;
        if (nxCgGoodsType == 0) {
            path = nxCgCardId != null ? "zeroGoodsCardPage/zeroGoodsCardPage" : "zeroGoodsPage/zeroGoodsPage";
        } else if (nxCgGoodsType == 1) {
            path = nxCgCardId != null ? "oneGoodsCardPage/oneGoodsCardPage" : "oneGoodsPage/oneGoodsPage";
        } else if (nxCgGoodsType == 2) {
            path = nxCgCardId != null ? "twoGoodsCardPage/twoGoodsCardPage" : "twoGoodsPage/twoGoodsPage";
        } else {
            path = nxCgCardId != null ? "threeGoodsCardPage/threeGoodsCardPage" : "threeGoodsPage/threeGoodsPage";
        }
        return path + "?nxCommunityGoodsId=" + goods.getNxCommunityGoodsId()
                + "&from=index&orderType=0&spId=-1&pindanId=-1"
                + "&serviceType=" + resolveGoodsServiceType(goods);
    }

    private static int resolveGoodsServiceType(NxCommunityGoodsEntity goods) {
        if (goods != null && goods.getNxCgServiceType() != null) {
            return goods.getNxCgServiceType();
        }
        return 0;
    }

    private String toMinuteZone(String hhmm) {
        String startHour = hhmm.substring(0, 2);
        String startMinute = hhmm.substring(3, 5);
        BigDecimal hourMinuteStart = new BigDecimal(startHour).multiply(new BigDecimal(60));
        return hourMinuteStart.add(new BigDecimal(startMinute)).setScale(0, BigDecimal.ROUND_HALF_UP).toString();
    }

    private Map<String, Object> buildResult(NxCommunityGoodsEntity goods, NxCommunityAdsenseEntity adsense,
                                            boolean publishVisible, TailDealAdsenseDraft draft) {
        Map<String, Object> data = new HashMap<>();
        data.put("status", publishVisible ? "PUBLISHED" : "SAVED");
        data.put("flowState", "SUCCESS");
        data.put("goodsId", goods.getNxCommunityGoodsId());
        data.put("adsenseId", adsense != null ? adsense.getNxCommunityAdsenseId() : null);
        data.put("goodsName", goods.getNxCgGoodsName());
        data.put("nxCgIsOpenAdsense", goods.getNxCgIsOpenAdsense());
        data.put("nxCaStatus", adsense != null ? adsense.getNxCaStatus() : (publishVisible ? 0 : 1));
        data.put("nxCgAdsenseRestQuantity", goods.getNxCgAdsenseRestQuantity());
        data.put("nxCgAdsenseStockQuantity", goods.getNxCgAdsenseStockQuantity());
        data.put("homepagePromotion", publishVisible);
        data.put("publishVisible", publishVisible);
        if (draft.getDeal() != null) {
            data.put("startTime", draft.getDeal().get("startTime"));
            data.put("endTime", draft.getDeal().get("endTime"));
        }
        String detailPath = buildClickPath(goods);
        data.put("goodsDetailPath", detailPath);
        Integer adsenseId = adsense != null ? adsense.getNxCommunityAdsenseId() : null;
        data.put("sharePath", "/pages/zeroGoodsPage/zeroGoodsPage?nxCommunityGoodsId="
                + goods.getNxCommunityGoodsId() + "&from=tailDeal&adsenseId=" + adsenseId);
        data.put("wecomGroupId", draft.getTargetWecomGroupId());
        data.put("targetYgtCampaignId", draft.getTargetYgtCampaignId());
        data.put("publishToWecomGroup", draft.getPublishToWecomGroup());
        data.put("userFacingSummary", publishVisible
                ? "已发布尾货广告商品，首页/广告栏对用户可见。"
                : "已保存尾货广告商品（Adsense 已配置，发布开关默认关闭，用户端不可见）。");
        return data;
    }
}

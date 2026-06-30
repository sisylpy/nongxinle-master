package com.nongxinle.community.marketing.ai.taildeal.draft;

import com.nongxinle.community.marketing.ai.taildeal.TailDealAdsenseDefaults;
import com.nongxinle.community.marketing.ai.taildeal.adapter.CommunityGoodsMatchAdapter;
import com.nongxinle.community.marketing.ai.taildeal.contract.TailDealAdsenseSemanticContract;
import com.nongxinle.community.marketing.ai.taildeal.validate.TailDealAdsenseDeterministicValidator;
import com.nongxinle.community.marketing.ai.taildeal.validate.TailDealAdsenseTimeParser;
import com.nongxinle.community.marketing.ai.taildeal.validate.TailDealAdsenseWeightResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TailDealAdsenseDraftBuilder {

    private static final Logger log = LoggerFactory.getLogger(TailDealAdsenseDraftBuilder.class);

    @Autowired
    private CommunityGoodsMatchAdapter goodsMatchAdapter;

    @Autowired
    private TailDealAdsenseDeterministicValidator validator;

    @Autowired
    private TailDealAdsenseWeightResolver weightResolver;

    private final TailDealAdsenseTimeParser timeParser = new TailDealAdsenseTimeParser();

    public TailDealAdsenseDraft build(TailDealAdsenseSemanticContract contract, ParseContext ctx) {
        TailDealAdsenseDraft draft = new TailDealAdsenseDraft();
        draft.setOperatorUserId(ctx.getOperatorUserId());
        draft.setCommunityId(ctx.getCommunityId());
        draft.setCommerceId(ctx.getCommerceId());
        draft.setRawText(ctx.getRawText());
        draft.setDefaultMarketCloseTime(ctx.getDefaultMarketCloseTime());
        draft.setTargetWecomGroupId(ctx.getTargetWecomGroupId() != null
                ? ctx.getTargetWecomGroupId() : contract.getTargetWecomGroupId());
        draft.setTargetYgtCampaignId(ctx.getTargetYgtCampaignId() != null
                ? ctx.getTargetYgtCampaignId() : contract.getTargetYgtCampaignId());
        draft.setPublishToWecomGroup(ctx.getPublishToWecomGroup() != null
                ? ctx.getPublishToWecomGroup() : contract.getPublishToWecomGroup());
        draft.setHomepagePromotion(ctx.getHomepagePromotion() != null
                ? ctx.getHomepagePromotion() : contract.getHomepagePromotion());
        draft.setSemanticContract(contract);
        enrichContractFromRawText(contract, ctx.getRawText());

        List<String> missing = validator.validateForDraft(contract, ctx.getDefaultMarketCloseTime());
        draft.setMissingFields(missing);
        if (!missing.isEmpty()) {
            log.info("[TailDealAI] draftNeedClarify missing={}", missing);
            draft.setStatus("NEED_CLARIFY");
            draft.setFlowState("NEED_CLARIFY");
            draft.setConfirmRequired(false);
            draft.setUserFacingSummary("还缺少：" + formatMissingFields(missing) + "。请补充后再生成草稿。");
            draft.setCommunityGoodsPlan(pendingPlan());
            return draft;
        }

        TailDealAdsenseTimeParser.ParsedTime start = timeParser.parseStartTime(contract.getStartTimeText());
        TailDealAdsenseTimeParser.ParsedTime end = timeParser.resolveEndTime(
                contract.getEndTimeText(), ctx.getDefaultMarketCloseTime());

        draft.setAdsensePlan(buildAdsensePlan(contract, start, end));
        draft.setDeal(buildDeal(contract, start, end, ctx.getRawText()));

        List<Map<String, Object>> candidates = goodsMatchAdapter.matchGoods(
                contract.getGoodsName(), ctx.getCommunityId());
        draft.setGoodsCandidates(candidates);
        log.info("[TailDealAI] draftMatch goodsName={}, candidateCount={}, topConfidence={}",
                contract.getGoodsName(), candidates.size(),
                candidates.isEmpty() ? null : candidates.get(0).get("confidence"));

        if (candidates.size() > 1 && candidates.get(0).get("confidence") instanceof Double
                && (Double) candidates.get(0).get("confidence") < 0.85) {
            draft.setStatus("GOODS_CHOICE");
            draft.setFlowState("GOODS_CHOICE");
            draft.setConfirmRequired(false);
            draft.setCommunityGoodsPlan(choicePlan());
            draft.setUserFacingSummary("找到多个相似商品，请选择要开启尾货广告的商品，或选择创建新商品。");
            return draft;
        }

        if (candidates.isEmpty()) {
            draft.setCommunityGoodsPlan(createNewPlan(contract, ctx));
            if (draft.getCommunityGoodsPlan().get("missingGoodsFields") != null) {
                List<String> createMissing = (List<String>) draft.getCommunityGoodsPlan().get("missingGoodsFields");
                draft.getMissingFields().addAll(createMissing);
                draft.setStatus("NEED_CLARIFY");
                draft.setFlowState("NEED_CLARIFY");
                draft.setConfirmRequired(false);
                draft.setUserFacingSummary("未匹配到现有商品，将创建新商品；请先选择商品分类。");
                return draft;
            }
        } else {
            Map<String, Object> top = candidates.get(0);
            draft.setCommunityGoodsPlan(matchPlan(top));
            draft.setMatchedGoods(top);
        }

        draft.setStatus("DRAFT_READY");
        draft.setFlowState("DRAFT_READY");
        draft.setConfirmRequired(true);
        draft.setUserFacingSummary(buildSummary(contract, draft));
        log.info("[TailDealAI] draftReady goodsPlan={}, adsenseStock={}",
                draft.getCommunityGoodsPlan().get("action"),
                draft.getAdsensePlan() == null ? null : draft.getAdsensePlan().get("nxCgAdsenseStockQuantity"));
        return draft;
    }

    private Map<String, Object> buildAdsensePlan(TailDealAdsenseSemanticContract c,
                                                  TailDealAdsenseTimeParser.ParsedTime start,
                                                  TailDealAdsenseTimeParser.ParsedTime end) {
        Map<String, Object> plan = new HashMap<>();
        plan.put("action", "ENABLE_ADSENSE");
        plan.put("nxCgIsOpenAdsense", 1);
        plan.put("nxCgAdsenseStartTime", start.getHhmm());
        plan.put("nxCgAdsenseStopTime", end.getHhmm());
        if (c.getTotalStock() != null && c.getTotalStock() > 0) {
            plan.put("nxCgAdsenseStockQuantity", c.getTotalStock());
            plan.put("nxCgAdsenseRestQuantity", c.getTotalStock());
        }
        plan.put("nxCgGoodsPrice", String.valueOf(c.getDealPrice().intValue() == c.getDealPrice()
                ? c.getDealPrice().intValue() : c.getDealPrice()));
        plan.put("nxCgPromotionPrice", plan.get("nxCgGoodsPrice"));
        if (c.getOriginalPrice() != null) {
            plan.put("nxCgGoodsHuaxianPrice", String.valueOf(c.getOriginalPrice().intValue()));
        }
        plan.put("homepagePromotion", c.getHomepagePromotion());
        return plan;
    }

    private String formatSpec(TailDealAdsenseSemanticContract c) {
        String spec = c.getGoodsSpec() != null ? c.getGoodsSpec().trim() : "";
        String unit = c.getUnit() != null ? c.getUnit().trim() : "";
        if (!spec.isEmpty() && !unit.isEmpty() && !spec.contains(unit)) {
            return spec + "/" + unit;
        }
        if (!spec.isEmpty()) {
            return spec;
        }
        return unit.isEmpty() ? "—" : unit;
    }

    private Map<String, Object> buildDeal(TailDealAdsenseSemanticContract c,
                                           TailDealAdsenseTimeParser.ParsedTime start,
                                           TailDealAdsenseTimeParser.ParsedTime end,
                                           String rawText) {
        Map<String, Object> deal = new HashMap<>();
        deal.put("dealPrice", c.getDealPrice());
        deal.put("originalPrice", c.getOriginalPrice());
        deal.put("goodsSpec", formatSpec(c));
        deal.put("startTime", TailDealAdsenseTimeParser.formatDateTime(start.getDateTime()));
        deal.put("endTime", TailDealAdsenseTimeParser.formatDateTime(end.getDateTime()));
        deal.put("startTimeText", "今日" + start.getHhmm());
        deal.put("endTimeText", "今日" + end.getHhmm());
        if (c.getEndTimeText() != null && !c.getEndTimeText().trim().isEmpty()) {
            deal.put("deadlineText", "今日" + end.getHhmm() + "结束");
        }
        if (c.getTotalStock() != null && c.getTotalStock() > 0) {
            deal.put("totalStock", c.getTotalStock());
            String stockUnit = (c.getUnit() != null && !c.getUnit().isEmpty()) ? c.getUnit() : "箱";
            deal.put("stockText", c.getTotalStock() + stockUnit);
        }
        applyWeightToDeal(deal, weightResolver.resolve(c, rawText));
        return deal;
    }

    private void applyWeightToDeal(Map<String, Object> deal, TailDealAdsenseWeightResolver.WeightPack pack) {
        if (pack == null || !pack.hasAny()) {
            return;
        }
        if (pack.getGrossWeight() != null) {
            deal.put("grossWeight", pack.getGrossWeight());
        }
        if (pack.getGrossPrice() != null) {
            deal.put("grossPrice", pack.getGrossPrice());
        }
        if (pack.getNetWeight() != null) {
            deal.put("netWeight", pack.getNetWeight());
        }
        if (pack.getNetPrice() != null) {
            deal.put("netPrice", pack.getNetPrice());
        }
        deal.put("weightMode", pack.getMode());
    }

    private Map<String, Object> pendingPlan() {
        Map<String, Object> plan = new HashMap<>();
        plan.put("action", "PENDING");
        plan.put("createNewGoodsRequired", null);
        return plan;
    }

    private Map<String, Object> choicePlan() {
        Map<String, Object> plan = new HashMap<>();
        plan.put("action", "PENDING");
        plan.put("createNewGoodsRequired", false);
        return plan;
    }

    private Map<String, Object> matchPlan(Map<String, Object> top) {
        Map<String, Object> plan = new HashMap<>();
        plan.put("action", "MATCH_EXISTING");
        plan.put("matchedGoodsId", top.get("goodsId"));
        plan.put("matchedGoodsName", top.get("goodsName"));
        plan.put("confidence", top.get("confidence"));
        plan.put("createNewGoodsRequired", false);
        plan.put("suggestedGoodsType", 0);
        return plan;
    }

    private Map<String, Object> createNewPlan(TailDealAdsenseSemanticContract c, ParseContext ctx) {
        Map<String, Object> proposed = new HashMap<>();
        proposed.put("nxCgGoodsName", c.getGoodsName());
        proposed.put("nxCgGoodsStandardname", c.getUnit() != null && !c.getUnit().isEmpty() ? c.getUnit() : "件");
        proposed.put("nxCgGoodsPrice", formatGoodsPrice(c.getDealPrice()));
        proposed.put("nxCgGoodsType", 0);
        proposed.put("nxCgCommunityId", ctx.getCommunityId());
        proposed.put("nxCgCommerceId", ctx.getCommerceId());
        proposed.put("nxCgGoodsDetail", joinDetail(c));
        if (c.getGoodsSpec() != null && !c.getGoodsSpec().isEmpty()) {
            proposed.put("nxCgGoodsStandardWeight", c.getGoodsSpec());
        }
        weightResolver.applyToMap(weightResolver.resolve(c, ctx.getRawText()),
                (key, value) -> proposed.put(key, value));

        Integer fatherId = ctx.getSelectedFatherGoodsId() != null
                ? ctx.getSelectedFatherGoodsId()
                : TailDealAdsenseDefaults.DEFAULT_FATHER_GOODS_ID;
        proposed.put("nxCgCfgGoodsFatherId", fatherId);

        Map<String, Object> plan = new HashMap<>();
        plan.put("action", "CREATE_NEW");
        plan.put("matchedGoodsId", null);
        plan.put("createNewGoodsRequired", true);
        plan.put("proposedGoods", proposed);
        plan.put("suggestedGoodsType", 0);
        return plan;
    }

    private String formatGoodsPrice(Double dealPrice) {
        if (dealPrice == null) {
            return "0";
        }
        if (dealPrice == Math.floor(dealPrice)) {
            return String.valueOf(dealPrice.intValue());
        }
        return String.valueOf(dealPrice);
    }

    private String formatMissingFields(List<String> missing) {
        Map<String, String> labels = new HashMap<>();
        labels.put("goodsName", "商品名称");
        labels.put("goodsSpec", "规格");
        labels.put("dealPrice", "价格");
        labels.put("endTimeText", "截止时间");
        labels.put("nxCgCfgGoodsFatherId", "商品分类");
        List<String> out = new ArrayList<>();
        for (String field : missing) {
            out.add(labels.getOrDefault(field, field));
        }
        return String.join("、", out);
    }

    private String joinDetail(TailDealAdsenseSemanticContract c) {
        StringBuilder sb = new StringBuilder();
        if (c.getQualityNote() != null && !c.getQualityNote().isEmpty()) {
            sb.append(c.getQualityNote());
        }
        if (c.getAfterSaleNote() != null && !c.getAfterSaleNote().isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(c.getAfterSaleNote());
        }
        return sb.toString();
    }

    private String buildSummary(TailDealAdsenseSemanticContract c, TailDealAdsenseDraft draft) {
        String name = c.getGoodsName();
        if (draft.getMatchedGoods() != null && draft.getMatchedGoods().get("goodsName") != null) {
            name = draft.getMatchedGoods().get("goodsName").toString();
        }
        StringBuilder sb = new StringBuilder("已生成尾货广告商品草稿：");
        sb.append(name).append("，规格").append(formatSpec(c));
        sb.append("，").append(c.getDealPrice()).append("元");
        Object deadlineText = draft.getDeal().get("deadlineText");
        if (deadlineText != null) {
            sb.append("，").append(deadlineText);
        }
        sb.append("。");
        return sb.toString();
    }

    private void enrichContractFromRawText(TailDealAdsenseSemanticContract c, String rawText) {
        if (rawText == null || rawText.trim().isEmpty() || c == null) {
            return;
        }
        if (c.getTotalStock() == null) {
            Integer stock = extractTotalStock(rawText);
            if (stock != null) {
                c.setTotalStock(stock);
            }
        }
        if ((c.getUnit() == null || c.getUnit().isEmpty()) && c.getTotalStock() != null) {
            String unit = extractStockUnit(rawText);
            if (unit != null) {
                c.setUnit(unit);
            }
        }
    }

    private Integer extractTotalStock(String rawText) {
        Pattern[] patterns = new Pattern[]{
                Pattern.compile("一共有\\s*(\\d+)"),
                Pattern.compile("共\\s*(\\d+)"),
                Pattern.compile("(\\d+)\\s*箱"),
                Pattern.compile("(\\d+)\\s*件")
        };
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(rawText);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return null;
    }

    private String extractStockUnit(String rawText) {
        Matcher matcher = Pattern.compile("(\\d+)\\s*([箱件斤个])").matcher(rawText);
        if (matcher.find()) {
            return matcher.group(2);
        }
        if (rawText.contains("箱")) {
            return "箱";
        }
        if (rawText.contains("件")) {
            return "件";
        }
        return null;
    }

    @lombok.Getter
    @lombok.Setter
    public static class ParseContext {
        private String rawText;
        private Integer operatorUserId;
        private Integer communityId;
        private Integer commerceId;
        private String defaultMarketCloseTime;
        private Integer targetWecomGroupId;
        private Integer targetYgtCampaignId;
        private Boolean publishToWecomGroup;
        private Boolean homepagePromotion;
        private Integer selectedFatherGoodsId;
    }
}

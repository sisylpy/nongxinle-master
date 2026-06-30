package com.nongxinle.community.marketing.ai.taildeal.service;

import com.nongxinle.community.marketing.ai.taildeal.adapter.CommunityGoodsMatchAdapter;
import com.nongxinle.community.marketing.ai.taildeal.client.CommunityGoodsLlmClient;
import com.nongxinle.community.marketing.ai.taildeal.contract.TailDealAdsenseSemanticContract;
import com.nongxinle.community.marketing.ai.taildeal.contract.TailDealAdsenseSemanticContractParser;
import com.nongxinle.community.marketing.ai.taildeal.draft.TailDealAdsenseDraft;
import com.nongxinle.community.marketing.ai.taildeal.draft.TailDealAdsenseDraftBuilder;
import com.nongxinle.community.marketing.ai.taildeal.gate.TailDealAdsenseConfirmSafetyGate;
import com.nongxinle.community.marketing.ai.taildeal.render.TailDealAdsenseDraftRenderer;
import com.nongxinle.community.marketing.ai.taildeal.session.TailDealAdsenseDraftSessionStore;
import com.nongxinle.community.marketing.ai.taildeal.validate.TailDealAdsenseDeterministicValidator;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TailDealAdsenseParseService {

    private static final Logger log = LoggerFactory.getLogger(TailDealAdsenseParseService.class);

    @Autowired
    private CommunityGoodsLlmClient llmClient;

    @Autowired
    private TailDealAdsenseSemanticContractParser contractParser;

    @Autowired
    private TailDealAdsenseDraftBuilder draftBuilder;

    @Autowired
    private TailDealAdsenseDraftSessionStore sessionStore;

    @Autowired
    private TailDealAdsenseDraftRenderer renderer;

    @Autowired
    private TailDealAdsenseDeterministicValidator validator;

    @Autowired
    private TailDealAdsenseConfirmSafetyGate safetyGate;

    @Autowired
    private TailDealAdsensePublishService publishService;

    public Map<String, Object> parseTailDealDraft(ParseRequest req) throws Exception {
        if (req.getRawText() == null || req.getRawText().trim().isEmpty()) {
            throw new IllegalArgumentException("rawText required");
        }
        if (req.getCommunityId() == null) {
            throw new IllegalArgumentException("communityId required");
        }

        JSONObject llmJson = llmClient.extractTailDealContract(req.getRawText(), req.getDefaultMarketCloseTime());
        log.info("[TailDealAI] llmContract goodsName={}, goodsSpec={}, dealPrice={}, endTimeText={}",
                llmJson.optString("goodsName"), llmJson.optString("goodsSpec"), llmJson.opt("dealPrice"),
                llmJson.optString("endTimeText"));

        TailDealAdsenseSemanticContract contract = contractParser.parse(llmJson);
        log.info("[TailDealAI] parsedContract goodsName={}, goodsSpec={}, unit={}, dealPrice={}, endTimeText={}",
                contract.getGoodsName(), contract.getGoodsSpec(), contract.getUnit(),
                contract.getDealPrice(), contract.getEndTimeText());

        TailDealAdsenseDraftBuilder.ParseContext ctx = new TailDealAdsenseDraftBuilder.ParseContext();
        ctx.setRawText(req.getRawText());
        ctx.setOperatorUserId(req.getOperatorUserId());
        ctx.setCommunityId(req.getCommunityId());
        ctx.setCommerceId(req.getCommerceId());
        ctx.setDefaultMarketCloseTime(req.getDefaultMarketCloseTime());
        ctx.setTargetWecomGroupId(req.getTargetWecomGroupId());
        ctx.setTargetYgtCampaignId(req.getTargetYgtCampaignId());
        ctx.setPublishToWecomGroup(req.getPublishToWecomGroup());
        ctx.setHomepagePromotion(req.getHomepagePromotion());

        TailDealAdsenseDraft draft = draftBuilder.build(contract, ctx);
        sessionStore.save(draft);
        log.info("[TailDealAI] draftBuilt draftId={}, flowState={}, missingFields={}, goodsPlan={}",
                draft.getDraftId(), draft.getFlowState(), draft.getMissingFields(),
                draft.getCommunityGoodsPlan() == null ? null : draft.getCommunityGoodsPlan().get("action"));
        return renderer.toResponseMap(draft);
    }

    public Map<String, Object> confirmTailDeal(ConfirmRequest req) {
        log.info("[TailDealAI] confirmStart draftId={}, action={}, confirmText={}",
                req.getDraftId(), req.getConfirmAction(), req.getConfirmText());
        TailDealAdsenseDraft draft = sessionStore.get(req.getDraftId());
        if (draft == null) {
            log.warn("[TailDealAI] confirmFail draftNotFound draftId={}", req.getDraftId());
            throw new IllegalArgumentException("draftId invalid or expired");
        }
        log.info("[TailDealAI] confirmDraftLoaded draftId={}, flowState={}, published={}",
                draft.getDraftId(), draft.getFlowState(), draft.isPublished());
        if (draft.isPublished()) {
            throw new IllegalStateException("draft already published");
        }
        if (!"DRAFT_READY".equals(draft.getFlowState())) {
            if (!("GOODS_CHOICE".equals(draft.getFlowState()) && req.getSelectedGoodsId() != null)) {
                throw new IllegalStateException("draft not ready for confirm: " + draft.getFlowState());
            }
        }

        TailDealAdsenseConfirmSafetyGate.GateResult gate = safetyGate.validate(
                req.getConfirmAction(), req.getConfirmText());
        if (!gate.isAllowed()) {
            log.warn("[TailDealAI] confirmGateRejected action={}, text={}, reason={}",
                    req.getConfirmAction(), req.getConfirmText(), gate.getMessage());
            Map<String, Object> err = new HashMap<>();
            err.put("flowState", "CONFIRM_REJECTED");
            err.put("allowedConfirmActions", Arrays.asList(
                    TailDealAdsenseConfirmSafetyGate.CONFIRM_PUBLISH,
                    TailDealAdsenseConfirmSafetyGate.CONFIRM_SAVE,
                    TailDealAdsenseConfirmSafetyGate.CONFIRM_SAVE_ADSENSE));
            throw new ConfirmRejectedException(gate.getMessage(), err);
        }

        if (req.getSelectedGoodsId() != null) {
            Map<String, Object> plan = new HashMap<>();
            plan.put("action", "MATCH_EXISTING");
            plan.put("matchedGoodsId", req.getSelectedGoodsId());
            plan.put("createNewGoodsRequired", false);
            draft.setCommunityGoodsPlan(plan);
            draft.setFlowState("DRAFT_READY");
        }
        if (req.getSelectedFatherGoodsId() != null) {
            Map<String, Object> plan = draft.getCommunityGoodsPlan();
            if (plan != null && plan.get("proposedGoods") instanceof Map) {
                ((Map<String, Object>) plan.get("proposedGoods")).put(
                        "nxCgCfgGoodsFatherId", req.getSelectedFatherGoodsId());
            }
        }

        List<String> errors = validator.validateForConfirm(draft, gate.getNormalizedAction());
        if (!errors.isEmpty()) {
            log.warn("[TailDealAI] confirmValidationFailed draftId={}, errors={}", req.getDraftId(), errors);
            if (errors.contains("endTimeExpired")) {
                throw new IllegalStateException("截止时间已过，请重新生成草稿或修改截止时间后再发布");
            }
            if (errors.contains("nxCgCfgGoodsFatherId")) {
                throw new IllegalStateException("创建新商品需要先选择商品分类");
            }
            throw new IllegalStateException("validation failed: " + String.join(",", errors));
        }

        log.info("[TailDealAI] confirmPublishStart draftId={}, normalizedAction={}, goodsPlan={}",
                req.getDraftId(), gate.getNormalizedAction(),
                draft.getCommunityGoodsPlan() == null ? null : draft.getCommunityGoodsPlan().get("action"));
        Map<String, Object> result = publishService.publish(
                draft,
                gate.getNormalizedAction(),
                req.getSelectedGoodsId(),
                req.getSelectedFatherGoodsId(),
                req.getOverride());

        sessionStore.markPublished(draft,
                (Integer) result.get("goodsId"),
                (Integer) result.get("adsenseId"));
        log.info("[TailDealAI] confirmDone draftId={}, goodsId={}, adsenseId={}, nxCaStatus={}",
                req.getDraftId(), result.get("goodsId"), result.get("adsenseId"), result.get("nxCaStatus"));
        return result;
    }

    @lombok.Getter
    @lombok.Setter
    public static class ParseRequest {
        private String rawText;
        private Integer operatorUserId;
        private Integer communityId;
        private Integer commerceId;
        private String scene;
        private String defaultMarketCloseTime;
        private Integer targetWecomGroupId;
        private Integer targetYgtCampaignId;
        private Boolean publishToWecomGroup;
        private Boolean homepagePromotion;
    }

    @lombok.Getter
    @lombok.Setter
    public static class ConfirmRequest {
        private String draftId;
        private Integer operatorUserId;
        private String confirmAction;
        private String confirmText;
        private Integer selectedGoodsId;
        private Integer selectedFatherGoodsId;
        private Map<String, Object> override;
    }

    public static class ConfirmRejectedException extends RuntimeException {
        private final Map<String, Object> data;

        public ConfirmRejectedException(String message, Map<String, Object> data) {
            super(message);
            this.data = data;
        }

        public Map<String, Object> getData() {
            return data;
        }
    }
}

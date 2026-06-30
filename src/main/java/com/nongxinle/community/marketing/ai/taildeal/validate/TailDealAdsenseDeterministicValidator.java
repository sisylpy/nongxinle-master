package com.nongxinle.community.marketing.ai.taildeal.validate;

import com.nongxinle.community.marketing.ai.taildeal.contract.TailDealAdsenseSemanticContract;
import com.nongxinle.community.marketing.ai.taildeal.draft.TailDealAdsenseDraft;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TailDealAdsenseDeterministicValidator {

    private static final Logger log = LoggerFactory.getLogger(TailDealAdsenseDeterministicValidator.class);

    private final TailDealAdsenseTimeParser timeParser = new TailDealAdsenseTimeParser();

    public List<String> validateForDraft(TailDealAdsenseSemanticContract contract, String defaultMarketCloseTime) {
        List<String> missing = new ArrayList<>();
        if (contract.getGoodsName() == null || contract.getGoodsName().trim().isEmpty()) {
            missing.add("goodsName");
        }
        boolean hasSpec = (contract.getGoodsSpec() != null && !contract.getGoodsSpec().trim().isEmpty())
                || (contract.getUnit() != null && !contract.getUnit().trim().isEmpty());
        if (!hasSpec) {
            missing.add("goodsSpec");
        }
        if (contract.getDealPrice() == null || contract.getDealPrice() <= 0) {
            missing.add("dealPrice");
        }
        if (contract.getEndTimeText() != null && !contract.getEndTimeText().trim().isEmpty()) {
            TailDealAdsenseTimeParser.ParsedTime end = timeParser.parseEndTime(
                    contract.getEndTimeText(), defaultMarketCloseTime);
            if (end == null) {
                missing.add("endTimeText");
            }
        }
        if (!missing.isEmpty()) {
            log.info("[TailDealAI] validateDraftMissing fields={}", missing);
        }
        return missing;
    }

    public List<String> validateForConfirm(TailDealAdsenseDraft draft, String normalizedConfirmAction) {
        List<String> errors = new ArrayList<>();
        errors.addAll(validateForDraft(draft.getSemanticContract(), draft.getDefaultMarketCloseTime()));

        Map<String, Object> plan = draft.getCommunityGoodsPlan();
        String action = plan == null ? null : (String) plan.get("action");
        if ("CREATE_NEW".equals(action)) {
            Map<String, Object> proposed = (Map<String, Object>) plan.get("proposedGoods");
            if (proposed == null || proposed.get("nxCgCfgGoodsFatherId") == null) {
                errors.add("nxCgCfgGoodsFatherId");
            }
        } else if ("GOODS_CHOICE".equals(draft.getFlowState())) {
            errors.add("selectedGoodsId");
        }

        if (com.nongxinle.community.marketing.ai.taildeal.gate.TailDealAdsenseConfirmSafetyGate
                .isPublishAction(normalizedConfirmAction)) {
            if (timeParser.isEndTimeExpired(
                    draft.getSemanticContract().getEndTimeText(), draft.getDefaultMarketCloseTime())) {
                errors.add("endTimeExpired");
            }
        }
        if (!errors.isEmpty()) {
            log.info("[TailDealAI] validateConfirmErrors action={}, errors={}", normalizedConfirmAction, errors);
        }
        return errors;
    }
}

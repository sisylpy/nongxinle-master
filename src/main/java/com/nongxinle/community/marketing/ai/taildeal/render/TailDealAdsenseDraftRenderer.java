package com.nongxinle.community.marketing.ai.taildeal.render;

import com.nongxinle.community.marketing.ai.taildeal.contract.TailDealAdsenseSemanticContract;
import com.nongxinle.community.marketing.ai.taildeal.draft.TailDealAdsenseDraft;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TailDealAdsenseDraftRenderer {

    public Map<String, Object> toResponseMap(TailDealAdsenseDraft draft) {
        Map<String, Object> data = new HashMap<>();
        data.put("draftId", draft.getDraftId());
        data.put("status", draft.getStatus());
        data.put("flowState", draft.getFlowState());
        data.put("confirmRequired", draft.isConfirmRequired());
        data.put("semanticContract", contractToMap(draft.getSemanticContract()));
        data.put("communityGoodsPlan", draft.getCommunityGoodsPlan());
        data.put("adsensePlan", draft.getAdsensePlan());
        data.put("deal", draft.getDeal());
        data.put("matchedGoods", draft.getMatchedGoods());
        data.put("goodsCandidates", draft.getGoodsCandidates());
        data.put("missingFields", draft.getMissingFields());
        data.put("riskWarnings", draft.getRiskWarnings());
        data.put("userFacingSummary", draft.getUserFacingSummary());
        return data;
    }

    private Map<String, Object> contractToMap(TailDealAdsenseSemanticContract c) {
        if (c == null) {
            return null;
        }
        Map<String, Object> m = new HashMap<>();
        m.put("intent", c.getIntent());
        m.put("goodsName", c.getGoodsName());
        m.put("goodsSpec", c.getGoodsSpec());
        m.put("unit", c.getUnit());
        m.put("dealPrice", c.getDealPrice());
        m.put("originalPrice", c.getOriginalPrice());
        m.put("totalStock", c.getTotalStock());
        m.put("grossWeight", c.getGrossWeight());
        m.put("netWeight", c.getNetWeight());
        m.put("startTimeText", c.getStartTimeText());
        m.put("endTimeText", c.getEndTimeText());
        m.put("homepagePromotion", c.getHomepagePromotion());
        m.put("qualityNote", c.getQualityNote());
        m.put("afterSaleNote", c.getAfterSaleNote());
        return m;
    }
}

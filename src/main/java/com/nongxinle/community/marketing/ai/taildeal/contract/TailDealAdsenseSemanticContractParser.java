package com.nongxinle.community.marketing.ai.taildeal.contract;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class TailDealAdsenseSemanticContractParser {

    public TailDealAdsenseSemanticContract parse(JSONObject json) {
        TailDealAdsenseSemanticContract c = new TailDealAdsenseSemanticContract();
        c.setIntent(json.optString("intent", TailDealAdsenseSemanticContract.INTENT));
        c.setGoodsName(trim(json.optString("goodsName")));
        c.setGoodsSpec(trim(json.optString("goodsSpec")));
        c.setUnit(trim(json.optString("unit")));
        c.setDealPrice(optDouble(json, "dealPrice"));
        c.setOriginalPrice(optDouble(json, "originalPrice"));
        c.setTotalStock(optInteger(json, "totalStock"));
        c.setMinOrderQty(optInteger(json, "minOrderQty"));
        c.setOrderMultiple(optInteger(json, "orderMultiple"));
        c.setLimitPerCustomer(optInteger(json, "limitPerCustomer"));
        c.setGrossWeight(optDouble(json, "grossWeight"));
        c.setNetWeight(optDouble(json, "netWeight"));
        c.setStartTimeText(trim(json.optString("startTimeText")));
        c.setEndTimeText(trim(json.optString("endTimeText")));
        c.setHomepagePromotion(optBoolean(json, "homepagePromotion", true));
        c.setPublishToWecomGroup(optBoolean(json, "publishToWecomGroup", false));
        c.setTargetWecomGroupId(optInteger(json, "targetWecomGroupId"));
        c.setTargetYgtCampaignId(optInteger(json, "targetYgtCampaignId"));
        c.setQualityNote(trim(json.optString("qualityNote")));
        c.setAfterSaleNote(trim(json.optString("afterSaleNote")));
        c.setNeedConfirmation(optBoolean(json, "needConfirmation", true));
        return c;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isAbsent(Object val) {
        if (val == null) {
            return true;
        }
        if (val instanceof net.sf.json.JSONNull) {
            return true;
        }
        String str = val.toString().trim();
        return str.isEmpty() || "null".equalsIgnoreCase(str);
    }

    private static Double optDouble(JSONObject json, String key) {
        if (!json.containsKey(key)) {
            return null;
        }
        Object val = json.get(key);
        if (isAbsent(val)) {
            return null;
        }
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        String str = val.toString().trim();
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer optInteger(JSONObject json, String key) {
        if (!json.containsKey(key)) {
            return null;
        }
        Object val = json.get(key);
        if (isAbsent(val)) {
            return null;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        String str = val.toString().trim();
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean optBoolean(JSONObject json, String key, Boolean defaultValue) {
        if (!json.containsKey(key) || isAbsent(json.get(key))) {
            return defaultValue;
        }
        try {
            return json.getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

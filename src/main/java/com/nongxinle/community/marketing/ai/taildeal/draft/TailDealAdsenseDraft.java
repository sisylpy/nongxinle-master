package com.nongxinle.community.marketing.ai.taildeal.draft;

import com.nongxinle.community.marketing.ai.taildeal.contract.TailDealAdsenseSemanticContract;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class TailDealAdsenseDraft {

    private String draftId;
    private String status;
    private String flowState;
    private boolean confirmRequired;
    private long createdAtMs;
    private long expiresAtMs;

    private Integer operatorUserId;
    private Integer communityId;
    private Integer commerceId;
    private String rawText;
    private String defaultMarketCloseTime;
    private Integer targetWecomGroupId;
    private Integer targetYgtCampaignId;
    private Boolean publishToWecomGroup;
    private Boolean homepagePromotion;

    private TailDealAdsenseSemanticContract semanticContract;
    private Map<String, Object> communityGoodsPlan = new HashMap<>();
    private Map<String, Object> adsensePlan = new HashMap<>();
    private Map<String, Object> deal = new HashMap<>();
    private Map<String, Object> matchedGoods = new HashMap<>();
    private List<Map<String, Object>> goodsCandidates = new ArrayList<>();
    private List<String> missingFields = new ArrayList<>();
    private List<String> riskWarnings = new ArrayList<>();
    private String userFacingSummary;

    private boolean published;
    private Integer publishedGoodsId;
    private Integer publishedAdsenseId;
}

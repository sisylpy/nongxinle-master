package com.nongxinle.community.marketing.ai.taildeal.contract;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TailDealAdsenseSemanticContract {

    public static final String INTENT = "CREATE_ADSENSE_TAIL_DEAL_GOODS";

    private String intent = INTENT;
    private String goodsName;
    private String goodsSpec;
    private String unit;
    private Double dealPrice;
    private Double originalPrice;
    private Integer totalStock;
    private Integer minOrderQty;
    private Integer orderMultiple;
    private Integer limitPerCustomer;
    /** 毛重（斤） */
    private Double grossWeight;
    /** 净重（斤） */
    private Double netWeight;
    private String startTimeText;
    private String endTimeText;
    private Boolean homepagePromotion = true;
    private Boolean publishToWecomGroup = false;
    private Integer targetWecomGroupId;
    private Integer targetYgtCampaignId;
    private String qualityNote;
    private String afterSaleNote;
    private Boolean needConfirmation = true;
}

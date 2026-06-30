package com.nongxinle.community.promotion.controller;

import com.nongxinle.entity.NxCustomerPromotionCampaignEntity;
import com.nongxinle.community.promotion.service.NxCustomerPromotionCampaignService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/nxcustomerpromotioncampaign")
public class NxCustomerPromotionCampaignController {

    @Autowired
    private NxCustomerPromotionCampaignService nxCustomerPromotionCampaignService;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public R save(@RequestBody NxCustomerPromotionCampaignEntity entity) {
        try {
            return R.ok().put("data", nxCustomerPromotionCampaignService.createCampaign(entity));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        }
    }

    @RequestMapping(value = "/info", method = RequestMethod.POST)
    @ResponseBody
    public R info(Integer campaignId) {
        if (campaignId == null) {
            return R.error(-1, "campaignId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCampaignService.queryObject(campaignId));
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public R list(Integer communityId, Integer commerceId, String ownerScope, String campaignStatus,
                  Integer offset, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("communityId", communityId);
        map.put("commerceId", commerceId);
        map.put("ownerScope", ownerScope);
        map.put("campaignStatus", campaignStatus);
        map.put("offset", offset);
        map.put("limit", limit);
        return R.ok().put("data", nxCustomerPromotionCampaignService.queryList(map));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public R update(Integer campaignId, String campaignName,
                    @DateTimeFormat(pattern = "yyyy-MM-dd") Date effectiveStartAt,
                    @DateTimeFormat(pattern = "yyyy-MM-dd") Date effectiveEndAt) {
        if (campaignId == null) {
            return R.error(-1, "campaignId不能为空");
        }
        try {
            return R.ok().put("data", nxCustomerPromotionCampaignService.updateCampaignEditableFields(
                    campaignId, campaignName, effectiveStartAt, effectiveEndAt));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return R.error(-1, e.getMessage());
        }
    }

    @RequestMapping(value = "/activate", method = RequestMethod.POST)
    @ResponseBody
    public R activate(Integer campaignId) {
        if (campaignId == null) {
            return R.error(-1, "campaignId不能为空");
        }
        try {
            return R.ok().put("data", nxCustomerPromotionCampaignService.activate(campaignId));
        } catch (IllegalStateException e) {
            return R.error(-1, e.getMessage());
        }
    }

    @RequestMapping(value = "/suspend", method = RequestMethod.POST)
    @ResponseBody
    public R suspend(Integer campaignId, String reason) {
        if (campaignId == null) {
            return R.error(-1, "campaignId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCampaignService.suspend(campaignId, reason));
    }

    @RequestMapping(value = "/terminate", method = RequestMethod.POST)
    @ResponseBody
    public R terminate(Integer campaignId, String reason) {
        if (campaignId == null) {
            return R.error(-1, "campaignId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCampaignService.terminate(campaignId, reason));
    }

    @RequestMapping(value = "/stats", method = RequestMethod.POST)
    @ResponseBody
    public R stats(Integer campaignId) {
        if (campaignId == null) {
            return R.error(-1, "campaignId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCampaignService.queryCampaignStats(campaignId));
    }

    @RequestMapping(value = "/statsByOwner", method = RequestMethod.POST)
    @ResponseBody
    public R statsByOwner(Integer campaignId) {
        if (campaignId == null) {
            return R.error(-1, "campaignId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCampaignService.statsByOwner(campaignId));
    }

    @RequestMapping(value = "/referralDetails", method = RequestMethod.POST)
    @ResponseBody
    public R referralDetails(Integer campaignId, String sourceOwnerType, Integer sourceOwnerId,
                             Integer offset, Integer limit) {
        if (campaignId == null) {
            return R.error(-1, "campaignId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCampaignService.queryReferralDetails(
                campaignId, sourceOwnerType, sourceOwnerId, offset, limit));
    }
}

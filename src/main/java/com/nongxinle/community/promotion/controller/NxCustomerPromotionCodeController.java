package com.nongxinle.community.promotion.controller;

import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.community.promotion.service.NxCustomerPromotionCodeService;
import com.nongxinle.community.customer.service.NxCustomerUserService;
import com.nongxinle.utils.CustomerReferralConstants;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/nxcustomerpromotioncode")
public class NxCustomerPromotionCodeController {

    @Autowired
    private NxCustomerPromotionCodeService nxCustomerPromotionCodeService;
    @Autowired
    private NxCustomerUserService nxCustomerUserService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public R create(String ownerType, Integer ownerId, Integer commerceId, Integer communityId,
                    @DateTimeFormat(pattern = "yyyy-MM-dd") Date validStartAt,
                    @DateTimeFormat(pattern = "yyyy-MM-dd") Date validEndAt,
                    Integer rewardRuleId) {
        if (ownerType == null || ownerId == null || commerceId == null || communityId == null) {
            return R.error(-1, "ownerType、ownerId、commerceId、communityId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCodeService.createCode(
                ownerType, ownerId, commerceId, communityId, validStartAt, validEndAt, rewardRuleId));
    }

    @RequestMapping(value = "/createForCustomerUser", method = RequestMethod.POST)
    @ResponseBody
    public R createForCustomerUser(Integer userId) {
        if (userId == null) {
            return R.error(-1, "userId不能为空");
        }
        NxCustomerUserEntity user = nxCustomerUserService.queryObject(userId);
        if (user == null) {
            return R.error(-1, "用户不存在");
        }
        return R.ok().put("data", nxCustomerPromotionCodeService.getOrCreateForCustomerUser(user));
    }

    @RequestMapping(value = "/createForCommunityUser", method = RequestMethod.POST)
    @ResponseBody
    public R createForCommunityUser(Integer communityUserId) {
        if (communityUserId == null) {
            return R.error(-1, "communityUserId不能为空");
        }
        try {
            return R.ok().put("data", nxCustomerPromotionCodeService.getOrCreateForCommunityUser(communityUserId));
        } catch (IllegalArgumentException e) {
            return R.error(-1, e.getMessage());
        }
    }

    @RequestMapping(value = "/activeByOwner", method = RequestMethod.POST)
    @ResponseBody
    public R activeByOwner(String ownerType, Integer ownerId) {
        if (ownerType == null || ownerId == null) {
            return R.error(-1, "ownerType和ownerId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCodeService.queryActiveByOwner(ownerType, ownerId));
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public R list(String ownerType, Integer ownerId, Integer communityId, Integer commerceId,
                    String codeStatus, String promotionCode, String ownerName,
                    Integer offset, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("ownerType", ownerType);
        map.put("ownerId", ownerId);
        map.put("communityId", communityId);
        map.put("commerceId", commerceId);
        map.put("codeStatus", codeStatus);
        map.put("promotionCode", promotionCode);
        map.put("ownerName", ownerName);
        map.put("offset", offset);
        map.put("limit", limit);
        return R.ok().put("data", nxCustomerPromotionCodeService.queryList(map));
    }

    @RequestMapping(value = "/info", method = RequestMethod.POST)
    @ResponseBody
    public R info(Integer promotionCodeId) {
        if (promotionCodeId == null) {
            return R.error(-1, "promotionCodeId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCodeService.queryObject(promotionCodeId));
    }

    @RequestMapping(value = "/activate", method = RequestMethod.POST)
    @ResponseBody
    public R activate(Integer promotionCodeId) {
        if (promotionCodeId == null) {
            return R.error(-1, "promotionCodeId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCodeService.updateCodeStatus(
                promotionCodeId, CustomerReferralConstants.CODE_STATUS_ACTIVE, null));
    }

    @RequestMapping(value = "/suspend", method = RequestMethod.POST)
    @ResponseBody
    public R suspend(Integer promotionCodeId, String reason) {
        if (promotionCodeId == null) {
            return R.error(-1, "promotionCodeId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCodeService.updateCodeStatus(
                promotionCodeId, CustomerReferralConstants.CODE_STATUS_SUSPENDED, reason));
    }

    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    @ResponseBody
    public R disable(Integer promotionCodeId, String reason) {
        if (promotionCodeId == null) {
            return R.error(-1, "promotionCodeId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCodeService.updateCodeStatus(
                promotionCodeId, CustomerReferralConstants.CODE_STATUS_DISABLED, reason));
    }

    @RequestMapping(value = "/updateValidity", method = RequestMethod.POST)
    @ResponseBody
    public R updateValidity(Integer promotionCodeId,
                            @DateTimeFormat(pattern = "yyyy-MM-dd") Date validStartAt,
                            @DateTimeFormat(pattern = "yyyy-MM-dd") Date validEndAt) {
        if (promotionCodeId == null) {
            return R.error(-1, "promotionCodeId不能为空");
        }
        return R.ok().put("data", nxCustomerPromotionCodeService.updateCodeValidity(
                promotionCodeId, validStartAt, validEndAt));
    }

    @RequestMapping(value = "/regenerate", method = RequestMethod.POST)
    @ResponseBody
    public R regenerate(Integer promotionCodeId, String reason) {
        if (promotionCodeId == null) {
            return R.error(-1, "promotionCodeId不能为空");
        }
        try {
            NxCustomerPromotionCodeEntity code = nxCustomerPromotionCodeService.regenerateCode(promotionCodeId, reason);
            return R.ok().put("data", code);
        } catch (IllegalArgumentException e) {
            return R.error(-1, e.getMessage());
        }
    }
}

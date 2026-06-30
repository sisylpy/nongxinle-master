package com.nongxinle.community.promotion.controller;

import com.nongxinle.entity.NxCustomerPromotionCodeEntity;
import com.nongxinle.entity.NxCustomerReferralRewardEntity;
import com.nongxinle.entity.NxCustomerUserCouponEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.community.promotion.service.NxCustomerPromotionCodeService;
import com.nongxinle.community.promotion.service.NxCustomerReferralRewardService;
import com.nongxinle.community.promotion.service.NxCustomerReferralService;
import com.nongxinle.community.customer.service.NxCustomerUserService;
import com.nongxinle.utils.CustomerReferralConstants;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/nxcustomerreferral")
public class NxCustomerReferralController {

    @Autowired
    private NxCustomerReferralService nxCustomerReferralService;
    @Autowired
    private NxCustomerReferralRewardService nxCustomerReferralRewardService;
    @Autowired
    private NxCustomerUserService nxCustomerUserService;
    @Autowired
    private NxCustomerPromotionCodeService nxCustomerPromotionCodeService;

    @RequestMapping(value = "/myReferralOverview", method = RequestMethod.POST)
    @ResponseBody
    public R myReferralOverview(Integer userId, Integer directOffset, Integer directLimit) {
        if (userId == null) {
            return R.error(-1, "userId不能为空");
        }
        return R.ok().put("data", nxCustomerReferralService.queryMyReferralOverview(
                userId, directOffset, directLimit));
    }

    @RequestMapping(value = "/markReferralDynamicsRead", method = RequestMethod.POST)
    @ResponseBody
    public R markReferralDynamicsRead(Integer userId, Integer upToReferralId) {
        if (userId == null || upToReferralId == null) {
            return R.error(-1, "userId和upToReferralId不能为空");
        }
        boolean updated = nxCustomerReferralService.markDynamicsRead(userId, upToReferralId);
        return R.ok().put("data", updated);
    }

    @RequestMapping(value = "/myReferralStats", method = RequestMethod.POST)
    @ResponseBody
    public R myReferralStats(Integer userId) {
        if (userId == null) {
            return R.error(-1, "userId不能为空");
        }
        return R.ok().put("data", nxCustomerReferralService.queryMyReferralStats(userId));
    }

    @RequestMapping(value = "/myDirectReferralList", method = RequestMethod.POST)
    @ResponseBody
    public R myDirectReferralList(Integer userId, Integer offset, Integer limit) {
        if (userId == null) {
            return R.error(-1, "userId不能为空");
        }
        return R.ok().put("data", nxCustomerReferralService.queryDirectReferralList(userId, offset, limit));
    }

    /**
     * 有效推广列表（与 myDirectReferralList 相同，兼容 page/limit/communityId 参数）
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public R list(Integer userId, Integer page, Integer limit, Integer communityId, Integer commId,
                  Integer offset) {
        if (userId == null) {
            return R.error(-1, "userId不能为空");
        }
        int pageSize = limit != null && limit > 0 ? limit : 20;
        int resolvedOffset;
        if (offset != null) {
            resolvedOffset = offset;
        } else {
            int pageNo = page != null && page > 0 ? page : 1;
            resolvedOffset = (pageNo - 1) * pageSize;
        }
        List<Map<String, Object>> list = nxCustomerReferralService.queryDirectReferralList(
                userId, resolvedOffset, pageSize);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        return R.ok().put("data", data);
    }

    @RequestMapping(value = "/myLevel2ReferralList", method = RequestMethod.POST)
    @ResponseBody
    public R myLevel2ReferralList(Integer userId, Integer offset, Integer limit) {
        if (userId == null) {
            return R.error(-1, "userId不能为空");
        }
        return R.ok().put("data", nxCustomerReferralService.queryLevel2ReferralList(userId, offset, limit));
    }

    @RequestMapping(value = "/myReferralRewards", method = RequestMethod.POST)
    @ResponseBody
    public R myReferralRewards(Integer userId, Integer status, Integer offset, Integer limit) {
        if (userId == null) {
            return R.error(-1, "userId不能为空");
        }
        if (status == null) {
            status = CustomerReferralConstants.REWARD_STATUS_PENDING;
        }
        List<NxCustomerReferralRewardEntity> list =
                nxCustomerReferralRewardService.queryRewardList(userId, status, offset, limit);
        return R.ok().put("data", list);
    }

    @RequestMapping(value = "/claimReferralReward", method = RequestMethod.POST)
    @ResponseBody
    public R claimReferralReward(Integer userId, Integer rewardId) {
        if (userId == null || rewardId == null) {
            return R.error(-1, "userId和rewardId不能为空");
        }
        try {
            NxCustomerUserCouponEntity userCoupon =
                    nxCustomerReferralRewardService.claimReward(userId, rewardId);
            Map<String, Object> data = new HashMap<>();
            data.put("userCoupon", userCoupon);
            data.put("rewardId", rewardId);
            return R.ok().put("data", data);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        }
    }

    @RequestMapping(value = "/claimReferralRewards", method = RequestMethod.POST)
    @ResponseBody
    public R claimReferralRewards(Integer userId, String rewardIds, Boolean claimAll) {
        if (userId == null) {
            return R.error(-1, "userId不能为空");
        }
        if (!Boolean.TRUE.equals(claimAll) && (rewardIds == null || rewardIds.trim().isEmpty())) {
            return R.error(-1, "请指定rewardIds或claimAll=true");
        }
        try {
            return R.ok().put("data", nxCustomerReferralRewardService.claimRewardsBatch(userId, rewardIds, claimAll));
        } catch (Exception e) {
            return R.error(-1, e.getMessage());
        }
    }

    @RequestMapping(value = "/myReferralCouponGroups", method = RequestMethod.POST)
    @ResponseBody
    public R myReferralCouponGroups(Integer userId) {
        if (userId == null) {
            return R.error(-1, "userId不能为空");
        }
        return R.ok().put("data", nxCustomerReferralRewardService.queryReferralCouponGroups(userId));
    }

    @RequestMapping(value = "/getMyShareCode", method = RequestMethod.POST)
    @ResponseBody
    public R getMyShareCode(Integer userId) {
        if (userId == null) {
            return R.error(-1, "userId不能为空");
        }
        NxCustomerUserEntity user = nxCustomerUserService.queryObject(userId);
        if (user == null) {
            return R.error(-1, "用户不存在");
        }
        NxCustomerPromotionCodeEntity code = nxCustomerPromotionCodeService.getOrCreateForCustomerUser(user);
        Map<String, Object> data = new HashMap<>();
        data.put("promotionCode", code.getPromotionCode());
        data.put("promotionCodeId", code.getNxCustomerPromotionCodeId());
        data.put("codeStatus", code.getCodeStatus());
        data.put("validStartAt", code.getValidStartAt());
        data.put("validEndAt", code.getValidEndAt());
        data.put("shareCodeParam", CustomerReferralConstants.SHARE_PARAM_PROMOTION_CODE);
        return R.ok().put("data", data);
    }
}

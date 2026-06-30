package com.nongxinle.community.promotion.controller;

import com.nongxinle.dao.NxCustomerUserReferralDao;
import com.nongxinle.entity.NxCommunityUserEntity;
import com.nongxinle.community.promotion.service.NxCommunityUserPromotionEligibleService;
import com.nongxinle.community.customer.service.NxCommunityUserService;
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
@RequestMapping("api/nxcommunityuserpromotion")
public class NxCommunityUserPromotionEligibleController {

    @Autowired
    private NxCommunityUserPromotionEligibleService nxCommunityUserPromotionEligibleService;
    @Autowired
    private NxCommunityUserService nxCommunityUserService;
    @Autowired
    private NxCustomerUserReferralDao nxCustomerUserReferralDao;

    @RequestMapping(value = "/info", method = RequestMethod.POST)
    @ResponseBody
    public R info(Integer communityUserId) {
        if (communityUserId == null) {
            return R.error(-1, "communityUserId不能为空");
        }
        return R.ok().put("data", nxCommunityUserPromotionEligibleService.queryObject(communityUserId));
    }

    @RequestMapping(value = "/enable", method = RequestMethod.POST)
    @ResponseBody
    public R enable(Integer communityUserId,
                    @DateTimeFormat(pattern = "yyyy-MM-dd") Date validStartAt,
                    @DateTimeFormat(pattern = "yyyy-MM-dd") Date validEndAt) {
        if (communityUserId == null) {
            return R.error(-1, "communityUserId不能为空");
        }
        NxCommunityUserEntity user = nxCommunityUserService.queryObject(communityUserId);
        if (user == null) {
            return R.error(-1, "社区工作人员不存在");
        }
        return R.ok().put("data", nxCommunityUserPromotionEligibleService.enable(communityUserId, validStartAt, validEndAt));
    }

    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    @ResponseBody
    public R disable(Integer communityUserId) {
        if (communityUserId == null) {
            return R.error(-1, "communityUserId不能为空");
        }
        return R.ok().put("data", nxCommunityUserPromotionEligibleService.disable(communityUserId));
    }

    @RequestMapping(value = "/stats", method = RequestMethod.POST)
    @ResponseBody
    public R stats(Integer communityUserId) {
        if (communityUserId == null) {
            return R.error(-1, "communityUserId不能为空");
        }
        Map<String, Object> q = new HashMap<>();
        q.put("sourceOwnerType", CustomerReferralConstants.OWNER_TYPE_COMMUNITY_USER);
        q.put("sourceOwnerId", communityUserId);
        return R.ok()
                .put("qualifiedReferralCount", nxCustomerUserReferralDao.countQualifiedBySourceOwner(q))
                .put("totalReferralCount", nxCustomerUserReferralDao.countBySourceOwner(q));
    }

    @RequestMapping(value = "/referralDetails", method = RequestMethod.POST)
    @ResponseBody
    public R referralDetails(Integer communityUserId, Integer offset, Integer limit) {
        if (communityUserId == null) {
            return R.error(-1, "communityUserId不能为空");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("sourceOwnerType", CustomerReferralConstants.OWNER_TYPE_COMMUNITY_USER);
        map.put("sourceOwnerId", communityUserId);
        map.put("offset", offset);
        map.put("limit", limit);
        return R.ok().put("data", nxCustomerUserReferralDao.queryBySourceOwner(map));
    }
}

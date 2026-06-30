package com.nongxinle.community.promotion.controller;

import com.nongxinle.entity.NxCustomerPromoterEntity;
import com.nongxinle.community.promotion.service.NxCustomerPromoterService;
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
@RequestMapping("api/nxcustomerpromoter")
public class NxCustomerPromoterController {

    @Autowired
    private NxCustomerPromoterService nxCustomerPromoterService;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public R save(@RequestBody NxCustomerPromoterEntity entity) {
        try {
            return R.ok().put("data", nxCustomerPromoterService.savePromoter(entity));
        } catch (IllegalArgumentException e) {
            return R.error(-1, e.getMessage());
        }
    }

    @RequestMapping(value = "/info", method = RequestMethod.POST)
    @ResponseBody
    public R info(Integer promoterId) {
        if (promoterId == null) {
            return R.error(-1, "promoterId不能为空");
        }
        return R.ok().put("data", nxCustomerPromoterService.queryObject(promoterId));
    }

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public R list(Integer communityId, Integer commerceId, String promoterStatus, Integer offset, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("communityId", communityId);
        map.put("commerceId", commerceId);
        map.put("promoterStatus", promoterStatus);
        map.put("offset", offset);
        map.put("limit", limit);
        return R.ok().put("data", nxCustomerPromoterService.queryList(map));
    }

    @RequestMapping(value = "/activate", method = RequestMethod.POST)
    @ResponseBody
    public R activate(Integer promoterId) {
        if (promoterId == null) {
            return R.error(-1, "promoterId不能为空");
        }
        return R.ok().put("data", nxCustomerPromoterService.activate(promoterId));
    }

    @RequestMapping(value = "/suspend", method = RequestMethod.POST)
    @ResponseBody
    public R suspend(Integer promoterId, String reason) {
        if (promoterId == null) {
            return R.error(-1, "promoterId不能为空");
        }
        return R.ok().put("data", nxCustomerPromoterService.suspend(promoterId, reason));
    }

    @RequestMapping(value = "/terminate", method = RequestMethod.POST)
    @ResponseBody
    public R terminate(Integer promoterId, String reason) {
        if (promoterId == null) {
            return R.error(-1, "promoterId不能为空");
        }
        return R.ok().put("data", nxCustomerPromoterService.terminate(promoterId, reason));
    }

    @RequestMapping(value = "/updateCooperationPeriod", method = RequestMethod.POST)
    @ResponseBody
    public R updateCooperationPeriod(Integer promoterId,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date cooperationStartAt,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date cooperationEndAt) {
        if (promoterId == null) {
            return R.error(-1, "promoterId不能为空");
        }
        return R.ok().put("data", nxCustomerPromoterService.updateCooperationPeriod(
                promoterId, cooperationStartAt, cooperationEndAt));
    }

    @RequestMapping(value = "/stats", method = RequestMethod.POST)
    @ResponseBody
    public R stats(Integer promoterId) {
        if (promoterId == null) {
            return R.error(-1, "promoterId不能为空");
        }
        return R.ok().put("data", nxCustomerPromoterService.queryPromoterStats(promoterId));
    }

    @RequestMapping(value = "/referralDetails", method = RequestMethod.POST)
    @ResponseBody
    public R referralDetails(Integer promoterId, Integer offset, Integer limit) {
        if (promoterId == null) {
            return R.error(-1, "promoterId不能为空");
        }
        return R.ok().put("data", nxCustomerPromoterService.queryReferralDetails(promoterId, offset, limit));
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public R delete(Integer promoterId) {
        if (promoterId == null) {
            return R.error(-1, "promoterId不能为空");
        }
        try {
            return R.ok().put("data", nxCustomerPromoterService.deletePromoter(promoterId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(-1, e.getMessage());
        }
    }
}

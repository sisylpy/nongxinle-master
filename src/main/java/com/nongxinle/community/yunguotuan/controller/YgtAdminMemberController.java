package com.nongxinle.community.yunguotuan.controller;

import com.nongxinle.utils.R;
import com.nongxinle.community.yunguotuan.service.YgtMemberShareService;
import com.nongxinle.community.yunguotuan.util.YgtIdentityLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/ygt/admin/member")
public class YgtAdminMemberController {
    @Autowired
    private YgtMemberShareService ygtMemberShareService;

    @RequestMapping(value = "/registrations", method = RequestMethod.GET)
    @ResponseBody
    public R registrations(@RequestParam(required = false) Long campaignId,
                           @RequestParam(required = false) Long wecomGroupId,
                           @RequestParam(defaultValue = "0") Integer offset,
                           @RequestParam(defaultValue = "100") Integer limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("campaignId", campaignId);
        params.put("wecomGroupId", wecomGroupId);
        params.put("offset", offset);
        params.put("limit", limit);
        return R.ok().put("data", ygtMemberShareService.registrations(params));
    }

    @RequestMapping(value = "/group-overview", method = RequestMethod.GET)
    @ResponseBody
    public R groupOverview(@RequestParam(required = false) Long campaignId,
                           @RequestParam(required = false) Long wecomGroupId) {
        try {
            YgtIdentityLog.info("API group-overview request campaignId=" + campaignId + " wecomGroupId=" + wecomGroupId);
            Map<String, Object> params = new HashMap<>();
            params.put("campaignId", campaignId);
            params.put("wecomGroupId", wecomGroupId);
            Map<String, Object> data = ygtMemberShareService.groupMemberOverview(params);
            YgtIdentityLog.info("API group-overview response"
                    + " memberCount=" + data.get("memberCount")
                    + " registeredCount=" + data.get("registeredCount")
                    + " matchedCount=" + data.get("matchedCount")
                    + " confirmedCount=" + data.get("confirmedCount"));
            return R.ok().put("data", data);
        } catch (Exception e) {
            YgtIdentityLog.info("API group-overview error: " + e.getMessage());
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/confirm-identity", method = RequestMethod.POST)
    @ResponseBody
    public R confirmIdentity(@RequestBody(required = false) Map<String, Object> body) {
        try {
            Map<String, Object> req = body == null ? new HashMap<String, Object>() : body;
            YgtIdentityLog.info("API confirm-identity request"
                    + " wecomGroupId=" + req.get("wecomGroupId")
                    + " customerUserId=" + req.get("customerUserId")
                    + " memberKey=" + req.get("memberKey"));
            Map<String, Object> data = ygtMemberShareService.confirmGroupIdentity(req);
            YgtIdentityLog.info("API confirm-identity response"
                    + " customerUserId=" + data.get("customerUserId")
                    + " bindStatus=" + data.get("bindStatus")
                    + " matchMethod=" + data.get("matchMethod")
                    + " displayName=" + data.get("displayName"));
            return R.ok().put("data", data);
        } catch (Exception e) {
            YgtIdentityLog.info("API confirm-identity error: " + e.getMessage());
            return R.error(e.getMessage());
        }
    }
}

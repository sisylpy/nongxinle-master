package com.nongxinle.community.yunguotuan.controller;

import com.nongxinle.utils.R;
import com.nongxinle.community.yunguotuan.service.YgtMemberShareService;
import com.nongxinle.community.yunguotuan.util.YgtIdentityLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/ygt/member")
public class YgtMemberShareController {
    @Autowired
    private YgtMemberShareService ygtMemberShareService;

    @RequestMapping(value = "/share/create", method = RequestMethod.POST)
    @ResponseBody
    public R createShare(@RequestBody(required = false) Map<String, Object> body) {
        try {
            return R.ok().put("data", ygtMemberShareService.createShare(body == null ? new HashMap<String, Object>() : body));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/share/landing", method = RequestMethod.GET)
    @ResponseBody
    public R landing(@RequestParam String shareCode) {
        try {
            return R.ok().put("data", ygtMemberShareService.landing(shareCode));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/register-from-share", method = RequestMethod.POST)
    @ResponseBody
    public R registerFromShare(@RequestBody(required = false) Map<String, Object> body) {
        try {
            Map<String, Object> req = body == null ? new HashMap<String, Object>() : body;
            YgtIdentityLog.info("API register-from-share request"
                    + " shareCode=" + req.get("shareCode")
                    + " customerUserId=" + req.get("customerUserId")
                    + " wxCode=" + YgtIdentityLog.present(stringValue(req.get("wxCode")))
                    + " groupEnterEncrypted=" + YgtIdentityLog.present(stringValue(req.get("groupEnterEncrypted")))
                    + " openId=" + YgtIdentityLog.maskId(stringValue(req.get("openId")))
                    + " unionId=" + YgtIdentityLog.maskId(stringValue(req.get("unionId")))
                    + " phone=" + YgtIdentityLog.maskPhone(stringValue(req.get("phone"))));
            Map<String, Object> data = ygtMemberShareService.registerFromShare(req);
            YgtIdentityLog.info("API register-from-share response"
                    + " customerUserId=" + data.get("customerUserId")
                    + " bindStatus=" + data.get("bindStatus")
                    + " matchMethod=" + data.get("matchMethod")
                    + " identityStatus=" + data.get("identityStatus")
                    + " identityHint=" + data.get("identityHint"));
            return R.ok().put("data", data);
        } catch (Exception e) {
            YgtIdentityLog.info("API register-from-share error: " + e.getMessage());
            return R.error(e.getMessage());
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }
}

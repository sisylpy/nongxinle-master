package com.nongxinle.community.yunguotuan.controller;

import com.nongxinle.utils.R;
import com.nongxinle.community.yunguotuan.service.YgtCampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/ygt/admin/campaign")
public class YgtAdminCampaignController {
    @Autowired
    private YgtCampaignService ygtCampaignService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public R create(@RequestBody Map<String, Object> body) {
        try {
            return R.ok().put("data", ygtCampaignService.createCampaign(body == null ? new HashMap<String, Object>() : body));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/goods/add", method = RequestMethod.POST)
    @ResponseBody
    public R addGoods(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            return R.ok().put("data", ygtCampaignService.addGoods(id, body == null ? new HashMap<String, Object>() : body));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/open", method = RequestMethod.POST)
    @ResponseBody
    public R open(@PathVariable Long id) {
        try {
            return R.ok().put("data", ygtCampaignService.openCampaign(id));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/close", method = RequestMethod.POST)
    @ResponseBody
    public R close(@PathVariable Long id) {
        try {
            return R.ok().put("data", ygtCampaignService.closeCampaign(id));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public R list(@RequestParam(required = false) Long groupId,
                  @RequestParam(required = false) String corpId,
                  @RequestParam(required = false) String status,
                  @RequestParam(defaultValue = "0") Integer offset,
                  @RequestParam(defaultValue = "100") Integer limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("corpId", corpId);
        params.put("status", status);
        params.put("offset", offset);
        params.put("limit", limit);
        return R.ok().put("data", ygtCampaignService.listCampaigns(params));
    }

    @RequestMapping(value = "/current", method = RequestMethod.GET)
    @ResponseBody
    public R current(@RequestParam(required = false) Long groupId,
                     @RequestParam(required = false) String corpId) {
        return R.ok().put("data", ygtCampaignService.currentCampaigns(groupId, corpId));
    }

    @RequestMapping(value = "/{id}/summary", method = RequestMethod.GET)
    @ResponseBody
    public R summary(@PathVariable Long id) {
        try {
            return R.ok().put("data", ygtCampaignService.campaignSummary(id));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    /**
     * 测试辅助: 注入一条模拟消息并生成候选单，用于验证 OPEN 团期自动关联 campaignId。
     * POST /api/ygt/admin/campaign/test/inject-message
     * body: { "groupId": 1, "content": "测试下单消息" }
     */
    @RequestMapping(value = "/test/inject-message", method = RequestMethod.POST)
    @ResponseBody
    public R injectTestMessage(@RequestBody(required = false) Map<String, Object> body) {
        try {
            Map<String, Object> params = body == null ? new HashMap<String, Object>() : body;
            Long groupId = params.get("groupId") == null ? null : Long.valueOf(String.valueOf(params.get("groupId")));
            String content = params.get("content") == null ? null : String.valueOf(params.get("content"));
            return R.ok().put("data", ygtCampaignService.createTestMessageForCandidate(groupId, content));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}

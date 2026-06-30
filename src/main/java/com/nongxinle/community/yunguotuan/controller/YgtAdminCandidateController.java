package com.nongxinle.community.yunguotuan.controller;

import com.nongxinle.utils.R;
import com.nongxinle.community.yunguotuan.entity.YgtOrderCandidateEntity;
import com.nongxinle.community.yunguotuan.entity.YgtOrderCandidateItemEntity;
import com.nongxinle.community.yunguotuan.service.YgtOrderCandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/ygt/admin/candidate")
public class YgtAdminCandidateController {
    @Autowired
    private YgtOrderCandidateService ygtOrderCandidateService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public R list(@RequestParam(required = false) String corpId,
                  @RequestParam(required = false) Long groupId,
                  @RequestParam(required = false) Long campaignId,
                  @RequestParam(required = false) String chatId,
                  @RequestParam(required = false) String status,
                  @RequestParam(defaultValue = "0") Integer offset,
                  @RequestParam(defaultValue = "100") Integer limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("corpId", corpId);
        params.put("groupId", groupId);
        params.put("campaignId", campaignId);
        params.put("chatId", chatId);
        params.put("status", status);
        params.put("offset", offset);
        params.put("limit", limit);
        List<YgtOrderCandidateEntity> candidates = ygtOrderCandidateService.queryCandidates(params);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (YgtOrderCandidateEntity candidate : candidates) {
            Long cid = candidate.getYgtOrderCandidateId();
            Map<String, Object> detail = ygtOrderCandidateService.candidateDetail(cid);
            Map<String, Object> row = toCandidateRow(candidate);
            row.put("items", detail.get("items"));
            rows.add(row);
        }
        return R.ok().put("data", rows);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public R detail(@PathVariable Long id) {
        try {
            return R.ok().put("data", ygtOrderCandidateService.candidateDetail(id));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/edit", method = RequestMethod.POST)
    @ResponseBody
    public R edit(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        try {
            return R.ok().put("data", ygtOrderCandidateService.editCandidate(id, body == null ? new HashMap<String, Object>() : body));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/ignore", method = RequestMethod.POST)
    @ResponseBody
    public R ignore(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        try {
            return R.ok().put("data", ygtOrderCandidateService.ignoreCandidate(id, body == null ? new HashMap<String, Object>() : body));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/restore", method = RequestMethod.POST)
    @ResponseBody
    public R restore(@PathVariable Long id) {
        try {
            return R.ok().put("data", ygtOrderCandidateService.restoreCandidate(id));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/confirm", method = RequestMethod.POST)
    @ResponseBody
    public R confirm(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        try {
            return R.ok().put("data", ygtOrderCandidateService.confirmCandidate(id, body == null ? new HashMap<String, Object>() : body));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    private Map<String, Object> toCandidateRow(YgtOrderCandidateEntity candidate) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", candidate.getYgtOrderCandidateId());
        row.put("corpId", candidate.getYgtOcCorpId());
        row.put("groupId", candidate.getYgtOcGroupId());
        row.put("chatId", candidate.getYgtOcChatId());
        row.put("messageId", candidate.getYgtOcMessageId());
        row.put("campaignId", candidate.getYgtOcCampaignId());
        row.put("fromUser", candidate.getYgtOcFromUser());
        row.put("externalUserId", candidate.getYgtOcExternalUserId());
        row.put("customerNameSnapshot", candidate.getYgtOcCustomerNameSnapshot());
        row.put("memberIdentifier", candidate.getYgtOcMemberIdentifier());
        row.put("msgTime", candidate.getYgtOcMsgTime());
        row.put("parseStatus", candidate.getYgtOcParseStatus());
        row.put("status", candidate.getYgtOcStatus());
        row.put("originalTextPreview", preview(candidate.getYgtOcOriginalText()));
        row.put("createTime", candidate.getYgtOcCreateTime());
        return row;
    }

    private String preview(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() > 80 ? normalized.substring(0, 80) : normalized;
    }
}

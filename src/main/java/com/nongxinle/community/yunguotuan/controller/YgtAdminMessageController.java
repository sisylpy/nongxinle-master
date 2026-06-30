package com.nongxinle.community.yunguotuan.controller;

import com.nongxinle.utils.R;
import com.nongxinle.community.yunguotuan.entity.YgtChatMessageEntity;
import com.nongxinle.community.yunguotuan.service.YgtMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/ygt/admin/message")
public class YgtAdminMessageController {
    @Autowired
    private YgtMessageService ygtMessageService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public R list(@RequestParam(required = false) String corpId,
                  @RequestParam(required = false) String chatId,
                  @RequestParam(required = false) String parseStatus,
                  @RequestParam(defaultValue = "0") Integer offset,
                  @RequestParam(defaultValue = "100") Integer limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("corpId", corpId);
        params.put("chatId", chatId);
        params.put("parseStatus", parseStatus);
        params.put("offset", offset);
        params.put("limit", limit);
        List<YgtChatMessageEntity> messages = ygtMessageService.queryMessages(params);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (YgtChatMessageEntity message : messages) {
            rows.add(toMessageRow(message));
        }
        return R.ok().put("data", rows);
    }

    @RequestMapping(value = "/{id}/parse", method = RequestMethod.POST)
    @ResponseBody
    public R parse(@PathVariable Long id) {
        try {
            return R.ok().put("data", ygtMessageService.parseMessage(id));
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    private Map<String, Object> toMessageRow(YgtChatMessageEntity message) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", message.getYgtChatMessageId());
        row.put("corpId", message.getYgtCmCorpId());
        row.put("groupId", message.getYgtCmGroupId());
        row.put("chatId", message.getYgtCmChatId());
        row.put("msgId", message.getYgtCmMsgId());
        row.put("seq", message.getYgtCmSeq());
        row.put("fromUser", message.getYgtCmFromUser());
        row.put("msgTime", message.getYgtCmMsgTime());
        row.put("msgType", message.getYgtCmMsgType());
        row.put("action", message.getYgtCmAction());
        row.put("parseStatus", message.getYgtCmParseStatus());
        row.put("parseError", message.getYgtCmParseError());
        row.put("contentPreview", preview(message.getYgtCmContent()));
        row.put("createTime", message.getYgtCmCreateTime());
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

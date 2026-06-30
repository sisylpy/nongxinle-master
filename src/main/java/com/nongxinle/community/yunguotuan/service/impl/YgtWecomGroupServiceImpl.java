package com.nongxinle.community.yunguotuan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.dao.QyGbDisCorpMsgAuditDao;
import com.nongxinle.dao.YgtWecomGroupDao;
import com.nongxinle.dao.QyGbDisCorpDao;
import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import com.nongxinle.entity.QyGbDisCorpEntity;
import com.nongxinle.community.yunguotuan.entity.YgtWecomGroupEntity;
import com.nongxinle.community.yunguotuan.service.YgtWecomGroupService;
import com.nongxinle.community.yunguotuan.util.YgtIdentityLog;
import com.nongxinle.utils.MyAPPIDConfig;
import com.nongxinle.utils.WeChatUtil;
import com.nongxinle.utils.WxMiniProgramDataCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;

@Service
public class YgtWecomGroupServiceImpl implements YgtWecomGroupService {
    private static final Integer STATUS_ENABLED = 1;
    private static final Integer STATUS_DISABLED = 0;

    @Autowired
    private YgtWecomGroupDao ygtWecomGroupDao;

    @Autowired
    private QyGbDisCorpMsgAuditDao qyGbDisCorpMsgAuditDao;

    @Autowired
    private QyGbDisCorpDao qyGbDisCorpDao;

    @Override
    public Map<String, Object> syncGroups(Map<String, Object> params) {
        String corpId = stringValue(params.get("corpId"));
        if (isBlank(corpId)) {
            throw new IllegalArgumentException("corpId不能为空");
        }

        String manualChatId = stringValue(params.get("chatId"));
        if (!isBlank(manualChatId)) {
            YgtWecomGroupEntity group = upsertGroup(corpId, manualChatId,
                    stringValue(params.get("chatName")),
                    stringValue(params.get("ownerUserId")),
                    intValue(params.get("memberCount")),
                    intValue(params.get("nxCommunityId")),
                    intValue(params.get("leaderUserId")),
                    intValue(params.get("adminUserId")),
                    "MANUAL");
            Map<String, Object> result = new HashMap<>();
            result.put("saved", 1);
            result.put("groupId", group.getYgtWecomGroupId());
            return result;
        }

        String accessToken = fetchAccessTokenSilently(corpId);
        if (isBlank(accessToken)) {
            throw new IllegalStateException("无法获取企微access_token");
        }

        String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/groupchat/list?access_token=" + accessToken;
        Map<String, Object> body = new HashMap<>();
        body.put("status_filter", intValue(params.get("statusFilter")) == null ? 0 : intValue(params.get("statusFilter")));
        body.put("offset", intValue(params.get("offset")) == null ? 0 : intValue(params.get("offset")));
        body.put("limit", intValue(params.get("limit")) == null ? 100 : intValue(params.get("limit")));

        String response = httpRequestSilently(url, "POST", JSON.toJSONString(body));
        JSONObject json = JSON.parseObject(response);
        if (json.getInteger("errcode") == null || json.getInteger("errcode") != 0) {
            throw new IllegalStateException("同步客户群失败: " + json.getString("errmsg"));
        }

        JSONArray groupChatList = json.getJSONArray("group_chat_list");
        int saved = 0;
        if (groupChatList != null) {
            for (int i = 0; i < groupChatList.size(); i++) {
                JSONObject chatObj = groupChatList.getJSONObject(i);
                String chatId = chatObj.getString("chat_id");
                Map<String, Object> detail = fetchGroupDetail(accessToken, chatId);
                upsertGroup(corpId, chatId,
                        stringValue(detail.get("name")),
                        stringValue(detail.get("owner")),
                        intValue(detail.get("memberCount")),
                        null, null, null, "API");
                saved++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("saved", saved);
        result.put("source", "API");
        return result;
    }

    @Override
    public List<YgtWecomGroupEntity> queryGroups(Map<String, Object> params) {
        return ygtWecomGroupDao.queryList(params);
    }

    @Override
    public int enableGroup(Long id) {
        return ygtWecomGroupDao.updateStatus(id, STATUS_ENABLED);
    }

    @Override
    public int disableGroup(Long id) {
        return ygtWecomGroupDao.updateStatus(id, STATUS_DISABLED);
    }

    @Override
    public List<YgtWecomGroupEntity> queryEnabledGroups(String corpId) {
        Map<String, Object> map = new HashMap<>();
        map.put("corpId", corpId);
        return ygtWecomGroupDao.queryEnabledGroups(map);
    }

    @Override
    public Map<String, Object> getGroupMembers(Long wecomGroupId) {
        if (wecomGroupId == null) {
            throw new IllegalArgumentException("wecomGroupId不能为空");
        }
        YgtWecomGroupEntity group = ygtWecomGroupDao.queryObject(wecomGroupId);
        if (group == null) {
            throw new IllegalArgumentException("客户群不存在");
        }
        String accessToken = fetchAccessTokenSilently(group.getYgtWgCorpId());
        if (isBlank(accessToken)) {
            throw new IllegalStateException("无法获取企微access_token");
        }

        String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/groupchat/get?access_token=" + accessToken;
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", group.getYgtWgChatId());
        body.put("need_name", 1);
        String response = httpRequestSilently(url, "POST", JSON.toJSONString(body));
        JSONObject json = JSON.parseObject(response);
        Integer errcode = json.getInteger("errcode");
        if (errcode == null || errcode != 0) {
            throw new IllegalStateException("获取客户群成员失败: " + json.getString("errmsg"));
        }

        JSONObject groupChat = json.getJSONObject("group_chat");
        List<Map<String, Object>> members = new ArrayList<>();
        if (groupChat != null) {
            JSONArray memberList = groupChat.getJSONArray("member_list");
            if (memberList != null) {
                for (int i = 0; i < memberList.size(); i++) {
                    members.add(parseGroupMember(memberList.getJSONObject(i)));
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("wecomGroupId", wecomGroupId);
        result.put("corpId", group.getYgtWgCorpId());
        result.put("chatId", group.getYgtWgChatId());
        result.put("groupName", firstNonBlank(
                groupChat == null ? null : groupChat.getString("name"),
                group.getYgtWgChatName()));
        result.put("memberCount", members.size());
        result.put("members", members);
        return result;
    }

    @Override
    public Map<String, Object> resolveGroupMemberIdentity(String corpId, String chatId, String wxCode,
                                                          String groupEnterEncrypted, String groupEnterIv) {
        Map<String, Object> result = new HashMap<>();
        result.put("identityStatus", "PENDING");
        YgtIdentityLog.info("resolveGroupMemberIdentity start"
                + " corpId=" + corpId
                + " chatId=" + chatId
                + " wxCode=" + YgtIdentityLog.present(wxCode)
                + " groupEnterEncrypted=" + YgtIdentityLog.present(groupEnterEncrypted));
        if (isBlank(corpId) || isBlank(chatId)) {
            result.put("identityStatus", "CONFIG_MISSING");
            YgtIdentityLog.info("resolveGroupMemberIdentity end identityStatus=CONFIG_MISSING");
            return result;
        }
        if (isBlank(wxCode)) {
            result.put("identityStatus", "WX_CODE_MISSING");
            YgtIdentityLog.info("resolveGroupMemberIdentity end identityStatus=WX_CODE_MISSING");
            return result;
        }

        JSONObject session = code2Session(wxCode);
        if (session == null) {
            result.put("identityStatus", "WX_SESSION_FAILED");
            YgtIdentityLog.info("resolveGroupMemberIdentity end identityStatus=WX_SESSION_FAILED session=null");
            return result;
        }
        if (session.containsKey("errcode") && session.getIntValue("errcode") != 0) {
            result.put("identityStatus", "WX_SESSION_FAILED");
            result.put("errmsg", session.getString("errmsg"));
            YgtIdentityLog.info("resolveGroupMemberIdentity end identityStatus=WX_SESSION_FAILED errcode="
                    + session.getInteger("errcode") + " errmsg=" + session.getString("errmsg"));
            return result;
        }

        String openId = session.getString("openid");
        String unionId = session.getString("unionid");
        String sessionKey = session.getString("session_key");
        result.put("openId", openId);
        result.put("unionId", unionId);
        YgtIdentityLog.info("code2session ok"
                + " openId=" + YgtIdentityLog.maskId(openId)
                + " unionId=" + YgtIdentityLog.maskId(unionId)
                + " hasSessionKey=" + YgtIdentityLog.present(sessionKey));

        if (!isBlank(groupEnterEncrypted) && !isBlank(groupEnterIv) && !isBlank(sessionKey)) {
            try {
                String openGId = WxMiniProgramDataCrypt.extractOpenGId(sessionKey, groupEnterEncrypted, groupEnterIv);
                result.put("openGId", openGId);
                if (!isBlank(openGId)) {
                    String resolvedChatId = openGIdToChatId(corpId, openGId);
                    result.put("resolvedChatId", resolvedChatId);
                    result.put("chatIdMatched", chatId.equals(resolvedChatId));
                    YgtIdentityLog.info("openGId resolved"
                            + " openGId=" + YgtIdentityLog.maskId(openGId)
                            + " resolvedChatId=" + resolvedChatId
                            + " chatIdMatched=" + chatId.equals(resolvedChatId));
                }
            } catch (Exception e) {
                result.put("openGIdError", e.getMessage());
                YgtIdentityLog.info("openGId decrypt error: " + e.getMessage());
            }
        } else {
            YgtIdentityLog.info("openGId skipped"
                    + " groupEnterEncrypted=" + YgtIdentityLog.present(groupEnterEncrypted)
                    + " groupEnterIv=" + YgtIdentityLog.present(groupEnterIv)
                    + " hasSessionKey=" + YgtIdentityLog.present(sessionKey));
        }

        if (isBlank(unionId)) {
            result.put("identityStatus", "UNIONID_MISSING");
            result.put("hint", "小程序未绑定微信开放平台，wx.login 拿不到 unionid，无法自动识别外部客户。请绑定开放平台，或在管理端手动确认群身份");
            YgtIdentityLog.info("resolveGroupMemberIdentity end identityStatus=UNIONID_MISSING");
            return result;
        }

        List<Map<String, Object>> members = fetchGroupMembersByChatId(corpId, chatId);
        result.put("groupMemberCount", members.size());
        logGroupMemberUnionIds(members);

        Map<String, Object> unionMatchedMember = findExternalMemberByUnionId(members, unionId);
        if (unionMatchedMember != null) {
            fillConfirmedResult(result, unionMatchedMember, "GROUP_MEMBER_UNIONID");
            YgtIdentityLog.info("resolveGroupMemberIdentity end identityStatus=CONFIRMED matchMethod=GROUP_MEMBER_UNIONID"
                    + " member=" + unionMatchedMember.get("displayName"));
            return result;
        }
        YgtIdentityLog.info("GROUP_MEMBER_UNIONID no match for unionId=" + YgtIdentityLog.maskId(unionId));

        JSONObject convertJson = unionIdToExternalUserIdDetail(corpId, unionId, openId);
        if (convertJson != null) {
            result.put("idConvertErrcode", convertJson.getInteger("errcode"));
            result.put("idConvertErrmsg", convertJson.getString("errmsg"));
        }
        String externalUserId = convertJson == null ? null
                : firstNonBlank(convertJson.getString("external_userid"), convertJson.getString("pending_id"));
        result.put("externalUserId", externalUserId);
        if (isBlank(externalUserId)) {
            result.put("identityStatus", "EXTERNAL_USERID_NOT_FOUND");
            result.put("hint", buildIdConvertHint(convertJson));
            YgtIdentityLog.info("resolveGroupMemberIdentity end identityStatus=EXTERNAL_USERID_NOT_FOUND"
                    + " idConvertErrcode=" + result.get("idConvertErrcode")
                    + " idConvertErrmsg=" + result.get("idConvertErrmsg"));
            return result;
        }

        for (Map<String, Object> member : members) {
            if (externalUserId.equals(stringValue(member.get("externalUserId")))) {
                fillConfirmedResult(result, member, "UNIONID_TO_EXTERNAL_USERID");
                YgtIdentityLog.info("resolveGroupMemberIdentity end identityStatus=CONFIRMED matchMethod=UNIONID_TO_EXTERNAL_USERID"
                        + " member=" + member.get("displayName"));
                return result;
            }
        }

        result.put("identityStatus", "NOT_IN_GROUP");
        result.put("hint", "external_userid 已解析，但不在该客户群成员列表中");
        YgtIdentityLog.info("resolveGroupMemberIdentity end identityStatus=NOT_IN_GROUP"
                + " externalUserId=" + YgtIdentityLog.maskId(externalUserId));
        return result;
    }

    private void logGroupMemberUnionIds(List<Map<String, Object>> members) {
        if (members == null) {
            YgtIdentityLog.info("group members empty");
            return;
        }
        StringBuilder builder = new StringBuilder("group members unionId snapshot:");
        for (Map<String, Object> member : members) {
            builder.append(" [")
                    .append(member.get("displayName"))
                    .append("/")
                    .append(member.get("memberType"))
                    .append(" unionId=")
                    .append(YgtIdentityLog.maskId(stringValue(member.get("unionId"))))
                    .append(" externalUserId=")
                    .append(YgtIdentityLog.maskId(stringValue(member.get("externalUserId"))))
                    .append("]");
        }
        YgtIdentityLog.info(builder.toString());
    }

    private Map<String, Object> findExternalMemberByUnionId(List<Map<String, Object>> members, String unionId) {
        if (members == null || isBlank(unionId)) {
            return null;
        }
        for (Map<String, Object> member : members) {
            if (!"EXTERNAL".equals(stringValue(member.get("memberType")))) {
                continue;
            }
            if (unionId.equals(stringValue(member.get("unionId")))) {
                return member;
            }
        }
        return null;
    }

    private void fillConfirmedResult(Map<String, Object> result, Map<String, Object> member, String matchMethod) {
        result.put("identityStatus", "CONFIRMED");
        result.put("matchMethod", matchMethod);
        result.put("memberType", member.get("memberType"));
        result.put("memberDisplayName", member.get("displayName"));
        result.put("memberKey", member.get("memberKey"));
        result.put("externalUserId", member.get("externalUserId"));
        result.put("hint", "群身份匹配成功");
    }

    private String buildIdConvertHint(JSONObject convertJson) {
        if (convertJson == null) {
            return "调用 unionid_to_external_userid 失败，请检查企微 access_token 与 API 权限";
        }
        Integer errcode = convertJson.getInteger("errcode");
        String errmsg = convertJson.getString("errmsg");
        if (errcode != null && errcode != 0) {
            return "unionid_to_external_userid 失败[" + errcode + "]: " + errmsg
                    + "。请确认企微已绑定小程序 AppID，且应用有 idconvert 权限";
        }
        return "unionid 无法映射 external_userid";
    }

    private JSONObject unionIdToExternalUserIdDetail(String corpId, String unionId, String openId) {
        String accessToken = fetchAccessTokenSilently(corpId);
        if (isBlank(accessToken)) {
            YgtIdentityLog.info("unionid_to_external_userid skipped accessToken missing corpId=" + corpId);
            return null;
        }
        String url = "https://qyapi.weixin.qq.com/cgi-bin/idconvert/unionid_to_external_userid?access_token=" + accessToken;
        Map<String, Object> body = new HashMap<>();
        body.put("unionid", unionId);
        body.put("openid", openId);
        body.put("subject_type", 0);
        String response = httpRequestSilently(url, "POST", JSON.toJSONString(body));
        if (isBlank(response)) {
            YgtIdentityLog.info("unionid_to_external_userid empty response");
            return null;
        }
        JSONObject json = JSON.parseObject(response);
        YgtIdentityLog.info("unionid_to_external_userid response"
                + " errcode=" + (json == null ? null : json.getInteger("errcode"))
                + " errmsg=" + (json == null ? null : json.getString("errmsg"))
                + " external_userid=" + YgtIdentityLog.maskId(json == null ? null : json.getString("external_userid")));
        return json;
    }

    private JSONObject code2Session(String wxCode) {
        MyAPPIDConfig config = new MyAPPIDConfig();
        String appId = config.getQingqingxiangAppId();
        String secret = config.getQingqingxiangScreat();
        YgtIdentityLog.info("code2session request appId=" + appId + " wxCode=" + YgtIdentityLog.present(wxCode));
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId
                + "&secret=" + secret + "&js_code=" + wxCode + "&grant_type=authorization_code";
        String response = WeChatUtil.httpRequest(url, "GET", null);
        if (isBlank(response)) {
            YgtIdentityLog.info("code2session empty response");
            return null;
        }
        JSONObject json = JSON.parseObject(response);
        YgtIdentityLog.info("code2session raw"
                + " hasOpenId=" + (json != null && json.containsKey("openid"))
                + " hasUnionId=" + (json != null && json.containsKey("unionid"))
                + " errcode=" + (json == null ? null : json.getInteger("errcode")));
        return json;
    }

    private String openGIdToChatId(String corpId, String openGId) {
        String accessToken = fetchAccessTokenSilently(corpId);
        if (isBlank(accessToken)) {
            return null;
        }
        String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/opengid_to_chatid?access_token=" + accessToken;
        Map<String, Object> body = new HashMap<>();
        body.put("opengid", openGId);
        JSONObject json = JSON.parseObject(httpRequestSilently(url, "POST", JSON.toJSONString(body)));
        if (json.getInteger("errcode") != null && json.getInteger("errcode") == 0) {
            return json.getString("chat_id");
        }
        return null;
    }

    private List<Map<String, Object>> fetchGroupMembersByChatId(String corpId, String chatId) {
        String accessToken = fetchAccessTokenSilently(corpId);
        if (isBlank(accessToken)) {
            return Collections.emptyList();
        }
        String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/groupchat/get?access_token=" + accessToken;
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("need_name", 1);
        JSONObject json = JSON.parseObject(httpRequestSilently(url, "POST", JSON.toJSONString(body)));
        if (json.getInteger("errcode") == null || json.getInteger("errcode") != 0) {
            return Collections.emptyList();
        }
        JSONObject groupChat = json.getJSONObject("group_chat");
        List<Map<String, Object>> members = new ArrayList<>();
        if (groupChat != null) {
            JSONArray memberList = groupChat.getJSONArray("member_list");
            if (memberList != null) {
                for (int i = 0; i < memberList.size(); i++) {
                    members.add(parseGroupMember(memberList.getJSONObject(i)));
                }
            }
        }
        return members;
    }

    private Map<String, Object> parseGroupMember(JSONObject member) {
        Map<String, Object> row = new HashMap<>();
        int type = member == null ? 0 : member.getIntValue("type");
        String userId = member == null ? null : member.getString("userid");
        String externalUserId = member == null ? null : member.getString("external_userid");
        row.put("memberType", type == 1 ? "INTERNAL" : "EXTERNAL");
        row.put("memberTypeText", type == 1 ? "企业成员" : "外部客户");
        row.put("userId", userId);
        row.put("externalUserId", externalUserId);
        row.put("groupNickname", member == null ? null : member.getString("group_nickname"));
        row.put("name", member == null ? null : member.getString("name"));
        row.put("displayName", firstNonBlank(
                member == null ? null : member.getString("group_nickname"),
                member == null ? null : member.getString("name"),
                type == 1 ? userId : externalUserId,
                "未命名成员"));
        row.put("unionId", member == null ? null : member.getString("unionid"));
        row.put("joinTime", member == null ? null : member.getLong("join_time"));
        row.put("memberKey", type == 1 ? "internal:" + userId : "external:" + externalUserId);
        return row;
    }

    private String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private String firstNonBlank(String first, String second, String third, String fourth) {
        if (!isBlank(first)) {
            return first;
        }
        if (!isBlank(second)) {
            return second;
        }
        if (!isBlank(third)) {
            return third;
        }
        return fourth;
    }

    private YgtWecomGroupEntity upsertGroup(String corpId, String chatId, String chatName, String ownerUserId,
                                            Integer memberCount, Integer nxCommunityId, Integer leaderUserId,
                                            Integer adminUserId, String source) {
        Date now = new Date();
        // 只有 mock 数据自动启用，真实 API 群默认禁用需手动开启
        boolean isMockSource = "MOCK".equals(source);

        YgtWecomGroupEntity existing = ygtWecomGroupDao.queryByCorpAndChatId(corpId, chatId);
        if (existing != null) {
            existing.setYgtWgChatName(chatName);
            existing.setYgtWgOwnerUserId(ownerUserId);
            existing.setYgtWgMemberCount(memberCount);
            if (nxCommunityId != null) {
                existing.setYgtWgNxCommunityId(nxCommunityId);
            }
            if (leaderUserId != null) {
                existing.setYgtWgLeaderUserId(leaderUserId);
            }
            if (adminUserId != null) {
                existing.setYgtWgAdminUserId(adminUserId);
            }
            existing.setYgtWgSource(source);
            if (isMockSource) {
                existing.setYgtWgStatus(STATUS_ENABLED);
            }
            existing.setYgtWgUpdateTime(now);
            ygtWecomGroupDao.update(existing);
            return existing;
        }

        YgtWecomGroupEntity group = new YgtWecomGroupEntity();
        group.setYgtWgCorpId(corpId);
        group.setYgtWgChatId(chatId);
        group.setYgtWgChatName(chatName);
        group.setYgtWgOwnerUserId(ownerUserId);
        group.setYgtWgMemberCount(memberCount);
        // 真实 API 群默认禁用，需管理人员手动开启
        group.setYgtWgStatus(isMockSource ? STATUS_ENABLED : STATUS_DISABLED);
        group.setYgtWgNxCommunityId(nxCommunityId);
        group.setYgtWgLeaderUserId(leaderUserId);
        group.setYgtWgAdminUserId(adminUserId);
        group.setYgtWgSource(source);
        group.setYgtWgCreateTime(now);
        group.setYgtWgUpdateTime(now);
        ygtWecomGroupDao.save(group);
        return group;
    }

    private Map<String, Object> fetchGroupDetail(String accessToken, String chatId) {
        Map<String, Object> detail = new HashMap<>();
        String url = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/groupchat/get?access_token=" + accessToken;
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("need_name", 1);
        String response = httpRequestSilently(url, "POST", JSON.toJSONString(body));
        JSONObject json = JSON.parseObject(response);
        if (json.getInteger("errcode") != null && json.getInteger("errcode") == 0) {
            JSONObject groupChat = json.getJSONObject("group_chat");
            if (groupChat != null) {
                detail.put("name", groupChat.getString("name"));
                detail.put("owner", groupChat.getString("owner"));
                JSONArray members = groupChat.getJSONArray("member_list");
                detail.put("memberCount", members == null ? 0 : members.size());
            }
        }
        return detail;
    }

    private String fetchAccessTokenSilently(String corpId) {
        QyGbDisCorpMsgAuditEntity config = qyGbDisCorpMsgAuditDao.queryByCorpId(corpId);
        if (config != null && !isBlank(config.getQyGbDisCorpMsgAuditSecret())) {
            if (!isBlank(config.getQyGbDisCorpMsgAuditAccessToken())
                    && config.getQyGbDisCorpMsgAuditTokenExpireTime() != null
                    && config.getQyGbDisCorpMsgAuditTokenExpireTime().after(new Date())) {
                return config.getQyGbDisCorpMsgAuditAccessToken();
            }

            String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + corpId
                    + "&corpsecret=" + config.getQyGbDisCorpMsgAuditSecret();
            String response = httpRequestSilently(url, "GET", null);
            JSONObject json = JSON.parseObject(response);
            if (json.getInteger("errcode") != null && json.getInteger("errcode") == 0) {
                String accessToken = json.getString("access_token");
                Integer expiresIn = json.getInteger("expires_in");
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, expiresIn == null ? 6900 : Math.max(60, expiresIn - 300));
                qyGbDisCorpMsgAuditDao.updateAccessToken(corpId, accessToken, calendar.getTime());
                return accessToken;
            }
        }

        QyGbDisCorpEntity corpEntity = qyGbDisCorpDao.queryQyCropByCropId(corpId);
        if (corpEntity != null && !isBlank(corpEntity.getQyGbDisCorpAccessToken())) {
            return corpEntity.getQyGbDisCorpAccessToken();
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Integer intValue(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String httpRequestSilently(String requestUrl, String requestMethod, String output) {
        try {
            URL url = new URL(requestUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            if (output != null) {
                connection.setDoOutput(true);
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(output.getBytes("utf-8"));
                outputStream.close();
            }
            InputStream inputStream = connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder buffer = new StringBuilder();
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            connection.disconnect();
            return buffer.toString();
        } catch (Exception e) {
            throw new IllegalStateException("调用企微接口失败", e);
        }
    }
}

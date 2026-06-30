package com.nongxinle.community.yunguotuan.service.impl;

import com.nongxinle.dao.*;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.entity.NxECommerceCommunityEntity;
import com.nongxinle.community.ecommerce.service.NxECommerceCommunityService;
import com.nongxinle.community.yunguotuan.entity.*;
import com.nongxinle.community.yunguotuan.service.YgtMemberShareService;
import com.nongxinle.community.yunguotuan.service.YgtWecomGroupService;
import com.nongxinle.community.yunguotuan.util.YgtIdentityLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

@Service
public class YgtMemberShareServiceImpl implements YgtMemberShareService {
    private static final String SHARE_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String DEFAULT_BENEFIT_CODE = "YGT_NEW_MEMBER";

    @Autowired
    private YgtMemberShareInviteDao ygtMemberShareInviteDao;
    @Autowired
    private YgtMemberJoinSourceDao ygtMemberJoinSourceDao;
    @Autowired
    private YgtMemberBenefitLogDao ygtMemberBenefitLogDao;
    @Autowired
    private YgtGroupBuyCampaignDao ygtGroupBuyCampaignDao;
    @Autowired
    private YgtWecomGroupDao ygtWecomGroupDao;
    @Autowired
    private NxCustomerUserDao nxCustomerUserDao;
    @Autowired
    private NxECommerceCommunityService nxECommerceCommunityService;
    @Autowired
    private YgtWecomGroupService ygtWecomGroupService;

    @Override
    @Transactional
    public Map<String, Object> createShare(Map<String, Object> body) {
        Long campaignId = longValue(body.get("campaignId"));
        Long groupId = longValue(body.get("wecomGroupId"));
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId不能为空");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("wecomGroupId不能为空");
        }
        YgtGroupBuyCampaignEntity campaign = ygtGroupBuyCampaignDao.queryObject(campaignId);
        if (campaign == null) {
            throw new IllegalArgumentException("团期不存在");
        }
        YgtWecomGroupEntity group = ygtWecomGroupDao.queryObject(groupId);
        if (group == null) {
            throw new IllegalArgumentException("客户群不存在");
        }

        Date now = new Date();
        YgtMemberShareInviteEntity invite = new YgtMemberShareInviteEntity();
        invite.setYgtMsiShareCode(nextShareCode());
        invite.setYgtMsiSourceType(firstNonBlank(stringValue(body.get("sourceType")), "GROUP_OWNER"));
        invite.setYgtMsiInviterCustomerUserId(intValue(body.get("inviterCustomerUserId")));
        invite.setYgtMsiWecomGroupId(groupId);
        invite.setYgtMsiCampaignId(campaignId);
        invite.setYgtMsiCorpId(group.getYgtWgCorpId());
        invite.setYgtMsiChatId(group.getYgtWgChatId());
        invite.setYgtMsiCommunityId(campaign.getYgtGbcNxCommunityId() == null ? group.getYgtWgNxCommunityId() : campaign.getYgtGbcNxCommunityId());
        invite.setYgtMsiStatus("ACTIVE");
        invite.setYgtMsiExpireTime(dateOrDefault(body.get("expireTime"), daysAfter(now, 30)));
        invite.setYgtMsiCreateTime(now);
        invite.setYgtMsiUpdateTime(now);
        ygtMemberShareInviteDao.save(invite);

        Map<String, Object> result = shareRow(invite);
        appendCommerceInfo(result, campaign, group);
        String miniProgramPath = buildMiniProgramPath(invite.getYgtMsiShareCode(), result);
        result.put("path", miniProgramPath);
        result.put("miniProgramPath", miniProgramPath);
        return result;
    }

    @Override
    public Map<String, Object> landing(String shareCode) {
        YgtMemberShareInviteEntity invite = requireActiveInvite(shareCode);
        YgtGroupBuyCampaignEntity campaign = ygtGroupBuyCampaignDao.queryObject(invite.getYgtMsiCampaignId());
        YgtWecomGroupEntity group = ygtWecomGroupDao.queryObject(invite.getYgtMsiWecomGroupId());
        NxCustomerUserEntity inviter = invite.getYgtMsiInviterCustomerUserId() == null
                ? null
                : nxCustomerUserDao.queryObject(invite.getYgtMsiInviterCustomerUserId());

        Map<String, Object> result = shareRow(invite);
        result.put("campaignName", campaign == null ? null : campaign.getYgtGbcTitle());
        result.put("groupName", group == null ? null : group.getYgtWgChatName());
        result.put("inviterName", inviter == null ? "群主" : firstNonBlank(inviter.getNxCuWxNickName(), "群主"));
        result.put("benefitText", "注册优果团会员可领取新人福利");
        result.put("memberStatusText", "已注册优果团会员");
        result.put("sourceText", "来源：本团分享链接");
        result.put("groupIdentityText", "群身份：待确认");
        appendCommerceInfo(result, campaign, group);
        result.put("miniProgramPath", buildMiniProgramPath(invite.getYgtMsiShareCode(), result));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> registerFromShare(Map<String, Object> body) {
        String shareCode = requiredString(body, "shareCode");
        Integer customerUserId = intValue(body.get("customerUserId"));
        if (customerUserId == null) {
            throw new IllegalArgumentException("customerUserId不能为空");
        }
        YgtIdentityLog.info("registerFromShare start"
                + " shareCode=" + shareCode
                + " customerUserId=" + customerUserId
                + " wxCode=" + YgtIdentityLog.present(stringValue(body.get("wxCode"))));
        YgtMemberShareInviteEntity invite = requireActiveInvite(shareCode);
        NxCustomerUserEntity user = nxCustomerUserDao.queryObject(customerUserId);
        if (user == null) {
            throw new IllegalArgumentException("会员不存在，请先完成小程序会员注册");
        }

        YgtMemberJoinSourceEntity source = ygtMemberJoinSourceDao.queryByCustomerAndShareCode(customerUserId, shareCode);
        boolean created = false;
        if (source == null) {
            source = buildJoinSource(body, invite, user);
            try {
                ygtMemberJoinSourceDao.save(source);
                created = true;
            } catch (DuplicateKeyException duplicate) {
                source = ygtMemberJoinSourceDao.queryByCustomerAndShareCode(customerUserId, shareCode);
                created = false;
            }
        }
        Map<String, Object> identityResolution = applyWecomIdentity(source, invite, body);
        ensureBenefit(source);
        Map<String, Object> result = registerResult(source, created, identityResolution);
        YgtIdentityLog.info("registerFromShare done"
                + " joinSourceId=" + source.getYgtMemberJoinSourceId()
                + " created=" + created
                + " bindStatus=" + source.getYgtMjsBindStatus()
                + " matchMethod=" + source.getYgtMjsMatchMethod()
                + " unionId=" + YgtIdentityLog.maskId(source.getYgtMjsUnionId())
                + " externalUserId=" + YgtIdentityLog.maskId(source.getYgtMjsWecomExternalUserId()));
        return result;
    }

    private Map<String, Object> applyWecomIdentity(YgtMemberJoinSourceEntity source, YgtMemberShareInviteEntity invite, Map<String, Object> body) {
        YgtIdentityLog.info("applyWecomIdentity start"
                + " joinSourceId=" + source.getYgtMemberJoinSourceId()
                + " corpId=" + invite.getYgtMsiCorpId()
                + " chatId=" + invite.getYgtMsiChatId()
                + " wxCode=" + YgtIdentityLog.present(stringValue(body.get("wxCode")))
                + " groupEnterEncrypted=" + YgtIdentityLog.present(stringValue(body.get("groupEnterEncrypted"))));
        Map<String, Object> identity = ygtWecomGroupService.resolveGroupMemberIdentity(
                invite.getYgtMsiCorpId(),
                invite.getYgtMsiChatId(),
                stringValue(body.get("wxCode")),
                stringValue(body.get("groupEnterEncrypted")),
                stringValue(body.get("groupEnterIv")));
        if (identity == null) {
            YgtIdentityLog.info("applyWecomIdentity identity=null");
            return null;
        }
        YgtIdentityLog.info("applyWecomIdentity resolved"
                + " identityStatus=" + identity.get("identityStatus")
                + " matchMethod=" + identity.get("matchMethod")
                + " unionId=" + YgtIdentityLog.maskId(stringValue(identity.get("unionId")))
                + " externalUserId=" + YgtIdentityLog.maskId(stringValue(identity.get("externalUserId")))
                + " hint=" + identity.get("hint")
                + " idConvertErrcode=" + identity.get("idConvertErrcode")
                + " idConvertErrmsg=" + identity.get("idConvertErrmsg"));

        String resolvedUnionId = stringValue(identity.get("unionId"));
        if (!isBlank(resolvedUnionId)) {
            source.setYgtMjsUnionId(resolvedUnionId);
        }
        String resolvedOpenId = stringValue(identity.get("openId"));
        if (!isBlank(resolvedOpenId)) {
            source.setYgtMjsOpenId(resolvedOpenId);
        }

        String externalUserId = stringValue(identity.get("externalUserId"));
        if (!isBlank(externalUserId)) {
            source.setYgtMjsWecomExternalUserId(externalUserId);
        }

        if ("CONFIRMED".equals(stringValue(identity.get("identityStatus")))) {
            source.setYgtMjsBindStatus("GROUP_IDENTITY_CONFIRMED");
            source.setYgtMjsMatchMethod(stringValue(identity.get("matchMethod")));
        } else if (isBlank(source.getYgtMjsMatchMethod())) {
            source.setYgtMjsMatchMethod(stringValue(identity.get("identityStatus")));
        }
        source.setYgtMjsUpdateTime(new Date());
        ygtMemberJoinSourceDao.update(source);
        return identity;
    }

    @Override
    public Map<String, Object> registrations(Map<String, Object> params) {
        List<YgtMemberJoinSourceEntity> sources = ygtMemberJoinSourceDao.queryList(params);
        List<Map<String, Object>> items = new ArrayList<>();
        for (YgtMemberJoinSourceEntity source : sources) {
            Map<String, Object> row = new HashMap<>();
            row.put("customerUserId", source.getYgtMjsCustomerUserId());
            row.put("nickname", source.getYgtMjsNicknameSnapshot());
            row.put("phone", maskPhone(source.getYgtMjsPhoneSnapshot()));
            row.put("bindStatus", source.getYgtMjsBindStatus());
            row.put("bindStatusText", "已注册优果团会员，群身份：待确认");
            row.put("sourceShareCode", source.getYgtMjsSourceShareCode());
            row.put("inviterCustomerUserId", source.getYgtMjsInviterCustomerUserId());
            row.put("createTime", source.getYgtMjsCreateTime());
            items.add(row);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", ygtMemberJoinSourceDao.queryTotal(params));
        result.put("items", items);
        return result;
    }

    @Override
    public Map<String, Object> groupMemberOverview(Map<String, Object> params) {
        Long wecomGroupId = longValue(params.get("wecomGroupId"));
        if (wecomGroupId == null) {
            throw new IllegalArgumentException("wecomGroupId不能为空");
        }

        Map<String, Object> groupData = ygtWecomGroupService.getGroupMembers(wecomGroupId);
        List<?> rawMembers = (List<?>) groupData.get("members");
        List<YgtMemberJoinSourceEntity> sources = ygtMemberJoinSourceDao.queryList(params);

        Map<String, YgtMemberJoinSourceEntity> byUnionId = new HashMap<>();
        Map<String, YgtMemberJoinSourceEntity> byExternalUserId = new HashMap<>();
        Map<String, YgtMemberJoinSourceEntity> byWecomUserId = new HashMap<>();
        Map<Integer, YgtMemberJoinSourceEntity> byCustomerUserId = new HashMap<>();
        for (YgtMemberJoinSourceEntity source : sources) {
            byCustomerUserId.put(source.getYgtMjsCustomerUserId(), source);
            if (!isBlank(source.getYgtMjsUnionId())) {
                byUnionId.put(source.getYgtMjsUnionId(), source);
            }
            if (!isBlank(source.getYgtMjsWecomExternalUserId())) {
                byExternalUserId.put(source.getYgtMjsWecomExternalUserId(), source);
            }
            if (!isBlank(source.getYgtMjsWecomUserId())) {
                byWecomUserId.put(source.getYgtMjsWecomUserId(), source);
            }
        }

        Set<Integer> matchedCustomerIds = new HashSet<>();
        List<Map<String, Object>> groupMembers = new ArrayList<>();
        if (rawMembers != null) {
            for (Object raw : rawMembers) {
                if (!(raw instanceof Map)) {
                    continue;
                }
                Map<?, ?> member = (Map<?, ?>) raw;
                Map<String, Object> row = new HashMap<>();
                for (Map.Entry<?, ?> entry : member.entrySet()) {
                    row.put(String.valueOf(entry.getKey()), entry.getValue());
                }

                YgtMemberJoinSourceEntity matched = findRegistrationMatch(member, byExternalUserId, byWecomUserId, byUnionId, matchedCustomerIds);

                row.put("registered", matched != null);
                row.put("matched", matched != null);
                if (matched != null) {
                    matchedCustomerIds.add(matched.getYgtMjsCustomerUserId());
                    appendRegistrationInfo(row, matched);
                } else {
                    row.put("bindStatusText", "未注册商城会员");
                }
                groupMembers.add(row);
            }
        }

        applySingleInternalRegistrationFallback(groupMembers, sources, matchedCustomerIds);

        List<Map<String, Object>> registeredUnmatched = new ArrayList<>();
        for (YgtMemberJoinSourceEntity source : sources) {
            if (!matchedCustomerIds.contains(source.getYgtMjsCustomerUserId())) {
                Map<String, Object> row = new HashMap<>();
                row.put("customerUserId", source.getYgtMjsCustomerUserId());
                row.put("displayName", registrationDisplayName(source));
                row.put("nickname", source.getYgtMjsNicknameSnapshot());
                row.put("phone", maskPhone(source.getYgtMjsPhoneSnapshot()));
                row.put("unionId", source.getYgtMjsUnionId());
                row.put("sourceShareCode", source.getYgtMjsSourceShareCode());
                row.put("registerTime", source.getYgtMjsCreateTime());
                row.put("joinSourceId", source.getYgtMemberJoinSourceId());
                row.put("bindStatus", source.getYgtMjsBindStatus());
                row.put("bindStatusText", "已注册，待匹配群成员");
                row.put("identityConfirmed", false);
                registeredUnmatched.add(row);
            }
        }

        int confirmedCount = 0;
        for (YgtMemberJoinSourceEntity source : sources) {
            if ("GROUP_IDENTITY_CONFIRMED".equals(source.getYgtMjsBindStatus())) {
                confirmedCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("wecomGroupId", wecomGroupId);
        result.put("groupName", groupData.get("groupName"));
        result.put("chatId", groupData.get("chatId"));
        result.put("memberCount", groupMembers.size());
        result.put("registeredCount", sources.size());
        result.put("matchedCount", matchedCustomerIds.size());
        result.put("confirmedCount", confirmedCount);
        result.put("groupMembers", groupMembers);
        result.put("registeredUnmatched", registeredUnmatched);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> confirmGroupIdentity(Map<String, Object> body) {
        Long wecomGroupId = longValue(body.get("wecomGroupId"));
        Integer customerUserId = intValue(body.get("customerUserId"));
        String memberKey = requiredString(body, "memberKey");
        YgtIdentityLog.info("confirmGroupIdentity start"
                + " wecomGroupId=" + wecomGroupId
                + " customerUserId=" + customerUserId
                + " memberKey=" + memberKey);
        if (wecomGroupId == null || customerUserId == null) {
            throw new IllegalArgumentException("wecomGroupId和customerUserId不能为空");
        }

        Map<String, Object> query = new HashMap<>();
        query.put("wecomGroupId", wecomGroupId);
        query.put("customerUserId", customerUserId);
        List<YgtMemberJoinSourceEntity> sources = ygtMemberJoinSourceDao.queryList(query);
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("未找到该会员的本团注册记录");
        }
        YgtMemberJoinSourceEntity source = sources.get(0);

        Map<String, Object> groupData = ygtWecomGroupService.getGroupMembers(wecomGroupId);
        Map<?, ?> targetMember = findGroupMemberByKey((List<?>) groupData.get("members"), memberKey);
        if (targetMember == null) {
            throw new IllegalArgumentException("群成员不存在");
        }
        assertWecomMemberNotBound(wecomGroupId, source.getYgtMemberJoinSourceId(), targetMember);

        String memberType = stringValue(targetMember.get("memberType"));
        if ("INTERNAL".equals(memberType)) {
            source.setYgtMjsWecomUserId(stringValue(targetMember.get("userId")));
            source.setYgtMjsWecomExternalUserId(null);
        } else {
            source.setYgtMjsWecomExternalUserId(stringValue(targetMember.get("externalUserId")));
            source.setYgtMjsWecomUserId(null);
            String unionId = stringValue(targetMember.get("unionId"));
            if (!isBlank(unionId)) {
                source.setYgtMjsUnionId(unionId);
            }
        }
        source.setYgtMjsBindStatus("GROUP_IDENTITY_CONFIRMED");
        source.setYgtMjsMatchMethod("ADMIN_MANUAL_CONFIRM");
        source.setYgtMjsUpdateTime(new Date());
        ygtMemberJoinSourceDao.update(source);

        Map<String, Object> result = new HashMap<>();
        result.put("joinSourceId", source.getYgtMemberJoinSourceId());
        result.put("customerUserId", customerUserId);
        result.put("memberKey", memberKey);
        result.put("displayName", stringValue(targetMember.get("displayName")));
        result.put("memberTypeText", stringValue(targetMember.get("memberTypeText")));
        result.put("bindStatus", source.getYgtMjsBindStatus());
        result.put("bindStatusText", bindStatusText(source.getYgtMjsBindStatus()));
        result.put("matchMethod", source.getYgtMjsMatchMethod());
        result.put("wecomExternalUserId", source.getYgtMjsWecomExternalUserId());
        result.put("wecomUserId", source.getYgtMjsWecomUserId());
        YgtIdentityLog.info("confirmGroupIdentity done"
                + " joinSourceId=" + source.getYgtMemberJoinSourceId()
                + " displayName=" + result.get("displayName")
                + " bindStatus=" + source.getYgtMjsBindStatus());
        return result;
    }

    private Map<?, ?> findGroupMemberByKey(List<?> members, String memberKey) {
        if (members == null || isBlank(memberKey)) {
            return null;
        }
        for (Object raw : members) {
            if (!(raw instanceof Map)) {
                continue;
            }
            Map<?, ?> member = (Map<?, ?>) raw;
            if (memberKey.equals(stringValue(member.get("memberKey")))) {
                return member;
            }
        }
        return null;
    }

    private void assertWecomMemberNotBound(Long wecomGroupId, Long currentJoinSourceId, Map<?, ?> targetMember) {
        Map<String, Object> query = new HashMap<>();
        query.put("wecomGroupId", wecomGroupId);
        List<YgtMemberJoinSourceEntity> sources = ygtMemberJoinSourceDao.queryList(query);
        String targetExternalUserId = stringValue(targetMember.get("externalUserId"));
        String targetUserId = stringValue(targetMember.get("userId"));
        String memberType = stringValue(targetMember.get("memberType"));

        for (YgtMemberJoinSourceEntity existing : sources) {
            if (existing.getYgtMemberJoinSourceId().equals(currentJoinSourceId)) {
                continue;
            }
            if (!"GROUP_IDENTITY_CONFIRMED".equals(existing.getYgtMjsBindStatus())) {
                continue;
            }
            if ("INTERNAL".equals(memberType)
                    && !isBlank(targetUserId)
                    && targetUserId.equals(existing.getYgtMjsWecomUserId())) {
                throw new IllegalStateException("该群成员已绑定其他商城会员");
            }
            if (!"INTERNAL".equals(memberType)
                    && !isBlank(targetExternalUserId)
                    && targetExternalUserId.equals(existing.getYgtMjsWecomExternalUserId())) {
                throw new IllegalStateException("该群成员已绑定其他商城会员");
            }
        }
    }

    private void appendRegistrationInfo(Map<String, Object> row, YgtMemberJoinSourceEntity source) {
        row.put("joinSourceId", source.getYgtMemberJoinSourceId());
        row.put("customerUserId", source.getYgtMjsCustomerUserId());
        row.put("mallNickname", source.getYgtMjsNicknameSnapshot());
        row.put("phone", maskPhone(source.getYgtMjsPhoneSnapshot()));
        row.put("bindStatus", source.getYgtMjsBindStatus());
        row.put("bindStatusText", bindStatusText(source.getYgtMjsBindStatus()));
        row.put("matchMethod", source.getYgtMjsMatchMethod());
        row.put("identityConfirmed", "GROUP_IDENTITY_CONFIRMED".equals(source.getYgtMjsBindStatus()));
        row.put("sourceShareCode", source.getYgtMjsSourceShareCode());
        row.put("registerTime", source.getYgtMjsCreateTime());
    }

    private YgtMemberJoinSourceEntity findRegistrationMatch(Map<?, ?> member,
                                                            Map<String, YgtMemberJoinSourceEntity> byExternalUserId,
                                                            Map<String, YgtMemberJoinSourceEntity> byWecomUserId,
                                                            Map<String, YgtMemberJoinSourceEntity> byUnionId,
                                                            Set<Integer> alreadyMatched) {
        String externalUserId = stringValue(member.get("externalUserId"));
        if (!isBlank(externalUserId)) {
            YgtMemberJoinSourceEntity byExternal = byExternalUserId.get(externalUserId);
            if (byExternal != null && !alreadyMatched.contains(byExternal.getYgtMjsCustomerUserId())) {
                return byExternal;
            }
        }

        if ("INTERNAL".equals(stringValue(member.get("memberType")))) {
            String userId = stringValue(member.get("userId"));
            if (!isBlank(userId)) {
                YgtMemberJoinSourceEntity byUser = byWecomUserId.get(userId);
                if (byUser != null && !alreadyMatched.contains(byUser.getYgtMjsCustomerUserId())) {
                    return byUser;
                }
            }
        }

        String unionId = stringValue(member.get("unionId"));
        if (!isBlank(unionId)) {
            YgtMemberJoinSourceEntity byUnion = byUnionId.get(unionId);
            if (byUnion != null && !alreadyMatched.contains(byUnion.getYgtMjsCustomerUserId())) {
                return byUnion;
            }
        }
        return null;
    }

    /**
     * 仅用于测试/小团兜底：企业成员无法走 unionid → external_userid 正式链路。
     */
    private void applySingleInternalRegistrationFallback(List<Map<String, Object>> groupMembers,
                                                         List<YgtMemberJoinSourceEntity> sources,
                                                         Set<Integer> matchedCustomerIds) {
        List<Map<String, Object>> unregisteredInternal = new ArrayList<>();
        for (Map<String, Object> row : groupMembers) {
            if ("INTERNAL".equals(row.get("memberType")) && !Boolean.TRUE.equals(row.get("registered"))) {
                unregisteredInternal.add(row);
            }
        }
        if (unregisteredInternal.size() != 1) {
            return;
        }

        YgtMemberJoinSourceEntity unmatchedSource = null;
        for (YgtMemberJoinSourceEntity source : sources) {
            if (!matchedCustomerIds.contains(source.getYgtMjsCustomerUserId())) {
                if (unmatchedSource != null) {
                    return;
                }
                unmatchedSource = source;
            }
        }
        if (unmatchedSource == null) {
            return;
        }

        Map<String, Object> internalRow = unregisteredInternal.get(0);
        internalRow.put("registered", true);
        internalRow.put("matched", true);
        internalRow.put("matchMethod", "INTERNAL_SINGLE_FALLBACK");
        appendRegistrationInfo(internalRow, unmatchedSource);
        matchedCustomerIds.add(unmatchedSource.getYgtMjsCustomerUserId());
    }

    private String registrationDisplayName(YgtMemberJoinSourceEntity source) {
        String phone = source.getYgtMjsPhoneSnapshot();
        String maskedPhone = maskPhone(phone);
        if (!isBlank(maskedPhone)) {
            return maskedPhone + " 的商城会员";
        }
        return firstNonBlank(source.getYgtMjsNicknameSnapshot(), "优果团会员");
    }

    private String bindStatusText(String bindStatus) {
        if ("SOURCE_REGISTERED".equals(bindStatus)) {
            return "已注册优果团会员，群身份：待确认";
        }
        if ("GROUP_IDENTITY_CONFIRMED".equals(bindStatus)) {
            return "群身份已确认";
        }
        return bindStatus;
    }

    private YgtMemberJoinSourceEntity buildJoinSource(Map<String, Object> body, YgtMemberShareInviteEntity invite, NxCustomerUserEntity user) {
        Date now = new Date();
        YgtMemberJoinSourceEntity source = new YgtMemberJoinSourceEntity();
        source.setYgtMjsCustomerUserId(user.getNxCuUserId());
        source.setYgtMjsOpenId(firstNonBlank(stringValue(body.get("openId")), user.getNxCuWxOpenId()));
        source.setYgtMjsUnionId(stringValue(body.get("unionId")));
        source.setYgtMjsPhoneSnapshot(firstNonBlank(stringValue(body.get("phone")), user.getNxCuWxPhoneNumber()));
        source.setYgtMjsNicknameSnapshot(firstNonBlank(stringValue(body.get("nickname")), user.getNxCuWxNickName()));
        source.setYgtMjsSourceCampaignId(invite.getYgtMsiCampaignId());
        source.setYgtMjsSourceWecomGroupId(invite.getYgtMsiWecomGroupId());
        source.setYgtMjsSourceShareCode(invite.getYgtMsiShareCode());
        source.setYgtMjsInviterCustomerUserId(invite.getYgtMsiInviterCustomerUserId());
        source.setYgtMjsBindStatus("SOURCE_REGISTERED");
        source.setYgtMjsBindSource("SHARE_REGISTER");
        source.setYgtMjsCreateTime(now);
        source.setYgtMjsUpdateTime(now);
        return source;
    }

    private void ensureBenefit(YgtMemberJoinSourceEntity source) {
        YgtMemberBenefitLogEntity existing = ygtMemberBenefitLogDao.queryByJoinSourceAndCode(source.getYgtMemberJoinSourceId(), DEFAULT_BENEFIT_CODE);
        if (existing != null) {
            return;
        }
        YgtMemberBenefitLogEntity benefit = new YgtMemberBenefitLogEntity();
        benefit.setYgtMblJoinSourceId(source.getYgtMemberJoinSourceId());
        benefit.setYgtMblCustomerUserId(source.getYgtMjsCustomerUserId());
        benefit.setYgtMblBenefitCode(DEFAULT_BENEFIT_CODE);
        benefit.setYgtMblBenefitName("优果团新人福利");
        benefit.setYgtMblDiscountText("满50减5");
        benefit.setYgtMblUseChannel("YOUGUOTUAN");
        benefit.setYgtMblStatus("PLACEHOLDER");
        benefit.setYgtMblCreateTime(new Date());
        try {
            ygtMemberBenefitLogDao.save(benefit);
        } catch (DuplicateKeyException ignored) {
        }
    }

    private Map<String, Object> registerResult(YgtMemberJoinSourceEntity source, boolean created, Map<String, Object> identityResolution) {
        Map<String, Object> result = new HashMap<>();
        result.put("customerUserId", source.getYgtMjsCustomerUserId());
        result.put("bindStatus", source.getYgtMjsBindStatus());
        result.put("bindSource", source.getYgtMjsBindSource());
        result.put("matchMethod", source.getYgtMjsMatchMethod());
        result.put("wecomExternalUserId", source.getYgtMjsWecomExternalUserId());
        result.put("unionId", source.getYgtMjsUnionId());
        result.put("created", created);
        result.put("memberStatusText", "已注册优果团会员");
        result.put("sourceText", "来源：本团分享链接");
        result.put("groupIdentityText", "GROUP_IDENTITY_CONFIRMED".equals(source.getYgtMjsBindStatus())
                ? "群身份：已确认"
                : "群身份：待确认");
        if (identityResolution != null) {
            result.put("identityStatus", identityResolution.get("identityStatus"));
            result.put("identityHint", identityResolution.get("hint"));
            result.put("identityResolution", identityResolution);
        }
        result.put("benefits", benefitRows(source.getYgtMemberJoinSourceId()));
        return result;
    }

    private List<Map<String, Object>> benefitRows(Long joinSourceId) {
        List<YgtMemberBenefitLogEntity> benefits = ygtMemberBenefitLogDao.queryByJoinSourceId(joinSourceId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (YgtMemberBenefitLogEntity benefit : benefits) {
            Map<String, Object> row = new HashMap<>();
            row.put("benefitName", benefit.getYgtMblBenefitName());
            row.put("discountText", benefit.getYgtMblDiscountText());
            row.put("useChannel", benefit.getYgtMblUseChannel());
            row.put("status", benefit.getYgtMblStatus());
            row.put("notice", "该福利为优果团新人福利占位，暂未写入商城优惠券系统");
            rows.add(row);
        }
        return rows;
    }

    private String buildMiniProgramPath(String shareCode, Map<String, Object> commerceContext) {
        StringBuilder builder = new StringBuilder("subPackage/pages/youguotuan/shareLanding/index?shareCode=");
        builder.append(shareCode);
        if (commerceContext != null) {
            appendQueryParam(builder, "commerceId", commerceContext.get("commerceId"));
            appendQueryParam(builder, "commId", commerceContext.get("communityId"));
            Object communityName = commerceContext.get("communityName");
            if (communityName != null && !String.valueOf(communityName).trim().isEmpty()) {
                appendQueryParam(builder, "commName", String.valueOf(communityName).trim(), true);
            }
        }
        return builder.toString();
    }

    private void appendQueryParam(StringBuilder builder, String key, Object value) {
        appendQueryParam(builder, key, value, false);
    }

    private void appendQueryParam(StringBuilder builder, String key, Object value, boolean encode) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return;
        }
        builder.append("&").append(key).append("=");
        String text = String.valueOf(value).trim();
        if (encode) {
            try {
                builder.append(URLEncoder.encode(text, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                builder.append(text);
            }
        } else {
            builder.append(text);
        }
    }

    private void appendCommerceInfo(Map<String, Object> result, YgtGroupBuyCampaignEntity campaign, YgtWecomGroupEntity group) {
        Integer communityId = null;
        if (campaign != null && campaign.getYgtGbcNxCommunityId() != null) {
            communityId = campaign.getYgtGbcNxCommunityId();
        } else if (group != null && group.getYgtWgNxCommunityId() != null) {
            communityId = group.getYgtWgNxCommunityId();
        }
        if (communityId == null) {
            return;
        }
        result.put("communityId", communityId);
        NxECommerceCommunityEntity ecc = nxECommerceCommunityService.queryByCommunityId(communityId);
        if (ecc != null && ecc.getNxEccEId() != null) {
            result.put("commerceId", ecc.getNxEccEId());
        }
        if (ecc != null && ecc.getNxCommunityEntity() != null) {
            result.put("communityName", ecc.getNxCommunityEntity().getNxCommunityName());
        }
    }

    private YgtMemberShareInviteEntity requireActiveInvite(String shareCode) {
        if (isBlank(shareCode)) {
            throw new IllegalArgumentException("shareCode不能为空");
        }
        YgtMemberShareInviteEntity invite = ygtMemberShareInviteDao.queryByShareCode(shareCode.trim());
        if (invite == null) {
            throw new IllegalArgumentException("分享链接不存在");
        }
        if (!"ACTIVE".equals(invite.getYgtMsiStatus())) {
            throw new IllegalStateException("分享链接已失效");
        }
        if (invite.getYgtMsiExpireTime() != null && invite.getYgtMsiExpireTime().before(new Date())) {
            throw new IllegalStateException("分享链接已过期");
        }
        return invite;
    }

    private Map<String, Object> shareRow(YgtMemberShareInviteEntity invite) {
        Map<String, Object> row = new HashMap<>();
        row.put("shareCode", invite.getYgtMsiShareCode());
        row.put("sourceType", invite.getYgtMsiSourceType());
        row.put("campaignId", invite.getYgtMsiCampaignId());
        row.put("wecomGroupId", invite.getYgtMsiWecomGroupId());
        row.put("inviterCustomerUserId", invite.getYgtMsiInviterCustomerUserId());
        row.put("communityId", invite.getYgtMsiCommunityId());
        row.put("corpId", invite.getYgtMsiCorpId());
        row.put("chatId", invite.getYgtMsiChatId());
        row.put("status", invite.getYgtMsiStatus());
        row.put("expireTime", invite.getYgtMsiExpireTime());
        return row;
    }

    private String nextShareCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = randomCode();
            if (ygtMemberShareInviteDao.queryByShareCode(code) == null) {
                return code;
            }
        }
        throw new IllegalStateException("生成分享码失败，请重试");
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder("YGT");
        for (int i = 0; i < 9; i++) {
            builder.append(SHARE_CODE_ALPHABET.charAt(SECURE_RANDOM.nextInt(SHARE_CODE_ALPHABET.length())));
        }
        return builder.toString();
    }

    private Date daysAfter(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    private Date dateOrDefault(Object value, Date defaultValue) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }
        return defaultValue;
    }

    private String maskPhone(String phone) {
        if (isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String requiredString(Map<String, Object> body, String key) {
        String value = stringValue(body.get(key));
        if (isBlank(value)) {
            throw new IllegalArgumentException(key + "不能为空");
        }
        return value;
    }

    private Integer intValue(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private Long longValue(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

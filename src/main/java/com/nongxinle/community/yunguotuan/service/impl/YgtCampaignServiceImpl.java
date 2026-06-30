package com.nongxinle.community.yunguotuan.service.impl;

import com.nongxinle.dao.*;
import com.nongxinle.entity.NxCommunityGoodsEntity;
import com.nongxinle.community.yunguotuan.entity.*;
import com.nongxinle.community.yunguotuan.service.YgtCampaignService;
import com.nongxinle.community.yunguotuan.service.YgtMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class YgtCampaignServiceImpl implements YgtCampaignService {
    @Autowired
    private YgtGroupBuyCampaignDao ygtGroupBuyCampaignDao;

    @Autowired
    private YgtCampaignGoodsDao ygtCampaignGoodsDao;

    @Autowired
    private YgtWecomGroupDao ygtWecomGroupDao;

    @Autowired
    private YgtGroupOrderDao ygtGroupOrderDao;

    @Autowired
    private YgtGroupOrderItemDao ygtGroupOrderItemDao;

    @Autowired
    private YgtOrderCandidateDao ygtOrderCandidateDao;

    @Autowired
    private NxCommunityGoodsDao nxCommunityGoodsDao;

    @Autowired
    private YgtChatMessageDao ygtChatMessageDao;

    @Autowired
    private YgtMessageService ygtMessageService;

    @Override
    @Transactional
    public Map<String, Object> createCampaign(Map<String, Object> body) {
        Long groupId = longValue(body.get("wecomGroupId"));
        if (groupId == null) {
            throw new IllegalArgumentException("wecomGroupId不能为空");
        }
        YgtWecomGroupEntity group = ygtWecomGroupDao.queryObject(groupId);
        if (group == null) {
            throw new IllegalArgumentException("客户群不存在");
        }

        Date now = new Date();
        YgtGroupBuyCampaignEntity campaign = new YgtGroupBuyCampaignEntity();
        campaign.setYgtGbcCorpId(group.getYgtWgCorpId());
        campaign.setYgtGbcGroupId(groupId);
        campaign.setYgtGbcNxCommunityId(intOrDefault(body.get("communityId"), group.getYgtWgNxCommunityId()));
        campaign.setYgtGbcTitle(requiredString(body, "campaignName"));
        campaign.setYgtGbcStatus("DRAFT");
        campaign.setYgtGbcPickupTime(dateValue(body.get("pickupTime")));
        campaign.setYgtGbcRemark(stringValue(body.get("remark")));
        campaign.setYgtGbcCreateUserId(intValue(body.get("createUserId")));
        campaign.setYgtGbcCreateTime(now);
        campaign.setYgtGbcUpdateTime(now);
        ygtGroupBuyCampaignDao.save(campaign);
        return campaignRow(campaign);
    }

    @Override
    @Transactional
    public Map<String, Object> addGoods(Long campaignId, Map<String, Object> body) {
        YgtGroupBuyCampaignEntity campaign = requireCampaign(campaignId);
        if (!"DRAFT".equals(campaign.getYgtGbcStatus()) && !"OPEN".equals(campaign.getYgtGbcStatus())) {
            throw new IllegalStateException("当前团期不可添加商品");
        }

        Integer nxGoodsId = intValue(body.get("nxCommunityGoodsId"));
        NxCommunityGoodsEntity nxGoods = nxGoodsId == null ? null : nxCommunityGoodsDao.queryObject(nxGoodsId);
        Date now = new Date();
        YgtCampaignGoodsEntity goods = new YgtCampaignGoodsEntity();
        goods.setYgtCgCampaignId(campaignId);
        goods.setYgtCgNxCommunityGoodsId(nxGoodsId);
        goods.setYgtCgGoodsNameSnapshot(firstNonBlank(stringValue(body.get("goodsNameSnapshot")),
                nxGoods == null ? null : nxGoods.getNxCgGoodsName()));
        goods.setYgtCgStandardSnapshot(firstNonBlank(stringValue(body.get("specSnapshot")),
                nxGoods == null ? null : nxGoods.getNxCgGoodsStandardname()));
        goods.setYgtCgUnitSnapshot(stringValue(body.get("unitSnapshot")));
        goods.setYgtCgPriceSnapshot(decimalOrDefault(body.get("priceSnapshot"),
                nxGoods == null ? null : nxGoods.getNxCgGoodsPrice()));
        goods.setYgtCgSort(intValue(body.get("sort")) == null ? 0 : intValue(body.get("sort")));
        goods.setYgtCgStatus(parseGoodsStatus(body.get("status")));
        goods.setYgtCgCreateTime(now);
        goods.setYgtCgUpdateTime(now);
        if (isBlank(goods.getYgtCgGoodsNameSnapshot())) {
            throw new IllegalArgumentException("商品快照名称不能为空");
        }
        ygtCampaignGoodsDao.save(goods);
        return goodsRow(goods);
    }

    @Override
    @Transactional
    public Map<String, Object> openCampaign(Long campaignId) {
        YgtGroupBuyCampaignEntity campaign = requireCampaign(campaignId);
        YgtGroupBuyCampaignEntity open = ygtGroupBuyCampaignDao.queryOpenByGroupId(campaign.getYgtGbcGroupId());
        if (open != null && !open.getYgtGroupBuyCampaignId().equals(campaignId)) {
            throw new IllegalStateException("该客户群已有进行中的团期");
        }
        campaign.setYgtGbcStatus("OPEN");
        if (campaign.getYgtGbcOpenTime() == null) {
            campaign.setYgtGbcOpenTime(new Date());
        }
        ygtGroupBuyCampaignDao.update(campaign);
        return campaignRow(ygtGroupBuyCampaignDao.queryObject(campaignId));
    }

    @Override
    @Transactional
    public Map<String, Object> closeCampaign(Long campaignId) {
        YgtGroupBuyCampaignEntity campaign = requireCampaign(campaignId);
        campaign.setYgtGbcStatus("CLOSED");
        campaign.setYgtGbcCloseTime(new Date());
        ygtGroupBuyCampaignDao.update(campaign);
        return campaignRow(ygtGroupBuyCampaignDao.queryObject(campaignId));
    }

    @Override
    public List<Map<String, Object>> currentCampaigns(Long groupId, String corpId) {
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("corpId", corpId);
        params.put("status", "OPEN");
        List<YgtGroupBuyCampaignEntity> campaigns = ygtGroupBuyCampaignDao.queryList(params);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (YgtGroupBuyCampaignEntity campaign : campaigns) {
            rows.add(campaignRow(campaign));
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> listCampaigns(Map<String, Object> params) {
        List<YgtGroupBuyCampaignEntity> campaigns = ygtGroupBuyCampaignDao.queryList(params);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (YgtGroupBuyCampaignEntity campaign : campaigns) {
            rows.add(campaignRow(campaign));
        }
        return rows;
    }

    @Override
    public Map<String, Object> campaignSummary(Long campaignId) {
        YgtGroupBuyCampaignEntity campaign = requireCampaign(campaignId);
        Map<String, Object> summary = new HashMap<>();
        summary.put("campaignId", campaignId);
        summary.put("campaignName", campaign.getYgtGbcTitle());
        summary.put("status", campaign.getYgtGbcStatus());

        // 商品级汇总（基于 ygt_group_order / ygt_group_order_item）
        List<Map<String, Object>> goodsSummary = ygtGroupOrderItemDao.queryCampaignGoodsSummary(campaignId);
        summary.put("goodsSummary", goodsSummary);

        // 订单级汇总
        Map<String, Object> orderSummary = ygtGroupOrderDao.queryCampaignOrderSummary(campaignId);
        if (orderSummary != null) {
            summary.put("orderCount", orderSummary.get("orderCount"));
            summary.put("customerCount", orderSummary.get("customerCount"));
            summary.put("totalAmount", orderSummary.get("totalAmount"));
        } else {
            summary.put("orderCount", 0);
            summary.put("customerCount", 0);
            summary.put("totalAmount", BigDecimal.ZERO);
        }

        // 候选单统计
        summary.put("pendingReviewCount", ygtOrderCandidateDao.countByCampaignAndStatus(campaignId, "PENDING_REVIEW"));
        summary.put("parsedCount", ygtOrderCandidateDao.countByCampaignAndStatus(campaignId, "PARSED"));
        summary.put("failedCount", ygtOrderCandidateDao.countByCampaignAndStatus(campaignId, "FAILED"));
        summary.put("ignoredCount", ygtOrderCandidateDao.countByCampaignAndStatus(campaignId, "IGNORED"));
        summary.put("confirmedCount", ygtOrderCandidateDao.countByCampaignAndStatus(campaignId, "CONFIRMED"));

        // 团期商品列表（以便知晓本团有哪些商品可选）
        Map<String, Object> goodsParams = new HashMap<>();
        goodsParams.put("campaignId", campaignId);
        List<YgtCampaignGoodsEntity> goodsList = ygtCampaignGoodsDao.queryList(goodsParams);
        List<Map<String, Object>> goodsRows = new ArrayList<>();
        for (YgtCampaignGoodsEntity g : goodsList) {
            goodsRows.add(goodsRow(g));
        }
        summary.put("campaignGoods", goodsRows);

        return summary;
    }

    @Override
    @Transactional
    public Map<String, Object> createTestMessageForCandidate(Long groupId, String content) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId不能为空");
        }
        YgtWecomGroupEntity group = ygtWecomGroupDao.queryObject(groupId);
        if (group == null) {
            throw new IllegalArgumentException("客户群不存在");
        }
        Date now = new Date();
        long seq = System.currentTimeMillis();
        String msgId = "TEST_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        YgtChatMessageEntity msg = new YgtChatMessageEntity();
        msg.setYgtCmCorpId(group.getYgtWgCorpId());
        msg.setYgtCmGroupId(groupId);
        msg.setYgtCmChatId(group.getYgtWgChatId());
        msg.setYgtCmMsgId(msgId);
        msg.setYgtCmSeq(seq);
        msg.setYgtCmPublicKeyVer(0);
        msg.setYgtCmAction("send");
        msg.setYgtCmFromUser("test_user");
        msg.setYgtCmMsgTime(now.getTime());
        msg.setYgtCmMsgType("text");
        msg.setYgtCmContent(isBlank(content) ? "测试消息 " + now.getTime() : content);
        msg.setYgtCmRawJson("{\"mock\":true}");
        msg.setYgtCmParseStatus("NEW");
        msg.setYgtCmCreateTime(now);
        msg.setYgtCmUpdateTime(now);
        ygtChatMessageDao.save(msg);

        Map<String, Object> parseResult = ygtMessageService.parseMessage(msg.getYgtChatMessageId());

        Map<String, Object> result = new HashMap<>();
        result.put("messageId", msg.getYgtChatMessageId());
        result.put("msgId", msgId);
        result.put("seq", seq);
        result.put("parseResult", parseResult);
        return result;
    }

    private YgtGroupBuyCampaignEntity requireCampaign(Long campaignId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId不能为空");
        }
        YgtGroupBuyCampaignEntity campaign = ygtGroupBuyCampaignDao.queryObject(campaignId);
        if (campaign == null) {
            throw new IllegalArgumentException("团期不存在");
        }
        return campaign;
    }

    private Map<String, Object> campaignRow(YgtGroupBuyCampaignEntity campaign) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", campaign.getYgtGroupBuyCampaignId());
        row.put("corpId", campaign.getYgtGbcCorpId());
        row.put("wecomGroupId", campaign.getYgtGbcGroupId());
        row.put("communityId", campaign.getYgtGbcNxCommunityId());
        row.put("campaignName", campaign.getYgtGbcTitle());
        row.put("status", campaign.getYgtGbcStatus());
        row.put("openTime", campaign.getYgtGbcOpenTime());
        row.put("closeTime", campaign.getYgtGbcCloseTime());
        row.put("pickupTime", campaign.getYgtGbcPickupTime());
        row.put("remark", campaign.getYgtGbcRemark());
        if (campaign.getYgtGbcGroupId() != null) {
            YgtWecomGroupEntity group = ygtWecomGroupDao.queryObject(campaign.getYgtGbcGroupId());
            if (group != null) {
                String chatName = isBlank(group.getYgtWgChatName()) ? "未命名群" : group.getYgtWgChatName();
                row.put("wecomGroupName", chatName);
                row.put("groupName", chatName);
            }
        }
        return row;
    }

    private Map<String, Object> goodsRow(YgtCampaignGoodsEntity goods) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", goods.getYgtCampaignGoodsId());
        row.put("campaignId", goods.getYgtCgCampaignId());
        row.put("nxCommunityGoodsId", goods.getYgtCgNxCommunityGoodsId());
        row.put("goodsNameSnapshot", goods.getYgtCgGoodsNameSnapshot());
        row.put("specSnapshot", goods.getYgtCgStandardSnapshot());
        row.put("unitSnapshot", goods.getYgtCgUnitSnapshot());
        row.put("priceSnapshot", goods.getYgtCgPriceSnapshot());
        row.put("sort", goods.getYgtCgSort());
        row.put("status", ygtCgStatusLabel(goods.getYgtCgStatus()));
        return row;
    }

    private String requiredString(Map<String, Object> body, String key) {
        String value = stringValue(body.get(key));
        if (isBlank(value)) {
            throw new IllegalArgumentException(key + "不能为空");
        }
        return value;
    }

    private String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private Integer intOrDefault(Object value, Integer defaultValue) {
        Integer parsed = intValue(value);
        return parsed == null ? defaultValue : parsed;
    }

    private Integer intValue(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private Integer parseGoodsStatus(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return 1;
        }
        String text = String.valueOf(value).trim();
        if ("ACTIVE".equalsIgnoreCase(text) || "1".equals(text)) {
            return 1;
        }
        if ("DISABLED".equalsIgnoreCase(text) || "0".equals(text)) {
            return 0;
        }
        throw new IllegalArgumentException("status值无效: " + text + "，支持 ACTIVE/DISABLED 或 1/0");
    }

    private String ygtCgStatusLabel(Integer status) {
        return status == null || status == 0 ? "DISABLED" : "ACTIVE";
    }

    private Long longValue(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    private BigDecimal decimalOrDefault(Object value, String defaultValue) {
        Object source = value == null ? defaultValue : value;
        if (source == null || String.valueOf(source).trim().isEmpty()) {
            return null;
        }
        return new BigDecimal(String.valueOf(source));
    }

    private Date dateValue(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return null;
        }
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }
        String text = String.valueOf(value).trim();
        List<String> patterns = Arrays.asList("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd");
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern).parse(text);
            } catch (ParseException ignored) {
            }
        }
        throw new IllegalArgumentException("日期格式不正确: " + text);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

package com.nongxinle.community.yunguotuan.service.impl;

import com.nongxinle.dao.*;
import com.nongxinle.community.yunguotuan.entity.*;
import com.nongxinle.community.yunguotuan.service.YgtOrderCandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class YgtOrderCandidateServiceImpl implements YgtOrderCandidateService {
    @Autowired
    private YgtOrderCandidateDao ygtOrderCandidateDao;

    @Autowired
    private YgtGroupBuyCampaignDao ygtGroupBuyCampaignDao;

    @Autowired
    private YgtOrderCandidateItemDao ygtOrderCandidateItemDao;

    @Autowired
    private YgtGroupOrderDao ygtGroupOrderDao;

    @Autowired
    private YgtGroupOrderItemDao ygtGroupOrderItemDao;

    @Autowired
    private YgtCampaignGoodsDao ygtCampaignGoodsDao;

    @Override
    public YgtOrderCandidateEntity createPlaceholderCandidate(YgtChatMessageEntity message) {
        YgtOrderCandidateEntity existing = ygtOrderCandidateDao.queryByMessageId(message.getYgtChatMessageId());
        if (existing != null) {
            return existing;
        }
        Date now = new Date();
        YgtGroupBuyCampaignEntity campaign = ygtGroupBuyCampaignDao.queryOpenByGroupId(message.getYgtCmGroupId());

        YgtOrderCandidateEntity candidate = new YgtOrderCandidateEntity();
        candidate.setYgtOcCorpId(message.getYgtCmCorpId());
        candidate.setYgtOcGroupId(message.getYgtCmGroupId());
        candidate.setYgtOcChatId(message.getYgtCmChatId());
        candidate.setYgtOcMessageId(message.getYgtChatMessageId());
        candidate.setYgtOcCampaignId(campaign == null ? null : campaign.getYgtGroupBuyCampaignId());
        candidate.setYgtOcFromUser(message.getYgtCmFromUser());
        candidate.setYgtOcMemberIdentifier(message.getYgtCmFromUser());
        candidate.setYgtOcMsgTime(message.getYgtCmMsgTime());
        candidate.setYgtOcOriginalText(message.getYgtCmContent());
        candidate.setYgtOcParseStatus(campaign == null ? "NO_OPEN_CAMPAIGN" : "PLACEHOLDER");
        candidate.setYgtOcStatus("PENDING_REVIEW");
        candidate.setYgtOcCreateTime(now);
        candidate.setYgtOcUpdateTime(now);
        ygtOrderCandidateDao.save(candidate);
        return candidate;
    }

    @Override
    public List<YgtOrderCandidateEntity> queryCandidates(Map<String, Object> params) {
        return ygtOrderCandidateDao.queryList(params);
    }

    @Override
    public Map<String, Object> candidateDetail(Long id) {
        return candidateDetailInternal(id);
    }

    @Override
    @Transactional
    public Map<String, Object> editCandidate(Long id, Map<String, Object> body) {
        YgtOrderCandidateEntity candidate = requireCandidate(id);
        if ("CONFIRMED".equals(candidate.getYgtOcStatus())) {
            throw new IllegalStateException("已确认候选单不可编辑");
        }
        candidate.setYgtOcCampaignId(longOrDefault(body.get("campaignId"), candidate.getYgtOcCampaignId()));
        candidate.setYgtOcExternalUserId(firstNonBlank(stringValue(body.get("externalUserId")), candidate.getYgtOcExternalUserId()));
        candidate.setYgtOcCustomerNameSnapshot(firstNonBlank(stringValue(body.get("customerNameSnapshot")), candidate.getYgtOcCustomerNameSnapshot()));
        candidate.setYgtOcMemberIdentifier(firstNonBlank(stringValue(body.get("memberIdentifier")), candidate.getYgtOcMemberIdentifier()));
        candidate.setYgtOcReviewRemark(stringValue(body.get("remark")));
        candidate.setYgtOcParseStatus("PARSED");
        candidate.setYgtOcStatus("PARSED");
        ygtOrderCandidateDao.update(candidate);

        Object itemsObj = body.get("items");
        if (itemsObj instanceof List) {
            ygtOrderCandidateItemDao.deleteByCandidateId(id);
            List<?> items = (List<?>) itemsObj;
            for (Object itemObj : items) {
                if (itemObj instanceof Map) {
                    saveCandidateItem(id, (Map<?, ?>) itemObj);
                }
            }
        }

        return candidateDetailInternal(id);
    }

    @Override
    @Transactional
    public Map<String, Object> ignoreCandidate(Long id, Map<String, Object> body) {
        YgtOrderCandidateEntity candidate = requireCandidate(id);
        if ("CONFIRMED".equals(candidate.getYgtOcStatus())) {
            throw new IllegalStateException("已确认候选单不可忽略");
        }
        candidate.setYgtOcStatus("IGNORED");
        candidate.setYgtOcReviewRemark(body == null ? null : stringValue(body.get("remark")));
        ygtOrderCandidateDao.update(candidate);
        return candidateDetailInternal(id);
    }

    @Override
    @Transactional
    public Map<String, Object> restoreCandidate(Long id) {
        YgtOrderCandidateEntity candidate = requireCandidate(id);
        if (!"IGNORED".equals(candidate.getYgtOcStatus())) {
            throw new IllegalStateException("只有已忽略候选单可以恢复");
        }
        candidate.setYgtOcStatus("PENDING_REVIEW");
        candidate.setYgtOcReviewRemark(null);
        ygtOrderCandidateDao.update(candidate);
        return candidateDetailInternal(id);
    }

    @Override
    @Transactional
    public Map<String, Object> confirmCandidate(Long id, Map<String, Object> body) {
        YgtGroupOrderEntity existing = ygtGroupOrderDao.queryByCandidateId(id);
        if (existing != null) {
            return orderResult(existing, false);
        }

        YgtOrderCandidateEntity candidate = requireCandidateForUpdate(id);
        existing = ygtGroupOrderDao.queryByCandidateId(id);
        if (existing != null) {
            return orderResult(existing, false);
        }

        if ("CONFIRMED".equals(candidate.getYgtOcStatus())) {
            throw new IllegalStateException("候选单已确认但未找到正式订单，请人工处理");
        }
        if (!isConfirmableStatus(candidate.getYgtOcStatus())) {
            throw new IllegalStateException("当前候选单状态不可确认: " + candidate.getYgtOcStatus());
        }
        if (candidate.getYgtOcCampaignId() == null) {
            throw new IllegalStateException("候选单未关联团期");
        }
        YgtGroupBuyCampaignEntity campaign = ygtGroupBuyCampaignDao.queryObject(candidate.getYgtOcCampaignId());
        if (campaign == null || !"OPEN".equals(campaign.getYgtGbcStatus())) {
            throw new IllegalStateException("候选单关联的团期不是OPEN状态");
        }
        List<YgtOrderCandidateItemEntity> items = ygtOrderCandidateItemDao.queryByCandidateId(id);
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("候选单没有明细，不能确认");
        }
        validateCandidateItems(items);

        Date now = new Date();
        Integer confirmUserId = resolveConfirmUserId(body);
        String confirmRemark = resolveConfirmRemark(body, candidate.getYgtOcReviewRemark());
        BigDecimal total = BigDecimal.ZERO;
        for (YgtOrderCandidateItemEntity item : items) {
            total = total.add(resolveAmount(item));
        }

        YgtGroupOrderEntity order = new YgtGroupOrderEntity();
        order.setYgtGoCandidateId(id);
        order.setYgtGoCampaignId(candidate.getYgtOcCampaignId());
        order.setYgtGoCorpId(candidate.getYgtOcCorpId());
        order.setYgtGoGroupId(candidate.getYgtOcGroupId());
        order.setYgtGoNxCommunityId(campaign.getYgtGbcNxCommunityId());
        order.setYgtGoChatId(candidate.getYgtOcChatId());
        order.setYgtGoSourceChatMessageId(candidate.getYgtOcMessageId());
        order.setYgtGoFromUser(candidate.getYgtOcFromUser());
        order.setYgtGoMemberIdentifier(candidate.getYgtOcMemberIdentifier());
        order.setYgtGoExternalUserId(candidate.getYgtOcExternalUserId());
        order.setYgtGoCustomerNameSnapshot(candidate.getYgtOcCustomerNameSnapshot());
        order.setYgtGoStatus("CONFIRMED");
        order.setYgtGoTotalAmount(total);
        order.setYgtGoConfirmUserId(confirmUserId);
        order.setYgtGoConfirmTime(now);
        order.setYgtGoRemark(confirmRemark);
        order.setYgtGoCreateTime(now);
        order.setYgtGoUpdateTime(now);
        try {
            ygtGroupOrderDao.save(order);
        } catch (DuplicateKeyException duplicate) {
            YgtGroupOrderEntity raced = ygtGroupOrderDao.queryByCandidateId(id);
            if (raced != null) {
                return orderResult(raced, false);
            }
            throw duplicate;
        }

        for (YgtOrderCandidateItemEntity item : items) {
            YgtCampaignGoodsEntity campaignGoods = item.getYgtOciCampaignGoodsId() == null
                    ? null
                    : ygtCampaignGoodsDao.queryObject(item.getYgtOciCampaignGoodsId());
            YgtGroupOrderItemEntity orderItem = new YgtGroupOrderItemEntity();
            orderItem.setYgtGoiOrderId(order.getYgtGroupOrderId());
            orderItem.setYgtGoiCampaignId(candidate.getYgtOcCampaignId());
            orderItem.setYgtGoiCandidateItemId(item.getYgtOrderCandidateItemId());
            orderItem.setYgtGoiCampaignGoodsId(item.getYgtOciCampaignGoodsId());
            orderItem.setYgtGoiNxCommunityGoodsId(item.getYgtOciNxCommunityGoodsId());
            orderItem.setYgtGoiGoodsNameSnapshot(item.getYgtOciGoodsNameSnapshot());
            orderItem.setYgtGoiSpecSnapshot(campaignGoods == null ? null : campaignGoods.getYgtCgStandardSnapshot());
            orderItem.setYgtGoiQuantity(item.getYgtOciQuantity());
            orderItem.setYgtGoiUnit(item.getYgtOciUnit());
            orderItem.setYgtGoiUnitSnapshot(firstNonBlank(
                    campaignGoods == null ? null : campaignGoods.getYgtCgUnitSnapshot(),
                    item.getYgtOciUnit()));
            orderItem.setYgtGoiPriceSnapshot(item.getYgtOciPriceSnapshot());
            orderItem.setYgtGoiAmount(resolveAmount(item));
            orderItem.setYgtGoiRemark(item.getYgtOciRemark());
            orderItem.setYgtGoiCreateTime(now);
            ygtGroupOrderItemDao.save(orderItem);
        }

        candidate.setYgtOcStatus("CONFIRMED");
        candidate.setYgtOcReviewerUserId(confirmUserId);
        candidate.setYgtOcReviewedTime(now);
        candidate.setYgtOcReviewRemark(confirmRemark);
        ygtOrderCandidateDao.update(candidate);
        return orderResult(order, true);
    }

    private Integer resolveConfirmUserId(Map<String, Object> body) {
        if (body == null) {
            return null;
        }
        Integer reviewerUserId = intValue(body.get("reviewerUserId"));
        return reviewerUserId == null ? intValue(body.get("confirmUserId")) : reviewerUserId;
    }

    private String resolveConfirmRemark(Map<String, Object> body, String defaultRemark) {
        if (body == null) {
            return defaultRemark;
        }
        return firstNonBlank(firstNonBlank(stringValue(body.get("reviewRemark")), stringValue(body.get("remark"))), defaultRemark);
    }

    private void validateCandidateItems(List<YgtOrderCandidateItemEntity> items) {
        for (YgtOrderCandidateItemEntity item : items) {
            if (isBlank(item.getYgtOciGoodsNameSnapshot())) {
                throw new IllegalStateException("候选明细商品名称不能为空");
            }
            if (item.getYgtOciQuantity() == null || item.getYgtOciQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("候选明细数量必须大于0");
            }
            if (item.getYgtOciAmount() == null && item.getYgtOciPriceSnapshot() == null) {
                throw new IllegalStateException("候选明细金额为空时必须提供价格快照");
            }
        }
    }

    private boolean isConfirmableStatus(String status) {
        return "PENDING_REVIEW".equals(status) || "PARSED".equals(status);
    }

    private void saveCandidateItem(Long candidateId, Map<?, ?> itemMap) {
        Date now = new Date();
        Long campaignGoodsId = longValue(itemMap.get("campaignGoodsId"));
        YgtCampaignGoodsEntity campaignGoods = campaignGoodsId == null ? null : ygtCampaignGoodsDao.queryObject(campaignGoodsId);
        BigDecimal quantity = decimalOrDefault(itemMap.get("quantity"), BigDecimal.ONE);
        BigDecimal price = decimalOrDefault(itemMap.get("priceSnapshot"),
                campaignGoods == null ? BigDecimal.ZERO : campaignGoods.getYgtCgPriceSnapshot());

        YgtOrderCandidateItemEntity item = new YgtOrderCandidateItemEntity();
        item.setYgtOciCandidateId(candidateId);
        item.setYgtOciCampaignGoodsId(campaignGoodsId);
        item.setYgtOciNxCommunityGoodsId(intOrDefault(itemMap.get("nxCommunityGoodsId"),
                campaignGoods == null ? null : campaignGoods.getYgtCgNxCommunityGoodsId()));
        item.setYgtOciGoodsNameSnapshot(firstNonBlank(stringValue(itemMap.get("goodsNameSnapshot")),
                campaignGoods == null ? null : campaignGoods.getYgtCgGoodsNameSnapshot()));
        item.setYgtOciQuantity(quantity);
        item.setYgtOciUnit(firstNonBlank(stringValue(itemMap.get("unit")),
                campaignGoods == null ? null : campaignGoods.getYgtCgStandardSnapshot()));
        item.setYgtOciPriceSnapshot(price);
        item.setYgtOciAmount(decimalOrDefault(itemMap.get("amount"), quantity.multiply(price)));
        item.setYgtOciRemark(stringValue(itemMap.get("remark")));
        item.setYgtOciConfidence(decimalOrDefault(itemMap.get("confidence"), null));
        item.setYgtOciManualAdjusted(1);
        item.setYgtOciCreateTime(now);
        item.setYgtOciUpdateTime(now);
        if (isBlank(item.getYgtOciGoodsNameSnapshot())) {
            throw new IllegalArgumentException("候选明细商品名称不能为空");
        }
        ygtOrderCandidateItemDao.save(item);
    }

    private Map<String, Object> candidateDetailInternal(Long id) {
        YgtOrderCandidateEntity candidate = ygtOrderCandidateDao.queryObject(id);
        Map<String, Object> row = new HashMap<>();
        row.put("candidate", candidateRow(candidate));
        row.put("items", ygtOrderCandidateItemDao.queryByCandidateId(id));
        return row;
    }

    private Map<String, Object> candidateRow(YgtOrderCandidateEntity candidate) {
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
        row.put("reviewRemark", candidate.getYgtOcReviewRemark());
        row.put("reviewerUserId", candidate.getYgtOcReviewerUserId());
        row.put("reviewedTime", candidate.getYgtOcReviewedTime());
        row.put("originalTextPreview", preview(candidate.getYgtOcOriginalText()));
        row.put("createTime", candidate.getYgtOcCreateTime());
        row.put("updateTime", candidate.getYgtOcUpdateTime());
        return row;
    }

    private Map<String, Object> orderResult(YgtGroupOrderEntity order, boolean created) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getYgtGroupOrderId());
        result.put("created", created);
        result.put("existed", !created);
        result.put("order", order);
        result.put("items", ygtGroupOrderItemDao.queryByOrderId(order.getYgtGroupOrderId()));
        return result;
    }

    private BigDecimal resolveAmount(YgtOrderCandidateItemEntity item) {
        if (item.getYgtOciAmount() != null) {
            return item.getYgtOciAmount();
        }
        BigDecimal quantity = item.getYgtOciQuantity() == null ? BigDecimal.ZERO : item.getYgtOciQuantity();
        BigDecimal price = item.getYgtOciPriceSnapshot() == null ? BigDecimal.ZERO : item.getYgtOciPriceSnapshot();
        return quantity.multiply(price);
    }

    private YgtOrderCandidateEntity requireCandidate(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("candidateId不能为空");
        }
        YgtOrderCandidateEntity candidate = ygtOrderCandidateDao.queryObject(id);
        if (candidate == null) {
            throw new IllegalArgumentException("候选单不存在");
        }
        return candidate;
    }

    private YgtOrderCandidateEntity requireCandidateForUpdate(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("candidateId不能为空");
        }
        YgtOrderCandidateEntity candidate = ygtOrderCandidateDao.queryObjectForUpdate(id);
        if (candidate == null) {
            throw new IllegalArgumentException("候选单不存在");
        }
        return candidate;
    }

    private String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private Long longOrDefault(Object value, Long defaultValue) {
        Long parsed = longValue(value);
        return parsed == null ? defaultValue : parsed;
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

    private Long longValue(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    private BigDecimal decimalOrDefault(Object value, BigDecimal defaultValue) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return defaultValue;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String preview(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() > 80 ? normalized.substring(0, 80) : normalized;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

package com.nongxinle.community.yunguotuan.service.impl;

import com.nongxinle.dao.YgtGroupOrderDao;
import com.nongxinle.dao.YgtGroupOrderItemDao;
import com.nongxinle.community.yunguotuan.entity.YgtGroupOrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nongxinle.community.yunguotuan.service.YgtGroupOrderService;

import java.util.*;

@Service
public class YgtGroupOrderServiceImpl implements YgtGroupOrderService {
    @Autowired
    private YgtGroupOrderDao ygtGroupOrderDao;

    @Autowired
    private YgtGroupOrderItemDao ygtGroupOrderItemDao;

    @Override
    public List<Map<String, Object>> queryOrders(Map<String, Object> params) {
        List<YgtGroupOrderEntity> orders = ygtGroupOrderDao.queryList(params);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (YgtGroupOrderEntity order : orders) {
            rows.add(orderRow(order, false));
        }
        return rows;
    }

    @Override
    public Map<String, Object> orderDetail(Long id) {
        YgtGroupOrderEntity order = ygtGroupOrderDao.queryObject(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return orderRow(order, true);
    }

    private Map<String, Object> orderRow(YgtGroupOrderEntity order, boolean includeItems) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", order.getYgtGroupOrderId());
        row.put("candidateId", order.getYgtGoCandidateId());
        row.put("campaignId", order.getYgtGoCampaignId());
        row.put("wecomGroupId", order.getYgtGoGroupId());
        row.put("communityId", order.getYgtGoNxCommunityId());
        row.put("chatId", order.getYgtGoChatId());
        row.put("sourceChatMessageId", order.getYgtGoSourceChatMessageId());
        row.put("fromUser", order.getYgtGoFromUser());
        row.put("externalUserId", order.getYgtGoExternalUserId());
        row.put("customerNameSnapshot", order.getYgtGoCustomerNameSnapshot());
        row.put("status", order.getYgtGoStatus());
        row.put("totalAmount", order.getYgtGoTotalAmount());
        row.put("confirmUserId", order.getYgtGoConfirmUserId());
        row.put("confirmTime", order.getYgtGoConfirmTime());
        row.put("remark", order.getYgtGoRemark());
        row.put("createTime", order.getYgtGoCreateTime());
        if (includeItems) {
            row.put("items", ygtGroupOrderItemDao.queryByOrderId(order.getYgtGroupOrderId()));
        }
        return row;
    }
}

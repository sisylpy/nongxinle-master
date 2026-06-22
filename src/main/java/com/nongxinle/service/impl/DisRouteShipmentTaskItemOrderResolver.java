package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDepartmentDao;
import com.nongxinle.dao.NxDepartmentOrderHistoryDao;
import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDepartmentOrderHistoryEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDisShipmentTaskEntity;
import com.nongxinle.entity.NxDisShipmentTaskItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 派车 task_item 读模型：商品名/数量/规格/状态/送达日期从 live 或 history 订单解析，不落库。
 */
@Component
public class DisRouteShipmentTaskItemOrderResolver {

    @Autowired
    private NxDepartmentOrdersDao nxDepartmentOrdersDao;
    @Autowired
    private NxDepartmentOrderHistoryDao nxDepartmentOrderHistoryDao;

    public void enrichTasks(List<NxDisShipmentTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        List<NxDisShipmentTaskItemEntity> allItems = new ArrayList<NxDisShipmentTaskItemEntity>();
        for (NxDisShipmentTaskEntity task : tasks) {
            if (task.getItems() != null) {
                allItems.addAll(task.getItems());
            }
        }
        enrichItems(allItems);
    }

    public void enrichItems(List<NxDisShipmentTaskItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        Set<Integer> liveIds = new HashSet<Integer>();
        Set<Integer> historyIds = new HashSet<Integer>();
        for (NxDisShipmentTaskItemEntity item : items) {
            if (item == null) {
                continue;
            }
            if (item.getNxDstiHistoryOrderId() != null) {
                historyIds.add(item.getNxDstiHistoryOrderId());
            } else if (item.getNxDstiLiveOrderId() != null) {
                liveIds.add(item.getNxDstiLiveOrderId());
            }
        }

        Map<Integer, NxDepartmentOrdersEntity> liveById = loadLiveOrders(liveIds);
        Map<Integer, NxDepartmentOrderHistoryEntity> historyById = loadHistoryOrders(historyIds);

        for (NxDisShipmentTaskItemEntity item : items) {
            if (item == null) {
                continue;
            }
            if (item.getNxDstiHistoryOrderId() != null) {
                applyHistoryOrder(item, historyById.get(item.getNxDstiHistoryOrderId()));
            } else if (item.getNxDstiLiveOrderId() != null) {
                applyLiveOrder(item, liveById.get(item.getNxDstiLiveOrderId()));
            }
        }
    }

    private Map<Integer, NxDepartmentOrdersEntity> loadLiveOrders(Set<Integer> liveIds) {
        Map<Integer, NxDepartmentOrdersEntity> map = new HashMap<Integer, NxDepartmentOrdersEntity>();
        if (liveIds.isEmpty()) {
            return map;
        }
        List<NxDepartmentOrdersEntity> rows = nxDepartmentOrdersDao.queryByOrderIds(new ArrayList<Integer>(liveIds));
        if (rows == null) {
            return map;
        }
        for (NxDepartmentOrdersEntity row : rows) {
            if (row != null && row.getNxDepartmentOrdersId() != null) {
                map.put(row.getNxDepartmentOrdersId(), row);
            }
        }
        return map;
    }

    private Map<Integer, NxDepartmentOrderHistoryEntity> loadHistoryOrders(Set<Integer> historyIds) {
        Map<Integer, NxDepartmentOrderHistoryEntity> map = new HashMap<Integer, NxDepartmentOrderHistoryEntity>();
        if (historyIds.isEmpty()) {
            return map;
        }
        List<NxDepartmentOrderHistoryEntity> rows = nxDepartmentOrderHistoryDao.queryByOrderIds(
                new ArrayList<Integer>(historyIds));
        if (rows == null) {
            return map;
        }
        for (NxDepartmentOrderHistoryEntity row : rows) {
            if (row != null && row.getNxDepartmentOrdersId() != null) {
                map.put(row.getNxDepartmentOrdersId(), row);
            }
        }
        return map;
    }

    private void applyLiveOrder(NxDisShipmentTaskItemEntity item, NxDepartmentOrdersEntity order) {
        if (order == null) {
            item.setOrderResolved(false);
            return;
        }
        item.setOrderResolved(true);
        item.setResolvedOrderDisId(order.getNxDoDistributerId());
        item.setResolvedOrderDepFatherId(order.getNxDoDepartmentFatherId());
        item.setNxDstiGoodsName(order.getNxDoGoodsName());
        item.setNxDstiQuantity(order.getNxDoQuantity());
        item.setNxDstiStandard(order.getNxDoStandard());
        item.setNxDstiRemark(order.getNxDoRemark());
        item.setOrderStatus(order.getNxDoStatus());
        item.setOrderArriveDate(firstNonBlank(order.getNxDoArriveDate(), order.getNxDoArriveOnlyDate()));
        item.setOrderFromHistory(false);
    }

    private void applyHistoryOrder(NxDisShipmentTaskItemEntity item, NxDepartmentOrderHistoryEntity order) {
        if (order == null) {
            item.setOrderResolved(false);
            return;
        }
        item.setOrderResolved(true);
        item.setResolvedOrderDisId(order.getNxDoDistributerId());
        item.setResolvedOrderDepFatherId(order.getNxDoDepartmentFatherId());
        item.setNxDstiGoodsName(order.getNxDoGoodsName());
        item.setNxDstiQuantity(order.getNxDoQuantity());
        item.setNxDstiStandard(order.getNxDoStandard());
        item.setNxDstiRemark(order.getNxDoRemark());
        item.setOrderStatus(order.getNxDoStatus());
        item.setOrderArriveDate(firstNonBlank(order.getNxDoArriveDate(), order.getNxDoArriveOnlyDate()));
        item.setOrderFromHistory(true);
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.trim().isEmpty()) {
            return primary;
        }
        if (fallback != null && !fallback.trim().isEmpty()) {
            return fallback;
        }
        return null;
    }
}

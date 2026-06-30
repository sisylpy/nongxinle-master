package com.nongxinle.community.pos.service.impl;

import com.nongxinle.entity.NxCommunityDeskEntity;
import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.community.pos.service.DeskOrderReconcileService;
import com.nongxinle.community.pos.service.NxCommunityDeskService;
import com.nongxinle.community.order.service.NxCommunityOrdersService;
import com.nongxinle.community.pos.service.PosDeskBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.community.pos.NxCommunityPosConstants.*;

@Service("posDeskBindingService")
public class PosDeskBindingServiceImpl implements PosDeskBindingService {

    @Autowired
    private NxCommunityDeskService nxCommunityDeskService;
    @Autowired
    private NxCommunityOrdersService nxCommunityOrdersService;
    @Autowired
    private DeskOrderReconcileService deskOrderReconcileService;

    @Override
    public NxCommunityDeskEntity requireDesk(Integer communityId, Integer deskId) {
        if (communityId == null) {
            throw new IllegalArgumentException("communityId 不能为空");
        }
        if (deskId == null) {
            throw new IllegalArgumentException("deskId 不能为空");
        }
        NxCommunityDeskEntity desk = nxCommunityDeskService.queryObject(deskId);
        if (desk == null) {
            throw new IllegalArgumentException("桌台不存在");
        }
        if (desk.getNxCdCommunityId() != null
                && !String.valueOf(communityId).equals(String.valueOf(desk.getNxCdCommunityId()))) {
            throw new IllegalArgumentException("桌台不属于当前门店");
        }
        return desk;
    }

    @Override
    @Transactional
    public void bindDeskToOrder(NxCommunityDeskEntity desk, Integer orderId) {
        if (desk == null || orderId == null) {
            return;
        }
        nxCommunityDeskService.bindCurrentOrder(desk.getNxCommunityDeskId(), orderId);
        desk.setNxCdCurrentOrderId(orderId);
        desk.setNxCdStatus(DESK_STATUS_BUSY);
    }

    @Override
    @Transactional
    public void releaseDesk(NxCommunityDeskEntity desk) {
        if (desk == null) {
            return;
        }
        nxCommunityDeskService.releaseCurrentOrder(desk.getNxCommunityDeskId());
        desk.setNxCdCurrentOrderId(null);
        desk.setNxCdStatus(DESK_STATUS_FREE);
    }

    @Override
    @Transactional
    public void releaseDeskByOrderId(Integer orderId) {
        if (orderId == null) {
            return;
        }
        NxCommunityDeskEntity desk = nxCommunityDeskService.queryDeskByCurrentOrderId(orderId);
        if (desk != null) {
            releaseDesk(desk);
        }
    }

    @Override
    public NxCommunityOrdersEntity resolveActivePosOrder(NxCommunityDeskEntity desk, Integer communityId) {
        if (desk == null) {
            return null;
        }
        deskOrderReconcileService.reconcileDesk(desk);
        Integer boundOrderId = desk.getNxCdCurrentOrderId();
        if (boundOrderId != null) {
            NxCommunityOrdersEntity bound = loadUnpaidPosOrder(boundOrderId, communityId, desk.getNxCommunityDeskId());
            if (bound != null) {
                return bound;
            }
            deskOrderReconcileService.reconcileDesk(desk);
        }
        NxCommunityOrdersEntity fallback = findUnpaidPosOrderByDesk(communityId, desk.getNxCommunityDeskId());
        if (fallback != null) {
            bindDeskToOrder(desk, fallback.getNxCommunityOrdersId());
            return fallback;
        }
        if (desk.getNxCdCurrentOrderId() != null || isDeskBusy(desk)) {
            releaseDesk(desk);
        }
        return null;
    }

    @Override
    @Transactional
    public void reconcileDeskState(NxCommunityDeskEntity desk, Integer communityId) {
        deskOrderReconcileService.reconcileDesk(desk);
    }

    @Override
    public String deskStatusLabel(NxCommunityDeskEntity desk) {
        if (desk == null) {
            return "FREE";
        }
        return desk.getNxCdCurrentOrderId() != null ? "BUSY" : "FREE";
    }

    private boolean isDeskBusy(NxCommunityDeskEntity desk) {
        return desk.getNxCdStatus() != null && desk.getNxCdStatus() == DESK_STATUS_BUSY;
    }

    private NxCommunityOrdersEntity loadUnpaidPosOrder(Integer orderId, Integer communityId, Integer deskId) {
        NxCommunityOrdersEntity order = nxCommunityOrdersService.queryObject(orderId);
        if (order == null) {
            return null;
        }
        if (!isActivePosOrder(order, communityId, deskId)) {
            return null;
        }
        return order;
    }

    private NxCommunityOrdersEntity findUnpaidPosOrderByDesk(Integer communityId, Integer deskId) {
        Map<String, Object> map = new HashMap<>();
        map.put("commId", communityId);
        map.put("date", formatWhatDay(0));
        map.put("orderChannel", ORDER_CHANNEL_POS);
        map.put("deskId", deskId);
        map.put("status", ORDER_STATUS_UNPAID);
        map.put("limit", 1);
        List<NxCommunityOrdersEntity> orders = nxCommunityOrdersService.queryCustomerOrder(map);
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        NxCommunityOrdersEntity order = orders.get(0);
        if (!isActivePosOrder(order, communityId, deskId)) {
            return null;
        }
        return order;
    }

    private boolean isActivePosOrder(NxCommunityOrdersEntity order, Integer communityId, Integer deskId) {
        if (order == null) {
            return false;
        }
        if (!ORDER_CHANNEL_POS.equals(order.getNxCoOrderChannel())) {
            return false;
        }
        if (!communityId.equals(order.getNxCoCommunityId())) {
            return false;
        }
        if (deskId != null && !deskId.equals(order.getNxCoDeskId())) {
            return false;
        }
        if (!Integer.valueOf(ORDER_STATUS_UNPAID).equals(order.getNxCoStatus())) {
            return false;
        }
        if (Integer.valueOf(PAYMENT_STATUS_PAID).equals(order.getNxCoPaymentStatus())) {
            return false;
        }
        return true;
    }
}

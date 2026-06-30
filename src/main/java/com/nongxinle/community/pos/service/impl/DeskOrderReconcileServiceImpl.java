package com.nongxinle.community.pos.service.impl;

import com.nongxinle.entity.NxCommunityDeskEntity;
import com.nongxinle.entity.NxCommunityOrdersEntity;
import com.nongxinle.community.pos.service.DeskOrderReconcileService;
import com.nongxinle.community.pos.service.NxCommunityDeskService;
import com.nongxinle.community.order.service.NxCommunityOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.community.pos.NxCommunityPosConstants.*;

@Service("deskOrderReconcileService")
public class DeskOrderReconcileServiceImpl implements DeskOrderReconcileService {

    @Autowired
    private NxCommunityDeskService nxCommunityDeskService;
    @Autowired
    private NxCommunityOrdersService nxCommunityOrdersService;

    @Override
    @Transactional
    public boolean reconcileDesk(NxCommunityDeskEntity desk) {
        if (desk == null) {
            return false;
        }
        Integer orderId = desk.getNxCdCurrentOrderId();
        if (orderId == null) {
            if (isDeskBusy(desk)) {
                releaseDesk(desk);
                return true;
            }
            return false;
        }
        NxCommunityOrdersEntity order = nxCommunityOrdersService.queryObject(orderId);
        if (order == null || !Integer.valueOf(ORDER_STATUS_UNPAID).equals(order.getNxCoStatus())) {
            releaseDesk(desk);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void reconcileDeskById(Integer deskId) {
        if (deskId == null) {
            return;
        }
        NxCommunityDeskEntity desk = nxCommunityDeskService.queryObject(deskId);
        reconcileDesk(desk);
    }

    @Override
    @Transactional
    public void reconcileByOrderId(Integer orderId) {
        if (orderId == null) {
            return;
        }
        NxCommunityDeskEntity desk = nxCommunityDeskService.queryDeskByCurrentOrderId(orderId);
        if (desk != null) {
            reconcileDesk(desk);
        }
    }

    @Override
    @Transactional
    public void reconcileCommunityDesks(Integer communityId) {
        if (communityId == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("commId", communityId);
        List<NxCommunityDeskEntity> desks = nxCommunityDeskService.queryComDeskByParams(map);
        if (desks == null) {
            return;
        }
        for (NxCommunityDeskEntity desk : desks) {
            reconcileDesk(desk);
        }
    }

    private boolean isDeskBusy(NxCommunityDeskEntity desk) {
        return desk.getNxCdStatus() != null && desk.getNxCdStatus() == DESK_STATUS_BUSY;
    }

    private void releaseDesk(NxCommunityDeskEntity desk) {
        nxCommunityDeskService.releaseCurrentOrder(desk.getNxCommunityDeskId());
        desk.setNxCdCurrentOrderId(null);
        desk.setNxCdStatus(DESK_STATUS_FREE);
    }
}

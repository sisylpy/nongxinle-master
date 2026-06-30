package com.nongxinle.community.pos.service;

import com.nongxinle.entity.NxCommunityDeskEntity;

public interface DeskOrderReconcileService {

    /**
     * 校验桌台绑定订单：不存在或非 UNPAID 则释放桌台。
     *
     * @return true 表示已释放桌台
     */
    boolean reconcileDesk(NxCommunityDeskEntity desk);

    void reconcileDeskById(Integer deskId);

    void reconcileByOrderId(Integer orderId);

    void reconcileCommunityDesks(Integer communityId);
}

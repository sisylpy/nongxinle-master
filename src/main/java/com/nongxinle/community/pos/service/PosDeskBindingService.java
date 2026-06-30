package com.nongxinle.community.pos.service;

import com.nongxinle.entity.NxCommunityDeskEntity;
import com.nongxinle.entity.NxCommunityOrdersEntity;

public interface PosDeskBindingService {

    NxCommunityDeskEntity requireDesk(Integer communityId, Integer deskId);

    void bindDeskToOrder(NxCommunityDeskEntity desk, Integer orderId);

    void releaseDesk(NxCommunityDeskEntity desk);

    void releaseDeskByOrderId(Integer orderId);

    NxCommunityOrdersEntity resolveActivePosOrder(NxCommunityDeskEntity desk, Integer communityId);

    void reconcileDeskState(NxCommunityDeskEntity desk, Integer communityId);

    String deskStatusLabel(NxCommunityDeskEntity desk);
}

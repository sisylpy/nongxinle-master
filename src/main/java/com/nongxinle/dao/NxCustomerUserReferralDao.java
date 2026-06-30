package com.nongxinle.dao;

import com.nongxinle.entity.NxCustomerUserReferralEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerUserReferralDao {

    NxCustomerUserReferralEntity queryObject(Integer id);

    NxCustomerUserReferralEntity queryByInviteeUserId(Integer inviteeUserId);

    int save(NxCustomerUserReferralEntity entity);

    int countDirectBySourceOwner(Map<String, Object> map);

    int countLevel2BySourceOwner(Map<String, Object> map);

    int countUnreadDirectInDisplayWindow(Map<String, Object> map);

    NxCustomerUserReferralEntity queryLatestUnreadDirectInWindow(Map<String, Object> map);

    Integer queryMaxDirectReferralIdBySourceOwner(Map<String, Object> map);

    List<NxCustomerUserReferralEntity> queryDirectList(Map<String, Object> map);

    List<NxCustomerUserReferralEntity> queryLevel2List(Map<String, Object> map);

    int countByPromoter(Map<String, Object> map);

    List<NxCustomerUserReferralEntity> queryByPromoter(Map<String, Object> map);

    int countQualifiedBySourceOwner(Map<String, Object> map);

    int countBySourceOwner(Map<String, Object> map);

    List<NxCustomerUserReferralEntity> queryBySourceOwner(Map<String, Object> map);

    int deleteBySourceOwner(Map<String, Object> map);

    int deleteByPromoter(Map<String, Object> map);
}

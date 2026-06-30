package com.nongxinle.dao;

import com.nongxinle.entity.NxCustomerReferralRewardEntity;

import java.util.List;
import java.util.Map;

public interface NxCustomerReferralRewardDao {

    NxCustomerReferralRewardEntity queryObject(Integer id);

    NxCustomerReferralRewardEntity queryObjectForUpdate(Integer id);

    NxCustomerReferralRewardEntity queryByUniqueKey(Map<String, Object> map);

    List<Integer> queryPendingRewardIds(Integer beneficiaryUserId);

    int save(NxCustomerReferralRewardEntity entity);

    int update(NxCustomerReferralRewardEntity entity);

    int countByBeneficiaryAndStatus(Map<String, Object> map);

    List<NxCustomerReferralRewardEntity> queryList(Map<String, Object> map);

    List<NxCustomerReferralRewardEntity> queryPendingByBeneficiary(Integer beneficiaryUserId);

    int deleteBySourceOwner(Map<String, Object> map);

    int deleteByPromoter(Map<String, Object> map);
}

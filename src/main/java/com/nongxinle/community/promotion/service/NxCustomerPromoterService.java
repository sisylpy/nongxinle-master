package com.nongxinle.community.promotion.service;

import com.nongxinle.entity.NxCustomerPromoterEntity;
import com.nongxinle.entity.NxCustomerUserReferralEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface NxCustomerPromoterService {

    NxCustomerPromoterEntity savePromoter(NxCustomerPromoterEntity entity);

    NxCustomerPromoterEntity queryObject(Integer id);

    List<NxCustomerPromoterEntity> queryList(Map<String, Object> map);

    boolean activate(Integer promoterId);

    boolean suspend(Integer promoterId, String reason);

    boolean terminate(Integer promoterId, String reason);

    boolean updateCooperationPeriod(Integer promoterId, Date startAt, Date endAt);

    Map<String, Object> queryPromoterStats(Integer promoterId);

    List<NxCustomerUserReferralEntity> queryReferralDetails(Integer promoterId, Integer offset, Integer limit);

    Map<String, Object> deletePromoter(Integer promoterId);
}

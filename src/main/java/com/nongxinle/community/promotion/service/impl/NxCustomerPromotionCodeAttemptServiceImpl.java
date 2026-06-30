package com.nongxinle.community.promotion.service.impl;

import com.nongxinle.dao.NxCustomerPromotionCodeAttemptDao;
import com.nongxinle.entity.NxCustomerPromotionCodeAttemptEntity;
import com.nongxinle.entity.NxCustomerUserEntity;
import com.nongxinle.community.promotion.service.NxCustomerPromotionCodeAttemptService;
import com.nongxinle.utils.CustomerReferralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("nxCustomerPromotionCodeAttemptService")
public class NxCustomerPromotionCodeAttemptServiceImpl implements NxCustomerPromotionCodeAttemptService {

    @Autowired
    private NxCustomerPromotionCodeAttemptDao nxCustomerPromotionCodeAttemptDao;

    @Override
    public List<Map<String, Object>> queryMyAttemptList(Integer userId, Integer communityId,
                                                        Integer offset, Integer limit) {
        List<NxCustomerPromotionCodeAttemptEntity> list =
                nxCustomerPromotionCodeAttemptDao.queryList(queryMap(userId, communityId, offset, limit));
        return toItemList(list);
    }

    @Override
    public int queryMyAttemptTotal(Integer userId, Integer communityId) {
        return nxCustomerPromotionCodeAttemptDao.queryTotal(queryMap(userId, communityId, null, null));
    }

    private Map<String, Object> queryMap(Integer userId, Integer communityId, Integer offset, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        map.put("sourceOwnerType", CustomerReferralConstants.OWNER_TYPE_CUSTOMER_USER);
        map.put("sourceOwnerId", userId);
        map.put("communityId", communityId);
        map.put("offset", offset);
        map.put("limit", limit);
        return map;
    }

    private List<Map<String, Object>> toItemList(List<NxCustomerPromotionCodeAttemptEntity> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (NxCustomerPromotionCodeAttemptEntity item : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("attemptId", item.getNxCustomerPromotionCodeAttemptId());
            row.put("promotionCodeSnapshot", item.getPromotionCodeSnapshot());
            row.put("invalidReason", item.getInvalidReason());
            row.put("communityId", item.getCommunityId());
            row.put("attemptedAt", item.getAttemptedAt());
            row.put("shareEntry", item.getShareEntry());
            if (item.getInviteeUser() != null) {
                NxCustomerUserEntity invitee = item.getInviteeUser();
                Map<String, Object> inviteeMap = new HashMap<>();
                inviteeMap.put("userId", invitee.getNxCuUserId());
                inviteeMap.put("nickName", invitee.getNxCuWxNickName());
                inviteeMap.put("avatarUrl", invitee.getNxCuWxAvatarUrl());
                inviteeMap.put("phone", maskPhone(invitee.getNxCuWxPhoneNumber()));
                inviteeMap.put("joinDate", invitee.getNxCuJoinDate());
                row.put("invitee", inviteeMap);
            }
            result.add(row);
        }
        return result;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}

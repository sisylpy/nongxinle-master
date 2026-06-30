package com.nongxinle.dao;

import com.nongxinle.entity.NxCommunityPosPaymentEntity;

import java.util.List;
import java.util.Map;

public interface NxCommunityPosPaymentDao {

    NxCommunityPosPaymentEntity queryObject(Integer id);

    NxCommunityPosPaymentEntity queryByOutTradeNo(String outTradeNo);

    List<NxCommunityPosPaymentEntity> queryList(Map<String, Object> map);

    void save(NxCommunityPosPaymentEntity entity);

    void update(NxCommunityPosPaymentEntity entity);
}

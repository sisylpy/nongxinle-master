package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 02-22 22:34
 */

import com.nongxinle.entity.NxDistributerBillEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerBillDao extends BaseDao<NxDistributerBillEntity> {

    Integer queryDisPurchaseBatchCount(Map<String, Object> mapS);

    Double querySupplierUnSettleSubtotal(Map<String, Object> mapS);

    /**
     * 查询两个协作伙伴之间的账单总金额（双向合计：他欠的+合作伙伴欠他的）
     */
    Double queryPartnerMutualBillTotal(Map<String, Object> map);

    /**
     * 查询两个协作伙伴之间的账单数量（双向合计）
     */
    Integer queryPartnerMutualBillCount(Map<String, Object> map);

    /**
     * 查询账单列表，支持 disId、collNxDisId、type、日期筛选，按日期倒序
     */
    List<NxDistributerBillEntity> queryDisBillsWithStatus(Map<String, Object> map);

    List<NxDistributerBillEntity> queryDisBillsDetail(Map<String, Object> map);

    Integer queryDisBillsDetailCount(Map<String, Object> map);
}

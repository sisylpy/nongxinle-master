package com.nongxinle.service;

/**
 *
 *
 * @author lpy
 * @date 02-22 22:34
 */

import com.nongxinle.entity.NxDistributerBillEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerBillService {
	
	NxDistributerBillEntity queryObject(Integer nxDistributerBillId);
	
	List<NxDistributerBillEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerBillEntity nxDistributerBill);
	
	void update(NxDistributerBillEntity nxDistributerBill);
	
	void delete(Integer nxDistributerBillId);
	
	void deleteBatch(Integer[] nxDistributerBillIds);

    Integer queryDisPurchaseBatchCount(Map<String, Object> mapS);

	Double querySupplierUnSettleSubtotal(Map<String, Object> mapS);

	/**
	 * 查询两个协作伙伴之间的账单总金额（双向合计）
	 */
	Double queryPartnerMutualBillTotal(Map<String, Object> map);

	/**
	 * 查询两个协作伙伴之间的账单数量（双向合计）
	 */
	Integer queryPartnerMutualBillCount(Map<String, Object> map);

	/**
	 * 查询账单列表，支持 type 筛选
	 */
	List<NxDistributerBillEntity> queryDisBillsWithStatus(Map<String, Object> map);

	List<NxDistributerBillEntity> queryDisBillsDetail(Map<String, Object> map);

    Integer queryDisBillsDetailCount(Map<String, Object> map);
}

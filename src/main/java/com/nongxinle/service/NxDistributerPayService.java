package com.nongxinle.service;

/**
 * 
 *
 * @author lpy
 * @date 10-18 12:14
 */

import com.nongxinle.entity.NxDistributerPayEntity;

import java.util.List;
import java.util.Map;

public interface NxDistributerPayService {
	
	NxDistributerPayEntity queryObject(Integer nxDistributerPayId);
	
	List<NxDistributerPayEntity> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	void save(NxDistributerPayEntity nxDistributerPay);
	
	void update(NxDistributerPayEntity nxDistributerPay);
	
	void delete(Integer nxDistributerPayId);
	
	void deleteBatch(Integer[] nxDistributerPayIds);

    NxDistributerPayEntity queryItemByTradeNo(String ordersSn);

    List<NxDistributerPayEntity> queryDisPayListByParams(Map<String, Object> map);

	NxDistributerPayEntity queryPayItemByPayId(Integer payId);

    List<NxDistributerPayEntity> queryItemListByTradeNo(String ordersSn);

    NxDistributerPayEntity queryUnPayByParams(Map<String, Object> mapP);

    /**
     * 查询充值记录（支持日期过滤）
     * @param params 查询参数
     * @return 充值记录列表
     */
    List<NxDistributerPayEntity> queryRechargeByParams(Map<String, Object> params);

    /**
     * 按市场ID查询充值记录（支持日期过滤）
     * @param params 查询参数，包含marketId
     * @return 充值记录列表（包含配送商信息）
     */
    List<Map<String, Object>> queryRechargeByMarketId(Map<String, Object> params);
}

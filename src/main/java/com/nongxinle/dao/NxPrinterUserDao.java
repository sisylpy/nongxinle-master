package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 05-25 15:21
 */

import com.nongxinle.entity.NxPrinterUserEntity;


public interface NxPrinterUserDao extends BaseDao<NxPrinterUserEntity> {

    NxPrinterUserEntity queryPrinterUserByOpenId(String openId);
}

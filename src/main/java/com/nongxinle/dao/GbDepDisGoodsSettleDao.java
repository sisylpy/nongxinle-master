package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 04-01 10:51
 */

import com.nongxinle.entity.GbDepDisGoodsSettleEntity;

import java.util.List;
import java.util.Map;


public interface GbDepDisGoodsSettleDao extends BaseDao<GbDepDisGoodsSettleEntity> {

    GbDepDisGoodsSettleEntity queryDisGoodsSettleByParams(Map<String, Object> mapGoodsSettle);

    List<GbDepDisGoodsSettleEntity> queryDepDisGoodsSettleByParams(Map<String, Object> map);
}

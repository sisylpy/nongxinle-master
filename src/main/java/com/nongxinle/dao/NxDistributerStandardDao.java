package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 07-27 21:41
 */

import com.nongxinle.entity.NxDistributerStandardEntity;

import java.util.List;
import java.util.Map;


public interface NxDistributerStandardDao extends BaseDao<NxDistributerStandardEntity> {

    List<NxDistributerStandardEntity> queryDisStandardByDisGoodsId(Integer nxDdgDisGoodsId);

    List<NxDistributerStandardEntity> queryDisStandardByParams(Map<String, Object> map);
    
    /**
     * 为 nx_dg_items_per_carton > 1 的商品自动添加订货规格
     * 规格名称使用外箱名称（nx_dg_carton_unit）
     */
    int batchAddCartonStandard();
}

package com.nongxinle.dao;

/**
 * 
 *
 * @author lpy
 * @date 2020-02-24 17:06:57
 */

import com.nongxinle.entity.NxCommunityFatherGoodsEntity;

import java.util.List;
import java.util.Map;


public interface NxCommunityFatherGoodsDao extends BaseDao<NxCommunityFatherGoodsEntity> {

    List<NxCommunityFatherGoodsEntity> queryFatherGoods(Integer dgGoodsFatherId);

    List<NxCommunityFatherGoodsEntity> queryCataListByCommunityId(Integer communityId);

    List<NxCommunityFatherGoodsEntity> queryHasComFathersFather(Map<String, Object> map2);

    List<NxCommunityFatherGoodsEntity> queryComGoodsCata(Map<String, Object> map2);

    List<NxCommunityFatherGoodsEntity> queryRankFatherGoods(Integer comId);

    List<NxCommunityFatherGoodsEntity> queryComFathersGoodsByParams(Map<String, Object> map);

    List<NxCommunityFatherGoodsEntity> queryFatherWithGoods(Map<String, Object> map);

    List<NxCommunityFatherGoodsEntity> queryFatherWithGoodsPindan(Map<String, Object> map);
}

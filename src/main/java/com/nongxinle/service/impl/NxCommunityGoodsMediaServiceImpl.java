package com.nongxinle.service.impl;

import com.nongxinle.dao.NxCommunityGoodsMediaDao;
import com.nongxinle.entity.NxCommunityGoodsMediaEntity;
import com.nongxinle.service.NxCommunityGoodsMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 社区商品媒体资源Service实现
 *
 * @author lpy
 * @date 2026-04-12 11:51:00
 */
@Service("nxCommunityGoodsMediaService")
public class NxCommunityGoodsMediaServiceImpl implements NxCommunityGoodsMediaService {

    @Autowired
    private NxCommunityGoodsMediaDao nxCommunityGoodsMediaDao;

    @Override
    public List<NxCommunityGoodsMediaEntity> getMediaListByGoodsId(Integer goodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goodsId);
        return nxCommunityGoodsMediaDao.queryListByGoodsId(map);
    }

    @Override
    public NxCommunityGoodsMediaEntity getPrimaryMedia(Integer goodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goodsId);
        return nxCommunityGoodsMediaDao.queryPrimaryMediaByGoodsId(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMediaBatch(List<NxCommunityGoodsMediaEntity> mediaList) {
        if (mediaList == null || mediaList.isEmpty()) {
            return;
        }
        nxCommunityGoodsMediaDao.saveBatch(mediaList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByGoodsId(Integer goodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goodsId);
        nxCommunityGoodsMediaDao.deleteByGoodsId(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMediaById(Integer mediaId) {
        Map<String, Object> map = new HashMap<>();
        map.put("mediaId", mediaId);
        nxCommunityGoodsMediaDao.deleteByMediaId(map);
    }

    @Override
    public void saveMedia(NxCommunityGoodsMediaEntity media) {
        nxCommunityGoodsMediaDao.save(media);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearPrimaryByGoodsId(Integer goodsId) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goodsId);
        nxCommunityGoodsMediaDao.clearPrimaryByGoodsId(map);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMediaSort(Integer goodsId, Integer mediaId, Integer newSort) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goodsId);
        map.put("mediaId", mediaId);
        map.put("newSort", newSort);
        nxCommunityGoodsMediaDao.updateMediaSort(map);
    }
}

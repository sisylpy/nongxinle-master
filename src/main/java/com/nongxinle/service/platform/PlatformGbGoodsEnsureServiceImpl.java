package com.nongxinle.service.platform;

import com.nongxinle.controller.GbDistributerGoodsController;
import com.nongxinle.entity.GbDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.service.GbDistributerGoodsService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlatformGbGoodsEnsureServiceImpl implements PlatformGbGoodsEnsureService {

    private static final Logger log = LoggerFactory.getLogger(PlatformGbGoodsEnsureServiceImpl.class);

    @Autowired
    private GbDistributerGoodsService gbDistributerGoodsService;
    @Autowired
    private GbDistributerGoodsController gbDistributerGoodsController;

    @Override
    public GbDistributerGoodsEntity ensureForNxDisGoods(
            Integer gbDistributerId,
            Integer catalogDepartmentId,
            NxDistributerGoodsEntity nxGoods) {

        GbDistributerGoodsEntity existing = lookupExisting(gbDistributerId, nxGoods);
        if (existing != null) {
            return linkNxDisGoodsIfNeeded(existing, nxGoods);
        }

        Integer nxGoodsId = nxGoods.getNxDgNxGoodsId();
        if (nxGoodsId == null) {
            throw new IllegalArgumentException("批发商商品缺少标准商品 nxGoodsId: nxDistributerGoodsId="
                    + nxGoods.getNxDistributerGoodsId());
        }
        Integer depId = catalogDepartmentId != null && catalogDepartmentId > 0 ? catalogDepartmentId : gbDistributerId;
        log.info("[platform/gb-goods-ensure] auto create gbDisId={} depId={} nxGoodsId={} nxDisGoodsId={}",
                gbDistributerId, depId, nxGoodsId, nxGoods.getNxDistributerGoodsId());

        GbDistributerGoodsEntity created = gbDistributerGoodsController.postDgnGbGoods(gbDistributerId, depId, nxGoodsId);
        return linkNxDisGoodsIfNeeded(created, nxGoods);
    }

    private GbDistributerGoodsEntity lookupExisting(Integer gbDistributerId, NxDistributerGoodsEntity nxGoods) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", gbDistributerId);
        map.put("nxDisGoodsId", nxGoods.getNxDistributerGoodsId());
        List<GbDistributerGoodsEntity> list = gbDistributerGoodsService.queryDisGoodsByParams(map);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        if (nxGoods.getNxDgNxGoodsId() != null) {
            map = new HashMap<>();
            map.put("disId", gbDistributerId);
            map.put("goodsId", nxGoods.getNxDgNxGoodsId());
            list = gbDistributerGoodsService.queryDisGoodsByParams(map);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
            map = new HashMap<>();
            map.put("disId", gbDistributerId);
            map.put("goodsId", nxGoods.getNxDgNxGoodsId());
            list = gbDistributerGoodsService.queryAddDistributerNxGoods(map);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    private GbDistributerGoodsEntity linkNxDisGoodsIfNeeded(
            GbDistributerGoodsEntity gbGoods,
            NxDistributerGoodsEntity nxGoods) {

        Integer nxDisGoodsId = nxGoods.getNxDistributerGoodsId();
        Integer nxDisId = nxGoods.getNxDgDistributerId();
        boolean needsLink = gbGoods.getGbDgNxDistributerGoodsId() == null
                || gbGoods.getGbDgNxDistributerGoodsId() <= 0
                || !gbGoods.getGbDgNxDistributerGoodsId().equals(nxDisGoodsId);
        if (!needsLink) {
            return gbGoods;
        }
        gbGoods.setGbDgNxDistributerId(nxDisId);
        gbGoods.setGbDgNxDistributerGoodsId(nxDisGoodsId);
        if (StringUtils.isNotBlank(nxGoods.getNxDgWillPrice())) {
            gbGoods.setGbDgNxDistributerGoodsPrice(nxGoods.getNxDgWillPrice());
        }
        gbDistributerGoodsService.update(gbGoods);
        return gbGoods;
    }
}

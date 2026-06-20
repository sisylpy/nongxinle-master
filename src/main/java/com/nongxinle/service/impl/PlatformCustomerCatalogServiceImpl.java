package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDistributerGoodsDao;
import com.nongxinle.dto.platform.PlatformCatalogGoodsRow;
import com.nongxinle.dto.platform.customer.PlatformCustomerGoodsCatalogListRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerGoodsCatalogTreeRequest;
import com.nongxinle.entity.NxGoodsEntity;
import com.nongxinle.entity.SysCityMarketEntity;
import com.nongxinle.service.NxGoodsService;
import com.nongxinle.service.PlatformCustomerCatalogService;
import com.nongxinle.service.SysCityMarketService;
import com.nongxinle.utils.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("platformCustomerCatalogService")
public class PlatformCustomerCatalogServiceImpl implements PlatformCustomerCatalogService {

    private static final Logger log = LoggerFactory.getLogger(PlatformCustomerCatalogServiceImpl.class);

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 15;
    private static final int MAX_LIMIT = 50;

    @Autowired
    private SysCityMarketService sysCityMarketService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxDistributerGoodsDao nxDistributerGoodsDao;

    @Override
    public Map<String, Object> buildCatalogTree(PlatformCustomerGoodsCatalogTreeRequest request) {
        log.info("[platform/catalog/tree] request={}", request);
        requireMarketId(request == null ? null : request.getMarketId());

        List<NxGoodsEntity> flat = nxGoodsService.getiBookCoverData();
        Map<String, Object> result = new HashMap<>();

        if (flat == null || flat.isEmpty()) {
            result.put("cataArr", new ArrayList<>());
            result.put("nxGoodsIdsSort", new ArrayList<>());
            return result;
        }

        Map<Integer, NxGoodsEntity> grandMap = new LinkedHashMap<>();
        for (NxGoodsEntity entity : flat) {
            Integer parentId = entity.getNxGoodsId();
            if (!grandMap.containsKey(parentId)) {
                grandMap.put(parentId, entity);
                entity.setNxGoodsEntityList(new ArrayList<>());
            }
            NxGoodsEntity subEntity = new NxGoodsEntity();
            subEntity.setNxGoodsId(entity.getSubNxGoodsId());
            subEntity.setNxGoodsName(entity.getSubNxGoodsName());
            subEntity.setNxGoodsDetail(entity.getSubNxGoodsDetail());
            subEntity.setNxGoodsFile(entity.getSubNxGoodsFile());
            subEntity.setNxGoodsFatherId(entity.getSubNxGoodsFatherId());
            subEntity.setNxGoodsSort(entity.getSubNxGoodsSort());
            grandMap.get(parentId).getNxGoodsEntityList().add(subEntity);
        }

        List<NxGoodsEntity> groupedList = new ArrayList<>(grandMap.values());

        Map<String, Object> sortParams = new HashMap<>();
        sortParams.put("isHidden", 0);
        sortParams.put("marketId", request.getMarketId());
        List<Integer> nxGoodsIdsSort = nxDistributerGoodsDao.queryPlatformCatalogGoodsIdsSort(sortParams);

        result.put("cataArr", groupedList);
        result.put("nxGoodsIdsSort", nxGoodsIdsSort != null ? nxGoodsIdsSort : new ArrayList<>());
        log.info("[platform/catalog/tree] marketId={} grandCount={} sortIds={}",
                request.getMarketId(), groupedList.size(),
                nxGoodsIdsSort != null ? nxGoodsIdsSort.size() : 0);
        return result;
    }

    @Override
    public PageUtils listGoodsByGrandCategory(PlatformCustomerGoodsCatalogListRequest request) {
        log.info("[platform/catalog/list] request={}", request);
        Integer marketId = requireMarketId(request == null ? null : request.getMarketId());
        if (request.getGreatGrandId() == null) {
            throw new IllegalArgumentException("greatGrandId 不能为空");
        }

        int page = normalizePage(request.getPage());
        int limit = normalizeLimit(request.getLimit());
        int offset = (page - 1) * limit;

        Map<String, Object> params = new HashMap<>();
        params.put("marketId", marketId);
        params.put("greatGrandId", request.getGreatGrandId());
        params.put("offset", offset);
        params.put("limit", limit);
        if (request.getDepartmentId() != null) {
            params.put("departmentId", request.getDepartmentId());
            params.put("applyDate", formatWhatDay(0));
        }

        List<PlatformCatalogGoodsRow> list = nxDistributerGoodsDao.queryPlatformCatalogGoodsByGreatGrandId(params);
        int total = nxDistributerGoodsDao.countPlatformCatalogGoodsByGreatGrandId(params);

        log.info("[platform/catalog/list] marketId={} greatGrandId={} page={} limit={} total={} returned={}",
                marketId, request.getGreatGrandId(), page, limit, total, list != null ? list.size() : 0);

        return new PageUtils(list, total, limit, page);
    }

    private Integer requireMarketId(Integer marketId) {
        if (marketId == null) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        SysCityMarketEntity market = sysCityMarketService.queryObject(marketId);
        if (market == null) {
            throw new IllegalArgumentException("市场不存在: " + marketId);
        }
        return marketId;
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}

package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDistributerGoodsDao;
import com.nongxinle.dao.NxGoodsDao;
import com.nongxinle.dto.platform.PlatformCategoryHomeRow;
import com.nongxinle.dto.platform.PlatformMarketSupplierHomeRow;
import com.nongxinle.dto.platform.customer.PlatformCustomerCategoryChildItem;
import com.nongxinle.dto.platform.customer.PlatformCustomerCategoryItem;
import com.nongxinle.dto.platform.customer.PlatformCustomerDepartmentInfo;
import com.nongxinle.dto.platform.customer.PlatformCustomerGoodsCategoriesRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerHomeInitRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerHomeInitResponse;
import com.nongxinle.dto.platform.customer.PlatformCustomerHomeSummary;
import com.nongxinle.dto.platform.customer.PlatformCustomerMarketInfo;
import com.nongxinle.dto.platform.customer.PlatformCustomerMarketSupplierItem;
import com.nongxinle.dto.platform.customer.PlatformCustomerMarketSuppliersRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerSearchInfo;
import com.nongxinle.entity.GbDepartmentEntity;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxGoodsEntity;
import com.nongxinle.entity.NxMarketDepartmentEntity;
import com.nongxinle.entity.SysCityMarketEntity;
import com.nongxinle.service.GbDepartmentService;
import com.nongxinle.service.NxDepartmentService;
import com.nongxinle.dto.platform.customer.PlatformCustomerHomeInitResponse;
import com.nongxinle.dto.platform.customer.PlatformOutstandingBillInfo;
import com.nongxinle.service.PlatformCustomerHomeService;
import com.nongxinle.service.platform.PlatformOutstandingBillService;
import com.nongxinle.service.PlatformMarketDepartmentService;
import com.nongxinle.service.SysCityMarketService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("platformCustomerHomeService")
public class PlatformCustomerHomeServiceImpl implements PlatformCustomerHomeService {

    private static final String DEFAULT_NOTICE = "今日市场";
    private static final String DEFAULT_NOTICE_SUB = "叶菜价格波动较大，建议尽早下单";
    private static final String DEFAULT_SEARCH_PLACEHOLDER = "搜索油菜、圆白菜、馒头、白醋";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    @Autowired
    private SysCityMarketService sysCityMarketService;
    @Autowired
    private PlatformMarketDepartmentService platformMarketDepartmentService;
    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired(required = false)
    private GbDepartmentService gbDepartmentService;
    @Autowired
    private NxDistributerGoodsDao nxDistributerGoodsDao;
    @Autowired
    private NxGoodsDao nxGoodsDao;
    @Autowired
    private PlatformOutstandingBillService platformOutstandingBillService;

    @Override
    public PlatformCustomerHomeInitResponse homeInit(PlatformCustomerHomeInitRequest request) {
        Integer marketId = requireMarketId(request == null ? null : request.getMarketId());
        SysCityMarketEntity marketEntity = requireMarket(marketId);

        PlatformCustomerHomeInitResponse response = new PlatformCustomerHomeInitResponse();
        response.setMarket(buildMarketInfo(marketEntity));
        response.setCurrentDepartment(resolveDepartmentInfo(marketId, request == null ? null : request.getDepartmentId()));
        response.setSearch(buildSearchInfo());
        response.setSummary(buildSummary(marketId));
        attachOutstandingBill(response, request == null ? null : request.getDepartmentId());
        return response;
    }

    private void attachOutstandingBill(PlatformCustomerHomeInitResponse response, Integer departmentId) {
        if (departmentId == null) {
            response.setHasOutstandingBill(false);
            return;
        }
        PlatformOutstandingBillInfo outstanding = platformOutstandingBillService.findOutstandingInfo(departmentId);
        response.setHasOutstandingBill(outstanding != null);
        response.setOutstandingBill(outstanding);
    }

    @Override
    public List<PlatformCustomerMarketSupplierItem> listMarketSuppliers(PlatformCustomerMarketSuppliersRequest request) {
        Integer marketId = requireMarketId(request == null ? null : request.getMarketId());
        requireMarket(marketId);

        int page = normalizePage(request == null ? null : request.getPage());
        int limit = normalizeLimit(request == null ? null : request.getLimit());
        int offset = (page - 1) * limit;

        Map<String, Object> params = new HashMap<>();
        params.put("marketId", marketId);
        params.put("offset", offset);
        params.put("limit", limit);

        List<PlatformMarketSupplierHomeRow> rows = nxDistributerGoodsDao.queryPlatformMarketSupplierHomeList(params);
        List<PlatformCustomerMarketSupplierItem> items = new ArrayList<>();
        for (PlatformMarketSupplierHomeRow row : rows) {
            PlatformCustomerMarketSupplierItem item = new PlatformCustomerMarketSupplierItem();
            item.setDistributerId(row.getDistributerId());
            item.setDistributerName(row.getDistributerName());
            item.setLogo(row.getLogo());
            item.setAddress(row.getAddress());
            item.setMainCategories(StringUtils.defaultString(row.getMainCategories()));
            item.setGoodsCount(row.getGoodsCount() == null ? 0 : row.getGoodsCount());
            item.setIsActive(item.getGoodsCount() > 0 ? 1 : 0);
            items.add(item);
        }
        return items;
    }

    @Override
    public List<PlatformCustomerCategoryItem> listGoodsCategories(PlatformCustomerGoodsCategoriesRequest request) {
        Integer marketId = requireMarketId(request == null ? null : request.getMarketId());
        requireMarket(marketId);

        Map<String, Object> params = new HashMap<>();
        params.put("marketId", marketId);
        List<PlatformCategoryHomeRow> rows = nxDistributerGoodsDao.queryPlatformCategoryHomeList(params);

        List<PlatformCustomerCategoryItem> items = new ArrayList<>();
        for (PlatformCategoryHomeRow row : rows) {
            PlatformCustomerCategoryItem item = new PlatformCustomerCategoryItem();
            item.setCategoryId(row.getCategoryId());
            item.setCategoryName(row.getCategoryName());
            item.setIcon(row.getIcon());
            item.setGoodsCount(row.getGoodsCount() == null ? 0 : row.getGoodsCount());
            item.setSupplierCount(row.getSupplierCount() == null ? 0 : row.getSupplierCount());
            item.setChildren(loadCategoryChildren(row.getCategoryId()));
            items.add(item);
        }
        return items;
    }

    private PlatformCustomerHomeSummary buildSummary(Integer marketId) {
        Map<String, Object> params = new HashMap<>();
        params.put("marketId", marketId);

        PlatformCustomerHomeSummary summary = new PlatformCustomerHomeSummary();
        summary.setSupplierCount(nxDistributerGoodsDao.countPlatformMarketSuppliers(params));
        summary.setCategoryCount(nxDistributerGoodsDao.countPlatformMarketCategories(params));
        summary.setGoodsCount(nxDistributerGoodsDao.countPlatformMarketDistinctGoods(params));
        return summary;
    }

    private PlatformCustomerMarketInfo buildMarketInfo(SysCityMarketEntity marketEntity) {
        PlatformCustomerMarketInfo market = new PlatformCustomerMarketInfo();
        market.setMarketId(marketEntity.getSysCityMarketId());
        market.setMarketName(StringUtils.defaultIfBlank(marketEntity.getSysCmMarketName(), ""));
        market.setAddress("");
        market.setNotice(DEFAULT_NOTICE);
        market.setNoticeSub(DEFAULT_NOTICE_SUB);
        return market;
    }

    private PlatformCustomerSearchInfo buildSearchInfo() {
        PlatformCustomerSearchInfo search = new PlatformCustomerSearchInfo();
        search.setPlaceholder(DEFAULT_SEARCH_PLACEHOLDER);
        return search;
    }

    private PlatformCustomerDepartmentInfo resolveDepartmentInfo(Integer marketId, Integer departmentId) {
        if (departmentId == null) {
            return null;
        }

        NxMarketDepartmentEntity activeBinding = platformMarketDepartmentService.queryActive(marketId, departmentId);
        if (activeBinding != null) {
            NxDepartmentEntity nxDepartment = nxDepartmentService.queryObject(departmentId);
            if (nxDepartment != null) {
                return toDepartmentInfo(departmentId, nxDepartment.getNxDepartmentName(), nxDepartment.getNxDepartmentAddress());
            }
        }

        NxDepartmentEntity nxDepartment = nxDepartmentService.queryObject(departmentId);
        if (nxDepartment != null) {
            return toDepartmentInfo(departmentId, nxDepartment.getNxDepartmentName(), nxDepartment.getNxDepartmentAddress());
        }

        GbDepartmentEntity gbDepartment = gbDepartmentService == null
                ? null
                : gbDepartmentService.queryObject(departmentId);
        if (gbDepartment != null) {
            String name = StringUtils.isNotBlank(gbDepartment.getGbDepartmentAttrName())
                    ? gbDepartment.getGbDepartmentAttrName()
                    : gbDepartment.getGbDepartmentName();
            return toDepartmentInfo(departmentId, name, "");
        }
        return null;
    }

    private PlatformCustomerDepartmentInfo toDepartmentInfo(Integer departmentId, String name, String address) {
        PlatformCustomerDepartmentInfo info = new PlatformCustomerDepartmentInfo();
        info.setDepartmentId(departmentId);
        info.setDepartmentName(StringUtils.defaultIfBlank(name, "当前饭店"));
        info.setAddress(StringUtils.defaultString(address));
        return info;
    }

    private List<PlatformCustomerCategoryChildItem> loadCategoryChildren(Integer categoryId) {
        Map<String, Object> params = new HashMap<>();
        params.put("fatherId", categoryId);
        List<NxGoodsEntity> children = nxGoodsDao.queryListWithFatherId(params);
        List<PlatformCustomerCategoryChildItem> items = new ArrayList<>();
        if (children == null) {
            return items;
        }
        for (NxGoodsEntity child : children) {
            if (child.getNxGoodsIsHidden() != null && child.getNxGoodsIsHidden() == 1) {
                continue;
            }
            PlatformCustomerCategoryChildItem item = new PlatformCustomerCategoryChildItem();
            item.setCategoryId(child.getNxGoodsId());
            item.setCategoryName(child.getNxGoodsName());
            item.setIcon(child.getNxGoodsFile());
            items.add(item);
        }
        return items;
    }

    private Integer requireMarketId(Integer marketId) {
        if (marketId == null) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        return marketId;
    }

    private SysCityMarketEntity requireMarket(Integer marketId) {
        SysCityMarketEntity market = sysCityMarketService.queryObject(marketId);
        if (market == null) {
            throw new IllegalArgumentException("市场不存在: marketId=" + marketId);
        }
        return market;
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

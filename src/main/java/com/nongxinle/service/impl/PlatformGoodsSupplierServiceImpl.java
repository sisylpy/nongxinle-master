package com.nongxinle.service.impl;

import com.nongxinle.dao.NxDistributerGoodsDao;
import com.nongxinle.dto.platform.PlatformSupplierItem;
import com.nongxinle.dto.platform.PlatformSupplierRow;
import com.nongxinle.dto.platform.PlatformSuppliersRequest;
import com.nongxinle.service.PlatformGoodsSupplierService;
import com.nongxinle.service.PlatformMarketDepartmentService;
import com.nongxinle.service.platform.PlatformDisGoodsValidator;
import com.nongxinle.utils.SalesPriceUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("platformGoodsSupplierService")
public class PlatformGoodsSupplierServiceImpl implements PlatformGoodsSupplierService {

    @Autowired
    private NxDistributerGoodsDao nxDistributerGoodsDao;
    @Autowired
    private PlatformMarketDepartmentService platformMarketDepartmentService;

    @Override
    public List<PlatformSupplierItem> listSuppliers(PlatformSuppliersRequest request) {
        if (request == null || request.getMarketId() == null
                || request.getDepartmentId() == null || request.getNxGoodsId() == null) {
            throw new IllegalArgumentException("marketId、departmentId、nxGoodsId 不能为空");
        }
        if (platformMarketDepartmentService.queryActive(request.getMarketId(), request.getDepartmentId()) == null) {
            throw new IllegalArgumentException("客户不属于该市场或未激活");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("marketId", request.getMarketId());
        params.put("departmentId", request.getDepartmentId());
        params.put("nxGoodsId", request.getNxGoodsId());
        if (StringUtils.isNotBlank(request.getStandard())) {
            params.put("standard", request.getStandard().trim());
        }

        List<PlatformSupplierRow> rows = nxDistributerGoodsDao.queryPlatformSuppliersByNxGoods(params);
        List<PlatformSupplierItem> items = new ArrayList<>();
        for (PlatformSupplierRow row : rows) {
            String currentQuotePrice = PlatformDisGoodsValidator.resolveCurrentQuotePrice(row);
            if (currentQuotePrice == null) {
                continue;
            }
            PlatformSupplierItem item = new PlatformSupplierItem();
            item.setDisGoodsId(row.getDisGoodsId());
            item.setDistributerId(row.getDistributerId());
            item.setDistributerName(row.getDistributerName());
            item.setNxGoodsId(row.getNxGoodsId());
            item.setGoodsName(row.getGoodsName());
            item.setStandard(row.getStandard());
            item.setBrand(row.getBrand());
            item.setPlace(row.getPlace());
            item.setCurrentQuotePrice(currentQuotePrice);
            item.setCustomerHistoryPrice(formatOptionalHistoryPrice(row.getCustomerHistoryPrice()));
            item.setIsDefaultRecommend(isDefaultRecommend(row) ? 1 : 0);
            items.add(item);
        }
        return items;
    }

    private boolean isDefaultRecommend(PlatformSupplierRow row) {
        return row.getDefaultDisGoodsId() != null
                && row.getDefaultDisGoodsId().equals(row.getDisGoodsId());
    }

    /** 历史价仅展示，不参与候选过滤；无效占位价统一返回 null */
    private String formatOptionalHistoryPrice(String historyPrice) {
        if (StringUtils.isBlank(historyPrice)) {
            return null;
        }
        String trimmed = historyPrice.trim();
        if (SalesPriceUtils.isValidSalesPrice(trimmed)) {
            return trimmed;
        }
        return null;
    }
}

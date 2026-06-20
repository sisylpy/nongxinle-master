package com.nongxinle.service.platform;

import com.nongxinle.dao.NxPlatformOrderAssignDao;
import com.nongxinle.dto.platform.distributer.PlatformDistributerCustomerItem;
import com.nongxinle.dto.platform.distributer.PlatformDistributerCustomerRow;
import com.nongxinle.dto.platform.distributer.PlatformDistributerTodayCustomersRequest;
import com.nongxinle.dto.platform.distributer.PlatformDistributerTodayCustomersResponse;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.service.NxDepartmentService;
import com.nongxinle.utils.PlatformOrderDisplaySupport;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlatformDistributerCustomerServiceImpl implements PlatformDistributerCustomerService {

    private static final Logger log = LoggerFactory.getLogger(PlatformDistributerCustomerServiceImpl.class);

    private static final String SOURCE_GB = "GB";
    private static final String SOURCE_NX = "NX";

    @Autowired
    private NxPlatformOrderAssignDao nxPlatformOrderAssignDao;
    @Autowired
    private NxDepartmentService nxDepartmentService;

    @Override
    public PlatformDistributerTodayCustomersResponse listTodayCustomers(PlatformDistributerTodayCustomersRequest request) {
        if (request == null || request.getDisId() == null) {
            throw new IllegalArgumentException("disId 不能为空");
        }
        Integer disId = request.getDisId();
        log.info("[platform/distributer/customers/today] 开始 disId={}", disId);

        Map<String, Object> params = new HashMap<>();
        params.put("disId", disId);
        List<PlatformDistributerCustomerRow> rows =
                nxPlatformOrderAssignDao.queryPlatformDistributerCustomers(params);
        int rowCount = rows != null ? rows.size() : 0;
        log.info("[platform/distributer/customers/today] SQL 返回客户组数={} disId={}", rowCount, disId);

        PlatformDistributerTodayCustomersResponse response = new PlatformDistributerTodayCustomersResponse();
        if (rows == null || rows.isEmpty()) {
            Map<String, Object> diag = nxPlatformOrderAssignDao.queryPlatformDistributerCustomerDiagnostics(params);
            log.warn("[platform/distributer/customers/today] 列表为空 disId={} diagnostics={}", disId, diag);
            response.setCustomerCount(0);
            response.setUnDoTotal(0);
            return response;
        }

        List<PlatformDistributerCustomerItem> customers = new ArrayList<>();
        int unDoTotal = 0;
        for (PlatformDistributerCustomerRow row : rows) {
            PlatformDistributerCustomerItem item = toItem(row);
            customers.add(item);
            log.info("[platform/distributer/customers/today] 客户 source={} groupKey={} displayName={} totalCount={} unDo={} gbDepId={}",
                    item.getCustomerSource(), row.getGroupKey(), item.getDisplayName(),
                    item.getTotalCount(), item.getUnDo(), item.getGbDepartmentId());
            if (item.getUnDo() != null) {
                unDoTotal += item.getUnDo();
            }
        }

        response.setCustomers(customers);
        response.setCustomerCount(customers.size());
        response.setUnDoTotal(unDoTotal);
        log.info("[platform/distributer/customers/today] 完成 disId={} customerCount={} unDoTotal={}",
                disId, customers.size(), unDoTotal);
        return response;
    }

    private PlatformDistributerCustomerItem toItem(PlatformDistributerCustomerRow row) {
        boolean gbCustomer = SOURCE_GB.equalsIgnoreCase(StringUtils.defaultString(row.getCustomerSource()));

        PlatformDistributerCustomerItem item = new PlatformDistributerCustomerItem();
        item.setCustomerSource(gbCustomer ? SOURCE_GB : SOURCE_NX);
        item.setTotalCount(row.getTotalCount());
        item.setFinishCount(row.getFinishCount());
        item.setHasPrice(row.getHasPrice());
        item.setHasWeight(row.getHasWeight());
        item.setUnDo(row.getUndoCount());
        double total = row.getTotalSubtotal() != null ? row.getTotalSubtotal() : 0.0;
        item.setTwoSubtotal(new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
        item.setIsPlatformCustomer(1);
        item.setOrderSource(PlatformOrderDisplaySupport.ORDER_SOURCE_PLATFORM);
        item.setPlatformLabel(PlatformOrderDisplaySupport.PLATFORM_CUSTOMER_LABEL);
        item.setPlatformSort(PlatformOrderDisplaySupport.platformSortKey(true));
        item.setMarketId(row.getMarketId());
        item.setGbDepartmentId(row.getGbDepartmentId());
        item.setGbDepartmentName(row.getGbDepartmentName());

        if (gbCustomer) {
            item.setRouteDepFatherId(-1);
            item.setRouteGbDepFatherId(row.getGroupKey());
            item.setDisplayName(firstNonBlank(row.getGbDepartmentAttrName(), row.getGbDepartmentName(), "平台客户"));
            item.setDep(buildGbDisplayDep(row));
        } else {
            item.setRouteDepFatherId(row.getGroupKey());
            item.setRouteGbDepFatherId(-1);
            item.setDisplayName(firstNonBlank(row.getNxDepAttrName(), "平台客户"));
            item.setDep(loadNxDep(row.getGroupKey(), row.getNxDepAttrName()));
        }
        return item;
    }

    private NxDepartmentEntity loadNxDep(Integer depFatherId, String attrName) {
        NxDepartmentEntity dep = depFatherId != null ? nxDepartmentService.queryObject(depFatherId) : null;
        if (dep == null) {
            dep = new NxDepartmentEntity();
            dep.setNxDepartmentId(depFatherId);
        }
        if (StringUtils.isNotBlank(attrName)) {
            dep.setNxDepartmentAttrName(attrName);
        }
        dep.setIsPlatformCustomer(1);
        dep.setOrderSource(PlatformOrderDisplaySupport.ORDER_SOURCE_PLATFORM);
        dep.setPlatformLabel(PlatformOrderDisplaySupport.PLATFORM_CUSTOMER_LABEL);
        return dep;
    }

    private NxDepartmentEntity buildGbDisplayDep(PlatformDistributerCustomerRow row) {
        NxDepartmentEntity dep = new NxDepartmentEntity();
        dep.setNxDepartmentId(row.getGroupKey());
        dep.setNxDepartmentAttrName(firstNonBlank(row.getGbDepartmentAttrName(), row.getGbDepartmentName()));
        dep.setIsPlatformCustomer(1);
        dep.setOrderSource(PlatformOrderDisplaySupport.ORDER_SOURCE_PLATFORM);
        dep.setPlatformLabel(PlatformOrderDisplaySupport.PLATFORM_CUSTOMER_LABEL);
        return dep;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }
}

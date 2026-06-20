package com.nongxinle.service.platform;

import com.nongxinle.dao.NxPlatformOrderAssignDao;
import com.nongxinle.dao.NxPlatformOrderFulfillmentDao;
import com.nongxinle.dto.platform.customer.PlatformCustomerOrderDeleteRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerOrderLineItem;
import com.nongxinle.dto.platform.customer.PlatformCustomerOrderListRequest;
import com.nongxinle.dto.platform.customer.PlatformCustomerOrderUpdateRequest;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxPlatformOrderAssignEntity;
import com.nongxinle.entity.NxPlatformOrderFulfillmentEntity;
import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.utils.PlatformConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nongxinle.utils.DateUtils.formatWhatDay;

@Service
public class PlatformCustomerOrderServiceImpl implements PlatformCustomerOrderService {

    private static final String STATUS_CODE_UNMATCHED = "UNMATCHED";
    private static final String STATUS_CODE_SEARCHING = "SEARCHING";
    private static final String STATUS_CODE_ASSIGNED = "ASSIGNED";

    @Autowired
    private NxPlatformOrderAssignDao nxPlatformOrderAssignDao;
    @Autowired
    private NxPlatformOrderFulfillmentDao nxPlatformOrderFulfillmentDao;
    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;

    @Override
    public List<PlatformCustomerOrderLineItem> listTodayLines(PlatformCustomerOrderListRequest request) {
        validateListRequest(request);
        Map<String, Object> params = new HashMap<>();
        params.put("marketId", request.getMarketId());
        params.put("departmentId", request.getDepartmentId());
        params.put("applyDate", resolveApplyDate(request.getApplyDate()));
        List<PlatformCustomerOrderLineItem> lines = nxPlatformOrderAssignDao.queryCustomerPlatformOrderLines(params);
        if (lines != null) {
            for (int i = 0; i < lines.size(); i++) {
                PlatformCustomerOrderLineItem line = lines.get(i);
                enrichStatus(line);
                enrichDisplayLines(line, i + 1);
            }
        }
        return lines;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformCustomerOrderLineItem updateLine(PlatformCustomerOrderUpdateRequest request) {
        validateUpdateRequest(request);
        NxPlatformOrderAssignEntity assign = nxPlatformOrderAssignDao.queryByOrderId(request.getOrderId());
        assertEditableAssign(assign, request.getMarketId(), request.getDepartmentId());

        NxDepartmentOrdersEntity order = nxDepartmentOrdersService.queryObject(request.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getNxDoStatus() != null && order.getNxDoStatus() > 2) {
            throw new IllegalArgumentException("订单已进入配送流程，无法修改");
        }

        order.setNxDoQuantity(request.getQuantity().trim());
        order.setNxDoStandard(StringUtils.isNotBlank(request.getStandard()) ? request.getStandard().trim() : "");
        order.setNxDoRemark(StringUtils.isNotBlank(request.getRemark()) ? request.getRemark().trim() : "");
        nxDepartmentOrdersService.update(order);

        Map<String, Object> params = new HashMap<>();
        params.put("marketId", request.getMarketId());
        params.put("departmentId", request.getDepartmentId());
        params.put("applyDate", resolveApplyDate(null));
        List<Integer> orderIds = new ArrayList<>(1);
        orderIds.add(request.getOrderId());
        params.put("orderIds", orderIds);
        List<PlatformCustomerOrderLineItem> lines = nxPlatformOrderAssignDao.queryCustomerPlatformOrderLines(params);
        if (lines == null || lines.isEmpty()) {
            PlatformCustomerOrderLineItem fallback = new PlatformCustomerOrderLineItem();
            fallback.setOrderId(order.getNxDepartmentOrdersId());
            fallback.setNxGoodsId(order.getNxDoNxGoodsId());
            fallback.setGoodsName(order.getNxDoGoodsName());
            fallback.setQuantity(order.getNxDoQuantity());
            fallback.setStandard(order.getNxDoStandard());
            fallback.setRemark(order.getNxDoRemark());
            fallback.setAssignStatus(assign.getNxPoaAssignStatus());
            enrichStatus(fallback);
            enrichDisplayLines(fallback, 1);
            return fallback;
        }
        PlatformCustomerOrderLineItem line = lines.get(0);
        enrichStatus(line);
        enrichDisplayLines(line, 1);
        return line;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLine(PlatformCustomerOrderDeleteRequest request) {
        if (request == null || request.getOrderId() == null) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        if (request.getMarketId() == null || request.getDepartmentId() == null) {
            throw new IllegalArgumentException("marketId / departmentId 不能为空");
        }
        NxPlatformOrderAssignEntity assign = nxPlatformOrderAssignDao.queryByOrderId(request.getOrderId());
        assertEditableAssign(assign, request.getMarketId(), request.getDepartmentId());

        NxDepartmentOrdersEntity order = nxDepartmentOrdersService.queryObject(request.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getNxDoStatus() != null && order.getNxDoStatus() > 2) {
            throw new IllegalArgumentException("订单已进入配送流程，无法删除");
        }

        NxPlatformOrderFulfillmentEntity fulfillment =
                nxPlatformOrderFulfillmentDao.queryByOrderId(request.getOrderId());
        if (fulfillment != null) {
            nxPlatformOrderFulfillmentDao.deleteByOrderId(request.getOrderId());
        }
        nxPlatformOrderAssignDao.deleteByOrderId(request.getOrderId());
        nxDepartmentOrdersService.delete(request.getOrderId());
    }

    private void enrichStatus(PlatformCustomerOrderLineItem line) {
        if (line == null) {
            return;
        }
        if (PlatformConstants.ASSIGN_STATUS_ASSIGNED.equals(line.getAssignStatus())
                || (line.getAssignedDistributerId() != null && line.getAssignedDistributerId() > 0)) {
            line.setStatusCode(STATUS_CODE_ASSIGNED);
            line.setStatusText(buildAssignedShortText(line.getAssignSource()));
            line.setEditable(false);
            return;
        }
        if (line.getDefaultDistributerId() != null && line.getDefaultDistributerId() > 0) {
            line.setStatusCode(STATUS_CODE_SEARCHING);
            line.setStatusText("正在找配送商");
        } else {
            line.setStatusCode(STATUS_CODE_UNMATCHED);
            line.setStatusText("还没有匹配到配送商");
        }
        line.setEditable(PlatformConstants.ASSIGN_STATUS_PENDING.equals(line.getAssignStatus()));
    }

    private void enrichDisplayLines(PlatformCustomerOrderLineItem line, int rowIndex) {
        if (line == null) {
            return;
        }
        String goodsLabel = buildGoodsLabel(line);
        String qty = StringUtils.defaultString(line.getQuantity(), "—");
        String std = StringUtils.defaultString(line.getStandard(), "");
        line.setTitleLine(rowIndex + ". " + goodsLabel + " 订:" + qty + std);

        String shipQty = resolveShipQuantity(line);
        String stdForPrice = std.length() > 0 ? std : "—";
        if (hasValidPrice(line.getOrderPrice())) {
            String unitPrice = trimDecimal(line.getOrderPrice());
            String subtotal = hasValidPrice(line.getOrderSubtotal())
                    ? trimDecimal(line.getOrderSubtotal()) + "元"
                    : "—";
            line.setPriceLine("出货:" + shipQty + stdForPrice
                    + "  单价:" + unitPrice + "/" + stdForPrice
                    + "  小计:" + subtotal);
        } else {
            line.setPriceLine("出货:" + shipQty + stdForPrice
                    + "  单价:待确认  小计:—");
        }

        line.setSupplierLine(buildSupplierLine(line));
    }

    private String buildGoodsLabel(PlatformCustomerOrderLineItem line) {
        String name = StringUtils.isNotBlank(line.getGoodsName())
                ? line.getGoodsName().trim()
                : ("商品#" + (line.getNxGoodsId() != null ? line.getNxGoodsId() : line.getOrderId()));
        if (StringUtils.isNotBlank(line.getGoodsBrand())
                && !"null".equalsIgnoreCase(line.getGoodsBrand().trim())
                && name.indexOf(line.getGoodsBrand().trim()) < 0) {
            return line.getGoodsBrand().trim() + name;
        }
        return name;
    }

    private String resolveShipQuantity(PlatformCustomerOrderLineItem line) {
        if (StringUtils.isNotBlank(line.getWeight()) && isPositiveNumber(line.getWeight())) {
            return line.getWeight().trim();
        }
        if (StringUtils.isNotBlank(line.getQuantity())) {
            return line.getQuantity().trim();
        }
        return "—";
    }

    private String buildSupplierLine(PlatformCustomerOrderLineItem line) {
        if (STATUS_CODE_ASSIGNED.equals(line.getStatusCode())) {
            String name = StringUtils.isNotBlank(line.getAssignedDistributerName())
                    ? line.getAssignedDistributerName().trim()
                    : ("配送商#" + line.getAssignedDistributerId());
            if (PlatformConstants.ASSIGN_SOURCE_CUSTOMER_SELECTED_SUPPLIER.equals(line.getAssignSource())) {
                return "配送商:" + name + "（客户自选）";
            }
            return "配送商:" + name + "（平台已分配）";
        }
        if (STATUS_CODE_SEARCHING.equals(line.getStatusCode())) {
            if (StringUtils.isNotBlank(line.getDefaultDistributerName())) {
                return "正在找配送商 · 推荐:" + line.getDefaultDistributerName().trim();
            }
            return "正在找配送商";
        }
        return "还没有匹配到配送商";
    }

    private String buildAssignedShortText(String assignSource) {
        if (PlatformConstants.ASSIGN_SOURCE_CUSTOMER_SELECTED_SUPPLIER.equals(assignSource)) {
            return "客户自选";
        }
        return "已分配";
    }

    private boolean hasValidPrice(String price) {
        if (StringUtils.isBlank(price)) {
            return false;
        }
        try {
            return new BigDecimal(price.trim()).compareTo(new BigDecimal("0.1")) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isPositiveNumber(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        try {
            return new BigDecimal(value.trim()).compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String trimDecimal(String value) {
        if (StringUtils.isBlank(value)) {
            return "—";
        }
        try {
            return new BigDecimal(value.trim()).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException ex) {
            return value.trim();
        }
    }

    private void assertEditableAssign(NxPlatformOrderAssignEntity assign, Integer marketId, Integer departmentId) {
        if (assign == null) {
            throw new IllegalArgumentException("平台订单不存在");
        }
        if (!PlatformConstants.ASSIGN_MODE_PLATFORM.equals(assign.getNxPoaAssignMode())) {
            throw new IllegalArgumentException("非平台订货订单");
        }
        if (!marketId.equals(assign.getNxPoaMarketId())) {
            throw new IllegalArgumentException("订单不属于当前市场");
        }
        if (!departmentId.equals(assign.getNxPoaDepartmentId())
                && !departmentId.equals(assign.getNxPoaGbDepartmentId())) {
            throw new IllegalArgumentException("订单不属于当前饭店");
        }
        if (!PlatformConstants.ASSIGN_STATUS_PENDING.equals(assign.getNxPoaAssignStatus())) {
            throw new IllegalArgumentException("订单已匹配配送商，无法删除");
        }
        if (assign.getNxPoaAssignedDistributerId() != null && assign.getNxPoaAssignedDistributerId() > 0) {
            throw new IllegalArgumentException("订单已匹配配送商，无法删除");
        }
    }

    private void validateListRequest(PlatformCustomerOrderListRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getMarketId() == null) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("departmentId 不能为空");
        }
    }

    private void validateUpdateRequest(PlatformCustomerOrderUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getMarketId() == null) {
            throw new IllegalArgumentException("marketId 不能为空");
        }
        if (request.getDepartmentId() == null) {
            throw new IllegalArgumentException("departmentId 不能为空");
        }
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("orderId 不能为空");
        }
        if (StringUtils.isBlank(request.getQuantity())) {
            throw new IllegalArgumentException("quantity 不能为空");
        }
        try {
            if (Double.parseDouble(request.getQuantity().trim()) <= 0) {
                throw new IllegalArgumentException("quantity 必须大于 0");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("quantity 格式不正确");
        }
    }

    private String resolveApplyDate(String applyDate) {
        if (StringUtils.isNotBlank(applyDate)) {
            return applyDate.trim();
        }
        return formatWhatDay(0);
    }
}

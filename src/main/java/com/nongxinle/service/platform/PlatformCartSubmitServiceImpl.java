package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.PlatformCartLineCreateCommand;
import com.nongxinle.dto.platform.PlatformCartLineCreateResult;
import com.nongxinle.dto.platform.customer.PlatformCartAddWithSupplierResponse;
import com.nongxinle.dto.platform.customer.PlatformCartLineItem;
import com.nongxinle.dto.platform.customer.PlatformCartSubmitItemRequest;
import com.nongxinle.dto.platform.customer.PlatformCartSubmitRequest;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.service.NxDistributerService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 原 submitBySupplier：购物车临时阶段写入带配送商 SKU 的临时行（status=-1）。
 * 不创建 bill、不 assign；checkoutConfirm 才新建 bill 并挂行。
 */
@Service
public class PlatformCartSubmitServiceImpl implements PlatformCartSubmitService {

    private static final Logger log = LoggerFactory.getLogger(PlatformCartSubmitServiceImpl.class);

    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private PlatformGbNxOrderLineService platformGbNxOrderLineService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlatformCartAddWithSupplierResponse submitBySupplier(PlatformCartSubmitRequest request) {
        validateRequest(request);
        log.info("[platform/cart/addWithSupplier] gbDepartmentId={} nxDistributerId={} itemCount={}",
                request.getGbDepartmentId(),
                request.getNxDistributerId(),
                request.getItems() == null ? 0 : request.getItems().size());

        List<PlatformCartLineCreateResult> created = new ArrayList<>();
        for (PlatformCartSubmitItemRequest item : request.getItems()) {
            PlatformCartLineCreateCommand cmd = new PlatformCartLineCreateCommand();
            cmd.setCartOnly(true);
            cmd.setMarketId(request.getMarketId());
            cmd.setGbDepartmentId(request.getGbDepartmentId());
            cmd.setGbDepartmentFatherId(request.getGbDepartmentFatherId());
            cmd.setGbDistributerId(request.getGbDistributerId());
            cmd.setNxDistributerId(request.getNxDistributerId());
            cmd.setGbOrderUserId(request.getGbOrderUserId());
            cmd.setDeliveryDate(request.getDeliveryDate());
            cmd.setRemark(request.getRemark());
            cmd.setItem(item);
            created.add(platformGbNxOrderLineService.createPlatformCartLine(cmd));
        }

        PlatformCartAddWithSupplierResponse response = new PlatformCartAddWithSupplierResponse();
        response.setNxDistributerId(request.getNxDistributerId());
        NxDistributerEntity supplier = nxDistributerService.queryObject(request.getNxDistributerId());
        if (supplier != null) {
            response.setSupplierName(supplier.getNxDistributerName());
        }
        response.setAddedLineCount(created.size());
        response.setLines(toLineItems(created));
        log.info("[platform/cart/addWithSupplier] done lineCount={}", created.size());
        return response;
    }

    private List<PlatformCartLineItem> toLineItems(List<PlatformCartLineCreateResult> lines) {
        List<PlatformCartLineItem> result = new ArrayList<>();
        for (PlatformCartLineCreateResult line : lines) {
            PlatformCartLineItem item = new PlatformCartLineItem();
            item.setGbOrderId(line.getGbOrderId());
            item.setNxOrderId(line.getNxOrderId());
            item.setGoodsName(line.getGoodsName());
            item.setQuantity(line.getQuantity());
            item.setStandard(line.getStandard());
            item.setPriceConfirmStatus(line.getPriceConfirmStatus());
            item.setLineSubtotal(line.getLineSubtotal());
            result.add(item);
        }
        return result;
    }

    private void validateRequest(PlatformCartSubmitRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request 不能为空");
        }
        if (request.getGbDepartmentId() == null) {
            throw new IllegalArgumentException("gbDepartmentId 不能为空");
        }
        if (request.getGbDistributerId() == null) {
            throw new IllegalArgumentException("gbDistributerId 不能为空");
        }
        if (request.getNxDistributerId() == null) {
            throw new IllegalArgumentException("nxDistributerId 不能为空");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("items 不能为空");
        }
        Set<Integer> nxDisGoodsIds = new HashSet<>();
        for (PlatformCartSubmitItemRequest item : request.getItems()) {
            if (item.getNxDistributerGoodsId() == null) {
                throw new IllegalArgumentException("nxDistributerGoodsId 不能为空");
            }
            if (!nxDisGoodsIds.add(item.getNxDistributerGoodsId())) {
                throw new IllegalArgumentException("items 中存在重复 nxDistributerGoodsId");
            }
            if (StringUtils.isBlank(item.getQuantity()) || StringUtils.isBlank(item.getStandard())) {
                throw new IllegalArgumentException("quantity/standard 不能为空");
            }
        }
    }
}

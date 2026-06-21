package com.nongxinle.service.platform;

import com.nongxinle.dto.platform.customer.PlatformCartLineItem;
import com.nongxinle.dto.platform.customer.PlatformCartSupplierItem;
import com.nongxinle.dto.platform.customer.PlatformCheckoutLineItem;
import com.nongxinle.entity.GbDepartmentOrdersEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxGoodsEntity;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.NxDistributerService;
import com.nongxinle.service.NxGoodsService;
import com.nongxinle.utils.GbBillPlatformConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 购物车 / checkout 预览行展示字段（商品图、单价、批发商卡片等）。
 */
@Component
public class PlatformCartLineDisplaySupport {

    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxGoodsService nxGoodsService;

    public void enrichCartLine(PlatformCartLineItem item,
                               NxDepartmentOrdersEntity nxOrder,
                               GbDepartmentOrdersEntity gbOrder,
                               String priceConfirmStatus,
                               Map<Integer, NxDistributerEntity> supplierCache) {
        if (item == null || nxOrder == null) {
            return;
        }
        CartLineDisplay display = buildDisplay(nxOrder, gbOrder, priceConfirmStatus, supplierCache);
        applyToCartLine(item, display);
    }

    public void enrichCheckoutLine(PlatformCheckoutLineItem item,
                                   NxDepartmentOrdersEntity nxOrder,
                                   GbDepartmentOrdersEntity gbOrder,
                                   String priceConfirmStatus,
                                   Map<Integer, NxDistributerEntity> supplierCache) {
        if (item == null || nxOrder == null) {
            return;
        }
        CartLineDisplay display = buildDisplay(nxOrder, gbOrder, priceConfirmStatus, supplierCache);
        applyToCheckoutLine(item, display);
    }

    private CartLineDisplay buildDisplay(NxDepartmentOrdersEntity nxOrder,
                                         GbDepartmentOrdersEntity gbOrder,
                                         String priceConfirmStatus,
                                         Map<Integer, NxDistributerEntity> supplierCache) {
        CartLineDisplay display = new CartLineDisplay();
        display.remark = StringUtils.trimToEmpty(nxOrder.getNxDoRemark());
        display.hasSupplierSelected = PlatformCartLineSupport.hasCustomerSelectedSupplier(nxOrder);
        display.editable = Boolean.TRUE;

        resolveGoodsImages(nxOrder, display);
        resolveUnitPrice(nxOrder, gbOrder, priceConfirmStatus, display);
        if (display.hasSupplierSelected) {
            display.supplier = resolveSupplier(nxOrder.getNxDoDistributerId(), supplierCache);
        }
        return display;
    }

    private void resolveGoodsImages(NxDepartmentOrdersEntity nxOrder, CartLineDisplay display) {
        if (nxOrder.getNxDoDisGoodsId() != null && nxOrder.getNxDoDisGoodsId() > 0) {
            NxDistributerGoodsEntity goods = nxDistributerGoodsService.queryObject(nxOrder.getNxDoDisGoodsId());
            if (goods != null) {
                display.goodsImage = firstNonBlank(goods.getNxDgGoodsFile(), goods.getNxDgNxFatherImg());
                display.goodsImageLarge = firstNonBlank(goods.getNxDgGoodsFileLarge(), goods.getNxDgGoodsFile(),
                        goods.getNxDgNxFatherImg());
                return;
            }
        }
        if (nxOrder.getNxDoNxGoodsId() != null) {
            NxGoodsEntity goods = nxGoodsService.queryObject(nxOrder.getNxDoNxGoodsId());
            if (goods != null) {
                display.goodsImage = firstNonBlank(goods.getNxGoodsFile(), goods.getNxGoodsFileBig());
                display.goodsImageLarge = firstNonBlank(goods.getNxGoodsFileBig(), goods.getNxGoodsFile());
            }
        }
    }

    private void resolveUnitPrice(NxDepartmentOrdersEntity nxOrder,
                                  GbDepartmentOrdersEntity gbOrder,
                                  String priceConfirmStatus,
                                  CartLineDisplay display) {
        if (!GbBillPlatformConstants.PRICE_CONFIRM_CONFIRMED.equals(priceConfirmStatus)) {
            return;
        }
        String unitPrice = firstNonBlank(
                nxOrder.getNxDoPrice(),
                gbOrder == null ? null : gbOrder.getGbDoPrice());
        if (StringUtils.isNotBlank(unitPrice)) {
            display.unitPrice = trimDecimal(unitPrice);
            display.pricingStandard = StringUtils.isNotBlank(nxOrder.getNxDoStandard())
                    ? nxOrder.getNxDoStandard().trim()
                    : null;
        }
    }

    private PlatformCartSupplierItem resolveSupplier(Integer nxDistributerId,
                                                     Map<Integer, NxDistributerEntity> supplierCache) {
        if (nxDistributerId == null || nxDistributerId <= 0) {
            return null;
        }
        NxDistributerEntity supplier = null;
        if (supplierCache != null) {
            supplier = supplierCache.get(nxDistributerId);
        }
        if (supplier == null) {
            supplier = nxDistributerService.queryObject(nxDistributerId);
            if (supplierCache != null && supplier != null) {
                supplierCache.put(nxDistributerId, supplier);
            }
        }
        if (supplier == null) {
            return null;
        }
        PlatformCartSupplierItem item = new PlatformCartSupplierItem();
        item.setNxDistributerId(supplier.getNxDistributerId());
        item.setSupplierName(firstNonBlank(supplier.getNxDistributerShowName(), supplier.getNxDistributerName()));
        item.setSupplierLogo(supplier.getNxDistributerImg());
        item.setSupplierAddress(supplier.getNxDistributerAddress());
        item.setSupplierPhone(supplier.getNxDistributerPhone());
        return item;
    }

    private static void applyToCartLine(PlatformCartLineItem item, CartLineDisplay display) {
        item.setGoodsImage(display.goodsImage);
        item.setGoodsImageLarge(display.goodsImageLarge);
        item.setUnitPrice(display.unitPrice);
        item.setPricingStandard(display.pricingStandard);
        item.setRemark(display.remark);
        item.setHasSupplierSelected(display.hasSupplierSelected);
        item.setEditable(display.editable);
        item.setSupplier(display.supplier);
    }

    private static void applyToCheckoutLine(PlatformCheckoutLineItem item, CartLineDisplay display) {
        item.setGoodsImage(display.goodsImage);
        item.setGoodsImageLarge(display.goodsImageLarge);
        item.setUnitPrice(display.unitPrice);
        item.setPricingStandard(display.pricingStandard);
        item.setRemark(display.remark);
        item.setHasSupplierSelected(display.hasSupplierSelected);
        item.setEditable(display.editable);
        item.setSupplier(display.supplier);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static String trimDecimal(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            return new java.math.BigDecimal(value.trim())
                    .stripTrailingZeros()
                    .toPlainString();
        } catch (NumberFormatException ex) {
            return value.trim();
        }
    }

    private static final class CartLineDisplay {
        private String goodsImage;
        private String goodsImageLarge;
        private String unitPrice;
        private String pricingStandard;
        private String remark;
        private boolean hasSupplierSelected;
        private Boolean editable;
        private PlatformCartSupplierItem supplier;
    }
}

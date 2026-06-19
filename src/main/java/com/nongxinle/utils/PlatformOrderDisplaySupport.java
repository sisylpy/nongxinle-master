package com.nongxinle.utils;

import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.OutGoodsSimpleDTO;
import com.nongxinle.entity.OutOrderSimpleDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Phase 2b Round 2-B：配送商端平台订单展示（排序/标签/汇总，不改出库主链）。
 */
public final class PlatformOrderDisplaySupport {

    public static final String ORDER_SOURCE_PLATFORM = "PLATFORM";
    public static final String ORDER_SOURCE_OWN = "OWN";
    public static final String PLATFORM_LABEL = "平台";
    public static final String PLATFORM_CUSTOMER_LABEL = "平台客户";

    private PlatformOrderDisplaySupport() {
    }

    public static void enrichOutGoodsList(List<OutGoodsSimpleDTO> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }
        for (OutGoodsSimpleDTO goods : goodsList) {
            enrichOutGoods(goods);
        }
    }

    public static void enrichOutGoods(OutGoodsSimpleDTO goods) {
        if (goods == null) {
            return;
        }
        List<OutOrderSimpleDTO> orders = goods.getNxDepartmentOrdersEntities();
        if (orders == null || orders.isEmpty()) {
            goods.setPlatformQuantity("0");
            goods.setOwnQuantity("0");
            return;
        }
        normalizeOrderPlatformFields(orders);
        sortOutOrderLines(orders);

        double platformQty = 0;
        double ownQty = 0;
        for (OutOrderSimpleDTO order : orders) {
            double qty = parseQuantity(order.getNxDoQuantity());
            if (isPlatformOrder(order)) {
                platformQty += qty;
            } else {
                ownQty += qty;
            }
        }
        goods.setPlatformQuantity(formatQuantity(platformQty));
        goods.setOwnQuantity(formatQuantity(ownQty));
    }

    public static void normalizeOrderPlatformFields(List<OutOrderSimpleDTO> orders) {
        for (OutOrderSimpleDTO order : orders) {
            if (order == null) {
                continue;
            }
            boolean platform = isPlatformFlag(order.getIsPlatformOrder());
            order.setIsPlatformOrder(platform ? 1 : 0);
            order.setOrderSource(platform ? ORDER_SOURCE_PLATFORM : ORDER_SOURCE_OWN);
            order.setPlatformLabel(platform ? PLATFORM_LABEL : null);
            order.setPriceEditable(1);
            order.setPlatformPriceLabel(platform ? PlatformOrderPriceSupport.PLATFORM_PRICE_LABEL : null);
            order.setActualPrice(order.getNxDoPrice());
            order.setExpectPrice(order.getNxDoExpectPrice());
            order.setPriceDifferent(order.getNxDoPriceDifferent());
            order.setPlatformSort(platform ? 0 : 1);
        }
    }

    public static void sortOutOrderLines(List<OutOrderSimpleDTO> orders) {
        if (orders == null || orders.size() < 2) {
            return;
        }
        orders.sort(Comparator
                .comparingInt((OutOrderSimpleDTO o) -> o.getPlatformSort() != null ? o.getPlatformSort() : platformSortKey(isPlatformOrder(o)))
                .thenComparing(o -> o.getNxDoDepartmentFatherId() != null ? o.getNxDoDepartmentFatherId() : 0)
                .thenComparing(o -> o.getNxDepartmentOrdersId() != null ? o.getNxDepartmentOrdersId() : 0));
    }

    public static void applyPlatformCustomerFields(NxDepartmentEntity dep, boolean isPlatformCustomer) {
        if (dep == null) {
            return;
        }
        dep.setIsPlatformCustomer(isPlatformCustomer ? 1 : 0);
        dep.setPlatformSort(platformSortKey(isPlatformCustomer));
        dep.setOrderSource(isPlatformCustomer ? ORDER_SOURCE_PLATFORM : ORDER_SOURCE_OWN);
        dep.setPlatformLabel(isPlatformCustomer ? PLATFORM_CUSTOMER_LABEL : null);
    }

    public static void enrichCustomerDepMap(Map<String, Object> mapDep, boolean isPlatformCustomer) {
        if (mapDep == null) {
            return;
        }
        mapDep.put("isPlatformCustomer", isPlatformCustomer ? 1 : 0);
        mapDep.put("platformSort", platformSortKey(isPlatformCustomer));
        mapDep.put("orderSource", isPlatformCustomer ? ORDER_SOURCE_PLATFORM : ORDER_SOURCE_OWN);
        mapDep.put("platformLabel", isPlatformCustomer ? PLATFORM_CUSTOMER_LABEL : null);
    }

    public static Comparator<Map<String, Object>> customerDepMapComparator() {
        return (a, b) -> {
            int sortA = a.get("platformSort") instanceof Number ? ((Number) a.get("platformSort")).intValue() : 1;
            int sortB = b.get("platformSort") instanceof Number ? ((Number) b.get("platformSort")).intValue() : 1;
            if (sortA != sortB) {
                return Integer.compare(sortA, sortB);
            }
            Object depA = a.get("dep");
            Object depB = b.get("dep");
            String pinyinA = depA instanceof NxDepartmentEntity ? ((NxDepartmentEntity) depA).getNxDepartmentPinyin() : null;
            String pinyinB = depB instanceof NxDepartmentEntity ? ((NxDepartmentEntity) depB).getNxDepartmentPinyin() : null;
            if (pinyinA == null && pinyinB == null) {
                return 0;
            }
            if (pinyinA == null) {
                return 1;
            }
            if (pinyinB == null) {
                return -1;
            }
            return pinyinA.compareTo(pinyinB);
        };
    }

    public static Comparator<NxDepartmentEntity> departmentEntityComparator() {
        return (a, b) -> {
            int sortA = a.getPlatformSort() != null ? a.getPlatformSort() : platformSortKey(a.getIsPlatformCustomer() != null && a.getIsPlatformCustomer() == 1);
            int sortB = b.getPlatformSort() != null ? b.getPlatformSort() : platformSortKey(b.getIsPlatformCustomer() != null && b.getIsPlatformCustomer() == 1);
            if (sortA != sortB) {
                return Integer.compare(sortA, sortB);
            }
            String pinyinA = a.getNxDepartmentPinyin();
            String pinyinB = b.getNxDepartmentPinyin();
            if (pinyinA == null && pinyinB == null) {
                return 0;
            }
            if (pinyinA == null) {
                return 1;
            }
            if (pinyinB == null) {
                return -1;
            }
            return pinyinA.compareTo(pinyinB);
        };
    }

    public static Set<Integer> toPlatformDepIdSet(List<Integer> depIds) {
        if (depIds == null || depIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(depIds);
    }

    public static int platformSortKey(boolean isPlatform) {
        return isPlatform ? 0 : 1;
    }

    public static boolean isPlatformOrder(OutOrderSimpleDTO order) {
        if (order == null) {
            return false;
        }
        if (isPlatformFlag(order.getIsPlatformOrder())) {
            return true;
        }
        return ORDER_SOURCE_PLATFORM.equals(order.getOrderSource());
    }

    private static boolean isPlatformFlag(Integer flag) {
        return flag != null && flag == 1;
    }

    private static double parseQuantity(String quantity) {
        if (quantity == null || quantity.trim().isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(quantity.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String formatQuantity(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.valueOf(value);
    }
}

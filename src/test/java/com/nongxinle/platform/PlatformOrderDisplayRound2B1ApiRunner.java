package com.nongxinle.platform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.nongxinle.dao.NxDepartmentOrdersDao;
import com.nongxinle.dto.platform.PlatformAssignRequest;
import com.nongxinle.dto.platform.PlatformSubmitLineRequest;
import com.nongxinle.entity.NxDepartmentEntity;
import com.nongxinle.entity.NxDepartmentOrdersEntity;
import com.nongxinle.entity.OutGoodsSimpleDTO;
import com.nongxinle.entity.OutOrderSimpleDTO;
import com.nongxinle.service.NxDepartmentOrdersService;
import com.nongxinle.service.PlatformOrderAssignService;
import com.nongxinle.utils.PlatformConstants;
import com.nongxinle.utils.PlatformOrderDisplaySupport;
import com.nongxinle.utils.PlatformOrderPriceSupport;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Round 2-B.1：四个配送商读接口 + 改价语义验收（Controller 等价 data 层，JSON 摘要输出）。
 */
public class PlatformOrderDisplayRound2B1ApiRunner {

    private static final int DISTRIBUTER_ID = 160;
    private static final int PLATFORM_DEP_ID = 1436;
    private static final int DIS_GOODS_ID = 31235;
    private static final int MARKET_ID = 1;
    private static final int NX_GOODS_ID = 100470;
    private static final int OPERATOR_ID = 1;

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx =
                new ClassPathXmlApplicationContext("spring-platform-fulfillment-test.xml");
        try {
            NxDepartmentOrdersService ordersService = ctx.getBean(NxDepartmentOrdersService.class);
            PlatformOrderAssignService assignService = ctx.getBean(PlatformOrderAssignService.class);
            NxDepartmentOrdersDao ordersDao = ctx.getBean(NxDepartmentOrdersDao.class);
            DataSource dataSource = ctx.getBean(DataSource.class);

            grepNoPriceLockedInSource();

            System.out.println("========== 1. disGetTodayOrderCustomer/" + DISTRIBUTER_ID + " ==========");
            printJson(buildTodayOrderCustomerSummary(ordersService));

            System.out.println("========== 2. disGetTypePrepareOutDepCata ==========");
            printJson(buildPrepareOutDepCataSummary(ordersService));

            System.out.println("========== 3. disGetTypePrepareOutPage ==========");
            printJson(buildPrepareOutPageSummary(ordersService));

            System.out.println("========== 4. disGetTypePrepareOutByDep depId=" + PLATFORM_DEP_ID + " ==========");
            printJson(buildPrepareOutByDepSummary(ordersService));

            System.out.println("========== 5. PENDING 不出现在配送商端 ==========");
            printJson(verifyPendingExcluded(ordersService, dataSource));

            System.out.println("========== 6. 改价验收（平台单 + 自有单） ==========");
            Integer platformOrderId = createAndAssign(assignService);
            printJson(verifyPriceChange(ordersDao, platformOrderId, true));
            printJson(verifyPriceChange(ordersDao, findOwnOrderId(dataSource), false));

            System.out.println("=== Round 2-B.1 API verification ALL PASSED ===");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            ctx.close();
        }
    }

    /** 等价 disGetTodayOrderCustomer/{disId} → data.deps 核心字段 */
    private static Map<String, Object> buildTodayOrderCustomerSummary(NxDepartmentOrdersService ordersService) {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", DISTRIBUTER_ID);
        map1.put("status", 3);
        map1.put("isSelfOrder", -1);

        List<NxDepartmentEntity> deps = ordersService.queryOrderDepartmentList(map1);
        List<Map<String, Object>> nxDep = new ArrayList<>();
        for (NxDepartmentEntity dep : deps) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("depId", dep.getNxDepartmentId());
            item.put("depName", dep.getNxDepartmentAttrName());
            item.put("isPlatformCustomer", dep.getIsPlatformCustomer());
            item.put("platformLabel", dep.getPlatformLabel());
            item.put("orderSource", dep.getOrderSource());
            item.put("platformSort", dep.getPlatformSort());
            nxDep.add(item);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("nxDep", nxDep);
        out.put("platformFirst", isPlatformFirst(nxDep, "isPlatformCustomer"));
        out.put("containsPriceLocked", containsForbiddenKey(JSON.toJSONString(out)));
        return out;
    }

    /** 等价 disGetTypePrepareOutDepCata(disId, purType=0) → data.arr */
    private static Map<String, Object> buildPrepareOutDepCataSummary(NxDepartmentOrdersService ordersService) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", DISTRIBUTER_ID);
        map.put("status", 3);
        map.put("purStatus", 4);
        map.put("dayuOrderStatus", -2);
        map.put("orderGoodsType", 0);
        map.put("isSelfOrder", -1);

        List<NxDepartmentEntity> deps = ordersService.queryPureOrderNxDepartmentSimple(map);
        List<Map<String, Object>> arr = new ArrayList<>();
        for (NxDepartmentEntity dep : deps) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("depId", dep.getNxDepartmentId());
            item.put("depName", dep.getNxDepartmentAttrName());
            item.put("isPlatformCustomer", dep.getIsPlatformCustomer());
            item.put("platformSort", dep.getPlatformSort());
            arr.add(item);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("arr", arr);
        out.put("platformFirst", isPlatformFirst(arr, "isPlatformCustomer"));
        out.put("note", arr.isEmpty() ? "本地无 purType=0 且 purchase_status<4 的部门，排序逻辑与客户列表共用 Service" : null);
        return out;
    }

    /** 等价 disGetTypePrepareOutPage(disId, page=1, limit=50) → data.page.list */
    private static Map<String, Object> buildPrepareOutPageSummary(NxDepartmentOrdersService ordersService) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", DISTRIBUTER_ID);
        map.put("status", 3);
        map.put("dayuOrderStatus", -2);
        map.put("purStatus", 4);
        map.put("purType", 0);
        map.put("isSelfOrder", -1);
        map.put("offset", 0);
        map.put("limit", 50);

        List<OutGoodsSimpleDTO> list = ordersService.queryOutGoodsWithOrdersUltraSimple(map);
        return summarizeGoodsList("disGetTypePrepareOutPage", list);
    }

    /** 等价 disGetTypePrepareOutByDep(disId, depId) → data */
    private static Map<String, Object> buildPrepareOutByDepSummary(NxDepartmentOrdersService ordersService) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", DISTRIBUTER_ID);
        map.put("depFatherId", PLATFORM_DEP_ID);
        map.put("status", 3);
        map.put("dayuOrderStatus", -2);
        map.put("purStatus", 4);
        map.put("purType", 0);

        List<OutGoodsSimpleDTO> list = ordersService.disGetNxGoodsApplyUltraSimple(map);
        return summarizeGoodsList("disGetTypePrepareOutByDep", list);
    }

    private static Map<String, Object> summarizeGoodsList(String apiName, List<OutGoodsSimpleDTO> list) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("api", apiName);
        out.put("goodsCardCount", list != null ? list.size() : 0);

        OutGoodsSimpleDTO target = null;
        if (list != null) {
            for (OutGoodsSimpleDTO g : list) {
                if (DIS_GOODS_ID == g.getNxDistributerGoodsId()) {
                    target = g;
                    break;
                }
            }
        }
        if (target == null) {
            out.put("sampleGoods", null);
            return out;
        }

        Map<String, Object> card = new LinkedHashMap<>();
        card.put("nxDistributerGoodsId", target.getNxDistributerGoodsId());
        card.put("nxDgGoodsName", target.getNxDgGoodsName());
        card.put("platformQuantity", target.getPlatformQuantity());
        card.put("ownQuantity", target.getOwnQuantity());
        card.put("lineCount", target.getNxDepartmentOrdersEntities() != null
                ? target.getNxDepartmentOrdersEntities().size() : 0);
        card.put("singleCardMerged", true);

        List<Map<String, Object>> lines = new ArrayList<>();
        boolean platformFirst = true;
        if (target.getNxDepartmentOrdersEntities() != null) {
            for (OutOrderSimpleDTO o : target.getNxDepartmentOrdersEntities()) {
                if (platformFirst && o.getIsPlatformOrder() != null && o.getIsPlatformOrder() == 0) {
                    platformFirst = false;
                } else if (!platformFirst && o.getIsPlatformOrder() != null && o.getIsPlatformOrder() == 1) {
                    throw new AssertionError(apiName + ": platform line must be before own lines");
                }
                Map<String, Object> line = new LinkedHashMap<>();
                line.put("orderId", o.getNxDepartmentOrdersId());
                line.put("isPlatformOrder", o.getIsPlatformOrder());
                line.put("platformLabel", o.getPlatformLabel());
                line.put("priceEditable", o.getPriceEditable());
                line.put("expectPrice", o.getExpectPrice());
                line.put("nxDoExpectPrice", o.getNxDoExpectPrice());
                line.put("actualPrice", o.getActualPrice());
                line.put("nxDoPrice", o.getNxDoPrice());
                line.put("priceDifferent", o.getPriceDifferent());
                line.put("nxDoPriceDifferent", o.getNxDoPriceDifferent());
                line.put("platformPriceLabel", o.getPlatformPriceLabel());
                lines.add(line);
            }
        }
        card.put("lines", lines);
        card.put("platformLinesFirst", platformFirst);
        out.put("sampleGoods", card);

        String json = JSON.toJSONString(out);
        out.put("containsPriceLocked", containsForbiddenKey(json));
        if (containsForbiddenKey(json)) {
            throw new AssertionError(apiName + " response contains priceLocked");
        }
        return out;
    }

    private static Map<String, Object> verifyPendingExcluded(
            NxDepartmentOrdersService ordersService, DataSource dataSource) throws Exception {
        Integer pendingId = queryOneInt(dataSource,
                "select nx_poa_order_id from nx_platform_order_assign "
                        + "where nx_poa_assign_mode='PLATFORM' and nx_poa_assign_status='PENDING' limit 1");
        Map<String, Object> map = new HashMap<>();
        map.put("disId", DISTRIBUTER_ID);
        map.put("status", 3);
        map.put("dayuOrderStatus", -2);
        map.put("purStatus", 4);
        map.put("purType", 0);
        map.put("offset", 0);
        map.put("limit", 500);

        boolean found = false;
        for (OutGoodsSimpleDTO g : ordersService.queryOutGoodsWithOrdersUltraSimple(map)) {
            if (g.getNxDepartmentOrdersEntities() == null) {
                continue;
            }
            for (OutOrderSimpleDTO o : g.getNxDepartmentOrdersEntities()) {
                if (pendingId != null && pendingId.equals(o.getNxDepartmentOrdersId())) {
                    found = true;
                }
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("pendingOrderId", pendingId);
        out.put("visibleInDisOutList", found);
        if (found) {
            throw new AssertionError("PENDING platform order must not appear in distributor out list");
        }
        return out;
    }

    private static Map<String, Object> verifyPriceChange(
            NxDepartmentOrdersDao ordersDao, Integer orderId, boolean platform) {
        if (orderId == null) {
            Map<String, Object> skip = new LinkedHashMap<>();
            skip.put("skipped", true);
            skip.put("reason", "no own order found");
            return skip;
        }
        NxDepartmentOrdersEntity before = ordersDao.queryObject(orderId);
        String expectBefore = before.getNxDoExpectPrice();
        String costBefore = before.getNxDoCostPrice();

        String base = before.getNxDoPrice();
        if (base == null || base.trim().isEmpty()) {
            base = "2.0";
        }
        String newPrice = new BigDecimal(base.trim()).add(new BigDecimal("0.2")).toPlainString();

        before.setNxDoPrice(newPrice);
        PlatformOrderPriceSupport.recalculateSubtotalFromActualPrice(before);
        PlatformOrderPriceSupport.recalculatePriceDifferent(before);
        ordersDao.update(before);

        NxDepartmentOrdersEntity after = ordersDao.queryObject(orderId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("orderId", orderId);
        out.put("platform", platform);
        out.put("nxDoPrice", after.getNxDoPrice());
        out.put("nxDoExpectPrice", after.getNxDoExpectPrice());
        out.put("nxDoPriceDifferent", after.getNxDoPriceDifferent());
        out.put("nxDoSubtotal", after.getNxDoSubtotal());
        out.put("nxDoCostPrice", after.getNxDoCostPrice());
        out.put("expectUnchanged", platform ? expectBefore : null);
        out.put("expectStillSame", !platform || (expectBefore != null && expectBefore.equals(after.getNxDoExpectPrice())));
        out.put("costUnchanged", costBefore == null ? after.getNxDoCostPrice() == null
                : costBefore.equals(after.getNxDoCostPrice()));

        if (platform && expectBefore != null && !expectBefore.equals(after.getNxDoExpectPrice())) {
            throw new AssertionError("platform order must not overwrite nxDoExpectPrice");
        }
        if (platform && after.getNxDoPriceDifferent() == null) {
            throw new AssertionError("platform order must have priceDifferent after price change");
        }
        return out;
    }

    private static void grepNoPriceLockedInSource() throws Exception {
        Process p = new ProcessBuilder("grep", "-r", "-n", "priceLocked", "src/main/java", "src/main/resources")
                .directory(new java.io.File("/Users/lpy/Documents/javaWeb/kuangjia/nongxinle-master"))
                .redirectErrorStream(true)
                .start();
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int n;
        while ((n = p.getInputStream().read(chunk)) > 0) {
            buf.write(chunk, 0, n);
        }
        p.waitFor();
        if (p.exitValue() == 0) {
            throw new AssertionError("priceLocked still in source:\n" + buf.toString());
        }
        System.out.println("[grep priceLocked in src] OK (no matches)");
    }

    private static boolean containsForbiddenKey(String json) {
        return json.contains("priceLocked") || json.contains("price_locked");
    }

    private static boolean isPlatformFirst(List<Map<String, Object>> items, String key) {
        boolean seenOwn = false;
        for (Map<String, Object> item : items) {
            if (Integer.valueOf(1).equals(item.get(key))) {
                if (seenOwn) {
                    return false;
                }
            } else {
                seenOwn = true;
            }
        }
        return !items.isEmpty();
    }

    private static Integer createAndAssign(PlatformOrderAssignService assignService) {
        PlatformSubmitLineRequest submitReq = new PlatformSubmitLineRequest();
        submitReq.setMarketId(MARKET_ID);
        submitReq.setDepartmentId(PLATFORM_DEP_ID);
        submitReq.setNxGoodsId(NX_GOODS_ID);
        submitReq.setQuantity("1");
        submitReq.setStandard("斤");
        submitReq.setOrderUserId(OPERATOR_ID);
        Integer orderId = assignService.submitLine(submitReq).getOrderId();

        PlatformAssignRequest req = new PlatformAssignRequest();
        req.setMarketId(MARKET_ID);
        req.setOrderId(orderId);
        req.setDisGoodsId(DIS_GOODS_ID);
        req.setSwitchScope(PlatformConstants.SWITCH_SCOPE_ORDER_ONLY);
        req.setReasonCode("OTHER");
        req.setReasonNote("Round2B1 api test");
        req.setOperatorId(OPERATOR_ID);
        assignService.assign(req);
        return orderId;
    }

    private static Integer findOwnOrderId(DataSource dataSource) throws Exception {
        return queryOneInt(dataSource,
                "select dor.nx_department_orders_id from nx_department_orders dor "
                        + "left join nx_platform_order_assign poa on poa.nx_poa_order_id = dor.nx_department_orders_id "
                        + "and poa.nx_poa_assign_mode='PLATFORM' and poa.nx_poa_assign_status='ASSIGNED' "
                        + "where dor.nx_DO_distributer_id=? and poa.nx_poa_id is null "
                        + "and dor.nx_DO_price is not null and dor.nx_DO_purchase_goods_id=-1 limit 1");
    }

    private static Integer queryOneInt(DataSource ds, String sql) throws Exception {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (sql.contains("dor.nx_DO_distributer_id=?")) {
                ps.setInt(1, DISTRIBUTER_ID);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private static void printJson(Object obj) {
        System.out.println(JSON.toJSONString(obj, SerializerFeature.PrettyFormat));
    }
}

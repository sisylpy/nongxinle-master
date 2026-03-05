package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nongxinle.dto.NxDepartmentOrdersSimpleDTO;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.nongxinle.dao.NxDepartmentOrdersDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.PinYin4jUtils.getHeadStringByString;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@Service("nxDepartmentOrdersService")
public class NxDepartmentOrdersServiceImpl implements NxDepartmentOrdersService {
    @Autowired
    private NxDepartmentOrdersDao nxDepartmentOrdersDao;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;

    @Autowired
    private NxDepartmentService nxDepartmentService;
    @Autowired
    private NxDistributerNxDistributerService nxDistributerNxDistributerService;
    @Autowired
    private NxDistributerBlockService nxDistributerBlockService;
    @Autowired
    private NxOrderOcrTrainingDataService nxOrderOcrTrainingDataService;
    @Autowired
    private NxDistributerGoodsService nxDistributerGoodsService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;
    @Autowired
    private NxDistributerPurchaseGoodsService nxDistributerPurchaseGoodsService;
    @Autowired
    private NxDistributerPurchaseBatchService nxDistributerPurchaseBatchService;
    @Autowired
    private NxDistributerService nxDistributerService;
    @Autowired
    private NxJrdhSupplierService jrdhSupplierService;
    @Autowired
    private NxJrdhUserService nxJrdhUserService;
    @Autowired
    private NxDistributerGoodsShelfStockService nxDisGoodsShelfStockService;
    @Autowired
    private NxGoodsService nxGoodsService;
    @Autowired
    private NxAliasService nxAliasService;


    private static final Logger logger = LoggerFactory.getLogger(NxDepartmentOrdersServiceImpl.class);

    @Override
    public List<NxDepartmentOrdersEntity> queryDisOrdersByParams(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryDisOrdersByParams(map);
    }

    @Override
    public List<NxDepartmentEntity> queryDistributerTodayDepartments(Map<String, Object> map) {
        return nxDepartmentOrdersDao.queryDistributerTodayDepartments(map);
    }


    @Override
    public void save(NxDepartmentOrdersEntity nxDepartmentOrders) {
        nxDepartmentOrdersDao.save(nxDepartmentOrders);

    }

    @Override
    public List<NxDistributerFatherGoodsEntity> disGetUnPlanPurchaseApplys(Map<String, Object> map) {
        return nxDepartmentOrdersDao.disGetUnPlanPurchaseApplys(map);
    }


//	///////////


    @Override
    public void update(NxDepartmentOrdersEntity nxDepartmentOrders) {
        nxDepartmentOrdersDao.update(nxDepartmentOrders);
    }

    @Override
    public void delete(Integer nxDepartmentOrdersId) {
        nxDepartmentOrdersDao.delete(nxDepartmentOrdersId);
    }

    @Override
    public int queryTotalByParams(Map<String, Object> map) {
        return nxDepartmentOrdersDao.queryTotalByParams(map);
    }


    @Override
    public List<NxDepartmentOrdersEntity> disQueryDisOrdersByParams(Map<String, Object> map) {

        return nxDepartmentOrdersDao.disQueryDisOrdersByParams(map);
    }


    @Override
    public List<NxDepartmentOrdersEntity> queryReturnOrdersByBillId(Integer billId) {

        return nxDepartmentOrdersDao.queryReturnOrdersByBillId(billId);
    }

    @Override
    public List<NxDepartmentEntity> queryOrderDepartmentList(Map<String, Object> map1) {
        return nxDepartmentOrdersDao.queryOrderDepartmentList(map1);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryDepOrdersOrderFatherGoods(Map<String, Object> map) {
        return nxDepartmentOrdersDao.queryDepOrdersOrderFatherGoods(map);
    }

    @Override
    public Integer queryDepOrdersAcount(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryDepOrdersAcount(map);
    }

    @Override
    public Double queryDepOrdersSubtotal(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryDepOrdersSubtotal(map);
    }


    @Override
    public Double queryDepOrdersProfitSubtotal(Map<String, Object> map2) {

        return nxDepartmentOrdersDao.queryDepOrdersProfitSubtotal(map2);

    }

    @Override
    public List<NxRestrauntEntity> queryOrderNxRestrauntList(Map<String, Object> map1) {

        return nxDepartmentOrdersDao.queryOrderNxRestrauntList(map1);

    }

    @Override
    public List<GbDepartmentEntity> queryOrderGbDepartmentList(Map<String, Object> map1) {

        return nxDepartmentOrdersDao.queryOrderGbDepartmentList(map1);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryDepWeightOrder(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryDepWeightOrder(map);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryNotWeightDisOrdersByParams(Map<String, Object> map1) {

        return nxDepartmentOrdersDao.queryNotWeightDisOrdersByParams(map1);
    }

    @Override
    public List<NxDepartmentOrdersSimpleDTO> queryNotWeightDisOrdersSimpleByParams(Map<String, Object> map1) {
        return nxDepartmentOrdersDao.queryNotWeightDisOrdersSimpleByParams(map1);
    }

    @Override
    public List<NxDepartmentOrdersSimpleDTO> queryDisOrdersSimpleByDepIds(Map<String, Object> map1) {
        return nxDepartmentOrdersDao.queryDisOrdersSimpleByDepIds(map1);
    }

    @Override
    public List<GbDistributerEntity> queryOrderGbDistributerList(Map<String, Object> map1) {

        return nxDepartmentOrdersDao.queryOrderGbDistributerList(map1);
    }

    @Override
    public void saveForGb(NxDepartmentOrdersEntity ordersEntity) {
        nxDepartmentOrdersDao.save(ordersEntity);
    }


    @Override
    public List<NxDistributerFatherGoodsEntity> disGetUnPlanPurchaseApplysSearch(Map<String, Object> map) {

        return nxDepartmentOrdersDao.disGetUnPlanPurchaseApplysSearch(map);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryDepWeightOrderSearch(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryDepWeightOrderSearch(map);
    }

    @Override
    public int disGetPurchaseGoodsApplysCount(Map<String, Object> map1) {

        return nxDepartmentOrdersDao.disGetPurchaseGoodsApplysCount(map1);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> disGetUnPlanPurchaseApplysNew(Map<String, Object> map) {

        return nxDepartmentOrdersDao.disGetUnPlanPurchaseApplysNew(map);
    }


    @Override
    public NxDepartmentOrdersEntity queryObjectNew(Integer nxDepartmentOrdersId) {

        return nxDepartmentOrdersDao.queryObjectNew(nxDepartmentOrdersId);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryGreatGrandOrderFatherGoods(Map<String, Object> map) {
        return nxDepartmentOrdersDao.queryGreatGrandOrderFatherGoods(map);
    }

    @Override
    public List<GreatGrandFatherGoodsSimpleDTO> queryGreatGrandOrderFatherGoodsUltraSimple(Map<String, Object> map) {
        return nxDepartmentOrdersDao.queryGreatGrandOrderFatherGoodsUltraSimple(map);
    }

    @Override
    public List<OutGoodsSimpleDTO> queryOutGoodsWithOrdersUltraSimple(Map<String, Object> map) {
        return nxDepartmentOrdersDao.queryOutGoodsWithOrdersUltraSimple(map);
    }


    @Override
    public List<NxDistributerFatherGoodsEntity> queryDisGoodsForTodayOrders(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryDisGoodsForTodayOrders(map);
    }

    @Override
    public List<NxDepartmentEntity> queryPureOrderNxDepartment(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryPureOrderNxDepartment(map);
    }


    public List<NxDepartmentEntity> queryPureOrderNxDepartmentSimple(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryPureOrderNxDepartmentSimple(map);
    }

    @Override
    public Map<Integer, Map<String, Integer>> batchQueryDepStats(List<Integer> depIds, Map<String, Object> params) {
        // 使用 selectList 而不是 selectOne
        List<Map<String, Object>> results = nxDepartmentOrdersDao.batchQueryDepStats(depIds, params);

        // 转换结果
        return results.stream()
                .collect(Collectors.toMap(
                        map -> (Integer) map.get("depId"),
                        map -> {
                            Map<String, Integer> stats = new HashMap<>();
                            stats.put("count0", ((Number) map.get("count0")).intValue());
                            stats.put("count1", ((Number) map.get("count1")).intValue());
                            stats.put("count2", ((Number) map.get("count2")).intValue());
                            return stats;
                        }
                ));
    }


    @Override
    public Map<Integer, Map<String, Integer>> batchQueryGbDepStats(List<Integer> gbDepIds, Map<String, Object> params) {
        // 使用 selectList 而不是 selectOne
        List<Map<String, Object>> results = nxDepartmentOrdersDao.batchQueryGbDepStats(gbDepIds, params);

        // 转换结果
        return results.stream()
                .collect(Collectors.toMap(
                        map -> (Integer) map.get("depId"),
                        map -> {
                            Map<String, Integer> stats = new HashMap<>();
                            stats.put("count0", ((Number) map.get("count0")).intValue());
                            stats.put("count1", ((Number) map.get("count1")).intValue());
                            stats.put("count2", ((Number) map.get("count2")).intValue());
                            return stats;
                        }
                ));
    }

    @Override
    public Map<Integer, Integer> batchQueryFatherGoodsOrderCount(List<Integer> grandIds, Map<String, Object> params) {
        // 构建查询参数
        Map<String, Object> queryParams = new HashMap<>(params);
        queryParams.put("grandIds", grandIds);
        
        // 执行批量查询
        List<Map<String, Object>> results = nxDepartmentOrdersDao.batchQueryFatherGoodsOrderCount(queryParams);
        
        // 转换结果
        return results.stream()
                .collect(Collectors.toMap(
                        map -> {
                            Object grandIdObj = map.get("grandId");
                            if (grandIdObj instanceof Integer) {
                                return (Integer) grandIdObj;
                            } else if (grandIdObj instanceof Long) {
                                return ((Long) grandIdObj).intValue();
                            } else if (grandIdObj instanceof Number) {
                                return ((Number) grandIdObj).intValue();
                            }
                            return Integer.valueOf(grandIdObj.toString());
                        },
                        map -> ((Number) map.get("orderCount")).intValue()
                ));
    }
    
    @Override
    public Map<Integer, Map<String, Object>> batchQueryDepartmentOrderStats(List<Integer> depFatherIds) {
        List<Map<String, Object>> results = nxDepartmentOrdersDao.batchQueryDepartmentOrderStats(depFatherIds);
        
        return results.stream()
                .collect(Collectors.toMap(
                        map -> (Integer) map.get("depFatherId"),
                        map -> {
                            Map<String, Object> stats = new HashMap<>();
                            stats.put("unDo", ((Number) map.get("unDo")).intValue());
                            stats.put("hasPrice", ((Number) map.get("hasPrice")).intValue());
                            stats.put("hasWeight", ((Number) map.get("hasWeight")).intValue());
                            stats.put("totalCount", ((Number) map.get("totalCount")).intValue());
                            stats.put("finishCount", ((Number) map.get("finishCount")).intValue());
                            stats.put("totalSubtotal", map.get("totalSubtotal"));
                            return stats;
                        }
                ));
    }
    
    @Override
    public Map<String, Map<String, Object>> batchQueryGbDistributerDepartmentStats(List<Integer> gbDisIds, List<Integer> gbDepIds) {
        List<Map<String, Object>> results = nxDepartmentOrdersDao.batchQueryGbDistributerDepartmentStats(gbDisIds, gbDepIds);
        
        return results.stream()
                .collect(Collectors.toMap(
                        map -> map.get("gbDisId") + "_" + map.get("gbDepId"), // 使用复合键
                        map -> {
                            Map<String, Object> stats = new HashMap<>();
                            stats.put("newOrder", ((Number) map.get("newOrder")).intValue());
                            stats.put("jinhuoOrder", ((Number) map.get("jinhuoOrder")).intValue());
                            stats.put("chukuOrder", ((Number) map.get("chukuOrder")).intValue());
                            stats.put("hasNotWeight", ((Number) map.get("hasNotWeight")).intValue());
                            stats.put("jinhuoHasWeight", ((Number) map.get("jinhuoHasWeight")).intValue());
                            stats.put("chukuHasWeight", ((Number) map.get("chukuHasWeight")).intValue());
                            stats.put("hasPrice", ((Number) map.get("hasPrice")).intValue());
                            stats.put("hasNotPrice", ((Number) map.get("hasNotPrice")).intValue());
                            stats.put("twoTotal", ((Number) map.get("twoTotal")).intValue());
                            stats.put("total", map.get("total"));
                            return stats;
                        }
                ));
    }



    @Override
    public Map<String, Object> getOrderStats(Integer disId) {
        Map<String, Object> stats = new HashMap<>();

        // 1. 获取采购订单统计
        Map<String, Object> map3 = new HashMap<>();
        map3.put("disId", disId);
        map3.put("status", 3);
        map3.put("purStatus", 4);
        Integer preOrders = nxDepartmentOrdersDao.queryDepOrdersAcount(map3);
        Integer buyOrders = nxDepartmentOrdersDao.queryDepOrdersAcount(map3);

        Map<String, Object> map3Ok = new HashMap<>();
        map3Ok.put("disId", disId);
        map3Ok.put("equalStatus", 2);
        Integer buyOrdersOk = nxDepartmentOrdersDao.queryDepOrdersAcount(map3Ok);

        stats.put("buyOrders", buyOrders);
        stats.put("buyOrdersOk", buyOrdersOk);
        stats.put("preOrders", preOrders);

        // 2. 获取各类订单统计
        Map<String, Object> map111 = new HashMap<>();
        map111.put("disId", disId);
        map111.put("status", 3);

        // 出库订单
        map111.put("goodsType", -1);
        int stockCount = nxDepartmentOrdersDao.disGetPurchaseGoodsApplysCount(map111);

        // 订货订单
        map111.put("goodsType", 1);
        int wxCount = nxDepartmentOrdersDao.disGetPurchaseGoodsApplysCount(map111);
        map111.put("goodsType", 2);
        int wxCountAuto = nxDepartmentOrdersDao.disGetPurchaseGoodsApplysCount(map111);
        int wxCountPur = wxCount + wxCountAuto;

        // 自采订单
        map111.put("goodsType", 0);
        int zicaiCount = nxDepartmentOrdersDao.disGetPurchaseGoodsApplysCount(map111);

        // 3. 获取完成订单统计
        Map<String, Object> mapOk = new HashMap<>();
        mapOk.put("disId", disId);
        mapOk.put("status", 3);
        mapOk.put("goodsType", -1);
        mapOk.put("equalPurStatus", 4);

        // 出库完成
        int stockCountOK = nxDepartmentOrdersDao.disGetPurchaseGoodsApplysCount(mapOk);

        // 订货完成
        mapOk.put("goodsType", 1);
        int wxCountOk = nxDepartmentOrdersDao.disGetPurchaseGoodsApplysCount(mapOk);
        mapOk.put("goodsType", 2);
        int wxCountOkAuto = nxDepartmentOrdersDao.disGetPurchaseGoodsApplysCount(mapOk);
        int wxCountPurOk = wxCountOk + wxCountOkAuto;

        stats.put("stockCount", stockCount);
        stats.put("stockCountOk", stockCountOK);
        stats.put("wxCount", wxCountPur);
        stats.put("wxCountOk", wxCountPurOk);
        stats.put("zicaiCount", zicaiCount);

        return stats;
    }

    @Override
    public List<Integer> queryOnlyNxGoodsIds(Map<String, Object> map) {

        return  nxDepartmentOrdersDao.queryOnlyNxGoodsIds(map);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> disGetOutGoodsGrandCata(Map<String, Object> map) {

        return nxDepartmentOrdersDao.disGetOutGoodsGrandCata(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> disGetNxGoodsApply(Map<String, Object> map) {

        return nxDepartmentOrdersDao.disGetNxGoodsApply(map);
    }

    @Override
    public List<OutGoodsSimpleDTO> disGetNxGoodsApplyUltraSimple(Map<String, Object> map) {
        return nxDepartmentOrdersDao.disGetNxGoodsApplyUltraSimple(map);
    }

    @Override
    public Integer queryOrderGoodsCount(Map<String, Object> mapCount) {

        return nxDepartmentOrdersDao.queryOrderGoodsCount(mapCount);
    }


    @Override
    public List<GbDepartmentEntity> queryPureOrderGbDepartment(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryPureOrderGbDepartment(map);
    }

    @Override
    public double queryDisGoodsOrderWeightTotal(Map<String, Object> map1) {

        return nxDepartmentOrdersDao.queryDisGoodsOrderWeightTotal(map1);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> disGetOutStockGoodsApplyForStock(Map<String, Object> map) {

        return nxDepartmentOrdersDao.disGetOutStockGoodsApplyForStock(map);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryPrintDepOrder(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryPrintDepOrder(map);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryGrandGoodsOrder(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryGrandGoodsOrder(map);
    }

    @Override
    public List<NxDistributerFatherGoodsEntity> queryDisGetPrintOrderGreatGrandGoods(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryDisGetPrintOrderGreatGrandGoods(map);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryDepWeightOrderGb(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryDepWeightOrderGb(map);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryDepOrdersWithTraceReport(Map<String, Object> map) {
        return nxDepartmentOrdersDao.queryDepOrdersWithTraceReport(map);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryOrdersByBillIdWithTraceReport(Map<String, Object> map) {
        return nxDepartmentOrdersDao.queryOrdersByBillIdWithTraceReport(map);
    }


    @Override
    public List<NxDistributerFatherGoodsEntity> queryFatherGoodsByParams(Map<String, Object> map1222) {

        return nxDepartmentOrdersDao.queryFatherGoodsByParams(map1222);
    }

    @Override
    public double queryDepOrdersProfitScale(Map<String, Object> map1222) {

        return nxDepartmentOrdersDao.queryDepOrdersProfitScale(map1222);
    }

    @Override
    public double queryCostSubtotal(Map<String, Object> map1222) {

        return nxDepartmentOrdersDao.queryCostSubtotal(map1222);
    }

    @Override
    public List<NxDistributerPurchaseBatchEntity> queryDisPurchaseBatch(Map<String, Object> map2) {

        return nxDepartmentOrdersDao.queryDisPurchaseBatch(map2);
    }

    @Override
    public void deleteBatch(Integer[] nxOrdersSubIds) {
        nxDepartmentOrdersDao.deleteBatch(nxOrdersSubIds);
    }

    @Override
    public Integer queryDepOrdersAcountByDepGoods(Map<String, Object> mapDep) {

        return nxDepartmentOrdersDao.queryDepOrdersAcountByDepGoods(mapDep);
    }

    @Override
    public List<NxDistributerGoodsShelfEntity> queryShelfGoodsOrder(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryShelfGoodsOrder(map);
    }

    @Override
    public Integer queryReturnOrderCount(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryReturnOrderCount(map);
    }

    @Override
    public double queryReturnSubtotal(Map<String, Object> mapR) {

        return nxDepartmentOrdersDao.queryReturnSubtotal(mapR);
    }


    @Override
    @Transactional
    public void moveOrderToHistory(NxDepartmentOrdersEntity order) {

        Integer orderId = order.getNxDepartmentOrdersId();
        System.out.println("订单开始：orderId = " + orderId);
        System.out.println("订单开始：orderId = " + order.getNxDoBillId());
        System.out.println("订单开始：orderId = " + order.getNxDoCollaborativeNxDisId());

        // 1. 防止重复迁移（先检查历史表中是否已有此订单）
        NxDepartmentOrdersEntity existing = nxDepartmentOrdersDao.queryHistoryOrderId(orderId);
        if (existing != null) {
            System.out.println("订单已存在于新表中，跳过迁移：orderId = " + orderId);
            return;
        }

        // 2. 插入到历史表
        int insertCount = nxDepartmentOrdersDao.insertFromOrder(orderId);
        System.out.println("indcouentnttt" + insertCount);
        if (insertCount == 0) {
            throw new RuntimeException("插入历史表失败，orderId = " + orderId);
        }

        // 3. 删除原始表
        int deleteCount = nxDepartmentOrdersDao.delete(orderId);
        System.out.println("deleteCountdeleteCount" + deleteCount);
        if (deleteCount == 0) {
            throw new RuntimeException("删除原始订单失败，orderId = " + orderId);
        }

        System.out.println("✅ 订单迁移成功，orderId = " + orderId);
    }



    @Override
    public Map<String, Object> queryStockGoodsData(Map<String, Object> params) {

        // 创建一个新的Service方法,一次性获取所有需要的数据
        Map<String, Object> result = new HashMap<>();
        long t1 = System.currentTimeMillis();

        // 1. 获取货架商品数据
        params.put("shelfId", 1);
        System.out.println("shelfrrrrrrr" + params);
        List<NxDistributerGoodsShelfEntity> shelfEntities = queryShelfGoodsOrder(params);
        long t2 = System.currentTimeMillis();
        params.put("shelfId", 0);
        List<NxDistributerGoodsShelfEntity> shelfEntities2 = queryShelfGoodsOrder(params);
        long t3 = System.currentTimeMillis();

        // 处理空货架数据
        NxDistributerGoodsShelfEntity emptyShelf = processEmptyShelf(shelfEntities2);
        if (emptyShelf != null) {
            emptyShelf.setNxDistributerGoodsShelfName("无货架");
            shelfEntities.add(emptyShelf);
        }

        // 组装结果
        result.put("shelfArr", shelfEntities);

        return result;
    }

    private NxDistributerGoodsShelfEntity processEmptyShelf(List<NxDistributerGoodsShelfEntity> shelfEntities2) {
        if (shelfEntities2.isEmpty()) {
            return null;
        }

        NxDistributerGoodsShelfEntity emptyShelf = new NxDistributerGoodsShelfEntity();
        List<NxDistributerGoodsShelfGoodsEntity> emptyList = shelfEntities2.stream()
                .flatMap(shelf -> shelf.getNxDisGoodsShelfGoodsEntities().stream())
                .collect(Collectors.toList());
        emptyShelf.setNxDisGoodsShelfGoodsEntities(emptyList);
        return emptyShelf;
    }

    @Override
    public NxDepartmentOrdersEntity queryObject(Integer nxDepartmentOrdersId) {
        return nxDepartmentOrdersDao.queryObject(nxDepartmentOrdersId);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryList(Map<String, Object> map) {
        return nxDepartmentOrdersDao.queryList(map);
    }


    @Override
    public List<NxDepartmentOrdersEntity> queryOrdersForDisGoods(Map<String, Object> map1) {

        return nxDepartmentOrdersDao.queryOrdersForDisGoods(map1);
    }

    @Override
    public Map<String, Object> queryShelfListWithDepIds(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 获取货架列表（包含订单数量统计）
        // SQL 查询已经返回了订单数量，直接使用查询结果
        System.out.println("shellis" + params);
        List<ShelfListSimpleDTO> shelfListDTO = nxDepartmentOrdersDao.queryShelfListBasic(params);
        
        // 2. 检查是否有非货架商品订单，如果有则追加"非货架"选项
        Integer nonShelfOrdersCount = nxDepartmentOrdersDao.queryNonShelfOrdersCount(params);
        if (nonShelfOrdersCount != null && nonShelfOrdersCount > 0) {
            ShelfListSimpleDTO nonShelfDTO = new ShelfListSimpleDTO();
            nonShelfDTO.setNxDistributerGoodsShelfId(0); // 使用0表示非货架
            nonShelfDTO.setNxDistributerGoodsShelfName("非货架");
            nonShelfDTO.setNxDistributerGoodsShelfSort(9999); // 设置一个较大的排序值，确保在最后
            if (params.get("disId") != null) {
                nonShelfDTO.setNxDistributerGoodsShelfDisId((Integer) params.get("disId"));
            }
            nonShelfDTO.setNewOrderCount(nonShelfOrdersCount);
            shelfListDTO.add(nonShelfDTO);
        }
        
        // 3. 获取部门数据
        List<NxDepartmentEntity> departmentEntities = queryPureOrderNxDepartmentSimple(params);
        List<GbDepartmentEntity> gbDepartmentEntities = queryPureOrderGbDepartment(params);
        
        // 4. 获取统计数据
        Integer count = queryDepOrdersAcount(params);
        result.put("shelfList", shelfListDTO);
        result.put("stockCount", count);
        result.put("depCount", departmentEntities.size() + gbDepartmentEntities.size());

        return result;
    }

    @Override
    public NxDistributerGoodsShelfEntity queryShelfGoodsDetail(Map<String, Object> params) {
        // 查询指定货架的商品详情（使用简化版查询，只返回必要字段）
        // 设置 shelfId = 1 表示只查询有货架的商品
        // 添加 targetShelfId 参数用于过滤指定货架
        Integer targetShelfId = (Integer) params.get("shelfId");
        params.put("shelfId", 1);
        params.put("targetShelfId", targetShelfId);
        
        // 使用简化版查询，减少数据传输量
        List<NxDistributerGoodsShelfEntity> shelfList = nxDepartmentOrdersDao.queryShelfGoodsDetailSimple(params);
        
        if (shelfList != null && !shelfList.isEmpty()) {
            // 找到指定货架的数据
            for (NxDistributerGoodsShelfEntity shelf : shelfList) {
                if (shelf.getNxDistributerGoodsShelfId().equals(targetShelfId)) {
                    return shelf;
                }
            }
        }
        
        // 如果没有找到，返回一个空的货架对象
        NxDistributerGoodsShelfEntity emptyShelf = new NxDistributerGoodsShelfEntity();
        emptyShelf.setNxDistributerGoodsShelfId(targetShelfId);
        emptyShelf.setNxDisGoodsShelfGoodsEntities(new ArrayList<>());
        return emptyShelf;
    }

    @Override
    public ShelfDetailSimpleDTO queryShelfGoodsDetailUltraSimple(Map<String, Object> params) {
        // 查询指定货架的商品详情（使用超简化版查询，字段扁平化）
        // 添加 targetShelfId 参数用于过滤指定货架
        Integer targetShelfId = (Integer) params.get("shelfId");
        params.put("targetShelfId", targetShelfId);
        
        // 根据 targetShelfId 设置 shelfId 参数
        // shelfId = 0 表示查询非货架商品，shelfId = 1 表示查询有货架的商品
        if (targetShelfId != null && targetShelfId == 0) {
            // 查询非货架商品
            params.put("shelfId", 0);
        } else {
            // 查询有货架的商品
            params.put("shelfId", 1);
        }
        
        // 使用超简化版查询，字段扁平化，大幅减少数据传输量
        // 注意：虽然返回 List，但 MyBatis 的 resultMap 会自动将多条记录聚合成单个对象
        List<ShelfDetailSimpleDTO> shelfDetailList = nxDepartmentOrdersDao.queryShelfGoodsDetailUltraSimple(params);
        
        ShelfDetailSimpleDTO shelfDetail = null;
        if (shelfDetailList != null && !shelfDetailList.isEmpty()) {
            // 取第一个（MyBatis 的 resultMap 应该已经聚合了所有记录为一个对象）
            shelfDetail = shelfDetailList.get(0);
        }
        
        // 如果没有找到，返回一个空的货架对象
        if (shelfDetail == null) {
            shelfDetail = new ShelfDetailSimpleDTO();
            shelfDetail.setNxDistributerGoodsShelfId(targetShelfId);
            if (targetShelfId != null && targetShelfId == 0) {
                shelfDetail.setNxDistributerGoodsShelfName("非货架");
                shelfDetail.setNxDistributerGoodsShelfSort(9999);
                if (params.get("disId") != null) {
                    shelfDetail.setNxDistributerGoodsShelfDisId((Integer) params.get("disId"));
                }
            }
            shelfDetail.setGoodsList(new ArrayList<>());
        } else {
            // 如果查询的是非货架，确保货架信息正确设置
            if (targetShelfId != null && targetShelfId == 0) {
                shelfDetail.setNxDistributerGoodsShelfId(0);
                shelfDetail.setNxDistributerGoodsShelfName("非货架");
                shelfDetail.setNxDistributerGoodsShelfSort(9999);
                if (params.get("disId") != null) {
                    shelfDetail.setNxDistributerGoodsShelfDisId((Integer) params.get("disId"));
                }
            }
        }
        
        return shelfDetail;
    }

    @Override
    public Map<String, Object> queryShelfStatistics(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 获取部门数据
        List<NxDepartmentEntity> departmentEntities = queryPureOrderNxDepartmentSimple(params);
        List<GbDepartmentEntity> gbDepartmentEntities = queryPureOrderGbDepartment(params);
        
        // 2. 获取统计数据
        Integer count = queryDepOrdersAcount(params);
        Integer stockCount = disGetPurchaseGoodsApplysCount(params);
        params.put("purStatus", null);
        params.put("dayuPurStatus", 3);
        Integer stockCountOK = disGetPurchaseGoodsApplysCount(params);
        
        // 组装结果
        result.put("waitDepNx", departmentEntities);
        result.put("waitDepGb", gbDepartmentEntities);
        result.put("depOrdersWait", count);
        result.put("stockCount", stockCount);
        result.put("stockCountOk", stockCountOK);
        
        return result;
    }

    @Override
    public Map<String, Object> queryCategoryListWithDepIds(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 获取类别列表（曾祖父级别，包含订单数量统计）
        // SQL 查询已经返回了订单数量，直接使用查询结果
        List<CategoryListSimpleDTO> categoryListDTO = nxDepartmentOrdersDao.queryCategoryListBasic(params);
        
        // 2. 获取部门数据
        List<NxDepartmentEntity> departmentEntities = queryPureOrderNxDepartmentSimple(params);
        List<GbDepartmentEntity> gbDepartmentEntities = queryPureOrderGbDepartment(params);
        
        // 3. 获取统计数据
        Integer count = queryDepOrdersAcount(params);
        
        // 组装结果
        result.put("categoryList", categoryListDTO);
        result.put("stockCount", count);
        result.put("depCount", departmentEntities.size() + gbDepartmentEntities.size());

        return result;
    }

    @Override
    public CategoryDetailSimpleDTO queryCategoryGoodsDetailUltraSimple(Map<String, Object> params) {
        // 查询指定类别的商品详情（使用超简化版查询，字段扁平化）
        CategoryDetailSimpleDTO categoryDetail = nxDepartmentOrdersDao.queryCategoryGoodsDetailUltraSimple(params);
        
        // 如果没有找到，返回一个空的类别对象
        if (categoryDetail == null) {
            categoryDetail = new CategoryDetailSimpleDTO();
            Integer categoryId = (Integer) params.get("categoryId");
            categoryDetail.setNxDistributerFatherGoodsId(categoryId);
            categoryDetail.setGoodsList(new ArrayList<>());
        }
        
        return categoryDetail;
    }

    @Override
    public Map<String, Object> queryCategoryStatistics(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 获取部门数据
        List<NxDepartmentEntity> departmentEntities = queryPureOrderNxDepartmentSimple(params);
        List<GbDepartmentEntity> gbDepartmentEntities = queryPureOrderGbDepartment(params);
        
        // 2. 获取统计数据
        Integer count = queryDepOrdersAcount(params);
        Integer stockCount = disGetPurchaseGoodsApplysCount(params);
        params.put("purStatus", null);
        params.put("dayuPurStatus", 3);
        Integer stockCountOK = disGetPurchaseGoodsApplysCount(params);
        
        // 组装结果
        result.put("waitDepNx", departmentEntities);
        result.put("waitDepGb", gbDepartmentEntities);
        result.put("depOrdersWait", count);
        result.put("stockCount", stockCount);
        result.put("stockCountOk", stockCountOK);
        
        return result;
    }

    @Override
    public Map<Integer, Map<String, Object>> batchQueryDepartmentOrderStatsSunla(List<Integer> depFatherIds) {
        List<Map<String, Object>> results = nxDepartmentOrdersDao.batchQueryDepartmentOrderStatsSunla(depFatherIds);

        return results.stream()
                .collect(Collectors.toMap(
                        map -> (Integer) map.get("depFatherId"),
                        map -> {
                            Map<String, Object> stats = new HashMap<>();
                            stats.put("unDo", ((Number) map.get("unDo")).intValue());
                            stats.put("hasPrice", ((Number) map.get("hasPrice")).intValue());
                            stats.put("hasWeight", ((Number) map.get("hasWeight")).intValue());
                            stats.put("totalCount", ((Number) map.get("totalCount")).intValue());
                            stats.put("finishCount", ((Number) map.get("finishCount")).intValue());
                            stats.put("totalSubtotal", map.get("totalSubtotal"));
                            return stats;
                        }
                ));

    }

    @Override
    public List<NxDistributerPurchaseBatchEntity> queryDisPurchaseBatchDto(Map<String, Object> map2) {

        return nxDepartmentOrdersDao.queryDisPurchaseBatchDto(map2);
    }


    @Override
    public NxDepartmentOrdersEntity saveCollaborativeOrderWhenNeeded(NxDepartmentOrdersEntity mainOrder, NxDistributerGoodsEntity disGoodsEntity) {
        if (mainOrder.getNxDoDistributerId() == null || disGoodsEntity.getNxDgDistributerId() == null
                || disGoodsEntity.getNxDgDistributerId().equals(mainOrder.getNxDoDistributerId())) {
            return null;
        }
        NxDepartmentOrdersEntity xiezuoOrder = new NxDepartmentOrdersEntity();
        BeanUtils.copyProperties(mainOrder, xiezuoOrder);
        xiezuoOrder.setNxDoCollaborativeNxDisId(mainOrder.getNxDoDistributerId());
        xiezuoOrder.setNxDepartmentOrdersId(null);
        xiezuoOrder.setNxDoDistributerId(disGoodsEntity.getNxDgDistributerId());
        xiezuoOrder.setNxDoNxRestrauntOrderId(null);
        xiezuoOrder.setNxDoOcrTaskId(null);
        xiezuoOrder.setNxDoTrainingDataId(null);
        xiezuoOrder.setNxDoPrice(null);
        xiezuoOrder.setNxDoSubtotal(null);
        xiezuoOrder.setNxDoProfitSubtotal(null);
        xiezuoOrder.setNxDoProfitScale(null);
        xiezuoOrder.setNxDoStatus(0);
        xiezuoOrder.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
        NxDepartmentOrdersEntity xiezuoOrderSaved = saveXiezuoOrderWithGoods(xiezuoOrder, disGoodsEntity);
        processOrderPrice(xiezuoOrderSaved, disGoodsEntity);
        // 协作订单也查询协作配送商的部门商品表，有则用部门商品单价赋值并更新
        applyDepartmentGoodsPriceIfFound(xiezuoOrderSaved, disGoodsEntity);
        nxDepartmentOrdersDao.update(xiezuoOrderSaved);
        mainOrder.setNxDoNxRestrauntOrderId(xiezuoOrderSaved.getNxDepartmentOrdersId());
        logger.info("[saveCollaborativeOrderWhenNeeded] 协作伙伴商品，已保存协作订单 id={}，主订单将关联该 id", xiezuoOrderSaved.getNxDepartmentOrdersId());
        return xiezuoOrderSaved;
    }

    /**
     * 查询部门商品表，若存在则用部门商品单价、打印规格、depDisGoodsId 赋值到订单
     * 主订单和协作订单均可复用
     */
    private void applyDepartmentGoodsPriceIfFound(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", order.getNxDoDistributerId());
        map.put("depId", order.getNxDoDepartmentId());
        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
        map.put("standard", order.getNxDoStandard());
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if (departmentDisGoodsEntity != null) {
            boolean isUsingCartonPrice = disGoodsEntity.getNxDgCartonUnit() != null
                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                    && order.getNxDoStandard() != null
                    && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim());
            if (isUsingCartonPrice) {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
            } else if (order.getNxDoCostPriceLevel() == null || order.getNxDoCostPriceLevel().equals("1")) {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
            } else {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
            }
            order.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
            if (order.getNxDoWeight() != null && !order.getNxDoWeight().trim().isEmpty()
                    && order.getNxDoPrice() != null && !order.getNxDoPrice().trim().isEmpty()) {
                try {
                    BigDecimal weight = new BigDecimal(order.getNxDoWeight());
                    BigDecimal price = new BigDecimal(order.getNxDoPrice());
                    BigDecimal subtotal = weight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                    order.setNxDoSubtotal(subtotal.toString());
                } catch (NumberFormatException e) {
                    logger.warn("[applyDepartmentGoodsPriceIfFound] 计算subtotal失败: weight={}, price={}, error={}",
                            order.getNxDoWeight(), order.getNxDoPrice(), e.getMessage());
                }
            }
            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
        } else {
            map.put("standard", null);
            NxDepartmentDisGoodsEntity departmentDisGoodsEntityO = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
            if (departmentDisGoodsEntityO != null) {
                order.setNxDoDepDisGoodsId(departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
            }else{
                boolean isUsingCartonPrice = disGoodsEntity.getNxDgCartonUnit() != null
                        && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                        && order.getNxDoStandard() != null
                        && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim());
                if (isUsingCartonPrice) {
                    order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
                } else if (order.getNxDoCostPriceLevel() == null || order.getNxDoCostPriceLevel().equals("1")) {
                    order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
                } else {
                    order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
                }
            }
        }
    }

    @Override
    public NxDepartmentOrdersEntity saveOrderWithGoods(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {

        saveCollaborativeOrderWhenNeeded(order, disGoodsEntity);

        System.out.println("saveONeOrderereerereeqonenenneorere");
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
        // 保存订单的原始状态（如果已经是 -2 待修正，则保持，否则设置为 0）
        Integer originalStatus = order.getNxDoStatus();
        System.out.println("baihz9iiii");
        if (originalStatus == null || originalStatus != 0 || order.getNxDoQuantity().trim().isEmpty() || order.getNxDoStandard().trim().isEmpty()) {
            System.out.println("baihz9iiii");
            order.setNxDoStatus(-2);
        }
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoNxGoodsId(disGoodsEntity.getNxDgNxGoodsId());
        order.setNxDoNxGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        order.setNxDoApplyDate(formatWhatDay(0));
        order.setNxDoArriveOnlyDate(formatWhatDate(0));
        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
        order.setNxDoArriveDate(formatWhatDay(0));
        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        order.setNxDoApplyOnlyTime(formatWhatTime(0));
        order.setNxDoGbDistributerId(-1);
        order.setNxDoGbDepartmentFatherId(-1);
        order.setNxDoGbDepartmentId(-1);
        order.setNxDoNxCommunityId(-1);
        order.setNxDoNxCommRestrauntFatherId(-1);
        order.setNxDoNxCommRestrauntId(-1);
        order.setNxDoArriveWhatDay(getWeek(0));
        order.setNxDoCostPriceLevel("1");
        order.setNxDoPurchaseGoodsId(-1);
        order.setNxDoCollaborativeNxDisId(-1);

        //给主订单单价赋值
        if (order.getNxDoDistributerId() != null && disGoodsEntity.getNxDgDistributerId() != null
                && disGoodsEntity.getNxDgDistributerId().equals(order.getNxDoDistributerId())) {
            // 使用完善的价格处理逻辑（平台型/非平台型分支、货架库存查询、规格匹配等）
            order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
            processOrderPrice(order, disGoodsEntity);

        }else{
            order.setNxDoGoodsType(0);
        }

        // 查询部门商品表，有则用部门商品单价赋值
        applyDepartmentGoodsPriceIfFound(order, disGoodsEntity);

        if (order.getNxDoTodayOrder() == null) {
            Map<String, Object> mapss = new HashMap<>();
            mapss.put("depId", order.getNxDoDepartmentId());
            mapss.put("status", 3);
            mapss.put("todayOrder", 1);
            int orderOrder = nxDepartmentOrdersDao.queryDepOrdersAcount(mapss);
            int todayOrder = orderOrder + 1;
            order.setNxDoTodayOrder(todayOrder);
           }

        // 检查订单是否已经有ID，如果有则更新，否则保存
        if (order.getNxDepartmentOrdersId() != null) {
            nxDepartmentOrdersDao.update(order);
        } else {
            nxDepartmentOrdersDao.save(order);
        }

        //主订单采购
        if (order.getNxDoDistributerId() != null && disGoodsEntity.getNxDgDistributerId() != null
                && disGoodsEntity.getNxDgDistributerId().equals(order.getNxDoDistributerId())
                && disGoodsEntity.getNxDgPurchaseAuto() != -1) {

            savePurGoodsAuto(order, getNxPurchaseGoodsInputTypeOrder(), getNxPurchaseGoodsTypeForSelf());
        }

        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDistributerGoodsEntity(distributerGoodsEntity);
        // 确保返回的订单状态正确
        logger.info("[saveOrderWithGoods] 返回订单: 商品名称={}, 订单ID={}, 状态={}, 分销商商品ID={}",
                order.getNxDoGoodsName(), order.getNxDepartmentOrdersId(),
                order.getNxDoStatus(), order.getNxDoDisGoodsId());

        return order;
    }


    public void savePurGoodsAuto(NxDepartmentOrdersEntity ordersEntity, Integer inputType, Integer purchaseType) {

        System.out.println("savePurGoodsAutosavePurGoodsAutosavePurGoodsAuto======================" + ordersEntity.getNxDoGoodsName());
        NxDistributerPurchaseGoodsEntity resultPurGoods = new NxDistributerPurchaseGoodsEntity();

        //判断是否有已经分的
        Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
        NxDistributerGoodsEntity disGoods = nxDistributerGoodsService.queryObject(doDisGoodsId);
        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", doDisGoodsId);
        map.put("status", 1);
        map.put("standard", ordersEntity.getNxDoStandard());
        System.out.println("purgogogo" + map);

        // 使用 queryPurchaseGoodsByParams 查询列表，避免 selectOne() 返回多条记录时报错
        List<NxDistributerPurchaseGoodsEntity> purGoodsList = nxDistributerPurchaseGoodsService.queryPurchaseGoodsByParams(map);
        NxDistributerPurchaseGoodsEntity havePurGoods = null;
        if (purGoodsList != null && !purGoodsList.isEmpty()) {
            havePurGoods = purGoodsList.get(0);
        }

        if (havePurGoods != null) {
            resultPurGoods = havePurGoods;
            havePurGoods.setNxDpgOrdersAmount(resultPurGoods.getNxDpgOrdersAmount() + 1);

            // 处理数量为空或空字符串的情况，使用默认值0
            String orderQtyStr = ordersEntity.getNxDoQuantity();
            if (orderQtyStr == null || orderQtyStr.trim().isEmpty()) {
                orderQtyStr = "0";
            }
            String purQtyStr = resultPurGoods.getNxDpgQuantity();
            if (purQtyStr == null || purQtyStr.trim().isEmpty()) {
                purQtyStr = "0";
            }

            BigDecimal orderQuantity = new BigDecimal(orderQtyStr);
            BigDecimal purQuantity = new BigDecimal(purQtyStr);
            BigDecimal totaoWeight = orderQuantity.add(purQuantity).setScale(1,BigDecimal.ROUND_HALF_UP);
            resultPurGoods.setNxDpgQuantity(totaoWeight.toString());
            System.out.println("ordsss" + ordersEntity.getNxDoSubtotal());
            if (ordersEntity.getNxDoSubtotal() != null && !ordersEntity.getNxDoSubtotal().equals("0")
                    && !ordersEntity.getNxDoSubtotal().trim().isEmpty() && new BigDecimal(ordersEntity.getNxDoSubtotal()).compareTo(BigDecimal.ZERO) > 1) {
                resultPurGoods.setNxDpgBuyQuantity(totaoWeight.toString());
                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(havePurGoods.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                resultPurGoods.setNxDpgBuySubtotal(decimal2.toString());
            }
            nxDistributerPurchaseGoodsService.update(resultPurGoods);
            ordersEntity.setNxDoPurchaseGoodsId(resultPurGoods.getNxDistributerPurchaseGoodsId());

        } else {
            resultPurGoods.setNxDpgDisGoodsFatherId(disGoods.getNxDgDfgGoodsFatherId());
            resultPurGoods.setNxDpgDisGoodsGrandId(disGoods.getNxDgDfgGoodsGrandId());
            resultPurGoods.setNxDpgDistributerId(disGoods.getNxDgDistributerId());
            resultPurGoods.setNxDpgDisGoodsId(doDisGoodsId);
            resultPurGoods.setNxDpgInputType(inputType);
            resultPurGoods.setNxDpgPurchaseType(purchaseType);
            resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
            resultPurGoods.setNxDpgApplyDate(formatWhatYearDayTime(0));
            resultPurGoods.setNxDpgOrdersAmount(1);
            resultPurGoods.setNxDpgFinishAmount(0);

            if(disGoods.getNxDgBuyingPriceOne() != null){
                ordersEntity.setNxDoCostPrice(disGoods.getNxDgBuyingPriceOne());
                resultPurGoods.setNxDpgExpectPrice(disGoods.getNxDgBuyingPriceOne());
                resultPurGoods.setNxDpgBuyPrice(disGoods.getNxDgBuyingPriceOne());
            }else{
                resultPurGoods.setNxDpgExpectPrice("0");
                resultPurGoods.setNxDpgBuyPrice("0");
            }

            resultPurGoods.setNxDpgQuantity(ordersEntity.getNxDoQuantity());
            resultPurGoods.setNxDpgStandard(ordersEntity.getNxDoStandard());
            System.out.println("saveoroororiid" + ordersEntity.getNxDoCostPrice());
            if (ordersEntity.getNxDoWeight() != null && !ordersEntity.getNxDoWeight().trim().isEmpty()
                    && ordersEntity.getNxDoCostPrice() != null && !ordersEntity.getNxDoCostPrice().trim().isEmpty()
            ) {
                BigDecimal totaoWeight = new BigDecimal(ordersEntity.getNxDoQuantity());
                BigDecimal decimal2 = totaoWeight.multiply(new BigDecimal(ordersEntity.getNxDoCostPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                // 采购商品数据
                resultPurGoods.setNxDpgBuyQuantity(ordersEntity.getNxDoQuantity());
                resultPurGoods.setNxDpgBuySubtotal(decimal2.toString());
            }
            nxDistributerPurchaseGoodsService.save(resultPurGoods);
            ordersEntity.setNxDoPurchaseGoodsId(resultPurGoods.getNxDistributerPurchaseGoodsId());
        }
        System.out.println("updaidiididii");
        nxDepartmentOrdersDao.update(ordersEntity);
        NxDistributerPurchaseBatchEntity batchEntity = new NxDistributerPurchaseBatchEntity();

        //给autoBatch更新gbDepartmentOrderid
        System.out.println("suplieridid" + disGoods.getNxDgGoodsName() + disGoods.getNxDgSupplierId());
        NxJrdhSupplierEntity supplierEntity = jrdhSupplierService.queryObject(disGoods.getNxDgSupplierId());

        if (disGoods.getNxDgSupplierId() != null && supplierEntity.getNxJrdhsUserId() != null && supplierEntity.getNxJrdhsUserId() != -1) {
            //
            Map<String, Object> mapBatch = new HashMap<>();
            Integer gbDgGbSupplierId = disGoods.getNxDgSupplierId();
            mapBatch.put("supplierId", gbDgGbSupplierId);
            mapBatch.put("status", 1);
            mapBatch.put("purchaseType", 2);

            List<NxDistributerPurchaseBatchEntity> entities = nxDistributerPurchaseBatchService.queryDisPurchaseBatch(mapBatch);
            if (entities.size() == 0) {
                //
                batchEntity.setNxDpbDate(formatWhatDay(0));
                batchEntity.setNxDpbTime(formatWhatTime(0));
                batchEntity.setNxDpbMonth(formatWhatMonth(0));
                batchEntity.setNxDpbPruchaseWeek(getWeek(0));
                batchEntity.setNxDpbYear(formatWhatYear(0));
                batchEntity.setNxDpbPayFullTime(formatWhatYearDayTime(0));
                batchEntity.setNxDpbStatus(-1);
                batchEntity.setNxDpbPurchaseType(2);
                batchEntity.setNxDpbSupplierId(gbDgGbSupplierId);
                batchEntity.setNxDpbSellUserId(supplierEntity.getNxJrdhsUserId());
                batchEntity.setNxDpbDistributerId(ordersEntity.getNxDoDistributerId());
                batchEntity.setNxDpbPurUserId(supplierEntity.getNxJrdhsNxPurUserId());
                batchEntity.setNxDpbBuyUserId(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(supplierEntity.getNxJrdhsNxJrdhBuyUserId());
                batchEntity.setNxDpbBuyUserOpenId(nxJrdhUserEntity.getNxJrdhWxOpenId());
                batchEntity.setNxDpbOrderIsNotice(1);
                LocalDateTime now = LocalDateTime.now();
                int hour = now.getHour();
                if( hour < 12){
                    batchEntity.setNxDpbNeedDate(formatWhatDay(0));
                }else{
                    batchEntity.setNxDpbNeedDate(formatWhatDay(1));
                }
                nxDistributerPurchaseBatchService.save(batchEntity);

                resultPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                resultPurGoods.setNxDpgPurchaseDate(formatWhatDay(0));
                resultPurGoods.setNxDpgTime(formatWhatYearDayTime(0));
                resultPurGoods.setNxDpgDistributerId(batchEntity.getNxDpbDistributerId());
                resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
                nxDistributerPurchaseGoodsService.update(resultPurGoods);
                ordersEntity.setNxDoPurchaseStatus(1);

            } else {
                batchEntity = entities.get(0);
                resultPurGoods.setNxDpgStatus(getNxDisPurchaseGoodsWithBatch());
                resultPurGoods.setNxDpgBatchId(batchEntity.getNxDistributerPurchaseBatchId());
                nxDistributerPurchaseGoodsService.update(resultPurGoods);
                ordersEntity.setNxDoPurchaseStatus(1);
            }

            Map<String, TemplateData> mapNotice = new HashMap<>();
            mapNotice.put("thing1", new TemplateData(disGoods.getNxDgGoodsName()));
            mapNotice.put("time6", new TemplateData(formatWhatDay(0)));
            NxDepartmentEntity departmentEntity = nxDepartmentService.queryObject(ordersEntity.getNxDoDepartmentId());
            mapNotice.put("thing7", new TemplateData(departmentEntity.getNxDepartmentName()));
            mapNotice.put("thing8", new TemplateData(departmentEntity.getNxDepartmentAttrName()));
            StringBuilder pathBuilder = new StringBuilder("pages/txs/disOrderBatch/disOrderBatch");
            pathBuilder.append("?batchId=").append(batchEntity.getNxDistributerPurchaseBatchId());
            pathBuilder.append("&retName=").append(nxDistributerService.queryObject(batchEntity.getNxDpbDistributerId()).getNxDistributerName());
            pathBuilder.append("&disId=").append(batchEntity.getNxDpbDistributerId());
            pathBuilder.append("&buyUserId=").append(batchEntity.getNxDpbBuyUserId());
            pathBuilder.append("&purUserId=").append(batchEntity.getNxDpbPurUserId());
            pathBuilder.append("&fromBuyer=1");
            String path = pathBuilder.toString();
            System.out.println("paththhththt" + path);

            Integer nxJrdhsUserId = supplierEntity.getNxJrdhsUserId();
            NxJrdhUserEntity nxJrdhUserEntity = nxJrdhUserService.queryObject(nxJrdhsUserId);
            if (nxJrdhUserEntity != null) {
                System.out.println("suppsleir" + path);
                WeNoticeService.nxSupplierOrderSave(nxJrdhUserEntity.getNxJrdhWxOpenId(), path, mapNotice);
                ordersEntity.setNxDoPurchaseGoodsId(resultPurGoods.getNxDistributerPurchaseGoodsId());
            }

        }

        nxDepartmentOrdersDao.update(ordersEntity);

    }


    @Override
    public List<NxDepartmentOrdersEntity> queryListByOcrTaskId(Integer ocrTaskId) {
        return nxDepartmentOrdersDao.queryListByOcrTaskId(ocrTaskId);
    }

    @Override
    public List<NxDepartmentOrdersEntity> queryListByOcrTaskIdWithPage(Map<String, Object> map) {
        return nxDepartmentOrdersDao.queryListByOcrTaskIdWithPage(map);
    }

    @Override
    public Integer queryTotalByOcrTaskId(Integer ocrTaskId) {
        return nxDepartmentOrdersDao.queryTotalByOcrTaskId(ocrTaskId);
    }

    @Override
    public int[] queryCountByOcrTaskIdGroupByStatus(Integer ocrTaskId) {
        Map<String, Object> map = nxDepartmentOrdersDao.queryCountByOcrTaskIdGroupByStatus(ocrTaskId);
        if (map == null || map.isEmpty()) {
            return new int[]{0, 0};
        }
        int completed = 0;
        int pending = 0;
        Object completedObj = map.get("completedCount");
        if (completedObj == null) completedObj = map.get("completedcount");
        Object pendingObj = map.get("pendingCount");
        if (pendingObj == null) pendingObj = map.get("pendingcount");
        if (completedObj != null) {
            completed = ((Number) completedObj).intValue();
        }
        if (pendingObj != null) {
            pending = ((Number) pendingObj).intValue();
        }
        return new int[]{completed, pending};
    }

    @Override
    public NxDepartmentOrdersEntity depSaveLinshiGoodsForPasteOrder(NxDepartmentOrdersEntity ordersEntity) {
        String goodsName = ordersEntity.getNxDoGoodsName();
        Integer disId = ordersEntity.getNxDoDistributerId();

        NxDistributerGoodsEntity goods = new NxDistributerGoodsEntity();
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("nxGoodsId", -1);
        map.put("goodsLevel", 2);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDistributerFatherGoodsService.queryDisFathersGoodsByParams(map);
        NxDistributerFatherGoodsEntity fatherGoodsEntity = fatherGoodsEntities.get(0);
        goods.setNxDgDfgGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        goods.setNxDgDfgGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
        goods.setNxDgPurchaseAuto(1);
        goods.setNxDgDistributerId(disId);
        goods.setNxDgGoodsFile("logo.jpg");
        goods.setNxDgGoodsFileLarge("");
        goods.setNxDgGoodsName(goodsName);
        String pinyin = hanziToPinyin(goodsName);
        String headPinyin = getHeadStringByString(goodsName, false, null);
        goods.setNxDgGoodsPinyin(pinyin);
        goods.setNxDgGoodsPy(headPinyin);
        goods.setNxDgDistributerId(disId);
        goods.setNxDgBuyingPriceIsGrade(0);
        goods.setNxDgBuyingPrice("0.1");
        goods.setNxDgWillPrice("0.1");
        goods.setNxDgBuyingPriceOne("0.1");
        goods.setNxDgBuyingPriceOneUpdate(formatWhatDate(0));
        goods.setNxDgWillPriceOneStandard(goods.getNxDgGoodsStandardname());
        goods.setNxDgWillPriceOne("0.1");
        goods.setNxDgWillPriceOneAboutPrice("0.1");
        goods.setNxDgGoodsIsHidden(0);
        goods.setNxDgGoodsStandardname(ordersEntity.getNxDoStandard());
        goods.setNxDgBuyingPriceUpdate(formatWhatDay(0));
        System.out.println("savegoogog" + goods);
        goods.setNxDgOutTotalWeight("0");
        goods.setNxDgPullOff(0);
        goods.setNxDgGoodsStatus(0);
        nxDistributerGoodsService.save(goods);

        Integer gbDfgGoodsAmount = fatherGoodsEntity.getNxDfgGoodsAmount();
        fatherGoodsEntity.setNxDfgGoodsAmount(gbDfgGoodsAmount + 1);
        nxDistributerFatherGoodsService.update(fatherGoodsEntity);

        ordersEntity.setNxDoDisGoodsId(goods.getNxDistributerGoodsId());
        ordersEntity.setNxDoDisGoodsFatherId(fatherGoodsEntity.getNxDistributerFatherGoodsId());
        ordersEntity.setNxDoDisGoodsGrandId(fatherGoodsEntity.getNxDfgFathersFatherId());
        ordersEntity.setNxDoStatus(-1);

        ordersEntity.setNxDoArriveDate(formatWhatDate(0));
        ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
        ordersEntity.setNxDoApplyDate(formatWhatDay(0));
        ordersEntity.setNxDoArriveOnlyDate(formatWhatDate(0));
        ordersEntity.setNxDoArriveWeeksYear(getWeekOfYear(0));
        ordersEntity.setNxDoArriveDate(formatWhatDay(0));
        ordersEntity.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        ordersEntity.setNxDoApplyOnlyTime(formatWhatTime(0));
        ordersEntity.setNxDoGbDistributerId(-1);
        ordersEntity.setNxDoGbDepartmentFatherId(-1);
        ordersEntity.setNxDoGbDepartmentId(-1);
        ordersEntity.setNxDoNxCommunityId(-1);
        ordersEntity.setNxDoNxCommRestrauntFatherId(-1);
        ordersEntity.setNxDoNxCommRestrauntId(-1);
        ordersEntity.setNxDoCollaborativeNxDisId(-1);
        ordersEntity.setNxDoPurchaseGoodsId(-1);
        ordersEntity.setNxDoProfitSubtotal("0");
        ordersEntity.setNxDoProfitScale("0");
        ordersEntity.setNxDoCostPriceLevel("1");
        ordersEntity.setNxDoCostPrice("0.1");
        ordersEntity.setNxDoPrice("0.1");
        ordersEntity.setNxDoPurchaseGoodsId(-1);
        ordersEntity.setNxDoArriveWhatDay(getWeek(0));
        ordersEntity.setNxDoPrintStandard(goods.getNxDgGoodsStandardname());
        ordersEntity.setNxDoExpectPrice(goods.getNxDgWillPriceOne());
        ordersEntity.setNxDoCostPriceUpdate(formatWhatDay(0));
        ordersEntity.setNxDoDepDisGoodsId(-1);
        ordersEntity.setNxDoWeight(ordersEntity.getNxDoQuantity());
        ordersEntity.setNxDoGoodsType(-1);
        ordersEntity.setNxDoProfitSubtotal("0.0");
        ordersEntity.setNxDoProfitScale("0");
        BigDecimal bigDecimal = new BigDecimal(ordersEntity.getNxDoQuantity()).multiply(new BigDecimal(0.1)).setScale(1, BigDecimal.ROUND_HALF_UP);
        ordersEntity.setNxDoSubtotal(bigDecimal.toString());
        ordersEntity.setNxDoCostSubtotal(bigDecimal.toString());

        Map<String, Object> mapss = new HashMap<>();
        mapss.put("depId", ordersEntity.getNxDoDepartmentId());
        mapss.put("status", 3);
        int orderOrder = nxDepartmentOrdersDao.queryDepOrdersAcount(mapss);
        ordersEntity.setNxDoTodayOrder(orderOrder + 1);
        System.out.println("savellinshssigogogo" + ordersEntity.getNxDoTodayOrder());
        nxDepartmentOrdersDao.save(ordersEntity);

//         Integer inputType, Integer purchaseType
        savePurGoodsAuto(ordersEntity, getNxPurchaseGoodsInputTypeOrder(), getNxPurchaseGoodsTypeForSelf());
        return ordersEntity;

    }

    @Override
    public NxDepartmentOrdersEntity updateOneOrderForChoice(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {

        System.out.println("saveONeOrderereerereeqonenenneorere" + order.getNxDepartmentOrdersId());
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDoStatus(0);
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoNxGoodsId(disGoodsEntity.getNxDgNxGoodsId());
        order.setNxDoNxGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
        order.setNxDoApplyDate(formatWhatDay(0));
        order.setNxDoArriveOnlyDate(formatWhatDate(0));
        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
        order.setNxDoArriveDate(formatWhatDay(0));
        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        order.setNxDoApplyOnlyTime(formatWhatTime(0));
        order.setNxDoGbDistributerId(-1);
        order.setNxDoGbDepartmentFatherId(-1);
        order.setNxDoGbDepartmentId(-1);
        order.setNxDoNxCommunityId(-1);
        order.setNxDoNxCommRestrauntFatherId(-1);
        order.setNxDoNxCommRestrauntId(-1);
        order.setNxDoPurchaseGoodsId(-1);
        order.setNxDoCollaborativeNxDisId(-1);
        order.setNxDoArriveWhatDay(getWeek(0));
        order.setNxDoCostPriceLevel("1");

        //auto
        Integer nxDepartmentOrdersId = order.getNxDepartmentOrdersId();
        NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersDao.queryObject(nxDepartmentOrdersId);

        if (order.getNxDoDistributerId() == null || disGoodsEntity.getNxDgDistributerId() == null
                || disGoodsEntity.getNxDgDistributerId().equals(order.getNxDoDistributerId())) {

            // 使用完善的价格处理逻辑（平台型/非平台型分支、货架库存查询、规格匹配等）
            order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
            System.out.println("ordersEntity.getNxDoPurchaseGoodsId()" + ordersEntity.getNxDoPurchaseGoodsId());
            if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {

                if(ordersEntity.getNxDoPurchaseGoodsId() != null && ordersEntity.getNxDoPurchaseGoodsId() != -1){
                    NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDistributerPurchaseGoodsService.queryObject(ordersEntity.getNxDoPurchaseGoodsId());
                    System.out.println("updattepurrrr" + purchaseGoodsEntity.getNxDpgOrdersAmount());
                    if(purchaseGoodsEntity.getNxDpgOrdersAmount()== 1) {
                        nxDistributerPurchaseGoodsService.delete(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
                        savePurGoodsAuto(order,getNxPurchaseGoodsInputTypeOrder(), getNxPurchaseGoodsTypeForSelf());
                    }else{
                        BigDecimal bigDecimal = new BigDecimal(purchaseGoodsEntity.getNxDpgQuantity()).subtract(new BigDecimal(ordersEntity.getNxDoQuantity())).setScale(1, BigDecimal.ROUND_HALF_UP);
                        purchaseGoodsEntity.setNxDpgQuantity(bigDecimal.toString());
                        if(purchaseGoodsEntity.getNxDpgBuyQuantity() != null && purchaseGoodsEntity.getNxDpgBuyPrice() != null){
                            BigDecimal bigDecimal1 = bigDecimal.subtract(new BigDecimal(purchaseGoodsEntity.getNxDpgBuyPrice())).setScale(1, BigDecimal.ROUND_HALF_UP);
                            purchaseGoodsEntity.setNxDpgBuyQuantity(bigDecimal.toString());
                            purchaseGoodsEntity.setNxDpgBuySubtotal(bigDecimal1.toString());
                            nxDistributerPurchaseGoodsService.update(purchaseGoodsEntity);
                        }
                    }
                }else{
                    savePurGoodsAuto(order,getNxPurchaseGoodsInputTypeOrder(), getNxPurchaseGoodsTypeForSelf());
                }
            }

            processOrderPrice(order, disGoodsEntity);
        }else{

            order.setNxDoGoodsType(0);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("disId", order.getNxDoDistributerId());
        map.put("depId", order.getNxDoDepartmentId());
        map.put("disGoodsId", disGoodsEntity.getNxDistributerGoodsId());
        map.put("standard", order.getNxDoStandard());
        System.out.println("depmapapmmdmmdd" + map);
        NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
        if (departmentDisGoodsEntity != null) {
            System.out.println("prinstnan" + departmentDisGoodsEntity.getNxDdgOrderStandard());
            // 判断是否使用了大包装单价，如果是则设置打印规格为大包装单位（支持智能匹配：件=箱）
            boolean isUsingCartonPrice = disGoodsEntity.getNxDgCartonUnit() != null
                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                    && order.getNxDoStandard() != null
                    && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim());

            if (isUsingCartonPrice) {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
                System.out.println("使用大包装单价，打印规格设置为: " + disGoodsEntity.getNxDgCartonUnit());
            } else if (order.getNxDoCostPriceLevel() == null || order.getNxDoCostPriceLevel().equals("1")) {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
            } else {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
            }

            order.setNxDoPrice(departmentDisGoodsEntity.getNxDdgOrderPrice());
            //如果有重量和单价，则计算 subtotal
            if(order.getNxDoWeight() != null && !order.getNxDoWeight().trim().isEmpty()
                    && order.getNxDoPrice() != null && !order.getNxDoPrice().trim().isEmpty()){
                try {
                    BigDecimal weight = new BigDecimal(order.getNxDoWeight());
                    BigDecimal price = new BigDecimal(order.getNxDoPrice());
                    BigDecimal subtotal = weight.multiply(price).setScale(1, BigDecimal.ROUND_HALF_UP);
                    order.setNxDoSubtotal(subtotal.toString());
                } catch (NumberFormatException e) {

                }
            }
            order.setNxDoDepDisGoodsId(departmentDisGoodsEntity.getNxDepartmentDisGoodsId());
        }else{
            map.put("standard", null);
            System.out.println("depmapapapappapa" + map);
            NxDepartmentDisGoodsEntity departmentDisGoodsEntityO = nxDepartmentDisGoodsService.queryDepartmentGoodsOnly(map);
            if (departmentDisGoodsEntityO != null) {
                order.setNxDoDepDisGoodsId(departmentDisGoodsEntityO.getNxDepartmentDisGoodsId());
            }
        }

        // 保留原始商品名称：若请求中未传入则从数据库保留，避免 choiceGoodsForApply 时 nxDoGoodsOriginalName 被覆盖丢失
        if (order.getNxDoGoodsOriginalName() == null || order.getNxDoGoodsOriginalName().trim().isEmpty()) {
            String dbOriginalName = ordersEntity.getNxDoGoodsOriginalName();
            if (dbOriginalName != null && !dbOriginalName.trim().isEmpty()) {
                order.setNxDoGoodsOriginalName(dbOriginalName);
            }
        }

        nxDepartmentOrdersDao.update(order);

        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDistributerGoodsEntity(distributerGoodsEntity);

        // 确保返回的订单状态正确
        logger.info("[saveOneOrder] 返回订单: 商品名称={}, 订单ID={}, 状态={}, 分销商商品ID={}",
                order.getNxDoGoodsName(), order.getNxDepartmentOrdersId(),
                order.getNxDoStatus(), order.getNxDoDisGoodsId());

        // 找到商品后，设置为0（已保存），但如果订单状态是 -2（待修正），则保持 -2
        if (order.getNxDoStatus() == null || (order.getNxDoStatus() != 0 && order.getNxDoStatus() != -2)) {
            order.setNxDoStatus(0);
        }

        return order;
    }

    @Override
    public NxDepartmentOrdersEntity saveXiezuoOrderWithGoods(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
        System.out.println("saveONeOrderereerereeqonenenneorerexiezuzozuzozuzo" +  order.getNxDoNxCommunityId());
        order.setNxDoDisGoodsFatherId(disGoodsEntity.getNxDgDfgGoodsFatherId());
        order.setNxDoDisGoodsGrandId(disGoodsEntity.getNxDgDfgGoodsGrandId());
        order.setNxDoDisGoodsId(disGoodsEntity.getNxDistributerGoodsId());
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoGoodsType(disGoodsEntity.getNxDgPurchaseAuto());
        order.setNxDoNxGoodsId(disGoodsEntity.getNxDgNxGoodsId());
        order.setNxDoNxGoodsFatherId(disGoodsEntity.getNxDgNxFatherId());
        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        order.setNxDoApplyDate(formatWhatDay(0));
        order.setNxDoArriveOnlyDate(formatWhatDate(0));
        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
        order.setNxDoArriveDate(formatWhatDay(0));
        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        order.setNxDoApplyOnlyTime(formatWhatTime(0));
        order.setNxDoGbDistributerId(-1);
        order.setNxDoGbDepartmentFatherId(-1);
        order.setNxDoGbDepartmentId(-1);
        order.setNxDoArriveWhatDay(getWeek(0));
        order.setNxDoCostPriceLevel("1");
        order.setNxDoPurchaseGoodsId(-1);
        // 协作订单的 nxDoCollaborativeNxDisId 由调用方设置为主配送商 id，不在此覆盖
        nxDepartmentOrdersDao.save(order);

        //auto
        if (disGoodsEntity.getNxDgPurchaseAuto() != -1) {
            savePurGoodsAuto(order, getNxPurchaseGoodsInputTypeOrder(), getNxPurchaseGoodsTypeForSelf());
        }

        return  order;
    }

    @Override
    public List<NxDistributerEntity> queryOfferOrderNxDistributer(Map<String, Object> mapOffer) {
        return nxDepartmentOrdersDao.queryOfferOrderNxDistributer(mapOffer);
    }

    @Override
    public List<NxDepartmentEntity> queryCollDisDeps(Map<String, Object> map) {

      return   nxDepartmentOrdersDao.queryCollDisDeps(map);
    }

    @Override
    public NxDepartmentOrdersEntity querycollOrder(Map<String, Object> map) {

        return nxDepartmentOrdersDao.querycollOrder(map);
    }

    @Override
    public List<NxDistributerEntity> queryOfferOrderNxDistributerWithOrder(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryOfferOrderNxDistributerWithOrder(map);

    }

    @Override
    public Integer queryCollReplyPartnerCount(Map<String, Object> map) {
        return nxDepartmentOrdersDao.queryCollReplyPartnerCount(map);
    }

    @Override
    public List<NxDistributerGoodsEntity> queryOfferOrdersGoods(Map<String, Object> map) {

        return nxDepartmentOrdersDao.queryOfferOrdersGoods(map);
    }

    @Override
    public NxDepartmentOrdersEntity queryByRestrauntId(Integer nxDoNxRestrauntOrderId) {

        return  nxDepartmentOrdersDao.queryByRestrauntId(nxDoNxRestrauntOrderId);
    }


    public int queryMaxTodayOrder(Integer depIdForTodayOrder) {

        return nxDepartmentOrdersDao.queryMaxTodayOrder(depIdForTodayOrder);
    }


    /**
     * 处理订单价格逻辑（平台型/非平台型分支、货架库存查询、规格匹配等）
     * 从 saveOrderWithGoods 方法中提取的价格处理逻辑，供多个方法复用
     *
     * @param order 订单实体
     * @param disGoodsEntity 分销商商品实体
     */
    public void processOrderPrice(NxDepartmentOrdersEntity order, NxDistributerGoodsEntity disGoodsEntity) {
        Integer nxDoDistributerId = order.getNxDoDistributerId();
        NxDistributerEntity distributerEntity = nxDistributerService.queryObject(nxDoDistributerId);
        //平台型
        if (distributerEntity.getNxDistributerBusinessTypeId() > 6) {
            if (disGoodsEntity.getNxDgWillPriceTwo() != null
                    && !disGoodsEntity.getNxDgWillPriceTwo().trim().isEmpty()
                    && order.getNxDoStandard().equals(disGoodsEntity.getNxDgWillPriceTwoStandard())) {
                System.out.println("levlellelelelel111222222222222222");
                BigDecimal doQuantity = new BigDecimal(order.getNxDoQuantity());
                BigDecimal cosPrice = new BigDecimal(disGoodsEntity.getNxDgBuyingPriceTwo());
                BigDecimal willPrice = new BigDecimal(disGoodsEntity.getNxDgWillPriceTwo());
                BigDecimal costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                BigDecimal scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

                // 判断是否使用大包装单价，如果是则设置打印规格为大包装单位
                if (disGoodsEntity.getNxDgCartonUnit() != null
                        && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()

                        && order.getNxDoStandard() != null
                        && order.getNxDoStandard().trim().equals(disGoodsEntity.getNxDgCartonUnit().trim())) {
                    order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
                    System.out.println("使用大包装单价，打印规格设置为: " + disGoodsEntity.getNxDgCartonUnit());
                } else {
                    order.setNxDoPrintStandard(disGoodsEntity.getNxDgWillPriceTwoStandard());
                }
                order.setNxDoPrice(disGoodsEntity.getNxDgWillPriceTwo());
                order.setNxDoExpectPrice(disGoodsEntity.getNxDgWillPriceTwo());
                order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceTwo());
                order.setNxDoSubtotal(subtotal.toString());
                order.setNxDoCostSubtotal(costSubtotal.toString());
                order.setNxDoCostPriceLevel("2");
                order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceTwoUpdate());
                order.setNxDoProfitSubtotal(profit.toString());
                order.setNxDoProfitScale(scaleB.toString());

            } else {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
                order.setNxDoCostPriceLevel("1");
                order.setNxDoSubtotal("0");
                order.setNxDoCostSubtotal("0");
                System.out.println("jimimhuaaa" + disGoodsEntity.getNxDgWillPriceOne());
                if (disGoodsEntity.getNxDgWillPriceOne() != null && !disGoodsEntity.getNxDgWillPriceOne().trim().isEmpty()) {
                    System.out.println("jieghuasu?????????" + order.getNxDoPrice());
                    order.setNxDoPrice(disGoodsEntity.getNxDgWillPriceOne());
                    order.setNxDoExpectPrice(disGoodsEntity.getNxDgWillPriceOne());
                }
                if (disGoodsEntity.getNxDgBuyingPriceOne() != null && !disGoodsEntity.getNxDgBuyingPriceOne().trim().isEmpty()) {
                    order.setNxDoCostPriceUpdate(disGoodsEntity.getNxDgBuyingPriceOneUpdate());
                    order.setNxDoCostPrice(disGoodsEntity.getNxDgBuyingPriceOne());
                }
            }
        } else {
            // 初始设置打印规格：如果订单规格等于大包装单位，则设置为大包装单位
            // 支持智能匹配：件=箱，盒=箱等同义词
            System.out.println("======== 设置打印规格(printStandard)开始 ========");
            System.out.println("订单ID: " + order.getNxDepartmentOrdersId());
            System.out.println("订单规格(nxDoStandard): " + order.getNxDoStandard());
            System.out.println("商品大包装单位(nxDgCartonUnit): " + disGoodsEntity.getNxDgCartonUnit());
            System.out.println("商品标准规格(nxDgGoodsStandardname): " + disGoodsEntity.getNxDgGoodsStandardname());
            System.out.println("设置前 printStandard: " + order.getNxDoPrintStandard());

            if (disGoodsEntity.getNxDgCartonUnit() != null
                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                    && order.getNxDoStandard() != null
                    && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim())) {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
                System.out.println("✅ [printStandard] 已设置为大包装单位: " + disGoodsEntity.getNxDgCartonUnit());
            } else {
                order.setNxDoPrintStandard(disGoodsEntity.getNxDgGoodsStandardname());
                System.out.println("✅ [printStandard] 已设置为商品标准规格: " + disGoodsEntity.getNxDgGoodsStandardname());
            }
            System.out.println("设置后 printStandard: " + order.getNxDoPrintStandard());
            System.out.println("======== 设置打印规格(printStandard)结束 ========");
            System.out.println("======== 从货架获取价格开始 ========");
            System.out.println("订单ID: " + order.getNxDepartmentOrdersId());
            System.out.println("商品ID: " + disGoodsEntity.getNxDistributerGoodsId());
            System.out.println("商品名称: " + disGoodsEntity.getNxDgGoodsName());
            System.out.println("商品名称12: " + disGoodsEntity.getNxDgCartonUnit());
            System.out.println("商品名称3333: " + disGoodsEntity.getNxDgCartonUnit());
            System.out.println("订单规格: " + order.getNxDoStandard());
            System.out.println("订单规格ppppp: " + order.getNxDoPrintStandard());
            System.out.println("商品规格: " + disGoodsEntity.getNxDgGoodsStandardname());

            Integer nxDistributerGoodsId = disGoodsEntity.getNxDistributerGoodsId();
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", nxDistributerGoodsId);
            map.put("restWeight", 0);

            System.out.println("查询货架库存参数: " + map);
            List<NxDistributerGoodsShelfStockEntity> stockEntities = nxDisGoodsShelfStockService.queryShelfStockListByParams(map);
            System.out.println("查询到库存批次数量: " + stockEntities.size());

            if (stockEntities.size() > 0) {
                NxDistributerGoodsShelfStockEntity nxDistributerGoodsShelfStockEntity = stockEntities.get(stockEntities.size() - 1);
                System.out.println("--- 最后一个库存批次信息 ---");
                System.out.println("库存批次ID: " + nxDistributerGoodsShelfStockEntity.getNxDistributerGoodsShelfStockId());
                System.out.println("成本价(nxDgssPrice): " + nxDistributerGoodsShelfStockEntity.getNxDgssPrice());
                System.out.println("外包装成本价(nxDgssPriceCarton): " + nxDistributerGoodsShelfStockEntity.getNxDgssPriceCarton());
                System.out.println("销售价(nxDgssSellingPrice): " + nxDistributerGoodsShelfStockEntity.getNxDgssSellingPrice());
                System.out.println("外包装销售价(nxDgssSellingPriceCarton): " + nxDistributerGoodsShelfStockEntity.getNxDgssSellingPriceCarton());
                System.out.println("剩余重量: " + nxDistributerGoodsShelfStockEntity.getNxDgssRestWeight());
                System.out.println("订单规格: " + order.getNxDoStandard());
                System.out.println("商品外包装单位: " + disGoodsEntity.getNxDgCartonUnit());

                // 判断订单规格是否与外包装单位匹配
                boolean useCartonPrice = false;
                if (disGoodsEntity.getNxDgCartonUnit() != null
                        && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                        && disGoodsEntity.getNxDgItemsPerCarton() != null
                        && disGoodsEntity.getNxDgItemsPerCarton() > 0
                        && order.getNxDoStandard() != null
                        && order.getNxDoStandard().trim().equals(disGoodsEntity.getNxDgCartonUnit().trim())) {
                    useCartonPrice = true;
                    System.out.println("✅ 订单规格与外包装单位匹配，将使用外包装单价");
                }

                // 根据匹配情况选择对应的单价
                String sellingPrice = null;
                String costPrice = null;

                if (useCartonPrice) {
                    // 使用外包装单价
                    if (nxDistributerGoodsShelfStockEntity.getNxDgssSellingPriceCarton() != null
                            && !nxDistributerGoodsShelfStockEntity.getNxDgssSellingPriceCarton().trim().isEmpty()) {
                        sellingPrice = nxDistributerGoodsShelfStockEntity.getNxDgssSellingPriceCarton();
                        System.out.println("使用外包装建议零售价: " + sellingPrice);
                    }
                    if (nxDistributerGoodsShelfStockEntity.getNxDgssPriceCarton() != null
                            && !nxDistributerGoodsShelfStockEntity.getNxDgssPriceCarton().trim().isEmpty()) {
                        costPrice = nxDistributerGoodsShelfStockEntity.getNxDgssPriceCarton();
                        System.out.println("使用外包装采购单价: " + costPrice);
                    }
                } else {
                    // 使用最小单位单价
                    if (nxDistributerGoodsShelfStockEntity.getNxDgssSellingPrice() != null
                            && !nxDistributerGoodsShelfStockEntity.getNxDgssSellingPrice().trim().isEmpty()) {
                        sellingPrice = nxDistributerGoodsShelfStockEntity.getNxDgssSellingPrice();
                        System.out.println("使用最小单位建议零售价: " + sellingPrice);
                    }
                    if (nxDistributerGoodsShelfStockEntity.getNxDgssPrice() != null
                            && !nxDistributerGoodsShelfStockEntity.getNxDgssPrice().trim().isEmpty()) {
                        costPrice = nxDistributerGoodsShelfStockEntity.getNxDgssPrice();
                        System.out.println("使用最小单位采购单价: " + costPrice);
                    }
                }

                // 设置订单价格
                if (sellingPrice != null) {
                    System.out.println("✅ 找到销售价，开始设置订单价格");
                    order.setNxDoPrice(sellingPrice);
                    if (costPrice != null) {
                        order.setNxDoCostPrice(costPrice);
                    }
                    // 如果使用了大包装单价，设置打印规格为大包装单位
                    if (useCartonPrice && disGoodsEntity.getNxDgCartonUnit() != null
                            && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                           ) {
                        order.setNxDoPrintStandard(disGoodsEntity.getNxDgCartonUnit());
                        System.out.println("使用大包装单价，打印规格设置为: " + disGoodsEntity.getNxDgCartonUnit());
                    }
                    System.out.println("订单销售价已设置为: " + order.getNxDoPrice());
                    System.out.println("订单成本价已设置为: " + order.getNxDoCostPrice());
                } else {

                    System.out.println("⚠️ 销售价为null，不设置价格");
                }
            }

            System.out.println("rodstntn" + order.getNxDoStandard() + "disganttt" + disGoodsEntity.getNxDgGoodsStandardname());
            // 检查订单规格是否等于商品标准规格，或者订单规格是否匹配大包装单位（智能匹配：件=箱）
            boolean isStandardMatch = order.getNxDoStandard() != null
                    && order.getNxDoStandard().equals(disGoodsEntity.getNxDgGoodsStandardname());
            boolean isCartonMatch = disGoodsEntity.getNxDgCartonUnit() != null
                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                    && order.getNxDoStandard() != null
                    && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim());

            if (isStandardMatch || isCartonMatch) {
                System.out.println("订单规格匹配: isStandardMatch=" + isStandardMatch + ", isCartonMatch=" + isCartonMatch);
                order.setNxDoWeight(order.getNxDoQuantity());

                // 检查价格是否已设置
                if (order.getNxDoPrice() != null && !order.getNxDoPrice().trim().isEmpty() && !order.getNxDoQuantity().trim().isEmpty() ) {

                    BigDecimal doQuantity = new BigDecimal(order.getNxDoQuantity());
                    BigDecimal willPrice = new BigDecimal(order.getNxDoPrice());

                    // 判断是否使用外包装单价计算（支持智能匹配：件=箱）
                    boolean useCartonPriceForCalc = false;
                    Integer itemsPerCartonForCalc = null;
                    if (disGoodsEntity.getNxDgCartonUnit() != null
                            && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                            && disGoodsEntity.getNxDgItemsPerCarton() != null
                            && disGoodsEntity.getNxDgItemsPerCarton() > 0
                            && order.getNxDoStandard() != null
                            && isStandardMatch(order.getNxDoStandard().trim(), disGoodsEntity.getNxDgCartonUnit().trim())
                            && disGoodsEntity.getNxDgItemsPerCarton() != null
                            && disGoodsEntity.getNxDgItemsPerCarton() > 0) {
                        useCartonPriceForCalc = true;
                        itemsPerCartonForCalc = disGoodsEntity.getNxDgItemsPerCarton();
                        System.out.println("订单规格匹配外包装单位（智能匹配），计算时将数量转换为箱数: " + doQuantity + "个 ÷ " + itemsPerCartonForCalc + " = " + doQuantity.divide(new BigDecimal(itemsPerCartonForCalc), 4, BigDecimal.ROUND_HALF_UP) + "箱");
                    }

                    BigDecimal subtotal;
                    BigDecimal costSubtotal = null;
                    if (useCartonPriceForCalc && itemsPerCartonForCalc != null) {
                        // 使用外包装单价：需要将数量转换为箱数
                        BigDecimal cartonCount = doQuantity.divide(new BigDecimal(itemsPerCartonForCalc), 4, BigDecimal.ROUND_HALF_UP);
                        subtotal = cartonCount.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        System.out.println("使用外包装单价计算: " + cartonCount + "箱 × " + willPrice + "元/箱 = " + subtotal + "元");

                        if (order.getNxDoCostPrice() != null && !order.getNxDoCostPrice().trim().isEmpty()) {
                            BigDecimal cosPrice = new BigDecimal(order.getNxDoCostPrice());
                            costSubtotal = cartonCount.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                            System.out.println("使用外包装成本价计算: " + cartonCount + "箱 × " + cosPrice + "元/箱 = " + costSubtotal + "元");
                        }
                    } else {
                        // 使用最小单位单价：直接相乘
                        subtotal = doQuantity.multiply(willPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        System.out.println("使用最小单位单价计算: " + doQuantity + " × " + willPrice + " = " + subtotal);

                        if (order.getNxDoCostPrice() != null && !order.getNxDoCostPrice().trim().isEmpty()) {
                            BigDecimal cosPrice = new BigDecimal(order.getNxDoCostPrice());
                            costSubtotal = doQuantity.multiply(cosPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        }
                    }

                    order.setNxDoSubtotal(subtotal.toString());

                    if (costSubtotal != null) {
                        BigDecimal profit = subtotal.subtract(costSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal scaleB = BigDecimal.ZERO;
                        if (subtotal.compareTo(BigDecimal.ZERO) != 0) {
                            scaleB = profit.divide(subtotal, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                        }
                        System.out.println("成本小计: " + costSubtotal);
                        System.out.println("销售小计: " + subtotal);
                        System.out.println("利润: " + profit);
                        System.out.println("利润率: " + scaleB + "%");
                        order.setNxDoCostSubtotal(costSubtotal.toString());
                        order.setNxDoProfitSubtotal(profit.toString());
                        order.setNxDoProfitScale(scaleB.toString());
                    }
                    System.out.println("✅ 订单金额计算完成");
                }
            }
        }
        nxDepartmentOrdersDao.update(order);
    }


    /**
     * 智能匹配单位规格
     * 支持同义词匹配，例如：件=箱，盒=箱等
     * @param orderStandard 订单规格（客户输入的规格，如"件"）
     * @param cartonUnit 大包装单位（商品的外包装单位，如"箱"）
     * @return 是否匹配
     */
    private boolean isStandardMatch(String orderStandard, String cartonUnit) {
        System.out.println("======== 智能匹配单位规格开始 ========");
        System.out.println("订单规格: [" + orderStandard + "]");
        System.out.println("包装单位: [" + cartonUnit + "]");

        if (orderStandard == null || cartonUnit == null) {
            System.out.println("❌ 匹配失败: 订单规格或包装单位为null");
            return false;
        }

        // 去除空格并转为小写进行比较
        String orderStd = orderStandard.trim();
        String carton = cartonUnit.trim();

        System.out.println("去除空格后 - 订单规格: [" + orderStd + "], 包装单位: [" + carton + "]");

        // 1. 完全匹配
        if (orderStd.equals(carton)) {
            System.out.println("✅ 完全匹配成功: [" + orderStd + "] == [" + carton + "]");
            return true;
        }

        // 2. 同义词映射：定义常见的单位同义词
        // 件 = 箱（订货"件"视为大包装"箱"）
        Map<String, Set<String>> synonymMap = new HashMap<>();
        synonymMap.put("箱", new HashSet<>(Arrays.asList("件")));
        synonymMap.put("件", new HashSet<>(Arrays.asList("箱")));

        System.out.println("开始检查同义词匹配...");

        // 3. 检查同义词匹配
        // 如果订单规格的同义词组包含包装单位，或者包装单位的同义词组包含订单规格，则认为匹配
        Set<String> orderSynonyms = synonymMap.get(orderStd);
        Set<String> cartonSynonyms = synonymMap.get(carton);

        if (orderSynonyms != null && orderSynonyms.contains(carton)) {
            System.out.println("✅ 智能匹配成功: 订单规格[" + orderStd + "] 的同义词组包含包装单位[" + carton + "]");
            System.out.println("订单规格[" + orderStd + "] 的同义词组: " + orderSynonyms);
            return true;
        }

        if (cartonSynonyms != null && cartonSynonyms.contains(orderStd)) {
            System.out.println("✅ 智能匹配成功: 包装单位[" + carton + "] 的同义词组包含订单规格[" + orderStd + "]");
            System.out.println("包装单位[" + carton + "] 的同义词组: " + cartonSynonyms);
            return true;
        }

        System.out.println("❌ 匹配失败: 订单规格[" + orderStd + "] 与 包装单位[" + carton + "] 不匹配");
        System.out.println("======== 智能匹配单位规格结束 ========");
        return false;
    }

    public R pasteSearchGoods(@RequestBody List<NxDepartmentOrdersEntity> orderList) {
        logger.info("[pasteSearchGoodsService] ========== 开始处理订单列表 ==========");
        logger.info("[pasteSearchGoodsService] 订单列表大小: {}", orderList != null ? orderList.size() : 0);
        
        List<NxDepartmentOrdersEntity> returnList = new ArrayList<>();
        // 保存原始订单JSON字符串的列表，索引与orderList对应
        List<String> originalOrderJsonList = new ArrayList<>();
        // 保存原始商品名称的列表，索引与orderList对应（客户录入的原始内容，不可修改）
        List<String> originalGoodsNameList = new ArrayList<>();
        // 保存returnList中每个订单对应的原始订单索引
        List<Integer> returnListOrderIndexList = new ArrayList<>();

        int orderIndex = 0;
        for (NxDepartmentOrdersEntity ordersEntity : orderList) {
            logger.info("[pasteSearchGoodsService] 开始处理订单[{}]: 商品名称={}", orderIndex, ordersEntity.getNxDoGoodsName());
            // 保存原始订单的JSON字符串（在处理前保存）
            String originalOrderJson = JSONObject.toJSONString(ordersEntity);
            originalOrderJsonList.add(originalOrderJson);
            // 保存原始商品名称（客户录入的原始内容，在处理前保存，不可修改）
            String originalGoodsName = ordersEntity.getNxDoGoodsName();
            originalGoodsNameList.add(originalGoodsName);

            if (ordersEntity.getNxDoRemark() != null && ordersEntity.getNxDoRemark().equals("-1")) {
                ordersEntity.setNxDoRemark(null);
            }

            String goodsName = ordersEntity.getNxDoGoodsName();

            Map<String, Object> mapZero = new HashMap<>();
            mapZero.put("disId", ordersEntity.getNxDoDistributerId());
            mapZero.put("searchStr", goodsName);
            mapZero.put("standard", ordersEntity.getNxDoStandard());
            mapZero.put("depId", ordersEntity.getNxDoDepartmentId());

            // 订货商品查询协作商户和自己的商品，构建多个配送商 id 列表
            Map<String, Object> mapGroup = new HashMap<>();
            mapGroup.put("orderDisId", ordersEntity.getNxDoDistributerId());
            List<Integer> ids = new ArrayList<>();
            ids.add(ordersEntity.getNxDoDistributerId());
            List<NxDistributerEntity> nxDistributerEntities = nxDistributerNxDistributerService.queryOfferNxDisByParams(mapGroup);
            if(nxDistributerEntities.size() > 0){
                Set<Integer> blockedIds = new HashSet<>(nxDistributerBlockService.queryBlockedDisIdsByBlocker(ordersEntity.getNxDoDistributerId()));
                for(NxDistributerEntity nxDistributerEntity: nxDistributerEntities){
                    Integer partnerId = nxDistributerEntity.getNxDistributerId();
                    if (!blockedIds.contains(partnerId)) {
                        ids.add(partnerId);
                    }
                }
            }
            mapZero.put("disIds", ids);

            List<NxDistributerGoodsEntity> distributerGoodsEntitiesZero = nxDistributerGoodsService.queryDisGoodsByName(mapZero);
            // 一级 没有修改商品
            if (distributerGoodsEntitiesZero.size() == 0) {
                Map<String, Object> mapOne = new HashMap<>();
                mapOne.put("disId", ordersEntity.getNxDoDistributerId());
                mapOne.put("searchStr", goodsName);
                mapOne.put("depId", ordersEntity.getNxDoDepartmentId());
                mapOne.put("disIds", ids);
                List<NxDistributerGoodsEntity> distributerGoodsEntitiesOne = nxDistributerGoodsService.queryDisGoodsByName(mapOne);
                logger.info("[pasteSearchGoodsService] 二级匹配-仅商品名称查询结果数量: {}", distributerGoodsEntitiesOne.size());
                // 二级 只有商品名称相同
                if (distributerGoodsEntitiesOne.size() == 0) {
                    // 二级匹配未找到，先尝试别名完全匹配
                    Map<String, Object> mapA = new HashMap<>();
                    mapA.put("disId", ordersEntity.getNxDoDistributerId());
                    mapA.put("alias", goodsName);
                    mapA.put("depId", ordersEntity.getNxDoDepartmentId());
                    mapA.put("disIds", ids);
                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesA = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
                    logger.info("[pasteSearchGoodsService] 二级匹配后-别名完全匹配查询结果数量: {}", distributerGoodsEntitiesA.size());

                    if (distributerGoodsEntitiesA.size() > 0) {
                        // 别名完全匹配有结果
                        if (distributerGoodsEntitiesA.size() == 1) {
                            // 别名完全匹配找到1个，直接保存订单
                            logger.info("[pasteSearchGoodsService] 二级匹配后-别名完全匹配找到1个结果，直接保存订单");
                            ordersEntity.setNxDoStatus(0);
                            addToReturnList(returnList, returnListOrderIndexList,
                                    saveOrderWithGoods(ordersEntity, distributerGoodsEntitiesA.get(0)), orderIndex);
                        } else {
                            // 别名完全匹配找到多个，列为候选商品列表
                            logger.info("[pasteSearchGoodsService] 二级匹配后-别名完全匹配找到{}个结果，列为候选商品列表", distributerGoodsEntitiesA.size());
                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesA);
                            NxDepartmentOrdersEntity tempOrder = aaaTemp(ordersEntity);
                            addToReturnList(returnList, returnListOrderIndexList, tempOrder, orderIndex);
                        }
                        orderIndex++;
                        continue; // 跳过后续匹配逻辑
                    }

                    // 别名完全匹配无结果，尝试商品名称模糊搜索
                    mapOne.put("depId", ordersEntity.getNxDoDepartmentId());
                    System.out.println("mappapaoneoeon" + mapOne);
                    List<NxDistributerGoodsEntity> distributerGoodsEntitiesLikeName = nxDistributerGoodsService.queryDisGoodsByLikeName(mapOne);
                    logger.info("[pasteSearchGoodsService] 二级匹配后-商品名称模糊搜索查询结果数量: {}", distributerGoodsEntitiesLikeName.size());
                    if (distributerGoodsEntitiesLikeName.size() > 0) {
                        // 商品名称模糊搜索有结果，无论数量多少都列为候选商品列表（不直接保存订单）
                        logger.info("[pasteSearchGoodsService] 二级匹配后-商品名称模糊搜索找到{}个结果，列为候选商品列表", distributerGoodsEntitiesLikeName.size());
                        // 记录商品ID和名称，用于调试
                        for (int idx = 0; idx < distributerGoodsEntitiesLikeName.size(); idx++) {
                            NxDistributerGoodsEntity goods = distributerGoodsEntitiesLikeName.get(idx);

                            logger.info("[pasteSearchGoodsService] 二级匹配后-商品名称模糊搜索-商品{}: id={}, name={}",
                                    idx + 1, goods.getNxDistributerGoodsId(), goods.getNxDgGoodsName());
                        }
                        ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesLikeName);
                        NxDepartmentOrdersEntity tempOrder = aaaTemp(ordersEntity);
                        // 验证aaaTemp后候选列表是否还在
                        if (tempOrder.getNxDistributerGoodsEntityList() != null) {
                            logger.info("[pasteSearchGoodsService] aaaTemp后，分销商商品候选列表数量: {}",
                                    tempOrder.getNxDistributerGoodsEntityList().size());
                        } else {
                            logger.warn("[pasteSearchGoodsService] aaaTemp后，分销商商品候选列表为null！");
                        }
                        addToReturnList(returnList, returnListOrderIndexList, tempOrder, orderIndex);
                        orderIndex++;
                        continue; // 跳过后续匹配逻辑
                    }
                    //1, 查拼音
                    String pinyinString = goodsName;
                    // 如果包含汉字才转换，否则直接用原名
                    if (goodsName.matches(".*[\u4E00-\u9FFF]+.*")) {
                        pinyinString = hanziToPinyin(goodsName);
                    }
                    Map<String, Object> mapTwo = new HashMap<>();
                    mapTwo.put("disId", ordersEntity.getNxDoDistributerId());
                    mapTwo.put("searchPinyin", pinyinString);
                    mapTwo.put("standard", ordersEntity.getNxDoStandard());
                    mapTwo.put("depId", ordersEntity.getNxDoDepartmentId());
                    mapTwo.put("disIds", ids);
                    List<NxDistributerGoodsEntity> disGoodsByNamePinyin = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
                    logger.info("[pasteSearchGoodsService] 三级匹配-拼音+规格查询结果数量: {}", disGoodsByNamePinyin.size());

                    // 三级 查询拼音完全一样 + 规格完全一样
                    // 三级 没有拼音完全一样的
                    if (disGoodsByNamePinyin.size() == 0) {
                        mapTwo.put("standard", null);
                        mapTwo.put("name", goodsName);
                        List<NxDistributerGoodsEntity> disGoodsByNamePinyinJust = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
                        logger.info("[pasteSearchGoodsService] 三级匹配-仅拼音查询结果数量: {}", disGoodsByNamePinyinJust.size());
                        if (disGoodsByNamePinyinJust.size() == 0) {
                            // 别名完全匹配已在二级匹配后执行，这里直接进行别名模糊匹配
                            logger.info("[pasteSearchGoodsService] 三级匹配-仅拼音无结果，进行别名模糊匹配，查询参数: {}", mapTwo);

                            List<NxDistributerGoodsEntity> distributerGoodsEntitiesALike = nxDistributerGoodsService.queryDisGoodsByAliasLike(mapTwo);
                            logger.info("[pasteSearchGoodsService] 四级匹配-别名模糊匹配查询结果数量: {}", distributerGoodsEntitiesALike.size());

                            if (distributerGoodsEntitiesALike.size() == 0) {
                                //查询 depGoosName
                                Map<String, Object> mapDep = new HashMap<>();
                                mapDep.put("disId", ordersEntity.getNxDoDistributerId());
                                mapDep.put("depId", ordersEntity.getNxDoDepartmentId());
                                mapDep.put("name", goodsName);
                                logger.info("[pasteSearchGoodsService] 查询部门商品历史记录，查询参数: {}", mapDep);
                                List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntityList = nxDepartmentDisGoodsService.queryDepartmentGoods(mapDep);
                                if (nxDepartmentDisGoodsEntityList.size() == 0) {
//                                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
                                    returnList.add(aaaTemp(ordersEntity));

                                } else if (nxDepartmentDisGoodsEntityList.size() == 1) {
                                    NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsEntityList.get(0);
                                    NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                                    returnList.add(saveOrderWithGoods(ordersEntity, distributerGoodsEntity));
                                } else {
                                    List<NxDistributerGoodsEntity> list = new ArrayList<>();
                                    for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : nxDepartmentDisGoodsEntityList) {
                                        Integer nxDdgDisGoodsId = departmentDisGoodsEntity.getNxDdgDisGoodsId();

                                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
                                        list.add(distributerGoodsEntity);
                                    }
                                    ordersEntity.setNxDistributerGoodsEntityList(list);
                                    returnList.add(aaaTemp(ordersEntity));

                                }

                            } else {
                                if (distributerGoodsEntitiesALike.size() == 1) {
                                    ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesALike);
                                    returnList.add(aaaTemp(ordersEntity));

                                } else {
                                    ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesALike);
                                    returnList.add(aaaTemp(ordersEntity));
                                }
                            }


                        } else {
                            if (disGoodsByNamePinyinJust.size() == 1) {
                                // 仅拼音匹配只有1个时，列为候选商品，不直接保存订单
                                ordersEntity.setNxDistributerGoodsEntityList(disGoodsByNamePinyinJust);
                                returnList.add(aaaTemp(ordersEntity));
                            } else {
                                ordersEntity.setNxDistributerGoodsEntityList(disGoodsByNamePinyinJust);
                                returnList.add(aaaTemp(ordersEntity));
                            }
                        }
                    } else {

                        // 三级 如果有拼音完全一样的
                        if (disGoodsByNamePinyin.size() == 1) {
                            //1 保存订单
                            NxDistributerGoodsEntity disGoodsEntity = disGoodsByNamePinyin.get(0);
                            ordersEntity.setNxDoStatus(0);
                            returnList.add(saveOrderWithGoods(ordersEntity, disGoodsEntity));
                        } else {
                            logger.info("[pasteSearchGoodsService] 三级匹配-拼音+规格查询到多个结果，数量: {}", disGoodsByNamePinyin.size());
                            ordersEntity.setNxDistributerGoodsEntityList(disGoodsByNamePinyin);
                            addToReturnList(returnList, returnListOrderIndexList,
                                    aaaTemp(ordersEntity), orderIndex);
                        }
                    }

                } else {
                    // 二级 有相同的
                    // 二级 1， 只有一个商品名称相同的，列为候选商品，不直接保存订单
                    if (distributerGoodsEntitiesOne.size() == 1) {
                        //查询 depGoods 是否有历史记录
                        Map<String, Object> map = new HashMap<>();
                        map.put("disId", ordersEntity.getNxDoDistributerId());
                        map.put("depId", ordersEntity.getNxDoDepartmentId());
                        map.put("name", ordersEntity.getNxDoGoodsName());
                        logger.info("[pasteSearchGoodsService] 二级匹配-查询部门商品历史记录，查询参数: {}", map);
                        List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntityList = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
                        if (nxDepartmentDisGoodsEntityList.size() == 1) {
                            // 如果有部门历史记录，则保存订单
                            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsEntityList.get(0);
                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                            ordersEntity.setNxDoStatus(0);
                            returnList.add(saveOrderWithGoods(ordersEntity, distributerGoodsEntity));
                        } else {
                            // 没有部门历史记录，列为候选商品
                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
                            returnList.add(aaaTemp(ordersEntity));
                        }

                    } else {
                        // 二级 商品名称相同
                        //查询 depGoods 是否有
                        Map<String, Object> map = new HashMap<>();
                        map.put("disId", ordersEntity.getNxDoDistributerId());
                        map.put("depId", ordersEntity.getNxDoDepartmentId());
                        map.put("name", ordersEntity.getNxDoGoodsName());
                        logger.info("[pasteSearchGoodsService] 二级匹配-查询部门商品历史记录，查询参数: {}", map);
                        List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntityList = nxDepartmentDisGoodsService.queryDepartmentGoods(map);
                        if (nxDepartmentDisGoodsEntityList.size() == 0) {
                            ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesOne);
                            returnList.add(aaaTemp(ordersEntity));

                        } else if (nxDepartmentDisGoodsEntityList.size() == 1) {
                            NxDepartmentDisGoodsEntity departmentDisGoodsEntity = nxDepartmentDisGoodsEntityList.get(0);
                            NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(departmentDisGoodsEntity.getNxDdgDisGoodsId());
                            ordersEntity.setNxDoStatus(0);
                            returnList.add(saveOrderWithGoods(ordersEntity, distributerGoodsEntity));
                        } else {
                            List<NxDistributerGoodsEntity> list = new ArrayList<>();
                            for (NxDepartmentDisGoodsEntity departmentDisGoodsEntity : nxDepartmentDisGoodsEntityList) {
                                Integer nxDdgDisGoodsId = departmentDisGoodsEntity.getNxDdgDisGoodsId();

                                NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryObject(nxDdgDisGoodsId);
                                list.add(distributerGoodsEntity);
                            }
                            ordersEntity.setNxDistributerGoodsEntityList(list);
                            returnList.add(aaaTemp(ordersEntity));

                        }
                    }
                }
            }

            else {
                if (distributerGoodsEntitiesZero.size() == 1) {
                    logger.info("[pasteSearchGoodsService] 一级匹配-商品名称+规格完全匹配，找到唯一商品，直接保存订单");
                    NxDistributerGoodsEntity disGoodsEntity = distributerGoodsEntitiesZero.get(0);
                    ordersEntity.setNxDoStatus(0);
                    NxDepartmentOrdersEntity savedOrder = saveOrderWithGoods(ordersEntity, disGoodsEntity);
                    returnList.add(savedOrder);
                } else {
                    //查询 depGoods 是否有
                    Map<String, Object> map = new HashMap<>();
                    map.put("disId", ordersEntity.getNxDoDistributerId());
                    map.put("depId", ordersEntity.getNxDoDepartmentId());
                    map.put("name", ordersEntity.getNxDoGoodsName());
                    logger.info("[pasteSearchGoodsService] 一级匹配-查询部门商品历史记录，查询参数: {}", map);
                    List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
                    if (nxDepartmentDisGoodsEntities.size() == 1) {
                        NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsEntities.get(0);
                        NxDistributerGoodsEntity distributerGoodsEntity = nxDistributerGoodsService.queryDisGoodsDetail(nxDepartmentDisGoodsEntity.getNxDdgDisGoodsId());
                        ordersEntity.setNxDoStatus(0);
                        NxDepartmentOrdersEntity savedOrder = saveOrderWithGoods(ordersEntity, distributerGoodsEntity);
                        returnList.add(savedOrder);
                    } else {
                        ordersEntity.setNxDistributerGoodsEntityList(distributerGoodsEntitiesZero);
                        returnList.add(aaaTemp(ordersEntity));
                    }

                }
            }

            orderIndex++;
            logger.info("[pasteSearchGoodsService] 订单[{}]处理完成，returnList 大小: {}", orderIndex - 1, returnList.size());
        }

//        logger.info("[pasteSearchGoodsService] 所有订单处理完成，orderList 大小: {}, returnList 大小: {}",
//                orderList != null ? orderList.size() : 0, returnList.size());
//
//        // 检查是否有订单未被处理
//        if (orderList != null && returnList.size() != orderList.size()) {
//            logger.warn("[pasteSearchGoodsService] 警告：returnList 大小({})与 orderList 大小({})不一致，可能有订单未被处理",
//                    returnList.size(), orderList.size());
//        }

//        // 直接返回订单实体列表（订单已包含 nxDistributerGoodsEntityList 等候选列表）
//        for (int i = 0; i < returnList.size(); i++) {
//            NxDepartmentOrdersEntity order = returnList.get(i);
//            logger.info("[pasteSearchGoodsService] 订单[{}]: 商品名称={}, 订单ID={}, 状态={}, 分销商商品ID={}, 候选商品数={}",
//                    i + 1, order.getNxDoGoodsName(), order.getNxDepartmentOrdersId(),
//                    order.getNxDoStatus(), order.getNxDoDisGoodsId(),
//                    order.getNxDistributerGoodsEntityList() != null ? order.getNxDistributerGoodsEntityList().size() : 0);
//        }
//
//        logger.info("[pasteSearchGoodsService] 订单列表处理完成，准备返回，数量: {}", returnList.size());
//        logger.info("[pasteSearchGoodsService] ========== 订单列表处理完成，准备返回 ==========");
//
        return R.ok().put("data", returnList);
    }


    private NxDepartmentOrdersEntity aaaTemp(NxDepartmentOrdersEntity order) {
        long aaaTempStartTime = System.currentTimeMillis();
        logger.info("[aaaTemp] ========== 开始执行aaaTemp，商品名称: {} ==========", order.getNxDoGoodsName());

        //1.查询 nxGoods 如果有完全一个的，就下载
        // 1.1 搜索商品名称+规格完全相同
        long query1StartTime = System.currentTimeMillis();
        Map<String, Object> map = new HashMap<>();
        map.put("name", order.getNxDoGoodsName());
        map.put("level", 3);
        List<NxGoodsEntity> nxGoodsEntitiesEx = nxGoodsService.queryNxGoodsByParams(map);
        long query1EndTime = System.currentTimeMillis();
        logger.info("[aaaTemp] [查询1-queryNxGoodsByParams] 耗时: {}ms, 结果数量: {}", 
                query1EndTime - query1StartTime, nxGoodsEntitiesEx.size());

        long query2StartTime = System.currentTimeMillis();
        List<NxGoodsEntity> nxGoodsEntitiesA = nxAliasService.queryNxGoodsByName(map);
        long query2EndTime = System.currentTimeMillis();
        logger.info("[aaaTemp] [查询2-queryNxGoodsByName] 耗时: {}ms, 结果数量: {}", 
                query2EndTime - query2StartTime, nxGoodsEntitiesA.size());

        String pinyinString = order.getNxDoGoodsName();
        for (int i = 0; i < order.getNxDoGoodsName().length(); i++) {
            String str = order.getNxDoGoodsName().substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(order.getNxDoGoodsName());
            }
        }
        long query3StartTime = System.currentTimeMillis();
        Map<String, Object> mapSame = new HashMap<>();
        mapSame.put("level", 3);
        mapSame.put("searchStr", order.getNxDoGoodsName());
        mapSame.put("searchPinyin", pinyinString);
        List<NxGoodsEntity> nxGoodsEntitiesSame = nxGoodsService.queryQuickSearchNxGoods(mapSame);
        long query3EndTime = System.currentTimeMillis();
        logger.info("[aaaTemp] [查询3-queryQuickSearchNxGoods] 耗时: {}ms, 结果数量: {}", 
                query3EndTime - query3StartTime, nxGoodsEntitiesSame.size());

        TreeSet<NxGoodsEntity> all = new TreeSet();
        all.addAll(nxGoodsEntitiesEx);
        all.addAll(nxGoodsEntitiesA);
        all.addAll(nxGoodsEntitiesSame);

        // 1.2 商品名称相同

        // 如果没有完全一样的，则视为临时
        if (all.size() > 0) {
            // 获取分销商ID，用于检查商品是否已下载
            Integer disId = order.getNxDoDistributerId();

            // 1. 收集自己(disId)的分销商商品对应的系统商品ID，协作配送商的商品不排除（系统商品库中仍显示，用户可下载）
            Set<Integer> distributerGoodsNxGoodsIds = new HashSet<>();
            List<NxDistributerGoodsEntity> distributerGoodsList = order.getNxDistributerGoodsEntityList();
            if (distributerGoodsList != null && distributerGoodsList.size() > 0) {
                logger.info("[aaaTemp] 已有分销商商品候选列表（数量: {}），只排除自己(disId={})的配送商品对应的系统商品", distributerGoodsList.size(), disId);
                for (NxDistributerGoodsEntity distributerGoods : distributerGoodsList) {
                    if (distributerGoods.getNxDgNxGoodsId() != null
                            && disId != null
                            && disId.equals(distributerGoods.getNxDgDistributerId())) {
                        distributerGoodsNxGoodsIds.add(distributerGoods.getNxDgNxGoodsId());
                    }
                }
                logger.info("[aaaTemp] 自己配送商品对应的系统商品ID集合（需从系统商品库排除）: {}", distributerGoodsNxGoodsIds);
            }

            // 2. 过滤系统商品：只保留从未下载过的商品
            //    如果商品已下载，需要将其对应的分销商商品加入到候选列表中
            long filterStartTime = System.currentTimeMillis();
            TreeSet<NxGoodsEntity> filteredNxGoodsSet = new TreeSet<>();
            List<NxDistributerGoodsEntity> distributerGoodsToAdd = new ArrayList<>();

            int totalGoodsCount = all.size();
            logger.info("[aaaTemp] [开始过滤系统商品] 系统商品总数: {}", totalGoodsCount);

            // 优化：批量查询系统商品的下载状态，避免N+1查询问题
            // 策略：收集需要检查的系统商品ID，然后批量查询这些ID对应的商品
            Map<Integer, NxDistributerGoodsEntity> downloadedGoodsMap = new HashMap<>();
            boolean useBatchQuery = totalGoodsCount >= 3; // 阈值可调整
            
            if (disId != null && totalGoodsCount > 0) {
                // 收集所有需要检查的系统商品ID（排除已在候选列表中的）
                List<Integer> goodsIdsToCheck = new ArrayList<>();
                for (NxGoodsEntity nxGoods : all) {
                    if (nxGoods.getNxGoodsId() != null) {
                        Integer nxGoodsId = nxGoods.getNxGoodsId();
                        // 只检查不在候选列表中的商品
                        if (!distributerGoodsNxGoodsIds.contains(nxGoodsId)) {
                            goodsIdsToCheck.add(nxGoodsId);
                        }
                    }
                }
                
                if (!goodsIdsToCheck.isEmpty() && useBatchQuery) {
                    // 批量查询：只查询这15个系统商品ID对应的商品（使用IN查询）
                    long batchQueryStartTime = System.currentTimeMillis();
                    logger.info("[aaaTemp] [批量查询下载状态] 系统商品数量: {}, 需要检查的商品ID数量: {}, 使用批量查询策略（IN查询）", 
                            totalGoodsCount, goodsIdsToCheck.size());
                    
                    // 使用IN查询一次性查询所有需要的商品
                    Map<String, Object> batchParams = new HashMap<>();
                    batchParams.put("disId", disId);
                    batchParams.put("depId", order.getNxDoDepartmentId());
                    batchParams.put("goodsIds", goodsIdsToCheck); // 传入商品ID列表
                    List<NxDistributerGoodsEntity> downloadedGoodsList = nxDistributerGoodsService.queryDisGoodsByParams(batchParams);
                    
                    // 将查询结果按nxGoodsId建立索引
                    if (downloadedGoodsList != null) {
                        for (NxDistributerGoodsEntity goods : downloadedGoodsList) {
                            if (goods.getNxDgNxGoodsId() != null) {
                                downloadedGoodsMap.put(goods.getNxDgNxGoodsId(), goods);
                            }
                        }
                    }
                    
                    long batchQueryEndTime = System.currentTimeMillis();
                    logger.info("[aaaTemp] [批量查询下载状态完成] 耗时: {}ms, 查询到已下载商品数量: {}, 需要检查的商品数量: {}", 
                            batchQueryEndTime - batchQueryStartTime, downloadedGoodsMap.size(), goodsIdsToCheck.size());
                } else if (!goodsIdsToCheck.isEmpty()) {
                    logger.info("[aaaTemp] [单个查询策略] 系统商品数量: {}, 需要检查的商品数量: {}, 使用单个查询策略", 
                            totalGoodsCount, goodsIdsToCheck.size());
                }
            }

            // 遍历系统商品，使用批量查询的结果
            for (NxGoodsEntity nxGoods : all) {
                if (nxGoods.getNxGoodsId() == null) {
                    continue;
                }

                Integer nxGoodsId = nxGoods.getNxGoodsId();

                // 2.1 检查是否已在候选列表中
                boolean inCandidateList = distributerGoodsNxGoodsIds.contains(nxGoodsId);

                // 2.2 检查是否已在数据库中下载过
                boolean alreadyDownloaded = false;
                NxDistributerGoodsEntity downloadedGoods = null;
                
                if (useBatchQuery) {
                    // 使用批量查询的结果
                    downloadedGoods = downloadedGoodsMap.get(nxGoodsId);
                    if (downloadedGoods != null) {
                        alreadyDownloaded = true;
                    }
                } else if (disId != null) {
                    // 使用单个查询（商品数量少时）
                    long checkStartTime = System.currentTimeMillis();
                    Map<String, Object> checkParams = new HashMap<>();
                    checkParams.put("disId", disId);
                    checkParams.put("goodsId", nxGoodsId);
                    checkParams.put("depId", order.getNxDoDepartmentId());
                    List<NxDistributerGoodsEntity> downloadedGoodsList = nxDistributerGoodsService.queryDisGoodsByParams(checkParams);
                    long checkEndTime = System.currentTimeMillis();
                    
                    if (downloadedGoodsList != null && !downloadedGoodsList.isEmpty()) {
                        alreadyDownloaded = true;
                        downloadedGoods = downloadedGoodsList.get(0);
                        if (checkEndTime - checkStartTime > 50) { // 只记录慢查询
                            logger.debug("[aaaTemp] 系统商品 nxGoodsId={} 检查下载状态耗时: {}ms", nxGoodsId, checkEndTime - checkStartTime);
                        }
                    }
                }

                // 2.3 如果已下载或已在候选列表中，不加入系统商品列表
                if (inCandidateList || alreadyDownloaded) {
                    // 如果已下载但不在候选列表中，需要加入到候选列表
                    if (alreadyDownloaded && !inCandidateList && downloadedGoods != null) {
                        // 检查是否已经在候选列表中（通过ID比较）
                        boolean existsInList = false;
                        if (distributerGoodsList != null) {
                            for (NxDistributerGoodsEntity existing : distributerGoodsList) {
                                if (existing.getNxDistributerGoodsId() != null &&
                                        existing.getNxDistributerGoodsId().equals(downloadedGoods.getNxDistributerGoodsId())) {
                                    existsInList = true;
                                    break;
                                }
                            }
                        }
                        if (!existsInList) {
                            distributerGoodsToAdd.add(downloadedGoods);
                            logger.info("[aaaTemp] 将已下载的分销商商品（ID={}）加入到候选列表", downloadedGoods.getNxDistributerGoodsId());
                        }
                    }
                } else {
                    // 从未下载过，加入系统商品列表
                    filteredNxGoodsSet.add(nxGoods);
                }
            }
            
            long filterEndTime = System.currentTimeMillis();
            long filterDuration = filterEndTime - filterStartTime;
            logger.info("[aaaTemp] [过滤系统商品完成] 总耗时: {}ms, 查询策略: {}, 过滤后系统商品数量: {}",
                    filterDuration, useBatchQuery ? "批量查询" : "单个查询", filteredNxGoodsSet.size());

            // 3. 将已下载的商品加入到候选列表（如果候选列表存在）
            if (!distributerGoodsToAdd.isEmpty() && distributerGoodsList != null) {
                distributerGoodsList.addAll(distributerGoodsToAdd);
                logger.info("[aaaTemp] 已将 {} 个已下载的分销商商品加入到候选列表", distributerGoodsToAdd.size());
            } else if (!distributerGoodsToAdd.isEmpty()) {
                // 如果候选列表不存在，创建新的列表
                order.setNxDistributerGoodsEntityList(distributerGoodsToAdd);
                logger.info("[aaaTemp] 创建新的候选列表，包含 {} 个已下载的分销商商品", distributerGoodsToAdd.size());
            }
            
            // 3.1 确保候选列表被显式保留（即使没有新商品要添加）
            if (distributerGoodsList != null && distributerGoodsList.size() > 0) {
                // 显式设置候选列表，确保数据一致性
                order.setNxDistributerGoodsEntityList(distributerGoodsList);
                logger.info("[aaaTemp] 显式保留分销商商品候选列表，数量: {}", distributerGoodsList.size());
            } else if (distributerGoodsList == null || distributerGoodsList.isEmpty()) {
                logger.info("[aaaTemp] 分销商商品候选列表为空或null，无需保留");
            }

            // 4. 设置过滤后的系统商品列表
            if (filteredNxGoodsSet.size() > 0) {
                logger.info("[aaaTemp] 过滤后，系统商品列表数量: {}（原始数量: {}），这些商品都是从未下载过的",
                        filteredNxGoodsSet.size(), all.size());
                order.setNxGoodsEntities(filteredNxGoodsSet);
            } else {
                logger.info("[aaaTemp] 过滤后，系统商品列表为空（所有系统商品都已下载过或已在候选列表中）");
                // 显式设置空列表，确保数据一致性
                order.setNxGoodsEntities(new TreeSet<>());
            }
        }

        order.setNxDoStatus(-2);
        order.setNxDoArriveDate(formatWhatDate(0));
        order.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
        order.setNxDoApplyDate(formatWhatDay(0));
        order.setNxDoArriveOnlyDate(formatWhatDate(0));
        order.setNxDoArriveWeeksYear(getWeekOfYear(0));
        order.setNxDoArriveDate(formatWhatDay(0));
        order.setNxDoApplyFullTime(formatWhatYearDayTime(0));
        order.setNxDoApplyOnlyTime(formatWhatTime(0));
        order.setNxDoGbDistributerId(-1);
        order.setNxDoGbDepartmentFatherId(-1);
        order.setNxDoGbDepartmentId(-1);
        order.setNxDoNxCommunityId(-1);
        order.setNxDoNxCommRestrauntFatherId(-1);
        order.setNxDoNxCommRestrauntId(-1);
        order.setNxDoPurchaseGoodsId(-1);
        order.setNxDoProfitSubtotal("0");
        order.setNxDoProfitScale("0");
        order.setNxDoCostPrice("0.1");
        order.setNxDoCostPriceLevel("1");
        order.setNxDoPurchaseGoodsId(-1);
        order.setNxDoCollaborativeNxDisId(-1);
        order.setNxDoArriveWhatDay(getWeek(0));

        // 在保存订单前，先保存候选列表的引用（候选列表是临时数据，不会保存到数据库）
        List<NxDistributerGoodsEntity> savedDistributerGoodsList = order.getNxDistributerGoodsEntityList() != null 
                ? new ArrayList<>(order.getNxDistributerGoodsEntityList()) : null;
        TreeSet<NxGoodsEntity> savedNxGoodsSet = order.getNxGoodsEntities() != null 
                ? new TreeSet<>(order.getNxGoodsEntities()) : null;

        // 检查是否是临时订单（用于查询推荐商品，不应该保存）
        // 临时订单的特征：没有ID，且没有设置订单用户ID（nxDoOrderUserId）
        boolean isTemporaryOrder = order.getNxDepartmentOrdersId() == null 
                && order.getNxDoOrderUserId() == null;
        
        if (isTemporaryOrder) {
            logger.info("[aaaTemp] 检测到临时订单（用于查询推荐商品），跳过保存操作");
        } else {
            // 如果订单还没有设置 nxDoTodayOrder，才查询数据库设置（避免覆盖预先设置的值）
            if (order.getNxDoTodayOrder() == null) {
                Map<String, Object> mapss = new HashMap<>();
                mapss.put("depId", order.getNxDoDepartmentId());
                mapss.put("status", 3);
                int orderOrder = nxDepartmentOrdersDao.queryDepOrdersAcount(mapss);
                int todayOrder = orderOrder + 1;
                order.setNxDoTodayOrder(todayOrder);
                logger.info("[aaaTemp] 设置订单 todayOrder: 商品名称={}, 订单ID={}, todayOrder={}, 查询到的最大order={}", 
                        order.getNxDoGoodsName(), order.getNxDepartmentOrdersId(), todayOrder, orderOrder);
            } else {
                logger.info("[aaaTemp] 订单已有 todayOrder，保留原值: 商品名称={}, 订单ID={}, todayOrder={}", 
                        order.getNxDoGoodsName(), order.getNxDepartmentOrdersId(), order.getNxDoTodayOrder());
            }
            // 检查订单是否已经有ID，如果有则更新，否则保存
            if (order.getNxDepartmentOrdersId() != null) {
                logger.info("[aaaTemp] 订单已有ID（{}），执行更新操作", order.getNxDepartmentOrdersId());
                nxDepartmentOrdersDao.update(order);
            } else {
                logger.info("[aaaTemp] 订单无ID，执行保存操作");
                nxDepartmentOrdersDao.save(order);
            }
            
            // 保存订单后，重新设置候选列表（因为候选列表是临时数据，不会保存到数据库）
            if (savedDistributerGoodsList != null && !savedDistributerGoodsList.isEmpty()) {
                order.setNxDistributerGoodsEntityList(savedDistributerGoodsList);
                logger.info("[aaaTemp] 保存订单后，重新设置分销商商品候选列表，数量: {}", savedDistributerGoodsList.size());
            }
            if (savedNxGoodsSet != null && !savedNxGoodsSet.isEmpty()) {
                order.setNxGoodsEntities(savedNxGoodsSet);
                logger.info("[aaaTemp] 保存订单后，重新设置系统商品列表，数量: {}", savedNxGoodsSet.size());
            } else if (savedNxGoodsSet != null && savedNxGoodsSet.isEmpty()) {
                // 显式设置空列表
                order.setNxGoodsEntities(new TreeSet<>());
            }
        }

        long aaaTempEndTime = System.currentTimeMillis();
        long aaaTempTotalDuration = aaaTempEndTime - aaaTempStartTime;
        logger.info("[aaaTemp] ========== aaaTemp执行完成，商品名称: {}, 总耗时: {}ms ==========", 
                order.getNxDoGoodsName(), aaaTempTotalDuration);

        return order;
    }

    /**
     * 将订单添加到返回列表，同时保存对应的原始订单索引
     *
     * @param returnList 返回列表
     * @param returnListOrderIndexList 返回列表对应的原始订单索引列表
     * @param order 订单实体
     * @param orderIndex 原始订单索引
     */
    private void addToReturnList(List<NxDepartmentOrdersEntity> returnList,
                                 List<Integer> returnListOrderIndexList,
                                 NxDepartmentOrdersEntity order,
                                 int orderIndex) {
        returnList.add(order);
        returnListOrderIndexList.add(orderIndex);
    }

    @Override
    public List<NxDepartmentOrdersEntity> searchAndSaveOrdersFromOcr(List<NxDepartmentOrdersEntity> orderList) {
        R result = pasteSearchGoods(orderList);
        
        @SuppressWarnings("unchecked")
        List<NxDepartmentOrdersEntity> responseList = (List<NxDepartmentOrdersEntity>) result.get("data");
        
        if (responseList == null) {
            responseList = new ArrayList<>();
        }
        
        // 遍历返回结果，如果找到唯一商品（status=0 且有商品ID），更新训练数据
        for (NxDepartmentOrdersEntity order : responseList) {
            if (order.getNxDoStatus() != null && order.getNxDoStatus() == 0 
                    && order.getNxDoDisGoodsId() != null 
                    && order.getNxDepartmentOrdersId() != null
                    && order.getNxDoTrainingDataId() != null) {
                
                NxOrderOcrTrainingDataEntity trainingData = nxOrderOcrTrainingDataService.queryObject(order.getNxDoTrainingDataId());
                
                if (trainingData != null && trainingData.getNxOtdDisGoodsId() == null) {
                    trainingData.setNxOtdDisGoodsId(order.getNxDoDisGoodsId());
                    trainingData.setNxOtdOrderId(order.getNxDepartmentOrdersId());
                    trainingData.setNxOtdFinalGoodsName(order.getNxDoGoodsName());
                    trainingData.setNxOtdFinalQuantity(order.getNxDoQuantity());
                    trainingData.setNxOtdFinalStandard(order.getNxDoStandard());
                    trainingData.setNxOtdFinalRemark(order.getNxDoRemark() != null ? order.getNxDoRemark() : "");
                    trainingData.setNxOtdIsNameManuallyAnnotated(1);
                    trainingData.setNxOtdIsQuantityManuallyAnnotated(1);
                    trainingData.setNxOtdIsStandardManuallyAnnotated(1);
                    trainingData.setNxOtdIsStandardWeightManuallyAnnotated(1);
                    trainingData.setNxOtdIsRemarkManuallyAnnotated(1);
                    if (trainingData.getNxOtdOriginalStandardWeight() != null) {
                        trainingData.setNxOtdFinalStandardWeight(trainingData.getNxOtdOriginalStandardWeight());
                    }
                    trainingData.setNxOtdUpdateDate(formatWhatYearDayTime(0));
                    nxOrderOcrTrainingDataService.update(trainingData);
                    logger.info("[searchAndSaveOrdersFromOcr] 更新训练数据（自动识别），训练数据ID: {}, 订单ID: {}, 商品ID: {}", 
                            trainingData.getNxOtdId(), order.getNxDepartmentOrdersId(), order.getNxDoDisGoodsId());
                }
            }
        }
        
        return responseList;
    }

    /**
     * 为订单添加推荐商品（参考 pasteSearchGoods 的查询方式）
     * 用于弱查询场景，在找到唯一商品后，继续查询相似商品作为推荐
     * 
     * @param order 已保存的订单（状态应为 -2）
     * @return 添加推荐商品后的订单实体（订单状态保持不变）
     */
    @Override
    public NxDepartmentOrdersEntity addCommentsGoodsForOrder(NxDepartmentOrdersEntity order) {
        long methodStartTime = System.currentTimeMillis();
        logger.info("[addCommentsGoodsForOrder] ========== 开始为订单添加推荐商品，订单ID: {}, 商品名称: {} ==========", 
                order.getNxDepartmentOrdersId(), order.getNxDoGoodsName());
        
        // 保存原始订单状态
        Integer originalStatus = order.getNxDoStatus();
        
        // 获取订单信息
        String goodsName = order.getNxDoGoodsName();
        String spec = order.getNxDoStandard();
        Integer disId = order.getNxDoDistributerId();
        Integer depId = order.getNxDoDepartmentId();
        
        // 获取已匹配的商品ID（用于后续将订单本身的商品排在第一位）
        Integer matchedDisGoodsId = order.getNxDoDisGoodsId();
        Set<Integer> excludeGoodsIds = new HashSet<>();
        // 不再排除订单本身的商品，因为-2状态的订单也需要显示订单本身的商品，并排在第一位
        logger.info("[addCommentsGoodsForOrder] 订单本身的商品ID: {}", matchedDisGoodsId);
        
        // 收集推荐商品列表
        List<NxDistributerGoodsEntity> recommendedGoodsList = new ArrayList<>();

        // 订货商品查询协作商户和自己的商品，构建多个配送商 id 列表
        Map<String, Object> mapGroup = new HashMap<>();
        mapGroup.put("orderDisId", disId);
        List<Integer> ids = new ArrayList<>();
        ids.add(disId);
        List<NxDistributerEntity> nxDistributerEntities = nxDistributerNxDistributerService.queryOfferNxDisByParams(mapGroup);
        if(nxDistributerEntities.size() > 0){
            Set<Integer> blockedIds = new HashSet<>(nxDistributerBlockService.queryBlockedDisIdsByBlocker(disId));
            for(NxDistributerEntity nxDistributerEntity: nxDistributerEntities){
                Integer partnerId = nxDistributerEntity.getNxDistributerId();
                if (!blockedIds.contains(partnerId)) {
                    ids.add(partnerId);
                }
            }
        }

        //部门商品

        
        // 1. 别名完全匹配
        long query1StartTime = System.currentTimeMillis();
        Map<String, Object> mapA = new HashMap<>();
        mapA.put("disId", disId);
        mapA.put("alias", goodsName);
        mapA.put("depId", depId);
        mapA.put("disIds", ids);
        List<NxDistributerGoodsEntity> distributerGoodsEntitiesA = nxDistributerGoodsService.queryDisGoodsByAlias(mapA);
        long query1EndTime = System.currentTimeMillis();
        logger.info("[addCommentsGoodsForOrder] [查询1-别名完全匹配] 耗时: {}ms, 结果数量: {}", 
                query1EndTime - query1StartTime, distributerGoodsEntitiesA.size());
        for (NxDistributerGoodsEntity goods : distributerGoodsEntitiesA) {
            if (goods.getNxDistributerGoodsId() != null && !excludeGoodsIds.contains(goods.getNxDistributerGoodsId())) {
                recommendedGoodsList.add(goods);
                excludeGoodsIds.add(goods.getNxDistributerGoodsId());
            }
        }
        
        // 2. 商品名称模糊搜索
        long query2StartTime = System.currentTimeMillis();
        Map<String, Object> mapOne = new HashMap<>();
        mapOne.put("disId", disId);
        mapOne.put("searchStr", goodsName);
        mapOne.put("depId", depId);
        mapOne.put("disIds", ids);
        List<NxDistributerGoodsEntity> distributerGoodsEntitiesLikeName = nxDistributerGoodsService.queryDisGoodsByLikeName(mapOne);
        long query2EndTime = System.currentTimeMillis();
        logger.info("[addCommentsGoodsForOrder] [查询2-商品名称模糊搜索] 耗时: {}ms, 结果数量: {}", 
                query2EndTime - query2StartTime, distributerGoodsEntitiesLikeName.size());
        for (NxDistributerGoodsEntity goods : distributerGoodsEntitiesLikeName) {
            if (goods.getNxDistributerGoodsId() != null && !excludeGoodsIds.contains(goods.getNxDistributerGoodsId())) {
                recommendedGoodsList.add(goods);
                excludeGoodsIds.add(goods.getNxDistributerGoodsId());
                logger.info("[addCommentsGoodsForOrder] 商品名称模糊搜索-添加推荐商品: ID={}, 名称={}", 
                        goods.getNxDistributerGoodsId(), goods.getNxDgGoodsName());
            } else if (goods.getNxDistributerGoodsId() != null) {
                logger.info("[addCommentsGoodsForOrder] 商品名称模糊搜索-跳过商品（已在排除列表）: ID={}, 名称={}", 
                        goods.getNxDistributerGoodsId(), goods.getNxDgGoodsName());
            }
        }
        
        // 3. 拼音匹配
        long query3StartTime = System.currentTimeMillis();
        String pinyinString = goodsName;
        if (goodsName.matches(".*[\u4E00-\u9FFF]+.*")) {
            pinyinString = hanziToPinyin(goodsName);
        }
        Map<String, Object> mapTwo = new HashMap<>();
        mapTwo.put("disId", disId);
        mapTwo.put("searchPinyin", pinyinString);
        mapTwo.put("standard", spec);
        mapTwo.put("depId", depId);
        mapTwo.put("disIds", ids);
        List<NxDistributerGoodsEntity> disGoodsByNamePinyin = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
        long query3EndTime = System.currentTimeMillis();
        logger.info("[addCommentsGoodsForOrder] [查询3-拼音+规格] 耗时: {}ms, 结果数量: {}", 
                query3EndTime - query3StartTime, disGoodsByNamePinyin.size());
        for (NxDistributerGoodsEntity goods : disGoodsByNamePinyin) {
            if (goods.getNxDistributerGoodsId() != null && !excludeGoodsIds.contains(goods.getNxDistributerGoodsId())) {
                recommendedGoodsList.add(goods);
                excludeGoodsIds.add(goods.getNxDistributerGoodsId());
            }
        }
        
        // 4. 仅拼音匹配（无规格）
        long query4StartTime = System.currentTimeMillis();
        long query4Duration = 0;
        if (disGoodsByNamePinyin.size() == 0) {
            mapTwo.put("standard", null);
            mapTwo.put("name", goodsName);
            mapTwo.put("depId", depId);
            List<NxDistributerGoodsEntity> disGoodsByNamePinyinJust = nxDistributerGoodsService.queryDisGoodsByNamePinyin(mapTwo);
            query4Duration = System.currentTimeMillis() - query4StartTime;
            logger.info("[addCommentsGoodsForOrder] [查询4-仅拼音] 耗时: {}ms, 结果数量: {}", 
                    query4Duration, disGoodsByNamePinyinJust.size());
            for (NxDistributerGoodsEntity goods : disGoodsByNamePinyinJust) {
                if (goods.getNxDistributerGoodsId() != null && !excludeGoodsIds.contains(goods.getNxDistributerGoodsId())) {
                    recommendedGoodsList.add(goods);
                    excludeGoodsIds.add(goods.getNxDistributerGoodsId());
                    logger.info("[addCommentsGoodsForOrder] 仅拼音查询-添加推荐商品: ID={}, 名称={}", 
                            goods.getNxDistributerGoodsId(), goods.getNxDgGoodsName());
                } else if (goods.getNxDistributerGoodsId() != null) {
                    logger.info("[addCommentsGoodsForOrder] 仅拼音查询-跳过商品（已在排除列表）: ID={}, 名称={}", 
                            goods.getNxDistributerGoodsId(), goods.getNxDgGoodsName());
                }
            }
        }
        
        // 5. 别名模糊匹配
        long query5StartTime = System.currentTimeMillis();
        List<NxDistributerGoodsEntity> distributerGoodsEntitiesALike = nxDistributerGoodsService.queryDisGoodsByAliasLike(mapTwo);
        long query5EndTime = System.currentTimeMillis();
        logger.info("[addCommentsGoodsForOrder] [查询5-别名模糊匹配] 耗时: {}ms, 结果数量: {}", 
                query5EndTime - query5StartTime, distributerGoodsEntitiesALike.size());
        for (NxDistributerGoodsEntity goods : distributerGoodsEntitiesALike) {
            if (goods.getNxDistributerGoodsId() != null && !excludeGoodsIds.contains(goods.getNxDistributerGoodsId())) {
                recommendedGoodsList.add(goods);
                excludeGoodsIds.add(goods.getNxDistributerGoodsId());
            }
        }
        
        // 6. 系统商品库查询（通过 aaaTemp 的逻辑）
        long query6StartTime = System.currentTimeMillis();
        // 创建临时订单对象，避免 aaaTemp 修改原始订单的字段
        // 重要：创建列表副本，避免 aaaTemp 修改 recommendedGoodsList 导致 ConcurrentModificationException
        NxDepartmentOrdersEntity tempOrderForSearch = new NxDepartmentOrdersEntity();
        tempOrderForSearch.setNxDoGoodsName(goodsName);
        tempOrderForSearch.setNxDoStandard(spec);
        tempOrderForSearch.setNxDoDistributerId(disId);
        tempOrderForSearch.setNxDoDepartmentId(depId);
        // 创建列表副本，避免 aaaTemp 修改原始列表
        List<NxDistributerGoodsEntity> recommendedGoodsListCopy = new ArrayList<>(recommendedGoodsList);
        tempOrderForSearch.setNxDistributerGoodsEntityList(recommendedGoodsListCopy);
        
        NxDepartmentOrdersEntity tempOrder = aaaTemp(tempOrderForSearch);
        long query6EndTime = System.currentTimeMillis();
        logger.info("[addCommentsGoodsForOrder] [查询6-aaaTemp系统商品库查询] 耗时: {}ms", query6EndTime - query6StartTime);
        
        // 从 aaaTemp 的结果中提取推荐商品
        if (tempOrder.getNxDistributerGoodsEntityList() != null && !tempOrder.getNxDistributerGoodsEntityList().isEmpty()) {
            logger.info("[addCommentsGoodsForOrder] aaaTemp返回的候选列表数量: {}", tempOrder.getNxDistributerGoodsEntityList().size());
            for (NxDistributerGoodsEntity goods : tempOrder.getNxDistributerGoodsEntityList()) {
                if (goods.getNxDistributerGoodsId() != null && !excludeGoodsIds.contains(goods.getNxDistributerGoodsId())) {
                    recommendedGoodsList.add(goods);
                    excludeGoodsIds.add(goods.getNxDistributerGoodsId());
                    logger.info("[addCommentsGoodsForOrder] aaaTemp-添加推荐商品: ID={}, 名称={}", 
                            goods.getNxDistributerGoodsId(), goods.getNxDgGoodsName());
                } else if (goods.getNxDistributerGoodsId() != null) {
                    logger.info("[addCommentsGoodsForOrder] aaaTemp-跳过商品（已在排除列表）: ID={}, 名称={}", 
                            goods.getNxDistributerGoodsId(), goods.getNxDgGoodsName());
                }
            }
        }
        
        // 系统商品（未下载的）
        if (tempOrder.getNxGoodsEntities() != null && !tempOrder.getNxGoodsEntities().isEmpty()) {
            order.setNxGoodsEntities(tempOrder.getNxGoodsEntities());
        }

        // 批量补充部门商品（推荐商品在部门的订货记录，用于前端显示历史单价/订货量）
        if (!recommendedGoodsList.isEmpty() && (depId != null || order.getNxDoDepartmentFatherId() != null)) {
            List<Integer> needDepGoodsIds = new ArrayList<>();
            for (NxDistributerGoodsEntity g : recommendedGoodsList) {
                if (g.getNxDistributerGoodsId() != null && g.getDepartmentDisGoodsEntity() == null) {
                    needDepGoodsIds.add(g.getNxDistributerGoodsId());
                }
            }
            if (!needDepGoodsIds.isEmpty()) {
                Map<String, Object> depQueryMap = new HashMap<>();
                depQueryMap.put("depId", depId);
                depQueryMap.put("depFatherId", order.getNxDoDepartmentFatherId());
                depQueryMap.put("disGoodsIds", needDepGoodsIds);
                List<NxDepartmentDisGoodsEntity> depGoodsList = nxDepartmentDisGoodsService.queryByDisGoodsIdsAndDep(depQueryMap);
                Map<Integer, NxDepartmentDisGoodsEntity> depGoodsMap = new HashMap<>();
                for (NxDepartmentDisGoodsEntity ddg : depGoodsList) {
                    if (ddg.getNxDdgDisGoodsId() != null && !depGoodsMap.containsKey(ddg.getNxDdgDisGoodsId())) {
                        depGoodsMap.put(ddg.getNxDdgDisGoodsId(), ddg);
                    }
                }
                for (NxDistributerGoodsEntity g : recommendedGoodsList) {
                    if (g.getDepartmentDisGoodsEntity() == null && g.getNxDistributerGoodsId() != null) {
                        g.setDepartmentDisGoodsEntity(depGoodsMap.get(g.getNxDistributerGoodsId()));
                    }
                }
                logger.info("[addCommentsGoodsForOrder] 批量补充部门商品: 需查询{}个, 查到{}个", needDepGoodsIds.size(), depGoodsMap.size());
            }
        }

        // 排序：1.有 departmentDisGoodsEntity（部门商品）的排最前；2.商品名称完全等于查询名称的排在仅包含名称的前面；3.同一系统商品时，自己的配送商品排在协作配送商前面
        recommendedGoodsList.sort((g1, g2) -> {
            // 有 departmentDisGoodsEntity 的优先排最前
            boolean g1Dep = g1.getDepartmentDisGoodsEntity() != null;
            boolean g2Dep = g2.getDepartmentDisGoodsEntity() != null;
            if (g1Dep && !g2Dep) return -1;
            if (!g1Dep && g2Dep) return 1;
            // 在部门商品下面：nxDgGoodsName 完全等于查询名称的排在仅包含名称的前面
            boolean g1Exact = goodsName != null && goodsName.equals(g1.getNxDgGoodsName());
            boolean g2Exact = goodsName != null && goodsName.equals(g2.getNxDgGoodsName());
            if (g1Exact && !g2Exact) return -1;
            if (!g1Exact && g2Exact) return 1;
            // 同一系统商品(nxDgNxGoodsId)时，自己的配送商品排在协作配送商前面
            Integer nx1 = g1.getNxDgNxGoodsId();
            Integer nx2 = g2.getNxDgNxGoodsId();
            if (nx1 != null && nx1.equals(nx2)) {
                Integer g1DisId = g1.getNxDgDistributerId();
                Integer g2DisId = g2.getNxDgDistributerId();
                if (g1DisId == null && g1.getDepartmentDisGoodsEntity() != null) {
                    g1DisId = g1.getDepartmentDisGoodsEntity().getNxDdgGoodsNxDistributerId();
                }
                if (g2DisId == null && g2.getDepartmentDisGoodsEntity() != null) {
                    g2DisId = g2.getDepartmentDisGoodsEntity().getNxDdgGoodsNxDistributerId();
                }
                boolean g1Own = disId != null && disId.equals(g1DisId);
                boolean g2Own = disId != null && disId.equals(g2DisId);
                if (g1Own && !g2Own) return -1;
                if (!g1Own && g2Own) return 1;
            }
            return 0;
        });
        
        // 对于-2状态的订单，将订单本身的商品排在第一位
        long query7StartTime = System.currentTimeMillis();
        long query7Duration = 0;
        if (originalStatus != null && originalStatus == -2 && matchedDisGoodsId != null) {
            // 查找订单本身的商品
            NxDistributerGoodsEntity matchedGoods = null;
            int matchedIndex = -1;
            for (int i = 0; i < recommendedGoodsList.size(); i++) {
                if (recommendedGoodsList.get(i).getNxDistributerGoodsId() != null 
                        && recommendedGoodsList.get(i).getNxDistributerGoodsId().equals(matchedDisGoodsId)) {
                    matchedGoods = recommendedGoodsList.get(i);
                    matchedIndex = i;
                    break;
                }
            }
            
            // 如果订单本身的商品不在列表中，需要查询并添加到第一位 
            if (matchedGoods == null) {
                try {
                    long query7SubStartTime = System.currentTimeMillis();
                    matchedGoods = nxDistributerGoodsService.queryObject(matchedDisGoodsId);
                    query7Duration = System.currentTimeMillis() - query7SubStartTime;
                    if (matchedGoods != null) {
                        logger.info("[addCommentsGoodsForOrder] [查询7-查询订单本身商品] 耗时: {}ms, 订单本身的商品不在推荐列表中，查询并添加到第一位: ID={}, 名称={}", 
                                query7Duration, matchedDisGoodsId, matchedGoods.getNxDgGoodsName());
                        recommendedGoodsList.add(0, matchedGoods);
                    }
                } catch (Exception e) {
                    logger.warn("[addCommentsGoodsForOrder] 查询订单本身的商品失败: ID={}, 错误: {}", matchedDisGoodsId, e.getMessage());
                }
            } else {
                query7Duration = System.currentTimeMillis() - query7StartTime;
                // 如果订单本身的商品已在列表中，将其移动到第一位
                if (matchedIndex > 0) {
                    logger.info("[addCommentsGoodsForOrder] [查询7-调整商品顺序] 耗时: {}ms, 订单本身的商品已在推荐列表中（位置{}），移动到第一位: ID={}, 名称={}", 
                            query7Duration, matchedIndex, matchedDisGoodsId, matchedGoods.getNxDgGoodsName());
                    recommendedGoodsList.remove(matchedIndex);
                    recommendedGoodsList.add(0, matchedGoods);
                } else {
                    logger.info("[addCommentsGoodsForOrder] [查询7-调整商品顺序] 耗时: {}ms, 订单本身的商品已在推荐列表第一位: ID={}, 名称={}", 
                            query7Duration, matchedDisGoodsId, matchedGoods.getNxDgGoodsName());
                }
            }
        }
        
        // 设置推荐商品列表
        if (!recommendedGoodsList.isEmpty()) {
            order.setNxDistributerGoodsEntityList(recommendedGoodsList);
            logger.info("[addCommentsGoodsForOrder] 找到 {} 个推荐商品", recommendedGoodsList.size());
        } else {
            logger.info("[addCommentsGoodsForOrder] 未找到推荐商品");
        }
        
        // 恢复原始订单状态（不改变订单状态）
        order.setNxDoStatus(originalStatus);
        
        // 更新订单
        long updateStartTime = System.currentTimeMillis();
        order.setNxDistributerGoodsEntityList(recommendedGoodsList);
        update(order);
        long updateEndTime = System.currentTimeMillis();
        logger.info("[addCommentsGoodsForOrder] [更新订单] 耗时: {}ms", updateEndTime - updateStartTime);
        
        long methodEndTime = System.currentTimeMillis();
        long methodTotalDuration = methodEndTime - methodStartTime;
        logger.info("[addCommentsGoodsForOrder] ========== 订单推荐商品添加完成，订单ID: {}, 推荐商品数量: {}, 订单状态: {}, 总耗时: {}ms ==========", 
                order.getNxDepartmentOrdersId(), 
                recommendedGoodsList != null ? recommendedGoodsList.size() : 0,
                order.getNxDoStatus(),
                methodTotalDuration);
        logger.info("[addCommentsGoodsForOrder] 性能统计 - 查询1(别名完全匹配): {}ms, 查询2(商品名称模糊): {}ms, 查询3(拼音+规格): {}ms, 查询4(仅拼音): {}ms, 查询5(别名模糊): {}ms, 查询6(aaaTemp): {}ms, 查询7(订单商品): {}ms, 更新订单: {}ms",
                query1EndTime - query1StartTime,
                query2EndTime - query2StartTime,
                query3EndTime - query3StartTime,
                query4Duration,
                query5EndTime - query5StartTime,
                query6EndTime - query6StartTime,
                query7Duration,
                updateEndTime - updateStartTime);
        
        return order;
    }


}

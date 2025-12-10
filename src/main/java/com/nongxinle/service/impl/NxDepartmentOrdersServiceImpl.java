package com.nongxinle.service.impl;

import com.alibaba.fastjson.JSON;
//import com.nongxinle.dao.NxDepartmentGoodsDao;
import com.nongxinle.dao.NxDepartmentUserDao;
import com.nongxinle.dao.NxDistributerUserDao;
import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.HttpUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.nongxinle.dao.NxDepartmentOrdersDao;
import org.springframework.transaction.annotation.Transactional;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDepOrderBuyStatusUnPurchase;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxOrderStatusNew;


@Service("nxDepartmentOrdersService")
public class NxDepartmentOrdersServiceImpl implements NxDepartmentOrdersService {
    @Autowired
    private NxDepartmentOrdersDao nxDepartmentOrdersDao;
    @Autowired
    private NxDepartmentDisGoodsService nxDepartmentDisGoodsService;
    @Autowired
    private NxDistributerGoodsService dgService;


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
        //如果不是连锁采购发送订单
//		nxDepartmentOrders.setNxDoStatus(getNxOrderStatusNew());
//		nxDepartmentOrders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusUnPurchase());
//		nxDepartmentOrders.setNxDoApplyDate(formatWhatDay(0));
//		nxDepartmentOrders.setNxDoApplyFullTime(formatWhatYearDayTime(0));
//		nxDepartmentOrders.setNxDoApplyOnlyTime(formatWhatTime(0));
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

//	@Override
//	public void saveGbOrders(NxDepartmentOrdersEntity ordersEntity) {
//
//		//判断是否是部门商品
//		Integer doDisGoodsId = ordersEntity.getNxDoDisGoodsId();
//		Integer doGbDepartmentId = ordersEntity.getNxDoGbDepartmentId();
//		//查询部门还是订货群是否添加过此商品
//		Map<String, Object> map = new HashMap<>();
//		map.put("gbDepId", doGbDepartmentId);
//		map.put("disGoodsId", doDisGoodsId);
//		List<NxDepartmentDisGoodsEntity> disGoodsEntities = nxDepartmentDisGoodsService.queryDepDisGoodsByParams(map);
//		if(disGoodsEntities.size() == 0 ){
//			//添加部门商品
//			NxDistributerGoodsEntity nxDistributerGoodsEntity = dgService.queryObject(doDisGoodsId);
//			String nxDgGoodsName = nxDistributerGoodsEntity.getNxDgGoodsName();
//			//
//			NxDepartmentDisGoodsEntity disGoodsEntity = new NxDepartmentDisGoodsEntity();
//			disGoodsEntity.setNxDdgDepGoodsName(nxDgGoodsName);
//			disGoodsEntity.setNxDdgDisGoodsId(doDisGoodsId);
//			disGoodsEntity.setNxDdgDisGoodsFatherId(nxDistributerGoodsEntity.getNxDgDfgGoodsFatherId());
//			disGoodsEntity.setNxDdgDepGoodsPinyin(nxDistributerGoodsEntity.getNxDgGoodsPinyin());
//			disGoodsEntity.setNxDdgDepGoodsPy(nxDistributerGoodsEntity.getNxDgGoodsPy());
//			disGoodsEntity.setNxDdgDepGoodsStandardname(nxDistributerGoodsEntity.getNxDgGoodsStandardname());
//			disGoodsEntity.setNxDdgGbDepartmentId(ordersEntity.getNxDoGbDepartmentId());
//			disGoodsEntity.setNxDdgGbDepartmentFatherId(ordersEntity.getNxDoGbDepartmentFatherId());
//			disGoodsEntity.setNxDdgIsGbDepartment(1);
//			disGoodsEntity.setNxDdgOrderQuantity(ordersEntity.getNxDoQuantity());
//			disGoodsEntity.setNxDdgOrderStandard(ordersEntity.getNxDoStandard());
//			disGoodsEntity.setNxDdgOrderRemark(ordersEntity.getNxDoRemark());
//			disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
//			nxDepartmentDisGoodsService.save(disGoodsEntity);
//			Integer nxDepartmentDisGoodsId = disGoodsEntity.getNxDepartmentDisGoodsId();
//			ordersEntity.setNxDoDepDisGoodsId(nxDepartmentDisGoodsId);
//		}else {
//			Integer nxDepartmentDisGoodsId = disGoodsEntities.get(0).getNxDepartmentDisGoodsId();
//			ordersEntity.setNxDoDepDisGoodsId(nxDepartmentDisGoodsId);
//			//
//			NxDepartmentDisGoodsEntity disGoodsEntity = nxDepartmentDisGoodsService.queryObject(nxDepartmentDisGoodsId);
//			disGoodsEntity.setNxDdgOrderQuantity(ordersEntity.getNxDoQuantity());
//			disGoodsEntity.setNxDdgOrderStandard(ordersEntity.getNxDoStandard());
//			disGoodsEntity.setNxDdgOrderRemark(ordersEntity.getNxDoRemark());
//			disGoodsEntity.setNxDdgOrderDate(formatWhatDay(0));
//			nxDepartmentDisGoodsService.update(disGoodsEntity);
//		}
//		ordersEntity.setNxDoStatus(getNxOrderStatusNew());
//		nxDepartmentOrdersDao.save(ordersEntity);
//
//	}

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

//    @Override
//    public List<NxDistributerFatherGoodsEntity> disGetOutStockGoodsApply(Map<String, Object> map) {
//
//		return nxDepartmentOrdersDao.disGetOutStockGoodsApply(map);
//    }

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
    public Map<Integer, Map<String, Integer>> batchQueryDepStats(List<Integer> depIds) {
        // 使用 selectList 而不是 selectOne
        List<Map<String, Object>> results = nxDepartmentOrdersDao.batchQueryDepStats(depIds);

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
    public Map<Integer, Map<String, Integer>> batchQueryGbDepStats(List<Integer> gbDepIds) {
        // 使用 selectList 而不是 selectOne
        List<Map<String, Object>> results = nxDepartmentOrdersDao.batchQueryGbDepStats(gbDepIds);

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

//    @Override
//    public List<GbDepartmentEntity> queryqueryOrderGbDepartmentList(Map<String, Object> map1) {
//        return null;
//    }

//    @Override
//    public List<GbDepartmentEntity> queryqueryOrderGbDepartmentList(Map<String, Object> map1) {
//
//        return nxDepartmentOrdersDao.queryqueryOrderGbDepartmentList(map1);
//    }



    @Override
    @Transactional
    public void moveOrderToHistory(NxDepartmentOrdersEntity order) {

        Integer orderId = order.getNxDepartmentOrdersId();
        System.out.println("订单开始：orderId = " + orderId);

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
            shelfEntities.add(emptyShelf);
        }
        long t4 = System.currentTimeMillis();

        // 2. 获取部门数据
        System.out.println("nxdepepep" + params);
        List<NxDepartmentEntity> departmentEntities = queryPureOrderNxDepartmentSimple(params);
        long t5 = System.currentTimeMillis();
        List<GbDepartmentEntity> gbDepartmentEntities = queryPureOrderGbDepartment(params);
        long t6 = System.currentTimeMillis();


        // 3. 获取统计数据
        Integer countDep = queryDepOrdersAcount(params);
        long t7 = System.currentTimeMillis();
        Integer count = queryDepOrdersAcount(params);
        long t8 = System.currentTimeMillis();
        Integer stockCount = disGetPurchaseGoodsApplysCount(params);
        long t9 = System.currentTimeMillis();
        Integer stockCountOK = disGetPurchaseGoodsApplysCount(params);
        long t10 = System.currentTimeMillis();
        System.out.println("queryShelfGoodsOrder 1 耗时: " + (t2-t1));
        System.out.println("queryShelfGoodsOrder 2 耗时: " + (t3-t2));
        System.out.println("processEmptyShelf 耗时: " + (t4-t3));
        System.out.println("queryPureOrderNxDepartmentSimple 耗时: " + (t5-t4));
        System.out.println("queryPureOrderGbDepartment 耗时: " + (t6-t5));
        System.out.println("queryDepOrdersAcount 1 耗时: " + (t7-t6));
        System.out.println("queryDepOrdersAcount 2 耗时: " + (t8-t7));
        System.out.println("disGetPurchaseGoodsApplysCount 1 耗时: " + (t9-t8));
        System.out.println("disGetPurchaseGoodsApplysCount 2 耗时: " + (t10-t9));


        // 组装结果
        result.put("shelfArr", shelfEntities);
        result.put("waitDepNx", departmentEntities);
        result.put("waitDepGb", gbDepartmentEntities);
        result.put("depOrdersWait", count);
        result.put("idDepOrdersWait", countDep);
        result.put("stockCount", stockCount);
        result.put("stockCountOk", stockCountOK);

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


    private String getToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token?appid=wxbc686226ccc443f1&secret=cefb0c474497e74879687862b0d8733e&grant_type=client_credential";
        String result = HttpUtils.get(url);
        Map<String, Object> map = JSON.parseObject(result);
        String access_token = map.get("access_token").toString();
        return access_token;
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
        List<ShelfListSimpleDTO> shelfListDTO = nxDepartmentOrdersDao.queryShelfListBasic(params);
        
        // 2. 获取部门数据
        List<NxDepartmentEntity> departmentEntities = queryPureOrderNxDepartmentSimple(params);
        List<GbDepartmentEntity> gbDepartmentEntities = queryPureOrderGbDepartment(params);
        
        // 3. 获取统计数据
        Integer count = queryDepOrdersAcount(params);
        Integer stockCount = disGetPurchaseGoodsApplysCount(params);
        params.put("purStatus", null);
        params.put("dayuPurStatus", 3);
        Integer stockCountOK = disGetPurchaseGoodsApplysCount(params);
        
        // 组装结果
        result.put("shelfList", shelfListDTO);
        result.put("waitDepNx", departmentEntities);
        result.put("waitDepGb", gbDepartmentEntities);
        result.put("depOrdersWait", count);
        result.put("stockCount", stockCount);
        result.put("stockCountOk", stockCountOK);
        
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
        // 设置 shelfId = 1 表示只查询有货架的商品
        // 添加 targetShelfId 参数用于过滤指定货架
        Integer targetShelfId = (Integer) params.get("shelfId");
        params.put("shelfId", 1);
        params.put("targetShelfId", targetShelfId);
        
        // 使用超简化版查询，字段扁平化，大幅减少数据传输量
        ShelfDetailSimpleDTO shelfDetail = nxDepartmentOrdersDao.queryShelfGoodsDetailUltraSimple(params);
        
        // 如果没有找到，返回一个空的货架对象
        if (shelfDetail == null) {
            shelfDetail = new ShelfDetailSimpleDTO();
            shelfDetail.setNxDistributerGoodsShelfId(targetShelfId);
            shelfDetail.setGoodsList(new ArrayList<>());
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
        Integer stockCount = disGetPurchaseGoodsApplysCount(params);
        params.put("purStatus", null);
        params.put("dayuPurStatus", 3);
        Integer stockCountOK = disGetPurchaseGoodsApplysCount(params);
        
        // 组装结果
        result.put("categoryList", categoryListDTO);
        result.put("waitDepNx", departmentEntities);
        result.put("waitDepGb", gbDepartmentEntities);
        result.put("depOrdersWait", count);
        result.put("stockCount", stockCount);
        result.put("stockCountOk", stockCountOK);
        
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


}

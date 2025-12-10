package com.nongxinle.controller;

/**
 * 用户与角色对应关系
 *
 * @author lpy
 * @date 05-09 18:47
 */

import java.util.*;

import com.nongxinle.entity.NxDistributerGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfGoodsEntity;
import com.nongxinle.entity.NxDistributerGoodsShelfStockEntity;
import com.nongxinle.service.NxDistributerGoodsService;
import com.nongxinle.service.NxDistributerGoodsShelfService;
import com.nongxinle.service.NxDistributerGoodsShelfStockService;
import com.nongxinle.utils.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nongxinle.service.NxDistributerGoodsShelfGoodsService;
import com.nongxinle.utils.R;

import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/nxdistributergoodsshelfgoods")
public class NxDistributerGoodsShelfGoodsController {
    private static final Logger logger = LoggerFactory.getLogger(NxDistributerGoodsShelfGoodsController.class);
    @Autowired
    private NxDistributerGoodsShelfGoodsService nxDisGoodsShelfGoodsService;
    @Autowired
    private NxDistributerGoodsService dgService;
    @Autowired
    private NxDistributerGoodsShelfService nxDistributerGoodsShelfService;
    @Autowired
    private NxDistributerGoodsShelfStockService nxDistributerGoodsShelfStockService;



    

    @RequestMapping(value = "/setShelfLayer", method = RequestMethod.POST)
    @ResponseBody
    public R setShelfLayer(@RequestBody Map<String, Object> payload) {
        Integer shelfGoodsId = extractShelfGoodsId(payload);
        if (shelfGoodsId == null) {
            return R.error("缺少货架商品ID");
        }

        NxDistributerGoodsShelfGoodsEntity target = nxDisGoodsShelfGoodsService.queryObject(shelfGoodsId);
        if (target == null) {
            return R.error("货架商品不存在");
        }
        Integer shelfId = target.getNxDgsgShelfId();

        List<NxDistributerGoodsShelfGoodsEntity> goodsList = nxDisGoodsShelfGoodsService.queryShelfGoodsBasic(shelfId);
        if (goodsList == null || goodsList.isEmpty()) {
            return R.error("货架暂无商品");
        }

        int targetIndex = -1;
        TreeSet<Integer> layerPositions = new TreeSet<>();
        for (int i = 0; i < goodsList.size(); i++) {
            NxDistributerGoodsShelfGoodsEntity item = goodsList.get(i);
            if (Objects.equals(item.getNxDistributerGoodsShelfGoodsId(), shelfGoodsId)) {
                targetIndex = i;
            }
            if (item.getNxDgsgShelfLayer() != null) {
                layerPositions.add(i);
            }
        }
        if (targetIndex == -1) {
            return R.error("货架商品不存在");
        }

        layerPositions.add(targetIndex);

        Map<Integer, Integer> positionToLayer = new HashMap<>();
        int layerNo = 0;
        for (Integer position : layerPositions) {
            layerNo++;
            positionToLayer.put(position, layerNo);
        }

        applyLayerAdjustments(goodsList, positionToLayer);
        Map<String, Object> data = buildLayerResponse(goodsList, shelfId, positionToLayer.get(targetIndex));
        return R.ok().put("data", data);
    }

    @RequestMapping(value = "/clearShelfLayer", method = RequestMethod.POST)
    @ResponseBody
    public R clearShelfLayer(@RequestBody Map<String, Object> payload) {
        Integer shelfGoodsId = extractShelfGoodsId(payload);
        if (shelfGoodsId == null) {
            return R.error("缺少货架商品ID");
        }

        NxDistributerGoodsShelfGoodsEntity target = nxDisGoodsShelfGoodsService.queryObject(shelfGoodsId);
        if (target == null) {
            return R.error("货架商品不存在");
        }
        Integer shelfId = target.getNxDgsgShelfId();

        List<NxDistributerGoodsShelfGoodsEntity> goodsList = nxDisGoodsShelfGoodsService.queryShelfGoodsBasic(shelfId);
        if (goodsList == null || goodsList.isEmpty()) {
            return R.error("货架暂无商品");
        }

        int targetIndex = -1;
        TreeSet<Integer> layerPositions = new TreeSet<>();
        for (int i = 0; i < goodsList.size(); i++) {
            NxDistributerGoodsShelfGoodsEntity item = goodsList.get(i);
            if (Objects.equals(item.getNxDistributerGoodsShelfGoodsId(), shelfGoodsId)) {
                targetIndex = i;
            }
            if (item.getNxDgsgShelfLayer() != null) {
                layerPositions.add(i);
            }
        }
        if (targetIndex == -1) {
            return R.error("货架商品不存在");
        }
        if (!layerPositions.remove(targetIndex)) {
            return R.ok().put("message", "该商品未设置层标志，无需取消");
        }

        Map<Integer, Integer> positionToLayer = new HashMap<>();
        int layerNo = 0;
        for (Integer position : layerPositions) {
            layerNo++;
            positionToLayer.put(position, layerNo);
        }

        applyLayerAdjustments(goodsList, positionToLayer);
        Map<String, Object> data = buildLayerResponse(goodsList, shelfId, null);
        return R.ok().put("data", data);
    }

    @RequestMapping(value = "/disGetUnshelfGoods/{disId}")
    @ResponseBody
    public R disGetUnshelfGoods(@PathVariable Integer disId, Integer page, Integer limit) {
        // 默认值处理
        if (page == null || page < 1) {
            page = 1;
        }
        if (limit == null || limit < 1) {
            limit = 10;
        }

        // 计算 offset
        int offset = (page - 1) * limit;

        // 构建查询参数
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("offset", offset);
        map.put("limit", limit);
        map.put("status", 3);

        // 查询未上架商品列表
        System.out.println("ushelelel" + map);
        List<NxDistributerGoodsEntity> goodsList = dgService.queryDisUnshelfGoodsWithPage(map);

        // 查询总数
        int total = dgService.queryDisUnshelfGoodsTotal(map);

        // 构造分页结果
        PageUtils pageUtil = new PageUtils(goodsList, total, limit, page);

        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/queryDisShelfGoods", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisShelfGoods(String searchStr, String disId) {
        System.out.println(searchStr);
        Map<String, Object> map = new HashMap<>();
        String pinyinString = searchStr;
        map.put("disId", disId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchStr", searchStr);
        map.put("searchPinyin", pinyinString);

        List<NxDistributerGoodsShelfGoodsEntity> goodsEntities = dgService.queryDisShelfGoods(map);
        System.out.println("unshheelemap" + map);
        List<NxDistributerGoodsEntity> unShelfGoods =  dgService.queryUnShelfDisGoodsQuickSearchStr(map);
        Map<String, Object> mapRe = new HashMap<>();
        mapRe.put("shelfArr", goodsEntities);
        mapRe.put("goodsArr", unShelfGoods );
        return R.ok().put("data", mapRe);
    }

    @RequestMapping(value = "/queryDisShelfGoodsWithNxGoodsId", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisShelfGoodsWithNxGoodsId(String searchStr, String disId) {
        System.out.println(searchStr);
        Map<String, Object> map = new HashMap<>();
        String pinyinString = searchStr;
        map.put("disId", disId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                pinyinString = hanziToPinyin(searchStr);
            }
        }
        map.put("searchStr", searchStr);
        map.put("searchPinyin", pinyinString);

        // 查询每个商品都要有nxGoodsId的货架商品
        List<NxDistributerGoodsShelfGoodsEntity> goodsEntities = dgService.queryDisShelfGoodsWithNxGoodsId(map);
        System.out.println("unshheelemap" + map);
        // 查询每个商品都要有nxGoodsId的非货架商品
        List<NxDistributerGoodsEntity> unShelfGoods = dgService.queryUnShelfDisGoodsQuickSearchStrWithNxGoodsId(map);
        Map<String, Object> mapRe = new HashMap<>();
        mapRe.put("shelfArr", goodsEntities);
        mapRe.put("goodsArr", unShelfGoods );
        return R.ok().put("data", mapRe);
    }


    @RequestMapping(value = "/addShelfGoods", method = RequestMethod.POST)
    @ResponseBody
    public R addShelfGoods(@RequestBody List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsList) {

        NxDistributerGoodsShelfGoodsEntity nxDistributerGoodsShelfGoodsEntity = shelfGoodsList.get(0);
        Integer nxDgsgSort = nxDistributerGoodsShelfGoodsEntity.getNxDgsgSort();
        Integer nxDgsgShelfId = nxDistributerGoodsShelfGoodsEntity.getNxDgsgShelfId();
        Integer nxDgsgDisGoodsId = nxDistributerGoodsShelfGoodsEntity.getNxDgsgDisGoodsId();

        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(nxDgsgDisGoodsId);
        distributerGoodsEntity.setNxDgPurchaseAuto(-1);
        dgService.update(distributerGoodsEntity);

        Map<String, Object> map = new HashMap<>();
        map.put("dayuSort", nxDgsgSort - 1);
        map.put("shelfId", nxDgsgShelfId);
        System.out.println("kkmap====" + map);
        List<NxDistributerGoodsShelfGoodsEntity> nxDistGoodsShelfEntities = nxDisGoodsShelfGoodsService.queryShelfForGoodsByParams(map);
        System.out.println(nxDistGoodsShelfEntities.size());
        for (int i = 0; i < nxDistGoodsShelfEntities.size(); i++) {
            int size = shelfGoodsList.size();
            nxDistGoodsShelfEntities.get(i).setNxDgsgSort(size + i + nxDgsgSort);
            nxDisGoodsShelfGoodsService.update(nxDistGoodsShelfEntities.get(i));
        }

        for (NxDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsList) {
            nxDisGoodsShelfGoodsService.save(shelfGoods);
        }

        // 如果商品有库存，更新库存的货架商品ID
        for (NxDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsList) {
            Integer disGoodsId = shelfGoods.getNxDgsgDisGoodsId();
            Integer shelfGoodsId = shelfGoods.getNxDistributerGoodsShelfGoodsId();
            if (disGoodsId != null && shelfGoodsId != null) {
                // 查询该商品的所有库存
                Map<String, Object> stockMap = new HashMap<>();
                stockMap.put("disGoodsId", disGoodsId);
                stockMap.put("restWeight", "0"); // 查询剩余库存大于0的
                List<NxDistributerGoodsShelfStockEntity> stockList = nxDistributerGoodsShelfStockService.queryShelfStockListByParams(stockMap);
                
                if (stockList != null && !stockList.isEmpty()) {
                    logger.info("[addShelfGoods] 商品 {} 有库存批次 {} 个，更新库存的货架商品ID为 {}", 
                            disGoodsId, stockList.size(), shelfGoodsId);
                    // 更新每个库存批次的货架商品ID
                    for (NxDistributerGoodsShelfStockEntity stock : stockList) {
                        // 只更新那些货架商品ID为空或不同的库存
                        if (stock.getNxDgssNxShelfGoodsId() == null || 
                            !stock.getNxDgssNxShelfGoodsId().equals(shelfGoodsId)) {
                            stock.setNxDgssNxShelfGoodsId(shelfGoodsId);
                            nxDistributerGoodsShelfStockService.update(stock);
                            logger.debug("[addShelfGoods] 更新库存批次 {} 的货架商品ID为 {}", 
                                    stock.getNxDistributerGoodsShelfStockId(), shelfGoodsId);
                        }
                    }
                }
            }
        }

        // 更新所有添加商品的重复标记
        Set<Integer> updatedGoodsIds = new HashSet<>();
        for (NxDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsList) {
            Integer disGoodsId = shelfGoods.getNxDgsgDisGoodsId();
            if (disGoodsId != null && !updatedGoodsIds.contains(disGoodsId)) {
                nxDisGoodsShelfGoodsService.updateDuplicateFlagForGoods(disGoodsId);
                updatedGoodsIds.add(disGoodsId);
                logger.debug("[addShelfGoods] 更新商品重复标记，disGoodsId={}", disGoodsId);
            }
        }

        return R.ok();
    }

    @RequestMapping(value = "/updateShelfGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updateShelfGoods(@RequestBody NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity) {
        // 查询货架信息并设置排序号
        if (shelfGoodsEntity.getNxDgsgShelfId() != null) {
            // 1. 查询货架信息，获取货架排序号（赋值给 nxDgsgSort）
            NxDistributerGoodsShelfEntity shelfEntity = nxDistributerGoodsShelfService.queryObject(shelfGoodsEntity.getNxDgsgShelfId());
            if (shelfEntity != null && shelfEntity.getNxDistributerGoodsShelfSort() != null) {
                shelfGoodsEntity.setNxDgsgSort(shelfEntity.getNxDistributerGoodsShelfSort());
                logger.debug("[updateShelfGoods] 设置货架排序号，货架ID: {}, 货架排序号: {}", 
                        shelfGoodsEntity.getNxDgsgShelfId(), shelfEntity.getNxDistributerGoodsShelfSort());
            } else {
                logger.warn("[updateShelfGoods] 未找到货架信息或货架排序号为空，货架ID: {}", 
                        shelfGoodsEntity.getNxDgsgShelfId());
            }
            
            // 2. 查询该货架下有多少个货架商品，数量+1就是新的货架商品排序（赋值给 nxDgsgShelfSort）
            Map<String, Object> countMap = new HashMap<>();
            countMap.put("shelfId", shelfGoodsEntity.getNxDgsgShelfId());
            List<NxDistributerGoodsShelfGoodsEntity> existingGoods = nxDisGoodsShelfGoodsService.queryShelfForGoodsByParams(countMap);
            int goodsCount = (existingGoods != null) ? existingGoods.size() : 0;
            // 如果当前商品已存在，需要排除它自己
            if (shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId() != null) {
                // 排除当前商品，计算其他商品的数量
                long otherGoodsCount = existingGoods.stream()
                    .filter(g -> !g.getNxDistributerGoodsShelfGoodsId().equals(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId()))
                    .count();
                goodsCount = (int) otherGoodsCount;
            }
            // 数量+1
            int newShelfSort = goodsCount + 1;
            shelfGoodsEntity.setNxDgsgShelfSort(newShelfSort);
            logger.debug("[updateShelfGoods] 设置货架商品排序，货架ID: {}, 当前商品数量: {}, 新排序号: {}", 
                    shelfGoodsEntity.getNxDgsgShelfId(), goodsCount, newShelfSort);
        } else {
            logger.warn("[updateShelfGoods] 货架ID为空，无法查询货架信息");
        }

        nxDisGoodsShelfGoodsService.update(shelfGoodsEntity);
        return R.ok();
    }


    @RequestMapping(value = "/updateShelfGoodsSort", method = RequestMethod.POST)
    @ResponseBody
    public R updateShelfGoodsSort(@RequestBody List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsList) {
        for (NxDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsList) {
            nxDisGoodsShelfGoodsService.update(shelfGoods);
        }
        return R.ok();
    }

    @RequestMapping(value = "/deleteShelfGoods/{id}")
    @ResponseBody
    public R deleteShelfGoods(@PathVariable Integer id) {

        NxDistributerGoodsShelfGoodsEntity nxDisGoodsShelfGoods = nxDisGoodsShelfGoodsService.queryObject(id);
        Integer nxDgsgSort = nxDisGoodsShelfGoods.getNxDgsgSort();
        Integer nxDgsgShelfId = nxDisGoodsShelfGoods.getNxDgsgShelfId();
        Map<String, Object> map = new HashMap<>();
        map.put("dayuSort", nxDgsgSort - 1);
        map.put("shelfId", nxDgsgShelfId);
        List<NxDistributerGoodsShelfGoodsEntity> nxDistGoodsShelfEntities = nxDisGoodsShelfGoodsService.queryShelfForGoodsByParams(map);
        System.out.println(nxDistGoodsShelfEntities.size());
        for (int i = 0; i < nxDistGoodsShelfEntities.size(); i++) {
            NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity = nxDistGoodsShelfEntities.get(i);
            shelfGoodsEntity.setNxDgsgSort(shelfGoodsEntity.getNxDgsgSort() - 1);
            nxDisGoodsShelfGoodsService.update(shelfGoodsEntity);
        }
        Integer nxDgsgDisGoodsId = nxDisGoodsShelfGoods.getNxDgsgDisGoodsId();
        nxDisGoodsShelfGoodsService.delete(id);
        
        // 更新该商品在所有货架的重复标记（删除后需要重新计算）
        if (nxDgsgDisGoodsId != null) {
            nxDisGoodsShelfGoodsService.updateDuplicateFlagForGoods(nxDgsgDisGoodsId);
            logger.debug("[deleteShelfGoods] 更新商品重复标记，disGoodsId={}", nxDgsgDisGoodsId);
        }
        
        return R.ok();

    }


    private void applyLayerAdjustments(List<NxDistributerGoodsShelfGoodsEntity> goodsList,
                                       Map<Integer, Integer> positionToLayer) {
        for (int i = 0; i < goodsList.size(); i++) {
            NxDistributerGoodsShelfGoodsEntity item = goodsList.get(i);
            Integer newLayer = positionToLayer.get(i);
            Integer oldLayer = item.getNxDgsgShelfLayer();
            if (!Objects.equals(newLayer, oldLayer)) {
                nxDisGoodsShelfGoodsService.updateShelfLayer(item.getNxDistributerGoodsShelfGoodsId(), newLayer);
                item.setNxDgsgShelfLayer(newLayer);
            }
        }
    }

    private Map<String, Object> buildLayerResponse(List<NxDistributerGoodsShelfGoodsEntity> goodsList,
                                                   Integer shelfId,
                                                   Integer assignedLayer) {
        Map<String, Object> data = new HashMap<>();
        data.put("shelfId", shelfId);
        data.put("assignedLayer", assignedLayer);

        List<Map<String, Object>> layerList = new ArrayList<>();
        int totalLayers = 0;
        for (NxDistributerGoodsShelfGoodsEntity item : goodsList) {
            if (item.getNxDgsgShelfLayer() != null) {
                totalLayers++;
                Map<String, Object> row = new HashMap<>();
                row.put("shelfGoodsId", item.getNxDistributerGoodsShelfGoodsId());
                row.put("layer", item.getNxDgsgShelfLayer());
                layerList.add(row);
            }
        }
        data.put("totalLayers", totalLayers);
        data.put("layers", layerList);
        return data;
    }

    private Integer extractShelfGoodsId(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }
        Object value = payload.get("shelfGoodsId");
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 查询未盘库的货架商品列表
     * @param shelfId 货架ID（必填）
     * @param inventoryType 盘库类型：1=日盘库，2=周盘库，3=月盘库（必填）
     * @param inventoryDate 日盘库：日期，格式 "2025-01-15"
     * @param inventoryWeek 周盘库：周数，格式 "3"（当前年第几周）
     * @param inventoryMonth 月盘库：月份，格式 "01"
     * @param page 页码，默认 1
     * @param limit 每页数量，默认 15
     * @return
     */
    @RequestMapping(value = "/queryUnInventoriedShelfGoods", method = RequestMethod.POST)
    @ResponseBody
    public R queryUnInventoriedShelfGoods(Integer shelfId, Integer inventoryType,
                                          String inventoryDate, String inventoryWeek, String inventoryMonth,
                                          Integer page, Integer limit) {
        // 1. 参数校验
        if (shelfId == null) {
            return R.error("货架ID不能为空");
        }
        if (inventoryType == null || (inventoryType != 1 && inventoryType != 2 && inventoryType != 3)) {
            return R.error("盘库类型无效，必须是 1(日)、2(周) 或 3(月)");
        }

        // 2. 校验周期参数
        if (inventoryType == 1) {
            if (inventoryDate == null || inventoryDate.trim().isEmpty()) {
                return R.error("日盘库必须提供盘库日期");
            }
        } else if (inventoryType == 2) {
            if (inventoryWeek == null || inventoryWeek.trim().isEmpty()) {
                return R.error("周盘库必须提供盘库周数");
            }
        } else if (inventoryType == 3) {
            if (inventoryMonth == null || inventoryMonth.trim().isEmpty()) {
                return R.error("月盘库必须提供盘库月份");
            }
        }

        // 3. 设置默认分页参数
        if (page == null || page < 1) {
            page = 1;
        }
        if (limit == null || limit < 1) {
            limit = 15;
        }

        // 4. 计算偏移量
        int offset = (page - 1) * limit;

        // 5. 构建查询参数
        Map<String, Object> map = new HashMap<>();
        map.put("shelfId", shelfId);
        map.put("inventoryType", inventoryType);
        if (inventoryDate != null) {
            map.put("inventoryDate", inventoryDate.trim());
        }
        if (inventoryWeek != null) {
            map.put("inventoryWeek", inventoryWeek.trim());
        }
        if (inventoryMonth != null) {
            map.put("inventoryMonth", inventoryMonth.trim());
        }
        map.put("offset", offset);
        map.put("limit", limit);

        logger.info("[queryUnInventoriedShelfGoods] 查询未盘库货架商品，参数: shelfId={}, inventoryType={}, inventoryDate={}, inventoryWeek={}, inventoryMonth={}, page={}, limit={}",
                shelfId, inventoryType, inventoryDate, inventoryWeek, inventoryMonth, page, limit);

        // 6. 查询总数（只查询库存数量大于0的货架商品）
        int total = nxDisGoodsShelfGoodsService.queryUnInventoriedShelfGoodsCount(map);
        logger.info("[queryUnInventoriedShelfGoods] 查询总数完成，总记录数: {} (仅包含库存数量>0的货架商品)", total);

        // 7. 查询分页数据（SQL中已计算总库存重量，只返回库存数量大于0的货架商品）
        List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsList = nxDisGoodsShelfGoodsService.queryUnInventoriedShelfGoods(map);
        logger.info("[queryUnInventoriedShelfGoods] 查询分页数据完成，返回记录数: {} (仅包含库存数量>0的货架商品)",
                shelfGoodsList != null ? shelfGoodsList.size() : 0);
        
        // 8. 打印每个商品的库存信息（用于调试）
        if (shelfGoodsList != null && !shelfGoodsList.isEmpty()) {
            logger.debug("[queryUnInventoriedShelfGoods] 商品详情:");
            for (NxDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsList) {
                String goodsName = shelfGoods.getNxDistributerGoodsEntity() != null 
                        ? shelfGoods.getNxDistributerGoodsEntity().getNxDgGoodsName() : "未知";
                String totalRestWeight = shelfGoods.getTotalRestWeight() != null 
                        ? shelfGoods.getTotalRestWeight() : "0";
                logger.debug("  - 商品ID: {}, 商品名称: {}, 总库存重量: {}", 
                        shelfGoods.getNxDistributerGoodsShelfGoodsId(), goodsName, totalRestWeight);
            }
        }

        // 9. 使用 PageUtils 返回分页结果
        PageUtils pageUtil = new PageUtils(shelfGoodsList, total, limit, page);
        return R.ok().put("page", pageUtil);
    }

}

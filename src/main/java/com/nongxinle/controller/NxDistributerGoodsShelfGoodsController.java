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
        for (int i = 0; i < goodsList.size(); i++) {
            if (Objects.equals(goodsList.get(i).getNxDistributerGoodsShelfGoodsId(), shelfGoodsId)) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex == -1) {
            return R.error("货架商品不存在");
        }

        TreeSet<Integer> boundaries = collectLayerBoundaryIndices(goodsList);
        boundaries.add(targetIndex);
        applySegmentLayersToShelf(goodsList, boundaries);

        Integer assignedLayer = goodsList.get(targetIndex).getNxDgsgShelfLayer();
        Map<String, Object> data = buildLayerResponse(goodsList, shelfId, assignedLayer);
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
        for (int i = 0; i < goodsList.size(); i++) {
            if (Objects.equals(goodsList.get(i).getNxDistributerGoodsShelfGoodsId(), shelfGoodsId)) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex == -1) {
            return R.error("货架商品不存在");
        }
        if (!isLayerEndMarker(goodsList.get(targetIndex))) {
            return R.ok().put("message", "该商品未设置层标志，无需取消");
        }

        TreeSet<Integer> boundaries = collectLayerBoundaryIndices(goodsList);
        if (!boundaries.remove(targetIndex)) {
            return R.ok().put("message", "该商品未设置层标志，无需取消");
        }

        applySegmentLayersToShelf(goodsList, boundaries);
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





    /**
     * 仅查询货架有几层，不查商品明细。返回层号列表 1、2、3…
     * 有效层号规则与 {@link #reapplyShelfLayerFieldsForShelf} 一致：列为 null 时继承上一行，首行 null 视为 1。
     */
    @RequestMapping(value = "/getShelfLayerlist/{shelfId}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public R getShelfLayerlist(@PathVariable Integer shelfId) {
        if (shelfId == null) {
            return R.error("货架ID不能为空");
        }
        List<NxDistributerGoodsShelfGoodsEntity> list = nxDisGoodsShelfGoodsService.queryShelfGoodsBasic(shelfId);
        if (list == null) {
            list = Collections.emptyList();
        }
        TreeSet<Integer> layerNumbers = new TreeSet<>();
        int lastEffective = 1;
        for (NxDistributerGoodsShelfGoodsEntity row : list) {
            Integer L = row.getNxDgsgShelfLayer();
            if (L != null) {
                lastEffective = L;
            }
            layerNumbers.add(lastEffective);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("shelfId", shelfId);
        data.put("totalLayers", layerNumbers.size());
        data.put("layerNumbers", new ArrayList<>(layerNumbers));
        return R.ok().put("data", data);
    }

    /**
     * 按货架 + 层级查询该层货架商品（含商品名称等），顺序与架上 sort 一致。
     * 层级判定与 {@link #getShelfLayerlist} 的有效层号一致。
     */
    @RequestMapping(value = "/getShelfGoodsByLayer", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public R getShelfGoodsByLayer(Integer shelfId, Integer layer) {
        if (shelfId == null) {
            return R.error("货架ID不能为空");
        }
        if (layer == null) {
            return R.error("层级不能为空");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("shelfId", shelfId);
        map.put("status", 3);
        List<NxDistributerGoodsShelfGoodsEntity> raw = nxDisGoodsShelfGoodsService.queryShelfForGoodsByParams(map);
        List<NxDistributerGoodsShelfGoodsEntity> list = dedupeShelfGoodsBySort(raw);

        List<Map<String, Object>> goods = new ArrayList<>();
        int lastEffective = 1;
        for (NxDistributerGoodsShelfGoodsEntity row : list) {
            Integer L = row.getNxDgsgShelfLayer();
            if (L != null) {
                lastEffective = L;
            }
            if (!Objects.equals(lastEffective, layer)) {
                continue;
            }
            goods.add(buildShelfGoodsItemMap(row, lastEffective));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("shelfId", shelfId);
        data.put("layer", layer);
        data.put("count", goods.size());
        data.put("goods", goods);
        return R.ok().put("data", data);
    }

    private static Map<String, Object> buildShelfGoodsItemMap(NxDistributerGoodsShelfGoodsEntity row, int effectiveLayer) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("effectiveLayer", effectiveLayer);
        item.put("nxDistributerGoodsShelfGoodsId", row.getNxDistributerGoodsShelfGoodsId());
        item.put("nxDgsgDisGoodsId", row.getNxDgsgDisGoodsId());
        item.put("nxDgsgSort", row.getNxDgsgSort());
        item.put("nxDgsgShelfSort", row.getNxDgsgShelfSort());
        item.put("nxDgsgShelfLayer", row.getNxDgsgShelfLayer());
        item.put("nxDgsgShelfLayerSeq", row.getNxDgsgShelfLayerSeq());
        item.put("nxDgsgShelfLayerLast", row.getNxDgsgShelfLayerLast());
        NxDistributerGoodsEntity ge = row.getNxDistributerGoodsEntity();
        if (ge != null) {
            item.put("nxDgGoodsName", ge.getNxDgGoodsName());
            item.put("nxDgGoodsStandardname", ge.getNxDgGoodsStandardname());
            item.put("nxDgGoodsStandardWeight", ge.getNxDgGoodsStandardWeight());
            item.put("nxDgGoodsFile", ge.getNxDgGoodsFile());
            item.put("nxDgGoodsFileLarge", ge.getNxDgGoodsFileLarge());
        } else {
            item.put("nxDgGoodsName", null);
            item.put("nxDgGoodsStandardname", null);
            item.put("nxDgGoodsStandardWeight", null);
            item.put("nxDgGoodsFile", null);
            item.put("nxDgGoodsFileLarge", null);
        }
        return item;
    }

    /**
     * 与库存等 left join 时可能重复同一货架商品，按 id 保留首次出现后再按 sort 排序。
     */
    private static List<NxDistributerGoodsShelfGoodsEntity> dedupeShelfGoodsBySort(List<NxDistributerGoodsShelfGoodsEntity> raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, NxDistributerGoodsShelfGoodsEntity> byId = new LinkedHashMap<>();
        for (NxDistributerGoodsShelfGoodsEntity e : raw) {
            Integer id = e.getNxDistributerGoodsShelfGoodsId();
            if (id != null) {
                byId.putIfAbsent(id, e);
            }
        }
        List<NxDistributerGoodsShelfGoodsEntity> list = new ArrayList<>(byId.values());
        list.sort(Comparator.comparing(NxDistributerGoodsShelfGoodsEntity::getNxDgsgSort, Comparator.nullsLast(Integer::compareTo)));
        return list;
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

        // 全局 sort 已后移、新行已插入：按当前顺序与层尾标记重算整架的 layer / layerSeq / layerLast（各层内序号与层尾一并纠正）
        reapplyShelfLayerFieldsForShelf(nxDgsgShelfId);

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



    @RequestMapping(value = "/addShelfGoodsOne", method = RequestMethod.POST)
    @ResponseBody
    public R addShelfGoodsOne(@RequestBody NxDistributerGoodsShelfGoodsEntity nxDistributerGoodsShelfGoodsEntity) {

        Integer nxDgsgSort = nxDistributerGoodsShelfGoodsEntity.getNxDgsgSort();
        Integer nxDgsgShelfId = nxDistributerGoodsShelfGoodsEntity.getNxDgsgShelfId();
        Integer nxDgsgDisGoodsId = nxDistributerGoodsShelfGoodsEntity.getNxDgsgDisGoodsId();

        NxDistributerGoodsEntity distributerGoodsEntity = dgService.queryObject(nxDgsgDisGoodsId);
        distributerGoodsEntity.setNxDgPurchaseAuto(-1);
        dgService.update(distributerGoodsEntity);


        return R.ok();
    }

    @RequestMapping(value = "/updateShelfGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updateShelfGoods(@RequestBody NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity) {
        // 1. 获取货架的排序号（赋值给 nxDgsgShelfSort）
        if (shelfGoodsEntity.getNxDgsgShelfId() != null) {
            NxDistributerGoodsShelfEntity shelfEntity = nxDistributerGoodsShelfService.queryObject(shelfGoodsEntity.getNxDgsgShelfId());
            if (shelfEntity != null && shelfEntity.getNxDistributerGoodsShelfSort() != null) {
                // nxDgsgShelfSort = 货架的排序号（固定值，如18）
                shelfGoodsEntity.setNxDgsgShelfSort(shelfEntity.getNxDistributerGoodsShelfSort());
                logger.debug("[updateShelfGoods] 设置货架排序号，货架ID: {}, 货架排序号: {}", 
                        shelfGoodsEntity.getNxDgsgShelfId(), shelfEntity.getNxDistributerGoodsShelfSort());
            } else {
                logger.warn("[updateShelfGoods] 未找到货架信息或货架排序号为空，货架ID: {}", 
                        shelfGoodsEntity.getNxDgsgShelfId());
            }

            // 2. 校验并保持 nxDgsgSort
            // nxDgsgSort 应该是商品在货架内的全局位置（1,2,3...），使用前端传入的值
            // 如果前端没有传，则使用当前最大值+1
            if (shelfGoodsEntity.getNxDgsgSort() == null) {
                Map<String, Object> countMap = new HashMap<>();
                countMap.put("shelfId", shelfGoodsEntity.getNxDgsgShelfId());
                List<NxDistributerGoodsShelfGoodsEntity> existingGoods = nxDisGoodsShelfGoodsService.queryShelfForGoodsByParams(countMap);
                int maxSort = existingGoods.stream()
                    .mapToInt(g -> g.getNxDgsgSort() != null ? g.getNxDgsgSort() : 0)
                    .max()
                    .orElse(0);
                shelfGoodsEntity.setNxDgsgSort(maxSort + 1);
            }
        } else {
            logger.warn("[updateShelfGoods] 货架ID为空，无法更新货架商品");
            return R.error("货架ID不能为空");
        }

        nxDisGoodsShelfGoodsService.update(shelfGoodsEntity);

        // 3. 重新计算该货架所有商品的层级、层内序号、层尾
        reapplyShelfLayerFieldsForShelf(shelfGoodsEntity.getNxDgsgShelfId());

        return R.ok();
    }





    /**
     * 批量更新架上顺序：先合并请求到该架全量数据，再按<strong>层块 + 层内 {@code nxDgsgShelfLayerSeq} + {@code nxDgsgSort}</strong>
     * 重算全局 {@code nxDgsgSort}/{@code nxDgsgShelfSort} 为 1..n（不再走「仅按 sort 排序」分支，否则只改了 seq 未带 layer 字段时顺序不会变）。
     */
    @RequestMapping(value = "/updateShelfGoodsSort", method = RequestMethod.POST)
    @ResponseBody
    public R updateShelfGoodsSort(@RequestBody List<NxDistributerGoodsShelfGoodsEntity> shelfGoodsList) {
        if (shelfGoodsList == null || shelfGoodsList.isEmpty()) {
            return R.ok();
        }
        Set<Integer> touchedShelfIds = new LinkedHashSet<>();
        for (NxDistributerGoodsShelfGoodsEntity shelfGoods : shelfGoodsList) {
            Integer sid = shelfGoods.getNxDgsgShelfId();
            if (sid == null && shelfGoods.getNxDistributerGoodsShelfGoodsId() != null) {
                NxDistributerGoodsShelfGoodsEntity dbRow = nxDisGoodsShelfGoodsService.queryObject(
                        shelfGoods.getNxDistributerGoodsShelfGoodsId());
                if (dbRow != null && dbRow.getNxDgsgShelfId() != null) {
                    sid = dbRow.getNxDgsgShelfId();
                    shelfGoods.setNxDgsgShelfId(sid);
                    logger.info("[updateShelfGoodsSort] 由 shelfGoodsId={} 补全 nxDgsgShelfId={}",
                            shelfGoods.getNxDistributerGoodsShelfGoodsId(), sid);
                } else {
                    logger.warn("[updateShelfGoodsSort] shelfGoodsId={} 在库中不存在或无货架ID，无法补全 nxDgsgShelfId",
                            shelfGoods.getNxDistributerGoodsShelfGoodsId());
                }
            }
            if (sid != null) {
                touchedShelfIds.add(sid);
            } else {
                logger.warn("[updateShelfGoodsSort] 单条缺少 nxDgsgShelfId 且无法从 nxDistributerGoodsShelfGoodsId 推断，本条不参与重排");
            }
        }
        if (touchedShelfIds.isEmpty()) {
            logger.warn("[updateShelfGoodsSort] bodySize={} 但未解析到任何货架ID，未执行重排；请传 nxDgsgShelfId 或有效的 nxDistributerGoodsShelfGoodsId",
                    shelfGoodsList.size());
            return R.error("未解析到货架ID：每条请传 nxDgsgShelfId，或传库中存在的 nxDistributerGoodsShelfGoodsId");
        }
        logger.info("[updateShelfGoodsSort] 开始 bodySize={} touchedShelfIds={}", shelfGoodsList.size(), touchedShelfIds);
        for (Integer shelfId : touchedShelfIds) {
            reorderShelfGoodsGlobalSort(shelfId, shelfGoodsList);
            reapplyShelfLayerFieldsForShelf(shelfId);
        }
        logger.info("[updateShelfGoodsSort] 结束 touchedShelfIds={}", touchedShelfIds);
        return R.ok();
    }

    /**
     * 合并 patch（层、层内序号、全局 sort）到库中该架全量行，再得到新顺序并重写 1..n。
     */
    private void reorderShelfGoodsGlobalSort(Integer shelfId, List<NxDistributerGoodsShelfGoodsEntity> incomingAll) {
        List<NxDistributerGoodsShelfGoodsEntity> fullList = nxDisGoodsShelfGoodsService.queryShelfGoodsBasic(shelfId);
        if (fullList == null || fullList.isEmpty()) {
            logger.info("[updateShelfGoodsSort] shelfId={} 无货架商品，跳过", shelfId);
            return;
        }
        Map<Integer, NxDistributerGoodsShelfGoodsEntity> patchById = new HashMap<>();
        for (NxDistributerGoodsShelfGoodsEntity e : incomingAll) {
            if (!Objects.equals(shelfId, e.getNxDgsgShelfId()) || e.getNxDistributerGoodsShelfGoodsId() == null) {
                continue;
            }
            patchById.put(e.getNxDistributerGoodsShelfGoodsId(), e);
        }
        logger.info("[updateShelfGoodsSort] shelfId={} DB行数={} patch条数={} patchIds={}",
                shelfId, fullList.size(), patchById.size(), patchById.keySet());
        for (NxDistributerGoodsShelfGoodsEntity e : incomingAll) {
            if (!Objects.equals(shelfId, e.getNxDgsgShelfId()) || e.getNxDistributerGoodsShelfGoodsId() == null) {
                continue;
            }
            logger.info("[updateShelfGoodsSort] PATCH shelfId={} shelfGoodsId={} layer={} layerSeq={} sort={} shelfSort={}",
                    shelfId, e.getNxDistributerGoodsShelfGoodsId(), e.getNxDgsgShelfLayer(), e.getNxDgsgShelfLayerSeq(),
                    e.getNxDgsgSort(), e.getNxDgsgShelfSort());
        }
        logShelfGoodsRows("DB顺序(merge前)", shelfId, fullList);
        List<NxDistributerGoodsShelfGoodsEntity> working = new ArrayList<>(fullList);
        for (NxDistributerGoodsShelfGoodsEntity row : working) {
            NxDistributerGoodsShelfGoodsEntity p = patchById.get(row.getNxDistributerGoodsShelfGoodsId());
            if (p == null) {
                continue;
            }
            if (p.getNxDgsgShelfLayer() != null) {
                row.setNxDgsgShelfLayer(p.getNxDgsgShelfLayer());
            }
            if (p.getNxDgsgShelfLayerSeq() != null) {
                row.setNxDgsgShelfLayerSeq(p.getNxDgsgShelfLayerSeq());
            }
            if (p.getNxDgsgSort() != null) {
                row.setNxDgsgSort(p.getNxDgsgSort());
            }
        }
        logShelfGoodsRows("merge后(层桶排序前)", shelfId, working);
        List<NxDistributerGoodsShelfGoodsEntity> newOrder = orderShelfGoodsByLayerBlocksThenSeq(shelfId, working);
        logShelfGoodsRows("重排后(写库前 行序即目标1..n 当前sort字段仍为旧值)", shelfId, newOrder);
        int sort = 1;
        for (NxDistributerGoodsShelfGoodsEntity row : newOrder) {
            boolean sortChanged = !Objects.equals(row.getNxDgsgSort(), sort);
            boolean shelfSortChanged = !Objects.equals(row.getNxDgsgShelfSort(), sort);
            if (sortChanged || shelfSortChanged) {
                logger.info("[updateShelfGoodsSort] UPDATE shelfId={} shelfGoodsId={} disGoodsId={} sort {}->{}, shelfSort {}->{}",
                        shelfId, row.getNxDistributerGoodsShelfGoodsId(), row.getNxDgsgDisGoodsId(),
                        row.getNxDgsgSort(), sort, row.getNxDgsgShelfSort(), sort);
                row.setNxDgsgSort(sort);
                row.setNxDgsgShelfSort(sort);
                nxDisGoodsShelfGoodsService.update(row);
            } else {
                logger.debug("[updateShelfGoodsSort] 跳过(已是目标序) shelfId={} shelfGoodsId={} sort={}",
                        shelfId, row.getNxDistributerGoodsShelfGoodsId(), sort);
            }
            sort++;
        }
    }

    private void logShelfGoodsRows(String phase, Integer shelfId, List<NxDistributerGoodsShelfGoodsEntity> list) {
        if (list == null) {
            logger.info("[updateShelfGoodsSort] {} shelfId={} list=null", phase, shelfId);
            return;
        }
        logger.info("[updateShelfGoodsSort] {} shelfId={} count={}", phase, shelfId, list.size());
        for (int i = 0; i < list.size(); i++) {
            NxDistributerGoodsShelfGoodsEntity r = list.get(i);
            logger.info("[updateShelfGoodsSort] {} [{}] shelfGoodsId={} disGoodsId={} layer={} layerSeq={} layerLast={} sort={} shelfSort={}",
                    phase, i, r.getNxDistributerGoodsShelfGoodsId(), r.getNxDgsgDisGoodsId(),
                    r.getNxDgsgShelfLayer(), r.getNxDgsgShelfLayerSeq(), r.getNxDgsgShelfLayerLast(),
                    r.getNxDgsgSort(), r.getNxDgsgShelfSort());
        }
    }

    /**
     * 按当前行顺序得到有效层号，层块顺序随列表中首次出现的层号；层内先 {@code nxDgsgShelfLayerSeq}（null 靠后）再 {@code nxDgsgSort}。
     */
    private List<NxDistributerGoodsShelfGoodsEntity> orderShelfGoodsByLayerBlocksThenSeq(Integer shelfId,
                                                                                         List<NxDistributerGoodsShelfGoodsEntity> working) {
        int n = working.size();
        int[] effectiveLayer = new int[n];
        int lastLayer = 1;
        for (int i = 0; i < n; i++) {
            Integer L = working.get(i).getNxDgsgShelfLayer();
            if (L != null) {
                lastLayer = L;
            }
            effectiveLayer[i] = lastLayer;
        }
        LinkedHashMap<Integer, List<Integer>> layerToIndices = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            int layerKey = effectiveLayer[i];
            layerToIndices.computeIfAbsent(layerKey, k -> new ArrayList<>()).add(i);
        }
        logger.info("[updateShelfGoodsSort] 层桶(排序前) shelfId={} effLayer逐行={} 桶keys顺序={}",
                shelfId, Arrays.toString(effectiveLayer), layerToIndices.keySet());
        Comparator<Integer> bySeqThenSort = (i1, i2) -> {
            NxDistributerGoodsShelfGoodsEntity a = working.get(i1);
            NxDistributerGoodsShelfGoodsEntity b = working.get(i2);
            Integer sa = a.getNxDgsgShelfLayerSeq();
            Integer sb = b.getNxDgsgShelfLayerSeq();
            if (sa != null && sb != null && !sa.equals(sb)) {
                return sa.compareTo(sb);
            }
            if (sa != null && sb == null) {
                return -1;
            }
            if (sa == null && sb != null) {
                return 1;
            }
            return Comparator.nullsLast(Integer::compareTo).compare(a.getNxDgsgSort(), b.getNxDgsgSort());
        };
        for (Map.Entry<Integer, List<Integer>> e : layerToIndices.entrySet()) {
            List<Integer> indices = e.getValue();
            indices.sort(bySeqThenSort);
            logger.info("[updateShelfGoodsSort] 层桶内排序后 shelfId={} layerKey={} 原下标序列(working索引)={}", shelfId, e.getKey(), indices);
        }
        List<NxDistributerGoodsShelfGoodsEntity> newOrder = new ArrayList<>();
        for (List<Integer> indices : layerToIndices.values()) {
            for (Integer idx : indices) {
                newOrder.add(working.get(idx));
            }
        }
        return newOrder;
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

        reapplyShelfLayerFieldsForShelf(nxDgsgShelfId);

        return R.ok();

    }

    /**
     * 在全局 {@code nx_DGSG_sort} 已变更后，按当前架上顺序重算 layer、layerSeq、layerLast。
     * <p>
     * 使用「按 {@code nxDgsgShelfLayer} 连续相同」分段，而不是仅依赖旧的 {@code nxDgsgShelfLayerLast} 下标。
     * 否则：原层尾（如香油）在插入后仍带 layerLast=1，会把新行（香辣酥）错误地划到下一层。
     * {@code nxDgsgShelfLayer} 为 null 时继承上一行的有效层号，首行 null 视为 1；新插入行应与点击行同层（前端传层号或依赖继承）。
     * </p>
     */
    private void reapplyShelfLayerFieldsForShelf(Integer shelfId) {
        if (shelfId == null) {
            return;
        }
        List<NxDistributerGoodsShelfGoodsEntity> goodsList = nxDisGoodsShelfGoodsService.queryShelfGoodsBasic(shelfId);
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }
        reapplyShelfLayerFieldsContiguousByLayerColumn(goodsList);
    }

    /**
     * 按 sort 顺序下连续相同的「有效层号」分段；段内 layerSeq 从 1 递增，仅段末 layerLast=1。
     * 写入库的 {@code nxDgsgShelfLayer} 使用本段的<strong>业务层号</strong>（effectiveLayer），
     * 而不是自上而下第几段（1、2、3…），否则前面已有一段时，新行即使传 layer=1 也会被写成「第二层」。
     * <p>
     * 新增功能：如果某段内层号不连续（如 1,3 缺少2），会自动重新编号（如 1,3 → 1,2），
     * 保证层号连续。
     * </p>
     */
    private void reapplyShelfLayerFieldsContiguousByLayerColumn(List<NxDistributerGoodsShelfGoodsEntity> goodsList) {
        int n = goodsList.size();
        int[] effectiveLayer = new int[n];
        int lastLayer = 1;
        for (int i = 0; i < n; i++) {
            Integer L = goodsList.get(i).getNxDgsgShelfLayer();
            if (L != null) {
                lastLayer = L;
            }
            effectiveLayer[i] = lastLayer;
            logger.debug("[reapplyShelfLayerFields] i={}, goodsId={}, originalLayer={}, effectiveLayer={}", 
                    i, goodsList.get(i).getNxDistributerGoodsShelfGoodsId(), 
                    goodsList.get(i).getNxDgsgShelfLayer(), effectiveLayer[i]);
        }
        
        // 第一步：收集所有商品的有效层号，检查整体是否连续
        Set<Integer> allUniqueLayers = new TreeSet<>();
        for (int i = 0; i < n; i++) {
            allUniqueLayers.add(effectiveLayer[i]);
        }
        logger.debug("[reapplyShelfLayerFields] 所有层号: {}", allUniqueLayers);
        
        // 检查整体层号是否连续
        boolean allConsecutive = true;
        List<Integer> allLayerList = new ArrayList<>(allUniqueLayers);
        for (int k = 1; k < allLayerList.size(); k++) {
            if (allLayerList.get(k) - allLayerList.get(k - 1) != 1) {
                allConsecutive = false;
                break;
            }
        }
        
        // 如果不连续，创建全局映射（如 1,3 → 1,2）
        Map<Integer, Integer> globalLayerMapping = null;
        if (!allConsecutive && allUniqueLayers.size() > 1) {
            globalLayerMapping = new HashMap<>();
            int mappedLayer = 1;
            for (Integer oldLayer : allLayerList) {
                globalLayerMapping.put(oldLayer, mappedLayer);
                logger.debug("[reapplyShelfLayerFields] 层号映射: {} -> {}", oldLayer, mappedLayer);
                mappedLayer++;
            }
            // 应用全局映射到所有商品
            for (int i = 0; i < n; i++) {
                effectiveLayer[i] = globalLayerMapping.get(effectiveLayer[i]);
            }
            logger.debug("[reapplyShelfLayerFields] 映射后的层号: {}", java.util.Arrays.toString(effectiveLayer));
        }
        
        // 第二步：按层号分段，计算每段的 logicalLayer 和 layerSeq/layerLast
        int segStart = 0;
        for (int i = 1; i <= n; i++) {
            boolean endSegment = (i == n) || (effectiveLayer[i] != effectiveLayer[i - 1]);
            if (endSegment) {
                int baseLayer = effectiveLayer[segStart];
                
                logger.debug("[reapplyShelfLayerFields] 段: segStart={}, i={}, baseLayer={}", segStart, i, baseLayer);
                
                for (int j = segStart; j < i; j++) {
                    int seq = j - segStart + 1;
                    int last = (j == i - 1) ? 1 : 0;
                    int sort = j + 1;
                    logger.debug("[reapplyShelfLayerFields] 商品ID={}, layer={}, layerSeq={}, layerLast={}, sort={}", 
                            goodsList.get(j).getNxDistributerGoodsShelfGoodsId(), baseLayer, seq, last, sort);
                    persistShelfLayerRow(goodsList.get(j), baseLayer, last, seq, sort);
                }
                segStart = i;
            }
        }
    }

    /**
     * 新数据：nxDgsgShelfLayerLast == 1 为层尾；
     * 旧数据（未迁层尾列）：仅 nxDgsgShelfLayer 有值的行视为层尾标记。
     */
    private boolean isLayerEndMarker(NxDistributerGoodsShelfGoodsEntity item) {
        if (item.getNxDgsgShelfLayerLast() != null && item.getNxDgsgShelfLayerLast() == 1) {
            return true;
        }
        return item.getNxDgsgShelfLayerLast() == null && item.getNxDgsgShelfLayer() != null;
    }

    private TreeSet<Integer> collectLayerBoundaryIndices(List<NxDistributerGoodsShelfGoodsEntity> goodsList) {
        TreeSet<Integer> boundaries = new TreeSet<>();
        for (int i = 0; i < goodsList.size(); i++) {
            if (isLayerEndMarker(goodsList.get(i))) {
                boundaries.add(i);
            }
        }
        return boundaries;
    }

    /**
     * 按层尾下标分段：每段内行共享同一 nxDgsgShelfLayer，仅段末行 nxDgsgShelfLayerLast=1。
     * 无层尾时：整架一层，最后一行层尾=1。
     */
    private void applySegmentLayersToShelf(List<NxDistributerGoodsShelfGoodsEntity> goodsList,
                                           TreeSet<Integer> boundaryIndices) {
        int n = goodsList.size();
        if (n == 0) {
            return;
        }
        if (boundaryIndices == null || boundaryIndices.isEmpty()) {
            for (int i = 0; i < n; i++) {
                persistShelfLayerRow(goodsList.get(i), 1, i == n - 1 ? 1 : 0, i + 1, i + 1);
            }
            return;
        }
        int layerNo = 0;
        int segmentStart = 0;
        for (int boundary : boundaryIndices) {
            if (boundary < segmentStart || boundary >= n) {
                continue;
            }
            layerNo++;
            for (int i = segmentStart; i <= boundary; i++) {
                int seq = i - segmentStart + 1;
                persistShelfLayerRow(goodsList.get(i), layerNo, i == boundary ? 1 : 0, seq, i + 1);
            }
            segmentStart = boundary + 1;
        }
        if (segmentStart < n) {
            layerNo++;
            for (int i = segmentStart; i < n; i++) {
                int seq = i - segmentStart + 1;
                persistShelfLayerRow(goodsList.get(i), layerNo, i == n - 1 ? 1 : 0, seq, i + 1);
            }
        }
    }

    private void persistShelfLayerRow(NxDistributerGoodsShelfGoodsEntity item, int layer, int layerLast, int layerSeq, int sort) {
        Integer id = item.getNxDistributerGoodsShelfGoodsId();
        Integer oldL = item.getNxDgsgShelfLayer();
        Integer oldLast = item.getNxDgsgShelfLayerLast();
        Integer oldSeq = item.getNxDgsgShelfLayerSeq();
        Integer oldSort = item.getNxDgsgSort();
        if (!Objects.equals(oldL, layer) || !Objects.equals(oldLast, layerLast) || !Objects.equals(oldSeq, layerSeq) || !Objects.equals(oldSort, sort)) {
            nxDisGoodsShelfGoodsService.updateShelfLayerFields(id, layer, layerLast, layerSeq, sort);
            item.setNxDgsgShelfLayer(layer);
            item.setNxDgsgShelfLayerLast(layerLast);
            item.setNxDgsgShelfLayerSeq(layerSeq);
            item.setNxDgsgSort(sort);
        }
    }

    private Map<String, Object> buildLayerResponse(List<NxDistributerGoodsShelfGoodsEntity> goodsList,
                                                   Integer shelfId,
                                                   Integer assignedLayer) {
        Map<String, Object> data = new HashMap<>();
        data.put("shelfId", shelfId);
        data.put("assignedLayer", assignedLayer);

        int maxLayer = 0;
        List<Map<String, Object>> layerList = new ArrayList<>();
        for (NxDistributerGoodsShelfGoodsEntity item : goodsList) {
            if (item.getNxDgsgShelfLayer() != null) {
                maxLayer = Math.max(maxLayer, item.getNxDgsgShelfLayer());
            }
            if (item.getNxDgsgShelfLayerLast() != null && item.getNxDgsgShelfLayerLast() == 1) {
                Map<String, Object> row = new HashMap<>();
                row.put("shelfGoodsId", item.getNxDistributerGoodsShelfGoodsId());
                row.put("layer", item.getNxDgsgShelfLayer());
                row.put("layerLast", 1);
                layerList.add(row);
            }
        }
        data.put("totalLayers", maxLayer);
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

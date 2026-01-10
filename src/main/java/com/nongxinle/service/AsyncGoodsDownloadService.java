package com.nongxinle.service;

import com.nongxinle.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.nongxinle.utils.DateUtils.formatWhatDay;
import static com.nongxinle.utils.DateUtils.formatWhatDate;

/**
 * 异步商品下载服务
 * 用于在配送商注册时异步下载所有商品
 */
@Service
public class AsyncGoodsDownloadService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncGoodsDownloadService.class);
    
    // 使用固定大小的线程池，避免创建过多线程
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    @Autowired
    private NxGoodsService nxGoodsService;
    
    @Autowired
    private com.nongxinle.controller.NxDistributerGoodsController nxDistributerGoodsController;

    /**
     * 异步下载指定曾祖父ID下的所有商品
     * @param distributerId 配送商ID
     * @param greatGrandIds 曾祖父ID列表
     */
    public void downloadGoodsByGreatGrandIdsAsync(Integer distributerId, List<Integer> greatGrandIds) {
        executorService.submit(() -> {
            try {
                logger.info("开始异步下载商品，配送商ID: {}, 曾祖父ID列表: {}", distributerId, greatGrandIds);
                
                if (greatGrandIds == null || greatGrandIds.isEmpty()) {
                    logger.warn("曾祖父ID列表为空，配送商ID: {}", distributerId);
                    return;
                }
                
                // 查询指定曾祖父ID下的所有商品（只查询level=3的商品）
                Map<String, Object> queryMap = new HashMap<>();
                queryMap.put("level", 3);
                queryMap.put("greatGrandIds", greatGrandIds);
                queryMap.put("isHidden", 0);
                List<NxGoodsEntity> allGoods = nxGoodsService.queryNxGoodsByParams(queryMap);
                
                if (allGoods == null || allGoods.isEmpty()) {
                    logger.warn("未找到任何商品，配送商ID: {}, 曾祖父ID列表: {}", distributerId, greatGrandIds);
                    return;
                }
                
                int batchSize = 50; // 每批处理50个商品
                int total = allGoods.size();
                int successCount = 0;
                int failCount = 0;
                List<Integer> failedGoodsIds = new ArrayList<>(); // 记录失败的商品ID列表
                
                logger.info("找到 {} 个商品，开始分批下载，配送商ID: {}, 曾祖父ID列表: {}", total, distributerId, greatGrandIds);
                
                // 分批处理，避免一次性处理过多商品
                for (int i = 0; i < total; i += batchSize) {
                    int end = Math.min(i + batchSize, total);
                    List<NxGoodsEntity> batch = allGoods.subList(i, end);
                    
                    logger.info("处理第 {}/{} 批商品（{} - {}），配送商ID: {}", 
                            (i / batchSize + 1), (total + batchSize - 1) / batchSize, i + 1, end, distributerId);
                    
                    for (NxGoodsEntity goods : batch) {
                        Integer goodsId = goods.getNxGoodsId();
                        String goodsName = goods.getNxGoodsName();
                        try {
                            // 调用下载商品的方法
                            downloadSingleGoods(distributerId, goodsId);
                            successCount++;
                            
                            // 每10个商品休息一下，避免数据库压力过大
                            if (successCount % 10 == 0) {
                                Thread.sleep(100); // 休息100ms
                            }
                        } catch (Exception e) {
                            failCount++;
                            failedGoodsIds.add(goodsId);
                            logger.error("下载商品失败，商品ID: {}, 商品名称: {}, 配送商ID: {}, 错误: {}", 
                                    goodsId, goodsName, distributerId, e.getMessage(), e);
                        }
                    }
                    
                    // 每批之间休息一下
                    if (end < total) {
                        Thread.sleep(500); // 休息500ms
                    }
                }
                
                logger.info("商品下载完成，配送商ID: {}, 曾祖父ID列表: {}, 总数: {}, 成功: {}, 失败: {}", 
                        distributerId, greatGrandIds, total, successCount, failCount);
                if (!failedGoodsIds.isEmpty()) {
                    logger.warn("失败的商品ID列表: {}", failedGoodsIds);
                }
                        
            } catch (Exception e) {
                logger.error("异步下载商品异常，配送商ID: {}, 曾祖父ID列表: {}", distributerId, greatGrandIds, e);
            }
        });
    }
    
    /**
     * 下载单个商品
     * 注意：新注册的配送商不需要检查商品是否存在，直接下载即可
     * @param distributerId 配送商ID
     * @param goodsId 商品ID
     */
    private void downloadSingleGoods(Integer distributerId, Integer goodsId) {
        try {
            // 创建配送商商品实体
            NxDistributerGoodsEntity distributerGoods = new NxDistributerGoodsEntity();
            distributerGoods.setNxDgNxGoodsId(goodsId);
            distributerGoods.setNxDgDistributerId(distributerId);
            
            // 调用Controller的下载方法（跳过检查，适用于全量下载）
            com.nongxinle.utils.R result = nxDistributerGoodsController.downloadGoodsForDistributerWithoutCheck(distributerGoods);
            
            // R类继承自HashMap，使用get方法获取值
            Object codeObj = result.get("code");
            Integer code = codeObj != null ? (Integer) codeObj : 0;
            String msg = (String) result.get("msg");
            
            if (code != null && code != 0) {
                // 如果是"已经下载"的错误，可以忽略（虽然理论上不应该发生）
                if (code == -1 && "已经下载".equals(msg)) {
                    logger.debug("商品已存在，跳过下载，商品ID: {}, 配送商ID: {}", goodsId, distributerId);
                    return;
                }
                throw new RuntimeException("下载商品失败: " + (msg != null ? msg : "未知错误"));
            }
            
        } catch (Exception e) {
            logger.error("下载单个商品失败，商品ID: {}, 配送商ID: {}", goodsId, distributerId, e);
            throw new RuntimeException("下载商品失败: " + e.getMessage(), e);
        }
    }
}


package com.nongxinle.controller;

/**
 * @author lpy
 * @date 06-24 11:45
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import org.apache.poi.util.Internal;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nongxinle.utils.PageUtils;
import com.nongxinle.utils.R;
import com.nongxinle.utils.UploadFile;
import com.nongxinle.utils.DateUtils;
import com.nongxinle.utils.PinYin4jUtils;

import javax.servlet.http.HttpSession;
import javax.swing.*;

import static com.nongxinle.utils.DateUtils.*;
import static com.nongxinle.utils.GbTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.*;
import static com.nongxinle.utils.NxDistributerTypeUtils.getNxDisPurchaseGoodsFinishBuy;
import static com.nongxinle.utils.PinYin4jUtils.hanziToPinyin;


@RestController
@RequestMapping("api/nxdistributerpurchasegoods")
public class NxDistributerPurchaseGoodsController {
    private static final Logger logger = LoggerFactory.getLogger(NxDistributerPurchaseGoodsController.class);
    
    @Autowired
    private NxDistributerPurchaseGoodsService nxDisPurcGoodsService;

    @Autowired
    private NxDistributerPurchaseBatchService nxDPBService;

    @Autowired
    private NxDepartmentOrdersService nxDepartmentOrdersService;

    @Autowired
    private GbDepartmentOrdersService gbDepartmentOrdersService;

    @Autowired
    private NxDistributerGoodsService nxDgService;
    @Autowired
    private NxDistributerFatherGoodsService nxDistributerFatherGoodsService;
    @Autowired
    private NxRestrauntOrdersService nxRestrauntOrdersService;
    @Autowired
    private NxDistributerWeightService nxDistributerWeightService;
    @Autowired
    private GbDepartmentDisGoodsService gbDepartmentDisGoodsService;
    @Autowired
    private NxCommunityGoodsService nxCommunityGoodsService;
    @Autowired
    private NxDistributerGoodsShelfStockService shelfStockService;
    @Autowired
    private NxDistributerGoodsShelfGoodsService shelfGoodsService;
    @Autowired
    private NxDistributerPurchaseBatchService batchService;
    @Autowired
    private NxDistributerGoodsShelfStockReduceService nxDisGoodsShelfStockReduceService;



    @RequestMapping(value = "/supplierGivePurchaseGoodsData", method = RequestMethod.POST)
    @ResponseBody
    public R supplierGivePurchaseGoodsData (Integer id, String quantity,  String price, String subtotal ) {

        System.out.println("iddididiid" +  id);
        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDisPurcGoodsService.queryObject(id);
        purchaseGoodsEntity.setNxDpgBuyQuantity(quantity);
        purchaseGoodsEntity.setNxDpgBuyPrice(price);
        purchaseGoodsEntity.setNxDpgBuySubtotal(subtotal);
        if(new BigDecimal(subtotal).compareTo(BigDecimal.ZERO) > 0){
            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsIsPurchase());
        }
        nxDisPurcGoodsService.update(purchaseGoodsEntity);
        return R.ok();
    }

    @RequestMapping(value = "/disReceiveStock", method = RequestMethod.POST)
    @ResponseBody
    public R disReceiveStock (Integer purGoodsId, Integer userId) {

        NxDistributerGoodsShelfStockEntity stockEntity = new NxDistributerGoodsShelfStockEntity();
        stockEntity.setNxDgssReceiveUserId(userId);

        stockEntity.setNxDgssNxPurGoodsId(purGoodsId);

        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDisPurcGoodsService.queryObject(purGoodsId);

        stockEntity.setNxDgssNxDistributerId(purchaseGoodsEntity.getNxDpgDistributerId());
        stockEntity.setNxDgssNxDisGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
        stockEntity.setNxDgssPrice(purchaseGoodsEntity.getNxDpgBuyPrice());
        stockEntity.setNxDgssWeight(purchaseGoodsEntity.getNxDpgBuyQuantity());
        stockEntity.setNxDgssSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
        stockEntity.setNxDgssRestWeight(purchaseGoodsEntity.getNxDpgBuyQuantity());
        stockEntity.setNxDgssRestSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
        stockEntity.setNxDgssStatus(0);

        stockEntity.setNxDgssDate(formatWhatDay(0));
        stockEntity.setNxDgssMonth(formatWhatMonth(0));
        stockEntity.setNxDgssYear(formatWhatYear(0));
        stockEntity.setNxDgssProduceWeight("0");
        stockEntity.setNxDgssProduceSubtotal("0");
        stockEntity.setNxDgssLossWeight("0");
        stockEntity.setNxDgssLossSubtotal("0");
        stockEntity.setNxDgssWasteWeight("0");
        stockEntity.setNxDgssWasteSubtotal("0");
        stockEntity.setNxDgssReturnWeight("0");
        stockEntity.setNxDgssReturnSubtotal("0");
        stockEntity.setNxDgssReceiveUserId(userId);
        if(purchaseGoodsEntity.getNxDpgExpectPrice() != null){
            stockEntity.setNxDgssSellingPrice(purchaseGoodsEntity.getNxDpgExpectPrice());
        }
        shelfStockService.save(stockEntity);


        purchaseGoodsEntity.setNxDpgStatus(4);
        nxDisPurcGoodsService.update(purchaseGoodsEntity);

        return R.ok();
    }


    @RequestMapping(value = "/staffApplyPurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R staffApplyPurGoods (@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
        purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
        purchaseGoodsEntity.setNxDpgOrdersAmount(0);
        purchaseGoodsEntity.setNxDpgFinishAmount(0);
        purchaseGoodsEntity.setNxDpgInputType(0);
        System.out.println("udduuduududduddid");
        nxDisPurcGoodsService.save(purchaseGoodsEntity);


        Map<String, Object> map = new HashMap<>();
        map.put("disGoodsId", purchaseGoodsEntity.getNxDpgDisGoodsId());
        map.put("restWeight", 0);
        List<NxDistributerGoodsShelfStockEntity> stockEntities = shelfStockService.queryShelfStockListByParams(map);
        if(stockEntities.size() > 0 && purchaseGoodsEntity.getNxDpgStockRestWeight() != null){
            //根据这个剩余库存总数量，修改批次的库存数量，根据批次的入库时间，用先进先出的方法自动减去库存
            BigDecimal restWeight = new BigDecimal(purchaseGoodsEntity.getNxDpgStockRestWeight());
            if (restWeight.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("[staffApplyPurGoods] 开始自动扣减库存 restWeight=" + restWeight
                        + ", purchaseGoodsId=" + purchaseGoodsEntity.getNxDistributerPurchaseGoodsId()
                        + ", 批次数量=" + stockEntities.size());
                BigDecimal remainingWeight = restWeight;
                BigDecimal totalCost = BigDecimal.ZERO;

                for (NxDistributerGoodsShelfStockEntity stockEntity : stockEntities) {
                    if (remainingWeight.compareTo(BigDecimal.ZERO) <= 0) {
                        break;
                    }

                    BigDecimal stockRestWeight = new BigDecimal(stockEntity.getNxDgssRestWeight());
                    if (stockRestWeight.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    BigDecimal stockPrice = new BigDecimal(stockEntity.getNxDgssPrice());
                    BigDecimal deductWeight = remainingWeight.compareTo(stockRestWeight) <= 0 ? remainingWeight : stockRestWeight;

                    System.out.println("[staffApplyPurGoods] 扣减批次 stockId=" + stockEntity.getNxDistributerGoodsShelfStockId()
                            + ", 当前剩余=" + stockRestWeight + ", 单价=" + stockPrice + ", 本次扣减=" + deductWeight);

                    BigDecimal batchCost = deductWeight.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    totalCost = totalCost.add(batchCost);

                    BigDecimal newRestWeight = stockRestWeight.subtract(deductWeight);
                    stockEntity.setNxDgssRestWeight(newRestWeight.toString());
                    BigDecimal newRestSubtotal = newRestWeight.multiply(stockPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    stockEntity.setNxDgssRestSubtotal(newRestSubtotal.toString());
                    shelfStockService.update(stockEntity);

                    NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
                    reduceEntity.setNxDgssrNxDistributerId(purchaseGoodsEntity.getNxDpgDistributerId());
                    reduceEntity.setNxDgssrNxDisGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
                    reduceEntity.setNxDgssrNxDisGoodsFatherId(stockEntity.getNxDgssNxDisGoodsFatherId());
                    reduceEntity.setNxDgssrNxStockId(stockEntity.getNxDistributerGoodsShelfStockId());
                    reduceEntity.setNxDgssrDate(formatWhatDay(0));
                    reduceEntity.setNxDgssrWeek(getWeek(0));
                    reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
                    reduceEntity.setNxDgssrFullTime(formatWhatYearDayTime(0));
                    reduceEntity.setNxDgssrType(1); // 1 = 正常出库
                    Integer reduceUserId = purchaseGoodsEntity.getNxDpgTypeAddUserId() != null
                            ? purchaseGoodsEntity.getNxDpgTypeAddUserId()
                            : purchaseGoodsEntity.getNxDpgPurUserId();
                    reduceEntity.setNxDgssrDoUserId(reduceUserId);
                    reduceEntity.setNxDgssrCostWeight(deductWeight.toString());
                    reduceEntity.setNxDgssrCostSubtotal(batchCost.toString());
                    reduceEntity.setNxDgssrProduceWeight(deductWeight.toString());
                    reduceEntity.setNxDgssrProduceSubtotal(batchCost.toString());
                    reduceEntity.setNxDgssrNxPurGoodsId(stockEntity.getNxDgssNxPurGoodsId());
                    reduceEntity.setNxDgssrStatus(0);
                    nxDisGoodsShelfStockReduceService.save(reduceEntity);

                    remainingWeight = remainingWeight.subtract(deductWeight);
                    System.out.println("[staffApplyPurGoods] 扣减完成 stockId=" + stockEntity.getNxDistributerGoodsShelfStockId()
                            + ", 新剩余=" + newRestWeight + ", remaining=" + remainingWeight);
                }

                if (remainingWeight.compareTo(BigDecimal.ZERO) > 0) {
                    System.out.println("[staffApplyPurGoods] 库存不足，尚需 " + remainingWeight);
                    purchaseGoodsEntity.setNxDpgStockRestWeight(remainingWeight.toString());
                    nxDisPurcGoodsService.update(purchaseGoodsEntity);
                    return R.error("库存不足，还差" + remainingWeight.toString());
                } else {
                    System.out.println("[staffApplyPurGoods] 扣减完成，总成本=" + totalCost + ", 扣减重量=" + restWeight);
                    purchaseGoodsEntity.setNxDpgStockRestWeight("0");
                    nxDisPurcGoodsService.update(purchaseGoodsEntity);
                    Map<String, Object> result = new HashMap<>();
                    result.put("reduceWeight", restWeight.toString());
                    result.put("reduceCost", totalCost.toString());
                    return R.ok().put("stockReduce", result);
                }
            }
        } else {
            System.out.println("[staffApplyPurGoods] 无库存批次可扣减或未提供restWeight，purchaseGoodsId="
                    + purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
        }

        return R.ok().put("data",purchaseGoodsEntity);
    }




    @RequestMapping(value = "/disSavePurGoodsSaveStock", method = RequestMethod.POST)
    @ResponseBody
    public R disSavePurGoodsSaveStock (@RequestBody Map<String, Object> requestMap) {
        System.out.println("[disSavePurGoodsSaveStock] 请求数据: " + requestMap);
        
        // 从 Map 中提取采购商品信息
        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
        if (requestMap.get("nxDpgDisGoodsId") != null) {
            purchaseGoodsEntity.setNxDpgDisGoodsId(Integer.parseInt(requestMap.get("nxDpgDisGoodsId").toString()));
        }
        if (requestMap.get("nxDpgDisGoodsFatherId") != null) {
            purchaseGoodsEntity.setNxDpgDisGoodsFatherId(Integer.parseInt(requestMap.get("nxDpgDisGoodsFatherId").toString()));
        }
        if (requestMap.get("nxDpgDisGoodsGrandId") != null) {
            purchaseGoodsEntity.setNxDpgDisGoodsGrandId(Integer.parseInt(requestMap.get("nxDpgDisGoodsGrandId").toString()));
        }
        if (requestMap.get("nxDpgDistributerId") != null) {
            purchaseGoodsEntity.setNxDpgDistributerId(Integer.parseInt(requestMap.get("nxDpgDistributerId").toString()));
        }
        if (requestMap.get("nxDpgQuantity") != null) {
            purchaseGoodsEntity.setNxDpgQuantity(requestMap.get("nxDpgQuantity").toString());
            purchaseGoodsEntity.setNxDpgBuyQuantity(requestMap.get("nxDpgQuantity").toString());
        }
        if (requestMap.get("nxDpgBuyPrice") != null) {
            purchaseGoodsEntity.setNxDpgBuyPrice(requestMap.get("nxDpgBuyPrice").toString());
        }
        if (requestMap.get("nxDpgBuySubtotal") != null) {
            purchaseGoodsEntity.setNxDpgBuySubtotal(requestMap.get("nxDpgBuySubtotal").toString());
        }
        if (requestMap.get("nxDpgExpectPrice") != null) {
            purchaseGoodsEntity.setNxDpgExpectPrice(requestMap.get("nxDpgExpectPrice").toString());
        }
        if (requestMap.get("nxDpgPurUserId") != null) {
            purchaseGoodsEntity.setNxDpgPurUserId(Integer.parseInt(requestMap.get("nxDpgPurUserId").toString()));
        }
        if (requestMap.get("nxDpgInputType") != null) {
            purchaseGoodsEntity.setNxDpgInputType(Integer.parseInt(requestMap.get("nxDpgInputType").toString()));
        }
        if (requestMap.get("nxDpgPurchaseType") != null) {
            purchaseGoodsEntity.setNxDpgPurchaseType(Integer.parseInt(requestMap.get("nxDpgPurchaseType").toString()));
        }
        if (requestMap.get("nxDpgStandard") != null) {
            purchaseGoodsEntity.setNxDpgStandard(requestMap.get("nxDpgStandard").toString());
        }
        
        // 提取前端传入的双单价字段
        String nxDgssPriceCarton = requestMap.get("nxDgssPriceCarton") != null ? requestMap.get("nxDgssPriceCarton").toString() : null;
        String nxDgssSellingPriceCarton = requestMap.get("nxDgssSellingPriceCarton") != null ? requestMap.get("nxDgssSellingPriceCarton").toString() : null;
        
        // 提取货架商品ID（可选，如果传入则直接使用，否则查询）
        Integer shelfGoodsId = null;
        if (requestMap.get("nxDistributerGoodsShelfGoodsId") != null) {
            shelfGoodsId = Integer.parseInt(requestMap.get("nxDistributerGoodsShelfGoodsId").toString());
        }

        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
        purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
        purchaseGoodsEntity.setNxDpgOrdersAmount(0);
        purchaseGoodsEntity.setNxDpgFinishAmount(0);
        purchaseGoodsEntity.setNxDpgInputType(0);
        purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
        purchaseGoodsEntity.setNxDpgPayType(-1);
        purchaseGoodsEntity.setNxDpgPurchaseType(-1); // 直接入库的采购
        System.out.println("[disSavePurGoodsSaveStock] 保存采购商品");
        nxDisPurcGoodsService.save(purchaseGoodsEntity);

        NxDistributerGoodsShelfStockEntity stockEntity = new NxDistributerGoodsShelfStockEntity();

        stockEntity.setNxDgssNxPurGoodsId(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
        stockEntity.setNxDgssNxDistributerId(purchaseGoodsEntity.getNxDpgDistributerId());
        stockEntity.setNxDgssNxDisGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
        stockEntity.setNxDgssNxDisGoodsFatherId(purchaseGoodsEntity.getNxDpgDisGoodsFatherId());
        
        // 查询商品信息，获取外包装信息
        NxDistributerGoodsEntity disGoodsEntity = nxDgService.queryObject(purchaseGoodsEntity.getNxDpgDisGoodsId());
        
        // 获取采购信息
        String buyPriceStr = purchaseGoodsEntity.getNxDpgBuyPrice();
        String buyQuantityStr = purchaseGoodsEntity.getNxDpgQuantity();
        String expectPriceStr = purchaseGoodsEntity.getNxDpgExpectPrice();
        
        BigDecimal buyPrice = buyPriceStr != null && !buyPriceStr.trim().isEmpty() 
            ? new BigDecimal(buyPriceStr) : BigDecimal.ZERO;
        BigDecimal buyQuantity = buyQuantityStr != null && !buyQuantityStr.trim().isEmpty() 
            ? new BigDecimal(buyQuantityStr) : BigDecimal.ZERO;
        BigDecimal expectPrice = expectPriceStr != null && !expectPriceStr.trim().isEmpty() 
            ? new BigDecimal(expectPriceStr) : null;
        
        // 检查是否有外包装信息
        boolean hasCarton = disGoodsEntity != null 
            && disGoodsEntity.getNxDgCartonUnit() != null 
            && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
            && disGoodsEntity.getNxDgItemsPerCarton() != null 
            && disGoodsEntity.getNxDgItemsPerCarton() > 0;
        
        // 判断采购规格是否与外包装单位匹配
        String purchaseStandard = purchaseGoodsEntity.getNxDpgStandard();
        boolean isCartonStandard = hasCarton 
            && purchaseStandard != null 
            && purchaseStandard.trim().equals(disGoodsEntity.getNxDgCartonUnit().trim());
        
        if (hasCarton) {
            // 有外包装：需要计算双单价和双零售价
            Integer itemsPerCarton = disGoodsEntity.getNxDgItemsPerCarton();
            BigDecimal itemsPerCartonBD = new BigDecimal(itemsPerCarton);
            
            // 1. 数量转换：按最小单位存储
            BigDecimal weightInMinUnit;
            BigDecimal actualCartonCount; // 实际箱数
            if (isCartonStandard) {
                // 如果采购规格是外包装单位（如"箱"），说明前端传入的数量是箱数
                // 例如：规格="箱"，数量=2箱，每箱200个，则入库数量=2×200=400个
                actualCartonCount = buyQuantity;
                weightInMinUnit = buyQuantity.multiply(itemsPerCartonBD);
                System.out.println("[disSavePurGoodsSaveStock] 采购规格是外包装单位，数量转换: " + buyQuantity 
                    + "箱 × " + itemsPerCarton + "个/箱 = " + weightInMinUnit + "个");
            } else {
                // 如果采购规格不是外包装单位，说明前端传入的数量已经是按最小单位的
                // 需要计算实际箱数（用于成本计算）
                actualCartonCount = buyQuantity.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                weightInMinUnit = buyQuantity;
                System.out.println("[disSavePurGoodsSaveStock] 采购规格不是外包装单位，数量=" + buyQuantity 
                    + "个，折合" + actualCartonCount + "箱");
            }
            stockEntity.setNxDgssWeight(weightInMinUnit.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            stockEntity.setNxDgssRestWeight(weightInMinUnit.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            
            // 2. 单价计算
            // 如果前端传入了外包装单价，使用前端传入的值；否则根据采购规格判断
            if (nxDgssPriceCarton != null && !nxDgssPriceCarton.trim().isEmpty()) {
                // 使用前端传入的外包装单价
                BigDecimal priceCarton = new BigDecimal(nxDgssPriceCarton);
                stockEntity.setNxDgssPriceCarton(priceCarton.stripTrailingZeros().toPlainString());
                // 计算最小单位单价
                BigDecimal priceInMinUnit = priceCarton.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                stockEntity.setNxDgssPrice(priceInMinUnit.stripTrailingZeros().toPlainString());
            } else if (isCartonStandard) {
                // 采购规格是外包装单位，采购单价是外包装单价
                stockEntity.setNxDgssPriceCarton(buyPrice.stripTrailingZeros().toPlainString());
                // 计算最小单位单价
                BigDecimal priceInMinUnit = buyPrice.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                stockEntity.setNxDgssPrice(priceInMinUnit.stripTrailingZeros().toPlainString());
            } else {
                // 采购规格不是外包装单位，采购单价是最小单位单价
                stockEntity.setNxDgssPrice(buyPrice.stripTrailingZeros().toPlainString());
                // 计算外包装单价
                BigDecimal priceCarton = buyPrice.multiply(itemsPerCartonBD);
                stockEntity.setNxDgssPriceCarton(priceCarton.stripTrailingZeros().toPlainString());
            }
            
            // 3. 建议零售价计算
            if (expectPrice != null) {
                // 如果前端传入了外包装建议零售价，使用前端传入的值；否则根据采购规格判断
                if (nxDgssSellingPriceCarton != null && !nxDgssSellingPriceCarton.trim().isEmpty()) {
                    BigDecimal sellingPriceCarton = new BigDecimal(nxDgssSellingPriceCarton);
                    stockEntity.setNxDgssSellingPriceCarton(sellingPriceCarton.stripTrailingZeros().toPlainString());
                    // 计算最小单位建议零售价
                    BigDecimal sellingPriceInMinUnit = sellingPriceCarton.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                    stockEntity.setNxDgssSellingPrice(sellingPriceInMinUnit.stripTrailingZeros().toPlainString());
                } else if (isCartonStandard) {
                    // 采购规格是外包装单位，期望价格是外包装建议零售价
                    stockEntity.setNxDgssSellingPriceCarton(expectPrice.stripTrailingZeros().toPlainString());
                    // 计算最小单位建议零售价
                    BigDecimal sellingPriceInMinUnit = expectPrice.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                    stockEntity.setNxDgssSellingPrice(sellingPriceInMinUnit.stripTrailingZeros().toPlainString());
                } else {
                    // 采购规格不是外包装单位，期望价格是最小单位建议零售价
                    stockEntity.setNxDgssSellingPrice(expectPrice.stripTrailingZeros().toPlainString());
                    // 计算外包装建议零售价
                    BigDecimal sellingPriceCarton = expectPrice.multiply(itemsPerCartonBD);
                    stockEntity.setNxDgssSellingPriceCarton(sellingPriceCarton.stripTrailingZeros().toPlainString());
                }
            }
            
            // 4. 成本计算
            // 如果采购规格是大包装单位，使用大包装单价 × 大包装数量
            // 如果采购规格是最小单位，使用最小单位单价 × 最小单位数量
            BigDecimal subtotal;
            if (isCartonStandard) {
                // 使用大包装单价 × 大包装数量
                BigDecimal priceCarton = new BigDecimal(stockEntity.getNxDgssPriceCarton());
                subtotal = actualCartonCount.multiply(priceCarton).setScale(1, BigDecimal.ROUND_HALF_UP);
                System.out.println("[disSavePurGoodsSaveStock] 成本计算（大包装单位）: " + actualCartonCount 
                    + "箱 × " + stockEntity.getNxDgssPriceCarton() + "元/箱 = " + subtotal + "元");
            } else {
                // 使用最小单位单价 × 最小单位数量
                BigDecimal priceForCalc = new BigDecimal(stockEntity.getNxDgssPrice());
                subtotal = weightInMinUnit.multiply(priceForCalc).setScale(1, BigDecimal.ROUND_HALF_UP);
                System.out.println("[disSavePurGoodsSaveStock] 成本计算（最小单位）: " + weightInMinUnit 
                    + "个 × " + stockEntity.getNxDgssPrice() + "元/个 = " + subtotal + "元");
            }
            stockEntity.setNxDgssSubtotal(subtotal.toPlainString());
            stockEntity.setNxDgssRestSubtotal(subtotal.toPlainString());
            
            System.out.println("[disSavePurGoodsSaveStock] 商品有外包装: " + disGoodsEntity.getNxDgCartonUnit() 
                + ", 每箱" + itemsPerCarton + "个, 采购数量=" + buyQuantity + (isCartonStandard ? "箱" : "个") 
                + ", 入库数量=" + weightInMinUnit + "个"
                + ", 最小单位单价=" + stockEntity.getNxDgssPrice() + ", 外包装单价=" + stockEntity.getNxDgssPriceCarton());
        } else {
            // 无外包装：按原有逻辑处理
            stockEntity.setNxDgssPrice(buyPrice.stripTrailingZeros().toPlainString());
            stockEntity.setNxDgssWeight(buyQuantity.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            stockEntity.setNxDgssRestWeight(buyQuantity.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
        stockEntity.setNxDgssSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
        stockEntity.setNxDgssRestSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
            
            if (expectPrice != null) {
                stockEntity.setNxDgssSellingPrice(expectPrice.stripTrailingZeros().toPlainString());
            }
        }
        
        stockEntity.setNxDgssStatus(0);

        stockEntity.setNxDgssDate(formatWhatDay(0));
        stockEntity.setNxDgssMonth(formatWhatMonth(0));
        stockEntity.setNxDgssYear(formatWhatYear(0));
        stockEntity.setNxDgssProduceWeight("0");
        stockEntity.setNxDgssProduceSubtotal("0");
        stockEntity.setNxDgssLossWeight("0");
        stockEntity.setNxDgssLossSubtotal("0");
        stockEntity.setNxDgssWasteWeight("0");
        stockEntity.setNxDgssWasteSubtotal("0");
        stockEntity.setNxDgssReturnWeight("0");
        stockEntity.setNxDgssReturnSubtotal("0");
        stockEntity.setNxDgssReceiveUserId(purchaseGoodsEntity.getNxDpgPurUserId());

        // 设置货架商品ID
        // 如果前端传入了货架商品ID，直接使用；否则查询（兼容旧逻辑）
        if (shelfGoodsId != null) {
            stockEntity.setNxDgssNxShelfGoodsId(shelfGoodsId);
            System.out.println("使用前端传入的货架商品ID: " + shelfGoodsId);
        } else {
            // 兼容旧逻辑：如果没有传入货架商品ID，使用原来的查询方式
            NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity = shelfGoodsService.queryShlefGoodsByGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
        if(shelfGoodsEntity != null){
            System.out.println("商品在货架上，货架商品ID: " + shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
            stockEntity.setNxDgssNxShelfGoodsId(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
        } else {
            System.out.println("商品不在货架上");
            }
        }

        shelfStockService.save(stockEntity);
        System.out.println("[disSavePurGoodsSaveStock] 库存批次已保存，库存ID: " + stockEntity.getNxDistributerGoodsShelfStockId());
        return R.ok().put("data",stockEntity);
    }

    /**
     * 首衡项目：保存采购商品并入库（包含库存图片和说明）
     */
    @RequestMapping(value = "/disSavePurGoodsSaveStockSunHola", method = RequestMethod.POST)
    @ResponseBody
    public R disSavePurGoodsSaveStockSunHola (@RequestBody Map<String, Object> requestMap) {
        System.out.println("[disSavePurGoodsSaveStockSunHola] 首衡项目请求数据: " + requestMap);
        
        // 从 Map 中提取采购商品信息
        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = new NxDistributerPurchaseGoodsEntity();
        if (requestMap.get("nxDpgDisGoodsId") != null) {
            purchaseGoodsEntity.setNxDpgDisGoodsId(Integer.parseInt(requestMap.get("nxDpgDisGoodsId").toString()));
        }
        if (requestMap.get("nxDpgDisGoodsFatherId") != null) {
            purchaseGoodsEntity.setNxDpgDisGoodsFatherId(Integer.parseInt(requestMap.get("nxDpgDisGoodsFatherId").toString()));
        }
        if (requestMap.get("nxDpgDisGoodsGrandId") != null) {
            purchaseGoodsEntity.setNxDpgDisGoodsGrandId(Integer.parseInt(requestMap.get("nxDpgDisGoodsGrandId").toString()));
        }
        if (requestMap.get("nxDpgDistributerId") != null) {
            purchaseGoodsEntity.setNxDpgDistributerId(Integer.parseInt(requestMap.get("nxDpgDistributerId").toString()));
        }
        if (requestMap.get("nxDpgQuantity") != null) {
            purchaseGoodsEntity.setNxDpgQuantity(requestMap.get("nxDpgQuantity").toString());
            purchaseGoodsEntity.setNxDpgBuyQuantity(requestMap.get("nxDpgQuantity").toString());
        }
        if (requestMap.get("nxDpgBuyPrice") != null) {
            purchaseGoodsEntity.setNxDpgBuyPrice(requestMap.get("nxDpgBuyPrice").toString());
        }
        if (requestMap.get("nxDpgBuySubtotal") != null) {
            purchaseGoodsEntity.setNxDpgBuySubtotal(requestMap.get("nxDpgBuySubtotal").toString());
        }
        if (requestMap.get("nxDpgExpectPrice") != null) {
            purchaseGoodsEntity.setNxDpgExpectPrice(requestMap.get("nxDpgExpectPrice").toString());
        }
        if (requestMap.get("nxDpgPurUserId") != null) {
            purchaseGoodsEntity.setNxDpgPurUserId(Integer.parseInt(requestMap.get("nxDpgPurUserId").toString()));
        }
        if (requestMap.get("nxDpgInputType") != null) {
            purchaseGoodsEntity.setNxDpgInputType(Integer.parseInt(requestMap.get("nxDpgInputType").toString()));
        }
        if (requestMap.get("nxDpgPurchaseType") != null) {
            purchaseGoodsEntity.setNxDpgPurchaseType(Integer.parseInt(requestMap.get("nxDpgPurchaseType").toString()));
        }
        if (requestMap.get("nxDpgStandard") != null) {
            purchaseGoodsEntity.setNxDpgStandard(requestMap.get("nxDpgStandard").toString());
        }
        
        // 提取前端传入的双单价字段
        String nxDgssPriceCarton = requestMap.get("nxDgssPriceCarton") != null ? requestMap.get("nxDgssPriceCarton").toString() : null;
        String nxDgssSellingPriceCarton = requestMap.get("nxDgssSellingPriceCarton") != null ? requestMap.get("nxDgssSellingPriceCarton").toString() : null;
        
        // 提取首衡项目新增字段：库存图片和库存说明
        String nxDgssStockImage = requestMap.get("nxDgssStockImage") != null ? requestMap.get("nxDgssStockImage").toString() : null;
        String nxDgssStockRemark = requestMap.get("nxDgssStockRemark") != null ? requestMap.get("nxDgssStockRemark").toString() : null;
        
        // 处理图片路径：如果是 HTTP URL，提取相对路径
        if (nxDgssStockImage != null && !nxDgssStockImage.trim().isEmpty()) {
            if (nxDgssStockImage.startsWith("http://") || nxDgssStockImage.startsWith("https://")) {
                // 从 HTTP URL 中提取相对路径（查找 stockImages/ 后面的部分）
                if (nxDgssStockImage.contains("stockImages/")) {
                    int index = nxDgssStockImage.indexOf("stockImages/");
                    nxDgssStockImage = nxDgssStockImage.substring(index);
                    System.out.println("[disSavePurGoodsSaveStockSunHola] 从 HTTP URL 提取相对路径: " + requestMap.get("nxDgssStockImage") + " -> " + nxDgssStockImage);
                } else {
                    System.err.println("[disSavePurGoodsSaveStockSunHola] HTTP URL 中不包含 stockImages/，忽略: " + nxDgssStockImage);
                    nxDgssStockImage = null; // 无效的 URL，忽略
                }
            } else if (!nxDgssStockImage.startsWith("stockImages/")) {
                // 如果不是 HTTP URL 且不以 stockImages/ 开头，尝试修正
                if (nxDgssStockImage.contains("stockImages/")) {
                    int index = nxDgssStockImage.indexOf("stockImages/");
                    nxDgssStockImage = nxDgssStockImage.substring(index);
                } else {
                    // 如果完全不包含，添加 stockImages/ 前缀
                    nxDgssStockImage = "stockImages/" + nxDgssStockImage;
                }
                System.out.println("[disSavePurGoodsSaveStockSunHola] 修正图片路径: " + requestMap.get("nxDgssStockImage") + " -> " + nxDgssStockImage);
            }
        }
        
        // 提取货架商品ID（可选，如果传入则直接使用，否则查询）
        Integer shelfGoodsId = null;
        if (requestMap.get("nxDistributerGoodsShelfGoodsId") != null) {
            shelfGoodsId = Integer.parseInt(requestMap.get("nxDistributerGoodsShelfGoodsId").toString());
        }

        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
        purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
        purchaseGoodsEntity.setNxDpgOrdersAmount(0);
        purchaseGoodsEntity.setNxDpgFinishAmount(0);
        purchaseGoodsEntity.setNxDpgInputType(0);
        purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
        purchaseGoodsEntity.setNxDpgPayType(-1);
        purchaseGoodsEntity.setNxDpgPurchaseType(-1); // 直接入库的采购
        System.out.println("[disSavePurGoodsSaveStockSunHola] 保存采购商品");
        nxDisPurcGoodsService.save(purchaseGoodsEntity);

        NxDistributerGoodsShelfStockEntity stockEntity = new NxDistributerGoodsShelfStockEntity();

        stockEntity.setNxDgssNxPurGoodsId(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
        stockEntity.setNxDgssNxDistributerId(purchaseGoodsEntity.getNxDpgDistributerId());
        stockEntity.setNxDgssNxDisGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
        stockEntity.setNxDgssNxDisGoodsFatherId(purchaseGoodsEntity.getNxDpgDisGoodsFatherId());
        
        // 查询商品信息，获取外包装信息
        NxDistributerGoodsEntity disGoodsEntity = nxDgService.queryObject(purchaseGoodsEntity.getNxDpgDisGoodsId());
        
        // 获取采购信息
        String buyPriceStr = purchaseGoodsEntity.getNxDpgBuyPrice();
        String buyQuantityStr = purchaseGoodsEntity.getNxDpgQuantity();
        String expectPriceStr = purchaseGoodsEntity.getNxDpgExpectPrice();
        
        BigDecimal buyPrice = buyPriceStr != null && !buyPriceStr.trim().isEmpty() 
            ? new BigDecimal(buyPriceStr) : BigDecimal.ZERO;
        BigDecimal buyQuantity = buyQuantityStr != null && !buyQuantityStr.trim().isEmpty() 
            ? new BigDecimal(buyQuantityStr) : BigDecimal.ZERO;
        BigDecimal expectPrice = expectPriceStr != null && !expectPriceStr.trim().isEmpty() 
            ? new BigDecimal(expectPriceStr) : null;
        
        // 检查是否有外包装信息
        boolean hasCarton = disGoodsEntity != null 
            && disGoodsEntity.getNxDgCartonUnit() != null 
            && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
            && disGoodsEntity.getNxDgItemsPerCarton() != null 
            && disGoodsEntity.getNxDgItemsPerCarton() > 0;
        
        // 判断采购规格是否与外包装单位匹配
        String purchaseStandard = purchaseGoodsEntity.getNxDpgStandard();
        boolean isCartonStandard = hasCarton 
            && purchaseStandard != null 
            && purchaseStandard.trim().equals(disGoodsEntity.getNxDgCartonUnit().trim());
        
        if (hasCarton) {
            // 有外包装：需要计算双单价和双零售价
            Integer itemsPerCarton = disGoodsEntity.getNxDgItemsPerCarton();
            BigDecimal itemsPerCartonBD = new BigDecimal(itemsPerCarton);
            
            // 1. 数量转换：按最小单位存储
            BigDecimal weightInMinUnit;
            BigDecimal actualCartonCount; // 实际箱数
            if (isCartonStandard) {
                // 如果采购规格是外包装单位（如"箱"），说明前端传入的数量是箱数
                // 例如：规格="箱"，数量=2箱，每箱200个，则入库数量=2×200=400个
                actualCartonCount = buyQuantity;
                weightInMinUnit = buyQuantity.multiply(itemsPerCartonBD);
                System.out.println("[disSavePurGoodsSaveStockSunHola] 采购规格是外包装单位，数量转换: " + buyQuantity 
                    + "箱 × " + itemsPerCarton + "个/箱 = " + weightInMinUnit + "个");
            } else {
                // 如果采购规格不是外包装单位，说明前端传入的数量已经是按最小单位的
                // 需要计算实际箱数（用于成本计算）
                actualCartonCount = buyQuantity.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                weightInMinUnit = buyQuantity;
                System.out.println("[disSavePurGoodsSaveStockSunHola] 采购规格不是外包装单位，数量=" + buyQuantity 
                    + "个，折合" + actualCartonCount + "箱");
            }
            stockEntity.setNxDgssWeight(weightInMinUnit.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            stockEntity.setNxDgssRestWeight(weightInMinUnit.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            
            // 2. 单价计算
            // 如果前端传入了外包装单价，使用前端传入的值；否则根据采购规格判断
            if (nxDgssPriceCarton != null && !nxDgssPriceCarton.trim().isEmpty()) {
                // 使用前端传入的外包装单价
                BigDecimal priceCarton = new BigDecimal(nxDgssPriceCarton);
                stockEntity.setNxDgssPriceCarton(priceCarton.stripTrailingZeros().toPlainString());
                // 计算最小单位单价
                BigDecimal priceInMinUnit = priceCarton.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                stockEntity.setNxDgssPrice(priceInMinUnit.stripTrailingZeros().toPlainString());
            } else if (isCartonStandard) {
                // 采购规格是外包装单位，采购单价是外包装单价
                stockEntity.setNxDgssPriceCarton(buyPrice.stripTrailingZeros().toPlainString());
                // 计算最小单位单价
                BigDecimal priceInMinUnit = buyPrice.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                stockEntity.setNxDgssPrice(priceInMinUnit.stripTrailingZeros().toPlainString());
            } else {
                // 采购规格不是外包装单位，采购单价是最小单位单价
                stockEntity.setNxDgssPrice(buyPrice.stripTrailingZeros().toPlainString());
                // 计算外包装单价
                BigDecimal priceCarton = buyPrice.multiply(itemsPerCartonBD);
                stockEntity.setNxDgssPriceCarton(priceCarton.stripTrailingZeros().toPlainString());
            }
            
            // 3. 建议零售价计算
            if (expectPrice != null) {
                // 如果前端传入了外包装建议零售价，使用前端传入的值；否则根据采购规格判断
                if (nxDgssSellingPriceCarton != null && !nxDgssSellingPriceCarton.trim().isEmpty()) {
                    BigDecimal sellingPriceCarton = new BigDecimal(nxDgssSellingPriceCarton);
                    stockEntity.setNxDgssSellingPriceCarton(sellingPriceCarton.stripTrailingZeros().toPlainString());
                    // 计算最小单位建议零售价
                    BigDecimal sellingPriceInMinUnit = sellingPriceCarton.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                    stockEntity.setNxDgssSellingPrice(sellingPriceInMinUnit.stripTrailingZeros().toPlainString());
                } else if (isCartonStandard) {
                    // 采购规格是外包装单位，期望价格是外包装建议零售价
                    stockEntity.setNxDgssSellingPriceCarton(expectPrice.stripTrailingZeros().toPlainString());
                    // 计算最小单位建议零售价
                    BigDecimal sellingPriceInMinUnit = expectPrice.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                    stockEntity.setNxDgssSellingPrice(sellingPriceInMinUnit.stripTrailingZeros().toPlainString());
                } else {
                    // 采购规格不是外包装单位，期望价格是最小单位建议零售价
                    stockEntity.setNxDgssSellingPrice(expectPrice.stripTrailingZeros().toPlainString());
                    // 计算外包装建议零售价
                    BigDecimal sellingPriceCarton = expectPrice.multiply(itemsPerCartonBD);
                    stockEntity.setNxDgssSellingPriceCarton(sellingPriceCarton.stripTrailingZeros().toPlainString());
                }
            }
            
            // 4. 成本计算
            // 如果采购规格是大包装单位，使用大包装单价 × 大包装数量
            // 如果采购规格是最小单位，使用最小单位单价 × 最小单位数量
            BigDecimal subtotal;
            if (isCartonStandard) {
                // 使用大包装单价 × 大包装数量
                BigDecimal priceCarton = new BigDecimal(stockEntity.getNxDgssPriceCarton());
                subtotal = actualCartonCount.multiply(priceCarton).setScale(1, BigDecimal.ROUND_HALF_UP);
                System.out.println("[disSavePurGoodsSaveStockSunHola] 成本计算（大包装单位）: " + actualCartonCount 
                    + "箱 × " + stockEntity.getNxDgssPriceCarton() + "元/箱 = " + subtotal + "元");
            } else {
                // 使用最小单位单价 × 最小单位数量
                BigDecimal priceForCalc = new BigDecimal(stockEntity.getNxDgssPrice());
                subtotal = weightInMinUnit.multiply(priceForCalc).setScale(1, BigDecimal.ROUND_HALF_UP);
                System.out.println("[disSavePurGoodsSaveStockSunHola] 成本计算（最小单位）: " + weightInMinUnit 
                    + "个 × " + stockEntity.getNxDgssPrice() + "元/个 = " + subtotal + "元");
            }
            stockEntity.setNxDgssSubtotal(subtotal.toPlainString());
            stockEntity.setNxDgssRestSubtotal(subtotal.toPlainString());
            
            System.out.println("[disSavePurGoodsSaveStockSunHola] 商品有外包装: " + disGoodsEntity.getNxDgCartonUnit() 
                + ", 每箱" + itemsPerCarton + "个, 采购数量=" + buyQuantity + (isCartonStandard ? "箱" : "个") 
                + ", 入库数量=" + weightInMinUnit + "个"
                + ", 最小单位单价=" + stockEntity.getNxDgssPrice() + ", 外包装单价=" + stockEntity.getNxDgssPriceCarton());
        } else {
            // 无外包装：按原有逻辑处理
            stockEntity.setNxDgssPrice(buyPrice.stripTrailingZeros().toPlainString());
            stockEntity.setNxDgssWeight(buyQuantity.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            stockEntity.setNxDgssRestWeight(buyQuantity.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            stockEntity.setNxDgssSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
            stockEntity.setNxDgssRestSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
            
            if (expectPrice != null) {
                stockEntity.setNxDgssSellingPrice(expectPrice.stripTrailingZeros().toPlainString());
            }
        }
        
        stockEntity.setNxDgssStatus(0);

        stockEntity.setNxDgssDate(formatWhatDay(0));
        stockEntity.setNxDgssMonth(formatWhatMonth(0));
        stockEntity.setNxDgssYear(formatWhatYear(0));
        stockEntity.setNxDgssProduceWeight("0");
        stockEntity.setNxDgssProduceSubtotal("0");
        stockEntity.setNxDgssLossWeight("0");
        stockEntity.setNxDgssLossSubtotal("0");
        stockEntity.setNxDgssWasteWeight("0");
        stockEntity.setNxDgssWasteSubtotal("0");
        stockEntity.setNxDgssReturnWeight("0");
        stockEntity.setNxDgssReturnSubtotal("0");
        stockEntity.setNxDgssReceiveUserId(purchaseGoodsEntity.getNxDpgPurUserId());

        // 设置货架商品ID
        // 如果前端传入了货架商品ID，直接使用；否则查询（兼容旧逻辑）
        if (shelfGoodsId != null) {
            stockEntity.setNxDgssNxShelfGoodsId(shelfGoodsId);
            System.out.println("[disSavePurGoodsSaveStockSunHola] 使用前端传入的货架商品ID: " + shelfGoodsId);
        } else {
            // 兼容旧逻辑：如果没有传入货架商品ID，使用原来的查询方式
            NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity = shelfGoodsService.queryShlefGoodsByGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
            if(shelfGoodsEntity != null){
                System.out.println("[disSavePurGoodsSaveStockSunHola] 商品在货架上，货架商品ID: " + shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
                stockEntity.setNxDgssNxShelfGoodsId(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
            } else {
                System.out.println("[disSavePurGoodsSaveStockSunHola] 商品不在货架上");
            }
        }

        // 首衡项目：设置库存图片和库存说明
        if (nxDgssStockImage != null && !nxDgssStockImage.trim().isEmpty()) {
            stockEntity.setNxDgssStockImage(nxDgssStockImage);
            System.out.println("[disSavePurGoodsSaveStockSunHola] 设置库存图片: " + nxDgssStockImage);
        }
        if (nxDgssStockRemark != null && !nxDgssStockRemark.trim().isEmpty()) {
            stockEntity.setNxDgssStockRemark(nxDgssStockRemark);
            System.out.println("[disSavePurGoodsSaveStockSunHola] 设置库存说明: " + nxDgssStockRemark);
        }

        shelfStockService.save(stockEntity);
        System.out.println("[disSavePurGoodsSaveStockSunHola] 首衡项目库存批次已保存，库存ID: " + stockEntity.getNxDistributerGoodsShelfStockId());
        
        // 规范化返回的图片路径（确保返回的是相对路径，不是 HTTP URL）
        String responseImagePath = stockEntity.getNxDgssStockImage();
        if (responseImagePath != null && !responseImagePath.trim().isEmpty()) {
            if (responseImagePath.startsWith("http://") || responseImagePath.startsWith("https://")) {
                // 如果返回的是 HTTP URL，提取相对路径
                if (responseImagePath.contains("stockImages/")) {
                    int index = responseImagePath.indexOf("stockImages/");
                    responseImagePath = responseImagePath.substring(index);
                    stockEntity.setNxDgssStockImage(responseImagePath);
                    System.out.println("[disSavePurGoodsSaveStockSunHola] 规范化返回路径: " + stockEntity.getNxDgssStockImage() + " -> " + responseImagePath);
                }
            }
        }
        
        return R.ok().put("data",stockEntity);
    }

    /**
     * 首衡项目：保存采购商品并入库（包含库存图片和说明）- 支持文件上传
     */
    @RequestMapping(value = "/disSavePurGoodsSaveStockSunHolaWithFile", method = RequestMethod.POST)
    @ResponseBody
    public R disSavePurGoodsSaveStockSunHolaWithFile(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("nxDpgDisGoodsId") String nxDpgDisGoodsIdStr,
            @RequestParam("nxDpgDistributerId") String nxDpgDistributerIdStr,
            @RequestParam("nxDpgQuantity") String nxDpgQuantity,
            @RequestParam("nxDpgBuyPrice") String nxDpgBuyPrice,
            @RequestParam("nxDpgBuySubtotal") String nxDpgBuySubtotal,
            @RequestParam(value = "nxDpgDisGoodsFatherId", required = false) String nxDpgDisGoodsFatherIdStr,
            @RequestParam(value = "nxDpgDisGoodsGrandId", required = false) String nxDpgDisGoodsGrandIdStr,
            @RequestParam(value = "nxDpgExpectPrice", required = false) String nxDpgExpectPrice,
            @RequestParam(value = "nxDpgPurUserId", required = false) String nxDpgPurUserIdStr,
            @RequestParam(value = "nxDpgInputType", required = false) String nxDpgInputTypeStr,
            @RequestParam(value = "nxDpgPurchaseType", required = false) String nxDpgPurchaseTypeStr,
            @RequestParam(value = "nxDpgStandard", required = false) String nxDpgStandard,
            @RequestParam(value = "nxDgssPriceCarton", required = false) String nxDgssPriceCarton,
            @RequestParam(value = "nxDgssSellingPriceCarton", required = false) String nxDgssSellingPriceCarton,
            @RequestParam(value = "nxDgssStockRemark", required = false) String nxDgssStockRemark,
            @RequestParam(value = "nxDistributerGoodsShelfGoodsId", required = false) String nxDistributerGoodsShelfGoodsIdStr,
            HttpSession session) {
        
        // 构建 requestMap，复用现有逻辑
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("nxDpgDisGoodsId", nxDpgDisGoodsIdStr);
        requestMap.put("nxDpgDistributerId", nxDpgDistributerIdStr);
        requestMap.put("nxDpgQuantity", nxDpgQuantity);
        requestMap.put("nxDpgBuyPrice", nxDpgBuyPrice);
        requestMap.put("nxDpgBuySubtotal", nxDpgBuySubtotal);
        if (nxDpgDisGoodsFatherIdStr != null) requestMap.put("nxDpgDisGoodsFatherId", nxDpgDisGoodsFatherIdStr);
        if (nxDpgDisGoodsGrandIdStr != null) requestMap.put("nxDpgDisGoodsGrandId", nxDpgDisGoodsGrandIdStr);
        if (nxDpgExpectPrice != null) requestMap.put("nxDpgExpectPrice", nxDpgExpectPrice);
        if (nxDpgPurUserIdStr != null) requestMap.put("nxDpgPurUserId", nxDpgPurUserIdStr);
        if (nxDpgInputTypeStr != null) requestMap.put("nxDpgInputType", nxDpgInputTypeStr);
        if (nxDpgPurchaseTypeStr != null) requestMap.put("nxDpgPurchaseType", nxDpgPurchaseTypeStr);
        if (nxDpgStandard != null) requestMap.put("nxDpgStandard", nxDpgStandard);
        if (nxDgssPriceCarton != null) requestMap.put("nxDgssPriceCarton", nxDgssPriceCarton);
        if (nxDgssSellingPriceCarton != null) requestMap.put("nxDgssSellingPriceCarton", nxDgssSellingPriceCarton);
        if (nxDgssStockRemark != null) requestMap.put("nxDgssStockRemark", nxDgssStockRemark);
        if (nxDistributerGoodsShelfGoodsIdStr != null) requestMap.put("nxDistributerGoodsShelfGoodsId", nxDistributerGoodsShelfGoodsIdStr);
        
        // 处理文件上传
        String nxDgssStockImage = null;
        if (file != null && !file.isEmpty()) {
            try {
                // 上传图片到 stockImages 文件夹（参考 updateFatherBigNx 的实现）
                String newUploadName = "stockImages";
                String stockId = nxDpgDisGoodsIdStr; // 使用商品ID作为文件名的一部分
                String originalName = "stock_" + stockId;
                originalName = originalName.replaceAll("[\\\\/:*?\"<>|]", "");
                // 注意：使用 originalName 而不是 englishKuohao，与 updateFatherBigNx 保持一致
                // 生成文件名，去掉空格和冒号等特殊字符，便于 URL 使用
                String timeStr = formatFullTime().replaceAll("[\\s:-]", "");
                String lastFileName = originalName + timeStr;
                String realPath = UploadFile.uploadFileName(session, newUploadName, file, lastFileName);
                nxDgssStockImage = newUploadName + "/" + lastFileName + ".jpg";
                System.out.println("[disSavePurGoodsSaveStockSunHolaWithFile] 上传图片成功: " + nxDgssStockImage);
                System.out.println("[disSavePurGoodsSaveStockSunHolaWithFile] 文件保存路径: " + realPath);
            } catch (Exception e) {
                System.err.println("[disSavePurGoodsSaveStockSunHolaWithFile] 上传图片失败: " + e.getMessage());
                e.printStackTrace();
                return R.error("上传图片失败: " + e.getMessage());
            }
        }
        
        // 设置图片路径
        if (nxDgssStockImage != null) {
            requestMap.put("nxDgssStockImage", nxDgssStockImage);
        }
        
        // 调用现有的处理方法
        return disSavePurGoodsSaveStockSunHola(requestMap);
    }

    @RequestMapping(value = "/purchaserGetHaveFinishedPurGoods")
    @ResponseBody
    public R purchaserGetHaveFinishedPurGoods(Integer userId,  Integer disId, Integer equalStatus) {
        Map<String, Object> map = new HashMap<>();
        map.put("buyUserId", userId);
        map.put("equalStatus", equalStatus);
        map.put("batchId", 0);
        System.out.println("mailelelee" + map);
        List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = nxDisPurcGoodsService.queryDisPurchaseGoodsGreat(map);

        return R.ok().put("data", fatherGoodsEntities);
    }

        @RequestMapping(value = "/markPurGoodsFinish", method = RequestMethod.POST)
    @ResponseBody
    public R markPurGoodsFinish(@RequestBody NxDistributerPurchaseGoodsEntity purgoods) {

        purgoods.setNxDpgBatchId(-1);
        purgoods.setNxDpgStatus(1);
        purgoods.setNxDpgPayType(0);
        purgoods.setNxDpgTime(formatWhatTime(0));
        purgoods.setNxDpgPurchaseDate(formatWhatDay(0));
        nxDisPurcGoodsService.update(purgoods);

        return R.ok();
    }
    
    

    @RequestMapping(value = "/deleteInputType/{id}")
    @ResponseBody
    public R deleteInputType(@PathVariable Integer id) {
        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDisPurcGoodsService.queryObject(id);
        purchaseGoodsEntity.setNxDpgInputType(null);
        nxDisPurcGoodsService.update(purchaseGoodsEntity);
        return R.ok();
    }


    @RequestMapping(value = "/deletePurGoods/{id}")
    @ResponseBody
    public R deletePurGoods(@PathVariable Integer id) {
        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDisPurcGoodsService.queryObject(id);
        if(purchaseGoodsEntity.getNxDpgBatchId() != null){
            Map<String, Object> map = new HashMap<>();
            map.put("batchId", purchaseGoodsEntity.getNxDpgBatchId());
            List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntities = nxDisPurcGoodsService.queryPurchaseGoodsByParams(map);
            if(purchaseGoodsEntities.size() == 1){
                nxDPBService.delete(purchaseGoodsEntity.getNxDpgBatchId());
            }
        }
        nxDisPurcGoodsService.delete(id);
        return R.ok();
    }


    @RequestMapping(value = "/savePurchaseGoodsInputTypeBundle", method = RequestMethod.POST)
    @ResponseBody
    public R savePurchaseGoodsInputTypeBundle(String ids, Integer type) {
        String[] arr = ids.split(",");
        for (String id : arr) {
            NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDisPurcGoodsService.queryObject(Integer.valueOf(id));
            purchaseGoodsEntity.setNxDpgInputType(type);
            nxDisPurcGoodsService.update(purchaseGoodsEntity);
        }

        return R.ok();
    }

//
//    @RequestMapping(value = "/disGetToPrintPurGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R disGetToPrintPurGoods(Integer disId, Integer fatherId) {
//        Map<String, Object> map4 = new HashMap<>();
//        map4.put("fatherId", fatherId);
//        map4.put("status", 1);
//        map4.put("weightId", -1);
//        System.out.println("map44444" + map4);
//        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);
//
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", disId);
//        map.put("orderStatus", 3);
//        map.put("purchaseType", 1);
//        map.put("batchId", 0);
//        map.put("weightId", 1);
//        map.put("equalStatus", 1);
//        System.out.println("priririiriririiri" + map);
//        Integer printOrderCount = nxDisPurcGoodsService.queryPurOrderCount(map);
//
//        Map<String, Object> mapWN = new HashMap<>();
//        mapWN.put("disId", disId);
//        mapWN.put("date", formatWhatDay(0));
//        mapWN.put("type", 2);
//        int count = nxDistributerWeightService.queryWeightCountByParams(mapWN);
//        BigDecimal trade = new BigDecimal(count).add(new BigDecimal(1));
//        String s = formatDayNumber(0) + "CGD" + trade;
//
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("arr", purchaseToday);
//        map1.put("printOrderCount", printOrderCount);
//        map1.put("tradeNo", s);
//
//        return R.ok().put("data", map1);
//    }
//
//
//    @RequestMapping(value = "/cancleFinishPurGoods", method = RequestMethod.POST)
//    @ResponseBody
//    public R cancleFinishPurGoods(@RequestBody NxDistributerPurchaseGoodsEntity purgoods) {
//        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getNxDepartmentOrdersEntities();
//
//        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
//            orders.setNxDoStatus(getNxOrderStatusNew());
//            orders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
//            orders.setNxDoPurchaseUserId(-1);
//            orders.setNxDoWeight(null);
//            orders.setNxDoSubtotal(null);
//            nxDepartmentOrdersService.update(orders);
//        }
//        purgoods.setNxDpgStatus(getGbPurchaseGoodsStatusNew());
//        purgoods.setNxDpgBuyPrice(null);
//        purgoods.setNxDpgBuyQuantity(null);
//        purgoods.setNxDpgBuySubtotal(null);
//        purgoods.setNxDpgStatus(0);
//        purgoods.setNxDpgTime(null);
//        purgoods.setNxDpgPurchaseDate(null);
//        purgoods.setNxDpgPurchaseType(null);
//        purgoods.setNxDpgPurUserId(null);
//        purgoods.setNxDpgPurchaseDate(null);
//        purgoods.setNxDpgTime(null);
//        purgoods.setNxDpgBuyUserId(null);
//        nxDisPurcGoodsService.update(purgoods);
//
//        return R.ok();
//    }

    @RequestMapping(value = "/disUserUpdateSelfPurGoodsOrdersCost", method = RequestMethod.POST)
    @ResponseBody
    public R disUserUpdateSelfPurGoodsOrdersCost(@RequestBody NxDistributerPurchaseGoodsEntity purGoods) {
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purGoods.getNxDepartmentOrdersEntities();
        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            nxDepartmentOrdersService.update(orders);
        }
        nxDisPurcGoodsService.update(purGoods);
        return R.ok();
    }

    @RequestMapping(value = "/disUserGetPurchaserDateBill", method = RequestMethod.POST)
    @ResponseBody
    public R disUserGetPurchaserDateBill(Integer userId, String date) {
        System.out.println("amma" + date);

        //购买采购商品batchId == -1
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("date", date);
        map.put("batchId", -1);
        map.put("payType", 0);
        System.out.println("dmapapap" + map);
        List<NxDistributerPurchaseGoodsEntity> goodsEntities = nxDisPurcGoodsService.queryPurchaseGoodsByParams(map);
        BigDecimal maileTotal = new BigDecimal(0);
        if (goodsEntities.size() > 0) {
            Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
            maileTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            maileTotal = new BigDecimal(0);
        }

        //现金订货
        Map<String, Object> map1 = new HashMap<>();
        map1.put("purUserId", userId);
        map1.put("date", date);
        map1.put("payType", 0);
        List<NxDistributerPurchaseBatchEntity> entitiesCash = nxDPBService.queryDisPurchaseBatch(map1);
        BigDecimal batchCashTotal = new BigDecimal(0);
        if (entitiesCash.size() > 0) {
            Double aDouble = nxDPBService.queryPurchaserCashTotal(map1);
            batchCashTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            batchCashTotal = new BigDecimal(0);
        }


        Map<String, Object> map2 = new HashMap<>();
        map2.put("purUserId", userId);
        map2.put("date", date);
        map2.put("payType", 1);
        List<NxDistributerPurchaseBatchEntity> entitiesBill = nxDPBService.queryDisPurchaseBatch(map2);
        BigDecimal batchBillTotal = new BigDecimal(0);
        if (entitiesBill.size() > 0) {
            Double aDouble = nxDPBService.queryPurchaserCashTotal(map2);
            batchBillTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            batchBillTotal = new BigDecimal(0);
        }

        BigDecimal add = maileTotal.add(batchCashTotal).add(batchBillTotal).setScale(2, BigDecimal.ROUND_HALF_UP);

        Map<String, Object> map123 = new HashMap<>();
        map123.put("total", add);
        map123.put("maileTotal", maileTotal);
        map123.put("batchCashTotal", batchCashTotal);
        map123.put("batchBillTotal", batchBillTotal);
        map123.put("maileArr", goodsEntities);
        map123.put("batchCashArr", entitiesCash);
        map123.put("batchBillArr", entitiesBill);

        return R.ok().put("data", map123);
    }


    @RequestMapping(value = "/disUserGetPurchasePurGoods/{userId}")
    @ResponseBody
    public R disUserGetPurchasePurGoods(@PathVariable Integer userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("purUserId", userId);
        map.put("dayuStatus", 1);
        map.put("batchId", 0);
        List<NxDistributerPurchaseGoodsEntity> nxDistributerPurchaseGoodsEntities = nxDisPurcGoodsService.queryPurchaseGoodsByParams(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("month", formatWhatMonth(0));
        map1.put("arr", nxDistributerPurchaseGoodsEntities);


        List<Map<String, Object>> result = new ArrayList<>();
        result.add(map1);
//        result.add(map3);
//        result.add(map5);
        return R.ok().put("data", result);
    }


    /**
     * disUser
     *
     * @param searchStr
     * @param disId
     * @return
     */
    @RequestMapping(value = "/queryDisPurGoodsByQuickSearch", method = RequestMethod.POST)
    @ResponseBody
    public R queryDisPurGoodsByQuickSearch(String searchStr, String disId) {

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        for (int i = 0; i < searchStr.length(); i++) {
            String str = searchStr.substring(i, i + 1);
            if (str.matches("[\u4E00-\u9FFF]")) {
                String pinyin = hanziToPinyin(searchStr);
                map.put("searchStr", searchStr);
                map.put("searchPinyin", pinyin);
            } else {
                map.put("searchPinyin", searchStr);
            }
        }

        List<NxDistributerGoodsEntity> distributerGoodsEntities = nxDgService.queryDisPurGoodsQuickSearchStr(map);

        return R.ok().put("data", distributerGoodsEntities);
    }



    @RequestMapping(value = "/purUserUpdatePurPrice", method = RequestMethod.POST)
    @ResponseBody
    public R purUserUpdatePurPrice(@RequestBody NxDistributerPurchaseGoodsEntity purgoods) {
        BigDecimal buyPrice = new BigDecimal(purgoods.getNxDpgBuyPrice());

        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getNxDepartmentOrdersEntities();
        if (nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                NxDepartmentOrdersEntity tableOrder = nxDepartmentOrdersService.queryObject(orders.getNxDepartmentOrdersId());
                tableOrder.setNxDoCostPriceUpdate(formatWhatDay(0));
                tableOrder.setNxDoCostPrice(purgoods.getNxDpgBuyPrice());
                if (tableOrder.getNxDoPrice() != null) {
                    //profit
                    BigDecimal nxDoPriceB = new BigDecimal(tableOrder.getNxDoPrice());
                    BigDecimal profitB = nxDoPriceB.subtract(buyPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                    BigDecimal scaleB = profitB.divide(nxDoPriceB, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                    tableOrder.setNxDoProfitScale(scaleB.toString());

                }
                if(tableOrder.getNxDoWeight() != null){
                    //cost
                    BigDecimal weightB = new BigDecimal(tableOrder.getNxDoWeight());
                    BigDecimal decimal = buyPrice.multiply(weightB).setScale(1, BigDecimal.ROUND_HALF_UP);
                    tableOrder.setNxDoCostSubtotal(decimal.toString());
                    if(tableOrder.getNxDoPrice() != null){
                        BigDecimal nxDoPriceB = new BigDecimal(tableOrder.getNxDoPrice());
                        BigDecimal profitB = nxDoPriceB.subtract(buyPrice).setScale(1, BigDecimal.ROUND_HALF_UP);
                        BigDecimal decimal1 = profitB.multiply(new BigDecimal(tableOrder.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
                        tableOrder.setNxDoProfitSubtotal(decimal1.toString());
                    }
                }

                nxDepartmentOrdersService.update(tableOrder);

            }
        }

        nxDisPurcGoodsService.update(purgoods);

        updateDisGoodsPriceThree(purgoods);

        return R.ok();
    }
    @RequestMapping(value = "/disUserFinishPurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disUserFinishPurGoods(@RequestBody NxDistributerPurchaseGoodsEntity purgoods) {
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getNxDepartmentOrdersEntities();
        if (nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                orders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishPurchase());
                orders.setNxDoCostPriceUpdate(formatWhatDayString(0));
                nxDepartmentOrdersService.update(orders);
                if (orders.getNxDoNxRestrauntOrderId() != null && orders.getNxDoNxRestrauntOrderId() > 0) {
                    transUpdateNxRestrauntOrder(orders);
                }

                if (orders.getNxDoGbDepartmentOrderId() != null && orders.getNxDoGbDepartmentOrderId() > 0) {
                    transUpdateGbDepartmentOrder(orders);
                }
            }
        }
        purgoods.setNxDpgPurchaseDate(formatWhatDay(0));
        purgoods.setNxDpgBatchId(-1);
        purgoods.setNxDpgPayType(0);
        purgoods.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
        nxDisPurcGoodsService.update(purgoods);


        updateDisGoodsPriceThree(purgoods);

//        NxDistributerGoodsEntity nxDistributerGoodsEntity = purgoods.getNxDistributerGoodsEntity();
//        nxDistributerGoodsEntity.setNxDgBuyingPriceUpdate(formatWhatFullTime(0));
//        nxDgService.update(nxDistributerGoodsEntity);

        return R.ok();
    }

    private void transUpdateGbDepartmentOrder(NxDepartmentOrdersEntity ordersEntity) {

        //更新gbDepOrder
        Integer nxDepartmentOrdersId = ordersEntity.getNxDepartmentOrdersId();
        if (nxDepartmentOrdersId != null) {
            GbDepartmentOrdersEntity gbDepartmentOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(nxDepartmentOrdersId);
            gbDepartmentOrdersEntity.setGbDoPrice(ordersEntity.getNxDoPrice());
            gbDepartmentOrdersEntity.setGbDoWeight(ordersEntity.getNxDoWeight());
            gbDepartmentOrdersEntity.setGbDoSubtotal(ordersEntity.getNxDoSubtotal());
            gbDepartmentOrdersEntity.setGbDoBuyStatus(ordersEntity.getNxDoPurchaseStatus());
            gbDepartmentOrdersEntity.setGbDoStatus(ordersEntity.getNxDoStatus());

            //sellingData
            Integer gbDoDepDisGoodsId = gbDepartmentOrdersEntity.getGbDoDepDisGoodsId();
            GbDepartmentDisGoodsEntity departmentDisGoodsEntity = gbDepartmentDisGoodsService.queryObject(gbDoDepDisGoodsId);
            String gbDdgSellingPrice = departmentDisGoodsEntity.getGbDdgSellingPrice();
            if (gbDdgSellingPrice != null) {
                gbDepartmentOrdersEntity.setGbDoSellingPrice(gbDdgSellingPrice);
                BigDecimal multiply = new BigDecimal(gbDdgSellingPrice).multiply(new BigDecimal(ordersEntity.getNxDoWeight()));
                gbDepartmentOrdersEntity.setGbDoSellingPrice(gbDdgSellingPrice);
                gbDepartmentOrdersEntity.setGbDoSellingSubtotal(multiply.toString());
            }
            gbDepartmentOrdersService.update(gbDepartmentOrdersEntity);
        }

    }

    private void transUpdateNxRestrauntOrder(NxDepartmentOrdersEntity nxOrdersEntity) {
        Integer nxDoNxRestrauntOrderId = nxOrdersEntity.getNxDoNxRestrauntOrderId();
        if (nxDoNxRestrauntOrderId != null) {
            NxRestrauntOrdersEntity restrauntOrdersEntity = nxRestrauntOrdersService.queryObject(nxDoNxRestrauntOrderId);
            restrauntOrdersEntity.setNxRoWeight(nxOrdersEntity.getNxDoWeight());
            restrauntOrdersEntity.setNxRoCostPrice(nxOrdersEntity.getNxDoPrice());
            BigDecimal decimal = new BigDecimal(nxOrdersEntity.getNxDoPrice()).multiply(new BigDecimal(nxOrdersEntity.getNxDoWeight())).setScale(1, BigDecimal.ROUND_HALF_UP);
            restrauntOrdersEntity.setNxRoCostSubtotal(decimal.toString());
            restrauntOrdersEntity.setNxRoBuyStatus(nxOrdersEntity.getNxDoPurchaseStatus());
            restrauntOrdersEntity.setNxRoStatus(nxOrdersEntity.getNxDoStatus());
            nxRestrauntOrdersService.update(restrauntOrdersEntity);

            Integer comGoodsId = restrauntOrdersEntity.getNxRoComGoodsId();
            NxCommunityGoodsEntity nxCommunityGoodsEntity = nxCommunityGoodsService.queryObject(comGoodsId);
            nxCommunityGoodsEntity.setNxCgBuyingPrice(nxOrdersEntity.getNxDoPrice());
            nxCommunityGoodsService.update(nxCommunityGoodsEntity);
        }
    }


    @RequestMapping(value = "/deleteIsPurchase", method = RequestMethod.POST)
    @ResponseBody
    public R deleteIsPurchase(@RequestBody NxDistributerPurchaseGoodsEntity purgoods) {
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purgoods.getNxDepartmentOrdersEntities();
        if (nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                orders.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
                orders.setNxDoCostPrice(null);
                nxDepartmentOrdersService.update(orders);
                //更新restraunt订单数据
                deleteUpdateNxRestrauntOrderData(orders);
            }
        }
        purgoods.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
        purgoods.setNxDpgBuyPrice(null);
        nxDisPurcGoodsService.update(purgoods);
        return R.ok();
    }


    /**
     * jrdh app
     *
     * @param fatherId
     * @return
     */
    @RequestMapping(value = "/nxPurchaserGetPurchaseGoodsByFatherId/{fatherId}")
    @ResponseBody
    public R nxPurchaserGetPurchaseGoodsByFatherId(@PathVariable Integer fatherId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("fatherId", fatherId);
        map4.put("status", 1);
        map4.put("weightId", -1);
        map4.put("batchId", -1);
//        map4.put("inputType", -1);
        System.out.println("map444" + map4);
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);
        int orderCount = 0;
        if (purchaseToday.size() > 0) {
            for (NxDistributerFatherGoodsEntity fatherGoodsEntity : purchaseToday) {
                Integer distributerFatherGoodsId = fatherGoodsEntity.getNxDistributerFatherGoodsId();
                map4.put("grandId", distributerFatherGoodsId);
                map4.put("purType", 1);
                Integer integer = nxDisPurcGoodsService.queryPurOrderCount(map4);
                fatherGoodsEntity.setPurOrderCount(integer);
                orderCount = orderCount + integer;
            }
        }
        return R.ok().put("data", purchaseToday);
    }



    @RequestMapping(value = "/disGetTypePreparePurGoodsPage", method = RequestMethod.POST)
    @ResponseBody
    public R disGetTypePreparePurGoodsPage(Integer disId,
                                 Integer page,
                                 Integer limit ) {
        logger.info("[disGetTypePreparePurGoodsPage] 开始查询，参数: disId={}, page={}, limit={}", disId, page, limit);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 2);
        map.put("orderPurStatus", 4);
        map.put("offset", (page - 1) * limit);
        map.put("limit", limit);
        map.put("batchId", -1);
        logger.info("[disGetTypePreparePurGoodsPage] 查询条件 map: {}", map);
        logger.info("[disGetTypePreparePurGoodsPage] 使用超简化版查询，减少数据传输量");
        
        // 使用超简化版查询，字段扁平化，大幅减少数据传输量
        List<com.nongxinle.entity.PurchaseGoodsSimpleDTO> purchaseGoodsSimpleList = nxDisPurcGoodsService.queryPurchaseGoodsWithOrdersUltraSimple(map);
        logger.info("[disGetTypePreparePurGoodsPage] 查询结果数量: {}", purchaseGoodsSimpleList != null ? purchaseGoodsSimpleList.size() : 0);
        
        if (purchaseGoodsSimpleList != null && !purchaseGoodsSimpleList.isEmpty()) {
            for (int i = 0; i < Math.min(purchaseGoodsSimpleList.size(), 5); i++) {
                com.nongxinle.entity.PurchaseGoodsSimpleDTO purGoods = purchaseGoodsSimpleList.get(i);
                logger.info("[disGetTypePreparePurGoodsPage]   采购商品[{}]: id={}, name={}, purchaseAuto={}, 订单数量={}, 曾祖父ID={}, 曾祖父Sort={}", 
                    i,
                    purGoods.getNxDistributerGoodsId(),
                    purGoods.getNxDgGoodsName(),
                    purGoods.getNxDgPurchaseAuto(),
                    purGoods.getOrders() != null ? purGoods.getOrders().size() : 0,
                    purGoods.getNxDgDfgGoodsGreatGrandId(),
                    purGoods.getNxDgDfgGoodsGreatGrandSort());
            }
        }

        Map<String, Object> mapCount = new HashMap<>();
        mapCount.put("disId", disId);
        mapCount.put("status", 1);
        mapCount.put("batchId", -1);
        Integer integer = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapCount);
        logger.info("[disGetTypePreparePurGoodsPage] 总数: {}", integer);

        PageUtils pageUtil = new PageUtils(purchaseGoodsSimpleList, integer, limit, page);
        logger.info("[disGetTypePreparePurGoodsPage] 返回数据完成（使用简化DTO，数据传输量大幅减少）");
        return R.ok().put("page", pageUtil);
    }

    @RequestMapping(value = "/getDisInputPurGoodsTx", method = RequestMethod.POST)
    @ResponseBody
    public R getDisInputPurGoodsTx(Integer disId, Integer type) {
        Map<String, Object> mapD = new HashMap<>();
        mapD.put("disId", disId);
        mapD.put("status", 1);
        mapD.put("batchId", -1);
//        map4.put("equalInputType", type);
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(mapD);
//        if (purchaseToday.size() > 0) {
//            for (NxDistributerFatherGoodsEntity fatherGoodsEntity : purchaseToday) {
//                Map<String, Object> mapF = new HashMap<>();
//                mapF.put("grandId", fatherGoodsEntity.getNxDistributerFatherGoodsId());
//                mapF.put("status", 3);
//                mapF.put("purType", 1);
//                mapF.put("equalPurStatus", 1);
//                Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(mapF);
//                fatherGoodsEntity.setNewOrderCount(integer);
//            }
//        }

//
        Map<String, Object> map1 = new HashMap<>();
        map1.put("disId", disId);
        map1.put("status", 3);
        map1.put("purType", 1);
        map1.put("equalPurStatus", 1);
        // 未采购
        int unPurCount = nxDepartmentOrdersService.disGetPurchaseGoodsApplysCount(map1);

        Map<String, Object> map111 = new HashMap<>();

        map111.put("arr", purchaseToday);
        map1.put("equalPurStatus", null);
        map1.put("purType", null);
        map1.put("dayuPurStatus", 1);
        map1.put("purStatus", 3);

        System.out.println("wokakkkskks" + map1);

        Integer integer = nxDepartmentOrdersService.queryDepOrdersAcount(map1);
        map111.put("unPurCount", unPurCount);
        map111.put("isBatchCountUnRepaly", integer);

        return R.ok().put("data", map111);
    }


    @RequestMapping(value = "/getDisInputPurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R getDisInputPurGoods(Integer disId, Integer type) {
        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("status", 1);
        map4.put("weightId", -1);
        map4.put("batchId", -1);
        map4.put("equalInputType", type);
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);

        //
        Integer wxUnBatchCount = nxDisPurcGoodsService.queryPurOrderCount(map4);

        // map4 订货已发送
        map4.put("status", 2); //NX_DIS_PURCHASE_GOODS_IS_PURCHASE == 2 huifu
        map4.put("orderStatus", 3);
        map4.put("batchId", 1);
        map4.put("dayuPurStatus", 1);
        map4.put("purStatus", 3);
        Integer wxIsBatchCountUnReply = nxDisPurcGoodsService.queryPurOrderCount(map4);
        map4.put("status", 4);
        map4.put("dayuStatus", 1);
        Integer wxIsBatchCountHaveReply = nxDisPurcGoodsService.queryPurOrderCount(map4);


        map4.put("batchId", -1);
        map4.put("weightId", 1);
        Integer wxIsPrintCount = nxDisPurcGoodsService.queryPurOrderCount(map4);
        map4.put("equalBuyStatus", 4);
        Integer wxIsPrintFinishCount = nxDisPurcGoodsService.queryPurOrderCount(map4);


        Map<String, Object> map41 = new HashMap<>();
        map41.put("disId", disId);
        map41.put("status", 1);
        map41.put("weightId", -1);
        map41.put("batchId", -1);
        map41.put("inputType", -1);
        System.out.println("map444aaa" + map4);
        Integer orderCount = nxDisPurcGoodsService.queryPurOrderCount(map41);


        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 4);
        map.put("equalInputType", 1);
        Integer wxOrderCount = nxDisPurcGoodsService.queryPurOrderCount(map);
        map.put("equalBuyStatus", 4);
        Integer wxOrderFinishCount = nxDisPurcGoodsService.queryPurOrderCount(map);

        Map<String, Object> mapP = new HashMap<>();
        mapP.put("disId", disId);
        mapP.put("orderStatus", 3);
        mapP.put("equalInputType", 0);
        Integer printOrderCount = nxDisPurcGoodsService.queryPurOrderCount(mapP);
        mapP.put("equalBuyStatus", 4);
        Integer printOrderFinishCount = nxDisPurcGoodsService.queryPurOrderCount(mapP);


        Map<String, Object> map111 = new HashMap<>();

        Map<String, Object> mapW = new HashMap<>();
        mapW.put("disId", disId);
        mapW.put("type", 2);
        int count = nxDistributerWeightService.queryWeightCountByParams(mapW);
        BigDecimal trade = new BigDecimal(count).add(new BigDecimal(1));
        String s = formatDayNumber(0) + "CGD" + trade;
        map111.put("arr", purchaseToday);
        map111.put("newOrderCount", orderCount);
        map111.put("wxOrderCount", wxOrderCount);
        map111.put("wxOrderFinishCount", wxOrderFinishCount);
        map111.put("printOrderCount", printOrderCount);
        map111.put("printOrderFinishCount", printOrderFinishCount);
        map111.put("wxUnBatchCount", wxUnBatchCount);
        map111.put("isBatchCountUnRepaly", wxIsBatchCountUnReply);
        map111.put("isBatchCountHaveRepaly", wxIsBatchCountHaveReply);
        map111.put("wxIsPrintCount", wxIsPrintCount);
        map111.put("wxIsPrintFinishCount", wxIsPrintFinishCount);
        map111.put("tradeNo", s);

        return R.ok().put("data", map111);
    }

    /**
     * jrdh app
     *
     * @param disId
     * @return
     */
    @RequestMapping(value = "/nxPurchaserGetPurchaseGoodsWithBatchCount",method = RequestMethod.POST)
    @ResponseBody
    public R nxPurchaserGetPurchaseGoodsWithBatchCount(Integer disId, Integer status) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("status", status);

        map4.put("weightId", -1);
        map4.put("batchId", -1);
//        map4.put("inputType", -1);
        System.out.println("map444aaabbbbbbbbbbb" + map4);
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoodsGreat(map4);
        int orderCount = 0;
        if (purchaseToday.size() > 0) {
            for (NxDistributerFatherGoodsEntity greatGoodsEntity : purchaseToday) {
                int greatCount = 0;
                int goodsCount = 0;
                List<NxDistributerFatherGoodsEntity> fatherGoodsEntities = greatGoodsEntity.getFatherGoodsEntities();
                if (fatherGoodsEntities.size() > 0) {
                    for (NxDistributerFatherGoodsEntity fatherGoodsEntity : fatherGoodsEntities) {
                        Integer distributerFatherGoodsId = fatherGoodsEntity.getNxDistributerFatherGoodsId();
                        map4.put("grandId", distributerFatherGoodsId);
                        map4.put("purType", 1);
                        System.out.println("abckckc" + map4);
                        Integer integer = nxDisPurcGoodsService.queryPurOrderCount(map4);
                        greatCount = greatCount + integer;
                        orderCount = orderCount + integer;
                        goodsCount = goodsCount + fatherGoodsEntity.getNxDistributerPurchaseGoodsEntities().size();
                    }

                }
                greatGoodsEntity.setPurOrderCount(greatCount);
                greatGoodsEntity.setStockOrderCount(goodsCount);
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("orderStatus", 3);
        map.put("equalInputType", 1);
        System.out.println("kdfjadfaklfaf" + map);
        Integer wxOrderCount = nxDisPurcGoodsService.queryPurOrderCount(map);
        map.put("equalBuyStatus", 4);
        Integer wxOrderFinishCount = nxDisPurcGoodsService.queryPurOrderCount(map);

        Map<String, Object> mapP = new HashMap<>();
        mapP.put("disId", disId);
        mapP.put("orderStatus", 3);
        mapP.put("equalInputType", 0);
        Integer printOrderCount = nxDisPurcGoodsService.queryPurOrderCount(mapP);
        mapP.put("equalBuyStatus", 4);
        Integer printOrderFinishCount = nxDisPurcGoodsService.queryPurOrderCount(mapP);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("arr", purchaseToday);
        map1.put("newOrderCount", orderCount);
        map1.put("wxOrderCount", wxOrderCount);
        map1.put("wxOrderFinishCount", wxOrderFinishCount);
        map1.put("printOrderCount", printOrderCount);
        map1.put("printOrderFinishCount", printOrderFinishCount);
        return R.ok().put("data", map1);
    }

//    @RequestMapping(value = "/jrdhGetPurchaseOrderGoodsWithBatchCount/{disId}")
//    @ResponseBody
//    public R jrdhGetPurchaseOrderGoodsWithBatchCount(@PathVariable Integer disId) {
//
//        Map<String, Object> map4 = new HashMap<>();
//        map4.put("disId", disId);
//        map4.put("status", 1);
//        map4.put("weightId", -1);
//        map4.put("purchaseType", 1);
//        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("disId", disId);
//        map.put("orderStatus", 3);
//        map.put("purchaseType", 1);
//        map.put("equalStatus", 0);
//        map.put("weightId", -1);
//        map.put("batchId", -1);
//        System.out.println("purorder" + map);
//        Integer purOrderCount = nxDisPurcGoodsService.queryPurOrderCount(map);
//        map.put("batchId", 1);
//        Integer batchOrderCount = nxDisPurcGoodsService.queryPurOrderCount(map);
//
//        Map<String, Object> map1 = new HashMap<>();
//        map1.put("arr", purchaseToday);
//        map1.put("purOrderCount", purOrderCount);
//        map1.put("batchOrderCount", batchOrderCount);
//
//        return R.ok().put("data", map1);
//    }

    /**
     * DISTRIBUTE
     * 批发商获取进货商品列表
     *
     * @param disId 批发商id getHandPurchase
     * @return 进货商品列表
     */
    @RequestMapping(value = "/disGetPurchaseData/{disId}")
    @ResponseBody
    public R disGetPurchaseData(@PathVariable Integer disId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("status", 1);
//        map4.put("weightId", -1);
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);
        int orderCount = 0;
        if (purchaseToday.size() > 0) {
            for (NxDistributerFatherGoodsEntity fatherGoodsEntity : purchaseToday) {
                Integer distributerFatherGoodsId = fatherGoodsEntity.getNxDistributerFatherGoodsId();
                map4.put("grandId", distributerFatherGoodsId);
                map4.put("purType", 1);
                Integer integer = nxDisPurcGoodsService.queryPurOrderCount(map4);
                fatherGoodsEntity.setPurOrderCount(integer);
                orderCount = orderCount + integer;
            }
        }

        return R.ok().put("data", purchaseToday);
    }

    @RequestMapping(value = "/disGetIsPurchaseData/{disId}")
    @ResponseBody
    public R disGetIsPurchaseData(@PathVariable Integer disId) {

        Map<String, Object> map4 = new HashMap<>();
        map4.put("disId", disId);
        map4.put("equalStatus", getNxDepOrderBuyStatusIsPurchase());
        List<NxDistributerFatherGoodsEntity> purchaseToday = nxDisPurcGoodsService.queryDisPurchaseGoods(map4);
        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("status", 2);
        int batchCount = nxDPBService.queryDisPurchaseBatchCount(map);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("purArr", purchaseToday);
        map1.put("batchCount", batchCount);
        return R.ok().put("data", map1);
    }





    /**
     * 供货商填写数量和单价
     * order 的buyStatus == 2
     *
     * @param purchaseGoodsEntity
     * @return
     */
    @RequestMapping(value = "/sellerSavePurGoodsWeight", method = RequestMethod.POST)
    @ResponseBody
    public R sellerSavePurGoodsWeight(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));

        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purchaseGoodsEntity.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            Integer nxDepartmentOrdersId = orders.getNxDepartmentOrdersId();
            NxDepartmentOrdersEntity tableOrderEntity = nxDepartmentOrdersService.queryObjectNew(nxDepartmentOrdersId);
            tableOrderEntity.setNxDoWeight(orders.getNxDoWeight());
            tableOrderEntity.setNxDoCostPrice(orders.getNxDoCostPrice());
            tableOrderEntity.setNxDoCostSubtotal(orders.getNxDoCostSubtotal());
            tableOrderEntity.setNxDoCostPriceUpdate(formatWhatDay(0));
            tableOrderEntity.setNxDoSubtotal(orders.getNxDoSubtotal());
            tableOrderEntity.setNxDoProfitSubtotal(orders.getNxDoProfitSubtotal());
            tableOrderEntity.setNxDoProfitScale(orders.getNxDoProfitScale());
            tableOrderEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusIsPurchase());
            nxDepartmentOrdersService.update(tableOrderEntity);
        }
        nxDisPurcGoodsService.update(purchaseGoodsEntity);
        return R.ok().put("data", purchaseGoodsEntity);
    }


    

    @RequestMapping(value = "/sellerSavePurGoodsPrice", method = RequestMethod.POST)
    @ResponseBody
    public R sellerSavePurGoodsPrice(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        Integer disGoodsId = purchaseGoodsEntity.getNxDpgDisGoodsId();
        String nxDpgBuyPrice = purchaseGoodsEntity.getNxDpgBuyPrice();

        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDgService.queryObject(disGoodsId);
        nxDistributerGoodsEntity.setNxDgBuyingPrice(nxDpgBuyPrice);
        nxDistributerGoodsEntity.setNxDgBuyingPriceUpdate(formatWhatDay(0));
        nxDgService.update(nxDistributerGoodsEntity);

//        //update PurGoods
//        Map<String, Object> map = new HashMap<>();
//        map.put("disGoodsId",disGoodsId);
//        map.put("status", 2);
//        List<NxDepartmentOrdersEntity> ordersEntities = nxDepartmentOrdersService.queryDisOrdersByParams(map);
//
//        if(ordersEntities.size() > 0){
//            for(NxDepartmentOrdersEntity ordersEntity: ordersEntities){
//                BigDecimal orderWeight = new BigDecimal(ordersEntity.getNxDoWeight());
//
//                if(ordersEntity.getNxDoPrice() != null && !ordersEntity.getNxDoPrice().equals("0") && !ordersEntity.getNxDoPrice().equals("0.0")){
//                    BigDecimal orderPrice = new BigDecimal(ordersEntity.getNxDoPrice());
//                    BigDecimal newSubtotal = orderWeight.multiply(orderPrice);
//                    BigDecimal newCostPrice = new BigDecimal(nxDpgBuyPrice);
//                    BigDecimal newScale = orderPrice.subtract(newCostPrice).divide(orderPrice, 2, BigDecimal.ROUND_HALF_UP);
//                    BigDecimal newCostSubtotal = orderWeight.multiply(newCostPrice);
//                    BigDecimal newProfit = newSubtotal.subtract(newCostSubtotal);
//                    ordersEntity.setNxDoCostSubtotal(newCostSubtotal.toString());
//                    ordersEntity.setNxDoProfitSubtotal(newProfit.toString());
//                    ordersEntity.setNxDoProfitScale(newScale.toString());
//                }
//
//                ordersEntity.setNxDoCostPrice(nxDpgBuyPrice);
//                System.out.println("updateororroror");
//                nxDepartmentOrdersService.update(ordersEntity);
//
//            }
//        }


        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purchaseGoodsEntity.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
        for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
            Integer nxDepartmentOrdersId = orders.getNxDepartmentOrdersId();
            NxDepartmentOrdersEntity tableOrderEntity = nxDepartmentOrdersService.queryObjectNew(nxDepartmentOrdersId);
            tableOrderEntity.setNxDoSubtotal(orders.getNxDoSubtotal());
            tableOrderEntity.setNxDoWeight(orders.getNxDoWeight());
            tableOrderEntity.setNxDoCostPrice(orders.getNxDoCostPrice());
            tableOrderEntity.setNxDoCostSubtotal(orders.getNxDoCostSubtotal());
            tableOrderEntity.setNxDoCostPriceUpdate(formatWhatDay(0));
            tableOrderEntity.setNxDoSubtotal(orders.getNxDoSubtotal());
            tableOrderEntity.setNxDoProfitSubtotal(orders.getNxDoProfitSubtotal());
            tableOrderEntity.setNxDoProfitScale(orders.getNxDoProfitScale());
            tableOrderEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusIsPurchase());
            nxDepartmentOrdersService.update(tableOrderEntity);
        }
        purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
        nxDisPurcGoodsService.update(purchaseGoodsEntity);
        return R.ok().put("data", purchaseGoodsEntity);
    }


    @RequestMapping(value = "/disIsPurchaseGoods", method = RequestMethod.POST)
    @ResponseBody
    public R disIsPurchaseGoods(@RequestBody NxDistributerPurchaseGoodsEntity purgoods) {
        System.out.println("disIsPurchaseGoodsdisIsPurchaseGoods");
        purgoods.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
        purgoods.setNxDpgPurchaseDate(formatWhatDay(0));
        purgoods.setNxDpgTime(formatWhatTime(0));
        System.out.println("stuautus====" + purgoods.getNxDpgStatus());
        nxDisPurcGoodsService.update(purgoods);

        NxDistributerGoodsEntity nxDistributerGoodsEntity = purgoods.getNxDistributerGoodsEntity();
        nxDgService.update(nxDistributerGoodsEntity);

        return R.ok();
    }


    @RequestMapping(value = "/savePlanPurchaseOrderBundle", method = RequestMethod.POST)
    @ResponseBody
    public R savePlanPurchaseOrderBundle(@RequestBody List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntityList) {

        for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntityList) {

            purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
            purchaseGoodsEntity.setNxDpgApplyDate(formatWhatDay(0));
            purchaseGoodsEntity.setNxDpgOrdersAmount(purchaseGoodsEntity.getNxDepartmentOrdersEntities().size());
            purchaseGoodsEntity.setNxDpgFinishAmount(0);
            nxDisPurcGoodsService.save(purchaseGoodsEntity);

            Integer nxDpgDisGoodsId = purchaseGoodsEntity.getNxDpgDisGoodsId();
            NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDgService.queryObject(nxDpgDisGoodsId);
            nxDistributerGoodsEntity.setNxDgPurchaseAuto(1);
            nxDgService.update(nxDistributerGoodsEntity);

            Integer nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();

            List<NxDepartmentOrdersEntity> ordersEntities = purchaseGoodsEntity.getNxDepartmentOrdersEntities();
            if (ordersEntities.size() > 0) {
                for (NxDepartmentOrdersEntity order : ordersEntities) {
                    if(order.getPurSelected()){
                        Integer nxDepartmentOrdersId = order.getNxDepartmentOrdersId();
                        NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObjectNew(nxDepartmentOrdersId);
                        ordersEntity1.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
                        ordersEntity1.setNxDoGoodsType(purchaseGoodsEntity.getNxDpgInputType());
                        ordersEntity1.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
                        nxDepartmentOrdersService.update(ordersEntity1);
                    }
                }
            }
        }

        return R.ok();
    }


    @RequestMapping(value = "/savePlanPurchaseOrderBundle0", method = RequestMethod.POST)
    @ResponseBody
    public R savePlanPurchaseOrderBundle0(@RequestBody List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntityList) {

        for (NxDistributerPurchaseGoodsEntity purchaseGoodsEntity : purchaseGoodsEntityList) {

            Integer nxDistributerPurchaseGoodsId = 0;
            //判断是否有已经分的
            Integer nxDpgDisGoodsId = purchaseGoodsEntity.getNxDpgDisGoodsId();
            Integer nxDpgInputType = purchaseGoodsEntity.getNxDpgInputType();
            Map<String, Object> map = new HashMap<>();
            map.put("disGoodsId", nxDpgDisGoodsId);
            map.put("equalInputType", nxDpgInputType);
            map.put("equalStatus", 0);
            NxDistributerPurchaseGoodsEntity havePurGoods = nxDisPurcGoodsService.queryIfHavePurGoods(map);
            if (havePurGoods != null) {
                BigDecimal decimal = new BigDecimal(havePurGoods.getNxDpgOrdersAmount());
                BigDecimal decimal1 = new BigDecimal(purchaseGoodsEntity.getNxDepartmentOrdersEntities().size());
                havePurGoods.setNxDpgOrdersAmount(Integer.valueOf(decimal.add(decimal1).toString()));
                nxDisPurcGoodsService.update(havePurGoods);
                nxDistributerPurchaseGoodsId = havePurGoods.getNxDistributerPurchaseGoodsId();

            } else {
                purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
                purchaseGoodsEntity.setNxDpgApplyDate(formatWhatDay(0));
                purchaseGoodsEntity.setNxDpgOrdersAmount(purchaseGoodsEntity.getNxDepartmentOrdersEntities().size());
                purchaseGoodsEntity.setNxDpgFinishAmount(0);
                if(purchaseGoodsEntity.getNxDpgInputType() == 0){
                    purchaseGoodsEntity.setNxDpgBuyPrice(purchaseGoodsEntity.getNxDpgExpectPrice());
                }
                nxDisPurcGoodsService.save(purchaseGoodsEntity);

                nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();

                NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDgService.queryObject(nxDpgDisGoodsId);
                nxDistributerGoodsEntity.setNxDgPurchaseAuto(nxDpgInputType);
                nxDgService.update(nxDistributerGoodsEntity);

            }

            List<NxDepartmentOrdersEntity> ordersEntities = purchaseGoodsEntity.getNxDepartmentOrdersEntities();
            if (ordersEntities.size() > 0) {
                for (NxDepartmentOrdersEntity order : ordersEntities) {
                    if(order.getPurSelected()){
                        Integer nxDepartmentOrdersId = order.getNxDepartmentOrdersId();
                        NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObjectNew(nxDepartmentOrdersId);
                        ordersEntity1.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
                        ordersEntity1.setNxDoGoodsType(nxDpgInputType);
                        nxDepartmentOrdersService.update(ordersEntity1);
                    }
                }
            }
        }

        return R.ok();
    }

    @RequestMapping(value = "/deletePlanPurchase", method = RequestMethod.POST)
    @ResponseBody
    public R deletePlanPurchase(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purchaseGoodsEntity.getNxDepartmentOrdersEntities();
        if (nxDepartmentOrdersEntities != null && nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orders.getNxDepartmentOrdersId());
                ordersEntity.setNxDoPurchaseGoodsId(-1);
                ordersEntity.setNxDoGoodsType(-1);
                ordersEntity.setNxDoPurchaseStatus(1);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

        nxDisPurcGoodsService.delete(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());

        return R.ok();
    }

    @RequestMapping(value = "/deletePurchaserPlanPurchase", method = RequestMethod.POST)
    @ResponseBody
    public R deletePurchaserPlanPurchase(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purchaseGoodsEntity.getNxDepartmentOrdersEntities();
        if (nxDepartmentOrdersEntities != null && nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity orders : nxDepartmentOrdersEntities) {
                NxDepartmentOrdersEntity ordersEntity = nxDepartmentOrdersService.queryObject(orders.getNxDepartmentOrdersId());
                ordersEntity.setNxDoPurchaseGoodsId(-1);
                ordersEntity.setNxDoGoodsType(-1);
                ordersEntity.setNxDoPurchaseStatus(1);
                nxDepartmentOrdersService.update(ordersEntity);
            }
        }

        nxDisPurcGoodsService.delete(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());

//        Integer disGoodsId = purchaseGoodsEntity.getNxDpgDisGoodsId();
//        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDgService.queryObject(disGoodsId);
//        nxDistributerGoodsEntity.setNxDgPurchaseAuto(1);
//        nxDgService.update(nxDistributerGoodsEntity);

        return R.ok();
    }

    @RequestMapping(value = "/deletePlanPurchaseGoods/{id}")
    @ResponseBody
    public R deletePlanPurchaseGoods (@PathVariable Integer id) {
        nxDisPurcGoodsService.delete(id);
        return R.ok();
    }

    @RequestMapping(value = "/updatePurchaseGoods", method = RequestMethod.POST)
    @ResponseBody
    public R updatePurchaseGoods(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {
        nxDisPurcGoodsService.update(purchaseGoodsEntity);
        NxDistributerGoodsEntity nxDistributerGoodsEntity = purchaseGoodsEntity.getNxDistributerGoodsEntity();
        nxDgService.update(nxDistributerGoodsEntity);
        return R.ok().put("data", purchaseGoodsEntity);

    }
    @RequestMapping(value = "/givePurGoodsQuantity", method = RequestMethod.POST)
    @ResponseBody
    public R givePurGoodsQuantity(Integer id, String quantity, String standard, Integer level) {
        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDisPurcGoodsService.queryObject(id);
        purchaseGoodsEntity.setNxDpgQuantity(quantity);
        purchaseGoodsEntity.setNxDpgStandard(standard);
        purchaseGoodsEntity.setNxDpgCostLevel(level);
        nxDisPurcGoodsService.update(purchaseGoodsEntity);
        return R.ok().put("data", purchaseGoodsEntity);

    }




    /**
     * 修改进货商
     *
     * @param purchaseGoodsEntity 进货商品
     * @return 进货商品
     */
    @RequestMapping(value = "/updatePlanPurchase", method = RequestMethod.POST)
    @ResponseBody
    public R updatePlanPurchase(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {

        Integer nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
        List<NxDepartmentOrdersEntity> ordersEntities = purchaseGoodsEntity.getNxDepartmentOrdersEntities();
        if (ordersEntities != null && ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity order : ordersEntities) {
                order.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
                order.setNxDoPurchaseStatus(1);
                nxDepartmentOrdersService.update(order);
            }
            purchaseGoodsEntity.setNxDpgOrdersAmount(purchaseGoodsEntity.getNxDpgOrdersAmount() + ordersEntities.size());
        }
        nxDisPurcGoodsService.update(purchaseGoodsEntity);
        return R.ok().put("data", purchaseGoodsEntity);

    }



    @RequestMapping(value = "/savePlanPurchaseGoods", method = RequestMethod.POST)
    @ResponseBody
    public R savePlanPurchaseGoods(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {

        List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities = purchaseGoodsEntity.getNxDepartmentOrdersEntities();
        if (nxDepartmentOrdersEntities != null && nxDepartmentOrdersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity ordersEntity : nxDepartmentOrdersEntities) {
                if (ordersEntity.getPurSelected()) {
                    ordersEntity.setNxDoPurchaseGoodsId(-1);
                    ordersEntity.setNxDoPurchaseStatus(getNxDepOrderBuyStatusFinishPurchase());
                    ordersEntity.setNxDoCostPriceLevel(purchaseGoodsEntity.getNxDpgCostLevel().toString());
                    ordersEntity.setNxDoCostPriceUpdate(formatWhatDay(0));
                    nxDepartmentOrdersService.update(ordersEntity);
                }
            }
        }
        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
        purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
        purchaseGoodsEntity.setNxDpgFinishAmount(0);


        Integer nxDpgDisGoodsFatherId = purchaseGoodsEntity.getNxDpgDisGoodsFatherId();
        NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDpgDisGoodsFatherId);
        Integer nxDfgFathersFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
        purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDfgFathersFatherId);
        nxDisPurcGoodsService.save(purchaseGoodsEntity);
        return R.ok().put("data", purchaseGoodsEntity);
    }

    @RequestMapping(value = "/savePlanPurchase", method = RequestMethod.POST)
    @ResponseBody
    public R savePlanPurchase(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {

        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
        purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
        purchaseGoodsEntity.setNxDpgFinishAmount(0);

        Integer nxDpgDisGoodsFatherId = purchaseGoodsEntity.getNxDpgDisGoodsFatherId();
        NxDistributerFatherGoodsEntity fatherGoodsEntity = nxDistributerFatherGoodsService.queryObject(nxDpgDisGoodsFatherId);
        Integer nxDfgFathersFatherId = fatherGoodsEntity.getNxDfgFathersFatherId();
        purchaseGoodsEntity.setNxDpgDisGoodsGrandId(nxDfgFathersFatherId);
        nxDisPurcGoodsService.save(purchaseGoodsEntity);
        return R.ok().put("data", purchaseGoodsEntity);
    }


    @RequestMapping(value = "/saveShelfGoodsStockWithBatch", method = RequestMethod.POST)
    @ResponseBody
    public R saveShelfGoodsStockWithBatch (@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity ) {
        Integer nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
        NxDistributerPurchaseGoodsEntity olePurGoods = nxDisPurcGoodsService.queryObject(nxDistributerPurchaseGoodsId);

        olePurGoods.setNxDpgBuyPrice(purchaseGoodsEntity.getNxDpgBuyPrice());
        olePurGoods.setNxDpgBuyQuantity(purchaseGoodsEntity.getNxDpgBuyQuantity());
        olePurGoods.setNxDpgBuySubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
        olePurGoods.setNxDpgStatus(getNxDisPurchaseGoodsIsPurchase());
        olePurGoods.setNxDpgPurUserId(purchaseGoodsEntity.getNxDpgPurUserId());
        if(purchaseGoodsEntity.getNxDpgExpectPrice() != null){
            olePurGoods.setNxDpgExpectPrice(purchaseGoodsEntity.getNxDpgExpectPrice());
        }


        NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = batchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());
        if(nxDistributerPurchaseBatchEntity.getNxDpbPurchaseType() == 13){
           olePurGoods.setNxDpgPurchaseDate(formatWhatDay(0));
        }

        //
        if(!purchaseGoodsEntity.getIsShowTools()){
            System.out.println("ueueueu");
            NxDistributerGoodsShelfStockEntity stockEntity = new NxDistributerGoodsShelfStockEntity();

            stockEntity.setNxDgssNxPurGoodsId(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
            stockEntity.setNxDgssNxDistributerId(purchaseGoodsEntity.getNxDpgDistributerId());
            stockEntity.setNxDgssNxDisGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
            stockEntity.setNxDgssNxDisGoodsFatherId(purchaseGoodsEntity.getNxDpgDisGoodsFatherId());
            
            // 查询商品信息，获取外包装信息
            NxDistributerGoodsEntity disGoodsEntity = nxDgService.queryObject(purchaseGoodsEntity.getNxDpgDisGoodsId());
            System.out.println("========== [saveShelfGoodsStockWithBatch] 开始处理库存入库 ==========");
            System.out.println("商品ID: " + purchaseGoodsEntity.getNxDpgDisGoodsId());
            System.out.println("商品名称: " + (disGoodsEntity != null ? disGoodsEntity.getNxDgGoodsName() : "null"));
            
            // 获取采购信息
            String buyPriceStr = purchaseGoodsEntity.getNxDpgBuyPrice();
            String buyQuantityStr = purchaseGoodsEntity.getNxDpgBuyQuantity();
            String expectPriceStr = purchaseGoodsEntity.getNxDpgExpectPrice();
            String purchaseStandard = purchaseGoodsEntity.getNxDpgStandard();
            
            System.out.println("采购单价: " + buyPriceStr);
            System.out.println("采购数量: " + buyQuantityStr);
            System.out.println("采购规格: " + purchaseStandard);
            System.out.println("期望价格: " + expectPriceStr);
            
            BigDecimal buyPrice = buyPriceStr != null && !buyPriceStr.trim().isEmpty() 
                ? new BigDecimal(buyPriceStr) : BigDecimal.ZERO;
            BigDecimal buyQuantity = buyQuantityStr != null && !buyQuantityStr.trim().isEmpty() 
                ? new BigDecimal(buyQuantityStr) : BigDecimal.ZERO;
            BigDecimal expectPrice = expectPriceStr != null && !expectPriceStr.trim().isEmpty() 
                ? new BigDecimal(expectPriceStr) : null;
            
            // 检查是否有外包装信息
            boolean hasCarton = disGoodsEntity != null 
                && disGoodsEntity.getNxDgCartonUnit() != null 
                && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                && disGoodsEntity.getNxDgItemsPerCarton() != null 
                && disGoodsEntity.getNxDgItemsPerCarton() > 0;
            
            if (hasCarton) {
                System.out.println("✅ 商品有外包装信息:");
                System.out.println("  外包装单位: " + disGoodsEntity.getNxDgCartonUnit());
                System.out.println("  每箱数量: " + disGoodsEntity.getNxDgItemsPerCarton() + "个");
            } else {
                System.out.println("⚠️ 商品无外包装信息，按最小单位处理");
            }
            
            // 判断采购规格是否与外包装单位匹配
            boolean isCartonStandard = hasCarton 
                && purchaseStandard != null 
                && purchaseStandard.trim().equals(disGoodsEntity.getNxDgCartonUnit().trim());
            
            if (hasCarton) {
                System.out.println("采购规格匹配检查: " + purchaseStandard + " == " + disGoodsEntity.getNxDgCartonUnit() 
                    + " ? " + (isCartonStandard ? "✅ 匹配（使用外包装单位）" : "❌ 不匹配（使用最小单位）"));
            }
            
            if (hasCarton) {
                // 有外包装：需要计算双单价和双零售价
                Integer itemsPerCarton = disGoodsEntity.getNxDgItemsPerCarton();
                BigDecimal itemsPerCartonBD = new BigDecimal(itemsPerCarton);
                
                // 1. 数量转换：按最小单位存储
                BigDecimal weightInMinUnit;
                BigDecimal actualCartonCount; // 实际箱数
                if (isCartonStandard) {
                    // 如果采购规格是外包装单位（如"箱"），说明前端传入的数量是箱数
                    // 例如：规格="箱"，数量=3箱，每箱200个，则入库数量=3*200=600个
                    actualCartonCount = buyQuantity;
                    weightInMinUnit = buyQuantity.multiply(itemsPerCartonBD);
                    System.out.println("📦 数量转换（外包装单位）:");
                    System.out.println("  采购数量: " + buyQuantity + "箱");
                    System.out.println("  每箱数量: " + itemsPerCarton + "个");
                    System.out.println("  入库数量: " + weightInMinUnit + "个");
                    System.out.println("  计算公式: " + buyQuantity + "箱 × " + itemsPerCarton + "个/箱 = " + weightInMinUnit + "个");
                } else {
                    // 如果采购规格不是外包装单位，说明前端传入的数量已经是按最小单位的
                    actualCartonCount = buyQuantity.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                    weightInMinUnit = buyQuantity;
                    System.out.println("📦 数量转换（最小单位）:");
                    System.out.println("  采购数量: " + buyQuantity + "个");
                    System.out.println("  折合箱数: " + actualCartonCount + "箱");
                    System.out.println("  入库数量: " + weightInMinUnit + "个");
                }
                stockEntity.setNxDgssWeight(weightInMinUnit.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                stockEntity.setNxDgssRestWeight(weightInMinUnit.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                
                // 2. 单价计算
                // 如果采购规格是外包装单位，采购单价就是外包装单价
                if (isCartonStandard) {
                    // 采购单价是外包装单价
                    stockEntity.setNxDgssPriceCarton(buyPrice.stripTrailingZeros().toPlainString());
                    // 计算最小单位单价
                    BigDecimal priceInMinUnit = buyPrice.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                    stockEntity.setNxDgssPrice(priceInMinUnit.stripTrailingZeros().toPlainString());
                    System.out.println("💰 单价计算（外包装单位）:");
                    System.out.println("  采购单价: " + buyPrice + "元/箱");
                    System.out.println("  外包装单价: " + stockEntity.getNxDgssPriceCarton() + "元/箱");
                    System.out.println("  最小单位单价: " + stockEntity.getNxDgssPrice() + "元/个");
                    System.out.println("  计算公式: " + buyPrice + "元/箱 ÷ " + itemsPerCarton + "个/箱 = " + stockEntity.getNxDgssPrice() + "元/个");
                } else {
                    // 采购单价是最小单位单价
                    stockEntity.setNxDgssPrice(buyPrice.stripTrailingZeros().toPlainString());
                    // 计算外包装单价
                    BigDecimal priceCarton = buyPrice.multiply(itemsPerCartonBD);
                    stockEntity.setNxDgssPriceCarton(priceCarton.stripTrailingZeros().toPlainString());
                    System.out.println("💰 单价计算（最小单位）:");
                    System.out.println("  采购单价: " + buyPrice + "元/个");
                    System.out.println("  最小单位单价: " + stockEntity.getNxDgssPrice() + "元/个");
                    System.out.println("  外包装单价: " + stockEntity.getNxDgssPriceCarton() + "元/箱");
                    System.out.println("  计算公式: " + buyPrice + "元/个 × " + itemsPerCarton + "个/箱 = " + stockEntity.getNxDgssPriceCarton() + "元/箱");
                }
                
                // 3. 建议零售价计算
                if (expectPrice != null) {
                    if (isCartonStandard) {
                        // 期望价格是外包装建议零售价
                        stockEntity.setNxDgssSellingPriceCarton(expectPrice.stripTrailingZeros().toPlainString());
                        // 计算最小单位建议零售价
                        BigDecimal sellingPriceInMinUnit = expectPrice.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                        stockEntity.setNxDgssSellingPrice(sellingPriceInMinUnit.stripTrailingZeros().toPlainString());
                        System.out.println("💵 建议零售价计算（外包装单位）:");
                        System.out.println("  期望价格: " + expectPrice + "元/箱");
                        System.out.println("  外包装建议零售价: " + stockEntity.getNxDgssSellingPriceCarton() + "元/箱");
                        System.out.println("  最小单位建议零售价: " + stockEntity.getNxDgssSellingPrice() + "元/个");
                        System.out.println("  计算公式: " + expectPrice + "元/箱 ÷ " + itemsPerCarton + "个/箱 = " + stockEntity.getNxDgssSellingPrice() + "元/个");
                    } else {
                        // 期望价格是最小单位建议零售价
                        stockEntity.setNxDgssSellingPrice(expectPrice.stripTrailingZeros().toPlainString());
                        // 计算外包装建议零售价
                        BigDecimal sellingPriceCarton = expectPrice.multiply(itemsPerCartonBD);
                        stockEntity.setNxDgssSellingPriceCarton(sellingPriceCarton.stripTrailingZeros().toPlainString());
                        System.out.println("💵 建议零售价计算（最小单位）:");
                        System.out.println("  期望价格: " + expectPrice + "元/个");
                        System.out.println("  最小单位建议零售价: " + stockEntity.getNxDgssSellingPrice() + "元/个");
                        System.out.println("  外包装建议零售价: " + stockEntity.getNxDgssSellingPriceCarton() + "元/箱");
                        System.out.println("  计算公式: " + expectPrice + "元/个 × " + itemsPerCarton + "个/箱 = " + stockEntity.getNxDgssSellingPriceCarton() + "元/箱");
                    }
                } else {
                    System.out.println("⚠️ 未设置期望价格，不计算建议零售价");
                }
                
                // 4. 成本计算
                // 如果采购规格是大包装单位，使用大包装单价 × 大包装数量
                // 如果采购规格是最小单位，使用最小单位单价 × 最小单位数量
                BigDecimal subtotal;
                if (isCartonStandard) {
                    // 使用大包装单价 × 大包装数量
                    BigDecimal priceCarton = new BigDecimal(stockEntity.getNxDgssPriceCarton());
                    subtotal = actualCartonCount.multiply(priceCarton).setScale(1, BigDecimal.ROUND_HALF_UP);
                    System.out.println("📊 成本计算（大包装单位）:");
                    System.out.println("  采购数量: " + actualCartonCount + "箱");
                    System.out.println("  大包装单价: " + stockEntity.getNxDgssPriceCarton() + "元/箱");
                    System.out.println("  成本小计: " + subtotal + "元");
                    System.out.println("  计算公式: " + actualCartonCount + "箱 × " + stockEntity.getNxDgssPriceCarton() + "元/箱 = " + subtotal + "元");
                } else {
                    // 使用最小单位单价 × 最小单位数量
                    BigDecimal priceForCalc = new BigDecimal(stockEntity.getNxDgssPrice());
                    subtotal = weightInMinUnit.multiply(priceForCalc).setScale(1, BigDecimal.ROUND_HALF_UP);
                    System.out.println("📊 成本计算（最小单位）:");
                    System.out.println("  入库数量: " + weightInMinUnit + "个");
                    System.out.println("  最小单位单价: " + stockEntity.getNxDgssPrice() + "元/个");
                    System.out.println("  成本小计: " + subtotal + "元");
                    System.out.println("  计算公式: " + weightInMinUnit + "个 × " + stockEntity.getNxDgssPrice() + "元/个 = " + subtotal + "元");
                }
                stockEntity.setNxDgssSubtotal(subtotal.toPlainString());
                stockEntity.setNxDgssRestSubtotal(subtotal.toPlainString());
                
                System.out.println("✅ [saveShelfGoodsStockWithBatch] 商品有外包装处理完成:");
                System.out.println("  外包装单位: " + disGoodsEntity.getNxDgCartonUnit());
                System.out.println("  每箱数量: " + itemsPerCarton + "个");
                System.out.println("  采购数量: " + buyQuantity + (isCartonStandard ? "箱" : "个"));
                System.out.println("  入库数量: " + weightInMinUnit + "个");
                System.out.println("  最小单位单价: " + stockEntity.getNxDgssPrice() + "元/个");
                System.out.println("  外包装单价: " + stockEntity.getNxDgssPriceCarton() + "元/箱");
                if (stockEntity.getNxDgssSellingPrice() != null) {
                    System.out.println("  最小单位建议零售价: " + stockEntity.getNxDgssSellingPrice() + "元/个");
                }
                if (stockEntity.getNxDgssSellingPriceCarton() != null) {
                    System.out.println("  外包装建议零售价: " + stockEntity.getNxDgssSellingPriceCarton() + "元/箱");
                }
            } else {
                // 无外包装：按原有逻辑处理
                System.out.println("📦 无外包装商品，按最小单位处理:");
                stockEntity.setNxDgssPrice(buyPrice.stripTrailingZeros().toPlainString());
                stockEntity.setNxDgssWeight(buyQuantity.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                stockEntity.setNxDgssRestWeight(buyQuantity.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            stockEntity.setNxDgssSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
            stockEntity.setNxDgssRestSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
                
                System.out.println("  入库数量: " + buyQuantity + "个");
                System.out.println("  单价: " + stockEntity.getNxDgssPrice() + "元/个");
                System.out.println("  成本小计: " + purchaseGoodsEntity.getNxDpgBuySubtotal() + "元");
                
                if (expectPrice != null) {
                    stockEntity.setNxDgssSellingPrice(expectPrice.stripTrailingZeros().toPlainString());
                    System.out.println("  建议零售价: " + stockEntity.getNxDgssSellingPrice() + "元/个");
                }
            }
            
            System.out.println("========== [saveShelfGoodsStockWithBatch] 处理完成 ==========");
            
            stockEntity.setNxDgssStatus(0);

            stockEntity.setNxDgssDate(formatWhatDay(0));
            stockEntity.setNxDgssMonth(formatWhatMonth(0));
            stockEntity.setNxDgssYear(formatWhatYear(0));
            stockEntity.setNxDgssProduceWeight("0");
            stockEntity.setNxDgssProduceSubtotal("0");
            stockEntity.setNxDgssLossWeight("0");
            stockEntity.setNxDgssLossSubtotal("0");
            stockEntity.setNxDgssWasteWeight("0");
            stockEntity.setNxDgssWasteSubtotal("0");
            stockEntity.setNxDgssReturnWeight("0");
            stockEntity.setNxDgssReturnSubtotal("0");
            stockEntity.setNxDgssReceiveUserId(purchaseGoodsEntity.getNxDpgPurUserId());

            // 查询该商品是否在货架上（使用采购商品的申请货架ID）
            NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity = null;
            if (olePurGoods.getNxDpgApplyShelfId() != null) {
                // 使用申请货架ID查询
                shelfGoodsEntity = shelfGoodsService.queryShlefGoodsByGoodsIdAndShelfId(
                    purchaseGoodsEntity.getNxDpgDisGoodsId(), 
                    olePurGoods.getNxDpgApplyShelfId());
                System.out.println("使用申请货架ID查询: 商品ID=" + purchaseGoodsEntity.getNxDpgDisGoodsId() 
                    + ", 货架ID=" + olePurGoods.getNxDpgApplyShelfId());
            } else {
                // 如果没有申请货架ID，回退到原来的查询方式（可能返回多个结果）
                shelfGoodsEntity = shelfGoodsService.queryShlefGoodsByGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
                System.out.println("未设置申请货架ID，使用商品ID查询（可能返回多个结果）");
            }
            
            if(shelfGoodsEntity != null){
                System.out.println("商品在货架上，货架商品ID: " + shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
                stockEntity.setNxDgssNxShelfGoodsId(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
            } else {
                System.out.println("商品不在货架上");
            }

            shelfStockService.save(stockEntity);

            olePurGoods.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());

        }


        nxDisPurcGoodsService.update(olePurGoods);

        if(purchaseGoodsEntity.getNxDpgBatchId() != -1 && purchaseGoodsEntity.getNxDpgBatchId() != null){
            Map<String, Object> map = new HashMap<>();
            map.put("batchId", purchaseGoodsEntity.getNxDpgBatchId());
//            NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = batchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());

            if(nxDistributerPurchaseBatchEntity.getNxDpbPurchaseType() == 12){
                map.put("status", 3);
            }else if(nxDistributerPurchaseBatchEntity.getNxDpbPurchaseType() == 13){
                map.put("status", 2);
            }

            System.out.println("查询参数: batchId=" + purchaseGoodsEntity.getNxDpgBatchId() + ", status<2");

            int count = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
            System.out.println("批次中未完成的采购商品数量: " + count);

            if(count == 0){
                System.out.println("批次所有采购商品已完成，准备更新批次状态");
                System.out.println("批次当前状态: " + nxDistributerPurchaseBatchEntity.getNxDpbStatus());
                nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
                batchService.update(nxDistributerPurchaseBatchEntity);
                System.out.println("批次状态已更新为: " + getNxDisPurchaseBatchDisUserFinish());
            } else {
                System.out.println("批次还有 " + count + " 个采购商品未完成，不更新批次状态");
            }
        } else {
            System.out.println("批次ID无效(为-1或null)，跳过批次状态检查");
        }

        return R.ok();
    }



    @RequestMapping(value = "/saveShelfGoodsStock", method = RequestMethod.POST)
    @ResponseBody
    public R saveShelfGoodsStock(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {

        Integer nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
        NxDistributerPurchaseGoodsEntity olePurGoods = nxDisPurcGoodsService.queryObject(nxDistributerPurchaseGoodsId);
        olePurGoods.setNxDpgPurchaseDate(formatWhatDay(0));
        olePurGoods.setNxDpgBuyPrice(purchaseGoodsEntity.getNxDpgBuyPrice());
        olePurGoods.setNxDpgBuyQuantity(purchaseGoodsEntity.getNxDpgBuyQuantity());
        olePurGoods.setNxDpgBuySubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
        olePurGoods.setNxDpgStatus(getNxDisPurchaseGoodsIsPurchase());
        olePurGoods.setNxDpgPurUserId(purchaseGoodsEntity.getNxDpgPurUserId());
        if(purchaseGoodsEntity.getNxDpgExpectPrice() != null){
            olePurGoods.setNxDpgExpectPrice(purchaseGoodsEntity.getNxDpgExpectPrice());
        }


        if(!purchaseGoodsEntity.getIsShowTools()){
            System.out.println("ueueueu");
            NxDistributerGoodsShelfStockEntity stockEntity = new NxDistributerGoodsShelfStockEntity();

            stockEntity.setNxDgssNxPurGoodsId(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
            stockEntity.setNxDgssNxDistributerId(purchaseGoodsEntity.getNxDpgDistributerId());
            stockEntity.setNxDgssNxDisGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
            stockEntity.setNxDgssNxDisGoodsFatherId(purchaseGoodsEntity.getNxDpgDisGoodsFatherId());
            
            // 查询商品信息，获取外包装信息
            NxDistributerGoodsEntity disGoodsEntity = nxDgService.queryObject(purchaseGoodsEntity.getNxDpgDisGoodsId());
            System.out.println("========== [saveShelfGoodsStock] 开始处理库存入库 ==========");
            System.out.println("商品ID: " + purchaseGoodsEntity.getNxDpgDisGoodsId());
            System.out.println("商品名称: " + (disGoodsEntity != null ? disGoodsEntity.getNxDgGoodsName() : "null"));
            
            // 获取采购信息
            String buyPriceStr = purchaseGoodsEntity.getNxDpgBuyPrice();
            String buyQuantityStr = purchaseGoodsEntity.getNxDpgBuyQuantity();
            String expectPriceStr = purchaseGoodsEntity.getNxDpgExpectPrice();
            String purchaseStandard = purchaseGoodsEntity.getNxDpgStandard();
            
            System.out.println("采购单价: " + buyPriceStr);
            System.out.println("采购数量: " + buyQuantityStr);
            System.out.println("采购规格: " + purchaseStandard);
            System.out.println("期望价格: " + expectPriceStr);
            
            BigDecimal buyPrice = buyPriceStr != null && !buyPriceStr.trim().isEmpty() 
                ? new BigDecimal(buyPriceStr) : BigDecimal.ZERO;
            BigDecimal buyQuantity = buyQuantityStr != null && !buyQuantityStr.trim().isEmpty() 
                ? new BigDecimal(buyQuantityStr) : BigDecimal.ZERO;
            BigDecimal expectPrice = expectPriceStr != null && !expectPriceStr.trim().isEmpty() 
                ? new BigDecimal(expectPriceStr) : null;
            
            // 检查是否有外包装信息
            boolean hasCarton = disGoodsEntity != null 
                && disGoodsEntity.getNxDgCartonUnit() != null 
                && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                && disGoodsEntity.getNxDgItemsPerCarton() != null 
                && disGoodsEntity.getNxDgItemsPerCarton() > 0;
            
            if (hasCarton) {
                System.out.println("✅ 商品有外包装信息:");
                System.out.println("  外包装单位: " + disGoodsEntity.getNxDgCartonUnit());
                System.out.println("  每箱数量: " + disGoodsEntity.getNxDgItemsPerCarton() + "个");
            } else {
                System.out.println("⚠️ 商品无外包装信息，按最小单位处理");
            }
            
            // 判断采购规格是否与外包装单位匹配
            boolean isCartonStandard = hasCarton 
                && purchaseStandard != null 
                && purchaseStandard.trim().equals(disGoodsEntity.getNxDgCartonUnit().trim());
            
            if (hasCarton) {
                System.out.println("采购规格匹配检查: " + purchaseStandard + " == " + disGoodsEntity.getNxDgCartonUnit() 
                    + " ? " + (isCartonStandard ? "✅ 匹配（使用外包装单位）" : "❌ 不匹配（使用最小单位）"));
            }
            
            if (hasCarton) {
                // 有外包装：需要计算双单价和双零售价
                Integer itemsPerCarton = disGoodsEntity.getNxDgItemsPerCarton();
                BigDecimal itemsPerCartonBD = new BigDecimal(itemsPerCarton);
                
                // 1. 数量转换：按最小单位存储
                BigDecimal weightInMinUnit;
                BigDecimal actualCartonCount; // 实际箱数
                if (isCartonStandard) {
                    // 如果采购规格是外包装单位（如"箱"），说明前端传入的数量是箱数
                    // 例如：规格="箱"，数量=3箱，每箱200个，则入库数量=3*200=600个
                    actualCartonCount = buyQuantity;
                    weightInMinUnit = buyQuantity.multiply(itemsPerCartonBD);
                    System.out.println("📦 数量转换（外包装单位）:");
                    System.out.println("  采购数量: " + buyQuantity + "箱");
                    System.out.println("  每箱数量: " + itemsPerCarton + "个");
                    System.out.println("  入库数量: " + weightInMinUnit + "个");
                    System.out.println("  计算公式: " + buyQuantity + "箱 × " + itemsPerCarton + "个/箱 = " + weightInMinUnit + "个");
                } else {
                    // 如果采购规格不是外包装单位，说明前端传入的数量已经是按最小单位的
                    actualCartonCount = buyQuantity.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                    weightInMinUnit = buyQuantity;
                    System.out.println("📦 数量转换（最小单位）:");
                    System.out.println("  采购数量: " + buyQuantity + "个");
                    System.out.println("  折合箱数: " + actualCartonCount + "箱");
                    System.out.println("  入库数量: " + weightInMinUnit + "个");
                }
                stockEntity.setNxDgssWeight(weightInMinUnit.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                stockEntity.setNxDgssRestWeight(weightInMinUnit.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                
                // 2. 单价计算
                // 如果采购规格是外包装单位，采购单价就是外包装单价
                if (isCartonStandard) {
                    // 采购单价是外包装单价
                    stockEntity.setNxDgssPriceCarton(buyPrice.stripTrailingZeros().toPlainString());
                    // 计算最小单位单价
                    BigDecimal priceInMinUnit = buyPrice.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                    stockEntity.setNxDgssPrice(priceInMinUnit.stripTrailingZeros().toPlainString());
                    System.out.println("💰 单价计算（外包装单位）:");
                    System.out.println("  采购单价: " + buyPrice + "元/箱");
                    System.out.println("  外包装单价: " + stockEntity.getNxDgssPriceCarton() + "元/箱");
                    System.out.println("  最小单位单价: " + stockEntity.getNxDgssPrice() + "元/个");
                    System.out.println("  计算公式: " + buyPrice + "元/箱 ÷ " + itemsPerCarton + "个/箱 = " + stockEntity.getNxDgssPrice() + "元/个");
                } else {
                    // 采购单价是最小单位单价
                    stockEntity.setNxDgssPrice(buyPrice.stripTrailingZeros().toPlainString());
                    // 计算外包装单价
                    BigDecimal priceCarton = buyPrice.multiply(itemsPerCartonBD);
                    stockEntity.setNxDgssPriceCarton(priceCarton.stripTrailingZeros().toPlainString());
                    System.out.println("💰 单价计算（最小单位）:");
                    System.out.println("  采购单价: " + buyPrice + "元/个");
                    System.out.println("  最小单位单价: " + stockEntity.getNxDgssPrice() + "元/个");
                    System.out.println("  外包装单价: " + stockEntity.getNxDgssPriceCarton() + "元/箱");
                    System.out.println("  计算公式: " + buyPrice + "元/个 × " + itemsPerCarton + "个/箱 = " + stockEntity.getNxDgssPriceCarton() + "元/箱");
                }
                
                // 3. 建议零售价计算
                if (expectPrice != null) {
                    if (isCartonStandard) {
                        // 期望价格是外包装建议零售价
                        stockEntity.setNxDgssSellingPriceCarton(expectPrice.stripTrailingZeros().toPlainString());
                        // 计算最小单位建议零售价
                        BigDecimal sellingPriceInMinUnit = expectPrice.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                        stockEntity.setNxDgssSellingPrice(sellingPriceInMinUnit.stripTrailingZeros().toPlainString());
                        System.out.println("💵 建议零售价计算（外包装单位）:");
                        System.out.println("  期望价格: " + expectPrice + "元/箱");
                        System.out.println("  外包装建议零售价: " + stockEntity.getNxDgssSellingPriceCarton() + "元/箱");
                        System.out.println("  最小单位建议零售价: " + stockEntity.getNxDgssSellingPrice() + "元/个");
                        System.out.println("  计算公式: " + expectPrice + "元/箱 ÷ " + itemsPerCarton + "个/箱 = " + stockEntity.getNxDgssSellingPrice() + "元/个");
                    } else {
                        // 期望价格是最小单位建议零售价
                        stockEntity.setNxDgssSellingPrice(expectPrice.stripTrailingZeros().toPlainString());
                        // 计算外包装建议零售价
                        BigDecimal sellingPriceCarton = expectPrice.multiply(itemsPerCartonBD);
                        stockEntity.setNxDgssSellingPriceCarton(sellingPriceCarton.stripTrailingZeros().toPlainString());
                        System.out.println("💵 建议零售价计算（最小单位）:");
                        System.out.println("  期望价格: " + expectPrice + "元/个");
                        System.out.println("  最小单位建议零售价: " + stockEntity.getNxDgssSellingPrice() + "元/个");
                        System.out.println("  外包装建议零售价: " + stockEntity.getNxDgssSellingPriceCarton() + "元/箱");
                        System.out.println("  计算公式: " + expectPrice + "元/个 × " + itemsPerCarton + "个/箱 = " + stockEntity.getNxDgssSellingPriceCarton() + "元/箱");
                    }
                } else {
                    System.out.println("⚠️ 未设置期望价格，不计算建议零售价");
                }
                
                // 4. 成本计算
                // 如果采购规格是大包装单位，使用大包装单价 × 大包装数量
                // 如果采购规格是最小单位，使用最小单位单价 × 最小单位数量
                BigDecimal subtotal;
                if (isCartonStandard) {
                    // 使用大包装单价 × 大包装数量
                    BigDecimal priceCarton = new BigDecimal(stockEntity.getNxDgssPriceCarton());
                    subtotal = actualCartonCount.multiply(priceCarton).setScale(1, BigDecimal.ROUND_HALF_UP);
                    System.out.println("📊 成本计算（大包装单位）:");
                    System.out.println("  采购数量(actualCartonCount): " + actualCartonCount + "箱");
                    System.out.println("  大包装单价(priceCarton): " + priceCarton + "元/箱");
                    System.out.println("  大包装单价(字符串): " + stockEntity.getNxDgssPriceCarton() + "元/箱");
                    System.out.println("  计算过程: " + actualCartonCount + " × " + priceCarton + " = " + actualCartonCount.multiply(priceCarton));
                    System.out.println("  成本小计(subtotal): " + subtotal + "元");
                    System.out.println("  成本小计(字符串): " + subtotal.toPlainString());
                    System.out.println("  计算公式: " + actualCartonCount + "箱 × " + stockEntity.getNxDgssPriceCarton() + "元/箱 = " + subtotal + "元");
                } else {
                    // 使用最小单位单价 × 最小单位数量
                    BigDecimal priceForCalc = new BigDecimal(stockEntity.getNxDgssPrice());
                    subtotal = weightInMinUnit.multiply(priceForCalc).setScale(1, BigDecimal.ROUND_HALF_UP);
                    System.out.println("📊 成本计算（最小单位）:");
                    System.out.println("  入库数量: " + weightInMinUnit + "个");
                    System.out.println("  最小单位单价: " + stockEntity.getNxDgssPrice() + "元/个");
                    System.out.println("  成本小计: " + subtotal + "元");
                    System.out.println("  计算公式: " + weightInMinUnit + "个 × " + stockEntity.getNxDgssPrice() + "元/个 = " + subtotal + "元");
                }
                stockEntity.setNxDgssSubtotal(subtotal.toPlainString());
                stockEntity.setNxDgssRestSubtotal(subtotal.toPlainString());
                System.out.println("🔍 [DEBUG] 设置库存小计:");
                System.out.println("  nxDgssSubtotal: " + stockEntity.getNxDgssSubtotal());
                System.out.println("  nxDgssRestSubtotal: " + stockEntity.getNxDgssRestSubtotal());
                
                System.out.println("✅ [saveShelfGoodsStock] 商品有外包装处理完成:");
                System.out.println("  外包装单位: " + disGoodsEntity.getNxDgCartonUnit());
                System.out.println("  每箱数量: " + itemsPerCarton + "个");
                System.out.println("  采购数量: " + buyQuantity + (isCartonStandard ? "箱" : "个"));
                System.out.println("  入库数量: " + weightInMinUnit + "个");
                System.out.println("  最小单位单价: " + stockEntity.getNxDgssPrice() + "元/个");
                System.out.println("  外包装单价: " + stockEntity.getNxDgssPriceCarton() + "元/箱");
                if (stockEntity.getNxDgssSellingPrice() != null) {
                    System.out.println("  最小单位建议零售价: " + stockEntity.getNxDgssSellingPrice() + "元/个");
                }
                if (stockEntity.getNxDgssSellingPriceCarton() != null) {
                    System.out.println("  外包装建议零售价: " + stockEntity.getNxDgssSellingPriceCarton() + "元/箱");
                }
            } else {
                // 无外包装：按原有逻辑处理
                stockEntity.setNxDgssPrice(buyPrice.stripTrailingZeros().toPlainString());
                stockEntity.setNxDgssWeight(buyQuantity.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                stockEntity.setNxDgssRestWeight(buyQuantity.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            stockEntity.setNxDgssSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
            stockEntity.setNxDgssRestSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
                
                if (expectPrice != null) {
                    stockEntity.setNxDgssSellingPrice(expectPrice.stripTrailingZeros().toPlainString());
                }
            }
            
            stockEntity.setNxDgssStatus(0);

            stockEntity.setNxDgssDate(formatWhatDay(0));
            stockEntity.setNxDgssMonth(formatWhatMonth(0));
            stockEntity.setNxDgssYear(formatWhatYear(0));
            stockEntity.setNxDgssProduceWeight("0");
            stockEntity.setNxDgssProduceSubtotal("0");
            stockEntity.setNxDgssLossWeight("0");
            stockEntity.setNxDgssLossSubtotal("0");
            stockEntity.setNxDgssWasteWeight("0");
            stockEntity.setNxDgssWasteSubtotal("0");
            stockEntity.setNxDgssReturnWeight("0");
            stockEntity.setNxDgssReturnSubtotal("0");
            stockEntity.setNxDgssReceiveUserId(purchaseGoodsEntity.getNxDpgPurUserId());

            // 查询该商品是否在货架上（使用采购商品的申请货架ID）
            NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity = null;
            if (olePurGoods.getNxDpgApplyShelfId() != null) {
                // 使用申请货架ID查询
                shelfGoodsEntity = shelfGoodsService.queryShlefGoodsByGoodsIdAndShelfId(
                    purchaseGoodsEntity.getNxDpgDisGoodsId(), 
                    olePurGoods.getNxDpgApplyShelfId());
                System.out.println("使用申请货架ID查询: 商品ID=" + purchaseGoodsEntity.getNxDpgDisGoodsId() 
                    + ", 货架ID=" + olePurGoods.getNxDpgApplyShelfId());
            } else {
                // 如果没有申请货架ID，回退到原来的查询方式（可能返回多个结果）
                shelfGoodsEntity = shelfGoodsService.queryShlefGoodsByGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
                System.out.println("未设置申请货架ID，使用商品ID查询（可能返回多个结果）");
            }
            
            if(shelfGoodsEntity != null){
                System.out.println("商品在货架上，货架商品ID: " + shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
                stockEntity.setNxDgssNxShelfGoodsId(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
            } else {
                System.out.println("商品不在货架上");
            }

            shelfStockService.save(stockEntity);

            olePurGoods.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());

        }

        System.out.println("shelelel" + olePurGoods);
        nxDisPurcGoodsService.update(olePurGoods);

        return R.ok().put("data", purchaseGoodsEntity);
    }


    @RequestMapping(value = "/purchaserReceiveBatch", method = RequestMethod.POST)
    @ResponseBody
    public R purchaserReceiveBatch(Integer batchId , Integer userId) {
        System.out.println("======== 开始批量接收采购批次 ========");
        System.out.println("采购批次ID: " + batchId);

        List<NxDistributerPurchaseGoodsEntity> nxDistributerPurchaseGoodsEntities = nxDisPurcGoodsService.queryPurchaseGoodsByBatchId(batchId);
        System.out.println("批次中的采购商品数量: " + nxDistributerPurchaseGoodsEntities.size());
        
        if(nxDistributerPurchaseGoodsEntities.size() > 0){
            int index = 1;
            for(NxDistributerPurchaseGoodsEntity purchaseGoodsEntityNew : nxDistributerPurchaseGoodsEntities){

                Integer nxDistributerPurchaseGoodsId = purchaseGoodsEntityNew.getNxDistributerPurchaseGoodsId();
                NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDisPurcGoodsService.queryObject(nxDistributerPurchaseGoodsId);

                NxDistributerGoodsShelfStockEntity stockEntity = new NxDistributerGoodsShelfStockEntity();

                stockEntity.setNxDgssNxPurGoodsId(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
                stockEntity.setNxDgssNxDistributerId(purchaseGoodsEntity.getNxDpgDistributerId());
                stockEntity.setNxDgssNxDisGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
                stockEntity.setNxDgssNxDisGoodsFatherId(purchaseGoodsEntity.getNxDpgDisGoodsFatherId());
                
                // 查询商品信息，获取外包装信息
                NxDistributerGoodsEntity disGoodsEntity = nxDgService.queryObject(purchaseGoodsEntity.getNxDpgDisGoodsId());
                
                // 获取采购信息
                String buyPriceStr = purchaseGoodsEntity.getNxDpgBuyPrice();
                String buyQuantityStr = purchaseGoodsEntity.getNxDpgBuyQuantity();
                String expectPriceStr = purchaseGoodsEntity.getNxDpgExpectPrice();
                
                BigDecimal buyPrice = buyPriceStr != null && !buyPriceStr.trim().isEmpty() 
                    ? new BigDecimal(buyPriceStr) : BigDecimal.ZERO;
                BigDecimal buyQuantity = buyQuantityStr != null && !buyQuantityStr.trim().isEmpty() 
                    ? new BigDecimal(buyQuantityStr) : BigDecimal.ZERO;
                BigDecimal expectPrice = expectPriceStr != null && !expectPriceStr.trim().isEmpty() 
                    ? new BigDecimal(expectPriceStr) : null;
                
                // 检查是否有外包装信息
                boolean hasCarton = disGoodsEntity != null 
                    && disGoodsEntity.getNxDgCartonUnit() != null 
                    && !disGoodsEntity.getNxDgCartonUnit().trim().isEmpty()
                    && disGoodsEntity.getNxDgItemsPerCarton() != null 
                    && disGoodsEntity.getNxDgItemsPerCarton() > 0;
                
                if (hasCarton) {
                    // 有外包装：需要计算双单价和双零售价
                    Integer itemsPerCarton = disGoodsEntity.getNxDgItemsPerCarton();
                    BigDecimal itemsPerCartonBD = new BigDecimal(itemsPerCarton);
                    
                    // 1. 数量转换：按最小单位存储
                    BigDecimal weightInMinUnit = buyQuantity.multiply(itemsPerCartonBD);
                    stockEntity.setNxDgssWeight(weightInMinUnit.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                    stockEntity.setNxDgssRestWeight(weightInMinUnit.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                    
                    // 2. 单价计算
                    // 最小单位单价 = 外包装单价 ÷ 每箱数量
                    BigDecimal priceInMinUnit = buyPrice.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                    stockEntity.setNxDgssPrice(priceInMinUnit.stripTrailingZeros().toPlainString());
                    // 外包装单价 = 原始采购单价（保持整数）
                    stockEntity.setNxDgssPriceCarton(buyPrice.stripTrailingZeros().toPlainString());
                    
                    // 3. 建议零售价计算
                    if (expectPrice != null) {
                        // 最小单位建议零售价 = 外包装建议零售价 ÷ 每箱数量
                        BigDecimal sellingPriceInMinUnit = expectPrice.divide(itemsPerCartonBD, 4, BigDecimal.ROUND_HALF_UP);
                        stockEntity.setNxDgssSellingPrice(sellingPriceInMinUnit.stripTrailingZeros().toPlainString());
                        // 外包装建议零售价 = 原始建议零售价（保持整数）
                        stockEntity.setNxDgssSellingPriceCarton(expectPrice.stripTrailingZeros().toPlainString());
                    }
                    
                    // 4. 成本计算（使用最小单位）
                    BigDecimal subtotal = weightInMinUnit.multiply(priceInMinUnit).setScale(1, BigDecimal.ROUND_HALF_UP);
                    stockEntity.setNxDgssSubtotal(subtotal.toPlainString());
                    stockEntity.setNxDgssRestSubtotal(subtotal.toPlainString());
                    
                    System.out.println("[purchaserReceiveBatch] 商品有外包装: " + disGoodsEntity.getNxDgCartonUnit() 
                        + ", 每箱" + itemsPerCarton + "个, 采购数量=" + buyQuantity + "箱, 入库数量=" + weightInMinUnit + "个"
                        + ", 最小单位单价=" + priceInMinUnit + ", 外包装单价=" + buyPrice);
                } else {
                    // 无外包装：按原有逻辑处理
                    stockEntity.setNxDgssPrice(buyPrice.stripTrailingZeros().toPlainString());
                    stockEntity.setNxDgssWeight(buyQuantity.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                    stockEntity.setNxDgssRestWeight(buyQuantity.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
                stockEntity.setNxDgssSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
                stockEntity.setNxDgssRestSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
                    
                    if (expectPrice != null) {
                        stockEntity.setNxDgssSellingPrice(expectPrice.stripTrailingZeros().toPlainString());
                    }
                }
                
                stockEntity.setNxDgssStatus(0);

                stockEntity.setNxDgssDate(formatWhatDay(0));
                stockEntity.setNxDgssMonth(formatWhatMonth(0));
                stockEntity.setNxDgssYear(formatWhatYear(0));
                stockEntity.setNxDgssProduceWeight("0");
                stockEntity.setNxDgssProduceSubtotal("0");
                stockEntity.setNxDgssLossWeight("0");
                stockEntity.setNxDgssLossSubtotal("0");
                stockEntity.setNxDgssWasteWeight("0");
                stockEntity.setNxDgssWasteSubtotal("0");
                stockEntity.setNxDgssReturnWeight("0");
                stockEntity.setNxDgssReturnSubtotal("0");
                stockEntity.setNxDgssReceiveUserId(userId);

                // 查询该商品是否在货架上
                NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity =  shelfGoodsService.queryShlefGoodsByGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
                if(shelfGoodsEntity != null){
                    System.out.println("商品在货架上，货架商品ID: " + shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
                    stockEntity.setNxDgssNxShelfGoodsId(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
                } else {
                    System.out.println("商品不在货架上");
                }

                shelfStockService.save(stockEntity);
                System.out.println("库存批次已保存");

                purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
                purchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
                nxDisPurcGoodsService.update(purchaseGoodsEntity);
                System.out.println("采购商品状态已更新为: " + getNxDisPurchaseGoodsFinishBuy());
                
                index++;
            }
            System.out.println("所有采购商品处理完成");
        } else {
            System.out.println("批次中没有采购商品");
        }

        NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = batchService.queryObject(batchId);
        System.out.println("批次当前状态: " + nxDistributerPurchaseBatchEntity.getNxDpbStatus());

        nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
        batchService.update(nxDistributerPurchaseBatchEntity);
        System.out.println("批次状态已更新为: " + getNxDisPurchaseBatchDisUserFinish());
        System.out.println("======== 批量接收采购批次完成 ========");
        
        return R.ok();
    }

    @RequestMapping(value = "/staffRecievePurGoods", method = RequestMethod.POST)
    @ResponseBody
    public R staffRecievePurGoods (Integer userId, Integer purGoodsId) {

        NxDistributerPurchaseGoodsEntity purchaseGoodsEntity = nxDisPurcGoodsService.queryObject(purGoodsId);
        NxDistributerGoodsShelfStockEntity stockEntity = new NxDistributerGoodsShelfStockEntity();

        stockEntity.setNxDgssNxPurGoodsId(purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
        stockEntity.setNxDgssNxDistributerId(purchaseGoodsEntity.getNxDpgDistributerId());
        stockEntity.setNxDgssNxDisGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
        stockEntity.setNxDgssNxDisGoodsFatherId(purchaseGoodsEntity.getNxDpgDisGoodsFatherId());
        stockEntity.setNxDgssPrice(purchaseGoodsEntity.getNxDpgBuyPrice());
        stockEntity.setNxDgssWeight(purchaseGoodsEntity.getNxDpgBuyQuantity());
        stockEntity.setNxDgssRestWeight(purchaseGoodsEntity.getNxDpgBuyQuantity());
        stockEntity.setNxDgssSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
        stockEntity.setNxDgssRestSubtotal(purchaseGoodsEntity.getNxDpgBuySubtotal());
        stockEntity.setNxDgssStatus(0);
        stockEntity.setNxDgssDate(formatWhatDay(0));
        stockEntity.setNxDgssMonth(formatWhatMonth(0));
        stockEntity.setNxDgssYear(formatWhatYear(0));
        stockEntity.setNxDgssProduceWeight("0");
        stockEntity.setNxDgssProduceSubtotal("0");
        stockEntity.setNxDgssLossWeight("0");
        stockEntity.setNxDgssLossSubtotal("0");
        stockEntity.setNxDgssWasteWeight("0");
        stockEntity.setNxDgssWasteSubtotal("0");
        stockEntity.setNxDgssReturnWeight("0");
        stockEntity.setNxDgssReturnSubtotal("0");
        stockEntity.setNxDgssReceiveUserId(userId);
        if(purchaseGoodsEntity.getNxDpgExpectPrice() != null){
            stockEntity.setNxDgssSellingPrice(purchaseGoodsEntity.getNxDpgExpectPrice());
        }
        NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity =  shelfGoodsService.queryShlefGoodsByGoodsId(purchaseGoodsEntity.getNxDpgDisGoodsId());
        System.out.println("sididididididd" + shelfGoodsEntity);
        if(shelfGoodsEntity != null){
            stockEntity.setNxDgssNxShelfGoodsId(shelfGoodsEntity.getNxDistributerGoodsShelfGoodsId());
        }

        shelfStockService.save(stockEntity);

        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsFinishBuy());
        nxDisPurcGoodsService.update(purchaseGoodsEntity);


        //搜索批次，判断批次是否全部完成
        System.out.println("======== 开始检查批次状态 ========");
        System.out.println("采购商品ID: " + purchaseGoodsEntity.getNxDistributerPurchaseGoodsId());
        System.out.println("批次ID: " + purchaseGoodsEntity.getNxDpgBatchId());
        
        if(purchaseGoodsEntity.getNxDpgBatchId() != null && purchaseGoodsEntity.getNxDpgBatchId() != -1){
            Map<String, Object> map = new HashMap<>();
            map.put("batchId", purchaseGoodsEntity.getNxDpgBatchId());
            map.put("status", 3);
            System.out.println("查询参数: batchId=" + purchaseGoodsEntity.getNxDpgBatchId() + ", status<3");
            
            int count = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
            System.out.println("批次中未完成的采购商品数量: " + count);
            
            if(count == 0){
                System.out.println("批次所有采购商品已完成，准备更新批次状态");
                NxDistributerPurchaseBatchEntity nxDistributerPurchaseBatchEntity = batchService.queryObject(purchaseGoodsEntity.getNxDpgBatchId());
                System.out.println("批次当前状态: " + nxDistributerPurchaseBatchEntity.getNxDpbStatus());
                
                nxDistributerPurchaseBatchEntity.setNxDpbStatus(getNxDisPurchaseBatchDisUserFinish());
                batchService.update(nxDistributerPurchaseBatchEntity);
                System.out.println("批次状态已更新为: " + getNxDisPurchaseBatchDisUserFinish());
            } else {
                System.out.println("批次还有 " + count + " 个采购商品未完成，不更新批次状态");
            }
        } else {
            System.out.println("批次ID无效(为-1或null)，跳过批次状态检查");
        }
        System.out.println("======== 批次状态检查结束 ========");
      

        return R.ok().put("data", stockEntity);


    }









    /**
     * txs
     * 老板添加进货商品
     * order的buyStatus == 1
     *
     * @param purchaseGoodsEntity 批发商商品
     * @return ok
     */
    @RequestMapping(value = "/savePlanPurchaseOrder", method = RequestMethod.POST)
    @ResponseBody
    public R savePlanPurchaseOrder(@RequestBody NxDistributerPurchaseGoodsEntity purchaseGoodsEntity) {

        purchaseGoodsEntity.setNxDpgStatus(getNxDisPurchaseGoodsUnBuy());
        purchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
        purchaseGoodsEntity.setNxDpgOrdersAmount(purchaseGoodsEntity.getNxDepartmentOrdersEntities().size());
        purchaseGoodsEntity.setNxDpgFinishAmount(0);
        nxDisPurcGoodsService.save(purchaseGoodsEntity);

        Integer nxDistributerPurchaseGoodsId = purchaseGoodsEntity.getNxDistributerPurchaseGoodsId();
        List<NxDepartmentOrdersEntity> ordersEntities = purchaseGoodsEntity.getNxDepartmentOrdersEntities();
        if (ordersEntities.size() > 0) {
            for (NxDepartmentOrdersEntity order : ordersEntities) {
                Integer nxDepartmentOrdersId = order.getNxDepartmentOrdersId();
                NxDepartmentOrdersEntity ordersEntity1 = nxDepartmentOrdersService.queryObject(nxDepartmentOrdersId);
                ordersEntity1.setNxDoPurchaseGoodsId(nxDistributerPurchaseGoodsId);
                ordersEntity1.setNxDoPurchaseStatus(getNxDepOrderBuyStatusWithPurchase());
                ordersEntity1.setNxDoCostPriceUpdate(formatWhatDayString(0));
                System.out.println("purododdododo" + formatWhatDayString(0));
                ordersEntity1.setNxDoCostPriceLevel(order.getNxDoCostPriceLevel());
                ordersEntity1.setNxDoCostPrice(order.getNxDoCostPrice());
                nxDepartmentOrdersService.update(ordersEntity1);
                if (order.getNxDoDepartmentId() == -1 && order.getNxDoGbDepartmentFatherId() > 0) {
                    GbDepartmentOrdersEntity gbOrdersEntity = gbDepartmentOrdersService.queryGbOrderByNxOrderId(order.getNxDepartmentOrdersId());
                    gbOrdersEntity.setGbDoBuyStatus(1);
                    gbDepartmentOrdersService.update(gbOrdersEntity);
                }
                if (order.getNxDoDepartmentId() == -1 && order.getNxDoNxCommRestrauntFatherId() > 0) {
                    NxRestrauntOrdersEntity resOrdersEntity = nxRestrauntOrdersService.queryNxRestrauntOrderByNxOrderId(order.getNxDepartmentOrdersId());
                    resOrdersEntity.setNxRoBuyStatus(1);
                    nxRestrauntOrdersService.update(resOrdersEntity);
                }
            }
        }

        return R.ok().put("data", purchaseGoodsEntity);
    }

    private void updateDisGoodsPriceThree(NxDistributerPurchaseGoodsEntity purgoods) {
        Integer nxDpgDisGoodsId = purgoods.getNxDpgDisGoodsId();
        NxDistributerGoodsEntity nxDistributerGoodsEntity = nxDgService.queryObject(nxDpgDisGoodsId);
        String nxDpgBuyPrice = purgoods.getNxDpgBuyPrice();
        System.out.println("updateDisGoodsPriceThree" +  nxDistributerGoodsEntity.getNxDgBuyingPriceIsGrade());
        if (nxDistributerGoodsEntity.getNxDgBuyingPriceIsGrade() == 1) {
            Integer level = purgoods.getNxDpgCostLevel();
            if (level == 1) {
                System.out.println("levvddld=="+level);
                System.out.println("nxDpgBuyPrice=="+nxDpgBuyPrice);
                nxDistributerGoodsEntity.setNxDgBuyingPriceOne(nxDpgBuyPrice);
                nxDistributerGoodsEntity.setNxDgBuyingPriceOneUpdate(formatWhatDayString(0));
                System.out.println("buyooene"+ nxDistributerGoodsEntity.getNxDistributerGoodsId());
                System.out.println("buyooene"+ nxDistributerGoodsEntity.getNxDgBuyingPriceOne());
                nxDgService.update(nxDistributerGoodsEntity);
            }
            if (level == 2) {
                nxDistributerGoodsEntity.setNxDgBuyingPriceTwo(nxDpgBuyPrice);
                nxDistributerGoodsEntity.setNxDgBuyingPriceTwoUpdate(formatWhatDayString(0));
                nxDgService.update(nxDistributerGoodsEntity);
            }
            if (level == 3) {
                nxDistributerGoodsEntity.setNxDgBuyingPriceThree(nxDpgBuyPrice);
                nxDistributerGoodsEntity.setNxDgBuyingPriceThreeUpdate(formatWhatDayString(0));
                nxDgService.update(nxDistributerGoodsEntity);
            }

        }

    }

    private void deleteUpdateNxRestrauntOrderData(NxDepartmentOrdersEntity orders) {
        Integer nxDoNxCommRestrauntId = orders.getNxDoNxCommRestrauntId();
        if (nxDoNxCommRestrauntId != -1) {
            NxRestrauntOrdersEntity resOrdersEntity = nxRestrauntOrdersService.queryObject(orders.getNxDoNxRestrauntOrderId());
            resOrdersEntity.setNxRoWeight(null);
            resOrdersEntity.setNxRoBuyStatus(getNxDepOrderBuyStatusUnPurchase());
            resOrdersEntity.setNxRoCostPrice(null);
            resOrdersEntity.setNxRoCostSubtotal(null);
            nxRestrauntOrdersService.update(resOrdersEntity);
        }
    }

    /**
     * NX系统采购日期统计
     * @param disId 批发商ID
     * @param startDate 开始日期
     * @param stopDate 结束日期
     * @return 统计数据
     */
    @RequestMapping(value = "/disGetPurchaseDate", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPurchaseDate(Integer disId, String startDate, String stopDate) {
        System.out.println("======== NX采购日期统计开始 ========");
        System.out.println("批发商ID: " + disId + ", 日期: " + startDate + " ~ " + stopDate);

        // 数据验证
        Map<String, Object> mapCheck = new HashMap<>();
        mapCheck.put("disId", disId);
        mapCheck.put("startDate", startDate);
        mapCheck.put("stopDate", stopDate);
        mapCheck.put("dayuStatus", 1); // 状态>1，已完成的采购
//        mapCheck.put("type", 0); // 状态>1，已完成的采购

        Integer purchaseCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapCheck);
        
        Map<String, Object> mapReduceCheck = new HashMap<>();
        mapReduceCheck.put("disId", disId);
        mapReduceCheck.put("startDate", startDate);
        mapReduceCheck.put("stopDate", stopDate);
        Integer reduceCount = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapReduceCheck);
        
        System.out.println("采购商品数量: " + purchaseCount + ", 出货记录数量: " + reduceCount);
        
        if (purchaseCount == 0 && reduceCount == 0) {
            System.out.println("没有数据，返回错误");
            return R.error(-1, "没有数据");
        }

        // 计算日期跨度
        List<Map<String, Object>> dayList = new ArrayList<>();
        Integer howManyDaysInPeriod = 0;
        
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        System.out.println("日期跨度: " + (howManyDaysInPeriod + 1) + " 天");

        // 按天统计
        for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
            String whichDay = "";
            if (i == 0) {
                whichDay = startDate;
            } else {
                whichDay = afterWhatDay(startDate, i);
            }
            
            System.out.println("--- 统计日期: " + whichDay + " ---");
            
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("day", whichDay);
            
            Map<String, Object> map = new HashMap<>();
            map.put("date", whichDay);
            map.put("disId", disId);
            map.put("dayuStatus", 1); // 已完成的采购

            // 1. 自采数据 (purchaseType = 0)
            map.put("purchaseType", 0);  // 自采：purchaseType = 0
            logger.info("[disGetPurchaseDate] 日期: {}, 自采查询参数: date={}, disId={}, dayuStatus={}, purchaseType={}", 
                    whichDay, map.get("date"), map.get("disId"), map.get("dayuStatus"), map.get("purchaseType"));
            Integer zicaiCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
            logger.info("[disGetPurchaseDate] 日期: {}, 自采查询结果: count={}", whichDay, zicaiCount);
            BigDecimal zicaiTotal = new BigDecimal(0);
            if (zicaiCount > 0) {
                Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
                logger.info("[disGetPurchaseDate] 日期: {}, 自采金额查询结果: subtotal={}", whichDay, aDouble);
                zicaiTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("zicai", zicaiTotal);
            logger.info("[disGetPurchaseDate] 日期: {}, 自采金额: {}", whichDay, zicaiTotal);
            // 移除 purchaseType，以便后续查询使用
            map.remove("purchaseType");

            // 2. 订货数据 (batchId is not null)
            // 确保移除之前的 purchaseType，避免影响订货查询
            map.remove("purchaseType");
            map.put("batchId", 1);  // 订货：有批次
            logger.info("[disGetPurchaseDate] 日期: {}, 订货查询参数: date={}, disId={}, dayuStatus={}, batchId={}", 
                    whichDay, map.get("date"), map.get("disId"), map.get("dayuStatus"), map.get("batchId"));
            Integer dinghuoCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
            BigDecimal dinghuoTotal = new BigDecimal(0);
            if (dinghuoCount > 0) {
                Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
                dinghuoTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("dinghuo", dinghuoTotal);
            System.out.println("订货金额: " + dinghuoTotal);

            // 3. 直接入库的采购统计 (purchaseType = -1)
            Map<String, Object> mapDirectStock = new HashMap<>();
            mapDirectStock.put("date", whichDay);
            mapDirectStock.put("disId", disId);
            mapDirectStock.put("dayuStatus", 1); // 已完成的采购
            mapDirectStock.put("purchaseType", -1);  // 直接入库的采购
            Integer directStockCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapDirectStock);
            BigDecimal directStockTotal = new BigDecimal(0);
            if (directStockCount > 0) {
                Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(mapDirectStock);
                directStockTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("directStock", directStockTotal);
            System.out.println("直接入库采购金额: " + directStockTotal);

            // 4. 所有入库的总和（包括自采、订货、直接入库）
            Map<String, Object> mapAllStock = new HashMap<>();
            mapAllStock.put("date", whichDay);
            mapAllStock.put("disId", disId);
            mapAllStock.put("dayuStatus", 1); // 已完成的采购
            Integer allStockCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapAllStock);
            BigDecimal allStockTotal = new BigDecimal(0);
            if (allStockCount > 0) {
                Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(mapAllStock);
                allStockTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("allStock", allStockTotal);
            System.out.println("所有入库采购金额: " + allStockTotal);

            // 5. 当日支出统计（3种类型）
            // 5.1 销售支出 (type = 0, 1)
            Map<String, Object> mapSale = new HashMap<>();
            mapSale.put("date", whichDay);
            mapSale.put("disId", disId);
            List<Integer> saleTypes = new ArrayList<>();
            saleTypes.add(0);
            saleTypes.add(1);
            mapSale.put("types", saleTypes);
            BigDecimal saleCostTotal = new BigDecimal(0);
            Integer saleCount = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapSale);
            if (saleCount > 0) {
                Double aDouble = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(mapSale);
                saleCostTotal = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("saleCostTotal", saleCostTotal);
            logger.info("[disGetPurchaseDate] 日期: {}, 销售支出: {}, 记录数: {}", whichDay, saleCostTotal, saleCount);

            // 5.2 损耗支出 (type = 2, 3)
            Map<String, Object> mapLoss = new HashMap<>();
            mapLoss.put("date", whichDay);
            mapLoss.put("disId", disId);
            List<Integer> lossTypes = new ArrayList<>();
            lossTypes.add(2);
            lossTypes.add(3);
            mapLoss.put("types", lossTypes);
            BigDecimal lossCostTotal = new BigDecimal(0);
            Integer lossCount = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapLoss);
            if (lossCount > 0) {
                Double aDouble = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(mapLoss);
                lossCostTotal = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("lossCostTotal", lossCostTotal);
            logger.info("[disGetPurchaseDate] 日期: {}, 损耗支出: {}, 记录数: {}", whichDay, lossCostTotal, lossCount);

            // 5.3 退货支出 (type = 4)
            Map<String, Object> mapReturn = new HashMap<>();
            mapReturn.put("date", whichDay);
            mapReturn.put("disId", disId);
            mapReturn.put("type", 4);
            BigDecimal returnCostTotal = new BigDecimal(0);
            Integer returnCount = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapReturn);
            if (returnCount > 0) {
                Double aDouble = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(mapReturn);
                returnCostTotal = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
            }
            dayMap.put("returnCostTotal", returnCostTotal);
            logger.info("[disGetPurchaseDate] 日期: {}, 退货支出: {}, 记录数: {}", whichDay, returnCostTotal, returnCount);

            // 5.4 总支出
            BigDecimal costTotal = saleCostTotal.add(lossCostTotal).add(returnCostTotal);
            dayMap.put("costTotal", costTotal);
            logger.info("[disGetPurchaseDate] 日期: {}, 总支出: {} (销售: {}, 损耗: {}, 退货: {})", 
                    whichDay, costTotal, saleCostTotal, lossCostTotal, returnCostTotal);

            dayList.add(dayMap);
        }

        // 汇总统计
        System.out.println("--- 开始汇总统计 ---");
        Map<String, Object> mapTotal = new HashMap<>();
        mapTotal.put("disId", disId);
        mapTotal.put("startDate", startDate);
        mapTotal.put("stopDate", stopDate);
        mapTotal.put("dayuStatus", 1);

        // 总采购金额
        BigDecimal purchaseTotal = new BigDecimal(0);
        Integer totalCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapTotal);
        if (totalCount > 0) {
            Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(mapTotal);
            purchaseTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        System.out.println("总采购金额: " + purchaseTotal);

        // 自采总额
        mapTotal.put("purchaseType", 0);  // 自采：purchaseType = 0
        logger.info("[disGetPurchaseDate] 汇总统计 - 自采查询参数: disId={}, startDate={}, stopDate={}, dayuStatus={}, purchaseType={}", 
                mapTotal.get("disId"), mapTotal.get("startDate"), mapTotal.get("stopDate"), 
                mapTotal.get("dayuStatus"), mapTotal.get("purchaseType"));
        BigDecimal zicaiTotalSum = new BigDecimal(0);
        Integer zicaiCountSum = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapTotal);
        logger.info("[disGetPurchaseDate] 汇总统计 - 自采查询结果: count={}", zicaiCountSum);
        if (zicaiCountSum > 0) {
            Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(mapTotal);
            logger.info("[disGetPurchaseDate] 汇总统计 - 自采金额查询结果: subtotal={}", aDouble);
            zicaiTotalSum = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        logger.info("[disGetPurchaseDate] 汇总统计 - 自采总额: {}", zicaiTotalSum);
        // 移除 purchaseType，以便后续查询使用
        mapTotal.remove("purchaseType");

        // 订货总额
        mapTotal.put("batchId", 1);  // 订货：有批次
        BigDecimal dinghuoTotalSum = new BigDecimal(0);
        Integer dinghuoCountSum = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapTotal);
        if (dinghuoCountSum > 0) {
            Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(mapTotal);
            dinghuoTotalSum = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        System.out.println("订货总额: " + dinghuoTotalSum);

        // 支出总额统计（3种类型）
        // 销售支出总额 (type = 0, 1)
        Map<String, Object> mapSaleTotal = new HashMap<>();
        mapSaleTotal.put("disId", disId);
        mapSaleTotal.put("startDate", startDate);
        mapSaleTotal.put("stopDate", stopDate);
        List<Integer> saleTypesTotal = new ArrayList<>();
        saleTypesTotal.add(0);
        saleTypesTotal.add(1);
        mapSaleTotal.put("types", saleTypesTotal);
        BigDecimal saleCostTotalSum = new BigDecimal(0);
        Integer saleCountSum = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapSaleTotal);
        if (saleCountSum > 0) {
            Double aDouble = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(mapSaleTotal);
            saleCostTotalSum = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
        }
        System.out.println("销售支出总额: " + saleCostTotalSum);

        // 损耗支出总额 (type = 2, 3)
        Map<String, Object> mapLossTotal = new HashMap<>();
        mapLossTotal.put("disId", disId);
        mapLossTotal.put("startDate", startDate);
        mapLossTotal.put("stopDate", stopDate);
        List<Integer> lossTypesTotal = new ArrayList<>();
        lossTypesTotal.add(2);
        lossTypesTotal.add(3);
        mapLossTotal.put("types", lossTypesTotal);
        BigDecimal lossCostTotalSum = new BigDecimal(0);
        Integer lossCountSum = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapLossTotal);
        if (lossCountSum > 0) {
            Double aDouble = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(mapLossTotal);
            lossCostTotalSum = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
        }
        System.out.println("损耗支出总额: " + lossCostTotalSum);

        // 退货支出总额 (type = 4)
        Map<String, Object> mapReturnTotal = new HashMap<>();
        mapReturnTotal.put("disId", disId);
        mapReturnTotal.put("startDate", startDate);
        mapReturnTotal.put("stopDate", stopDate);
        mapReturnTotal.put("type", 4);
        BigDecimal returnCostTotalSum = new BigDecimal(0);
        Integer returnCountSum = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapReturnTotal);
        if (returnCountSum > 0) {
            Double aDouble = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(mapReturnTotal);
            returnCostTotalSum = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
        }
        System.out.println("退货支出总额: " + returnCostTotalSum);

        // 总支出
        BigDecimal costTotalSum = saleCostTotalSum.add(lossCostTotalSum).add(returnCostTotalSum);
        System.out.println("总支出: " + costTotalSum);

        // 计算日均值
        BigDecimal purchasePerDay = purchaseTotal;
        BigDecimal costPerDay = costTotalSum;
        if (howManyDaysInPeriod > 0) {
            purchasePerDay = purchaseTotal.divide(new BigDecimal(howManyDaysInPeriod + 1), 1, BigDecimal.ROUND_HALF_UP);
            costPerDay = costTotalSum.divide(new BigDecimal(howManyDaysInPeriod + 1), 1, BigDecimal.ROUND_HALF_UP);
        }
        System.out.println("日均采购: " + purchasePerDay + ", 日均支出: " + costPerDay);

        // 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("allTotal", purchaseTotal);
        result.put("purchasePerDay", purchasePerDay);
        result.put("zicaiTotal", zicaiTotalSum);
        result.put("dinghuoTotal", dinghuoTotalSum);
        result.put("costTotal", costTotalSum);
        result.put("costPerDay", costPerDay);
        // 3种支出总额
        result.put("saleCostTotal", saleCostTotalSum);
        result.put("lossCostTotal", lossCostTotalSum);
        result.put("returnCostTotal", returnCostTotalSum);
        result.put("arr", dayList);

        System.out.println("======== NX采购日期统计完成 ========");
        return R.ok().put("data", result);
    }

    @RequestMapping(value = "/purchaseGetShelfPurGoods/{id}")
    @ResponseBody
    public R purchaseGetShelfPurGoods(@PathVariable Integer id) {
        Map<String, Object> map = new HashMap<>();
        map.put("disId", id);
        map.put("equalStatus", 0);
        map.put("purchaseType", 0);
        List<NxDistributerPurchaseGoodsEntity> nxDistributerPurchaseGoodsEntities = nxDisPurcGoodsService.queryPurchaseGoodsWithDetailByParams(map);
        
        // 打印日志：检查货架商品列表
        System.out.println("========================================");
        System.out.println("[purchaseGetShelfPurGoods] 查询到采购商品数量: " + nxDistributerPurchaseGoodsEntities.size());
        for (NxDistributerPurchaseGoodsEntity purchaseGoods : nxDistributerPurchaseGoodsEntities) {
            System.out.println("--- 采购商品ID: " + purchaseGoods.getNxDistributerPurchaseGoodsId() 
                + ", 商品ID: " + purchaseGoods.getNxDpgDisGoodsId()
                + ", 申请货架ID: " + purchaseGoods.getNxDpgApplyShelfId());
            
            // 检查申请货架
            if (purchaseGoods.getApplyShelfEntity() != null) {
                System.out.println("  ✅ 申请货架: " + purchaseGoods.getApplyShelfEntity().getNxDistributerGoodsShelfName() 
                    + " (ID: " + purchaseGoods.getApplyShelfEntity().getNxDistributerGoodsShelfId() + ")");
            } else {
                System.out.println("  ⚠️ 申请货架: null");
            }
            
            // 检查货架商品列表
            if (purchaseGoods.getShelfGoodsEntities() != null) {
                System.out.println("  ✅ 货架商品列表数量: " + purchaseGoods.getShelfGoodsEntities().size());
                for (NxDistributerGoodsShelfGoodsEntity shelfGoods : purchaseGoods.getShelfGoodsEntities()) {
                    System.out.println("    - 货架商品ID: " + shelfGoods.getNxDistributerGoodsShelfGoodsId() 
                        + ", 货架ID: " + shelfGoods.getNxDgsgShelfId()
                        + ", 商品ID: " + shelfGoods.getNxDgsgDisGoodsId());
                }
            } else {
                System.out.println("  ⚠️ 货架商品列表: null");
            }
        }
        System.out.println("========================================");

        map.put("equalStatus", null);
        map.put("dayuStatus",0);
        map.put("status",3);
        System.out.println("mmmddddd d" + map);
        int count = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
        Map<String, Object> mapR = new HashMap<>();
        mapR.put("arr", nxDistributerPurchaseGoodsEntities);
        mapR.put("unBuyCount",nxDistributerPurchaseGoodsEntities.size());
        mapR.put("buyingCount",count);
        return R.ok().put("data", mapR);
    }

    /**
     * NX系统采购统计汇总
     * @param disId 批发商ID
     * @param startDate 开始日期
     * @param stopDate 结束日期
     * @param greatId 最顶级分类ID（可选，用于按分类筛选）
     * @return 统计汇总数据（自采、订货、入库）
     */
    @RequestMapping(value = "/disGetPurchaseStatistics", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPurchaseStatistics(Integer disId, String startDate, String stopDate, Integer greatId) {
        System.out.println("======== NX采购统计汇总开始 ========");
        System.out.println("批发商ID: " + disId + ", 日期: " + startDate + " ~ " + stopDate + ", 分类ID: " + greatId);

        try {
            // 构建查询参数
            Map<String, Object> map = new HashMap<>();
            map.put("disId", disId);
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
            map.put("dayuStatus", 1); // 状态>1，已完成的采购
            if (greatId != null && greatId != -1) {
                map.put("disGoodsGreatId", greatId);
            }

            // 总采购统计
            Integer purCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
            Double allDouble = 0.0;
            if (purCount > 0) {
                allDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
            }
            System.out.println("总采购金额: " + allDouble);

            // 自采数据 (batchId == -1 且 purchaseType != -1，排除入库)
            map.put("batchId", -1);  // 自采：没有批次
            map.put("purchaseTypeNotEqual", -1); // 排除入库类型
            Integer zicaiCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
            Double zicaiAmount = 0.0;
            Integer zicaiGoodsCount = 0;
            if (zicaiCount > 0) {
                zicaiAmount = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
                zicaiGoodsCount = nxDisPurcGoodsService.queryDistinctGoodsCount(map);
            }
            System.out.println("自采金额: " + zicaiAmount + ", 种类数: " + zicaiGoodsCount);

            // 订货数据 (batchId == 1 且 purchaseType != -1，排除入库)
            map.put("batchId", 1);  // 订货：有批次
            map.put("purchaseTypeNotEqual", -1); // 排除入库类型
            Integer dinghuoCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
            Double dinghuoAmount = 0.0;
            Integer dinghuoGoodsCount = 0;
            if (dinghuoCount > 0) {
                dinghuoAmount = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
                dinghuoGoodsCount = nxDisPurcGoodsService.queryDistinctGoodsCount(map);
            }
            System.out.println("订货金额: " + dinghuoAmount + ", 种类数: " + dinghuoGoodsCount);

            // 入库数据 (purchaseType == -1)
            map.remove("batchId"); // 清除 batchId 条件
            map.remove("purchaseTypeNotEqual"); // 清除排除条件
            map.put("purchaseType", -1);  // 入库：直接入库的采购
            Integer rukuCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
            Double rukuAmount = 0.0;
            Integer rukuGoodsCount = 0;
            if (rukuCount > 0) {
                rukuAmount = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
                rukuGoodsCount = nxDisPurcGoodsService.queryDistinctGoodsCount(map);
            }
            System.out.println("入库金额: " + rukuAmount + ", 种类数: " + rukuGoodsCount);

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("allDouble", String.format("%.1f", allDouble));
            result.put("dinghuo", new BigDecimal(dinghuoAmount).setScale(2, BigDecimal.ROUND_HALF_UP));
            result.put("dinghuoCount", dinghuoGoodsCount);
            result.put("zicai", new BigDecimal(zicaiAmount).setScale(2, BigDecimal.ROUND_HALF_UP));
            result.put("zicaiCount", zicaiGoodsCount);
            result.put("ruku", new BigDecimal(rukuAmount).setScale(2, BigDecimal.ROUND_HALF_UP));
            result.put("rukuCount", rukuGoodsCount);

            System.out.println("======== NX采购统计汇总完成 ========");
            return R.ok().put("data", result);

        } catch (Exception e) {
            e.printStackTrace();
            return R.error("获取统计信息失败：" + e.getMessage());
        }
    }

    /**
     * NX系统分页获取采购商品列表（带采购记录）
     * @param disId 批发商ID
     * @param startDate 开始日期
     * @param stopDate 结束日期
     * @param page 页码
     * @param limit 每页数量
     * @param greatId 最顶级分类ID（可选，用于按分类筛选）
     * @return 商品列表（含采购记录）
     */
    @RequestMapping(value = "/disGetPurchaseGoodsList", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPurchaseGoodsList(Integer disId, String startDate, String stopDate,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer limit,
                                     Integer greatId) {
        System.out.println("======== NX采购商品列表开始 ========");
        System.out.println("批发商ID: " + disId + ", 日期: " + startDate + " ~ " + stopDate + ", 页码: " + page + ", 分类ID: " + greatId);

        try {
            // 构建查询参数
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("disId", disId);
            queryMap.put("startDate", startDate);
            queryMap.put("stopDate", stopDate);
            queryMap.put("dayuStatus", 1); // 已完成的采购
            queryMap.put("offset", (page - 1) * limit);
            queryMap.put("limit", limit);
            // 排除入库数据（purchaseType == -1），只显示自采和订货
//            queryMap.put("purchaseTypeNotEqual", -1);
            if (greatId != null && greatId != -1) {
                queryMap.put("disGoodsGreatId", greatId);
            }

            // 获取商品总数
            Integer totalCount = nxDisPurcGoodsService.queryGoodsListCount(queryMap);
            Integer totalPages = (int) Math.ceil((double) totalCount / limit);
            System.out.println("商品总数: " + totalCount + ", 总页数: " + totalPages);

            // 获取商品列表（每个商品包含采购记录）
            List<NxDistributerGoodsEntity> goodsList = nxDisPurcGoodsService.queryGoodsListWithPurchase(queryMap);
            System.out.println("商品列表数量: " + (goodsList != null ? goodsList.size() : "null"));

//            // 打印每个商品的采购记录详情
//            if (goodsList != null && goodsList.size() > 0) {
//                for (int i = 0; i < goodsList.size(); i++) {
//                    NxDistributerGoodsEntity goods = goodsList.get(i);
//                    System.out.println("--- 商品 " + (i + 1) + " ---");
//                    System.out.println("商品ID: " + goods.getNxDistributerGoodsId());
//                    System.out.println("商品名称: " + goods.getNxDgGoodsName());
//                    System.out.println("规格: " + goods.getNxDgGoodsStandardname());
//                    System.out.println("采购总金额(sellSubtotal): " + goods.getSellSubtotal());
//
//                    List<NxDistributerPurchaseGoodsEntity> purchaseList = goods.getNxDistributerPurchaseGoodsEntities();
//                    if (purchaseList != null && purchaseList.size() > 0) {
//                        System.out.println("采购记录数量: " + purchaseList.size());
//                        for (int j = 0; j < purchaseList.size(); j++) {
//                            NxDistributerPurchaseGoodsEntity purchase = purchaseList.get(j);
//                            System.out.println("  采购记录 " + (j + 1) + ":");
//                            System.out.println("    采购ID: " + purchase.getNxDistributerPurchaseGoodsId());
//                            System.out.println("    采购价格: " + purchase.getNxDpgBuyPrice());
//                            System.out.println("    采购数量: " + purchase.getNxDpgBuyQuantity());
//                            System.out.println("    采购小计: " + purchase.getNxDpgBuySubtotal());
//                            System.out.println("    采购日期: " + purchase.getNxDpgPurchaseDate());
//                            System.out.println("    采购类型: " + purchase.getNxDpgPurchaseType());
//                        }
//                    } else {
//                        System.out.println("⚠️ 采购记录为空或null");
//                    }
//                }
//            }

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("goodsList", goodsList);

            System.out.println("======== NX采购商品列表完成 ========");
            return R.ok().put("data", result);

        } catch (Exception e) {
            System.out.println("查询商品列表异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取商品列表失败：" + e.getMessage());
        }
    }

    /**
     * NX系统出货成本统计汇总
     * @param disId 批发商ID
     * @param startDate 开始日期
     * @param stopDate 结束日期
     * @param greatId 最顶级分类ID（可选，用于按分类筛选，-1表示全部）
     * @return 出货成本统计汇总数据（销售、损耗、退货）
     */
    @RequestMapping(value = "/getNxGoodsCostStatistics", method = RequestMethod.POST)
    @ResponseBody
    public R getNxGoodsCostStatistics(String startDate, String stopDate, Integer disId, Integer greatId) {
        logger.info("[getNxGoodsCostStatistics] 查询出货成本统计汇总，参数: disId={}, startDate={}, stopDate={}, greatId={}",
                disId, startDate, stopDate, greatId);

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", disId);
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
            if (greatId != null && greatId != -1) {
                map.put("disGoodsGreatId", greatId);
            }

            // 验证是否有数据
            Integer countCheck = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(map);
            if (countCheck == 0) {
                logger.warn("[getNxGoodsCostStatistics] 没有出货数据");
                return R.error(-1, "没有数据");
            }

            // 1. 统计销售金额（type = 0, 1）
            List<Integer> saleTypes = new ArrayList<>();
            saleTypes.add(0);
            saleTypes.add(1);
            map.put("types", saleTypes);
            Double salesTotal = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(map);
            Integer salesCount = nxDisGoodsShelfStockReduceService.queryReduceGoodsTotalCount(map);
            logger.info("[getNxGoodsCostStatistics] 销售金额: {}, 商品种类: {}", salesTotal, salesCount);

            // 2. 统计损耗金额（type = 2, 3）
            List<Integer> lossTypes = new ArrayList<>();
            lossTypes.add(2);
            lossTypes.add(3);
            map.remove("types");
            map.put("types", lossTypes);
            // 损耗金额 = waste_subtotal + loss_subtotal
            Double wasteTotal = nxDisGoodsShelfStockReduceService.queryReduceWasteSubtotal(map);
            Double lossSubtotal = nxDisGoodsShelfStockReduceService.queryReduceLossSubtotal(map);
            Double lossTotal = (wasteTotal != null ? wasteTotal : 0.0) + (lossSubtotal != null ? lossSubtotal : 0.0);
            Integer lossCount = nxDisGoodsShelfStockReduceService.queryReduceGoodsTotalCount(map);
            logger.info("[getNxGoodsCostStatistics] 损耗金额: {} (浪费: {}, 损耗: {}), 商品种类: {}", 
                    lossTotal, wasteTotal, lossSubtotal, lossCount);

            // 3. 统计退货金额（type = 4）
            map.remove("types");
            map.remove("equalType"); // 确保清除之前的 equalType
            map.put("equalType", 4);
            logger.debug("[getNxGoodsCostStatistics] 查询退货金额，参数: {}", map);
            Double returnTotal = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(map);
            Integer returnCount = nxDisGoodsShelfStockReduceService.queryReduceGoodsTotalCount(map);
            logger.info("[getNxGoodsCostStatistics] 退货金额: {}, 商品种类: {} (type=4)", returnTotal, returnCount);

            // 计算总成本
            double allTotal = (salesTotal != null ? salesTotal : 0.0) + 
                             (lossTotal != null ? lossTotal : 0.0) + 
                             (returnTotal != null ? returnTotal : 0.0);
            logger.info("[getNxGoodsCostStatistics] 总出货成本: {} (销售: {}, 损耗: {}, 退货: {})", 
                    allTotal, salesTotal, lossTotal, returnTotal);

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("allTotal", new BigDecimal(allTotal).setScale(1, BigDecimal.ROUND_HALF_UP));
            result.put("salesTotal", new BigDecimal(salesTotal != null ? salesTotal : 0.0).setScale(1, BigDecimal.ROUND_HALF_UP));
            result.put("lossTotal", new BigDecimal(lossTotal != null ? lossTotal : 0.0).setScale(1, BigDecimal.ROUND_HALF_UP));
            result.put("returnTotal", new BigDecimal(returnTotal != null ? returnTotal : 0.0).setScale(1, BigDecimal.ROUND_HALF_UP));
            result.put("salesCount", salesCount);
            result.put("lossCount", lossCount);
            result.put("returnCount", returnCount);

            logger.info("[getNxGoodsCostStatistics] 查询完成，返回统计结果");
            return R.ok().put("data", result);

        } catch (Exception e) {
            logger.error("[getNxGoodsCostStatistics] 查询统计信息异常", e);
            return R.error("获取统计信息失败：" + e.getMessage());
        }
    }

    /**
     * NX系统出货商品列表（分页）
     * @param disId 批发商ID
     * @param startDate 开始日期
     * @param stopDate 结束日期
     * @param type 出货类型: sales(销售), waste(浪费), loss(损耗)
     * @param greatId 最顶级分类ID（可选，用于按分类筛选，-1表示全部）
     * @param page 页码
     * @param limit 每页数量
     * @return 出货商品列表
     */
    @RequestMapping(value = "/getNxGoodsCostList", method = RequestMethod.POST)
    @ResponseBody
    public R getNxGoodsCostList(String startDate, String stopDate, Integer disId,
                                String type, Integer greatId,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer limit) {
        logger.info("[getNxGoodsCostList] 查询商品成本列表，参数: disId={}, startDate={}, stopDate={}, type={}, greatId={}, page={}, limit={}",
                disId, startDate, stopDate, type, greatId, page, limit);

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("disId", disId);
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
            map.put("offset", (page - 1) * limit);
            map.put("limit", limit);
            if (greatId != null && greatId != -1) {
                map.put("disGoodsGreatId", greatId);
            }

            // 根据类型设置查询条件和排序
            String orderType = null;
            Integer equalType = null;
            List<Integer> types = null;
            if (type != null) {
                switch (type) {
                    case "sales":
                        // 售出：type = 0, 1
                        orderType = "sales";
                        types = new ArrayList<>();
                        types.add(0);
                        types.add(1);
                        break;
                    case "loss":
                        // 损耗：type = 2, 3
                        orderType = "loss";
                        types = new ArrayList<>();
                        types.add(2);
                        types.add(3);
                        break;
                    case "return":
                        // 退货：type = 4
                        orderType = "return";
                        equalType = 4;
                        break;
                    default:
                        orderType = null;
                        break;
                }
            }

            if (orderType != null) {
                map.put("orderType", orderType);
            }
            if (equalType != null) {
                map.put("equalType", equalType);
            }
            if (types != null) {
                map.put("types", types);
                // 为了子查询传递，将 types 转换为字符串
                StringBuilder typesStr = new StringBuilder();
                for (int i = 0; i < types.size(); i++) {
                    if (i > 0) typesStr.append(",");
                    typesStr.append(types.get(i));
                }
                map.put("typesStr", typesStr.toString());
            }

            logger.debug("[getNxGoodsCostList] 查询参数: {}", map);

            // 统计商品总数
            Integer totalCount = nxDisGoodsShelfStockReduceService.queryReduceGoodsTotalCount(map);
            Integer totalPages = (int) Math.ceil((double) totalCount / limit);
            logger.info("[getNxGoodsCostList] 商品总数: {}, 总页数: {}", totalCount, totalPages);

            // 查询商品列表
            List<NxDistributerGoodsEntity> goodsList = nxDisGoodsShelfStockReduceService.queryGoodsWithReduceRecords(map);
            logger.info("[getNxGoodsCostList] 查询完成，返回商品数量: {}", goodsList != null ? goodsList.size() : 0);

            // 打印每个商品的出货记录详情（debug级别）
            if (goodsList != null && goodsList.size() > 0) {
                logger.debug("[getNxGoodsCostList] 商品详情:");
                for (int i = 0; i < goodsList.size(); i++) {
                    NxDistributerGoodsEntity goods = goodsList.get(i);
                    logger.debug("  商品 {}: ID={}, 名称={}, 规格={}, 销售总额={}, 损耗总额={}",
                            i + 1, goods.getNxDistributerGoodsId(), goods.getNxDgGoodsName(),
                            goods.getNxDgGoodsStandardname(), goods.getGoodsSalesTotal(), goods.getGoodsLossTotal());

                    List<NxDistributerGoodsShelfStockReduceEntity> reduceList = goods.getNxDistributerGoodsShelfStockReduceEntities();
                    if (reduceList != null && reduceList.size() > 0) {
                        logger.debug("    出货记录数量: {}", reduceList.size());
                    }
                }
            }

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", totalCount);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("arr", goodsList);

            logger.info("[getNxGoodsCostList] 查询完成，返回数据");
            return R.ok().put("data", result);

        } catch (Exception e) {
            logger.error("[getNxGoodsCostList] 查询商品列表异常", e);
            return R.error("获取商品列表失败：" + e.getMessage());
        }
    }

    /**
     * NX系统按采购员/供应商分组的采购统计
     * @param disId 批发商ID
     * @param type 类型：0=自采(按采购员), 1=订货(按供应商), -1=入库(按采购员)
     * @param startDate 开始日期
     * @param stopDate 结束日期
     * @param purUserIds 采购员IDs（逗号分隔，可选，"-1"表示全部）
     * @param supplierIds 供应商IDs（逗号分隔，可选，"-1"表示全部）
     * @return 分组统计数据
     */
    @RequestMapping(value = "/disGetPurchaseDetailType", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPurchaseDetailType(Integer disId, String purUserIds, Integer type,
                                      String startDate, String stopDate, String supplierIds) {
        System.out.println("======== NX采购分组统计开始 ========");
        System.out.println("批发商ID: " + disId + ", 类型: " + type + ", 日期: " + startDate + " ~ " + stopDate);

        Map<String, Object> map = new HashMap<>();
        map.put("disId", disId);
        map.put("startDate", startDate);
        map.put("stopDate", stopDate);
        map.put("dayuStatus", 1); // 已完成的采购

        // 处理采购员IDs
        if (purUserIds != null && !purUserIds.equals("-1")) {
            String[] arrIds = purUserIds.split(",");
            List<Integer> idsList = new ArrayList<>();
            for (String id : arrIds) {
                idsList.add(Integer.valueOf(id.trim()));
            }
            if (idsList.size() > 0) {
                map.put("purUserIds", idsList);
            }
        }

        // 处理供应商IDs
        if (supplierIds != null && !supplierIds.equals("-1")) {
            String[] arrIds = supplierIds.split(",");
            List<Integer> idsList = new ArrayList<>();
            for (String id : arrIds) {
                idsList.add(Integer.valueOf(id.trim()));
            }
            if (idsList.size() > 0) {
                map.put("supplierIds", idsList);
            }
        }

        Map<String, Object> mapR = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();

        if (type == 0) {
            // 自采：没有批次ID（按采购员分组）
            System.out.println("=== 自采统计（按采购员分组） ===");
            map.put("batchId", -1);  // 自采：没有批次
            map.put("purchaseTypeNotEqual", -1); // 排除入库类型

            // 计算总额
            Double totalAmount = 0.0;
            Integer totalCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
            if (totalCount > 0) {
                totalAmount = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
            }
            System.out.println("自采总额: " + totalAmount);

            // 获取采购员列表（使用NxDistributerUser）
            System.out.println("查询采购员列表，参数: " + map);
            List<NxDistributerUserEntity> purUserList = nxDisPurcGoodsService.queryPurUserList(map);
            System.out.println("采购员数量: " + purUserList.size());
            if (purUserList.size() > 0) {
                for (int i = 0; i < purUserList.size(); i++) {
                    System.out.println("  采购员 " + (i + 1) + ": ID=" + purUserList.get(i).getNxDistributerUserId() + 
                                     ", 姓名=" + purUserList.get(i).getNxDiuWxNickName());
                }
            } else {
                System.out.println("⚠️ 没有查询到采购员，请检查SQL查询条件");
            }

            if (purUserList.size() > 0) {
                for (NxDistributerUserEntity userEntity : purUserList) {
                    Map<String, Object> mapUser = new HashMap<>();
                    mapUser.put("user", userEntity);

                    // 创建新的查询参数，避免污染
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.put("batchId", -1);  // 自采：没有批次
                    queryMap.put("purchaseTypeNotEqual", -1); // 排除入库类型
                    queryMap.put("dayuStatus", 1);
                    queryMap.put("startDate", startDate);
                    queryMap.put("stopDate", stopDate);
                    queryMap.put("disId", disId);
                    queryMap.put("purUserId", userEntity.getNxDistributerUserId());
                    queryMap.put("offset", 0);
                    queryMap.put("limit", 100);

                    System.out.println("查询采购员商品: " + queryMap);
                    Integer goodsCount = nxDisPurcGoodsService.queryGoodsListCount(queryMap);
                    List<NxDistributerGoodsEntity> goodsList = nxDisPurcGoodsService.queryGoodsListWithPurchase(queryMap);
                    Double subTotal = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(queryMap);

                    mapUser.put("arr", goodsList);
                    mapUser.put("count", goodsCount);
                    mapUser.put("purSubtotal", String.format("%.1f", subTotal));
                    result.add(mapUser);
                }
            }

            mapR.put("total", new BigDecimal(totalAmount).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapR.put("purUserArr", result);

        } else if (type == 1) {
            // 订货：有批次ID（按供应商/批次分组）
            System.out.println("=== 订货统计（按供应商分组） ===");
            map.put("batchId", 1);  // 订货：有批次（batchId > 0）
            map.put("purchaseTypeNotEqual", -1); // 排除入库类型

            // 计算总额
            Double totalAmount = 0.0;
            Integer totalCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
            if (totalCount > 0) {
                totalAmount = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
            }
            System.out.println("订货总额: " + totalAmount);

            // 获取供应商列表（通过批次关联）
            List<NxJrdhUserEntity> supplierList = nxDisPurcGoodsService.querySupplierList(map);
            System.out.println("供应商数量: " + supplierList.size());

            if (supplierList.size() > 0) {
                for (NxJrdhUserEntity supplierEntity : supplierList) {
                    Map<String, Object> mapUser = new HashMap<>();
                    mapUser.put("user", supplierEntity);

                    // 创建新的查询参数
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.put("batchId", 1);  // 订货：有批次
                    queryMap.put("purchaseTypeNotEqual", -1); // 排除入库类型
                    queryMap.put("dayuStatus", 1);
                    queryMap.put("startDate", startDate);
                    queryMap.put("stopDate", stopDate);
                    queryMap.put("disId", disId);
                    queryMap.put("sellUserId", supplierEntity.getNxJrdhUserId());
                    queryMap.put("offset", 0);
                    queryMap.put("limit", 100);

                    System.out.println("查询供应商商品: " + queryMap);
                    Integer goodsCount = nxDisPurcGoodsService.queryGoodsListCount(queryMap);
                    List<NxDistributerGoodsEntity> goodsList = nxDisPurcGoodsService.queryGoodsListWithPurchase(queryMap);
                    Double subTotal = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(queryMap);

                    mapUser.put("arr", goodsList);
                    mapUser.put("count", goodsCount);
                    mapUser.put("purSubtotal", String.format("%.1f", subTotal));
                    result.add(mapUser);
                }
            }

            mapR.put("total", new BigDecimal(totalAmount).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapR.put("supplierArr", result);
            
        } else if (type == -1) {
            // 入库：purchaseType == -1（按采购员分组）
            System.out.println("=== 入库统计（按采购员分组） ===");
            map.remove("batchId"); // 清除 batchId 条件
            map.put("purchaseType", -1);  // 入库：直接入库的采购

            // 计算总额
            Double totalAmount = 0.0;
            Integer totalCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
            if (totalCount > 0) {
                totalAmount = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
            }
            System.out.println("入库总额: " + totalAmount);

            // 获取采购员列表（使用NxDistributerUser）
            System.out.println("查询采购员列表，参数: " + map);
            List<NxDistributerUserEntity> purUserList = nxDisPurcGoodsService.queryPurUserList(map);
            System.out.println("采购员数量: " + purUserList.size());
            if (purUserList.size() > 0) {
                for (int i = 0; i < purUserList.size(); i++) {
                    System.out.println("  采购员 " + (i + 1) + ": ID=" + purUserList.get(i).getNxDistributerUserId() + 
                                     ", 姓名=" + purUserList.get(i).getNxDiuWxNickName());
                }
            } else {
                System.out.println("⚠️ 没有查询到采购员，请检查SQL查询条件");
            }

            if (purUserList.size() > 0) {
                for (NxDistributerUserEntity userEntity : purUserList) {
                    Map<String, Object> mapUser = new HashMap<>();
                    mapUser.put("user", userEntity);

                    // 创建新的查询参数，避免污染
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.remove("batchId"); // 清除 batchId 条件
                    queryMap.put("purchaseType", -1);  // 入库：直接入库的采购
                    queryMap.put("dayuStatus", 1);
                    queryMap.put("startDate", startDate);
                    queryMap.put("stopDate", stopDate);
                    queryMap.put("disId", disId);
                    queryMap.put("purUserId", userEntity.getNxDistributerUserId());
                    queryMap.put("offset", 0);
                    queryMap.put("limit", 100);

                    System.out.println("查询采购员商品: " + queryMap);
                    Integer goodsCount = nxDisPurcGoodsService.queryGoodsListCount(queryMap);
                    List<NxDistributerGoodsEntity> goodsList = nxDisPurcGoodsService.queryGoodsListWithPurchase(queryMap);
                    Double subTotal = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(queryMap);

                    mapUser.put("arr", goodsList);
                    mapUser.put("count", goodsCount);
                    mapUser.put("purSubtotal", String.format("%.1f", subTotal));
                    result.add(mapUser);
                }
            }

            mapR.put("total", new BigDecimal(totalAmount).setScale(1, BigDecimal.ROUND_HALF_UP));
            mapR.put("purUserArr", result);
        }

        System.out.println("======== NX采购分组统计完成 ========");
        return R.ok().put("data", mapR);
    }

    /**
     * NX系统库存商品列表（分页）
     * @param disId 批发商ID
     * @param startDate 开始日期
     * @param stopDate 结束日期
     * @param page 页码
     * @param limit 每页数量
     * @return 库存商品列表（含库存批次）
     */
    @RequestMapping(value = "/getNxStockGoodsList", method = RequestMethod.POST)
    @ResponseBody
    public R getNxStockGoodsList(Integer disId, String startDate, String stopDate,
                                 String type,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer limit) {
        logger.info("[getNxStockGoodsList] 查询库存商品列表，参数: disId={}, startDate={}, stopDate={}, type={}, page={}, limit={}",
                disId, startDate, stopDate, type, page, limit);

        try {
            // 构建查询参数
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("disId", disId);
            queryMap.put("startDate", startDate);
            queryMap.put("stopDate", stopDate);
            queryMap.put("offset", (page - 1) * limit);
            queryMap.put("limit", limit);
            queryMap.put("restWeight", 0);  // 只查询有剩余库存的
            queryMap.put("orderByGoodsStockTotal", 1);  // 按库存总额排序

            // 根据类型设置查询条件（用于过滤支出记录）
            Integer equalType = null;
            String typesStr = null;
            if (type != null) {
                switch (type) {
                    case "sales":
                        // 售出：type = 0, 1
                        typesStr = "0,1";
                        break;
                    case "loss":
                        // 损耗：type = 2, 3
                        typesStr = "2,3";
                        break;
                    case "return":
                        // 退货：type = 4
                        equalType = 4;
                        break;
                    default:
                        // 不设置过滤条件，查询所有类型的支出记录
                        break;
                }
            }
            if (equalType != null) {
                queryMap.put("equalType", equalType);
            }
            if (typesStr != null) {
                queryMap.put("typesStr", typesStr);
            }

            logger.debug("[getNxStockGoodsList] 查询参数: {}", queryMap);

            // 统计商品总数
            Integer stockCount = shelfStockService.queryStockGoodsCount(queryMap);
            Double restSubtotal = shelfStockService.queryShelfStockRestTotal(queryMap);
            Integer totalPages = (int) Math.ceil((double) stockCount / limit);
            logger.info("[getNxStockGoodsList] 商品总数: {}, 库存总额: {}, 总页数: {}", stockCount, restSubtotal, totalPages);

            // 查询商品列表（每个商品包含库存批次和支出记录）
            List<NxDistributerGoodsEntity> goodsList = shelfStockService.queryGoodsStockList(queryMap);
            logger.info("[getNxStockGoodsList] 查询完成，返回商品数量: {}", goodsList != null ? goodsList.size() : 0);

            // 打印每个商品的库存批次和支出记录详情（debug级别）
            if (goodsList != null && goodsList.size() > 0) {
                logger.debug("[getNxStockGoodsList] 商品详情:");
                for (int i = 0; i < goodsList.size(); i++) {
                    NxDistributerGoodsEntity goods = goodsList.get(i);
                    logger.debug("--- 商品 {}: ID={}, 名称={}, 规格={}, 库存总金额={}, 库存总重量={}",
                            i + 1, goods.getNxDistributerGoodsId(), goods.getNxDgGoodsName(),
                            goods.getNxDgGoodsStandardname(), goods.getSellSubtotal(), goods.getSellAmount());

                    List<NxDistributerGoodsShelfStockEntity> stockList = goods.getNxDisGoodsShelfStockEntities();
                    if (stockList != null && stockList.size() > 0) {
                        logger.debug("  库存批次数量: {}", stockList.size());
                        for (int j = 0; j < stockList.size(); j++) {
                            NxDistributerGoodsShelfStockEntity stock = stockList.get(j);
                            logger.debug("  库存批次 {}: ID={}, 入库日期={}, 采购单价={}, 原始重量={}, 剩余重量={}, 剩余金额={}",
                                    j + 1, stock.getNxDistributerGoodsShelfStockId(), stock.getNxDgssInventoryDate(),
                                    stock.getNxDgssPrice(), stock.getNxDgssWeight(), stock.getNxDgssRestWeight(),
                                    stock.getNxDgssRestSubtotal());

                            // 打印支出记录列表
                            List<NxDistributerGoodsShelfStockReduceEntity> reduceList = stock.getReduceEntityList();
                            if (reduceList != null && reduceList.size() > 0) {
                                logger.debug("    支出记录数量: {}", reduceList.size());
                                for (int k = 0; k < reduceList.size(); k++) {
                                    NxDistributerGoodsShelfStockReduceEntity reduce = reduceList.get(k);
                                    logger.info("    支出记录 {}: 类型={}, 日期={}, 成本金额={}, 损耗金额={}, 部门={}, 订单ID={}, 用户ID={}, 订货数量={}, 订货规格={}, 支出人={}",
                                            k + 1, reduce.getNxDgssrType(), reduce.getNxDgssrDate(),
                                            reduce.getNxDgssrCostSubtotal(), reduce.getNxDgssrWasteSubtotal(),
                                            reduce.getDepartmentName(), reduce.getNxDgssrNxDepOrderId(), 
                                            reduce.getNxDgssrDoUserId(), reduce.getOrderQuantity(), 
                                            reduce.getOrderStandard(), reduce.getDoUserName());
                        }
                    } else {
                                logger.debug("    支出记录: 无");
                            }
                        }
                    } else {
                        logger.debug("  库存批次: 无");
                    }
                }
            }

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", stockCount);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("goodsList", goodsList);
            result.put("restSubtotal", String.format("%.1f", restSubtotal));

            logger.info("[getNxStockGoodsList] 查询完成，返回数据");
            return R.ok().put("data", result);

        } catch (Exception e) {
            logger.error("[getNxStockGoodsList] 查询库存商品列表异常", e);
            return R.error("获取库存商品列表失败：" + e.getMessage());
        }
    }

    /**
     * NX采购分类统计接口
     * 参考GB项目的disGetPurchaseCata接口
     * 按商品分类统计采购和支出数据
     * 
     * @param disId 配送商ID
     * @param startDate 开始日期
     * @param stopDate 结束日期
     * @param purUserId 采购员ID（可选，"-1"表示全部）
     * @param supplierId 供应商ID（可选，"-1"表示全部）
     * @return 分类统计数据
     */
    @RequestMapping(value = "/disGetPurchaseCata", method = RequestMethod.POST)
    @ResponseBody
    public R disGetPurchaseCata(Integer disId, String startDate, String stopDate,
                                String purUserId, String supplierId) {
        logger.info("[disGetPurchaseCata] 开始查询，参数: disId={}, startDate={}, stopDate={}, purUserId={}, supplierId={}", 
                disId, startDate, stopDate, purUserId, supplierId);

        Map<String, Object> map123 = new HashMap<>();
        Map<String, Object> mapDep = new HashMap<>();
        mapDep.put("disId", disId);
        mapDep.put("dayuStatus", 1); // 已完成的采购
        mapDep.put("startDate", startDate);
        mapDep.put("stopDate", stopDate);

        // 1. 采购总额统计
        Integer purchaseCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapDep);
        BigDecimal purchaseTotal = new BigDecimal(0);
        
        if (purchaseCount == 0) {
            logger.warn("[disGetPurchaseCata] 没有数据");
            return R.error(-1, "没有数据");
        }
        
        if (purchaseCount > 0) {
            Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(mapDep);
            purchaseTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        logger.info("[disGetPurchaseCata] 采购总额: {}", purchaseTotal);

        // 2. 支出总额统计（3种类型：销售、损耗、退货）
        // 2.1 销售支出总额 (type = 0, 1)
        Map<String, Object> mapSaleTotal = new HashMap<>();
        mapSaleTotal.put("disId", disId);
        mapSaleTotal.put("startDate", startDate);
        mapSaleTotal.put("stopDate", stopDate);
        List<Integer> saleTypes = new ArrayList<>();
        saleTypes.add(0);
        saleTypes.add(1);
        mapSaleTotal.put("types", saleTypes);
        BigDecimal saleCostTotal = new BigDecimal(0);
        Integer saleCount = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapSaleTotal);
        if (saleCount > 0) {
            Double aDouble = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(mapSaleTotal);
            saleCostTotal = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
        }

        // 2.2 损耗支出总额 (type = 2, 3)
        Map<String, Object> mapLossTotal = new HashMap<>();
        mapLossTotal.put("disId", disId);
        mapLossTotal.put("startDate", startDate);
        mapLossTotal.put("stopDate", stopDate);
        List<Integer> lossTypes = new ArrayList<>();
        lossTypes.add(2);
        lossTypes.add(3);
        mapLossTotal.put("types", lossTypes);
        BigDecimal lossCostTotal = new BigDecimal(0);
        Integer lossCount = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapLossTotal);
        if (lossCount > 0) {
            Double aDouble = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(mapLossTotal);
            lossCostTotal = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
        }

        // 2.3 退货支出总额 (type = 4)
        Map<String, Object> mapReturnTotal = new HashMap<>();
        mapReturnTotal.put("disId", disId);
        mapReturnTotal.put("startDate", startDate);
        mapReturnTotal.put("stopDate", stopDate);
        mapReturnTotal.put("equalType", 4);
        BigDecimal returnCostTotal = new BigDecimal(0);
        Integer returnCount = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapReturnTotal);
        if (returnCount > 0) {
            Double aDouble = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(mapReturnTotal);
            returnCostTotal = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
        }

        BigDecimal totalCost = saleCostTotal.add(lossCostTotal).add(returnCostTotal);
        logger.info("[disGetPurchaseCata] 支出总额: {} (销售: {}, 损耗: {}, 退货: {})", 
                totalCost, saleCostTotal, lossCostTotal, returnCostTotal);

        // 3. 查询采购员列表和供应商列表
        List<NxDistributerUserEntity> purUserList = nxDisPurcGoodsService.queryPurUserList(mapDep);
        List<NxJrdhUserEntity> supplierList = new ArrayList<>();
        if (supplierId != null && !supplierId.equals("-1")) {
            // 如果有供应商过滤，查询供应商列表
            Map<String, Object> supplierMap = new HashMap<>();
            supplierMap.put("disId", disId);
            supplierMap.put("startDate", startDate);
            supplierMap.put("stopDate", stopDate);
            supplierMap.put("dayuStatus", 1);
            supplierList = nxDisPurcGoodsService.querySupplierList(supplierMap);
        }

        map123.put("purUserList", purUserList);
        map123.put("supplierList", supplierList);

        // 4. 处理过滤条件
        if (purUserId != null && !purUserId.equals("-1")) {
            mapDep.put("purUserId", purUserId);
        }
        if (supplierId != null && !supplierId.equals("-1")) {
            mapDep.put("supplierId", supplierId);
        }

        // 5. 查询商品分类树（基于库存商品）
        // 先检查是否有库存记录
        Map<String, Object> mapStockTree = new HashMap<>();
        mapStockTree.put("disId", disId);
        mapStockTree.put("status", 0); // 有效库存
        if (startDate != null) {
            mapStockTree.put("startDate", startDate);
        }
        if (stopDate != null) {
            mapStockTree.put("stopDate", stopDate);
        }
        
        Integer stockCount = shelfStockService.queryShelfStockCount(mapStockTree);
        if (stockCount > 0) {
            logger.info("[disGetPurchaseCata] 开始查询商品分类树（基于库存），参数: {}", mapStockTree);
            
            // 通过库存记录查询分类树
            List<NxDistributerFatherGoodsEntity> fatherGoodsList = shelfStockService.queryStockTreeFatherGoodsByParams(mapStockTree);
            
            // 转换为TreeSet
            TreeSet<NxDistributerFatherGoodsEntity> greatGrandGoods = new TreeSet<>();
            if (fatherGoodsList != null) {
                greatGrandGoods.addAll(fatherGoodsList);
            }
            
            logger.info("[disGetPurchaseCata] 查询到分类数: {}", greatGrandGoods.size());

            // 6. 对每个分类统计数据
            for (NxDistributerFatherGoodsEntity greatEntity : greatGrandGoods) {
                Map<String, Object> cataMap = new HashMap<>();

                // 6.1 该分类的采购总额
                Map<String, Object> mapPur = new HashMap<>();
                mapPur.put("disId", disId);
                mapPur.put("startDate", startDate);
                mapPur.put("stopDate", stopDate);
                mapPur.put("dayuStatus", 1);
                mapPur.put("disGoodsGreatId", greatEntity.getNxDistributerFatherGoodsId());
                
                Integer purCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapPur);
                BigDecimal greatPurTotal = new BigDecimal(0);
                if (purCount > 0) {
                    Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(mapPur);
                    greatPurTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                
                // 计算占比
                BigDecimal greatPercent = new BigDecimal(0);
                if (purchaseTotal.compareTo(new BigDecimal(0)) > 0) {
                    greatPercent = greatPurTotal.divide(purchaseTotal, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(new BigDecimal(100));
                }
                
                cataMap.put("greatPurTotal", greatPurTotal);
                cataMap.put("greatPercent", greatPercent);
                cataMap.put("purTotal", purchaseTotal);

                // 6.2 自采数据 (purchaseType = 0)
                mapPur.put("purchaseType", 0);
                Integer zicaiCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapPur);
                BigDecimal zicaiTotal = new BigDecimal(0);
                if (zicaiCount > 0) {
                    Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(mapPur);
                    zicaiTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                cataMap.put("zicai", zicaiTotal);
                mapPur.remove("purchaseType");

                // 6.3 订货数据 (batchId = 1)
                mapPur.put("batchId", 1);
                Integer dinghuoCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapPur);
                BigDecimal dinghuoTotal = new BigDecimal(0);
                if (dinghuoCount > 0) {
                    Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(mapPur);
                    dinghuoTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                cataMap.put("dinghuo", dinghuoTotal);
                mapPur.remove("batchId");

                // 6.4 直接入库数据 (purchaseType = -1)
                mapPur.put("purchaseType", -1);
                Integer directStockCount = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapPur);
                BigDecimal directStockTotal = new BigDecimal(0);
                if (directStockCount > 0) {
                    Double aDouble = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(mapPur);
                    directStockTotal = new BigDecimal(aDouble).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                cataMap.put("directStock", directStockTotal);
                mapPur.remove("purchaseType");

                // 6.5 支出数据（3种类型）
                Map<String, Object> mapCost = new HashMap<>();
                mapCost.put("disId", disId);
                mapCost.put("startDate", startDate);
                mapCost.put("stopDate", stopDate);
                mapCost.put("disGoodsGreatId", greatEntity.getNxDistributerFatherGoodsId());

                // 6.5.1 销售支出 (type = 0, 1)
                mapCost.put("types", saleTypes);
                BigDecimal saleCost = new BigDecimal(0);
                Integer saleCountCat = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapCost);
                if (saleCountCat > 0) {
                    Double aDouble = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(mapCost);
                    saleCost = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
                }

                // 6.5.2 损耗支出 (type = 2, 3)
                mapCost.put("types", lossTypes);
                BigDecimal lossCost = new BigDecimal(0);
                Integer lossCountCat = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapCost);
                if (lossCountCat > 0) {
                    Double aDouble = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(mapCost);
                    lossCost = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
                }

                // 6.5.3 退货支出 (type = 4)
                mapCost.remove("types");
                mapCost.put("equalType", 4);
                BigDecimal returnCost = new BigDecimal(0);
                Integer returnCountCat = nxDisGoodsShelfStockReduceService.queryReduceTypeCount(mapCost);
                if (returnCountCat > 0) {
                    Double aDouble = nxDisGoodsShelfStockReduceService.queryReduceCostSubtotal(mapCost);
                    returnCost = new BigDecimal(aDouble).setScale(1, BigDecimal.ROUND_HALF_UP);
                }

                BigDecimal costTotal = saleCost.add(lossCost).add(returnCost);
                BigDecimal costPercent = new BigDecimal(0);
                if (totalCost.compareTo(new BigDecimal(0)) > 0) {
                    costPercent = costTotal.divide(totalCost, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(new BigDecimal(100));
                }

                cataMap.put("saleCostTotal", saleCost);
                cataMap.put("lossCostTotal", lossCost);
                cataMap.put("returnCostTotal", returnCost);
                cataMap.put("costTotal", costTotal);
                cataMap.put("costAllTotal", totalCost);
                cataMap.put("costPercent", costPercent);

                // 6.6 库存数据
                Map<String, Object> mapStock = new HashMap<>();
                mapStock.put("disId", disId);
                mapStock.put("disGoodsGreatId", greatEntity.getNxDistributerFatherGoodsId());
                mapStock.put("status", 0); // 有效库存

                // 6.6.1 所有库存
                Double allStock = 0.0;
                Integer allStockCount = shelfStockService.queryShelfStockCount(mapStock);
                if (allStockCount > 0) {
                    allStock = shelfStockService.queryShelfStockRestTotal(mapStock);
                }

                // 6.6.2 本期库存
                Map<String, Object> mapStockThis = new HashMap<>();
                mapStockThis.put("disId", disId);
                mapStockThis.put("disGoodsGreatId", greatEntity.getNxDistributerFatherGoodsId());
                mapStockThis.put("status", 0);
                mapStockThis.put("startDate", startDate);
                mapStockThis.put("stopDate", stopDate);
                Double perStock = 0.0;
                Integer perStockCount = shelfStockService.queryShelfStockCount(mapStockThis);
                if (perStockCount > 0) {
                    perStock = shelfStockService.queryShelfStockRestTotal(mapStockThis);
                }

                // 6.6.3 上期库存
                Map<String, Object> mapStockLast = new HashMap<>();
                mapStockLast.put("disId", disId);
                mapStockLast.put("disGoodsGreatId", greatEntity.getNxDistributerFatherGoodsId());
                mapStockLast.put("status", 0);
                mapStockLast.put("stopDate", startDate);
                Double lastStock = 0.0;
                Integer lastStockCount = shelfStockService.queryShelfStockCount(mapStockLast);
                if (lastStockCount > 0) {
                    lastStock = shelfStockService.queryShelfStockRestTotal(mapStockLast);
                }

                cataMap.put("lastStock", String.format("%.1f", lastStock));
                cataMap.put("perStock", String.format("%.1f", perStock));
                cataMap.put("costTotal", String.format("%.1f", costTotal));
                cataMap.put("costAllTotal", String.format("%.1f", totalCost));
                cataMap.put("costPercent", String.format("%.1f", costPercent.doubleValue()));
                cataMap.put("saleCostTotal", String.format("%.1f", saleCost.doubleValue()));
                cataMap.put("lossCostTotal", String.format("%.1f", lossCost.doubleValue()));
                cataMap.put("returnCostTotal", String.format("%.1f", returnCost.doubleValue()));

                greatEntity.setDailyData(cataMap);
            }
            
            map123.put("arr", greatGrandGoods);
        } else {
            map123.put("arr", new TreeSet<NxDistributerFatherGoodsEntity>());
        }

        logger.info("[disGetPurchaseCata] 查询完成");
        return R.ok().put("data", map123);
    }

    /**
     * 获取供货商统计信息
     */
    @RequestMapping(value = "/getNxPurGoodsStatisticsForDis", method = RequestMethod.POST)
    @ResponseBody
    public R getNxPurGoodsStatisticsForDis(@RequestParam String supplierIds,
                                           @RequestParam String purUserIds,
                                           @RequestParam Integer disId,
                                           @RequestParam String startDate,
                                           @RequestParam String stopDate) {

        try {
            // 构建查询参数
            Map<String, Object> map = new HashMap<>();

            if (!purUserIds.equals("-1")) {
                String[] arrNx = purUserIds.split(",");
                List<String> idsNx = new ArrayList<>();
                for (String idNx : arrNx) {
                    idsNx.add(idNx);
                    if (idsNx.size() > 0) {
                        map.put("purUserIds", idsNx);
                    }
                }
            }
            if (!supplierIds.equals("-1")) {
                String[] arrNx = supplierIds.split(",");
                List<String> idsNx = new ArrayList<>();
                for (String idNx : arrNx) {
                    idsNx.add(idNx);
                    if (idsNx.size() > 0) {
                        map.put("supplierIds", idsNx);
                    }
                }
            }

            map.put("disId", disId);
            map.put("startDate", startDate);
            map.put("stopDate", stopDate);
            // map.put("purchaseTypeNotEqual", -1);  // 排除入库（purchaseType=-1）的记录

            Integer integer = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
            double purtotal = 0.0;
            if (integer > 0) {
                purtotal = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
            }

            //采购员数据
            List<Map<String, Object>> purUserList = new ArrayList<>();
            System.out.println("puruuruuruuruuruuuruur" + map);
            map.put("disGoodsGrandId", null);
            List<NxDistributerUserEntity> userEntities = nxDisPurcGoodsService.queryPurUserList(map);
            if (userEntities.size() > 0) {
                for (NxDistributerUserEntity userEntity : userEntities) {
                    Map<String, Object> mapPurUser = new HashMap<>();
                    mapPurUser.put("purUserName", userEntity.getNxDiuWxNickName());
                    map.put("purUserId", userEntity.getNxDistributerUserId());
                    map.put("supplierId", null);
                    Double subTotal = 0.0;
                    Integer integerD = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
                    if (integerD > 0) {
                        subTotal = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
                    }

                    mapPurUser.put("totalAmount", String.format("%.1f", subTotal));
                    purUserList.add(mapPurUser);
                }
            }


            //供货商数据
            List<Map<String, Object>> supplerList = new ArrayList<>();
            map.put("disGoodsGrandId", null);
            List<NxJrdhUserEntity> supplierEntities = nxDisPurcGoodsService.querySupplierList(map);
            if (supplierEntities.size() > 0) {
                for (NxJrdhUserEntity supplierEntity : supplierEntities) {
                    Map<String, Object> mapPurUser = new HashMap<>();
                    mapPurUser.put("supplierName", supplierEntity.getNxJrdhWxNickName());
                    map.put("supplierId", supplierEntity.getNxJrdhUserId());
                    map.put("purUserId", null);
                    Double subTotal = 0.0;

                    System.out.println("suppsmap" + map);
                    Integer integerS = nxDisPurcGoodsService.queryPurchaseGoodsCount(map);
                    if (integerS > 0) {
                        subTotal = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(map);
                    }
                    mapPurUser.put("totalAmount", String.format("%.1f", subTotal));
                    supplerList.add(mapPurUser);
                }
            }

            //按采购频率数据
            map.put("supplierId", null);
            System.out.println("tootititittiitqueryNxPurchaseGoodsTopTimes" + map);
            List<NxDistributerGoodsEntity> topGoods = new ArrayList<>();

            //按采购金额数据
            List<NxDistributerGoodsEntity> topGoodsWeight  = new ArrayList<>();
            double topSubtotal = 0.0;

            List<NxDistributerGoodsEntity> topGoodsPrice = new ArrayList<>();

            if(integer > 0){
                topGoods  = nxDisPurcGoodsService.queryNxPurchaseGoodsTopTimes(map);
                topGoodsWeight = nxDisPurcGoodsService.queryNxPurchaseGoodsTopSubtotal(map);
                Double topSubtotalObj = nxDisPurcGoodsService.queryNxPurchaseSubtotalTopSubtotal(map);
                topSubtotal = topSubtotalObj != null ? topSubtotalObj : 0.0;
                //按价格浮动
                map.put("supplierId", null);
                System.out.println("tootititittiit" + map);
                topGoodsPrice = nxDisPurcGoodsService.queryNxPurchaseGoodsTopPriceFluctuation(map);
            }
            // 构建返回数据
            Map<String, Object> result = new HashMap<>();

            result.put("purTotal", String.format("%.1f", purtotal));
            result.put("purUserData", purUserList);
            result.put("supplierData", supplerList);
            result.put("topTimesGoods", topGoods);
            result.put("topSubtotalGoods", topGoodsWeight);
            result.put("topGoodsPrice", topGoodsPrice);
            BigDecimal bigDecimal = new BigDecimal(0);

            if(purtotal > 0){
                double v = topSubtotal / purtotal;
                bigDecimal = new BigDecimal(v).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            }

            result.put("topSubtotalGoodsPercent", bigDecimal);
            result.put("topSubtotalGoodsSubtotal", new BigDecimal(topSubtotal).setScale(1, BigDecimal.ROUND_HALF_UP));
            return R.ok().put("data", result);

        } catch (Exception e) {
            e.printStackTrace();
            return R.error(-1, "没有数据");
        }
    }

    /**
     * 获取采购商品详情列表
     */
    @RequestMapping(value = "/getNxPurGoodsDetailList", method = RequestMethod.POST)
    @ResponseBody
    public R getNxPurGoodsDetailList(
            @RequestParam Integer disGoodsId,
            @RequestParam String startDate,
            @RequestParam String stopDate) {

        Integer howManyDaysInPeriod = 0;
        if (!startDate.equals(stopDate)) {
            howManyDaysInPeriod = getHowManyDaysInPeriod(stopDate, startDate);
        }
        Map<String, Object> mapResult = new HashMap<>();

        try {
            // 构建查询参数
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("goodsId", disGoodsId);  // NX项目使用goodsId参数
            queryMap.put("startDate", startDate);
            queryMap.put("stopDate", stopDate);
            queryMap.put("dayuStatus", 2);  // 已完成的采购（状态 > 2）
            queryMap.put("purchaseTypeNotEqual", -1);  // 排除入库（purchaseType=-1）的记录

            // 获取商品列表
            System.out.println("查询商品map" + queryMap);
            System.out.println("开始查询商品列表...");
            List<NxDistributerPurchaseGoodsEntity> purchaseGoodsEntityList = nxDisPurcGoodsService.queryPurchaseGoodsWithDetailByParams(queryMap);
            List<Map<String, Object>> purchaseDayValue = new ArrayList<>();

            if (howManyDaysInPeriod > 0) {
                // 按天统计
                for (int i = 0; i < howManyDaysInPeriod + 1; i++) {
                    Map<String, Object> mapEvery = new HashMap<>();

                    // dateList
                    String whichDay = "";
                    if (i == 0) {
                        whichDay = startDate;
                    } else {
                        whichDay = afterWhatDay(startDate, i);
                    }
                    Map<String, Object> mapDay = new HashMap<>();
                    mapDay.put("date", whichDay);
                    mapDay.put("disGoodsId", disGoodsId);
                    mapDay.put("purchaseTypeNotEqual", -1);  // 排除入库记录
                    mapDay.put("dayuStatus", 2);  // 已完成的采购（状态 > 2）
                    Integer integer1 = nxDisPurcGoodsService.queryPurchaseGoodsCount(mapDay);
                    mapEvery.put("date", whichDay);
                    if (integer1 > 0) {
                        Double subTotal = nxDisPurcGoodsService.queryPurchaseGoodsSubTotal(mapDay);
                        mapEvery.put("purSubtotal", String.format("%.1f", subTotal));
                    } else {
                        mapEvery.put("purSubtotal", 0);
                    }
                    purchaseDayValue.add(mapEvery);
                }
            }

            mapResult.put("arr", purchaseGoodsEntityList);
            mapResult.put("itemList", purchaseDayValue);
            return R.ok().put("data", mapResult);

        } catch (Exception e) {
            System.out.println("查询商品列表异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("获取商品列表失败：" + e.getMessage());
        }
    }

    /**
     * 库存调拨接口：不同货架商品之间，如果是同一个商品，可以相互调库存
     * 
     * @param requestMap 请求参数Map，包含：
     *                   - purchaseGoodsId: 调出方的采购商品ID
     *                   - applyShelfId: 调入方的申请货架ID
     *                   - sourceStockId: 调出的库存批次ID
     *                   - transferQuantity: 调出的数量
     *                   - userId: 操作人ID（可选）
     * @return
     */
    @RequestMapping(value = "/transferStockBetweenShelves", method = RequestMethod.POST)
    @ResponseBody
    public R transferStockBetweenShelves(@RequestBody Map<String, Object> requestMap) {
        
        try {
            // 从请求Map中提取参数
            Integer purchaseGoodsId = requestMap.get("purchaseGoodsId") != null 
                ? Integer.parseInt(requestMap.get("purchaseGoodsId").toString()) : null;
            Integer applyShelfId = requestMap.get("applyShelfId") != null 
                ? Integer.parseInt(requestMap.get("applyShelfId").toString()) : null;
            Integer sourceStockId = requestMap.get("sourceStockId") != null 
                ? Integer.parseInt(requestMap.get("sourceStockId").toString()) : null;
            String transferQuantity = requestMap.get("transferQuantity") != null 
                ? requestMap.get("transferQuantity").toString() : null;
            Integer userId = requestMap.get("userId") != null 
                ? Integer.parseInt(requestMap.get("userId").toString()) : null;
            
            System.out.println("========== [transferStockBetweenShelves] 开始库存调拨 ==========");
            System.out.println("请求参数: " + requestMap);
            System.out.println("调出采购商品ID: " + purchaseGoodsId);
            System.out.println("调入货架ID: " + applyShelfId);
            System.out.println("调出库存批次ID: " + sourceStockId);
            System.out.println("调出数量: " + transferQuantity);
            System.out.println("操作人ID: " + userId);
            
            // 1. 查询源库存批次
            NxDistributerGoodsShelfStockEntity sourceStock = shelfStockService.queryObject(sourceStockId);
            if (sourceStock == null) {
                return R.error("源库存批次不存在");
            }
            
            // 2. 验证调出数量
            BigDecimal transferQty = new BigDecimal(transferQuantity);
            BigDecimal sourceRestWeight = new BigDecimal(sourceStock.getNxDgssRestWeight());
            if (transferQty.compareTo(BigDecimal.ZERO) <= 0) {
                return R.error("调出数量必须大于0");
            }
            if (transferQty.compareTo(sourceRestWeight) > 0) {
                return R.error("调出数量不能大于剩余库存数量");
            }
            
            // 3. 获取源库存批次关联的采购商品ID（直接使用，不创建新记录）
            Integer sourcePurGoodsId = sourceStock.getNxDgssNxPurGoodsId();
            if (sourcePurGoodsId == null) {
                return R.error("源库存批次未关联采购商品");
            }
            
            // 4. 查询源采购商品信息（用于获取商品ID等信息）
            NxDistributerPurchaseGoodsEntity sourcePurchaseGoods = nxDisPurcGoodsService.queryObject(sourcePurGoodsId);
            if (sourcePurchaseGoods == null) {
                return R.error("源采购商品不存在");
            }
            
            // 5. 查询商品信息
            NxDistributerGoodsEntity disGoodsEntity = nxDgService.queryObject(sourcePurchaseGoods.getNxDpgDisGoodsId());
            if (disGoodsEntity == null) {
                return R.error("商品信息不存在");
            }
            
            // 6. 查询或创建目标货架的商品
            NxDistributerGoodsShelfGoodsEntity targetShelfGoods = 
                shelfGoodsService.queryShlefGoodsByGoodsIdAndShelfId(
                    sourcePurchaseGoods.getNxDpgDisGoodsId(), applyShelfId);
            
            if (targetShelfGoods == null) {
                // 如果目标货架上没有该商品，需要先创建货架商品
                return R.error("目标货架上不存在该商品，请先添加商品到目标货架");
            }
            
            // 7. 计算调拨的单价和成本
            BigDecimal sourcePrice = new BigDecimal(sourceStock.getNxDgssPrice());
            BigDecimal transferSubtotal = transferQty.multiply(sourcePrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            
            // 8. 更新源库存批次（扣减数量）
            BigDecimal newRestWeight = sourceRestWeight.subtract(transferQty);
            sourceStock.setNxDgssRestWeight(newRestWeight.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            BigDecimal newRestSubtotal = newRestWeight.multiply(sourcePrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            sourceStock.setNxDgssRestSubtotal(newRestSubtotal.toPlainString());
            shelfStockService.update(sourceStock);
            System.out.println("源库存批次已更新，剩余数量: " + newRestWeight);
            
            // 9. 创建调出采购商品记录（状态=3完成，type=-2调出，数量单价都为0）
//            NxDistributerPurchaseGoodsEntity transferOutPurchaseGoods = new NxDistributerPurchaseGoodsEntity();
//            transferOutPurchaseGoods.setNxDpgDisGoodsId(sourcePurchaseGoods.getNxDpgDisGoodsId());
//            transferOutPurchaseGoods.setNxDpgDisGoodsFatherId(sourcePurchaseGoods.getNxDpgDisGoodsFatherId());
//            transferOutPurchaseGoods.setNxDpgDisGoodsGrandId(sourcePurchaseGoods.getNxDpgDisGoodsGrandId());
//            transferOutPurchaseGoods.setNxDpgDistributerId(sourcePurchaseGoods.getNxDpgDistributerId());
            NxDistributerPurchaseGoodsEntity nxDistributerPurchaseGoodsEntity = nxDisPurcGoodsService.queryObject(purchaseGoodsId);
            nxDistributerPurchaseGoodsEntity.setNxDpgQuantity("0"); // 数量为0
            nxDistributerPurchaseGoodsEntity.setNxDpgStatus(3); // 3 = 完成
            nxDistributerPurchaseGoodsEntity.setNxDpgPurchaseType(-2); // -2 = 调出
            nxDistributerPurchaseGoodsEntity.setNxDpgApplyDate(formatWhatYearDayTime(0));
            nxDistributerPurchaseGoodsEntity.setNxDpgPurchaseDate(formatWhatDay(0));
            nxDistributerPurchaseGoodsEntity.setNxDpgBuyPrice("0"); // 单价为0
            nxDistributerPurchaseGoodsEntity.setNxDpgBuyQuantity("0"); // 数量为0
            nxDistributerPurchaseGoodsEntity.setNxDpgBuySubtotal("0"); // 金额为0
            nxDistributerPurchaseGoodsEntity.setNxDpgPurUserId(userId);
            nxDistributerPurchaseGoodsEntity.setNxDpgInputType(0);
            nxDistributerPurchaseGoodsEntity.setNxDpgPayType(-1);
            nxDisPurcGoodsService.update(nxDistributerPurchaseGoodsEntity);
            System.out.println("调出采购商品记录已创建，ID: " + nxDistributerPurchaseGoodsEntity.getNxDistributerPurchaseGoodsId() + "，状态=3完成，type=-2调出");
            
            // 10. 在目标货架上创建新的库存批次（使用源库存批次关联的采购商品ID）
            NxDistributerGoodsShelfStockEntity targetStock = new NxDistributerGoodsShelfStockEntity();
            targetStock.setNxDgssNxPurGoodsId(sourcePurGoodsId); // 使用源库存批次关联的采购商品ID
            targetStock.setNxDgssNxDistributerId(sourcePurchaseGoods.getNxDpgDistributerId());
            targetStock.setNxDgssNxDisGoodsId(sourcePurchaseGoods.getNxDpgDisGoodsId());
            targetStock.setNxDgssNxDisGoodsFatherId(sourcePurchaseGoods.getNxDpgDisGoodsFatherId());
            targetStock.setNxDgssNxShelfGoodsId(targetShelfGoods.getNxDistributerGoodsShelfGoodsId());
            targetStock.setNxDgssWeight(transferQty.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            targetStock.setNxDgssRestWeight(transferQty.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            targetStock.setNxDgssPrice(sourcePrice.toPlainString());
            targetStock.setNxDgssPriceCarton(sourceStock.getNxDgssPriceCarton());
            targetStock.setNxDgssSellingPrice(sourceStock.getNxDgssSellingPrice());
            targetStock.setNxDgssSellingPriceCarton(sourceStock.getNxDgssSellingPriceCarton());
            targetStock.setNxDgssSubtotal(transferSubtotal.toPlainString());
            targetStock.setNxDgssRestSubtotal(transferSubtotal.toPlainString());
            targetStock.setNxDgssStatus(0);
            targetStock.setNxDgssDate(formatWhatDay(0));
            targetStock.setNxDgssMonth(formatWhatMonth(0));
            targetStock.setNxDgssYear(formatWhatYear(0));
            targetStock.setNxDgssProduceWeight("0");
            targetStock.setNxDgssProduceSubtotal("0");
            targetStock.setNxDgssLossWeight("0");
            targetStock.setNxDgssLossSubtotal("0");
            targetStock.setNxDgssWasteWeight("0");
            targetStock.setNxDgssWasteSubtotal("0");
            targetStock.setNxDgssReturnWeight("0");
            targetStock.setNxDgssReturnSubtotal("0");
            targetStock.setNxDgssReceiveUserId(userId);
            shelfStockService.save(targetStock);
            System.out.println("目标货架库存批次已创建，ID: " + targetStock.getNxDistributerGoodsShelfStockId());
            
            // 11. 创建库存减少记录（type = 5，表示调出）
            NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
            reduceEntity.setNxDgssrNxDistributerId(sourcePurchaseGoods.getNxDpgDistributerId());
            reduceEntity.setNxDgssrNxDisGoodsId(sourcePurchaseGoods.getNxDpgDisGoodsId());
            reduceEntity.setNxDgssrNxDisGoodsFatherId(sourcePurchaseGoods.getNxDpgDisGoodsFatherId());
            reduceEntity.setNxDgssrNxStockId(sourceStockId);
            reduceEntity.setNxDgssrNxPurGoodsId(sourcePurchaseGoods.getNxDistributerPurchaseGoodsId());
            reduceEntity.setNxDgssrDate(formatWhatDay(0));
            reduceEntity.setNxDgssrWeek(getWeek(0));
            reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
            reduceEntity.setNxDgssrFullTime(formatWhatYearDayTime(0));
            reduceEntity.setNxDgssrType(5); // 5 = 调出
            reduceEntity.setNxDgssrDoUserId(userId);
            reduceEntity.setNxDgssrCostWeight(transferQty.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            reduceEntity.setNxDgssrCostSubtotal(transferSubtotal.toPlainString());
            reduceEntity.setNxDgssrWasteWeight("0");
            reduceEntity.setNxDgssrWasteSubtotal("0");
            reduceEntity.setNxDgssrLossWeight("0");
            reduceEntity.setNxDgssrLossSubtotal("0");
            reduceEntity.setNxDgssrReturnWeight("0");
            reduceEntity.setNxDgssrReturnSubtotal("0");
            reduceEntity.setNxDgssrProduceWeight("0");
            reduceEntity.setNxDgssrProduceSubtotal("0");
            nxDisGoodsShelfStockReduceService.save(reduceEntity);
            System.out.println("库存减少记录已创建，ID: " + reduceEntity.getNxDistributerGoodsShelfStockReduceId());
            
            System.out.println("========== [transferStockBetweenShelves] 库存调拨完成 ==========");
            
            Map<String, Object> result = new HashMap<>();
            result.put("transferOutPurchaseGoodsId", sourcePurchaseGoods.getNxDistributerPurchaseGoodsId()); // 调出采购商品ID
            result.put("targetStockId", targetStock.getNxDistributerGoodsShelfStockId());
            result.put("reduceRecordId", reduceEntity.getNxDistributerGoodsShelfStockReduceId());
            
            return R.ok().put("data", result);
            
        } catch (Exception e) {
            System.out.println("库存调拨异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("库存调拨失败：" + e.getMessage());
        }
    }

    /**
     * 简单的库存调拨接口：从一个库存批次调出到指定货架商品
     * 
     * @param sourceStockId 调出的库存批次ID
     * @param transferQuantity 调出的数量
     * @param targetShelfGoodsId 调入的货架商品ID
     * @param userId 操作人ID（可选）
     * @return
     */
    @RequestMapping(value = "/transferStockFromBatch", method = RequestMethod.POST)
    @ResponseBody
    public R transferStockFromBatch(
            @RequestParam("sourceStockId") Integer sourceStockId,
            @RequestParam("transferQuantity") String transferQuantity,
            @RequestParam("targetShelfGoodsId") Integer targetShelfGoodsId,
            @RequestParam(value = "userId", required = false) Integer userId) {
        
        try {
            System.out.println("========== [transferStockFromBatch] 开始库存调拨 ==========");
            System.out.println("调出库存批次ID: " + sourceStockId);
            System.out.println("调出数量: " + transferQuantity);
            System.out.println("调入货架商品ID: " + targetShelfGoodsId);
            System.out.println("操作人ID: " + userId);
            
            // 1. 参数验证
            if (sourceStockId == null) {
                return R.error("调出库存批次ID不能为空");
            }
            if (transferQuantity == null || transferQuantity.trim().isEmpty()) {
                return R.error("调出数量不能为空");
            }
            if (targetShelfGoodsId == null) {
                return R.error("调入货架商品ID不能为空");
            }
            
            // 2. 查询源库存批次
            NxDistributerGoodsShelfStockEntity sourceStock = shelfStockService.queryObject(sourceStockId);
            if (sourceStock == null) {
                return R.error("源库存批次不存在");
            }
            
            // 3. 验证调出数量
            BigDecimal transferQty = new BigDecimal(transferQuantity);
            BigDecimal sourceRestWeight = new BigDecimal(sourceStock.getNxDgssRestWeight());
            if (transferQty.compareTo(BigDecimal.ZERO) <= 0) {
                return R.error("调出数量必须大于0");
            }
            if (transferQty.compareTo(sourceRestWeight) > 0) {
                return R.error("调出数量不能大于剩余库存数量");
            }
            
            // 4. 查询目标货架商品
            NxDistributerGoodsShelfGoodsEntity targetShelfGoods = shelfGoodsService.queryObject(targetShelfGoodsId);
            if (targetShelfGoods == null) {
                return R.error("目标货架商品不存在");
            }
            
            // 5. 验证商品是否一致（源库存批次和目标货架商品必须是同一个商品）
            if (!sourceStock.getNxDgssNxDisGoodsId().equals(targetShelfGoods.getNxDgsgDisGoodsId())) {
                return R.error("源库存批次和目标货架商品不是同一个商品，无法调拨");
            }
            
            // 6. 计算调拨的单价和成本
            BigDecimal sourcePrice = new BigDecimal(sourceStock.getNxDgssPrice());
            BigDecimal transferSubtotal = transferQty.multiply(sourcePrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            
            // 7. 更新源库存批次（扣减数量）
            BigDecimal newRestWeight = sourceRestWeight.subtract(transferQty);
            sourceStock.setNxDgssRestWeight(newRestWeight.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            BigDecimal newRestSubtotal = newRestWeight.multiply(sourcePrice).setScale(1, BigDecimal.ROUND_HALF_UP);
            sourceStock.setNxDgssRestSubtotal(newRestSubtotal.toPlainString());
            shelfStockService.update(sourceStock);
            System.out.println("源库存批次已更新，剩余数量: " + newRestWeight);
            
            // 8. 在目标货架商品下创建新的库存批次
            NxDistributerGoodsShelfStockEntity targetStock = new NxDistributerGoodsShelfStockEntity();
            targetStock.setNxDgssNxPurGoodsId(sourceStock.getNxDgssNxPurGoodsId()); // 使用源库存批次关联的采购商品ID
            targetStock.setNxDgssNxDistributerId(sourceStock.getNxDgssNxDistributerId());
            targetStock.setNxDgssNxDisGoodsId(sourceStock.getNxDgssNxDisGoodsId());
            targetStock.setNxDgssNxDisGoodsFatherId(sourceStock.getNxDgssNxDisGoodsFatherId());
            targetStock.setNxDgssNxShelfGoodsId(targetShelfGoodsId); // 使用传入的目标货架商品ID
            targetStock.setNxDgssWeight(transferQty.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            targetStock.setNxDgssRestWeight(transferQty.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            targetStock.setNxDgssPrice(sourcePrice.toPlainString());
            targetStock.setNxDgssPriceCarton(sourceStock.getNxDgssPriceCarton());
            targetStock.setNxDgssSellingPrice(sourceStock.getNxDgssSellingPrice());
            targetStock.setNxDgssSellingPriceCarton(sourceStock.getNxDgssSellingPriceCarton());
            targetStock.setNxDgssSubtotal(transferSubtotal.toPlainString());
            targetStock.setNxDgssRestSubtotal(transferSubtotal.toPlainString());
            targetStock.setNxDgssStatus(0);
            targetStock.setNxDgssDate(formatWhatDay(0));
            targetStock.setNxDgssMonth(formatWhatMonth(0));
            targetStock.setNxDgssYear(formatWhatYear(0));
            targetStock.setNxDgssProduceWeight("0");
            targetStock.setNxDgssProduceSubtotal("0");
            targetStock.setNxDgssLossWeight("0");
            targetStock.setNxDgssLossSubtotal("0");
            targetStock.setNxDgssWasteWeight("0");
            targetStock.setNxDgssWasteSubtotal("0");
            targetStock.setNxDgssReturnWeight("0");
            targetStock.setNxDgssReturnSubtotal("0");
            targetStock.setNxDgssReceiveUserId(userId);
            shelfStockService.save(targetStock);
            System.out.println("目标货架库存批次已创建，ID: " + targetStock.getNxDistributerGoodsShelfStockId());
            
            // 9. 创建库存减少记录（type = 5，表示调出）
            NxDistributerGoodsShelfStockReduceEntity reduceEntity = new NxDistributerGoodsShelfStockReduceEntity();
            reduceEntity.setNxDgssrNxDistributerId(sourceStock.getNxDgssNxDistributerId());
            reduceEntity.setNxDgssrNxDisGoodsId(sourceStock.getNxDgssNxDisGoodsId());
            reduceEntity.setNxDgssrNxDisGoodsFatherId(sourceStock.getNxDgssNxDisGoodsFatherId());
            reduceEntity.setNxDgssrNxStockId(sourceStockId);
            reduceEntity.setNxDgssrNxPurGoodsId(sourceStock.getNxDgssNxPurGoodsId());
            reduceEntity.setNxDgssrDate(formatWhatDay(0));
            reduceEntity.setNxDgssrWeek(getWeek(0));
            reduceEntity.setNxDgssrMonth(formatWhatMonth(0));
            reduceEntity.setNxDgssrFullTime(formatWhatYearDayTime(0));
            reduceEntity.setNxDgssrType(5); // 5 = 调出
            reduceEntity.setNxDgssrDoUserId(userId);
            reduceEntity.setNxDgssrCostWeight(transferQty.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString());
            reduceEntity.setNxDgssrCostSubtotal(transferSubtotal.toPlainString());
            reduceEntity.setNxDgssrWasteWeight("0");
            reduceEntity.setNxDgssrWasteSubtotal("0");
            reduceEntity.setNxDgssrLossWeight("0");
            reduceEntity.setNxDgssrLossSubtotal("0");
            reduceEntity.setNxDgssrReturnWeight("0");
            reduceEntity.setNxDgssrReturnSubtotal("0");
            reduceEntity.setNxDgssrProduceWeight("0");
            reduceEntity.setNxDgssrProduceSubtotal("0");
            nxDisGoodsShelfStockReduceService.save(reduceEntity);
            System.out.println("库存减少记录已创建，ID: " + reduceEntity.getNxDistributerGoodsShelfStockReduceId());
            
            System.out.println("========== [transferStockFromBatch] 库存调拨完成 ==========");
            
            Map<String, Object> result = new HashMap<>();
            result.put("targetStockId", targetStock.getNxDistributerGoodsShelfStockId());
            result.put("reduceRecordId", reduceEntity.getNxDistributerGoodsShelfStockReduceId());
            
            return R.ok().put("data", result);
            
        } catch (Exception e) {
            System.out.println("库存调拨异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("库存调拨失败：" + e.getMessage());
        }
    }

}

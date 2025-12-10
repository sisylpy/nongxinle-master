package com.nongxinle.controller;

import com.nongxinle.entity.SysCityMarketEntity;
import com.nongxinle.service.SysCityMarketService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 市场配置管理控制器
 * 管理市场的自助打印相关配置
 * 
 * @author lpy
 * @date 2025-01-17
 */
@RestController
@RequestMapping("api/market")
public class MarketConfigController {
    
    @Autowired
    private SysCityMarketService sysCityMarketService;
    
    /**
     * 获取市场配置信息
     * 
     * @param marketId 市场ID
     * @return 市场配置信息
     */
    @RequestMapping(value = "/config/{marketId}", method = RequestMethod.GET)
    @ResponseBody
    public R getMarketConfig(@PathVariable("marketId") Integer marketId) {
        try {
            SysCityMarketEntity market = sysCityMarketService.queryObject(marketId);
            if (market == null) {
                return R.error("市场不存在");
            }
            
            return R.ok().put("data", market);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新市场配置信息
     * 
     * @param market 市场配置信息
     * @return 更新结果
     */
    @RequestMapping(value = "/config/update", method = RequestMethod.POST)
    @ResponseBody
    public R updateMarketConfig(@RequestBody SysCityMarketEntity market) {
        try {
            // 验证必填字段
            if (market.getSysCityMarketId() == null) {
                return R.error("市场ID不能为空");
            }
            
            // 验证数据范围
            if (market.getSysCmRegisterGiftPoints() != null && market.getSysCmRegisterGiftPoints() < 0) {
                return R.error("注册赠送点数不能为负数");
            }
            
            if (market.getSysCmPointsPerYuan() != null && market.getSysCmPointsPerYuan() <= 0) {
                return R.error("兑换比例必须大于0");
            }
            
            if (market.getSysCmDeliveryRadius() != null && market.getSysCmDeliveryRadius() <= 0) {
                return R.error("配送半径必须大于0");
            }
            
            // 验证坐标格式
            if (market.getSysCmCenterLatitude() != null) {
                BigDecimal lat = market.getSysCmCenterLatitude();
                if (lat.compareTo(new BigDecimal("90")) > 0 || lat.compareTo(new BigDecimal("-90")) < 0) {
                    return R.error("纬度必须在-90到90之间");
                }
            }
            
            if (market.getSysCmCenterLongitude() != null) {
                BigDecimal lng = market.getSysCmCenterLongitude();
                if (lng.compareTo(new BigDecimal("180")) > 0 || lng.compareTo(new BigDecimal("-180")) < 0) {
                    return R.error("经度必须在-180到180之间");
                }
            }
            
            sysCityMarketService.update(market);
            return R.ok("更新成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 验证配送商是否在市场区域内
     * 
     * @param marketId 市场ID
     * @param latitude 配送商纬度
     * @param longitude 配送商经度
     * @return 验证结果
     */
    @RequestMapping(value = "/validateLocation", method = RequestMethod.GET)
    @ResponseBody
    public R validateDistributerLocation(@RequestParam("marketId") Integer marketId,
                                       @RequestParam("latitude") BigDecimal latitude,
                                       @RequestParam("longitude") BigDecimal longitude) {
        try {
            SysCityMarketEntity market = sysCityMarketService.queryObject(marketId);
            if (market == null) {
                return R.error("市场不存在");
            }
            
            // 检查市场是否设置了坐标
            if (market.getSysCmCenterLatitude() == null || market.getSysCmCenterLongitude() == null) {
                return R.error("市场未设置区域范围");
            }
            
            // 计算距离（简化版本，实际应用中可以使用更精确的地理计算）
            double distance = calculateDistance(
                market.getSysCmCenterLatitude().doubleValue(),
                market.getSysCmCenterLongitude().doubleValue(),
                latitude.doubleValue(),
                longitude.doubleValue()
            );
            
            int radius = market.getSysCmDeliveryRadius() != null ? market.getSysCmDeliveryRadius() : 5000;
            boolean inRange = distance <= radius;
            
            return R.ok()
                .put("inRange", inRange)
                .put("distance", Math.round(distance))
                .put("radius", radius)
                .put("message", inRange ? "在市场配送范围内" : "超出市场配送范围");
                
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("验证失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取市场列表（用于选择）
     * 
     * @return 市场列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public R getMarketList() {
        try {
            List<SysCityMarketEntity> markets = sysCityMarketService.queryList(null);
            return R.ok().put("data", markets);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 计算两点间距离（米）
     * 使用Haversine公式
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（公里）
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // 转换为米
        
        return distance;
    }
}

package com.nongxinle.controller;

import com.nongxinle.entity.NxMachinePaperRefillRecordEntity;
import com.nongxinle.entity.NxMachinePrintRecordEntity;
import com.nongxinle.entity.NxMachinePrinterDeviceEntity;
import com.nongxinle.entity.NxMachineMarketManagerEntity;
import com.nongxinle.service.NxMachinePaperRefillRecordService;
import com.nongxinle.service.NxMachinePrintRecordService;
import com.nongxinle.service.NxMachinePrinterDeviceService;
import com.nongxinle.service.NxMachineMarketManagerService;
import com.nongxinle.utils.R;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 市场换纸管理Controller
 * 用于统计市场的换纸情况和纸张消耗
 * 
 * @author lpy
 * @date 2025-01-16
 */
@RestController
@RequestMapping("/api/machine/refill")
public class MachineRefillController {
    
    private static final Logger logger = Logger.getLogger(MachineRefillController.class);

    @Autowired
    private NxMachinePaperRefillRecordService nxPaperRefillRecordService;

    /**
     * 查询市场设备换纸统计列表
     * 返回某市场下所有有换纸记录的设备及其统计数据
     * 
     * @param marketId 市场ID（必填）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param stopDate 结束日期（可选，格式：yyyy-MM-dd）
     * @param offset 分页偏移量（可选）
     * @param limit 每页数量（可选，默认20）
     * @return 设备列表和统计汇总
     */
    @RequestMapping(value = "/listMarketRecords", method = RequestMethod.GET)
    public R listMarketRefillRecords(@RequestParam("marketId") Integer marketId,
                        @RequestParam(value = "startDate", required = false) String startDate,
                        @RequestParam(value = "stopDate", required = false) String stopDate,
                        @RequestParam(value = "offset", required = false) Integer offset,
                        @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        try {
            logger.info("========== 查询市场设备换纸统计开始 ==========");
            logger.info("请求参数 - marketId: " + marketId);
            logger.info("请求参数 - startDate: " + startDate);
            logger.info("请求参数 - stopDate: " + stopDate);
            logger.info("请求参数 - offset: " + offset);
            logger.info("请求参数 - limit: " + limit);
            
            // 1. 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("marketId", marketId);
            
            // 日期参数处理
            if (startDate != null && !startDate.isEmpty()) {
                params.put("startDate", startDate);
            }
            if (stopDate != null && !stopDate.isEmpty()) {
                params.put("stopDate", stopDate);
            }
            
            // 分页参数
            if (offset != null) {
                params.put("offset", offset);
            }
            if (limit != null) {
                params.put("limit", limit);
            }

            logger.info("查询参数 params: " + params);

            // 2. 查询设备统计列表（按设备分组聚合）
            List<Map<String, Object>> list = nxPaperRefillRecordService.queryDeviceStatsByMarketId(params);
            logger.info("查询到设备数量: " + (list != null ? list.size() : 0));
            
            // 3. 查询设备总数
            int total = nxPaperRefillRecordService.countDevicesByMarketId(params);
            logger.info("设备总数: " + total);

            // 4. 计算汇总统计（整个市场的统计）
            int totalRefill = 0;
            int totalRefillWaste = 0;
            int totalCalibrateWaste = 0;

            for (Map<String, Object> device : list) {
                Integer deviceTotalRefill = ((Number) device.get("totalRefill")).intValue();
                Integer deviceRefillWaste = ((Number) device.get("refillWaste")).intValue();
                Integer deviceCalibrateWaste = ((Number) device.get("calibrateWaste")).intValue();
                
                logger.info("设备统计 - deviceId: " + device.get("deviceId") + 
                           ", deviceName: " + device.get("deviceName") + 
                           ", totalRefill: " + deviceTotalRefill + 
                           ", refillWaste: " + deviceRefillWaste + 
                           ", calibrateWaste: " + deviceCalibrateWaste);
                
                totalRefill += deviceTotalRefill;
                totalRefillWaste += deviceRefillWaste;
                totalCalibrateWaste += deviceCalibrateWaste;
            }

            logger.info("市场统计汇总 - totalRefill: " + totalRefill);
            logger.info("市场统计汇总 - totalRefillWaste: " + totalRefillWaste);
            logger.info("市场统计汇总 - totalCalibrateWaste: " + totalCalibrateWaste);
            logger.info("市场统计汇总 - totalWaste: " + (totalRefillWaste + totalCalibrateWaste));

            // 5. 构建汇总数据
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRefill", totalRefill);
            summary.put("totalWaste", totalRefillWaste + totalCalibrateWaste);
            summary.put("refillWaste", totalRefillWaste);
            summary.put("calibrateWaste", totalCalibrateWaste);

            logger.info("========== 查询市场设备换纸统计结束 ==========");
            
            return R.ok()
                .put("list", list)
                .put("total", total)
                .put("summary", summary);

        } catch (Exception e) {
            logger.error("查询失败: " + e.getMessage(), e);
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询设备换纸记录列表
     * 返回某设备的所有换纸记录，包括：
     * 1. 正常加纸/更换（类型1）
     * 2. 手动校准（类型3）
     * 
     * @param deviceId 设备ID（必填）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param stopDate 结束日期（可选，格式：yyyy-MM-dd）
     * @param offset 分页偏移量（可选）
     * @param limit 每页数量（可选，默认20）
     * @return 换纸记录列表和统计汇总
     */
    @RequestMapping(value = "/listDeviceRecords", method = RequestMethod.GET)
    public R listDeviceRefillRecords(@RequestParam("deviceId") Integer deviceId,
                        @RequestParam(value = "startDate", required = false) String startDate,
                        @RequestParam(value = "stopDate", required = false) String stopDate,
                        @RequestParam(value = "offset", required = false) Integer offset,
                        @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        try {
            logger.info("========== 查询设备换纸记录开始 ==========");
            logger.info("请求参数 - deviceId: " + deviceId);
            logger.info("请求参数 - startDate: " + startDate);
            logger.info("请求参数 - stopDate: " + stopDate);
            logger.info("请求参数 - offset: " + offset);
            logger.info("请求参数 - limit: " + limit);
            
            // 1. 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("deviceId", deviceId);
            
            // 日期参数处理
            if (startDate != null && !startDate.isEmpty()) {
                params.put("startDate", startDate);
            }
            if (stopDate != null && !stopDate.isEmpty()) {
                params.put("stopDate", stopDate);
            }
            
            // 分页参数
            if (offset != null) {
                params.put("offset", offset);
            }
            if (limit != null) {
                params.put("limit", limit);
            }

            logger.info("查询参数 params: " + params);

            // 2. 查询换纸记录列表（一次性查询，包含设备和操作人信息）
            List<Map<String, Object>> list = nxPaperRefillRecordService.queryByDeviceIdWithDetails(params);
            logger.info("查询到换纸记录数量: " + (list != null ? list.size() : 0));
            
            // 3. 查询总数
            int total = nxPaperRefillRecordService.countByDeviceIdWithDetails(params);
            logger.info("换纸记录总数: " + total);

            // 4. 计算汇总统计
            int totalRefill = 0;
            int totalRefillWaste = 0;
            int totalCalibrateWaste = 0;

            for (Map<String, Object> record : list) {
                Integer refillType = (Integer) record.get("refillType");
                Integer addCount = (Integer) record.get("addCount");
                Integer wasteCount = (Integer) record.get("wasteCount");
                
                logger.info("记录详情 - recordId: " + record.get("recordId") + 
                           ", refillType: " + refillType + 
                           ", addCount: " + addCount + 
                           ", wasteCount: " + wasteCount);
                
                if (refillType != null && refillType == 1) {
                    totalRefill += (addCount != null ? addCount : 0);
                    totalRefillWaste += (wasteCount != null ? wasteCount : 0);
                } else if (refillType != null && refillType == 3) {
                    totalCalibrateWaste += (wasteCount != null ? wasteCount : 0);
                }
            }

            logger.info("统计汇总 - totalRefill: " + totalRefill);
            logger.info("统计汇总 - totalRefillWaste: " + totalRefillWaste);
            logger.info("统计汇总 - totalCalibrateWaste: " + totalCalibrateWaste);
            logger.info("统计汇总 - totalWaste: " + (totalRefillWaste + totalCalibrateWaste));

            // 5. 构建汇总数据
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRefill", totalRefill);
            summary.put("totalWaste", totalRefillWaste + totalCalibrateWaste);
            summary.put("refillWaste", totalRefillWaste);
            summary.put("calibrateWaste", totalCalibrateWaste);

            logger.info("========== 查询设备换纸记录结束 ==========");
            
            return R.ok()
                .put("list", list)
                .put("total", total)
                .put("summary", summary);

        } catch (Exception e) {
            logger.error("查询失败: " + e.getMessage(), e);
            return R.error("查询失败：" + e.getMessage());
        }
    }
}
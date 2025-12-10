package com.nongxinle.controller;

import com.nongxinle.entity.NxMachinePrintRecordEntity;
import com.nongxinle.service.NxMachinePrintRecordService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印机系统 - 打印记录Controller
 *
 * @author lpy
 * @date 2025-10-15
 */
@RestController
@RequestMapping("api/machine/print")
public class MachinePrintRecordController {

    @Autowired
    private NxMachinePrintRecordService nxPrintRecordService;

    /**
     * 记录单据打印（核心接口）
     * @param billId 单据ID
     * @param deviceId 打印机设备ID
     * @param marketId 市场ID
     * @param distributerId 配送商ID
     * @param paperCount 打印纸张数量（必填）
     * @param billTotal 单据金额（可选，指订单金额）
     * @param billTradeNo 单据流水号（可选）
     * @param operatorId 操作人ID（可选）
     * @param operatorName 操作人姓名（可选）
     * @return 操作结果（包含打印记录ID和打印费用）
     */
//    @RequestMapping(value = "/recordPrint", method = RequestMethod.POST)
//    public R recordPrint(@RequestParam("billId") Integer billId,
//                        @RequestParam("deviceId") Integer deviceId,
//                        @RequestParam("marketId") Integer marketId,
//                        @RequestParam("distributerId") Integer distributerId,
//                        @RequestParam("paperCount") Integer paperCount,
//                        @RequestParam(value = "billTotal", required = false) Double billTotal,
//                        @RequestParam(value = "billTradeNo", required = false) String billTradeNo,
//                        @RequestParam(value = "operatorId", required = false) Integer operatorId,
//                        @RequestParam(value = "operatorName", required = false) String operatorName) {
//        try {
//            // 调用Service，传递纸张数量
//            Map<String, Object> result = nxPrintRecordService.recordPrint(
//                billId, deviceId, marketId, distributerId, paperCount,
//                billTotal, billTradeNo, operatorId, operatorName
//            );
//
//            return R.ok("记录成功")
//                .put("recordId", result.get("recordId"))
//                .put("printFee", result.get("printFee"))          // 打印费用
//                .put("paperCount", result.get("paperCount"))      // 打印张数
//                .put("pricePerSheet", result.get("pricePerSheet")); // 单价
//        } catch (Exception e) {
//            return R.error("记录失败：" + e.getMessage());
//        }
//    }

    /**
     * 查询打印历史列表（包含配送商名称和设备名称）
     * @param marketId 市场ID（必填）
     * @param deviceId 设备ID（可选）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param stopDate 结束日期（可选，格式：yyyy-MM-dd）
     * @param offset 分页偏移量
     * @param limit 每页数量
     * @return 打印记录列表（包含 distributerName-配送商名称、deviceName-设备名称、deviceNo-设备编号）
     */
    @RequestMapping(value = "/listPrintRecords", method = RequestMethod.GET)
    public R listPrintRecords(@RequestParam("marketId") Integer marketId,
                 @RequestParam(value = "deviceId", required = false) Integer deviceId,
                 @RequestParam(value = "startDate", required = false) String startDate,
                 @RequestParam(value = "stopDate", required = false) String stopDate,
                 @RequestParam(value = "offset", defaultValue = "0") Integer offset,
                 @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("marketId", marketId);
            params.put("deviceId", deviceId);
            params.put("printStatus", 1); // 只查询成功的记录
            params.put("offset", offset);
            params.put("limit", limit);
            
            // 日期参数处理（直接传字符串）
            if (startDate != null && !startDate.isEmpty()) {
                params.put("startDate", startDate);
            }
            if (stopDate != null && !stopDate.isEmpty()) {
                params.put("stopDate", stopDate);
            }
            
            // 查询打印记录列表（包含配送商名称）
            List<Map<String, Object>> list = nxPrintRecordService.queryListWithDistributer(params);
            int total = nxPrintRecordService.queryTotal(params);
            
            return R.ok()
                .put("list", list)
                .put("total", total);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询打印统计（合并版 - 同时返回汇总、每日统计和设备统计）
     * 按照查询日期和市场ID，统计一段时间内每个设备的打印情况
     *
     * @param marketId 市场ID（必填）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param stopDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 综合统计数据，包含：
     *         - summary: 汇总数据（总打印数、总纸张、总金额）
     *         - dailyStats: 每日统计列表
     *         - deviceStats: 设备统计列表（含设备详细信息和使用分析）
     */
    @RequestMapping(value = "/stats/print", method = RequestMethod.GET)
    public R deviceStatsPrint(@RequestParam("marketId") Integer marketId,
                         @RequestParam(value = "startDate", required = false) String startDate,
                         @RequestParam(value = "stopDate", required = false) String stopDate) {
        try {
            System.out.println("========== 打印统计接口开始 ==========");
            System.out.println("请求参数 - marketId: " + marketId);
            System.out.println("请求参数 - startDate: " + startDate);
            System.out.println("请求参数 - stopDate: " + stopDate);

            Map<String, Object> params = new HashMap<>();
            params.put("marketId", marketId);

            // 日期参数处理（只传日期字符串，在SQL中用DATE函数比较）
            if (startDate != null && !startDate.isEmpty()) {
                params.put("startDate", startDate);
                System.out.println("解析后 startDate: " + startDate);
            }
            if (stopDate != null && !stopDate.isEmpty()) {
                params.put("stopDate", stopDate);
                System.out.println("解析后 stopDate: " + stopDate);
            }

            System.out.println("查询参数 params: " + params);

            // 1. 获取每日统计
            List<Map<String, Object>> dailyStats = nxPrintRecordService.queryDailyStats(params);
            System.out.println("每日统计结果数量: " + dailyStats.size());
            if (!dailyStats.isEmpty()) {
                System.out.println("每日统计第一条: " + dailyStats.get(0));
            }

//            // 2. 获取设备统计
//            List<Map<String, Object>> deviceStats = nxPrintRecordService.queryDeviceStats(params);
//            System.out.println("设备统计结果数量: " + deviceStats.size());
//            if (!deviceStats.isEmpty()) {
//                System.out.println("设备统计第一条: " + deviceStats.get(0));
//            }

            // 3. 计算汇总数据
            int totalPrintCount = 0;
            int totalPaperUsed = 0;
            double totalAmount = 0.0;

            for (Map<String, Object> daily : dailyStats) {
                totalPrintCount += ((Number) daily.get("print_count")).intValue();
                totalPaperUsed += ((Number) daily.get("total_paper")).intValue();
                if (daily.get("total_amount") != null) {
                    totalAmount += ((Number) daily.get("total_amount")).doubleValue();
                }
            }

            System.out.println("汇总数据 - printCount: " + totalPrintCount);
            System.out.println("汇总数据 - totalPaper: " + totalPaperUsed);
            System.out.println("汇总数据 - totalAmount: " + totalAmount);

            // 4. 构建汇总对象
            Map<String, Object> summary = new HashMap<>();
            summary.put("printCount", totalPrintCount);
            summary.put("totalPaper", totalPaperUsed);
            summary.put("totalAmount", totalAmount);

            System.out.println("========== 打印统计接口结束 ==========");

            // 5. 返回综合数据
            return R.ok()
                    .put("summary", summary)           // 汇总数据
                    .put("dailyStats", dailyStats);  // 每日统计

        } catch (Exception e) {
            System.err.println("统计失败异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("统计失败：" + e.getMessage());
        }
    }

    /**
     * 查询打印统计（合并版 - 同时返回汇总、每日统计和设备统计）
     * 按照查询日期和市场ID，统计一段时间内每个设备的打印情况
     * 
     * @param marketId 市场ID（必填）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param stopDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 综合统计数据，包含：
     *         - summary: 汇总数据（总打印数、总纸张、总金额）
     *         - dailyStats: 每日统计列表
     *         - deviceStats: 设备统计列表（含设备详细信息和使用分析）
     */
    @RequestMapping(value = "/stats/device", method = RequestMethod.GET)
    public R deviceStats(@RequestParam("marketId") Integer marketId,
                        @RequestParam(value = "startDate", required = false) String startDate,
                        @RequestParam(value = "stopDate", required = false) String stopDate) {
        try {
            System.out.println("========== 打印统计接口开始 ==========");
            System.out.println("请求参数 - marketId: " + marketId);
            System.out.println("请求参数 - startDate: " + startDate);
            System.out.println("请求参数 - stopDate: " + stopDate);
            
            Map<String, Object> params = new HashMap<>();
            params.put("marketId", marketId);
            
            // 日期参数处理（只传日期字符串，在SQL中用DATE函数比较）
            if (startDate != null && !startDate.isEmpty()) {
                params.put("startDate", startDate);
                System.out.println("解析后 startDate: " + startDate);
            }
            if (stopDate != null && !stopDate.isEmpty()) {
                params.put("stopDate", stopDate);
                System.out.println("解析后 stopDate: " + stopDate);
            }
            
            System.out.println("查询参数 params: " + params);
            
            // 1. 获取每日统计
            List<Map<String, Object>> dailyStats = nxPrintRecordService.queryDailyStats(params);
            System.out.println("每日统计结果数量: " + dailyStats.size());
            if (!dailyStats.isEmpty()) {
                System.out.println("每日统计第一条: " + dailyStats.get(0));
            }
            
            // 2. 获取设备统计
            List<Map<String, Object>> deviceStats = nxPrintRecordService.queryDeviceStats(params);
            System.out.println("设备统计结果数量: " + deviceStats.size());
            if (!deviceStats.isEmpty()) {
                System.out.println("设备统计第一条: " + deviceStats.get(0));
            }
            
            // 3. 计算汇总数据（从每日统计中累加）
            int totalPrintCount = 0;
            int totalPaperUsed = 0;
            double totalAmount = 0.0;
            
            for (Map<String, Object> daily : dailyStats) {
                totalPrintCount += ((Number) daily.get("print_count")).intValue();
                totalPaperUsed += ((Number) daily.get("total_paper")).intValue();
                if (daily.get("total_amount") != null) {
                    totalAmount += ((Number) daily.get("total_amount")).doubleValue();
                }
            }
            
            System.out.println("汇总数据 - printCount: " + totalPrintCount);
            System.out.println("汇总数据 - totalPaper: " + totalPaperUsed);
            System.out.println("汇总数据 - totalAmount: " + totalAmount);
            
            // 4. 构建汇总对象
            Map<String, Object> summary = new HashMap<>();
            summary.put("printCount", totalPrintCount);
            summary.put("totalPaper", totalPaperUsed);
            summary.put("totalAmount", totalAmount);
            
            System.out.println("========== 打印统计接口结束 ==========");
            
            // 5. 返回综合数据
            return R.ok()
                .put("summary", summary)           // 汇总数据
                .put("dailyStats", dailyStats)     // 每日统计
                .put("deviceStats", deviceStats);  // 设备统计
                
        } catch (Exception e) {
            System.err.println("统计失败异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("统计失败：" + e.getMessage());
        }
    }



    /**
     * 查询配送商打印统计
     * @param marketId 市场ID（必填）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param stopDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 统计数据
     */
    @RequestMapping(value = "/stats/distributer", method = RequestMethod.GET)
    public R distributerStats(@RequestParam("marketId") Integer marketId,
                             @RequestParam(value = "startDate", required = false) String startDate,
                             @RequestParam(value = "stopDate", required = false) String stopDate) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("marketId", marketId);
            
            // 日期参数处理（直接传字符串）
            if (startDate != null && !startDate.isEmpty()) {
                params.put("startDate", startDate);
            }
            if (stopDate != null && !stopDate.isEmpty()) {
                params.put("stopDate", stopDate);
            }
            
            List<Map<String, Object>> stats = nxPrintRecordService.queryDistributerStats(params);
            return R.ok().put("stats", stats);
        } catch (Exception e) {
            return R.error("统计失败：" + e.getMessage());
        }
    }

    /**
     * 查询单据的打印记录
     * @param billId 单据ID
     * @return 打印记录列表
     */
    @RequestMapping(value = "/byBill", method = RequestMethod.GET)
    public R queryByBillId(@RequestParam("billId") Integer billId) {
        try {
            List<NxMachinePrintRecordEntity> list = nxPrintRecordService.queryByBillId(billId);
            return R.ok().put("list", list);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }
}


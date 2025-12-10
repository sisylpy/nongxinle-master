package com.nongxinle.controller;

import com.nongxinle.entity.NxMachinePrinterDeviceEntity;
import com.nongxinle.service.NxMachinePrinterDeviceService;
import com.nongxinle.service.NxMachineAlertThresholdService;
import com.nongxinle.service.NxMachinePaperRefillRecordService;
import com.nongxinle.service.NxMachineAlertRecordService;
import com.nongxinle.service.PrinterAlertService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印机系统 - 设备管理Controller
 *
 * @author lpy
 * @date 2025-10-14
 */
@RestController
@RequestMapping("api/machine/device")
public class MachineDeviceController {

    @Autowired
    private NxMachinePrinterDeviceService nxPrinterDeviceService;

    @Autowired
    private NxMachineAlertThresholdService nxAlertThresholdService;

    @Autowired
    private NxMachinePaperRefillRecordService nxPaperRefillRecordService;

    @Autowired
    private NxMachineAlertRecordService nxAlertRecordService;

    @Autowired
    private PrinterAlertService printerAlertService;

    /**
     * 根据市场ID查询设备列表
     * @param marketId 市场ID
     * @param managerId 管理员ID（可选，用于查询未读提醒数量）
     * @param days 查询N天内的未读数量（可选，默认2天）
     * @return 设备列表和未读提醒数量
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public R list(@RequestParam("marketId") Integer marketId,
                 @RequestParam(value = "managerId", required = false) Integer managerId,
                 @RequestParam(value = "days", required = false, defaultValue = "2") Integer days) {
        try {
            // 1. 查询设备列表
            List<NxMachinePrinterDeviceEntity> list = nxPrinterDeviceService.queryByMarketId(marketId);
            
            // 2. 如果传了managerId，则查询未读提醒数量
            Integer unReadCount = 0;
            if (managerId != null) {
                Map<String, Object> params = new HashMap<>();
                params.put("managerId", managerId);
                if (days != null && days > 0) {
                    params.put("days", days);
                }
                unReadCount = nxAlertRecordService.queryUnreadCount(params);
            }
            
            return R.ok()
                .put("list", list)
                .put("unReadCount", unReadCount);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询设备详情（包含阈值配置和责任人）
     * @param deviceId 设备ID
     * @return 设备详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public R detail(@RequestParam("deviceId") Integer deviceId) {
        try {
            NxMachinePrinterDeviceEntity device = nxPrinterDeviceService.queryDeviceDetail(deviceId);
            if (device == null) {
                return R.error("设备不存在");
            }
            return R.ok().put("device", device);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 打印扣减（打印时调用）
     * @param deviceId 设备ID
     * @return 扣减后的纸张数量
     */
    @RequestMapping(value = "/print", method = RequestMethod.POST)
    public R print(@RequestParam("deviceId") Integer deviceId) {
        try {
            Integer paperCount = printerAlertService.printAndCheckAlert(deviceId);
            if (paperCount == null) {
                return R.error("打印失败：纸张不足");
            }
            return R.ok("打印成功")
                .put("paperCount", paperCount);
        } catch (Exception e) {
            return R.error("打印失败：" + e.getMessage());
        }
    }

    /**
     * 正常加纸/更换新纸（旧纸作废）
     * @param deviceId 设备ID
     * @param newPaperCount 新纸数量（直接设置，不累加）
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param remark 备注
     * @return 操作结果，包含新纸数量和旧纸作废数量
     */
    @RequestMapping(value = "/refill", method = RequestMethod.POST)
    public R refill(@RequestParam("deviceId") Integer deviceId,
                   @RequestParam("newPaperCount") Integer newPaperCount,
                   @RequestParam("operatorId") Integer operatorId,
                   @RequestParam("operatorName") String operatorName,
                   @RequestParam(value = "remark", required = false) String remark) {
        try {
            // 调用新的加纸方法
            Integer wasteCount = nxPaperRefillRecordService.refillPaperReplace(
                deviceId, newPaperCount, operatorId, operatorName, remark);
            
            return R.ok("加纸成功")
                .put("paperCount", newPaperCount)
                .put("wasteCount", wasteCount)
                .put("message", "已更换新纸" + newPaperCount + "张，旧纸作废" + wasteCount + "张");
        } catch (Exception e) {
            return R.error("加纸失败：" + e.getMessage());
        }
    }

    /**
     * 手动校准纸张数量（只能减少，校准作废）
     * @param deviceId 设备ID
     * @param paperCount 校准后的纸张数量（必须 <= 当前数量）
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param remark 备注
     * @return 操作结果，包含校准后数量和作废数量
     */
    @RequestMapping(value = "/calibrate", method = RequestMethod.POST)
    public R calibrate(@RequestParam("deviceId") Integer deviceId,
                      @RequestParam("paperCount") Integer paperCount,
                      @RequestParam("operatorId") Integer operatorId,
                      @RequestParam("operatorName") String operatorName,
                      @RequestParam(value = "remark", required = false) String remark) {
        try {
            // 调用新的校准方法
            Integer wasteCount = nxPaperRefillRecordService.calibratePaper(
                deviceId, paperCount, operatorId, operatorName, remark);
            
            return R.ok("校准成功")
                .put("paperCount", paperCount)
                .put("wasteCount", wasteCount)
                .put("message", "校准完成，当前余量" + paperCount + "张，校准作废" + wasteCount + "张");
        } catch (IllegalArgumentException e) {
            // 捕获校验异常，返回友好提示
            return R.error(e.getMessage());
        } catch (Exception e) {
            return R.error("校准失败：" + e.getMessage());
        }
    }
}


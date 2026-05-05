package com.nongxinle.controller;

import com.nongxinle.entity.*;
import com.nongxinle.service.*;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印机系统 - 后台管理Controller
 * 用于后台管理员添加/编辑/删除设备
 *
 * @author lpy
 * @date 2025-10-15
 */
@RestController
@RequestMapping("api/machine/admin")
public class MachineAdminController {

    @Autowired
    private NxMachinePrinterDeviceService nxMachinePrinterDeviceService;

    @Autowired
    private NxMachineAlertThresholdService nxMachineAlertThresholdService;

    @Autowired
    private NxMachinePaperRefillRecordService nxMachinePaperRefillRecordService;

    @Autowired
    private NxMachineMarketManagerService nxMachineMarketManagerService;
    
    @Autowired
    private NxMachineDeviceManagerService nxMachineDeviceManagerService;
    
    @Autowired
    private NxMachineAlertRecordService nxMachineAlertRecordService;
    
    @Autowired
    private NxMachinePrintRecordService nxMachinePrintRecordService;
    
    @Autowired
    private PrinterAlertService printerAlertService;

    /**
     * 添加新设备（自动初始化阈值）
     * @param marketId 市场ID
     * @param deviceName 设备名称
     * @param model 设备型号
     * @param paperType 纸张类型（1-整张 2-半张 3-三分之一张）
     * @param printPrice 打印价格（元/张，默认0.00表示免费）
     * @param location 设备位置
     * @param paperCount 初始纸张数量
     * @param paperMax 最大容量
     * @return 操作结果
     */
    @RequestMapping(value = "/addDevice", method = RequestMethod.POST)
    public R addDevice(@RequestParam("marketId") Integer marketId,
                      @RequestParam("deviceName") String deviceName,
                      @RequestParam(value = "model", defaultValue = "Epson LQ-730K") String model,
                      @RequestParam(value = "paperType", defaultValue = "1") Integer paperType,
                      @RequestParam(value = "printPrice", defaultValue = "0.00") Double printPrice,
                      @RequestParam(value = "location", required = false) String location,
                      @RequestParam(value = "paperCount", defaultValue = "1000") Integer paperCount,
                      @RequestParam(value = "paperMax", defaultValue = "1000") Integer paperMax) {
        try {
            // 1. 创建设备实体
            NxMachinePrinterDeviceEntity device = new NxMachinePrinterDeviceEntity();
            device.setNxPdMarketId(marketId);
            device.setNxPdDeviceName(deviceName);
            device.setNxPdModel(model);
            device.setNxPdPaperType(paperType);
            device.setNxPdPrintPrice(new java.math.BigDecimal(printPrice));
            device.setNxPdLocation(location);
            device.setNxPdPaperCount(paperCount);
            device.setNxPdPaperMax(paperMax);
            device.setNxPdStatus(1); // 正常状态
            device.setNxPdInstallDate(new Date());
            
            // 2. 保存设备（会自动生成设备编号）
            nxMachinePrinterDeviceService.save(device);
            
            // 3. 自动初始化阈值配置
            nxMachineAlertThresholdService.initDefaultThresholds(device.getNxPdId());
            
            // 4. 添加初始化加纸记录（设备安装时的初始纸张）
            if (paperCount > 0) {
                NxMachinePaperRefillRecordEntity initRecord = new NxMachinePaperRefillRecordEntity();
                initRecord.setNxPrrDeviceId(device.getNxPdId());
                initRecord.setNxPrrBeforeCount(0);              // 新设备初始纸张为0
                initRecord.setNxPrrAddCount(paperCount);        // 初始装入的纸张数量
                initRecord.setNxPrrAfterCount(paperCount);      // 装纸后数量
                initRecord.setNxPrrWasteCount(0);               // 新设备无作废纸张
                initRecord.setNxPrrOperatorId(0);               // 0表示系统操作
                initRecord.setNxPrrOperatorName("系统初始化");
                initRecord.setNxPrrRefillType(1);               // 类型1：正常加纸
                initRecord.setNxPrrRemark("设备安装时初始装入纸张");
                
                nxMachinePaperRefillRecordService.save(initRecord);
                
                System.out.println("✅ 已添加设备初始化加纸记录：设备ID=" + device.getNxPdId() + 
                                 ", 初始纸张=" + paperCount + "张（0→" + paperCount + "）");
            }
            
            // 5. 返回设备信息
            return R.ok("设备添加成功")
                .put("deviceId", device.getNxPdId())
                .put("deviceNo", device.getNxPdDeviceNo())
                .put("deviceName", device.getNxPdDeviceName());
        } catch (Exception e) {
            return R.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 编辑设备信息
     * @param deviceId 设备ID
     * @param deviceName 设备名称（可选）
     * @param model 设备型号（可选）
     * @param paperType 纸张类型（可选）
     * @param printPrice 打印价格（元/张，可选）
     * @param location 设备位置（可选）
     * @return 操作结果
     */
    @RequestMapping(value = "/updateDevice", method = RequestMethod.POST)
    public R updateDevice(@RequestParam("deviceId") Integer deviceId,
                         @RequestParam(value = "deviceName", required = false) String deviceName,
                         @RequestParam(value = "model", required = false) String model,
                         @RequestParam(value = "paperType", required = false) Integer paperType,
                         @RequestParam(value = "printPrice", required = false) Double printPrice,
                         @RequestParam(value = "location", required = false) String location) {
        try {
            NxMachinePrinterDeviceEntity device = nxMachinePrinterDeviceService.queryObject(deviceId);
            if (device == null) {
                return R.error("设备不存在");
            }
            
            // 更新字段（只更新传递的参数）
            if (deviceName != null && !deviceName.trim().isEmpty()) {
                device.setNxPdDeviceName(deviceName);
            }
            if (model != null && !model.trim().isEmpty()) {
                device.setNxPdModel(model);
            }
            if (paperType != null) {
                device.setNxPdPaperType(paperType);
            }
            if (printPrice != null) {
                device.setNxPdPrintPrice(new java.math.BigDecimal(printPrice));
            }
            if (location != null) {
                device.setNxPdLocation(location);
            }
            
            nxMachinePrinterDeviceService.update(device);
            return R.ok("更新成功");
        } catch (Exception e) {
            return R.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除设备（级联删除所有关联数据）
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @RequestMapping(value = "/deleteDevice", method = RequestMethod.POST)
    public R deleteDevice(@RequestParam("deviceId") Integer deviceId) {
        try {
            // 检查设备是否存在
            NxMachinePrinterDeviceEntity device = nxMachinePrinterDeviceService.queryObject(deviceId);
            if (device == null) {
                return R.error("设备不存在");
            }
            
            // 检查是否有打印记录
            Map<String, Object> printRecordParams = new HashMap<>();
            printRecordParams.put("deviceId", deviceId);
            int printRecordCount = nxMachinePrintRecordService.queryTotal(printRecordParams);
            
            if (printRecordCount > 0) {
                // 有打印记录，不允许删除，建议禁用
                return R.error("该设备已有打印记录，无法删除。建议使用「禁用」功能代替删除。");
            }
            
            // 1. 删除阈值配置
            Map<String, Object> thresholdParams = new HashMap<>();
            thresholdParams.put("deviceId", deviceId);
            List<NxMachineAlertThresholdEntity> thresholds = nxMachineAlertThresholdService.queryList(thresholdParams);
            if (thresholds != null && !thresholds.isEmpty()) {
                for (NxMachineAlertThresholdEntity threshold : thresholds) {
                    nxMachineAlertThresholdService.delete(threshold.getNxAtId());
                }
            }
            
            // 2. 删除责任人绑定
            Map<String, Object> managerParams = new HashMap<>();
            managerParams.put("deviceId", deviceId);
            List<NxMachineDeviceManagerEntity> managers = nxMachineDeviceManagerService.queryList(managerParams);
            if (managers != null && !managers.isEmpty()) {
                for (NxMachineDeviceManagerEntity manager : managers) {
                    nxMachineDeviceManagerService.delete(manager.getNxDmId());
                }
            }
            
            // 3. 删除提醒记录
            Map<String, Object> alertParams = new HashMap<>();
            alertParams.put("deviceId", deviceId);
            List<NxMachineAlertRecordEntity> alerts = nxMachineAlertRecordService.queryList(alertParams);
            if (alerts != null && !alerts.isEmpty()) {
                for (NxMachineAlertRecordEntity alert : alerts) {
                    nxMachineAlertRecordService.delete(alert.getNxArId());
                }
            }
            
            // 4. 删除加纸记录
            Map<String, Object> refillParams = new HashMap<>();
            refillParams.put("deviceId", deviceId);
            List<NxMachinePaperRefillRecordEntity> refills = nxMachinePaperRefillRecordService.queryList(refillParams);
            if (refills != null && !refills.isEmpty()) {
                for (NxMachinePaperRefillRecordEntity refill : refills) {
                    nxMachinePaperRefillRecordService.delete(refill.getNxPrrId());
                }
            }
            
            // 5. 最后删除设备
            nxMachinePrinterDeviceService.delete(deviceId);
            
            return R.ok("删除成功，已清理所有关联数据");
        } catch (Exception e) {
            return R.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 初始化设备的阈值配置
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @RequestMapping(value = "/initThresholds", method = RequestMethod.POST)
    public R initThresholds(@RequestParam("deviceId") Integer deviceId) {
        try {
            nxMachineAlertThresholdService.initDefaultThresholds(deviceId);
            return R.ok("初始化成功");
        } catch (Exception e) {
            return R.error("初始化失败：" + e.getMessage());
        }
    }

    /**
     * 获取市场的所有管理员列表（用于选择责任人）
     * @param marketId 市场ID
     * @return 管理员列表
     */
    @RequestMapping(value = "/listManagersByMarket", method = RequestMethod.GET)
    public R listManagersByMarket(@RequestParam("marketId") Integer marketId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("marketId", marketId);
            
            List<NxMachineMarketManagerEntity> list = nxMachineMarketManagerService.queryList(params);
            
            return R.ok().put("list", list).put("total", list.size());
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 测试发送微信订阅消息
     * GET /api/machine/admin/testWxMessage
     */
    @RequestMapping("/testWxMessage")
    public R testWxMessage(@RequestParam Long recordId) {
        try {
            System.out.println("测试发送微信订阅消息，记录ID: {}" +  recordId);
            
            // 调用PrinterAlertService的发送方法
            boolean success = printerAlertService.sendWxTemplateMessage(recordId);
            
            if (success) {
                return R.ok("微信消息发送成功");
            } else {
                return R.error("微信消息发送失败");
            }
        } catch (Exception e) {
            System.out.println("测试发送微信消息失败" +  e);
            return R.error("发送失败：" + e.getMessage());
        }
    }
}


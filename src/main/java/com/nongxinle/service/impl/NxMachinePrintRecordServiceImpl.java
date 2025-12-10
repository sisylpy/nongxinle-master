package com.nongxinle.service.impl;

import com.nongxinle.dao.NxMachinePrintRecordDao;
import com.nongxinle.entity.NxMachinePrintRecordEntity;
import com.nongxinle.entity.NxMachinePrinterDeviceEntity;
import com.nongxinle.service.NxMachinePrintRecordService;
import com.nongxinle.service.NxMachinePrinterDeviceService;
import com.nongxinle.service.PrinterAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自助打印记录Service实现类
 * 
 * @author lpy
 * @date 2025-10-15
 */
@Service("nxMachinePrintRecordService")
public class NxMachinePrintRecordServiceImpl implements NxMachinePrintRecordService {
    
    private static final Logger logger = LoggerFactory.getLogger(NxMachinePrintRecordServiceImpl.class);

    @Autowired
    private NxMachinePrintRecordDao nxPrintRecordDao;
    
    @Autowired
    private NxMachinePrinterDeviceService nxPrinterDeviceService;
    
    @Autowired
    private PrinterAlertService printerAlertService;

    @Override
    public NxMachinePrintRecordEntity queryObject(Integer nxPrId) {
        return nxPrintRecordDao.queryObject(nxPrId);
    }

    @Override
    public List<NxMachinePrintRecordEntity> queryList(Map<String, Object> map) {
        return nxPrintRecordDao.queryList(map);
    }

    @Override
    public List<Map<String, Object>> queryListWithDistributer(Map<String, Object> map) {
        return nxPrintRecordDao.queryListWithDistributer(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxPrintRecordDao.queryTotal(map);
    }

    @Override
    public void save(NxMachinePrintRecordEntity printRecord) {
        nxPrintRecordDao.save(printRecord);
    }

    @Override
    public void update(NxMachinePrintRecordEntity printRecord) {
        nxPrintRecordDao.update(printRecord);
    }

    @Override
    public void delete(Integer nxPrId) {
        nxPrintRecordDao.delete(nxPrId);
    }

    @Override
    public void deleteBatch(Integer[] nxPrIds) {
        nxPrintRecordDao.deleteBatch(nxPrIds);
    }

    @Override
    public List<NxMachinePrintRecordEntity> queryByBillId(Integer billId) {
        return nxPrintRecordDao.queryByBillId(billId);
    }

    @Override
    public List<Map<String, Object>> queryDailyStats(Map<String, Object> params) {
        return nxPrintRecordDao.queryDailyStats(params);
    }

    @Override
    public List<Map<String, Object>> queryDeviceStats(Map<String, Object> params) {
        return nxPrintRecordDao.queryDeviceStats(params);
    }

    @Override
    public List<Map<String, Object>> queryDistributerStats(Map<String, Object> params) {
        return nxPrintRecordDao.queryDistributerStats(params);
    }

    @Override
    @Transactional
    public Map<String, Object> recordPrint(Integer billId, Integer deviceId, Integer marketId, Integer distributerId,
                                          Integer paperCount, Double billTotal, String billTradeNo, 
                                          Integer operatorId, String operatorName) {
        logger.info("记录打印：billId={}, deviceId={}, marketId={}, distributerId={}, paperCount={}", 
                   billId, deviceId, marketId, distributerId, paperCount);
        
        try {
            // 1. 获取设备信息（获取纸张类型和打印单价）
            NxMachinePrinterDeviceEntity device = nxPrinterDeviceService.queryObject(deviceId);
            if (device == null) {
                logger.error("打印机设备不存在，deviceId={}", deviceId);
                throw new RuntimeException("打印机设备不存在");
            }
            
            // 2. 计算打印费用：纸张数量 × 打印单价
            BigDecimal printPrice = device.getNxPdPrintPrice(); // 设备单价
            if (printPrice == null) {
                printPrice = BigDecimal.ZERO; // 如果没有设置单价，默认为0
            }
            BigDecimal printFee = printPrice.multiply(BigDecimal.valueOf(paperCount)); // 总打印费用
            
            logger.info("打印费用计算：单价={}, 张数={}, 总费用={}", printPrice, paperCount,printFee);
            
            // 3. 创建打印记录
            NxMachinePrintRecordEntity printRecord = new NxMachinePrintRecordEntity();
            printRecord.setNxPrBillId(billId);
            printRecord.setNxPrDeviceId(deviceId);
            printRecord.setNxPrMarketId(marketId);
            printRecord.setNxPrDistributerId(distributerId);
            printRecord.setNxPrPaperType(device.getNxPdPaperType()); // 从设备获取纸张类型
            printRecord.setNxPrPaperCount(paperCount); // 实际打印张数
            printRecord.setNxPrBillTotal(printFee);
            printRecord.setNxPrPrintTime(new Date());
            printRecord.setNxPrPrintStatus(1); // 打印成功
            
            // 冗余字段（便于统计）
            if (billTotal != null) {
                printRecord.setNxPrBillTotal(BigDecimal.valueOf(billTotal)); // 单据金额（订单金额）
            }
            printRecord.setNxPrBillTradeNo(billTradeNo);
            printRecord.setNxPrBillDate(new Date()); // 设置为当前日期
            printRecord.setNxPrOperatorId(operatorId);
            printRecord.setNxPrOperatorName(operatorName);
            
            // 保存打印记录
            nxPrintRecordDao.save(printRecord);
            logger.info("打印记录已保存，记录ID={}", printRecord.getNxPrId());
            
            // 4. 扣减纸张数量并检查提醒（循环扣减实际打印张数）
            Integer remainingPaper = null;
            for (int i = 0; i < paperCount; i++) {
                remainingPaper = printerAlertService.printAndCheckAlert(deviceId);
                if (remainingPaper == null) {
                    logger.warn("第{}张纸扣减失败，纸张不足，deviceId={}", i + 1, deviceId);
                    throw new RuntimeException("纸张不足，无法完成打印");
                }
            }
            logger.info("纸张扣减成功，共扣减{}张，当前余量={}", paperCount, remainingPaper);
            
            // 5. 返回打印结果（包含打印费用等信息）
            Map<String, Object> result = new HashMap<>();
            result.put("recordId", printRecord.getNxPrId());
            result.put("printFee", printFee.doubleValue());      // 打印费用（元）
            result.put("paperCount", paperCount);                 // 打印张数
            result.put("pricePerSheet", printPrice.doubleValue()); // 单价（元/张）
            result.put("remainingPaper", remainingPaper);         // 剩余纸张数
            
            return result;
            
        } catch (Exception e) {
            logger.error("记录打印失败", e);
            throw new RuntimeException("记录打印失败：" + e.getMessage());
        }
    }
}


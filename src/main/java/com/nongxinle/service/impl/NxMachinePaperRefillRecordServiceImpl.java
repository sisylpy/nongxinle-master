package com.nongxinle.service.impl;

import com.nongxinle.dao.NxMachinePaperRefillRecordDao;
import com.nongxinle.dao.NxMachinePrinterDeviceDao;
import com.nongxinle.dao.NxMachineAlertRecordDao;
import com.nongxinle.entity.NxMachinePaperRefillRecordEntity;
import com.nongxinle.entity.NxMachinePrinterDeviceEntity;
import com.nongxinle.service.NxMachinePaperRefillRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 加纸记录Service实现类
 *
 * @author lpy
 * @date 2025-10-14
 */
@Service("nxMachinePaperRefillRecordService")
public class NxMachinePaperRefillRecordServiceImpl implements NxMachinePaperRefillRecordService {

    @Autowired
    private NxMachinePaperRefillRecordDao nxPaperRefillRecordDao;

    @Autowired
    private NxMachinePrinterDeviceDao nxPrinterDeviceDao;

    @Autowired
    private NxMachineAlertRecordDao nxAlertRecordDao;

    @Override
    public NxMachinePaperRefillRecordEntity queryObject(Long nxPrrId) {
        return nxPaperRefillRecordDao.queryObject(nxPrrId);
    }

    @Override
    public List<NxMachinePaperRefillRecordEntity> queryList(Map<String, Object> map) {
        return nxPaperRefillRecordDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxPaperRefillRecordDao.queryTotal(map);
    }

    @Override
    public void save(NxMachinePaperRefillRecordEntity nxPaperRefillRecord) {
        nxPaperRefillRecordDao.save(nxPaperRefillRecord);
    }

    @Override
    public void update(NxMachinePaperRefillRecordEntity nxPaperRefillRecord) {
        nxPaperRefillRecordDao.update(nxPaperRefillRecord);
    }

    @Override
    public void delete(Long nxPrrId) {
        nxPaperRefillRecordDao.delete(nxPrrId);
    }

    @Override
    public void deleteBatch(Long[] nxPrrIds) {
        nxPaperRefillRecordDao.deleteBatch(nxPrrIds);
    }

    @Override
    public List<NxMachinePaperRefillRecordEntity> queryByDeviceId(Integer deviceId, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("deviceId", deviceId);
        return nxPaperRefillRecordDao.queryByDeviceId(params);
    }

    @Override
    public List<NxMachinePaperRefillRecordEntity> queryByOperatorId(Integer operatorId) {
        Map<String, Object> map = new HashMap<>();
        map.put("operatorId", operatorId);
        return nxPaperRefillRecordDao.queryByOperatorId(map);
    }

    @Override
    public NxMachinePaperRefillRecordEntity queryLatestByDeviceId(Integer deviceId) {
        return nxPaperRefillRecordDao.queryLatestByDeviceId(deviceId);
    }

    @Override
    @Transactional
    @Deprecated
    public void refillPaper(Integer deviceId, Integer addCount, Integer operatorId, String operatorName, 
                           Integer refillType, String remark) {
        // 1. 获取设备当前纸张数量
        NxMachinePrinterDeviceEntity device = nxPrinterDeviceDao.queryObject(deviceId);
        Integer beforeCount = device.getNxPdPaperCount();
        Integer afterCount;
        
        // 2. 根据加纸类型更新设备纸张数量
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("deviceId", deviceId);
        
        if (refillType == 2) {
            // 初始化：直接设置为指定值（通常是满载）⭐
            updateMap.put("paperCount", addCount);
            nxPrinterDeviceDao.setPaperCount(updateMap);
            afterCount = addCount;
        } else {
            // 正常加纸 or 手动校准：增加纸张数量 ⭐
            updateMap.put("count", addCount);
            nxPrinterDeviceDao.addPaperCount(updateMap);
            
            // 3. 获取更新后的数量
            device = nxPrinterDeviceDao.queryObject(deviceId);
            afterCount = device.getNxPdPaperCount();
        }
        
        // 4. 保存加纸记录
        NxMachinePaperRefillRecordEntity record = new NxMachinePaperRefillRecordEntity();
        record.setNxPrrDeviceId(deviceId);
        record.setNxPrrBeforeCount(beforeCount);
        record.setNxPrrAddCount(addCount);
        record.setNxPrrAfterCount(afterCount);
        record.setNxPrrOperatorId(operatorId);
        record.setNxPrrOperatorName(operatorName);
        record.setNxPrrRefillType(refillType);
        record.setNxPrrPaperType(device.getNxPdPaperType()); // 记录纸张类型（1=整张,2=半张,3=三分之一张）
        record.setNxPrrRemark(remark);
        nxPaperRefillRecordDao.save(record);
        
        // 5. 清除该设备的所有未清除提醒记录（防重复的关键）
        nxAlertRecordDao.clearRecordsByDeviceId(deviceId);
    }

    @Override
    @Transactional
    public Integer refillPaperReplace(Integer deviceId, Integer newPaperCount, Integer operatorId, 
                                      String operatorName, String remark) {
        // 1. 获取设备当前纸张数量（即将作废的旧纸）
        NxMachinePrinterDeviceEntity device = nxPrinterDeviceDao.queryObject(deviceId);
        Integer wasteCount = device.getNxPdPaperCount();
        
        // 2. 直接设置新纸数量
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("deviceId", deviceId);
        updateMap.put("paperCount", newPaperCount);
        nxPrinterDeviceDao.setPaperCount(updateMap);
        
        // 3. 保存加纸记录
        NxMachinePaperRefillRecordEntity record = new NxMachinePaperRefillRecordEntity();
        record.setNxPrrDeviceId(deviceId);
        record.setNxPrrBeforeCount(wasteCount);              // 旧纸数量
        record.setNxPrrAddCount(newPaperCount);              // 新纸数量
        record.setNxPrrAfterCount(newPaperCount);            // 加纸后数量
        record.setNxPrrWasteCount(wasteCount);               // 作废数量
        record.setNxPrrOperatorId(operatorId);
        record.setNxPrrOperatorName(operatorName);
        record.setNxPrrRefillType(1);                        // 类型1：正常加纸/更换
        record.setNxPrrPaperType(device.getNxPdPaperType()); // 记录纸张类型（1=整张,2=半张,3=三分之一张）
        
        // 构建备注信息
        String fullRemark = (remark != null ? remark : "正常加纸") + 
                           "（旧纸作废" + wasteCount + "张）";
        record.setNxPrrRemark(fullRemark);
        
        nxPaperRefillRecordDao.save(record);
        
        // 4. 清除该设备的所有未清除提醒记录
        nxAlertRecordDao.clearRecordsByDeviceId(deviceId);
        
        return wasteCount;
    }

    @Override
    @Transactional
    public Integer calibratePaper(Integer deviceId, Integer paperCount, Integer operatorId, 
                                 String operatorName, String remark) throws IllegalArgumentException {
        // 1. 获取设备当前数量
        NxMachinePrinterDeviceEntity device = nxPrinterDeviceDao.queryObject(deviceId);
        Integer currentCount = device.getNxPdPaperCount();
        
        // 2. 校验：只能减少，不能增加
        if (paperCount > currentCount) {
            throw new IllegalArgumentException(
                "校准值（" + paperCount + "张）不能大于当前余量（" + currentCount + "张）");
        }
        
        // 3. 计算作废数量
        Integer wasteCount = currentCount - paperCount;
        
        // 4. 设置校准后的数量
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("deviceId", deviceId);
        updateMap.put("paperCount", paperCount);
        nxPrinterDeviceDao.setPaperCount(updateMap);
        
        // 5. 保存校准记录
        NxMachinePaperRefillRecordEntity record = new NxMachinePaperRefillRecordEntity();
        record.setNxPrrDeviceId(deviceId);
        record.setNxPrrBeforeCount(currentCount);            // 校准前数量
        record.setNxPrrAddCount(-wasteCount);                // 负数表示减少
        record.setNxPrrAfterCount(paperCount);               // 校准后数量
        record.setNxPrrWasteCount(wasteCount);               // 校准作废数量
        record.setNxPrrOperatorId(operatorId);
        record.setNxPrrOperatorName(operatorName);
        record.setNxPrrRefillType(3);                        // 类型3：手动校准
        record.setNxPrrPaperType(device.getNxPdPaperType()); // 记录纸张类型（1=整张,2=半张,3=三分之一张）
        
        // 构建备注信息
        String fullRemark = (remark != null ? remark : "手动校准") + 
                           "（校准作废" + wasteCount + "张）";
        record.setNxPrrRemark(fullRemark);
        
        nxPaperRefillRecordDao.save(record);
        
        // 6. 清除该设备的所有未清除提醒记录（如果有）
        nxAlertRecordDao.clearRecordsByDeviceId(deviceId);
        
        return wasteCount;
    }

    @Override
    public List<Map<String, Object>> queryByMarketIdWithDetails(Map<String, Object> params) {
        return nxPaperRefillRecordDao.queryByMarketIdWithDetails(params);
    }

    @Override
    public List<Map<String, Object>> queryDeviceStatsByMarketId(Map<String, Object> params) {
        return nxPaperRefillRecordDao.queryDeviceStatsByMarketId(params);
    }

    @Override
    public int countByMarketId(Map<String, Object> params) {
        return nxPaperRefillRecordDao.countByMarketId(params);
    }

    @Override
    public int countDevicesByMarketId(Map<String, Object> params) {
        return nxPaperRefillRecordDao.countDevicesByMarketId(params);
    }

    @Override
    public List<Map<String, Object>> queryByDeviceIdWithDetails(Map<String, Object> params) {
        return nxPaperRefillRecordDao.queryByDeviceIdWithDetails(params);
    }

    @Override
    public int countByDeviceIdWithDetails(Map<String, Object> params) {
        return nxPaperRefillRecordDao.countByDeviceIdWithDetails(params);
    }
}


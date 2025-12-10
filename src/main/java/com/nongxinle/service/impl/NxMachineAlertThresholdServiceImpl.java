package com.nongxinle.service.impl;

import com.nongxinle.dao.NxMachineAlertThresholdDao;
import com.nongxinle.entity.NxMachineAlertThresholdEntity;
import com.nongxinle.service.NxMachineAlertThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提醒阈值配置Service实现类
 *
 * @author lpy
 * @date 2025-10-14
 */
@Service("nxMachineAlertThresholdService")
public class NxMachineAlertThresholdServiceImpl implements NxMachineAlertThresholdService {

    @Autowired
    private NxMachineAlertThresholdDao nxAlertThresholdDao;

    @Override
    public NxMachineAlertThresholdEntity queryObject(Integer nxAtId) {
        return nxAlertThresholdDao.queryObject(nxAtId);
    }

    @Override
    public List<NxMachineAlertThresholdEntity> queryList(Map<String, Object> map) {
        return nxAlertThresholdDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxAlertThresholdDao.queryTotal(map);
    }

    @Override
    public void save(NxMachineAlertThresholdEntity nxAlertThreshold) {
        nxAlertThresholdDao.save(nxAlertThreshold);
    }

    @Override
    public void update(NxMachineAlertThresholdEntity nxAlertThreshold) {
        nxAlertThresholdDao.update(nxAlertThreshold);
    }

    @Override
    public void delete(Integer nxAtId) {
        nxAlertThresholdDao.delete(nxAtId);
    }

    @Override
    public void deleteBatch(Integer[] nxAtIds) {
        nxAlertThresholdDao.deleteBatch(nxAtIds);
    }

    @Override
    public List<NxMachineAlertThresholdEntity> queryByDeviceId(Integer deviceId) {
        return nxAlertThresholdDao.queryByDeviceId(deviceId);
    }

    @Override
    public List<NxMachineAlertThresholdEntity> queryTriggeredThresholds(Integer deviceId, Integer paperCount) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", deviceId);
        map.put("paperCount", paperCount);
        return nxAlertThresholdDao.queryTriggeredThresholds(map);
    }

    @Override
    public void initDefaultThresholds(Integer deviceId) {
        // 先检查该设备是否已有阈值配置
        List<NxMachineAlertThresholdEntity> existingThresholds = queryByDeviceId(deviceId);
        if (!existingThresholds.isEmpty()) {
            // 如果已存在配置，跳过初始化
            return;
        }
        
        List<NxMachineAlertThresholdEntity> thresholds = new ArrayList<>();
        
        // 级别1（低）：剩余 < 500张
        NxMachineAlertThresholdEntity threshold1 = new NxMachineAlertThresholdEntity();
        threshold1.setNxAtDeviceId(deviceId);
        threshold1.setNxAtLevel(1);
        threshold1.setNxAtThreshold(500);
        threshold1.setNxAtMessage("纸张剩余不足500张");
        threshold1.setNxAtEnable(1);
        thresholds.add(threshold1);
        
        // 级别2（中）：剩余 < 200张
        NxMachineAlertThresholdEntity threshold2 = new NxMachineAlertThresholdEntity();
        threshold2.setNxAtDeviceId(deviceId);
        threshold2.setNxAtLevel(2);
        threshold2.setNxAtThreshold(200);
        threshold2.setNxAtMessage("纸张即将用尽，请及时补充");
        threshold2.setNxAtEnable(1);
        thresholds.add(threshold2);
        
        // 级别3（高）：剩余 < 50张
        NxMachineAlertThresholdEntity threshold3 = new NxMachineAlertThresholdEntity();
        threshold3.setNxAtDeviceId(deviceId);
        threshold3.setNxAtLevel(3);
        threshold3.setNxAtThreshold(50);
        threshold3.setNxAtMessage("纸张严重不足，请立即加纸！");
        threshold3.setNxAtEnable(1);
        thresholds.add(threshold3);
        
        // 级别4（紧急）：剩余 < 10张
        NxMachineAlertThresholdEntity threshold4 = new NxMachineAlertThresholdEntity();
        threshold4.setNxAtDeviceId(deviceId);
        threshold4.setNxAtLevel(4);
        threshold4.setNxAtThreshold(10);
        threshold4.setNxAtMessage("紧急！纸张即将耗尽！");
        threshold4.setNxAtEnable(1);
        thresholds.add(threshold4);
        
        // 批量保存
        nxAlertThresholdDao.saveBatchThresholds(thresholds);
    }
}


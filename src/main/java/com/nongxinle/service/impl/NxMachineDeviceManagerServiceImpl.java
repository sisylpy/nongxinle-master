package com.nongxinle.service.impl;

import com.nongxinle.dao.NxMachineDeviceManagerDao;
import com.nongxinle.entity.NxMachineDeviceManagerEntity;
import com.nongxinle.service.NxMachineDeviceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备责任人绑定Service实现类
 *
 * @author lpy
 * @date 2025-10-14
 */
@Service("nxMachineDeviceManagerService")
public class NxMachineDeviceManagerServiceImpl implements NxMachineDeviceManagerService {

    @Autowired
    private NxMachineDeviceManagerDao nxDeviceManagerDao;

    @Override
    public NxMachineDeviceManagerEntity queryObject(Integer nxDmId) {
        return nxDeviceManagerDao.queryObject(nxDmId);
    }

    @Override
    public List<NxMachineDeviceManagerEntity> queryList(Map<String, Object> map) {
        return nxDeviceManagerDao.queryList(map);
    }

    @Override
    public List<Map<String, Object>> queryListWithManager(Map<String, Object> map) {
        return nxDeviceManagerDao.queryListWithManager(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxDeviceManagerDao.queryTotal(map);
    }

    @Override
    public void save(NxMachineDeviceManagerEntity nxDeviceManager) {
        nxDeviceManagerDao.save(nxDeviceManager);
    }

    @Override
    public void update(NxMachineDeviceManagerEntity nxDeviceManager) {
        nxDeviceManagerDao.update(nxDeviceManager);
    }

    @Override
    public void delete(Integer nxDmId) {
        nxDeviceManagerDao.delete(nxDmId);
    }

    @Override
    public void deleteBatch(Integer[] nxDmIds) {
        nxDeviceManagerDao.deleteBatch(nxDmIds);
    }

    @Override
    public List<NxMachineDeviceManagerEntity> queryByDeviceId(Integer deviceId) {
        return nxDeviceManagerDao.queryByDeviceId(deviceId);
    }

    @Override
    public List<NxMachineDeviceManagerEntity> queryByManagerId(Integer managerId) {
        return nxDeviceManagerDao.queryByManagerId(managerId);
    }

    @Override
    public List<NxMachineDeviceManagerEntity> queryNotifyManagers(Integer deviceId, Integer alertLevel) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", deviceId);
        map.put("alertLevel", alertLevel);
        return nxDeviceManagerDao.queryNotifyManagers(map);
    }

    @Override
    public void bindDeviceManager(Integer deviceId, Integer managerId, Integer notifyLevel, Integer isPrimary) {
        // 检查是否已存在绑定关系
        Map<String, Object> checkMap = new HashMap<>();
        checkMap.put("deviceId", deviceId);
        checkMap.put("managerId", managerId);
        NxMachineDeviceManagerEntity existing = nxDeviceManagerDao.queryByDeviceAndManager(checkMap);
        
        if (existing != null) {
            // 已存在，更新
            existing.setNxDmNotifyLevel(notifyLevel);
            existing.setNxDmIsPrimary(isPrimary);
            existing.setNxDmEnable(1);
            nxDeviceManagerDao.update(existing);
        } else {
            // 不存在，新增
            NxMachineDeviceManagerEntity entity = new NxMachineDeviceManagerEntity();
            entity.setNxDmDeviceId(deviceId);
            entity.setNxDmManagerId(managerId);
            entity.setNxDmNotifyLevel(notifyLevel);
            entity.setNxDmIsPrimary(isPrimary);
            entity.setNxDmEnable(1);
            nxDeviceManagerDao.save(entity);
        }
    }

    @Override
    public void unbindDeviceManager(Integer deviceId, Integer managerId) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", deviceId);
        map.put("managerId", managerId);
        NxMachineDeviceManagerEntity existing = nxDeviceManagerDao.queryByDeviceAndManager(map);
        
        if (existing != null) {
            // 设置为禁用
            existing.setNxDmEnable(0);
            nxDeviceManagerDao.update(existing);
        }
    }
}


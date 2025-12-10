package com.nongxinle.service.impl;

import com.nongxinle.dao.NxMachineAlertRecordDao;
import com.nongxinle.entity.NxMachineAlertRecordEntity;
import com.nongxinle.service.NxMachineAlertRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提醒记录Service实现类
 *
 * @author lpy
 * @date 2025-10-14
 */
@Service("nxMachineAlertRecordService")
public class NxMachineAlertRecordServiceImpl implements NxMachineAlertRecordService {

    @Autowired
    private NxMachineAlertRecordDao nxAlertRecordDao;

    @Override
    public NxMachineAlertRecordEntity queryObject(Long nxArId) {
        return nxAlertRecordDao.queryObject(nxArId);
    }

    @Override
    public List<NxMachineAlertRecordEntity> queryList(Map<String, Object> map) {
        return nxAlertRecordDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxAlertRecordDao.queryTotal(map);
    }

    @Override
    public void save(NxMachineAlertRecordEntity nxAlertRecord) {
        nxAlertRecordDao.save(nxAlertRecord);
    }

    @Override
    public void update(NxMachineAlertRecordEntity nxAlertRecord) {
        nxAlertRecordDao.update(nxAlertRecord);
    }

    @Override
    public void delete(Long nxArId) {
        nxAlertRecordDao.delete(nxArId);
    }

    @Override
    public void deleteBatch(Long[] nxArIds) {
        nxAlertRecordDao.deleteBatch(nxArIds);
    }

    @Override
    public List<NxMachineAlertRecordEntity> queryByDeviceId(Integer deviceId, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("deviceId", deviceId);
        return nxAlertRecordDao.queryByDeviceId(params);
    }

    @Override
    public List<NxMachineAlertRecordEntity> queryByManagerId(Integer managerId, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("managerId", managerId);
        return nxAlertRecordDao.queryByManagerId(params);
    }

    @Override
    public NxMachineAlertRecordEntity queryUnclearedRecord(Integer deviceId, Integer alertLevel) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", deviceId);
        map.put("alertLevel", alertLevel);
        return nxAlertRecordDao.queryUnclearedRecord(map);
    }

    @Override
    public void clearRecordsByDeviceId(Integer deviceId) {
        nxAlertRecordDao.clearRecordsByDeviceId(deviceId);
    }

    @Override
    public void markAsRead(Long recordId) {
        nxAlertRecordDao.markAsRead(recordId);
    }

    @Override
    public int queryUnreadCount(Map<String, Object> params) {
        return nxAlertRecordDao.queryUnreadCount(params);
    }

    @Override
    public void updateSendStatus(Long recordId, Integer sendStatus) {
        Map<String, Object> map = new HashMap<>();
        map.put("recordId", recordId);
        map.put("sendStatus", sendStatus);
        nxAlertRecordDao.updateSendStatus(map);
    }
}


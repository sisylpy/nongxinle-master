package com.nongxinle.service.impl;

import com.nongxinle.dao.NxMachinePrinterDeviceDao;
import com.nongxinle.entity.NxMachinePrinterDeviceEntity;
import com.nongxinle.service.NxMachinePrinterDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印设备Service实现类
 *
 * @author lpy
 * @date 2025-10-14
 */
@Service("nxMachinePrinterDeviceService")
public class NxMachinePrinterDeviceServiceImpl implements NxMachinePrinterDeviceService {

    @Autowired
    private NxMachinePrinterDeviceDao nxPrinterDeviceDao;

    @Override
    public NxMachinePrinterDeviceEntity queryObject(Integer nxPdId) {
        return nxPrinterDeviceDao.queryObject(nxPdId);
    }

    @Override
    public List<NxMachinePrinterDeviceEntity> queryList(Map<String, Object> map) {
        return nxPrinterDeviceDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxPrinterDeviceDao.queryTotal(map);
    }

    @Override
    public void save(NxMachinePrinterDeviceEntity nxPrinterDevice) {
        // 如果没有设备编号，自动生成
        if (nxPrinterDevice.getNxPdDeviceNo() == null || nxPrinterDevice.getNxPdDeviceNo().isEmpty()) {
            nxPrinterDevice.setNxPdDeviceNo(generateDeviceNo());
        }
        nxPrinterDeviceDao.save(nxPrinterDevice);
    }

    @Override
    public void update(NxMachinePrinterDeviceEntity nxPrinterDevice) {
        nxPrinterDeviceDao.update(nxPrinterDevice);
    }

    @Override
    public void delete(Integer nxPdId) {
        nxPrinterDeviceDao.delete(nxPdId);
    }

    @Override
    public void deleteBatch(Integer[] nxPdIds) {
        nxPrinterDeviceDao.deleteBatch(nxPdIds);
    }

    @Override
    public NxMachinePrinterDeviceEntity queryByDeviceNo(String deviceNo) {
        return nxPrinterDeviceDao.queryByDeviceNo(deviceNo);
    }

    @Override
    public List<NxMachinePrinterDeviceEntity> queryByMarketId(Integer marketId) {
        Map<String, Object> map = new HashMap<>();
        map.put("marketId", marketId);
        return nxPrinterDeviceDao.queryByMarketId(map);
    }

    @Override
    public Integer reducePaperCount(Integer deviceId, Integer count) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", deviceId);
        map.put("count", count);
        int result = nxPrinterDeviceDao.reducePaperCount(map);
        
        if (result > 0) {
            // 扣减成功，返回当前纸张数量
            NxMachinePrinterDeviceEntity device = nxPrinterDeviceDao.queryObject(deviceId);
            return device.getNxPdPaperCount();
        }
        return null;
    }

    @Override
    public Integer addPaperCount(Integer deviceId, Integer count) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", deviceId);
        map.put("count", count);
        nxPrinterDeviceDao.addPaperCount(map);
        
        // 返回当前纸张数量
        NxMachinePrinterDeviceEntity device = nxPrinterDeviceDao.queryObject(deviceId);
        return device.getNxPdPaperCount();
    }

    @Override
    public void setPaperCount(Integer deviceId, Integer paperCount) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", deviceId);
        map.put("paperCount", paperCount);
        nxPrinterDeviceDao.setPaperCount(map);
    }

    @Override
    public void updateStatus(Integer deviceId, Integer status) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", deviceId);
        map.put("status", status);
        nxPrinterDeviceDao.updateStatus(map);
    }

    @Override
    public NxMachinePrinterDeviceEntity queryDeviceDetail(Integer deviceId) {
        return nxPrinterDeviceDao.queryDeviceDetail(deviceId);
    }

    @Override
    public String generateDeviceNo() {
        // 格式：PD{年月日}{4位序号}
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        
        // TODO: 查询当天已有的设备数量，生成序号
        // 简化处理：使用时间戳后4位
        String timestamp = String.valueOf(System.currentTimeMillis());
        String suffix = timestamp.substring(timestamp.length() - 4);
        
        return "PD" + dateStr + suffix;
    }
}


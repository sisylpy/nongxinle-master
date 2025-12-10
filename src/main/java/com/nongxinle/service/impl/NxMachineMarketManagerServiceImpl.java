package com.nongxinle.service.impl;

import com.nongxinle.dao.NxMachineMarketManagerDao;
import com.nongxinle.entity.NxMachineMarketManagerEntity;
import com.nongxinle.entity.SysCityMarketEntity;
import com.nongxinle.service.NxMachineMarketManagerService;
import com.nongxinle.service.SysCityMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 市场管理员Service实现类
 *
 * @author lpy
 * @date 2025-10-14
 */
@Service("nxMachineMarketManagerService")
public class NxMachineMarketManagerServiceImpl implements NxMachineMarketManagerService {

    @Autowired
    private NxMachineMarketManagerDao nxMarketManagerDao;

    @Autowired
    private SysCityMarketService sysCityMarketService;

    @Override
    public NxMachineMarketManagerEntity queryObject(Integer nxMmId) {
        return nxMarketManagerDao.queryObject(nxMmId);
    }

    @Override
    public List<NxMachineMarketManagerEntity> queryList(Map<String, Object> map) {
        return nxMarketManagerDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return nxMarketManagerDao.queryTotal(map);
    }

    @Override
    public void save(NxMachineMarketManagerEntity nxMarketManager) {
        nxMarketManagerDao.save(nxMarketManager);
    }

    @Override
    public void update(NxMachineMarketManagerEntity nxMarketManager) {
        nxMarketManagerDao.update(nxMarketManager);
    }

    @Override
    public void delete(Integer nxMmId) {
        nxMarketManagerDao.delete(nxMmId);
    }

    @Override
    public void deleteBatch(Integer[] nxMmIds) {
        nxMarketManagerDao.deleteBatch(nxMmIds);
    }

    @Override
    public NxMachineMarketManagerEntity queryByOpenid(String openid) {
        return nxMarketManagerDao.queryByOpenid(openid);
    }

    @Override
    public NxMachineMarketManagerEntity queryByPhone(String phone) {
        return nxMarketManagerDao.queryByPhone(phone);
    }

    @Override
    public Map<String, Object> printSoftwareLogin(String phone) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 根据手机号查询管理员
        NxMachineMarketManagerEntity manager = nxMarketManagerDao.queryByPhone(phone);
        
        if (manager == null) {
            result.put("success", false);
            result.put("message", "手机号未注册，请联系系统管理员");
            return result;
        }
        
        // 2. 检查账号状态
        if (manager.getNxMmStatus() != 1) {
            result.put("success", false);
            result.put("message", "账号已被禁用，请联系系统管理员");
            return result;
        }
        
        // 3. 查询关联的市场信息
        SysCityMarketEntity market = sysCityMarketService.queryObject(manager.getNxMmMarketId());
        
        if (market == null) {
            result.put("success", false);
            result.put("message", "关联的市场不存在");
            return result;
        }
        
        // 4. 更新最后登录时间
        nxMarketManagerDao.updateLastLoginTime(manager.getNxMmId());
        
        // 5. 返回登录成功信息
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("managerId", manager.getNxMmId());
        result.put("managerName", manager.getNxMmName());
        result.put("phone", manager.getNxMmPhone());
        result.put("marketId", market.getSysCityMarketId());
        result.put("marketName", market.getSysCmMarketName());
        
        return result;
    }

    @Override
    public NxMachineMarketManagerEntity wxLogin(String code) {
        // 1. 使用 code 换取 openid
        String openid = com.nongxinle.utils.WxMiniProgramUtil.getOpenidByCode(code);
        
        if (openid == null) {
            throw new RuntimeException("获取openid失败，请检查code是否有效或网络连接");
        }
        
        // 2. 根据 openid 查询管理员
        NxMachineMarketManagerEntity manager = nxMarketManagerDao.queryByOpenid(openid);
        
        // 3. 如果不存在，返回错误（需要后台先添加管理员）
        if (manager == null) {
            throw new RuntimeException("管理员不存在，openid: " + openid + "，请先在后台添加该管理员");
        }
        
        // 4. 更新最后登录时间
        nxMarketManagerDao.updateLastLoginTime(manager.getNxMmId());
        
        // 5. 返回管理员实体
        return manager;
    }

    @Override
    public void updateLastLoginTime(Integer managerId) {
        nxMarketManagerDao.updateLastLoginTime(managerId);
    }

    @Override
    public List<NxMachineMarketManagerEntity> queryByMarketId(Integer marketId) {
        Map<String, Object> map = new HashMap<>();
        map.put("marketId", marketId);
        return nxMarketManagerDao.queryByMarketId(map);
    }
}


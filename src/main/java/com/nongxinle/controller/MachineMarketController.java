package com.nongxinle.controller;

import com.nongxinle.entity.SysCityMarketEntity;
import com.nongxinle.service.SysCityMarketService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印机系统 - 市场管理Controller
 *
 * @author lpy
 * @date 2025-10-15
 */
@RestController
@RequestMapping("api/machine/market")
public class MachineMarketController {

    @Autowired
    private SysCityMarketService sysCityMarketService;

    /**
     * 获取市场列表（用于打印软件选择市场）
     * @return 市场列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public R list() {
        try {
            // 查询所有市场
            Map<String, Object> params = new HashMap<>();
            List<SysCityMarketEntity> list = sysCityMarketService.queryList(params);
            
            return R.ok().put("list", list);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据市场ID查询市场详情
     * @param marketId 市场ID
     * @return 市场详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public R detail(@RequestParam("marketId") Integer marketId) {
        try {
            SysCityMarketEntity market = sysCityMarketService.queryObject(marketId);
            if (market == null) {
                return R.error("市场不存在");
            }
            return R.ok().put("market", market);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }
}


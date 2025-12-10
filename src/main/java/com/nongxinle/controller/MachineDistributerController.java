package com.nongxinle.controller;

import com.nongxinle.entity.NxDistributerEntity;
import com.nongxinle.service.NxDistributerService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 打印机系统 - 配送商管理Controller
 *
 * @author lpy
 * @date 2025-10-15
 */
@RestController
@RequestMapping("api/machine/distributer")
public class MachineDistributerController {

    @Autowired
    private NxDistributerService nxDistributerService;

    /**
     * 根据市场ID获取配送商列表
     * @param marketId 市场ID
     * @return 配送商列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public R list(@RequestParam("marketId") Integer marketId) {
        try {
            if (marketId == null) {
                return R.error("市场ID不能为空");
            }
            
            List<NxDistributerEntity> list = nxDistributerService.queryByMarketId(marketId);
            
            return R.ok()
                .put("list", list)
                .put("total", list.size());
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据配送商ID获取配送商详情
     * @param distributerId 配送商ID
     * @return 配送商详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public R detail(@RequestParam("distributerId") Integer distributerId) {
        try {
            if (distributerId == null) {
                return R.error("配送商ID不能为空");
            }
            
            NxDistributerEntity distributer = nxDistributerService.queryObject(distributerId);
            
            if (distributer == null) {
                return R.error("配送商不存在");
            }
            
            return R.ok().put("distributer", distributer);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }
}


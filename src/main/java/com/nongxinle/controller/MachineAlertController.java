package com.nongxinle.controller;

import com.nongxinle.entity.NxMachineAlertRecordEntity;
import com.nongxinle.service.NxMachineAlertRecordService;
import com.nongxinle.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印机系统 - 提醒记录Controller
 *
 * @author lpy
 * @date 2025-10-14
 */
@RestController
@RequestMapping("api/machine/alert")
public class MachineAlertController {

    @Autowired
    private NxMachineAlertRecordService nxAlertRecordService;

    /**
     * 查询管理员的提醒记录列表
     * @param managerId 管理员ID（必填）
     * @param isCleared 是否已清除（0-未清除 1-已清除）可选
     * @param isRead 是否已读（0-未读 1-已读）可选
     * @param days 查询N天内的记录（可选，如：days=2表示查询2天内的记录）
     * @param offset 分页起始
     * @param limit 分页大小
     * @return 提醒记录列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public R list(@RequestParam("managerId") Integer managerId,
                 @RequestParam(value = "isCleared", required = false) Integer isCleared,
                 @RequestParam(value = "isRead", required = false) Integer isRead,
                 @RequestParam(value = "days", required = false) Integer days,
                 @RequestParam(value = "offset", defaultValue = "0") Integer offset,
                 @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (isCleared != null) {
                params.put("isCleared", isCleared);
            }
            if (isRead != null) {
                params.put("isRead", isRead);
            }
            if (days != null && days > 0) {
                params.put("days", days);
            }
            params.put("offset", offset);
            params.put("limit", limit);
            
            List<NxMachineAlertRecordEntity> list = nxAlertRecordService.queryByManagerId(managerId, params);
            int total = nxAlertRecordService.queryTotal(params);
            
            // 未读数量也按天数统计
            Map<String, Object> countParams = new HashMap<>();
            countParams.put("managerId", managerId);
            if (days != null && days > 0) {
                countParams.put("days", days);
            }
            int count = nxAlertRecordService.queryUnreadCount(countParams);
            
            return R.ok()
                .put("list", list)
                .put("total", total)
                .put("unReadCount", count);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询未读消息数量
     * @param managerId 管理员ID（必填）
     * @param days 查询N天内的未读数量（可选）
     * @return 未读消息数量
     */
    @RequestMapping(value = "/unreadCount", method = RequestMethod.GET)
    public R unreadCount(@RequestParam("managerId") Integer managerId,
                        @RequestParam(value = "days", required = false) Integer days) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("managerId", managerId);
            if (days != null && days > 0) {
                params.put("days", days);
            }
            int count = nxAlertRecordService.queryUnreadCount(params);
            return R.ok().put("count", count);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     * @param recordId 记录ID
     * @return 操作结果
     */
    @RequestMapping(value = "/markRead", method = RequestMethod.POST)
    public R markRead(@RequestParam("recordId") Long recordId) {
        try {
            nxAlertRecordService.markAsRead(recordId);
            return R.ok("标记成功");
        } catch (Exception e) {
            return R.error("标记失败：" + e.getMessage());
        }
    }

    /**
     * 批量标记为已读
     * @param recordIds 记录ID数组
     * @return 操作结果
     */
    @RequestMapping(value = "/batchMarkRead", method = RequestMethod.POST)
    public R batchMarkRead(@RequestParam("recordIds") Long[] recordIds) {
        try {
            for (Long recordId : recordIds) {
                nxAlertRecordService.markAsRead(recordId);
            }
            return R.ok("批量标记成功");
        } catch (Exception e) {
            return R.error("批量标记失败：" + e.getMessage());
        }
    }

    /**
     * 查询提醒记录详情
     * @param recordId 记录ID
     * @return 提醒记录详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public R detail(@RequestParam("recordId") Long recordId) {
        try {
            NxMachineAlertRecordEntity record = nxAlertRecordService.queryObject(recordId);
            if (record == null) {
                return R.error("记录不存在");
            }
            return R.ok().put("record", record);
        } catch (Exception e) {
            return R.error("查询失败：" + e.getMessage());
        }
    }
}


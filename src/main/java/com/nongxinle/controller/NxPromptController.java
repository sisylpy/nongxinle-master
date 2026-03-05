package com.nongxinle.controller;

import com.nongxinle.entity.NxPromptEntity;
import com.nongxinle.service.NxPromptService;
import com.nongxinle.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统 Prompt Controller
 * 提供前端获取 prompt 的接口
 * 
 * @author lpy
 * @date 2025-01-XX
 */
@RestController
@RequestMapping("api/prompt")
public class NxPromptController {
    private static final Logger logger = LoggerFactory.getLogger(NxPromptController.class);

    @Autowired
    private NxPromptService nxPromptService;

    /**
     * 根据 prompt_key 获取单个 prompt
     * 
     * @param promptKey prompt 的唯一键（如：OCR_IMAGE, OCR_EXCEL, OCR_PASTE）
     * @return prompt 信息
     */
    @RequestMapping(value = "/getByKey", method = RequestMethod.GET)
    @ResponseBody
    public R getByKey(@RequestParam String promptKey) {
        try {
            if (promptKey == null || promptKey.trim().isEmpty()) {
                logger.warn("[getByKey] promptKey 参数为空");
                return R.error("promptKey 参数不能为空");
            }

            NxPromptEntity prompt = nxPromptService.queryByKey(promptKey.trim());
            if (prompt == null) {
                logger.warn("[getByKey] 未找到 prompt，promptKey: {}", promptKey);
                return R.error("未找到对应的 prompt 或 prompt 未启用");
            }

            logger.info("[getByKey] 成功获取 prompt，promptKey: {}, promptName: {}", 
                    promptKey, prompt.getNxPromptName());
            return R.ok().put("prompt", prompt);
        } catch (Exception e) {
            logger.error("[getByKey] 获取 prompt 失败，promptKey: {}", promptKey, e);
            return R.error("获取 prompt 失败: " + e.getMessage());
        }
    }

    /**
     * 根据 prompt_key 获取 prompt 内容（只返回内容字符串）
     * 
     * @param promptKey prompt 的唯一键（如：OCR_IMAGE, OCR_EXCEL, OCR_PASTE）
     * @return prompt 内容
     */
    @RequestMapping(value = "/getContentByKey", method = RequestMethod.GET)
    @ResponseBody
    public R getContentByKey(@RequestParam String promptKey) {
        try {
            if (promptKey == null || promptKey.trim().isEmpty()) {
                logger.warn("[getContentByKey] promptKey 参数为空");
                return R.error("promptKey 参数不能为空");
            }

            String content = nxPromptService.getPromptContentByKey(promptKey.trim());
            if (content == null) {
                logger.warn("[getContentByKey] 未找到 prompt 内容，promptKey: {}", promptKey);
                return R.error("未找到对应的 prompt 内容或 prompt 未启用");
            }

            logger.info("[getContentByKey] 成功获取 prompt 内容，promptKey: {}, 内容长度: {}", 
                    promptKey, content.length());
            return R.ok().put("content", content);
        } catch (Exception e) {
            logger.error("[getContentByKey] 获取 prompt 内容失败，promptKey: {}", promptKey, e);
            return R.error("获取 prompt 内容失败: " + e.getMessage());
        }
    }

    /**
     * 根据 category 获取 prompt 列表
     * 
     * @param category prompt 分类（如：OCR）
     * @return prompt 列表
     */
    @RequestMapping(value = "/getListByCategory", method = RequestMethod.GET)
    @ResponseBody
    public R getListByCategory(@RequestParam String category) {
        try {
            if (category == null || category.trim().isEmpty()) {
                logger.warn("[getListByCategory] category 参数为空");
                return R.error("category 参数不能为空");
            }

            List<NxPromptEntity> promptList = nxPromptService.queryListByCategory(category.trim());
            logger.info("[getListByCategory] 成功获取 prompt 列表，category: {}, 数量: {}", 
                    category, promptList.size());
            return R.ok().put("list", promptList);
        } catch (Exception e) {
            logger.error("[getListByCategory] 获取 prompt 列表失败，category: {}", category, e);
            return R.error("获取 prompt 列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据 api_path 获取单个 prompt
     * 
     * @param apiPath API 路径（如：/api/ocr/recognizeOrder）
     * @return prompt 信息
     */
    @RequestMapping(value = "/getByApiPath", method = RequestMethod.GET)
    @ResponseBody
    public R getByApiPath(@RequestParam String apiPath) {
        try {
            if (apiPath == null || apiPath.trim().isEmpty()) {
                logger.warn("[getByApiPath] apiPath 参数为空");
                return R.error("apiPath 参数不能为空");
            }

            NxPromptEntity prompt = nxPromptService.queryByApiPath(apiPath.trim());
            if (prompt == null) {
                logger.warn("[getByApiPath] 未找到 prompt，apiPath: {}", apiPath);
                return R.error("未找到对应的 prompt 或 prompt 未启用");
            }

            logger.info("[getByApiPath] 成功获取 prompt，apiPath: {}, promptName: {}", 
                    apiPath, prompt.getNxPromptName());
            return R.ok().put("prompt", prompt);
        } catch (Exception e) {
            logger.error("[getByApiPath] 获取 prompt 失败，apiPath: {}", apiPath, e);
            return R.error("获取 prompt 失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有 OCR 相关的 prompt 列表
     * 
     * @return OCR prompt 列表
     */
    @RequestMapping(value = "/getOcrPrompts", method = RequestMethod.GET)
    @ResponseBody
    public R getOcrPrompts() {
        try {
            List<NxPromptEntity> promptList = nxPromptService.queryListByCategory("OCR");
            logger.info("[getOcrPrompts] 成功获取 OCR prompt 列表，数量: {}", promptList.size());
            return R.ok().put("list", promptList);
        } catch (Exception e) {
            logger.error("[getOcrPrompts] 获取 OCR prompt 列表失败", e);
            return R.error("获取 OCR prompt 列表失败: " + e.getMessage());
        }
    }

    /**
     * 添加 prompt
     * 如果数据库中存在相同 key 的启用记录，则将其设为禁用（作为历史记录）
     * 新添加的记录状态设为启用（status=1）
     * 
     * @param prompt prompt 实体对象（通过 RequestBody 接收 JSON）
     * @return 操作结果
     */
    @RequestMapping(value = "/addPrompt", method = RequestMethod.POST)
    @ResponseBody
    public R addPrompt(@RequestBody NxPromptEntity prompt) {
        try {
            // 参数校验
            if (prompt == null) {
                logger.warn("[addPrompt] prompt 对象为空");
                return R.error("prompt 对象不能为空");
            }
            if (prompt.getNxPromptKey() == null || prompt.getNxPromptKey().trim().isEmpty()) {
                logger.warn("[addPrompt] promptKey 为空");
                return R.error("promptKey 不能为空");
            }
            if (prompt.getNxPromptContent() == null || prompt.getNxPromptContent().trim().isEmpty()) {
                logger.warn("[addPrompt] promptContent 为空");
                return R.error("promptContent 不能为空");
            }

            // 调用 Service 方法（带事务，会自动处理相同 key 的旧记录）
            nxPromptService.saveWithKeySwitch(prompt);

            logger.info("[addPrompt] 成功添加 prompt，promptKey: {}, promptName: {}", 
                    prompt.getNxPromptKey(), prompt.getNxPromptName());
            return R.ok().put("prompt", prompt);
        } catch (IllegalArgumentException e) {
            logger.warn("[addPrompt] 参数错误: {}", e.getMessage());
            return R.error(e.getMessage());
        } catch (Exception e) {
            logger.error("[addPrompt] 添加 prompt 失败，promptKey: {}", 
                    prompt != null ? prompt.getNxPromptKey() : "null", e);
            return R.error("添加 prompt 失败: " + e.getMessage());
        }
    }

    /**
     * 设置 prompt 的状态（0=禁用，1=启用）
     * 如果设置为启用（status=1），会将相同 key 的其他启用记录设为禁用，保证每个 key 只有一个启用记录
     * 
     * @param promptId prompt ID
     * @param status 状态值（0 或 1）
     * @return 操作结果
     */
    @RequestMapping(value = "/setStatus", method = RequestMethod.POST)
    @ResponseBody
    public R setStatus(@RequestParam Integer promptId, @RequestParam Integer status) {
        try {
            // 参数校验
            if (promptId == null) {
                logger.warn("[setStatus] promptId 参数为空");
                return R.error("promptId 参数不能为空");
            }
            if (status == null) {
                logger.warn("[setStatus] status 参数为空");
                return R.error("status 参数不能为空");
            }
            if (status != 0 && status != 1) {
                logger.warn("[setStatus] status 参数值无效: {}", status);
                return R.error("status 必须为 0（禁用）或 1（启用）");
            }

            // 调用 Service 方法（带事务，会自动处理相同 key 的旧记录）
            nxPromptService.updateStatus(promptId, status);

            logger.info("[setStatus] 成功设置 prompt 状态，promptId: {}, status: {}", promptId, status);
            return R.ok().put("message", "状态设置成功");
        } catch (IllegalArgumentException e) {
            logger.warn("[setStatus] 参数错误: {}", e.getMessage());
            return R.error(e.getMessage());
        } catch (Exception e) {
            logger.error("[setStatus] 设置 prompt 状态失败，promptId: {}, status: {}", promptId, status, e);
            return R.error("设置 prompt 状态失败: " + e.getMessage());
        }
    }
}




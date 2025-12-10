package com.nongxinle.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.dto.AdpParseResponse;
import com.nongxinle.dto.Round1Request;
import com.nongxinle.service.TencentCloudAgentService;
import com.nongxinle.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 临时订单智能助手控制器
 */
@Slf4j
@Controller
@RequestMapping("api/ai/linshi")
public class LinshiOrderController {
    
    @Autowired
    private TencentCloudAgentService tencentCloudAgentService;
    
    @Value("${nx.quick-search-url:https://grainservice.club:8443/nongxinle/api/nxdistributergoods/queryLinshiGoodsAndNxGoodsByQuickSearch}")
    private String quickSearchUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 测试接口：验证智能体返回格式
     * 
     * 用途：测试 ADP 工作流的实际输出，用于调整解析逻辑
     * 
     * 示例请求：
     * POST /ai/linshi/test
     * {
     *   "rawText": "进口羊奶粉 1 罐",
     *   "distributorId": "56"
     * }
     */
    @RequestMapping(value = "/linshiOrderOpen", method = RequestMethod.POST)
    @ResponseBody
    public R testAgent(@RequestParam String rawText, 
                       @RequestParam(required = false) String distributorId) {

        System.out.println("testAgenttestAgent" + rawText);
        try {
            log.info("=== 临时订单智能体测试 ===");
            log.info("输入文本: {}", rawText);
            log.info("配送商ID: {}", distributorId);
            
            // 生成测试会话ID
            String sessionId = "linshi_test_" + System.currentTimeMillis();
            
            // 构建自定义参数
            Map<String, String> customVars = new HashMap<>();
            if (distributorId != null && !distributorId.isEmpty()) {
                customVars.put("distributor_id", distributorId);
            }
            
            // 调用智能体
            String agentReply = tencentCloudAgentService.getAgentReply(rawText, sessionId, customVars);
            
            log.info("智能体原始返回: {}", agentReply);
            
            if (agentReply == null || agentReply.isEmpty()) {
                return R.error("智能体无返回内容");
            }
            
            // 返回原始结果供分析
            return R.ok()
                .put("success", true)
                .put("rawText", rawText)
                .put("distributorId", distributorId)
                .put("sessionId", sessionId)
                .put("customVariables", customVars)
                .put("agentReply", agentReply)
                .put("agentReplyLength", agentReply.length())
                .put("agentReplyType", detectReplyType(agentReply))
                .put("message", "请查看 agentReply 字段了解智能体的实际返回格式");
        } catch (Exception e) {
            log.error("测试智能体异常", e);
            return R.error("测试异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试接口：GET方式（浏览器快速测试）
     * 
     * 示例：
     * /ai/linshi/test-get?rawText=进口羊奶粉%201%20罐&distributorId=56
     */
    @RequestMapping(value = "/test-get", method = RequestMethod.GET)
    @ResponseBody
    public R testAgentGet(@RequestParam String rawText, 
                          @RequestParam(required = false) String distributorId) {
        return testAgent(rawText, distributorId);
    }
    
    /**
     * 检测返回内容类型（辅助分析）
     */
    private String detectReplyType(String reply) {
        if (reply == null) return "null";
        
        String trimmed = reply.trim();
        
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return "JSON对象";
        } else if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return "JSON数组";
        } else if (trimmed.contains("{") && trimmed.contains("}")) {
            return "包含JSON的文本";
        } else {
            return "纯文本";
        }
    }
    
    /**
     * 第一个回合：智能解析订单并查询候选商品
     * 
     * POST /ai/linshi/round1
     * 
     * Request:
     * {
     *   "raw_text": "进口羊奶粉 1 瓶",
     *   "distributor_id": "56",
     *   "order_id": "LS12345"
     * }
     * 
     * Response:
     * {
     *   "code": 0,
     *   "ok": true,
     *   "searchStr": "羊奶粉",
     *   "searchStrCandidates": ["羊奶粉"],
     *   "disArr": [...],
     *   "conversationId": "conv-abc123",
     *   "round": 1
     * }
     */
    @RequestMapping(value = "/round1", method = RequestMethod.POST)
    @ResponseBody
    public R round1(@RequestParam String rawText,
                    @RequestParam(required = false) String distributorId) {
        try {
            // 参数校验

            
            long startTime = System.currentTimeMillis();
            

            // 1. 调用 ADP 智能体，提取 searchStr
            AdpParseResponse parseResp = parseSearchStrFromAgent(rawText, distributorId);
            
            // 兜底：若 ADP 没抽到，使用兜底规范化逻辑
            String primarySearchStr = isBlank(parseResp.getSearchStr()) 
                ? fallbackNormalize(rawText)
                : parseResp.getSearchStr();
            
            log.info("ADP解析结果 searchStr={}, candidates={}", primarySearchStr, parseResp.getSearchStrCandidates());
            
            // 2. 用主词查库（只查 disArr）
            List<Map<String, Object>> disArr = queryDisArr(primarySearchStr, distributorId);
            log.info("主词查询 '{}' 结果数: {}", primarySearchStr, disArr != null ? disArr.size() : 0);
            
            // 3. 候选词查询并合并去重
            List<String> candidates = safeList(parseResp.getSearchStrCandidates());
            for (String candidateStr : candidates) {
                if (!equalsIgnoreCase(candidateStr, primarySearchStr)) {
                    List<Map<String, Object>> candidateResults = queryDisArr(candidateStr, distributorId);
                    log.info("候选词查询 '{}' 结果数: {}", candidateStr, candidateResults != null ? candidateResults.size() : 0);
                    disArr = mergeDedup(disArr, candidateResults);
                }
            }
            
            // 4. 空结果处理
            if (disArr == null || disArr.isEmpty()) {
                return R.error("没查到相关商品，请换个说法再试")
                    .put("ok", false)
                    .put("code", "NO_CANDIDATE");
            }
            
            // 5. 返回成功结果
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("=== Round1 完成 === searchStr={}, disArr.size={}, elapsed={}ms", 
                primarySearchStr, disArr.size(), elapsed);
            
            return R.ok()
                .put("ok", true)
                .put("searchStr", primarySearchStr)
                .put("disArr", disArr);
                
        } catch (Exception e) {
            log.error("Round1异常 distributor_id={}, raw_text={}", distributorId, rawText, e);
            return R.error("服务繁忙，请稍后再试")
                .put("ok", false)
                .put("code", "ADP_ERROR");
        }
    }
    
    /**
     * 调用 ADP 智能体，提取 searchStr
     */
    private AdpParseResponse parseSearchStrFromAgent(String rawText, String distributorId) {
        try {
            // 生成会话ID
            String sessionId = "linshi_" + System.currentTimeMillis();
            
            // 构建自定义参数
            Map<String, String> customVars = new HashMap<>();
            customVars.put("distributor_id", distributorId);
            
            // 调用智能体
            String agentReply = tencentCloudAgentService.getAgentReply(rawText, sessionId, customVars);
            
            log.debug("ADP原始返回: {}", agentReply);
            
            // 解析智能体返回
            AdpParseResponse response = new AdpParseResponse();
            
            if (agentReply != null && !agentReply.trim().isEmpty()) {
                try {
                    // 尝试解析为 JSON
                    JSONObject json = JSON.parseObject(agentReply.trim());
                    
                    // 提取 searchStr
                    response.setSearchStr(json.getString("searchStr"));
                    
                    // 提取 searchStrCandidates（可能是数组）
                    JSONArray candidatesArr = json.getJSONArray("searchStrCandidates");
                    List<String> candidates = new ArrayList<>();
                    if (candidatesArr != null) {
                        for (int i = 0; i < candidatesArr.size(); i++) {
                            candidates.add(candidatesArr.getString(i));
                        }
                    }
                    response.setSearchStrCandidates(candidates);
                    
                } catch (Exception e) {
                    log.warn("智能体返回不是标准JSON，尝试提取关键词: {}", agentReply);
                    // 如果不是JSON，可能是纯文本关键词
                    response.setSearchStr(agentReply.trim());
                    response.setSearchStrCandidates(new ArrayList<>());
                }
            } else {
                response.setSearchStr("");
                response.setSearchStrCandidates(new ArrayList<>());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("调用ADP智能体失败", e);
            AdpParseResponse response = new AdpParseResponse();
            response.setSearchStr("");
            response.setSearchStrCandidates(new ArrayList<>());
            return response;
        }
    }
    
    /**
     * 查询商品（调用现有接口）
     */
    private List<Map<String, Object>> queryDisArr(String searchStr, String distributorId) {
        try {
            log.debug("查询商品 searchStr={}, distributorId={}", searchStr, distributorId);
            
            // 构建表单参数
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("searchStr", searchStr);
            formData.add("disId", distributorId);
            
            // 设置表单请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // 发送请求
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(quickSearchUrl, requestEntity, String.class);
            
            log.debug("查询接口响应: {}", response.getBody());
            
            // 解析响应
            JSONObject jsonResp = JSON.parseObject(response.getBody());
            JSONObject data = jsonResp.getJSONObject("data");
            
            if (data != null) {
                JSONArray disArr = data.getJSONArray("disArr");
                if (disArr != null && !disArr.isEmpty()) {
                    List<Map<String, Object>> result = new ArrayList<>();
                    for (int i = 0; i < disArr.size(); i++) {
                        JSONObject item = disArr.getJSONObject(i);
                        Map<String, Object> map = new HashMap<>();
                        for (String key : item.keySet()) {
                            map.put(key, item.get(key));
                        }
                        result.add(map);
                    }
                    return result;
                }
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("查询商品失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 兜底：规范化订单文本，提取关键词
     */
    private String fallbackNormalize(String rawText) {
        if (rawText == null) return "";
        
        // 去除数字+单位
        String normalized = rawText.replaceAll("[0-9]+(\\.[0-9]+)?\\s*(斤|千克|公斤|kg|g|克|箱|袋|包|盒|个|瓶|罐|斤装|kg装)", "");
        
        // 去除空格
        normalized = normalized.replaceAll("\\s+", "");
        
        // 去除修饰词
        String[] dropWords = {"进口", "国产", "本地", "新鲜", "大", "小", "特级", "精选", "A级", "B级", "原箱", "散装"};
        for (String word : dropWords) {
            normalized = normalized.replace(word, "");
        }
        
        // 只保留中文字符
        normalized = normalized.replaceAll("[^\\u4e00-\\u9fa5]", "");
        
        log.info("兜底规范化: {} -> {}", rawText, normalized);
        
        return normalized;
    }
    
    /**
     * 合并去重
     */
    private List<Map<String, Object>> mergeDedup(List<Map<String, Object>> base, List<Map<String, Object>> more) {
        Map<String, Map<String, Object>> merged = new LinkedHashMap<>();
        
        // 添加基础列表
        for (Map<String, Object> item : safeList(base)) {
            String id = String.valueOf(getItemId(item));
            merged.put(id, item);
        }
        
        // 添加新列表（去重）
        for (Map<String, Object> item : safeList(more)) {
            String id = String.valueOf(getItemId(item));
            merged.putIfAbsent(id, item);
        }
        
        return new ArrayList<>(merged.values());
    }
    
    /**
     * 获取商品ID（优先级：nxDgGoodsId > gbDisGoodsId > nxGoodsId > id）
     */
    private Object getItemId(Map<String, Object> item) {
        if (item == null) return null;
        
        if (item.get("nxDgGoodsId") != null) return item.get("nxDgGoodsId");
        if (item.get("gbDisGoodsId") != null) return item.get("gbDisGoodsId");
        if (item.get("nxGoodsId") != null) return item.get("nxGoodsId");
        if (item.get("id") != null) return item.get("id");
        
        return item.hashCode();
    }
    
    /**
     * 去重并保持顺序（primary 优先）
     */
    private List<String> distinctKeepOrder(String primary, List<String> candidates) {
        Set<String> seen = new LinkedHashSet<>();
        List<String> result = new ArrayList<>();
        
        // 先添加主关键词
        if (!isBlank(primary)) {
            seen.add(primary.toLowerCase());
            result.add(primary);
        }
        
        // 再添加候选关键词（去重）
        for (String candidate : safeList(candidates)) {
            if (!isBlank(candidate) && seen.add(candidate.toLowerCase())) {
                result.add(candidate);
            }
        }
        
        return result;
    }
    
    /**
     * 生成会话ID
     */
    private String genConvId(String orderId, String distributorId) {
        if (!isBlank(orderId)) {
            return "conv_" + orderId + "_" + distributorId;
        }
        return "conv_" + distributorId + "_" + System.currentTimeMillis();
    }
    
    /**
     * 安全获取列表
     */
    private <T> List<T> safeList(List<T> list) {
        return list != null ? list : new ArrayList<>();
    }
    
    /**
     * 判断字符串是否为空
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 忽略大小写比较
     */
    private boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null || str2 == null) return false;
        return str1.equalsIgnoreCase(str2);
    }
}


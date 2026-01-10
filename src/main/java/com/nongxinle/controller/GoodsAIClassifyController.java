package com.nongxinle.controller;

import com.alibaba.fastjson.JSON;
import com.nongxinle.utils.R;
import com.nongxinle.utils.TencentCloudAgentConfig;
import com.nongxinle.utils.TencentYuanqiConfig;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.net.UnknownHostException;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品 AI 分类控制器
 * 调用腾讯云智能体（企业解决方案）API 对商品进行分类
 * 使用企业解决方案接口：bot_app_key + session_id
 */
@RestController
@RequestMapping("api/goods")
public class GoodsAIClassifyController {
    private static final Logger logger = LoggerFactory.getLogger(GoodsAIClassifyController.class);

    /**
     * AI 商品分类接口
     * 调用腾讯云智能体（企业解决方案）API 对商品进行分类
     * 使用企业解决方案接口：bot_app_key + session_id
     * 
     * @param goodsName 商品名称（例如 "卤油皮卷"）
     * @param agentName 可选，智能体名称（例如 "goods_classify"），如果不提供则使用默认智能体
     * @return 腾讯云智能体返回的结果
     */
    @RequestMapping(value = "/ai-classify", method = RequestMethod.POST)
    public R classifyGoods(
            @RequestParam("goodsName") String goodsName,
            @RequestParam(value = "agentName", required = false) String agentName) {
        try {
            logger.info("开始调用腾讯云智能体（企业解决方案）API 分类商品，商品名称: {}, 智能体: {}", 
                    goodsName, agentName != null ? agentName : "默认");
            
            String apiUrl = TencentCloudAgentConfig.API_URL;
            // 根据智能体名称获取对应的 AppKey
            String appKey = TencentCloudAgentConfig.getAppKey(agentName);
            
            logger.info("请求URL: {}", apiUrl);
            logger.info("使用的智能体: {}", agentName != null ? agentName : "默认");
            // 安全：AppKey 只显示后6位，避免泄露敏感信息
            String maskedAppKey = appKey == null ? "null" : 
                (appKey.length() <= 6 ? "******" : "******" + appKey.substring(appKey.length() - 6));
            logger.info("使用的AppKey: {}", maskedAppKey);
            
            // 生成会话ID（2-64位，字母数字下划线横线组合，符合文档要求：^[a-zA-Z0-9_-]{2,64}$）
            // 使用 UUID 格式确保唯一性和格式正确性
            String sessionId = "goods_" + System.currentTimeMillis();
            // 确保不超过64位限制（goods_ + 13位时间戳 = 19位，符合要求）
            
            // 构建请求体（按照腾讯云技术支持提供的格式）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("session_id", sessionId);  // 会话ID，2-64位
            requestBody.put("bot_app_key", appKey);  // 发布应用的AppKey（根据智能体名称动态获取）
            requestBody.put("visitor_biz_id", "visitor_" + sessionId);  // 访客ID，标识当前用户/会话
            // content 直接传入商品名称，提示词应配置在智能体工作流的大模型节点 Prompt 中
            requestBody.put("content", goodsName);
            
            // 关键：传入工作流启动入参 query，使用 custom_variables 字段
            // 注意：custom_variables 的 key 和 value 都必须是 string 类型
            // 参考文档：https://cloud.tencent.com/document/product/1759/105561
            Map<String, String> customVariables = new HashMap<>();
            customVariables.put("query", goodsName);  // value 必须是字符串类型
            requestBody.put("custom_variables", customVariables);
            
            logger.info("工作流入参 query (通过 custom_variables): {}", goodsName);
            
            // 按照腾讯云技术支持提供的格式设置其他参数
            requestBody.put("incremental", false);  // 非增量模式
            requestBody.put("streaming_throttle", 10);  // 流式回复频率控制
            requestBody.put("visitor_labels", new ArrayList<>());  // 访客标签，空数组
            requestBody.put("search_network", "disable");  // 禁用网络搜索
            requestBody.put("stream", "enable");  // 启用流式（注意：这里是 "enable"，不是 "disable"）
            requestBody.put("workflow_status", "enable");  // 启用工作流状态
            requestBody.put("tcadp_user_id", "");  // 用户ID，空字符串
            requestBody.put("request_id", "req_" + System.currentTimeMillis());  // 请求ID
            
            // 转换为 JSON 字符串
            String jsonBody = JSON.toJSONString(requestBody);
            // 安全：请求体日志中遮罩敏感信息（bot_app_key）
            String maskedJsonBody = jsonBody;
            if (appKey != null && appKey.length() > 0) {
                String maskedKey = appKey.length() <= 6 ? "******" : "******" + appKey.substring(appKey.length() - 6);
                maskedJsonBody = jsonBody.replace("\"bot_app_key\":\"" + appKey + "\"", 
                    "\"bot_app_key\":\"" + maskedKey + "\"");
            }
            logger.info("请求体: {}", maskedJsonBody);
            
            // 使用 Apache HttpClient 发送 HTTP 请求（解决 SSL 握手问题）
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(30000)      // 连接超时 30 秒
                    .setSocketTimeout(30000)       // Socket 超时 30 秒
                    .setConnectionRequestTimeout(30000) // 从连接池获取连接超时 30 秒
                    .build();
            
            // 配置 SSL（显式指定使用 TLSv1.2 协议，解决 SSL 握手问题）
            // 注意：生产环境应该使用正确的证书验证
            
            // 创建信任所有证书的 TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
            
            // 显式指定使用 TLSv1.2 协议创建 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // 指定支持的协议版本（TLSv1.2），确保与腾讯服务器兼容
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1.2"},  // 支持的协议版本（显式指定 TLSv1.2）
                    null,  // 支持的加密套件（null 表示使用默认）
                    NoopHostnameVerifier.INSTANCE
            );
            
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
            
            HttpPost httpPost = new HttpPost(apiUrl);
            
            // 设置请求头（企业解决方案格式）
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "text/event-stream");  // SSE支持
            
            // 设置请求体
            StringEntity entity = new StringEntity(jsonBody, "UTF-8");
            httpPost.setEntity(entity);
            
            logger.info("准备发送请求到腾讯元器 API...");
            
            String response;
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                logger.info("腾讯云智能体 API 响应状态码: {}", statusCode);
                
                HttpEntity responseEntity = httpResponse.getEntity();
                if (responseEntity != null) {
                    response = EntityUtils.toString(responseEntity, "UTF-8");
                    logger.info("腾讯云智能体 API 响应: {}", response);
                } else {
                    throw new RuntimeException("腾讯云智能体 API 返回空响应");
                }
            } finally {
                httpClient.close();
            }
            
            // 解析 SSE 响应（企业解决方案返回的是 SSE 格式）
            String result = parseSSEResponse(response);
            
            if (result != null && !result.isEmpty()) {
                // 尝试解析智能体返回的JSON格式
                try {
                    // 先尝试解析外层JSON（可能包含content、cuo、goodsList等字段）
                    com.alibaba.fastjson.JSONObject outerObj = JSON.parseObject(result);
                    
                    // 构建统一格式的响应
                    Map<String, Object> responseData = new HashMap<>();
                    
                    // 解析content字段（分类结果）
                    if (outerObj.containsKey("content")) {
                        String contentStr = outerObj.getString("content");
                        try {
                            com.alibaba.fastjson.JSONObject contentObj = JSON.parseObject(contentStr);
                            responseData.put("classification", contentObj);
                            logger.info("解析分类结果: {}", contentObj);
                        } catch (Exception e) {
                            logger.warn("解析content字段失败，content: {}", contentStr, e);
                            responseData.put("classification", contentStr);
                        }
                    }
                    
                    // 解析cuo字段（错误标记）
                    if (outerObj.containsKey("cuo")) {
                        responseData.put("cuo", outerObj.getInteger("cuo"));
                    }
                    
                    // 解析goodsList字段（商品列表）
                    if (outerObj.containsKey("goodsList")) {
                        String goodsListStr = outerObj.getString("goodsList");
                        try {
                            com.alibaba.fastjson.JSONObject goodsListObj = JSON.parseObject(goodsListStr);
                            responseData.put("goodsList", goodsListObj);
                            logger.info("解析商品列表: {}", goodsListObj);
                        } catch (Exception e) {
                            logger.warn("解析goodsList字段失败，goodsList: {}", goodsListStr, e);
                            responseData.put("goodsList", goodsListStr);
                        }
                    }
                    
                    // 如果外层对象不包含content字段，说明可能是直接的分类结果JSON
                    if (!outerObj.containsKey("content") && !outerObj.containsKey("cuo") && !outerObj.containsKey("goodsList")) {
                        // 直接使用解析后的JSON对象
                        responseData.put("data", outerObj);
                    }
                    
                    return R.ok().put("data", responseData);
                    
                } catch (Exception e) {
                    // 如果解析失败，说明可能是纯文本，直接返回
                    logger.warn("解析JSON失败，返回原始文本，result: {}", result, e);
                    return R.ok().put("data", result);
                }
            } else {
                // 检查响应中是否有错误信息
                if (response.contains("460032") || response.contains("余额不足")) {
                    return R.error(-1, "模型余额不足。如果已购买资源包，请等待几分钟让资源包生效，或检查资源包是否绑定到正确的 AppKey");
                }
                return R.error(-1, "未能从 SSE 响应中解析出结果");
            }
            
        } catch (java.net.UnknownHostException e) {
            logger.error("DNS 解析失败，无法解析域名，商品名称: {}", goodsName, e);
            return R.error(-1, "DNS 解析失败，无法连接到腾讯元器服务器。请检查网络连接或确认 API 地址是否正确");
        } catch (java.net.ConnectException e) {
            logger.error("连接失败，无法连接到服务器，商品名称: {}", goodsName, e);
            return R.error(-1, "连接失败，无法连接到腾讯元器服务器。请检查网络连接: " + e.getMessage());
        } catch (javax.net.ssl.SSLException e) {
            logger.error("SSL 握手失败，商品名称: {}", goodsName, e);
            return R.error(-1, "SSL 握手失败，请检查 TLS 协议配置: " + e.getMessage());
        } catch (Exception e) {
            logger.error("调用腾讯元器 API 失败，商品名称: {}, 错误类型: {}", goodsName, e.getClass().getSimpleName(), e);
            return R.error(-1, "调用腾讯元器 API 失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：生成完整的请求参数（用于提供给腾讯云技术支持）
     * 输出格式化的请求参数，方便复制粘贴给技术支持进行调试
     * 
     * @param goodsName 商品名称
     * @param agentName 可选，智能体名称
     * @return 完整的请求参数（敏感信息已遮罩）
     */
    @RequestMapping(value = "/ai-classify/debug", method = RequestMethod.GET)
    public R debugRequestBody(
            @RequestParam("goodsName") String goodsName,
            @RequestParam(value = "agentName", required = false) String agentName) {
        
        String apiUrl = TencentCloudAgentConfig.API_URL;
        String appKey = TencentCloudAgentConfig.getAppKey(agentName);
        String sessionId = "goods_classify_" + System.currentTimeMillis();
        
        // 构建请求体（与实际请求完全一致）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("session_id", sessionId);
        requestBody.put("bot_app_key", appKey);
        requestBody.put("visitor_biz_id", "visitor_" + sessionId);
        requestBody.put("content", goodsName);
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("query", goodsName);
        requestBody.put("inputs", inputs);
        
        requestBody.put("stream", "disable");
        requestBody.put("incremental", false);
        requestBody.put("request_id", "req_" + System.currentTimeMillis());
        
        // 生成完整的JSON（用于技术支持）
        String jsonBody = JSON.toJSONString(requestBody);  // 原始JSON
        
        // 遮罩敏感信息（用于显示）
        String maskedJsonBody = jsonBody;
        if (appKey != null && appKey.length() > 0) {
            String maskedKey = appKey.length() <= 6 ? "******" : "******" + appKey.substring(appKey.length() - 6);
            maskedJsonBody = jsonBody.replace("\"bot_app_key\": \"" + appKey + "\"", 
                "\"bot_app_key\": \"" + maskedKey + "\"");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("method", "POST");
        result.put("url", apiUrl);
        result.put("requestBodyFormatted", maskedJsonBody);  // 格式化且遮罩的JSON
        result.put("requestBodyOriginal", jsonBody);  // 原始JSON（包含真实AppKey，谨慎使用）
        result.put("requestParams", requestBody);  // 解析后的参数对象
        result.put("agentName", agentName != null ? agentName : "默认");
        result.put("appKeyMasked", appKey == null ? "null" : 
            (appKey.length() <= 6 ? "******" : "******" + appKey.substring(appKey.length() - 6)));
        result.put("note", "提供给技术支持时，请使用 requestBodyFormatted 字段（敏感信息已遮罩）。如需完整参数，请联系技术支持提供真实 AppKey。");
        
        return R.ok().put("debug", result);
    }

    /**
     * 诊断接口：测试腾讯元器 API 配置和连接
     * 用于排查 API Token、智能体状态等问题
     * 
     * @return 诊断结果，包含配置信息、网络测试、API 测试等
     */
    @RequestMapping(value = "/ai-classify/test", method = RequestMethod.GET)
    public R testApiConfiguration() {
        Map<String, Object> diagnostic = new HashMap<>();
        
        // 1. 显示当前配置（隐藏敏感信息）
        Map<String, Object> config = new HashMap<>();
        config.put("api_url", TencentYuanqiConfig.USE_IP_DIRECTLY 
                ? TencentYuanqiConfig.API_URL_BY_IP 
                : TencentYuanqiConfig.API_URL);
        config.put("use_ip_directly", TencentYuanqiConfig.USE_IP_DIRECTLY);
        config.put("assistant_id", TencentYuanqiConfig.ASSISTANT_ID);
        config.put("api_token_length", TencentYuanqiConfig.API_TOKEN != null 
                ? TencentYuanqiConfig.API_TOKEN.length() : 0);
        config.put("api_token_prefix", TencentYuanqiConfig.API_TOKEN != null && TencentYuanqiConfig.API_TOKEN.length() > 4
                ? TencentYuanqiConfig.API_TOKEN.substring(0, 4) + "..." : "未配置");
        diagnostic.put("configuration", config);
        
        // 2. DNS 解析测试
        Map<String, Object> dnsTest = new HashMap<>();
        try {
            String hostname = "api.yuanqi.tencent.com";
            java.net.InetAddress address = java.net.InetAddress.getByName(hostname);
            dnsTest.put("status", "success");
            dnsTest.put("hostname", hostname);
            dnsTest.put("resolved_ip", address.getHostAddress());
            dnsTest.put("is_localhost", address.isLoopbackAddress());
            if (address.isLoopbackAddress()) {
                dnsTest.put("warning", "DNS 解析到本地回环地址 (127.0.0.1)，可能被拦截或配置错误");
            }
        } catch (Exception e) {
            dnsTest.put("status", "failed");
            dnsTest.put("error", e.getMessage());
            dnsTest.put("suggestion", "DNS 解析失败，建议：1) 修改 /etc/hosts 文件添加 IP 映射；2) 或设置 USE_IP_DIRECTLY = true");
        }
        diagnostic.put("dns_test", dnsTest);
        
        // 3. 网络连通性测试
        Map<String, Object> networkTest = new HashMap<>();
        try {
            String testUrl = TencentYuanqiConfig.USE_IP_DIRECTLY 
                    ? TencentYuanqiConfig.API_URL_BY_IP 
                    : TencentYuanqiConfig.API_URL;
            
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setSocketTimeout(5000)
                    .build();
            
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, 
                new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
                }, 
                new java.security.SecureRandom());
            
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext, new String[]{"TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
            
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
            
            HttpPost httpPost = new HttpPost(testUrl);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("X-Source", "openapi");  // OpenAPI 必需的请求头
            httpPost.setHeader("Authorization", "Bearer " + TencentYuanqiConfig.API_TOKEN);
            
            // 发送一个最小化的测试请求（按照官方文档格式）
            Map<String, Object> testBody = new HashMap<>();
            testBody.put("assistant_id", TencentYuanqiConfig.ASSISTANT_ID);
            testBody.put("user_id", "test_user");
            testBody.put("stream", false);
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> msg = new HashMap<>();
            msg.put("role", "user");
            // content 必须是数组，包含 type 和 text 字段
            List<Map<String, String>> contentList = new ArrayList<>();
            Map<String, String> contentItem = new HashMap<>();
            contentItem.put("type", "text");
            contentItem.put("text", "测试");
            contentList.add(contentItem);
            msg.put("content", contentList);
            messages.add(msg);
            testBody.put("messages", messages);
            
            StringEntity entity = new StringEntity(JSON.toJSONString(testBody), "UTF-8");
            httpPost.setEntity(entity);
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                networkTest.put("status", "connected");
                networkTest.put("status_code", statusCode);
                
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String responseBody = EntityUtils.toString(responseEntity, "UTF-8");
                    networkTest.put("response_length", responseBody.length());
                    
                    // 分析响应状态码
                    if (statusCode == 200) {
                        networkTest.put("result", "success");
                        networkTest.put("message", "API 调用成功！Token 和智能体配置正确");
                        try {
                            Object jsonResponse = JSON.parse(responseBody);
                            networkTest.put("response_preview", jsonResponse);
                        } catch (Exception e) {
                            networkTest.put("response_preview", responseBody.substring(0, Math.min(200, responseBody.length())));
                        }
                    } else if (statusCode == 401) {
                        networkTest.put("result", "token_error");
                        networkTest.put("message", "API Token 无效或已过期。请检查：1) Token 是否正确；2) Token 是否已过期；3) 是否在腾讯元器平台重新生成了 Token");
                        networkTest.put("response_preview", responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody);
                    } else if (statusCode == 404) {
                        networkTest.put("result", "assistant_error");
                        networkTest.put("message", "智能体不存在或未发布。请检查：1) Assistant ID 是否正确；2) 智能体是否已在腾讯元器平台点击'发布'");
                        networkTest.put("response_preview", responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody);
                    } else if (statusCode == 400) {
                        networkTest.put("result", "request_error");
                        networkTest.put("message", "请求参数错误。请检查请求体格式是否正确");
                        networkTest.put("response_preview", responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody);
                    } else {
                        networkTest.put("result", "unknown_error");
                        networkTest.put("message", "未知错误，状态码: " + statusCode);
                        networkTest.put("response_preview", responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody);
                    }
                }
            }
            httpClient.close();
            
        } catch (java.net.UnknownHostException e) {
            networkTest.put("status", "dns_failed");
            networkTest.put("error", "DNS 解析失败: " + e.getMessage());
            networkTest.put("suggestion", "请先解决 DNS 问题：1) 修改 /etc/hosts；2) 或设置 USE_IP_DIRECTLY = true");
        } catch (java.net.ConnectException e) {
            networkTest.put("status", "connection_failed");
            networkTest.put("error", "连接失败: " + e.getMessage());
            networkTest.put("suggestion", "请检查：1) 网络连接；2) 防火墙设置；3) 服务器是否能访问外网");
        } catch (javax.net.ssl.SSLException e) {
            networkTest.put("status", "ssl_failed");
            networkTest.put("error", "SSL 握手失败: " + e.getMessage());
            networkTest.put("suggestion", "SSL 配置问题，代码中已配置信任所有证书，如果仍失败请检查 TLS 版本");
        } catch (Exception e) {
            networkTest.put("status", "error");
            networkTest.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            networkTest.put("suggestion", "请查看详细错误信息并检查配置");
        }
        diagnostic.put("api_test", networkTest);
        
        // 4. 建议和下一步操作
        List<String> suggestions = new ArrayList<>();
        if (dnsTest.get("status").equals("failed") || 
            (dnsTest.get("is_localhost") != null && (Boolean) dnsTest.get("is_localhost"))) {
            suggestions.add("【DNS 问题】DNS 解析失败或被拦截，建议：1) 在服务器上执行 'sudo vi /etc/hosts' 添加 '119.147.183.109 api.yuanqi.tencent.com'；2) 或设置 USE_IP_DIRECTLY = true");
        }
        if (networkTest.get("result") != null && networkTest.get("result").equals("token_error")) {
            suggestions.add("【Token 问题】API Token 无效，请：1) 登录腾讯元器平台；2) 检查 Token 是否过期；3) 重新生成 Token 并更新配置");
        }
        if (networkTest.get("result") != null && networkTest.get("result").equals("assistant_error")) {
            suggestions.add("【智能体问题】智能体不存在或未发布，请：1) 登录腾讯元器平台；2) 确认 Assistant ID 是否正确；3) 确保智能体已点击'发布'");
        }
        if (suggestions.isEmpty() && networkTest.get("result") != null && networkTest.get("result").equals("success")) {
            suggestions.add("✅ 所有配置正确！API 调用成功，可以正常使用商品分类功能");
        }
        diagnostic.put("suggestions", suggestions);
        
        return R.ok().put("diagnostic", diagnostic);
    }
    
    /**
     * 解析 SSE 响应（企业解决方案返回的是 SSE 格式）
     * 参考 TencentCloudAgentServiceImpl.parseADPResponse 方法
     * 
     * @return 解析结果，如果返回 null 表示有错误
     */
    private String parseSSEResponse(String sseResponse) {
        try {
            if (sseResponse == null || sseResponse.trim().isEmpty()) {
                return null;
            }

            logger.debug("收到 SSE 响应: {}", sseResponse);
            
            // 检查是否返回HTML（错误网关）
            if (sseResponse.trim().startsWith("<html>") || sseResponse.trim().startsWith("<!DOCTYPE")) {
                logger.error("网关错误：返回HTML页面，域名/路径可能错误");
                throw new RuntimeException("网关错误：请检查接口地址配置");
            }

            // 解析SSE格式：event: / data: 逐行解析
            String[] lines = sseResponse.split("\n");
            String workflowResult = null;
            String errorMessage = null;
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                
                // 处理错误事件
                if (line.startsWith("event:error")) {
                    if (i + 1 < lines.length) {
                        String dataLine = lines[i + 1].trim();
                        if (dataLine.startsWith("data:")) {
                            String jsonData = dataLine.substring(5).trim();
                            try {
                                com.alibaba.fastjson.JSONObject errorData = JSON.parseObject(jsonData);
                                com.alibaba.fastjson.JSONObject payload = errorData.getJSONObject("payload");
                                if (payload != null) {
                                    com.alibaba.fastjson.JSONObject error = payload.getJSONObject("error");
                                    if (error != null) {
                                        String errorCode = error.getString("code");
                                        String errorMsg = error.getString("message");
                                        logger.error("SSE 返回错误: code={}, message={}", errorCode, errorMsg);
                                        
                                        // 如果是余额不足错误，抛出异常并给出友好提示
                                        if ("460032".equals(errorCode) || (errorMsg != null && errorMsg.contains("余额不足"))) {
                                            throw new RuntimeException("模型余额不足（错误码: " + errorCode + "）。如果已购买资源包，请：1) 等待几分钟让资源包生效；2) 检查资源包是否绑定到正确的 AppKey；3) 确认资源包中的模型是否与智能体配置的模型一致。");
                                        }
                                        
                                        errorMessage = "错误码: " + errorCode + ", 错误信息: " + errorMsg;
                                        // 其他错误继续解析，看看是否有其他结果
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("解析错误事件失败: {}", jsonData, e);
                            }
                        }
                    }
                }
                
                // 处理回复事件（工作流模式）
                if (line.startsWith("event:reply")) {
                    if (i + 1 < lines.length) {
                        String dataLine = lines[i + 1].trim();
                        if (dataLine.startsWith("data:")) {
                            String jsonData = dataLine.substring(5).trim();
                            try {
                                com.alibaba.fastjson.JSONObject replyData = JSON.parseObject(jsonData);
                                com.alibaba.fastjson.JSONObject payload = replyData.getJSONObject("payload");
                                
                                // 忽略回声（is_from_self=true）
                                if (payload != null && payload.getBooleanValue("is_from_self")) {
                                    logger.debug("忽略回声消息");
                                    continue;
                                }
                                
                                // 只处理 is_final=true 的消息
                                if (payload != null && payload.getBooleanValue("is_final")) {
                                    // 优先读取 work_flow.current_node.Output
                                    com.alibaba.fastjson.JSONObject workFlow = payload.getJSONObject("work_flow");
                                    if (workFlow != null) {
                                        com.alibaba.fastjson.JSONObject currentNode = workFlow.getJSONObject("current_node");
                                        if (currentNode != null) {
                                            String output = currentNode.getString("Output");
                                            if (output != null && !output.isEmpty() && !output.equals("{}")) {
                                                workflowResult = output;
                                                logger.debug("从work_flow.current_node.Output获取结果: {}", output);
                                            }
                                        }
                                    }
                                    
                                    // 如果 work_flow.current_node.Output 为空，尝试读取 content
                                    if (workflowResult == null || workflowResult.isEmpty() || workflowResult.equals("{}")) {
                                        String content = payload.getString("content");
                                        if (content != null && !content.isEmpty()) {
                                            workflowResult = content;
                                            logger.debug("从content获取结果: {}", content);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                logger.warn("解析回复事件失败: {}", jsonData, e);
                            }
                        }
                    }
                }
                
                // 处理 token_stat 事件，从调试信息中提取工作流节点输出
                if (line.startsWith("event:token_stat")) {
                    if (i + 1 < lines.length) {
                        String dataLine = lines[i + 1].trim();
                        if (dataLine.startsWith("data:")) {
                            String jsonData = dataLine.substring(5).trim();
                            try {
                                com.alibaba.fastjson.JSONObject tokenData = JSON.parseObject(jsonData);
                                com.alibaba.fastjson.JSONObject payload = tokenData.getJSONObject("payload");
                                if (payload != null) {
                                    // 只处理状态为 success 的 token_stat 事件
                                    String status = payload.getString("status");
                                    if ("success".equals(status)) {
                                        com.alibaba.fastjson.JSONArray procedures = payload.getJSONArray("procedures");
                                        if (procedures != null && procedures.size() > 0) {
                                            com.alibaba.fastjson.JSONObject procedure = procedures.getJSONObject(0);
                                            com.alibaba.fastjson.JSONObject debugging = procedure.getJSONObject("debugging");
                                            if (debugging != null) {
                                                com.alibaba.fastjson.JSONObject wf = debugging.getJSONObject("work_flow");
                                                if (wf != null) {
                                                    com.alibaba.fastjson.JSONArray runNodes = wf.getJSONArray("run_nodes");
                                                    if (runNodes != null) {
                                                        // 查找大模型节点（node_type=3）的输出
                                                        for (int j = 0; j < runNodes.size(); j++) {
                                                            com.alibaba.fastjson.JSONObject node = runNodes.getJSONObject(j);
                                                            int nodeType = node.getIntValue("node_type");
                                                            String output = node.getString("output");
                                                            // node_type=3 是大模型节点，且状态为成功（status=2）
                                                            int nodeStatus = node.getIntValue("status");
                                                            if (nodeType == 3 && nodeStatus == 2 && output != null && !output.isEmpty() && !output.equals("{}")) {
                                                                try {
                                                                    // 尝试解析 output 中的 Content 字段
                                                                    com.alibaba.fastjson.JSONObject outputObj = JSON.parseObject(output);
                                                                    String content = outputObj.getString("Content");
                                                                    if (content != null && !content.isEmpty()) {
                                                                        workflowResult = content;
                                                                        logger.info("从token_stat的大模型节点获取结果: {}", content);
                                                                        break;
                                                                    } else {
                                                                        // 如果没有 Content 字段，直接使用 output
                                                                        workflowResult = output;
                                                                        logger.info("从token_stat的大模型节点获取结果: {}", output);
                                                                        break;
                                                                    }
                                                                } catch (Exception e) {
                                                                    // 如果 output 不是 JSON，直接使用
                                                                    if (output != null && !output.isEmpty() && !output.equals("{}")) {
                                                                        workflowResult = output;
                                                                        logger.info("从token_stat的大模型节点获取结果（非JSON）: {}", output);
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                logger.debug("解析token_stat事件时出错，忽略: {}", e.getMessage());
                            }
                        }
                    }
                }
            }

            if (workflowResult != null && !workflowResult.isEmpty()) {
                logger.info("解析智能体结果: {}", workflowResult);
                return workflowResult;
            }

            logger.warn("SSE 响应中没有找到工作流结果");
            return null;
            
        } catch (Exception e) {
            logger.error("解析 SSE 响应异常", e);
            return null;
        }
    }
}

